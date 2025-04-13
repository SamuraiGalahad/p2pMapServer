package trotech.dao.database

import trotech.dto.entities.Peer
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import trotech.dto.entities.LayersTable

object DatabaseFactory {
    fun init() {
        Database.connect(
            "jdbc:postgresql://localhost:5432/mydb",
            driver = "org.postgresql.Driver",
            user = "user",
            password = "password"
        )

        transaction {
            SchemaUtils.create(LayersTable)
        }
    }
}