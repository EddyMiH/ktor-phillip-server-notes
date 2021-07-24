package com.androiddev

import com.androiddev.data.checkPasswordForEmail
import com.androiddev.data.collections.User
import com.androiddev.data.registerUser
import com.androiddev.routes.getNotesList
import com.androiddev.routes.loginUser
import com.androiddev.routes.registerUser
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(DefaultHeaders)
    install(CallLogging)
    install(ContentNegotiation) {
        gson {
            setPrettyPrinting()
        }
    }
    install(Authentication) {
        configureAuthentication()
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
