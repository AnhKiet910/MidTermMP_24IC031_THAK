package com.example.midtermmp_24ic031.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.midtermmp_24ic031.model.User

@Composable
fun UserItem(user: User, isAdmin: Boolean, onDelete: () -> Unit, onEdit: () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Hiển thị ảnh bằng Coil (Link ảnh từ Firestore)
            AsyncImage(
                model = user.imageUrl,
                contentDescription = null,
                modifier = Modifier.size(50.dp).clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
                Text(text = user.email, style = MaterialTheme.typography.titleMedium)
                Text(text = "Quyền: ${user.role}", style = MaterialTheme.typography.bodySmall)
            }

            // Chỉ Admin mới thấy nút Menu để Sửa/Xóa
            if (isAdmin) {
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = null)
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(text = { Text("Sửa") }, onClick = { showMenu = false; onEdit() })
                        DropdownMenuItem(text = { Text("Xóa", color = androidx.compose.ui.graphics.Color.Red) },
                            onClick = { showMenu = false; onDelete() })
                    }
                }
            }
        }
    }
}