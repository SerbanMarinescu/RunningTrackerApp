import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.DynamicFeatureExtension
import com.example.convention.ExtensionType
import com.example.convention.addUiLayerDependencies
import com.example.convention.configureAndroidCompose
import com.example.convention.configureBuildTypes
import com.example.convention.configureKotlinAndroid
import com.example.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.kotlin

class AndroidDynamicFeatureConventionPlugin: Plugin<Project> {
    override fun apply(target: Project) {
       target.run {
           pluginManager.run {
               apply("com.android.dynamic-feature")
               apply("org.jetbrains.kotlin.android")
           }

           extensions.configure<DynamicFeatureExtension> {
               configureKotlinAndroid(this)
               configureAndroidCompose(this)

               configureBuildTypes(
                   commonExtension = this,
                   extensionType = ExtensionType.DYNAMIC_FEATURE
               )
           }

           dependencies {
               addUiLayerDependencies(target)
               "testImplementation"(kotlin("test"))
           }
       }
    }
}