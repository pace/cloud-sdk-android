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
- Add a file `config.json` and add the following properties:

```json
{
  "appName": "Connected Fueling App",
  "signing": {
    "keyPath": "../keystore.jks",
    "keyPassword": "YOUR_KEYSTORE_PASSWORD",
    "keyAlias": "YOUR_ALIAS",
    "keyAliasPassword": "YOUR_KEY_PASSWORD"
  },
  "sdk": {
    "apiKey": "YOUR_API_KEY",
    "clientId": "YOUR_CLIENT_ID",
    "redirectUrl": "YOUR_REDIRECT_URL",
    "redirectScheme": "YOUR_REDIRECT_SCHEME",
    "uniqueId": "YOUR_UUID"
  }
}
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
