package com.example.midtermmp_24ic031.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.midtermmp_24ic031.service.FirebaseService

class AddUserActivity : ComponentActivity() {
    private val service = FirebaseService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Kiểm tra xem là SỬA hay THÊM dựa vào Intent
        val updateId = intent.getStringExtra("USER_ID")
        val isEditMode = updateId != null

        setContent {
            // Khai báo các biến lưu trữ nội dung nhập vào
            var email by remember { mutableStateOf(intent.getStringExtra("USER_EMAIL") ?: "") }
            var password by remember { mutableStateOf("") }
            var role by remember { mutableStateOf(intent.getStringExtra("USER_ROLE") ?: "user") }
            var imageUrl by remember { mutableStateOf(intent.getStringExtra("USER_IMAGE") ?: "") }
            var isLoading by remember { mutableStateOf(false) }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                Text(
                    text = if (isEditMode) "CẬP NHẬT NHÂN VIÊN" else "THÊM NHÂN VIÊN MỚI",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email (Username)") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isEditMode // Không cho sửa email nếu là chế độ Sửa
                )

                if (!isEditMode) { // Chỉ hiện ô Password khi Thêm mới
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Mật khẩu") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = role,
                    onValueChange = { role = it },
                    label = { Text("Vai trò (admin/user)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("Link ảnh nhân viên") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (isLoading) {
                    CircularProgressIndicator()
                } else {
                    Button(
                        onClick = {
                            if (email.isEmpty() || imageUrl.isEmpty() || (!isEditMode && password.isEmpty())) {
                                Toast.makeText(this@AddUserActivity, "Vui lòng nhập đủ thông tin!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            isLoading = true
                            if (isEditMode) {
                                // Logic Sửa (Chỉ cập nhật Firestore)
                                // Bạn có thể thêm hàm update trong FirebaseService nếu cần
                                Toast.makeText(this@AddUserActivity, "Tính năng sửa đang cập nhật", Toast.LENGTH_SHORT).show()
                                finish()
                            } else {
                                // Logic Thêm: Gọi Service để lưu cả Auth lẫn Database
                                service.addUserWithAuth(email, password, role, imageUrl) { success ->
                                    isLoading = false
                                    if (success) {
                                        Toast.makeText(this@AddUserActivity, "Thêm thành công!", Toast.LENGTH_SHORT).show()
                                        finish()
                                    } else {
                                        Toast.makeText(this@AddUserActivity, "Lỗi tạo tài khoản!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isEditMode) "LƯU CẬP NHẬT" else "TẠO TÀI KHOẢN")
                    }
                }
            }
        }
    }
}