def create_unit() {

    add_unit(name, orgId, category, description, holidayConf, shiftType, modir, employee, isDefault ?: "N")

}

def create_unit_v2() {
    step = ec.entity.find("Step").condition([username: ec.user.username]).one()
    if (!step) {
        description = ec.l10n.toPerisanLocale("noStepFound")
        state = 0
        return
    }
    orgId = step?.orgId
    if (!step) {
        description = ec.l10n.toPerisanLocale("noOrgFound")
        state = 0
        return
    }
    add_unit(name, orgId, category, description, holidayConf, shiftType, modir, employee, "N")
}

def add_unit(name, orgId, category, desc, holidayConf = "N", shiftType = "general", modir, employee = [], isDefault = "N") {

    try {
        org = ec.entity.find("Organization").condition([orgId: orgId]).one()
        if (!org) {
            description = ec.l10n.toPersianLocale("noOrgFound")
            state = 0
            return
        }
        karmands = ec.entity.find("EmplUnit").condition([employeeId: employee?.employeeId, role: "karmand"]).list()
        if (karmands.size() > 0) {
            description = ec.l10n.toPersianLocale('karmandError')
            ec.transaction.rollback(description, new Exception())
            state = 0
            return
        }
        if (modir) {
            modirRel = ec.entity.find("EmplUnit").condition([unitId: unitId, role: "modir"]).one()
            if (modirRel) {
                empl = ec.entity.find("Employee").condition([employeeId: modirRel?.employeeId, role: "modir"]).one()
                modir = ec.entity.find("UserAccount").condition([employeeId: empl.employeeId]).one()?.userFullName
                description = ec.l10n.toPersianLocale('modirPrefixError') + " " + modir + " " + ec.l10n.toPersianLocale('modirSuffixError')
                ec.transaction.rollback(description, new Exception())
                return [state: 0, description: description]
            }
        }
        orgName = org?.name
        serviceCall = ec.service.sync().name("create#Unit").parameters([isDefault: isDefault, name: name, orgId: orgId, category: category, description: desc, holidayConf: holidayConf, shiftType: shiftType]).call()
        unitId = serviceCall?.unitId
        employee.each { ele ->
            ec.service.sync().name("create#EmplUnit").parameters([employeeId: ele.employeeId, unitId: unitId, role: "karmand"]).call()
        }
        if (modir) {
            ec.service.sync().name("create#EmplUnit").parameters([employeeId: modir.employeeId, unitId: unitId, role: "modir"]).call()
        }
        call = ec.service.sync().name("general.general.get#connection").call()
        state = call.state
        if (state != 1) {
            description = ec.l10n.toPersianLocale("sqlConnectionError")
            state = 0
            return
        }
        sql = call?.sql
        query = "INSERT INTO GROUPS(TITLE,GRP_NO) VALUES(?,?)"
        unitName = orgName + "-" + name
        sql.executeInsert(query, [unitName, unitId])
        description = ec.l10n.toPersianLocale("successCreateUnit")
        state = 1
    }

    catch (Exception e) {
        ec.transaction.rollback(description, new Exception())
        state = 0
    }
}

def delete_unit() {

    employees = ec.entity.find("EmplUnit").condition([unitId: unitId]).list()
    if (employees.size() > 0) {
        description = ec.l10n.toPersianLocale("unitError")
        state = 0
        return
    }
    un = ec.entity.find("Unit").condition([unitId: unitId]).one()
    if ("Y".equals(un.isDefault)) {
        description = ec.l10n.toPersianLocale("defaultUnitError")
        state = 0
        return
    }
    ec.entity.find("Unit").condition([unitId: unitId]).deleteAll()
    description = ec.l10n.toPersianLocale("successDeleteUnit")
    state = 1


}

def create_page() {

    try {
        call = ec.service.sync().name("organization.unit.search#unit").call()
        isAdmin = call?.isAdmin
        units = call?.units
        orgIds = units?.orgId
        emps = ec.entity.find("Employee").condition([orgId: orgIds]).list()
        employees = ec.entity.find("Employee").condition([employeeId: emps.employeeId]).list()?.getPlainValueList(0)
        employees.each { ele ->
            empl = emps.find { it -> it.employeeId == ele.employeeId }
            ele.orgId = empl?.orgId
        }
        if (isAdmin) {
            organizations = ec.entity.find("Organization").list()
        } else {
            organization = ec.entity.find("Organization").condition([admin: ec.user.userAccount?.employeeId]).one()
            organizations = [organization]
        }
        roles = ec.entity.find("Role").list()
        state = 1
    }
    catch (Exception e) {
        e.printStackTrace()
        state = 0
    }
}

def create_page_v2() {

    try {
        call = ec.service.sync().name("organization.unit.search#unit").call()
        isAdmin = call?.isAdmin
        units = call?.units
        orgIds = units?.orgId
        emps = ec.entity.find("Employee").condition([orgId: orgIds]).list()
        employees = ec.entity.find("Employee").condition([employeeId: emps.employeeId]).list()?.getPlainValueList(0)
        employees.each { ele ->
            empl = emps.find { it -> it.employeeId == ele.employeeId }
            ele.orgId = empl?.orgId
        }
        if (isAdmin) {
            organizations = ec.entity.find("Organization").list()
        } else {
            organization = ec.entity.find("Organization").condition([admin: ec.user.userAccount?.employeeId]).one()
            organizations = [organization]
        }
        admin = ec.entity.find("Organization").condition([admin: ec.user.userAccount?.employeeId])?.one()
        if (!admin) {
            description = ec.l10n.toPersianLocale("noAdminError")
            state = 0
            return
        }
        employees = ec.entity.find("EmployeeDetail").condition([orgId: admin?.orgId, employmentDeleted: "N", "unitDeleted": "N"]).list().unique { e -> e.employeeId }
        roles = ec.entity.find("Role").list()
        category = ec.entity.find("Enumeration").condition([enumTypeId: "UnitCategory"]).list()

        state = 1
    }
    catch (Exception e) {
        e.printStackTrace()
        state = 0
    }
}

def search_unit() {
    try {
        adminGroup = org.moqui.util.SystemBinding.getPropOrEnv('adminGroup')
        isAdmin = ec.entity.find("UserGroupMember").condition([userId: ec.user.userId, userGroupId: adminGroup]).one() ? true : false
        if (!isAdmin) orgId = ec.entity.find("Organization").condition([admin: ec.user.userAccount?.employeeId]).one()?.orgId
        if (!isAdmin && !orgId) {
            description = ec.l10n.toPersianLocale("noAdminError")
            state = 0
            return
        }
        data = [:]
        if (orgId) data.orgId = orgId
        unitList = ec.entity.find("Unit").condition(data).list()
        unitIds = unitList.unitId
        orgs = ec.entity.find("Organization").condition([orgId: unitList.orgId]).list()
        uns = ec.entity.find("EmplUnit").condition([unitId: unitIds]).list().getPlainValueList(0)
        employees = ec.entity.find("Employee").condition([employeeId: uns?.employeeId]).list().getPlainValueList(0)
        units = []
        unitList.each { it ->
            entry = [:]
            entry.name = it.name
            entry.description = it.description
            entry.unitId = it.unitId
            entry.category = it.category
            entry.holidayConf = it.holidayConf
            entry.shiftType = it.shiftType
            entry.orgName = it.org?.name ?: ""
            entry.orgId = it.org?.orgId ?: ""
            modirEmp = uns.find { ele -> "modir".equals(ele.role) && ele.unitId == it.unitId }
            modir = employees.find { ele -> ele.employeeId == modirEmp?.employeeId }
            entry.modir = (modir?.name ?: "") + " " + (modir?.family ?: "")
            entry.modirObj = modir
            units.add(entry)
        }
        state = 1
    }
    catch (Exception e) {
        e.printStackTrace()
        state = 0
    }
}

def search_unit_v2() {
    try {

        step = ec.entity.find("Step").condition([username: ec.user.username]).one()
        if (!step) {
            description = ec.l10n.toPerisanLocale("noStepFound")
            state = 0
            return
        }
        orgId = step?.orgId
        unitList = ec.entity.find("Unit").condition("orgId", orgId).list()
        unitIds = unitList.unitId
        uns = ec.entity.find("EmplUnit").condition([unitId: unitIds]).list()
        employees = ec.entity.find("Employee").condition([employeeId: uns?.employeeId]).list()
        units = []
        unitList.each { it ->
            ele = [:]
            ele.orgName = it.org?.name ?: ""
            ele.name = it.name ?: ""
            ele.unitId = it.unitId
            ele.orgId = it.orgId
            ele.category = it.category ?: ""
            modirEmp = uns.find { e -> "modir".equals(e.role) && e.unitId == it.unitId }
            ele.modirName = ""
            ele.modir = [:]
            if (modirEmp) {
                modir = modirEmp?.empl
                ele.modir = modir
                ele.modirName = (modir?.name ?: "") + " " + (modir?.family ?: "") ?: ""
            }
            empIds = uns.findAll { e -> e.unitId.equals(it.unitId) && "karmand".equals(e.role) }
            ele.employee = employees.findAll { e -> empIds?.employeeId?.contains(e.employeeId) }
            ele.count = uns.findAll { e -> e.unitId == it.unitId && !"modir".equals(e.role) }.unique { e -> e.employeeId }?.size() ?: ""
            ele.description = it.description ?: ""
            units.add(ele)
        }
        state = 1
    }
    catch (Exception e) {
        e.printStackTrace()
        state = 0
    }
}

def update_unit() {

    this.edit_unit(unitId, ["employeeId": modirEmployeeId], name, description, category, holidayConf, shiftType, null)
}

def update_unit_v2() {

    step = ec.entity.find("Step").condition([username: ec.user.username]).one()
    if (!step) {
        description = ec.l10n.toPersianLocale("noStepFound")
        state = 0
        return
    }
    this.edit_unit(unitId, modir, name, description, category, holidayConf, shiftType, employee)

}

def edit_unit(unitId, modir, name, desc, category, holidayConf = "N", shiftType = "general", employee) {

    unit = ec.entity.find("Unit").condition(["unitId": unitId]).one()
    if (!unit) {
        description = ec.l10n.toPersianLocale("noUnitFound")
        state = 0
        return
    }
    modirEmployeeId = modir?.employeeId
    orgId = unit?.orgId
    orgName = ec.entity.find("Organization").condition([orgId: unit?.orgId]).one()?.name
    call = ec.service.sync().name("general.general.get#connection").call()
    state = call.state
    if (state != 1) {
        description = ec.l10n.toPersianLocale("sqlConError")
        throw new Exception(description)
    }
    sql = call?.sql
    unitName = orgName + "-" + name
    query = "UPDATE GROUPS SET TITLE=? WHERE GRP_NO=?"
    unitName = orgName + "-" + name
    sql.executeInsert(query, [unitName, unitId])
    data = [unitId: unitId, name: name, category: category, description: desc]
    if (description) data.desc = desc
    ec.service.sync().name("update#Unit").parameters(data).call()
    currentModir = ec.entity.find("EmplUnit").condition([unitId: unitId, role: "modir"]).one()
    println(currentModir)
    println(modirEmployeeId)
    println("rrrrrrrrrrrrrrrrrrrrrrrr")

    if (currentModir && !modirEmployeeId) {
        delete_modir(currentModir)
    }
    if (!currentModir && modirEmployeeId) {
        create_modir(modirEmployeeId, unitId, orgId)
    }
    if (currentModir && modirEmployeeId) {
        currentEmplId = ec.entity.find("Employee").condition([employeeId: currentModir?.employeeId, orgId: orgId]).one()?.employeeId
        if (currentEmplId != modirEmployeeId) {
            delete_modir(currentModir)
            create_modir(modirEmployeeId, unitId, orgId)
        }
    }
    if (employee != null) {
        currentEmployee = ec.entity.find("EmplUnit").condition([unitId: unitId, role: "karmand"]).list()
        deleteEmployee = currentEmployee.findAll { e -> !employee.employeeId.contains(e.employeeId) }
        addEmployee = employee.findAll { e -> !currentEmployee.employeeId.contains(e.employeeId) }

        empls = ec.entity.find("EmplUnit").condition([employeeId: addEmployee.employeeId, role: "karmand"]).list().findAll { e -> e.unitId != unitId }
        if (empls.size() > 0) {
            description = ec.l10n.toPersianLocale("karmandError")
            ec.transaction.rollback(descriptin, new Exception())
            state = 0
            return
        }
        deleteEmployee.each { e ->
            ec.service.sync().name("delete#EmplUnit").parameters([emplUnitId: e.emplUnitId]).call()
        }
        addEmployee.each { e ->
            ec.service.sync().name("create#EmplUnit").parameters([employeeId: e.employeeId, unitId: unitId, role: "karmand"]).call()
        }
    }
    state = 1
    description = ec.l10n.toPersianLocale("successEditUnit")

}

def delete_modir(currentModir) {
    ec.entity.find("EmplUnit").condition([emplUnitId: currentModir?.emplUnitId]).deleteAll()
    employeeId = ec.entity.find("Employee").condition([employeeId: currentModir?.employeeId]).one()?.employeeId
    modirUser = ec.entity.find("UserAccount").condition([employeeId: employeeId]).one()?.userId
    companyModir = org.moqui.util.SystemBinding.getPropOrEnv('companyModir')
    if (modirUser) ec.entity.find("UserGroupMember").condition([userId: modirUser, userGroupId: companyModir]).deleteAll()
}

def create_modir(modirEmployeeId, unitId, orgId) {
    println "shittttttttttttt"
    try {
        cu = ec.entity.find("EmplUnit").condition([employeeId: modirEmployeeId, role: "modir", unitId: unitId]).one()
        if (!cu) ec.service.sync().name("create#EmplUnit").parameters([employeeId: modirEmployeeId, unitId: unitId, role: "modir"]).call()
        modirUser = ec.entity.find("UserAccount").condition([employeeId: modirEmployeeId]).one()?.userId
        companyModir = org.moqui.util.SystemBinding.getPropOrEnv('companyModir')
        if (modirUser) {
            ug = ec.entity.find("UserGroupMember").condition([userId: modirUser, userGroupId: companyModir]).one()
            if (!ug) ec.service.sync().name("create#UserGroupMember").parameters([userGroupId: companyModir, userId: modirUser, fromDate: new Date()]).call()
        }
    }
    catch (Exception e) {
        e.printStackTrace()
    }
}

def add_employee(role, employeeId, unitId) {

    try {
        if ("karmand".equals(role)) {
            karmand = ec.entity.find("EmplUnit").condition([employeeId: employeeId, role: "karmand"]).one()
            if (karmand) {
                description = ec.l10n.toPersianLocale('karmandError')
                state = 0
                return
            }
        } else if ("modir".equals(role)) {
            modirRel = ec.entity.find("EmplUnit").condition([unitId: unitId, role: "modir"]).one()
            if (modirRel) {
                empl = ec.entity.find("Employee").condition([employeeId: modirRel?.employeeId, role: "modir"]).one()
                modir = ec.entity.find("UserAccount").condition([employeeId: empl.employeeId]).one()?.userFullName
                description = ec.l10n.toPersianLocale('modirPrefixError') + " " + modir + " " + ec.l10n.toPersianLocale('modirSuffixError')
                return [state: 0, description: description]
            }
        }
        ec.service.sync().name("create#EmplUnit").parameters([employeeId: employeeId, unitId: unitId, role: role]).call()
        return [state: 1]
    }
    catch (Exception e) {
        e.printStackTrace()
        return [state: 0]

    }

}


def get_manager() {
    modir = ec.entity.find("EmplUnit").condition([unitId: unitId, role: role]).one()?.employeeId
    if (!modir) {
        state = 0
        return;
    }
    manager = ec.entity.find("UserAccount").condition([employeeId: modir]).one()?.username
    state = 1

}

def get_employee() {

    uns = ec.entity.find("EmplUnit").condition([unitId: unitId]).list()
    empls = ec.entity.find("Employee").condition([employeeId: uns?.employeeId]).list()
    employee = []
    uns.each { e ->
        it = [:]
        emp = empls.find { en -> en.employeeId == e.employeeId }
        it.name = emp?.name
        it.family = emp?.family
        it.roleDescription = e?.rl?.description
        it.role = e?.role
        it.unitId = e?.unitId
        it.orgId = empl?.orgId
        it.emplUnitId = e.emplUnitId
        employee.add(it)
    }


}