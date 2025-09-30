import java.sql.Timestamp
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalUnit
import java.sql.Date

def search() {

    data=["isDeleted":"N"]
    if(type)data.type=type
    discount = ec.entity.find("Discount").condition(data).list()
    state = 1

}

def create(){

   try {
       if(date) {
           expireDate = Date.valueOf(LocalDate.parse(date,DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX",Locale.forLanguageTag("fa"))))
       }
       call=ec.service.sync().name("create#Discount").parameters(context).call()
       discountId=call?.discountId
      state=1
   }
   catch (Exception e){
      e.printStackTrace()
      state=0
   }

}

def update(){

   try {
       if(date) {
           d = Instant.ofEpochSecond(date);
           expireDate=Date.valueOf(d.atZone(ZoneId.of("Asia/Tehran")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
       }
       ec.service.sync().name("update#Discount").parameters(context).call()
       state=1
   }
   catch (Exception e){
      e.printStackTrace()
      state=0
   }

}

def delete(){

    try {
        ec.service.sync().name("update#Discount").parameters([discountId: discountId, isDeleted: "Y"]).call()
        state=1
    }
    catch (Exception e){
        state=0
    }

}