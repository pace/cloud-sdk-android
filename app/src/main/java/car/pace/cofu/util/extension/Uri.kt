package car.pace.cofu.util.extension

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

fun String.encode(): String {
    return URLEncoder.encode(this, StandardCharsets.UTF_8.toString())
}
