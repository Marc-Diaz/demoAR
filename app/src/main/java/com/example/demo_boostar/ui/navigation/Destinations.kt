package com.example.demo_boostar.ui.navigation
import kotlinx.serialization.Serializable


sealed class Destinations {
    @Serializable
    object PantallaHome: Destinations()

    @Serializable
    object PantallaParaTi: Destinations()

    @Serializable
    object PantallaTendencias: Destinations()

    @Serializable
    object PantallaPermisos: Destinations()

    @Serializable
    object PantallaCamara: Destinations()
}
