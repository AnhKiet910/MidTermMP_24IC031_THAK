package com.example.midtermmp_24ic031.service

import com.example.midtermmp_24ic031.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseService {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // 1. Lấy danh sách User và tự động cập nhật (Realtime)
    fun streamUsers(onResult: (List<User>) -> Unit) {
        db.collection("users").addSnapshotListener { value, _ ->
            val users = value?.map { doc ->
                val user = doc.toObject(User::class.java)
                user.id = doc.id
                user
            } ?: emptyList()
            onResult(users)
        }
    }

    // 2. Tạo tài khoản: Vừa lưu Auth vừa lưu Firestore
    fun addUserWithAuth(email: String, pass: String, role: String, imageUrl: String, onComplete: (Boolean) -> Unit) {
        auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val uid = task.result?.user?.uid ?: ""
                val userMap = hashMapOf(
                    "id" to uid,
                    "email" to email,
                    "password" to pass,
                    "role" to role,
                    "imageUrl" to imageUrl
                )
                db.collection("users").document(uid).set(userMap).addOnSuccessListener {
                    onComplete(true)
                }
            } else {
                onComplete(false)
            }
        }
    }

    // 3. Xóa User
    fun deleteUser(id: String) {
        db.collection("users").document(id).delete()
    }

    // 4. Sửa User (Dành cho Admin)
    fun updateUser(id: String, role: String, imageUrl: String, onComplete: (Boolean) -> Unit) {
        val updates = mapOf(
            "role" to role,
            "imageUrl" to imageUrl
        )
        db.collection("users").document(id).update(updates)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    // 5. User tự cập nhật Profile (Đổi ảnh và Mật khẩu)
    fun updateUserProfile(id: String, newImageUrl: String, newPassword: String, onComplete: (Boolean) -> Unit) {
        val updates = mutableMapOf<String, Any>("imageUrl" to newImageUrl)
        if (newPassword.isNotEmpty()) {
            updates["password"] = newPassword // Cập nhật mật khẩu vào Database
        }

        db.collection("users").document(id).update(updates)
            .addOnSuccessListener {
                // Nếu có nhập mật khẩu mới thì cập nhật luôn cả trên Firebase Auth
                if (newPassword.isNotEmpty() && auth.currentUser != null) {
                    auth.currentUser!!.updatePassword(newPassword).addOnCompleteListener { task ->
                        onComplete(task.isSuccessful)
                    }
                } else {
                    onComplete(true)
                }
            }
            .addOnFailureListener { onComplete(false) }
    }
}