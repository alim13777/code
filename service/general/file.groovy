import org.apache.commons.fileupload.disk.DiskFileItem
import java.util.zip.*;

def create_file_v2() {

    try {
        name = java.util.UUID.randomUUID().toString()
        webapp_http_host = org.moqui.util.SystemBinding.getPropOrEnv('webapp_http_host')
        webapp_http_port = org.moqui.util.SystemBinding.getPropOrEnv('webapp_http_port')
        if (!filename) filename = file.getName()
        if (file && file.size > 0) {
            dateDir = Calendar.getInstance().get(Calendar.YEAR).toString() + '_' + Calendar.getInstance().get(Calendar.MONTH).toString()
            fileExtension = filename.split('\\.')[filename.split('\\.').size() - 1]
            if (extension) fileExtension = extension
            filename = name + "." + fileExtension
        }
        persistName = java.util.UUID.randomUUID().toString()
        persistNameWithExtension = persistName + '.' + fileExtension
        call = ec.service.sync().name("create#File").parameters([size: file.size, fileExtension: fileExtension, name: name, filename: filename, persistName: persistName, persistNameWithExtension: persistNameWithExtension, originalName: filename, size: file.size, name: name, storage: 'url', fromDate: ec.user.nowTimestamp]).call()
        fileId = call?.fileId
        moquiRuntimePath = org.moqui.util.SystemBinding.getPropOrEnv('moqui.runtime')
        contentRoot = org.moqui.util.SystemBinding.getPropOrEnv('trnFileRootLocation')
        contentLocation = "file://${moquiRuntimePath}/${contentRoot}/live/${fileEntityName}/${dateDir}/${persistName}/${filename}"
        ref = ec.resource.getLocationReference(contentLocation)
        fileStream = file.getInputStream()
        ref.putStream(fileStream)
        url = org.moqui.util.SystemBinding.getPropOrEnv('httpOrhttps') + ec.web.getWebappRootUrl(true, true).split("http")[1] + '/rest/s1/general/download?fileId= ' + fileId
        relativeLocation = '/live/' + fileEntityName + '/' + dateDir + '/' + persistName + '/' + filename
        ec.service.sync().name("update#File").parameters([fileId: fileId, url: url, location: contentLocation, relativeLocation: relativeLocation]).call()
        state = 1
    }
    catch (Exception e) {
        e.printStackTrace()
        state = 0
    }
    finally {
        if (fileStream) fileStream.close()
    }
}

def get_file_v2() {

    try {
        file = ec.entity.find("File").condition([fileId: fileId]).one()
        if (!file) {
            descrtion = ec.l10n.toPersianLocale("noFileFound")
            state = 0
            return
        }
        fileId = file?.fileId
        url = org.moqui.util.SystemBinding.getPropOrEnv('httpOrhttps') + ec.web.getWebappRootUrl(true, true).split("http")[1] + '/rest/s1/general/download?fileId= ' + fileId
        state = 1
    }
    catch (Exception e) {
        if (!descrtion) descrtion = ec.l10n.toPersianLocale("generalError")
        state = 0
    }
}

def get_file() {
    data = [:]
    if (fileId) data.fileId = fileId
    if (url) data.url = url
    file = ec.entity.find("general.File").condition(data).one()
    def fileLoc = file
    fileLoc = file?.location?.split("file://")[1]
    ec.web.sendResourceResponse(fileLoc, false)
}

def download_file() {
    data = [:]
    if (fileId) data.fileId = fileId
    if (url) data.url = url
    file = ec.entity.find("general.File").condition(data).one()
    def fileLoc = file
    splitt = file?.location?.split("file://")
    if (splitt && splitt?.size() > 0) {
        fileLoc = splitt[1]
        ec.web.sendResourceResponse(fileLoc, true)
    }
}

def open_file() {
    data = [:]
    if (fileId) data.fileId = fileId
    if (url) data.url = url
    file = ec.entity.find("general.File").condition(data).one()
    def fileLoc = file
    splitt = file?.location?.split("file://")
    if (splitt && splitt?.size() > 0) {
        fileLoc = splitt[1]
        ec.web.sendResourceResponse(fileLoc, false)
    }
}

def get_file_info() {
    data = [:]
    if (fileId) data.fileId = fileId
    file = ec.entity.find("general.File").condition(data).list()

}

def create_file() {

    serviceCall = ec.service.sync().name("general.file.create#physicalFile").parameters([file: file, type: type, extension: extension]).call()
    url = serviceCall.url;
    fileId = serviceCall.fileId
}

def get_url() {

    data = [:]
    data.url = url
    url = 'Files/RequestUrl'
    serviceCall = ec.service.sync().name("api.api.execute#post").parameters([postData: data, url: url]).call()
    state = serviceCall?.state
    url = serviceCall?.data?.data
}

def create_api_file() {

    data = [:]
    data.file = file
    data.request_id = requestId
    url = "/v2/EnteringSystem/Request/" + requestId + "/Files"
    serviceCall = ec.service.sync().name("api.api.execute#post").parameters([postData: data, url: url]).call()
    state = serviceCall?.state

}

def create_base64_file() {

    byte[] buffer = new byte[1024]
    ZipInputStream zis = new ZipInputStream(fileContent.getInputStream())
    ZipEntry zipEntry = zis.getNextEntry()
    while (zipEntry != null) {
        File newFile = new File(File.separator, zipEntry.name)
        if (zipEntry.isDirectory()) {
            if (!newFile.isDirectory() && !newFile.mkdirs() ) {
                throw new IOException("Failed to create directory " + newFile)
            }
        } else {
            File parent = newFile.parentFile
            if (!parent.isDirectory() && !parent.mkdirs() ) {
                throw new IOException("Failed to create directory " + parent)
            }
            FileOutputStream fos = new FileOutputStream(newFile)
            int len = 0
            while ((len = zis.read(buffer)) > 0) {
                fos.write(buffer, 0, len)
            }
            out.write(fos);
            fos.close()
        }
        zipEntry = zis.getNextEntry()
    }
    zis.closeEntry()
    zis.close()
    trnFile = ec.service.sync().name("general.file.create#physicalFile").parameters([file: item]).call()
    url = trnFile.url;
    fileId = trnFile.fileId
    out.close();
    state = 1

}