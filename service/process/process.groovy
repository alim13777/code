import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity;
import groovy.json.JsonOutput;
import groovy.json.JsonSlurper;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.client.methods.HttpGet
import org.apache.http.entity.StringEntity;
import groovy.json.JsonOutput;
import groovy.json.*
import java.time.*
import org.apache.commons.validator.routines.checkdigit.VerhoeffCheckDigit;
import groovy.json.*
import java.text.SimpleDateFormat

def execute_post() {


    bpmsEngine = org.moqui.util.SystemBinding.getPropOrEnv('BPMS_enginePath');
    httpOrhttps = org.moqui.util.SystemBinding.getPropOrEnv('httpOrhttps');
    host = ec.web.getWebappRootUrl(true, true).split("http")[1]
    urlAddress = httpOrhttps + host + bpmsEngine + url;

    basicToken = 'Basic ' + ec.user.getPreference('basicToken')
    //basicToken='Basic '+org.moqui.util.SystemBinding.getPropOrEnv('bpmsToken')
    postData = JsonOutput.toJson(postData)
    HttpClient client = HttpClientBuilder.create().build()
    HttpPost request = new HttpPost(urlAddress)
    request.addHeader("Authorization", basicToken)
    request.addHeader("Content-type", "application/json")
    request.setEntity(new StringEntity(postData, "UTF-8"))
    HttpResponse response = client.execute(request)
    responseCode = response.getStatusLine().getStatusCode()

    if (responseCode == 401) {
        state = 0;
        return;
    }
    BufferedReader rd;
    if (response?.getEntity()?.getContent()) rd = new BufferedReader(new InputStreamReader(response?.getEntity()?.getContent(), "UTF-8"));
    StringBuffer result = new StringBuffer();
    String line = "";
    if (rd) while ((line = rd.readLine()) != null) {
        result.append(line);
    }
    def jsonSlurper = new groovy.json.JsonSlurper()
    if (result) data = jsonSlurper.parseText(result.toString())
    if (!(responseCode == 200 || responseCode == 204)) {
        state = 0
        description = data?.message;
        return
    }
    state = 1

}

def execute_get(){
    bpmsEngine=org.moqui.util.SystemBinding.getPropOrEnv('BPMS_enginePath');
    httpOrhttps=org.moqui.util.SystemBinding.getPropOrEnv('httpOrhttps');
    host=ec.web.getWebappRootUrl(true,true).split("http")[1]
    urlAddress=httpOrhttps+host+bpmsEngine+url;

    basicToken='Basic '+ec.user.getPreference('basicToken')
    //basicToken='Basic '+org.moqui.util.SystemBinding.getPropOrEnv('bpmsToken')
    HttpClient client=HttpClientBuilder.create().build()
    HttpGet request=new HttpGet(urlAddress)
    request.addHeader("Authorization",basicToken)
    HttpResponse response=client.execute(request)
    if (response.getStatusLine().getStatusCode() == 401) {
        state=0;
        description="Authorization"
        return;
    }
    BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
    StringBuffer result = new StringBuffer();
    String line = "";
    while ((line = rd.readLine()) != null) {result.append(line);}
    def jsonSlurper = new groovy.json.JsonSlurper()
    data = jsonSlurper.parseText(result.toString())
    state=1
}

def start_process(){

    try{
        if(variables instanceof String){
            converter=new JsonSlurper()
            variables=converter.parseText(variables)
        }
        if(!variables)variables=[:]
        serviceCall=ec.service.sync().name("process.process.create#businessKey").parameters([processDefinitionKey:processDefinitionKey]).call()
        businessKey=serviceCall?.businessKey
        if(!businessKey){
            state=0;
            description=ec.l10n.toPersianLocale("fetchDataError")
            return;
        }
        if(type!="screen"){
            requestData=[:]
            employeeId=ec.entity.find("UserAccount").condition(["username":variables["userId"]]).one()?.employeeId
            requestData.employeeId=employeeId
            requestData.businessKey=businessKey ;
            requestData.type=type;
            requestData.clientId=variables["client_id"]
            requestData.startDate=java.sql.Date.valueOf(LocalDate.now(ZoneId.of("Asia/Tehran")))
            requestData.status='baresiDarkhast';
            serviceCall=ec.service.sync().name("create#Request").parameters(requestData).call()
            requestId=serviceCall.requestId
        }
        processVariable=variables
        processVariable.employeeId=employeeId
        if(file){
            serviceCall=ec.service.sync().name("general.file.create#physicalFile").parameters([file:file,extension:fileType,type:fileType]).call()
            fileId=serviceCall?.fileId;
            processVariable.fileId=fileId
        }
        processVariable.businessKey=businessKey
        processVariable.basicToken='Basic '+ec.user.getPreference("basicToken")
        processVariable.requestId=requestId
        processVariable.type=type
        processVariable.businessKey=businessKey
        service=org.camunda.bpm.BpmPlatform.getDefaultProcessEngine().getRuntimeService()
        processInstance = service.startProcessInstanceByKey(processDefinitionKey, businessKey, processVariable);
        instanceId=processInstance.getId();
        state=1
    }
    catch(Exception e){
        e.printStackTrace()
        state=0
    }
}

def get_process(){
    try{
        url="/process-definition/key/"+definitionKey
        serviceCall=ec.service.sync().name("process.api.execute#get").parameters([url:url]).call()
        state=serviceCall?.state
        process=serviceCall?.data
    }
    catch(Exception e){
        e.printStackTrace()
        state=0
    }
}

def create_businessKey(){
    try{
        serviceCall=ec.service.sync().name("create#bpms.businessKey").parameters([processDefinitionKey:processDefinitionKey]).call()
        generateServiceCall=ec.service.sync().name("process.process.generate#code").parameters([id:serviceCall.processBusinessKeyId,length:14,operation:01]).call()
        ec.service.sync().name("update#bpms.businessKey").parameters([processBusinessKeyId:serviceCall.processBusinessKeyId,businessKey:generateServiceCall.code,processBusinessKeyCode:generateServiceCall.code,processBusinessKeyCode:generateServiceCall.code,processDefinitionId:processDefinitionId,processDefinitionKey:processDefinitionKey]).call()
        businessKey=generateServiceCall.code
        state=1
    }
    catch(Exception e){
        e.printStackTrace()
        state=0
    }
}

def generate_code(){
    VerhoeffCheckDigit verhoeffCheckDigit = new VerhoeffCheckDigit();
    def zero = "";
    code=''
    zero = zero.padLeft(length-2,'0')
    code += (zero + id).substring((id + "").length())
    code = verhoeffCheckDigit.calculate(code) + operation + code;
    code += verhoeffCheckDigit.calculate(code);
}

def trigger_message(){
    try{
        data=[:]
        if(variables instanceof String){
            converter=new JsonSlurper()
            variables=converter.parseText(variables)
        }
        if(requestId && !businessKey){
            request=ec.entity.find("Request").condition([requestId:requestId]).one()
            businessKey=request?.businessKey
        }
        data.messageName=messageName;
        if(instanceId)data.processInstanceId=instanceId;
        if(businessKey)data.businessKey=businessKey
        processVariable=[:]
        if(variables){
            if(!variables.end_date)variables.end_date="";
            variables.each{k,v->
                processVariable.put(k,["value":v])
            }
        }
        processVariable.newFile=["value":false]
        if(file){
            serviceCall=ec.service.sync().name("general.file.create#physicalFile").parameters([file:file,extension:fileType]).call()
            fileId=serviceCall?.fileId;
            processVariable.fileId=["value":fileId]
            processVariable.newFile=["value":true]
        }
        processVariable.basicToken=["value":'Basic '+ec.user.getPreference('basicToken')]
        data.processVariables=processVariable
        url="/message"
        triggerMsg=ec.service.sync().name("process.api.execute#post").parameters([postData:data,url:url]).call()
        state=triggerMsg?.state
    }
    catch(Exception e){
        e.printStackTrace()
        state=0
    }
}

def list_process(){
    url="/history/process-instance"
    data=[:]
    if(processInstanceId)data.processInstanceIds=processInstanceId;
    if(businessKey)data.processInstanceBusinessKeyLike=businessKey

    process=ec.service.sync().name("process.api.execute#post").parameters([url:url,postData:data]).call()
    process=process?.data
    state=process?.state
    description=process?.description
}

def get_task(){
    try{
        taskService=org.camunda.bpm.BpmPlatform.getDefaultProcessEngine().getTaskService()
        tasks = taskService.createTaskQuery().initializeFormKeys().taskAssignee(ec.user.username).list();
        task=[]
        tasks.each{it->
            entry=[:]
            entry.name=it.name;
            entry.process=it?.getProcessDefinitionId()
            entry.created =it.createTime ;
            entry.id =it.id ;
            entry.formKey =it.getFormKey() ;
            task.add(entry);
        }
        task=task.sort{it->it.created}.reverse()
        state=1
    }
    catch(Exception e){
        e.printStackTrace()
        state=0;
    }
}

def get_form(){
    try{
        url=""
        ec.service.sync().name("process.api.execute#get").parameters([url:url]).call()
        state=1
    }
    catch(Exception e){
        state=0
    }
}

def get_variables(){
    try{
        variable=org.camunda.bpm.BpmPlatform.getDefaultProcessEngine().getTaskService().getVariables(taskId)
        state=1
    }
    catch(Exception e){
        state=0
    }
}

def complete_task(){
    try{
        variables.put("basicToken","Basic "+ec.user.getPreference("basicToken"))
        taskService=org.camunda.bpm.BpmPlatform.getDefaultProcessEngine().getTaskService()
        taskService.complete(taskId,variables)
        state=1
    }
    catch(Exception e){
        e.printStackTrace()
        state=0
    }
}

def get_task_history(){

    url="/history/task"

    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    if(startedAfter) startedAfter = df.format(startedAfter);
    if(startedBefore) startedBefore = df.format(startedBefore);
    if(finishedAfter) finishedAfter = df.format(finishedAfter);
    if(finishedBefore) finishedBefore = df.format(finishedBefore);

    data=[:]
    data.firstResult=firstResult?firstResult:0
    data.maxResults=maxResult?maxResult:1000
    if(!username)username=ec.user.username
    data.taskAssignee=username;
    if(taskName)data.taskNameLike=taskName
    if(startedAfter)data.startedAfter=startedAfter
    if(startedBefore)data.startedBefore=startedBefore
    if(finishedAfter)data.finishedAfter=finishedAfter
    if(finishedBefore)data.finishedBefore=finishedBefore
    if(processDefinitionKey)data.processDefinitionKey=processDefinitionKey
    if(businessKey)data.processInstanceBusinessKey=businessKey
    if(businessKeyList)data.processInstanceBusinessKeyIn=businessKeyList.findAll{ele->ele!=null}
    task=ec.service.sync().name("process.api.execute#post").parameters([url:url,postData:data]).call()
    if(task?.state!=1){
        status="0"
        description=task?.description
        return;
    }
    task=task?.data;
    if(task?.size()==0){
        status="1"
        return;
    }
    processInstanceId=task?.processInstanceId;
    process=ec.service.sync().name("processService.list#process").parameters([processInstanceId:processInstanceId,businessKey:businessKey]).call()
    status=process?.state
    errorDescription=process?.description
    process=process?.process;
}