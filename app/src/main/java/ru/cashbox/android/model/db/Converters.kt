package ru.cashbox.android.model.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.cashbox.android.model.Buy
import ru.cashbox.android.model.BuyRecord
import ru.cashbox.android.model.Date

class ConverterListStr {

    @TypeConverter
    fun fromString(content: String?): List<String> {
        return if (content?.isNotEmpty() == true) content.split(";") else ArrayList()
    }

    @TypeConverter
    fun listToString(list: List<String>?): String {
        return list?.joinToString(";") ?: ""
    }
}

class ConverterDate {

    @TypeConverter
    fun stringToDate(content: String?): Date {
        return if (content?.isNotEmpty() == true) Date(time = content.toLong()) else Date()
    }

    @TypeConverter
    fun dateToString(date: Date?): String {
        return date?.time?.toString() ?: ""
    }
}

class BuyListConverter {

    @TypeConverter
    fun fromBuyList(value: List<Buy>): String {
        val gson = Gson()
        val type = object : TypeToken<List<Buy>>() {}.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toBuyList(value: String): List<Buy> {
        val gson = Gson()
        val type = object : TypeToken<List<Buy>>() {}.type
        return gson.fromJson(value, type)
    }
}

class BuyRecordListConverter {

    @TypeConverter
    fun fromBuyRecordList(value: List<BuyRecord>): String {
        val gson = Gson()
        val type = object : TypeToken<List<BuyRecord>>() {}.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toBuyRecordList(value: String): List<BuyRecord> {
        val gson = Gson()
        val type = object : TypeToken<List<BuyRecord>>() {}.type
        return gson.fromJson(value, type)
    }
}