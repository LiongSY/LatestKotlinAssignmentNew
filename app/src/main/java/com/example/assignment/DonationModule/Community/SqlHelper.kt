package com.example.assignment.DonationModule.Community

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class SqlHelper (context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, 2) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(TABLE_QUESTIONS)
        db.execSQL(TABLE_COMMENTS)
        db.execSQL(TABLE_VERIFIEDCOMMENTS)

    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS questions")
        db.execSQL("DROP TABLE IF EXISTS comments")
        db.execSQL("DROP TABLE IF EXISTS verifiedComments")
        onCreate(db)
    }

    fun insertQuestion(questionData: QuestionEntity, wifiConnection:Boolean): Boolean {
        Log.d("Try","Success")
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put("questionId", questionData.questionId)
        cv.put("questionTitle", questionData.questTitle)
        cv.put("email", questionData.email)
        cv.put("questions", questionData.question)
        cv.put("questImage", questionData.questImage)
        if(wifiConnection){
            cv.put("syncStatus", "Done")
        }else{
            cv.put("syncStatus", "pending")
        }
        val result = db.insert("questions", null, cv)
        Log.d("Try2","{$result}")
        return result != -1L
    }

    fun retrieveQuestions():ArrayList<QuestionEntity>{
        val questionList: ArrayList<QuestionEntity> = ArrayList()
        val selectQuery = "select * from questions"
        Log.d("Try2",selectQuery)
        val db = this.readableDatabase

        val cursor: Cursor?

        try{
            cursor = db.rawQuery(selectQuery,null)
        }catch (e:Exception){
            e.printStackTrace()
            db.execSQL(selectQuery)
            return ArrayList()
        }
        var id: String
        var title: String
        var question: String
        var questImage: String
        var email: String

        if(cursor.moveToFirst()){
            do{
                id = cursor.getString(cursor.getColumnIndexOrThrow("questionId"))
                title = cursor.getString(cursor.getColumnIndexOrThrow("questionTitle"))
                question = cursor.getString(cursor.getColumnIndexOrThrow("questions"))
                questImage = cursor.getString(cursor.getColumnIndexOrThrow("questImage"))
                email = cursor.getString(cursor.getColumnIndexOrThrow("email"))
                val quest = QuestionEntity(questionId = id, questTitle = title, email = email, question = question, questImage = questImage)
                questionList.add(quest)

            }while (cursor.moveToNext())
        }
        cursor.close()
        return questionList
    }

    fun updateSyncStatus(questionId:String,status:String): Int {
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put("syncStatus", status)
        // Define the WHERE clause to update a specific question by ID
        val whereClause = "questionId = ?"
        val whereArgs = arrayOf(questionId)

        // Perform the update and return the number of rows affected
        val rowsAffected = db.update("questions", cv, whereClause, whereArgs)
        Log.d("Update row affected",rowsAffected.toString())
        db.close()
        return rowsAffected
    }

    fun updateQuestion(updatedQuestions: QuestionEntity, wifiConnection: Boolean): Int {
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put("questionTitle", updatedQuestions.questTitle)
        cv.put("questions",updatedQuestions.question)
        cv.put("questImage", updatedQuestions.questImage)
        if(wifiConnection){
            cv.put("syncStatus", "Done")
        }else{
            cv.put("syncStatus", "pending")
        }
        // Define the WHERE clause to update a specific question by ID
        val whereClause = "questionId = ?"
        val whereArgs = arrayOf(updatedQuestions.questionId)

        // Perform the update and return the number of rows affected
        val rowsAffected = db.update("questions", cv, whereClause, whereArgs)
        Log.d("Update row affected",rowsAffected.toString())
        db.close()
        return rowsAffected
    }



    fun retrieveOwnQuestions(email:String):ArrayList<QuestionEntity>{
        val questionList: ArrayList<QuestionEntity> = ArrayList()
        val selectQuery = "select * from questions where email = ?"
        Log.d("Try2",selectQuery)
        val db = this.readableDatabase

        val cursor: Cursor?

        try{
            cursor = db.rawQuery(selectQuery, arrayOf(email))
        }catch (e:Exception){
            e.printStackTrace()
            db.execSQL(selectQuery)
            return ArrayList()
        }
        var id: String
        var title: String
        var question: String
        var questImage: String
        var email: String

        if(cursor.moveToFirst()){
            do{
                id = cursor.getString(cursor.getColumnIndexOrThrow("questionId"))
                title = cursor.getString(cursor.getColumnIndexOrThrow("questionTitle"))
                question = cursor.getString(cursor.getColumnIndexOrThrow("questions"))
                questImage = cursor.getString(cursor.getColumnIndexOrThrow("questImage"))
                email = cursor.getString(cursor.getColumnIndexOrThrow("email"))
                val quest = QuestionEntity(questionId = id, questTitle = title, email = email, question = question, questImage = questImage)
                questionList.add(quest)

            }while (cursor.moveToNext())
        }
        cursor.close()
        return questionList
    }

    fun retrieveQuestion(questId:String): QuestionEntity {
        val selectedQuestion : QuestionEntity
        val selectQuery = "select * from questions where questionId = ?"
        Log.d("Try2",selectQuery)
        val db = this.readableDatabase

        val cursor: Cursor?
        cursor = db.rawQuery(selectQuery, arrayOf(questId.toString()))


        try {
            if (cursor.moveToFirst()) {
                selectedQuestion = QuestionEntity(
                    questionId = cursor.getString(cursor.getColumnIndexOrThrow("questionId")),
                    questTitle = cursor.getString(cursor.getColumnIndexOrThrow("questionTitle")),
                    question = cursor.getString(cursor.getColumnIndexOrThrow("questions")),
                    questImage = cursor.getString(cursor.getColumnIndexOrThrow("questImage")),
                    email = cursor.getString(cursor.getColumnIndexOrThrow("email")),
                )
            }else{
                throw NoSuchElementException("Question not found for questId: $questId")
            }
        }finally {
            cursor.close()
        }


        return selectedQuestion
    }

    fun deleteQuestion(questionID:String):Boolean{
        val db = this.writableDatabase

        val tableName = "questions"
        val whereClause = "questionId = ?"
        val whereArgs = arrayOf(questionID)

        val rowsDeleted = db.delete(tableName, whereClause, whereArgs)

        db.close()
        return rowsDeleted > 0
    }

    fun getPendingSyncData(): ArrayList<QuestionEntity>{
        val questionList: ArrayList<QuestionEntity> = ArrayList()
        val selectQuery = "select * from questions where syncStatus = ?"
        Log.d("Try2",selectQuery)
        val db = this.readableDatabase

        val cursor: Cursor?

        try{
            cursor = db.rawQuery(selectQuery, arrayOf("pending"))
        }catch (e:Exception){
            e.printStackTrace()
            db.execSQL(selectQuery)
            return ArrayList()
        }
        var id: String
        var title: String
        var question: String
        var questImage: String
        var email: String
        var syncStatus : String

        if(cursor.moveToFirst()){
            do{
                id = cursor.getString(cursor.getColumnIndexOrThrow("questionId"))
                title = cursor.getString(cursor.getColumnIndexOrThrow("questionTitle"))
                question = cursor.getString(cursor.getColumnIndexOrThrow("questions"))
                questImage = cursor.getString(cursor.getColumnIndexOrThrow("questImage"))
                email = cursor.getString(cursor.getColumnIndexOrThrow("email"))
                syncStatus = cursor.getString(cursor.getColumnIndexOrThrow("syncStatus"))
                val quest = QuestionEntity(questionId = id, questTitle = title, email = email, question = question, questImage = questImage,syncStatus=syncStatus)
                questionList.add(quest)

            }while (cursor.moveToNext())
        }
        cursor.close()
        Log.d("QuestionsList Array SQL","$questionList")
        return questionList
    }


    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "Education.db"


        // Define CREATE TABLE statements for each table
        const val TABLE_QUESTIONS = "CREATE TABLE questions (" +
                "questionId TEXT," +
                "questionTitle TEXT,"+
                "email TEXT,"+
                "questions TEXT," +
                "questImage TEXT," +
                "syncStatus TEXT"+
                ")"

        const val TABLE_COMMENTS = "CREATE TABLE comments (" +
                "commentsId INTEGER PRIMARY KEY AUTOINCREMENT," +
                "questionId INTEGER," +
                "commenterName TEXT," +
                "comments TEXT," +
                "email TEXT" +
                ")"

        const val TABLE_VERIFIEDCOMMENTS = "CREATE TABLE verifiedComments (" +
                "verifiedCommentsId INTEGER PRIMARY KEY AUTOINCREMENT," +
                "commentId INTEGER," +
                "isVerified INTEGER" +
                ")"
    }
}