package com.example.othermbti.ui.main

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation3.runtime.NavKey
import com.example.othermbti.data.MbtiRepository
import com.example.othermbti.ui.MbtiDashboardScreen

@Composable
fun MainScreen(
  onItemClick: (NavKey) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val repository = remember(context) { MbtiRepository(context) }


  MbtiDashboardScreen(
    repository = repository,
    onShowToast = { message ->
      Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
  )
}
