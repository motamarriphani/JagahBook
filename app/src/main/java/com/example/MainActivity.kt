package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.data.AppDatabase
import com.example.data.LocationRepository
import com.example.ui.PinBookApp
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : ComponentActivity() {

  private val sharedUrlFlow = MutableStateFlow<String?>(null)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    val database = AppDatabase.getDatabase(this)
    val repository = LocationRepository(database.locationDao())

    handleIntent(intent)

    setContent {
      MyApplicationTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          val sharedUrl by sharedUrlFlow.collectAsState()
          PinBookApp(repository = repository, sharedUrl = sharedUrl)
        }
      }
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    handleIntent(intent)
  }

  private fun handleIntent(intent: Intent?) {
    if (intent == null) return
    if (intent.action == Intent.ACTION_SEND && intent.type == "text/plain") {
      sharedUrlFlow.value = intent.getStringExtra(Intent.EXTRA_TEXT)
    } else if (intent.action == Intent.ACTION_VIEW && intent.data != null) {
      sharedUrlFlow.value = intent.data.toString()
    } else {
      sharedUrlFlow.value = null
    }
  }
}
