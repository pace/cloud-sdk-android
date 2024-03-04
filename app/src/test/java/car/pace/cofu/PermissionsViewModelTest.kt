package car.pace.cofu

import android.Manifest
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import car.pace.cofu.data.PermissionRepository
import car.pace.cofu.data.PermissionRepository.Companion.locationPermissions
import car.pace.cofu.ui.more.permissions.PermissionsViewModel
import car.pace.cofu.util.BuildProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import junit.framework.TestCase.assertEquals
import org.junit.Test

class PermissionsViewModelTest {
    private val permissionRepository = mockk<PermissionRepository>(relaxed = true)
    private val activity = mockk<AppCompatActivity>(relaxed = true)
    private lateinit var viewModel: PermissionsViewModel

    private fun setup(
        analyticsEnabled: Boolean = true,
        buildVersion: Int = Build.VERSION_CODES.TIRAMISU
    ) {
        mockkObject(BuildProvider)
        every { BuildProvider.isAnalyticsEnabled() } returns analyticsEnabled
        every { BuildProvider.getSDKVersion() } returns buildVersion
        viewModel = PermissionsViewModel(permissionRepository)
    }

    @Test
    fun `show notification setting if analytics enabled`() {
        setup()
        assertEquals(listOf(PermissionsViewModel.PermissionsItem.NOTIFICATIONS, PermissionsViewModel.PermissionsItem.LOCATION), viewModel.items)
    }

    @Test
    fun `hide notification setting if analytics disabled`() {
        setup(analyticsEnabled = false)
        assertEquals(listOf(PermissionsViewModel.PermissionsItem.LOCATION), viewModel.items)
    }

    @Test
    fun `hide notification setting if build version too low`() {
        setup(analyticsEnabled = true, buildVersion = 30)
        assertEquals(listOf(PermissionsViewModel.PermissionsItem.LOCATION), viewModel.items)
    }

    @Test
    fun `trigger notification disable dialog`() {
        setup()
        viewModel.enableNotifications(activity, false)
        val expectedDialog = PermissionsViewModel.PermissionDialog.SettingsDialog(R.string.alert_notification_permission_disabled_title)
        assertEquals(expectedDialog, viewModel.permissionsDialog)
    }

    @Test
    fun `trigger notification enable dialog`() {
        setup()
        every { permissionRepository.shouldShowRequestPermissionRationale(activity, Manifest.permission.POST_NOTIFICATIONS) } returns false
        viewModel.enableNotifications(activity, true)
        val expectedDialog = PermissionsViewModel.PermissionDialog.SettingsDialog(R.string.alert_notification_permission_denied_title)
        assertEquals(expectedDialog, viewModel.permissionsDialog)
    }

    @Test
    fun `trigger notification system dialog`() {
        setup()
        every { permissionRepository.shouldShowRequestPermissionRationale(activity, Manifest.permission.POST_NOTIFICATIONS) } returns true
        viewModel.enableNotifications(activity, true)
        val expectedDialog = PermissionsViewModel.PermissionDialog.SystemDialog(listOf(PermissionRepository.NOTIFICATION_PERMISSION))
        assertEquals(expectedDialog, viewModel.permissionsDialog as PermissionsViewModel.PermissionDialog.SystemDialog)
    }

    @Test
    fun `trigger location disable dialog`() {
        setup()
        viewModel.enableLocation(activity, false)
        val expectedDialog = PermissionsViewModel.PermissionDialog.SettingsDialog(R.string.alert_location_permission_disabled_title)
        assertEquals(expectedDialog, viewModel.permissionsDialog)
    }

    @Test
    fun `trigger location enable dialog`() {
        setup()
        every { permissionRepository.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION) } returns false
        viewModel.enableLocation(activity, true)
        val expectedDialog = PermissionsViewModel.PermissionDialog.SettingsDialog(R.string.alert_location_permission_denied_title)
        assertEquals(expectedDialog, viewModel.permissionsDialog)
    }

    @Test
    fun `trigger location system dialog`() {
        setup()
        every { permissionRepository.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION) } returns true
        viewModel.enableLocation(activity, true)
        val expectedDialog = PermissionsViewModel.PermissionDialog.SystemDialog(locationPermissions)
        assertEquals(expectedDialog, viewModel.permissionsDialog as PermissionsViewModel.PermissionDialog.SystemDialog)
    }
}
