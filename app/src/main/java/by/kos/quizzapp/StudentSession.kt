package by.kos.quizzapp

import android.content.Context

object StudentSession {

    private const val PREFS_NAME = "student_session"
    private const val KEY_NAME = "student_name"
    private const val KEY_GROUP = "student_group"

    fun saveStudent(
        context: Context,
        name: String,
        group: String
    ) {
        context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        ).edit()
            .putString(KEY_NAME, name.trim())
            .putString(KEY_GROUP, group.trim())
            .apply()
    }

    fun getStudentName(context: Context): String {
        return context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        ).getString(KEY_NAME, "").orEmpty()
    }

    fun getStudentGroup(context: Context): String {
        return context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        ).getString(KEY_GROUP, "").orEmpty()
    }

    fun hasStudent(context: Context): Boolean {
        return getStudentName(context).isNotBlank() &&
                getStudentGroup(context).isNotBlank()
    }

    fun clearStudent(context: Context) {
        context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        ).edit()
            .clear()
            .apply()
    }
}
