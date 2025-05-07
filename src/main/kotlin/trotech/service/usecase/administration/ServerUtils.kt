package trotech.service.usecase.administration

import java.io.BufferedReader
import java.io.InputStreamReader

fun reloadDockerContainers() {
    val process = ProcessBuilder("docker", "ps", "-q").start()
    val reader = BufferedReader(InputStreamReader(process.inputStream))
    val containerIds = reader.readLines()

    for (id in containerIds) {
        ProcessBuilder("docker", "restart", id).start()
    }
}
