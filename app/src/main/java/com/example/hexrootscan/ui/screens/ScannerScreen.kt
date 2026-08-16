package com.example.hexrootscan.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hexrootscan.logic.ScannerViewModel
import com.example.hexrootscan.logic.Screen
import com.example.hexrootscan.ui.components.HexButton
import com.example.hexrootscan.ui.components.HexInput
import com.example.hexrootscan.ui.components.StatusLed
import com.example.hexrootscan.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(viewModel: ScannerViewModel) {
    val isDarkMode = viewModel.isDarkMode
    val currentPanel = if (isDarkMode) HexPanel else Color.White
    val currentAccent = if (isDarkMode) HexAccent else Color(0xFF0066FF)
    val currentAccentLow = if (isDarkMode) HexAccentLow else Color(0xFFD0E0FF)
    val currentText = if (isDarkMode) HexText else Color(0xFF333333)
    val currentOk = if (isDarkMode) HexOk else Color(0xFF008800)
    val terminalBg = if (isDarkMode) Color.Black else Color(0xFFE9EDF0)

    var showOptionsSuggestions by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Column(modifier = Modifier.padding(8.dp).fillMaxSize()) {
        Text("COMMAND CONFIGURATION", color = currentAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))
        Card(colors = CardDefaults.cardColors(containerColor = currentPanel), border = BorderStroke(1.dp, currentAccentLow), shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(8.dp)) {
                HexInput(viewModel.target, { viewModel.target = it }, "TARGET HOST / IP", Icons.Default.Language, currentAccent)
                Spacer(modifier = Modifier.height(6.dp))
                Box {
                    HexInput(
                        value = viewModel.options,
                        onValueChange = { viewModel.options = it },
                        label = "SCAN OPTIONS",
                        icon = Icons.Default.Tune,
                        accent = currentAccent,
                        trailingIcon = {
                            Row {
                                IconButton(onClick = { showHelpDialog = true }) {
                                    Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = "Help", tint = currentAccent.copy(0.7f), modifier = Modifier.size(20.dp))
                                }
                                IconButton(onClick = { showOptionsSuggestions = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Suggestions", tint = currentAccent.copy(0.7f), modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = showOptionsSuggestions,
                        onDismissRequest = { showOptionsSuggestions = false },
                        modifier = Modifier.background(currentPanel).border(1.dp, currentAccentLow)
                    ) {
                        listOf("Quick Ping" to "-sn", "Turbo Scan" to "-F -T5 --open", "Stealth" to "-sS -Pn -T4", "Aggressive" to "-A -v -T4", "All Ports" to "-p- -T4", "Services" to "-sV -sC", "Fast Scan" to "-F -T4", "OS Detect" to "-O --osscan-guess").forEach { (name, cmd) ->
                            DropdownMenuItem(text = { Column { Text(name, color = currentAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold); Text(cmd, color = currentText.copy(0.7f), fontSize = 10.sp, fontFamily = FontFamily.Monospace) } }, onClick = { viewModel.options = cmd; showOptionsSuggestions = false })
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text("QUICK ACTIONS", color = currentAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))
        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(scrollState), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            HexButton("NMAP", Icons.Default.Router, accent = currentAccent, accentLow = currentAccentLow, panel = currentPanel) { val t = viewModel.target.trim(); if (t.isNotEmpty()) viewModel.runRootCommand("nmap ${viewModel.options} $t") else viewModel.runRootCommand("echo '![ERR] NO TARGET SPECIFIED'") }
            HexButton("TRIAGE", Icons.Default.Explore, accent = Color.Yellow, accentLow = Color.Yellow.copy(0.2f), panel = currentPanel) { viewModel.runTriage() }
            HexButton("NIKTO", Icons.Default.BugReport, accent = currentAccent, accentLow = currentAccentLow, panel = currentPanel) { val t = viewModel.target.trim(); if (t.isNotEmpty()) viewModel.runRootCommand("perl /data/data/com.termux/files/home/nikto/program/nikto.pl -h $t") else viewModel.runRootCommand("echo '![ERR] NO TARGET SPECIFIED'") }
            HexButton("WHATWEB", Icons.Default.Language, accent = currentAccent, accentLow = currentAccentLow, panel = currentPanel) { val t = viewModel.target.trim(); if (t.isNotEmpty()) viewModel.runRootCommand("ruby /data/data/com.termux/files/home/WhatWeb/whatweb $t") else viewModel.runRootCommand("echo '![ERR] NO TARGET SPECIFIED'") }
            HexButton("WHOIS", Icons.Default.Info, accent = currentAccent, accentLow = currentAccentLow, panel = currentPanel) { val t = viewModel.target.trim(); if (t.isNotEmpty()) viewModel.runRootCommand("whois $t") else viewModel.runRootCommand("echo '![ERR] NO TARGET SPECIFIED'") }
            HexButton("DNSENUM", Icons.Default.Dns, accent = currentAccent, accentLow = currentAccentLow, panel = currentPanel) { viewModel.currentScreen = Screen.DNSENUM }
            HexButton("STOP", Icons.Default.Stop, isError = true, accent = currentAccent, accentLow = currentAccentLow, panel = currentPanel) { viewModel.runRootCommand("pkill nmap || pkill perl || pkill ruby") }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 4.dp).fillMaxWidth()) {
            Icon(Icons.Default.Terminal, contentDescription = null, tint = currentAccent, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text("LIVE TERMINAL OUTPUT (TRIAGE MODE)", color = currentAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            StatusLed(viewModel.status)
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = { viewModel.isDarkMode = !viewModel.isDarkMode }, modifier = Modifier.size(24.dp)) { Icon(imageVector = Icons.Default.DarkMode, contentDescription = "Theme Toggle", tint = currentAccent.copy(alpha = 0.5f), modifier = Modifier.size(16.dp)) }
        }
        
        Box(modifier = Modifier.weight(1f).fillMaxWidth().background(terminalBg, RoundedCornerShape(8.dp)).border(1.dp, currentAccentLow, RoundedCornerShape(8.dp)).padding(1.dp)) {
            Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(if (isDarkMode) Color(0xFF080808) else Color(0xFFF0F2F5), terminalBg))))
            SelectionContainer {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(10.dp)) {
                    items(viewModel.logs) { log ->
                        val color = when {
                            log.contains("open", true) || log.contains("vulnerable", true) || 
                            log.contains("found", true) || log.contains("[✔]", true) || 
                            log.contains("[200 OK]", true) || log.contains("Title[", true) ||
                            log.contains("Country[", true) || log.contains("HTTPS[", true) -> currentOk
                            
                            log.contains("port", true) || log.contains("service", true) || 
                            log.contains("warning", true) || log.contains("[#]", true) ||
                            log.contains("IP[", true) || log.contains("Email[", true) -> Color.Yellow
                            
                            log.contains("![ERR]", true) || log.contains("failed", true) || 
                            log.contains("denied", true) || log.contains("![CRITICAL]", true) -> Color.Red
                            
                            else -> currentText.copy(alpha = 0.7f)
                        }
                        Text(text = log, color = color, fontFamily = FontFamily.Monospace, fontSize = 11.sp, lineHeight = 14.sp)
                    }
                }
            }
        }

        if (showHelpDialog) {
            AlertDialog(
                onDismissRequest = { showHelpDialog = false },
                containerColor = currentPanel,
                title = { Text("NMAP PARAMETERS HELP", color = currentAccent, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold) },
                text = {
                    SelectionContainer {
                        Text(
                            text = "-sS  -> SYN Scan (rápido, sigiloso)\n-sT  -> TCP Connect Scan\n-sU  -> UDP Scan\n-sV  -> Detección de versiones\n-A   -> Escaneo agresivo (SO, scripts, traceroute)\n-O   -> Detección de sistema operativo\n--script vuln -> Scripts de vulnerabilidades\n-p 80,443 -> Puertos específicos\n-p-  -> Todos los puertos (1–65535)\n-T4  -> Acelera el escaneo\n--open -> Solo mostrar puertos abiertos\n-v   -> Verbose\n--script xxx -> NSE script",
                            color = currentText,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                    }
                },
                confirmButton = { TextButton(onClick = { showHelpDialog = false }) { Text("UNDERSTOOD", color = currentAccent) } }
            )
        }

        if (viewModel.showTriageDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.showTriageDialog = false },
                containerColor = currentPanel,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Explore, contentDescription = null, tint = Color.Yellow)
                        Spacer(Modifier.width(8.dp))
                        Text("🧭 TRIAGE ENGINE", color = Color.Yellow, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column {
                        Text("TARGET: ${viewModel.target.ifEmpty { "UNDEFINED" }}", color = currentAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        if (viewModel.triageResults.isEmpty()) {
                            Text("NO OPEN PORTS DETECTED IN LOGS.", color = currentText.copy(0.6f), fontSize = 11.sp)
                        } else {
                            LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                                items(viewModel.triageResults) { (label, cmd) ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { 
                                            viewModel.showTriageDialog = false
                                            viewModel.runRootCommand(cmd) 
                                        },
                                        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(0.3f)),
                                        border = BorderStroke(1.dp, Color.Yellow.copy(0.3f))
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Text(label, color = Color.Yellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Text(cmd, color = currentText, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = { TextButton(onClick = { viewModel.showTriageDialog = false }) { Text("CLOSE", color = Color.Gray) } }
            )
        }
    }
}
