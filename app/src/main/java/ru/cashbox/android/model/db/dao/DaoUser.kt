package ru.cashbox.android.model.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.cashbox.android.common.USERS
import ru.cashbox.android.model.User

@Dao
interface DaoUser {

    companion object {

        private const val COLLECTION = USERS
    }

    @Query("SELECT * FROM $COLLECTION WHERE id LIKE :id LIMIT 1")
    fun getLiveDataById(id: Long): LiveData<User>

    @Query("SELECT * FROM $COLLECTION WHERE id LIKE :id LIMIT 1")
    fun getById(id: Long): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(item: User)
}