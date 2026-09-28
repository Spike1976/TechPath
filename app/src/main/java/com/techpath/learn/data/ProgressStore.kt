package com.techpath.learn.data

import android.content.Context
import com.techpath.learn.model.UserProgress

class ProgressStore(context: Context) {
    private val prefs = context.getSharedPreferences("techpath_progress", Context.MODE_PRIVATE)

    fun load(): UserProgress = UserProgress(
        mastered = prefs.getStringSet("mastered", emptySet()) ?: emptySet(),
        started = prefs.getStringSet("started", emptySet()) ?: emptySet(),
        selectedProjectId = prefs.getString("project", null)
    )

    fun save(progress: UserProgress) {
        prefs.edit()
            .putStringSet("mastered", progress.mastered)
            .putStringSet("started", progress.started)
            .putString("project", progress.selectedProjectId)
            .apply()
    }
}
