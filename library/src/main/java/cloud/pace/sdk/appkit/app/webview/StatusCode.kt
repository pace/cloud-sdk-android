package cloud.pace.sdk.appkit.app.webview

enum class StatusCode(val code: Int) {
    Ok(200),
    Unauthorized(401),
    NotFound(404),
    NotAllowed(405),
    InternalError(500)
}
