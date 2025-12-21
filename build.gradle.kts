import tgx.gradle.flavorImplementation

plugins {
  id(libs.plugins.android.library.get().pluginId)
  alias(libs.plugins.kotlin.android)
  id("tgx-module")
}

dependencies {
  flavorImplementation(
    libs.androidx.core.ktx.legacy,
    libs.androidx.core.ktx.latest
  )
  implementation(project(":vkryl:core"))
}

android {
  namespace = "me.vkryl.android"
}