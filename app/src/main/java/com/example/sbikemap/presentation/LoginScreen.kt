package com.example.sbikemap.presentation

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.sbikemap.presentation.viewmodel.AuthViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavHostController,
    viewModel: AuthViewModel = viewModel()
){
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current
    // Theo dõi trạng thái từ ViewModel
    val authState by viewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        when (authState) {
            is AuthViewModel.AuthState.Success -> {
                val response = (authState as AuthViewModel.AuthState.Success).response
                // Log/Lưu AccessToken và RefreshToken ở đây
                Toast.makeText(context, "Đăng nhập Backend thành công! User: ${response.email}", Toast.LENGTH_SHORT).show()
                viewModel.resetState() // Reset để tránh lặp lại navigation
                navController.navigate("home"){
                    popUpTo("login") {inclusive = true}
                }
            }
            is AuthViewModel.AuthState.Error -> {
                val message = (authState as AuthViewModel.AuthState.Error).message
                Toast.makeText(context, "Lỗi Backend: $message", Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            is AuthViewModel.AuthState.Loading -> {
                // Hiển thị vòng quay loading nếu cần
                Toast.makeText(context, "Đang xử lý Backend...", Toast.LENGTH_SHORT).show()
            }
            else -> { /* Idle */ }
        }
    }

    Box(modifier = Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center){
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Đăng nhập", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = email,
                onValueChange = {email = it},
                label = { Text("Email")},
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedLabelColor = Color.Black,
                    unfocusedLabelColor = Color.Black,
                    cursorColor = Color.Black
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = {password = it},
                label = { Text("Mật khẩu")},
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedLabelColor = Color.Black,
                    unfocusedLabelColor = Color.Black,
                    cursorColor = Color.Black
                )
            )
            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = {
                // 4. Bắt đầu luồng: Xác thực Firebase
                Firebase.auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // 5. Thành công trên Firebase, LẤY ID TOKEN
                            Firebase.auth.currentUser?.getIdToken(true)?.addOnSuccessListener { result ->
                                val idToken = result.token
                                // 6. Gửi ID Token đến ViewModel để gọi Backend
                                viewModel.handleFirebaseLogin(idToken ?: "")
                            }?.addOnFailureListener {
                                // Lỗi lấy ID Token (thường do mạng hoặc cấu hình)
                                Toast.makeText(context, "Lỗi lấy Firebase Token", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            // Lỗi xác thực Firebase
                            Toast.makeText(context, task.exception?.message ?: "Đăng nhập Firebase thất bại", Toast.LENGTH_SHORT).show()
                        }
                    }
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Đăng nhập")
            }
            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = {
                navController.navigate("signup") {
                    popUpTo("login") {inclusive = true}
                }
            }) {
                Text("Bạn chưa có tài khoản? Đăng ký")
            }
        }
    }
}