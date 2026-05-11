package com.example.myapplication.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.model.ArtRecommendation
import com.example.myapplication.ui.theme.HeritageGold
import com.example.myapplication.ui.theme.KarnatakaRed

@Composable
fun AIRecommendationStrip(
    recommendations: List<ArtRecommendation>,
    onNavigate: (String) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        Text(
            text = "KALA SUGGESTS FOR YOU",
            style = MaterialTheme.typography.labelSmall,
            color = KarnatakaRed,
            modifier = Modifier.padding(horizontal = 24.dp),
            letterSpacing = 2.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(Modifier.height(12.dp))
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(recommendations) { rec ->
                Surface(
                    modifier = Modifier
                        .width(280.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .clickable { onNavigate(rec.name) },
                    color = Color.White.copy(alpha = 0.5f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, HeritageGold.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Text("✨", fontSize = 18.sp)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = rec.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = KarnatakaRed
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = rec.reason,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}
