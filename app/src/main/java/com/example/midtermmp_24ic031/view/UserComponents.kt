package com.example.midtermmp_24ic031.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.midtermmp_24ic031.model.User

@Composable
fun UserItem(user: User, isAdmin: Boolean, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
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
            if (isAdmin) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Xóa", tint = androidx.compose.ui.graphics.Color.Red)
                }
            }
        }
    }
}