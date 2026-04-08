package com.example.midtermmp_24ic031.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.midtermmp_24ic031.R
import com.example.midtermmp_24ic031.service.FirebaseService
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : ComponentActivity() {
    private val service = FirebaseService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var email by remember { mutableStateOf("") }
            var password by remember { mutableStateOf("") }
            var confirmPassword by remember { mutableStateOf("") }

            // Trạng thái ẩn hiện cho 2 ô mật khẩu
            var passwordVisible by remember { mutableStateOf(false) }
            var confirmPasswordVisible by remember { mutableStateOf(false) }
            var isLoading by remember { mutableStateOf(false) }

            // Thêm màu nền nhạt đồng bộ với Login
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {

                    // --- PHẦN HEADER ---
                    Image(
                        painter = painterResource(id = R.drawable.logo_vku),
                        contentDescription = "VKU Logo",
                        modifier = Modifier
                            .size(120.dp) // Nhỏ hơn màn hình login 1 xíu cho gọn
                            .padding(bottom = 16.dp)
                    )

                    Text(
                        text = "ĐĂNG KÝ TÀI KHOẢN",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "Tạo tài khoản để tham gia hệ thống",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
                    )

                    // --- PHẦN FORM CARD (Bóng nổi 3D) ---
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            // Ô Email
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("Email") },
                                leadingIcon = {
                                    Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            // Ô Mật khẩu
                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("Mật khẩu") },
                                leadingIcon = {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                },
                                trailingIcon = {
                                    val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(image, contentDescription = null)
                                    }
                                },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            // Ô Xác nhận Mật khẩu
                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                label = { Text("Xác nhận mật khẩu") },
                                leadingIcon = {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                },
                                trailingIcon = {
                                    val image = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                        Icon(image, contentDescription = null)
                                    }
                                },
                                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(modifier = Modifier.height(32.dp))

                            // --- NÚT BẤM VÀ LOADING ---
                            if (isLoading) {
                                CircularProgressIndicator()
                            } else {
                                Button(
                                    onClick = {
                                        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                                            Toast.makeText(this@RegisterActivity, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                        if (password != confirmPassword) {
                                            Toast.makeText(this@RegisterActivity, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }

                                        isLoading = true

                                        // Logic tạo tài khoản giữ nguyên
                                        service.addUserWithAuth(email, password, "user", "") { success ->
                                            isLoading = false
                                            if (success) {
                                                Toast.makeText(this@RegisterActivity, "Đăng ký thành công!", Toast.LENGTH_SHORT).show()

                                                val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                                                val intent = Intent(this@RegisterActivity, UserProfileActivity::class.java).apply {
                                                    putExtra("USER_ID", uid)
                                                    putExtra("USER_EMAIL", email)
                                                    putExtra("USER_ROLE", "user")
                                                    putExtra("USER_IMAGE", "")
                                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                }
                                                startActivity(intent)
                                                finish()
                                            } else {
                                                Toast.makeText(this@RegisterActivity, "Tài khoản này đã bị xóa hoặc vô hiệu hóa!", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(50.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("ĐĂNG KÝ", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                }
                            }
                        }
                    }

                    // --- LIÊN KẾT QUAY LẠI ---
                    Spacer(modifier = Modifier.height(24.dp))
                    TextButton(onClick = { finish() }) {
                        Text(
                            text = "Đã có tài khoản? Quay lại Đăng nhập",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}