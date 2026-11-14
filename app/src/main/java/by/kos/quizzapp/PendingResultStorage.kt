
package by.kos.quizzapp

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object PendingResultStorage {

    private const val PREFS_NAME = "pending_test_results"
    private const val KEY_RESULTS = "results"

    @Synchronized
    fun addResult(
        context: Context,
        documentId: String,
        result: JSONObject
    ): Boolean {
        val prefs = context.applicationContext
            .getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            )

        return try {
            val results = JSONArray(
                prefs.getString(KEY_RESULTS, "[]") ?: "[]"
            )

            // Не добавляем одну попытку дважды
            for (i in 0 until results.length()) {
                val item = results.getJSONObject(i)

                if (item.getString("documentId") == documentId) {
                    return true
                }
            }

            val item = JSONObject().apply {
                put("documentId", documentId)
                put("result", result)
            }

            results.put(item)

            // commit() подтверждает запись на диск
            prefs.edit()
                .putString(KEY_RESULTS, results.toString())
                .commit()

        } catch (e: Exception) {
            false
        }
    }

    @Synchronized
    fun getResults(context: Context): List<JSONObject> {
        val prefs = context.applicationContext
            .getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            )

        return try {
            val results = JSONArray(
                prefs.getString(KEY_RESULTS, "[]") ?: "[]"
            )

            List(results.length()) { index ->
                results.getJSONObject(index)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    @Synchronized
    fun removeResult(
        context: Context,
        documentId: String
    ): Boolean {
        val prefs = context.applicationContext
            .getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            )

        return try {
            val results = JSONArray(
                prefs.getString(KEY_RESULTS, "[]") ?: "[]"
            )

            val updatedResults = JSONArray()

            for (i in 0 until results.length()) {
                val item = results.getJSONObject(i)

                if (item.getString("documentId") != documentId) {
                    updatedResults.put(item)
                }
            }

            prefs.edit()
                .putString(KEY_RESULTS, updatedResults.toString())
                .commit()

        } catch (e: Exception) {
            false
        }
    }
}
