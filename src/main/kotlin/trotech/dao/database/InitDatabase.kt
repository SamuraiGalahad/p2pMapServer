package trotech.dao.database

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init() {
        Database.connect(
            "jdbc:postgresql://localhost:5432/mydb",
            driver = "org.postgresql.Driver",
            user = "user",
            password = "password"
        )
        transaction {
            SchemaUtils.create(*allTables)
        }
    }

}