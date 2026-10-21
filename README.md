# Movie Search

Android app that searches movies through the [OMDb API](https://www.omdbapi.com/), shows details,
keeps a list of favorites in a local database and monetizes with AdMob (native + interstitial ads)
through a separate, provider-agnostic `:SDK` module.

Kotlin, Jetpack Compose, Hilt, Room, Retrofit, Coil, Coroutines/Flow.

## Setup

1. Get a free key at <https://www.omdbapi.com/apikey.aspx>.
2. Add it to `local.properties` (git-ignored, never commit it):
   ```
   OMDB_API_KEY=your_key_here
   ```
3. Open the project in Android Studio (Ladybug or newer) and run the `app` configuration.

Ads use Google's **test** ad units out of the box, so nothing else has to be configured.

## Architecture

```
:app                                         :SDK
presentation  (Compose, ViewModels)  --->    AdsSdk          single entry point
     |                                       AdProvider      interface, one per ad network
domain        (models, repository contract)  NativeAdAsset   renderable native ad
     |                                       NativeAdCard    Composable slot
data          (Retrofit, Room)               internal/       ad policy (no network knowledge)
                                             admob/          the only place that imports AdMob
```

There are deliberately no use-case classes: the app has no business rules beyond "search" and "toggle favorite",
so ViewModels talk to `MovieRepository` directly. The repository is still an interface so tests can use a fake.

Dependencies point inward: `presentation -> domain <- data`. `:app` depends on `:SDK`, never the reverse,
and no AdMob type is visible outside `:SDK` (the dependency is declared with `implementation`).

| Concern                                              | Where                                                   |
|------------------------------------------------------|---------------------------------------------------------|
| Search with debounce, cancellation of stale requests | `SearchViewModel`                                       |
| Ad positions (indexes 1 and 3, only with 3+ results) | `ListItem.kt` -> `List<Movie>.withAds()`                |
| Favorites persistence                                | `data/local` (Room), exposed as `Flow`                  |
| Heart button state                                   | `DetailsViewModel` observes the database, no local flag |
| Provider fallback, per-slot native ad cache          | `sdk/internal/AdsManager`, `NativeAdCache`              |
| "Interstitial on the 2nd return to the app"          | `sdk/internal/ReturnCounter` + `AdsManager`             |

### Interstitial rule

`ProcessLifecycleOwner` reports app-level foreground events (rotations and in-app navigation are ignored).
The first one is the launch, so the interstitial is queued on the second *return* and shown on the next
activity resume. It is shown once; if it has not finished loading by then it is skipped rather than shown late.

### Adding another ad provider (AppLovin, Meta, ...)

1. Implement `AdProvider` (and a `NativeAdAsset` that knows how to render and track its own ads).
2. Pass it to `AdsSdk.init(app, listOf(AdProviders.admob(), MyNewProvider()))`.

Native ads are requested from providers in list order; the first one that fills the slot wins.
Interstitials use the first provider that has one ready. `:app` code does not change.

## Tests

```
./gradlew test
```

Unit tests cover the logic that is easiest to get wrong: ad positions in the list, search (debounce, cancellation,
error states), OMDb response handling in the repository, the favorites toggle, and the SDK's ad policy
(interstitial on the second return, provider fallback, per-slot native ad cache).

## Production checklist

- Replace the test ids: `AdMobConfig(...)` passed in `MoviesApp`, and the AdMob app id in `:app`'s
  manifest (`tools:replace="android:value"` on the `com.google.android.gms.ads.APPLICATION_ID` meta-data).
- Add ProGuard/R8 rules if minification is enabled.
