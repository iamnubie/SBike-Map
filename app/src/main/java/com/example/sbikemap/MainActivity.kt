package com.example.sbikemap

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.sbikemap.navigate.Navigate
import com.example.sbikemap.ui.theme.SBikeMapTheme
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()
        setContent {
            SBikeMapTheme {
                Navigate()
//                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//                    Greeting(
//                        name = "Android",
//                        modifier = Modifier.padding(innerPadding)
//                    )
//                }
//                Surface(
//                    modifier = Modifier.fillMaxSize(),
//                ) {
//                    val navController = rememberNavController()
//                    NavHost(navController = navController, startDestination = "sign_in"){
//                        composable("sign_in"){
//                            val viewModel = viewModel<SignInViewModel>()
//                            val state by viewModel.state.collectAsStateWithLifecycle()
//
//
//                            LaunchedEffect(key1 = state.isSignInSuccessful) {
//                                if(state.isSignInSuccessful){
//                                    Toast.makeText(
//                                        applicationContext,
//                                        "Đăng nhập thành công",
//                                        Toast.LENGTH_LONG
//                                    ).show()
//                                }
//                            }
//
//                            SignInScreen(
//                                state = state,
//                                onSignInClick = {
//                                    lifecycleScope.launch {
//                                        val signInResult = googleAuthUiClient.signIn()
//
//                                        if (signInResult.data != null) {
//                                            viewModel.onSignInSuccess()
//                                        } else {
//                                            viewModel.onSignInError(signInResult.errorMessage)
//                                        }
//                                    }
//                                }
//                            )
//                        }
//                    }
//                }
            }
        }
    }
}
