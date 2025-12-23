package com.example.sbikemap.presentation

import android.widget.Toast
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sbikemap.App
import com.example.sbikemap.data.remote.models.SavePlaceRequest
import com.example.sbikemap.data.remote.models.SmartPlaceResponse
import com.mapbox.common.MapboxOptions
import com.mapbox.geojson.Point
import com.mapbox.maps.Style
import com.mapbox.search.autocomplete.PlaceAutocomplete
import com.mapbox.search.autocomplete.PlaceAutocompleteSuggestion
import kotlinx.coroutines.launch

val CYCLING_CATEGORIES = mapOf(
    "Sửa xe đạp" to "bicycle_shop",
    "Cà phê" to "coffee",
    "Cửa hàng tiện lợi" to "convenience",
    "Nhà vệ sinh" to "restroom",
    "Công viên" to "park",
    "Trạm sạc" to "charging_station",
    "ATM" to "atm"
)
// Định nghĩa sealed class để list có thể chứa cả 3 loại item
sealed class SearchItemUi {
    data class SmartAction(val query: String) : SearchItemUi() // Dòng bấm "Tìm thông minh..."
    data class MapboxItem(val data: PlaceAutocompleteSuggestion) : SearchItemUi() // Kết quả Mapbox
    data class BackendItem(val data: SmartPlaceResponse) : SearchItemUi() // Kết quả từ Backend trả về
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteSearchBox(
    isExpanded: Boolean,
    onExpandRequest: () -> Unit,
    originAddress: String?,
    destinationAddress: String?,
    onOriginSelected: (Point?, String) -> Unit,      // Callback khi chọn điểm đi (Null = Vị trí của bạn)
    onDestinationSelected: (Point?, String) -> Unit,   // Callback khi chọn điểm đến
    onCategorySelected: (String) -> Unit,
    onProfileClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    val appContainer = App.container
    val searchApi = appContainer.searchApi

    var uiSuggestions by remember { mutableStateOf<List<SearchItemUi>>(emptyList()) }
    // State xác định đang focus vào ô nào: "NONE", "ORIGIN", "DEST"
    var activeField by remember { mutableStateOf("NONE") }
    var isLoadingAI by remember { mutableStateOf(false) }

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
    // Hàm Search Mapbox cơ bản
    fun performMapboxSearch(query: String) {
        if (query.isEmpty()) {
            uiSuggestions = emptyList()
            return
        }
        scope.launch {
            val response = placeAutocomplete.suggestions(query)
            response.onValue { mapboxResults ->
                // Tạo list hỗn hợp:
                // 1. Dòng "Tìm kiếm thông minh"
                // 2. Các kết quả từ Mapbox
                val list = mutableListOf<SearchItemUi>()
                list.add(SearchItemUi.SmartAction(query)) // Luôn chèn đầu tiên
                list.addAll(mapboxResults.map { SearchItemUi.MapboxItem(it) })
                uiSuggestions = list
            }.onError { uiSuggestions = emptyList() }
        }
    }
    // Hàm gọi Backend AI Search
    fun performSmartSearch(query: String) {
        isLoadingAI = true
        scope.launch {
            try {
                val results = searchApi.smartSearch(query)
                // Sau khi có kết quả, thay thế toàn bộ list gợi ý bằng kết quả từ AI
                uiSuggestions = results.map { SearchItemUi.BackendItem(it) }
                if (results.isEmpty()) {
                    Toast.makeText(context, "Chưa thấy được địa điểm nào khớp!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Lỗi kết nối AI: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                isLoadingAI = false
            }
        }
    }
    // Hàm lưu địa điểm vào DB (Crowdsourcing)
    fun savePlaceToBackend(name: String, address: String?, category: String?, lat: Double, lng: Double) {
        scope.launch {
            try {
                searchApi.savePlace(
                    SavePlaceRequest(
                        name = name,
                        address = address,
                        // Sử dụng biến category được truyền vào, nếu null thì mới dùng fallback
                        category = category ?: "Địa điểm người dùng chọn",
                        lat = lat,
                        lng = lng
                    )
                )
                android.util.Log.d("SMART_SEARCH", "Đã lưu: $name - $category")
            } catch (e: Exception) {
                android.util.Log.e("SMART_SEARCH", "Lỗi lưu DB: ${e.message}")
            }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp, start = 16.dp, end = 16.dp)
    ) {
        if (!isExpanded) {
            // 1. GIAO DIỆN THU GỌN (Giống Google Maps mặc định)
            CompactSearchBar(
                onClick = onExpandRequest,
                onCategoryClick = onCategorySelected,
                onProfileClick = onProfileClick
            )
        } else {
            // --- KHUNG NHẬP LIỆU (2 Ô) ---
            Surface(
                modifier = Modifier.shadow(8.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                color = Color.White
            ) {
                Column(modifier = Modifier.padding(8.dp)) {

                    // 1. Ô ĐIỂM ĐI (ORIGIN)
                    SearchTextFieldRow(
                        icon = Icons.Default.Home,
                        iconTint = Color.Blue,
                        value = originQuery,
                        placeholder = "Chọn điểm đi",
                        onValueChange = {
                            originQuery = it
                            performMapboxSearch(it) // <--- QUAN TRỌNG: Gọi hàm search mới
                        },
                        onClear = {
                            originQuery = ""
                            onOriginSelected(null, "Vị trí của bạn")
                        },
                        isFocused = activeField == "ORIGIN",
                        onFocus = {
                            activeField = "ORIGIN"
                            // Tự động xóa chữ mặc định để nhập cho nhanh
                            if (originQuery == "Vị trí của bạn") originQuery = ""
                        }
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 32.dp), thickness = 0.5.dp)

                    // 2. Ô ĐIỂM ĐẾN (DESTINATION)
                    SearchTextFieldRow(
                        icon = Icons.Default.LocationOn,
                        iconTint = Color.Red,
                        value = destQuery,
                        placeholder = "Chọn điểm đến",
                        onValueChange = {
                            destQuery = it
                            performMapboxSearch(it) // <--- QUAN TRỌNG: Gọi hàm search mới
                        },
                        onClear = {
                            destQuery = ""
                            onDestinationSelected(null, "")
                        },
                        isFocused = activeField == "DEST",
                        onFocus = { activeField = "DEST" }
                    )
                }
            }
        }

        // --- DANH SÁCH GỢI Ý ---
        // Lưu ý: Đổi điều kiện kiểm tra từ 'suggestions' sang 'uiSuggestions'
        if (isExpanded && activeField != "NONE" && uiSuggestions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                modifier = Modifier.shadow(4.dp, RoundedCornerShape(8.dp)),
                shape = RoundedCornerShape(8.dp),
                color = Color.White
            ) {
                LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) { // Tăng chiều cao lên chút

                    // 1. Item "Vị trí của bạn" (Chỉ hiện khi đang chọn điểm đi)
                    if (activeField == "ORIGIN") {
                        item {
                            SuggestionItemRaw("Vị trí của bạn (Mặc định)", "GPS hiện tại") {
                                originQuery = "Vị trí của bạn"
                                activeField = "NONE"
                                uiSuggestions = emptyList() // Xóa list gợi ý
                                focusManager.clearFocus()
                                onOriginSelected(null, "Vị trí của bạn")
                            }
                        }
                    }

                    // 2. Duyệt qua danh sách hỗn hợp (SearchItemUi)
                    items(uiSuggestions) { item ->
                        val interactionSource = remember { MutableInteractionSource() }
                        when (item) {
                            // --- LOẠI A: Dòng kích hoạt "Tìm kiếm thông minh..." ---
                            is SearchItemUi.SmartAction -> {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(
                                            interactionSource = interactionSource,
                                            indication = null
                                        ) { performSmartSearch(item.query) } // Gọi API AI
                                        .background(Color(0xFFE3F2FD)) // Màu xanh nhạt nổi bật
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isLoadingAI) {
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                    } else {
                                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFF673AB7)) // Màu tím AI
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Tìm kiếm thông minh: \"${item.query}\"",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF673AB7)
                                    )
                                }
                            }

                            // --- LOẠI B: Kết quả từ Mapbox (Cần lấy Category & Lưu về Backend) ---
                            is SearchItemUi.MapboxItem -> {
                                SuggestionItem(item.data) {
                                    scope.launch {
                                        val response = placeAutocomplete.select(item.data)
                                        response.onValue { result ->
                                            val point = result.coordinate
                                            val name = item.data.name
                                            val address = item.data.formattedAddress ?: "Không có địa chỉ"

                                            // Lấy danh mục từ Mapbox & Gửi về Backend
                                            val categoryFromMapbox = result.categories?.joinToString(", ")

                                            savePlaceToBackend(
                                                name = name,
                                                address = address,
                                                category = categoryFromMapbox, // Truyền category vào đây
                                                lat = point.latitude(),
                                                lng = point.longitude()
                                            )
                                            // ----------------------------------------------------

                                            if (activeField == "ORIGIN") {
                                                originQuery = name
                                                onOriginSelected(point, name)
                                            } else {
                                                destQuery = name
                                                onDestinationSelected(point, name)
                                            }

                                            // Reset trạng thái
                                            activeField = "NONE"
                                            uiSuggestions = emptyList()
                                            focusManager.clearFocus()
                                        }
                                    }
                                }
                            }

                            // --- LOẠI C: Kết quả từ Backend AI trả về ---
                            is SearchItemUi.BackendItem -> {
                                SuggestionItemRaw(item.data.name, item.data.address ?: "Gợi ý từ AI") {
                                    // Backend trả về mảng [lng, lat]
                                    val point = Point.fromLngLat(item.data.location.coordinates[0], item.data.location.coordinates[1])
                                    val name = item.data.name

                                    if (activeField == "ORIGIN") {
                                        originQuery = name
                                        onOriginSelected(point, name)
                                    } else {
                                        destQuery = name
                                        onDestinationSelected(point, name)
                                    }

                                    // Reset trạng thái
                                    activeField = "NONE"
                                    uiSuggestions = emptyList()
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
fun CompactSearchBar(
    onClick: () -> Unit,
    onCategoryClick: (String) -> Unit,
    onProfileClick: () -> Unit
) {
    // 1. Tạo InteractionSource để quản lý trạng thái click
    val interactionSource = remember { MutableInteractionSource() }
    var showMenu by remember { mutableStateOf(false) } // State quản lý menu
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
            IconButton(onClick = onProfileClick) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Hồ sơ",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Tìm địa điểm ở đây...",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray,
                modifier = Modifier.weight(1f)
            )
            // Nút Filter/Danh mục
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        Icons.Default.List,
                        contentDescription = "Danh mục",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                // Menu xổ xuống
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    CYCLING_CATEGORIES.forEach { (label, query) ->
                        DropdownMenuItem(
                            text = { Text(label, color = Color.Black) },
                            onClick = {
                                showMenu = false
                                onCategoryClick(query) // Trả về từ khóa (VD: "cafe")
                            },
                            leadingIcon = {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Red)
                            }
                        )
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

// Hàm Helper để vẽ ô nhập liệu cho gọn code
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTextFieldRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    onClear: () -> Unit,
    isFocused: Boolean,
    onFocus: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder) },
            singleLine = true,
            textStyle = TextStyle(fontSize = 15.sp, color = Color.Black),
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
                .onFocusChanged { if (it.isFocused) onFocus() },
            trailingIcon = {
                // Chỉ hiện nút X khi đang focus và có chữ
                if (isFocused && value.isNotEmpty()) {
                    IconButton(onClick = onClear, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray)
                    }
                }
            }
        )
    }
}