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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.midtermmp_24ic031.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var email by remember { mutableStateOf("") }
            var password by remember { mutableStateOf("") }
            var isLoading by remember { mutableStateOf(false) }

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {

                    // Logo VKU
                    Image(
                        painter = painterResource(id = R.drawable.logo_vku),
                        contentDescription = "VKU Logo",
                        modifier = Modifier
                            .size(150.dp)
                            .padding(bottom = 16.dp)
                    )

                    // Tiêu đề chính in đậm
                    Text(
                        text = "QUẢN LÝ NHÂN SỰ",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Mật khẩu") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(32.dp))

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
                                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            fetchUserRoleAndNavigate(email) {
                                                isLoading = false
                                            }
                                        } else {
                                            isLoading = false
                                            Toast.makeText(this@LoginActivity, "Sai tài khoản hoặc mật khẩu", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(16.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("ĐĂNG NHẬP")
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        TextButton(onClick = {
                            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
                            startActivity(intent)
                        }) {
                            Text("Chưa có tài khoản? Đăng ký ngay")
                        }
                    }
                }
            }
        }
    }

    private fun fetchUserRoleAndNavigate(email: String, onFinished: () -> Unit) {
        FirebaseFirestore.getInstance().collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { docs ->
                onFinished()

                if (!docs.isEmpty) {
                    val role = docs.documents[0].getString("role") ?: "user"
                    val uid = docs.documents[0].id
                    val imageUrl = docs.documents[0].getString("imageUrl") ?: ""

                    // PHÂN QUYỀN ĐIỂM 1.0
                    val intent = if (role.lowercase() == "admin") {
                        Intent(this, MainActivity::class.java)
                    } else {
                        Intent(this, UserProfileActivity::class.java)
                    }

                    intent.putExtra("USER_ID", uid)
                    intent.putExtra("USER_EMAIL", email)
                    intent.putExtra("USER_ROLE", role)
                    intent.putExtra("USER_IMAGE", imageUrl)

                    startActivity(intent)
                    finish()
                } else {
                    FirebaseAuth.getInstance().signOut()
                    Toast.makeText(this, "Tài khoản này đã bị xóa hoặc vô hiệu hóa!", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener {
                onFinished()
                Toast.makeText(this, "Lỗi kết nối cơ sở dữ liệu", Toast.LENGTH_SHORT).show()
            }
    }
}