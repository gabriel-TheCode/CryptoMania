# CryptoMania 2.0

A modern, offline-first cryptocurrency market tracker for Android, built with Jetpack Compose on
top of the free [CoinGecko API](https://www.coingecko.com/en/api).

> CryptoMania 2.0 is a complete rewrite. The original 2021 app (XML views, MVVM) is preserved on the
> [`old`](https://github.com/gabriel-TheCode/CryptoMania/tree/old) branch.

<a href="https://play.google.com/store/apps/details?id=com.thecode.cryptomania">
  <img alt="Get it on Google Play" height="64" src="https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png" />
</a>

<img alt="CryptoMania 2.0: market, coin details in dark mode, Hot filter" src="docs/cryptomania.png" />

## Features

- **Market overview** – total market cap, 24h volume, BTC/ETH dominance, top movers (biggest 24h
  moves), and the top 250 coins with 7-day sparklines. Filters: All · **Hot** (CoinGecko trending
  coins) · Watchlist · Gainers · Losers, with a directional page transition.
- **Coin details** – price that rolls digit by digit on updates, interactive chart (1D · 1W · 1M ·
  3M · 1Y) with drag/long-press scrubbing and haptics, performance, 24h range, supply, all-time high,
  description, categories and website.
- **Search** – instant ranking over the cached market, CoinGecko search only when the cache cannot
  answer, recent searches.
- **Watchlist** – star a coin from its details, or long-press any market row for a quick
  "Add to watchlist" menu (also exposed to TalkBack as a custom action); works for coins outside
  the top 250 too.
- **Exchanges** – ranked by trust score, 24h volume estimated in USD.
- **Animated splash** – crypto coins rain down, swirl into a vortex, turn gold and merge into
  the CryptoMania logo, then the splash opens onto the app as an expanding circle (tap to skip).
- **Fluid navigation** – detail screens slide over the list with a parallax and slide back out
  (also driven by the predictive back gesture); tabs fade over the previous page, and the bottom
  bar slides away instead of vanishing. The page underneath always stays drawn, so nothing flashes.
- **Onboarding** – the original CryptoMania intro (Lottie animations) rebuilt in Compose.
- **Offline-first** – everything already downloaded stays browsable in airplane mode, when rate
  limited, or when CoinGecko is down; a pill on the header wave tells how old the data is.
- **Adaptive** – bottom bar on phones, navigation rail on tablets/foldables, extra market columns and
  a two-pane coin screen on wide windows, landscape-aware onboarding.
- **Themes & accessibility** – light/dark/system, color-blind friendly mode (blue/orange instead of
  green/red), arrows and signs on every change, TalkBack descriptions for rows and charts.

## Visuals

<img alt="Animated splash screen" src="docs/splash.gif" width="260" align="right" />

The splash is one Compose `Canvas` (`AnimatedSplash`) driven by a single clock read at draw time:
no recomposition per frame. The system splash window is plain brand blue and is dropped as soon
as the app draws, so the animation starts on screen instead of behind it; it plays once per
launch, while the market loads underneath, and finishes at once when animations are disabled.

Play Store assets live in [`docs/playstore`](docs/playstore): the 512×512 hi-res icon (rendered
from the launcher vectors), eight 1080×1920 phone screenshots and the 1024×500 feature graphic.

<br clear="right" />

## Architecture

Single `:app` module with strict package boundaries (a multi-module split is not justified at this
size; the packages are drawn so it could be split mechanically later).

```
presentation ──▶ domain ◀── data
```

```
com.thecode.cryptomania
├── domain            Pure Kotlin: models, AppError/Outcome, repository contracts, use cases
├── data
│   ├── remote        Retrofit API + kotlinx.serialization DTOs
│   ├── local         Room database, entities, DAOs
│   ├── preferences   DataStore (settings, recent searches, onboarding)
│   ├── network       RequestCoordinator, error mapping, connectivity monitor
│   ├── mapper        DTO → entity → domain
│   └── repository    Offline-first repositories + CachePolicy
├── di                Hilt modules
└── presentation
    ├── designsystem  Theme tokens, components, chart
    ├── navigation    Type-safe routes, adaptive navigation suite
    ├── model / util  UI models, formatters, shared UI state
    └── feature       market · search · coin · exchanges · settings · onboarding
```

**MVI / UDF.** Each screen has an immutable `UiState`, a sealed `Intent`, and one-off `Effect`s where
a side effect must complete before navigating (search, settings, onboarding). ViewModels combine
repository flows into state with `stateIn(WhileSubscribed)`; screens are stateless composables
(`XxxScreen(state, onIntent)`) wrapped by a `XxxRoute` that wires the ViewModel. Data is never
replaced by an error page once something is cached: problems surface as a `SyncStatus` pill.

## API & caching strategy

CoinGecko's free tier (Demo: 100 calls/min; keyless: IP-shared limits, `/coins/markets` cached 60 s
server-side, history capped at 365 days) shaped the data layer:

| Data | Endpoint | Freshness (TTL) |
|---|---|---|
| Top 250 coins + sparkline + 1h/24h/7d | `/coins/markets` (1 call) | 2 min |
| Watched coins outside the top 250 | `/coins/markets?ids=…` (1 batched call) | with the market |
| Global cap/volume/dominance | `/global` | 5 min |
| Hot (trending coins) | `/search/trending` (+ same batched `ids` call) | 15 min |
| Price history | `/coins/{id}/market_chart` | 5 min (1D) → 12 h (1Y) |
| Description, links, categories | `/coins/{id}` (heavy sections disabled) | 24 h |
| Exchanges | `/exchanges` | 1 h |
| Search | `/search` (debounced, only if needed) | 10 min in memory |

- **Room is the single source of truth.** Screens observe the database; network calls only refresh it.
- **One market call feeds everything**: list, movers, filters, local search and coin headers. Opening
  a listed coin costs 0 calls for the header; 1 for the chart; 1 per day for metadata.
- **`RequestCoordinator`**: in-flight deduplication by key, retry with backoff (1 s, 2 s) for timeouts
  and 5xx only, and a global cooldown after HTTP 429 honouring `Retry-After`.
- **`CachePolicy`**: all TTLs in one place; pull-to-refresh is ignored inside the 30 s window where
  CoinGecko would return identical data anyway.
- **No polling.** Refresh happens on screen start/resume when stale, on reconnect, and on pull.
- Errors are mapped once (`Throwable.toAppError()`) to `AppError` (NetworkUnavailable, Timeout,
  RateLimited, ServerUnavailable, Unauthorized, NotFound, Unknown).

## Charts

A ~200-line Compose `Canvas` chart (`PriceChart`) replaces AnyChart (a WebView). Compose chart
libraries were considered; for one controlled line chart a local implementation is lighter and gives
full control. Geometry is cached with `drawWithCache`; scrubbing and the reveal animation only
invalidate drawing. Points are placed by timestamp (sparse history keeps its gaps), a dashed baseline
marks the period open, high/low are labelled, and TalkBack reads a summary of the period.

## Design system

Semantic color tokens (dark/light + color-blind variants), **Ubuntu** carried over from
CryptoMania 1.x (bold headlines, tabular digits for figures), spacing/size tokens, Material 3
Expressive spring motion, and the original **wave header**, redrawn as an adaptive Compose `Shape`
from the 1.x vector: two layers in slow liquid motion (drawn wider than the screen so no edge ever
shows), drawn over the content so lists scroll under it. The wavy edge keeps a fixed height and only
the flat part stretches, so title, subtitle and status pill always sit on solid blue; the header
collapses on scroll. The launcher icon, splash and onboarding use a new vector mark: two stylized gold coins. Components: `CryptoManiaTopBar`, `CryptoManiaCard` (soft
tinted shadows), `CryptoPrice` (rolling ticker), `PriceChangeBadge`, `MarketStat`, `CryptoListItem`,
`MoverCard`, `Sparkline`, `CryptoManiaSearchBar`, `CryptoManiaFilterChip`, `SegmentedSelector`
(time ranges), `PriceChart`, skeletons, `ErrorState`, `EmptyState`, `NetworkStatusIndicator`,
`CryptoManiaPullToRefresh`.

## Tech stack

Kotlin 2.4 · AGP 9 (built-in Kotlin) · Gradle 9.7 · Jetpack Compose (BOM 2026.09) · Material 3 ·
Navigation Compose (type-safe routes) · Hilt + KSP · Room · DataStore · Coroutines/Flow · Retrofit 3 +
kotlinx.serialization · OkHttp 5 · Coil 3 · Lottie Compose · SplashScreen API.
All versions live in [`gradle/libs.versions.toml`](gradle/libs.versions.toml).

## Setup

Requirements: Android Studio with JDK 17, Android SDK 37.

```bash
git clone https://github.com/gabriel-TheCode/CryptoMania.git
```

The app works without any key (public keyless tier). For a higher, dedicated quota create a free
[CoinGecko Demo key](https://www.coingecko.com/en/developers/dashboard) and add it to
`local.properties` (never committed) or the `COINGECKO_API_KEY` environment variable:

```properties
coingecko.apiKey=CG-xxxxxxxxxxxxxxxx
```

> A key embedded in an APK can always be extracted. Treat it as a quota identifier, not a secret; a
> backend proxy is the only way to keep a paid key private.

## Build & test

```bash
./gradlew assembleDebug        # debug APK
./gradlew assembleRelease      # R8-minified release (unsigned)
./gradlew test                 # unit + Robolectric Compose UI tests
./gradlew lint                 # Android lint (warnings fail the build)
```

Kotlin compiler warnings are errors. Tests cover behavior rather than implementation:
error mapping, request deduplication/backoff/cooldown, cache policy, mappers, an offline-first
repository test with real Room + Retrofit against MockWebServer, use cases, MVI reducers (debounced
search on virtual time) and screen flows (load, refresh, filters, empty states, error, offline,
chart range, watchlist long-press, search, onboarding).

> Avoid running command-line Gradle builds while Android Studio builds the same project: two builds
> writing `app/build` at once can package an APK with missing classes.

## Limitations

- Prices are in USD only: a currency switch would multiply cache entries and API calls.
- Free-tier history stops at 365 days; finer-than-hourly data beyond one day is paid-only.
- CoinGecko's free tier may cache prices for up to a minute; the app does not pretend to be real time.
- Exchange volume in USD is an estimate (BTC volume × cached BTC price).
- Robolectric UI tests run on SDK 35 because SDK 36+ sandboxes require JDK 21.
- No baseline profile module yet.
- Full Material 3 Expressive (`MaterialExpressiveTheme`, `LoadingIndicator`) needs material3
  1.5.0-alpha, which pulls Compose core to alpha; the app stays on stable and uses the Expressive
  navigation bar and spring motion available today.

## License

MIT License — Copyright (c) 2021 TEKOMBO Gabriel. See [LICENSE](LICENSE).
