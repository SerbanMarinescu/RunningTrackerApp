plugins {
    alias(libs.plugins.runique.android.library)
}

android {
    namespace = "com.example.wear.run.data"

    defaultConfig {
        minSdk = 30
    }
}

dependencies {
    implementation(libs.androidx.health.services.client)
    implementation(libs.bundles.koin)
}