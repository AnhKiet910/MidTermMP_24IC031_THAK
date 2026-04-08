package com.example.midtermmp_24ic031.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.midtermmp_24ic031.service.FirebaseService
import com.google.firebase.auth.FirebaseAuth

class UserProfileActivity : ComponentActivity() {
    private val service = FirebaseService()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val id = intent.getStringExtra("USER_ID") ?: ""
        val email = intent.getStringExtra("USER_EMAIL") ?: ""
        val role = intent.getStringExtra("USER_ROLE") ?: "user"
        val initialImage = intent.getStringExtra("USER_IMAGE") ?: ""

        setContent {
            var imageUrl by remember { mutableStateOf(initialImage) }
            var newPassword by remember { mutableStateOf("") } // Biến lưu mật khẩu mới
            var isLoading by remember { mutableStateOf(false) }

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Hồ sơ của tôi", fontWeight = FontWeight.Bold) },
                        actions = {
                            IconButton(onClick = {
                                FirebaseAuth.getInstance().signOut()
                                val intent = Intent(this@UserProfileActivity, LoginActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                finish()
                            }) {
                                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Đăng xuất", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    )
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()) // Cuộn mượt mà
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(15.dp)
                ) {

                    // --- AVATAR HÌNH TRÒN ---
                    val painter = rememberAsyncImagePainter(
                        model = imageUrl.ifEmpty { "https://cdn-icons-png.flaticon.com/512/149/149071.png" } // Ảnh mặc định nếu trống
                    )
                    Image(
                        painter = painter,
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = {},
                        label = { Text("Email (Tên đăng nhập)") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false
                    )

                    OutlinedTextField(
                        value = role.uppercase(),
                        onValueChange = {},
                        label = { Text("Quyền hạn") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false
                    )

                    OutlinedTextField(
                        value = imageUrl,
                        onValueChange = { imageUrl = it },
                        label = { Text("Link ảnh đại diện") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // --- Ô NHẬP MẬT KHẨU MỚI ---
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("Đổi mật khẩu mới") },
                        placeholder = { Text("Bỏ trống nếu không muốn đổi") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (isLoading) {
                        CircularProgressIndicator()
                    } else {
                        Button(
                            onClick = {
                                isLoading = true
                                service.updateUserProfile(id, imageUrl, newPassword) { success ->
                                    isLoading = false
                                    if (success) {
                                        Toast.makeText(this@UserProfileActivity, "Cập nhật thành công!", Toast.LENGTH_SHORT).show()
                                        newPassword = "" // Reset ô mật khẩu
                                    } else {
                                        Toast.makeText(this@UserProfileActivity, "Lỗi cập nhật!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("LƯU THAY ĐỔI")
                        }
                    }
                }
            }
        }
    }
}