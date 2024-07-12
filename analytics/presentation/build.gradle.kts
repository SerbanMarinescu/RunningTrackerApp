plugins {
    alias(libs.plugins.runique.android.feature.ui)
}

android {
    namespace = "com.example.analytics.presentation"
}

dependencies {
    implementation(projects.analytics.domain)
}