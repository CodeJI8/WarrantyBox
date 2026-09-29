import os

with open('gradle/libs.versions.toml', 'r') as f:
    content = f.read()

versions_section = """
room = "2.6.1"
navCompose = "2.8.5"
coil = "2.6.0"
gson = "2.11.0"
"""

libraries_section = """
androidx-room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
androidx-room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
androidx-room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navCompose" }
androidx-material-icons-extended = { group = "androidx.compose.material", name = "material-icons-extended", version = "1.7.5" }
coil-compose = { group = "io.coil-kt", name = "coil-compose", version.ref = "coil" }
gson = { group = "com.google.code.gson", name = "gson", version.ref = "gson" }
"""

content = content.replace('[libraries]', versions_section + '\n[libraries]\n' + libraries_section)

with open('gradle/libs.versions.toml', 'w') as f:
    f.write(content)

with open('app/build.gradle.kts', 'r') as f:
    app_build = f.read()

if 'id("kotlin-kapt")' not in app_build:
    app_build = app_build.replace('plugins {', 'plugins {\n    id("kotlin-kapt")')
    app_build = app_build.replace('dependencies {', '''dependencies {
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.coil.compose)
    implementation(libs.gson)
''')

with open('app/build.gradle.kts', 'w') as f:
    f.write(app_build)

print("done")
