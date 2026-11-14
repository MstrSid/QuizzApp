
package by.kos.quizzapp

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import org.json.JSONArray
import org.json.JSONObject

object PendingResultSync {

    private const val TAG = "PENDING_SYNC"
    private const val CHECK_TIMEOUT_MS = 20_000L

    private val handler = Handler(Looper.getMainLooper())

    private var isSyncing = false
    private var generation = 0L
    private var checkTimeout: Runnable? = null
    private var offlineMessageLogged = false

    @Suppress("DEPRECATION")
    private fun isInternetAvailable(context: Context): Boolean {

        val manager = context.getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            val network = manager.activeNetwork ?: return false

            val capabilities =
                manager.getNetworkCapabilities(network) ?: return false

            capabilities.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_INTERNET
            ) && capabilities.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_VALIDATED
            )

        } else {

            manager.activeNetworkInfo?.isConnected == true
        }
    }

    @Synchronized
    fun sync(context: Context) {

        if (isSyncing) {
            Log.d(TAG, "Синхронизация уже выполняется")
            return
        }

        val appContext = context.applicationContext

        if (!isInternetAvailable(appContext)) {

            if (!offlineMessageLogged) {
                Log.d(
                    TAG,
                    "Интернет недоступен. Результаты остаются в очереди"
                )

                offlineMessageLogged = true
            }

            return
        }

// Интернет восстановлен — разрешаем повторное сообщение
        offlineMessageLogged = false

        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {
            Log.d(TAG, "Пользователь ещё не авторизован")
            return
        }

        val pendingResults =
            PendingResultStorage.getResults(appContext)

        if (pendingResults.isEmpty()) {
            Log.d(TAG, "Очередь пуста")
            return
        }

        isSyncing = true
        generation++

        val currentGeneration = generation

        Log.d(
            TAG,
            "Ожидают отправки: ${pendingResults.size}"
        )

        syncNext(
            appContext,
            pendingResults,
            0,
            user.uid,
            currentGeneration
        )
    }

    @Synchronized
    private fun isCurrent(g: Long): Boolean {
        return isSyncing && generation == g
    }

    @Synchronized
    private fun cancelCheckTimeout() {

        checkTimeout?.let {
            handler.removeCallbacks(it)
        }

        checkTimeout = null
    }

    @Synchronized
    private fun startCheckTimeout(g: Long) {

        cancelCheckTimeout()

        val timeout = Runnable {

            synchronized(this) {

                if (isCurrent(g)) {

                    Log.w(
                        TAG,
                        "Превышено время проверки сервера. " +
                                "Локальные результаты сохранены"
                    )

                    cancelCheckTimeout()

                    isSyncing = false
                    generation++
                }
            }
        }

        checkTimeout = timeout

        handler.postDelayed(
            timeout,
            CHECK_TIMEOUT_MS
        )
    }

    @Synchronized
    private fun finishSync(g: Long) {

        if (!isCurrent(g)) {
            return
        }

        cancelCheckTimeout()

        isSyncing = false

        Log.d(TAG, "Синхронизация завершена")
    }

    private fun syncNext(
        context: Context,
        results: List<JSONObject>,
        index: Int,
        userId: String,
        g: Long
    ) {

        if (!isCurrent(g)) {
            return
        }

        if (index >= results.size) {
            finishSync(g)
            return
        }

        try {

            val item = results[index]

            val documentId =
                item.getString("documentId")

            val jsonResult =
                item.getJSONObject("result")

            // Не отправляем результат другого пользователя
            if (jsonResult.getString("userId") != userId) {

                Log.w(
                    TAG,
                    "Пропущен результат другого пользователя: $documentId"
                )

                syncNext(
                    context,
                    results,
                    index + 1,
                    userId,
                    g
                )

                return
            }

            val document = FirebaseFirestore.getInstance()
                .collection("test_results")
                .document(documentId)

            // Проверяем документ на сервере
            startCheckTimeout(g)

            document.get(Source.SERVER)
                .addOnSuccessListener { snapshot ->

                    synchronized(this) {

                        if (!isCurrent(g)) {
                            return@addOnSuccessListener
                        }

                        cancelCheckTimeout()

                        if (snapshot.exists()) {

                            if (matchesLocalResult(
                                    snapshot,
                                    jsonResult
                                )
                            ) {

                                val removed =
                                    PendingResultStorage.removeResult(
                                        context,
                                        documentId
                                    )

                                Log.d(
                                    TAG,
                                    "Документ подтверждён: $documentId, " +
                                            "локальная копия удалена: $removed"
                                )

                            } else {

                                Log.e(
                                    TAG,
                                    "Данные документа $documentId " +
                                            "не совпадают с локальными. " +
                                            "Локальная копия сохранена"
                                )
                            }

                            syncNext(
                                context,
                                results,
                                index + 1,
                                userId,
                                g
                            )

                        } else {

                            try {

                                val result =
                                    jsonToFirestoreMap(jsonResult)

                                // Firestore завершит ожидающую запись
                                // после восстановления соединения
                                document.set(result)
                                    .addOnSuccessListener {

                                        synchronized(this) {

                                            if (!isCurrent(g)) {
                                                return@addOnSuccessListener
                                            }

                                            val removed =
                                                PendingResultStorage.removeResult(
                                                    context,
                                                    documentId
                                                )

                                            Log.d(
                                                TAG,
                                                "Результат отправлен: $documentId, " +
                                                        "локальная копия удалена: $removed"
                                            )

                                            syncNext(
                                                context,
                                                results,
                                                index + 1,
                                                userId,
                                                g
                                            )
                                        }
                                    }
                                    .addOnFailureListener { exception ->

                                        synchronized(this) {

                                            if (!isCurrent(g)) {
                                                return@addOnFailureListener
                                            }

                                            Log.e(
                                                TAG,
                                                "Ошибка отправки $documentId. " +
                                                        "Локальная копия сохранена",
                                                exception
                                            )

                                            syncNext(
                                                context,
                                                results,
                                                index + 1,
                                                userId,
                                                g
                                            )
                                        }
                                    }

                            } catch (exception: Exception) {

                                Log.e(
                                    TAG,
                                    "Ошибка подготовки $documentId",
                                    exception
                                )

                                syncNext(
                                    context,
                                    results,
                                    index + 1,
                                    userId,
                                    g
                                )
                            }
                        }
                    }
                }
                .addOnFailureListener { exception ->

                    synchronized(this) {

                        if (!isCurrent(g)) {
                            return@addOnFailureListener
                        }

                        cancelCheckTimeout()

                        Log.e(
                            TAG,
                            "Не удалось проверить $documentId. " +
                                    "Локальная копия сохранена",
                            exception
                        )

                        syncNext(
                            context,
                            results,
                            index + 1,
                            userId,
                            g
                        )
                    }
                }

        } catch (exception: Exception) {

            Log.e(
                TAG,
                "Ошибка обработки результата",
                exception
            )

            syncNext(
                context,
                results,
                index + 1,
                userId,
                g
            )
        }
    }

    // Сравниваем результат на сервере с локальной копией
    private fun matchesLocalResult(
        snapshot: DocumentSnapshot,
        local: JSONObject
    ): Boolean {

        if (!snapshot.exists()) {
            return false
        }

        return try {

            snapshot.getString("userId") ==
                    local.getString("userId") &&

                    snapshot.getString("studentName") ==
                    local.getString("studentName") &&

                    snapshot.getString("studentGroup") ==
                    local.getString("studentGroup") &&

                    snapshot.getLong("testNumber") ==
                    local.getInt("testNumber").toLong() &&

                    snapshot.getLong("correctAnswers") ==
                    local.getInt("correctAnswers").toLong() &&

                    snapshot.getLong("totalQuestions") ==
                    local.getInt("totalQuestions").toLong() &&

                    snapshot.get("answers") ==
                    jsonToFirestoreMap(local)["answers"]

        } catch (exception: Exception) {

            Log.e(
                TAG,
                "Ошибка сравнения результатов",
                exception
            )

            false
        }
    }

    // Преобразуем локальный JSON в данные Firestore
    private fun jsonToFirestoreMap(
        json: JSONObject
    ): HashMap<String, Any> {

        val answersJson =
            json.getJSONArray("answers")

        val answers =
            mutableListOf<Map<String, Any>>()

        for (i in 0 until answersJson.length()) {

            val answerJson =
                answersJson.getJSONObject(i)

            val answer =
                mutableMapOf<String, Any>()

            val keys = answerJson.keys()

            while (keys.hasNext()) {

                val key = keys.next()
                val value = answerJson.get(key)

                answer[key] = convertJsonValue(value)
            }

            answers.add(answer)
        }

        return hashMapOf(
            "studentName" to json.getString("studentName"),
            "studentGroup" to json.getString("studentGroup"),
            "testNumber" to json.getInt("testNumber"),
            "correctAnswers" to json.getInt("correctAnswers"),
            "totalQuestions" to json.getInt("totalQuestions"),
            "completedAt" to FieldValue.serverTimestamp(),
            "answers" to answers,
            "userId" to json.getString("userId")
        )
    }

    // Преобразуем вложенные JSON-массивы и объекты
    private fun convertJsonValue(value: Any): Any {

        return when (value) {

            is JSONArray -> {

                List(value.length()) { index ->
                    convertJsonValue(value.get(index))
                }
            }

            is JSONObject -> {

                val result =
                    mutableMapOf<String, Any>()

                val keys = value.keys()

                while (keys.hasNext()) {

                    val key = keys.next()

                    result[key] =
                        convertJsonValue(value.get(key))
                }

                result
            }

            JSONObject.NULL -> ""

            is Int -> value.toLong()

            else -> value
        }
    }
}
