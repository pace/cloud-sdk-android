21.1.0 Release notes (2024-02-20)
=============================================================

### Enhancements

* Add function to request CoFu gas stations within visible region

### Fixes

* Only enable web contents debugging in debug builds

### Internal

* Re-generate pay API 2022-1

21.0.1 Release notes (2023-12-06)
=============================================================

### Internal

* Add cofu gas station object to gas station
* Add network security config to example apps

21.0.0 Release notes (2023-11-07)
=============================================================

### Breaking Changes

* Set min SDK version to Android 8
* Add clientId as requirement for SDK setup

### Fixes

* Regenerate API files to fix relationship and type generation
* Use git tags instead of GitLab tags to retrieve last tag to construct the snapshot version
* Add suffix to API filenames to avoid filenames that are reserved on Windows

### Internal

* Also publish the SDK as snapshot release
* TargetSdk and compileSdk to Android 14

20.1.0 Release notes (2023-08-09)
=============================================================

### Enhancements

* Add a communication handler to start the navigation to the specified location

### Fixes

* Return the correct response in the Google Pay availability check
> **_NOTE:_** If you want to use Google Pay, please upgrade to this PACE Cloud SDK version.

### Internal

* Add Google Pay availability check as util function
* Rename brand id key

20.0.0 Release notes (2023-07-05)
=============================================================

### Breaking Changes

* Re-implement the local available apps and AppDrawer logic in Jetpack Compose
> **_BREAKING CHANGE:_** You no longer need to call AppKit.requestLocalApps, AppKit.openApps and AppKit.closeApps yourself. This is now automatically managed by the AppDrawerHost. So just add the AppDrawerHost composable function to your Jetpack Compose layout or the AppDrawerHostView to your view-based layout. See migration guide or documentation for more information.

### Fixes

* Add merchantId to request model to fix payment on production
> **_NOTE:_** If you want to use Google Pay, please upgrade to this PACE Cloud SDK version.

### Internal

* Add brand id to gas station

19.1.0 Release notes (2023-06-12)
=============================================================

### Enhancements

* Implement payment process with Google Pay

### Internal

* Add timeout to location functions

19.0.0 Release notes (2023-05-16)
=============================================================

### Breaking Changes

* Moved and renamed the CdnAPI to PaymentMethodVendorsAPI
* Change POIKit.observe functions to return live gas station response instead of observables

### Enhancements

* Add danish localization

18.1.0 Release notes (2023-04-25)
=============================================================

### Enhancements

* Add updating distance label to app drawers
* Add GasStation.isOpen function

18.0.0 Release notes (2023-03-31)
=============================================================

### Breaking Changes

* Upgraded dependencies for Android gradle plugin, Kotlin and Compose. Make sure client dependencies are compatible or update them.

### Fixes

* Correctly set webview background color

17.1.2 Release notes (2023-03-06)
=============================================================

### Internal

* Change Gradle configuration of Room testing from api to androidTestImplementation
* Generate API 2022-1
* Cleanup documentation
* Decrease log level of unimportant error logs

17.1.1 Release notes (2022-12-12)
=============================================================

### Fixes

* Fix crash in user preferences migration if an empty session is persisted

17.1.0 Release notes (2022-12-06)
=============================================================

### Enhancements

* Implement meta collector

### Internal

* Add the interruptible flag to the merge request CI jobs to cancel old pipelines

17.0.1 Release notes (2022-11-30)
=============================================================

### Internal

* Scope user preferences to user id

17.0.0 Release notes (2022-11-22)
=============================================================

### Breaking Changes

* Catch ActivityNotFoundExeption if no supported browser is installed during authorization

### Fixes

* Crash when selecting the feedback button in the fueling canceled error
* Disable all activity transition animations

### Internal

* Cleanup variables
* Force the use of Chrome custom tab for the authorization if it is installed and enabled
* Adjust GitLab base URL

16.0.3 Release notes (2022-10-12)
=============================================================

### Internal

* Remove createdAtStart = true flag in the GeoAPIManager Koin declaration to not make requests during SDK setup

16.0.2 Release notes (2022-10-11)
=============================================================

### Internal

* Add fuel.site to list of trusted domains
* Unified user agents from requests and the apps
* Support absolute paths in manifest
* Restructure generated API files so that the client can overwrite the request setup

16.0.1 Release notes (2022-08-05)
=============================================================

### Internal

* Add the ability to add additional query parameters to each payment method vendors request

16.0.0 Release notes (2022-08-04)
=============================================================

### Breaking Changes

* Cleanup todos in code
* Remove the stage environment

### Internal

* Add migration guide for stage removal

15.0.2 Release notes (2022-07-14)
=============================================================

### Fixes

* Make sure that the GeoJson is only requested once during the init of the caches

15.0.1 Release notes (2022-06-30)
=============================================================

### Fixes

* Remove AppFragment memory leak in AppActivity

### Internal

* Introduce environment-based CDN base url for payment method vendors request
* Change GeoAPIClient to use cdn url

15.0.0 Release notes (2022-06-15)
=============================================================

### Breaking Changes

* Revise user info request and response model
* Remove the internal POI GeoJSON API files
* Regenerate API files to fix problems with wrong return types

### Fixes

* Improve the handling of process kills during the creation of a payment method within the custom tab
* Correctly generate relationships and resources of API files

### Internal

* Remove bot configs
* Update documentation link
* Exclude unused 'Errors' classes from generated API code

14.0.0 Release notes (2022-05-16)
=============================================================

### Breaking Changes

* Remove enums in GasStation model

### Fixes

* Fix typo in GitLab bot config

### Internal

* Refactor URL and manifest handling to use fueling URL from GeoJSON
* Update string resources
* Add label config for PACEBot
* Remove GitLab issue templates

13.3.3 Release notes (2022-05-05)
=============================================================

### Internal

* Use cdn instead of cms

13.3.2 Release notes (2022-04-29)
=============================================================

### Fixes

* Use Gson instead of Moshi for CMSApi

### Internal

* Reformat code and optimize imports of all files
* Add pre-commit git hook to format generated code

13.3.1 Release notes (2022-04-20)
=============================================================

### Fixes

* Make sure that the API call returns the server error and not 401 if PACE ID is not reachable

### Internal

* Regenerate all API files to remove Authenticator
* Update GitLab templates

13.3.0 Release notes (2022-04-07)
=============================================================

### Enhancements

* Add callbacks to report breadcrumbs or errors to client app

13.2.0 Release notes (2022-04-06)
=============================================================

### Enhancements

* Implement fueling example app

### Fixes

* Fetch the gas stations to observe first if we don't have the coordinates

### Internal

* Add shellCard payment method and dieselB0 fuel type to GasStation model
* Remove geofence related code and strings
* Adjust bump script to automatically create changelog

13.1.1 Release notes (2022-03-18)
=============================================================

### Fixes

* Fix `IllegalStateException` in POIKit where the database was accessed on the main thread


13.1.0 Release notes (2022-03-10)
=============================================================

### Enhancements

* Add the following languages: Czech, French, Portuguese, Dutch, Italian, Polish, Romanian, Russian and Spanish
* Regenerate all API files with a new headers parameters per request
* Add new property `isOnlineCoFuGasStation` to `GasStation`

### Fixes

* Regenerate all API files to change the return type from `Void` to `ResponseBody`
* Fix wrong setup check error message
* Check if the manifest request was successful before deserializing the response body
* Add process payment quick fix for pre auth payments

### Internal

* Replace Kotlin synthetics with Jetpack View Binding
* Change the `screenOrientation` for all activities to `behind`, which uses the same `screenOrientation` as the activity immediately beneath it in the activity stack
* Remove JCenter as dependency repository
* Remove fallback to `apps/query` endpoint to request local apps if GeoJSON file couldn't be retrieved


13.0.0 Release notes (2022-02-10)
=============================================================

### Breaking Changes

* The `connectedFuelingStatus` property of the `CofuGasStation` is now nullable so that the Cofu gas station is not skipped in the cache loading if this property is absent in the API response

### Fixes

* Fix a bug where the `additionalParameters` of the `CustomOIDConfiguration` were overwritten by the values from the `PACECloudSDK.additionalQueryParams`
* Fix a bug where the payment process was not possible if the `pace_redirect_scheme` is not specified

### Internal

* Reduce requests to the token endpoint by refreshing the token only once if multiple API requests fail with `401 - Unauthorized`


12.2.0 Release notes (2022-01-14)
=============================================================

### Enhancements

* Add the possibility to open authorization and end session requests in a WebView instead of the custom tab. This behavior can be controlled with the new `integrated` attribute of the `oidConfiguration` of the SDK setup `configuration`. Set `integrated` to `true`, if you want to open the authorization and end session requests in a WebView. Set it to `false` (default), if you want to open them in the custom tab as before.


12.1.0 Release notes (2022-01-10)
=============================================================

### Enhancements

* Add environment based URL extensions. All extensions are defined in the `URL` object e.g. `URL.dashboard` to get the URL of the dashboard app of the set environment. `Environment.$ENV.$url` is deprecated now.


12.0.1 Release notes (2021-12-07)
=============================================================

### Internal

* Regenerate communication code and rewrite the `openUrlInNewTab` PWA request to handle the new `integrated` parameter. If the `integrated` parameter is set to true, then the URL will be loaded in a new WebView instead of the custom tab.


12.0.0 Release notes (2021-12-06)
=============================================================

### Breaking Changes

* Simplified setup of IDKit. SDK now decides which `OIDConfiguration` should be used so that clients only need to provide a `CustomOIDConfiguration` with a `clientId` and `redirectUri`.

### Enhancements

* Add `additionalProperties` property to `GasStation` model
* Set `pace.cloud` as default value of the `domainACL` property in the PACE Cloud SDK configuration

### Fixes

* Add default ProGuard rules to fix possible problems when client apps use ProGuard

### Internal

* Add `ACCESS_COARSE_LOCATION` permission check in SystemManager
* Update Room's version to `2.4.0-beta02` to fix a bug on Apple's M1 chips


11.1.0 Release notes (2021-11-19)
=============================================================

### Enhancements

* Introduce `shareText` PWA communication handler, which by default opens the system share sheet for sharing text. This behavior can be overridden in `AppCallback.onShareTextReceived(text, title)`.


11.0.1 Release notes (2021-11-18)
=============================================================

### Fixes

* **IMPORTANT: We have updated all dependencies to the latest stable versions to fix possible security vulnerabilities in `kotlin-reflect`. We have also updated target and compile SDK versions to API level 31 (Android 12).**

### Internal

* Change routing URL endpoint to a `pace.cloud` domain


11.0.0 Release notes (2021-11-09)
=============================================================

### Breaking changes

* Combine IDKit setup with PACECloudSDK setup. `IDKit.setup(...)` is no longer accessible. The `IDKit` is now initialized via `PACECloudSDK.setup(...)`. Therefore an optional `OIDConfiguration` parameter was added to the `Configuration` class of the `PACECloudSDK`, which must be initialized with at least the `clientId` and the `redirectUri`.
* Change properties of `CofuGasStation` from `var` to `val`

### Enhancements

* Add the attribute `properties: Map<String, Any>` to the `CofuGasStation` which includes different information of the gas station e.g. app URL

### Fixes

* Adjust handling of utm parameters

### Internal

* Adjust TOTP secret handling
* Send 499 status when user cancels login
* Add `appAuthRedirectScheme` as `metaData` in Manifest


10.2.2 Release notes (2021-10-20)
=============================================================

### Fixes

* Fix generated fueling api 2021-2


10.2.1 Release notes (2021-10-20)
=============================================================

### Internal

* Add `request-id` from backend to log message if request fails
* Remove default timeout in PWA communication API
* Use minified GeoJSON to save bandwidth and change default of `geoAppsScope` to `pace-min`
* Regenerate fueling API 2021-2


10.2.0 Release notes (2021-10-13)
=============================================================

### Enhancements

* Implement `isRemoteConfigAvailable` PWA communication handler to check if the remote config feature is generally available and returns `false` by default. This behavior can be overridden in `AppCallback.isRemoteConfigAvailable(isAvailable)`.

### Internal

* Add custom user agent in interceptor
* Regenerate Api for 2021-2


10.1.1 Release notes (2021-09-30)
=============================================================

### Fixes

* Override all callbacks for all `LocationListener` instances and adjust callback implementations


10.1.0 Release notes (2021-09-29)
=============================================================

### Enhancements

* Implement the `isSignedIn` PWA communication handler which returns `true` by default if the authorization is valid in `IDKit`. This behavior can be overridden in `AppCallback.isSignedIn(isSignedIn)`.
* Add `AppKit.openDashboard(...)` to open the Connected Fueling dashboard app
* Implement new price history endpoints in `POIKit` to fetch fuel price history by country or gas station

### Fixes

* Regenerate communication code to fix problem with unparsable error response
* Fix overwriting of custom set `utm_params`
* Fix bug where `AppActivity` was finished when it was started and `AppKit.openApps(...)` was called at the same time
* Overwrite  `LocationListener` callback functions to avoid problems/crashes for devices with Android version smaller than 11

### Internal

* Add setup flag to check whether `PACECloudSDK` has been set up correctly before using it's `Kit`s

10.0.0 Release notes (2021-09-16)
=============================================================

### Breaking changes

* Move everything that belongs to the GeoAPI from `AppKit`/`API` to `POIKit`: `AppKit.requestCofuGasStations(...)` moved to `POIKit.requestCofuGasStations(...)` and `AppKit.isPoiInRange(...)` moved to `POIKit.isPoiInRange(...)`

### Fixes

* Fix bug where CoFu stations outside the radius were returned from `requestCofuGasStations` call

### Internal

* Also use the center point (if available) of the CoFu station in the `isPoiInRange` check instead of only the coordinates of the polygons as already done in the local apps check


9.3.1 Release notes (2021-09-10)
=============================================================

### Fixes

* Refactor `openUrlInNewTab` flow to fix loading of `cancelUrl` when creating a payment method which opens in custom tab


9.3.0 Release notes (2021-09-09)
=============================================================

### Enhancements

* Change `requestCofuGasStations()` to return all cofu stations (online and offline)

### Fixes

* Fix Fueling PWA URLs for non-prod environment


9.2.1 Release notes (2021-09-02)
=============================================================

### Fixes

* Added `FLAG_ACTIVITY_NEW_TASK` flag to fix a bug where the receipt share sheet was not opened when the PACE Cloud SDK was initialized with an application context


9.2.0 Release notes (2021-09-01)
=============================================================

### Enhancements

* Add logging for successful setup 'PACECloudSDK' and 'IDKit' and for missing mandatory values for setup
* Open the share sheet by default when the PWA sends image data, e.g. when clicking on a receipt. This behavior can be overridden in `AppCallback.onImageDataReceived(bitmap)`
* The location's speed will only affect when app drawers are shown, but not when they are being removed again

### Fixes

* Only show network error in WebView if it is a main frame error
* Fix bug that third party browser custom tabs were not closed after adding the payment method e.g. PayPal
* Add methods to `IDKit` that returns the user's payment methods, transactions and checks the PIN requirements
* Remove `LocationAvailability` check since it causes issues on some devices
* Ensure that AppKit's requestLocalApps will always call the completion callback

### Internal

* Migrate Gradle files from Groovy to Kotlin
* Use Kotlin Coroutines instead of Future to fetch the PWA manifest and icons
* Add `bearing` parameter to `GetLocation` PWA call


9.1.0 Release notes (2021-08-03)
=============================================================

### Enhancements

* Add `AppRedirect` handler to PWA communication to let the client app decide if a redirect from the current PWA to another specified PWA should be allowed

### Fixes

* Adjust handling of too large bounding boxes when requesting tiles


9.0.0 Release notes (2021-07-20)
=============================================================

### Breaking changes

* Change default authentication mode to `AuthenticationMode.NATIVE`
* Request with multiple accept headers correctly generated e.g. `GetTermsAPI`
* Remove `autoClose` option when starting the `AppActivity`

### Enhancements

* Offer default configurations for all environments for clients that login with PACE ID (IDKit)

### Fixes

* Fix bug that could occur when the user tried to log in but was still logged in with another account
* Add function to Appkit to request Connected Fueling gas stations by location and radius

### Internal

* Use gas station endpoint as fallback for POI position if none was found in the database when observing a single POI by ID
* Return location based apps based on `appsDistanceThresholdInMeters` configuration and not if the location is in the polygon
* Rework communication between apps and SDK
* Add `isBiometricAuthEnabled` handler to PWA communication


8.2.0 Release notes (2021-07-12)
=============================================================

### Enhancements

* Added new generated request with possibility to set readTimeout times for requests
* Added classes to generated requests
* Correctly set content type for request with object schema
* Add function to POIKit to observe gas stations by ID and location

### Internal

* Update protobuf dependency and regenerate protobuf code


8.1.0 Release notes (2021-06-24)
=============================================================

### Enhancements

* Added new `Stats` endpoint to determine total number of connected fueling gas stations

8.0.1 Release notes (2021-06-22)
=============================================================

# Fixes

* Remove completion lambda from `isPoiInRange` and return `true` or `false` synchronously

8.0.0 Release notes (2021-06-21)
=============================================================

### Breaking changes

* Deprecated the `AppCallback.onTokenInvalid` method. Use `AppCallback.getAccessToken` instead that introduces the `isInitialToken` flag.
* Implement automatic session handling for apps. If `IDKit` is used, the SDK will now try to renew the session automatically when an app requests a new token. In this case the `getAccessToken` AppCallback will no longer be called. If the renewal fails the `onSessionRenewalFailed` AppCallback may be implemented to specify a custom behavior for the token retrieval. Otherwise the sign in mask will be shown.
* Speed up `isPoiInRange` call. Add optional `location` parameter which will be used instead of the current location if specified. Also make `isPoiInRange` suspend to call it in a own Coroutine.

### Enhancements

* Add a new `onLogin` AppCallback that is invoked when the user logs in via an automatic authorization request from the SDK within the PWA (not if `IDKit.authorize(...)` is called manually). This callback provides the `AppActivity` context and the authorization result.
* Intercept the URLs and close the PWA if it is the close redirect URI `cloudsdk://close`

### Internal

* Add `logout` handler to PWA communication
* Automatically refresh the access token and retry the request if it returns status code 401 (unauthorized). If `IDKit` is not initialized, no session is available or the token renewal failed, the error is passed to the client app.
* Add `getLocation` handler to PWA communication
* Content type and more headers are now generated
* Authorization header only used for needing requests

7.6.0 Release notes (2021-06-10)
=============================================================

### Enhancements

* Adjust Geo API to new version `2021-1`
* Add function to `AppKit` to request Connected Fueling gas stations

### Internal

* Improve selection of app drawer icon size


7.5.2 Release notes (2021-06-08)
=============================================================

### Fixes

* Change expected `getTraceId` message from `{}` to `""`


7.5.1 Release notes (2021-06-08)
=============================================================

### Fixes

* Fix NullPointerExceptions while deserializing JSON messages from PWA


7.5.0 Release notes (2021-06-02)
=============================================================

### Enhancements

* Make app drawer more robust by increasing distance threshold and checking whether same apps are returning when requesting local apps
* Add option to `IDKit.setPINWithBiometry(...)` to authenticate with the device PIN, pattern, or password instead of biometry (defaults to `true`)

### Fixes

* Implement a default handler for `getConfig` PWA message which returns `null` to prevent waiting for result
* Fix host in key under which secure data is persisted

### Internal

* Use `LocationProvider.currentLocation()` first and `LocationProvider.firstValidLocation()` as fallback to speed up the verify location call in the PWA
* Refactor `LocationProvider` so that every one-time location e.g. used in `isPoiInRange` or `requestLocalApps` is returned inline
* Decrease `getConfig` PWA message handling timeout to 5 seconds
* Set `utm_source` per default and add defined list of default `utm_params` to make sure these params won't be removed
* Return location accuracy in `verifyLocation` PWA message response
* Add option to authenticate payments with the device PIN, pattern, or password instead of biometry


7.4.0 Release notes (2021-05-12)
=============================================================

### Enhancements

* Add functions to launch the authorization and end session request as well as the handling of the response inline

### Internal

* Add default implementation of `onTokenInvalid` callback which automatically tries to refresh the token and shows the login form in case of error
* Refactor `isPoiInRange` check so that it no longer checks if the position is within the POI's polygon, but if beeline to the POI is within 500m


7.3.1 Release notes (2021-05-10)
=============================================================

### Fixes

* Ensure that `additionalQueryParams` is added to all PWA URLs


7.3.0 Release notes (2021-05-06)
=============================================================

### Fixes

* Fix body models in generated API requests

### Internal

* Introduce tracing identifier for all API requests and communication with PWA
* Refactor `SharedPreferences` handling in `AuthorizationManager`


7.2.1 Release notes (2021-04-29)
=============================================================

### Fixes

* Fix crash in POIKit database converter


7.2.0 Release notes (2021-04-28)
=============================================================

### Fixes

* Generate Pay API `2020-4`, which includes removal of enums to prevent the clients from crashing when new values are added

### Enhancements

* Add payment method vendor request
* Add cofuPaymentMethods field to GasStation entity

### Internal

* Introduce timeouts for PWA communication


7.1.0 Release notes (2021-04-20)
=============================================================

### Enhancements

* Intercept PWA logs and pass all logs to client
* Add handlers to set user properties, log analytic events and get configurations
* Add function to enable biometry within 5 minutes after authorization

### Internal

* Add missing parameter to JS communication
* Remove token validity check and send the received token to the PWA right away to prevent a possible `onTokenInvalid` loop


7.0.0 Release notes (2021-04-08)
=============================================================

### Breaking changes

* Pass `reason` and `oldToken` in the `onTokenInvalid` callback
> **_NOTE:_** Remember to adjust the implementation of your `tokenInvalid` function.

### Enhancements

* Implement check for chrome installation and always use it if installed and not deactivated
* Add convenience method for fueling app
* Add methods to handle PIN and biometry actions


6.0.0 Release notes (2021-03-19)
=============================================================

### Breaking changes

* Implement end session request and add end session endpoint to `OIDConfiguration` to clear user session on logout

### Internal

* Improve communication with PWA


5.2.0 Release notes (2021-03-12)
=============================================================

### Enhancements

* Remove Bintray integration because it will be discontinued
* Add Maven Central and JitPack as alternative repositories (see the installation section in README)


5.1.1 Release notes (2021-03-08)
=============================================================

### Enhancements

* Remove fetching of manifest and location speed check in `isPoiInRange`

### Fixes

* Return exception instead of last access token in `refreshToken` in case of error


5.1.0 Release notes (2021-03-03)
=============================================================

### Enhancements

* Add convenience method for most common URLs
* Generate new GeoJSON service with new filter possibilities, e.g. onlinePaymentMethod
* Checks for available apps in a given location is now done on device


5.0.0 Release notes (2021-02-24)
=============================================================

### Breaking changes

* Rework deep linking setup by configuring manifest placeholders

### Enhancements

* Add handler for deep linking redirect scheme
* Add zoom level to refresh POIs method
* Remove fixed POI search box size and add padding to visible region instead
* Add incremental padding util method
* Remove ZoomException


4.1.0 Release notes (2021-02-17)
=============================================================

### Enhancements

* Add generated User API


4.0.0 Release notes (2021-02-11)
=============================================================

### Breaking Changes

* Remove passing of initial access token from the PACECloudSDK.setup

### Enhancements

* Implement back handler for PWA communication
* Update generated POI API
* Add generated Pay API
* Add generated Fueling API
* Add generated GeoJSON API


3.1.0 Release notes (2021-01-28)
=============================================================

### Enhancements

* Pass the activity context instead of the application context to the onCustomSchemeError AppCallback


3.0.1 Release notes (2021-01-26)
=============================================================

### Enhancements

* Adjust communication with the web apps


3.0.1 Release notes (2021-01-21)
=============================================================

### Enhancements

* Add possibility to attach additional query params to all requests
* Extend documentation for custom drawer and add an example to the app


3.0.0 Release notes (2021-01-15)
=============================================================

### Breaking Changes

* Introduce new setup method for PACECloudSDK

### Enhancements

* Remove old cloud SDK and use the generated apps endpoints for the requests
* Extend the documentation with authorization via Activity Result API
* Return app object in onOpen callback
* Use manifest description instead of shortName for the first line of the app drawer

### Fixes

* Fix bug where location LiveData was emitted before location was updated


2.0.5 Release notes (2020-12-18)
=============================================================

### Enhancements

* Add user info endpoint
* Add method to get cached access token
* Add generated POI API models and requests


2.0.4 Release notes (2020-12-10)
=============================================================

### Enhancements

* Add Pay service API

### Fixes

* Fix fetching of web app manifest's URL


2.0.3 Release notes (2020-12-03)
=============================================================

### Enhancements

* Make automatic closing of the AppActivity optional
* Implement `verifyLocation` check

### Fixes

* Fix outdated location state LiveData values


2.0.2 Release notes (2020-11-30)
=============================================================

### Fixes

* Fix deep linking

### Internal

* Set version and build number based on commit tag


2.0.1 Release notes (2020-11-25)
=============================================================

### Enhancements

* Remove device ID from request headers
* Remove device ID and API key from user agent
* Remove fetching apps by app ID


2.0.0 Release notes (2020-11-17)
=============================================================

* Initial PACE Cloud SDK replacing the old Cloud SDK
