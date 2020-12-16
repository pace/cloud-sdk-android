# PACE Cloud SDK

## Setup

Add JCenter to your top-level `build.gradle` (if not yet):

```groovy
allprojects {
    repositories {
        ...
        jcenter()
    }
}
```

Add the **PACE Cloud SDK** dependency to your module's `build.gradle`:

```groovy
dependencies {
    ...
    implementation "cloud.pace:sdk:$pace_cloud_sdk_version"
}
```

Because the PACE Cloud SDK uses [AppAuth for Android](https://github.com/openid/AppAuth-Android) for the IDKit, the AppAuth redirect scheme must be registered in your app's `build.gradle` file:
```groovy
android {
    ...
    defaultConfig {
        ...
        manifestPlaceholders = ['appAuthRedirectScheme': 'YOUR_REDIRECT_URI_SCHEME_OR_EMPTY']
    }
    ...
}
```

## IDKit
This framework manages the OpenID (OID) authorization and the general session flow with its token handling.

### Setup
This code example shows how to setup IDKit. The parameter `additionalCaching` defines if IDKit persists the session additionally to the native WebView/Browser caching.
```kotlin
val config = OIDConfiguration(
    authorizationEndpoint, 
    tokenEndpoint,
    userInfoEndpoint, // optional
    clientId, 
    clientSecret, // optional
    scopes, // optional
    redirectUri,
    responseType, // Default `ResponseTypeValues.CODE`
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
The IDKit offers a function to discover the authorization service configuration by an issuer URI. For example:

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

##### 1. Starting the authorization intent via `startActivityForResult` and handle the response in `onActivityResult`:

```kotlin
if (IDKit.isAuthorizationValid()) {
    // No login required
} else {
    startActivityForResult(IDKit.authorize(), REQUEST_CODE)
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

##### 2. Using `PendingIntent` and providing completion and cancelation handling activities:

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

### Reset session
Resetting the current session works as follows:
```kotlin
IDKit.resetSession()
```
A new authorization will be required afterwards.

# AppKit

## Main features

* Get apps by location or URL
* Shows an `AppDrawer` for each app
* Opens the app in the `AppActivity` (recommended) or `AppWebView`
* Checks if there is an app for the given POI ID at the current location

## Usage

### Setup

This code example shows how to setup AppKit. This can be done in the `Application` class.
```kotlin
val config = Configuration(
    clientAppName = "PACECloudSDKExample",
    clientAppVersion = BuildConfig.VERSION_NAME,
    clientAppBuild = BuildConfig.VERSION_CODE.toString(),
    apiKey = "YOUR_API_KEY",
    isDarkTheme = false,
    environment = Environment.DEVELOPMENT
)
AppKit.setup(context, config)
```

### Requirements

Please make sure that the user grants the following permission.

* `Manifest.permission.ACCESS_FINE_LOCATION`

**_Note:_** AppKit needs this permission to get the user location but it will not request the permission.

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

You can also fetch apps by URL and references (e.g. gas station references).

**_Note:_** The reference starts with a specific namespace identifier followed by the gas station ID in this case it has to conform to the URN format.

##### Kotlin example

```kotlin
AppKit.fetchAppsByUrl(url, "prn:poi:gas-stations:c977190d-049b-4023-bcbd-1dab88f02924", "prn:poi:gas-stations:f86a1818-353c-425f-a92b-705cc6ec5259") {
    when (it) {
        is Success -> // `it.result` contains the app objects
        is Failure -> // `it.throwable` contains the Throwable of the failed request
    }
}
```

##### Java example

```java
AppKit.INSTANCE.fetchAppsByUrl(url, new String[]{"prn:poi:gas-stations:c977190d-049b-4023-bcbd-1dab88f02924", "prn:poi:gas-stations:f86a1818-353c-425f-a92b-705cc6ec5259"}, result -> {
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

The AppKit contains a default `Activity` which can be used to display an app. To open an app in the `AppActivity` you have to call `AppKit.openAppActivity(...)`. The `AppActivity` will be closed when the user clicks the close button in the app.

### AppWebView

Moreover the AppKit contains a default `AppWebView`. To display an app in this WebView you have to call `AppWebView.loadApp(parent: Fragment, url: String)`. The `AppWebView`s parent needs to be a fragment as that's the needed context for the integrated `Android Biometric API`.

### AppDrawer

The `AppDrawer` is an expandable button that can be used to display a AppDrawer. It shows an icon in collapsed state and additionally a title and subtitle in expanded state. By default it will be opened in expanded mode. To fade in the button with an animation, use `appDrawer.show()`. The AppDrawer will be removed if the App is not returned on the next App check again.

**_Note:_** You need to call `AppKit.requestLocalApps(...)` periodically to make sure that Apps get closed when they are not longer available. This can be done when the app comes into the foreground or the location changes.

The button can be added dynamically by the AppKit or statically as view.

##### Dynamic example

The following code shows an example of how `AppDrawer` can be displayed dynamically. You can pass an optional `AppCallbackImpl` to communicate with the app.
```kotlin
AppKit.openApps(context, apps, isDarkBackground, parentLayout, callback = object : AppCallbackImpl() {
    override fun onOpen() {
        // AppDrawer clicked
    }
})
```

The AppKit will now create an `AppDrawer` for each app in `apps` and add it to the given `parentLayout`.
Clicking on the button opens the `AppActivity` with the app in the `AppWebView`.

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
        AppKit.openAppActivity(context, app.appUrl)
    }

    app_drawer.show()
}
```

### Deep Linking

Some of our services (e.g. PayPal) do not open the URL in the WebView, but in a Chrome Custom Tab within the app. After completion of the process the user is redirected back to the WebView via a Deep Link. In order to set the redirect URL correctly and to ensure that the client app intercepts the Deep Link, the following requirements must be met:

* Set `clientId` in AppKit configuration in `setup(...)`, because it is needed for the redirect URL
* Specify the `AppActivity` as Deep Link intent filter in your app manifest. **`pace.${clientId}` (same `clientId` as passed in the AppKit configuration) must be passed to `android:scheme`:**
* If the scheme is not set, the `AppKit` calls the `onCustomSchemeError(context: Context?, scheme: String)` callback


```xml
<activity
    android:name="cloud.pace.sdk.appkit.app.AppActivity"
    android:launchMode="singleTop"
    android:screenOrientation="portrait"
    android:theme="@style/AppKitTheme">
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />

        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />

        <data
            android:host="redirect"
            android:scheme="pace.$clientId" />
    </intent-filter>
</activity>
```

### Native login communication

If the client app uses its own login and wants to pass an access token to the apps, follow these steps:

1. Initialize the `AppKit` with `authenticationMode = AuthenticationMode.NATIVE` and an optional start `accessToken = "Your access token"`.
2. Pass an `AppCallbackImpl` instance to `AppKit.openApps(...)` or `AppKit.openAppActivity(...)` and override the required callbacks (`onTokenInvalid(onResult: (String) -> Unit) {}` in this case)
3. If the access token is invalid, the `AppKit` calls the `onTokenInvalid` function. The client app needs to call the `onResult` function to set a new access token. In case that you can't retrieve a new valid token, don't call `onResult`, otherwise you will most likely end up
in an endless loop. Make sure to clean up all the app related views as well (see [Removal of Apps](#removal-of-apps)).

##### Kotlin example

```kotlin
AppKit.openAppActivity(context, url, false, object : AppCallbackImpl() {
    override fun onTokenInvalid(onResult: (String) -> Unit) {
        // Token is invalid, call your function to request a new one (async or not)
        // and pass the new token to onResult
        getTokenAsnyc { token ->
            onResult(token)
        }      
    }
}
```

##### Java example

```java
AppKit.INSTANCE.openAppActivity(context, url, false, new AppCallbackImpl() {
    @Override
    public void onTokenInvalid(@NotNull Function1<? super String, Unit> onResult) {
        // Token is invalid, call your function to request a new one (async or not)
        // and pass the new token to onResult
        getTokenAsnyc( token -> {
            onResult.invoke(token);
        });
    }
});
```

### Removal of Apps

In case you want to remove the `AppActivity`, simply call `AppKit.closeAppActivity()`.

If you want to remove all `AppDrawer`s *and* the `AppActivity` (only if it was started with autoClose = true), you can call the `AppKit.closeApps(buttonContainer: ConstraintLayout)` method and pass your `ConstraintLayout` where you've added the `AppDrawer`s to (see [AppDrawer](#appdrawer)).
