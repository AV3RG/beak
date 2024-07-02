package gg.rohan.beak

import org.gradle.api.model.ObjectFactory
import java.io.File
import java.nio.file.Path
import javax.inject.Inject

open class BeakExtension @Inject constructor(
    factory: ObjectFactory
) {
    open val server: ServerExtensions = factory.newInstance(ServerExtensions::class.java)

}

open class HttpSettings {
    open val apiKey: String = ""
    open val panelUrl: String = ""
}

open class SftpSettings {
    val hostname: String = ""
    val port: Int = 22
    val username: String = ""
    val password: String = ""
    val knownHostsFile: Path = File(System.getenv("HOME")).toPath().resolve(".ssh/known_hosts")
    val timeout: Int = 10
}

open class ServerExtensions {
    val servers: List<ServerInfo> = listOf()
}

open class ServerInfo @Inject constructor(
    factory: ObjectFactory
) {
    val serverId: String = ""
    val uploadMapping: Map<Path, String> = mapOf()
    val dontRestart: Boolean = false
    val uploadMethod: UploadVia = UploadVia.SFTP
    open val sftp: SftpSettings = factory.newInstance(SftpSettings::class.java)
}

enum class UploadVia {
    SFTP
}
