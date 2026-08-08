package com.example.hexrootscan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hexrootscan.logic.ScannerViewModel
import com.example.hexrootscan.logic.Screen
import com.example.hexrootscan.ui.components.DrawerItem
import com.example.hexrootscan.ui.components.HexButton
import com.example.hexrootscan.ui.components.HexInput
import com.example.hexrootscan.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel: ScannerViewModel by lazy { 
            androidx.lifecycle.ViewModelProvider(this)[ScannerViewModel::class.java]
        }
        
        setContent {
            LaunchedEffect(viewModel.isDarkMode) {
                enableEdgeToEdge(
                    statusBarStyle = if (viewModel.isDarkMode) {
                        SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
                    }
                )
            }
            
            HexRootScanTheme(darkTheme = viewModel.isDarkMode) {
                HexRootReconApp(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HexRootReconApp(viewModel: ScannerViewModel = viewModel()) {
    val isDarkMode = viewModel.isDarkMode
    val currentPanel = if (isDarkMode) HexPanel else Color.White
    val currentAccent = if (isDarkMode) HexAccent else Color(0xFF0066FF)
    val currentAccentLow = if (isDarkMode) HexAccentLow else Color(0xFFD0E0FF)
    val currentText = if (isDarkMode) HexText else Color(0xFF333333)
    val currentBg = if (isDarkMode) HexBg else Color(0xFFF4F7FA)

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showMenu by remember { mutableStateOf(false) }

    val titleShadow = Shadow(
        color = Color.Black.copy(alpha = 0.8f),
        offset = Offset(6f, 6f),
        blurRadius = 12f
    )

    val hexTitle = buildAnnotatedString {
        val capsStyle = SpanStyle(
            color = if (isDarkMode) HexAccent else Color(0xFF3E6BDB),
            fontWeight = FontWeight.Black,
            shadow = titleShadow,
            fontFamily = FontFamily.Monospace
        )
        val themeStyle = SpanStyle(
            color = if (isDarkMode) Color.White else Color(0xFF0D0D0D),
            fontWeight = FontWeight.Black,
            shadow = titleShadow,
            fontFamily = FontFamily.Monospace
        )
        withStyle(style = themeStyle) { append("😈 ") }
        withStyle(style = capsStyle) { append("HEX ") }
        withStyle(style = capsStyle) { append("ROOT ") }
        withStyle(style = themeStyle) { append("SCAN") }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = currentPanel,
                drawerTonalElevation = 0.dp,
                drawerShape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp),
                modifier = Modifier
                    .width(280.dp)
                    .fillMaxHeight()
                    .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Vertical))
                    .drawBehind {
                        val stroke = 1.dp.toPx()
                        val r = 16.dp.toPx()
                        val p = Path().apply {
                            moveTo(0f, 0f)
                            lineTo(size.width - r, 0f)
                            arcTo(Rect(size.width - 2 * r, 0f, size.width, 2 * r), -90f, 90f, false)
                            lineTo(size.width, size.height - r)
                            arcTo(Rect(size.width - 2 * r, size.height - 2 * r, size.width, size.height), 0f, 90f, false)
                            lineTo(0f, size.height)
                        }
                        drawPath(p, currentAccentLow, style = Stroke(stroke))
                    }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(Brush.verticalGradient(listOf(currentAccentLow.copy(alpha = 0.3f), Color.Transparent)))
                        .padding(20.dp),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Column {
                        Text("😈", fontSize = 40.sp, modifier = Modifier.padding(bottom = 8.dp), style = TextStyle(shadow = titleShadow))
                        Row {
                            Text("HEX ROOT ", color = currentAccent, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace, shadow = titleShadow))
                            Text("SCAN", color = if (isDarkMode) Color.White else Color(0xFF0D0D0D), style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace, shadow = titleShadow))
                        }
                        Text("CONTROL PANEL v2.0", color = currentText, fontSize = 10.sp, letterSpacing = 2.sp)
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                DrawerItem("NETWORK SCANNER", Icons.Default.Radar, viewModel.currentScreen == Screen.SCANNER, currentAccent, currentPanel) { 
                    viewModel.currentScreen = Screen.SCANNER
                    scope.launch { drawerState.close() } 
                }
                DrawerItem("TERMINAL ACCESS", Icons.Default.Terminal, viewModel.currentScreen == Screen.TERMINAL, currentAccent, currentPanel) { 
                    viewModel.currentScreen = Screen.TERMINAL
                    scope.launch { drawerState.close() } 
                }
                DrawerItem("ROOT EXPLORER", Icons.Default.FolderZip, viewModel.currentScreen == Screen.EXPLORER, currentAccent, currentPanel) { 
                    viewModel.currentScreen = Screen.EXPLORER
                    viewModel.refreshExplorer()
                    scope.launch { drawerState.close() } 
                }
                DrawerItem("SHODAN INTEL", Icons.Default.Search, viewModel.currentScreen == Screen.SHODAN, currentAccent, currentPanel) { 
                    viewModel.currentScreen = Screen.SHODAN
                    scope.launch { drawerState.close() } 
                }
                DrawerItem("SYSTEM SETTINGS", Icons.Default.Settings, viewModel.currentScreen == Screen.SETTINGS, currentAccent, currentPanel) { 
                    viewModel.currentScreen = Screen.SETTINGS
                    scope.launch { drawerState.close() } 
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                Column(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("HexRootScan v1.0.1", color = currentText.copy(alpha = 0.6f), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Text("Created by JAYLIZ with ❤️", color = currentText.copy(alpha = 0.6f), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = currentBg,
            topBar = {
                CenterAlignedTopAppBar(
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Open Menu", tint = currentAccent)
                        }
                    },
                    title = {
                        Text(text = hexTitle, modifier = Modifier.offset(x = (-12).dp), style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 2.sp))
                    },
                    actions = {
                        if (viewModel.currentScreen == Screen.SCANNER) {
                            IconButton(onClick = { showMenu = !showMenu }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = currentAccent)
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
                                        viewModel.runRootCommand("pkg update -y && pkg install perl ruby nmap dnsutils whois -y")
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("EXPORT RESULTS", color = currentText) },
                                    leadingIcon = { Icon(Icons.Default.Save, contentDescription = null, tint = currentAccent) },
                                    onClick = {
                                        showMenu = false
                                        viewModel.exportLogs()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("CLEAR TERMINAL", color = currentText) },
                                    leadingIcon = { Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = currentAccent) },
                                    onClick = {
                                        showMenu = false
                                        viewModel.clearLogs()
                                    }
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = currentPanel)
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                when (viewModel.currentScreen) {
                    Screen.SCANNER -> ScannerScreen(viewModel)
                    Screen.TERMINAL -> TerminalScreen(viewModel)
                    Screen.EXPLORER -> ExplorerScreen(viewModel)
                    else -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("MÓDULO EN DESARROLLO", color = currentAccent, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

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
    var showShodanDialog by remember { mutableStateOf(false) }
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
                            IconButton(onClick = { showOptionsSuggestions = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Suggestions", tint = currentAccent.copy(0.7f), modifier = Modifier.size(20.dp))
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
            HexButton("NMAP", Icons.Default.Router, accent = currentAccent, accentLow = currentAccentLow, panel = currentPanel) { val t = viewModel.target.trim(); if (t.isNotEmpty()) viewModel.runRootCommand("nmap ${viewModel.options} $t") else viewModel.logs = viewModel.logs + "![ERR] NO TARGET SPECIFIED" }
            HexButton("TRIAGE", Icons.Default.Explore, accent = Color.Yellow, accentLow = Color.Yellow.copy(0.2f), panel = currentPanel) { viewModel.runTriage() }
            HexButton("NIKTO", Icons.Default.BugReport, accent = currentAccent, accentLow = currentAccentLow, panel = currentPanel) { val t = viewModel.target.trim(); if (t.isNotEmpty()) viewModel.runRootCommand("/data/data/com.termux/files/usr/bin/perl /data/data/com.termux/files/home/nikto/program/nikto.pl -h $t") else viewModel.logs = viewModel.logs + "![ERR] NO TARGET SPECIFIED" }
            HexButton("WHATWEB", Icons.Default.Language, accent = currentAccent, accentLow = currentAccentLow, panel = currentPanel) { val t = viewModel.target.trim(); if (t.isNotEmpty()) viewModel.runRootCommand("/data/data/com.termux/files/usr/bin/ruby /data/data/com.termux/files/home/WhatWeb/whatweb $t") else viewModel.logs = viewModel.logs + "![ERR] NO TARGET SPECIFIED" }
            HexButton("WHOIS", Icons.Default.Info, accent = currentAccent, accentLow = currentAccentLow, panel = currentPanel) { val t = viewModel.target.trim(); if (t.isNotEmpty()) viewModel.runRootCommand("whois $t") else viewModel.logs = viewModel.logs + "![ERR] NO TARGET SPECIFIED" }
            HexButton("DNSENUM", Icons.Default.Dns, accent = currentAccent, accentLow = currentAccentLow, panel = currentPanel) { val t = viewModel.target.trim(); if (t.isNotEmpty()) viewModel.runRootCommand("/data/data/com.termux/files/usr/bin/perl /data/data/com.termux/files/home/dnsenum/dnsenum.pl $t") else viewModel.logs = viewModel.logs + "![ERR] NO TARGET SPECIFIED" }
            HexButton("SHODAN", Icons.Default.Search, accent = currentAccent, accentLow = currentAccentLow, panel = currentPanel) { showShodanDialog = true }
            HexButton("STOP", Icons.Default.Stop, isError = true, accent = currentAccent, accentLow = currentAccentLow, panel = currentPanel) { viewModel.runRootCommand("pkill nmap || pkill perl || pkill ruby") }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 4.dp).fillMaxWidth()) {
            Icon(Icons.Default.Terminal, contentDescription = null, tint = currentAccent, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text("LIVE TERMINAL OUTPUT (TRIAGE MODE)", color = currentAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            IconButton(onClick = { viewModel.isDarkMode = !viewModel.isDarkMode }, modifier = Modifier.size(24.dp)) { Icon(imageVector = Icons.Default.DarkMode, contentDescription = "Theme Toggle", tint = currentAccent.copy(alpha = 0.5f), modifier = Modifier.size(16.dp)) }
        }
        
        Box(modifier = Modifier.fillMaxSize().background(terminalBg, RoundedCornerShape(8.dp)).border(1.dp, currentAccentLow, RoundedCornerShape(8.dp)).padding(1.dp)) {
            Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(if (isDarkMode) Color(0xFF080808) else Color(0xFFF0F2F5), terminalBg))))
            SelectionContainer {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(10.dp)) {
                    items(viewModel.logs) { log ->
                        val color = when {
                            log.contains("open", true) || log.contains("vulnerable", true) || log.contains("found", true) || log.contains("[✔]", true) -> currentOk
                            log.contains("port", true) || log.contains("service", true) || log.contains("warning", true) || log.contains("[#]", true) -> Color.Yellow
                            log.contains("![ERR]", true) || log.contains("failed", true) || log.contains("denied", true) -> Color.Red
                            else -> currentText.copy(alpha = 0.7f)
                        }
                        Text(text = log, color = color, fontFamily = FontFamily.Monospace, fontSize = 11.sp, lineHeight = 14.sp)
                    }
                }
            }
        }

        // --- SHODAN DIALOG ---
        if (showShodanDialog) {
            AlertDialog(
                onDismissRequest = { showShodanDialog = false },
                containerColor = currentPanel,
                title = { Text("SHODAN API ACCESS", color = currentAccent, fontFamily = FontFamily.Monospace) },
                text = { 
                    OutlinedTextField(
                        value = viewModel.shodanKey, 
                        onValueChange = { viewModel.shodanKey = it }, 
                        label = { Text("API KEY", color = currentAccent.copy(0.5f)) }, 
                        modifier = Modifier.fillMaxWidth(), 
                        textStyle = TextStyle(color = if (isDarkMode) Color.White else Color.Black, fontFamily = FontFamily.Monospace),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = currentAccent,
                            unfocusedBorderColor = Color.DarkGray
                        )
                    ) 
                },
                confirmButton = { 
                    TextButton(onClick = { 
                        showShodanDialog = false 
                        viewModel.runRootCommand("curl -s https://api.shodan.io/shodan/host/${viewModel.target}?key=${viewModel.shodanKey}") 
                    }) { 
                        Text("CONNECT", color = currentAccent, fontWeight = FontWeight.Bold) 
                    } 
                },
                dismissButton = { 
                    TextButton(onClick = { showShodanDialog = false }) { 
                        Text("CANCEL", color = Color.Gray) 
                    } 
                }
            )
        }

        // --- TRIAGE DIALOG ---
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
                confirmButton = {
                    TextButton(onClick = { viewModel.showTriageDialog = false }) {
                        Text("CLOSE", color = Color.Gray, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}

@Composable
fun TerminalScreen(viewModel: ScannerViewModel) {
    val isDarkMode = viewModel.isDarkMode
    val currentAccent = if (isDarkMode) HexAccent else Color(0xFF0066FF)
    val currentAccentLow = if (isDarkMode) HexAccentLow else Color(0xFFD0E0FF)
    val terminalBg = if (isDarkMode) Color.Black else Color(0xFFE9EDF0)
    val currentOk = if (isDarkMode) HexOk else Color(0xFF008800)
    
    val listState = rememberLazyListState()
    
    LaunchedEffect(viewModel.terminalLogs.size) {
        if (viewModel.terminalLogs.isNotEmpty()) {
            listState.animateScrollToItem(viewModel.terminalLogs.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        Text("SYSTEM ROOT CONSOLE", color = currentAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
        
        Box(modifier = Modifier.weight(1f).fillMaxWidth().background(terminalBg, RoundedCornerShape(8.dp)).border(1.dp, currentAccentLow, RoundedCornerShape(8.dp)).padding(8.dp)) {
            SelectionContainer {
                LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                    items(viewModel.terminalLogs) { log ->
                        Text(
                            text = log,
                            color = when {
                                log.startsWith("root@hex:#") -> currentAccent
                                log.contains("![ERR]") || log.contains("![CRITICAL]") -> Color.Red
                                else -> currentOk
                            },
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().background(currentAccentLow.copy(alpha = 0.1f), RoundedCornerShape(8.dp)).border(1.dp, currentAccentLow.copy(alpha = 0.5f), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp)) {
            Text(">", color = currentAccent, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            TextField(
                value = viewModel.terminalInput,
                onValueChange = { viewModel.terminalInput = it },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = currentAccent,
                    focusedTextColor = if (isDarkMode) Color.White else Color.Black
                ),
                textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 14.sp),
                placeholder = { Text("Enter command...", color = Color.Gray, fontSize = 12.sp) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { viewModel.runTerminalCommand() }),
                singleLine = true
            )
        }
    }
}

@Composable
fun ExplorerScreen(viewModel: ScannerViewModel) {
    val isDarkMode = viewModel.isDarkMode
    val currentAccent = if (isDarkMode) HexAccent else Color(0xFF0066FF)
    val currentPanel = if (isDarkMode) HexPanel else Color.White
    val currentText = if (isDarkMode) HexText else Color(0xFF333333)

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("ROOT EXPORTS EXPLORER", color = currentAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            IconButton(onClick = { viewModel.refreshExplorer() }) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = currentAccent)
            }
        }
        
        Card(
            modifier = Modifier.fillMaxSize(),
            colors = CardDefaults.cardColors(containerColor = currentPanel),
            border = BorderStroke(1.dp, currentAccent.copy(0.3f))
        ) {
            if (viewModel.explorerFiles.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("NO EXPORTED FILES FOUND", color = currentText.copy(0.5f), fontFamily = FontFamily.Monospace)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                    items(viewModel.explorerFiles) { file ->
                        ListItem(
                            modifier = Modifier.fillMaxWidth().clickable { viewModel.openFile(file) },
                            headlineContent = { Text(file.name, color = currentText, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold) },
                            supportingContent = { 
                                val date = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(file.lastModified()))
                                Text("${file.length() / 1024} KB | $date", color = currentText.copy(0.6f), fontSize = 10.sp) 
                            },
                            leadingContent = { Icon(Icons.Default.Description, contentDescription = null, tint = currentAccent) },
                            trailingContent = {
                                IconButton(onClick = { 
                                    file.delete()
                                    viewModel.refreshExplorer()
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(0.6f))
                                }
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                        HorizontalDivider(color = currentAccent.copy(0.1f))
                    }
                }
            }
        }

        // --- Visor de Archivos ---
        if (viewModel.selectedFileContent != null) {
            AlertDialog(
                onDismissRequest = { viewModel.selectedFileContent = null },
                modifier = Modifier.fillMaxSize().padding(16.dp),
                properties = DialogProperties(usePlatformDefaultWidth = false),
                containerColor = currentPanel,
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("REPORT VIEWER", color = currentAccent, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = { viewModel.selectedFileContent = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = currentAccent)
                        }
                    }
                },
                text = {
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.2f), RoundedCornerShape(8.dp)).border(1.dp, currentAccent.copy(0.2f), RoundedCornerShape(8.dp)).padding(8.dp)) {
                        SelectionContainer {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                item {
                                    Text(
                                        text = viewModel.selectedFileContent ?: "",
                                        color = if (isDarkMode) Color.White else Color.Black,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.selectedFileContent = null }) {
                        Text("CLOSE", color = currentAccent, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}
