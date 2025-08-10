plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    id("com.google.dagger.hilt.android")
    id("kotlin-parcelize")
}

android {
    namespace = "com.shinhan.ble"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.shinhan.ble"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // DEV_BASE_URL을 환경변수로 주입 (없으면 기본값 사용)
        val envBaseUrl = System.getenv("DEV_BASE_URL") ?: "http://192.168.219.107:8080/api/v1/"
        buildConfigField("String", "DEV_BASE_URL", "\"$envBaseUrl\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    
    buildFeatures {
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}


dependencies {

     // Core AndroidX
    implementation(libs.androidx.core.ktx.v1120)
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation(libs.material.v1110)
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")

   // Nordic BLE 관련 (Scanner + BLE 기능 포함)
   implementation("no.nordicsemi.android.support.v18:scanner:1.6.0")  // BLE 스캔용 최적 라이브러리
   implementation("no.nordicsemi.android:ble:2.6.1")                 // BLE 관련 전체 기능 제공 (연결, 데이터 송수신 포함)

   // Architecture Components
   implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")         // MVVM ViewModel
      implementation("androidx.navigation:navigation-fragment-ktx:2.7.6")       // UI 내비게이션

      // Database (Room)
      implementation("androidx.room:room-runtime:2.6.1")
      implementation("androidx.room:room-ktx:2.6.1")
      ksp("androidx.room:room-compiler:2.6.1")   // annotation processor 필요 (추가)

      // Dependency Injection (Hilt) - Compatible version
      implementation("com.google.dagger:hilt-android:2.50")
      ksp("com.google.dagger:hilt-compiler:2.50")

      // Security (암호화)
      implementation("androidx.security:security-crypto:1.1.0-alpha06")
      
      // Networking (Retrofit + OkHttp)
      implementation("com.squareup.retrofit2:retrofit:2.9.0")
      implementation("com.squareup.retrofit2:converter-gson:2.9.0")
      implementation("com.squareup.okhttp3:okhttp:4.12.0")
      implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
      
      // JSON Serialization
      implementation("com.google.code.gson:gson:2.10.1")
      
      // Testing dependencies
      testImplementation(kotlin("test"))
      testImplementation("org.mockito:mockito-core:5.7.0")
      testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
      androidTestImplementation("org.mockito:mockito-android:5.7.0")
}
