import org.moqui.entity.EntityCondition
import org.moqui.util.StringUtilities

import java.sql.Timestamp
import java.time.LocalDate
import java.time.ZoneId

def loadPlanUtils() {
    if (!this.plan) {
        path = ec.resource.getLocationReference("component/core/service/customer/plan.groovy").toString().split("file:")[1]
        this.plan = new GroovyShell().parse(new File(path))
    }
}

def signup_user() {

    data = [name: name, family: family, code_meli: national_id]
    url = "Users/Mobile/" + username + "/CreateUser"
    token = org.moqui.util.SystemBinding.getPropOrEnv('_systemToken')
    call = ec.service.sync().name("api.api.execute#post").parameters([url: url, postData: data, token: token, version: "v3"]).call()
    if (call.state != 1) {
        description = call.description
        state = 0
        return
    }
    namaUser = ec.entity.find("UserAccount").condition([username: username]).one()
    userFullName = name + " " + family
    if (!namaUser) {
        emailAddress = username + "@sshb.ir"
        pass = username + '@1A23d!'
        data = [planId: planId, userCode: userCode, username: username, newPassword: pass, newPasswordVerify: pass, userFullName: userFullName, emailAddress: emailAddress, locale: "fa_IR", timeZone: "Asia/Tehran", employeeId: employeeId]
        userId = ec.service.sync().name("create#moqui.security.UserAccount").parameters(data).call()?.userId
    } else {
        userId = namaUser?.userId
        ec.service.sync().name("update#UserAccount").parameters(context).call()
    }
    generalAccess = org.moqui.util.SystemBinding.getPropOrEnv('generalAccess')
    ug = ec.entity.find("UserGroupMember").condition([userId: userId, userGroupId: generalAccess]).one()
    if (!ug) ec.service.sync().name("create#UserGroupMember").parameters([userId: userId, userGroupId: generalAccess, fromDate: new Date()]).call()

}

def validate_otp() {

    url = "Users/" + mobile + "/RegisterUser"
    clientId = org.moqui.util.SystemBinding.getPropOrEnv('hamkarPanelClientId')
    clientName = org.moqui.util.SystemBinding.getPropOrEnv('hamkarPanelClientName')
    data = [client_id: clientId, client_name: clientName, confirm_code: code, scope: "public login personsView personAdmin smsPublic smsSingle nama hamrazm collectionsView"]
    call = ec.service.sync().name("api.api.execute#post").parameters([url: url, postData: data]).call()
    if (call.state != 1) {
        description = call.description
        state = 0
        return
    }
    token = call?.data?.data?.access_token
    username = mobile
    namaUser = ec.entity.find("UserAccount").condition([username: username]).one()
    if (!namaUser) {
        emailAddress = username + "@sshb.ir"
        pass = token
        data = [planId: planId, userCode: userCode, username: username, newPassword: token, newPasswordVerify: token, userFullName: " ", emailAddress: emailAddress, locale: "fa_IR", timeZone: "Asia/Tehran"]
        userId = ec.service.sync().name("create#moqui.security.UserAccount").parameters(data).call()?.userId
        ec.service.sync().name("org.moqui.impl.UserServices.update#PasswordInternal").parameters([userId: userId, newPassword: token, newPasswordVerify: token, requirePasswordChange: "N"]).disableAuthz().call()
    } else {
        userId = namaUser?.userId
        ec.service.sync().name("org.moqui.impl.UserServices.update#PasswordInternal").parameters([userId: userId, newPassword: token, newPasswordVerify: token, requirePasswordChange: "N"]).disableAuthz().call()
    }
    isLogin = ec.user.loginUser(username, token)
    ec.user.setPreference("apiToken", token)
    ec.user.setPreference("basicToken", Base64.getEncoder().encodeToString((username + ":" + token).getBytes()))
    wizardCompleted = true
    stepNum = 0
    step = ec.entity.find("Step").condition([username: username]).one()
    if (!step) {
        stepNum = 0
        isSkip = "N"
        ec.service.sync().name("create#Step").parameters(["username": username, "stepNum": stepNum, isSkip: isSkip]).call()
    } else {
        stepNum = step?.stepNum
        isSkip = step?.isSkip
    }
    if (stepNum < 8) wizardCompleted = false
    generalAccess = org.moqui.util.SystemBinding.getPropOrEnv('generalAccess')
    ug = ec.entity.find("UserGroupMember").condition([userId: userId, userGroupId: generalAccess]).one()
    if (!ug) ec.service.sync().name("create#UserGroupMember").parameters([userId: userId, userGroupId: generalAccess, fromDate: new Date()]).call()
    loginKey = get_login_key(userId)
    loadPlanUtils()
    employeeId = ec.user.userAccount?.employeeId
    hasPlan = false
    if (employeeId) {
        hasPlan = this.plan.get_active_plan(ec.user.userAccount?.employeeId)["hasPlan"]
    }
    state = 1
}

def get_login_key(userId) {
    try {
        loginKey = StringUtilities.getRandomString(40)
        String hashedKey = ec.ecfi.getSimpleHash(loginKey, "", "SHA-256", false)
        int expireHours = ec.ecfi.getLoginKeyExpireHours()
        Timestamp fromDate = new Timestamp(System.currentTimeMillis())
        long thruTime = fromDate.getTime() + (expireHours * 60 * 60 * 1000)
        ec.service.sync().name("create#UserLoginKey").parameters([loginKey: hashedKey, userId: userId, fromDate: fromDate, thruDate: new Timestamp(thruTime)]).call()
        return loginKey
    }
    catch (Exception e) {
        e.printStackTrace()
    }
}

def send_otp() {

    try {
        current = ec.entity.find("UserAccount").condition([username: mobile]).one()
        hasAccount = current != null
        url = "Users/" + mobile + "/RequestRegisterUser"
        packageName = org.moqui.util.SystemBinding.getPropOrEnv('hamkarPanelPackage')
        version = org.moqui.util.SystemBinding.getPropOrEnv('hamkarPanelVersion')
        data = [package_name: packageName, current_version: version]
        call = ec.service.sync().name("api.api.execute#post").parameters([url: url, postData: data]).call()
        description = call.description
        state = call.state
    }
    catch (Exception ignored) {
        state = 0
    }

}

def get_token() {

    ua = ec.entity.find("UserAccount").condition([username: username]).one()
    if (!ua) {
        description = ec.l10n.toPersianLocale("noRecordFound")
        state = 0
        return
    }
    credentials = [:]
    credentials.username = username
    credentials.password = password
    credentials.client_id = org.moqui.util.SystemBinding.getPropOrEnv('hamkarClientId')
    credentials.client_secret = org.moqui.util.SystemBinding.getPropOrEnv('hamkarSecret')
    credentials.grant_type = "password"
    credentials.scope = org.moqui.util.SystemBinding.getPropOrEnv('hamkarScope')

    url = 'Oauth2/PasswordCredentials'
    serviceCall = ec.service.sync().name("api.api.execute#post").parameters([url: url, token: "false", postData: credentials]).call()
    state = serviceCall.state
    description = serviceCall.description
    if (state != 1) {
        return
    }
    token = serviceCall?.data['access_token'].toString().trim()
    ec.service.sync().name("org.moqui.impl.UserServices.update#PasswordInternal").parameters([userId: ua.userId, newPassword: token, newPasswordVerify: token, requirePasswordChange: requirePasswordChange]).disableAuthz().call()
    ec.user.loginUser(username, token)
    ec.user.setPreference("apiToken", token)
    ec.user.setPreference("basicToken", Base64.getEncoder().encodeToString((username + ":" + token).getBytes()))

    return token
}

def create_api_user() {
    try {
        data = [:]
        namaUser = ec.entity.find("UserAccount").condition([username: username]).one()
        data.user_name = username
        data.send_sms = 0
        if (name) data.name = name
        if (family) data.family = family
        if (nationalId) data.code_meli = nationalId
        data.package_name = "hamkar-app.ir"
        data.require_update = 1
        url = 'Users/Mobile/' + username + '/CreateUser'
        token = org.moqui.util.SystemBinding.getPropOrEnv('_systemToken')
        serviceCall = ec.service.sync().name("api.api.execute#post").parameters([postData: data, url: url, token: token]).call()
        state = serviceCall?.state
        description = serviceCall?.description
        if (state != "1") return
        user = serviceCall?.data?.data
        text = ec.l10n.toPersianLocale("greeting") + " " + name + " " + family + " " + ec.l10n.toPersianLocale("friend") + "\n" + ec.l10n.toPersianLocale("createGeneralUser") + "\n" +
                ec.l10n.toPersianLocale("telegramLink") + "\n" +
                ec.l10n.toPersianLocale("baleLink") + "\n" +
                ec.l10n.toPersianLocale("siteLink")
        ec.service.sync().name("create#Sms").parameters([text: text, mobile: username, createTime: new Date()]).call()
        state = 1
    }
    catch (Exception e) {
        e.printStackTrace()
        state = 0
    }
}

def create_api_user_v2(username, password, name, family, nationalId) {
    try {
        data = [:]
        namaUser = ec.entity.find("UserAccount").condition([username: username]).one()
        data.username = username
        if (password) data.password = password
        if (name) data.name = name
        if (family) data.family = family
        if (nationalId) data.national_id = nationalId
        url = 'EnteringSystem/Users'
        token = org.moqui.util.SystemBinding.getPropOrEnv('_systemToken')
        serviceCall = ec.service.sync().name("api.api.execute#post").parameters([postData: data, url: url, token: token, version: "v3"]).call()
        state = serviceCall?.state
        description = serviceCall?.description
        if (state != "1") return
        if (password) {
            text = ec.l10n.toPersianLocale("createUser") + "\n" +
                    ":نام کاربری" + username + "\n" +
                    ":رمز عبور" + password + "\n"
        } else {
            text = ec.l10n.toPersianLocale("greeting") + " " + name + " " + family + " " + ec.l10n.toPersianLocale("friend") + "\n" + ec.l10n.toPersianLocale("createGeneralUser") + "\n" +
                    ec.l10n.toPersianLocale("telegramLink") + "\n" +
                    ec.l10n.toPersianLocale("baleLink") + "\n" +
                    ec.l10n.toPersianLocale("siteLink")
        }
        ec.service.sync().name("create#Sms").parameters([text: text, mobile: username, createTime: new Date()]).call()
        return ["state": 1, userCode: serviceCall?.data?.data?.user_code]
    }
    catch (Exception e) {
        e.printStackTrace()
        state = 0
    }
}

def create_user() {
    try {
        data = [:]
        call = create_api_user_v2(username, password, name, family, nationalId)
        userCode = call?.userCode
        namaUser = ec.entity.find("UserAccount").condition([username: username]).one()
        userFullName = name + " " + family
        emailAddress = username + "@sshb.ir"
        if (!namaUser) {
            pass = username + '@1A23d!'
            data = [userCode: userCode, username: username, newPassword: pass, newPasswordVerify: pass, userFullName: userFullName, emailAddress: emailAddress, locale: "fa_IR", timeZone: "Asia/Tehran", employeeId: employeeId]
            userId = ec.service.sync().name("create#moqui.security.UserAccount").parameters(data).call()?.userId
        } else {
            userId = namaUser?.userId
            ec.service.sync().name("update#UserAccount").parameters(context + [disabled: "N"]).call()
        }
        generalAccess = org.moqui.util.SystemBinding.getPropOrEnv('generalAccess')
        ug = ec.entity.find("UserGroupMember").condition([userId: userId, userGroupId: generalAccess]).one()
        if (!ug) ec.service.sync().name("create#UserGroupMember").parameters([userId: userId, userGroupId: generalAccess, fromDate: new Date()]).call()
        if (isModir) {
            modirAccess = org.moqui.util.SystemBinding.getPropOrEnv('companyModir')
            cu = ec.entity.find("UserGroupMember").condition([userId: userId, userGroupId: modirAccess]).one()
            if (!cu) ec.service.sync().name("create#UserGroupMember").parameters([userId: userId, userGroupId: modirAccess, fromDate: new java.util.Date()]).call()
        }
        if (isAdmin) {
            cur = ec.entity.find("UserGroupMember").condition([userId: userId]).list()?.userGroupId
            generalAccess = org.moqui.util.SystemBinding.getPropOrEnv('generalAccess')
            companyAdmin = org.moqui.util.SystemBinding.getPropOrEnv('companyAdmin')
            if (!cur.contains(generalAccess)) ec.service.sync().name("create#UserGroupMember").parameters([userId: userId, userGroupId: generalAccess, fromDate: new java.util.Date()]).call()
            if (!cur.contains(companyAdmin)) ec.service.sync().name("create#UserGroupMember").parameters([userId: userId, userGroupId: companyAdmin, fromDate: new java.util.Date()]).call()
        }
        state = 1
    }
    catch (Exception e) {
        e.printStackTrace()
        state = 0
    }
}

def get_user() {
    try {
        user = ec.entity.find("UserAccount").condition([username: username]).selectField("userFullName").one()
        state = 1
    }
    catch (Exception e) {
        e.printStackTrace()
        state = 0
    }
}

def get_groups() {
    try {
        userEntity = ec.entity.find("UserAccount").condition("username", EntityCondition.LIKE, "%${username}%").list()
        if (userEntity.size() == 0) {
            state = 0
            description = ec.l10n.toPersianLocale("noUserError")
            return
        }
        if (userEntity.size() > 1) {
            state = 0
            description = ec.l10n.toPersianLocale('moreUserError')
            return
        }
        userObj = userEntity[0]
        user = [
                "username"             : userObj?.username,
                "userFullName"         : userObj?.userFullName,
                "userId"               : userObj?.userId,
                "employeeId"           : userObj?.employeeId,
                "emailAddress"         : userObj?.emailAddress,
                "requirePasswordChange": userObj?.requirePasswordChange,
                "passwordSetDate"      : userObj?.passwordSetDate
        ]
        userGroup = ec.entity.find("moqui.security.UserGroupMember").condition([userId: user.userId]).list()
        group = userGroup.collect { ele -> ["username": userObj?.username, "userId": ele.userId, "fromDate": ele?.fromDate, "thruDate": ele?.thruDate, "description": ele?.group?.description, "userGroupId": ele?.group?.userGroupId] }
        state = 1
    }
    catch (Exception e) {
        e.printStackTrace()
        state = 0
    }
}

def create_groups() {
    try {
        user = ec.entity.find("UserAccount").condition([username: username]).one()
        if (!user) {
            state = 0
            return
        }
        currentGroups = ec.entity.find("UserGroupMember").condition([userId: user?.userId]).list()?.userGroupId
        userGroupList.each { ele ->
            if (!currentGroups.contains(ele)) {
                ec.service.sync().name("create#UserGroupMember").parameters([fromDate: new Date(), userId: user?.userId, userGroupId: ele]).call()
            }
        }
        state = 1
    }
    catch (Exception ignored) {
        state = 0
    }
}

def delete_groups() {
    try {
        user = ec.entity.find("UserAccount").condition([username: username]).one()
        if (!user) {
            state = 0
            return
        }
        ec.service.sync().name("delete#UserGroupMember").parameters([userId: user?.userId, userGroupId: userGroupId]).call()
        state = 1
    }
    catch (Exception ignored) {
        state = 0
    }
}

def check_login() {

    try {
        canLogin = false
        user = ec.entity.find("UserAccount").condition([username: username]).one()
        if (!user) {
            state = 1
            description = ec.l10n.toPersianLocale('userError')
            return
        }
        employeeId = user?.employeeId
        emp = ec.entity.find("Employee").condition([employeeId: employeeId]).one()
        dev = emp?.device
        if (dev && !"AllDevice".equals(dev)) {
            if (!dev.equalsIgnoreCase(device)) {
                state = 1
                description = ec.l10n.toPersianLocale("deviceError")
                return
            }
        }
        orgId = ec.entity.find("Employee").condition([employeeId: employeeId]).one()?.orgId
        if (!orgId) {
            description = ec.l10n.toPersianLocale("employmentError")
            state = 1
            return
        }
        org = ec.entity.find("Organization").condition([orgId: orgId]).one()
        if (!org) {
            description = ec.l10n.toPersianLocale("orgError")
            state = 1
            return
        }
        plan = ec.entity.find("Customer").condition([orgId: org?.orgId, isEnable: "Y"]).one()
        if (!plan) {
            description = ec.l10n.toPersianLocale("orgExpire")
            state = 1
            return
        }
        if (plan.startDate) fromDate = LocalDate.parse(plan.startDate.toString())
        if (plan.endDate) toDate = LocalDate.parse(plan.endDate.toString())
        date = LocalDate.now(ZoneId.of("Asia/Tehran"))
        if (date.equals(fromDate) || date.equals(toDate)) {
            canLogin = true
        } else if (fromDate?.compareTo(date) < 0 && toDate?.compareTo(date) > 0) {
            canLogin = true
        } else {
            description = ec.l10n.toPersianLocale("orgExpire")
        }
        isAdmin = ec.entity.find("UserGroupMember").condition([userId: ec.user.userId, userGroupId: "ADMIN"]).one() ? true : false
        state = 1
    }
    catch (Exception e) {
        description = "generalError"
        e.printStackTrace()
        state = 0
    }
}

def get_expire_users() {
    try {
        orgs = ec.entity.find("Organization").list()
        date = new Date()
        orgs = orgs.findAll { ele -> !(ele.startDate.before(date) && ele.endDate.after(date)) }
        empls = ec.entity.find("Employee").condition([orgId: orgs.orgId]).list()?.employeeId
        users = ec.entity.find("UserAccount").condition([employeeId: empls]).list()?.username
        state = 1
    }
    catch (Exception e) {
        e.printStackTrace()
        state = 0
    }
}

def expire_app() {
    try {
        if (!clientId) clientId = org.moqui.util.SystemBinding.getPropOrEnv('hamkarClientId')
        url = "/Users/Client/" + clientId
        token = org.moqui.util.SystemBinding.getPropOrEnv('_systemToken')
        call = ec.service.sync().name("api.api.execute#post").parameters([postData: [usernames: users], version: "v3", url: url, token: token]).call()
        description = call.description
        state = call.state
    }
    catch (Exception ignored) {
        state = 0
    }
}

def logout_user() {
    ec.user.logoutUser()
}

def search_user() {

    try {
        user = ec.entity.find("UserDetail")
        if (text) {
            user = user.condition("userFullName", EntityCondition.LIKE, "%${text}%")
        }
        if (username) {
            user = user.condition("username", username)
        }
        user = user.selectFields(["userId,employeeId,pelak,userCode,personCode,userFullName,username,disabled"]).list()
        state = 1
    }
    catch (Exception e) {
        state = 0
        e.printStackTrace()
    }
}

def check_user() {

    userId = ec.user.userId
    user = ec.entity.find("UserAccount").condition([userId: userId]).one()
    if (!user) {
        state = 0
        return
    }
    username = user?.username
    userFullName = user?.userFullName
    admin = ec.entity.find("Organization").condition([admin: user?.employeeId]).one()
    wizardCompleted = true
    stepNum = 1
    if (admin) {
        step = ec.entity.find("Step").condition([username: username, orgId: admin?.orgId]).one()
        stepNum = step?.stepNum ? step?.stepNum : 0
        if (stepNum < 8) wizardCompleted = false
    }
    state = 1

}

def update_password() {

    try {
        if (!password.equals(passwordVerify)) {
            description = ec.l10n.toPersianLocale("passwordVerifyError")
            state = 0
            return
        }
        user = ec.entity.find("UserAccount").condition([employeeId: employeeId]).one()
        if (!user) {
            description = ec.l10n.toPersianLocale("noRecordFound")
            state = 0
            return
        }
        data = [password: password, username: user?.username]
        url = "EnteringSystem/Password"
        call = ec.service.sync().name("api.api.execute#put").parameters([url: url, postData: data, version: "v3"]).call()
        state = call?.state
        description = call?.description

    }
    catch (Exception ignored) {

        state = 0
        description = ec.l10n.toPersianLocale("generalError")
    }
}

def login_user() {

    try {
        data = [:]
        data.username = username
        data.password = password
        data.client_id = "74183b8c-5r2000e438rtg7477627c4ebd6149e69jhkh12f8.panel.taradod.hamkar"
        data.client_secret = "95fse1cec7sxzc012778dc8c852q8daa"
        data.grant_type = "password"
        data.scope = "public login personsView personAdmin smsPublic smsSingle nama hamrazm collectionsView"
        url = "Oauth2/PasswordCredentials"
        call = ec.service.sync().name("api.api.execute#post").parameters([url: url, postData: data, token: "false"]).call()
        token = call?.data?.access_token
        if (!token) {
            description = "نام کاربری و رمز عبور اشتباه است"
            ec.web.sendError(400, description)
            return
        }
        def ua = ec.entity.find("moqui.security.UserAccount").condition("username", username).disableAuthz().one()
        if (!ua) {
            sri.sendRedirectAndStopRender(ssoUrl + "&amp;messageType=error&amp;messageText=" + URLEncoder.encode("کاربر در سامانه همکار تعریف نشده است!", "UTF-8"))
        }
        ec.service.sync().name("org.moqui.impl.UserServices.update#PasswordInternal").parameters([userId: ua.userId, newPassword: token, newPasswordVerify: token, requirePasswordChange: requirePasswordChange]).disableAuthz().call()
        if (ec.user.loginUser(username, token)) {
            admin = ec.entity.find("Organization").condition([admin: ec.user.userAccount?.employeeId]).one()
            call = ec.service.sync().name("security.user.check#login").parameters([username: username]).call()
            adminAccess = org.moqui.util.SystemBinding.getPropOrEnv('companyAdmin')
            planAccess = org.moqui.util.SystemBinding.getPropOrEnv('planAccess')
            if (call?.canLogin == false && call.isAdmin == false) {
                if (!admin) {
                    ec.user.logoutUser()
                    sri.sendRedirectAndStopRender(ssoUrl + "&amp;messageType=error&amp;messageText=" + URLEncoder.encode(call?.description, "UTF-8"))
                    return
                } else {
                    ec.entity.find("UserGroupMember").condition([userId: ec.user.userId, userGroupId: adminAccess]).deleteAll()
                    ugs = ec.entity.find("UserGroupMember").condition([userId: ua.userId, userGroupId: planAccess]).one()
                    if (!ugs) ec.service.sync().name("create#UserGroupMember").parameters([userId: ec.user.userId, userGroupId: planAccess, fromDate: new Date()]).call()
                }
            }
            if (call?.canLogin == true && admin) {
                hasGrp = ec.entity.find("UserGroupMember").condition([userId: ec.user.userId, userGroupId: adminAccess]).one()
                if (!hasGrp) ec.service.sync().name("create#UserGroupMember").parameters([userId: ec.user.userId, userGroupId: adminAccess, fromDate: new Date()]).call()
            }
            ec.user.setPreference("apiToken", token)
            ec.user.setPreference("basicToken", Base64.getEncoder().encodeToString((username + ":" + token).getBytes()))
            loginKey = get_login_key(ua.userId)
            state = 1
            loadPlanUtils()
            hasPlan = this.plan.get_active_plan(ec.user.userAccount?.employeeId)["hasPlan"]
        } else {
            description = "نام کاربری و رمز عبور اشتباه است"
            ec.web.sendError(400, description)
            state = 0
        }
    }
    catch (Exception ignored) {
        state = 0
    }

}

def change_password() {

    try {
        ua = ec.entity.find("UserAccount").condition([username: username]).one()
        if (!ua) {
            description = ec.l10n.toPersianLocale("noRecordFound")
            state = 0
            return
        }
        userCode = ua?.userCode
        if (!userCode) {
            description = ec.l10n.toPersianLocale("noRecordFound")
            state = 0
            return
        }
        url = "Users/" + userCode + "/ChangePassword"
        data = [:]
        data.new_password = newPassword
        data.client_id = "74183b8c-5r2000e438rtg7477627c4ebd6149e69jhkh12f8.panel.taradod.hamkar"
        data.scope = "resetPassword"
        token = org.moqui.util.SystemBinding.getPropOrEnv('_systemToken')
        call = ec.service.sync().name("api.api.execute#patch").parameters([url: url, postData: data, token: token]).call()
        println(call)
        state = call?.state
        description = call?.description
    }
    catch (Exception e) {
        e.printStackTrace()
        state = 0
    }

}

def search_menu_user_group() {
    data=[ groupTypeEnumId: "UGALL"]
    if(menuId)data.menuId=menuId
    if(userGroupId)data.userGroupId=userGroupId
    menuUserGroup = ec.entity.find("MenuDetail").condition(data).list()
}

def delete_menu_user_group() {

    ec.service.sync().name("delete#MenuUserGroup").parameters(context).call()

}

def create_menu_user_group() {

    ec.service.sync().name("create#MenuUserGroup").parameters(context).call()

}

def get_menu_page() {


    menu = ec.entity.find("Menu").list()
    userGroup = ec.entity.find("UserGroup").condition("groupTypeEnumId", EntityCondition.NOT_EQUAL, "UGSEC").list()

}

def get_profile_page() {

    role = ec.entity.find("Role").list()
    group = ec.entity.find("UserGroup").list().findAll { ele -> ele.groupTypeEnumId == "UGALL" }
    organization = ec.entity.find("Organization").list()

}

def delete_user_group() {

    users = ec.entity.find("UserGroupMember").condition([userGroupId: userGroupId]).list()
    if (users.size() > 0) {
        state = 0
        description = ec.l10n.toPersianLocale('groupHasUserError')
        return
    }
    ec.service.sync().name("delete#UserGroup").parameters([userGroupId: userGroupId]).call()

}

def create_user_group() {

    groupTypeEnumId = "UGALL"
    serviceCall = ec.service.sync().name("create#UserGroup").parameters(context).call()
    userGroupId = serviceCall?.userGroupId

}

def update_user_group() {

    ec.service.sync().name("update#UserGroup").parameters(context).call()

}