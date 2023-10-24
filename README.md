<div align="center">
<img src="./icon.png" width="150" height="150" />

<h1 align="center">
    Connected Fueling Android App
</h1>
</div>

## General

Connected Fueling is an android project serving as an example
of [PACE Cloud SDK](https://github.com/pace/cloud-sdk-android) integration.

- This project uses at least Kotlin *1.5*.
- Uses [MVVM architecture](https://developer.android.com/jetpack/guide#recommended-app-arch)
  with [Hilt](https://developer.android.com/training/dependency-injection/hilt-android)
  and [Jetpack Compose](https://developer.android.com/jetpack/compose/documentation)

## Setup

- Add a keystore and store it in the root directory as `keystore.jks`.
- Add a file `secrets.properties` (or duplicate the available `secrets.sample.properties` file)
  and add the following properties:

```properties
signingKeyPath=../keystore.jks -> path to the keystore that is used for signing.
signingKeyPassword=123456 -> key store password
signingKeyAlias=my-alias -> key store alias
signingKeyAliasPassword=123456 -> key alias password
paceCloudApiKey=12345678-1234-5678-1234-1234567890ab -> 36-digit api key, contact PACE to obtain one
paceCloudClientId=my-pace-app -> your pace cloud app id, contact PACE to obtain one
paceCloudRedirectUrl=my-app://callback -> the pace cloud api callback url, contact PACE to obtain one
paceCloudRedirectScheme=my-app -> only the scheme of the redirect url
paceCloudUniqueId=pace.8c5c2735-8778-4686-aa1d-94345ccb1a8e -> a self-chosen unique id for your app in the format pace.UUID
paceCloudAppName=My App -> The user-readable name of your app
```

### Local Builds

For local builds, you do not need to create a signing key but can use your local debug keystore.

```
signingKeyPath=/Users/[username]/.android/debug.keystore
signingKeyPassword=android
signingKeyAlias=androiddebugkey
signingKeyAliasPassword=android
```

### Building with Gitlab CI

When building via Gitlab CI, the properties need to be set in Settings > CI/CD > Variables using the
following keys:

- `KEYSTORE`: the keystore needs to be base64 encoded since the variables cannot store binary
  data: `base64 -i keystore.jks`
- `KEYSTORE_PASSWORD`
- `KEYSTORE_ALIAS`
- `KEYSTORE_ALIAS_PASSWORD`
- `API_KEY`
- `CLIENT_ID`
- `REDIRECT_URL`
- `REDIRECT_SCHEME`
- `UNIQUE_ID`
- `APP_NAME`

## Localization

The project is localized for following languages:

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

## License

This project is licensed under the terms of the MIT license. See the [LICENSE](./LICENSE.md) file.
