package com.androiddev

import com.androiddev.data.collections.User
import com.auth0.jwt.JWT
import com.auth0.jwt.*
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.application.*
import java.util.*

object JwtConfig {

    private const val secret: String = "eddy-secret"
    private const val issuer: String = "http://0.0.0.0:8001"
    private const val audience: String = "http://0.0.0.0:8001/"
    private const val validityInMs = 36_000_00 * 6 // 1 hour
    private val algorithm = Algorithm.HMAC256(secret)

    val verifier: JWTVerifier = JWT
        .require(algorithm)
        .withAudience(audience)
        .withIssuer(issuer)
        .build()

    fun createToken(user: User): String = JWT.create()
        .withSubject("Authentication")
        .withIssuer(issuer)
        .withClaim("email", user.email)
        .withClaim("password", user.password)
        .withClaim("id", user.id)
        .withExpiresAt(Date(System.currentTimeMillis() + validityInMs))
        .sign(algorithm)
}