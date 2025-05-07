package trotech.routes

import at.favre.lib.crypto.bcrypt.BCrypt
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

import trotech.dao.database.findAdminByUsername

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.Date

fun generateToken(username: String): String {
    val secret = "secret"
    val issuer = "ktor.io"
    val expiration = Date(System.currentTimeMillis() + 600_000)

    return JWT.create()
        .withIssuer(issuer)
        .withClaim("username", username)
        .withExpiresAt(expiration)
        .sign(Algorithm.HMAC256(secret))
}

fun generateRefreshToken(username: String): String {
    val secret = "refresh_secret"
    val issuer = "ktor.io"
    val expiration = Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000) // 30 дней

    return JWT.create()
        .withIssuer(issuer)
        .withClaim("username", username)
        .withExpiresAt(expiration)
        .sign(Algorithm.HMAC256(secret))
}

data class LoginRequest(val username: String, val password: String)

fun verifyPassword(plainPassword: String, hashedPassword: String): Boolean {
    return BCrypt.verifyer().verify(plainPassword.toCharArray(), hashedPassword).verified
}

fun Route.authRoutes() {
    post("/login") {
        val login = call.receive<LoginRequest>()
        val admin = findAdminByUsername(login.username)

        if (admin != null && verifyPassword(login.password, admin.passwordHash)) {
            val accessToken = generateToken(admin.username)
            val refreshToken = generateRefreshToken(admin.username)
            call.respond(mapOf(
                "access_token" to accessToken,
                "refresh_token" to refreshToken
            ))
        } else {
            call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")
        }
    }

    post("/refresh") {
        val refreshRequest = call.receive<Map<String, String>>()
        val refreshToken = refreshRequest["refresh_token"] ?: ""

        try {
            val decodedJWT = JWT
                .require(Algorithm.HMAC256("refresh_secret"))
                .withIssuer("ktor.io")
                .build()
                .verify(refreshToken)

            val username = decodedJWT.getClaim("username").asString()

            val newAccessToken = generateToken(username)

            call.respond(mapOf("access_token" to newAccessToken))
        } catch (e: Exception) {
            call.respond(HttpStatusCode.Unauthorized, "Invalid refresh token")
        }
    }
}