import groovy.transform.Field
import org.moqui.entity.EntityCondition

import java.sql.Date
import java.time.LocalDate
import java.time.ZoneId;

@Field
        eci = org.moqui.Moqui.getExecutionContextFactory()

def get_active_plan(employeeId) {

    emp = eci.entity.find("Employee").condition([employeeId: employeeId]).one()
    if (!emp) {
        return ["hasPlan": false]
    }
    orgId = emp.orgId
    if (!orgId) {
        return ["hasPlan": false]
    }
    p = eci.entity.find("Customer").condition("orgId", orgId).condition("isEnable": "Y").condition("endDate", EntityCondition.GREATER_THAN_EQUAL_TO, Date.valueOf(LocalDate.now())).one()
    return ["hasPlan": p != null]
}

def create_plan() {

    planId = ec.service.sync().name("create#Plan").parameters([title: title, users: users, cost: cost, category: category, days: days, description: description, discount: discount, hasDiscount: hasDiscount, giftUsers: giftUsers, isDeleted: "N", sortNum: sortNum]).call()?.planId

}

def update_plan() {
    serviceCall = ec.service.sync().name("update#Plan").parameters([planId: planId, title: title, users: users, cost: cost, category: category, days: days, description: description, discount: discount, hasDiscount: hasDiscount, giftUsers: giftUsers, isDeleted: "N", sortNum: sortNum]).call()
}

def delete_plan() {

    ec.service.sync().name("update#Plan").parameters([planId: planId, isDeleted: "Y"]).call()

}

def search_plan() {

    if (!title) title = ""
    plans = ec.entity.find("customer.Plan").condition("title", EntityCondition.LIKE, "%${title}%").condition(["isDeleted": "N"]).list()

}

def get_customer_page() {

    planList = ec.entity.find("Plan").condition(["isDeleted": "N"]).list()

}

def get_ticket_page() {

    statusList = ec.entity.find("StatusItem").condition([statusTypeId: "TicketStatus"]).list()
    orgList = ec.entity.find("Organization").list()
    importance = ec.entity.find("Enumeration").condition([enumTypeId: "TicketImp"]).list()
    section = ec.entity.find("Enumeration").condition([enumTypeId: "TicketSect"]).list()

}

def get_create_page() {

    importance = ec.entity.find("Enumeration").condition([enumTypeId: "TicketImp"]).list()
    section = ec.entity.find("Enumeration").condition([enumTypeId: "TicketSect"]).list()

}

def get_plan_page() {

    category = ec.entity.find("Enumeration").condition([enumTypeId: "PlanCategory"]).list()

}

def get_plan_list_page() {

    employeeId = ec.user.userAccount?.employeeId
    org = ec.entity.find("Organization").condition(["admin": employeeId]).one()
    if (!org) {
        description = ec.l10n.toPersianLocale("noAdminError")
        state = 0
        return
    }
    orgId = org?.orgId
    type = "N"
    allPlans = ec.entity.find("Plan").condition(["isDeleted": "N"]).list()
    plans = allPlans.groupBy { e -> e.category }
    planList = []
    plans.each { key, val ->
        t = [:]
        t.category = key
        t.plans = val.sort { e -> e.days }
        planList.add(t)
    }

    category = ec.entity.find("Enumeration").condition([enumTypeId: "PlanCategory"]).orderBy("sequenceNum").list()
    pln = ec.entity.find("Customer").condition([orgId: orgId, isEnable: "Y"]).one()?.getPlainValueMap(0)
    reservedPlan = ec.entity.find("Customer").condition([orgId: orgId, isEnable: "R"]).one()
    if (pln && pln.endDate) {
        endDate = LocalDate.parse(pln.endDate.toString())
        now = LocalDate.now(ZoneId.of("Asia/Tehran"))
        if (endDate.compareTo(now) < 0) {
            ec.service.sync().name("update#Customer").parameters([customerId: pln.customerId, isEnable: "N"]).call()
        } else {
            invoice = ec.entity.find("Invoice").condition([invoiceId: pln.invoiceId]).one()
            currentPlan = pln
            currentPlan.category = allPlans.find { e -> e.planId == pln?.planId }?.category
            type = reservedPlan ? "R" : "P"
        }
    }

}