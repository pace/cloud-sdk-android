package car.pace.cofu.ui.main

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import car.pace.cofu.R
import car.pace.cofu.core.events.ActivityEvent
import car.pace.cofu.core.events.CloseDrawer
import car.pace.cofu.core.events.ShowSnack
import car.pace.cofu.core.mvvm.BaseActivity
import car.pace.cofu.databinding.ActivityMainBinding
import car.pace.cofu.ui.home.HomeFragmentDirections
import cloud.pace.sdk.appkit.AppKit
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

// Workaround for https://github.com/google/dagger/issues/1904
abstract class BaseMainActivity : BaseActivity<ActivityMainBinding, MainViewModel>(
    R.layout.activity_main,
    MainViewModel::class
)

@AndroidEntryPoint
class MainActivity : BaseMainActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the start destination depending on whether onboarding is done
        val navHostFragment =
            (supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment)
        val inflater = navHostFragment.navController.navInflater
        val graph = inflater.inflate(R.navigation.nav_graph_main)
        graph.startDestination =
            if (viewModel.onboardingDone) R.id.fragment_home else R.id.fragment_onboarding
        navHostFragment.navController.graph = graph
    }

    inline val binding get() = getBinding<ActivityMainBinding>()

    override fun onBackPressed() {
        if (binding?.drawerLayout?.isDrawerOpen(GravityCompat.START) == true) {
            hideDrawer()
        } else {
            super.onBackPressed()
        }
    }

    /**
     * sets whether or not the user can open the drawer menu by swiping from the edge
     */
    var menuSwipingActive: Boolean
        get() = binding?.drawerLayout?.getDrawerLockMode(GravityCompat.START) == DrawerLayout.LOCK_MODE_LOCKED_CLOSED
        set(value) {
            val mode =
                if (value) DrawerLayout.LOCK_MODE_UNLOCKED else DrawerLayout.LOCK_MODE_LOCKED_CLOSED
            binding?.drawerLayout?.setDrawerLockMode(mode, GravityCompat.START)
        }

    /**
     * opens the drawer menu when it's closed and vice versa
     */
    fun toggleMenu() {
        if (binding?.drawerLayout?.isDrawerOpen(GravityCompat.START) == true) {
            binding?.drawerLayout?.closeDrawer(GravityCompat.START)
        } else {
            binding?.drawerLayout?.openDrawer(GravityCompat.START)
        }
    }

    /**
     * closes the navigation drawer menu
     */
    private fun hideDrawer() {
        binding?.drawerLayout?.closeDrawer(GravityCompat.START)
    }

    override fun onHandleActivityEvent(event: ActivityEvent) {
        when (event) {
            is CloseDrawer -> hideDrawer()
            is MainViewModel.MenuItemClickEvent -> onMenuItemClick(event.menuItemId)
            else -> super.onHandleActivityEvent(event)
        }
    }

    private fun onMenuItemClick(menuItemId: Int) {
        hideDrawer()

        when (menuItemId) {
            R.string.menu_item_impressum -> openWebView("impressum.html")
            R.string.menu_item_privacy -> openWebView("privacy.html")
            R.string.menu_item_licenses -> showLicenseScreen()
            R.string.menu_item_fuel_type -> openFuelTypeSettings()
            R.string.menu_item_payment -> AppKit.openPaymentApp(this)
            R.string.menu_item_history -> AppKit.openTransactions(this)
            R.string.menu_item_logout -> showLogoutConfirmation()
        }
    }

    private val navController: NavController?
        get() {
            val fragmentContainer = binding?.fragmentContainer ?: return null
            return findNavController(fragmentContainer.id)
        }

    private fun showLogoutConfirmation() {
        navController?.navigate(HomeFragmentDirections.openLogoutConfirmation())
    }

    private fun openFuelTypeSettings() {
        navController?.navigate(HomeFragmentDirections.openFuelTypeSettings())
    }

    private fun openWebView(filename: String) {
        navController?.navigate(HomeFragmentDirections.openWebview(filename))
    }

    private fun showLicenseScreen() {
        startActivity(Intent(this, OssLicensesMenuActivity::class.java))
    }

    private var currentSnackbar: Snackbar? = null

    override fun onShowMessage(showSnack: ShowSnack) {
        val host = getBinding<ActivityMainBinding>()?.coordinator ?: return
        val text = showSnack.messageRes?.let { getString(it) } ?: showSnack.message ?: return
        currentSnackbar?.dismiss()

        val snackBar = Snackbar.make(
            host, text, if (showSnack.actionListener != null)
                Snackbar.LENGTH_INDEFINITE else Snackbar.LENGTH_LONG
        )

        val textView =
            snackBar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        textView.maxLines = 5
        textView.setTextColor(ContextCompat.getColor(this, showSnack.type.textColorRes))

        snackBar.view.background = ContextCompat.getDrawable(this, showSnack.type.backgroundRes)

        showSnack.actionListener?.let { action ->
            snackBar.setActionTextColor(ContextCompat.getColor(this, showSnack.type.actionColorRes))
            snackBar.setAction(showSnack.actionText) { snackBar.dismiss(); action.invoke() }
        }

        snackBar.show()
        currentSnackbar = snackBar
    }

    override fun dismissSnackbars() {
        currentSnackbar?.dismiss()
        currentSnackbar = null
    }
}