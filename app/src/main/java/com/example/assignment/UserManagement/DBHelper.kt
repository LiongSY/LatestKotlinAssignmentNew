package com.example.assignment.UserManagement

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Bitmap
import com.example.assignment.DonationModule.Database.DBConnection
import java.io.ByteArrayOutputStream


class DBHelper(context: Context) : SQLiteOpenHelper(context, "Education", null, 8) {

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(
            "CREATE TABLE Userdata (email TEXT PRIMARY KEY, firstName TEXT, lastName TEXT, phoneNum TEXT, password TEXT, confirmPass TEXT, image BLOB, position TEXT)"
        )

        db?.execSQL(
            "CREATE TABLE EmailType (id INTEGER PRIMARY KEY AUTOINCREMENT, schoolName TEXT, typeName TEXT)"
        )
        db?.execSQL("CREATE TABLE DonationDraft (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, date TEXT, amount TEXT, email TEXT, method TEXT, contact TEXT)")

    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db?.execSQL("ALTER TABLE Userdata ADD COLUMN image BLOB")
            db?.execSQL("ALTER TABLE Userdata ADD COLUMN position TEXT")
        }
    }


    fun insert(name:String,date:String,amount:String,email:String,method:String,contact:String):Long{
        val db:SQLiteDatabase =this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put("name", name)
        contentValues.put("date",date)
        contentValues.put("amount", amount)
        contentValues.put("email",email)
        contentValues.put("method", method)
        contentValues.put("contact",contact)
        val result:Long = db.insert("DonationDraft",null,contentValues)
        return result
    }

    fun retrieveDonationDrafts(email: String?): Cursor {
        val db: SQLiteDatabase = readableDatabase
        val columns = arrayOf(
            DBConnection.col1,
            DBConnection.col2,
            DBConnection.col3,
            DBConnection.col4,
            DBConnection.col5,
            DBConnection.col6,
            DBConnection.col7
        )
        val selection = "${DBConnection.col5} = ?" // Use DBConnection.col5 for the "email" column
        val selectionArgs = arrayOf(email) // Pass the email as an argument

        return db.query(DBConnection.tableName, columns, selection, selectionArgs, null, null, null)
    }


    fun retrieveDonationDraftByRecordId(recordId: String): Cursor {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM ${DBConnection.tableName} WHERE ${DBConnection.col1} = ?", arrayOf(recordId))
    }

    fun deleteDonationDraftByRecordId(recordId: String) {
        val db = this.writableDatabase
        db.delete(DBConnection.tableName, "${DBConnection.col1} = ?", arrayOf(recordId))
    }

    fun deleteDonationDraft(id: String?): Int {
        val db = writableDatabase
        val selection = "${DBConnection.col1} = ?"
        val selectionArgs = arrayOf(id)

        val deletedRows = db.delete(DBConnection.tableName, selection, selectionArgs)
        db.close()
        return deletedRows
    }

    fun updateDonationDraft(id: String?, name: String, amount: String, contact: String): Int {
        val db = writableDatabase
        val values = ContentValues()
        values.put(DBConnection.col2, name)
        values.put(DBConnection.col4, amount)
        values.put(DBConnection.col7, contact)

        val selection = "${DBConnection.col1} = ?"
        val selectionArgs = arrayOf(id)

        val updatedRows = db.update(DBConnection.tableName, values, selection, selectionArgs)
        db.close()
        return updatedRows
    }

    fun insertData(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        phoneNum: String,
        confirmPass: String,
        image: Bitmap?,
        position: String // Add the "position" parameter
    ): Boolean {
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put("email", email)
        cv.put("firstName", firstName)
        cv.put("lastName", lastName)
        cv.put("phoneNum", phoneNum)
        cv.put("password", password)
        cv.put("confirmPass", confirmPass)
        cv.put("position", position) // Insert the "position" into the ContentValues

        val imageByteArray = if (image != null) convertBitmapToByteArray(image) else null

        cv.put("image", imageByteArray)
        val result = db.insert("Userdata", null, cv)
        return result != -1L
    }


    fun convertBitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    fun checkUserPassword(email: String, password: String): Boolean {
        val db = this.readableDatabase
        val query = "select * from Userdata where email='$email' and password='$password'"
        val cursor = db.rawQuery(query, null)
        val count = cursor.count
        cursor.close()
        return count > 0
    }
    private fun isValidEmailDomainForTeacher(context: Context, email: String): Boolean {
        val emailDBHelper = EmailDBHelper(context)

        val emailType = emailDBHelper.getEmailTypeByEmail(email)

        return emailType == "teacher"
    }
    fun updatePassword(email: String, newPassword: String, newConfirmPassword: String): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put("password", newPassword)
        contentValues.put("confirmPass", newConfirmPassword)

        // Update the password and confirmpassword for the given email
        val whereClause = "email = ?"
        val whereArgs = arrayOf(email)
        val updateSuccess = db.update("Userdata", contentValues, whereClause, whereArgs) > 0

        db.close()
        return updateSuccess
    }



    fun getUserType(userEmail: String, userPassword: String): String? {
        val db = this.readableDatabase

        try {
            val query = "SELECT position FROM Userdata WHERE email = ? AND password = ?"
            val selectionArgs = arrayOf(userEmail, userPassword)

            val cursor = db.rawQuery(query, selectionArgs)
            if (cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndex("position"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.close()
        }

        return null
    }


    fun deleteUserAccount(userEmail: String): Boolean {
        val db = this.writableDatabase

        try {
            val whereClause = "email = ?"
            val whereArgs = arrayOf(userEmail)
            db.delete("Userdata", whereClause, whereArgs)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        } finally {
            db.close()
        }
    }


    fun retrieveOneRecord(email: String): Cursor? {
        val db = this.readableDatabase
        val query = "SELECT * FROM Userdata WHERE email = ?"
        val selectionArgs = arrayOf(email)

        return db.rawQuery(query, selectionArgs)
    }

    fun isEmailExists(email: String): Boolean {
        val db = this.readableDatabase
        val query = "select * from Userdata where email='$email'"
        val cursor = db.rawQuery(query, null)
        val count = cursor.count
        cursor.close()
        return count > 0
    }

    fun getUserDataByEmailType(emailType: String): List<UserData> {
        val matchingUserDataList = mutableListOf<UserData>()
        val db = this.readableDatabase

        val query = "SELECT * FROM Userdata WHERE emailType = ?"
        val selectionArgs = arrayOf(emailType)
        val cursor = db.rawQuery(query, selectionArgs)

        while (cursor.moveToNext()) {
            val email = cursor.getString(cursor.getColumnIndex("email"))
            val firstName = cursor.getString(cursor.getColumnIndex("firstName"))
            val lastName = cursor.getString(cursor.getColumnIndex("lastName"))
            val phoneNum = cursor.getString(cursor.getColumnIndex("phoneNum"))
            val password = cursor.getString(cursor.getColumnIndex("password"))
            val confirmPass = cursor.getString(cursor.getColumnIndex("confirmPass"))
            val position = cursor.getString(cursor.getColumnIndex("position"))

            val imageByteArray = cursor.getBlob(cursor.getColumnIndex("image"))

            val userData = UserData(email, firstName, lastName, phoneNum, password, confirmPass, imageByteArray, position)
            matchingUserDataList.add(userData)
        }

        cursor.close()

        return matchingUserDataList
    }

    fun updateUserData(
        email: String,
        firstName: String,
        lastName: String,
        phoneNum: String,
        password: String,
        confirmPass: String,
        image: ByteArray?,
        position: String?
    ): Boolean {
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put("firstName", firstName)
        cv.put("lastName", lastName)
        cv.put("phoneNum", phoneNum)
        cv.put("password", password)
        cv.put("confirmPass", confirmPass)
        cv.put("image", image)
        cv.put("position", position) // Set the position field

        val result = db.update("Userdata", cv, "email=?", arrayOf(email))
        return result > 0
    }
    fun retrieveUserData(email: String, password: String): UserData? {
        val db = this.readableDatabase
        val query = "SELECT * FROM Userdata WHERE email = ? AND password = ?"
        val selectionArgs = arrayOf(email, password)
        val cursor = db.rawQuery(query, selectionArgs)

        return cursor.use {
            if (it.moveToFirst()) {
                val firstName = it.getString(it.getColumnIndex("firstName"))
                val lastName = it.getString(it.getColumnIndex("lastName"))
                val phoneNum = it.getString(it.getColumnIndex("phoneNum"))
                val confirmPass = it.getString(it.getColumnIndex("confirmPass"))
                val position = it.getString(it.getColumnIndex("position"))
                val imageByteArray = it.getBlob(it.getColumnIndex("image"))

                UserData(email, firstName, lastName, phoneNum, password, confirmPass, imageByteArray, position)
            } else {
                null
            }
        }
    }


    fun getAllUserAccounts(): List<UserData> {
        val userAccounts = mutableListOf<UserData>()
        val db = this.readableDatabase
        val query = "SELECT * FROM Userdata"
        val cursor = db.rawQuery(query, null)

        cursor.use {
            while (cursor.moveToNext()) {
                val email = cursor.getString(cursor.getColumnIndex("email"))
                val firstName = cursor.getString(cursor.getColumnIndex("firstName"))
                val lastName = cursor.getString(cursor.getColumnIndex("lastName"))
                val phoneNum = cursor.getString(cursor.getColumnIndex("phoneNum"))
                val password = cursor.getString(cursor.getColumnIndex("password"))
                val confirmPass = cursor.getString(cursor.getColumnIndex("confirmPass"))
                val position = cursor.getString(cursor.getColumnIndex("position"))
                val imageByteArray = cursor.getBlob(cursor.getColumnIndex("image"))

                val userData = UserData(email, firstName, lastName, phoneNum, password, confirmPass, imageByteArray, position)
                userAccounts.add(userData)
            }
        }

        return userAccounts
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
    fun getLatestEmailTypeId(): Long {
        val db = readableDatabase
        val query = "SELECT id FROM EmailType ORDER BY id DESC LIMIT 1"
        val cursor = db.rawQuery(query, null)
        val latestId: Long = if (cursor.moveToFirst()) {
            cursor.getLong(cursor.getColumnIndex("id"))
        } else {
            -1
        }
        cursor.close()
        return latestId
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


    fun isTeacher(email: String): Boolean {
        val db = this.readableDatabase
        val query = "SELECT COUNT(*) FROM EmailType WHERE ? LIKE '%' || typeName"
        val selectionArgs = arrayOf(email)

        val cursor = db.rawQuery(query, selectionArgs)

        var isTeacher = false
        if (cursor.moveToFirst()) {
            val count = cursor.getInt(0)
            isTeacher = count > 0
        }

        cursor.close()
        return isTeacher
    }



    fun searchEmailTypes(keyword: String): Cursor? {
        val db = this.readableDatabase
        val query = "SELECT id AS _id, schoolName, typeName FROM EmailType WHERE typeName LIKE ?"
        val selectionArgs = arrayOf("%$keyword%")
        return db.rawQuery(query, selectionArgs)
    }
}
