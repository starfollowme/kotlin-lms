package com.mext.utbk.tutor.domain.model

data class Subject(
    val id: String,
    val name: String,
    val category: String, // "MEXT" or "UTBK"
    val description: String
)

data class Topic(
    val id: String,
    val subjectId: String,
    val title: String,
    val content: String,
    val summary: String
)

data class QuizQuestion(
    val id: String,
    val topicId: String,
    val questionText: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String
)

data class ChatMessage(
    val id: String,
    val sender: String, // "USER" or "AI"
    val messageText: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class StudyPlan(
    val id: String,
    val day: String,
    val target: String,
    val isCompleted: Boolean = false
)

data class HistoryEntry(
    val id: String,
    val type: String, // "CHAT", "QUIZ", "MATERIAL"
    val title: String,
    val detail: String,
    val timestamp: Long = System.currentTimeMillis()
)
