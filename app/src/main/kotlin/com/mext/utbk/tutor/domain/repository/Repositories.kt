package com.mext.utbk.tutor.domain.repository

import com.mext.utbk.tutor.domain.model.*

interface MaterialRepository {
    suspend fun getSubjects(): List<Subject>
    suspend fun getTopics(subjectId: String): List<Topic>
    suspend fun getTopicDetail(subjectId: String, topicId: String): Topic?
}

interface QuizRepository {
    suspend fun generateQuestions(topicId: String): List<QuizQuestion>
    suspend fun submitQuizResult(score: Int, totalQuestions: Int)
}

interface ChatRepository {
    suspend fun getChatHistory(): List<ChatMessage>
    suspend fun sendMessage(message: String): ChatMessage
    suspend fun clearHistory()
}

interface PlannerRepository {
    suspend fun getStudyPlans(): List<StudyPlan>
    suspend fun addStudyPlan(plan: StudyPlan)
    suspend fun togglePlanCompletion(planId: String)
}
