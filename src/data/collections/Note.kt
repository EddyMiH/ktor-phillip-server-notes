package com.androiddev.data.collections

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

class Note(
    @BsonId
    val id: String = ObjectId().toString(),
    val title: String,
    val content: String,
    val owners: List<String>,
    val color: String,
    val date: Long
)
