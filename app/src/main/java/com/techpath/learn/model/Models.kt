package com.techpath.learn.model

data class Category(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
)

data class Concept(
    val id: String,
    val categoryId: String,
    val title: String,
    val summary: String,
    val whyItMatters: String,
    val prerequisites: List<String>,
    val nextSteps: List<String>,
    val learnSteps: List<String>,
    val handsOn: String,
    val realWorldUses: List<String>,
    val checkQuestion: String,
    val checkAnswer: String,
    val safetyNote: String? = null,
)

data class ProjectPath(
    val id: String,
    val title: String,
    val description: String,
    val conceptIds: List<String>,
    val outcome: String,
)

data class Catalog(
    val categories: List<Category>,
    val concepts: List<Concept>,
    val projects: List<ProjectPath>,
)

data class UserProgress(
    val mastered: Set<String> = emptySet(),
    val started: Set<String> = emptySet(),
    val selectedProjectId: String? = null,
)
