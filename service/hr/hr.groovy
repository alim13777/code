import com.atlas.hamkar.JalaliCalendar
import groovy.transform.Field

import java.sql.Date
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Field dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.forLanguageTag("fa"))


def create_emplUnit() {

    try {
        if (unitId && role == "modir") {
            modirRel = ec.entity.find("EmplUnit").condition([unitId: unitId, role: "modir"]).one()
            if (modirRel) {
                empl = ec.entity.find("Employee").condition([employeeId: modirRel.employeeId]).one()
                modir = ec.entity.find("UserAccount").condition([employeeId: empl?.employeeId]).one()?.userFullName
                description = ec.l10n.toPersianLocale("modirPrefixError") + " " + modir + " " + ec.l10n.toPersianLocale("modirSuffixError")
                state = 0
                return
            }
        }
        employment = ec.entity.find("EmplUnit").condition([employeeId: employeeId, role: "karmand"]).list()
        if (employment && role == "karmand") {
            description = ec.l10n.toPersianLocale('karmandError')
            state = 0
            return
        }
        call = ec.service.sync().name("general.general.get#connection").call()
        sql = call?.sql
        employee = ec.entity.find("Employee").condition([employeeId: employeeId]).one()
        if (!employee) {
            description = ec.l10n.toPersianLocale("noEmployeeFound")
            state = 0
            return
        }
        if (!employee.nationalId) {
            description = ec.l10n.toPersianLocale("noNationalId")
            state = 0
            return
        }
        result = sql.rows("select * from EMPLOYEE WHERE PERS_NO=?", [Long.valueOf(employee.nationalId)])
        data = [name: employee.name, family: employee.family, nationalId: employee.nationalId, orgId: orgId, employeeId: employeeId]
        if (role == "karmand") data.unitId = unitId
        if (result.size() > 0) {
            ec.service.sync().name("hr.employee.update#enteringSystemEmployee").parameters(data).call()
        } else {
            ec.service.sync().name("hr.employee.create#enteringSystemEmployee").parameters(data).call()
        }
        if (role == "modir") {
            userId = ec.entity.find("UserAccount").condition([employeeId: employeeId]).one()?.userId
            modirAccess = org.moqui.util.SystemBinding.getPropOrEnv('companyModir')
            ug = ec.entity.find("UserGroupMember").condition([userId: userId, userGroupId: modirAccess]).one()
            if (!ug) ec.service.sync().name("create#UserGroupMember").parameters([userId: userId, userGroupId: modirAccess, fromDate: new java.util.Date()]).call()
        }
        curUn = ec.entity.find("EmplUnit").condition([employeeId: employeeId, role: role, unitId: unitId]).one()
        if (!curUn) {
            call = ec.service.sync().name("create#EmplUnit").parameters(context).call()
            emplUnitId = call.emplUnitId
        } else emplUnitId = curUn.emplUnitId
        state = 1
    }
    catch (Exception e) {
        e.printStackTrace()
        state = 0
    }
}

def disable_employee() {
    try {
        ec.entity.find("UserAccount").condition([employeeId: employeeId]).updateAll([disabled: "Y"])
        state = 1
    }
    catch (Exception e) {
        e.printStackTrace()
        state = 0
    }
}

def get_userEmployee() {
    try {
        user = ec.entity.find("UserAccount").condition([username: username]).one()
        if (!user) {
            employee = []
            state = 0
            return
        }
        employeeId = user.employeeId
        if (!employeeId) {
            employee = []
            state = 0
            return
        }
        empl = ec.entity.find("Employee").condition([employeeId: employeeId]).one()
        unit = ec.entity.find("EmplUnit").condition([employeeId: empl?.employeeId, role: "modir"]).list()
        if (unit.size() == 0) {
            employee = []
            state = 1
            return
        }
        unitIds = unit?.unitId
        uns = ec.entity.find("EmplUnit").condition([unitId: unitIds, role: "karmand"]).list()
        emps = ec.entity.find("Employee").condition([employeeId: uns?.employeeId]).list()
        users = ec.entity.find("UserAccount").condition([employeeId: emps?.employeeId]).list()
        employee = []
        users.each { it ->
            empl = emps.find { a -> a.employeeId == it?.employeeId }
            e = ["userFullName": it.userFullName, "username": it.username, image: "https://panel.hamkar-app.ir/rest/s1/general/download?fileId=" + empl?.profileFileId]
            employee.add(e)
        }
        state = 1
    }
    catch (Exception e) {
        e.printStackTrace()
        state = 0
    }
}

def get_createPage() {
    try {
        isAdmin = false
        orgs = ec.entity.find("Organization").list()
        units = ec.entity.find("Unit").list()
        roles = ec.entity.find("Role").list()
        serviceCall = ec.service.sync().name("general.general.get#dataType").parameters([type: "empType"]).call()
        empType = serviceCall?.data
        groups = ec.entity.find("UserGroupMember").condition(["userId": ec.user.userId]).list()?.userGroupId
        if (groups.contains("ADMIN")) isAdmin = true
        else {
            employeeId = ec.entity.find("UserAccount").condition([userId: ec.user.userId]).one()?.employeeId
            org = ec.entity.find("Organization").condition([admin: employeeId]).one()
            units = ec.entity.find("Unit").condition([orgId: org?.orgId]).list()
        }
        devices = ec.entity.find("Enumeration").condition([enumTypeId: "Device"]).list()
        j = new JalaliCalendar(LocalDate.now())
        j.setDay(1)
        j.setMonth(1)
        d = j.toGregorian()
        firstYearDate = d.getTime().format("YYYY-MM-dd")

    }
    catch (Exception e) {
        e.printStackTrace()
        state = 0
    }
}

def get_reportConfig() {
    try {
        org = ec.entity.find('Organization').condition([admin: ec.user.userAccount?.employeeId]).one()
        if (!org) {
            state = 0
            return
        }
        orgId = org?.orgId
        config = ec.entity.find('ReportConfig').condition([orgId: orgId]).one()
        state = 1
    }
    catch (Exception ignored) {
        state = 0
    }
}

def get_unitEmployee() {
    try {
        data = [:]
        dataUnit = [:]
        groups = ec.entity.find("UserGroupMember").condition([userId: ec.user.userId]).one()?.userGroupId
        if (unitId) {
            dataUnit["unitId"] = unitId
            data.employeeId = ec.entity.find("EmplUnit").condition(dataUnit).list()?.employeeId
        } else if (orgId) {
            dataUnit["orgId"] = orgId
            data.employeeId = ec.entity.find("Employee").condition([orgId: orgId]).list()?.employeeId
        } else if (!groups.contains("ADMIN")) {
            orgIds = ec.entity.find("Organization").condition([admin: ec.user.userAccount?.employeeId]).list()?.orgId
            dataUnit["orgId"] = orgIds
            employeeId = ec.entity.find("Employee").condition(dataUnit).list()?.employeeId
            data.employeeId = employeeId
        }
        data.employmentDeleted = "N"
        employee = ec.entity.find("EmployeeDetail").condition(data).list().unique { ele -> ele.nationalId }
        state = 1
    }
    catch (Exception e) {
        e.printStackTrace()
        state = 0
    }
}

def delete_emplUnit() {

    try {
        today = java.sql.Date.valueOf(LocalDate.now(ZoneId.of("Asia/Tehran")))
        emplUnit = ec.entity.find("EmplUnit").condition([emplUnitId: emplUnitId]).one()
        employeeId = emplUnit?.employeeId
        if (!employeeId) {
            state = 0
            return
        }
        if (emplUnit?.role == "karmand") {
            nationalId = ec.entity.find("Employee").condition([employeeId: employeeId]).one()?.nationalId
            if (!nationalId) {
                state = 0
                return
            }
            nationalId = Long.valueOf(nationalId)
            sql = ec.service.sync().name("general.general.get#connection").call()?.sql
            sql.executeInsert("update EMPLOYEE set GRP_NO=NULL WHERE PERS_NO=?", nationalId)
            sql.close()
        }
        ec.service.sync().name("update#EmplUnit").parameters([emplUnitId: emplUnitId, endDate: today]).call()
        if (emplUnit?.role == "modir") {
            empls = ec.entity.find("Employee").condition([employeeId: employeeId]).list()
            otherRole = ec.entity.find("EmplUnit").condition([employeeId: empls.employeeId]).list().findAll { e -> e.emplUnitId != emplUnitId }?.role
            if (otherRole?.indexOf("modir") == -1) {
                userId = ec.entity.find("UserAccount").condition([employeeId: employeeId]).one()?.userId
                modirAccess = org.moqui.util.SystemBinding.getPropOrEnv('companyModir')
                ec.entity.find("UserGroupMember").condition([userId: userId, userGroupId: modirAccess]).disableAuthz().deleteAll()
            }
        }
        state = 1
    }
    catch (Exception e) {
        e.printStackTrace()
        state = 0
    }
    finally {
        if (sql) sql.close()
    }
}

def get_createPage_v2() {

    employeeId = ec.entity.find("UserAccount").condition([userId: ec.user.userId]).one()?.employeeId
    org = ec.entity.find("Organization").condition([admin: employeeId]).one()
    if (!org) {
        description = ec.l10n.toPersianLocale("noAdminError")
        state = 0
        return
    }
    units = ec.entity.find("Unit").condition([orgId: org?.orgId]).list()
    roles = ec.entity.find("Role").list()
    serviceCall = ec.service.sync().name("general.general.get#dataType").parameters([type: "empType"]).call()
    empType = serviceCall?.data
    devices = ec.entity.find("Enumeration").condition([enumTypeId: "Device"]).list()

}

def create_holiday() {
    try {
        d = Date.valueOf(LocalDate.parse(date, dateTimeFormatter))
        h = ec.entity.find("Holiday").condition(["date": d]).one()
        if (h) {
            description = ec.l10n.toPersianLocale('duplicateRecordFound')
            state = 0
            return
        }
        if (d.compareTo(Date.valueOf(LocalDate.now(ZoneId.of("Asia/Tehran")))) <= 0) {
            description = ec.l10n.toPersianLocale('holidayCreateError')
            state = 0
            return
        }

        beganTransaction = ec.transaction.begin(null)
        shamsiDate = new JalaliCalendar(d)
        year = shamsiDate.getYear()
        ec.service.sync().name("create#Holiday").parameters([date: d, year: year, title: title, description: description]).call()
        update_shift(d)
        ec.transaction.commit(beganTransaction)
        state = 1
        description = ""
    }
    catch (Exception e) {
        ec.transaction.rollback(e.getMessage(), e)
        state = 0
    }

}

def search_holiday() {


    try {
        data = [:]
        if (year) data.year = year
        holidayList = ec.entity.find("Holiday").condition(data).selectFields(["holidayId", "title", "description", "date", "year"]).list()
        holiday = []
        holidayList.each { e ->
            t = e
            t.date = e?.date?.toString()
            holiday.add(t)
        }
        state = 1
    }
    catch (Exception e) {
        e.printStackTrace()
        state = 0
    }

}

def update_holiday() {

    try {
        hol = ec.entity.find("Holiday").condition([holidayId: holidayId]).one()
        if (!hol) {
            description = ec.l10n.toPersianLocale('noRecordFound')
            state = 0
            return
        }
        beganTransaction = ec.transaction.begin(null)
        current_date = hol.get("date")
        parsedDate = Date.valueOf(LocalDate.parse(date, dateTimeFormatter))
        today = Date.valueOf(LocalDate.now(ZoneId.of("Asia/Tehran")))
        if (!parsedDate.equals(current_date)) {
            if (current_date.compareTo(today) <= 0) {
                description = ec.l10n.toPersianLocale('holidayPassError')
                state = 0
                return
            }
        }
        shamsiDate = new JalaliCalendar(parsedDate)
        year = shamsiDate.getYear()
        ec.service.sync().name("update#Holiday").parameters([holidayId: holidayId, date: parsedDate, year: year, title: title, description: description]).call()
        if (!parsedDate.equals(current_date)) {
            delete_shift(current_date)
            update_shift(parsedDate)
        }
        ec.transaction.commit(beganTransaction)
        state = 1
        description = ""
    }
    catch (Exception e) {
        e.printStackTrace()
        description = ec.l10n.toPersianLocale("generalError")
        state = 0
    }
}

def delete_holiday() {
    try {
        hol = ec.entity.find("Holiday").condition([holidayId: holidayId]).one()
        if (!hol) {
            description = ec.l10n.toPersianLocale("noRecordFound")
            state = 0
            return
        }
        date = hol["date"]
        if (date.compareTo(Date.valueOf(LocalDate.now(ZoneId.of("Asia/Tehran")))) <= 0) {
            description = ec.l10n.toPersianLocale('holidayPassError')
            state = 0
            return
        }
        beganTransaction = ec.transaction.begin(null)
        ec.service.sync().name("delete#Holiday").parameters([holidayId: holidayId]).call()
        delete_shift(date)
        ec.transaction.commit(beganTransaction)
        state = 1
        description = ""
    }
    catch (Exception e) {
        ec.transaction.rollback(e.getMessage(), e)
        e.printStackTrace()
        description = ec.l10n.toPersianLocale("generalError")
        state = 0
    }
}

def update_shift(date) {

    try {
        if (date.compareTo(Date.valueOf(LocalDate.now(ZoneId.of("Asia/Tehran")))) <= 0) {
            return
        }
        shamsiDate = new JalaliCalendar(date)
        year = shamsiDate.getYear()
        month = shamsiDate.getMonth()
        day = shamsiDate.getDay()
        sql = ec.service.sync().name("general.general.get#connection").call()?.sql
        q = "UPDATE GrpShift set GrpShift_Day" + day + "=50 where GrpShift_Year=? and GrpShift_month=?"
        sql.executeInsert(q, [year, month])
    }
    catch (Exception e) {
        throw e
    }
    finally {
        if (sql) sql.close()
    }

}

def delete_shift(date) {

    try {
        d = Date.valueOf(LocalDate.now(ZoneId.of("Asia/Tehran")))
        if (date.compareTo(d) <= 0) {
            return
        }
        shamsiDate = new JalaliCalendar(date)
        year = shamsiDate.getYear()
        month = shamsiDate.getMonth()
        day = shamsiDate.getDay()
        dayW = shamsiDate.getDayOfWeek() + 1
        if (dayW == 8) dayW = 1

        sql = (groovy.sql.Sql) ec.service.sync().name("general.general.get#connection").call()?.sql
        q = "SELECT * FROM GrpShift where GrpShift_Year=? and GrpShift_month=? and GrpShift_DAY" + day + "=50"
        rows = sql.rows(q, [year, month])
        unitIds = rows.collect { e -> e.get("GrpShift_GrpNo") }
        if (unitIds.isEmpty()) {
            return
        }
        uns = unitIds.join(",")
        qs = "SELECT * FROM SHIFTS WHERE UNIT_ID in (" + uns + ") AND SHIFT_TYPE!=-1 AND DAY=?"
        shifts = sql.rows(qs, [dayW])
        if (shifts.isEmpty()) {
            return
        }
        rows.each { e ->
            shiftId = null
            shift = null
            unitId = e.get("GrpShift_GrpNo")
            if (unitId == null) return
            shift = shifts.find { ele -> ele.get("UNIT_ID") == unitId && ele.get("DAY") == dayW }
            if (shift == null) return
            shiftId = shift.get("SHIFT_NO")
            up = "UPDATE GrpShift SET GrpShift_DAY" + day + "=? WHERE GrpShift_Year=? and GrpShift_month=? and GrpShift_GrpNo=?"
            sql.executeUpdate(up, [shiftId, year, month, unitId])
        }
    }
    catch (Exception e) {
        e.printStackTrace()
        throw e
    }
    finally {
        if (sql) sql.close()
    }

}

def get_managers() {

    try {
        user = ec.entity.find("UserAccount").condition([username: employeeId]).one()
        if (!user) {
            state = 0
            description = ec.l10n.toPersianLocale("noUserFound")
            return
        }
        employee = ec.entity.find("Employee").condition([employeeId: user.employeeId]).one()
        org = ec.entity.find("Organization").condition([orgId: employee?.orgId]).one()
        employeeIds = []
        admin = org?.admin
        if ("Y".equals(org?.checkAdmin)) {
            if (admin) employeeIds.add(admin)
        }
        unitId = ec.entity.find("EmplUnit").condition([employeeId: employee?.employeeId, role: "karmand"]).list()?.unitId
        modirEmpl = ec.entity.find("EmplUnit").condition([unitId: unitId, role: "modir"]).list()?.employeeId
        modir = ec.entity.find("Employee").condition([employeeId: modirEmpl]).list()?.employeeId
        if (modir.isEmpty() && admin) {
            employeeIds.add(admin)
        }
//        if (!modir) {
//            state = 0
//            description = ec.l10n.toPersianLocale("noModirFound")
//            return
//        }
        employeeIds.addAll(modir)
        manager = ec.entity.find("UserAccount").condition([employeeId: employeeIds]).list()?.username
        state = 1
    }
    catch (Exception e) {
        e.printStackTrace()
        state = 0
    }
}

def create_profileFile() {

    try {
        tr = ec.transaction.begin(null)
        user = ec.entity.find("UserAccount").condition([username: username]).one()
        if (!user) {
            description = ec.l10n.toPersianLocale("noUserFound")
            state = 0
            return
        }
        employeeId = user?.employeeId
        if (!employeeId) {
            description = ec.l10n.toPersianLocale("noEmployeeFound")
            state = 0
            return
        }
        call = ec.service.sync().name("general.file.create#file_v2").parameters([file: file, filename: filename]).call()
        state = call?.state
        description = call?.description
        if (state == 0) {
            throw new Exception(description)
        }
        fileId = call?.fileId
        ec.service.sync().name("update#Employee").parameters([employeeId: employeeId, profileFileId: fileId]).call()
        url = call?.url
        ec.transaction.commit(tr)
        state = 1
    }
    catch (Exception e) {
        if (state != 0) description = ec.l10n.toPersianLocale("generalError")
        state = 0
        ec.transaction.rollback(description, e)
    }


}

def get_profileFile() {

    try {
        url = ""
        user = ec.entity.find("UserAccount").condition([username: username]).one()
        if (!user) {
            description = ec.l10n.toPersianLocale("noRecordFound")
            state = 0
            return
        }
        employeeId = user?.employeeId
        emp = ec.entity.find("Employee").condition([employeeId: employeeId]).one()
        if (emp.profileFileId) {
            url = ec.service.sync().name("general.file.get#file_v2").parameters([fileId: emp?.profileFileId]).call()?.url
        }
        state = 1
    }
    catch (Exception ignored) {
        state = 0
    }

}

def delete_empl_unit() {
    ec.service.sync().name("delete#EmplUnit").parameters([emplUnitId: emplUnitId]).call()
}

def create_empl_unit() {
    if ("karmand".equals(role)) {
        karmands = ec.entity.find("EmplUnit").condition([employeeId: employeeId, role: "karmand"]).list()
        if (karmands.size() > 0) {
            description = ec.l10n.toPersianLocale('karmandError')
            state = 0
            return
        }
    }
    if ("modir".equals(role)) {
        modirRel = ec.entity.find("EmplUnit").condition([unitId: unitId, role: "modir"]).one()
        if (modirRel) {
            empl = ec.entity.find("Employee").condition([employeeId: modirRel?.employeeId, role: "modir"]).one()
            modir = ec.entity.find("UserAccount").condition([employeeId: empl.employeeId]).one()?.userFullName
            description = ec.l10n.toPersianLocale('modirPrefixError') + " " + modir + " " + ec.l10n.toPersianLocale('modirSuffixError')
            state = 0
        }
    }
    emplUnitId = ec.service.sync().name("create#EmplUnit").parameters([unitId: unitId, role: role, employeeId: employeeId]).call()?.emplUnitId
}

def get_sub_employee() {

    employeeId = ec.user.userAccount?.employeeId
    if (!employeeId) return;
    employeeIds = []
    units = ec.entity.find("EmplUnit").condition([employeeId: employeeId, role: "modir"]).list()?.unitId
    empls = ec.entity.find("EmplUnit").condition([unitId: units, role: "karmand"]).list()?.employeeId
    employeeIds.addAll(empls)
    adminOrg = ec.entity.find("Organization").condition([admin: employeeId]).list()
    if (adminOrg) {
        adminEmpls = ec.entity.find("Employee").condition([orgId: adminOrg?.orgId]).list()?.employeeId
        employeeIds.addAll(adminEmpls)
    }
    employees = ec.entity.find("Employee").condition([employeeId: employeeIds]).list()
    users = ec.entity.find("UserAccount").condition([employeeId: employees?.employeeId]).list()
    employee = []
    employees.each { e ->
        t = [:]
        user = users.find { l -> l.employeeId == e.employeeId }
        t.userFullName = e.name + " " + e.family
        t.username = user?.username
        t.nationalId = e.nationalId
        t.employeeId = e.employeeId
        employee.add(t)
    }

}

def get_manager() {
    try {
        employee = ec.entity.find("UserAccount").condition([username: employeeId]).one()?.employeeId
        if (!employee) {
            description = ec.l10n.toPersianLocale("noUserFound")
            state = 0
            return;
        }
        unitId = ec.entity.find("EmplUnit").condition([employeeId: employee]).one()?.unitId
        if (!unitId) {
            description = ec.l10n.toPersianLocale("noUnitFound")
            state = 0
            return;
        }
        modirEmpl = ec.entity.find("EmplUnit").condition([unitId: unitId, role: "modir"]).one()?.employeeId
        if (!modirEmpl) {
            state = 0
            description = ec.l10n.toPersianLocale("noModirFound")
            return;
        }
        manager = ec.entity.find("UserAccount").condition([employeeId: modirEmpl]).one()?.username
        state = 1
    }
    catch (Exception e) {
        e.printStackTrace()
        state = 0
    }
}

def get_unit_employee() {

    employeeIds = []
    if (unitId) {
        employeeIds = ec.entity.find("EmplUnit").condition([unitId: unitId, role: "karmand"]).list()?.employeeId

    } else if (orgId) {
        employeeIds = ec.entity.find("Employee").condition([orgId: orgId]).list()?.employeeId
    }
    println(employeeIds)
    employees = ec.entity.find("Employee").condition([employeeId: employeeIds]).list()
    println(employees)
    uss = ec.entity.find("UserAccount").condition([employeeId: employees?.employeeId]).list()
    employee = []
    employees.each { e ->
        it = [:]
        user = uss.find { t -> t.employeeId == e.employeeId }
        if (user) {
            it.userFullName = e.name + " " + e.family
            it.mobile = user.username
            it.name = e.name ?: ""
            it.nationalId = e.nationalId ?: ""
            it.family = e.family ?: ""
            employee.add(it)
        }
    }
}

def create_report_config() {

    if (!orgId) {
        org = ec.entity.find("Organization").condition([admin: ec.user.userAccount?.employeeId]).one()
        if (!org) {
            state = 0;
            return;
        }
        orgId = org?.orgId
    }
    conf = ec.entity.find("ReportConfig").condition([orgId: orgId]).one()
    if (conf) {
        configId = conf.configId
        ec.service.sync().name("update#ReportConfig").parameters(context).call()
    } else {
        configId = ec.service.sync().name("create#ReportConfig").parameters(context).call()?.configId
    }

}

def get_report_config() {

    config = ec.entity.find("ReportConfig").condition([orgId: orgId]).one()

}

def update_report_config() {

    ec.service.sync().name("update#ReportConfig").parameters(context).call()

}

def delete_role() {

    users = ec.entity.find("EmplUnit").condition([role: roleTypeId]).list()
    if (users.size() > 0) {
        description = ec.l10n.toPersianLocale('roleUserError')
        return
    }
    ec.service.sync().name("delete#Role").parameters([title: roleTypeId]).call()

}

def create_role() {

    ec.service.sync().name("create#Role").parameters([title:title,description: description]).call()


}

def update_role() {

    ec.service.sync().name("update#Role").parameters([title:title,description: description]).call()

}