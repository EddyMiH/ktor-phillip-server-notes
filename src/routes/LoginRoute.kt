package com.androiddev.routes

import com.androiddev.data.checkPasswordForEmail
import com.androiddev.data.request.RegisterRequest
import com.androiddev.data.response.SimpleResponse
import io.ktor.application.*
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
}