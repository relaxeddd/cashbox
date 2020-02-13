package ru.cashbox.android.model.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.cashbox.android.model.Cashsession
import ru.cashbox.android.model.Session
import ru.cashbox.android.model.User
import ru.cashbox.android.model.db.dao.DaoCashsession
import ru.cashbox.android.model.db.dao.DaoSession
import ru.cashbox.android.model.db.dao.DaoUser

@Database(entities = [User::class, Session::class, Cashsession::class], version = 1, exportSchema = false)
@TypeConverters(ConverterListStr::class, ConverterDate::class, BuyListConverter::class, BuyRecordListConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun daoUser(): DaoUser
    abstract fun daoSession(): DaoSession
    abstract fun daoCashsession(): DaoCashsession
}