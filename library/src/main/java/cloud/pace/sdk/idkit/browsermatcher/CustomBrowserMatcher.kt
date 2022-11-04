package cloud.pace.sdk.idkit.browsermatcher

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import net.openid.appauth.browser.AnyBrowserMatcher
import net.openid.appauth.browser.BrowserDescriptor
import net.openid.appauth.browser.BrowserMatcher
import net.openid.appauth.browser.Browsers
import net.openid.appauth.browser.VersionedBrowserMatcher.CHROME_CUSTOM_TAB
import timber.log.Timber

class CustomBrowserMatcher(private val context: Context) : BrowserMatcher {

    private val packageInfo by lazy { getInstalledChromePackageInfo() }

    override fun matches(descriptor: BrowserDescriptor): Boolean {
        Timber.i("Check if this browser matches: package = ${descriptor.packageName}, version = ${descriptor.version}")
        if (CHROME_CUSTOM_TAB.matches(descriptor)) {
            Timber.i("This browser descriptor matches Chrome custom tab")
            return true
        }

        val packageInfo = packageInfo
        return if (packageInfo != null && CHROME_CUSTOM_TAB.matches(BrowserDescriptor(packageInfo, true)) && packageInfo.applicationInfo.enabled) {
            Timber.i(
                "A supported Chrome custom tab is installed and enabled but this browser descriptor is not the Chrome custom tab. " +
                    "Return false so that AppAuth continues with next browser."
            )
            false
        } else {
            Timber.w("No supported Chrome custom tab is installed or enabled. Use AppAuth's default browser matcher.")
            AnyBrowserMatcher.INSTANCE.matches(descriptor)
        }
    }

    private fun getInstalledChromePackageInfo(): PackageInfo? {
        return try {
            context.packageManager.getPackageInfo(Browsers.Chrome.PACKAGE_NAME, PackageManager.GET_SIGNATURES).also {
                Timber.d("Chrome is installed: version = ${it.versionName}, enabled = ${it.applicationInfo.enabled}")
            }
        } catch (e: PackageManager.NameNotFoundException) {
            Timber.e(e, "Chrome is not installed")
            null
        }
    }
}
