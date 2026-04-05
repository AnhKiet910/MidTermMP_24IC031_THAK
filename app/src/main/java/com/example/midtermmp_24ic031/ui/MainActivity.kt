package com.example.midtermmp_24ic031.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.midtermmp_24ic031.model.User
import com.example.midtermmp_24ic031.service.FirebaseService
import com.example.midtermmp_24ic031.view.UserItem
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    private val service = FirebaseService()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val role = intent.getStringExtra("USER_ROLE") ?: "user"
        val isAdmin = role == "admin"

        setContent {
            var userList by remember { mutableStateOf(listOf<User>()) }
            var searchQuery by remember { mutableStateOf("") }

            // --- TÍNH NĂNG 3: TRẠNG THÁI DIALOG XÓA ---
            var showDeleteDialog by remember { mutableStateOf(false) }
            var userToDelete by remember { mutableStateOf<User?>(null) }
            // ----------------------------------------

            val filteredList = userList.filter {
                it.email.contains(searchQuery, ignoreCase = true)
            }

            LaunchedEffect(Unit) {
                service.streamUsers { userList = it }
            }

            // --- GIAO DIỆN DIALOG XÁC NHẬN XÓA ---
            if (showDeleteDialog && userToDelete != null) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("Xác nhận xóa") },
                    text = { Text("Bạn có chắc chắn muốn xóa nhân viên ${userToDelete?.email} không? Hành động này không thể hoàn tác.") },
                    confirmButton = {
                        TextButton(onClick = {
                            userToDelete?.let { service.deleteUser(it.id) }
                            showDeleteDialog = false
                            Toast.makeText(this@MainActivity, "Đã xóa!", Toast.LENGTH_SHORT).show()
                        }) {
                            Text("XÓA", color = Color.Red, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) {
                            Text("HỦY")
                        }
                    }
                )
            }

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Quản lý nhân viên") },
                        actions = {
                            IconButton(onClick = {
                                // FIX: Quay về LoginActivity chuyên nghiệp
                                FirebaseAuth.getInstance().signOut()
                                val intent = Intent(this@MainActivity, LoginActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                finish()
                            }) {
                                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                            }
                        }
                    )
                },
                floatingActionButton = {
                    if (isAdmin) {
                        FloatingActionButton(onClick = {
                            val intent = Intent(this@MainActivity, AddUserActivity::class.java)
                            startActivity(intent)
                        }) { Icon(Icons.Default.Add, contentDescription = null) }
                    }
                }
            ) { innerPadding ->
                LazyColumn(
                    modifier = Modifier.padding(innerPadding).fillMaxSize()
                ) {
                    item { DashboardSection(userList = userList) }

                    item {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                            placeholder = { Text("Tìm theo email...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = null)
                                    }
                                }
                            },
                            singleLine = true,
                            shape = MaterialTheme.shapes.medium
                        )
                    }

                    item {
                        Text(
                            text = if (searchQuery.isEmpty()) "Danh sách nhân sự" else "Kết quả tìm kiếm (${filteredList.size})",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    items(filteredList) { user ->
                        UserItem(
                            user = user,
                            isAdmin = isAdmin,
                            onDelete = {
                                // Thay vì xóa ngay, ta hiện Dialog
                                userToDelete = user
                                showDeleteDialog = true
                            },
                            onEdit = {
                                val intent = Intent(this@MainActivity, AddUserActivity::class.java).apply {
                                    putExtra("USER_ID", user.id)
                                    putExtra("USER_EMAIL", user.email)
                                    putExtra("USER_ROLE", user.role)
                                    putExtra("USER_IMAGE", user.imageUrl)
                                }
                                startActivity(intent)
                            }
                        )
                    }

                    if (filteredList.isEmpty() && searchQuery.isNotEmpty()) {
                        item {
                            Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text("Không tìm thấy nhân viên nào!", color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- DashboardSection và StatCard giữ nguyên như cũ ---
@Composable
fun DashboardSection(userList: List<User>) {
    val total = userList.size
    val adminCount = userList.count { it.role.lowercase() == "admin" }
    val userCount = total - adminCount

    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatCard("Tổng", total.toString(), MaterialTheme.colorScheme.primaryContainer, Modifier.weight(1f))
        StatCard("Admin", adminCount.toString(), Color(0xFFFFE082), Modifier.weight(1f))
        StatCard("Staff", userCount.toString(), Color(0xFFA5D6A7), Modifier.weight(1f))
    }
}

@Composable
fun StatCard(label: String, value: String, color: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(text = label, style = MaterialTheme.typography.labelSmall)
        }
    }
}