package com.example.sbikemap.navigate

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.sbikemap.presentation.HomeScreen
import com.example.sbikemap.presentation.LocationPermissionWrapper
import com.example.sbikemap.presentation.LoginScreen
import com.example.sbikemap.presentation.MapScreen
import com.example.sbikemap.presentation.SignupScreen
import com.example.sbikemap.presentation.UserProfileScreen

@Composable
fun Navigate(){
    val navController = rememberNavController()
    NavHost(navController, startDestination = "map_route") {
        composable("login") { LoginScreen(navController)}
        composable("signup") { SignupScreen(navController)}
        composable("home") { HomeScreen(navController)}
        composable("map_route") {
//            MapScreen()
            LocationPermissionWrapper(navController)
        }
        composable("profile") {
            UserProfileScreen(navController)
        }
    }
}