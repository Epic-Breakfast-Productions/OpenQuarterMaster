import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	alias(libs.plugins.kotlinMultiplatform)
	alias(libs.plugins.androidMultiplatformLibrary)
	alias(libs.plugins.composeMultiplatform)
	alias(libs.plugins.composeCompiler)
}

kotlin {
	listOf(
		iosArm64(),
		iosSimulatorArm64()
	).forEach { iosTarget ->
		iosTarget.binaries.framework {
			baseName = "Shared"
			isStatic = true
		}
	}
	jvm()
	android {
		namespace = "tech.ebp.oqm.plugin.pda.shared"
		compileSdk = libs.versions.android.compileSdk.get().toInt()
		minSdk = libs.versions.android.minSdk.get().toInt()
		compilerOptions {
			jvmTarget = JvmTarget.JVM_11
		}
		androidResources {
			enable = true
		}
		withHostTest {
			isIncludeAndroidResources = true
		}
		withDeviceTestBuilder {
			sourceSetTreeName = "test"
		}.configure {
			instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
		}
	}
	sourceSets {
		androidMain.dependencies {
			implementation(libs.compose.uiToolingPreview)
			implementation(libs.compose.uiTooling)
		}
		commonMain.dependencies {
			implementation(libs.compose.runtime)
			implementation(libs.compose.foundation)
			implementation(libs.compose.material3)
			implementation(libs.compose.ui)
			implementation(libs.compose.components.resources)
			implementation(libs.compose.uiToolingPreview)
			implementation(libs.androidx.lifecycle.viewmodelCompose)
			implementation(libs.androidx.lifecycle.runtimeCompose)
		}
		commonTest.dependencies {
			implementation(libs.kotlin.test)
		}
		getByName("androidDeviceTest").dependencies {
			implementation(libs.androidx.testRunner)
		}
	}
}

dependencies {
	androidRuntimeClasspath(libs.compose.uiTooling)
}
