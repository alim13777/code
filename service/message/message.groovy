def send_notification() {

    data = [:]
    data.url = url;
    data.body = body;
    data.title = title;
    data.users = users;
    data.date = date;
    if (client == "hamkar") data.client_id = org.moqui.util.SystemBinding.getPropOrEnv('hamkarClientId')
    if (client == "hamkarWeb") data.client_id = org.moqui.util.SystemBinding.getPropOrEnv('hamkarWebClientId')
    url = "/EnteringSystem/Notifications"
    serviceCall = ec.service.sync().name("api.api.execute#post").parameters([postData: data, url: url, version: "v3"]).call()
    state = serviceCall?.state
    description = serviceCall?.description

}

def get_notification() {

    url = "EnteringSystem/Notifications"
    serviceCall = ec.service.sync().name("api.api.execute#get").parameters([url: url, version: "v3"]).call()
    state = serviceCall?.state
    notification = serviceCall?.data?.data.findAll { e -> e.username == ec.user.username }

}

def get_notification_users() {

    url = "EnteringSystem/Notifications/" + notificationId + "/Users";
    serviceCall = ec.service.sync().name("api.api.execute#get").parameters([url: url, version: "v3"]).call()
    state = serviceCall?.state
    users = serviceCall?.data?.data

}

def send_notification_excel() {

    serviceCall = ec.service.sync().name("general.general.read#excel").parameters([fileId: fileId]).call()
    state = serviceCall?.state
    if (state != 1) {
        return;
    }
    data = serviceCall?.data
    textList = text.split("")
    keys = data[0].keySet().toList()
    data.each { it ->
        body = "";
        textList.eachWithIndex { ele, idx ->
            if (ele == "#") {
                val = Integer.valueOf(textList[idx + 1]) - 1
                textList[idx] = it[keys[val]]
            }
        }
        it.body = textList.join("")
    }
    postData = [:]
    postData.notifs = data
    postData.title = title
    if (client == "hamrazm") postData.client_id = org.moqui.util.SystemBinding.getPropOrEnv('hamrazmClientId')
    if (client == "arman") postData.client_id = org.moqui.util.SystemBinding.getPropOrEnv('armanClientId')
    url = "EnteringSystem/Notifications/List"
    serviceCall = ec.service.sync().name("api.api.execute#post").parameters([postData: postData, url: url, version: "v3"]).call()
    state = serviceCall?.state
    description = serviceCall?.description

}

def receive_notification_file() {

    serviceCall = ec.service.sync().name("general.file.create#physicalFile").parameters([file: file]).call()
    fileId = serviceCall?.fileId
    serviceCall = ec.service.sync().name("general.general.read#excel").parameters([fileId: fileId]).call()
    data = serviceCall?.data
    keys = data[0].keySet()
    state = 1
}

def read_excel() {

    serviceCall = ec.service.sync().name("general.file.create#physicalFile").parameters([file: file]).call()
    fileId = serviceCall?.fileId
    serviceCall = ec.service.sync().name("general.general.read#excel").parameters([fileId: fileId]).call()
    data = serviceCall?.data
    state = 1

}

def get_notification_page() {

    isAdmin = false;
    groups = ec.entity.find("UserGroupMember").condition([userId: ec.user.userId]).list()?.userGroupId
    if (groups.contains("ADMIN")) {
        isAdmin = true
        orgs = ec.entity.find("Organization").list()
        units = ec.entity.find("Unit").list()
    } else {
        employeeId = ec.entity.find("UserAccount").condition([userId: ec.user.userId]).one()?.employeeId
        orgs = ec.entity.find("Organization").condition([admin: employeeId]).list()
        units = ec.entity.find("Unit").condition([orgId: orgs?.orgId]).list()
    }
}