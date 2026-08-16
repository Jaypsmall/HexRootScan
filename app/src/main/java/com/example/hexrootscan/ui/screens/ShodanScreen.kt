package com.example.hexrootscan.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hexrootscan.logic.ScannerViewModel
import com.example.hexrootscan.ui.components.HexInput
import com.example.hexrootscan.ui.components.StatusLed
import com.example.hexrootscan.ui.theme.HexAccent
import com.example.hexrootscan.ui.theme.HexAccentLow
import com.example.hexrootscan.ui.theme.HexOk
import com.example.hexrootscan.ui.theme.HexPanel
import com.example.hexrootscan.ui.theme.HexText

@Composable
fun ShodanScreen(viewModel: ScannerViewModel) {
    val isDarkMode = viewModel.isDarkMode
    val currentAccent = if (isDarkMode) HexAccent else Color(0xFF0066FF)
    val currentAccentLow = if (isDarkMode) HexAccentLow else Color(0xFFD0E0FF)
    val currentPanel = if (isDarkMode) HexPanel else Color.White
    val currentText = if (isDarkMode) HexText else Color(0xFF333333)
    val terminalBg = if (isDarkMode) Color.Black else Color(0xFFE9EDF0)
    val currentOk = if (isDarkMode) HexOk else Color(0xFF008800)

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        Text("SHODAN INTELLIGENCE ENGINE", color = currentAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = currentPanel),
            border = BorderStroke(1.dp, currentAccentLow),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                HexInput(viewModel.target, { viewModel.target = it }, "TARGET IP / HOST", Icons.Default.Language, currentAccent)
                Spacer(modifier = Modifier.height(8.dp))
                HexInput(viewModel.shodanKey, { viewModel.shodanKey = it }, "SHODAN API KEY", Icons.Default.Key, currentAccent)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { 
                    viewModel.updateShodanKey(viewModel.shodanKey)
                    viewModel.runRootCommand("curl -s https://api.shodan.io/shodan/host/${viewModel.target}?key=${viewModel.shodanKey}")
                },
                modifier = Modifier.weight(1f).height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = currentAccent),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.WifiTethering, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("API SCAN", fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { 
                    viewModel.runRootCommand("curl -s https://internetdb.shodan.io/${viewModel.target}")
                },
                modifier = Modifier.weight(1f).height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = currentAccentLow),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, currentAccent)
            ) {
                Icon(Icons.Default.CloudQueue, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("FREE SCAN", fontWeight = FontWeight.Bold, color = currentAccent)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 4.dp).fillMaxWidth()) {
            Icon(Icons.Default.Analytics, contentDescription = null, tint = currentAccent, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text("ENGINE RESULTS", color = currentAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            StatusLed(viewModel.status)
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(terminalBg, RoundedCornerShape(8.dp))
                .border(1.dp, currentAccentLow, RoundedCornerShape(8.dp))
                .padding(1.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(if (isDarkMode) Color(0xFF080808) else Color(0xFFF0F2F5), terminalBg))))
            SelectionContainer {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(10.dp)) {
                    items(viewModel.logs.filter { it.contains("shodan", true) || it.contains("{", true) || it.contains("ip_str", true) || it.contains("port", true) || logContainsShodanKeywords(it) }) { log ->
                        Text(
                            text = log,
                            color = when {
                                log.contains("vulnerabilities", true) || log.contains("ports", true) -> currentAccent
                                log.contains("ip_str", true) || log.contains("org", true) -> currentOk
                                else -> currentText.copy(alpha = 0.8f)
                            },
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }
    }
}

private fun logContainsShodanKeywords(text: String): Boolean {
    val keywords = listOf("asn", "country_name", "hostnames", "domains", "isp", "data", "product", "version")
    return keywords.any { text.contains(it, true) }
}
