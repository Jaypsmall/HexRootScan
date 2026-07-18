package com.example.hexrootscan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hexrootscan.ui.theme.HexRootScanTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader

// --- SISTEMA DE COLORES HEX DEMON ---
val HexBg = Color(0xFF050505)
val HexPanel = Color(0xFF0F0F0F)
val HexAccent = Color(0xFFFF0000)
val HexAccentLow = Color(0xFF550000)
val HexText = Color(0xFFBBBBBB)
val HexOk = Color(0xFF00FF41)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HexRootScanTheme {
                HexRootReconApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HexRootReconApp() {
    var isDarkMode by remember { mutableStateOf(true) }
    
    val currentBg = if (isDarkMode) HexBg else Color(0xFFF4F7FA)
    val currentPanel = if (isDarkMode) HexPanel else Color.White
    val currentAccent = if (isDarkMode) HexAccent else Color(0xFF0066FF)
    val currentAccentLow = if (isDarkMode) HexAccentLow else Color(0xFFD0E0FF)
    val currentText = if (isDarkMode) HexText else Color(0xFF333333)
    val currentOk = if (isDarkMode) HexOk else Color(0xFF008800)
    val terminalBg = if (isDarkMode) Color.Black else Color(0xFFE9EDF0)

    var target by remember { mutableStateOf("") }
    var options by remember { mutableStateOf("-sS -Pn -T4") }
    var logs by remember { mutableStateOf(listOf("💀 SYSTEM INITIALIZED - ROOT ACCESS GRANTED")) }
    var showMenu by remember { mutableStateOf(false) }
    var showShodanDialog by remember { mutableStateOf(false) }
    var showOptionsSuggestions by remember { mutableStateOf(false) }
    var shodanKey by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    fun runRootCommand(command: String) {
        scope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) { logs = logs + "[#] > $command" }
            try {
                val process = Runtime.getRuntime().exec("su")
                val os = DataOutputStream(process.outputStream)
                os.writeBytes("export PATH=/data/data/com.termux/files/usr/bin:\$PATH\n")
                os.writeBytes("export LD_LIBRARY_PATH=/data/data/com.termux/files/usr/lib\n")
                os.writeBytes("$command\n")
                os.writeBytes("exit\n")
                os.flush()

                BufferedReader(InputStreamReader(process.inputStream)).forEachLine { line ->
                    scope.launch(Dispatchers.Main) { logs = logs + line }
                }
                BufferedReader(InputStreamReader(process.errorStream)).forEachLine { line ->
                    scope.launch(Dispatchers.Main) { logs = logs + "![ERR] $line" }
                }
                process.waitFor()
                withContext(Dispatchers.Main) { logs = logs + "[✔] SESSION_FINISHED" }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { logs = logs + "![CRITICAL] ${e.message}" }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = currentBg,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "HEX ROOT SCAN",
                            color = currentAccent,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 2.sp
                            )
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(Icons.Default.Settings, contentDescription = "Config", tint = currentAccent)
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(currentPanel).border(1.dp, currentAccent)
                    ) {
                        DropdownMenuItem(
                            text = { Text("FULL INSTALL (RUBY/PERL)", color = currentText) },
                            leadingIcon = { Icon(Icons.Default.Download, contentDescription = null, tint = currentAccent) },
                            onClick = {
                                showMenu = false
                                runRootCommand("pkg update -y && pkg install perl ruby nmap dnsutils whois -y")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("CLEAR TERMINAL", color = currentText) },
                            leadingIcon = { Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = currentAccent) },
                            onClick = {
                                showMenu = false
                                logs = listOf("💀 TERMINAL RESET")
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = currentPanel)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(8.dp).fillMaxSize()) {
            
            // --- SECCIÓN: CONFIGURACIÓN ---
            Text("COMMAND CONFIGURATION", color = currentAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = currentPanel),
                border = BorderStroke(1.dp, currentAccentLow),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    HexInput(target, { target = it }, "TARGET HOST / IP", Icons.Default.Language, currentAccent)
                    Spacer(modifier = Modifier.height(6.dp))
                    Box {
                        HexInput(
                            value = options,
                            onValueChange = { options = it },
                            label = "SCAN OPTIONS",
                            icon = Icons.Default.Tune,
                            accent = currentAccent,
                            trailingIcon = {
                                IconButton(onClick = { showOptionsSuggestions = true }) {
                                    Icon(Icons.Default.Menu, contentDescription = "Suggestions", tint = currentAccent.copy(0.7f), modifier = Modifier.size(20.dp))
                                }
                            }
                        )
                        DropdownMenu(
                            expanded = showOptionsSuggestions,
                            onDismissRequest = { showOptionsSuggestions = false },
                            modifier = Modifier.background(currentPanel).border(1.dp, currentAccentLow)
                        ) {
                            listOf(
                                "Quick Ping" to "-sn",
                                "Turbo Scan" to "-F -T5 --open",
                                "Stealth" to "-sS -Pn -T4",
                                "Aggressive" to "-A -v -T4",
                                "All Ports" to "-p- -T4",
                                "Services" to "-sV -sC",
                                "Fast Scan" to "-F -T4",
                                "OS Detect" to "-O --osscan-guess"
                            ).forEach { (name, cmd) ->
                                DropdownMenuItem(
                                    text = { Column {
                                        Text(name, color = currentAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text(cmd, color = currentText.copy(0.7f), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                    }},
                                    onClick = {
                                        options = cmd
                                        showOptionsSuggestions = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- SECCIÓN: ACCIONES ---
            Text("QUICK ACTIONS", color = currentAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HexButton("NMAP", Icons.Default.Router, accent = currentAccent, accentLow = currentAccentLow, panel = currentPanel) { runRootCommand("nmap $options $target") }
                HexButton("NIKTO", Icons.Default.BugReport, accent = currentAccent, accentLow = currentAccentLow, panel = currentPanel) { runRootCommand("/data/data/com.termux/files/usr/bin/perl /data/data/com.termux/files/home/nikto/program/nikto.pl -h $target") }
                HexButton("WHATWEB", Icons.Default.Language, accent = currentAccent, accentLow = currentAccentLow, panel = currentPanel) { runRootCommand("/data/data/com.termux/files/usr/bin/ruby /data/data/com.termux/files/home/WhatWeb/whatweb $target") }
                HexButton("WHOIS", Icons.Default.Info, accent = currentAccent, accentLow = currentAccentLow, panel = currentPanel) { runRootCommand("whois $target") }
                HexButton("DNSENUM", Icons.Default.Dns, accent = currentAccent, accentLow = currentAccentLow, panel = currentPanel) { runRootCommand("/data/data/com.termux/files/usr/bin/perl /data/data/com.termux/files/home/dnsenum/dnsenum.pl $target") }
                HexButton("SHODAN", Icons.Default.Search, accent = currentAccent, accentLow = currentAccentLow, panel = currentPanel) { showShodanDialog = true }
                HexButton("STOP", Icons.Default.Stop, isError = true, accent = currentAccent, accentLow = currentAccentLow, panel = currentPanel) { runRootCommand("pkill nmap || pkill perl || pkill ruby") }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- SECCIÓN: TERMINAL ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 4.dp).fillMaxWidth()
            ) {
                Icon(Icons.Default.Terminal, contentDescription = null, tint = currentAccent, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text("LIVE TERMINAL OUTPUT", color = currentAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                IconButton(
                    onClick = { isDarkMode = !isDarkMode },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DarkMode,
                        contentDescription = "Theme Toggle",
                        tint = currentAccent.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(terminalBg, RoundedCornerShape(8.dp))
                    .border(1.dp, currentAccentLow, RoundedCornerShape(8.dp))
                    .padding(1.dp)
            ) {
                // Efecto de gradiente sutil para la terminal
                Box(modifier = Modifier.fillMaxSize().background(
                    Brush.verticalGradient(listOf(if (isDarkMode) Color(0xFF080808) else Color(0xFFF0F2F5), terminalBg))
                ))
                
                LazyColumn(modifier = Modifier.fillMaxSize().padding(10.dp)) {
                    items(logs) { log ->
                        Text(
                            text = log,
                            color = when {
                                log.startsWith("!") -> currentAccent
                                log.startsWith("[#]") -> if (isDarkMode) Color.Cyan else Color(0xFF0056D2)
                                log.startsWith("[✔]") -> currentOk
                                else -> currentOk.copy(alpha = 0.8f)
                            },
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }

        if (showShodanDialog) {
            AlertDialog(
                onDismissRequest = { showShodanDialog = false },
                containerColor = currentPanel,
                title = { Text("SHODAN API ACCESS", color = currentAccent, fontFamily = FontFamily.Monospace) },
                text = {
                    OutlinedTextField(
                        value = shodanKey,
                        onValueChange = { shodanKey = it },
                        label = { Text("API KEY", color = currentAccent.copy(0.5f)) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = androidx.compose.ui.text.TextStyle(color = if (isDarkMode) Color.White else Color.Black, fontFamily = FontFamily.Monospace),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = currentAccent, unfocusedBorderColor = Color.DarkGray)
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        showShodanDialog = false
                        runRootCommand("curl -s https://api.shodan.io/shodan/host/$target?key=$shodanKey")
                    }) { Text("CONNECT", color = currentAccent, fontWeight = FontWeight.Bold) }
                },
                dismissButton = {
                    TextButton(onClick = { showShodanDialog = false }) { Text("CANCEL", color = Color.Gray) }
                }
            )
        }
    }
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
        textStyle = androidx.compose.ui.text.TextStyle(color = if (accent == HexAccent) Color.White else Color.Black, fontFamily = FontFamily.Monospace, fontSize = 14.sp),
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
