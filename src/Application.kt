package com.androiddev

import com.androiddev.data.checkPasswordForEmail
import com.androiddev.data.collections.User
import com.androiddev.data.registerUser
import com.androiddev.routes.getNotesList
import com.androiddev.routes.loginUser
import com.androiddev.routes.registerUser
import com.androiddev.security.checkPasswordHash
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(Authentication) {
        //configureAuthentication()
        configureJWTAuthentication()
    }
    install(DefaultHeaders)
    install(CallLogging)
    install(ContentNegotiation) {
        gson {
            setPrettyPrinting()
        }
    }
    install(Routing) {
        registerUser()
        loginUser()
        getNotesList()
    }
}

private fun Authentication.Configuration.configureAuthentication() = basic {
    realm = "Note Server"
    validate { credentials ->
        val email = credentials.name
        val password = credentials.password
        if (checkPasswordForEmail(email, password)) {
            UserIdPrincipal(email)
        } else {
            null
        }
    }
}

private fun Authentication.Configuration.configureJWTAuthentication() = jwt {
    realm = "Note Server 2"
    verifier(JwtConfig.verifier)
    challenge { defaultScheme, realm ->
        val errorMessage = call.request.headers["Authorization"]
        try {
            //val jwt = errorMessage?.replace("Bearer ", "")
            JwtConfig.verifier.verify(errorMessage)
            ""
        } catch (e: Exception) {
            when (e) {
                is JWTVerificationException ->
                    if (e.localizedMessage.contains("expired")) "Token expired" else "Invalid token"
                else -> "Unknown token error"
            }
        }
        println("here")
    }
    validate { jwtCredential ->
        val email = jwtCredential.payload.getClaim("email").asString()
        val password = jwtCredential.payload.getClaim("password").asString()
        if (checkPasswordForEmail(email, password)) {
            JWTPrincipal(jwtCredential.payload)
        } else {
            null
        }
    }
}
