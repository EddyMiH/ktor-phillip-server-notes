package com.androiddev.routes

import com.androiddev.data.*
import com.androiddev.data.collections.Note
import com.androiddev.data.request.AddOwnerRequest
import com.androiddev.data.request.DeleteNoteRequest
import com.androiddev.data.response.SimpleResponse
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.ContentTransformationException
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

fun Route.getNotesList() {
    route("/getNotes") {
        authenticate {
            get {
                val email = call.principal<UserIdPrincipal>()?.name
                requireNotNull(email)
                val notes = getUserNotes(email)
                call.respond(HttpStatusCode.OK, notes)
            }
        }
    }
    route("/deleteNote") {
        authenticate {
            delete {
                val email = call.principal<UserIdPrincipal>()?.name
                requireNotNull(email)
                val request = try {
                    call.receive<DeleteNoteRequest>()
                } catch (e: ContentTransformationException) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@delete
                }
                if (deleteNoteForUser(email, request.id)) {
                    call.respond(HttpStatusCode.OK, SimpleResponse(true, "note deleted"))
                } else {
                    call.respond(HttpStatusCode.Conflict, SimpleResponse(false, "cannot delete note"))
                }
            }
        }
    }
    route("/saveNote") {
        authenticate {
            post {
                val request = try {
                    call.receive<Note>()
                } catch (e: ContentTransformationException) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@post
                }
                if (com.androiddev.data.saveNote(request)) {
                    call.respond(HttpStatusCode.OK, SimpleResponse(true, "note saved"))
                } else {
                    call.respond(HttpStatusCode.Conflict, SimpleResponse(false, "cannot save note"))
                }
            }
        }
    }
    route("/addNoteOwner") {
        authenticate {
            post {
                val request = try {
                    call.receive<AddOwnerRequest>()
                } catch (e: ContentTransformationException) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@post
                }
                if (!checkIfUserExists(request.ownerEmail)) {
                    call.respond(HttpStatusCode.OK, SimpleResponse(false, "User with given email not registered"))
                    return@post
                }
                if (isOwnerOfNote(request.noteId, request.ownerEmail)) {
                    call.respond(HttpStatusCode.OK, SimpleResponse(false, "User already has access to note"))
                    return@post
                }
                if (addOwnerToNote(request.noteId, request.ownerEmail)) {
                    call.respond(HttpStatusCode.OK, SimpleResponse(true, "User added"))
                } else {
                    call.respond(HttpStatusCode.Conflict, SimpleResponse(false, "Something went wrong"))
                }
            }
        }
    }
}