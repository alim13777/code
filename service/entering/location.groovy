def get_user_location() {


    data = [isDeleted: 0]
    grps = ec.entity.find("UserGroupMember").condition([userId: ec.user.userId, userGroupId: "ADMIN"]).one()
    organizations = ec.entity.find("Organization").list()
    if (!grps) {
        org = ec.entity.find("Organization").condition([admin: ec.user.userAccount?.employeeId]).list()
        if (org.size()==0) {
            description = ec.l10n.toPersianLocale("noAdminError")
            state = 0
            return
        }
        data.orgId = org?.orgId
    }
    locs = ec.entity.find("Location").condition(data).list()
    locationList = ec.entity.find("UserLocation").condition([locationId: locs?.locationId]).list()
    employees = ec.entity.find("Employee").condition([employeeId: locationList?.employeeId]).list()
    users = ec.entity.find("UserAccount").condition([employeeId: employees?.employeeId]).list()
    locations = locationList.groupBy { ele -> ele.employeeId }
    location = []
    locations.each { key, value ->
        entry = [:]
        empl = employees.find { e -> e.employeeId == key }
        user = users.find { e -> e.employeeId == key }
        if(user) {
            entry.username = user?.username
            entry.userCaption = empl?.name + " " + empl?.family
            entry.locationNames = value.collect { ele -> ele?.loc?.name }.join(",")
            entry.locations = value
            entry.orgName = organizations.find { e -> e.orgId == value?.get(0)?.loc?.orgId }?.name
            location.add(entry)
        }
    }
    location
}

def get_user_locations() {

    employeeId = ec.entity.find("UserAccount").condition([username: username]).one()?.employeeId
    if (!employeeId) {
        description = ec.l10n.localize("noEmployeeFound")
        state = 0
        return
    }
    uls = ec.entity.find("UserLocation").condition([employeeId: employeeId]).list()
    locationList = uls.loc.findAll { e -> e.isDeleted == 0 }
    locations=[]
    uls.each{e->
        loc=locationList.find{t->t.locationId==e.locationId}
        entry=[:]
        if(loc) {
            entry.name = loc.name
            entry.point = loc.point
            entry.radius = loc.radius
            entry.locationId = loc.locationId
            entry.userLocationId = e.userLocationId
            locations.add(entry)
        }
    }
}

def get_location_page() {

    data=[isDeleted: 0]
    isAdmin = false
    users = []
    groups = ec.entity.find("UserGroupMember").condition(["userId": ec.user.userId]).list()?.userGroupId
    if (groups.contains("ADMIN")) isAdmin = true
    if (isAdmin) {
        users = ec.entity.find("UserAccount").list().collect { ele -> [userFullName: ele.userFullName, username: ele.username] }
        organizations = ec.entity.find("Organization").list()
    } else {
        employeeId = ec.user.userAccount?.employeeId
        organization = ec.entity.find("Organization").condition([admin: employeeId]).one()
        if (!organization) {
            description = ec.l10n.toPersianLocale("noAdminError")
            state = 0
            return
        }
        organizations = [organization]
        orgId = organization?.orgId
        data.orgId=orgId
        if (orgId) {
            empIds = ec.entity.find("Employee").condition([orgId: orgId])?.list()?.employeeId
            users = ec.entity.find("UserAccount").condition([employeeId: empIds])?.list()?.unique { ele -> ele.username }?.collect { ele -> [userFullName: ele.userFullName, username: ele.username] }
        }
    }
    locations = ec.entity.find("Location").condition(data).list()

}

def create_user_location() {

    emps = ec.entity.find("UserAccount").condition([username: users]).list()?.employeeId
    emps.each { e ->
        ec.service.sync().name("create#UserLocation").parameters([locationId: locationId, employeeId: e]).call()
    }
}

def delete_user_location() {
    ec.service.sync().name("delete#UserLocation").parameters([userLocationId: userLocationId]).call()
}

def get_entering_location_page() {
    organizations = []
    isAdmin = false
    groups = ec.entity.find("UserGroupMember").condition(["userId": ec.user.userId]).list()?.userGroupId
    if (groups.contains("ADMIN")) isAdmin = true
    if (!isAdmin) {
        org = ec.entity.find("Organization").condition([admin: ec.user.userAccount?.employeeId]).one()
        if (!org) {
            description = ec.l10n.toPersianLocale("noAdminError")
            state = 0
        }
        organizations.add(org)
    } else {
        organizations = ec.entity.find("Organization").list()
    }

}

def get_entering_location() {

    orgs = ec.entity.find("Organization").list()
    groups = ec.entity.find("UserGroupMember").condition(["userId": ec.user.userId]).list()?.userGroupId
    data = ["isDeleted": 0]
    if (!groups.contains("ADMIN")) {
        user = ec.entity.find("UserAccount").condition([userId: ec.user.userId]).one()
        org = orgs.find { e -> e.admin == user?.employeeId }
        if (!org) {
            description = ec.l10n.toPersianLocale("noAdminError")
            state = 0
            return
        }
        data.orgId = org?.orgId
    }
    location = ec.entity.find("Location").condition(data).list()

}

def delete_entering_location() {

    ec.entity.find("UserLocation").condition([locationId:enteringLocationId]).deleteAll()
    ec.service.sync().name("update#Location").parameters([locationId: enteringLocationId, isDeleted: 1]).call()
}

def update_entering_location() {

    data = [:]
    data.point = point
    data.radius = radius
    data.orgId = orgId
    data.description = description
    data.name = name
    call = ec.service.sync().name("update#Location").parameters(data + [locationId: enteringLocationId]).call()
    currentEmps = ec.entity.find("UserLocation").condition([locationId: enteringLocationId]).list()
    currentUsers = ec.entity.find("UserAccount").condition([employeeId: currentEmps?.employeeId]).list()
    newUsers = users
    deleteUser = currentUsers.findAll { e -> !newUsers.contains(e.username) }
    addUser = newUsers.findAll { e -> !currentUsers.username.contains(e) }
    deleteUser.each { e ->
        rec=currentEmps.find{t->t.employeeId==e.employeeId}
        if(rec) ec.service.sync().name("delete#UserLocation").parameters([userLocationId: rec.userLocationId]).call()
    }
    addEmps = ec.entity.find("UserAccount").condition([username: addUser]).list()?.employeeId
    addEmps.each { e ->
        ec.service.sync().name("create#UserLocation").parameters([locationId: enteringLocationId, employeeId: e]).call()
    }
    description=ec.l10n.toPersianLocale("successEditLocation")
}

def create_entering_location() {

    data = [point: point, orgId: orgId, radius: radius, description: description, name: name,isDeleted: 0]
    add_location(data, [])
    description = ec.l10n.toPersianLocale("generalSuccess")
}

def add_location(data, users) {

    call = ec.service.sync().name("create#Location").parameters(data).call()
    locationId = call.locationId
    empls = ec.entity.find("UserAccount").condition([username: users]).list()
    empls.each { e ->
        if (e.employeeId) {
            ec.service.sync().name("create#UserLocation").parameters([employeeId: e.employeeId, locationId: locationId]).call()
        }
    }
}

def get_user_location_page() {

    groups = ec.entity.find("UserGroupMember").condition(["userId": ec.user.userId]).list()?.userGroupId
    if (!groups.contains("ADMIN")) {
        employeeId = ec.user.userAccount?.employeeId
        org = ec.entity.find("Organization").condition([admin: employeeId]).one()
        if (!org) {
            description = ec.l10n.toPersianLocale("noAdminError")
            state = 0
            return
        }
        orgId = org?.orgId
        units = ec.entity.find("Unit").condition([orgId: orgId]).list()
    } else {
        units = ec.entity.find("Unit").list()
    }
    locations = ec.entity.find("Location").condition([orgId: orgId,isDeleted: 0]).list()

}

def get_entering_location_v2() {


    step = ec.entity.find("Step").condition([username: ec.user.username]).one()
    if (!step) {
        description = ec.l10n.toPersianLocale("noRecordFound")
        state = 0
        return
    }
    orgId = step?.orgId
    groups = ec.entity.find("UserGroupMember").condition(["userId": ec.user.userId]).list()?.userGroupId
    if (!groups.contains("ADMIN")) {
        employeeId = ec.user.userAccount?.employeeId
        org = ec.entity.find("Organization").condition([admin: employeeId]).one()
        if (!org) {
            description = ec.l10n.toPersianLocale("noAdminError")
            state = 0
            return
        }
        orgId = org?.orgId
        units = ec.entity.find("Unit").condition([orgId: orgId]).list()
    } else {
        units = ec.entity.find("Unit").list()
    }
    locations = ec.entity.find("Location").condition([orgId: orgId,isDeleted: 0]).list()
    emps = ec.entity.find("UserLocation").condition([locationId: locations.locationId]).list()
    users = ec.entity.find("UserAccount").condition([employeeId: emps.employeeId]).list()
    location = []
    locations.each { e ->
        it = [:]
        empList = emps.findAll { ele -> e.locationId == ele.locationId }
        userList = users.findAll { ele -> empList?.employeeId?.contains(ele.employeeId) }
        it.name = e.name ?: ""
        it.id = e.locationId ?: ""
        it.description = e.description ?: ""
        it.radius = e.radius ?: ""
        it.point = e.point ?: ""
        it.users = userList.collect { ele -> ["username": ele.username] } ?: []
        location.add(it)
    }

}

def create_entering_location_v2() {

    step = ec.entity.find("Step").condition([username: ec.user.username]).one()
    if (!step) {
        description = ec.l10n.toPersianLocale("noStepFound")
        state = 0
        return
    }
    orgId = step?.orgId
    data = [:]
    data.point = point
    data.orgId = orgId
    data.radius = radius
    data.description = description
    data.isDeleted=0
    data.name = name
    add_location(data, users)

}

def get_entering_location_page_v2() {
    try {
        step = ec.entity.find("Step").condition([username: ec.user.username]).one()
        if (!step) {
            description = ec.l10n.toPersianLocale("noRecordFound")
            state = 0
            return
        }
        orgId = step?.orgId
        employeeId = ec.entity.find("Employee").condition([orgId: orgId]).list()?.employeeId
        userList = ec.entity.find("UserAccount").condition([employeeId: employeeId]).list()
        users = []
        userList.each { e ->
            it = [:]
            it.username = e.username
            it.userFullName = e.userFullName
            users.add(it)
        }
        state = 1
    }
    catch (Exception e) {
        e.printStackTrace()
        state = 0
    }
}

