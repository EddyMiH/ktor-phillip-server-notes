package com.androiddev.data

import com.androiddev.data.collections.Note
import com.androiddev.data.collections.User
import com.androiddev.security.checkPasswordHash
import org.litote.kmongo.contains
import org.litote.kmongo.coroutine.CoroutineClient
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.eq
import org.litote.kmongo.reactivestreams.KMongo
import org.litote.kmongo.setValue

private const val DATABASE_NAME = "note_database"

private val client: CoroutineClient = KMongo.createClient().coroutine
private val database: CoroutineDatabase = client.getDatabase(DATABASE_NAME)
private val users = database.getCollection<User>()
private val notes = database.getCollection<Note>()

/**
 * create new entry of [User] in mongo db [database]
 *
 * @return true if insertion was successful and false otherwise
 * */
suspend fun registerUser(user: User): Boolean = users.insertOne(user).wasAcknowledged()

suspend fun checkIfUserExists(email: String): Boolean = users.findOne(User::email eq email) != null

suspend fun checkPasswordForEmail(email: String, password: String): Boolean {
    val actualPassword = users.findOne(User::email eq email)?.password ?: return false
    return checkPasswordHash(password, actualPassword)
}

suspend fun getUserNotes(email: String): List<Note> =
    notes.find(Note::owners contains email).toList()

suspend fun saveNote(note: Note): Boolean {
    val isNoteExists = notes.findOneById(note.id) != null
    return if (isNoteExists) {
        notes.updateOneById(note.id, note).wasAcknowledged()
    } else {
        notes.insertOne(note).wasAcknowledged()
    }
}

suspend fun deleteNoteForUser(email: String, noteId: String): Boolean {
    notes.findOne(Note::id eq noteId, Note::owners contains email)?.let {
        return if (it.owners.size > 1) {
            val updatedOwnerList = it.owners - email
            notes.updateOne(Note::id eq noteId, setValue(Note::owners, updatedOwnerList)).wasAcknowledged()
        } else {
            notes.deleteOne(Note::id eq noteId).wasAcknowledged()
        }
    } ?: return false
}

suspend fun addOwnerToNote(noteID: String, owner: String): Boolean {
    val owners = notes.findOneById(noteID)?.owners ?: return false
    return notes.updateOneById(noteID, setValue(Note::owners, owners + owner)).wasAcknowledged()
}

suspend fun isOwnerOfNote(noteId: String, ownerEmail: String): Boolean {
    val note = notes.findOneById(noteId) ?: return false
    return ownerEmail in note.owners
}
