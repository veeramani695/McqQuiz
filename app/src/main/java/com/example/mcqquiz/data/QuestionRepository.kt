package com.example.mcqquiz.data

import android.content.Context
import com.example.mcqquiz.model.Question
import org.json.JSONArray
import java.io.IOException

class QuestionRepository(private val context: Context) {

    fun loadQuestionsFromAssets(): List<Question> {
        val jsonString: String
        try {
            jsonString = context.assets.open("quiz_questions.json").bufferedReader().use { it.readText() }
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            return emptyList()
        }

        val questions = mutableListOf<Question>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val id = jsonObject.getInt("id")
                val questionText = jsonObject.getString("questionText")
                
                val jsonOptions = jsonObject.getJSONArray("options")
                val options = mutableListOf<String>()
                for (j in 0 until jsonOptions.length()) {
                    options.add(jsonOptions.getString(j))
                }
                
                val correctAnswer = jsonObject.getString("correctAnswer")
                
                questions.add(Question(id, questionText, options, correctAnswer))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return questions
    }
}
