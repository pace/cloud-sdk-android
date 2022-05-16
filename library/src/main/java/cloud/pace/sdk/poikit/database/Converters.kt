package cloud.pace.sdk.poikit.database

import androidx.room.TypeConverter
import cloud.pace.sdk.poikit.poi.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

class Converters {
    private val gson = Gson()

    /**
     * OpeningHours
     */
    private val typeOpeningHours = object : TypeToken<List<OpeningHours>>() {}.type

    @TypeConverter
    fun fromOpeningHours(list: List<OpeningHours>): String {
        return gson.toJson(list, typeOpeningHours)
    }

    @TypeConverter
    fun toOpeningHours(json: String): List<OpeningHours> {
        return gson.fromJson(json, typeOpeningHours) ?: mutableListOf()
    }

    /**
     * PriceList
     */
    private val typePriceList = object : TypeToken<List<Price>>() {}.type

    @TypeConverter
    fun fromPriceList(prices: List<Price>): String {
        return gson.toJson(prices, typePriceList)
    }

    @TypeConverter
    fun toPriceList(priceString: String): List<Price> {
        return gson.fromJson(priceString, typePriceList) ?: mutableListOf()
    }

    /**
     * Geometry
     */
    private val typeGeometry = object : TypeToken<ArrayList<Geometry.CommandGeo>>() {}.type

    @TypeConverter
    fun fromGeometry(list: ArrayList<Geometry.CommandGeo>): String {
        return gson.toJson(list, typeGeometry)
    }

    @TypeConverter
    fun toGeometry(geometryString: String): ArrayList<Geometry.CommandGeo> {
        return gson.fromJson(geometryString, typeGeometry) ?: arrayListOf()
    }

    @TypeConverter
    fun fromDate(date: Date): Long {
        return date.time
    }

    @TypeConverter
    fun toDate(timestamp: Long): Date {
        return Date(timestamp)
    }

    /**
     * List<String>
     */
    private val strings = object : TypeToken<MutableList<String>>() {}.type

    @TypeConverter
    fun fromStringList(list: MutableList<String>): String {
        return gson.toJson(list, strings)
    }

    @TypeConverter
    fun toStringList(json: String): MutableList<String> {
        return gson.fromJson(json, strings) ?: mutableListOf()
    }
}
