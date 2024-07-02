package gg.rohan.beak

import gg.rohan.beak.pterodactyl.PowerState
import gg.rohan.beak.retrofit.RetrofitClient
import gg.rohan.beak.upload.SftpUploader
import kotlinx.coroutines.*
import org.gradle.api.Plugin
import org.gradle.api.Project

class BeakPlugin: Plugin<Project> {
    override fun apply(project: Project) {
        val config = project.extensions.create("beak", BeakExtension::class.java)
        project.tasks.register("beak") { task ->
            task.doLast {
                runBlocking {
                    println("Starting Beak")
                    coroutineScope {
                        val jobs = config.server.servers.map { currentServer ->
                            launch {
                                val uploader = when (currentServer.uploadMethod) {
                                    UploadVia.SFTP -> SftpUploader(currentServer, currentServer.sftp)
                                }
                                val uploadFuture = uploader.upload(currentServer.uploadMapping)
                                uploadFuture.join()
                                if (!currentServer.dontRestart) RetrofitClient.pterodactylService.changePowerState(currentServer.serverId, PowerState.RESTART)
                            }
                        }.toList()
                        jobs.forEach { it.join() }
                    }
                }
            }
        }
    }
}