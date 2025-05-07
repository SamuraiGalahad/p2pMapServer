package trotech.dao.database

import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import trotech.dto.entities.Admin

fun findAdminByUsername(username: String): Admin? {
    return transaction {
        AdminsTable
            .select { AdminsTable.username eq username }
            .map { row ->
                Admin(
                    id = row[AdminsTable.id],
                    username = row[AdminsTable.username],
                    passwordHash = row[AdminsTable.passwordHash]
                )
            }
            .singleOrNull()
    }
}