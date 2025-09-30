import groovy.json.JsonSlurper
import org.moqui.entity.EntityCondition

import java.sql.Date

def get_employee() {

    employees = ec.entity.find("Employee").condition([orgId: orgId]).list()

}

def create_admin() {
    try {
        tr = ec.transaction.begin(null)
        if (!password.equals(passwordVerify)) {
            description = ec.l10n.toPersianLocale("passwordVerifyError")
            state = 0
            return
        }
        call = ec.service.sync().name("hr.employee.create#employee").parameters(context + [isAdmin: true]).call()
        state = call?.state
        if (!state.equals(1)) {
            throw new RuntimeException(call?.description)
        }
        employeeId = call?.employeeId
        defaultUnit = ec.entity.find("Unit").condition([orgId: orgId, isDefault: "Y"]).one()
        if (defaultUnit) {
            cur = ec.entity.find("EmplUnit").condition([unitId: defaultUnit?.unitId, role: "karmand", employeeId: employeeId]).one()
            if (!cur) {
                ec.service.sync().name("create#EmplUnit").parameters([employeeId: employeeId, unitId: defaultUnit?.unitId, role: "karmand"]).call()
            }
        }
        ec.service.sync().name("update#Organization").parameters(["admin": employeeId, orgId: orgId]).call()
        userId = ec.entity.find("UserAccount").condition([employeeId: employeeId]).one()?.userId
        cur = ec.entity.find("UserGroupMember").condition([userId: userId]).list()?.userGroupId
        generalAccess = org.moqui.util.SystemBinding.getPropOrEnv('generalAccess')
        companyAdmin = org.moqui.util.SystemBinding.getPropOrEnv('companyAdmin')
        if (!cur.contains(generalAccess)) ec.service.sync().name("create#UserGroupMember").parameters([userId: userId, userGroupId: generalAccess, fromDate: new Date()]).call()
        if (!cur.contains(companyAdmin)) ec.service.sync().name("create#UserGroupMember").parameters([userId: userId, userGroupId: companyAdmin, fromDate: new Date()]).call()
        ec.transaction.commit(tr)
        state = 1
    }
    catch (Exception e) {
        description = e.getMessage()
        ec.transaction.rollback(description, e)
        state = 0
    }
}

def create_organization() {
    try {
        call = ec.service.sync().name("create#Organization").parameters(context).call()
        orgId = call?.orgId
        call = ec.service.sync().name("general.general.get#connection").call()
        if ("1".equals(call?.state)) {
            throw new RuntimeException(ec.l10n.toPersianLocale("sqlConError"))
        }
        sql = call?.sql
        sql.executeInsert("INSERT INTO SECTIONS(TITLE,SEC_NO) VALUES(?,?)", [name, orgId])
        sql.close()
        unitName = ec.l10n.toPersianLocale('defaultUnitName')
        call = ec.service.sync().name("organization.unit.create#unit").parameters([isDefault: "Y", orgId: orgId, name: unitName, holidayConf: "Y", shiftType: "General"]).call()
        unitId = call?.unitId
        days = org.moqui.util.SystemBinding.getPropOrEnv('days')
        JsonSlurper slurper = new groovy.json.JsonSlurper()
        dayList = slurper.parseText(days);

        call = ec.service.sync().name("organization.shift.create#shift").parameters([shiftType: "General", unitId: unitId, unitName: unitName, orgName: name, dayList: dayList, withHoliday: true]).call()
        state = 1
    }
    catch (Exception e) {
        description = e.getMessage()
        state = 0
    }
    finally {
        if (sql) sql.close()
    }
}

def edit_organization() {

    call = ec.service.sync().name("general.general.get#connection").call()
    if (1 != call?.state) {
        throw new RuntimeException(ec.l10n.toPersianLocale("sqlConError"))
    }
    sql = call?.sql
    sql.executeInsert("UPDATE SECTIONS SET TITLE=? WHERE SEC_NO=?", [name, orgId])
    ec.service.sync().name("update#Organization").parameters(context + [email: email, category: category]).call()
    state = 1
}

def patch_organization() {
    try {
        data = [orgId: orgId]
        if (checkAdmin) data.checkAdmin = checkAdmin
        if (checkLocation) data.checkLocation = checkLocation
        ec.service.sync().name("update#Organization").parameters(data).call()
        state = 1
    }
    catch (Exception e) {
        ec.transaction.rollback(e.getMessage(), e)
        description = ec.l10n.toPersianLocale("generalError")
        state = 0
    }
}

def disable_organization() {

    try {
        tr = ec.transaction.begin(null)
        emps = ec.entity.find("Employee").condition([orgId: orgId]).list()
        employeeId = emps.employeeId
        users = ec.entity.find("UserAccount").condition([employeeId: employeeId])
        if (disable == "Y") {
            userList = users?.list()
            if (userList.size() > 0) {
                call = ec.service.sync().name("security.user.expire#app").parameters([users: userList?.username]).call()
                if (!call?.state?.equals("1")) {
                    description = call.description
                    throw new RuntimeException(call?.description)
                }
            }
        }
        ec.service.sync().name("update#Organization").parameters([orgId: orgId, disabled: disable]).call()
        users.updateAll([disabled: disable])
        ec.transaction.commit(tr)
        state = 1
    }
    catch (Exception e) {
        ec.transaction.rollback(e.getMessage(), e)
        state = 0
    }

}

def delete_organization() {
    try {
        tr = ec.transaction.begin(null)
        units = ec.entity.find("Unit").condition([orgId: orgId])
        allUnit = units.list().unitId
        emUn = ec.entity.find("EmplUnit").condition([unitId: allUnit])
        emps = ec.entity.find("Employee").condition([orgId: orgId])
        ec.entity.find("Employee").condition([employeeId: emps.list().employeeId]).deleteAll()
        units.deleteAll()
        emps.deleteAll()
//        ec.service.sync().name("update#Organization").parameters([isDeleted: "Y", orgId: orgId]).call()
        state = 1
    }
    catch (Exception e) {
        ec.transaction.rollback(e.getMessage(), e)
        e.printStackTrace()
        state = 0
    }
}

def search_organization() {
    try {
        organizations = ec.entity.find("Organization").condition("name", EntityCondition.LIKE, '%' + name + '%').list().getPlainValueList(0)
        orgIds = organizations.orgId
        employees = ec.entity.find("Employee").condition([employeeId: organizations?.admin]).list()
        users = ec.entity.find("UserAccount").condition([employeeId: employees?.employeeId]).list()
        empls = ec.entity.find("Employee").condition([employeeId: employees?.employeeId]).list()
        allEmpls = ec.entity.find("Employee").condition([orgId: orgIds]).list()
        organizations.each { it ->
            emp = empls.findAll { ele -> ele?.orgId == it?.orgId }
            admin = employees.find { ele -> ele?.employeeId == it?.admin }
            user = users.find { ele -> ele?.employeeId == admin?.employeeId }
            it.adminName = (admin?.name ?: "") + " " + (admin?.family ?: "")
            it.adminMobile = user?.username
            it.usersCount = allEmpls.findAll { ele -> ele.orgId == it.orgId }.size()
        }
        state = 1
    }
    catch (Exception e) {
        e.printStackTrace()
        state = 0
    }
}

def create_organization_v2() {

    try {
        if (adminMobile != ec.user.username) {
            description = ec.l10n.toPersianLocale("requestUserError")
            state = 0
            return
        }
        curStep = ec.entity.find("Step").condition(["username": adminMobile]).one()
        if (!curStep || !curStep.orgId) {
            adminEmpl = ec.user.userAccount?.employeeId
            if (adminEmpl) {
                adminRec = ec.entity.find("Organization").condition([admin: adminEmpl]).one()
                if (adminRec) {
                    defaultUnit = ec.entity.find("Unit").condition([isDefault: "Y", orgId: adminRec.orgId]).one()
                    curStep = ec.service.sync().name("create#Step").parameters([username: adminMobile, orgId: adminRec?.orgId, unitId: defaultUnit?.unitId, isSkip: "N"]).call()
                    curStep.orgId = adminRec?.orgId
                    curStep.unitId = defaultUnit?.unitId
                }
            }
        }
        adminOrg = ec.entity.find("UserAccount").condition([username: adminMobile]).one()
        if (!adminOrg) {
            state = 0
            description = ec.l10n.toPersianLocale("noUserFound")
            return;
        }
        if (curStep?.orgId) {
            unitId = curStep.unitId
            orgId = curStep?.orgId
            call = ec.service.sync().name("organization.organization.edit#organization").parameters(context + [orgId: orgId]).call()
        } else {
            call = ec.service.sync().name("organization.organization.create#organization").parameters([name: name, category: category, phone: phone, email: email, logo: logo]).call()
            unitId = call.unitId
            orgId = call.orgId
        }
        if (call.state != 1) {
            state = call.state
            description = call.description
        }
        path = ec.resource.getLocationReference("component/core/service/hr/employee.groovy").toString().split("file:")[1]
        employeeScript = new GroovyShell().parse(new File(path))

        if (adminOrg.employeeId) {
            employeeScript.edit_employee(adminOrg.employeeId, null, [], [empType: 1, name: adminName, family: adminFamily, nationalId: adminNationalId])
            adminEmployeeId = adminOrg.employeeId
        } else {
            employeeScript.add_employee(orgId, true, adminMobile, [empType: 1, name: adminName, family: adminFamily, nationalId: adminNationalId], null, role, null)
            adminEmployeeId = employeeScript.employeeId
        }
        if (unitId && adminEmployeeId) {
            cur = ec.entity.find("EmplUnit").condition([unitId: unitId, role: "karmand", employeeId: adminEmployeeId]).one()
            if (!cur) {
                ec.service.sync().name("create#EmplUnit").parameters([employeeId: adminEmployeeId, unitId: unitId, role: "karmand"]).call()
            }
        }
        if (employeeScript.state != 1) {
            state = 0
            description = employeeScript.description
            return
        }
        ec.service.sync().name("update#Organization").parameters([orgId: orgId, admin: adminEmployeeId]).call()
        if (curStep) {
            ec.service.sync().name("update#Step").parameters([stepId: curStep?.stepId, "username": adminMobile, stepNum: 1, orgId: orgId, unitId: unitId]).call()
        } else {
            ec.service.sync().name("create#Step").parameters(["username": adminMobile, orgId: orgId, unitId: unitId, stepNum: 1]).call()
        }
        description = ec.l10n.toPersianLocale("successOrg")
        state = 1
    }
    catch (Exception e) {
        e.printStackTrace()
        state = 0
    }
}

def get_createPage() {

    category = ec.entity.find("Enumeration").condition([enumTypeId: "OrgCategory"]).orderBy("sequenceNum").list()
    state = 1
}

def get_organization() {

    try {
        step = ec.entity.find("Step").condition([username: ec.user.username]).one()
        org = [:]
        if (!step || !step.orgId) {
            state = 1
            return
        }
        orgId = step?.orgId
        organ = ec.entity.find("Organization").condition([orgId: orgId]).one()
        admin = organ?.admin
        adminUser = ec.entity.find("UserAccount").condition([employeeId: admin]).one()
        adminEmployee = ec.entity.find("Employee").condition([employeeId: admin]).one()
        org = [
                "name"           : organ?.name ?: "",
                "category"       : organ?.category ?: "",
                "phone"          : organ?.phone ?: "",
                "orgId"          : organ?.orgId ?: "",
                "email"          : organ?.email ?: "",
                "logo"           : organ?.logo ? org.moqui.util.SystemBinding.getPropOrEnv('httpOrhttps') + ec.web.getWebappRootUrl(true, true).split("http")[1] + "/rest/s1/general/download?fileId=" + organ?.logo : "",
                "adminMobile"    : adminUser?.username ?: "",
                "adminName"      : adminEmployee?.name ?: "",
                "adminFamily"    : adminEmployee?.family ?: "",
                "adminNationalId": adminEmployee?.nationalId ?: "",
        ]
        state = 1
    }
    catch (Exception e) {
        e.printStackTrace()
        description = ec.l10n.toPersianLocale('generalError')
        state = 0
    }


}

def delete_admin() {

    org = ec.entity.find("Organization").condition([orgId: orgId])
    employeeId = org.one().admin
    userId = ec.entity.find("UserAccount").condition([employeeId: employeeId]).one()?.userId
    adminAccess = org.moqui.util.SystemBinding.getPropOrEnv('companyAdmin')
    ec.entity.find("UserGroupMember").condition([userId: userId, userGroupId: adminAccess]).disableAuthz().deleteAll()
    org.updateAll([admin: null])
    state = 1
}

def get_unit() {

    orgs = ec.entity.find("Organization").condition([admin: ec.user.userAccount?.employeeId]).list()
    orgId = orgs?.orgId
    units = ec.entity.find("Unit").condition([orgId: orgId]).list()
}


def get_plan() {

    plan = ec.entity.find("Customer").condition([orgId: orgId]).list().find { e -> e.endDate > new Date() }

}

def disable_plan() {


    orgs = ec.entity.find("Customer").list()
    dOrgs = orgs.findAll { e -> e.endDate & lt; new Date() }
    allOrgs = ec.entity.find("Organization").list()
    allOrgs = allOrgs.findAll { e -> orgs.orgId.indexOf(e.orgId) == -1 }
    orgIds = dOrgs.orgId
    orgIds.addAll(allOrgs?.orgId)
    empls = ec.entity.find("Employee").condition([orgId: orgIds]).list()
    emplIds = empls?.employeeId
    admins = ec.entity.find("Organization").condition([orgId: orgIds]).list()?.admin
    emplIds.removeAll(admins)
    users = ec.entity.find("UserAccount").condition([employeeId: emplIds])
    ec.service.sync().name("security.user.expire#app").parameters([users: users?.list()?.username]).call()
    users.updateAll([disabled: "Y"])

}