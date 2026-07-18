package com.mext.utbk.tutor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mext.utbk.tutor.domain.model.*
import com.mext.utbk.tutor.domain.repository.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MaterialViewModel(private val repository: MaterialRepository) : ViewModel() {
    private val _subjects = MutableStateFlow<List<Subject>>(emptyList())
    val subjects: StateFlow<List<Subject>> = _subjects

    private val _topics = MutableStateFlow<List<Topic>>(emptyList())
    val topics: StateFlow<List<Topic>> = _topics

    private val _currentTopic = MutableStateFlow<Topic?>(null)
    val currentTopic: StateFlow<Topic?> = _currentTopic

    private val _isCurrentTopicBookmarked = MutableStateFlow(false)
    val isCurrentTopicBookmarked: StateFlow<Boolean> = _isCurrentTopicBookmarked

    private val _bookmarkedTopics = MutableStateFlow<List<Topic>>(emptyList())
    val bookmarkedTopics: StateFlow<List<Topic>> = _bookmarkedTopics

    init {
        loadSubjects()
        loadBookmarkedTopics()
    }

    private fun loadSubjects() {
        viewModelScope.launch {
            _subjects.value = repository.getSubjects()
        }
    }

    fun loadTopics(subjectId: String) {
        viewModelScope.launch {
            _topics.value = repository.getTopics(subjectId)
        }
    }

    fun loadTopicDetail(subjectId: String, topicId: String) {
        viewModelScope.launch {
            val detail = repository.getTopicDetail(subjectId, topicId)
            _currentTopic.value = detail
            if (detail != null) {
                checkIsBookmarked(detail.id)
            }
        }
    }

    fun checkIsBookmarked(topicId: String) {
        viewModelScope.launch {
            _isCurrentTopicBookmarked.value = repository.isBookmarked(topicId)
        }
    }

    fun toggleBookmark(topic: Topic) {
        viewModelScope.launch {
            val currentlyBookmarked = repository.isBookmarked(topic.id)
            if (currentlyBookmarked) {
                repository.unbookmarkTopic(topic)
                _isCurrentTopicBookmarked.value = false
            } else {
                repository.bookmarkTopic(topic)
                _isCurrentTopicBookmarked.value = true
            }
            loadBookmarkedTopics() // Refresh bookmarks list
        }
    }

    fun loadBookmarkedTopics() {
        viewModelScope.launch {
            _bookmarkedTopics.value = repository.getBookmarkedTopics()
        }
    }
}

class QuizViewModel(private val repository: QuizRepository) : ViewModel() {
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
        viewModelScope.launch {
            _questions.value = repository.generateQuestions(topicId)
            _currentQuestionIndex.value = 0
            _selectedAnswerIndex.value = null
            _isAnswerSubmitted.value = false
            _score.value = 0
        }
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

            // If it's the last question, save history
            if (_currentQuestionIndex.value == _questions.value.size - 1) {
                viewModelScope.launch {
                    repository.submitQuizResult(_score.value, _questions.value.size)
                }
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

class ChatViewModel(private val repository: ChatRepository) : ViewModel() {
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            val history = repository.getChatHistory()
            if (history.isEmpty()) {
                _messages.value = listOf(
                    ChatMessage("m0", "AI", "Halo! Saya adalah Tutor AI MEXT/UTBK Anda. Ada materi atau soal matematika/fisika yang ingin dibahas hari ini?")
                )
            } else {
                _messages.value = history
            }
        }
    }

    fun updateInputText(text: String) {
        _inputText.value = text
    }

    fun sendMessage() {
        val text = _inputText.value.trim()
        if (text.isEmpty()) return

        // Instant UI feedback for user message
        val userMessage = ChatMessage(
            id = "user_${System.currentTimeMillis()}",
            sender = "USER",
            messageText = text
        )
        _messages.value = _messages.value + userMessage
        _inputText.value = ""
        _isTyping.value = true

        viewModelScope.launch {
            // repository.sendMessage handles user insert, AI response generation, and AI insert
            val aiResponse = repository.sendMessage(text)
            _messages.value = _messages.value + aiResponse
            _isTyping.value = false
        }
    }
}

class PlannerViewModel(private val repository: PlannerRepository) : ViewModel() {
    private val _plans = MutableStateFlow<List<StudyPlan>>(emptyList())
    val plans: StateFlow<List<StudyPlan>> = _plans

    init {
        loadPlans()
    }

    private fun loadPlans() {
        viewModelScope.launch {
            _plans.value = repository.getStudyPlans()
        }
    }

    fun togglePlan(id: String) {
        viewModelScope.launch {
            repository.togglePlanCompletion(id)
            loadPlans() // Reload after modification
        }
    }

    fun addPlan(day: String, target: String) {
        viewModelScope.launch {
            val plan = StudyPlan("p_${System.currentTimeMillis()}", day, target, false)
            repository.addStudyPlan(plan)
            loadPlans()
        }
    }

    fun deletePlan(id: String) {
        viewModelScope.launch {
            repository.deleteStudyPlan(id)
            loadPlans()
        }
    }
}

class HistoryViewModel(
    private val quizRepository: QuizRepository,
    private val plannerRepository: PlannerRepository,
    private val materialRepository: MaterialRepository
) : ViewModel() {
    private val _history = MutableStateFlow<List<HistoryEntry>>(emptyList())
    val history: StateFlow<List<HistoryEntry>> = _history

    private val _statsQuizzes = MutableStateFlow(0)
    val statsQuizzes: StateFlow<Int> = _statsQuizzes

    private val _statsAvgScore = MutableStateFlow(0)
    val statsAvgScore: StateFlow<Int> = _statsAvgScore

    private val _statsPlans = MutableStateFlow(0)
    val statsPlans: StateFlow<Int> = _statsPlans

    private val _statsBookmarks = MutableStateFlow(0)
    val statsBookmarks: StateFlow<Int> = _statsBookmarks

    init {
        loadHistoryAndStats()
    }

    fun loadHistoryAndStats() {
        viewModelScope.launch {
            val hist = quizRepository.getHistory()
            _history.value = hist

            // Calculate quiz stats
            val quizEntries = hist.filter { it.type == "QUIZ" }
            _statsQuizzes.value = quizEntries.size

            var totalScore = 0
            quizEntries.forEach { entry ->
                // Detail pattern: "Menyelesaikan kuis dengan skor X ..."
                val scoreMatch = Regex("skor (\\d+)").find(entry.detail)
                scoreMatch?.groupValues?.getOrNull(1)?.toIntOrNull()?.let {
                    totalScore += it
                }
            }
            _statsAvgScore.value = if (quizEntries.isNotEmpty()) totalScore / quizEntries.size else 0

            // Calculate planner stats
            val completedPlans = plannerRepository.getStudyPlans().count { it.isCompleted }
            _statsPlans.value = completedPlans

            // Calculate bookmark stats
            val bookmarksCount = materialRepository.getBookmarkedTopics().size
            _statsBookmarks.value = bookmarksCount
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            quizRepository.clearHistory()
            loadHistoryAndStats()
        }
    }
}

class SimulationViewModel(private val repository: QuizRepository) : ViewModel() {
    private val _questions = MutableStateFlow<List<QuizQuestion>>(emptyList())
    val questions: StateFlow<List<QuizQuestion>> = _questions

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex

    private val _selectedAnswers = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val selectedAnswers: StateFlow<Map<Int, Int>> = _selectedAnswers

    private val _isSubmitted = MutableStateFlow(false)
    val isSubmitted: StateFlow<Boolean> = _isSubmitted

    private val _timeLeft = MutableStateFlow(600) // 10 minutes in seconds
    val timeLeft: StateFlow<Int> = _timeLeft

    private val _timerString = MutableStateFlow("10:00")
    val timerString: StateFlow<String> = _timerString

    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score

    private val _examType = MutableStateFlow("UTBK")
    val examType: StateFlow<String> = _examType

    private var timerJob: kotlinx.coroutines.Job? = null

    fun startSimulation(type: String) {
        _examType.value = type
        _currentQuestionIndex.value = 0
        _selectedAnswers.value = emptyMap()
        _isSubmitted.value = false
        _timeLeft.value = 600
        _timerString.value = "10:00"
        _score.value = 0

        // Mock simulation questions (5 items)
        _questions.value = listOf(
            QuizQuestion(
                id = "sim_q1",
                topicId = "sim",
                questionText = "Jika log 2 = a dan log 3 = b, maka nilai log 72 adalah...",
                options = listOf("3a + 2b", "2a + 3b", "a^3 * b^2", "a^2 * b^3"),
                correctAnswerIndex = 0,
                explanation = "log 72 = log (8 * 9) = log (2^3 * 3^2) = 3 log 2 + 2 log 3 = 3a + 2b."
            ),
            QuizQuestion(
                id = "sim_q2",
                topicId = "sim",
                questionText = "Sebuah mobil bergerak dengan kecepatan v(t) = 3t^2 + 2t. Tentukan posisi s(t) jika s(0) = 5!",
                options = listOf("t^3 + t^2", "t^3 + t^2 + 5", "3t^3 + 2t^2 + 5", "t^3 + t^2 - 5"),
                correctAnswerIndex = 1,
                explanation = "s(t) = integral v(t) dt = t^3 + t^2 + C. Karena s(0) = 5, maka C = 5. Jadi s(t) = t^3 + t^2 + 5."
            ),
            QuizQuestion(
                id = "sim_q3",
                topicId = "sim",
                questionText = "Suatu partikel bermassa 2 kg bergerak dengan kecepatan 10 m/s. Berapakah energi kinetik partikel tersebut?",
                options = listOf("100 J", "200 J", "50 J", "25 J"),
                correctAnswerIndex = 0,
                explanation = "Ek = 0.5 * m * v^2 = 0.5 * 2 * 10^2 = 100 J."
            ),
            QuizQuestion(
                id = "sim_q4",
                topicId = "sim",
                questionText = "Sebuah pegas memiliki konstanta k = 200 N/m. Jika ditarik sejauh 10 cm, berapakah energi potensial pegas?",
                options = listOf("1 J", "2 J", "10 J", "20 J"),
                correctAnswerIndex = 0,
                explanation = "Ep = 0.5 * k * x^2 = 0.5 * 200 * (0.1)^2 = 100 * 0.01 = 1 J."
            ),
            QuizQuestion(
                id = "sim_q5",
                topicId = "sim",
                questionText = "Jika f(x) = (2x + 3) / (x - 1) untuk x != 1, tentukan f^(-1)(x)!",
                options = listOf("(x + 3)/(x - 2)", "(x - 3)/(x + 2)", "(3x + 1)/(x - 2)", "(3x - 1)/(x + 2)"),
                correctAnswerIndex = 0,
                explanation = "y = (2x + 3)/(x - 1) -> y(x - 1) = 2x + 3 -> xy - y = 2x + 3 -> x(y - 2) = y + 3 -> x = (y + 3)/(y - 2). Jadi f^(-1)(x) = (x + 3)/(x - 2)."
            )
        )

        // Start countdown timer
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_timeLeft.value > 0 && !_isSubmitted.value) {
                kotlinx.coroutines.delay(1000)
                _timeLeft.value -= 1
                val min = _timeLeft.value / 60
                val sec = _timeLeft.value % 60
                val minStr = if (min < 10) "0$min" else "$min"
                val secStr = if (sec < 10) "0$sec" else "$sec"
                _timerString.value = "$minStr:$secStr"
            }
            if (_timeLeft.value <= 0 && !_isSubmitted.value) {
                submitSimulation()
            }
        }
    }

    fun selectAnswer(questionIndex: Int, answerIndex: Int) {
        if (!_isSubmitted.value) {
            val updated = _selectedAnswers.value.toMutableMap()
            updated[questionIndex] = answerIndex
            _selectedAnswers.value = updated
        }
    }

    fun setCurrentQuestion(index: Int) {
        if (index in 0 until _questions.value.size) {
            _currentQuestionIndex.value = index
        }
    }

    fun submitSimulation() {
        if (_isSubmitted.value) return
        _isSubmitted.value = true
        timerJob?.cancel()

        // Calculate score
        var correct = 0
        _questions.value.forEachIndexed { index, question ->
            if (_selectedAnswers.value[index] == question.correctAnswerIndex) {
                correct++
            }
        }
        val computedScore = (correct.toFloat() / _questions.value.size * 100).toInt()
        _score.value = computedScore

        // Save to database
        viewModelScope.launch {
            repository.submitSimulationResult(computedScore, _examType.value)
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
