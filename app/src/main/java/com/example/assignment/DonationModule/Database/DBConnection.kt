package com.example.assignment.DonationModule.Database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBConnection(context: Context) : SQLiteOpenHelper(context, "Education", null, 8) {
    companion object{
        val tableName:String = "DonationDraft"
        val col1:String = "id"
        val col2:String = "name"
        val col3:String = "date"
        val col4:String = "amount"
        val col5:String = "email"
        val col6:String = "method"
        val col7:String = "contact"
    }

    val query:String =
        "create table $tableName($col1 integer primary key autoincrement,$col2 text,$col3 text,$col4 text,$col5 text,$col6 text,$col7 text)"

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(query)
    }

    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {
        db?.execSQL("drop table $tableName")
        db?.execSQL(query)
    }

    fun insert(name:String,date:String,amount:String,email:String,method:String,contact:String):Long{
        val db:SQLiteDatabase =this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(col2, name)
        contentValues.put(col3,date)
        contentValues.put(col4, amount)
        contentValues.put(col5,email)
        contentValues.put(col6, method)
        contentValues.put(col7,contact)
        val result:Long = db.insert(tableName,null,contentValues)
        return result
    }

    fun retrieveDonationDrafts(): Cursor {
        val db: SQLiteDatabase = readableDatabase
        val columns = arrayOf(col1, col2, col3, col4, col5, col6, col7)
        return db.query(tableName, columns, null, null, null, null, null)
    }

    fun retrieveDonationDraftByRecordId(recordId: String): Cursor {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM $tableName WHERE $col1 = ?", arrayOf(recordId))
    }

    fun deleteDonationDraftByRecordId(recordId: String) {
        val db = this.writableDatabase
        db.delete(tableName, "$col1 = ?", arrayOf(recordId))
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
}