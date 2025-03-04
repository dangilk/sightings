package com.djg.sightings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.djg.sightings.navigation.BottomBar
import com.djg.sightings.navigation.MainNavHost
import com.djg.sightings.ui.theme.SightingsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SightingsTheme {
                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize(), bottomBar = { BottomBar(navController = navController) }) { innerPadding ->
                    MainNavHost(navController = navController, modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

//@Composable
//fun Greeting(name: String, modifier: Modifier = Modifier) {
//    Text(
//        text = "Hello $name!",
//        modifier = modifier
//    )
//}
//
//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    SightingsTheme {
//        Greeting("Android")
//    }
//}