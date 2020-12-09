package cloud.pace.sdk.poikit.routing

import android.os.Parcelable
import android.text.TextUtils
import cloud.pace.sdk.poikit.poi.LocationPoint
import cloud.pace.sdk.poikit.utils.ApiException
import cloud.pace.sdk.utils.*
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * API to handle navigation requests, calling backend on OSRM server.
 */

internal class NavigationApiClient(environment: Environment, apiKey: String) {
    private val api = NavigationApi.create(environment.routingBaseUrl, apiKey)

    fun getRoute(request: NavigationRequest, completion: (Completion<Route?>) -> Unit) {
        val coordPairs = request.coordinates.map { point -> point.lon.toString() + "," + point.lat.toString() }
        var annotations = request.annotations.joinToString(",", transform = { it.apiName })
        if (TextUtils.isEmpty(annotations)) {
            annotations = "false"
        }

        var bearings: MutableList<Bearing?>? = request.bearings?.toMutableList()

        if (request.userCourseInDegrees != null) {
            bearings = mutableListOf(Bearing(request.userCourseInDegrees.toInt(), 30))
            request.coordinates.forEach {
                bearings.add(null)
            }
        }

        var bearingString: String? = null

        if (bearings != null) {
            if (bearings.size > 0 && bearings.size == request.coordinates.size) {
                bearingString = bearings.joinToString(";") { it.toString() ?: "" }
            }
        }

        api.route(
            request.navigationMode.apiName,
            coordPairs.joinToString(separator = ";"),
            request.steps,
            bearingString,
            annotations,
            request.overview.apiName,
            request.geometry.apiName,
            request.alternatives
        ).enqueue {
            onResponse = {
                val body = it.body()
                if (it.isSuccessful && body != null) {
                    completion(Success(body.routes?.get(0)))
                } else {
                    completion(Failure(ApiException(it.code(), it.message())))
                }
            }

            onFailure = {
                completion(Failure(it ?: Exception("Unknown exception")))
            }
        }
    }
}

interface NavigationApi {
    @GET("{mode}/{coordinates}")
    fun route(
        @Path("mode") mode: String,
        @Path("coordinates") coordinates: String,
        @Query("steps") steps: Boolean,
        @Query("bearings") bearings: String?,
        @Query("annotations") annotations: String,
        @Query("overview") overview: String,
        @Query("geometries") geometries: String,
        @Query("alternatives") alternatives: Boolean
    ): Call<NavigationResponse>

    companion object Factory {
        fun create(baseUrl: String, apiKey: String): NavigationApi {
            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BASIC

            val retrofit = Retrofit.Builder()
                .client(
                    OkHttpClient.Builder()
                        .addInterceptor(loggingInterceptor)
                        .addInterceptor {
                            it.proceed(
                                it.request()
                                    .newBuilder()
                                    .header(ApiUtils.USER_AGENT_HEADER, ApiUtils.getUserAgent())
                                    .header(ApiUtils.API_KEY_HEADER, apiKey)
                                    .build()
                            )
                        }
                        .build()
                )
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(baseUrl)
                .build()

            return retrofit.create(NavigationApi::class.java)
        }
    }
}

/**
 * Navigation Response with a list of waypoints.
 */
class NavigationResponse() {
    lateinit var uuid: String

    @SerializedName("routes")
    var routes: List<Route>? = null

    @SerializedName("waypoints")
    var waypoints: List<NavigationWaypoint>? = null

    @SerializedName("code")
    var code: String? = null
}

@Parcelize
data class Route(
    @SerializedName("legs")
    var legs: List<RouteLeg>? = null,
    @SerializedName("geometry")
    var encodedPolyline: String? = null,
    @SerializedName("distance")
    var distanceInM: Double? = null,
    @SerializedName("duration")
    var durationInSeconds: Double? = null,

    var navigationMode: NavigationMode = NavigationMode.CAR
) : Parcelable {
    @IgnoredOnParcel
    val polyline: Polyline? by lazy {
        val pl = encodedPolyline
        if (pl != null) {
            Polyline(pl)
        } else {
            null
        }
    }

    @IgnoredOnParcel
    val coordinates: List<LocationPoint>? by lazy { polyline?.coordinates }

    fun remainingDistance(legIndex: Int, stepIndex: Int, geometryIndex: Int): Pair<Double, Double>? {
        if (legIndex >= legs?.size ?: -1) {
            return null
        }

        return legs?.let {
            val distance = it[legIndex].remainingDistance(stepIndex, geometryIndex)
            val remainingLegs = (legIndex + 1 until it.size).map { i -> it[i] }
            val remainingDistanceForRoute = remainingLegs.sumByDouble {
                it.distanceInM ?: 0.0
            } + distance.second

            Pair(distance.first, remainingDistanceForRoute)
        }
    }

    fun remainingDuration(legIndex: Int, stepIndex: Int, geometryIndex: Int): Pair<Double, Double>? {
        if (legIndex >= legs?.size ?: -1) {
            return null
        }

        return legs?.let {
            val duration = it[legIndex].remainingDuration(stepIndex, geometryIndex)
            val remainingLegs = (legIndex + 1 until it.size).map { i -> it[i] }
            val remainingDurationForRoute = remainingLegs.sumByDouble {
                it.durationInS ?: 0.0
            } + duration.second

            Pair(duration.first, remainingDurationForRoute)
        }
    }

    fun splitPolyline(legIndex: Int, stepIndex: Int, geometryIndex: Int): Pair<List<LocationPoint>, List<LocationPoint>> {
        if (legIndex >= legs?.size ?: -1) {
            return Pair(listOf(), listOf())
        }

        legs?.let {
            val previousLegs = (0 until legIndex).map { i -> it[i] }
            val currentLeg = it[legIndex]
            val nextLegs = (legIndex + 1 until it.size).map { i -> it[i] }

            val splittedPolyline = currentLeg.splitPolyline(stepIndex, geometryIndex)

            val previousCoordinates = previousLegs.flatMap { it.coordinates } + splittedPolyline.first
            val nextCoordinates = splittedPolyline.second + nextLegs.flatMap { it.coordinates }

            return Pair(previousCoordinates, nextCoordinates)
        } ?: return Pair(listOf(), listOf())
    }

    companion object {
        fun makeLocation(coordinates: List<Double>?): LocationPoint? {
            return if (coordinates?.size == 2) {
                LocationPoint(coordinates[1], coordinates[0])
            } else {
                null
            }
        }
    }
}

@Parcelize
data class RouteLeg(
    @SerializedName("annotation")
    var apiAnnotation: RouteAnnotation? = null,
    @SerializedName("steps")
    var steps: List<RouteStep>? = null,
    @SerializedName("distance")
    var distanceInM: Double? = null,
    @SerializedName("duration")
    var durationInS: Double? = null,
    @SerializedName("summary")
    var summary: String? = null
) : Parcelable {
    @IgnoredOnParcel
    val distances: List<Double>? by lazy { apiAnnotation?.distances }

    @IgnoredOnParcel
    val durations: List<Double>? by lazy { apiAnnotation?.durations }

    @IgnoredOnParcel
    val coordinates: MutableList<LocationPoint> by lazy {
        // Add all coordinates from steps to route leg steps.
        val result: MutableList<LocationPoint> = mutableListOf()
        steps?.forEach { it.coordinates?.forEach { result.add(it) } }
        result
    }

    fun remainingDistance(stepIndex: Int, geometryIndex: Int): Pair<Double, Double> {
        steps?.let { localSteps ->
            if (stepIndex >= localSteps.size) {
                return Pair(0.0, 0.0)
            }

            val currentStep = localSteps[stepIndex]
            val remainingSteps = (stepIndex + 1 until localSteps.size).map { localSteps[it] }

            val distanceForCurrentStep = currentStep.remainingDistance(geometryIndex)
            var remainingDistance = distanceForCurrentStep.second
            remainingSteps.forEach { remainingDistance += it.distanceInM ?: 0.0 }

            return Pair(distanceForCurrentStep.first, remainingDistance)
        }
        return Pair(0.0, 0.0)
    }

    fun remainingDuration(stepIndex: Int, geometryIndex: Int): Pair<Double, Double> {
        steps?.let { localSteps ->
            if (stepIndex >= localSteps.size) {
                return Pair(0.0, 0.0)
            }

            val currentStep = localSteps[stepIndex]
            val remainingSteps = (stepIndex + 1 until localSteps.size).map { localSteps[it] }

            val durationForCurrentStep = currentStep.remainingDuration(geometryIndex)
            var remainingDuration = durationForCurrentStep.second
            remainingSteps.forEach { remainingDuration += it.durationInS ?: 0.0 }

            return Pair(durationForCurrentStep.first, remainingDuration)
        }
        return Pair(0.0, 0.0)
    }

    fun splitPolyline(stepIndex: Int, geometryIndex: Int): Pair<List<LocationPoint>, List<LocationPoint>> {
        steps?.let {
            if (stepIndex >= it.size) {
                return Pair(listOf(), listOf())
            }

            val previousSteps: List<RouteStep> = (0 until stepIndex).map { i -> it[i] }
            val currentStep: RouteStep = it[stepIndex]
            val nextSteps: List<RouteStep> = ((stepIndex + 1) until it.size).map { i -> it[i] }

            val coordinates = currentStep.coordinates

            if (geometryIndex >= coordinates?.size ?: -1) {
                return Pair(listOf(), listOf())
            }

            val previousCoordinatesOfStep: List<LocationPoint> = (0 until geometryIndex).map { i -> coordinates!![i] }
            val nextCoordinatesOfStep: List<LocationPoint> = (geometryIndex until coordinates!!.size).map { i -> coordinates[i] }

            val previousCoordinates: MutableList<LocationPoint> = mutableListOf()
            previousSteps.forEach {
                it.coordinates?.let { previousCoordinates.addAll(it) }
            }
            previousCoordinates.addAll(previousCoordinatesOfStep)

            val nextCoordinates: MutableList<LocationPoint> = mutableListOf()
            nextCoordinates.addAll(nextCoordinatesOfStep)
            nextSteps.forEach {
                it.coordinates?.let { nextCoordinates.addAll(it) }
            }

            return Pair(previousCoordinates, nextCoordinates)
        }
        return Pair(listOf(), listOf())
    }
}

@Parcelize
data class RouteAnnotation(
    @SerializedName("distance")
    var distances: List<Double>? = null,
    @SerializedName("duration")
    var durations: List<Double>? = null
) : Parcelable

@Parcelize
data class RouteStep(
    @SerializedName("maneuver")
    var maneuver: StepManeuver? = null,
    @SerializedName("duration")
    var durationInS: Double? = null,
    @SerializedName("distance")
    var distanceInM: Double? = null,
    @SerializedName("geometry")
    var encodedPolyline: String? = null,
    @SerializedName("name")
    var name: String? = null,
    @SerializedName("ref")
    var ref: String? = null,
    @SerializedName("rotary_name")
    var rotaryName: String? = null,
    @SerializedName("intersections")
    var intersections: List<RouteIntersection>? = null,

    var remainingDistanceInM: Double = 0.0,
    var iconResId: Int = 0,
    val distances: MutableList<Double> = mutableListOf(),
    val durations: MutableList<Double> = mutableListOf()
) : Parcelable {
    /**
     * Sets [distanceInM] and [remainingDistanceInM] if [distance] not null.
     * This should be used for any case where [distanceInM] is set.
     */
    fun setDistance(distance: Double?) {
        distance?.let { remainingDistanceInM = it }
        distanceInM = distance
    }

    @IgnoredOnParcel
    val polyline: Polyline? by lazy {
        val pl = encodedPolyline
        if (pl != null) {
            Polyline(pl)
        } else {
            null
        }
    }

    @IgnoredOnParcel
    val coordinates: List<LocationPoint>? by lazy { polyline?.coordinates }

    fun remainingDistance(geometryIndex: Int): Pair<Double, Double> {
        if (geometryIndex <= 0) {
            return Pair(0.0, distanceInM ?: 0.0)
        }

        if (geometryIndex > distances.size) {
            return Pair(0.0, 0.0)
        }

        val distanceForCurrentGeometry = distances[geometryIndex - 1]
        val distance = (geometryIndex until distances.size).map { i -> distances[i] }.sum()

        return Pair(distanceForCurrentGeometry, distance)
    }

    fun remainingDuration(geometryIndex: Int): Pair<Double, Double> {
        if (geometryIndex <= 0) {
            return Pair(0.0, durationInS ?: 0.0)
        }

        if (geometryIndex > durations.size) {
            return Pair(0.0, 0.0)
        }

        val durationForCurrentGeometry = durations[geometryIndex - 1]
        val duration = (geometryIndex until durations.size).map { i -> durations[i] }.sum()

        return Pair(durationForCurrentGeometry, duration)
    }
}

@Parcelize
data class StepManeuver(
    @SerializedName("bearing_after")
    var bearingAfter: Int? = null,
    @SerializedName("bearing_before")
    var bearingBefore: Int? = null,
    @SerializedName("type")
    var apiType: String? = null,
    @SerializedName("modifier")
    var apiModifier: String? = null,
    @SerializedName("exit")
    var exit: Int? = null,
    @SerializedName("location")
    var apiLocation: List<Double>? = null,

    var rotaryExitBearing: Double? = null
) : Parcelable {
    @IgnoredOnParcel
    val locationPoint: LocationPoint? by lazy { Route.makeLocation(apiLocation) }

    @IgnoredOnParcel
    val maneuverType: ManeuverType? by lazy {
        val type = apiType
        if (type != null) {
            ManeuverType.makeManeuverType(type)
        } else {
            null
        }
    }

    @IgnoredOnParcel
    val direction: Direction? by lazy {
        val mod = apiModifier
        if (mod != null) {
            Direction.makeDirection(mod)
        } else {
            null
        }
    }
}

@Parcelize
data class RouteIntersection(
    @SerializedName("out")
    var routeOut: Int? = null,
    @SerializedName("in")
    var routeIn: Int? = null,
    @SerializedName("bearings")
    var bearings: List<Int>? = null,
    @SerializedName("location")
    var apiLocation: List<Double>? = null,
    @SerializedName("classes")
    var classes: List<String>? = null,
    @SerializedName("entry")
    var entries: List<Boolean>? = null,
    @SerializedName("lanes")
    var lanes: List<RouteLane>? = null
) : Parcelable {
    @IgnoredOnParcel
    val locationPoint: LocationPoint? by lazy { Route.makeLocation(apiLocation) }
}

@Parcelize
data class RouteLane(
    @SerializedName("indications")
    val apiIndications: List<String>? = null,
    @SerializedName("valid")
    val valid: Boolean? = null
) : Parcelable {
    @IgnoredOnParcel
    val indicationList: MutableList<Direction> by lazy {
        val result: MutableList<Direction> = mutableListOf()
        apiIndications?.forEach { result.add(Direction.makeDirection(it)) }
        result
    }
}

class NavigationWaypoint() {
    @SerializedName("name")
    var name: String? = null

    @SerializedName("distance")
    var distance: Double? = null

    @SerializedName("location")
    var apiLocation: List<Double>? = null

    val locationPoint: LocationPoint? by lazy { Route.makeLocation(apiLocation) }
}

enum class ManeuverType {
    // A basic turn into direction of the modifier.
    TURN,

    // No turn is taken/possible, but the road name changes. The road can take a turn itself, following modifier.
    NEWNAME,

    // Indicates the departure of the leg.
    DEPART,

    // Indicates the destination of a leg.
    ARRIVE,

    // Merge onto a street.
    MERGE,

    // Take ramp to enter a highway, direction given by modifier.
    ONRAMP,

    // Take a ramp to exit a highway, direction given by modifier)
    OFFRAMP,

    // Take the left/right side at a fork depending on modifier.
    FORK,

    // Road ends in a T intersection, turn in direction of modifier.
    ENDOFROAD,

    // Turn in direction of modifier to stay on the same road.
    CONTINUE,

    // Traverse roundabout, has additional property exit with NR if the roundabout is left.
    // The modifier specifies the direction of entering the roundabout.
    ROUNDABOUT,

    //  Describes a maneuver exiting a roundabout (usually preceeded by a roundabout instruction)
    EXITROUNDABOUT,

    // A traffic circle. While very similar to a larger version of a roundabout, it does not necessarily follow roundabout rules for right of way.
    // It can offer rotary_name and/or rotary_pronunciation parameters (located in the RouteStep object) in addition to the
    // exit parameter (located on the StepManeuver object).
    ROTARY,

    // Describes the maneuver exiting a rotary (large named roundabout)
    EXITROTARY,

    // Example instruction: At the roundabout, turn left.
    // Describes a turn at a small roundabout that should be treated as normal turn. The modifier indicates the turn direction.
    ROUNDABOUTTURN,

    // Not an actual turn but a change in the driving conditions. For example the travel mode or classes.
    // If the road takes a turn itself, the modifier describes the direction
    NOTIFICATION;

    companion object {
        fun makeManeuverType(apiName: String): ManeuverType {
            return when (apiName) {
                "turn" -> TURN
                "new name" -> NEWNAME
                "depart" -> DEPART
                "arrive" -> ARRIVE
                "merge" -> MERGE
                "on ramp" -> ONRAMP
                "off ramp" -> OFFRAMP
                "fork" -> FORK
                "end of road" -> ENDOFROAD
                "continue" -> CONTINUE
                "roundabout" -> ROUNDABOUT
                "exit roundabout" -> EXITROUNDABOUT
                "rotary" -> ROTARY
                "exit rotary" -> EXITROTARY
                "roundabout turn" -> ROUNDABOUTTURN
                "notification" -> NOTIFICATION
                else -> throw UnsupportedOperationException("Unknown maneuver of type: " + apiName)
            }
        }
    }
}

/*enum class SpeedConstraints {
    // Highway, >100 km/h limit
    FAST,

    // Country road, <= 100 km/h && > 50 km/h
    NORMAL,

    // City Street, <= 50 km/h
    SLOW;

    companion object {
        fun make(road: Road?): SpeedConstraints {
            road?.let {
                val speedLimit = it.speedLimit
                if (speedLimit.type == SpeedLimit.SpeedLimitType.KMH) {
                    val kmh = speedLimit.kmh
                    return if (kmh <= 50.0) SLOW
                    else if (kmh > 50.0 && kmh < 100.0) NORMAL
                    else FAST
                }
            }

            return SLOW
        }

        fun allowedDistanceDeviation(constraint: SpeedConstraints?): Int {
            if (constraint == null) {
                return 30
            }
            return when (constraint) {
                FAST -> 70
                NORMAL -> 50
                SLOW -> 30
            }
        }
    }
}*/

enum class Direction {
    NONE,
    UTURN,
    SHARPRIGHT,
    RIGHT,
    SLIGHTRIGHT,
    STRAIGHT,
    SLIGHTLEFT,
    LEFT,
    SHARPLEFT;

    companion object {
        fun makeDirection(apiName: String): Direction {
            return when (apiName) {
                "none" -> NONE
                "uturn" -> UTURN
                "sharp right" -> SHARPRIGHT
                "right" -> RIGHT
                "slight right" -> SLIGHTRIGHT
                "straight" -> STRAIGHT
                "slight left" -> SLIGHTLEFT
                "left" -> LEFT
                "sharp left" -> SHARPLEFT
                else -> NONE
            }
        }

        fun makeDirection(bearing: Int): Direction {
            return when (bearing) {
                in -25..25 -> STRAIGHT
                in 26..60 -> SLIGHTRIGHT
                in 61..120 -> RIGHT
                in 121..155 -> SHARPRIGHT
                in 156..180 -> UTURN
                in -180..-156 -> UTURN
                in -155..-121 -> SHARPLEFT
                in -120..-61 -> LEFT
                in -60..-26 -> SLIGHTLEFT
                else -> NONE
            }
        }
    }
}
