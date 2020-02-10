package ru.cashbox.android.model.db

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.cashbox.android.model.User
import ru.cashbox.android.model.db.dao.DaoUser

@Database(entities = [User::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun daoUser(): DaoUser
}