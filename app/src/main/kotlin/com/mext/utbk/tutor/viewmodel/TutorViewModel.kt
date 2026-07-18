package com.mext.utbk.tutor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mext.utbk.tutor.domain.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MaterialViewModel : ViewModel() {
    private val _subjects = MutableStateFlow<List<Subject>>(emptyList())
    val subjects: StateFlow<List<Subject>> = _subjects

    private val _topics = MutableStateFlow<List<Topic>>(emptyList())
    val topics: StateFlow<List<Topic>> = _topics

    private val _currentTopic = MutableStateFlow<Topic?>(null)
    val currentTopic: StateFlow<Topic?> = _currentTopic

    init {
        loadMockSubjects()
    }

    private fun loadMockSubjects() {
        _subjects.value = listOf(
            Subject("utbk_mat", "Matematika UTBK", "UTBK", "Materi TPS Kuantitatif & Matematika UTBK"),
            Subject("utbk_ind", "Literasi Bahasa Indonesia", "UTBK", "Materi Pemahaman Bacaan & Menulis"),
            Subject("mext_mat", "Matematika MEXT", "MEXT", "Matematika MEXT Kategori A/B (Aljabar, Kalkulus, dll)"),
            Subject("mext_phy", "Fisika MEXT", "MEXT", "Mekanika, Termodinamika, Gelombang, & Elektromagnetik")
        )
    }

    fun loadTopics(subjectId: String) {
        _topics.value = listOf(
            Topic("t1", subjectId, "Bab 1: Persamaan Kuadrat", "Materi tentang akar-akar persamaan kuadrat, diskriminan, dan sifat-sifatnya.", "Ringkasan: D = b^2 - 4ac. Jika D > 0 akar real dan berbeda."),
            Topic("t2", subjectId, "Bab 2: Turunan (Kalkulus)", "Penjelasan konsep limit fungsi, diferensial turunan pertama, dan aplikasi turunan.", "Ringkasan: f'(x) menyatakan laju perubahan sesaat dari fungsi f(x).")
        )
    }

    fun loadTopicDetail(subjectId: String, topicId: String) {
        _currentTopic.value = Topic(
            id = topicId,
            subjectId = subjectId,
            title = if (topicId == "t1") "Bab 1: Persamaan Kuadrat" else "Bab 2: Turunan (Kalkulus)",
            content = "Ini adalah konten lengkap materi pelajaran. " +
                    "Persamaan kuadrat adalah persamaan polinomial orde dua. Bentuk umumnya adalah ax^2 + bx + c = 0. " +
                    "Rumus abc digunakan untuk mencari akar-akarnya: x = (-b ± √(b^2 - 4ac)) / 2a. " +
                    "Konsep ini sangat sering diuji baik di UTBK maupun ujian beasiswa MEXT.",
            summary = "Ringkasan Cepat: Persamaan kuadrat ax^2 + bx + c = 0 memiliki dua akar."
        )
    }
}

class QuizViewModel : ViewModel() {
    private val _questions = MutableStateFlow<List<QuizQuestion>>(emptyList())
    val questions: StateFlow<List<QuizQuestion>> = _questions

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex

    private val _selectedAnswerIndex = MutableStateFlow<Int?>(null)
    val selectedAnswerIndex: StateFlow<Int?> = _selectedAnswerIndex

    private val _isAnswerSubmitted = MutableStateFlow(false)
    val isAnswerSubmitted: StateFlow<Boolean> = _isAnswerSubmitted

    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score

    fun loadMockQuiz(topicId: String) {
        _questions.value = listOf(
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
        _currentQuestionIndex.value = 0
        _selectedAnswerIndex.value = null
        _isAnswerSubmitted.value = false
        _score.value = 0
    }

    fun selectAnswer(index: Int) {
        if (!_isAnswerSubmitted.value) {
            _selectedAnswerIndex.value = index
        }
    }

    fun submitAnswer() {
        val current = _questions.value.getOrNull(_currentQuestionIndex.value) ?: return
        if (_selectedAnswerIndex.value != null && !_isAnswerSubmitted.value) {
            _isAnswerSubmitted.value = true
            if (_selectedAnswerIndex.value == current.correctAnswerIndex) {
                _score.value += 50
            }
        }
    }

    fun nextQuestion() {
        if (_currentQuestionIndex.value < _questions.value.size - 1) {
            _currentQuestionIndex.value += 1
            _selectedAnswerIndex.value = null
            _isAnswerSubmitted.value = false
        }
    }
}

class ChatViewModel : ViewModel() {
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping

    init {
        _messages.value = listOf(
            ChatMessage("m0", "AI", "Halo! Saya adalah Tutor AI MEXT/UTBK Anda. Ada materi atau soal matematika/fisika yang ingin dibahas hari ini?")
        )
    }

    fun updateInputText(text: String) {
        _inputText.value = text
    }

    fun sendMessage() {
        val text = _inputText.value.trim()
        if (text.isEmpty()) return

        val userMessage = ChatMessage(
            id = "user_${System.currentTimeMillis()}",
            sender = "USER",
            messageText = text
        )

        _messages.value = _messages.value + userMessage
        _inputText.value = ""
        _isTyping.value = true

        viewModelScope.launch {
            kotlinx.coroutines.delay(1500) // Simulasi berpikir AI
            val responseText = generateMockAIResponse(text)
            val aiMessage = ChatMessage(
                id = "ai_${System.currentTimeMillis()}",
                sender = "AI",
                messageText = responseText
            )
            _messages.value = _messages.value + aiMessage
            _isTyping.value = false
        }
    }

    private fun generateMockAIResponse(question: String): String {
        return "Pertanyaan Anda: \"$question\".\n\nUntuk menjawab ini, mari kita breakdown konsep dasarnya terlebih dahulu. Di MEXT/UTBK, materi ini biasanya diselesaikan dengan rumus atau cara eliminasi cepat. \n\nMisalkan kita memiliki variabel x dan y, kita dapat mensubstitusikannya ke persamaan utama. Apakah ada langkah spesifik yang ingin Anda tanyakan lebih detail?"
    }
}

class PlannerViewModel : ViewModel() {
    private val _plans = MutableStateFlow<List<StudyPlan>>(emptyList())
    val plans: StateFlow<List<StudyPlan>> = _plans

    init {
        loadMockPlans()
    }

    private fun loadMockPlans() {
        _plans.value = listOf(
            StudyPlan("p1", "Senin", "Belajar Persamaan Kuadrat UTBK", true),
            StudyPlan("p2", "Selasa", "Latihan Soal Turunan MEXT", false),
            StudyPlan("p3", "Rabu", "Simulasi Mandiri Kimia MEXT", false),
            StudyPlan("p4", "Kamis", "Membaca Rangkuman Bahasa Inggris", false)
        )
    }

    fun togglePlan(id: String) {
        _plans.value = _plans.value.map {
            if (it.id == id) it.copy(isCompleted = !it.isCompleted) else it
        }
    }
}
