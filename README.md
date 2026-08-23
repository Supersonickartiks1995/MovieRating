# MovieRating - Modern Android Movie App

MovieRating is a high-performance Android application built with the latest technologies to demonstrate Modern Android Development (MAD) practices. It allows users to browse popular movies, search for specific titles, and save favorites for offline viewing.

## 🚀 Features

- **Paginated Browsing**: Uses Paging 3 to load popular movies efficiently.
- **Advanced Search**: Debounced search functionality to find movies by title.
- **Offline Support**: 
    - Offline caching for popular movies and details.
    - Persistent "Favorites" feature using Room database.
- **Material 3 Design**: Fully polished UI using Material 3 components, adaptive shapes, and dark mode support.
- **Robust Feedback**: Comprehensive loading and error states with retry functionality.

## 🛠 Tech Stack

- **UI**: Jetpack Compose
- **Architecture**: Clean Architecture + MVVM
- **Dependency Injection**: Hilt
- **Local Database**: Room
- **Network**: Retrofit + OkHttp + Kotlinx Serialization
- **Concurrency**: Kotlin Coroutines + Flow
- **Image Loading**: Coil 3
- **Pagination**: Paging 3

## 🏗 Architecture & Design Decisions

### Clean Architecture
The project is divided into layers:
- **Domain**: Contains business logic, models (`Movie`), and repository interfaces.
- **Data**: Implements the repository, handling network requests (Retrofit) and local persistence (Room).
- **UI**: Compose-based screens and ViewModels managing state with `StateFlow`.

### Key Decisions
- **Offline-First**: We prioritize local data and cache API responses to ensure a smooth user experience even without internet.
- **Search Debouncing**: A 500ms delay is implemented to optimize network usage during typing.
- **Elevated UI**: Used Material 3 `ElevatedCard` and adaptive `SearchBar` to provide a premium feel.

## 📝 Setup
1. Clone the repository.
2. Open in Android Studio.
3. Add your TMDB API Key in `MovieRepositoryImpl.kt` (Note: Currently using a hardcoded placeholder for demonstration).
4. Build and Run!
