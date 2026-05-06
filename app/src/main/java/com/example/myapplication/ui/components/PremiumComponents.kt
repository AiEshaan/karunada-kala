package com.example.myapplication.ui.components

import android.content.ContentValues
import android.graphics.Bitmap
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.io.OutputStream

@Composable
fun LegacyTree(guru: String, artist: String, students: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            "CULTURAL LINEAGE",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.secondary,
            letterSpacing = 1.5.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(24.dp))
        
        LegacyNode(title = "Guru (Teacher)", name = guru, isTop = true)
        LegacyConnector()
        LegacyNode(title = "Preservation Custodian", name = artist, isHighlight = true)
        LegacyConnector()
        LegacyNode(title = "Sangha (Students)", name = students, isBottom = true)
    }
}

@Composable
fun LegacyNode(title: String, name: String, isTop: Boolean = false, isBottom: Boolean = false, isHighlight: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            modifier = Modifier.size(12.dp),
            color = if (isHighlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
            shape = CircleShape
        ) {}
        
        Spacer(Modifier.width(16.dp))
        
        Column {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = if (isHighlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = name,
                style = if (isHighlight) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
                fontWeight = if (isHighlight) FontWeight.ExtraBold else FontWeight.Medium,
                color = if (isHighlight) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun LegacyConnector() {
    Box(
        modifier = Modifier
            .padding(start = 5.dp)
            .width(2.dp)
            .height(32.dp)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
    )
}

@Composable
fun ReceiptDialog(title: String, date: String, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val qrBitmap = remember(title, date) { generateQrCode("KarunadaKala: $title | $date") }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("CULTURAL PASS", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, letterSpacing = 2.sp)
                Spacer(Modifier.height(16.dp))
                
                // QR Code
                qrBitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = "QR Code",
                        modifier = Modifier.size(180.dp).clip(RoundedCornerShape(16.dp))
                    )
                }
                
                Spacer(Modifier.height(24.dp))
                
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
                Text(date, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                
                Spacer(Modifier.height(32.dp))
                
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                
                Spacer(Modifier.height(16.dp))
                
                Text(
                    "Present this digital pass at the venue to begin your journey.",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )
                
                Spacer(Modifier.height(24.dp))
                
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Text("Done", fontWeight = FontWeight.Bold)
                }
                
                Spacer(Modifier.height(8.dp))
                
                TextButton(onClick = {
                    qrBitmap?.let { bitmap ->
                        saveImageToGallery(context, bitmap, "KarunadaKala_Pass_${System.currentTimeMillis()}")
                    }
                }) {
                    Text("Save to Gallery 📥", color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

private fun saveImageToGallery(context: android.content.Context, bitmap: Bitmap, name: String) {
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, name)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/KarunadaKala")
        }
    }

    val resolver = context.contentResolver
    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

    uri?.let {
        val outputStream: OutputStream? = resolver.openOutputStream(it)
        outputStream?.use { stream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            Toast.makeText(context, "Pass saved to gallery!", Toast.LENGTH_SHORT).show()
        }
    }
}

private fun generateQrCode(content: String): Bitmap? {
    return try {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 512, 512)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap[x, y] = if (bitMatrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE
            }
        }
        bitmap
    } catch (e: Exception) {
        null
    }
}
