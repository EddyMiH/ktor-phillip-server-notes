package com.androiddev.routes

import com.androiddev.JwtConfig
import com.androiddev.data.checkPasswordAndEmail
import com.androiddev.data.checkPasswordForEmail
import com.androiddev.data.request.RegisterRequest
import com.androiddev.data.response.SimpleResponse
import com.auth0.jwt.JWT
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.features.ContentTransformationException
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

fun Route.loginUser() {
    route("/login") {
        post {
            val request = try {
                call.receive<RegisterRequest>()
            } catch (e: ContentTransformationException) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            val isPasswordCorrect = checkPasswordForEmail(request.email, request.password)
            if (isPasswordCorrect) {
                call.respond(HttpStatusCode.OK, SimpleResponse(true, "Logged in successfully!"))
            } else {
                call.respond(HttpStatusCode.OK, SimpleResponse(false, "Wrong email or password"))
            }
        }
    }
    route("/auth/login") {
        post {
            val request = try {
                call.receive<RegisterRequest>()
            } catch (e: ContentTransformationException) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            val authenticatedUser = checkPasswordAndEmail(request.email, request.password)
            if (authenticatedUser != null) {
                val token = JwtConfig.createToken(authenticatedUser)
                call.respond(HttpStatusCode.OK, SimpleResponse(true, "Logged in successfully! Token is:  $token"))
            } else {
                call.respond(HttpStatusCode.OK, SimpleResponse(false, "Wrong email or password"))
            }
        }
    }

    route("/auth/getUser") {
        authenticate("auth-jwt") {
            get {
                val principal = call.principal<JWTPrincipal>()
                requireNotNull(principal)
                val email = principal.payload.getClaim("email").toString()
                val pass = principal.payload.getClaim("password").toString()
                val id = principal.payload.getClaim("id").toString()
                val expiresAt = principal.payload.expiresAt?.time?.minus(System.currentTimeMillis())
                call.respond(HttpStatusCode.OK, SimpleResponse(true, """User data :
                    | email: $email, 
                    | pass: $pass, 
                    | id: $id, 
                    | expiresAt: $expiresAt""".trimMargin()))
            }
        }
    }
}