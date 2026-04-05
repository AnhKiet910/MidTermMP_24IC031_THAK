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

    // 2. Tạo tài khoản: Vừa lưu Auth vừa lưu Firestore (Sửa lỗi bạn gặp)
    fun addUserWithAuth(email: String, pass: String, role: String, imageUrl: String, onComplete: (Boolean) -> Unit) {
        auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val uid = task.result?.user?.uid ?: ""
                val userMap = hashMapOf(
                    "id" to uid,
                    "email" to email,
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
}