package com.techpath.learn.data

import android.content.Context
import android.util.Base64
import com.techpath.learn.model.Catalog
import com.techpath.learn.model.Category
import com.techpath.learn.model.Concept
import com.techpath.learn.model.ProjectPath
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.util.zip.GZIPInputStream

class CatalogRepository(private val context: Context) {
    fun load(): Catalog {
        val encoded = (1..3).joinToString("") { part ->
            context.assets
                .open("catalog/catalog.part$part.b64")
                .bufferedReader()
                .use { it.readText().trim() }
        }

        val compressed = Base64.decode(encoded, Base64.DEFAULT)
        val raw = GZIPInputStream(ByteArrayInputStream(compressed))
            .bufferedReader(Charsets.UTF_8)
            .use { it.readText() }

        val root = JSONObject(raw)
        return Catalog(
            categories = root.getJSONArray("categories").mapObjects { o ->
                Category(
                    id = o.getString("id"),
                    title = o.getString("title"),
                    description = o.getString("description"),
                    icon = o.optString("icon", "•")
                )
            },
            concepts = root.getJSONArray("concepts").mapObjects { o ->
                Concept(
                    id = o.getString("id"),
                    categoryId = o.getString("categoryId"),
                    title = o.getString("title"),
                    summary = o.getString("summary"),
                    whyItMatters = o.getString("whyItMatters"),
                    prerequisites = o.optJSONArray("prerequisites").strings(),
                    nextSteps = o.optJSONArray("nextSteps").strings(),
                    learnSteps = o.optJSONArray("learnSteps").strings(),
                    handsOn = o.getString("handsOn"),
                    realWorldUses = o.optJSONArray("realWorldUses").strings(),
                    checkQuestion = o.getString("checkQuestion"),
                    checkAnswer = o.getString("checkAnswer"),
                    safetyNote = o.optString("safetyNote").takeIf { it.isNotBlank() }
                )
            },
            projects = root.getJSONArray("projects").mapObjects { o ->
                ProjectPath(
                    id = o.getString("id"),
                    title = o.getString("title"),
                    description = o.getString("description"),
                    conceptIds = o.getJSONArray("conceptIds").strings(),
                    outcome = o.getString("outcome")
                )
            }
        )
    }
}

private inline fun <T> JSONArray.mapObjects(block: (JSONObject) -> T): List<T> =
    (0 until length()).map { block(getJSONObject(it)) }

private fun JSONArray?.strings(): List<String> =
    if (this == null) emptyList() else (0 until length()).map { getString(it) }
