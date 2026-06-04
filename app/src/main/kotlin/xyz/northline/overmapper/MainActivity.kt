package xyz.northline.overmapper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import xyz.northline.overmapper.ui.navigation.NavGraph
import xyz.northline.overmapper.ui.theme.OverMapperTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            OverMapperTheme {
                NavGraph()
            }
        }
    }
}
