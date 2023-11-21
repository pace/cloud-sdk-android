package car.pace.cofu.configuration

const val CONFIGURATION_FILE_NAME = "configuration.json"

data class Configuration(
    val app_name: String,
    val application_id: String,
    val android_keystore_password: String,
    val android_signing_key_alias: String,
    val android_signing_key_password: String,
    val client_id: String,
    val default_idp: String?,
    val primary_branding_color: String,
    val secondary_branding_color: String,
    val google_maps_api_key: String,
    val map_enabled: Boolean,
    val sentry_dsn_android: String?,
    val sentry_project_name: String?,
    val sentry_enabled: Boolean,
    val analytics_enabled: Boolean,
    val crashlytics_enabled: Boolean,
    val hide_prices: Boolean,
    val automatic_production_updates_enabled: Boolean,
    val native_fuelcard_management_enabled: Boolean,
    val vehicle_integration_enabled: Boolean,
    val onboarding_show_custom_header: Boolean = false, // TODO: set to true if images not found (follow up)
    val home_show_custom_header: Boolean = false, // TODO: set to true if images not found (follow up)
    val menu_entries: List<MenuEntry>
)

data class MenuEntry(
    val menu_entries_id: MenuEntriesId
)

data class MenuEntriesId(
    val menu_entry: List<MenuEntryLocalization>
)

data class MenuEntryLocalization(
    val languages_code: String,
    val name: String,
    val url: String
)
