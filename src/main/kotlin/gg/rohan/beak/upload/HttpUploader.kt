package gg.rohan.beak.upload

import gg.rohan.beak.HttpSettings
import gg.rohan.beak.ServerInfo
import gg.rohan.beak.retrofit.RetrofitClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.nio.file.Path
import kotlin.io.path.readBytes

class HttpUploader(serverInfo: ServerInfo, private val httpConfig: HttpSettings): FileUploader(serverInfo) {

    private val pteroService = RetrofitClient.pterodactylService

    override suspend fun upload(files: Map<Path, String>): Job {
        return coroutineScope {
            launch {
                val jobs = files.map { (localFile, remoteFile) ->
                    launch {
                        println("Uploading ${localFile.fileName} to $remoteFile")
                        val uploadUrl = pteroService.createUploadUrl(serverInfo.serverId)
                        val uploadResponse = pteroService.uploadFile(uploadUrl, remoteFile, localFile.readBytes())
                    }
                }.toList()
                jobs.forEach { it.join() }
            }

        }
    }
}