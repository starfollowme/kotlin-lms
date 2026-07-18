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
}
