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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

// Colores Hex Demon
val HexBg = Color(0xFF0B0B0B)
val HexPanel = Color(0xFF131212)
val HexAccent = Color(0xFFFF3333)
val HexText = Color(0xFFD7D7D7)
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
    var target by remember { mutableStateOf("") }
    var options by remember { mutableStateOf("-sS -Pn -T4") }
    var logs by remember { mutableStateOf(listOf("💀 ROOT ACCESS READY - HEX RECON MOBILE")) }
    var showMenu by remember { mutableStateOf(false) }
    var showShodanDialog by remember { mutableStateOf(false) }
    var shodanKey by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Función principal para ejecutar comandos ROOT
    fun runRootCommand(command: String) {
        scope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                logs = logs + "[#] Executing: $command"
            }
            try {
                val process = Runtime.getRuntime().exec("su")
                val os = DataOutputStream(process.outputStream)
                val reader = BufferedReader(InputStreamReader(process.inputStream))
                val errorReader = BufferedReader(InputStreamReader(process.errorStream))

                // Configurar entorno de Termux para que encuentre todo
                os.writeBytes("export PATH=/data/data/com.termux/files/usr/bin:\$PATH\n")
                os.writeBytes("export LD_LIBRARY_PATH=/data/data/com.termux/files/usr/lib\n")

                os.writeBytes("$command\n")
                os.writeBytes("exit\n")
                os.flush()

                reader.forEachLine { line ->
                    scope.launch(Dispatchers.Main) { logs = logs + line }
                }
                errorReader.forEachLine { line ->
                    scope.launch(Dispatchers.Main) { logs = logs + "![ERR] $line" }
                }

                process.waitFor()
                withContext(Dispatchers.Main) { logs = logs + "[✔] Command Finished" }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { logs = logs + "![CRITICAL] ${e.message}" }
            }
        }
    }

    // Funciones específicas para herramientas con intérpretes
    fun ejecutarWhatWeb(target: String) = runRootCommand("/data/data/com.termux/files/usr/bin/ruby /data/data/com.termux/files/home/WhatWeb/whatweb $target")
    fun ejecutarNikto(target: String) = runRootCommand("/data/data/com.termux/files/usr/bin/perl /data/data/com.termux/files/home/nikto/program/nikto.pl -h $target")
    fun ejecutarDnsenum(target: String) = runRootCommand("/data/data/com.termux/files/usr/bin/perl /data/data/com.termux/files/home/dnsenum/dnsenum.pl $target")

    fun instalarTodoYDarPermisos() {
        val script = """
            export PATH=/data/data/com.termux/files/usr/bin:${'$'}{PATH}
            echo "[#] Actualizando repositorios..."
            pkg update -y && pkg upgrade -y
            echo "[#] Instalando Perl, Ruby y herramientas..."
            pkg install perl ruby nmap dnsutils whois -y
            
            echo "[#] Aplicando permisos de ejecución..."
            [ -f "/data/data/com.termux/files/home/nikto/program/nikto.pl" ] && chmod +x /data/data/com.termux/files/home/nikto/program/nikto.pl && echo "[✓] Nikto OK"
            [ -f "/data/data/com.termux/files/home/WhatWeb/whatweb" ] && chmod +x /data/data/com.termux/files/home/WhatWeb/whatweb && echo "[✓] WhatWeb OK"
            [ -f "/data/data/com.termux/files/home/dnsenum/dnsenum.pl" ] && chmod +x /data/data/com.termux/files/home/dnsenum/dnsenum.pl && echo "[✓] Dnsenum OK"
            
            echo "[✓] ¡Instalación y permisos completados!"
        """.trimIndent()
        runRootCommand(script)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = HexBg,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.icono_scan),
                            contentDescription = "Logo",
                            modifier = Modifier.size(40.dp).padding(end = 8.dp)
                        )
                        Text(
                            "HEX ROOT SCAN",
                            color = HexAccent,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 18.sp
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = HexAccent)
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(HexPanel).border(1.dp, HexAccent)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Instalar Todo (Ruby/Perl/Tools)", color = HexText) },
                            onClick = {
                                showMenu = false
                                instalarTodoYDarPermisos()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Limpiar Consola", color = HexText) },
                            onClick = {
                                showMenu = false
                                logs = listOf("💀 CONSOLE CLEARED - READY")
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = HexPanel)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            HexInput(target, { target = it }, "🎯 Target IP/Host")
            Spacer(modifier = Modifier.height(8.dp))
            HexInput(options, { options = it }, "⚙️ Tool Options")

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(scrollState).padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HexButton("🛰 NMAP") { runRootCommand("nmap $options $target") }
                HexButton("📡 WHOIS") { runRootCommand("whois $target") }
                HexButton("🧬 DIG") { runRootCommand("dig $target ANY") }
                HexButton("🔥 NIKTO") { ejecutarNikto(target) }
                HexButton("🌐 WHATWEB") { ejecutarWhatWeb(target) }
                HexButton("🧬 DNSENUM") { ejecutarDnsenum(target) }
                HexButton("🔎 SUBS") { runRootCommand("curl -s https://crt.sh/?q=$target") }
                HexButton("👁 SHODAN") { showShodanDialog = true }
                HexButton("🛑 STOP", isError = true) { runRootCommand("pkill nmap || pkill perl || pkill ruby") }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("LOGS:", color = HexAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black).border(1.dp, HexAccent.copy(0.4f)).padding(8.dp)
            ) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(logs) { log ->
                        Text(
                            text = log,
                            color = if (log.startsWith("!")) HexAccent else HexOk,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        if (showShodanDialog) {
            AlertDialog(
                onDismissRequest = { showShodanDialog = false },
                containerColor = HexPanel,
                title = { Text("SHODAN API KEY", color = HexAccent) },
                text = {
                    OutlinedTextField(
                        value = shodanKey,
                        onValueChange = { shodanKey = it },
                        label = { Text("Introduce tu API Key", color = HexAccent.copy(0.6f)) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = HexAccent, unfocusedBorderColor = Color.DarkGray)
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        showShodanDialog = false
                        runRootCommand("curl -s https://api.shodan.io/shodan/host/$target?key=$shodanKey")
                    }) { Text("CONECTAR", color = HexAccent) }
                },
                dismissButton = {
                    TextButton(onClick = { showShodanDialog = false }) { Text("CANCELAR", color = Color.Gray) }
                }
            )
        }
    }
}

@Composable
fun HexInput(value: String, onValueChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange,
        label = { Text(label, color = HexAccent.copy(0.6f)) },
        modifier = Modifier.fillMaxWidth(),
        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontFamily = FontFamily.Monospace),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = HexAccent, unfocusedBorderColor = Color.DarkGray, cursorColor = HexAccent)
    )
}

@Composable
fun HexButton(text: String, modifier: Modifier = Modifier, isError: Boolean = false, onClick: () -> Unit) {
    Button(
        onClick = onClick, modifier = modifier.height(48.dp),
        colors = ButtonDefaults.buttonColors(containerColor = if (isError) Color(0xFF330000) else HexPanel),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, if (isError) Color.Red else HexAccent)
    ) {
        Text(text, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}
