package com.example.project250304.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ScheduleDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        val SQL_CREATE_ENTRIES =
            "CREATE TABLE $TABLE_NAME (" +
                    "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "$COLUMN_COURSE TEXT NOT NULL," +
                    "$COLUMN_DATE TEXT NOT NULL," +
                    "$COLUMN_START_TIME TEXT NOT NULL," +
                    "$COLUMN_END_TIME TEXT NOT NULL," +
                    "$COLUMN_ISOLATION TEXT NOT NULL)"
        db.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // 在這裡處理資料庫升級，例如：
        // db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        // onCreate(db)
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN new_column TEXT")
        }
    }

    // 插入 Schedule 資料
    fun insertSchedule(schedule: Schedule): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_COURSE, schedule.course)
            put(COLUMN_DATE, schedule.date)
            put(COLUMN_START_TIME, schedule.startTime)
            put(COLUMN_END_TIME, schedule.endTime)
            put(COLUMN_ISOLATION, schedule.isolation)
        }
        return db.insert(TABLE_NAME, null, values)
    }

    // 查詢所有 Schedule 資料
    fun getAllSchedules(): List<Schedule> {
        val db = readableDatabase
        val cursor = db.query(TABLE_NAME, null, null, null, null, null, null)
        val schedules = mutableListOf<Schedule>()
        cursor.use {
            while (it.moveToNext()) {
                schedules.add(cursorToSchedule(it))
            }
        }
        return schedules
    }

    // 更新 Schedule 資料
    fun updateSchedule(schedule: Schedule): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_COURSE, schedule.course)
            put(COLUMN_DATE, schedule.date)
            put(COLUMN_START_TIME, schedule.startTime)
            put(COLUMN_END_TIME, schedule.endTime)
            put(COLUMN_ISOLATION, schedule.isolation)
        }
        return db.update(TABLE_NAME, values, "$COLUMN_ID = ?", arrayOf(schedule.id.toString()))
    }

    // 刪除 Schedule 資料
    fun deleteSchedule(id: Int): Int {
        val db = writableDatabase
        return db.delete(TABLE_NAME, "$COLUMN_ID = ?", arrayOf(id.toString()))
    }

    // 將 Cursor 轉換為 Schedule 類別
    private fun cursorToSchedule(cursor: Cursor): Schedule {
        return Schedule(
            id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
            course = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COURSE)),
            date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)),
            startTime = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_START_TIME)),
            endTime = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_END_TIME)),
            isolation = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ISOLATION))
        )
    }

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "Schedule.db"
        const val TABLE_NAME = "schedules"
        const val COLUMN_ID = "id"
        const val COLUMN_COURSE = "course"
        const val COLUMN_DATE = "date"
        const val COLUMN_START_TIME = "start_time"
        const val COLUMN_END_TIME = "end_time"
        const val COLUMN_ISOLATION = "isolation"
    }
}