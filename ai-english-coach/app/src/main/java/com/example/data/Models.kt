package com.example.data

data class LearningLevel(
    val levelNumber: Int, // 0 to 6
    val title: String,
    val subtitle: String,
    val description: String,
    val targetCefr: String,
    val hindiAssistanceDefault: Boolean
)

data class Lesson(
    val id: String,
    val levelNumber: Int,
    val title: String,
    val category: String,
    val description: String,
    val durationMinutes: Int,
    val isCompleted: Boolean = false,
    val contentSummary: String = ""
)

data class Scenario(
    val id: String,
    val title: String,
    val description: String,
    val levelNumber: Int,
    val category: String,
    val iconName: String,
    val durationMin: Int = 5
)

data class GrammarLesson(
    val id: String,
    val levelNumber: Int,
    val lessonNumber: Int,
    val title: String,
    val description: String,
    val exampleEnglish: String,
    val exampleHindi: String,
    val durationMinutes: Int,
    val badge: String
)

data class VocabularyWord(
    val id: String,
    val levelNumber: Int,
    val word: String,
    val phonetic: String,
    val partOfSpeech: String,
    val hindiMeaning: String,
    val exampleSentence: String,
    val category: String,
    val isMastered: Boolean = false
)

data class AssessmentAudit(
    val id: String,
    val title: String,
    val context: String,
    val timeAgo: String,
    val score: Int,
    val feedback: String,
    val keyTerm: String
)

data class UserMistake(
    val id: String,
    val mistakeText: String,
    val correctionText: String,
    val ruleExplanation: String,
    val category: String,
    val frequency: Int = 1
)

data class UserProfileState(
    val learnerName: String = "English Learner",
    val currentLevelNumber: Int = 1, // 0 to 6
    val proficiencyLevel: String = "Beginner (Level 1)",
    val preferredMode: String = "Spoken Conversation Focused",
    val dailyTargetMinutes: Int = 20,
    val hindiAssistanceEnabled: Boolean = true,
    val audioPronunciationGuide: Boolean = true,
    val tutorVoice: String = "Maya",
    val speechPace: Float = 1.0f,
    val dailyStreak: Int = 7,
    val wordsMastered: Int = 142,
    val minutesToday: Int = 12,
    val hasCompletedPlacement: Boolean = true
)

data class PlacementQuestion(
    val id: Int,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val targetLevel: Int
)
