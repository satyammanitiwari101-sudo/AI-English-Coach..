package com.example.viewmodel

import androidx.lifecycle.ViewModel
import com.example.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ChatMessage(
    val id: String,
    val sender: String, // "AI" or "User"
    val text: String,
    val hindiTranslation: String = "",
    val pronunciationScore: Int? = null,
    val timestamp: String = "Just now"
)

class CoachViewModel : ViewModel() {
    private val _profileState = MutableStateFlow(UserProfileState())
    val profileState: StateFlow<UserProfileState> = _profileState.asStateFlow()

    private val _currentTab = MutableStateFlow("home")
    val currentTab: StateFlow<String> = _currentTab.asStateFlow()

    private val _activeScenario = MutableStateFlow("Ordering Food at a Café")
    val activeScenario: StateFlow<String> = _activeScenario.asStateFlow()

    // Placement Assessment State
    private val _showPlacementDialog = MutableStateFlow(false)
    val showPlacementDialog: StateFlow<Boolean> = _showPlacementDialog.asStateFlow()

    private val _placementStep = MutableStateFlow(0)
    val placementStep: StateFlow<Int> = _placementStep.asStateFlow()

    private val _userMistakes = MutableStateFlow(
        listOf(
            UserMistake("1", "He go to school.", "He goes to school.", "Subject-verb agreement: third-person singular takes -s/-es.", "Grammar", 3),
            UserMistake("2", "I am agree with you.", "I agree with you.", "State verbs like agree do not take 'am' in present simple.", "Grammar", 2)
        )
    )
    val userMistakes: StateFlow<List<UserMistake>> = _userMistakes.asStateFlow()

    private val _routineItems = MutableStateFlow(
        listOf(
            Triple("Speaking Practice", "5 min AI conversation completed", true),
            Triple("Vocabulary Builder", "3 of 5 new terms practiced", false),
            Triple("Listen & Repeat", "1 scenario pending", false)
        )
    )
    val routineItems: StateFlow<List<Triple<String, String, Boolean>>> = _routineItems.asStateFlow()

    private val _chatMessages = MutableStateFlow(
        listOf(
            ChatMessage(
                id = "1",
                sender = "AI",
                text = "Hello! Welcome to the city cafe. What can I get started for you today? Would you like hot coffee or an iced brew?",
                hindiTranslation = "नमस्ते! सिटी कैफे में आपका स्वागत है। आपके लिए क्या लाऊं? आप गरम कॉफी पसंद करेंगे या आइस्ड ब्रू?"
            ),
            ChatMessage(
                id = "2",
                sender = "User",
                text = "Good morning! Can I see the menu, please?",
                pronunciationScore = 94
            ),
            ChatMessage(
                id = "3",
                sender = "AI",
                text = "Sure thing! Here’s our morning menu. We have freshly baked croissants and specialty lattes.",
                hindiTranslation = "बिल्कुल! ये रहा हमारा मेन्यू। हमारे पास ताजे बेक किए गए क्रोइसैन और लट्टे हैं।"
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    fun setTab(tab: String) {
        _currentTab.value = tab
    }

    fun setActiveScenario(scenarioTitle: String) {
        _activeScenario.value = scenarioTitle
        _currentTab.value = "conversation"
    }

    fun startPlacementTest() {
        _placementStep.value = 0
        _showPlacementDialog.value = true
    }

    fun answerPlacementQuestion(selectedOptionIndex: Int) {
        val questions = placementQuestions
        val current = _placementStep.value
        if (selectedOptionIndex == questions[current].correctIndex) {
            if (current < questions.size - 1) {
                _placementStep.value = current + 1
            } else {
                finishPlacement(levelResult = 4)
            }
        } else {
            val assignedLevel = when {
                current <= 1 -> 0
                current <= 3 -> 1
                current <= 5 -> 2
                else -> 3
            }
            finishPlacement(levelResult = assignedLevel)
        }
    }

    private fun finishPlacement(levelResult: Int) {
        _showPlacementDialog.value = false
        val levelName = when (levelResult) {
            0 -> "Absolute Beginner (Level 0)"
            1 -> "Beginner (Level 1)"
            2 -> "Elementary (Level 2)"
            3 -> "Intermediate (Level 3)"
            4 -> "Upper Intermediate (Level 4)"
            5 -> "Advanced (Level 5)"
            else -> "Fluent / High (Level 6)"
        }
        _profileState.update {
            it.copy(
                currentLevelNumber = levelResult,
                proficiencyLevel = levelName,
                hasCompletedPlacement = true,
                hindiAssistanceEnabled = levelResult <= 2
            )
        }
        showToast("Placement complete! Assigned to $levelName")
    }

    fun updateProfile(
        level: String,
        mode: String,
        targetMinutes: Int,
        hindi: Boolean,
        audioGuide: Boolean,
        voice: String,
        pace: Float
    ) {
        val lvlNum = when {
            level.contains("Level 0") || level.contains("Absolute") -> 0
            level.contains("Level 1") || level.contains("Beginner") -> 1
            level.contains("Level 2") || level.contains("Elementary") -> 2
            level.contains("Level 3") || level.contains("Intermediate") -> 3
            level.contains("Level 4") || level.contains("Upper") -> 4
            level.contains("Level 5") || level.contains("Advanced") -> 5
            else -> 6
        }
        _profileState.update {
            it.copy(
                currentLevelNumber = lvlNum,
                proficiencyLevel = level,
                preferredMode = mode,
                dailyTargetMinutes = targetMinutes,
                hindiAssistanceEnabled = hindi,
                audioPronunciationGuide = audioGuide,
                tutorVoice = voice,
                speechPace = pace
            )
        }
        showToast("Settings & preferences saved successfully!")
    }

    fun toggleRoutineItem(index: Int) {
        _routineItems.update { list ->
            list.mapIndexed { i, item ->
                if (i == index) item.copy(third = !item.third) else item
            }
        }
    }

    fun sendUserMessage(text: String) {
        if (text.isBlank()) return
        val newUserMsg = ChatMessage(
            id = System.currentTimeMillis().toString(),
            sender = "User",
            text = text,
            pronunciationScore = 95
        )
        var aiFeedback = "That sounds wonderful! Let's practice phrasing that clearly with natural intonation."
        var hindiTr = "बहुत बढ़िया! आइए इसका अभ्यास करें।"
        if (text.contains("he go", ignoreCase = true) || text.contains("she go", ignoreCase = true)) {
            aiFeedback = "Good try! Remember subject-verb agreement: 'He goes to school' with an 's'."
            hindiTr = "अच्छा प्रयास! याद रखें: ही/शी के साथ 'goes' का प्रयोग होता है।"
            _userMistakes.update { list ->
                listOf(UserMistake(System.currentTimeMillis().toString(), text, text.replace("go", "goes", true), "Third-person singular verb agreement", "Grammar", 1)) + list
            }
        }

        val aiReply = ChatMessage(
            id = (System.currentTimeMillis() + 1).toString(),
            sender = "AI",
            text = aiFeedback,
            hindiTranslation = hindiTr
        )
        _chatMessages.update { it + newUserMsg + aiReply }
        _profileState.update {
            it.copy(
                minutesToday = (it.minutesToday + 1).coerceAtMost(60),
                wordsMastered = it.wordsMastered + 1
            )
        }
    }

    fun showToast(msg: String) {
        _toastMessage.value = msg
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun resetLearningPlan() {
        _profileState.value = UserProfileState()
        showToast("Learning plan reset to baseline defaults")
    }

    val levels = listOf(
        LearningLevel(0, "Level 0 — Absolute Beginner", "Alphabet, sounds, numbers, basic words & greetings", "For users starting from zero English knowledge with full Hindi support.", "A1 Baseline", true),
        LearningLevel(1, "Level 1 — Beginner", "Sentence formation, family, home, food & daily routine", "Build basic confidence with everyday words and simple Q&A.", "A1 Elementary", true),
        LearningLevel(2, "Level 2 — Elementary", "Daily conversations, shopping, restaurant, travel & hotel", "Handle practical real-world interactions and travel scenarios.", "A2 Pre-Inter", true),
        LearningLevel(3, "Level 3 — Intermediate", "Natural conversations, fluency, tenses & roleplay", "Express opinions smoothly and master common past/future tenses.", "B1 Inter", false),
        LearningLevel(4, "Level 4 — Upper Intermediate", "Workplace English, meetings, presentations & interviews", "Excel in professional environments with business idioms & phrasal verbs.", "B2 Upper Inter", false),
        LearningLevel(5, "Level 5 — Advanced", "Academic English, discussions, debates & complex grammar", "Participate in complex intellectual discourse and debates.", "C1 Advanced", false),
        LearningLevel(6, "Level 6 — High / Fluent", "Unrestricted AI conversation, immersion & fluency refinement", "Master native-level rhythm, subtle nuance, and professional mastery.", "C2 Mastery", false)
    )

    val placementQuestions = listOf(
        PlacementQuestion(1, "What is the English greeting for 'नमस्ते' in the morning?", listOf("Good night", "Good morning", "Goodbye", "Thank you"), 1, 1),
        PlacementQuestion(2, "Choose the correct sentence:", listOf("He go to school.", "He goes to school.", "He going school.", "He gone school"), 1, 2),
        PlacementQuestion(3, "How do you ask the price of an item in a shop?", listOf("Where is this?", "How much is this?", "What time is it?", "Who are you?"), 1, 3),
        PlacementQuestion(4, "Select the correct past tense of 'go':", listOf("goed", "gone", "went", "going"), 2, 4),
        PlacementQuestion(5, "Which phrase is best for a formal job interview?", listOf("Hey boss, what's up?", "I am eager to contribute my expertise.", "Give me this job.", "I don't know."), 1, 5)
    )

    val scenarios = listOf(
        Scenario("1", "Daily Life", "Morning routines, grocery errands, and casual neighbor greetings.", 1, "Daily", "wb_sunny"),
        Scenario("2", "Friends Hangout", "Catching up, weekend plans, gossiping, and sharing memes.", 1, "Social", "diversity_3"),
        Scenario("3", "Family Dinner", "Discussing household chores, parenting, and family gatherings.", 1, "Family", "home"),
        Scenario("4", "Shopping & Retail", "Asking for sizes, discounts, return policies, and checkout.", 2, "Shopping", "shopping_bag"),
        Scenario("5", "Restaurant & Cafe", "Ordering food, specifying allergies, and handling billing split.", 2, "Dining", "restaurant"),
        Scenario("6", "Travel & Exploring", "Asking for directions, booking tours, and sightseeing tips.", 2, "Travel", "explore"),
        Scenario("7", "Railway Station", "Platform announcements, ticket counter, delays, and transfers.", 2, "Transit", "train"),
        Scenario("8", "Airport Immigration", "Baggage drop, customs security check, boarding announcements.", 3, "Travel", "flight_takeoff"),
        Scenario("9", "Hotel Check-In", "Room upgrades, asking for amenities, and room service orders.", 3, "Travel", "hotel"),
        Scenario("10", "Office & Standup", "Project updates, collaborating with colleagues, and scheduling meetings.", 4, "Work", "business_center"),
        Scenario("11", "Job Interview", "Behavioral questions, salary negotiation, and storytelling STAR method.", 4, "Career", "badge"),
        Scenario("12", "Phone Call & Booking", "Customer support, leaving voicemails, and handling bad audio.", 3, "Communication", "call"),
        Scenario("13", "Free Unscripted AI", "Open-ended philosophical, tech, or daily topics with spontaneous feedback.", 6, "General", "psychology")
    )

    val grammarLessons = listOf(
        GrammarLesson("0_1", 0, 1, "Alphabet & Basic Sounds", "English letters A-Z and phonetic sounds", "A for Apple, B for Ball", "ए फॉर एप्पल", 10, "Level 0"),
        GrammarLesson("0_2", 0, 2, "Pronouns: I, You, He, She, It", "Basic subject pronouns for daily communication", "I am a learner. You are a student.", "मैं एक शिक्षार्थी हूँ।", 10, "Level 0"),
        GrammarLesson("1_1", 1, 1, "Present Simple: To Be", "Am, Is, Are in daily sentences", "I am happy. She is a doctor.", "मैं खुश हूँ।", 10, "Level 1"),
        GrammarLesson("2_1", 2, 1, "Past Simple Tense", "Talking about yesterday and completed actions", "I visited my family yesterday.", "मैंने कल अपने परिवार से मुलाकात की।", 12, "Level 2"),
        GrammarLesson("3_1", 3, 14, "Tenses for Everyday Conversations", "Past Simple vs Present Perfect", "\"I lost my keys\" vs \"I have lost my keys\".", "बीते हुए कल की बात बनाम हाल ही में पूरी हुई क्रिया।", 12, "Level 3"),
        GrammarLesson("4_1", 4, 15, "Polite Requests & Modal Verbs", "Mastering Could, Would & May", "Transform \"Give me that\" into \"Could you please pass that?\"", "\"मुझे वो दो\" को विनम्र बनाएं।", 8, "Level 4")
    )

    val vocabularyWordOfTheDay = VocabularyWord(
        id = "wotd_1",
        levelNumber = 4,
        word = "Serendipity",
        phonetic = "/ˌser.ənˈdɪp.ə.ti/",
        partOfSpeech = "noun",
        hindiMeaning = "नसीब से अच्छी चीज़ मिलना / सुखद संयोग",
        exampleSentence = "Finding my college roommate at the London airport was pure serendipity!",
        category = "Advanced Lexicon"
    )

    val assessmentAudits = listOf(
        AssessmentAudit("1", "Job Interview Practice", "Simulated Panel • Yesterday", "88/100", 88, "Great vocabulary choice! Try pausing naturally between sentences rather than using filler sounds.", "strategic"),
        AssessmentAudit("2", "Restaurant Ordering", "Bistro Context • 3 days ago", "92/100", 92, "Excellent pronunciation and natural rhythm. Politeness markers were deployed effortlessly.", "would you recommend")
    )
}
