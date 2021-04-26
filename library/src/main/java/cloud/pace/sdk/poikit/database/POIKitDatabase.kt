package cloud.pace.sdk.poikit.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import cloud.pace.sdk.poikit.poi.GasStation
import cloud.pace.sdk.poikit.poi.Geometry
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

@Database(entities = [GasStation::class], version = 6)
@TypeConverters(Converters::class)
abstract class POIKitDatabase : RoomDatabase() {
    abstract fun gasStationDao(): GasStationDAO

    companion object {
        const val DATABASE_NAME = "poikit_database"

        val migration1to2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE GasStation ADD COLUMN latitude REAL;")
                database.execSQL("ALTER TABLE GasStation ADD COLUMN longitude REAL;")

                val cursor = database.query("SELECT * FROM GasStation")
                val typeGeometry = object : TypeToken<ArrayList<Geometry.CommandGeo>>() {}.type
                val gson = Gson()

                while (cursor.moveToNext()) {
                    val index = cursor.getColumnIndex("geometry")
                    val geometryString = cursor.getString(index)

                    // Extract position
                    val geometry: ArrayList<Geometry.CommandGeo> = gson.fromJson(geometryString, typeGeometry)
                    var lat = 0.0
                    var lon = 0.0

                    geometry.forEach {
                        lat += it.locationPoint.lat
                        lon += it.locationPoint.lon
                    }

                    lat /= geometry.size
                    lon /= geometry.size

                    val id = cursor.getString(cursor.getColumnIndex("id"))
                    database.execSQL("UPDATE GasStation SET latitude = $lat, longitude = $lon WHERE id = '$id'")
                }
            }
        }

        val migration2to3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE GasStation ADD COLUMN priceFormat TEXT;")
            }
        }

        val migration3to4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE GasStation ADD COLUMN brand TEXT;")
            }
        }

        val migration4to5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE GasStation ADD COLUMN priceComparisonOptOut INTEGER")
            }
        }

        val migration5to6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE GasStation ADD COLUMN cofuPaymentMethods TEXT NOT NULL DEFAULT ''")
            }
        }
    }
}
