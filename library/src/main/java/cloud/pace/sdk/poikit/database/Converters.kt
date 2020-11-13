package cloud.pace.sdk.poikit.database

import androidx.room.TypeConverter
import cloud.pace.sdk.poikit.poi.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

class Converters {
    private val gson = Gson()

    /**
     * PaymentMethods
     */
    private val typePaymentMethods = object : TypeToken<MutableList<PaymentMethod>>() {}.type

    @TypeConverter
    fun fromPaymentMethods(list: MutableList<PaymentMethod>): String {
        return gson.toJson(list, typePaymentMethods)
    }

    @TypeConverter
    fun toPaymentMethods(json: String): MutableList<PaymentMethod> {
        return gson.fromJson(json, typePaymentMethods)
    }

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
        return gson.fromJson(json, typeOpeningHours)
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
        return gson.fromJson(priceString, typePriceList)
    }

    // TODO: check if needed
    /**
     * PoiType
     */
    /*@TypeConverter
    fun fromPoiType(poiType: PoiType): String {
        return poiType.toString()
    }

    @TypeConverter
    fun toPoiType(poiTypeString: String): PoiType {
        return PoiType.valueOf(poiTypeString)
    }*/

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
        return gson.fromJson(geometryString, typeGeometry)
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
     * Amenity
     */
    private val typeAmenity = object : TypeToken<MutableList<Amenity>>() {}.type

    @TypeConverter
    fun fromAmenity(list: MutableList<Amenity>): String {
        return gson.toJson(list, typeAmenity)
    }

    @TypeConverter
    fun toAmenity(json: String): MutableList<Amenity> {
        return gson.fromJson(json, typeAmenity)
    }

    /**
     * LoyaltyProgram
     */
    private val typeLoyaltyProgram = object : TypeToken<MutableList<LoyaltyProgram>>() {}.type

    @TypeConverter
    fun fromLoyaltyProgram(list: MutableList<LoyaltyProgram>): String {
        return gson.toJson(list, typeLoyaltyProgram)
    }

    @TypeConverter
    fun toLoyaltyProgram(json: String): MutableList<LoyaltyProgram> {
        return gson.fromJson(json, typeLoyaltyProgram)
    }

    /**
     * PostalService
     */
    private val typePostalService = object : TypeToken<MutableList<PostalService>>() {}.type

    @TypeConverter
    fun fromPostalService(list: MutableList<PostalService>): String {
        return gson.toJson(list, typePostalService)
    }

    @TypeConverter
    fun toPostalService(json: String): MutableList<PostalService> {
        return gson.fromJson(json, typePostalService)
    }

    /**
     * Service
     */
    private val typeService = object : TypeToken<MutableList<Service>>() {}.type

    @TypeConverter
    fun fromService(list: MutableList<Service>): String {
        return gson.toJson(list, typeService)
    }

    @TypeConverter
    fun toService(json: String): MutableList<Service> {
        return gson.fromJson(json, typeService)
    }

    /**
     * ShopGood
     */
    private val typeShopGood = object : TypeToken<MutableList<ShopGood>>() {}.type

    @TypeConverter
    fun fromShopGood(list: MutableList<ShopGood>): String {
        return gson.toJson(list, typeShopGood)
    }

    @TypeConverter
    fun toShopGood(json: String): MutableList<ShopGood> {
        return gson.fromJson(json, typeShopGood)
    }

    /**
     * Food
     */
    private val typeFood = object : TypeToken<MutableList<Food>>() {}.type

    @TypeConverter
    fun fromFood(list: MutableList<Food>): String {
        return gson.toJson(list, typeFood)
    }

    @TypeConverter
    fun toFood(json: String): MutableList<Food> {
        return gson.fromJson(json, typeFood)
    }
}
