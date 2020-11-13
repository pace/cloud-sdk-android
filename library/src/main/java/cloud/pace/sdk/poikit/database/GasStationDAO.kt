package cloud.pace.sdk.poikit.database

import androidx.lifecycle.LiveData
import androidx.room.*
import cloud.pace.sdk.poikit.poi.GasStation

@Dao
interface GasStationDAO {
    @Query("SELECT * FROM gasstation WHERE id=:id")
    fun getGasStation(id: String): GasStation

    @Query("SELECT * FROM gasstation WHERE id IN(:ids)")
    fun getByIds(ids: List<String>): List<GasStation>

    @Query("SELECT * FROM gasstation WHERE id IN(:ids)")
    fun getByIdsLive(ids: List<String>): LiveData<List<GasStation>>

    @Query("SELECT * FROM gasstation")
    fun getAll(): List<GasStation>

    @Query("SELECT * FROM gasstation")
    fun getAllLive(): LiveData<List<GasStation>>

    @Query("SELECT * FROM gasstation WHERE latitude BETWEEN :minLat AND :maxLat AND longitude BETWEEN :minLon AND :maxLon")
    fun getInBoundingBoxLive(minLat: Double, maxLat: Double, minLon: Double, maxLon: Double): LiveData<List<GasStation>>

    @Query("SELECT * FROM gasstation WHERE latitude BETWEEN :minLat AND :maxLat AND longitude BETWEEN :minLon AND :maxLon")
    fun getInBoundingBox(minLat: Double, maxLat: Double, minLon: Double, maxLon: Double): List<GasStation>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertGasStations(gasStations: List<GasStation>): List<Long>

    @Delete
    fun delete(gasStations: List<GasStation>): Int
}
