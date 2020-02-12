package ru.cashbox.android.model.db

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.cashbox.android.model.Session
import ru.cashbox.android.model.User
import ru.cashbox.android.model.db.dao.DaoSession
import ru.cashbox.android.model.db.dao.DaoUser

@Database(entities = [User::class, Session::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun daoUser(): DaoUser
    abstract fun daoSession(): DaoSession
}