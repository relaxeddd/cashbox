package ru.cashbox.android.model.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.cashbox.android.common.CASHSESSIONS
import ru.cashbox.android.model.Cashsession

@Dao
interface DaoCashsession {

    companion object {

        private const val COLLECTION = CASHSESSIONS
    }

    @Query("SELECT * FROM $COLLECTION WHERE innerId LIKE :innerId LIMIT 1")
    fun getCashsession(innerId: String): LiveData<Cashsession?>

    @Query("SELECT * FROM $COLLECTION WHERE innerId LIKE :innerId LIMIT 1")
    fun getCashsessionById(innerId: String): Cashsession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCashsession(item: Cashsession)

    @Query("DELETE FROM $COLLECTION WHERE innerId = :innerId")
    fun deleteCashsession(innerId: String)

    @Query("SELECT * FROM $COLLECTION")
    fun getAll(): List<Cashsession>
}