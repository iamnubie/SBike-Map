package com.example.sbikemap.navigate

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.sbikemap.presentation.HomeScreen
import com.example.sbikemap.presentation.LoginScreen
import com.example.sbikemap.presentation.SignupScreen

@Composable
fun Navigate(){
    val navController = rememberNavController()
    NavHost(navController, startDestination = "login") {
        composable("login") { LoginScreen(navController)}
        composable("signup") { SignupScreen(navController)}
        composable("home") { HomeScreen(navController)}
    }
}