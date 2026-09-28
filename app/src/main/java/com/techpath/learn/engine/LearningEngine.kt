package com.techpath.learn.engine

import com.techpath.learn.model.*

object LearningEngine {
    fun readiness(project: ProjectPath, progress: UserProgress): Int {
        if (project.conceptIds.isEmpty()) return 0
        val done = project.conceptIds.count { it in progress.mastered }
        return ((done.toDouble() / project.conceptIds.size) * 100).toInt()
    }

    fun nextConcept(project: ProjectPath, catalog: Catalog, progress: UserProgress): Concept? {
        val map = catalog.concepts.associateBy { it.id }
        return project.conceptIds
            .mapNotNull(map::get)
            .firstOrNull { concept ->
                concept.id !in progress.mastered && concept.prerequisites.all { it in progress.mastered || it !in project.conceptIds }
            }
            ?: project.conceptIds.mapNotNull(map::get).firstOrNull { it.id !in progress.mastered }
    }

    fun missingPrerequisites(concept: Concept, progress: UserProgress): List<String> =
        concept.prerequisites.filterNot(progress.mastered::contains)

    fun recommendedNext(concept: Concept, catalog: Catalog): List<Concept> {
        val map = catalog.concepts.associateBy { it.id }
        return concept.nextSteps.mapNotNull(map::get)
    }

    fun search(catalog: Catalog, query: String): List<Concept> {
        val q = query.trim().lowercase()
        if (q.isBlank()) return emptyList()
        return catalog.concepts.filter {
            it.title.lowercase().contains(q) ||
            it.summary.lowercase().contains(q) ||
            it.realWorldUses.any { use -> use.lowercase().contains(q) }
        }.take(30)
    }
}
