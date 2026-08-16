package com.example.hexrootscan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hexrootscan.logic.ScannerViewModel
import com.example.hexrootscan.logic.Screen
import com.example.hexrootscan.ui.components.DrawerItem
import com.example.hexrootscan.ui.screens.*
import com.example.hexrootscan.ui.theme.*
import kotlinx.coroutines.launch

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
                DrawerItem("DNS ENUMERATION", Icons.Default.Dns, viewModel.currentScreen == Screen.DNSENUM, currentAccent, currentPanel) { 
                    viewModel.currentScreen = Screen.DNSENUM
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
                DrawerItem("TOOL INSTALLER", Icons.Default.Download, viewModel.currentScreen == Screen.INSTALLER, currentAccent, currentPanel) { 
                    viewModel.currentScreen = Screen.INSTALLER
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
                    Text("HexRootScan v1.0.2", color = currentText.copy(alpha = 0.8f), fontSize = 11.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                    Text("Created by JAYLIZ with ❤️", color = currentText.copy(alpha = 0.4f), fontSize = 9.sp, fontWeight = FontWeight.Thin, fontFamily = FontFamily.Monospace)
                }
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = currentBg,
            topBar = {
                Column {
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
                                        text = { Text("OPEN INSTALLER", color = currentText) },
                                        leadingIcon = { Icon(Icons.Default.Build, contentDescription = null, tint = currentAccent) },
                                        onClick = {
                                            showMenu = false
                                            viewModel.currentScreen = Screen.INSTALLER
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
                    HorizontalDivider(color = currentAccent.copy(alpha = 0.5f), thickness = 0.5.dp)
                }
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                when (viewModel.currentScreen) {
                    Screen.SCANNER -> ScannerScreen(viewModel)
                    Screen.TERMINAL -> TerminalScreen(viewModel)
                    Screen.EXPLORER -> ExplorerScreen(viewModel)
                    Screen.DNSENUM -> DnsenumScreen(viewModel)
                    Screen.INSTALLER -> InstallerScreen(viewModel)
                    Screen.SHODAN -> ShodanScreen(viewModel)
                    Screen.SETTINGS -> SettingsScreen(viewModel)
                    else -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("MÓDULO EN DESARROLLO", color = currentAccent, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
