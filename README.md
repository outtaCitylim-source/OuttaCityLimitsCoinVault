# Outta City Limits Coin Vault Android

Version 1.0.16 is the coin-focused Outta City Limits app and uses the production OCL web
service at https://outta-city-limits.vercel.app.
The Android application provides camera and multiple-photo selection, persisted login cookies,
Shop / Inventory / Account navigation, and connection recovery. Inventory editing, validation,
authentication, authorization, and database persistence run through the existing web application.

Sign in using an OCL website account. Inventory requires an administrator account. Customer
registration is available through the website's sign-up screen. Each inventory item uses the
same record and photos in the app and the website.

The GitHub Actions workflow builds and lints a sideloadable debug APK. Download the APK artifact
from the successful workflow run. This test build uses a new Android package, com.outtacitylimits.vault,
and installs alongside the previous Manus app. Production distribution requires a persistent
owner-controlled signing key. The previous APK's data must be exported before migration.

Shopify and Google Drive still require server-side integration credentials. A connected ChatGPT
connector does not provision credentials inside the production web application. Whatnot channel
activation is a separate merchant setup step. The Android shell does not claim these connections
are active. Automatic coin identification and PhotoGrade remain future features.

Build locally with JDK 17, Android SDK 35 and Gradle 8.9 by running `gradle assembleDebug lintDebug`
from this directory. The output is app/build/outputs/apk/debug/app-debug.apk.
