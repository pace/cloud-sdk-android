package cloud.pace.sdk.appkit.utils

import androidx.core.content.FileProvider

/**
 * Create custom [FileProvider] so that the client app does not get a manifest merge error if it uses the default Android [FileProvider].
 */
class PACECloudSDKFileProvider : FileProvider()
