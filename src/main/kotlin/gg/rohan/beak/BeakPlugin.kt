package gg.rohan.beak

import gg.rohan.beak.pterodactyl.PowerState
import gg.rohan.beak.upload.HttpUploader
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
                    project.logger.info("Starting Beak")
                    coroutineScope {
                        if (config.servers.isEmpty()) {
                            project.logger.error("No servers found in configuration\nExiting...")
                            return@coroutineScope
                        }
                        val jobs = config.servers.map { currentServer ->
                            currentServer.validate()
                            launch {
                                val uploader = when (currentServer.uploadMethod) {
                                    UploadVia.SFTP -> SftpUploader(currentServer, currentServer.sftpSettings)
                                    UploadVia.HTTP -> HttpUploader(currentServer, currentServer.httpSettings)
                                }
                                val uploadFuture = uploader.upload(currentServer.uploadMapping)
                                uploadFuture.join()
                                if (!currentServer.dontRestart) currentServer.httpSettings.pteroService.changePowerState(currentServer.serverId, PowerState.RESTART)
                            }
                        }.toList()
                        jobs.forEach { it.join() }
                    }
                }
            }
        }
    }
}