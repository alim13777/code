import com.atlas.hamkar.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.text.SimpleDateFormat
import org.moqui.entity.EntityCondition

import java.time.LocalDate
import java.time.format.DateTimeFormatter

def dayReserve(day, mehd, mejd, mtd, md = []) {
    if (mehd.contains(day) || mejd.contains(day) || mtd.contains(day) || md.contains(day)) return true
    return false
}

def pwTime2pbTime(time) {
    if (!time || time.toString().trim() == "") {
        return "00:00"
    }
    if (Integer.valueOf(time) < 0) time = time * -1
    time = time.toString()
    if (time.length() == 1) {
        return '00:0' + time
    } else if (time.length() == 2) {
        return '00:' + time
    } else if (time.length() == 3) {
        return '0' + time.substring(0, 1) + ':' + time.substring(1, 3)
    } else if (time.length() == 4) {
        return time.substring(0, 2) + ':' + time.substring(2, 4)
    } else {
        return time.substring(0, time.length() - 2) + ':' + time.substring(time.length() - 2, time.length())
    }
}

def time2min(time) {
    if (!time) return 0
    else t = time.split(':')
    t0 = t[0] != "" ? Integer.valueOf(t[0]) : 0
    t1 = t[0] != "" ? Integer.valueOf(t[1]) : 0
    result = (t0 * 60) + t1
    return result
}

def timeSub(time1, time2) {
    time1 = pwTime2pbTime(time1)
    time2 = pwTime2pbTime(time2)
    time1 = time2min(time1)
    time2 = time2min(time2)
    t = time1 - time2
    f = ((int) ((double) t / 60)) ? (int) (t / 60) : 0
    s = (t % 60)
    if (f < 0 || s < 0) {
        return "0"
    }
    if (s == 0) s = '00'
    else if (s.toString().length() == 1) s = '0'.concat(s.toString())
    return f.toString().concat(s.toString())
}

def timeSum(t1, t2) {
    t = time2min(pwTime2pbTime(t1)) + time2min(pwTime2pbTime(t2))
    f = ((int) (t / 60)) ? (int) (t / 60) : ''
    s = (t % 60)
    if (s == 0) s = '00'
    else if (s.toString().length() == 1) s = '0'.concat(s.toString())
    result = f.toString() + s.toString()
}

def convertToListV2(date1, date2) {
    dates = []
    fromDate = LocalDate.parse(date1, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    toDate = LocalDate.parse(date2, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    while (fromDate <= toDate) {
        dates.add(fromDate)
        fromDate = fromDate.plusDays(1)
    }
    return dates
}

def get_unit(employeeId) {
    unit = ec.entity.find("EmplUnit").condition([employeeId: employeeId, role: "karmand"]).one()
    return unit
}

def karnameh() {

    try {
        dates = convertToListV2(startDate, endDate)
        user = ec.entity.find("UserAccount").condition([username: username]).one()
        if (!user) {
            state = 0
            return
        }
        employeeId = user?.employeeId
        employee = ec.entity.find("Employee").condition([employeeId: employeeId]).one()
        if (!employee) {
            state = 0
            return
        }
        nationalId = employee?.nationalId
        if (!nationalId) {
            description = ec.l10n.toPersianLocale("nationalIdError")
            state = 0
            return
        }
        nationalId = Long.valueOf(nationalId)
        serviceCall = ec.service.sync().name("general.general.get#connection").call()
        state = serviceCall?.state
        if (state != 1) {
            return
        }
        sql = serviceCall?.sql
        query = "SELECT * FROM EMPLOYEE WHERE PERS_NO=?"
        emp = sql.rows(query, nationalId)[0]
        unit = get_unit(employeeId)
        if (!unit) {
            description = ec.l10n.toPersianLocale('noUnitError')
            state = 0
            return
        }
        groupId = unit?.unitId
        shiftType = ec.entity.find("Unit").condition([unitId: unit?.unitId]).one()?.shiftType
        shiftGrps = sql.rows('SELECT * from GrpShift WHERE GrpShift_GrpNo = ?', [groupId])
        allShifts = sql.rows('select * from SHIFTS')
        karkardList = sql.rows('SELECT STATUS,TIME_,DATE_ FROM DataFile WHERE EMP_NO = ?', [emp.get('EMP_NO')])

        return calculate()

    }
    catch (Exception e) {
        e.printStackTrace()
        return ["state": 0]
    }
    finally {
        sql.close()
    }
}

def karkard() {
    try {

        dates = convertToListV2(startDate, endDate)
        if (dates.size() > 31) {
            state = 0;
            description = ec.l10n.toPersianLocale("dateSizeError")
            return
        }
        user = ec.entity.find("UserAccount").condition([username: username]).one()
        if (!user) {
            state = 0
            description = ec.l10n.toPersianLocale("noUserFound")

            return
        }
        employeeId = user?.employeeId
        unit = get_unit(employeeId)
        if (!unit) {
            description = ec.l10n.toPersianLocale("noUnitFound")
            state = 0
            return
        }
        employee=ec.entity.find("Employee").condition([employeeId:employeeId]).one()
        nationalId = employee?.nationalId
        if (!nationalId) {
            description = ec.l10n.toPersianLocale("nationalIdNullError")
            state = 0
            return
        }
        nationalId = Long.valueOf(nationalId)
        serviceCall = ec.service.sync().name("general.general.get#connection").call()
        state = serviceCall?.state
        if (state != 1) {
            return
        }
        sql = serviceCall?.sql

        query = "SELECT * FROM EMPLOYEE WHERE PERS_NO=?"
        emp = sql.rows(query, nationalId)[0]
        groupId = Integer.valueOf(unit?.unitId)
        finalArray = []
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd")
        queryShift = sql.rows('SELECT * from GrpShift WHERE GrpShift_GrpNo = ?', [groupId])
        allShifts = sql.rows('select TITLE,dbo.TimeForm(START_TIME) as START_TIME,dbo.TimeForm(END_TIME) as END_TIME,dbo.TimeForm(SHIF_TIME) as SHIF_TIME,SHIFT_NO from SHIFTS')
        vacations = sql.rows('select * from MOR_MAM WHERE EMP_NO=?', [emp.get('EMP_NO')])
        karkard = sql.rows('select STATUS,dbo.TimeForm(TIME_) as TIME_,DATE_ from DataFile WHERE EMP_NO = ?', [emp.get('EMP_NO')])
        Collections.reverse(dates)
        apiSql = ec.service.sync().name("api.api.get#connection").parameters([databaseName: "sshbprofile"]).call()?.sql
//        if (user?.userCode == null) {
//            description =ec.l10n.toPersianLocale('dataError')
//            state = 0
//            return
//        }
        dailyReports = apiSql.rows("SELECT * FROM dailyReport d left join sshbapi.oauthUser10000 us on us.userCode=d.userCode where us.username=?", user?.username)

        for (i = 0; i < dates.size(); i++) {
            finalArray[i] = [:]
            shamsiDate = new JalaliCalendar(dates[i])
            shamsiYear = shamsiDate.getYear()
            shamsiMonth = shamsiDate.getMonth()
            shamsiDay = shamsiDate.getDay()
            report = dailyReports.find { ele -> ele.date == dates[i] }
            dateStr = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(dates[i])
            finalArray[i]['report'] = dailyReports.find({ ele -> ele.get("date").toString().equals(dateStr) })?.text
            finalArray[i]['miladi_date'] = dateStr
            finalArray[i]['shamsi_date'] = shamsiDate.toString().replaceAll("-", "/")
            finalArray[i]['miladi_day'] = DateTimeFormatter.ofPattern("dd").format(dates[i])
            finalArray[i]['shamsi_day'] = getDayOfWeekString(shamsiDate.getDayOfWeek())
            rowsShift = queryShift.findAll { ele -> ele["GrpShift_Year"] == shamsiYear && ele["GrpShift_Month"] == shamsiMonth }
            if (rowsShift.size() > 0) {
                shiftNumber = rowsShift[0]["GrpShift_Day" + shamsiDay]
            }
            if (shiftNumber) {
                shiftRows = allShifts.findAll { ele -> ele.get("SHIFT_NO") == shiftNumber }
                if (shiftRows.size() > 0) {
                    finalArray[i]['shift_status'] = 10
                    finalArray[i]['shift_title'] = shiftRows[0].get('TITLE')
                    finalArray[i]['shift_start'] = shiftRows[0].get('START_TIME')
                    finalArray[i]['shift_end'] = shiftRows[0].get('END_TIME')
                    if (shiftRows[0].get('START_TIME') == "00:00" && shiftRows[0].get('END_TIME') == "00:00") {
                        finalArray[i]['shift_title'] = "تعطیل شیفت"
                        finalArray[i]['shift_status'] = 22
                    }
                } else {
                    shiftRows = ''
                    if (["50", "51", "52"].contains(shiftNumber.toString())) {
                        if (shiftNumber.toString().equals("50")) {
                            title = "تعطیل رسمی"
                            status = 21
                        }
                        if (shiftNumber.toString().equals("51")) {
                            title = "تعطیل شیفت"
                            status = 22
                        }
                        if (shiftNumber.toString().equals("52")) {
                            title = "تعطیل خاص"
                            status = 23
                        }
                        finalArray[i]['shift_status'] = status
                        finalArray[i]['shift_title'] = title
                        finalArray[i]['shift_start'] = 0
                        finalArray[i]['shift_end'] = 0
                    } else {
                        continue
                    }
                }
            }

            result = sql.rows('select dbo.Miladi2Pwkara(?) as pwKaraDate', [dates[i]])

            if (result.size() == 0) {
                continue
            }

            dateTime = result[0]
            result = vacations.findAll { ele -> ele.get("S_DATE") <= dateTime.get('pwKaraDate') && ele.get("E_DATE") >= dateTime.get('pwKaraDate') }
            if (result.size() > 0) {
                type = result[0].get('TYP')
                if (type == 54) {
                    daily_request = "daily_mission"
                    daily_request_caption = "ماموریت روزانه"
                    daily_request_color = "#d290eb"
                }
                if (type == 57 || type == 60 || type == 67 || type == 64 || type == 61) {
                    daily_request = "daily_vacation"
                    daily_request_caption = "مرخصی روزانه"
                    daily_request_color = "#90ebe3"
                }
            } else {
                daily_request = ''
                daily_request_caption = ''
                daily_request_color = ''
            }
            rows = karkard.findAll { ele -> ele.get("DATE_") == dateTime.get('pwKaraDate') }
            taradod = []

            if (rows.size() > 0) {
                finalArray[i]['daily_request'] = daily_request
                finalArray[i]['daily_request_caption'] = daily_request_caption
                finalArray[i]['daily_request_color'] = daily_request_color
                if (daily_request == '') {
                    finalArray[i]['first_taratod'] = rows[0].get('TIME_')
                    finalArray[i]['last_taratod'] = rows[rows.size() - 1].get('TIME_')
                } else {
                    finalArray[i]['first_taratod'] = ''
                    finalArray[i]['last_taratod'] = ''
                }
            }
//            else {
//                output_status = "absent"
//                output_caption = "عدم حضور"
//                output_color = "#e32626"
//                if (shiftRows?.size() > 0) {
//                    taradod.add([
//                            'hourly_request_id': "",
//                            'from'             : shiftRows[0].get('START_TIME'),
//                            'to'               : shiftRows[0].get('END_TIME'),
//                            'status'           : output_status,
//                            'caption'          : output_caption,
//                            'color'            : output_color,
//                    ])
//                }
//            }
            finalArray[i]['daily_presence_time'] = dailyPresenceTime(rows, dates[i])

            if (daily_request == '') {
                for (j = 0; j < rows.size(); j++) {
                    if (j == 0 && shiftRows != '' && shiftRows != null && rows[j] && rows[j].get('TIME_') > shiftRows[0].get('START_TIME')) {
                        if (rows[j] != null && rows[j].get('STATUS') == 0) {
                            if (shiftRows[0].get('START_TIME') && rows[j].get('TIME_')) {
                                output_status = "absent"
                                output_caption = "عدم حضور"
                                output_color = "#e32626"
                                end = rows[j].get('TIME_') > shiftRows[0].get('END_TIME') ? shiftRows[0].get('END_TIME') : rows[j].get('TIME_')
                                taradod.add([
                                        'hourly_request_id': "",
                                        'from'             : shiftRows[0].get('START_TIME'),
                                        'to'               : end,
                                        'status'           : output_status,
                                        'caption'          : output_caption,
                                        'color'            : output_color,
                                ])
                            }
                        } else if (rows[j].get('STATUS') == 17 || rows[j].get('STATUS') == 9) {
                            if (rows[j].get('STATUS') == 17) {
                                status = "hourly_vacation"
                                name = "مرخصی ساعتی"
                                color = "#0b0b90"
                            } else {
                                status = "hourly_mission"
                                name = "ماموریت ساعتی"
                                color = "#420967"
                            }
                            if (shiftRows[0].get('START_TIME') && rows[j].get('TIME_')) {
                                taradod.add([
                                        'hourly_request_id': '',
                                        'from'             : shiftRows[0].get('START_TIME'),
                                        'to'               : rows[j].get('TIME_'),
                                        'status'           : status,
                                        'caption'          : name,
                                        'color'            : color,
                                ])
                            }
                        }
                        if (rows[j] && rows[j].get('TIME_') && rows[j + 1] && rows[j + 1].get('TIME_')) {
                            taradod.add([
                                    'hourly_request_id': '',
                                    'from'             : rows[j].get('TIME_'),
                                    'to'               : rows[j + 1].get('TIME_'),
                                    'status'           : "present",
                                    'caption'          : "حضور",
                                    'color'            : "#d7d7d7",
                            ])
                        }
                    } else {
                        if (j != 0 && shiftRows != null && shiftRows != '' && rows[j].get('TIME_') < shiftRows[0].get('END_TIME')) {
                            if (rows[j - 1].get('STATUS') == 0) {
                                if (rows[j - 1].get('TIME_') && rows[j].get('TIME_')) {
                                    output_status = "absent"
                                    output_caption = "عدم حضور"
                                    output_color = "#e32626"

                                    if ((rows[j - 1].get('TIME_') < shiftRows[0].get('START_TIME') && rows[j].get('TIME_') < shiftRows[0].get('START_TIME')) ||
                                            (rows[j - 1].get('TIME_') > shiftRows[0].get('END_TIME') && rows[j].get('TIME_') > shiftRows[0]['END_TIME'])) {
                                        hrid = ''
                                        output_status = "absent_out_of_shift"
                                        output_caption = 'غیبت خارج از شیفت'
                                        output_color = '#f9f9f9'
                                    } else {
                                        hrid = ""
                                    }
                                    from = rows[j - 1].get('TIME_')
                                    if (rows[j - 1].get('TIME_') < shiftRows[0].get('START_TIME')) {
                                        if (rows[j].get('TIME_') > shiftRows[0].get('START_TIME')) from = shiftRows[0].get('START_TIME')
                                    }
                                    if (output_status != "absent_out_of_shift") {
                                        taradod.add([
                                                'hourly_request_id': hrid,
                                                'from'             : from,
                                                'to'               : rows[j].get('TIME_'),
                                                'status'           : output_status,
                                                'caption'          : output_caption,
                                                'color'            : output_color,
                                        ])
                                    }
                                }
                            } else if (rows[j - 1].get('STATUS') == 17 || rows[j - 1].get('STATUS') == 9) {
                                if (rows[j - 1].get('STATUS') == 17) {
                                    status = "hourly_vacation"
                                    name = 'مرخصی ساعتی'
                                    color = '#0b0b90'
                                } else {
                                    status = "hourly_mission"
                                    name = 'ماموریت ساعتی'
                                    color = '#420967'
                                }

                                if (rows[j - 1].get('TIME_') && rows[j].get('TIME_')) {
                                    taradod.add([
                                            'hourly_request_id': '',
                                            'from'             : rows[j - 1].get('TIME_'),
                                            'to'               : rows[j].get('TIME_'),
                                            'status'           : status,
                                            'caption'          : name,
                                            'color'            : color
                                    ])
                                }
                            }
                        } else {
                            if (j != 0 && shiftRows != null && shiftRows != '' && rows[j - 1].get('TIME_') < shiftRows[0].get('END_TIME')) {
                                if (rows[j - 1].get('STATUS') == 0) {
                                    if (rows[j - 1].get('TIME_') && rows[j].get('STATUS')) {
                                        output_status = 'absent'
                                        output_caption = 'عدم حضور'
                                        output_color = '#e32626'

                                        if (rows[j - 1].get('TIME_') < shiftRows[0].get('START_TIME') && shiftRows[0].get('END_TIME') < shiftRows[0].get('START_TIME') ||
                                                (rows[j - 1].get('TIME_') > shiftRows[0].get('END_TIME') && shiftRows[0].get('END_TIME') > shiftRows[0].get('END_TIME'))) {
                                            hrid = ''
                                            output_status = "absent_out_of_shift"
                                            output_caption = 'غیبت خارج از شیفت'
                                            output_color = '#f9f9f9'
                                        } else {
                                            hrid = ""
                                        }
                                        from = rows[j - 1].get('TIME_')
                                        if (rows[j - 1].get('TIME_') < shiftRows[0].get('START_TIME')) {
                                            if (rows[j].get('TIME_') > shiftRows[0].get('START_TIME')) from = shiftRows[0].get('START_TIME')
                                        }
                                        if (output_status != "absent_out_of_shift") {
                                            taradod.add([
                                                    'hourly_request_id': hrid,
                                                    'from'             : from,
                                                    'to'               : shiftRows[0]['END_TIME'],
                                                    'status'           : output_status,
                                                    'caption'          : output_caption,
                                                    'color'            : output_color,
                                            ])
                                        }
                                    }
                                } else if (rows[j - 1].get('STATUS') == 17 || rows[j - 1].get('STATUS') == 9) {
                                    if (rows[j - 1].get('STATUS') == 17) {
                                        status = "hourly_vacation"
                                        name = "مرخصی ساعتی"
                                        color = '#0b0b90'
                                    } else {
                                        status = "hourly_mission"
                                        name = 'ماموریت ساعتی'
                                        color = '#420967'
                                    }
                                    if (rows[j - 1].get('TIME_') && shiftRows[0].get('END_TIME')) {
                                        taradod.add([
                                                'hourly_request_id': '',
                                                'from'             : rows[j - 1].get('TIME_'),
                                                'to'               : shiftRows[0].get('END_TIME'),
                                                'status'           : status,
                                                'caption'          : name,
                                                'color'            : color,
                                        ])
                                    }
                                }
                            }
                        }
                        if (rows[j] && rows[j + 1] && rows[j].get('TIME_') && rows[j + 1].get('TIME_')) {
                            taradod.add([
                                    'hourly_request_id': '',
                                    'from'             : rows[j].get('TIME_'),
                                    'to'               : rows[j + 1].get('TIME_'),
                                    'status'           : "present",
                                    'caption'          : "حضور",
                                    'color'            : '#d7d7d7'

                            ])
                        }

                    }
                    j++;
                }
                if (rows.size() > 0 && rows.size() % 2 == 0 && shiftRows != '' && shiftRows != null && rows[rows.size() - 1].get('TIME_') < shiftRows[0].get('END_TIME')) {
                    if (rows[rows.size() - 1].get('TIME_') && shiftRows[0].get('END_TIME')) {
                        start = (rows[rows.size() - 1].get('TIME_') < shiftRows[0]['START_TIME']) ? shiftRows[0]['START_TIME'] : rows[rows.size() - 1].get('TIME_')
                        if (rows[rows.size() - 1].get('STATUS') == 0) {
                            hrid = ""
                            output_status = 'absent'
                            output_caption = "عدم حضور"
                            output_color = '#e32626'
                        } else {
                            hrid = ''
                            if (rows[rows.size() - 1].get('STATUS') == 17) {
                                status = "hourly_vacation"
                                caption = "مرخصی ساعتی"
                                color = '#0b0b90'
                            }
                            if (rows[rows.size() - 1].get('STATUS') == 9) {
                                status = "hourly_mission"
                                caption = 'ماموریت ساعتی'
                                color = '#420967'
                            }
                        }
                        taradod.add([
                                'hourly_request_id': "",
                                'from'             : start,
                                'to'               : shiftRows[0].get('END_TIME'),
                                'status'           : output_status,
                                'caption'          : output_caption,
                                'color'            : output_color,
                        ])
                    }
                }
            }

            finalArray[i]['taradod'] = taradod

            if (finalArray[i]['taradod'].size() > 0) {
                formatter2 = new SimpleDateFormat('HH:mm')
                totalAbsentTime = formatter2.parse("00:00")
                finalArray[i]['taradod'].each { value ->
                    if ('absent'.equals(value['status'])) {
                        start_time = value['from']
                        end_time = value['to']
                        start = formatter2.parse(start_time)
                        end = formatter2.parse(end_time)
                        use(groovy.time.TimeCategory) {
                            interval = end.minus(start)
                            sumPresenceTime = sumPresenceTime.plus(interval)
                            if (shiftRows[0].get('END_TIME') != "23:59") totalAbsentTime = totalAbsentTime.plus(interval)
                        }
                        finalArray[i]['total_absent_daily'] = formatter2.format(totalAbsentTime)
                    } else {
                        if (!finalArray[i]['total_absent_daily']) finalArray[i]['total_absent_daily'] = ""
                    }
                }
            } else {
                if (!finalArray[i]['total_absent_daily']) finalArray[i]['total_absent_daily'] = ""
            }


            final_taradods = []
            for (y = 0; y < finalArray[i]['taradod'].size(); y++) {
                if (finalArray[i]['taradod'][y]['from'] == finalArray[i]['shift_start'] &&
                        finalArray[i]['taradod'][y]['to'] == finalArray[i]['shift_end'] &&
                        finalArray[i]['taradod'][y]['hourly_request_id'] != '' &&
                        (finalArray[i]['taradod'][y]['status'] == 'hourly_vacation' || finalArray[i]['taradod'][y]['status'] == 'hourly_mission')
                ) {

                } else {
                    final_taradods.add(finalArray[i]['taradod'][y])
                }
            }
            finalArray[i]['taradod'] = final_taradods


            //taradod_naghes = user_taradods.size() % 2
            if (finalArray[i] && !finalArray[i]['total_absent_daily']) finalArray[i]['total_absent_daily'] = ""
//            finalArray[i]['taradod_naghes'] = taradod_naghes
//            finalArray[i]['last_taradod_naghes'] = taradod_naghes ? user_taradods['tc' +user_taradods.size()] : ""
        }
        return [
                "state": 1,
                'data' : finalArray
        ]
    }
    catch (Exception e) {
        e.printStackTrace()
        return [
                "state": 0
        ]
    }
    finally {
        if (sql) sql.close()
    }
}

def dailyPresenceTime(times, date) {
    SimpleDateFormat formatter1 = new SimpleDateFormat("HH:mm")
    sumPresenceTime = formatter1.parse("00:00")
    for (k = 0; k < times.size(); k = k + 2) {
        if (times[k + 1] && times[k + 1]['TIME_'] != null) {
            ent = formatter1.parse(times[k]['TIME_'])
            out = formatter1.parse(times[k + 1]['TIME_'])
            use(groovy.time.TimeCategory) {
                interval = out.minus(ent)
                sumPresenceTime = sumPresenceTime.plus(interval)
            }
        }
    }
    return formatter1.format(sumPresenceTime)

}

def getDayOfWeekString(day) {
    switch (day) {
        case 1:
            return "یک‌شنبه";
        case 2:
            return "دوشنبه";
        case 3:
            return "سه‌شنبه";
        case 4:
            return "چهارشنبه";
        case 5:
            return "پنجشنبه";
        case 6:
            return "جمعه";
        case 7:
            return "شنبه";
        default:
            return "نامعلوم";
    }
}

def dailyKarkard() {
    try {
        employees=ec.service.sync().name("hr.employee.get#subEmployee").call()?.employee
        employeeIds = employees?.employeeId
        if (employeeIds.size() == 0) {
            description = ec.l10n.toPersianLocale("noEmpError")
            state = 0
            return
        }
        userList = ec.entity.find("UserAccount").condition([employeeId: employeeIds]).list()
        persons = employees
        if (persons.size() == 0) {
            description = ec.l10n.toPersianLocale("noEmpPersonError")
            state = 0
            return
        }
        nationalIds = persons.nationalId
        serviceCall = ec.service.sync().name("general.general.get#connection").call()
        state = serviceCall?.state

        if (state != 1) {
            description = ec.l10n.toPersianLocale("sqlConError")

            return
        }

        sql = serviceCall?.sql
        strNationalId = "(" + nationalIds.findAll { ele -> ele.toString().trim() != "" }.collect { ele -> Long.valueOf(ele) }.join(",") + ")"
        emps = sql.rows("SELECT * FROM EMPLOYEE WHERE PERS_NO IN " + strNationalId)
        empList = "(" + emps.collect { ele -> ele.get("EMP_NO") }.join(',') + ")"

        if (emps.size() == 0) {
            description = ec.l10n.toPersianLocale("noEmpPwkError")
            state = 0
            return
        }
        dateStr = DateTimeFormatter.ofPattern("yyyy-MM-DD")
        result = sql.rows('select dbo.Miladi2Pwkara(?) as pwKaraDate', date)

        if (result.size() == 0) {
            state = 0
            return
        }

        dateTime = result[0].get("pwKaraDate")

        karkard = sql.rows('select STATUS,dbo.TimeForm(TIME_) as TIME_,DATE_,EMP_NO from DataFile WHERE EMP_NO in ' + empList + ' and DATE_=' + dateTime)
        Map<Long, List> karkard = karkard.groupBy { ele -> (long) (ele.get("EMP_NO")) }
        karkardList = []

        userList.each { it ->
            employeeId = it?.employeeId
            nationalId = persons.find { ele -> ele.employeeId == employeeId }?.nationalId
            if (nationalId && nationalId.trim() != "") {
                nationalId = Long.valueOf(nationalId)
                empNo = emps.find { ele -> ele.get("PERS_NO") == nationalId }?.get("EMP_NO")

                if (empNo) {
                    karkardUser = karkard.find { ele, val -> ele == empNo }
                    val = karkardUser?.getValue() ?: []
                    entry = [:]
                    taradod = []
                    for (int i = 0; i < val.size(); i = i + 2) {
                        ent = [:]
                        if (val[i].get("STATUS") != 0) continue
                        if (val[i]) ent.from = val[i].get("TIME_")
                        if (val[i + 1]) ent.to = val[i + 1].get("TIME_")
                        ent.status = "present"
                        taradod.add(ent)
                    }
                    entry.taradod = taradod
                    entry.presentTime = dailyPresenceTime(val, date)
                    entry.user = it?.userFullName
                    karkardList.add(entry)
                }
            }
        }
        state = 1
        return karkardList;
    }
    catch (Exception e) {
        description = ec.l10n.toPersianLocale("generalError")
        e.printStackTrace()
        state = 0

    }
    finally {
        if (sql) sql.close()
    }

}

def karnamehList() {
    try {

        dates = convertToListV2(startDate, endDate)
        employee=ec.service.sync().name("hr.employee.get#subEmployee").call()?.employee
        if (employee.size() == 0) {
            description = ec.l10n.toPersianLocale("noEmpPersonError")
            state = 0
            return
        }
        employeeRel = ec.entity.find("EmplUnit").condition([employeeId: employee?.employeeId]).list()
        user = ec.entity.find("UserAccount").condition([employeeId: employee?.employeeId]).list()
        units=employeeRel.unitId
        nationalIds = employee?.nationalId?.findAll { ele -> ele.toString().trim() != "" }?.collect { ele -> Long.valueOf(ele) }
        serviceCall = ec.service.sync().name("general.general.get#connection").call()
        state = serviceCall?.state
        if (state != 1) {
            description = ec.l10n.toPersianLocale("sqlConError")
            return
        }
        sql = serviceCall?.sql
        query = "SELECT * FROM EMPLOYEE WHERE PERS_NO in (" + nationalIds.join(",") + ")"
        emps = sql.rows(query)
        karkards = []
        if (emps.size() == 0) {
            user.each { userEmp ->
                entry = [:]
                entry.user = userEmp.username
                entry.name = userEmp.userFullName
                karkards.add(entry)
            }
            state = 1
            return;
        }
        sysQuery = 'SELECT p.MIN_ABS2R, p.MIN_MOR2R,p.MOR_DAY_EZ,p.MAM_DAY_EZ,p.ABS2R_EZ,p.M_Y_ESTE,p.EMP_ESTE,p.MAX_DELAY,p.ABS_CARDEX FROM PARMFILE p WHERE p.ID = 1'
        settings = sql.rows(sysQuery)
        if (settings.size() == 0) {
            description = ec.l10n.toPersianLocale("settingError")
            state = 0
            return
        }
        setting = settings[0]
        groupIds = units
        unitEntity = ec.entity.find("Unit").condition([unitId: units]).list()
        allShiftGrps = sql.rows('SELECT * from GrpShift WHERE GrpShift_GrpNo in (' + groupIds.join(",") + ")")
        allShifts = sql.rows('select * from SHIFTS')
        user.each { userEmp ->
            empl = employee.find { ele -> ele.employeeId == userEmp.employeeId }
            userUnit = employeeRel.find { e -> e.employeeId == userEmp?.employeeId }?.unitId
            entry = [:]
            entry.user = userEmp.username
            entry.name = userEmp.userFullName
            if (!empl?.nationalId || empl?.nationalId?.toString()?.trim() == "") {
                karkards.add(entry)
                return;
            }
            emp = emps.find { it -> it.get("PERS_NO") == Long.valueOf(empl?.nationalId) }

            if (!emp) {
                karkards.add(entry)
                return
            }
            groupId = userUnit
            shiftType = unitEntity.find { e -> e.unitId == userUnit }?.shiftType
            if(groupId) {
                println(groupId)
                shiftGrps = allShiftGrps.findAll { ele -> ele.get("GrpShift_GrpNo") == Integer.valueOf(groupId) }
                karkardList = sql.rows('SELECT STATUS,TIME_,DATE_ FROM DataFile WHERE EMP_NO = ?', [emp.get('EMP_NO')])
                entry.data = calculate()?.data
            }
            karkards.add(entry)
        }
        state = 1
        return karkards
    }
    catch (Exception e) {
        e.printStackTrace()
        return ["state": 0]
    }
    finally {
        if (sql) {
            sql.close()
        }
    }
}

def getLocation() {


    locations = ec.entity.find("Entering").condition([username: username, date: date]).list()
    state = 1

}

def calculate() {


    try {
        sysQuery = 'SELECT p.MIN_ABS2R, p.MIN_MOR2R,p.MOR_DAY_EZ,p.MAM_DAY_EZ,p.ABS2R_EZ,p.M_Y_ESTE,p.EMP_ESTE,p.MAX_DELAY,p.ABS_CARDEX FROM PARMFILE p WHERE p.ID = 1'
        settings = sql.rows(sysQuery)
        if (settings.size() == 0) {
            state = 0
            return
        }
        setting = settings[0]
        shifts = [:]
        karkard = [:]
        dayHourStatus = [:]
        tatilat = []
        mamooriatDay = []
        morakhasiEstehghaghiDay = []
        morakhasiTashvighiDay = []
        morakhasiEstelajiDay = []
        dates.each { ele ->
            shamsiDate = new JalaliCalendar(ele)
            shamsiYear = shamsiDate.getYear()
            shamsiMonth = shamsiDate.getMonth()
            shamsiDay = shamsiDate.getDay()
            result = sql.rows('select dbo.Miladi2Pwkara(?) as pwKaraDate', ele)
            if (result.size() == 0) {
                return
            }
            result = result[0]
            day = result.get("pwKaraDate")
            dayStr = day.toString()
            if (dayStr.indexOf(".") != -1) dayStr = dayStr.tokenize(".")[0]
            queryShift = shiftGrps.findAll { it -> it != null && it.get("GrpShift_Year") == shamsiYear && it.get("GrpShift_Month") == shamsiMonth }

            if (queryShift.size() > 0) {
                shiftNumber = queryShift[0]["GrpShift_Day" + shamsiDay]
            }
            if (!shiftNumber) {
                return
            }
            shiftQuery = allShifts.findAll { it -> it != null && it.get("SHIFT_NO") == shiftNumber }
            karkard[day] = []
            if (!shiftQuery.isEmpty()) {
                if (shiftQuery[0].get('START_TIME') == 0) {
                    if (shiftQuery[0].get('END_TIME') == 0) {
                        shifts[day] = ['start': 0, 'end': 0, 'duration': 0, 'min_prev_moj': 0, 'max_prev_moj': 0, 'min_after_moj': 0, 'max_after_moj': 0]
                        tatilat.add(day)
                        entering = karkardList.findAll { it -> it != null && it.get("DATE_") == day }
                        entering.each { it ->
                            karkard[day].add(it.get("TIME_"))
                            if (it.get("STATUS") != 0) dayHourStatus[dayStr + it.get('TIME_').toString()] = it.get('STATUS')
                        }
                        if (karkard[day].size() % 2 != 0) karkard[day].remove(karkard[day].size() - 1)
                    } else {
                        shifts[day] = ['start'       : shiftQuery[0].get('START_TIME'), 'end': shiftQuery[0].get('END_TIME'), 'duration': shiftQuery[0].get('SHIF_TIME'), 'min_prev_moj': shiftQuery[0].get('MinPREV_MOJ'),
                                       'max_prev_moj': shiftQuery[0].get('MaxPREV_MOJ'), 'min_after_moj': shiftQuery[0].get('MinAFTER_MOJ'), 'max_after_moj': shiftQuery[0].get('MaxAFTER_MOJ')]
                        entering = karkardList.findAll { it -> it != null && it.get("DATE_") == day }
                        entering.each { it ->
                            karkard[day].add(it.get("TIME_"))
                            if (it.get("STATUS") != 0) dayHourStatus[dayStr + it.get('TIME_').toString()] = it.get('STATUS')
                        }
                        if (karkard[day].size() % 2 != 0) karkard[day].remove(karkard[day].size() - 1)
                    }
                } else {
                    shifts[day] = ['start'       : shiftQuery[0].get('START_TIME'), 'end': shiftQuery[0].get('END_TIME'), 'duration': shiftQuery[0].get('SHIF_TIME'), 'min_prev_moj': shiftQuery[0].get('MinPREV_MOJ'),
                                   'max_prev_moj': shiftQuery[0].get('MaxPREV_MOJ'), 'min_after_moj': shiftQuery[0].get('MinAFTER_MOJ'), 'max_after_moj': shiftQuery[0].get('MaxAFTER_MOJ')]
                    entering = karkardList.findAll { it -> it != null && it.get("DATE_") == day }
                    entering.each { it ->
                        karkard[day].add(it.get("TIME_"))
                        if (it.get("STATUS") != 0) dayHourStatus[dayStr + it.get('TIME_').toString()] = it.get('STATUS')
                    }
                    if (karkard[day].size() % 2 != 0) karkard[day].remove(karkard[day].size() - 1)
                }
            } else {
                if (shiftNumber == 50 || shiftNumber == 51 || shiftNumber == 52) {
                    shifts[day] = ['start': 0, 'end': 0, 'duration': 0, 'min_prev_moj': 0, 'max_prev_moj': 0, 'min_after_moj': 0, 'max_after_moj': 0]
                    tatilat.add(day)
                } else {
                    state = 0
                    return
                }
                entering = karkardList.findAll { it -> it != null && it.get("DATE_") == day }
                entering.each { it ->
                    karkard[day].add(it.get("TIME_"))
                    if (it.get("STATUS") != 0) dayHourStatus[dayStr + it.get('TIME_').toString()] = it.get('STATUS')
                }
                if (karkard[day].size() % 2 != 0) karkard[day].remove(karkard[day].size() - 1)
            }
            mormam = sql.rows("SELECT * FROM MOR_MAM WHERE EMP_NO=? AND S_DATE=? AND E_DATE!=0 UNION SELECT * FROM MOR_MAM WHERE EMP_NO=? AND (S_DATE <=? AND E_DATE>=?)AND S_DATE!=0 AND E_DATE!=0 AND S_DATE!=E_DATE", [emp.get('EMP_NO'), day, emp.get('EMP_NO'), day, day])
            arr = []
            if (mormam.size() > 0) {
                sDate = mormam[0].get('S_DATE')
                eDate = mormam[0].get('E_DATE')
                if (sDate && eDate) {
                    date = sDate
                    while (date <= eDate) {
                        arr.add(date)
                        date = date + 1
                    }
                } else {
                    arr.add(day)
                }
                if (mormam[0].get('TYP') == 54) mamooriatDay.addAll(arr)
                if (mormam[0].get('TYP') == 57) morakhasiEstehghaghiDay.addAll(arr)
                if (mormam[0].get('TYP') == 60) morakhasiEstelajiDay.addAll(arr)
                if (mormam[0].get('TYP') == 67 || mormam[0].get('TYP') == 64 || mormam[0].get('TYP') == 61) morakhasiTashvighiDay.addAll(arr)
            }
        }
        totalKasrkarHour = []
        totalMorakhasiEstehghaghiHour = []
        totalMamooriatHour = []
        totalMamooriatHourInOutOfShift = []
        totalHozour = []
        totalHozourInDay = [:]
        totalHozourInTatilat = []
        morakhasiEstehghaghiCount = 0
        morakhasiEstehghaghiDaysToHour = []
        morakhasiEstelajiCount = 0
        morakhasiEstelajiDaysToHour = []
        morakhasiTashvighiCount = 0
        morakhasiTashvighiDaysToHour = []
        mamooriatCount = 0
        mamooriatDaysToHour = []
        absentDaysToHour = []
        daysMovazzafi = 0
        daysMovazzafiToHour = []
        ezafekarBaMojavez = []
        ezafekarBedooneMojavez = []
        taradodNaghes = 0
        daysGheybatkarkardDescByHourlyKasrkar = 0
        karkard.each { day, hours ->
            dayStr = day.toString()
            if (dayStr.indexOf(".") != -1) dayStr = dayStr.tokenize(".")[0]
            ezafeKarEmrooz = 0
            karkardEmrooz = 0
            dailyKasrkarHour = [:]
            dailyMorakhasiHour = [:]
            dailyMorakhasiHour[day] = []
            dailyKasrkarHour[day] = []
            totalHozourInDay[day] = []
            ezafekarBaMojavezTodayBefore = []
            ezafekarBaMojavezTodayAfter = []

            if (morakhasiEstehghaghiDay.contains(day)) {
                morakhasiEstehghaghiCount++
                morakhasiEstehghaghiDaysToHour.add(shifts[day]['duration'])
            }
            if (morakhasiEstelajiDay.contains(day)) {
                morakhasiEstelajiCount++
                morakhasiEstelajiDaysToHour.add(shifts[day]['duration'])
            }
            if (morakhasiTashvighiDay.contains(day)) {
                morakhasiTashvighiCount++
                morakhasiTashvighiDaysToHour.add(shifts[day]['duration'])
            }
            if (mamooriatDay.contains(day)) {
                mamooriatCount++
                mamooriatDaysToHour.add(shifts[day]['duration'])
            }
            if (hours.size() > 1) {
                totalEzafekarInTodayBefore = 0
                totalEzafekarInTodayAfter = 0
                for (i = 0; i < hours.size(); i++) {
                    if (hours[i + 1]) {
                        h2 = hours[i + 1]
                        h1 = hours[i]
                        result = timeSub(h2, h1)
                        totalHozour.add(result)
                        totalHozourInDay[day].add(result)
                    } else {
                        if (hours.size() % 2 != 0 && (i + 1) == hours.size()) {
                            if (hours[i] < shifts[day]['end']) {
                                result = timeSub(shifts[day]['end'], hours[i])
                                if (shifts[day]['end'] != 2359) {
                                    totalKasrkarHour.add(result)
                                    dailyKasrkarHour[day].add(result)
                                }
                            } else {
                                result = timeSub(shifts[day]['end'], hours[i - 1])
                                if (shifts[day]['end'] != 2359) {
                                    totalKasrkarHour.add(result)
                                    dailyKasrkarHour[day].add(result)
                                }
                            }
                        }
                    }
                    if (hours[i] < shifts[day]['start']) {
                        if (hours[i + 1]) {
                            if (hours[i + 1] < shifts[day]['start']) next = hours[i + 1]
                            else next = shifts[day]['start']

                            diff = timeSub(next, hours[i])
                            key = dayStr + hours[i].toString()
                            stat = 0
                            if (dayHourStatus.keySet().contains(key)) stat = dayHourStatus[key]
                            if ((stat == 0 || stat == 5 || stat == 9 || stat == 2) && !dayReserve(day, morakhasiEstehghaghiDay, morakhasiEstelajiDay, morakhasiTashvighiDay)) {
                                if (stat == 5 || stat == 9) {
                                    if (!mamooriatDay.contains(day)) {
                                        ezafekarBaMojavez.add(diff)
                                        ezafekarBaMojavezTodayBefore.add(diff)
                                    }
                                } else if (stat == 2) {
                                    ezafekarBedooneMojavez.add(diff)
                                } else {
                                    if (shifts[day]['max_prev_moj'] > 0) {
                                        if (ezafekarBaMojavezTodayBefore.size() > 0) {
                                            for (k = 0; k < ezafekarBaMojavezTodayBefore.size(); k++) {
                                                totalEzafekarInTodayBefore = timeSum(totalEzafekarInTodayBefore, ezafekarBaMojavezTodayBefore[k])
                                            }
                                        }
                                        result = timeSum(totalEzafekarInTodayBefore, diff)
                                        if (Double.valueOf(result) < shifts[day]['max_prev_moj']) {
                                            result = timeSum(diff, 0)
                                            if (Double.valueOf(result) >= shifts[day]['min_prev_moj']) {
                                                if (!mamooriatDay.contains(day)) {
                                                    ezafekarBaMojavez.add(diff)
                                                    ezafekarBaMojavezTodayBefore.add(diff)
                                                }
                                            } else ezafekarBedooneMojavez.add(diff)
                                        } else {
                                            newDiff = timeSub(shifts[day]['max_prev_moj'], totalEzafekarInTodayBefore)
                                            result = timeSum(newDiff, 0)
                                            if (result >= shifts[day]['min_prev_moj']) {
                                                if (!mamooriatDay.contains(day)) {
                                                    ezafekarBaMojavez.add(newDiff)
                                                    ezafekarBaMojavezTodayBefore.add(newDiff)
                                                }
                                            } else ezafekarBedooneMojavez.add(newDiff)
                                            remain = timeSub(diff, newDiff)
                                            ezafekarBedooneMojavez.add(remain)
                                        }
                                    } else {
                                        if (mamooriatDay.contains(day) && tatilat.contains(day)) {
                                            ezafekarBaMojavez.add(diff)
                                        } else {
                                            if (shifts[day]['start'] != 0) ezafekarBedooneMojavez.add(diff)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (i >= 1 && hours[i - 1]) {
                        if (hours[i - 1] > shifts[day]['end']) {
                            if (i >= 2 && hours[i - 2]) {
                                if (hours[i - 2] < shifts[day]['end']) prev = shifts[day]['end']
                                else prev = hours[i - 2]
                                diff = timeSub(hours[i - 1], prev)
                                key = dayStr + hours[i].toString()
                                stat = 0
                                if (dayHourStatus.keySet().contains(key)) stat = dayHourStatus[key]

                                if ((stat == 0 || stat == 5 || stat == 9 || stat == 2) && !dayReserve(day, morakhasiEstehghaghiDay, morakhasiEstelajiDay, morakhasiTashvighiDay)) {
                                    if (stat == 5 || stat == 9) {
                                        ezafekarBaMojavez.add(diff)
                                        ezafekarBaMojavezTodayAfter.add(diff)
                                    } else if (stat == 2) {
                                        ezafekarBedooneMojavez.add(diff)
                                    } else {
                                        if (shifts[day]['max_after_moj'] > 0) {
                                            if (ezafekarBaMojavezTodayAfter.size() > 0) {
                                                for (k = 0; k < ezafekarBaMojavezTodayAfter.size(); k++) {
                                                    totalEzafekarInTodayAfter = timeSum(totalEzafekarInTodayAfter, ezafekarBaMojavezTodayAfter[k])
                                                }
                                            }
                                            result = timeSum(totalEzafekarInTodayAfter, diff)
                                            if (Double.valueOf(result) < shifts[day]['max_after_moj']) {
                                                ezafekarBaMojavez.add(diff)
                                                ezafekarBaMojavezTodayAfter.add(diff)
                                            } else {
                                                newDiff = timeSub(shifts[day]['max_after_moj'], totalEzafekarInTodayAfter)
                                                ezafekarBaMojavez.add(newDiff)
                                                ezafekarBaMojavezTodayAfter.add(newDiff)
                                                remain = timeSub(diff, newDiff)
                                                ezafekarBedooneMojavez.add(remain)
                                            }
                                        } else {
                                            if (mamooriatDay.contains(day) && tatilat.contains(day)) {
                                                ezafekarBaMojavez.add(diff)
                                            } else {
                                                if (shifts[day]['start'] != 0) {
                                                    ezafekarBedooneMojavez.add(diff)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (i == 0 && hours[i] > shifts[day]['start']) {
                        if (hours[i] < shifts[day]['end']) {
                            diff = timeSub(hours[i], shifts[day]['start'])
                            key = dayStr + hours[i].toString()
                            stat = 0
                            if (dayHourStatus.keySet().contains(key)) stat = dayHourStatus[key]

                            if (stat == 0 && !dayReserve(day, morakhasiEstehghaghiDay, morakhasiEstelajiDay, morakhasiTashvighiDay, mamooriatDay)) {
                                if (shifts[day]['end'] != 2359) {
                                    totalKasrkarHour.add(diff)
                                    dailyKasrkarHour[day].add(diff)
                                }
                            }

                            if (stat == 17 && !dayReserve(day, morakhasiEstehghaghiDay, morakhasiEstelajiDay, morakhasiTashvighiDay, mamooriatDay)) {
                                totalMorakhasiEstehghaghiHour.add(diff)
                                dailyMorakhasiHour[day].add(diff)
                            }

                            if (stat == 9 && !dayReserve(day, morakhasiEstehghaghiDay, morakhasiEstelajiDay, morakhasiTashvighiDay, mamooriatDay)) {
                                totalMamooriatHour.add(diff)
                            }
                        }
                    } else {
                        if (i != 0 && hours[i] < shifts[day]['end']) {
                            diff = timeSub(hours[i], hours[i - 1])
                            key = dayStr + hours[i - 1].toString()
                            stat = 0
                            if (dayHourStatus.keySet().contains(key)) stat = dayHourStatus[key]
                            if (stat == 0 && !dayReserve(day, morakhasiEstehghaghiDay, morakhasiEstelajiDay, morakhasiTashvighiDay, mamooriatDay)) {
                                if (i >= 1 && hours[i - 1] >= shifts[day]['start'] && hours[i] <= shifts[day]['end']) {
                                    if (shifts[day]['end'] != 2359) {
                                        totalKasrkarHour.add(diff)
                                        dailyKasrkarHour[day].add(diff)
                                    }
                                }

                                if (i >= 1 && hours[i - 1] < shifts[day]['start'] && hours[i] >= shifts[day]['start']) {

                                    diff = timeSub(hours[i], shifts[day]['start'])
                                    if (shifts[day]['end'] != 2359) {
                                        totalKasrkarHour.add(diff)
                                        dailyKasrkarHour[day].add(diff)
                                    }
                                }

                                if (i >= 1 && hours[i - 1] < shifts[day]['end'] && hours[i] >= shifts[day]['end']) {
                                    diff = timeSub(hours[i], shifts[day]['end'])
                                    if (shifts[day]['end'] != 2359) {
                                        totalKasrkarHour.add(diff)
                                        dailyKasrkarHour[day].add(diff)
                                    }
                                }
                            }

                            if (stat == 17 && !dayReserve(day, morakhasiEstehghaghiDay, morakhasiEstelajiDay, morakhasiTashvighiDay, mamooriatDay)) {
                                totalMorakhasiEstehghaghiHour.add(diff)
                                dailyMorakhasiHour[day].add(diff)
                            }

                            if (stat == 9 && !dayReserve(day, morakhasiEstehghaghiDay, morakhasiEstelajiDay, morakhasiTashvighiDay, mamooriatDay)) {
                                totalMamooriatHour.add(diff)
                            }
                        }
                    }
                    i = i + 1
                }

                if (hours.size() >= 1 && hours.size() % 2 == 0 && hours[hours.size() - 1] > shifts[day]['end']) {
                    if (hours.size() >= 2 && hours[hours.size() - 2] < shifts[day]['end']) prev = shifts[day]['end']
                    else if (hours.size() >= 2) prev = hours[hours.size() - 2]
                    diff = timeSub(hours[hours.size() - 1], prev)
                    key = dayStr + (hours[i].toString())
                    stat = 0
                    if (dayHourStatus.keySet().contains(key)) stat = dayHourStatus[key]

                    if ((stat == 0 || stat == 5 || stat == 9 || stat == 2) && !dayReserve(day, morakhasiEstehghaghiDay, morakhasiEstelajiDay, morakhasiTashvighiDay)) {
                        if (stat == 5 || stat == 9) {
                            ezafekarBaMojavez.add(diff)
                            ezafekarBaMojavezTodayAfter.add(diff)
                        } else if (stat == 2) ezafekarBedooneMojavez.add(diff)
                        else {
                            if (shifts[day]['max_after_moj'] > 0) {
                                if (ezafekarBaMojavezTodayAfter.size() > 0) {
                                    for (k = 0; k < ezafekarBaMojavezTodayAfter.size(); k++) {
                                        totalEzafekarInTodayAfter = timeSum(totalEzafekarInTodayAfter, ezafekarBaMojavezTodayAfter[k])
                                    }
                                }
                                result = timeSum(totalEzafekarInTodayAfter, diff)
                                if (Double.valueOf(result) < shifts[day]['max_after_moj']) {
                                    ezafekarBaMojavez.add(diff)
                                    ezafekarBaMojavezTodayAfter.add(diff)
                                } else {
                                    newDiff = timeSub(shifts[day]['max_after_moj'], totalEzafekarInTodayAfter)
                                    ezafekarBaMojavez.add(newDiff)
                                    ezafekarBaMojavezTodayAfter.add(newDiff)
                                    remain = timeSub(diff, newDiff)
                                    ezafekarBedooneMojavez.add(remain)
                                }
                            } else {

                                if (mamooriatDay.contains(day) && tatilat.contains(day)) {
                                    ezafekarBaMojavez.add(diff)
                                } else {
                                    if (shifts[day]['start'] != 0) {
                                        ezafekarBedooneMojavez.add(diff)
                                    }
                                }
                            }
                        }
                    }
                }

                if (hours.size() >= 1 && hours.size() % 2 == 0 && hours[hours.size() - 1] < shifts[day]['end']) {
                    diff = timeSub(shifts[day]['end'], hours[hours.size() - 1])

                    key = dayStr + hours[hours.size() - 1].toString()
                    stat = 0

                    if (dayHourStatus.keySet().contains(key)) stat = dayHourStatus[key]
                    if (stat == 0 && shifts[day]['end'] != 2359 && !dayReserve(day, morakhasiEstehghaghiDay, morakhasiEstelajiDay, morakhasiTashvighiDay, mamooriatDay)) {
                        totalKasrkarHour.add(diff)
                        dailyKasrkarHour[day].add(diff)
                    }
                    if (stat == 17 && !dayReserve(day, morakhasiEstehghaghiDay, morakhasiEstelajiDay, morakhasiTashvighiDay, mamooriatDay)) {
                        totalMorakhasiEstehghaghiHour.add(diff)
                        dailyMorakhasiHour[day].add(diff)
                    }
                    if (stat == 9 && !dayReserve(day, morakhasiEstehghaghiDay, morakhasiEstelajiDay, morakhasiTashvighiDay, mamooriatDay)) {
                        totalMamooriatHour.add(diff)
                    }
                }

            } else {
                if (hours.size() == 1) {
                    taradod_naghes++
                    if (hours[0] > shifts[day]['start'] && hours[0] <= shifts[day]['end']) {
                        result = timeSub(hours[0], shifts[day]['start'])
                        totaKasrkarHour.add(result)
                        dailyKasrkarHour[day].add(result)
                    }
                }

                if (!dayReserve(day, morakhasiEstehghaghiDay, morakhasiEstelajiDay, morakhasiTashvighiDay, mamooriatDay)) {
                    if (!tatilat.contains(day)) absentDaysToHour.add(shifts[day]['duration'])
                }
            }

            if (!tatilat.contains(day)) {
                daysMovazzafi++
                daysMovazzafiToHour.add(shifts[day]['duration'])
            }

            if (tatilat.contains(day) && !mamooriatDay.contains(day) && hours.size() > 0 && totalHozour.size() >= 1 && totalHozour[totalHozour.size() - 1]) {
                totalHozourInTatilat.add(totalHozour[totalHozour.size() - 1])
            }

            for (i = 0; i < ezafekarBaMojavezTodayBefore.size(); i++) {
                ezafeKarEmrooz = timeSum(ezafeKarEmrooz, ezafekarBaMojavezTodayBefore[i])
            }
            for (i = 0; i < ezafekarBaMojavezTodayAfter.size(); i++) {
                ezafeKarEmrooz = timeSum(ezafeKarEmrooz, ezafekarBaMojavezTodayAfter[i])
            }
            for (i = 0; i < totalHozourInDay[day].size(); i++) {
                karkardEmrooz = timeSum(karkardEmrooz, totalHozourInDay[day][i])
            }

            karkardEmroozBedooneEzafekar = timeSub(karkardEmrooz, ezafeKarEmrooz)

            if (setting['MOR_DAY_EZ'] == true) {
                if ((morakhasiEstehghaghiDay.contains(day) || morakhasiEstelajiDay.contains(day) || morakhasiTashvighiDay.contains(day)) && totalHozourInDay[day].size() > 0) {
                    ezafekarBaMojavez.add(karkardEmroozBedooneEzafekar)
                }
            }
            if (setting['MAM_DAY_EZ'] == true) {
                if (mamooriatDay.contains(day) && totalHozourInDay[day].size() > 0) {
                    ezafekarBaMojavez.add(karkardEmroozBedooneEzafekar)
                }
            }
            if (setting['MIN_ABS2R']) {
                totalDailyKasrkar = 0
                if (dailyKasrkarHour[day].size() > 0) {
                    for (i = 0; i < dailyKasrkarHour[day].size(); i++) {
                        totalDailyKasrkar = timeSum(totalDailyKasrkar, dailyKasrkarHour[day][i])
                    }
                }

                if (Double.valueOf(totalDailyKasrkar) > setting['MIN_ABS2R']) {
                    daysGheybatkarkardDescByHourlyKasrkar++
                    totalKasrkarHour.removeAll(dailyKasrkarHour[day])
                    if (setting['ABS2R_EZ']) {
                        ezafekarBaMojavez.add(karkardEmroozBedooneEzafekar)
                    }
                }
            }
            if (setting['MIN_MOR2R']) {
                totalDailyMorakhasi = 0
                if (dailyMorakhasiHour[day]?.size() > 0) {
                    for (i = 0; i < dailyMorakhasiHour[day].size(); i++) {
                        totalDailyMorakhasi = timeSum(totalDailyMorakhasi, dailyMorakhasiHour[day][i])
                    }
                }

                if (Double.valueOf(totalDailyMorakhasi) > setting['MIN_MOR2R']) {
                    daysGheybatkarkardDescByHourlyKasrkar++
                    totalMorakhasiEstehghaghiHour.removeAll(dailyMorakhasiHour[day])
                    if (setting['MOR_DAY_EZ']) {
                        ezafekarBaMojavez.add(karkardEmroozBedooneEzafekar)
                    }
                }
            }
        }

        totalEzafekarBaMojavezHourValue = 0

        for (i = 0; i < ezafekarBaMojavez.size(); i++) {
            totalEzafekarBaMojavezHourValue = timeSum(totalEzafekarBaMojavezHourValue, ezafekarBaMojavez[i])
        }
        hozourDarTatilat = 0
        for (i = 0; i < totalHozourInTatilat.size(); i++) {
            hozourDarTatilat = timeSum(hozourDarTatilat, totalHozourInTatilat[i])
        }

        totalEzafekarBedooneMojavezHourValue = 0
        for (i = 0; i < ezafekarBedooneMojavez.size(); i++) {
            totalEzafekarBedooneMojavezHourValue = timeSum(totalEzafekarBedooneMojavezHourValue, ezafekarBedooneMojavez[i])
        }
        daysMovazzafiToHourValue = 0
        for (i = 0; i < daysMovazzafiToHour.size(); i++) {
            daysMovazzafiToHourValue = timeSum(daysMovazzafiToHourValue, daysMovazzafiToHour[i])
        }

        absentDaysToHourValue = 0
        for (i = 0; i < absentDaysToHour.size(); i++) {
            absentDaysToHourValue = timeSum(absentDaysToHourValue, absentDaysToHour[i])
        }

        morakhasiEstehghaghiDaysToHourValue = 0
        for (i = 0; i < morakhasiEstehghaghiDaysToHour.size(); i++) {
            morakhasiEstehghaghiDaysToHourValue = timeSum(morakhasiEstehghaghiDaysToHourValue, morakhasiEstehghaghiDaysToHour[i])
        }

        morakhasiEstelajiDaysToHourValue = 0
        for (i = 0; i < morakhasiEstelajiDaysToHour.size(); i++) {
            morakhasiEstelajiDaysToHourValue = timeSum(morakhasiEstelajiDaysToHourValue, morakhasiEstelajiDaysToHour[i])
        }

        morakhasiTashvighiDaysToHourValue = 0
        for (i = 0; i < morakhasiTashvighiDaysToHour.size(); i++) {
            morakhasiTashvighiDaysToHourValue = timeSum(morakhasiTashvighiDaysToHourValue, morakhasiTashvighiDaysToHour[i])
        }

        mamooriatDaysToHourValue = 0
        for (i = 0; i < mamooriatDaysToHour.size(); i++) {
            mamooriatDaysToHourValue = timeSum(mamooriatDaysToHourValue, mamooriatDaysToHour[i])
        }

        c1 = []
        karkard.each { key, val ->
            if (val?.size() > 0) {
                c1.add(key)
            }
        }

        totalWorkedDay = (tatilat + morakhasiTashvighiDay + morakhasiEstehghaghiDay + morakhasiEstelajiDay + mamooriatDay + c1).toSet()

        userKasrkarHour = 0
        for (i = 0; i < totalKasrkarHour.size(); i++) {
            userKasrkarHour = timeSum(userKasrkarHour, totalKasrkarHour[i])
        }

        totalEstehghaghiHour = 0
        for (i = 0; i < totalMorakhasiEstehghaghiHour.size(); i++) {
            totalEstehghaghiHour = timeSum(totalEstehghaghiHour, totalMorakhasiEstehghaghiHour[i])
        }

        totalMissionHour = 0
        if (totalMamooriatHour.size() > 0 || totalMamooriatHourInOutOfShift.size() > 0) {
            for (i = 0; i < totalMamooriatHour.size(); i++) {
                totalMissionHour = timeSum(totalMissionHour, totalMamooriatHour[i])
            }
            totalHozour.add(totalMissionHour)
            for (i = 0; i < totalMamooriatHourInOutOfShift.size(); i++) {
                totalMissionHour = timeSum(totalMissionHour, totalMamooriatHourInOutOfShift[i])
            }
        }

        totalPresentHour = 0
        for (i = 0; i < totalHozour.size(); i++) {
            totalPresentHour = timeSum(totalPresentHour, totalHozour[i])
        }
        finalArray = [:]
        finalArray['movazzafi'] = ['days': '0', 'days_in_hour': '0']

        dayKarkard = daysMovazzafi - ((karkard.size() - totalWorkedDay.size() + taradodNaghes))
        finalArray['karkard'] = ['days': dayKarkard - daysGheybatkarkardDescByHourlyKasrkar, 'hours_total': pwTime2pbTime(totalPresentHour)]

        if (hozourDarTatilat) {
            totalEzafekarBaMojavezHourValue = timeSum(hozourDarTatilat, totalEzafekarBaMojavezHourValue)
        }


        if (setting['MAX_DELAY'] && Integer.valueOf(userKasrkarHour) <= Integer.valueOf(setting['MAX_DELAY'])) {
            if (setting['ABS_CARDEX'] == 2) userKasrkarHour = 0
            if (setting['ABS_CARDEX'] == 3) {
                totalEzafekarBaMojavezHourValue = timeSub(totalEzafekarBaMojavezHourValue, userKasrkarHour)
                userKasrkarHour = 0
            }
        }
        finalArray['ezafeh_kar'] = ['ba_mojavez': pwTime2pbTime(totalEzafekarBaMojavezHourValue), 'bedoone_mojavez': pwTime2pbTime(totalEzafekarBedooneMojavezHourValue)]


        vacation_total = timeSum(timeSum(timeSum(totalEstehghaghiHour, morakhasiEstehghaghiDaysToHourValue), morakhasiEstelajiDaysToHourValue), morakhasiTashvighiDaysToHourValue)

        finalArray['morakhasi'] = [
                'hours'           : pwTime2pbTime(totalEstehghaghiHour),
                'estehghaghi_days': morakhasiEstehghaghiCount,
                'estelaji_days'   : morakhasiEstelajiCount,
                'tashvighi_days'  : morakhasiTashvighiCount,
        ]

        finalArray['gheybat_kasrekar'] = [
                'hours': pwTime2pbTime(userKasrkarHour),
                'days' : karkard.size() - totalWorkedDay.size() + taradodNaghes + daysGheybatkarkardDescByHourlyKasrkar
        ]


        finalArray['mamooriat'] = [
                'hours': pwTime2pbTime(totalMissionHour),
                'days' : mamooriatCount,
        ]

        finalArray['morakhasi_remain'] = [
                'hours': "0",
                'days' : "0",
        ]
        if ("Hour".equals(shiftType)) {
            finalArray['gheybat_kasrekar'] = [
                    'hours': 0,
                    'days' : 0
            ]
            finalArray['ezafeh_kar'] = [
                    'ba_mojavez'     : 0,
                    'bedoone_mojavez': 0
            ]
        }
        return ["state": 1, 'data': finalArray]
    }
    catch (Exception e) {
        e.printStackTrace()
    }
}

