package com.example.hexrootscan.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hexrootscan.logic.ScannerViewModel
import com.example.hexrootscan.logic.Screen
import com.example.hexrootscan.ui.components.HexButton
import com.example.hexrootscan.ui.theme.HexAccent
import com.example.hexrootscan.ui.theme.HexPanel
import com.example.hexrootscan.ui.theme.HexText

@Composable
fun SettingsScreen(viewModel: ScannerViewModel) {
    val isDarkMode = viewModel.isDarkMode
    val currentAccent = if (isDarkMode) HexAccent else Color(0xFF0066FF)
    val currentPanel = if (isDarkMode) HexPanel else Color.White
    val currentText = if (isDarkMode) HexText else Color(0xFF333333)

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("SYSTEM SETTINGS", color = currentAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = currentPanel),
            border = BorderStroke(1.dp, currentAccent.copy(0.2f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DarkMode, contentDescription = null, tint = currentAccent)
                    Spacer(Modifier.width(12.dp))
                    Text("DARK MODE", color = currentText, modifier = Modifier.weight(1f))
                    Switch(
                        checked = viewModel.isDarkMode,
                        onCheckedChange = { viewModel.isDarkMode = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = currentAccent)
                    )
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = currentAccent.copy(0.1f))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Key, contentDescription = null, tint = currentAccent)
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("SHODAN API KEY", color = currentText)
                        Text(if (viewModel.shodanKey.isEmpty()) "Not set" else "••••••••••••", color = currentText.copy(0.5f), fontSize = 10.sp)
                    }
                    IconButton(onClick = { viewModel.currentScreen = Screen.SHODAN }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = currentAccent)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("LOGS & STORAGE", color = currentAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = currentPanel),
            border = BorderStroke(1.dp, currentAccent.copy(0.2f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                HexButton("CLEAR ALL LOGS", Icons.Default.DeleteSweep, accent = currentAccent, accentLow = currentAccent.copy(0.1f), panel = currentPanel) {
                    viewModel.clearLogs()
                }
                Spacer(modifier = Modifier.height(12.dp))
                HexButton("RESET TERMINAL", Icons.Default.Terminal, accent = currentAccent, accentLow = currentAccent.copy(0.1f), panel = currentPanel) {
                    viewModel.terminalLogs = listOf("HEX-ROOT-TERMINAL v1.1", "Root/User Hybrid Mode Active", "")
                }
                Spacer(modifier = Modifier.height(12.dp))
                HexButton("OPEN EXPORTS", Icons.Default.FolderZip, accent = currentAccent, accentLow = currentAccent.copy(0.1f), panel = currentPanel) {
                    viewModel.currentScreen = Screen.EXPLORER
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Text("SYSTEM VERSION: 2.1.0", color = currentText.copy(0.4f), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
        Text("DEVICE MODE: ${if (viewModel.logs.any { it.contains("ROOT NOT FOUND") }) "USER" else "ROOT/HYBRID"}", color = currentText.copy(0.4f), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
    }
}
