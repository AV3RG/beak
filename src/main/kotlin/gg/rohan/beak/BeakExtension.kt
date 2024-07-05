package gg.rohan.beak

import com.google.gson.GsonBuilder
import com.google.gson.Strictness
import gg.rohan.beak.pterodactyl.PteroService
import okhttp3.OkHttpClient
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.model.ObjectFactory
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.nio.file.Path
import java.time.Duration
import javax.inject.Inject

open class BeakExtension @Inject constructor(factory: ObjectFactory) {
    internal val servers: NamedDomainObjectContainer<ServerInfo> = factory.domainObjectContainer(ServerInfo::class.java)

    fun server(name: String, action: ServerInfo.() -> Unit) {
        servers.create(name, action)
    }
}

open class TimeoutSettings {
    var callTimeout: Duration = Duration.ofMinutes(2)
    var connectTimeout: Duration = Duration.ofMinutes(2)
    var readTimeout: Duration = Duration.ofMinutes(2)
    var writeTimeout: Duration = Duration.ofMinutes(2)
}

open class HttpSettings {
    lateinit var apiKey: String
    lateinit var panelUrl: String
    private val timeoutConfig: TimeoutSettings = TimeoutSettings()

    fun timeouts(action: TimeoutSettings.() -> Unit) {
        timeoutConfig.action()
    }

    internal val pteroService: PteroService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(panelUrl)
            .client(
                OkHttpClient.Builder().addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .addHeader("Authorization", "Bearer $apiKey")
                        .addHeader("User-Agent", "Beak Gradle Plugin/0.1")
                        .build()
                    chain.proceed(request)
                }.callTimeout(timeoutConfig.callTimeout)
                .connectTimeout(timeoutConfig.connectTimeout)
                .readTimeout(timeoutConfig.readTimeout)
                .writeTimeout(timeoutConfig.writeTimeout)
                .build()
            )
            .addConverterFactory(GsonConverterFactory.create(
                GsonBuilder()
                    .setStrictness(Strictness.LENIENT)
                    .create()
            ))
            .build()
        retrofit.create(PteroService::class.java)
    }

    internal fun validate() {
        require(this::apiKey.isInitialized) { "API key must be initialized" }
        require(this::panelUrl.isInitialized) { "Panel URL must be initialized" }
    }
}

open class SftpSettings {
    lateinit var hostname: String
    var port: Int = 2022
    lateinit var username: String
    lateinit var password: String
    val knownHostsFile: Path = File(System.getenv("HOME")?: System.getenv("HOMEPATH"))
        .toPath()
        .resolve(".ssh/known_hosts")
    var timeout: Int = 10

    internal fun validate() {
        require(this::hostname.isInitialized) { "Hostname must be initialized" }
        require(this::username.isInitialized) { "Username must be initialized" }
        require(this::password.isInitialized) { "Password must be initialized" }
    }
}

open class ServerInfo (
    val name: String
) {
    lateinit var serverId: String
    lateinit var uploadMapping: Map<Path, String>
    var dontRestart: Boolean = false
    var uploadMethod: UploadVia = UploadVia.SFTP
    internal lateinit var sftpSettings: SftpSettings
    internal lateinit var httpSettings: HttpSettings

    fun sftp(action: SftpSettings.() -> Unit) {
        sftpSettings = SftpSettings()
        sftpSettings.action()
    }

    fun http(action: HttpSettings.() -> Unit) {
        httpSettings = HttpSettings()
        httpSettings.action()
    }


    internal fun validate() {
        require(this::serverId.isInitialized) { "Server ID must be initialized" }
        require(this::uploadMapping.isInitialized) { "Upload mapping must be initialized" }
        when (uploadMethod) {
            UploadVia.SFTP -> {
                require(this::sftpSettings.isInitialized) { "SFTP settings must be initialized" }
                sftpSettings.validate()
            }
            UploadVia.HTTP -> {
                require(this::httpSettings.isInitialized) { "HTTP settings must be initialized" }
                httpSettings.validate()
            }
        }
        if (!dontRestart) {
            require(this::httpSettings.isInitialized) { "HTTP settings must be initialized to restart server" }
        }
    }
}

enum class UploadVia {
    SFTP,
    HTTP
}
