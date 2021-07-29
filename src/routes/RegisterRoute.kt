package com.androiddev.routes

import com.androiddev.data.checkIfUserExists
import com.androiddev.data.collections.User
import com.androiddev.data.request.RegisterRequest
import com.androiddev.data.response.SimpleResponse
import com.androiddev.security.getHashWithSalt
import io.ktor.application.*
import io.ktor.features.ContentTransformationException
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

fun Route.registerUser() {
    route("/register") {
        post {
            val request = try {
                call.receive<RegisterRequest>()
            } catch (e: ContentTransformationException) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            if (!checkIfUserExists(request.email)) {
                if (
                    com.androiddev.data.registerUser(
                        User(
                            email = request.email,
                            password = getHashWithSalt(request.password)
                        )
                    )
                ) {
                    call.respond(
                        HttpStatusCode.OK,
                        SimpleResponse(true, "Successfully register user")
                    )
                } else {
                    call.respond(
                        HttpStatusCode.OK,
                        SimpleResponse(false, "An unknown error occur")
                    )
                }
            } else {
                call.respond(
                    HttpStatusCode.OK,
                    SimpleResponse(false, "A user with this email already registered")
                )
            }
        }
    }
}