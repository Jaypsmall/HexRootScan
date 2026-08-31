package com.example.hexrootscan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.example.hexrootscan.ui.screens.DnsenumScreen
import com.example.hexrootscan.ui.screens.ExplorerScreen
import com.example.hexrootscan.ui.screens.InstallerScreen
import com.example.hexrootscan.ui.screens.ScannerScreen
import com.example.hexrootscan.ui.screens.SettingsScreen
import com.example.hexrootscan.ui.screens.ShodanScreen
import com.example.hexrootscan.ui.screens.TerminalScreen
import com.example.hexrootscan.ui.theme.GrisPlata
import com.example.hexrootscan.ui.theme.HexAccent
import com.example.hexrootscan.ui.theme.HexAccentLow
import com.example.hexrootscan.ui.theme.HexBg
import com.example.hexrootscan.ui.theme.HexPanel
import com.example.hexrootscan.ui.theme.HexRootScanTheme
import com.example.hexrootscan.ui.theme.HexText
import com.example.hexrootscan.ui.theme.HexYellow
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
    val currentBg = if (isDarkMode) HexBg else GrisPlata

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showMenu by remember { mutableStateOf(false) }

    val titleShadow = Shadow(
        color = Color.Black.copy(alpha = 0.8f),
        offset = Offset(6f, 6f),
        blurRadius = 12f
    )

    val hexTitle = buildAnnotatedString {
        val hexRootStyle = SpanStyle(
            color = HexAccent,
            fontWeight = FontWeight.Black,
            shadow = titleShadow,
            fontFamily = FontFamily.Monospace
        )
        val scanStyle = SpanStyle(
            color = if (isDarkMode) Color.White else Color(0xFF0D0D0D),
            fontWeight = FontWeight.Black,
            shadow = titleShadow,
            fontFamily = FontFamily.Monospace
        )

        withStyle(style = scanStyle) { append("😈 ") }
        withStyle(style = hexRootStyle) { append("HEXROOT") }
        withStyle(style = scanStyle) { append("SCAN") }
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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .background(Brush.verticalGradient(listOf(currentAccentLow.copy(alpha = 0.3f), Color.Transparent)))
                            .padding(20.dp),
                        contentAlignment = Alignment.BottomStart
                    ) {
                        Column {
                            val drawerTitle = buildAnnotatedString {
                                val hexRootStyle = SpanStyle(
                                    color = HexAccent,
                                    fontWeight = FontWeight.Black,
                                    shadow = titleShadow,
                                    fontFamily = FontFamily.Monospace
                                )
                                val scanStyle = SpanStyle(
                                    color = if (isDarkMode) Color.White else Color(0xFF0D0D0D),
                                    fontWeight = FontWeight.Black,
                                    shadow = titleShadow,
                                    fontFamily = FontFamily.Monospace
                                )
                                withStyle(style = hexRootStyle) { append("HEXROOT") }
                                withStyle(style = scanStyle) { append("SCAN") }
                            }
                            Text(
                                text = drawerTitle,
                                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 20.sp)
                            )
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
                    
                    Spacer(modifier = Modifier.weight(1f).heightIn(min = 40.dp))
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("HexRootScan v1.0.2", color = currentText.copy(alpha = 0.8f), fontSize = 11.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                        Text("Created by JAYLIZ with ❤️", color = currentText.copy(alpha = 0.4f), fontSize = 9.sp, fontWeight = FontWeight.Thin, fontFamily = FontFamily.Monospace)
                    }
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
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = currentPanel,
                            scrolledContainerColor = Color.Unspecified,
                            navigationIconContentColor = Color.Unspecified,
                            titleContentColor = Color.Unspecified,
                            actionIconContentColor = Color.Unspecified
                        )
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
                }
            }
        }
    }
}
