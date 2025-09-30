


def create_ticket() {

    employeeId = ec.user.userAccount?.employeeId
    if (!employeeId) {
        description = ec.l10n.toPersianLocale("noEmployeeFound")
        state = 0
        return
    }
    org = ec.entity.find("Organization").condition([admin: employeeId]).one()
    if (!org) {
        description = ec.l10n.toPersianLocale("noAdminError")
        state = 0
        return
    }
    ticketId = ec.service.sync().name("create#Ticket").parameters([registerUser: ec.user.userId, orgId: org?.orgId, fileId: fileId, title: title, importance: importance, section: section, description: description, status: "TICInProgress", registerDate: new java.util.Date()]).call()?.ticketId
}

def update_ticket() {

    ec.service.sync().name("update#Ticket").parameters([fileId: fileId, title: title, importance: importance, section: section, description: description, status: status, ticketId: ticketId]).call()
}

def delete_ticket() {

    ec.service.sync().name("update#Ticket").parameters([ticketId: ticketId, status: "TICCanceled"]).call()

}

def search_tickets() {

    tickets = []
    data = [:]
    if (status) data.status = status
    if (orgId) data.orgId = orgId
    ticketList = ec.entity.find("Ticket").condition(data).list()
    tickets = []
    ticketList.each { e ->
        en = [
                "ticketId"         : e.ticketId,
                "user"             : e.user.userFullName,
                "org"              : e?.org?.name,
                "title"            : e.title,
                "description"      : e.description,
                "section"          : e.section,
                "importance"       : e.importance,
                "registerDate"     : e.registerDate,
                "fileId"           : e.fileId,
                "title"            : e.title,
                "status"           : e.status,
                "statusDescription": e.state.description
        ]
        tickets.add(en)
    }

}

def get_org_tickets() {

    tickets = []
    employeeId = ec.user.userAccount?.employeeId
    orgId = ec.entity.find("Organization").condition([admin: employeeId]).one()?.orgId
    if (!orgId) {
        state = 1
        return
    }
    data = [orgId: orgId]
    if (status) data.status = status
    ticketList = ec.entity.find("Ticket").condition(data).list()
    tickets = []
    ticketList.each { e ->
        en = [
                "ticketId"         : e.ticketId,
                "user"             : e.user.userFullName,
                "org"              : e.org.name,
                "title"            : e.title,
                "importance"       : e.importance,
                "section"          : e.section,
                "description"      : e.description,
                "registerDate"     : e.registerDate,
                "fileId"           : e.fileId,
                "title"            : e.title,
                "status"           : e.status,
                "statusDescription": e.state.description
        ]
        tickets.add(en)
    }
    statusList = ec.entity.find("StatusItem").condition([statusTypeId: "TicketStatus"]).list()
    importance = ec.entity.find("Enumeration").condition([enumTypeId: "TicketImp"]).list()
    section = ec.entity.find("Enumeration").condition([enumTypeId: "TicketSect"]).list()

}

def get_answers() {

    tic = ec.entity.find("Ticket").condition([ticketId: ticketId]).one()
    ans = ec.entity.find("TicketAns").condition([ticketId: ticketId]).list()
    ansList = []
    ansList.add(["ticketId": ticketId, "text": tic.description, "date": tic.registerDate, "type": "user"])
    ans.each { e ->
        ansList.add(["ticketId": e.ticketId, "text": e.text, "date": e.registerDate, "type": e.type])
    }
}

def create_answer() {

    registerUser = ec.user.userId
    registerDate = new Date()
    ec.service.sync().name("create#TicketAns").parameters(context).call()

}