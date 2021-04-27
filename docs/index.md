# PACE Cloud SDK – Android

This framework combines multipe functionalities provided by PACE i.e. authorizing via **PACE ID** or requesting and displaying **Apps**. These functionalities are separated and structured into different ***Kits*** by namespaces, i.e. [IDKit](#idkit), [AppKit](#appkit).

- [PACE Cloud SDK](#pace-cloud-sdk)
    * [Source code](#source-code)
    * [Specifications](#specifications)
    * [Installation](#installation)
        + [Maven Central](#maven-central)
    * [Setup](#setup)
    * [Migration](#migration)
        + [2.x.x -> 3.x.x](#from-2xx-to-3xx)
    * [IDKit](#idkit)
        + [Setup](#setup-1)
        + [Discover configuration](#discover-configuration)
        + [Authorization](#authorization)
        + [Access user information](#access-user-information)
        + [Token refresh](#token-refresh)
        + [Cached token](#cached-token)
        + [Check intent](#check-intent)
        + [End session](#end-session)
        + [2FA setup](#2fa-setup)
            * [Mail-OTP](#mail-otp)
            * [Biometry](#biometry)
            * [PIN](#pin)
    * [AppKit](#appkit)
        + [Main Features](#main-features)
        + [Setup](#setup-2)
            + [Permission](#permission)
            + [Biometric authentication](#biometric-authentication)
        + [Request local apps](#request-local-apps)
        + [Fetch apps by URL](#fetch-apps-by-url)
        + [Is POI in range?](#is-poi-in-range)
        + [AppActivity](#appactivity)
        + [AppWebView](#appwebview)
        + [AppDrawer](#appdrawer)
            + [Default AppDrawer](#default-appdrawer)
            + [Custom AppDrawer](#custom-appdrawer)
        + [Deep Linking](#deep-linking)
        + [Native login](#native-login)
        + [Removal of Apps](#removal-of-apps)
        + [Miscellaneous](#miscellaneous)
            + [Preset Urls](#preset-urls)
            + [Logging](#logging)

## Source code
The complete source code of the SDK can be found on [GitHub](https://github.com/pace/cloud-sdk-android).

## Specifications
`PACECloudSDK` currently supports Android 6.0 (API level 23) and above.

## Installation
You can get the `PACECloudSDK` from **one** of the following repositories.

### Maven Central
Add Maven Central to your top-level `build.gradle` (if not yet):
```groovy
allprojects {
    repositories {
        ...
        mavenCentral()
    }
}
```

Add the `PACECloudSDK` dependency to your module's `build.gradle`:
```groovy
dependencies {
    ...
    implementation "cloud.pace:sdk:$pace_cloud_sdk_version"
}
```

## Setup
The `PACECloudSDK` needs to be setup before any of its `Kits` can be used. Therefore you *must* call `PACECloudSDK.setup(context: Context, configuration: Configuration)`. The best way to do this is inside your `Application` class. It will automatically authorize your application with the provided api key.

The `Configuration` only has `clientAppName`, `clientAppVersion`, `clientAppBuild` and `apiKey` as mandatory properties. All others are optional and can be passed as necessary.

**Note**: `PACECloudSDK` is using the `PRODUCTION` environment as default. In case you are still doing tests, you probably want to change it to `SANDBOX` or `STAGING`.

Available parameters:

```kotlin
clientAppName: String
clientAppVersion: String
clientAppBuild: String
apiKey: String
authenticationMode: AuthenticationMode // Default: AuthenticationMode.WEB
environment: Environment // Default: Environment.PRODUCTION
extensions: List<String> // Default: emptyList()
domainACL: List<String> // Default: emptyList()
locationAccuracy: Int? // Default: null
speedThresholdInKmPerHour: Int // Default: 50
geoAppsScope: String // Default: "pace"
```

PACE Cloud SDK uses [AppAuth for Android](https://github.com/openid/AppAuth-Android) for the native authentication in *IDKit*, which needs `appAuthRedirectScheme` manifest placeholder to be set. PACE Cloud SDK requires `pace_redirect_scheme` for [Deep Linking](#deep-linking) to be set. Both these manifest placeholder must be configured in your app's `build.gradle` file. In case you won't be using native login, you can set an empty string for `appAuthRedirectScheme`.

For the `pace_redirect_scheme` we recommend to use the following pattern to prevent collisions with other apps that might be using PACE Cloud SDK: `pace.$UUID`, where `$UUID` can be any UUID of your choice:
```groovy
android {
    ...
    defaultConfig {
        ...
        manifestPlaceholders = [
            'appAuthRedirectScheme': 'YOUR_REDIRECT_URI_SCHEME_OR_EMPTY', // e.g. reverse domain name notation: cloud.pace.app
            'pace_redirect_scheme': 'YOUR_REDIRECT_SCHEME_OR_EMPTY'] // e.g. pace.ad50262a-9c88-4a5f-bc55-00dc31b81e5a
    }
    ...
}
```

## Migration
### From 2.x.x to 3.x.x
In `3.0.0` we've introduced a universal setup method: `PACECloudSDK.setup(context: Context, configuration: Configuration)` and removed the setup for `AppKit` and `POIKit`.

The universal `Configuration` almost has the same signature as the previous *AppKit* `Configuration`, only the `isDarkTheme` parameter has been removed, which is now an enum instead of a Boolean and defaults to `Theme.LIGHT`. In case you want to change it, you can set it via `AppKit`'s `theme` property: `AppKit.theme = Theme.LIGHT/Theme.DARK`.

## IDKit
**IDKit** manages the OpenID (OID) authorization and the general session flow with its token handling via **PACE ID**.

### Setup
This code example shows how to setup *IDKit*. The parameter `additionalCaching` defines if *IDKit* persists the session additionally to the native WebView/Browser caching.
```kotlin
val config = OIDConfiguration(
    authorizationEndpoint,
    endSessionEndpoint,
    tokenEndpoint,
    userInfoEndpoint, // optional
    clientSecret, // optional
    scopes, // optional
    redirectUri,
    responseType, // Default: `ResponseTypeValues.CODE`
    additionalParameters // optional
)
IDKit.setup(context, config, true)
```

Once the authorization flow is completed in the browser, the authorization service will redirect to a URI specified as part of the authorization request.
In order for your app to capture this response, you must register the **redirect URI scheme** in your app's `build.gradle` as described above.

From Android API 30 (R) and above, set queries in the your app's manifest, to enable AppAuth searching for usable installed browsers:
```xml
<queries>
    <intent>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data android:scheme="https" />
    </intent>
    <intent>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.APP_BROWSER" />
        <data android:scheme="https" />
    </intent>
</queries>
```

### Discover configuration
The *IDKit* offers a function to discover the authorization service configuration by an issuer URI. For example:

```kotlin
IDKit.discoverConfiguration(issuerUri) {
    when (it) {
        is Success -> // it.result contains configuration
        is Failure -> // it.throwable contains error
    }
}
```

### Authorization
The authorization request to the authorization service can be dispatched using **one** of the following two approaches:

#### 1. Getting result from an activity

##### a. Using `Activity Result API` and a `StartActivityForResult` contract and handle the result in `ActivityResultCallback` inline:

```kotlin
val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
    if (result.resultCode == Activity.RESULT_OK) {
        val intent = result.data
        if (intent != null) {
            // Pass result to IDKit to retrieve the access token
            IDKit.handleAuthorizationResponse(intent) {
                when (it) {
                    is Success -> // it.result contains accessToken
                    is Failure -> // it.throwable contains error
                }
            }
        }
    }
}

login_button.setOnClickListener {
    if (IDKit.isAuthorizationValid()) {
        // No login required
    } else {
        startForResult.launch(IDKit.authorize())
    }
}
```

##### b. Using `Activity.startActivityForResult` and handle the response in `Activity.onActivityResult` callback:

```kotlin
login_button.setOnClickListener {
    if (IDKit.isAuthorizationValid()) {
        // No login required
    } else {
        startActivityForResult(IDKit.authorize(), REQUEST_CODE)
    }
}
```
Upon completion of this authorization request, `onActivityResult` will be invoked with the authorization result intent.

Call the following function when an intent is passed to `onActivityResult` from [Chrome Custom Tab](https://developer.chrome.com/multidevice/android/customtabs) to retrieve the access token:
```kotlin
IDKit.handleAuthorizationResponse(intent) {
    when (it) {
        is Success -> // it.result contains accessToken
        is Failure -> // it.throwable contains error
    }
}
```

#### 2. Using `PendingIntent` and providing completion and cancellation handling activities:

```kotlin
if (IDKit.isAuthorizationValid()) {
    // No login required
} else {
    IDKit.authorize(completedActivity, canceledActivity)
}
```
Upon completion of this authorization request, a `PendingIntent` of the `completedActivity` will be invoked.
If the user cancels the authorization request, a `PendingIntent` of the `canceledActivity` will be invoked.

Call the following function when an `intent` is passed to the `completedActivity` or `canceledActivity` from [Chrome Custom Tab](https://developer.chrome.com/multidevice/android/customtabs) to retrieve the access token:
```kotlin
IDKit.handleAuthorizationResponse(intent) {
    when (it) {
        is Success -> // it.result contains accessToken
        is Failure -> // it.throwable contains error
    }
}
```

### Access user information
In case you want to fetch the current access token's user info, you need to make sure to set the `userInfoEndpoint` during the [setup](#setup), and then you can call:
```kotlin
IDKit.userInfo(accessToken = it) {
    when (it) {
        is Success -> // it.result contains UserInfoResponse object
        is Failure -> // it.throwable contains error
    }
}
```

### Token refresh
If you prefer to refresh the access token of your current session manually call:
```kotlin
IDKit.refreshToken(force) {
    when (it) {
        is Success -> // it.result contains accessToken
        is Failure -> // it.throwable contains error
    }
}
```
If the refresh attempt fails an error will be returned.

### Cached token
You can get the cached (last refreshed) access token (nullable) as follows:
```kotlin
IDKit.cachedToken()
```

### Check intent
You can use the following functions to check if your intent contains an authorization response, end session response or an authorization/end session exception.
```kotlin
fun containsAuthorizationResponse(intent: Intent)

fun containsEndSessionResponse(intent: Intent)

fun containsException(intent: Intent)
```

### End session
The end session request works the same way as the authorization request. It can also be performed using **one** of the following two approaches (a new authorization will be required afterwards):

#### 1. Getting result from an activity

##### a. Using `Activity Result API` and a `StartActivityForResult` contract and handle the result in `ActivityResultCallback` inline:

```kotlin
val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
    if (result.resultCode == Activity.RESULT_OK) {
        val intent = result.data
        if (intent != null) {
            // Pass result to IDKit to reset the session
            IDKit.handleEndSessionResponse(intent) {
                when (it) {
                    is Success -> // it.result contains Unit (success)
                    is Failure -> // it.throwable contains error
                }
            }
        }
    }
}

logout_button.setOnClickListener {
    startForResult.launch(IDKit.endSession())
}
```

##### b. Using `Activity.startActivityForResult` and handle the response in `Activity.onActivityResult` callback:

```kotlin
logout_button.setOnClickListener {
    startActivityForResult(IDKit.endSession(), REQUEST_CODE)
}
```
Upon completion of this end session request, `onActivityResult` will be invoked with the end session result intent.

Call the following function when an intent is passed to `onActivityResult` from [Chrome Custom Tab](https://developer.chrome.com/multidevice/android/customtabs) to reset the session:
```kotlin
IDKit.handleEndSessionResponse(intent) {
    when (it) {
        is Success -> // it.result contains Unit (success)
        is Failure -> // it.throwable contains error
    }
}
```

#### 2. Using `PendingIntent` and providing completion and cancellation handling activities:

```kotlin
IDKit.endSession(completedActivity, canceledActivity)
```
Upon completion of this end session request, a `PendingIntent` of the `completedActivity` will be invoked.
If the user cancels the end session request, a `PendingIntent` of the `canceledActivity` will be invoked.

Call the following function when an `intent` is passed to the `completedActivity` or `canceledActivity` from [Chrome Custom Tab](https://developer.chrome.com/multidevice/android/customtabs) to reset the session:
```kotlin
IDKit.handleEndSessionResponse(intent) {
    when (it) {
        is Success -> // it.result contains Unit (success)
        is Failure -> // it.throwable contains error
    }
}
```

### 2FA setup
In numerous cases a second authentication factor is required when using Connected Fueling, e.g. when authorizing a payment. Following are methods that can be used to setup biometric authentication on the user's device or setup an account PIN.

In order to prevent websites from accessing your TOTP secrets (used when biometric authentication is used), a domain access control list has to be passed to the `domainACL` property of the `Configuration` object in the [setup phase](#setup). If you're not using a custom PWA, then setting the `domainACL` to `"pace.cloud"` is enough.

#### Mail-OTP
For some of the below mentioned methods an OTP is needed, which can be requested to be sent to the user's email via

```kotlin
IDKit.sendMailOTP(completion)
```

#### Biometry
The `PACECloudSDK` provides the following methods to enable and disable biometric authentication:

* Check if biometric authentication has been enabled on the device:

	```kotlin
	IDKit.isBiometricAuthenticationEnabled()
	```

* Enable biometric authentication with the user PIN

	```kotlin
	IDKit.enableBiometricAuthenticationWithPIN(pin, completion)
	```

* Enable biometric authentication with the user password:

	```kotlin
	IDKit.enableBiometricAuthenticationWithPassword(password, completion)
	```

* Enable biometric authentication with an OTP previously sent by mail (see [OTP](#mail-otp))

	```kotlin
	IDKit.enableBiometricAuthenticationWithOTP(otp, completion)
	```

* Disable biometric authentication on the device

	```kotlin
	IDKit.disableBiometricAuthentication()
	```

#### PIN
The `PACECloudSDK` provides the following methods to check and set the PIN:

* Check if the user PIN has been set

	```kotlin
	IDKit.isPINSet(completion)
	```

* Check if the user password has been set

	```kotlin
	IDKit.isPasswordSet(completion)
	```

* Check if the user PIN or password has been set

	```kotlin
	IDKit.isPINOrPasswordSet(completion)
	```

* Set the user PIN and authorize with biometry

	```kotlin
	IDKit.setPINWithBiometry(...)
	```

* Set the user PIN and authorize with the user password

	```kotlin
	IDKit.setPINWithPassword(pin, password, completion)
	```

* Set the user PIN and authorize with an OTP previously sent by mail (see [OTP](#mail-otp))

	```kotlin
	IDKit.setPINWithOTP(pin, otp, completion)
	```

## AppKit
### Main features
* Get apps at the current location or by URL
* Shows an [AppDrawer](#appdrawer) for each app
* Opens the app in the [AppActivity](#appactivity) (recommended) or [AppWebView](#appwebview)
* Checks if there is an app for the given POI ID at the current location

### Setup

#### Permission
Please make sure that the user grants the following permission:
* `Manifest.permission.ACCESS_FINE_LOCATION`

**_Note:_** *AppKit* needs this permission to get the user location but it will not request the permission.

#### Biometric authentication
To be able to authorize payments with biometry, the `domainACL` must be set during the [setup](#setup) of the `PACECloudSDK`.
The `domainACL` (domain access control list) is a list of domains which should be allowed to use biometric authentication.

### Request local apps
Location based apps can be requested with the following function. The completion contains a list with the available apps as `App` objects or a `Throwable`, if an error occurs.

##### Kotlin example
```kotlin
AppKit.requestLocalApps {
    when (it) {
        is Success -> // `it.result` contains the app objects
        is Failure -> // `it.throwable` contains the Throwable of the failed request
    }
}
```

##### Java example
```java
AppKit.INSTANCE.requestLocalApps(result -> {
    if (result instanceof Success) {
        List<App> apps = ((Success<List<App>>) result).getResult();
        // `Apps` contains the app objects
    } else {
        Throwable throwable = ((Failure<List<App>>) result).getThrowable();
        // `throwable` contains the Throwable of the failed request
    }

    return null;
});
```

### Fetch apps by URL
You can also fetch apps by URL and references (e.g. referenced gas station UUIDs).

##### Kotlin example
```kotlin
AppKit.fetchAppsByUrl(url, "c977190d-049b-4023-bcbd-1dab88f02924", "f86a1818-353c-425f-a92b-705cc6ec5259") {
    when (it) {
        is Success -> // `it.result` contains the app objects
        is Failure -> // `it.throwable` contains the Throwable of the failed request
    }
}
```

##### Java example
```java
AppKit.INSTANCE.fetchAppsByUrl(url, new String[]{"c977190d-049b-4023-bcbd-1dab88f02924", "f86a1818-353c-425f-a92b-705cc6ec5259"}, result -> {
    if (result instanceof Success) {
        List<App> apps = ((Success<List<App>>) result).getResult();
        // `Apps` contains the app objects
    } else {
        Throwable throwable = ((Failure<List<App>>) result).getThrowable();
        // `throwable` contains the Throwable of the failed request
    }

    return null;
});
```

### Is POI in range?
To check if there is an app for the given POI ID at the current location, call `AppKit.isPoiInRange(...)`.

##### Kotlin example
```kotlin
AppKit.isPoiInRange(poiId) {
    // True or false
}
```

##### Java example
```java
AppKit.isPoiInRange(poiId, result -> {
    // True or false

    return null;
});
```

### AppActivity
The *AppKit* contains a default `Activity` which can be used to display an app. To open an app in the `AppActivity` you have to call `AppKit.openAppActivity(...)`. The `AppActivity` will be closed when the user clicks the close button in the app. You may also use a `presetUrl` (see [Preset Urls](#preset-urls)).

### AppWebView
Moreover the *AppKit* contains a default `AppWebView`. To display an app in this WebView you have to call `AppWebView.loadApp(parent: Fragment, url: String)`. The `AppWebView`s parent needs to be a fragment as that's the needed context for the integrated `Android Biometric API`.

### AppDrawer
#### Default AppDrawer
The default `AppDrawer` is an expandable button that can be used to display an app. It shows an icon in collapsed state and additionally a title and subtitle in expanded state. By default it will be opened in expanded mode. To fade in the button with an animation, use `appDrawer.show()`. The `AppDrawer` will be removed if the app is not returned on the next App check again.

**_Note:_** You need to call `AppKit.requestLocalApps(...)` periodically to make sure that Apps get closed when they are not longer available. This can be done when the app comes into the foreground or the location changes.

The button can be added dynamically by the *AppKit* or statically as view.

##### Dynamic example
The following code shows an example of how `AppDrawer` can be displayed dynamically. You can pass an optional `AppCallbackImpl` to communicate with the app.
```kotlin
AppKit.openApps(context, apps, parentLayout, callback = object : AppCallbackImpl() {
    override fun onOpen(app: App?) {
        // AppDrawer clicked
    }
    // Override more needed callbacks
})
```

The *AppKit* will now create an `AppDrawer` for each app in `apps` and add it to the given `parentLayout`.
Clicking on the button opens the [AppActivity](#appactivity) with the app in the [AppWebView](#appwebview).

##### Static example
```xml
<cloud.pace.sdk.appkit.app.drawer.AppDrawer
    android:id="@+id/app_drawer"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:visibility="gone"
    app:icon="@drawable/ic_app_drawer"
    app:iconTint="@color/colorAccent"
    app:layout_constraintBottom_toBottomOf="parent"
    app:layout_constraintEnd_toEndOf="parent"
    app:subtitle="PACE Connected Fueling"
    app:title="Pay here at the pump" />
```

```kotlin
fun showAppDrawer(app: App) {
    app_drawer.setApp(app, isDarkBackground) {
        AppKit.openAppActivity(context, app)
    }

    app_drawer.show()
}
```

#### Custom AppDrawer
If you don't want to use the [default AppDrawer](#default-appdrawer), you can also create your own app drawer/button. To create a custom app drawer/button you need the `App` object that you can request from the *AppKit* using the `AppKit.requestLocalApps()`, `AppKit.requestApps()` or `AppKit.fetchAppsByUrl(...)` methods. All texts are returned in the system language, if available. The app object consists of the following properties:
```kotlin
name: String // e.g. "PACE Connected Fueling"
shortName: String // e.g. "Connected Fueling"
description: String? // e.g. "Pay at the pump"
url: String // App URL
logo: Bitmap? // App logo e.g. from the gas station
iconBackgroundColor: String? // App icon/logo background color e.g. #57C2E4
textBackgroundColor: String? // App background color e.g. #222424
textColor: String? // App text color e.g. #121414
display: String? // Not relevant
gasStationId: String? // Referenced gas station ID, if available
```

You can now display this data in your own views, e.g. your app drawer/button consists of a **title**, **description** and **icon** as in the following XML layout:
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/custom_app_button"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginTop="8dp"
    android:layout_marginBottom="8dp"
    android:clickable="true"
    android:focusable="true"
    android:foreground="?attr/selectableItemBackground"
    android:padding="16dp"
    tools:background="@android:color/black">

    <TextView
        android:id="@+id/name"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_marginEnd="10dp"
        android:textAppearance="@style/TextAppearance.AppCompat.Body1"
        android:textColor="@android:color/white"
        app:layout_constraintEnd_toStartOf="@id/icon"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent"
        tools:text="PACE Connected Fueling" />

    <TextView
        android:id="@+id/description"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_marginTop="5dp"
        android:layout_marginEnd="10dp"
        android:textAppearance="@style/TextAppearance.AppCompat.Medium"
        android:textColor="@android:color/white"
        app:layout_constraintEnd_toStartOf="@id/icon"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toBottomOf="@id/name"
        tools:text="Pay at the pump" />

    <ImageView
        android:id="@+id/icon"
        android:layout_width="64dp"
        android:layout_height="0dp"
        android:src="@drawable/ic_default"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintTop_toTopOf="parent" />

</androidx.constraintlayout.widget.ConstraintLayout>
```
The texts and the icon of the above views can now be set programmatically. When clicking the app drawer/button the app is opened in the [AppActivity](#appactivity) via `AppKit.openAppActivity(...)`:
```kotlin
AppKit.requestLocalApps { app ->
    name.text = app.name
    description.text = app.description
    app.textColor?.let {
        val textColor = Color.parseColor(it) // Parses hex color string to color int
        name.setTextColor(textColor)
        description.setTextColor(textColor)
    }

    app.logo?.let { icon.setImageBitmap(it) }
    app.iconBackgroundColor?.let { icon.setBackgroundColor(Color.parseColor(it)) }
    app.textBackgroundColor?.let { itemView.setBackgroundColor(Color.parseColor(it)) }

    custom_app_button.setOnClickListener {
        AppKit.openAppActivity(context, app, autoClose = false, callback = yourAppCallback)
    }
}
```
**Note**: For a more detailed example, where the apps are displayed in a `RecyclerView`, see the `PACECloudSDK` example app.

### Deep Linking
Some of our services (e.g. `PayPal`) do not open the URL in the WebView, but in a Chrome Custom Tab within the app, due to security reasons. After completion of the process the user is redirected back to the WebView via deep linking. In order to set the redirect URL correctly and to ensure that the client app intercepts the deep link, the following requirements must be met:

* Specify the `pace_redirect_scheme` as manifest placeholder in your app's `build.gradle` file (see [setup](#setup))
* If the scheme is empty, the *AppKit* calls the `onCustomSchemeError(context: Context?, scheme: String)` callback

### Native login
If the client app uses its own login and wants to pass an access token to the apps, follow these steps:

1. Initialize the `PACECloudSDK` with `authenticationMode = AuthenticationMode.NATIVE`
2. Pass an `AppCallbackImpl` instance to `AppKit.openApps(...)` or `AppKit.openAppActivity(...)` and override the required callbacks (`onTokenInvalid(reason: InvalidTokenReason, oldToken: String?, onResult: (String) -> Unit)` in this case)
3. If the access token is invalid, the *AppKit* calls the `onTokenInvalid` function. The client app needs to call the `onResult` function to set a new access token. In case that you can't retrieve a new valid token, don't call `onResult`, otherwise you will most likely end up
in an endless loop. Make sure to clean up all the app related views as well (see [Removal of Apps](#removal-of-apps)).

##### Kotlin example
```kotlin
AppKit.openAppActivity(context, url, object : AppCallbackImpl() {
    override fun onTokenInvalid(reason: InvalidTokenReason, oldToken: String?, onResult: (String) -> Unit) {
        // Token is invalid, check reason and oldToken parameters
        // Call your function to request a new one (async or not) and pass the new token to onResult
        getTokenAsnyc { token ->
            onResult(token)
        }
    }
}
```

##### Java example
```java
AppKit.INSTANCE.openAppActivity(context, url, true, false, new AppCallbackImpl() {
    @Override
    public void onTokenInvalid(@NotNull InvalidTokenReason reason, @Nullable String oldToken, @NotNull Function1<? super String, Unit> onResult) {
        // Token is invalid, check reason and oldToken parameters
        // Call your function to request a new one (async or not) and pass the new token to onResult
        getTokenAsnyc( token -> {
            onResult.invoke(token);
        });
    }
});
```

### Removal of Apps
In case you want to remove the [AppActivity](#appactivity), simply call `AppKit.closeAppActivity()`.

If you want to remove all [AppDrawers](#appdrawer) *and* the [AppActivity](#appactivity) (only if it was started with `autoClose = true`), you can call the `AppKit.closeApps(buttonContainer: ConstraintLayout)` method and pass your `ConstraintLayout` where you've added the `AppDrawer`s to (see [AppDrawer](#appdrawer)).

### Miscellaneous
#### Preset Urls
`PACECloudSDK` provides preset urls for the most common apps, such as `PACE ID`, `payment`, `fueling`  and `transactions` based on the environment the sdk was initialized with. You may access these urls via the enum `Environment.kt`.

### Logging 
Besides the own logs of the SDK's kits, the `AppWebView` also intercepts the logs of their loaded apps. You may retrieve all of the mentioned logs as shown in the following code example:
```kotlin
PACECloudSDK.isLoggingEnabled = true // Defaults to `false`
PACECloudSDK.setLoggingListener {
    // it = log message
}
```

## SDK API Docs

Here is a complete list of all our SDK API documentations:

- [latest](/latest/-p-a-c-e-cloud-s-d-k/index.html) – the current `master`
- [3.0.1](/3.0.1/-p-a-c-e-cloud-s-d-k/index.html)
