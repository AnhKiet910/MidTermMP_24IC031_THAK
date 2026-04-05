package com.example.midtermmp_24ic031.ui

import android.content.Intent
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var email by remember { mutableStateOf("") }
            var password by remember { mutableStateOf("") }
            var isLoading by remember { mutableStateOf(false) }

            Column(
                modifier = Modifier.fillMaxSize().padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("ĐĂNG NHẬP HỆ THỐNG", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Mật khẩu") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(20.dp))

                if (isLoading) {
                    CircularProgressIndicator()
                } else {
                    Button(
                        onClick = {
                            if (email.isEmpty() || password.isEmpty()) {
                                Toast.makeText(this@LoginActivity, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            isLoading = true
                            // BƯỚC 1: Đăng nhập vào Firebase Auth
                            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        // BƯỚC 2: Kiểm tra hồ sơ ở Firestore xem còn tồn tại không
                                        fetchUserRoleAndNavigate(email) {
                                            isLoading = false // Callback để tắt loading khi xong việc
                                        }
                                    } else {
                                        isLoading = false
                                        Toast.makeText(this@LoginActivity, "Sai tài khoản hoặc mật khẩu", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("ĐĂNG NHẬP")
                    }
                }
            }
        }
    }

    // Hàm kiểm tra hồ sơ và Phân quyền
    private fun fetchUserRoleAndNavigate(email: String, onFinished: () -> Unit) {
        FirebaseFirestore.getInstance().collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { docs ->
                onFinished() // Tắt vòng xoay loading

                if (!docs.isEmpty) {
                    // TRƯỜNG HỢP: Tài khoản tồn tại hợp lệ
                    val role = docs.documents[0].getString("role") ?: "user"
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("USER_ROLE", role)
                    startActivity(intent)
                    finish()
                } else {
                    // TRƯỜNG HỢP: Auth đúng nhưng Firestore trống (Admin đã xóa user)
                    FirebaseAuth.getInstance().signOut() // Đuổi ra ngay lập tức
                    Toast.makeText(this, "Tài khoản này đã bị xóa hoặc vô hiệu hóa!", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener {
                onFinished()
                Toast.makeText(this, "Lỗi kết nối cơ sở dữ liệu", Toast.LENGTH_SHORT).show()
            }
    }
}