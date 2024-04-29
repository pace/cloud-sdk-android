# Style Guide

## Table Of Contents
- [Coding architecture](#coding-architecture)
  - [Modern App Architecture](#modern-app-architecture)
  - [MVVM](#mvvm)
  - [Dependency injection](#dependency-injection)
- [Libraries](#libraries)
- [Localization](#localization)
- [Packages structure](#packages-structure)
- [Theming](#theming)
- [UI](#ui)
- [Navigation](#navigation)
- [Naming conventions](#naming-conventions)
- [Formatting](#formatting)
- [Configuration](#configuration)

## Coding architecture

### Modern App Architecture
The app follows the recommendations of the modern app architecture described by Android. It consists of two layers:
- The UI layer that displays application data on the screen.
- The data layer that contains the business logic of your app and exposes application data.

#### UI layer
The UI layer, also known as the presentation layer, serves the purpose of displaying application data on the screen. Whenever there's a modification in the data, triggered either by user actions or external inputs, the UI should update to reflect these changes.
This layer comprises two essential components:
- UI elements responsible for presenting data visually on the screen. These elements are constructed using Jetpack Compose functions.
- State holders, like ViewModel classes, which store data, make it accessible to the UI, and manage associated logic.

#### Data layer
The data layer contains the business logic which contains rules that determine how the app creates, stores, and changes data.
The data layer is made of various repositories that handles the different types of data within the app. For example, this app has an `UserRepository` for user related data.
The repository classes expose data to the rest of the app, resolve conflicts between multiple data sources and abstract sources of data from the rest of the app.

### MVVM
MVVM stands for Model-View-ViewModel is a design pattern that aims to separate the concerns of user interface logic (View) from the business logic (Model) by introducing an intermediary component called ViewModel.
- **Model**: This represents the data and business logic of the application. It encapsulates the data and behavior of the application's domain, such as fetching data from a database, performing calculations, or implementing algorithms.

- **View**: This represents the user interface components of the application, such as buttons, text fields, and other graphical elements. In MVVM, the View is passive and only responsible for displaying data and forwarding user input to the ViewModel.

- **ViewModel**: This acts as an intermediary between the View and the Model. It exposes data and commands that the View can bind to, providing the necessary abstraction to decouple the View from the Model. The ViewModel prepares data for display in the View and responds to user actions by invoking commands on the Model.

### Dependency injection
Dependency injection (DI) is a software design pattern that allows the creation of dependent objects outside of a class and provides those objects to a class through different ways such as constructor injection, method injection, or field injection.
This project uses Hilt which is a framework built on top of Dagger 2, specifically designed to simplify the implementation of dependency injection in Android applications. Hilt manages the dependency injection process by automatically generating the Dagger components required for the application.

## Libraries
- CloudSDK: PACE Cloud SDK is a tool to easily integrate PACE's Connected Fueling into apps. This framework combines multiple functionalities provided by PACE i.e. authorizing via PACE ID or requesting and displaying Apps for fueling and payment. These functionalities are separated and structured into different Kits by namespaces, e.g. IDKit, AppKit or POIKit.
- Firebase: Firebase is a comprehensive mobile and web application development platform provided by Google. This project uses the features Analytics, Crashlytics, In-App Messaging and Cloud Messaging.
  - Analytics: Firebase Analytics provides insights into user behavior and app performance, helping developers understand how users interact with their app. It offers user segmentation, event tracking, and integration with other Firebase services to optimize user engagement and retention.
  - Crashlytics: Firebase Crashlytics helps developers track and prioritize app crashes, providing detailed crash reports and analytics to quickly diagnose and fix issues. It offers real-time alerts and integration with other Firebase services for a seamless debugging experience.
  - In-App Messaging: Firebase In-App Messaging helps engaging the app's active users by sending them targeted, contextual messages. It can be combined with analytics to send messages only to specific users, for example based on users' demographics or past behavior.
  - Cloud Messaging: Firebase Cloud Messaging helps engaging the app's users by sending push notification messages. Moreover data messages can be send that are handled in the application code.
- Places search: The Google Places Search API is a part of the Google Maps Platform, providing developers with access to rich location data. It can be used to perform text-based searches for places using keywords, categories, or specific phrases. The API returns a list of relevant places based on the search query, along with detailed information such as name, address, phone number, website, opening hours, ratings, reviews, and more.
- About libraries: It is used to collect and show all dependency details including licenses at compile time.

## Localization
- Lokalise
  - The apps localization is done with Lokalise. This is a localization and translation management platform which simplifies the process of managing translations.
  - The text changes that are done in Lokalise can be integrated in the app via the `update_strings.sh` script.
- Naming conventions for string keys
 - Structure: "feature"_"type of string"
    - Example:
      - Title: `onboarding_create_pin_title`
      - Description: `onboarding_create_pin_description`
- Supported Languages: 
  * English
  * German
  * Spanish
  * Czech
  * French
  * Italian
  * Dutch
  * Polish
  * Portuguese
  * Romanian
  * Russian

## Packages structure
- The code files are mainly divided into the packages data, di, features, ui and util.
- `data`: It contains the classes that belongs to the model component, such as repositories and cache.
- `di`: It contains the definitions of the hilt modules that are needed for dependency injection.
- `ui`: It contains the view related code. Each feature has its own package with the ui and viewmodel code. It also contains the code for the in-app-navigation.
  - `animation`: It contains all animations.
  - `app`: App "container" (apptheme, scaffold, appnavhost)
  - `component`: It contains views, that are needed at various points in the app and therefore can be reused, for example custom buttons, dialogs, list items.
  - `consent`: It contains views that are responsible for obtaining the user's consent for the terms.
  - `detail`: It contains the code for the detail view of gas stations.
  - `icon`: It contains all needed icons, that are not included in material-icons-extended.
  - `list`: It contains the code for the list view of all gas stations.
  - `map`: It contains the code for the map screen with gas station map markers.
  - `more`: It contains the code for the screens that are displayed in the more section, for example legal, licences and permissions screens.
  - `onboarding`: It contains the views for the apps onboarding-
  - `theme`: It contains the code for theming, especially colors and typographies.
  - `wallet`: It contains the code for the wallet screen.
- `util`: It contains all extensions and util functions.

## Theming
- The app currently only support light mode.
- To adjust the apps color, the material3 color scheme is overwritten.
- Two colors of the color scheme can be adjusted via the configuration process:
  - A primary color which is used for `MaterialTheme.colorScheme.primary` and `MaterialTheme.colorScheme.onSecondary`
  - A secondary color which is used for `MaterialTheme.colorScheme.secondary`

## UI
- The implementation of views is done with jetpack compose.
- Guidelines:
  - Views that are needed at various points in the app, for example custom buttons, dialogs, list items, are created at separated compose functions so they can be reused.
  - At least one preview for each compose function
  - Previews are limited when using ViewModel within a composable. The previews system is not capable of constructing all of the parameters passed to a ViewModel, such as repositories, use cases, managers, or similar. Also, the previews system can't build the whole dependency graph to construct the ViewModel. That's why, if we have a composable that uses a ViewModel, we create another composable with the parameters from ViewModel passed as arguments of the composable. This way, we don't need to preview the composable that uses the ViewModel.
  - If a needed material icon is not included in the `material-icons-extended` library, they are integrated as ImageVectors and placed in the icons package. The advantage of this is that the icons can be used via `Icons.Outlined.NameOfIcon`, just like icons from the library 
  - Remember is used to minimize expensive calculations
    - Composable functions can run very frequently, as often as for every frame of an animation. For this reason, we want to do as little calculation in the body of our composable as we can.
    - An important technique is to store the results of calculations with remember. That way, the calculation runs once, and the results can be fetched whenever they're needed.

## Navigation
- Jetpack composes navigation library is used to handle the in-app-navigation, which mainly consists of the following components: host, graph and controller.
- **Host**: A UI element that contains the current navigation destination. That is, when a user navigates through an app, the app essentially swaps destinations in and out of the navigation host.
  - This project uses the `NavHost` class, which is implemented in `AppNavHost`.
- **Graph**: A data structure that defines all the navigation destinations within the app and how they connect together.
  - This project uses a custom graph class. A `graph` object contains the route string and, if needed, an icon and label for the corresponding bottom bar tab. Moreover there is a `Route` class, that defines the route for every screen.
- **Controller**: The central coordinator for managing navigation between destinations. The controller offers methods for navigating between destinations, handling deep links, managing the back stack, and more.
  - This project uses a `NavHostController` which is handled by the class `AppState`.

## Naming conventions
- Names of packages are always lowercase and do not use underscores.
- Names of classes and objects start with an uppercase letter and use the camel case.
- Names of functions, properties and local variables start with a lowercase letter and use the camel case and no underscores.
- Names of tests can contain spaces enclosed in backticks.
- Names of constants (properties marked with const, or top-level or object val properties with no custom get function that hold deeply immutable data) should use uppercase underscore-separated names.
- Choose good names
  - The name of a class is usually a noun or a noun phrase explaining what the class is: `Marker`, `MapScreen`.
  - The name of a method is usually a verb or a verb phrase saying what the method does: `finish`, `startFueling`.
  - The names should make it clear what the purpose of the entity is.

## Formatting
This project uses Ktlint to check the code style for each merge request. The default `ktlint_official` code style is used, which combines elements from the Kotlin Coding conventions and Android's Kotlin styleguide.
More information about the code style can be seen in our `.editorconfig` file and [here](https://pinterest.github.io/ktlint/latest/rules/code-styles/).

## Configuration
The app can be customized using various parameters via the `configuration.json` file. Some of these values are used during release build, others are added as `BuildConfigField` so they are accessible vial `BuildConfig.NAME_OF_CONFIG_FIELD`.
The following settings can be adjusted, for example:
- `HIDE_PRICES`, Boolean: Decides whether the gas stations prices should be shown in the map markers, the list view and the gas station detail view.
- `ONBOARDING_SHOW_CUSTOM_HEADER`, Boolean: Shows a custom header image when true and the image is available, otherwise the default onboarding headers are used.
- `LIST_SHOW_CUSTOM_HEADER`, Boolean: Shows a custom header image when true and the image is available, otherwise the header is smaller and just displays an icon.
- `DETAIL_SCREEN_SHOW_ICON`, Boolean: Decides whether the companies logo should be shown in gas station detail screens.
- `MAP_ENABLED`, Boolean: Decides whether the map tab is available.
- `SENTRY_ENABLED`, Boolean: Decides whether sentry is used as crash reporting system.
- `CRASHLYTICS_ENABLED`, Boolean: Decides whether firebase crashlytics is used as crash reporting system.
- `ANALYTICS_ENABLED`, Boolean: Decides whether firebase analytics is used as analytic reporting system.
