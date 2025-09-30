import com.atlas.hamkar.ExcelUtil
import com.atlas.hamkar.JalaliCalendar
import com.ibm.icu.text.SimpleDateFormat
import com.ibm.icu.util.ULocale
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.sql.Sql
import org.apache.http.HttpResponse
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClientBuilder
import org.moqui.entity.EntityCondition

import javax.servlet.http.Cookie
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

def loadPlanUtils() {
    if (!this.plan) {
        path = ec.resource.getLocationReference("component/core/service/customer/plan.groovy").toString().split("file:")[1]
        this.plan = new GroovyShell().parse(new File(path))
    }
}

def create_step() {
    try {
        userStep = ec.entity.find("Step").condition([username: username]).one()
        if (!userStep) {
            ec.service.sync().name("create#Step").parameters([username: username, stepNum: step, isSkip: isSkip]).call()
        } else {
            curStep = userStep?.stepNum ?: 0
            data = [stepId: userStep?.stepId]
            if (isSkip) data.isSkip = isSkip
            if (step) {
                if (step > 8) {
                    description = ec.l10n.toPersianLocale("stepMaxError")
                    return
                }
                if (step < 0) {
                    description = ec.l10n.toPersianLocale("stepMinError")
                    return
                }
                data.stepNum = step
            }
            ec.service.sync().name("update#Step").parameters(data).call()
        }
        state = 1
    }
    catch (Exception ignored) {
        state = 0
    }
}

def get_app_setting() {
    try {
        dailyReportIsEnabled = "N"
        dailyReportIsRequired = "N"
        checkLocation = "Y"
        requiredLocation = 1000
        user = ec.entity.find("UserAccount").condition([username: username]).one()
        if (!user) {
            state = 1
            return
        }
        employeeId = user?.employeeId
        employment = ec.entity.find("Employee").condition([employeeId: employeeId]).one()
        employee = ec.entity.find("Employee").condition([employeeId: employeeId]).one()
        locationCheck = employee?.locationCheck ?: "Y"
        if (!employment) {
            state = 1
            return
        }
        orgId = employment?.orgId
        org = ec.entity.find("Organization").condition([orgId: orgId]).one()
        if (org && org.checkLocation != null) {
            requiredLocation = org?.checkLocation
        }
        reportConfig = ec.entity.find("ReportConfig").condition([orgId: orgId, "isEnabled": "Y"]).one()
        if (reportConfig) {
            dailyReportIsEnabled = "Y"
            if (reportConfig.isRequired != null) dailyReportIsRequired = reportConfig?.isRequired
            if (reportConfig?.charLimit) dailyReportLimit = reportConfig?.charLimit
        }
        state = 1
    }
    catch (Exception e) {
        e.printStackTrace()
        state = 0
    }
}

def get_step() {

    mobile = ec.user.username
    stepRecord = ec.entity.find("Step").condition([username: ec.user.username]).one()
    if (!stepRecord) {
        data = [username: ec.user.username, stepNum: 0, isSkip: "N"]
        employeeId = ec.entity.find("UserAccount").condition([username: ec.user.username]).one()?.employeeId
        if (employeeId) {
            org = ec.entity.find("Organization").condition([admin: employeeId]).one()
            if (org) data.orgId = org?.orgId
        }
        ec.service.sync().name("create#Step").parameters(data).call()
        step = 0
        isSkip = "N"
    } else {
        step = stepRecord?.stepNum
        isSkip = stepRecord?.isSkip ?: "N"
    }
    loadPlanUtils()
    hasPlan = this.plan.get_active_plan(ec.user.userAccount?.employeeId)["hasPlan"]
    state = 1
}

def get_menu() {

    try {
        if (webView == true) {
            serviceCall = ec.service.sync().name("general.general.login#webView").parameters([cookie: cookie]).call()
            curSession = serviceCall?.curToken
        }
        userId = ec.user.userId
        if (!userId) {
            state = 401
            return
        }
        serviceCall = check_login()
        println(serviceCall)
        if (serviceCall.state!=1) {
            state = 401
            return
        }
        username = ec.user.username
        userFullName = ec.user.userAccount.userFullName
        employeeId = ec.user.userAccount?.employeeId
        orgId = ec.entity.find("Organization").condition([admin: employeeId]).one()?.orgId
        if (orgId) {
            plan = ec.entity.find("Customer").condition([orgId: orgId, isEnable: "Y"]).one()
            if (plan && LocalDate.parse(plan?.endDate?.toString()).compareTo(LocalDate.now(ZoneId.of("Asia/Tehran"))) >= 0) {
                activePlan = plan?.planTitle
                daysLeft = ChronoUnit.DAYS.between(LocalDate.now(ZoneId.of("Asia/Tehran")), LocalDate.parse(plan?.endDate?.toString()))
            }
        }
        emp = ec.entity.find("Employee").condition([employeeId: employeeId]).one()
        orgName = emp?.org?.name ?: ""
        profileFileId = ec.entity.find("Employee").condition([employeeId: ec.user.userAccount?.employeeId]).one()?.profileFileId
        userPic = profileFileId
        userGroup = ec.entity.find("moqui.security.UserGroupMember").condition([userId: userId]).list()
        menuUser = ec.entity.find("general.MenuUserGroup").condition([userGroupId: userGroup?.userGroupId]).list()
        menu = ec.entity.find("general.Menu").condition([menuId: menuUser?.menuId]).list().getPlainValueList(0)
        menu = menu.unique { ele -> ele.menuId }
        loginKey = ec.user.getLoginKey()
        description = ec.l10n.toPersianLocale("generalSuccess")
    }
    catch (Exception ignored) {
        ignored.printStackTrace()
        state = 400
    }

}

def get_enum() {
    data = [:]
    if (type) data.enumTypeId = type
    enums = ec.entity.find("EnumDetail").condition(data).orderBy("enumCode").list()
}

def create_enum() {
    ec.service.sync().name("create#Enumeration").parameters(context).call()

}

def update_enum() {
    ec.service.sync().name("update#Enumeration").parameters(context).call()
}

def clear_cache() {
    ec.cache.clearAllCaches()
}

def get_sql_connection() {
    try {
        def mssqlUrl = "jdbc:sqlserver://;serverName=192.168.100.84;databaseName=PwKara;encrypt=false;trustServerCertificate=false;integratedSecurity=false;sslProtocol=TLS13"
        def mssqlServer = '192.168.100.84'
        def mssqlDatabase = 'PwKara'
        def mssqlUsername = 'sa'
        def mssqlPassword = 'pw@123456'
        sql = Sql.newInstance(mssqlUrl, mssqlUsername, mssqlPassword, "com.microsoft.sqlserver.jdbc.SQLServerDriver")
        state = 1
    }
    catch (Exception e) {
        e.printStackTrace()
        state = 0
    }
}

def check_health() {

}

def get_login_app() {

    userCode = ec.entity.find("UserAccount").condition([employeeId: employeeId]).one()?.userCode
    if (!userCode) {
        state = 0
        return
    }
    url = "/Apps/Users/" + userCode
    serviceCall = ec.service.sync().name("api.api.execute#get").parameters([url: url, version: "v2"]).call()
    apps = serviceCall?.data?.data
    apps = apps.findAll { ele -> ["78", "67", "77"].contains(ele.id) }
    state = serviceCall?.state
    description = serviceCall?.description

}

def delete_login_app() {

    url = "/Apps/Login/" + loginId
    serviceCall = ec.service.sync().name("api.api.execute#delete").parameters([url: url, version: "v2"]).call()
    state = serviceCall?.state
    description = serviceCall?.description

}

def update_login_app() {

    url = "/Apps/" + loginId
    data = [:]
    data.multi_device = multi
    serviceCall = ec.service.sync().name("api.api.execute#put").parameters([postData: data, url: url, version: "v2"]).call()
    state = serviceCall?.state
    description = serviceCall?.description

}

def convert_date() {
    try {
        jalali = [:]
        ULocale locale = new ULocale("fa_IR@calendar=persian");
        Date parsedDate;

        if (date) {
            date = date.trim()
            if (date.matches("(.*)T")) {
                parsedDate = new Date().parse("yyyy-MM-dd HH:mm:ss", date.replaceAll("T", " ").replaceAll("Z", "").replaceAll("z", ""))
            } else if (date.matches("(.*) (.*)"))
                parsedDate = new Date().parse("yyyy-MM-dd HH:mm", date)
            else
                parsedDate = new Date().parse("yyyy-MM-dd", date)

        } else {
            parsedDate = new Date();
        }
        if (parsedDate) {
            SimpleDateFormat sdfWeek = new SimpleDateFormat(SimpleDateFormat.WEEKDAY, locale);
            def jWeekDay = sdfWeek.format(parsedDate)
            SimpleDateFormat sdfDate;
            if (fullYear == 'Y') sdfDate = new SimpleDateFormat("YYYY/MM/dd", locale);
            else sdfDate = new SimpleDateFormat("YY/MM/dd", locale);
            def jDate = sdfDate.format(parsedDate);
            SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm", locale);
            def jTime = sdfTime.format(parsedDate);

            jalali['weekDay'] = jWeekDay
            jalali['date'] = jDate.replaceAll("۰", "0").replaceAll("۱", "1").replaceAll("۲", "2").replaceAll("۳", "3").replaceAll("۴", "4").replaceAll("۵", "5").replaceAll("۶", "6").replaceAll("۷", "7").replaceAll("۸", "8").replaceAll("۹", "9")

            jalali['time'] = jTime.replaceAll("۰", "0").replaceAll("۱", "1").replaceAll("۲", "2").replaceAll("۳", "3").replaceAll("۴", "4").replaceAll("۵", "5").replaceAll("۶", "6").replaceAll("۷", "7").replaceAll("۸", "8").replaceAll("۹", "9")
        }
        state = 1
    }

    catch (Exception) {
        state = 0
    }
}

def convert_date_list() {
    dateList = []
    jalali = [:]
    try {
        ULocale locale = new ULocale("fa_IR@calendar=persian");
        def out = [];
        def object = list

        def NewList = new JsonSlurper().parseText(object)

        def personAgeMap = []
        assert NewList instanceof List
        NewList.eachWithIndex { it, index ->
            def a = [:]
            parsedDate = new Date().parse("yyyy-MM-dd", it)
            SimpleDateFormat sdfWeek = new SimpleDateFormat(SimpleDateFormat.WEEKDAY, locale);
            def jWeekDay = sdfWeek.format(parsedDate)
            SimpleDateFormat sdfDate = new SimpleDateFormat("YYYY/MM/dd", locale);
            def jDate = sdfDate.format(parsedDate);
            SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm", locale);
            def jTime = sdfTime.format(parsedDate);

            a['weekDay'] = jWeekDay
            a['date'] = jDate.replaceAll("۰", "0").replaceAll("۱", "1").replaceAll("۲", "2").replaceAll("۳", "3").replaceAll("۴", "4").replaceAll("۵", "5").replaceAll("۶", "6").replaceAll("۷", "7").replaceAll("۸", "8").replaceAll("۹", "9")
            a['time'] = jTime.replaceAll("۰", "0").replaceAll("۱", "1").replaceAll("۲", "2").replaceAll("۳", "3").replaceAll("۴", "4").replaceAll("۵", "5").replaceAll("۶", "6").replaceAll("۷", "7").replaceAll("۸", "8").replaceAll("۹", "9")
            dateList.add(a)
        }
    }
    catch (Exception e) {
        e.printStackTrace()
        state = 0
    }
}

def change_date_format() {
    if (inputTime == null || inputTime == '') inputTime = '00:00'
    String[] spearedDate = inputDate.split("/");
    String[] spearedTime = inputTime.split(":");
    JalaliCalendar jalaliCalendar = new JalaliCalendar(Integer.parseInt(spearedDate[0]),
            Integer.parseInt(spearedDate[1]), Integer.parseInt(spearedDate[2]), Integer.parseInt(spearedTime[0]),
            Integer.parseInt(spearedTime[1]));
    jalaliCalendar.toGregorian();

    Date dateObj = jalaliCalendar.toGregorian().getTime();
    dateObject = dateObj
}

def get_data_value() {

    url = "/DataValues?group_data=" + type
    serviceCall = ec.service.sync().name("api.api.execute#get").parameters([url: url, diffResult: true]).call()
    state = serviceCall?.state
    data = serviceCall?.data?.data
    description = serviceCall?.description

}

def get_location() {

    url = "Locations?client_name=Bpms&amp;input_type=" + input + "&amp;input_value=" + inputValue + "&amp;output_type=" + output
    serviceCall = ec.service.sync().name("api.api.execute#get").parameters([url: url]).call()
    location = serviceCall?.data?.data?.records
    state = serviceCall?.state
    description = serviceCall?.description

}

def create_message() {

    ec.service.sync().name("create#Sms").parameters([text: content, mobile: phone, createTime: new Date()]).call()

}

def get_setting() {

    url = "Apps/AppSetting?package_name=" + packageName
    serviceCall = ec.service.sync().name("api.api.execute#get").parameters([token: token, url: url]).call()
    setting = serviceCall?.data?.data
    state = serviceCall?.state
    description = serviceCall?.description

}

def check_login() {

    token = ec.user.getPreference("apiToken")
    urlAddress = org.moqui.util.SystemBinding.getPropOrEnv('_apiUrl')
    fullUrl = urlAddress + "Oauth2/Token/IsExpire/" + token;
    HttpClient client = HttpClientBuilder.create().build();
    HttpPost request = new HttpPost(fullUrl);
    HttpResponse response = client.execute(request);

    BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
    StringBuffer result = new StringBuffer();
    String line = "";
    statusCode = response.getStatusLine().getStatusCode()
    while ((line = rd.readLine()) != null) {
        result.append(line);
    }
    def jsonSlurper = new groovy.json.JsonSlurper()
    data = jsonSlurper.parseText(result.toString())
    if (data.state == "err") return [state : 0]
    else return [state : 1]
}

def logout() {
    ec.user.logoutUser()

}

def export_excel() {

    if (data.size() == 0) {
        description = ec.l10n.toPersianLocale("excelError")
        state = 0
        return
    }
    encoded = ExcelUtil.write(data)
    state = 1
}

def login_webview() {


    url = "EnteringSystem/WebView?cookie=" + cookie
    serviceCall = ec.service.sync().name("api.api.execute#get").parameters([url: url, version: "v3"]).call();
    data = serviceCall?.data?.data
    token = data?.token;
    username = data?.username;
    def ua = ec.entity.find("moqui.security.UserAccount").condition("username", username).disableAuthz().one()
    if (!ua) {
        state = 0
        return;
    }
    ec.service.sync().name("org.moqui.impl.UserServices.update#PasswordInternal").parameters([userId: ua.userId, newPassword: token, newPasswordVerify: token, requirePasswordChange: requirePasswordChange]).disableAuthz().call()
    if (ec.user.loginUser(username, token)) {
        ec.user.setPreference("apiToken", token)
        ec.user.setPreference("basicToken", Base64.getEncoder().encodeToString((username + ":" + token).getBytes()))
        curToken = ec.web.getSessionToken()
        Cookie session = new Cookie("moquiSession", curToken);
        session.setPath("/")
        ec.web.response.addCookie(session)
        state = 1
    } else {
        state = 0
    }

}

def read_excel() {

    file = ec.entity.find("general.File").condition([fileId: fileId]).one()
    if (!file) {
        state = 0
        return
    }
    location = file?.location.toString().split("file:")[1]
    File file = new File(location)
    data = ExcelUtil.getDataListFromExcel(file);


}

def get_token() {

    try {
        url = org.moqui.util.SystemBinding.getPropOrEnv('ssoTokenUrl');
        queryMap = [:]
        queryMap.put("code", code)
        queryMap.put("grant_type", "authorization_code")
        queryMap.put("redirect_uri", org.moqui.util.SystemBinding.getPropOrEnv('ssoRedirectUrl'))
        queryMap.put("client_id", org.moqui.util.SystemBinding.getPropOrEnv('ssoClientId'))
        queryMap.put("client_secret", org.moqui.util.SystemBinding.getPropOrEnv('ssoClientSecret'))
        queryMap.put("scope", org.moqui.util.SystemBinding.getPropOrEnv('ssoScope'))

        HttpClient client = HttpClientBuilder.create().build();
        HttpPost request = new HttpPost(url)
        postData = JsonOutput.toJson(queryMap)
        request.setEntity(new StringEntity(postData, "UTF-8"))
        request.addHeader("Content-type", "application/json");
        HttpResponse response = client.execute(request);

        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        StringBuffer result = new StringBuffer();
        String line = "";
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }

        if (response.getStatusLine().getStatusCode() != 200) {
            throw new Exception("SSO internal Error")
        }
        def jsonSlurper = new groovy.json.JsonSlurper()
        outTokenMap = jsonSlurper.parseText(result.toString())

        token = outTokenMap?.access_token
        username = outTokenMap?.username
        state=1
    }
    catch (Exception e){
        e.printStackTrace()
    }
}

def get_auth_token() {

    token = ""
    credentials = [:]
    credentials.client_id = org.moqui.util.SystemBinding.getPropOrEnv('hamkarClientId')
    credentials.client_secret = org.moqui.util.SystemBinding.getPropOrEnv('hamkarSecret')
    credentials.grant_type = org.moqui.util.SystemBinding.getPropOrEnv('hamkarGrantType')
    credentials.scope = org.moqui.util.SystemBinding.getPropOrEnv('hamkarScope')
    url = 'Oauth2/ClientCredentials'
    serviceCall = ec.service.sync().name("api.api.execute#post").parameters([url: url, token: "false", postData: credentials]).call()
    state = serviceCall.state;
    description = serviceCall.description
    if (state == 1) token = serviceCall?.data['token_type'].toString().trim() + ' ' + serviceCall?.data['access_token'].toString().trim()

}

def get_data_type() {
    try {
        query = ""
        switch (type) {
            case "empType":
                query = 'SELECT et.Emptype_no AS id,et.title FROM EmpTypes et';
                break;
            default:
                break;
        }
        serviceCall = ec.service.sync().name("general.general.get#connection").call()
        state = serviceCall?.state?.toString()
        if (!state.equals("1") || query == "") {
            return
        }
        sql = serviceCall?.sql
        per = sql.rows(query)
        data = []
        per.each { it ->
            data.push(it)
        }
    }
    catch (Exception e) {
        state = 0
    }
}

def get_customer_page() {

    planList = ec.entity.find("Plan").condition(["isDeleted": "N"]).list()

}

def get_enum_page() {

    enumTypes = ec.entity.find("EnumerationType").list()

}