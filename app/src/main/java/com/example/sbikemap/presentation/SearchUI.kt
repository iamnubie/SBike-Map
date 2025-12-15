package com.example.sbikemap.presentation

/*import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.mapbox.common.MapboxOptions // Import quan trọng
import com.mapbox.geojson.Point
import com.mapbox.search.autocomplete.PlaceAutocomplete
import com.mapbox.search.autocomplete.PlaceAutocompleteSuggestion
import kotlinx.coroutines.launch // Import cho Coroutine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBarWithAutocomplete(
    onLocationSelected: (Point, String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var active by remember { mutableStateOf(false) }
    var suggestions by remember { mutableStateOf<List<PlaceAutocompleteSuggestion>>(emptyList()) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope() // 1. Tạo Scope để chạy hàm suspend

    // 2. Khởi tạo theo tài liệu mới: Set token vào MapboxOptions
    // Lưu ý: Tốt nhất nên set token ở MainActivity, nhưng đặt tạm ở đây để fix lỗi
    SideEffect {
        MapboxOptions.accessToken = context.getString(com.example.sbikemap.R.string.mapbox_access_token)
    }

    // Khởi tạo PlaceAutocomplete (không truyền token vào constructor nữa)
    val placeAutocomplete = remember { PlaceAutocomplete.create() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp, start = 16.dp, end = 16.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            color = Color.White
        ) {
            TextField(
                value = query,
                onValueChange = { newText ->
                    query = newText
                    active = true
                    if (newText.isNotEmpty()) {
                        // 3. Gọi hàm suggestions trong Coroutine Scope
                        scope.launch {
                            val response = placeAutocomplete.suggestions(newText)
                            response.onValue { results ->
                                suggestions = results
                            }.onError { e ->
                                suggestions = emptyList()
                            }
                        }
                    } else {
                        suggestions = emptyList()
                    }
                },
                placeholder = { Text("Tìm địa điểm...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = {
                            query = ""
                            suggestions = emptyList()
                            active = false
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray)
                        }
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (active && suggestions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                modifier = Modifier.fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(8.dp)),
                shape = RoundedCornerShape(8.dp),
                color = Color.White
            ) {
                LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                    items(suggestions) { suggestion ->
                        SuggestionItem(suggestion) {
                            query = suggestion.name
                            active = false
                            suggestions = emptyList()

                            // 4. Gọi hàm select trong Coroutine Scope
                            scope.launch {
                                // Gọi select() trả về response
                                val response = placeAutocomplete.select(suggestion)

                                // Xử lý kết quả trả về
                                response.onValue { result ->
                                    val point = result.coordinate
                                    onLocationSelected(point, suggestion.name)
                                }.onError { e ->
                                    // Xử lý lỗi nếu cần
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SuggestionItem(
    suggestion: PlaceAutocompleteSuggestion,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null // Tạm tắt hiệu ứng gợn sóng để tránh crash do lệch version
            ) { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray)
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = suggestion.name, style = MaterialTheme.typography.bodyLarge)
            // 5. Sửa 'address' thành 'formattedAddress' (thuộc tính đúng của Suggestion)
            suggestion.formattedAddress?.let {
                Text(text = it, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}*/

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mapbox.common.MapboxOptions
import com.mapbox.geojson.Point
import com.mapbox.search.autocomplete.PlaceAutocomplete
import com.mapbox.search.autocomplete.PlaceAutocompleteSuggestion
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteSearchBox(
    isExpanded: Boolean,
    onExpandRequest: () -> Unit,
    originAddress: String?,
    destinationAddress: String?,
    onOriginSelected: (Point?, String) -> Unit,      // Callback khi chọn điểm đi (Null = Vị trí của bạn)
    onDestinationSelected: (Point?, String) -> Unit   // Callback khi chọn điểm đến
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // State cho text hiển thị
    var originQuery by remember { mutableStateOf("Vị trí của bạn") }
    var destQuery by remember { mutableStateOf("") }

    // Lắng nghe sự thay đổi từ bên ngoài để cập nhật text trong ô nhập
    LaunchedEffect(originAddress) {
        if (originAddress != null) {
            originQuery = originAddress
        }
    }

    LaunchedEffect(destinationAddress) {
        if (destinationAddress != null) {
            destQuery = destinationAddress
        }
    }

    // State xác định đang focus vào ô nào: "NONE", "ORIGIN", "DEST"
    var activeField by remember { mutableStateOf("NONE") }

    // List gợi ý
    var suggestions by remember { mutableStateOf<List<PlaceAutocompleteSuggestion>>(emptyList()) }

    // Setup Mapbox Search
    SideEffect {
        MapboxOptions.accessToken = context.getString(com.example.sbikemap.R.string.mapbox_access_token)
    }
    val placeAutocomplete = remember { PlaceAutocomplete.create() }

    // Hàm chung để search
    fun performSearch(query: String) {
        if (query.isNotEmpty()) {
            scope.launch {
                val response = placeAutocomplete.suggestions(query)
                response.onValue { suggestions = it }.onError { suggestions = emptyList() }
            }
        } else {
            suggestions = emptyList()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp, start = 16.dp, end = 16.dp)
    ) {
        if (!isExpanded) {
            // 1. GIAO DIỆN THU GỌN (Giống Google Maps mặc định)
            CompactSearchBar(onClick = onExpandRequest)
        } else {
            // --- KHUNG NHẬP LIỆU (2 Ô) ---
            Surface(
                modifier = Modifier.shadow(8.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                color = Color.White
            ) {
                Column(modifier = Modifier.padding(8.dp)) {

                    // 1. Ô ĐIỂM ĐI (ORIGIN)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Home,
                            contentDescription = "Start",
                            tint = Color.Blue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        TextField(
                            value = originQuery,
                            onValueChange = {
                                originQuery = it
                                performSearch(it) // Search khi gõ
                            },
                            placeholder = { Text("Chọn điểm đi") },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                cursorColor = Color.Black
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { focusState ->
                                    if (focusState.isFocused) {
                                        activeField = "ORIGIN"
                                        // Nếu đang là mặc định thì xóa text để user nhập
                                        if (originQuery == "Vị trí của bạn") originQuery = ""
                                    }
                                },
                            trailingIcon = {
                                if (activeField == "ORIGIN" && originQuery.isNotEmpty()) {
                                    IconButton(onClick = {
                                        originQuery = ""
                                        suggestions = emptyList()
                                        onOriginSelected(null, "Vị trí của bạn")
                                    }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray)
                                    }
                                }
                            }
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 32.dp), thickness = 0.5.dp)

                    // 2. Ô ĐIỂM ĐẾN (DESTINATION)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = "End",
                            tint = Color.Red,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        TextField(
                            value = destQuery,
                            onValueChange = {
                                destQuery = it
                                performSearch(it)
                            },
                            placeholder = { Text("Chọn điểm đến") },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                cursorColor = Color.Black
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { focusState ->
                                    if (focusState.isFocused) {
                                        activeField = "DEST"
                                    }
                                },
                            trailingIcon = {
                                if (activeField == "DEST" && destQuery.isNotEmpty()) {
                                    IconButton(onClick = {
                                        destQuery = ""
                                        suggestions = emptyList()
                                        onDestinationSelected(null, "")
                                    }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }

        // --- DANH SÁCH GỢI Ý ---
        if (isExpanded && activeField != "NONE" && suggestions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                modifier = Modifier.shadow(4.dp, RoundedCornerShape(8.dp)),
                shape = RoundedCornerShape(8.dp),
                color = Color.White
            ) {
                LazyColumn(modifier = Modifier.heightIn(max = 250.dp)) {
                    // Item đặc biệt cho "Vị trí của bạn" (Chỉ hiện khi chọn điểm đi)
                    if (activeField == "ORIGIN") {
                        item {
                            SuggestionItemRaw("Vị trí của bạn (Mặc định)", "Vị trí hiện tại GPS") {
                                originQuery = "Vị trí của bạn"
                                activeField = "NONE"
                                suggestions = emptyList()
                                focusManager.clearFocus()
                                onOriginSelected(null, "Vị trí của bạn") // Null -> User location
                            }
                        }
                    }

                    items(suggestions) { suggestion ->
                        SuggestionItem(suggestion) {
                            // Xử lý khi chọn
                            scope.launch {
                                val response = placeAutocomplete.select(suggestion)
                                response.onValue { result ->
                                    val point = result.coordinate
                                    val name = suggestion.name

                                    if (activeField == "ORIGIN") {
                                        originQuery = name
                                        onOriginSelected(point, name)
                                    } else {
                                        destQuery = name
                                        onDestinationSelected(point, name)
                                    }

                                    // Reset trạng thái
                                    activeField = "NONE"
                                    suggestions = emptyList()
                                    focusManager.clearFocus()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// [MỚI] Giao diện thanh tìm kiếm thu gọn (Google Maps Style)
@Composable
fun CompactSearchBar(onClick: () -> Unit) {
    // 1. Tạo InteractionSource để quản lý trạng thái click
    val interactionSource = remember { MutableInteractionSource() }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(6.dp, RoundedCornerShape(28.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null // Tắt hiệu ứng gợn sóng để tránh crash
            ) { onClick() },
        shape = RoundedCornerShape(28.dp),
        color = Color.White
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Icon(Icons.Default.Search, contentDescription = null, tint = Color.Black)
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Tìm địa điểm ở đây...",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.weight(1f))
            // Có thể thêm icon Avatar hoặc Mic ở đây nếu muốn giống hệt Google
        }
    }
}

@Composable
fun SuggestionItem(
    suggestion: PlaceAutocompleteSuggestion,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null // Tắt hiệu ứng ripple để tránh crash
            ) { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray)
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = suggestion.name, style = MaterialTheme.typography.bodyLarge)
            suggestion.formattedAddress?.let {
                Text(text = it, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}

// Item dùng để hiển thị text cứng (như "Vị trí của bạn")
@Composable
fun SuggestionItemRaw(title: String, subtitle: String, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Home, contentDescription = null, tint = Color.Blue)
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}