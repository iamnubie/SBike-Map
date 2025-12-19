package com.example.sbikemap.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.sbikemap.presentation.viewmodel.PlanViewModel
import com.example.sbikemap.utils.ChatMessage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanScreen(
    navController: NavController,
    initialPlanContent: String,
    planViewModel: PlanViewModel = viewModel() // ViewModel riêng cho màn này
) {
    // Nạp dữ liệu ban đầu 1 lần duy nhất
    LaunchedEffect(Unit) {
        planViewModel.initializePlan(initialPlanContent)
    }

    val messages by planViewModel.messages.collectAsState()
    val isLoading by planViewModel.isLoading.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Lắng nghe sự thay đổi của bàn phím để cuộn xuống
    val imeInsets = WindowInsets.ime
    val density = LocalDensity.current

    // Tự động cuộn xuống cuối khi có tin nhắn mới HOẶC khi bàn phím hiện lên
    LaunchedEffect(messages.size, imeInsets.getBottom(density)) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Trợ lý hành trình",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Black
                    ) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
    ) { padding ->

        // [QUAN TRỌNG]: Layout chính xử lý bàn phím
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding) // Padding của TopBar
                .imePadding() // [QUAN TRỌNG]: Tự động đẩy layout lên khi bàn phím mở
                .background(Color(0xFFF5F5F5))
        ) {
            // 1. Danh sách tin nhắn (Chiếm hết phần không gian còn lại)
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f) // Chiếm toàn bộ chiều cao trừ thanh input
                    .padding(horizontal = 12.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages) { msg ->
                    if (msg.role != "system") {
                        ChatBubble(message = msg)
                    }
                }
            }

            // 2. Thanh nhập chat (Luôn nằm đáy Column)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Hỏi thêm về calo, địa điểm...") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.LightGray,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        planViewModel.sendMessage(inputText)
                        inputText = ""
                    },
                    enabled = !isLoading && inputText.isNotBlank(),
                    modifier = Modifier.background(
                        color = if (!isLoading && inputText.isNotBlank()) MaterialTheme.colorScheme.primary else Color.Gray,
                        shape = androidx.compose.foundation.shape.CircleShape
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        Icon(Icons.Default.Send, null, tint = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val isUser = message.isUser
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = if (isUser) MaterialTheme.colorScheme.primary else Color.White,
            shape = RoundedCornerShape(
                topStart = 16.dp, topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 0.dp,
                bottomEnd = if (isUser) 0.dp else 16.dp
            ),
            shadowElevation = 2.dp,
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Text(
                text = message.content,
                modifier = Modifier.padding(12.dp),
                color = if (isUser) Color.White else Color.Black,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}