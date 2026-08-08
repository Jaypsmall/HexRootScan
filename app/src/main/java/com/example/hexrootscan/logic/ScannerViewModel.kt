package com.example.hexrootscan.logic

import android.app.Application
import android.os.Environment
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

enum class Screen {
    SCANNER, TERMINAL, EXPLORER, SHODAN, SETTINGS
}

class ScannerViewModel(application: Application) : AndroidViewModel(application) {
    var currentScreen by mutableStateOf(Screen.SCANNER)
    
    var target by mutableStateOf("")
    var options by mutableStateOf("-sS -Pn -T4")
    var logs by mutableStateOf(listOf("💀 SYSTEM INITIALIZED - ROOT ACCESS GRANTED"))
    
    // Terminal dedicada
    var terminalLogs by mutableStateOf(listOf("HEX-ROOT-TERMINAL v1.0", "Type 'help' for commands", ""))
    var terminalInput by mutableStateOf("")

    var isDarkMode by mutableStateOf(true)
    var shodanKey by mutableStateOf("")

    // Explorer State
    var explorerFiles by mutableStateOf(listOf<File>())
    var selectedFileContent by mutableStateOf<String?>(null)
    val exportDir = File(application.getExternalFilesDir(null), "Exports")

    // Triage Engine State
    var triageResults by mutableStateOf(listOf<Pair<String, String>>())
    var showTriageDialog by mutableStateOf(false)

    init {
        if (!exportDir.exists()) exportDir.mkdirs()
        refreshExplorer()
    }

    private fun stripAnsi(text: String): String {
        return text.replace(Regex("\u001B\\[[0-9;]*[mK]"), "")
    }

    fun runRootCommand(command: String) {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) { logs = logs + "[#] > $command" }
            try {
                val process = Runtime.getRuntime().exec("su")
                val os = DataOutputStream(process.outputStream)
                val setupEnv = "export PATH=/data/data/com.termux/files/usr/bin:/data/data/com.termux/files/usr/bin/applets:\$PATH\n" +
                              "export LD_LIBRARY_PATH=/data/data/com.termux/files/usr/lib\n" +
                              "export HOME=/data/data/com.termux/files/home\n"
                
                os.write(setupEnv.toByteArray())
                os.write(("$command\n").toByteArray())
                os.write("exit\n".toByteArray())
                os.flush()

                BufferedReader(InputStreamReader(process.inputStream)).forEachLine { line ->
                    val cleanLine = stripAnsi(line)
                    viewModelScope.launch(Dispatchers.Main) { logs = logs + cleanLine }
                }
                BufferedReader(InputStreamReader(process.errorStream)).forEachLine { line ->
                    val cleanLine = stripAnsi(line)
                    viewModelScope.launch(Dispatchers.Main) { logs = logs + "![ERR] $cleanLine" }
                }
                process.waitFor()
                withContext(Dispatchers.Main) { logs = logs + "[✔] SESSION_FINISHED" }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { logs = logs + "![CRITICAL] ${e.message}" }
            }
        }
    }
    
    fun clearLogs() {
        logs = listOf("💀 TERMINAL RESET")
    }

    fun runTerminalCommand() {
        val cmd = terminalInput.trim()
        if (cmd.isEmpty()) return
        
        terminalLogs = terminalLogs + "root@hex:# $cmd"
        terminalInput = ""
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val process = Runtime.getRuntime().exec("su")
                val os = DataOutputStream(process.outputStream)
                val setupEnv = "export PATH=/data/data/com.termux/files/usr/bin:/data/data/com.termux/files/usr/bin/applets:\$PATH\n" +
                              "export LD_LIBRARY_PATH=/data/data/com.termux/files/usr/lib\n" +
                              "export HOME=/data/data/com.termux/files/home\n"
                
                os.write(setupEnv.toByteArray())
                os.write(("$cmd\n").toByteArray())
                os.write("exit\n".toByteArray())
                os.flush()

                BufferedReader(InputStreamReader(process.inputStream)).forEachLine { line ->
                    val cleanLine = stripAnsi(line)
                    viewModelScope.launch(Dispatchers.Main) { terminalLogs = terminalLogs + cleanLine }
                }
                BufferedReader(InputStreamReader(process.errorStream)).forEachLine { line ->
                    val cleanLine = stripAnsi(line)
                    viewModelScope.launch(Dispatchers.Main) { terminalLogs = terminalLogs + "![ERR] $cleanLine" }
                }
                process.waitFor()
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { terminalLogs = terminalLogs + "![CRITICAL] ${e.message}" }
            }
        }
    }

    fun exportLogs() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val fileName = "scan_$timeStamp.txt"
                val file = File(exportDir, fileName)
                file.writeText(logs.joinToString("\n"))
                withContext(Dispatchers.Main) {
                    logs = logs + "[✔] EXPORTED TO: $fileName"
                    refreshExplorer()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { logs = logs + "![ERR] EXPORT FAILED: ${e.message}" }
            }
        }
    }

    fun refreshExplorer() {
        explorerFiles = exportDir.listFiles()?.toList()?.sortedByDescending { it.lastModified() } ?: listOf()
    }

    fun openFile(file: File) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val content = file.readText()
                withContext(Dispatchers.Main) {
                    selectedFileContent = content
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    logs = logs + "![ERR] FAILED TO OPEN FILE: ${e.message}"
                }
            }
        }
    }

    // --- TRIAGE ENGINE LOGIC ---
    fun runTriage() {
        val allText = logs.joinToString("\n")
        val ports = parsePorts(allText)
        val recommendations = mutableListOf<Pair<String, String>>()
        
        ports.forEach { port ->
            recommendations.addAll(getRecommendations(port))
        }
        
        // Add general info
        if (target.isNotEmpty()) {
            recommendations.add("Whois Info" to "whois $target")
            recommendations.add("IP Info (CURL)" to "curl ipinfo.io/$target")
        }

        triageResults = recommendations.distinct()
        showTriageDialog = true
    }

    private fun parsePorts(text: String): List<Int> {
        val ports = mutableSetOf<Int>()
        // Match 80/tcp, 443/udp, etc.
        val regex = Regex("""(\d{1,5})/(tcp|udp)""")
        regex.findAll(text).forEach { match ->
            match.groupValues[1].toIntOrNull()?.let { if (it in 1..65535) ports.add(it) }
        }
        
        // If nothing found, search for standalone numbers in likely port lines
        if (ports.isEmpty()) {
            val lines = text.lines()
            lines.forEach { line ->
                if (line.contains("open", true) || line.contains("port", true)) {
                    Regex("""\b(\d{1,5})\b""").findAll(line).forEach { match ->
                        match.groupValues[1].toIntOrNull()?.let { if (it in 1..65535) ports.add(it) }
                    }
                }
            }
        }
        return ports.sorted()
    }

    private fun getRecommendations(port: Int): List<Pair<String, String>> {
        val recs = mutableListOf<Pair<String, String>>()
        val t = target.ifEmpty { "TARGET" }
        
        recs.add("Nmap Service Detect (P$port)" to "nmap -sV -p $port $t")
        
        when (port) {
            80, 8080, 8000, 81 -> {
                recs.add("HTTP Headers (P$port)" to "curl -I --max-time 8 http://$t:$port")
                recs.add("WhatWeb (P$port)" to "whatweb http://$t:$port")
            }
            443, 8443, 7443 -> {
                recs.add("HTTPS Headers (P$port)" to "curl -I -k --max-time 8 https://$t:$port")
                recs.add("WhatWeb SSL (P$port)" to "whatweb https://$t:$port")
            }
            21 -> recs.add("FTP Banner (P21)" to "timeout 5 nc -w 3 $t 21")
            22 -> recs.add("SSH Banner (P22)" to "timeout 5 nc -w 3 $t 22")
            25, 587, 465 -> recs.add("SMTP EHLO (P$port)" to "echo EHLO | nc -w 3 $t $port")
            53 -> recs.add("DNS Zone Transfer" to "dig @$t axfr")
            3389 -> recs.add("RDP Check" to "nmap --script rdp-enum-encryption -p 3389 $t")
        }
        return recs
    }
}
