# PACE Cloud SDK

[![License](https://img.shields.io/github/license/pace/cloud-sdk-android)](https://github.com/pace/cloud-sdk-android/blob/master/LICENSE.md)
[![Maven Central](https://img.shields.io/maven-central/v/cloud.pace/sdk)](https://search.maven.org/artifact/cloud.pace/sdk)

`PACE Cloud SDK` is a tool for developers to easily integrate `PACE's` [Connected Fueling](https://connectedfueling.com) into their own apps.

This framework combines multiple functionalities provided by `PACE` i.e. authorizing via **PACE ID** or requesting and displaying **Apps** for fueling and payment. These functionalities are separated and structured into different ***Kits*** by namespaces, e.g. `IDKit`, `AppKit` or `POIKit`.

## Documentation

The complete documentation can be found on [pace.github.io/cloud-sdk-android](https://pace.github.io/cloud-sdk-android).

## Contribution

### Commit Message Format

Each commit message consists of a mandatory `header`, `body` and `footer` are optional.
Commit messages are structured as follows:

```
<header>
<BLANK LINE>
<body>
<BLANK LINE>
<footer>
```

### Commit Message Header

```
<type>(<scope>): <short summary>
  │       │             │
  │       │             └─⫸ Summary in present tense. Not capitalized. No period at the end.
  │       │
  │       └─⫸ Commit Scope: geo|tiles|communication|biometry|webview|...|
  │
  └─⫸ Commit Type: build|ci|docs|feat|fix|perf|refactor|test|chore
```

The `<type>` and `<summary>` fields are mandatory, the `(<scope>)` field is optional.

#### Examples

```
fix(bundle): correctly trim whitespaces in bundle name

#####################################################################

feat: implement ultimate connected fueling functionality

#####################################################################

docs: add migration guide for version 1.0.0

#####################################################################

perf(geo): dispatch geo service response handling to background queue

BREAKING CHANGE: The geoAppsScope property in the configuration has been removed.
```

#### Type

Must be one of the following:

- `build`: Changes that affect the build system or external dependencies
- `ci`: Changes to our CI configuration files and scripts ( `bump_version.sh`, `gitlab-ci.yaml`, ...)
- `docs`: Documentation only changes
- `feat`: A new feature
- `fix`: A bug fix
- `perf`: A code change that improves performance
- `refactor`: A code change that neither fixes a bug nor adds a feature
- `test`: Adding missing tests or correcting existing tests
- `chore`: Maintenance work (removing newlines, etc.)

#### Summary

- use the imperative, present tense: "fix" not "fixed" nor "fixes"
- don't capitalize the first letter
- usually there is no `.` at the end

#### Breaking changes

- Include `BREAKING CHANGE:` in commit body
- Short description on what caused the breaking change

## License

This project is licensed under the terms of the MIT license. See the [LICENSE](LICENSE.md) file.
