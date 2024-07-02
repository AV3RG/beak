package gg.rohan.beak.upload

import gg.rohan.beak.ServerInfo
import kotlinx.coroutines.Job
import java.nio.file.Path

abstract class FileUploader(protected val serverInfo: ServerInfo) {

    abstract suspend fun upload(files: Map<Path, String>): Job

}