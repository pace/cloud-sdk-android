x.y.z Release notes (yyyy-MM-dd)
=============================================================
### Enhancements

* Implement check for chrome installation and always use it if installed and not deactivated

### Fixes

* None.

<!-- ### Breaking Changes - ONLY INCLUDE FOR NEW MAJOR version -->
<!-- ### Internal - Include, if needed -->

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
