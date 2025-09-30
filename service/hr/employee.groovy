import org.moqui.Moqui
import org.moqui.entity.EntityCondition

import java.sql.Date
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

def create_employee() {

    if (!isAdmin) {
        if (!password || !passwordVerify || password.trim() == "" || passwordVerify.trim() == "") {
            description = ec.l10n.toPersianLocale("passwordError")
            state = 0
            return
        }
        if (!password.equals(passwordVerify)) {
            description = ec.l10n.toPersianLocale("passwordVerifyError")
            state = 0
            return
        }
    }
    if (birthDate) birthDate = birthDate.split("T")[0]
    if (empDate) empDate = empDate.split("T")[0]
    if (endDate) endDate = endDate.split("T")[0]
    person = ["name": name, "family": family, "nationalId": nationalId, mobile: mobile, profileFileId: profileFileId, gender: gender, empType: empType, dayFloat: dayFloat, monthFloat: monthFloat, empDate: empDate, endDate: endDate, birthDate: birthDate, karkard: karkard, device: device, locationCheck: locationCheck]
    add_employee(orgId, isAdmin, mobile, person, unitId, role, password)

}

def create_employee_v2() {

    step = ec.entity.find("Step").condition([username: ec.user.username]).one()
    if (!step) {
        description = ec.l10n.toPersianLocale("noStepFound")
        state = 0
        return
    }
    orgId = step?.orgId
    if (!orgId) {
        description = ec.l10n.toPersianLocale("noOrgFound")
        state = 0
        return
    }
    person = ["name": name, "family": family, "nationalId": nationalId, mobile: mobile, profileFileId: profileFileId, gender: gender, empType: empType, dayFloat: dayFloat, monthFloat: monthFloat, empDate: empDate, endDate: endDate, birthDate: birthDate, karkard: 100,device:device,locationCheck: locationCheck]
    add_employee(orgId, false, mobile, person, unitId, role, null)

}

def add_employee(orgId, isAdmin, username, person, unitId, role, password) {

    try {
        if (!this.hasProperty("ec")) this.ec = Moqui.getExecutionContextFactory()
        current_employee = ec.entity.find("Employee").condition([nationalId: person.nationalId]).one()
        if (!isAdmin && current_employee) {
            state = 0
            description = ec.l10n.toPersianLocale("duplicateEmployee")
            return
        }
        current_user = ec.entity.find("UserAccount").condition([username: username]).one()
        if (!isAdmin && current_user && current_user.employeeId != null) {
            state = 0
            description = ec.l10n.toPersianLocale("duplicateUsername")
            return
        }
        birthDate = null
        if (person.birthDate) {
            birthDate = LocalDate.parse(person.birthDate)
            if (birthDate.isAfter(LocalDate.now())) {
                description = ec.l10n.toPersianLocale("dateAfterError")
                state = 0
                return
            }
        }
        activePlan = ec.entity.find("Customer").condition("orgId", orgId).condition("isEnable", "Y").condition("endDate", EntityCondition.GREATER_THAN_EQUAL_TO, Date.valueOf(LocalDate.now())).one()
        if (!activePlan && !isAdmin) {
            description = ec.l10n.toPersianLocale("noPlanError")
            state = 0
            return
        }
        users = activePlan?.users ?: 0
        giftUsers = activePlan?.giftUsers ?: 0
        countUser = users + giftUsers
        currentUser = ec.entity.find("Employee").condition([orgId: orgId]).count()
        if (currentUser + 1 > countUser && !isAdmin) {
            description = ec.l10n.toPersianLocale('maxUserError')
            state = 0
            return
        }
        if (unitId && "modir".equals(role)) {
            modirRel = ec.entity.find("EmplUnit").condition([unitId: unitId, role: "modir"]).one()
            if (modirRel) {
                empl = ec.entity.find("Employee").condition([employeeId: modirRel?.employeeId, role: "modir"]).one()
                modir = ec.entity.find("UserAccount").condition([employeeId: empl.employeeId]).one()?.userFullName
                description = ec.l10n.toPersianLocale('modirPrefixError') + " " + modir + " " + ec.l10n.toPersianLocale('modirSuffixError')
                state = 0
                return
            }
        }

        serviceCall = ec.service.sync().name("security.user.create#user").parameters([password: person?.password, username: username, name: person?.name, family: person?.family, nationalId: person?.nationalId, isAdmin: isAdmin, isModir: "modir".equals(role)]).call()
        state = serviceCall?.state
        if (!"1".equals(state?.toString())) {
            description = serviceCall?.description
            state = 0
            throw new Exception(description)
        }
        emp = ec.entity.find("Employee").condition([nationalId: person.nationalId]).one()
        if (!emp) {
            serviceCall = ec.service.sync().name("create#Employee").parameters([device: person?.device,locationCheck: person?.locationCheck,"name": person.name, "family": person.family, "nationalId": person.nationalId, profileFileId: person.profileFileId, gender: person.gender, empType: person.empType, dayFloat: person.dayFloat, monthFloat: person.monthFloat, empDate: person.empDate, endDate: person.endDate, birthDate: person.birthDate, karkard: 100, orgId: orgId]).call()
            employeeId = serviceCall?.employeeId
        } else {
            if (!isAdmin && !emp.orgId.equals(orgId)) {
                description = ec.l10n.toPersianLocale('karmandOrgError')
                employeeId = null
                state = 0
                throw new Exception(description)
            }
            employeeId = emp?.employeeId
            serviceCall = ec.service.sync().name("update#Employee").parameters([device: person?.device,locationCheck: person?.locationCheck,"employeeId": employeeId, "name": person.name, "family": person.family, "nationalId": person.nationalId, profileFileId: person.profileFileId, gender: person.gender, empType: person.empType, dayFloat: person.dayFloat, monthFloat: person.monthFloat, empDate: person.empDate, endDate: person.endDate, birthDate: person.birthDate, karkard: 100, orgId: orgId]).call()
        }

        if (!isAdmin && "karmand".equals(role)) {
            karmand = ec.entity.find("EmplUnit").condition([employeeId: employeeId, role: "karmand"]).one()
            if (karmand) {
                description = ec.l10n.toPersianLocale('karmandError')
                employeeId = null
                state = 0
                throw new Exception(description)
            }
        }
        ec.entity.find("UserAccount").condition([username: username]).updateAll([employeeId: employeeId])
        call = create_enteringSystemEmployee(person.name, person.family, person.nationalId, person.empType, person.dayFloat, person.monthFloat, person.empDate, person.endDate, person.birthDate, person.karkard, orgId, unitId, employeeId)
        state = call?.state
        if (state != 1) {
            description = serviceCall.description ?: ec.l10n.toPersianLocale("generalError")
            state = 0
            throw new Exception(description)
        }
        emplUn = ec.entity.find("EmplUnit").condition([employeeId: employeeId, role: role, unitId: unitId]).one()
        if (!emplUn && !isAdmin) {
            ec.service.sync().name("create#EmplUnit").parameters([employeeId: employeeId, unitId: unitId, role: role]).call()
        }
        description = ec.l10n.toPersianLocale("successEmployee")
        state = 1

    }
    catch (Exception e) {
        e.printStackTrace()
        if (state == 0) description = e.getMessage()
        else description = ec.l10n.toPersianLocale("generalError")
        ec.transaction.rollback(e.getMessage(),e)
        state = 0
        employeeId = null
    }
}

def create_enteringSystemEmployee(name, family, nationalId, empType, dayFloat, monthFloat, empDate, endDate, birthDate, karkard, orgId, unitId, employeeId) {
    try {
        if (!this.hasProperty("ec")) this.ec = Moqui.getExecutionContextFactory()
        if (birthDate) birthDate = birthDate.replaceAll("-", "")
        if (!empDate) empDate = new java.util.Date()
        serviceCall = ec.service.sync().name("general.general.get#connection").call()
        state = serviceCall?.state?.toString()
        if (!state.equals("1")) {
            return [state: 0]
        }
        sql = serviceCall?.sql
        query = "SELECT * FROM EMPLOYEE WHERE PERS_NO=?"
        per = sql.rows(query, [nationalId])
        if (per.size() > 0) {
            per = per[0]
            empNo = per.get("EMP_NO")
            whSt = " WHERE EMP_NO=?"
            data = [nationalId, name, family, orgId, empDate]
            updSt = """UPDATE EMPLOYEE SET PERS_NO=?,NAME=?,FAMILY=?,SEC_NO=?,EMP_DATE=?"""
            if (unitId) {
                updSt = updSt + ",GRP_NO=? " + whSt
                data.add(unitId)
                data.add(empNo)
            } else {
                updSt = updSt + whSt
                data.add(empNo)
            }
            sql.executeInsert(updSt, data)
            return [state: 1]
        }
        if(!empType)empType=0
        data = [employeeId, nationalId, name, family, orgId, empDate, "2100-12-12", birthDate, empType, monthFloat, dayFloat, unitId, 1, 0, 0, 0, 0, 2400, 2400, 2400, 0, 0, 0, 0, 0, 0, 0, karkard]
        insSt = """INSERT INTO EMPLOYEE (EMP_NO,PERS_NO,NAME,FAMILY,SEC_NO,EMP_DATE,END_DATE,BIRTH_DATE,EMP_TYPE,MMonth_,MDay_,GRP_NO,servic_no,ESTEH_SAVE,JANBAZ_TIM,JANBAZ_E_T,PREV_MOJ,AFTER_MOJ,HOLID_MOJ,KASR_ESTH,KASR_bim,Kasr_BH,ALMOSANNA,Hs_KasrEsth,Mor_Kasr,KVPriorMonth,KVCurrentMonth,KARKERD3) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) """
        sql.executeInsert(insSt, data)
        sql.close()
        return [state: 1]
    }
    catch (Exception ignored) {
        ignored.printStackTrace()
        return [state: 0]
    }
}

def update_enteringSystemEmployee(nationalId, name, family, empDate, endDate, birthDate, empType, monthFloat, dayFloat, unitId) {
    try {
        if (birthDate) {
            birthDate = birthDate.replaceAll("-", "").substring(0, 8)
        }
        if (empDate) {
            if (!empDate.toString().contains("T")) empDate = empDate + "T00:00:00.000Z"
            empDate = LocalDateTime.parse(empDate, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"))
        }
        if(!empType)empType=0
        serviceCall = ec.service.sync().name("general.general.get#connection").call()
        if (serviceCall?.state != 1) {
            description = ec.l10n().toPersianLocale("sqlConnectionError")
            return [state: 1, description: description]
        }
        sql = serviceCall?.sql
        query = "SELECT * FROM EMPLOYEE WHERE PERS_NO=?"
        per = sql.rows(query, [nationalId])
        data = [nationalId, name, family, empDate, "2100-12-12", birthDate, empType, monthFloat, dayFloat]
        if (per.size() > 0) {
            per = per[0]
            empNo = per.get("EMP_NO")
            whSt = " WHERE EMP_NO=?"
            updSt = """UPDATE EMPLOYEE SET PERS_NO=?,NAME=?,FAMILY=?,EMP_DATE=?,END_DATE=?,BIRTH_DATE=?,
                      EMP_TYPE=?,MMonth_=?,MDay_=? """
            if (unitId) {
                updSt = updSt + ",GRP_NO=?" + whSt
                data.add(unitId)
                data.add(empNo)
            } else {
                updSt = updSt + whSt
                data.add(empNo)
            }
            sql.executeInsert(updSt, data)
            return [state: 1]
        }
    }
    catch (Exception ignored) {
        return [state: 0, description: ec.l10n.toPersianLocale("updateEmpSqlError")]
    }
    finally {
        if (sql) sql.close()
    }
}

def update_employee() {

    if (birthDate) birthDate = birthDate.replaceAll("/", "-")
    if (endDate) endDate = endDate.replaceAll("/", "-")
    if (empDate) empDate = empDate.replaceAll("/", "-")
    person = [nationalId: nationalId, device: device, locationCheck: locationCheck, name: name, family: family, gender: gender, dayFloat: dayFloat, monthFloat: monthFloat, empType: empType, profileFileId: profileFileId, empDate: empDate, endDate: endDate, birthDate: birthDate]
    edit_employee(employeeId, karmand, modirList, person)

}

def update_employee_v2() {


    step = ec.entity.find("Step").condition([username: ec.user.username]).one()
    if (!step) {
        description = ec.l10n.toPersianLocale("noStepFound")
        state = 0
        return
    }
    orgId = step?.orgId
    if (!orgId) {
        description = ec.l10n.toPersianLocale("noStepFound")
        state = 0
        return
    }
    if ("karmand".equals(role)) {
        karmand = [
                "unitId"    : unitId,
                "employeeId": employeeId
        ]
    }
    if ("modir".equals(role)) {
        modirList = [
                ["unitId": unitId, employeeId: employeeId]
        ]
    }
    person = [nationalId: nationalId, name: name, family: family, gender: gender, dayFloat: dayFloat, monthFloat: monthFloat, empType: empType, profileFileId: profileFileId, empDate: empDate, endDate: endDate, birthDate: birthDate,device:device,locationCheck: locationCheck]
    edit_employee(employeeId, karmand, modirList, person)

}

def edit_employee(employeeId, karmand, modirList, person) {


    try {
        if (!this.hasProperty("ec")) this.ec = Moqui.getExecutionContextFactory()
        current_employee = ec.entity.find("Employee").condition([nationalId: person.nationalId]).list().find { e -> e.employeeId != employeeId }
        if (current_employee) {
            state = 0
            description = ec.l10n.toPersianLocale("duplicateEmployee")
            return
        }
        birthDate = null
        if (person.birthDate) {
            birthDate = LocalDate.parse(person.birthDate)
            if (birthDate.isAfter(LocalDate.now())) {
                description = ec.l10n.toPersianLocale("dateAfterError")
                state = 0
                return
            }
        }
        emp = ec.entity.find("Employee").condition([employeeId: employeeId]).one()
        if (!emp) {
            description = ec.l10n.toPersianLocale("noEmployeeFound")
            state = 0
            return
        }
        user = ec.entity.find("UserAccount").condition([employeeId: employeeId])
        userRecord = user.one()
        userId = userRecord?.userId
        currentDevice = emp?.device
        call = [:]
        switch (person?.device) {
            case "Android":
                if (!"Android".equals(currentDevice)) {
                    webHamkar = org.moqui.util.SystemBinding.getPropOrEnv('hamkarWebClientId')
                    call = ec.service.sync().name("security.user.expire#app").parameters([users: [userRecord?.username], clientId: webHamkar]).call()
                }
                break
            case "WebApp":
                if (!"WebApp".equals(currentDevice)) {
                    hamkarApp = org.moqui.util.SystemBinding.getPropOrEnv('hamkarClientId')
                    call = ec.service.sync().name("security.user.expire#app").parameters([users: [userRecord?.username], clientId: hamkarApp]).call()
                }
                break
            default:
                call.state = 1
                break
        }
        if (call && call.state != 1) {
            state = 0
            description = ec.l10n.toPersianLocale("apiError")
            return
        }
        unitId = karmand?.unitId
        call = update_enteringSystemEmployee(person.nationalId, person.name, person.family, person.empDate, person.endDate, person.birthDate, person.empType, person.monthFloat, person.dayFloat, unitId)
        if (call && call?.state != 1) {
            state = 0
            description = call?.description
            ec.transaction.rollback(description, new Exception())
            return
        }
        if (karmand) {
            curK = ec.entity.find("Employee").condition([employeeId: employeeId]).one()
            cur = ec.entity.find("EmplUnit").condition([employeeId: employeeId, "role": "karmand"]).one()
            ec.service.sync().name("store#EmplUnit").parameters([emplUnitId: cur?.emplUnitId, role: "karmand", unitId: karmand?.unitId, employeeId: employeeId]).call()
        } else {
            ec.entity.find("EmplUnit").condition([employeeId: employeeId, "role": "karmand"]).deleteAll()
        }
        userFullName = person.name + " " + person.family
        user.updateAll([userFullName: userFullName])
        currentModir = ec.entity.find("EmplUnit").condition([role: "modir", employeeId: employeeId]).list()
        if (modirList?.size() > 0) {
            modirs = modirList?.unitId
            currentUns = currentModir.unitId
            deleteList = currentModir.findAll { e -> modirs?.indexOf(e.unitId) == -1 }
            newList = modirList.findAll { e -> currentUns?.indexOf(e.unitId) == -1 }
            ec.entity.find("EmplUnit").condition([employeeId: deleteList.employeeId, "role": "modir"]).deleteAll()

            companyModir = org.moqui.util.SystemBinding.getPropOrEnv('companyModir')
            if (currentModir?.size() > 0 && modirList?.size() == 0) {
                ec.entity.find("UserGroupMember").condition([userId: userId, userGroupId: companyModir]).deleteAll()
            }
            newList?.each { e ->
                mo = ec.entity.find("EmplUnit").condition([unitId: e.unitId, role: "modir"]).one()
                if (mo) {
                    mo = ec.entity.find("Employee").condition([employeeId: mo.employeeId]).one()
                    if (mo.employeeId != employeeId) {
                        modir = ec.entity.find("UserAccount").condition([employeeId: mo.employeeId]).one()?.userFullName
                        description = ec.l10n.toPersianLocale("modirPrefixError") + " " + modir + " " + ec.l10n.toPersianLocale("modirSuffixError")
                        state = 0
                        throw new Exception(description)

                    }
                } else {
                    ec.service.sync().name("create#EmplUnit").parameters([unitId: e.unitId, employeeId: employeeId, "role": "modir"]).call()
                }
            }
            ug = ec.entity.find("UserGroupMember").condition([userId: userId, userGroupId: companyModir]).one()
            if (!ug) ec.service.sync().name("create#UserGroupMember").parameters([userId: userId, userGroupId: companyModir, fromDate: new java.util.Date()]).call()
        } else {
            ec.entity.find("EmplUnit").condition([employeeId: employeeId, "role": "modir"]).deleteAll()
        }
        ec.service.sync().name("update#Employee").parameters([employeeId: employeeId] + person).call()
        description = ec.l10n.toPersianLocale("successEditEmployee")
        state = 1
    }
    catch (Exception ignored) {
        if (state != 0) description = ec.l10n.toPersianLocale("generalError")
        ec.transaction.rollback(description, new Exception())
        state = 0
    }

}

def delete_employee() {

    users = ec.entity.find("UserAccount").condition([employeeId: employeeId]).list()
    userIds = users.userId
    usernames = users?.username
    call = ec.service.sync().name("security.user.expire#app").parameters([users: usernames]).call()
    if (call.state != 1) {
        state = 0
        description = call.description
        return
    }
    ec.entity.find("EmplUnit").condition([employeeId: employeeId]).deleteAll()
    ec.entity.find("UserGroupMember").condition(["userId": userIds]).deleteAll()
    ec.entity.find("UserAccount").condition(["employeeId": employeeId]).updateAll(["disabled": "Y", "employeeId": null])
    ec.entity.find("UserLocation").condition(["employeeId": employeeId]).deleteAll()
    ec.entity.find("Request").condition(["employeeId": employeeId]).deleteAll()
    ec.entity.find("Organization").condition(["admin": employeeId]).updateAll([admin: null])
    ec.entity.find("EmployeeRequest").condition(["employeeId": employeeId]).deleteAll()
    ec.entity.find("Step").condition([username: usernames]).deleteAll()
    ec.entity.find("Employee").condition(["employeeId": employeeId]).deleteAll()
    description = ec.l10n.toPersianLocale("successDeleteEmployee")


}

def search_employee_v2() {

    step = ec.entity.find("Step").condition([username: ec.user.username]).one()
    orgId = step?.orgId
    if (!orgId) {
        description = ec.l10n.toPersianLocale("noAdminError")
        state = 0
        return
    }
    list_employee(orgId)
}

def get_employee() {

    if (!employeeId && !username) employeeId = ec.user.userAccount?.employeeId
    if (username) {
        user = ec.entity.find("UserAccount").condition([username: username]).one()
        if (!user) {
            state = 0
            return
        }
        employeeId = user?.employeeId
    }
    admin = ec.entity.find("Organization").condition([admin: employeeId]).one()
    employeeIds = []
    unitIds = []
    if (admin) {
        units = ec.entity.find("Unit").condition([orgId: admin?.orgId]).list()
        employmentIds = ec.entity.find("EmplUnit").condition([unitId: units?.unitId]).list()?.employeeId
        employees = ec.entity.find("Employee").condition([employeeId: employmentIds]).list()?.employeeId
        employeeIds.addAll(employees)
        unitIds.addAll(units?.unitId)
    }
    empls = ec.entity.find("Employee").condition([employeeId: employeeId]).list()?.employeeId
    unitList = ec.entity.find("EmplUnit").condition([role: "modir", "employeeId": empls]).list()?.unitId
    unitIds.addAll(unitList)
    employeeRel = ec.entity.find("EmplUnit").condition([unitId: unitIds, role: "karmand"]).list()
    employmentIds = employeeRel?.employeeId
    employeeList = ec.entity.find("Employee").condition([employeeId: employmentIds]).list()?.employeeId
    employeeIds.addAll(employeeList)
    state = 1

}

def search_employee() {

    orgId = null
    adminGrp = org.moqui.util.SystemBinding.getPropOrEnv('adminGroup')
    isAdmin = ec.userFacade.userGroupIdSet.contains(adminGrp)
    if (!isAdmin) {
        employeeId = ec.entity.find("UserAccount").condition([userId: ec.user.userId]).one()?.employeeId
        orgId = ec.entity.find("Organization").condition([admin: employeeId]).one()?.orgId
        if (!orgId) {
            description = ec.l10n.toPersianLocale("noAdminError")
            state = 1
            return
        }
        orgId = orgId
    }
    list_employee(orgId)
}

def list_employee(orgId) {

    data = [:]

    if (orgId) data.orgId = orgId
    employeesList = ec.entity.find("Employee").condition(data).list().getPlainValueList(0)
    users = ec.entity.find("UserAccount").condition([employeeId: employeesList?.employeeId]).list()
    units = ec.entity.find("EmplUnit").condition([employeeId: employeesList?.employeeId]).list()
    uns = ec.entity.find("Unit").condition([unitId: units?.unitId]).list()
    orgs = ec.entity.find("Organization").condition([orgId: employeesList.orgId]).list()
    employees = []
    employeesList.each { person ->
        it = [:]
        user = users.find { e -> e?.employeeId == person?.employeeId }
        un = units.find { e -> e?.employeeId == person?.employeeId }
        unit = uns.find { e -> e?.unitId == un?.unitId }
        org = orgs.find { t -> t.orgId == person.orgId }
        it.name = person?.name ?: ""
        it.family = person?.family ?: ""
        it.mobile = user?.username ?: ""
        it.organizationName = org?.name ?: ""
        it.unitName = unit?.name ?: ""
        it.dayFloat = person?.dayFloat ?: ""
        it.monthFloat = person?.monthFloat ?: ""
        it.unitId = unit?.unitId ?: ""
        it.orgId = person?.orgId ?: ""
        it.gender = person?.gender ?: ""
        it.employeeId = person?.employeeId ?: ""
        it.nationalId = person?.nationalId ?: ""
        it.birthDate = person?.birthDate ? person?.birthDate?.toString()?.split(" ")[0] : ""
        it.empDate = person?.empDate ? person?.empDate?.toString()?.split(" ")[0] : ""
        it.endDate = person?.endDate ? person?.endDate?.toString()?.split(" ")[0] : ""
        it.empType = person?.empType ?: ""
        it.role = un?.rl?.description ?: ""
        it.device = person.device
        it.locationCheck = person.locationCheck
        it.profileFileId = person?.profileFileId ? org.moqui.util.SystemBinding.getPropOrEnv('httpOrhttps') + ec.web.getWebappRootUrl(true, true).split("http")[1] + "/rest/s1/general/download?fileId=" + person?.profileFileId : ""
        roles = units.findAll { i -> i.employeeId == person.employeeId }
        karmand = roles.find { l -> "karmand".equals(l.role) }
        if (karmand) {
            unit = uns.find { l -> l.unitId == karmand.unitId }
            it.karmand = unit
            it.shift = unit?.shiftType == "Hour" ? "ساعتی" : "عادی"
        }
        modirList = roles.findAll { l -> "modir".equals(l.role) }?.unitId
        it.modirList = uns.findAll { l -> modirList.indexOf(l.unitId) != -1 }
        employees.add(it)
    }
}
