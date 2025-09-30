import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import org.moqui.entity.EntityCondition

def get_customer_plan() {

    try {
        employeeId = ec.user.userAccount?.employeeId
        org = ec.entity.find("Organization").condition([admin: employeeId]).one()
        if (!org) {
            description = ec.l10n.toPersianLocale("noAdminError")
            state = 0
            return
        }
        plans = ec.entity.find("CustomerDetail").condition([orgId: org?.orgId]).list()
    }
    catch (Exception ignored) {
        state = 0
    }

}

def search_customer() {

    try {
        data = [:]
        if (planId) data.planId = planId
        customers = ec.entity.find("CustomerDetail").condition(data).list()
        state = 1
    }
    catch (Exception e) {
        e.printStackTrace()
        state = 0
    }

}

def check_plan() {

    try {
        activePlans = ec.entity.find("Customer").condition(["isEnable": "Y"]).list()
        reservedPlan = ec.entity.find("Customer").condition(["isEnable": "R"]).list()
        deActiveCustomers = []
        reservedCustomer = []
        now = LocalDate.now(ZoneId.of("Asia/Tehran"))
        activePlans.each { e ->
            date = LocalDate.parse(e.endDate.toString(), DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            if (date.compareTo(now) == 0) {
                resPln = reservedPlan.find { i -> i.orgId == e.orgId }
                if (resPln) reservedCustomer.add(resPln)
                else deActiveCustomers.add(e)
            }
        }
        deactiveOrgIds = deActiveCustomers.orgId
        emplIds = ec.entity.find("Employee").condition([orgId: deactiveOrgIds]).list()?.employeeId
        users = ec.entity.find("UserAccount").condition([employeeId: emplIds]).list()?.username
        if (users.size() > 0) {
            call = ec.service.sync().name("security.user.expire#app").parameters([users: users]).call()
            if (!call.state.equals("1")) {
                return
            }
        }
        tr = ec.transaction.begin(null)
        deActiveCustomers.each { e ->
            ec.service.sync().name("update#Customer").parameters([customerId: e?.customerId, isEnable: "N"]).call()
        }
        admins = ec.entity.find("Organization").condition([orgId: deactiveOrgIds]).list()?.admin
        adminUsers = ec.entity.find("UserAccount").condition([employeeId: admins]).list()
        companyAdmin = org.moqui.util.SystemBinding.getPropOrEnv('companyAdmin')
        planAccess = org.moqui.util.SystemBinding.getPropOrEnv('planAccess')
        ec.entity.find("UserGroupMember").condition([userId: adminUsers.userId, userGroupId: companyAdmin]).deleteAll()
        currentAccess = ec.entity.find("UserGroupMember").condition([userGroupId: planAccess]).list()?.userId
        text = ec.l10n.toPersianLocale("expirePlanSms")
        adminUsers.each { e ->
            if (!currentAccess.contains(e.userId)) {
                ec.service.sync().name("create#UserGroupMember").parameters([userGroupId: planAccess, userId: e.userId, fromDate: new Date()]).call()
            }
            ec.service.sync().name("create#Sms").parameters(["text": text, "mobile": e.username]).call()
        }
        reservedCustomer.each { e ->
            currentPlan = activePlans.find { i -> i.orgId == e.orgId }
            if (currentPlan) {
                ec.service.sync().name("update#Customer").parameters([customerId: currentPlan.customerId, isEnable: "N"]).call()
            }
            ec.service.sync().name("update#Customer").parameters([customerId: e.customerId, isEnable: "Y"]).call()
        }
        ec.transaction.commit(tr)
    }
    catch (Exception e) {
        ec.transaction.rollback(e.getMessage(), e)
    }
}

def send_sms() {

    activePlans = ec.entity.find("Customer").condition(["isEnable": "Y"]).list()
    now = LocalDate.now(ZoneId.of("Asia/Tehran"))
    smsList = []
    activePlans.each { e ->
        if (!e.endDate) return
        d = e.endDate.toString()
        date = LocalDate.parse(d, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        if (ChronoUnit.DAYS.between(now, date) == 3) {
            smsList.add(e.orgId)
        }
    }
    admins = ec.entity.find("Organization").condition(["orgId": smsList]).list()?.admin
    users = ec.entity.find("UserAccount").condition([employeeId: admins]).list()?.username
    text = ec.l10n.toPersianLocale('planSms')
    users.each { e ->
        ec.service.sync().name("create#Sms").parameters([mobile: e, text: text, createTime: new Date()]).call()
    }


}

def buy_plan() {
    try {
        invEntity = ec.entity.find("Invoice").condition([authority: Authority])
        inv = invEntity.one()
        if(inv==null){
            ec.web?.response.sendRedirect(org.moqui.util.SystemBinding.getPropOrEnv("planUrl")+"?status="+false+"&id=0")
            return
        }
        startPage = inv.startPage ?: "app"
        url=startPage=="app"?org.moqui.util.SystemBinding.getPropOrEnv("planUrl"):org.moqui.util.SystemBinding.getPropOrEnv("wizardUrl")

        if (!inv) {
            status = false
            ec.service.sync().name("create#Payment").parameters([registerDate: new Date(), status: "NotFount", userId: ec.user.userId]).call()
            ec.web?.response.sendRedirect(url+"?status="+false+"&id=0")
            return
        }

        if (Status.equals("NOK")) {
            status = false
            invEntity.updateAll([status: "InvFail"])
            ec.service.sync().name("create#Payment").parameters([invoiceId: inv.invoiceId, amount: inv?.payAmount, registerDate: new Date(), status: "NOK", userId: ec.user.userId]).call()
            ec.web?.response.sendRedirect(url+"?status="+false+"&id=0")
            return
        }

        data = ["authority": Authority, "amount": inv.payAmount]
        serviceCall = ec.service.sync().name("api.api.execute#zarinPalPost").parameters([url: "pg/v4/payment/verify.json", data: data]).call()
        data = serviceCall?.data?.data
        call = ec.service.sync().name("create#Payment").parameters([invoiceId: inv.invoiceId, amount: inv?.payAmount, registerDate: new Date(), status: Status, userId: ec.user.userId, fee: data?.fee, cardHash: data?.card_hash, cardPan: data?.card_pan, feeType: data?.fee_type, refId: data?.ref_id, code: data?.code]).call()
        if (!([100, 101].contains(data?.code))) {
            status = false
            invEntity.updateAll([status: "InvFail"])
            ec.web?.response.sendRedirect(url+"?status="+false+"&id=0")
            return
        }
        if (data.code == 101) {
            status = true
            refId = data.ref_id
            ec.web?.response.sendRedirect(url+"?status="+false+"&id="+refId)
            return
        }
        paymentId = call?.paymentId
        startDate = LocalDate.now(ZoneId.of("Asia/Tehran"))
        isEnable = "Y"
        pln = ec.entity.find("Customer").condition([orgId: inv.orgId, isEnable: "Y"]).one()
        if (pln) {
            isEnable = "R"
            startDate = LocalDate.parse(pln.endDate.toString())
        }
        endDate = startDate.plusDays((int) inv.days)
        st = java.sql.Date.valueOf(startDate)
        ed = java.sql.Date.valueOf(endDate)

        ec.service.sync().name("create#Customer").parameters([planId: inv.planId, giftUsers: inv.giftUsers, planTitle: inv.title, invoiceId: inv.invoiceId, startDate: st, endDate: ed, orgId: inv.orgId, users: inv.users, days: inv.days, paymentId: paymentId, isEnable: isEnable, userId: ec.user.userId]).call()
        invEntity.updateAll([status: "InvSuccess"])
        currentGrp = ec.entity.find("UserGroupMember").condition([userId: ec.user.userId]).list()?.userGroupId
        companyAdmin = org.moqui.util.SystemBinding.getPropOrEnv('companyAdmin')
        planAccess = org.moqui.util.SystemBinding.getPropOrEnv('planAccess')
        if (!currentGrp.contains(companyAdmin)){
            ec.service.sync().name("create#UserGroupMember").parameters([userId: ec.user.userId, userGroupId: companyAdmin, fromDate: new Date()]).call()
        }
        ec.entity.find("UserGroupMember").condition([userId: ec.user.userId, userGroupId: planAccess])?.deleteAll()
        
        status = true
        refId = data?.ref_id
        ec.web.response.sendRedirect(url+"?status="+status+"&id="+refId)
    }
    catch (Exception e) {
        e.printStackTrace()
        ec.transaction.rollback(descriptin, new Exception())
        ec.web.response.sendRedirect(url+"?status=false&id=0")
    }
}

def create_invoice_v2() {

    org = ec.entity.find("Step").condition([username: ec.user.username]).one()?.orgId
    if (!org) {
        description = ec.l10n.toPersianLocale("noAdminError")
        state = 0
        return
    }
    add_invoice("wizard",org)

}

def create_invoice() {


    org = ec.entity.find("Organization").condition([admin: ec.user.userAccount?.employeeId]).one()
    if (!org) {
        description = ec.l10n.toPersianLocale("noAdminError")
        state = 0
        return
    }
    add_invoice("app",org.orgId)

}

def add_invoice(startPage,orgId){

    try {
        tr = ec.transaction.begin(null)
        plan = ec.entity.find("Plan").condition([planId: planId, isDeleted: "N"]).one()
        if (!plan) {
            description = ec.l10n.toPersianLocale('notValidPlan')
            state = 0
            return
        }
        employeeId = ec.user.userAccount?.employeeId
        userId = ec.user.userId

        payAmount=plan?.hasDiscount=="Y"?plan.discount:plan.cost
        category=plan?.category
        cost=plan?.cost
        hasDiscount=plan?.hasDiscount
        users = plan.users
        giftUsers = plan.giftUsers
        days = plan.days
        title = plan.title
        registerUser=ec.user.userId
        if (discountId) {
            dis = ec.entity.find("Discount").condition([discountId: discountId, isDeleted: "N"]).one()
            if (!dis) {
                description = ec.l10n.toPersianLocale("invalidDiscount")
                state = 0
                return
            }
            if(!dis?.code?.equals(discountCode)){
                description = ec.l10n.toPersianLocale("invalidDiscount")
                state = 0
                return
            }
            date = dis.expireDate
            if (date && LocalDate.now(ZoneId.of("Asia/Tehran")).compareTo(LocalDate.parse(date.toString())) > 0) {
                description = ec.l10n.toPersianLocale("expiredDiscount")
                state = 0
                return
            } else {
                discountId = dis?.discountId
            }
            disType = dis?.type
            if ("money".equals(disType)) {
                discount = dis?.money
                payAmount = payAmount - discount>0?payAmount - discount:0
            }
            if ("percent".equals(disType)) {
                value = dis?.percent
                d = (value / 100) * payAmount
                payAmount = payAmount - d>0? payAmount - d:0
            }
        }
        orgUsers = ec.entity.find("Employee").condition([orgId: orgId]).count()
        if (orgUsers > users + giftUsers) {
            description = ec.l10n.toPersianLocale('planUserError')
            state = 0
            return
        }
        statusId = "InvSuccess"
        registerDate = new Date()
        if (payAmount==0) statusId = "InvRequest"
        call = ec.service.sync().name("create#Invoice").parameters(context+[startPage:startPage,orgId: orgId]).call()
        invoiceId = call?.invoiceId

        if (payAmount==0) {
            isEnable = "Y"
            startDate = LocalDate.now(ZoneId.of("Asia/Tehran"))
            pln = ec.entity.find("Customer").condition([orgId: orgId, isEnable: "Y"]).one()
            if (pln) {
                isEnable = "R"
                startDate = LocalDate.parse(pln.endDate.toString())
            }
            endDate = startDate.plusDays((int) days)
            st = java.sql.Date.valueOf(startDate)
            ed = java.sql.Date.valueOf(endDate)
            ec.service.sync().name("create#Customer").parameters([planId: planId, giftUsers: giftUsers, planTitle: title, invoiceId: invoiceId, startDate: st, endDate: ed, orgId: orgId, users: users, days: days, paymentId: null, isEnable: isEnable, userId: ec.user.userId]).call()
            companyAdmin=org.moqui.util.SystemBinding.getPropOrEnv('companyAdmin')
            currentGrp = ec.entity.find("UserGroupMember").condition([userId: ec.user.userId,userGroupId:companyAdmin]).one()
            if (!currentGrp){
                ec.service.sync().name("create#UserGroupMember").parameters([userId: ec.user.userId, userGroupId: companyAdmin, fromDate: new Date()]).call()
            }
            ec.entity.find("UserGroupMember").condition([userId: ec.user.userId, userGroupId: org.moqui.util.SystemBinding.getPropOrEnv('planAccess')])?.deleteAll()
            code = 0
            url="#"
            description=ec.l10n.toPersianLocale("successZeroPlan")
            state=1
        } else {
            data = [
                    amount      : payAmount,
                    description : "plan buy",
                    callback_url: "http://panel.hamkar-app.ir/rest/s1/customer/pay",
//                    callback_url: org.moqui.util.SystemBinding.getPropOrEnv('zarinPalCallBack'),
                    merchant_id : org.moqui.util.SystemBinding.getPropOrEnv('zarinPalId')
            ]
            serviceCall = ec.service.sync().name("api.api.execute#zarinPalPost").parameters([url: org.moqui.util.SystemBinding.getPropOrEnv('zarinPalPay'), data: data]).call()
            if(serviceCall.state==0){
                description=ec.l10n.toPersianLocale("zarinpalError")
                state=0
                return
            }
            data = serviceCall?.data
            if(!data?.errors?.isEmpty()){
                ec.service.sync().name("update#Invoice").parameters([invoiceId: invoiceId, status: "InvRequestFail", errorCode: data.errors?.code]).call()
                if(data.errors && data.errors.validations.size()>0) {
                    description = data?.errors?.validations[0]
                }
                if(!description){
                    description=data?.errors?.message
                }
                if(!description){
                    description=ec.l10n.toPersianLocale("zarinpalError")
                }
                state=0
                return
            }
            else{
                data=data?.data
                ec.service.sync().name("update#Invoice").parameters([invoiceId: invoiceId, authority: data.authority, code: data.code, status: "InvRequest"]).call()
                code = data.authority
            }
            url=org.moqui.util.SystemBinding.getPropOrEnv('zarinPalUrl')+"pg/StartPay/"+code
            description=ec.l10n.toPersianLocale("planProgress")
            state = 1
        }
    }
    catch (Exception e) {
        e.printStackTrace()
        description = e.getMessage()
        ec.transaction.rollback(description, e)
        state = 0
    }
}

def check_discount() {

    try {
        discount=0
        plan = ec.entity.find("Plan").condition([planId: planId, isDeleted: "N"]).one()
        if (!planId) {
            description = ec.l10n.toPersianLocale("noPlanError")
            state = 0
            return
        }
        payAmount=plan?.hasDiscount=="Y"?plan.discount:plan.cost
        dis = ec.entity.find("Discount").condition([code: code, isDeleted: "N"]).one()
        if (!dis) {
            description = ec.l10n.toPersianLocale("invalidDiscount")
            state = 0
            return
        }
        date = dis.expireDate
        if (date && LocalDate.now(ZoneId.of("Asia/Tehran")).compareTo(LocalDate.parse(date.toString())) > 0) {
            description = ec.l10n.toPersianLocale("expiredDiscount")
            state = 0
            return
        } else {
            discountId = dis?.discountId
            disType = dis?.type
            if ("money".equals(disType)) {
                discount = dis?.money
                payAmount = payAmount - discount>0?payAmount - discount:0
            }
            if ("percent".equals(disType)) {
                value = dis?.percent
                d = (value / 100) * payAmount
                payAmount = payAmount - d>0? payAmount - d:0
                discount=d
            }
        }
        state = 1
    }
    catch (Exception ignored) {
        state = 0
    }

}

def search_plan_v2(){
    try{
        if(ec.user.username) {
            orgId = ec.entity.find("Step").condition([username: ec.user.username]).one()?.orgId
        }
        if(orgId){
            currentPlan=ec.entity.find("Customer").condition([orgId:orgId,isEnable: "Y"]).list()
            currentPlan=currentPlan.find{e->e.endDate.compareTo(java.sql.Date.valueOf(LocalDate.now()))>=0}
        }
        planList=ec.entity.find("customer.Plan").condition(["isDeleted":"N"]).list()
        plans=[]
        cat=ec.entity.find("Enumeration").condition([enumTypeId:"PlanCategory"]).list()
        planList.each{e->
            ele=[:]
            discount=0
            ele.isCurrent=false
            if(currentPlan) ele.isCurrent=currentPlan.planId==e.planId
            discountPercent=0
            finalCost=e.cost
            cost=e.cost?:0
            if(e.hasDiscount=="Y"){
                if(cost!=0){
                    discount=e.discount?:0
                    discountPercent=(discount/cost)*100
                    finalCost=cost-discount
                }
            }
            ele.planId=e.planId?:""
            ele.title=e.title?:""
            ele.users=e.users?:0
            ele.category=e.category?:""
            ele.categoryDesc=cat.find{i->i.enumId==e?.category}?.description?:""
            ele.cost=e.cost?:0
            ele.days=e.days?:0
            ele.description=e.description?:0
            ele.discount=discount
            ele.finalCost=finalCost
            ele.discountPercent=discountPercent
            ele.giftUsers=e.giftUsers?:0
            bullet1="اشتراک "+ele.days+" "+"روزه"
            allUsers=ele.users+ele.giftUsers
            bullet2=allUsers+" پرسنل"
            bullet3="آموزش کامل راه اندازی"
            bullet4="پشتیبانی همه جانبه"
            ele.bullets=[bullet1,bullet2,bullet3,bullet4].join(",")
            plans.add(ele)
        }
        state=1
    }
    catch(Exception e){
        e.printStackTrace()
        state=0}

}

