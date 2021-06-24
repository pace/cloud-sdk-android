x.y.z Release notes (yyyy-MM-dd)
=============================================================

<!-- ### Breaking Changes - Include, if needed -->
<!-- ### Enhancements - Include, if needed -->
<!-- ### Fixes - Include, if needed -->
<!-- ### Internal - Include, if needed -->

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
