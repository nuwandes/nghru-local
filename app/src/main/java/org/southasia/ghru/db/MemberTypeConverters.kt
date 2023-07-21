package org.southasia.ghru.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.southasia.ghru.vo.Member
import java.util.*


object MemberTypeConverters {
    var gson = Gson()
    @TypeConverter
    @JvmStatic
    fun stringToIntList(data: String?): List<Member>? {
        if (data == null) {
            return Collections.emptyList()
        }

        val listType = object : TypeToken<List<Member>>() {

        }.getType()

        return gson.fromJson(data, listType)
    }

    @TypeConverter
    @JvmStatic
    fun intListToString(ints: List<Member>?): String? {
        return gson.toJson(ints);
    }
}