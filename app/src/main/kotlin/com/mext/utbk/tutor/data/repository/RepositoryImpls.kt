package com.mext.utbk.tutor.data.repository

import com.mext.utbk.tutor.data.local.dao.*
import com.mext.utbk.tutor.data.local.entity.*
import com.mext.utbk.tutor.domain.model.*
import com.mext.utbk.tutor.domain.repository.*
import kotlinx.coroutines.flow.first

class MaterialRepositoryImpl : MaterialRepository {
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
}

class ChatRepositoryImpl(private val chatDao: ChatDao) : ChatRepository {
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

        // Generate AI Response
        val responseText = "Pertanyaan Anda: \"$message\".\n\nUntuk menjawab ini, mari kita breakdown konsep dasarnya terlebih dahulu. Di MEXT/UTBK, materi ini biasanya diselesaikan dengan rumus atau cara eliminasi cepat. \n\nMisalkan kita memiliki variabel x dan y, kita dapat mensubstitusikannya ke persamaan utama. Apakah ada langkah spesifik yang ingin Anda tanyakan lebih detail?"
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
}
