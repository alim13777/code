import groovy.json.JsonSlurper

def create_correction() {

    converter = new JsonSlurper()
    startTime = converter.parseText(startTime).join(",")
    endTime = converter.parseText(endTime).join(",")
    enteringId = ec.service.sync().name("create#entering.EnteringCorrection").parameters([date: date, description: description, startTime: startTime, endTime: endTime, requestId: requestId]).call()?.enteringId

}

def update_correction() {

    converter = new JsonSlurper()
    startTime = converter.parseText(startTime).join(",")
    endTime = converter.parseText(endTime).join(",")
    ec.service.sync().name("update#entering.EnteringCorrection").parameters([date: date, description: description, startTime: startTime, endTime: endTime, enteringId: enteringId]).call()

}

def register_correction() {
    try {
        serviceCall = ec.service.sync().name("general.general.get#authToken").parameters([clientId: clientId]).call()
        if (serviceCall.state != 1) {
            state = 0
            description = serviceCall?.description
            return;
        }
        token = serviceCall?.token
        url = 'EnteringSystem/Users/' + userCode + '/Entering'
        data = [:]
        data.userCode = userCode
        data.date = date;
        data.current_time = currentTime;
        data.new_time = newTime;
        serviceCall = ec.service.sync().name("api.api.execute#put").parameters([url: url, postData: data, token: token]).call()
        state = serviceCall.state
        description = serviceCall.description
    }
    catch (Exception e) {
        e.printStackTrace()
        state = 0;
        description = ec.l10n.toPersianLocale("namaError")
    }
}

def create_hourly() {

    hourlyId = ec.service.sync().name("create#Hourly").parameters([date: date, description: description, hourlyRequestId: hourlyRequestId, startTime: startTime, endTime: endTime, location: location, requestId: requestId]).call()?.hourlyId

}

def update_hourly() {

    ec.service.sync().name("update#Hourly").parameters([date: date, description: description, startTime: startTime, endTime: endTime, location: location, hourlyId: hourlyId]).call()

}

def register_hourly() {
    try {
        serviceCall = ec.service.sync().name("general.general.get#authToken").parameters([clientId: clientId]).call()
        if (!"1".equals(serviceCall.state.toString())) {
            state = "0"
            return;
        }
        token = serviceCall?.token
        url = 'EnteringSystem/Users/' + userCode + '/RegisterHourlyRequest'
        data = [:]
        data.userCode = userCode
        data.hourly_request_id = hourlyRequestId;
        data.hourly_request_type = type;
        serviceCall = ec.service.sync().name("api.api.execute#post").parameters([url: url, postData: data, token: token]).call()
        state = serviceCall.state
        description = serviceCall.description
    }
    catch (Exception e) {
        e.printStackTrace()
        state = 0;
        description = ec.l10n.toPersianLocale("namaError")
    }
}

def get_hourly_range() {
    try {
        if (!userCode) {
            user = ec.entity.find("moqui.security.UserAccount").condition([userId: ec.user.userId]).one()
            userCode = user?.userCode
            if (!userCode || userCode == "" || userCode.equalsIgnoreCase("null") || userCode == null || userCode == "" || userCode.length() < 6) {
                url = "Users/Mobile/" + ec.user.username + "/CreateUser"
                serviceCall = ec.service.sync().name("api.api.execute#post").parameters([url: url, postData: [:]]).call()
                if (serviceCall?.state != 1) {
                    userCode = serviceCall?.data?.data?.user_code;
                }
            }
        }
        url = 'EnteringSystem/Users/' + userCode + '/HourlyRequestValidRange?date=' + date
        serviceCall = ec.service.sync().name("api.api.execute#get").parameters([url: url]).call()
        state = serviceCall.state
        validRange = serviceCall.data?.data?.valid_range
        description = serviceCall.description
    }
    catch (Exception e) {
        state = 0;
    }
}

def create_mission() {

    converter = new JsonSlurper()
    dateStr = converter.parseText(date).join(",")
    missionId = ec.service.sync().name("store#Mission").parameters([date: dateStr, description: description, date: dateStr, requestId: requestId, location: location, startTime: startTime, endTime: endTime]).call().missionId


}

def update_mission() {

    converter = new JsonSlurper()
    date = converter.parseText(date).join(",")
    ec.service.sync().name("store#Mission").parameters([location: location, missionId: missionId, date: date, description: description, startTime: startTime, endTime: endTime]).call()

}

def create_vacation() {
    try {
        if (token) token = "Bearer " + token;
        serviceCall = ec.service.sync().name("general.general.get#appSetting").parameters([token: token, packageName: "ir.sshb.hamrazm"]).call()
        if (serviceCall?.state != 1) {
            state = 0
            return;
        }
        vacationWithFile = serviceCall?.setting?.vacation_id_with_attachment
        vacationWithFile = vacationWithFile.collect { ele -> ele.toString() }
        if (vacationWithFile.indexOf(type) == -1) fileId = null;
        converter = new JsonSlurper()
        date = converter.parseText(date).join(",")
        serviceCall = ec.service.sync().name("create#entering.Vacation").parameters([date: date, description: description, requestId: requestId, type: type, file: fileId]).call()
        vacationId = serviceCall.vacationId
        state = 1;
    }
    catch (Exception e) {
        state = 0;
    }
}

def update_vacation() {
    try {
        khatamFileId = ""
        notFile = false
        serviceCall = ec.service.sync().name("general.general.get#appSetting").parameters([token: token, packageName: "ir.sshb.hamrazm"]).call()
        if (serviceCall?.state != 1) {
            state = 0
            return;
        }
        vacationWithFile = serviceCall?.setting?.vacation_id_with_attachment
        vacationWithFile = vacationWithFile.collect { ele -> ele.toString() }
        if (vacationWithFile.indexOf(type) == -1) {
            fileId = null
            notFile = true
        }
        converter = new JsonSlurper()
        date = converter.parseText(date).join(",")
        data = [:]
        data.vacationId = vacationId
        if (date) data.date = date
        if (description) data.description = description
        if (description) data.description = description
        if (type) data.type = type
        if (fileId) data.file = fileId
        if (notFile) data.file = ""
        ec.service.sync().name("update#Vacation").parameters(data).call()
        state = 1;
    }
    catch (Exception e) {
        state = 0;
        description = ec.l10n.toPersianLocale("namaError")
    }
}

def get_vacation_type() {
    url = "Apps/DailyVacationMissionList"
    serviceCall = ec.service.sync().name("api.api.execute#get").parameters([url: url]).call()
    state = serviceCall.status
    description = serviceCall.description;
    vacationList = serviceCall?.data?.data
    vacationList = vacationList.findAll { ele -> ele.id != 54 }
}

def register_daily_request() {
    try {
        serviceCall = ec.service.sync().name("general.general.get#authToken").parameters([clientId: clientId]).call()
        if (serviceCall.state != 1) {
            state = 0
            return;
        }
        token = serviceCall?.token
        url = 'EnteringSystem/Users/' + userCode + '/RegisterDailyRequest'
        data = [:]
        data.userCode = userCode
        data.daily_request_dates = date;
        data.daily_request_type_id = type;
        data.description = description
        serviceCall = ec.service.sync().name("api.api.execute#post").parameters([version: "v2", url: url, postData: data, token: token]).call()
        state = serviceCall.state
        description = serviceCall.description
    }
    catch (Exception e) {
        e.printStackTrace()
        state = 0;
        description = ec.l10n.toPersianLocale("namaError")
    }
}

def get_vacation_page() {
    try {
        serviceCall = ec.service.sync().name("entering.request.get#vacationType").call()
        vacation = serviceCall?.vacationList
        state = serviceCall.state
        description = serviceCall?.description
    }
    catch (Exception e) {
        e.printStackTrace()
        state = 0
    }
}

def get_mission_page() {
    try {
        serviceCall = ec.service.sync().name("general.general.get#location").parameters([inputValue: 1, input: "country_id", output: "province"]).call()
        location = serviceCall?.location
        state = serviceCall?.state
        description = serviceCall?.description
    }
    catch (Exception e) {
        state = 0
    }
}

def get_request_page() {
    try {
        serviceList = ec.entity.find("moqui.basic.Enumeration").condition(["enumTypeId": "Entering"]).list()
        statusList = ec.entity.find("moqui.basic.StatusItem").condition("statusTypeId", "Entering").list()
        userEmployeeId = ec.user.userAccount?.employeeId
        state = 1
    }
    catch (Exception e) {
        e.printStackTrace()
        state = 0
    }
}

def get_limit_page() {

    limitType = ec.entity.find("Enumeration").condition([enumTypeId: 'RequestLimitType']).list()

}

def get_notification_page() {
    try {
        organization = ec.entity.find("Organization").list()
        state = serviceCall?.state
    }
    catch (Exception e) {
        state = 0
    }
}

def get_request_limit_page() {
    try {
        if (!orgId) {
            org = ec.entity.find("Organization").condition([admin: ec.user.userAccount?.employeeId]).one()
            if (!org) {
                state = 0;
                return;
            }
            orgId = org?.orgId
        }
        org = ec.entity.find("Organization").condition([orgId: orgId]).one()
        emps = ec.entity.find("Employee").condition([orgId: orgId]).list()?.employeeId
        users = ec.entity.find("UserAccount").condition([employeeId: emps]).collect { ele -> ["userFullName": ele.userFullName, "userCode": ele.userCode, "username": ele.username] }
        units = ec.entity.find("Unit").condition([orgId: orgId]).list()
        role = ec.entity.find("Role").list()
        state = 1
    }
    catch (Exception e) {
        e.printStackTrace()
        state = 0
    }
}