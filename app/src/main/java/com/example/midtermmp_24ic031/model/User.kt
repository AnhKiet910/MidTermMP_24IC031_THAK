package com.example.midtermmp_24ic031.model

data class User(
    var id: String = "",
    val email : String = "",
    val password: String = "",
    val role: String = "",
    val imageUrl: String = ""
)