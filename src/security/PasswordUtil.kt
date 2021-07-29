package com.androiddev.security

import org.apache.commons.codec.binary.Hex
import org.apache.commons.codec.digest.DigestUtils
import java.security.SecureRandom

fun getHashWithSalt(stringToHash: String, saltLength: Int = 64): String {
    val salt = SecureRandom.getInstance("SHA1PRNG").generateSeed(saltLength)
    val hexSalt = Hex.encodeHexString(salt)
    val hash = DigestUtils.sha256Hex("$hexSalt:$stringToHash")
    return "$hexSalt:$hash"
}

fun checkPasswordHash(password: String, passwordHash: String): Boolean {
    val (salt, hash) = passwordHash.split(":")
    val hashToCheck = DigestUtils.sha256Hex("$salt:$password")
    return hashToCheck == hash
}