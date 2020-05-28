package gleb.kalinin.myreminder.model.dataBase

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import gleb.kalinin.myreminder.model.dataBase.*
import gleb.kalinin.myreminder.model.dto.ToDo
import gleb.kalinin.myreminder.model.dto.ToDoItem

class DBHandler (val context: Context) : SQLiteOpenHelper (context, DB_NAME, null, DB_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        val createToDoTable = "CREATE TABLE $TABLE_TODO (" +
                "$COL_ID integer PRIMARY KEY AUTOINCREMENT," +
                "$COL_CREATED_AT datetime DEFAULT CURRENT_TIMESTAMP," +
                "$COL_NAME varchar);"
        val createToDoItemTable =
                "CREATE TABLE $TABLE_TODO_ITEM (" +
                "$COL_ID integer PRIMARY KEY AUTOINCREMENT," +
                "$COL_CREATED_AT datetime DEFAULT CURRENT_TIMESTAMP," +
                "$COL_TODO_ID integer," +
                "$COL_ITEM_NAME varchar," +
                "$COL_IS_COMPLETED integer);"
        // execute DB
        db.execSQL(createToDoTable)
        db.execSQL(createToDoItemTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

    }


    fun addToDo (toDo: ToDo) : Boolean {
        val db = writableDatabase
        // cv = ContentValues
        val cv = ContentValues()
        cv.put(COL_NAME, toDo.name)
        val result = db.insert(TABLE_TODO, null, cv)
        // If result doesn't equals -1 => True. У нас в (dto.doTo) -> Таблица SQL начинается с Id(long) -1.
        return result != (-1).toLong()
    }

    fun getToDos() : MutableList<ToDo> {
        // Storing tоDo items
        val result: MutableList<ToDo> = ArrayList()
        // read the data
        val db = readableDatabase
        // getting all the records from TABLE_TODO
        val queryResult = db.rawQuery("SELECT * FROM $TABLE_TODO", null)
        // Если сработает -> У нас есть какие-то данные в таблице.
        if(queryResult.moveToFirst()) {
            do{
                // Storing the data inside todo
                val todo = ToDo()
                todo.id = queryResult.getLong(queryResult.getColumnIndex(COL_ID))
                todo.name = queryResult.getString(queryResult.getColumnIndex(COL_NAME))
                result.add(todo)

            } while (queryResult.moveToNext())
        }
        queryResult.close()
        return result
    }

    fun addToDoItem (item : ToDoItem) : Boolean {
        val db = writableDatabase
        val cv = ContentValues()
        cv.put(COL_ITEM_NAME, item.itemName)
        cv.put(COL_TODO_ID, item.toDoId)
        if (item.isCompleted)
            cv.put(COL_IS_COMPLETED, true) // В SQLite ( true = 1, false = 0)
        else
            cv.put(COL_IS_COMPLETED, false)

        val result = db.insert(TABLE_TODO_ITEM, null, cv)
        return result != (-1).toLong()
    }

    fun updateToDoItem (item : ToDoItem) {
        val db = writableDatabase
        val cv = ContentValues()
        cv.put(COL_ITEM_NAME, item.itemName)
        cv.put(COL_TODO_ID, item.toDoId)
        if (item.isCompleted)
            cv.put(COL_IS_COMPLETED, true) // В SQLite ( true = 1, false = 0)
        else
            cv.put(COL_IS_COMPLETED, false)

        db.update(TABLE_TODO_ITEM, cv, "$COL_ID=?" , arrayOf(item.id.toString()))
    }

    fun getToDoItem (todoId: Long) : MutableList<ToDoItem> {
        val result : MutableList<ToDoItem> = ArrayList()

        val db = readableDatabase
        val queryResult = db.rawQuery("SELECT * FROM $TABLE_TODO_ITEM WHERE $COL_TODO_ID=$todoId",null)

        if (queryResult.moveToFirst()) {
            do {
                val item = ToDoItem()
                item.id = queryResult.getLong(queryResult.getColumnIndex(COL_ID))
                item.toDoId = queryResult.getLong(queryResult.getColumnIndex(COL_TODO_ID))
                item.itemName = queryResult.getString(queryResult.getColumnIndex(COL_ITEM_NAME))
                item.isCompleted = queryResult.getInt(queryResult.getColumnIndex(COL_IS_COMPLETED)) == 1 // true while 1
                item.toDoId = todoId
                result.add(item)
            } while (queryResult.moveToNext())
        }

        queryResult.close()
        return result
    }
}