package com.example.demo_boostar.ui.navigation

import com.example.demo_boostar.ui.screens.PantallaAR
import android.util.Log
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.demo_boostar.ui.screens.PantallaHome
import com.example.demo_boostar.ui.screens.PantallaParaTi
import com.example.demo_boostar.ui.screens.PantallaTendencias
import com.example.demo_boostar.ui.screens.PantallaPermisos
import com.example.demo_boostar.viewmodels.PoseViewModel

@Composable
fun NavigationWrapper(innerPadding: PaddingValues, navController: NavHostController, poseViewModel: PoseViewModel) {
    NavHost(navController, Destinations.PantallaPermisos) {
        composable<Destinations.PantallaPermisos> {
            PantallaPermisos { navController.navigate(Destinations.PantallaHome) }
        }
        composable<Destinations.PantallaHome> {
            PantallaHome(innerPadding){
                navController.navigate(it)
            }
        }
        composable<Destinations.PantallaParaTi> {
            Log.d("ParaTi", "ParaTi")
            PantallaParaTi(innerPadding)
        }
        composable<Destinations.PantallaTendencias>{
            Log.d("TENDENCIAS", "TENDENCIAS")
            PantallaTendencias(innerPadding)
        }
        composable<Destinations.PantallaCamara>{

            PantallaAR(poseViewModel)
        }
    }
}