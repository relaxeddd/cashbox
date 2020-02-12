package ru.cashbox.android.model.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.cashbox.android.common.SESSIONS
import ru.cashbox.android.model.Session

@Dao
interface DaoSession {

    companion object {

        private const val COLLECTION = SESSIONS
    }

    /*@Query("SELECT * FROM $COLLECTION WHERE token LIKE :token LIMIT 1")
    fun getSession(token: String): LiveData<Session?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSession(item: Session)

    @Query("DELETE FROM $COLLECTION WHERE token = :token")
    fun deleteSession(token: String)*/

    @Query("SELECT * FROM $COLLECTION WHERE innerId LIKE :innerId LIMIT 1")
    fun getSession(innerId: String): LiveData<Session?>

    @Query("SELECT * FROM $COLLECTION WHERE innerId LIKE :innerId LIMIT 1")
    fun getSessionById(innerId: String): Session?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSession(item: Session)

    @Query("DELETE FROM $COLLECTION WHERE innerId = :innerId")
    fun deleteSession(innerId: String)

    @Query("SELECT * FROM $COLLECTION")
    fun getAll(): List<Session>
}