# PACE Cloud SDK – Android

This framework combines multipe functionalities provided by PACE i.e. authorizing via **PACE ID** or requesting and displaying **Apps**. These functionalities are separated and structured into different ***Kits*** by namespaces, i.e. [IDKit](#idkit), [AppKit](#appkit) and [POIKit](#poikit).

- [PACE Cloud SDK](#pace-cloud-sdk)
    * [Documentation](#documentation)
    * [Source code](#source-code)
    * [Specifications](#specifications
    * [Migration](#migration)
        + [2.x.x -> 3.x.x](#from-2xx-to-3xx)
        + [7.x.x -> 8.x.x](#from-7xx-to-8xx)
        + [9.x.x -> 10.x.x](#from-9xx-to-10xx)
        + [10.x.x -> 11.x.x](#from-10xx-to-11xx)
        + [11.x.x -> 12.x.x](#from-11xx-to-12xx)
        + [12.x.x -> 13.x.x](#from-12xx-to-13xx)
        + [13.x.x -> 14.x.x](#from-13xx-to-14xx)
        + [14.x.x -> 15.x.x](#from-14xx-to-15xx)
        + [15.x.x -> 16.x.x](#from-15xx-to-16xx)
        + [16.x.x -> 17.x.x](#from-16xx-to-17xx)
        + [17.x.x -> 18.x.x](#from-17xx-to-18xx)

## Documentation
The full documentation and instructions on how to integrate PACE Cloud SDK can be found [here](https://docs.pace.cloud/en/integrating/mobile-app)

## Source code
The complete source code of the SDK can be found on [GitHub](https://github.com/pace/cloud-sdk-android).

## Specifications
`PACECloudSDK` currently supports Android 6.0 (API level 23) and above.

It uses the following dependencies:

- [Kotlin Standard Library](https://kotlinlang.org/api/latest/jvm/stdlib/): The Kotlin Standard Library provides living essentials for everyday work with Kotlin.
- [Kotlinx Coroutines Android](https://github.com/Kotlin/kotlinx.coroutines#android): Provides Dispatchers.Main context for Android applications.
- [Android Core KTX](https://developer.android.com/kotlin/ktx#core): The Core KTX module provides extensions for common libraries that are part of the Android framework.
- [Android Appcompat](https://developer.android.com/jetpack/androidx/releases/appcompat): Allows access to new Android APIs on older Android devices.
- [Android Constraintlayout](https://developer.android.com/jetpack/androidx/releases/constraintlayout): Position and size widgets in a flexible way with relative positioning.
- [Android Preference KTX](https://developer.android.com/jetpack/androidx/releases/preference): Build interactive settings screens without needing to interact with device storage or manage the UI.
- [Android Fragment KTX](https://developer.android.com/kotlin/ktx#fragment): The Fragment KTX module provides a number of extensions to simplify the fragment API.
- [Android Lifecycle](https://developer.android.com/jetpack/androidx/releases/lifecycle): Lifecycle-aware components perform actions in response to a change in the lifecycle status of another component, such as activities and fragments.
- [Android Biometric](https://developer.android.com/jetpack/androidx/releases/biometric): Authenticate with biometrics or device credentials, and perform cryptographic operations.
- [Android Browser](https://developer.android.com/jetpack/androidx/releases/browser): This artifact is used for the Chrome Custom Tabs.
- [Android Room](https://developer.android.com/jetpack/androidx/releases/room): The Room persistence library provides an abstraction layer over SQLite to allow for more robust database access while harnessing the full power of SQLite.
- [Google Play Services Location](https://developers.google.com/android/reference/com/google/android/gms/location/package-summary): The location APIs available in Google Play services facilitate adding location awareness with automated location tracking, geofencing, and activity recognition.
- [Google Play Services Maps](https://developers.google.com/android/reference/com/google/android/gms/maps/package-summary): Contains the Google Maps SDK for Android.
- [Maps SDK for Android Utility Library](https://github.com/googlemaps/android-maps-utils): This open-source library contains utilities for Google Maps Android API.
- [Protocol Buffers](https://github.com/protocolbuffers/protobuf/tree/master/java): Protocol Buffers (a.k.a., protobuf) are Google's language-neutral, platform-neutral, extensible mechanism for serializing structured data.
- [Koin for Android](https://insert-koin.io/): A pragmatic lightweight dependency injection framework for Kotlin developers.
- [Retrofit](https://square.github.io/retrofit/): A type-safe HTTP client for Android and Java.
- [Retrofit Moshi Converter](https://github.com/square/retrofit/tree/master/retrofit-converters/moshi): A Converter which uses Moshi for serialization to and from JSON.
- [Retrofit Gson Converter](https://github.com/square/retrofit/tree/master/retrofit-converters/gson): A Converter which uses Gson for serialization to and from JSON.
- [Retrofit RxJava3 Adapter](https://github.com/square/retrofit/tree/master/retrofit-adapters/rxjava3): An Retrofit call adapter for adapting RxJava 3.x types.
- [OkHttp logging interceptor](https://github.com/square/okhttp/tree/master/okhttp-logging-interceptor): An OkHttp interceptor which logs HTTP request and response data.
- [Moshi](https://github.com/square/moshi): Moshi is a modern JSON library for Android and Java.
- [Moshi adapters](https://github.com/square/moshi/tree/master/adapters): Prebuilt Moshi JsonAdapters for various things, such as Rfc3339DateJsonAdapter for parsing java.util.Date objects.
- [moshi-jsonapi](https://github.com/kamikat/moshi-jsonapi): Java implementation of JSON:API specification v1.0 for Moshi.
- [Gson](https://github.com/google/gson): A Java serialization/deserialization library to convert Java Objects into JSON and back.
- [RxJava](https://github.com/ReactiveX/RxJava): RxJava is a Java VM implementation of Reactive Extensions: a library for composing asynchronous and event-based programs by using observable sequences.
- [RxAndroid](https://github.com/ReactiveX/RxAndroid): Android specific bindings for RxJava.
- [AppAuth](https://github.com/openid/AppAuth-Android): AppAuth for Android is a client SDK for communicating with OAuth 2.0 and OpenID Connect providers.
- [Kotlin One-Time Password](https://github.com/marcelkliemannel/kotlin-onetimepassword): A Kotlin one-time password library to generate "Google Authenticator", "Time-based One-time Password" (TOTP) and "HMAC-based One-time Password" (HOTP) codes based on RFC 4226 and 6238.
- [Timber](https://github.com/JakeWharton/timber): A logger with a small, extensible API which provides utility on top of Android's normal Log class.


## Migration
### From 2.x.x to 3.x.x
In `3.0.0` we've introduced a universal setup method: `PACECloudSDK.setup(context: Context, configuration: Configuration)` and removed the setup for `AppKit` and `POIKit`.

The universal `Configuration` almost has the same signature as the previous *AppKit* `Configuration`, only the `isDarkTheme` parameter has been removed, which is now an enum instead of a Boolean and defaults to `Theme.LIGHT`. In case you want to change it, you can set it via `AppKit`'s `theme` property: `AppKit.theme = Theme.LIGHT/Theme.DARK`.

### From 7.x.x to 8.x.x
We've added two new `AppCallback`s: `getAccessToken` and `logout`. The `getAccessToken` method replaces the `invalidToken` call. While its callback is equal to the `invalidToken` callback, we changed the response to be an object with an `accessToken` property and a new `isInitialToken` boolean.

The `logout` callback is used to handle the logout natively. Please refer to [Native login, token renewal and logout](#native-login-token-renewal-and-logout) for more information.

Also from now on this callback will only be sent if `IDKit` is not used/set up. If you're using `IDKit` the SDK will now first try to renew the session automatically. If the renewal fails there is a new `fun onSessionRenewalFailed(throwable: Throwable?, onResult: (String?) -> Unit)` AppCallback that you may implement to specify your own behavior to retrieve a new access token. To implement this method pass an `AppCallbackImpl` instance to `AppKit.openApps(...)` or `AppKit.openAppActivity(...)` and override `onSessionRenewalFailed`. If either this method is not implemented or you didn't pass an `AppCallbackImpl` instance at all the SDK will automatically perform an authorization hence showing a sign in mask for the user.

### From 9.x.x to 10.x.x
We've moved everything that belongs to the GeoAPI from `AppKit`/`API` to `POIKit`:
* The `AppKit.isPoiInRange(...)` call is now part of `POIKit`, available under `POIKit.isPoiInRange(...)`
* The two `AppKit.requestCofuGasStations(...)` calls are now part of `POIKit`, available under `POIKit.requestCofuGasStations(...)`
* In case of unresolved references: All classes inside package `cloud.pace.sdk.api.geo` and `cloud.pace.sdk.appkit.geo` were moved to package `cloud.pace.sdk.poikit.geo`

### From 10.x.x to 11.x.x
The `IDKit` setup has been combined with the general `PACECloudSDK` setup:
* `IDKit.setup(...)` is no longer accessible.
* An optional `OIDConfiguration` has been added to the `Configuration` class of the `PACECloudSDK`.
* If you do not pass an `OIDConfiguration`, the `IDKit` can not be used.
* If used, the `OIDConfiguration` needs to be initialized with at least the `clientId` and the `redirectUri` of your identity provider. You can also use the factory methods of the `OIDConfiguration` if you want to use another environment than production which is now set by default with the primary constructor.
* Please head over to [IDKit setup](#setup-1) to learn more about how to set up this functionality.

### From 11.x.x to 12.x.x
The `PACECloudSDK.setup()` has been simplified:
* An optional `CustomOIDConfiguration` has been added to the `Configuration` class of the `PACECloudSDK`.
* If you do not pass an `CustomOIDConfiguration`, the `IDKit` can not be used.
* If used, the `CustomOIDConfiguration` needs to be initialized with at least the `clientId` and the `redirectUri` of your identity provider. All other properties in `CustomOIDConfiguration` can be additionally set if e.g. own identity provider endpoints are wanted.
* Please head over to [IDKit setup](#setup-1) to learn more about how to set up this functionality.

### From 12.x.x to 13.x.x
The `connectedFuelingStatus` property of the `CofuGasStation` is now nullable so that the Cofu gas station is not skipped in the cache loading if this property is absent in the API response.
If you use the `CofuGasStation` object, keep in mind that the `connectedFuelingStatus` property can now be `null`.

### From 13.x.x to 14.x.x
The `GasStations` properties `paymentMethods`, `amenities`, `foods`, `loyaltyPrograms`, `postalServices`, `services`, `shopGoods` and `fuelType` will now be from type string instead of enums. The client app has to then manage these values itself.

### From 14.x.x to 15.x.x

- The `POIKit.getRegionalPrice(...)` function now returns `List<RegionalPrices>` instead of the `RegionalPrices` typealias, which was `List<RegionalPrice>` before. This means that the only difference now is that the `RegionalPrice` model is called `RegionalPrices` and the `RegionalPrices` typealias no longer exists. The properties of the new `RegionalPrices` model are the same as those of the old `RegionalPrice` model.
- The [POI GeoJSON API](https://api.pace.cloud/schema/2021-2/_poi-geojson.html) has been removed because it is an internal API. If you still need this API, please write us on GitHub: https://github.com/pace/cloud-sdk-android.
- The user info request has been revised:
  - You don't have to pass an access token to the `IDKit.userInfo` method anymore, because it is automatically added to the request by the IDKit.
  - You can now add optional additional headers and optional additional parameters to the user info request, just like with the other API requests.
  - The properties of the `UserInfoResponse` model have changed to `subject`, `zoneInfo`, `emailVerified`, `createdAt`, `locale` and `email`. The old properties are outdated.

### From 15.x.x to 16.x.x

- We've removed the `Environment.STAGE` environment completely. Please use `Environment.SANDBOX` during testing and `Environment.PRODUCTION` for everything else.

### From 16.x.x to 17.x.x

- Introduced a `NoSupportedBrowser` exception which will now be returned from all `IDKit.authorize(...)` and `IDKit.endSession(...)` calls instead of crashing the app with an `ActivityNotFoundException` if no supported browser is installed and enabled to handle the Custom Tab intent. In this case, we show a Toast prompting the user to enable Google Chrome.
- The suspendable calls `IDKit.authorize(...)` and `IDKit.endSession(...)` now return the `Completion` result instead of invoking a function parameter on result to make the calls synchronous.

### From 17.x.x to 18.x.x

- Upgraded dependencies for Android gradle plugin, Kotlin and Compose. Make sure client dependencies are compatible or update them.


## SDK API Docs

Here is a complete list of all our SDK API documentations:

- [latest](/latest/-p-a-c-e-cloud-s-d-k/index.html) – the current `master`
- [3.0.1](/3.0.1/-p-a-c-e-cloud-s-d-k/index.html)
