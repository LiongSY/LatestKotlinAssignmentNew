package com.example.assignment.UserManagement
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class EmailDBHelper(context: Context) : SQLiteOpenHelper(context, "EmailTypeData", null, 2) {
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(
            "CREATE TABLE EmailType (id INTEGER PRIMARY KEY AUTOINCREMENT, schoolName TEXT, typeName TEXT)"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS EmailType")
        onCreate(db)
    }

    fun insertEmailType(schoolName: String, typeName: String): Long {
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put("schoolName", schoolName)
        cv.put("typeName", typeName)
        return db.insert("EmailType", null, cv)
    }

    fun getAllEmailTypes(): Cursor? {
        val db = this.readableDatabase
        return db.rawQuery("SELECT id AS _id, schoolName, typeName FROM EmailType", null)
    }

    fun updateEmailType(id: Long, schoolName: String, typeName: String): Int {
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put("schoolName", schoolName)
        cv.put("typeName", typeName)
        return db.update("EmailType", cv, "id=?", arrayOf(id.toString()))
    }

    fun deleteEmailType(id: Long): Int {
        val db = this.writableDatabase
        return db.delete("EmailType", "id=?", arrayOf(id.toString()))
    }
    fun updateEmailType(emailType: EmailType): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("schoolName", emailType.schoolName)
            put("typeName", emailType.typeName)
        }

        return db.update(
            "EmailType",  // Use the correct table name here ("EmailType" instead of "email_types")
            values,
            "id = ?",
            arrayOf(emailType.id.toString())
        )
    }


    fun getEmailTypeByEmail(email: String): String? {
        val db = this.readableDatabase
        val query = "SELECT typeName FROM EmailType WHERE schoolName = ?"
        val selectionArgs = arrayOf(email.substringAfter("@"))
        val cursor = db.rawQuery(query, selectionArgs)

        var emailType: String? = null
        if (cursor.moveToFirst()) {
            emailType = cursor.getString(cursor.getColumnIndex("typeName"))
        }

        cursor.close()
        return emailType
    }


    fun searchEmailTypes(keyword: String): Cursor? {
        val db = this.readableDatabase
        val query = "SELECT id AS _id, schoolName, typeName FROM EmailType WHERE typeName LIKE ?"
        val selectionArgs = arrayOf("%$keyword%")
        return db.rawQuery(query, selectionArgs)
    }
}
