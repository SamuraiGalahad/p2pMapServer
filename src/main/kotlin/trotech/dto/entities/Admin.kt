package trotech.dto.entities

data class Admin(
    val id: Int,
    val username: String,
    val passwordHash: String
)