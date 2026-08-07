package com.example.othermbti

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.othermbti.theme.OtherMBTITheme
import com.google.android.gms.ads.MobileAds

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Initialize Google Mobile Ads SDK with Production App ID (ca-app-pub-5254974097452914~6238198724)
    try {
      MobileAds.initialize(this)
    } catch (e: Exception) {
      e.printStackTrace()
    }

    enableEdgeToEdge()
    setContent {
      OtherMBTITheme { Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) { MainNavigation() } }
    }
  }
}

