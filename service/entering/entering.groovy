import org.moqui.entity.EntityCondition

import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

def searchEntering() {

    toDate = toDate.replaceAll("/", "-")
    fromDate = fromDate.replaceAll("/", "-")
    endDate = java.sql.Date.valueOf(LocalDate.parse(toDate, DateTimeFormatter.ofPattern("yyyy-MM-dd")))
    startDate = java.sql.Date.valueOf(LocalDate.parse(fromDate, DateTimeFormatter.ofPattern("yyyy-MM-dd")))
    entering = ec.entity.find("Entering").condition([username: username]).condition(["date", EntityCondition.LESS_THAN, endDate]).condition(["date", EntityCondition.GREATER_THAN, startDate]).list()
    enteringList = []
    entering.each { e ->
        t = [:]
        t.date = e.date
        t.time = e.time
        t.locationName = e.locationName
        t.type = "in".equals(e.dataType) ? "ورود" : "خروج"
        t.geoPoint = e.geoPoint
        enteringList.add(t)
    }
    state = 1

}

def create_request_limit() {
    data = [:]
    if (!orgId) {
        org = ec.entity.find("Organization").condition([admin: ec.user.userAccount?.employeeId]).one()
        if (!org) {
            state = 0;
            description = ec.l10n.toPersianLocale("noAdminError")
            return;
        }
        data.orgId = org?.orgId
    }
    if ("enteringCorrection".equals(type)) data.correctionLimit = value
    if ("tashvighi".equals(type)) data.tashvighiLimit = value
    ec.service.sync().name("update#Organization").parameters(data).call()

}

def delete_request_limit() {
    data = [:]
    if (!orgId) {
        org = ec.entity.find("Organization").condition([admin: ec.user.userAccount?.employeeId]).one()
        if (!org) {
            state = 0;
            description = ec.l10n.toPersianLocale("noAdminError")
            return;
        }
        data.orgId = org?.orgId
    }
    if ("enteringCorrection".equals(type)) data.correctionLimit = null
    if ("tashvighi".equals(type)) data.tashvighiLimit = null
    ec.service.sync().name("update#Organization").parameters(data).call()

}

def get_request_limit() {

    if (!orgId) {
        org = ec.entity.find("Organization").condition([admin: ec.user.userAccount?.employeeId]).one()
        if (!org) {
            state = 0;
            description = ec.l10n.toPersianLocale("noAdminError")
            return;
        }
        orgId = org?.orgId
    }
    org = ec.entity.find("Organization").condition([orgId: orgId]).one()
    tashvighi = [type: "tashvighi", value: org?.tashvighiLimit]
    enteringCorrection = [type: "enteringCorrection", value: org?.correctionLimit]
    requestLimit = [tashvighi, enteringCorrection]
    state = 1
}

def get_attendance() {

    url = "Users/Mobile/" + mobile + "/CreateUser"
    serviceCall = ec.service.sync().name("api.api.execute#post").parameters([url: url, postData: [:]]).call()
    if (serviceCall?.state != 1) {
        userCode = serviceCall?.data?.data?.user_code;
    } else {
        state = 0
        return;
    }
    serviceCall = ec.service.sync().name("general.general.get#authToken").call()
    state = serviceCall?.state
    if (!state.toString().equals("1")) {
        return;
    }
    token = serviceCall?.token
    url = "/EnteringSystem/Users/" + userCode + "/Karkard?start_date=" + startDate + "&amp;end_date=" + endDate
    serviceCall = ec.service.sync().name("api.api.execute#get").parameters([url: url, version: 'v3', token: token]).call()
    description = serviceCall?.description
    state = serviceCall?.state
    if (!state?.toString()?.equals("1")) {
        return;
    }
    karkard = serviceCall?.data?.data
    karkard = karkard?.reverse()
    url = "/EnteringSystem/Users/" + userCode + "/Karnameh?start_date=" + startDate + "&amp;end_date=" + endDate
    serviceCall = ec.service.sync().name("api.api.execute#get").parameters([url: url, version: 'v2', token: token]).call()
    state = serviceCall?.state
    description = serviceCall?.description
    report = serviceCall?.data?.data

}

def get_user_attendance() {

    username = ec.user.username
    karkardCall = ec.service.sync().name("entering.karkard.get#karkard").parameters([username: ec.user.username, startDate: startDate, endDate: endDate]).call()
    if (karkardCall.state != 1) {
        description = karkardCall?.description
        state = 0
        return;
    }
    karkard = karkardCall?.data
    karnameCall = ec.service.sync().name("entering.karkard.get#karnameh").parameters([username: ec.user.username, startDate: startDate, endDate: endDate]).call()
    if (karnameCall.state != 1) {
        description = karnameCall?.description
        state = 0
        return;
    }
    report = karnameCall?.data
    println(karkard)
    println(report)
}

def get_daily_attendance() {

    data = [:]
    data.users = users
    data.date = date
    serviceCall = ec.service.sync().name("general.general.get#authToken").call()
    state = serviceCall?.state
    if (!state.toString().equals("1")) {
        return;
    }
    url = "/EnteringSystem/Users/Karkard"
    token = serviceCall?.token
    serviceCall = ec.service.sync().name("api.api.execute#post").parameters([token: token, postData: data, url: url, version: 'v3']).call()
    description = serviceCall?.description
    state = serviceCall?.state
    karkard = serviceCall?.data?.data

}

def get_karkard() {

    data = [:]
    data.users = users
    data.start_date = startDate?.replaceAll("-", "/")
    data.end_date = endDate?.replaceAll("-", "/")
    serviceCall = ec.service.sync().name("general.general.get#authToken").call()
    state = serviceCall?.state
    if (!state.toString().equals("1")) {
        return;
    }
    url = "/EnteringSystem/Users/Karnameh"
    token = serviceCall?.token
    serviceCall = ec.service.sync().name("api.api.execute#post").parameters([token: token, postData: data, url: url, version: 'v3']).call()
    description = serviceCall?.description
    state = serviceCall?.state
    karkard = serviceCall?.data?.data

}

def create_confirm() {

    if (!orgId) {
        org = ec.entity.find("Organization").condition([admin: ec.user.userAccount?.employeeId]).one()
        if (!org) {
            state = 0;
            description = ec.l10n.toPersianLocale("noAdminError")
            return;
        }
        orgId = org?.orgId
    }
    confirmId = ec.service.sync().name("create#Confirm").parameters(context).call()?.confirmId

}

def delete_confirm() {

    ec.service.sync().name("delete#Confirm").parameters([confirmId: confirmId]).call()

}

def get_confirm() {

    hasConfirm = false;
    employeeId = ec.entity.find("UserAccount").condition([username: username]).one()?.employeeId
    if (!employeeId) return
    emp = ec.entity.find("Employee").condition([employeeId: employeeId]).one()
    orgId = emp.orgId
    if (!orgId) {
        state = 1
        return
    }
    con = ec.entity.find("Confirm").condition([orgId: orgId, process: process]).one()
    unitId = con?.unitId
    role = con?.role
    if (role && unitId) {
        hasConfirm = true;
    }

}

def search_confirm() {

    if (!orgId) {
        org = ec.entity.find("Organization").condition([admin: ec.user.userAccount?.employeeId]).one()
        if (!org) {
            state = 0;
            return;
        }
        orgId = org?.orgId
    }
    confirm = ec.entity.find("Confirm").condition([orgId: orgId]).list()

}

def create_request() {

    try {
        if (username && !employeeId) {
            user = ec.entity.find("moqui.security.UserAccount").condition(username: username).one()
            if (!user) {
                state = 0;
                return;
            }
            employeeId = user?.employeeId;
        }
        if (!username && !employeeId) employeeId = ec.user.userAccount?.employeeId
        startDate = java.sql.Date.valueOf(LocalDate.now(ZoneId.of("Asia/Tehran")))
        serviceCall = ec.service.sync().name("create#Request").parameters(context + [startDate: startDate, employeeId: employeeId]).call()
        requestId = serviceCall?.requestId
        state = 1;
    }
    catch (Exception e) {
        e.printStackTrace()
        state = 0
    }
}

def update_request() {
    try {
        data = [:]
        data.requestId = requestId
        if (endDate) data.endDate = java.sql.Date.valueOf(LocalDate.now(ZoneId.of("Asia/Tehran")))
        if (assignee) data.assignee = assignee
        if (description) data.description = description
        if (status) data.status = status
        ec.service.sync().name("update#Request").parameters(data).call()
    }
    catch (Exception e) {
        e.printStackTrace()
        state = 0
    }
}

def get_report_config() {
    try {
        if (!orgId) {
            org = ec.entity.find("Organization").condition([admin: ec.user.userAccount?.employeeId]).one()
            if (!org) {
                state = 0;
                return;
            }
            orgId = org?.orgId
        }
        config = ec.entity.find('ReportConfig').condition([orgId: orgId]).one()
    }
    catch (Exception e) {
        e.printStackTrace()
        state = 0
    }
}

def create_entering_limit() {
    try {
        if (!orgId) {
            org = ec.entity.find("Organization").condition([admin: ec.user.userAccount?.employeeId]).one()
            if (!org) {
                state = 0;
                return;
            }
            orgId = org?.orgId
        }
        url = 'EnteringSystem/EnteringLimit'
        data = [:];
        data.person_type = "person";
        data.month = month
        data.year = year;
        data.org_id = orgId;
        data.start_date = startDate
        data.end_date = endDate
        data.expire_date = expireDate
        serviceCall = ec.service.sync().name("api.api.execute#post").parameters([token: token, url: url, postData: data, version: "v3"]).call()
        state = serviceCall.state
        description = serviceCall.description
        limitId = serviceCall?.data?.data?.id
    }
    catch (Exception e) {
        e.printStackTrace()
        state = 0;
    }
}

def update_entering_limit() {
    try {
        if (!orgId) {
            org = ec.entity.find("Organization").condition([admin: ec.user.userAccount?.employeeId]).one()
            if (!org) {
                state = 0;
                description = ec.l10n.toPersianLocale("noAdminError")
                return;
            }
            orgId = org?.orgId
        }
        url = 'EnteringSystem/EnteringLimit/' + limitId
        data = [:];
        data.month = month
        data.person_type = "person"
        data.year = year;
        data.org_id = orgId
        data.start_date = startDate
        data.end_date = endDate
        data.expire_date = expireDate
        serviceCall = ec.service.sync().name("api.api.execute#put").parameters([url: url, postData: data, version: "v3"]).call()
        state = serviceCall.state
        description = serviceCall.description
    }
    catch (Exception e) {
        e.printStackTrace()
        state = 0;
    }
}

def get_entering_limit() {
    try {
        if (!orgId) {
            org = ec.entity.find("Organization").condition([admin: ec.user.userAccount?.employeeId]).one()
            if (!org) {
                state = 0;
                description = ec.l10n.toPersianLocale("noAdminError")
                return;
            }
            orgId = org?.orgId
        }
        url = 'EnteringSystem/EnteringLimit?person_type=person&amp;&amp;org_id=' + orgId
        if (year) url = url + "&amp;&amp;year=" + year
        token = org.moqui.util.SystemBinding.getPropOrEnv('_systemToken')
        serviceCall = ec.service.sync().name("api.api.execute#get").parameters([url: url, version: "v3", token: token]).call()
        entering = serviceCall?.data?.data
        state = serviceCall.state
        description = serviceCall.description
    }
    catch (Exception e) {
        e.printStackTrace()
        description = ec.l10n.toPersianLocale("generalError")
        state = 0;
    }
}

def delete_entering_limit(){
    try{
        url='EnteringSystem/EnteringLimit/'+limitId
        serviceCall=ec.service.sync().name("api.api.execute#delete").parameters([url:url,version:"v3"]).call()
        state=serviceCall.state
        description=serviceCall.description
    }
    catch(Exception e){
        state=0;
    }
}

def get_entering_access(){
    try{
        if(!orgId){
            org=ec.entity.find("Organization").condition([admin:ec.user.userAccount?.employeeId]).one()
            if(!org){
                description=ec.l10n.toPersianLocale("noAdminError")
                state=0;
                return;
            }
            orgId=org?.orgId
        }
        url='EnteringSystem/EnteringAccess?org_id='+orgId+'&amp;&amp;client_id='+org.moqui.util.SystemBinding.getPropOrEnv("hamkarClientId")
        if(username)url=url+"&amp;&amp;user_name="+username
        serviceCall=ec.service.sync().name("api.api.execute#get").parameters([version:"v3",token:token,url:url]).call()
        access=serviceCall?.data?.data
        state=serviceCall.state
        description=serviceCall.description
    }
    catch(Exception e){
        e.printStackTrace()
        state=0;
    }
}

def create_entering_access(){
    try{
        if(!orgId){
            org=ec.entity.find("Organization").condition([admin:ec.user.userAccount?.employeeId]).one()
            if(!org){
                state=0;
                return;
            }
            orgId=org?.orgId
        }
        url='EnteringSystem/EnteringAccess'
        data=[:];
        data.users=users
        data.org_id=orgId
        data.client_id=org.moqui.util.SystemBinding.getPropOrEnv("hamkarClientId")
        serviceCall=ec.service.sync().name("api.api.execute#post").parameters([version:"v3",token:token,url:url,postData:data]).call()
        state=serviceCall.state
        description=serviceCall.description
    }
    catch(Exception e){
        e.printStackTrace()
        state=0;
    }
}

def delete_entering_access(){
    try{
        url="EnteringSystem/Users/"+userCode+"/EnteringAccess"
        serviceCall=ec.service.sync().name("api.api.execute#delete").parameters([url:url,version:"v3"]).call()
        state=serviceCall?.state
        description=serviceCall?.description
    }
    catch(Exception e){
        state=0
    }
}