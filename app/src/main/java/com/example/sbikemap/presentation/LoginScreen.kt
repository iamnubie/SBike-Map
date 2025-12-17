package com.example.sbikemap.presentation

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.sbikemap.R
import com.example.sbikemap.presentation.viewmodel.AuthViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavHostController,
    viewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    // State quản lý việc ẩn/hiện mật khẩu
    var isPasswordVisible by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current // Quản lý focus để chuyển ô nhập
    val authState by viewModel.authState.collectAsState()

    // 1. Tách logic đăng nhập ra hàm riêng để tái sử dụng (cho nút Bấm và nút Enter bàn phím)
    val onLoginRequest = {
        if (email.isNotBlank() && password.isNotBlank()) {
            // Ẩn bàn phím khi bắt đầu login
            focusManager.clearFocus()

            Firebase.auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Firebase.auth.currentUser?.getIdToken(false)?.addOnSuccessListener { result ->
                            val idToken = result.token
                            viewModel.handleFirebaseLogin(idToken ?: "")
                        }?.addOnFailureListener {
                            Toast.makeText(context, "Lỗi lấy Firebase Token", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, task.exception?.message ?: "Đăng nhập thất bại", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            Toast.makeText(context, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthViewModel.AuthState.Success -> {
                val response = (authState as AuthViewModel.AuthState.Success).response
                Toast.makeText(context, "Đăng nhập thành công! Hello ${response.email}", Toast.LENGTH_SHORT).show()
                viewModel.resetState()
                navController.navigate("map_route") {
                    popUpTo("login") { inclusive = true }
                }
            }
            is AuthViewModel.AuthState.Error -> {
                val message = (authState as AuthViewModel.AuthState.Error).message
                Toast.makeText(context, "Lỗi Backend: $message", Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            is AuthViewModel.AuthState.Loading -> {
                Toast.makeText(context, "Đang xác thực...", Toast.LENGTH_SHORT).show()
            }
            else -> { /* Idle */ }
        }
    }

    // 2. Layout chính: Dùng Column trực tiếp với Scroll và ImePadding
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .imePadding() // Tự động đẩy layout lên khi bàn phím hiện
            .verticalScroll(rememberScrollState()), // Cho phép cuộn nếu màn hình nhỏ
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center // Căn giữa nội dung khi bàn phím ẩn
    ) {
        Text("Đăng nhập", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            // Cấu hình gợi ý Email và nút Next
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email, // Hiện gợi ý email trên bàn phím
                imeAction = ImeAction.Next // [UX] Nút Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) } // Nhảy xuống ô password
            ),
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
            onValueChange = { password = it },
            label = { Text("Mật khẩu") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            // Logic ẩn hiện mật khẩu
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val iconRes = if (isPasswordVisible) R.drawable.openeye else R.drawable.closeeye

                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = if (isPasswordVisible) "Ẩn mật khẩu" else "Hiện mật khẩu",
                        modifier = Modifier.size(24.dp), // Kích thước chuẩn, cân đối
                        tint = Color.Gray
                    )
                }
            },
            // Cấu hình nút Done (Hoàn tất) để login luôn
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { onLoginRequest() } // Bấm Done là Login luôn
            ),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedLabelColor = Color.Black,
                unfocusedLabelColor = Color.Black,
                cursorColor = Color.Black
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onLoginRequest() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Đăng nhập")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = {
            navController.navigate("signup") {
                popUpTo("login") { inclusive = true }
            }
        }) {
            Text("Bạn chưa có tài khoản? Đăng ký")
        }
    }
}