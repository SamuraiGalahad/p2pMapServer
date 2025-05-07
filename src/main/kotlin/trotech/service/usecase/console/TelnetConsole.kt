package trotech.service.usecase.console

import kotlinx.coroutines.*
import trotech.service.usecase.administration.reloadDockerContainers
import java.net.ServerSocket
import java.net.Socket

fun startTelnetServer(port: Int = 2323) {
    CoroutineScope(Dispatchers.IO).launch {
        val serverSocket = ServerSocket(port)
        println("Telnet-сервер запущен на порту $port")

        while (true) {
            val client = serverSocket.accept()
            println("Новое Telnet-подключение от ${client.inetAddress.hostAddress}")

            launch {
                handleTelnetClient(client)
            }
        }
    }
}

suspend fun handleTelnetClient(client: Socket) {
    client.getOutputStream().writer().use { writer ->
        client.getInputStream().bufferedReader().use { reader ->
            writer.write("Telnet Console: P2P Map Server\r\n")
            writer.flush()

            while (true) {
                writer.write("> ")
                writer.flush()

                val command = reader.readLine() ?: break
                println("Команда от Telnet-клиента: $command")

                when (command.lowercase()) {
                    "restart" -> {
                        reloadDockerContainers()
                        writer.write("Docker Containers Reloaded!\r\n")
                    }
                    "status" -> {
                        writer.write("Stable\r\n")
                    }
                    "exit" -> {
                        writer.write("See you, space cowboy...\r\n")
                        break
                    }
                    else -> {
                        writer.write("Unknown command: $command\r\n")
                    }
                }
                writer.flush()
            }
        }
    }

    println("Telnet клиент отключился: ${client.inetAddress.hostAddress}")
    client.close()
}