package com.example.hexrootscan.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hexrootscan.logic.ExecutionStatus
import com.example.hexrootscan.ui.theme.HexAccent
import com.example.hexrootscan.ui.theme.HexPanel

@Composable
fun StatusLed(status: ExecutionStatus) {
    val infiniteTransition = rememberInfiniteTransition(label = "LedBlink")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Alpha"
    )

    val color = when (status) {
        ExecutionStatus.IDLE -> Color(0xFF00FF41) // Verde
        ExecutionStatus.WORKING -> Color(0xFFFFD700) // Amarillo
        ExecutionStatus.FINISHED -> Color.Red
    }

    val displayAlpha = if (status == ExecutionStatus.WORKING) alpha else 1f

    Box(
        modifier = Modifier
            .size(10.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = displayAlpha))
            .border(1.dp, color.copy(alpha = 0.5f), CircleShape)
            .drawBehind {
                drawCircle(
                    color = color.copy(alpha = 0.4f * displayAlpha),
                    radius = size.minDimension / 1.2f,
                    style = Stroke(width = 4.dp.toPx())
                )
            }
    )
}

@Composable
fun DrawerItem(label: String, icon: ImageVector, selected: Boolean, accent: Color, panel: Color, onClick: () -> Unit) {
    NavigationDrawerItem(
        label = { Text(label, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 12.sp) },
        selected = selected,
        onClick = onClick,
        icon = { Icon(icon, contentDescription = null) },
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = accent.copy(alpha = 0.2f),
            unselectedContainerColor = Color.Transparent,
            selectedIconColor = accent,
            unselectedIconColor = if (panel == HexPanel) Color.Gray else Color.DarkGray,
            selectedTextColor = accent,
            unselectedTextColor = if (panel == HexPanel) Color.White else Color.Black
        )
    )
}

@Composable
fun HexInput(
    value: String, 
    onValueChange: (String) -> Unit, 
    label: String, 
    icon: ImageVector, 
    accent: Color,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange,
        label = { Text(label, fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = accent.copy(0.6f), modifier = Modifier.size(18.dp)) },
        trailingIcon = trailingIcon,
        modifier = Modifier.fillMaxWidth(),
        textStyle = TextStyle(color = if (accent == HexAccent) Color.White else Color.Black, fontFamily = FontFamily.Monospace, fontSize = 14.sp),
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = accent,
            unfocusedBorderColor = Color.DarkGray,
            cursorColor = accent,
            focusedLabelColor = accent,
            unfocusedLabelColor = Color.Gray
        )
    )
}

@Composable
fun HexButton(text: String, icon: ImageVector, isError: Boolean = false, accent: Color, accentLow: Color, panel: Color, onClick: () -> Unit) {
    val isDark = panel != Color.White
    val errorColor = if (isDark) Color.Red else Color(0xFFD32F2F)
    val errorBg = if (isDark) Color(0xFF330000) else Color(0xFFFFEBEE)

    Button(
        onClick = onClick,
        modifier = Modifier.height(42.dp),
        colors = ButtonDefaults.buttonColors(containerColor = if (isError) errorBg else panel),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, if (isError) errorColor else accentLow),
        contentPadding = PaddingValues(horizontal = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon, 
                contentDescription = null, 
                modifier = Modifier.size(16.dp), 
                tint = if (isError) errorColor else if (isDark) Color.White else accent
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text, 
                color = if (isError) errorColor else if (isDark) Color.White else Color.Black, 
                fontSize = 11.sp, 
                fontWeight = FontWeight.Bold, 
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
