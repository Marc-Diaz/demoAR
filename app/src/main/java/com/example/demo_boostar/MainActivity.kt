package com.example.demo_boostar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.navigation.compose.rememberNavController
import com.example.demo_boostar.ui.navigation.Destinations
import com.example.demo_boostar.ui.navigation.NavigationItem
import com.example.demo_boostar.ui.navigation.NavigationWrapper
import com.example.demo_boostar.ui.theme.DemoboostTheme
import com.example.demo_boostar.viewmodels.PoseViewModel
import com.example.demo_boostar.viewmodels.PoseViewModelFactory


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DemoboostTheme {
                MyApp()
            }
        }
    }

    @Composable
    fun MyApp() {
        val poseViewModel: PoseViewModel = viewModel<PoseViewModel>(factory = PoseViewModelFactory(application))
        var selectedItem: Int by remember { mutableIntStateOf(0) }
        val items = listOf(
            NavigationItem("HOME", Icons.Default.Home, Destinations.PantallaHome, 0),
            NavigationItem("CAMARA", Icons.Default.Person, Destinations.PantallaCamara, 1),
            NavigationItem("AJUSTES", Icons.Default.Settings, Destinations.PantallaHome, 2)
        )
        val navController = rememberNavController()
        Scaffold(
            bottomBar = {
                NavigationBar {
                    items.forEachIndexed { index, item ->
                        NavigationBarItem(
                            selected = item.index == selectedItem,
                            label = { Text(item.label) },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label
                                )
                            },
                            onClick = {
                                selectedItem = index
                                navController.navigate(item.route)
                            }
                        )
                    }
                }
            },
            content = { innerPadding ->
                NavigationWrapper(innerPadding, navController, poseViewModel)
            }
        )
    }
}