import com.atlas.hamkar.JalaliCalendar
import groovy.json.JsonSlurper

import java.sql.Date
import java.time.LocalDate
import java.time.format.DateTimeFormatter;

def create_shift(){

    try{
        holidayConf=withHoliday?"Y":"N"
        if("Hour".equals(shiftType)){
            days=org.moqui.util.SystemBinding.getPropOrEnv('hourShiftDays')
            JsonSlurper slurper=new groovy.json.JsonSlurper()
            dayList=slurper.parseText(days);
        }
        add(unitId,holidayConf,dayList,shiftType)
        state=1
    }
    catch(Exception e){
        e.printStackTrace()
        state=0
    }

}

def create_shift_v2(){

    try{
        step=ec.entity.find("Step").condition([username:ec.user.username]).one()
        if(!step){
            description=ec.l10n.toPersianLocale("noStepFound")
            state=0
            return
        }
        if(!unitId)unitId=step?.unitId
        add(unitId,withHoliday==true?"Y":"N",dayList,shiftType)
    }
    catch(Exception e){
        e.printStackTrace()
        state=0
    }

}

def convertToTime(str){
    time="0:00";
    if(!str)return time;
    str=str.toString().trim()
    if(str=="")return time;
    switch(str.length()){
        case 0:
            time="0:00";
            break;
        case 1:
            time="0:00";
            break;
        case 2:
            time="0:00";
            break;
        case 3:
            time=str.substring(0,1)+":"+str.substring(1,3);
            break;
        case 4:
            time=str.substring(0,2)+":"+str.substring(2,4);
            break;
        default:
            break;
    }
    return time;
}

def get_shift(){
    try{
        get(unitId)
    }
    catch(Exception e){
        e.printStackTrace()
        state=0
    }
}

def get_shift_v2(){
    step=ec.entity.find("Step").condition([username:ec.user.username]).one()
    if(!step){
        description=ec.l10n.toPersianLocale("noRecordFound")
        state=0
        return
    }
    if(!unitId)unitId=step?.unitId
    get(unitId)
}

def get(unitId){

    try{
        serviceCall=ec.service.sync().name("general.general.get#connection").call()
        state=serviceCall?.state
        if(!"1".equals(state?.toString())){
            return
        }
        sql=serviceCall?.sql
        result=sql.rows("SELECT * FROM SHIFTS WHERE UNIT_ID=? AND SHIFT_TYPE!=-1",[unitId])
        shifts=[]
        dayList=["شنبه","یکشنبه","دوشنبه","سه شنبه","چهارشنبه","پنج شنبه","جمعه"]
        result.each{ele->
            entry=[
                    "day":dayList.get(ele.get("day")-1),
                    "shiftId":ele.get("SHIFT_NO"),
                    "dayIndex":ele.get("DAY"),
                    "fromTime":convertToTime(ele.get("START_TIME")),
                    "toTime":convertToTime(ele.get("END_TIME")),
                    "type":["value":ele.get("SHIFT_TYPE"),"label":ele.get("SHIFT_TYPE")==1 ? "کاری" : "تعطیل"]
            ]
            shifts.add(entry)
        }
        shifts=shifts.sort{ele->ele.dayIndex}
        unit=ec.entity.find("Unit").condition([unitId:unitId]).one()
        withHoliday=unit?.holidayConf=="Y"
        shiftType=unit?.shiftType
        state=1
    }
    catch(Exception e){
        e.printStackTrace()
        state=0
    }
}

def add(unitId,holiday_conf,dayList,shiftType){

    unit=ec.entity.find("Unit").condition(["unitId":unitId]).one()
    if(!unit){
        description=ec.l10n.toPersianLocale("noUnitFound")
        state=0
        return
    }
    unitName=unit?.name
    orgId=unit?.orgId
    org=ec.entity.find("Organization").condition(["orgId":orgId]).one()
    if(!org){
        description=ec.l10n.toPersianLocale("noOrgFound")
        state=0
        return
    }
    orgName=org?.name
    call=ec.service.sync().name("general.general.get#connection").call()
    state=call?.state
    if(!state?.toString()?.equals("1")){
        return
    }
    sql=call?.sql
    maxId=0
    def row = sql.firstRow("SELECT MAX(s.SHIFT_NO) FROM SHIFTS s")
    if(row.size()>0){
        maxId=row[0]
    }
    dayList.eachWithIndex{ele,index->
        if(maxId>49 && maxId<60)id=60
        id=++maxId
        fromTime=ele.fromTime.replaceAll(":","")
        toTime=ele.toTime.replaceAll(":","")
        fromTimeRam=0730
        toTimeRam=1630
        ramTime=9
        shiftTime=Integer.valueOf(toTime)-Integer.valueOf(fromTime)
        result=sql.rows("SELECT * FROM SHIFTS WHERE UNIT_ID=? AND DAY=? AND START_TIME=? AND END_TIME=? AND SHIFT_TYPE!=-1",[unitId,index+1,fromTime,toTime])
        if(result.size()>0){
            ele.shiftId=result[0]?.get("SHIFT_NO")
            sql.executeInsert("UPDATE SHIFTS SET SHIFT_TYPE=? WHERE SHIFT_NO=?",[ele.type.value,result[0]?.get("SHIFT_NO")])
        }
        else{
            resultUnit=sql.rows("SELECT * FROM SHIFTS WHERE UNIT_ID=? AND DAY=? AND SHIFT_TYPE!=-1",[unitId,index+1])
            if(resultUnit.size()>0){
                shiftIds=resultUnit.collect{e->e.get("SHIFT_NO")}
                shiftIds.each{e->
                sql.executeInsert("UPDATE SHIFTS SET SHIFT_TYPE=? WHERE SHIFT_NO=?",[-1,e])
                }
            }
            title=orgName+"-"+unitName+"-"+ele.day
            data=[id,unitId,index+1,fromTime,shiftTime,toTime,0,0,2400,fromTime,toTime,toTime,shiftTime,ele.type.value,fromTimeRam,ramTime,toTimeRam,900,720,900,0,0,title]
            insSt ="""INSERT INTO SHIFTS (SHIFT_NO,UNIT_ID,DAY,START_TIME,SHIF_TIME,END_TIME,DELAY_TIME,HUR_TIME,MAX_OVER,S2_TIME,E2_TIME,END5_TIME,HOLID_TIME,SHIFT_TYPE,RAMEZAN_S,RAMEZAN_T,RAMEZAN_E5,KASR_GH,KASR_BIM,KASR_B_H,TIME1_S,TIME1_E,TITLE) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) """
            sql.executeInsert(insSt,data)
            ele.shiftId=id
        }
    }
    calendar=new JalaliCalendar()
    currentYear=calendar.getYear()
    currentDay=calendar.getDay()+1
    currentMonth=calendar.getMonth()
    month=1
    holidays=[]
    if(withHoliday)holidays=ec.entity.find("Holiday").condition([year:currentYear]).list()
    while(month<13){
        calendar.setMonth(month)
        day=1
        result=sql.rows("SELECT * FROM GrpShift where GrpShift_GrpNo=? and GrpShift_Year=? and GrpShift_Month=?",[unitId,currentYear,month])
        if(result.size()>0){
            values=[]
            varList=[]
            data=[]
            while(day<32){
                if((month==currentMonth && day<currentDay) || month<currentMonth){
                    if(result["GrpShift_Day"+day].size()>0 && result["GrpShift_Day"+day][0]==null){
                        varList.add("GrpShift_Day"+day+"=?")
                        values.add("?")
                        calendar.setDay(day)
                        dd=calendar.toGregorian().getTime().format("yyyy/MM/dd")
                        if(holidays.find{e-> Date.valueOf(LocalDate.parse(dd, DateTimeFormatter.ofPattern("yyyy/MM/dd"))).compareTo(e.date)==0})data.add(50)
                        else if(calendar.getDayOfWeek()!=7){
                            data.add(dayList[calendar.getDayOfWeek()]?.shiftId)
                        }
                        else{
                            data.add(dayList[0]?.shiftId)
                        }
                    }
                }
                else {
                    varList.add("GrpShift_Day" + day + "=?")
                    values.add("?")
                    calendar.setDay(day)
                    dd = calendar.toGregorian().getTime().format("yyyy/MM/dd")
                    if (holidays.find { e -> Date.valueOf(LocalDate.parse(dd, DateTimeFormatter.ofPattern("yyyy/MM/dd"))).compareTo(e.date) == 0 }) data.add(50)
                    else if (calendar.getDayOfWeek() != 7) {
                        data.add(dayList[calendar.getDayOfWeek()]?.shiftId)
                    } else {
                        data.add(dayList[0]?.shiftId)
                    }
                }
                day++
            }
            if(!varList.isEmpty()) {
                updateSt = "UPDATE GrpShift SET "
                updateSt = updateSt + varList.join(",")
                updateSt = updateSt + " WHERE GrpShift_GrpNo=? and GrpShift_Year=? and GrpShift_Month=?"
                sql.executeInsert(updateSt, data + [unitId, currentYear, month])
            }
        }
        else{
            day=1
            values=["?","?","?"]
            varList=["GrpShift_GrpNo","GrpShift_Year","GrpShift_Month"]
            data=[unitId,currentYear,month]
            while(day <32){
                varList.add("GrpShift_Day"+day)
                values.add("?")
                calendar.setDay(day)
                dd=calendar.toGregorian().getTime().format("yyyy/MM/dd")
                if(holidays.find{e-> Date.valueOf(LocalDate.parse(dd, DateTimeFormatter.ofPattern("yyyy/MM/dd"))).compareTo(e.date)==0})data.add(50)
                else if(calendar.getDayOfWeek()!=7){
                    data.add(dayList[calendar.getDayOfWeek()]?.shiftId)
                }
                else{
                    data.add(dayList[0]?.shiftId)
                }
                day++
            }
            varList.add("GrpShift_DaysNo")
            values.add("?")
            if(month<7){
                data.add(31)
            }
            else {
                data.add(30)
            }
            varState=varList.join(",")
            valState=values.join(",")
            insSt ="INSERT INTO GrpShift("+varState+") VALUES("+valState+")"
            sql.executeInsert(insSt,data)
        }
        month++
    }
    ec.service.sync().name("update#Unit").parameters([unitId:unitId,holidayConf:holiday_conf,shiftType:shiftType]).call()
    shiftTitle=orgName+"-"+unitName
    description=ec.l10n.toPersianLocale("successShift")
}

def search_shift(){

    try {
        if (!orgId) {
            admin = ec.entity.find("UserGroupMember").condition([userId: ec.user.userId, userGroupId: "ADMIN"]).one()
            if (admin) {
                units = ec.entity.find("Unit").list()
            } else {
                org = ec.entity.find("Organization").condition([admin: ec.user.userAccount?.employeeId]).one()
                if (!org) {
                    description = ec.l10n.toPersianLocale("noAdminError")
                    state = 0
                    return
                }
                units = ec.entity.find("Unit").condition([orgId: org?.orgId]).list()
            }
        }
        else {
            units=ec.entity.find("Unit").condition([orgId:orgId]).list()
        }
        if (!units) {
            state = 1
            return
        }
        unitIds = units?.unitId?.join(",")
        orgs = ec.entity.find("Organization").condition([orgId: units?.orgId]).list()
        sql = (groovy.sql.Sql) ec.service.sync().name("general.general.get#connection").call()?.sql
        rows = sql.rows("SELECT * FROM SHIFTS where SHIFT_TYPE!=-1 AND UNIT_ID in ("+unitIds+")")
        shiftList = []
        units.each {e->
            it=[:]
            unitId=Long.valueOf(e.unitId)
            shifts=rows.findAll{ele->ele.get("UNIT_ID")==unitId}
            if(shifts.isEmpty())return
            org = orgs.find { ele -> ele.orgId == e?.orgId }
            it.unitId = unitId
            it.unit = e?.name ?: ""
            it.shiftType = e?.shiftType
            it.holidayConf = e?.holidayConf
            it.title = (org?.name?:"") + "-" + (e?.name?:"")
            it.shift=shifts
            shiftList.add(it)
        }
        state = 1
    }
    catch (Exception e){
        e.printStackTrace()
        state=0
    }

}

def get_shiftPage(){

    isAdmin=false
    admin=ec.entity.find("UserGroupMember").condition([userId:ec.user.userId,userGroupId:"ADMIN"]).one()
    if(admin){
        isAdmin=true
        organizations=ec.entity.find("Organization").list()
        units=ec.entity.find("Unit").list()
    }
    else{
        organization=ec.entity.find("Organization").condition([admin:ec.user.userAccount?.employeeId]).one()
        organizations=[organization]
        if(!organization){
            description=ec.l10n.toPersianLocale("noAdminError")
            state=0
            return
        }
        units=ec.entity.find("Unit").condition([orgId:organization?.orgId]).list()
    }
}