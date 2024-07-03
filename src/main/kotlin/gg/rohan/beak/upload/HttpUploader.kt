package gg.rohan.beak.upload

import gg.rohan.beak.HttpSettings
import gg.rohan.beak.ServerInfo
import gg.rohan.beak.pterodactyl.PowerState
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.nio.file.Path
import kotlin.io.path.readBytes

class HttpUploader(serverInfo: ServerInfo, private val httpConfig: HttpSettings): FileUploader(serverInfo) {

    override suspend fun upload(files: Map<Path, String>): Job {
        return coroutineScope {
            launch {
                val jobs = files.map { (localFile, remoteFile) ->
                    launch {
                        println("Uploading ${localFile.fileName} to $remoteFile")
                        val uploadUrl = serverInfo.httpSettings.pteroService.createUploadUrl(serverInfo.serverId)
                        val filePart = MultipartBody.Part.createFormData("files", localFile.fileName.toString(), RequestBody.create(
                            MediaType.parse("application/octet-stream"),
                            localFile.readBytes()
                        ))
                        serverInfo.httpSettings.pteroService.uploadFile(uploadUrl.attributes.url, remoteFile, filePart)
                        println("Uploaded ${localFile.fileName} to $remoteFile")
                        if (!serverInfo.dontRestart)
                            serverInfo.httpSettings.pteroService.changePowerState(serverInfo.serverId, PowerState.RESTART)
                    }
                }.toList()
                jobs.forEach { it.join() }
            }

        }
    }
}