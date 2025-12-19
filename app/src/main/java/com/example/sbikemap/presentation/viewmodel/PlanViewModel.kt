package com.example.sbikemap.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sbikemap.utils.AIJourneyPlanner
import com.example.sbikemap.utils.ChatMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlanViewModel : ViewModel() {
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // Khởi tạo đoạn chat với kết quả ban đầu từ MapScreen
    fun initializePlan(initialPlan: String) {
        if (_messages.value.isEmpty()) {
            _messages.value = listOf(
                // Context ẩn để AI biết ngữ cảnh
                ChatMessage("system", "Bạn là trợ lý hành trình đạp xe. Người dùng đã có lịch trình, giờ họ muốn hỏi thêm chi tiết."),
                ChatMessage("assistant", initialPlan) // Lời giải ban đầu của AI
            )
        }
    }

    // Gửi tin nhắn mới
    fun sendMessage(text: String) {
        if (text.isBlank()) return

        val userMsg = ChatMessage("user", text)
        // Cập nhật UI ngay lập tức
        _messages.value = _messages.value + userMsg
        _isLoading.value = true

        viewModelScope.launch {
            // Gọi AI với toàn bộ lịch sử chat để nó nhớ ngữ cảnh
            val responseText = AIJourneyPlanner.chatWithAI(_messages.value)

            val aiMsg = ChatMessage("assistant", responseText)
            _messages.value = _messages.value + aiMsg
            _isLoading.value = false
        }
    }
}