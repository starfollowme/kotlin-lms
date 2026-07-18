package com.mext.utbk.tutor.data.repository

import com.mext.utbk.tutor.data.local.dao.*
import com.mext.utbk.tutor.data.local.entity.*
import com.mext.utbk.tutor.domain.model.*
import com.mext.utbk.tutor.domain.repository.*
import com.mext.utbk.tutor.BuildConfig
import com.mext.utbk.tutor.data.remote.OpenRouterService
import com.mext.utbk.tutor.data.remote.model.ApiChatMessage
import com.mext.utbk.tutor.data.remote.model.ChatRequest
import kotlinx.coroutines.flow.first

class MaterialRepositoryImpl(private val bookmarkDao: BookmarkDao) : MaterialRepository {
    override suspend fun getSubjects(): List<Subject> {
        return listOf(
            Subject("utbk_mat", "Matematika UTBK", "UTBK", "Materi TPS Kuantitatif & Matematika UTBK"),
            Subject("utbk_ind", "Literasi Bahasa Indonesia", "UTBK", "Materi Pemahaman Bacaan & Menulis"),
            Subject("mext_mat", "Matematika MEXT", "MEXT", "Matematika MEXT Kategori A/B (Aljabar, Kalkulus, dll)"),
            Subject("mext_phy", "Fisika MEXT", "MEXT", "Mekanika, Termodinamika, Gelombang, & Elektromagnetik")
        )
    }

    override suspend fun getTopics(subjectId: String): List<Topic> {
        return listOf(
            Topic("t1", subjectId, "Bab 1: Persamaan Kuadrat", "Materi tentang akar-akar persamaan kuadrat, diskriminan, dan sifat-sifatnya.", "Ringkasan: D = b^2 - 4ac. Jika D > 0 akar real dan berbeda."),
            Topic("t2", subjectId, "Bab 2: Turunan (Kalkulus)", "Penjelasan konsep limit fungsi, diferensial turunan pertama, dan aplikasi turunan.", "Ringkasan: f'(x) menyatakan laju perubahan sesaat dari fungsi f(x).")
        )
    }

    override suspend fun getTopicDetail(subjectId: String, topicId: String): Topic {
        return Topic(
            id = topicId,
            subjectId = subjectId,
            title = if (topicId == "t1") "Bab 1: Persamaan Kuadrat" else "Bab 2: Turunan (Kalkulus)",
            content = "Persamaan kuadrat adalah persamaan polinomial orde dua. Bentuk umumnya adalah ax^2 + bx + c = 0. " +
                    "Rumus abc digunakan untuk mencari akar-akarnya: x = (-b ± √(b^2 - 4ac)) / 2a. " +
                    "Konsep ini sangat sering diuji baik di UTBK maupun ujian beasiswa MEXT.",
            summary = "Ringkasan Cepat: Persamaan kuadrat ax^2 + bx + c = 0 memiliki dua akar."
        )
    }

    override suspend fun bookmarkTopic(topic: Topic) {
        bookmarkDao.insertBookmark(
            BookmarkEntity(
                id = topic.id,
                subjectId = topic.subjectId,
                topicId = topic.id,
                title = topic.title,
                content = topic.content,
                summary = topic.summary,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    override suspend fun unbookmarkTopic(topic: Topic) {
        bookmarkDao.deleteBookmark(
            BookmarkEntity(
                id = topic.id,
                subjectId = topic.subjectId,
                topicId = topic.id,
                title = topic.title,
                content = topic.content,
                summary = topic.summary,
                timestamp = 0L
            )
        )
    }

    override suspend fun isBookmarked(topicId: String): Boolean {
        return bookmarkDao.isBookmarked(topicId)
    }

    override suspend fun getBookmarkedTopics(): List<Topic> {
        return bookmarkDao.getAllBookmarks().first().map {
            Topic(
                id = it.id,
                subjectId = it.subjectId,
                title = it.title,
                content = it.content,
                summary = it.summary
            )
        }
    }
}

class QuizRepositoryImpl(private val historyDao: HistoryDao) : QuizRepository {
    override suspend fun generateQuestions(topicId: String): List<QuizQuestion> {
        return listOf(
            QuizQuestion(
                id = "q1",
                topicId = topicId,
                questionText = "Tentukan akar-akar dari persamaan kuadrat x^2 - 5x + 6 = 0!",
                options = listOf("x = 2 dan x = 3", "x = -2 dan x = -3", "x = 1 dan x = 6", "x = -1 dan x = -6"),
                correctAnswerIndex = 0,
                explanation = "x^2 - 5x + 6 = 0 dapat difaktorkan menjadi (x - 2)(x - 3) = 0. Maka, akar-akarnya adalah x = 2 atau x = 3."
            ),
            QuizQuestion(
                id = "q2",
                topicId = topicId,
                questionText = "Berapakah nilai diskriminan dari 2x^2 + 4x + 2 = 0?",
                options = listOf("D = 16", "D = 8", "D = 0", "D = -4"),
                correctAnswerIndex = 2,
                explanation = "D = b^2 - 4ac. Maka D = 4^2 - 4(2)(2) = 16 - 16 = 0."
            )
        )
    }

    override suspend fun submitQuizResult(score: Int, totalQuestions: Int) {
        val entry = HistoryEntity(
            id = "quiz_${System.currentTimeMillis()}",
            type = "QUIZ",
            title = "Latihan Soal Matematika",
            detail = "Menyelesaikan kuis dengan skor $score (Total Soal: $totalQuestions)",
            timestamp = System.currentTimeMillis()
        )
        historyDao.insertHistory(entry)
    }

    override suspend fun getHistory(): List<HistoryEntry> {
        val entities = historyDao.getAllHistory().first()
        return entities.map { HistoryEntry(it.id, it.type, it.title, it.detail, it.timestamp) }
    }

    override suspend fun clearHistory() {
        historyDao.clearHistory()
    }

    override suspend fun submitSimulationResult(score: Int, examType: String) {
        val entry = HistoryEntity(
            id = "sim_${System.currentTimeMillis()}",
            type = "QUIZ", // Record as quiz activity type
            title = "Simulasi Ujian $examType",
            detail = "Menyelesaikan simulasi dengan skor $score",
            timestamp = System.currentTimeMillis()
        )
        historyDao.insertHistory(entry)
    }
}

class ChatRepositoryImpl(
    private val chatDao: ChatDao,
    private val apiService: OpenRouterService
) : ChatRepository {
    override suspend fun getChatHistory(): List<ChatMessage> {
        val entities = chatDao.getAllMessages().first()
        return entities.map { ChatMessage(it.id, it.sender, it.messageText, it.timestamp) }
    }

    override suspend fun sendMessage(message: String): ChatMessage {
        // Save user message to database
        val userEntity = ChatMessageEntity(
            id = "user_${System.currentTimeMillis()}",
            sender = "USER",
            messageText = message,
            timestamp = System.currentTimeMillis()
        )
        chatDao.insertMessage(userEntity)

        // Fetch recent history from DB for conversation context (last 10 messages)
        val historyEntities = chatDao.getAllMessages().first().takeLast(10)
        val apiMessages = historyEntities.map {
            ApiChatMessage(
                role = if (it.sender == "USER") "user" else "assistant",
                content = it.messageText
            )
        }

        // Call OpenRouter API
        val responseText = try {
            val response = apiService.getChatCompletion(
                authorization = "Bearer ${BuildConfig.OPENROUTER_API_KEY}",
                referer = "https://github.com/starfollowme/kotlin-lms",
                title = "MEXT UTBK Tutor",
                request = ChatRequest(
                    model = "tencent/hy3:free",
                    messages = apiMessages
                )
            )
            response.choices.firstOrNull()?.message?.content ?: "Maaf, tidak ada respon dari tutor."
        } catch (e: Exception) {
            e.printStackTrace()
            "Koneksi internet bermasalah atau API Key tidak valid. Silakan periksa kembali jaringan Anda. Error: ${e.localizedMessage}"
        }

        val aiEntity = ChatMessageEntity(
            id = "ai_${System.currentTimeMillis() + 1}",
            sender = "AI",
            messageText = responseText,
            timestamp = System.currentTimeMillis() + 1
        )
        chatDao.insertMessage(aiEntity)

        return ChatMessage(aiEntity.id, aiEntity.sender, aiEntity.messageText, aiEntity.timestamp)
    }

    override suspend fun clearHistory() {
        chatDao.clearHistory()
    }
}

class PlannerRepositoryImpl(private val studyPlanDao: StudyPlanDao) : PlannerRepository {
    override suspend fun getStudyPlans(): List<StudyPlan> {
        val entities = studyPlanDao.getAllPlans().first()
        if (entities.isEmpty()) {
            // Populate initial plans
            val initial = listOf(
                StudyPlanEntity("p1", "Senin", "Belajar Persamaan Kuadrat UTBK", true),
                StudyPlanEntity("p2", "Selasa", "Latihan Soal Turunan MEXT", false),
                StudyPlanEntity("p3", "Rabu", "Simulasi Mandiri Kimia MEXT", false),
                StudyPlanEntity("p4", "Kamis", "Membaca Rangkuman Bahasa Inggris", false)
            )
            for (p in initial) {
                studyPlanDao.insertPlan(p)
            }
            return initial.map { StudyPlan(it.id, it.day, it.target, it.isCompleted) }
        }
        return entities.map { StudyPlan(it.id, it.day, it.target, it.isCompleted) }
    }

    override suspend fun addStudyPlan(plan: StudyPlan) {
        studyPlanDao.insertPlan(StudyPlanEntity(plan.id, plan.day, plan.target, plan.isCompleted))
    }

    override suspend fun togglePlanCompletion(planId: String) {
        val entities = studyPlanDao.getAllPlans().first()
        val target = entities.find { it.id == planId } ?: return
        studyPlanDao.updatePlanCompletion(planId, !target.isCompleted)
    }

    override suspend fun deleteStudyPlan(planId: String) {
        studyPlanDao.deletePlan(planId)
    }
}
