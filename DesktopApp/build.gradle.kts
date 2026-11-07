import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm") version "2.1.21"
    kotlin("plugin.serialization") version "2.1.21"
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.21"
    id("org.jetbrains.compose") version "1.8.2"
}

group = "com.devsoft"
version = "1.0.0"

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)

    // Same backend as the Android app (Supabase)
    implementation(platform("io.github.jan-tennert.supabase:bom:3.1.4"))
    implementation("io.github.jan-tennert.supabase:postgrest-kt")
    implementation("io.github.jan-tennert.supabase:auth-kt")
    implementation("io.ktor:ktor-client-okhttp:3.1.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.10.2")
    implementation("org.slf4j:slf4j-simple:2.0.17")

    testImplementation(kotlin("test"))
}

compose.desktop {
    application {
        mainClass = "com.devsoft.desktop.MainKt"
        jvmArgs += listOf("-Dfile.encoding=UTF-8")

        nativeDistributions {
            // Msi / Exe need the WiX toolset on the build machine; `createDistributable`
            // produces a portable folder with FreshFoodMaster.exe without WiX.
            targetFormats(TargetFormat.Msi, TargetFormat.Exe)
            packageName = "FreshFoodMaster"
            packageVersion = "1.0.0"
            description = "FreshFood - Poste principal (Windows master station)"
            vendor = "DevSoft"
            modules("java.naming", "java.net.http", "jdk.crypto.ec", "java.prefs")
            windows {
                menuGroup = "FreshFood"
                shortcut = true
                dirChooser = true
                perUserInstall = true
                upgradeUuid = "6c3e8f62-5d7a-4c55-9a59-2f0b1f6a7e11"
            }
        }
    }
}

tasks.test {
    useJUnitPlatform()
}
