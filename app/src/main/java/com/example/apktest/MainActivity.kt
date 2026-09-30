package com.example.apktest

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.apktest.screens.LiveStatusActivityScreen
import kotlinx.serialization.Serializable

@Serializable
data class LiveStatus(
    val trainNo:String
)
@Serializable
object TrainSelector
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.S)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {

                val navController = rememberNavController()
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {


                    NavHost(navController, startDestination = TrainSelector) {
                        composable<TrainSelector> { TrainSelector(onNavigateToLiveScreen={ navController.navigate(route = LiveStatus(trainNo = it))})  }

                        composable<LiveStatus> {
                            backStackEntry->
                            val data: LiveStatus = backStackEntry.toRoute()
                            LiveStatusActivityScreen(data.trainNo) }
                    }
                }
            }
        }
    }


}

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        darkTheme -> darkColorScheme()
        else -> lightColorScheme()
    }

    MaterialTheme(colorScheme = colorScheme, content = content)
}