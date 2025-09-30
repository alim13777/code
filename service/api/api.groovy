
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.client.methods.HttpGet
import groovy.sql.Sql;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpPut
import org.apache.http.client.methods.HttpPatch
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.entity.StringEntity;
import groovy.json.JsonOutput;
import groovy.json.JsonSlurper;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.ClientProtocolException;
import java.sql.DriverManager
import java.sql.Connection;
import groovy.sql.Sql;
import java.io.BufferedReader

def execute_post_zarinpal() {
    try {
        urlAddress = org.moqui.util.SystemBinding.getPropOrEnv('zarinPalUrl') + url;
        data.merchant_id=org.moqui.util.SystemBinding.getPropOrEnv('zarinPalId')
        postData = JsonOutput.toJson(data)
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost request = new HttpPost(urlAddress);
        request.setEntity(new StringEntity(postData, "UTF-8"))
        request.addHeader("Accept", "application/json; q=0.5");
        request.addHeader("Content-Type", "application/json");
        HttpResponse response = client.execute(request);
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        StringBuffer result = new StringBuffer();
        String line = "";
        statusCode = response.getStatusLine().getStatusCode()
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        def jsonSlurper = new groovy.json.JsonSlurper()
        if (result && result != "" ) data = jsonSlurper.parseText(result.toString())
        state=1
    }
    catch (Exception e) {
        e.printStackTrace()
        state = 0
    }
}

def execute_get(){

    try{
        urlAddress=org.moqui.util.SystemBinding.getPropOrEnv('_apiUrl')+url;
        if(version=="v2")urlAddress=org.moqui.util.SystemBinding.getPropOrEnv('_apiUrl_v2')+url;
        if(version=="v3")urlAddress=org.moqui.util.SystemBinding.getPropOrEnv('_apiUrl_v3')+url;
        if(!token)token="Bearer "+ec.user.getPreference("apiToken")
        if(!token)token=org.moqui.util.SystemBinding.getPropOrEnv('_systemToken')
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(urlAddress);
        request.addHeader("Accept", "application/json; q=0.5");
        request.addHeader("Content-Type", "application/json");
        request.addHeader("Authorization", token);
        HttpResponse response = client.execute(request);

        BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        StringBuffer result = new StringBuffer();
        String line = "";
        statusCode=response.getStatusLine().getStatusCode()
        while ((line = reader.readLine()) != null) {result.append(line);}
        def jsonSlurper = new groovy.json.JsonSlurper()
        data=[]
        if(statusCode!=200 || data?.state=="err" || data?.state=="invalid_token"){
            state=0
            description=data?.description;
            data=[]
            return;
        }
        if(result!=null && result?.toString().trim()!=""){
            data = jsonSlurper.parseText(result.toString())
        }
        description=data?.description;
        state=1
    }
    catch(org.apache.http.conn.HttpHostConnectException e){
        description=ec.l10n.toPersianLocale("apiConnectionError")
        state=500
    }
    catch(Exception e){
        e.printStackTrace()
        state=0
    }
}

def execute_patch(){
    try{
        urlAddress=org.moqui.util.SystemBinding.getPropOrEnv('_apiUrl')+url;
        if(version=="v2")urlAddress=org.moqui.util.SystemBinding.getPropOrEnv('_apiUrl_v2')+url;
        if(version=="v3")urlAddress=org.moqui.util.SystemBinding.getPropOrEnv('_apiUrl_v3')+url;
        if(!token)token="Bearer "+ec.user.getPreference("apiToken")
        postData=JsonOutput.toJson(postData)

        HttpClient client = HttpClientBuilder.create().build();
        HttpPatch request = new HttpPatch(urlAddress);
        request.setEntity(new StringEntity(postData,"UTF-8"))
        request.addHeader("Accept", "application/json; q=0.5");
        request.addHeader("Content-Type", "application/json");
        request.addHeader("Authorization", token);
        HttpResponse response = client.execute(request);

        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        StringBuffer result = new StringBuffer();
        String line = "";
        statusCode=response.getStatusLine().getStatusCode()
        while ((line = rd.readLine()) != null) {result.append(line);}

        def jsonSlurper = new groovy.json.JsonSlurper()
        data = jsonSlurper.parseText(result.toString())
        description=data?.description
        state=data?.state=="err"?0:1
    }
    catch(org.apache.http.conn.HttpHostConnectException e){
        description=ec.l10n.toPersianLocale("apiConnectionError")
        state=500
    }
    catch(Exception e){
        state=0
    }
}

def execute_post(){

    try{
        if(!token)token="Bearer "+ec.user.getPreference("apiToken")
        urlAddress=org.moqui.util.SystemBinding.getPropOrEnv('_apiUrl')+url;
        if(version=="v2")urlAddress=org.moqui.util.SystemBinding.getPropOrEnv('_apiUrl_v2')+url;
        if(version=="v3")urlAddress=org.moqui.util.SystemBinding.getPropOrEnv('_apiUrl_v3')+url;
        if(fullUrl)urlAddress=fullUrl
        postData=JsonOutput.toJson(postData)
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost request = new HttpPost(urlAddress);
        request.setEntity(new StringEntity(postData,"UTF-8"))
        request.addHeader("Accept", "application/json; q=0.5");
        request.addHeader("Content-Type", "application/json");
        if(!token.equals("false")){
            request.addHeader("Authorization", token);
        }
        HttpResponse response = client.execute(request);
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        StringBuffer result = new StringBuffer();
        String line = "";
        statusCode=response.getStatusLine().getStatusCode()
        while ((line = rd.readLine()) != null) {result.append(line);}
        def jsonSlurper = new groovy.json.JsonSlurper()

        if(result && result!="")data = jsonSlurper.parseText(result.toString())
        description=data?.description
        state=data?.state=="ok"?1:0
        if(data?.access_token!=null)state=1
    }
    catch(org.apache.http.conn.HttpHostConnectException e){
        description=ec.l10n.toPersianLocale("apiConnectionError")
        state=500
    }
    catch(Exception e){
        e.printStackTrace()
        state=0
    }
}

def execute_put(){


    try{
        if(!token)token= "Bearer " +ec.user.getPreference("apiToken").toString()

        urlAddress=org.moqui.util.SystemBinding.getPropOrEnv('_apiUrl')+url;
        if(version=="v2")urlAddress=org.moqui.util.SystemBinding.getPropOrEnv('_apiUrl_v2')+url;
        if(version=="v3")urlAddress=org.moqui.util.SystemBinding.getPropOrEnv('_apiUrl_v3')+url;
        postData=JsonOutput.toJson(postData)
        HttpClient client = HttpClientBuilder.create().build();
        HttpPut request = new HttpPut(urlAddress);
        request.setEntity(new StringEntity(postData,"UTF-8"))
        request.addHeader("Accept", "application/json; q=0.5");
        request.addHeader("Content-Type", "application/json");
        request.addHeader("Authorization", token);
        HttpResponse response = client.execute(request);

        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        StringBuffer result = new StringBuffer();
        String line = "";
        statusCode=response.getStatusLine().getStatusCode()
        while ((line = rd.readLine()) != null) {result.append(line);}

        def jsonSlurper = new groovy.json.JsonSlurper()
        data = jsonSlurper.parseText(result.toString())
        if(data?.state=="err") state="0"
        else state="1"
        description=data?.description
    }
    catch(org.apache.http.conn.HttpHostConnectException e){
        description=ec.l10n.toPersianLocale("apiConnectionError")
        state=500
    }
    catch(Exception e){
        state="0"
    }
}

def execute_delete(){

    try{
        if(!token)token= "Bearer " +ec.user.getPreference("apiToken").toString()
        if(!token)token=org.moqui.util.SystemBinding.getPropOrEnv('_systemToken')
        urlAddress=org.moqui.util.SystemBinding.getPropOrEnv('_apiUrl')+url;
        if(version=="v2")urlAddress=org.moqui.util.SystemBinding.getPropOrEnv('_apiUrl_v2')+url;
        if(version=="v3")urlAddress=org.moqui.util.SystemBinding.getPropOrEnv('_apiUrl_v3')+url;

        HttpClient client = HttpClientBuilder.create().build();
        HttpDelete request = new HttpDelete(urlAddress);
        request.addHeader("Accept", "application/json; q=0.5");
        request.addHeader("Content-Type", "application/json");
        request.addHeader("Authorization", token);
        HttpResponse response = client.execute(request);

        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        StringBuffer result = new StringBuffer();
        String line = "";
        statusCode=response.getStatusLine().getStatusCode()
        while ((line = rd.readLine()) != null) {result.append(line);}

        def jsonSlurper = new groovy.json.JsonSlurper()
        outMap = jsonSlurper.parseText(result.toString())
        description=outMap?.description
        if(outMap?.state=="err")state=0
        else state=1
    }
    catch(org.apache.http.conn.HttpHostConnectException e){
        description=ec.l10n.toPersianLocale("apiConnectionError")
        state=500
    }
    catch(Exception e){
        e.printStackTrace()
        state=0
    }
}

def request_post(){

    try {
        urlAddress=org.moqui.util.SystemBinding.getPropOrEnv('_apiUrl')+url;
        if(version=="v2")urlAddress=org.moqui.util.SystemBinding.getPropOrEnv('_apiUrl_v2')+url;
        if(!token)token= "Bearer " +ec.user.getPreference("apiToken").toString()
        if(!token)token=org.moqui.util.SystemBinding.getPropOrEnv('_systemToken')
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(urlAddress);
        post.setEntity(data);

        post.addHeader("Authorization", token);
        HttpResponse response = client.execute(post);
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        StringBuffer result = new StringBuffer();
        String line = "";
        statusCode=response.getStatusLine().getStatusCode()
        while ((line = rd.readLine()) != null) {result.append(line);}
        def jsonSlurper = new JsonSlurper()
        data = jsonSlurper.parseText(result.toString())
        description=data?.description
        state=data?.state=="ok"?"1":"0"
        if(data?.access_token!=null)state="1"
    }
    catch(org.apache.http.conn.HttpHostConnectException e){
        description=ec.l10n.toPersianLocale("apiConnectionError")
        state=500
    }
    catch (Exception e) {
        state=0;
        description="خطای ارسال نامه"
    }
}

def get_connection(){

    try{
        url=org.moqui.util.SystemBinding.getPropOrEnv("api_database_url")+databaseName
        username=org.moqui.util.SystemBinding.getPropOrEnv("api_database_username")
        password=org.moqui.util.SystemBinding.getPropOrEnv("api_database_password")
        sql = Sql.newInstance(url, username, password, "com.mysql.jdbc.Driver")
        state=1
    }
    catch(Exception e){
        e.printStackTrace()
        state=0
    }
}