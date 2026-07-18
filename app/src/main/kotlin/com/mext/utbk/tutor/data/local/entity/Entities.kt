package com.mext.utbk.tutor.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val sender: String,
    val messageText: String,
    val timestamp: Long
)

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey val id: String,
    val subjectId: String,
    val topicId: String,
    val title: String,
    val content: String,
    val summary: String,
    val timestamp: Long
)

@Entity(tableName = "study_plans")
data class StudyPlanEntity(
    @PrimaryKey val id: String,
    val day: String,
    val target: String,
    val isCompleted: Boolean
)

@Entity(tableName = "history_entries")
data class HistoryEntity(
    @PrimaryKey val id: String,
    val type: String,
    val title: String,
    val detail: String,
    val timestamp: Long
)
