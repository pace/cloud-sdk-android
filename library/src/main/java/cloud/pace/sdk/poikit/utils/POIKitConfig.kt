package cloud.pace.sdk.poikit.utils

/**
 * Global constants to configure POIKit.
 */

object POIKitConfig {

    // Road & POI pinning

    /**
     * Max number of download jobs in the tile downloading queue.
     */
    val MAX_DOWNLOAD_JOBS_IN_QUEUE = 2

    // Other

    /**
     * Zoom level of Open Street Map tiles.
     */
    val ZOOMLEVEL = 15

    /**
     * Earth radius in km.
     */
    val EARTH_RADIUS_KM = 6371.0

    /**
     * Http client connection timeout in seconds.
     */
    val CONNECT_TIMEOUT = 10L

    /**
     * Http client read timeout in seconds.
     */
    val READ_TIMEOUT = 10L
}
