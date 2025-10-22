import org.gradle.api.tasks.Copy
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Locale

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.jetbrains.kotlin.serialization)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.room)
    alias(libs.plugins.ksp)
    alias(libs.plugins.buildConfig)
}

kotlin {
    androidTarget {
        compilerOptions { jvmTarget.set(JvmTarget.JVM_11) }
    }
    applyDefaultHierarchyTemplate()
    iosArm64()
    iosSimulatorArm64()
    jvm()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)
                implementation(compose.material3)
                implementation(compose.materialIconsExtended)
                implementation(libs.jetbrains.compose.navigation)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.koin.compose)
                implementation(libs.androidx.lifecycle.viewmodel)
                implementation(libs.androidx.lifecycle.runtime.compose)
                implementation(libs.koin.core)
                implementation(libs.bundles.ktor)
                implementation(libs.bundles.coil)
                implementation(libs.navigator)
                implementation(libs.navigator.tabs)
                implementation(libs.navigator.transitions)
                implementation(libs.navigator.viewmodel)
                implementation(libs.navigator.koin)
                // this the library
                implementation(libs.ehsannarmani.compose.charts)
                implementation(libs.datetime)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(compose.preview)
                implementation(libs.androidx.activity.compose)
                implementation(libs.koin.android)
                implementation(libs.koin.androidx.compose)
                implementation(libs.ktor.client.okhttp)
                implementation(libs.androidx.room.runtime)
                implementation(libs.swiperefreshlayout)
            }
        }
        val iosMain by getting {
            dependencies {
                implementation(libs.ktor.client.darwin)
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.kotlinx.coroutines.swing)
                implementation(libs.ktor.client.cio)
            }
        }
        val commonTest by getting {
            dependencies { implementation(libs.kotlin.test) }
        }
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "org.com.bayarair"
}

room {
    schemaDirectory("$projectDir/schemas")
}

android {
    namespace = "org.com.bayarair"
    compileSdk =
        libs.versions.android.compileSdk
            .get()
            .toInt()

    defaultConfig {
        applicationId = "org.com.bayarair"
        minSdk =
            libs.versions.android.minSdk
                .get()
                .toInt()
        targetSdk =
            libs.versions.android.targetSdk
                .get()
                .toInt()
        versionCode = 6
        versionName = "1.0.0"
    }

    packaging { resources.excludes += "/META-INF/{AL2.0,LGPL2.1}" }

    buildTypes { getByName("release") { isMinifyEnabled = false } }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

val renameArtifacts by tasks.register<Copy>("renameArtifacts") {
    val vName = android.defaultConfig.versionName ?: "0.0.0"

    from(layout.buildDirectory.dir("outputs/apk")) { include("**/*.apk") }
    from(layout.buildDirectory.dir("outputs/bundle")) { include("**/*.aab") }

    into(layout.buildDirectory.dir("dist"))

    rename { fileName ->
        when {
            fileName.endsWith(".apk") -> "BayarAir-v$vName.apk"
            fileName.endsWith(".aab") -> "BayarAir-v$vName.aab"
            else -> fileName
        }
    }
}

tasks.configureEach {
    when (name) {
        "assembleRelease", "assemble" -> finalizedBy(renameArtifacts)
        "bundleRelease", "bundle" -> finalizedBy(renameArtifacts)
    }
}

tasks.register("buildAllArtifacts") {
    dependsOn("assembleRelease", "bundleRelease")
}

buildConfig {
    packageName("org.com.bayarair")

    useKotlinOutput {
        topLevelConstants = false
        internalVisibility = false
    }

    val vName = android.defaultConfig.versionName ?: "0.0.0"
    val vCode = android.defaultConfig.versionCode ?: 1

    buildConfigField("String", "APP_VERSION", "\"$vName\"")
    buildConfigField("Int", "APP_VERSION_CODE", "$vCode")
}

dependencies {
    debugImplementation(compose.uiTooling)
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspJvm", libs.androidx.room.compiler)
}

compose.desktop {
    application {
        mainClass = "org.com.bayarair.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "org.com.bayarair"
            packageVersion = "1.0.0"
        }
    }
}
