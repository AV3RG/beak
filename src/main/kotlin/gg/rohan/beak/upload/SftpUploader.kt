package gg.rohan.beak.upload

import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.JSch
import gg.rohan.beak.ServerInfo
import gg.rohan.beak.SftpSettings
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.nio.file.Path
import kotlin.io.path.absolutePathString

class SftpUploader(serverInfo: ServerInfo, sftpSettings: SftpSettings): FileUploader(serverInfo) {

    private val jSch = JSch()
    private val channel: ChannelSftp
    private val timeout = sftpSettings.timeout

    init {
        jSch.setKnownHosts(sftpSettings.knownHostsFile.absolutePathString())
        val session = jSch.getSession(sftpSettings.username, sftpSettings.hostname, sftpSettings.port)
        session.setPassword(sftpSettings.password)
        session.connect(timeout * 1000)
        channel = session.openChannel("sftp") as ChannelSftp
        channel.connect(timeout)
    }

    override suspend fun upload(files: Map<Path, String>): Job {
        return coroutineScope {
            launch {
                val jobs = files.map { (localFile, remoteFile) ->
                    launch {
                        println("Uploading ${localFile.fileName} to $remoteFile")
                        channel.put(localFile.toString(), remoteFile)
                    }
                }
                jobs.forEach { it.join() }
            }
        }
    }

}