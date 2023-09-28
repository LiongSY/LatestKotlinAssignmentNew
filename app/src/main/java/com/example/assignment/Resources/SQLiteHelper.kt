package com.example.assignment.Resources

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.lang.Exception


//CRUD (Create,Read,Update,Delete)

class SQLiteHelper (context:Context):SQLiteOpenHelper(context, DATABASE_NAME,null,DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE_RESOURCES)


    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS resources")

        onCreate(db)
    }

    //Create
    fun insertResources(resource: ResourceModel): Boolean {

        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put("resourcesID", resource.name)
        contentValues.put("name", resource.name)
        contentValues.put("email", resource.email)
        contentValues.put("resourceTitle", resource.resourceTitle)
        contentValues.put("resourceDocument", resource.resourceDocument)
        contentValues.put("resourceDesc", resource.resourceDesc)
        val result = db.insert("resources", null, contentValues)
        Log.d("Result insert", result.toString())
        return result != -1L
    }


    //Retrieve
    fun getResources(): ArrayList<ResourceModel> {
        val resourcesList: ArrayList<ResourceModel> = ArrayList()
        val selectQuery = "select * from resources"
        val db = this.readableDatabase

        val cursor: Cursor

        try {
            cursor = db.rawQuery(selectQuery, null)
        } catch (e: Exception) {
            e.printStackTrace()
            db.execSQL(selectQuery)
            return ArrayList()
        }
        var resourceId: String
        var name: String
        var email: String
        var resourceTitle: String
        var resourceDocument: String
        var resourceDesc: String

        if (cursor.moveToFirst()) {
            do {
                resourceId = cursor.getString(cursor.getColumnIndexOrThrow("resourcesID"))
                name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                email = cursor.getString(cursor.getColumnIndexOrThrow("email"))
                resourceTitle = cursor.getString(cursor.getColumnIndexOrThrow("resourceTitle"))
                resourceDocument =
                    cursor.getString(cursor.getColumnIndexOrThrow("resourceDocument"))
                resourceDesc = cursor.getString(cursor.getColumnIndexOrThrow("resourceDesc"))
                val resources = ResourceModel(
                    resourceId = resourceId,
                    name = name,
                    email = email,
                    resourceTitle = resourceTitle,
                    resourceDocument = resourceDocument,
                    resourceDesc = resourceDesc
                )
                resourcesList.add(resources)
                Log.d("PDF name", resourceDocument.toString())
            } while (cursor.moveToNext())
        }
        cursor.close()
        Log.d("Size", resourcesList.size.toString())
        return resourcesList
    }


    fun getResource(resourceId: String): ResourceModel {
        val resource: ResourceModel
        val selectQuery = "select * from resources where resourcesID = ?"
        Log.d("Try2", selectQuery)
        val db = this.readableDatabase

        val cursor: Cursor?
        cursor = db.rawQuery(selectQuery, arrayOf(resourceId))


        try {
            if (cursor.moveToFirst()) {
                resource = ResourceModel(
                    resourceId = cursor.getString(cursor.getColumnIndexOrThrow("resourcesID")),
                    name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    email = cursor.getString(cursor.getColumnIndexOrThrow("email")),
                    resourceTitle = cursor.getString(cursor.getColumnIndexOrThrow("resourceTitle")),
                    resourceDocument = cursor.getString(cursor.getColumnIndexOrThrow("resourceDocument")),
                    resourceDesc = cursor.getString(cursor.getColumnIndexOrThrow("resourceDesc"))
                )
            } else {
                throw NoSuchElementException("Resource not found")
            }
        } finally {
            cursor.close()
        }
        Log.d("Try2", resource.resourceTitle)
        return resource
    }

    fun educatorOwnResources(email: String): ArrayList<ResourceModel> {
        val resourcesList: ArrayList<ResourceModel> = ArrayList()
        val selectQuery = "select * from resources where email = ?"
        val db = this.readableDatabase

        val cursor: Cursor?

        try {
            cursor = db.rawQuery(selectQuery, arrayOf(email))
        } catch (e: Exception) {
            e.printStackTrace()
            db.execSQL(selectQuery)
            return ArrayList()
        }
        var resourceId: String
        var name: String
        var email: String
        var resourceTitle: String
        var resourceDocument: String
        var resourceDesc: String

        if (cursor.moveToFirst()) {
            do {
                resourceId = cursor.getString(cursor.getColumnIndexOrThrow("resourcesID"))
                name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                email = cursor.getString(cursor.getColumnIndexOrThrow("email"))
                resourceTitle = cursor.getString(cursor.getColumnIndexOrThrow("resourceTitle"))
                resourceDocument =
                    cursor.getString(cursor.getColumnIndexOrThrow("resourceDocument"))
                resourceDesc = cursor.getString(cursor.getColumnIndexOrThrow("resourceDesc"))
                val resources = ResourceModel(
                    resourceId = resourceId,
                    name = name,
                    email = email,
                    resourceTitle = resourceTitle,
                    resourceDocument = resourceDocument,
                    resourceDesc = resourceDesc
                )
                resourcesList.add(resources)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return resourcesList
    }

    companion object {
        const val DATABASE_VERSION = 2
        const val DATABASE_NAME = "Education.db"

        const val CREATE_TABLE_RESOURCES = "CREATE TABLE resources (" +
                "resourcesID TEXT," +
                "name TEXT," +
                "email TEXT," +
                "resourceTitle TEXT," +
                "resourceDocument TEXT," +
                "resourceDesc TEXT" +
                ")"

    }


    //Update
    fun updateResource(resource: ResourceModel): Boolean {

        val db = this.writableDatabase
        val contentValues = ContentValues()

        contentValues.put("name", resource.name)
        contentValues.put("email", resource.email)
        contentValues.put("resourceTitle", resource.resourceTitle)
        contentValues.put("resourceDocument", resource.resourceDocument)
        contentValues.put("resourceDesc", resource.resourceDesc)

        val whereClause = "resourcesID = ?"
        val whereArgs = arrayOf(resource.resourceId)

        val rowsUpdated = db.update("resources", contentValues, whereClause, whereArgs)
        return rowsUpdated > 0
    }


    // Delete
    fun deleteResource(resourceId: String): Boolean {
        val db = this.writableDatabase

        val whereClause = "resourcesID = ?"
        val whereArgs = arrayOf(resourceId)

        val rowDeleted = db.delete("resources", whereClause, whereArgs)
        Log.d("delete local", rowDeleted.toString())
        return rowDeleted > 0
    }


    //Search
    /*    fun searchResources(query: String): ArrayList<ResourceModel> {
            val resourcesList: ArrayList<ResourceModel> = ArrayList()
            val db = this.readableDatabase
            val selectQuery =
              "SELECT * FROM resources WHERE resourceTitle LIKE ? OR resourceDesc LIKE ?"
            val args = arrayOf("%query%", "%query%")

            val cursor: Cursor = db.rawQuery(selectQuery,args)

            try{
                if(cursor.moveToFirst()){
                    do{

                    }
                }
            }
        }*/


    //Email derrr
//    fun geResourcesCountForEmail(email: String): Int {
//        val db = this.readableDatabase
//        val query = "SELECT COUNT (*) FROM resources WHERE email = ?"
//        val cursor = db.rawQuery(query, arrayOf(email))
//        var count = 0
//        if (cursor != null) {
//            if (cursor.moveToFirst()) {
//                count = cursor.getInt(0)
//            }
//            cursor.close()
//        }
//        return count
//    }
}