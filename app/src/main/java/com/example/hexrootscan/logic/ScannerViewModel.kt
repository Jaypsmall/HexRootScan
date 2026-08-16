package com.example.hexrootscan.logic

import android.app.Application
import android.content.Context
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

enum class Screen {
    SCANNER, TERMINAL, EXPLORER, SHODAN, DNSENUM, INSTALLER, SETTINGS
}

enum class ExecutionStatus {
    IDLE, WORKING, FINISHED
}

class ScannerViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("hex_prefs", Context.MODE_PRIVATE)
    
    var currentScreen by mutableStateOf(Screen.SCANNER)
    var status by mutableStateOf(ExecutionStatus.IDLE)
    
    var target by mutableStateOf("")
    var options by mutableStateOf("-sS -Pn -T4")
    var logs by mutableStateOf(listOf("💀 SYSTEM INITIALIZED - READY FOR RECON"))
    
    var terminalLogs by mutableStateOf(listOf("HEX-ROOT-TERMINAL v1.2", "Hybrid Engine Active", ""))
    var terminalInput by mutableStateOf("")

    var isDarkMode by mutableStateOf(true)
    var shodanKey by mutableStateOf(prefs.getString("shodan_key", "") ?: "")

    var explorerFiles by mutableStateOf(listOf<File>())
    var selectedFileContent by mutableStateOf<String?>(null)
    val exportDir = File(application.getExternalFilesDir(null), "Exports")

    var triageResults by mutableStateOf(listOf<Pair<String, String>>())
    var showTriageDialog by mutableStateOf(false)

    init {
        if (!exportDir.exists()) exportDir.mkdirs()
        refreshExplorer()
    }

    fun updateShodanKey(newKey: String) {
        shodanKey = newKey
        prefs.edit().putString("shodan_key", newKey).apply()
    }

    private fun stripAnsi(text: String): String {
        return text.replace(Regex("\u001B\\[[0-9;]*[mK]"), "")
    }

    fun runRootCommand(command: String, forceUser: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) { status = ExecutionStatus.WORKING }
            val useRoot = !forceUser
            
            try {
                val setupEnv = "export PREFIX=/data/data/com.termux/files/usr\n" +
                              "export HOME=/data/data/com.termux/files/home\n" +
                              "export PATH=\$PREFIX/bin:\$PREFIX/bin/applets:\$HOME/bin:\$PATH\n" +
                              "export LD_LIBRARY_PATH=\$PREFIX/lib\n" +
                              "export LANG=en_US.UTF-8\n"

                val process = if (useRoot) {
                    try {
                        Runtime.getRuntime().exec("su")
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) { logs = logs + "![WARN] ROOT UNAVAILABLE - TRYING USER SHELL" }
                        Runtime.getRuntime().exec("sh")
                    }
                } else {
                    Runtime.getRuntime().exec("sh")
                }

                val os = DataOutputStream(process.outputStream)
                os.write(setupEnv.toByteArray())
                os.write(("$command\n").toByteArray())
                os.write("exit\n".toByteArray())
                os.flush()

                val reader = BufferedReader(InputStreamReader(process.inputStream))
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    val cleanLine = stripAnsi(line!!)
                    withContext(Dispatchers.Main) {
                        updateLogs(cleanLine)
                    }
                }

                val errorReader = BufferedReader(InputStreamReader(process.errorStream))
                while (errorReader.readLine().also { line = it } != null) {
                    val cleanLine = stripAnsi(line!!)
                    withContext(Dispatchers.Main) {
                        updateLogs("![ERR] $cleanLine")
                    }
                }

                process.waitFor()
                withContext(Dispatchers.Main) { 
                    updateLogs("[✔] EXECUTION_FINISHED")
                    status = ExecutionStatus.FINISHED
                }
                delay(2000)
                withContext(Dispatchers.Main) { status = ExecutionStatus.IDLE }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { 
                    updateLogs("![CRITICAL] ${e.message}")
                    status = ExecutionStatus.FINISHED
                }
                delay(2000)
                withContext(Dispatchers.Main) { status = ExecutionStatus.IDLE }
            }
        }
    }

    private fun updateLogs(newLine: String) {
        val currentLogs = logs.toMutableList()
        if (currentLogs.size > 1000) currentLogs.removeAt(0)
        logs = currentLogs + newLine
        
        if (currentScreen == Screen.TERMINAL) {
            val currentTerm = terminalLogs.toMutableList()
            if (currentTerm.size > 1000) currentTerm.removeAt(0)
            terminalLogs = currentTerm + newLine
        }
    }
    
    fun clearLogs() {
        logs = listOf("💀 TERMINAL RESET")
    }

    fun runTerminalCommand() {
        val cmd = terminalInput.trim()
        if (cmd.isEmpty()) return
        terminalLogs = terminalLogs + "root@hex:# $cmd"
        val toRun = terminalInput
        terminalInput = ""
        runRootCommand(toRun)
    }

    fun exportLogs() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val fileName = "scan_$timeStamp.txt"
                val file = File(exportDir, fileName)
                file.writeText(logs.joinToString("\n"))
                withContext(Dispatchers.Main) {
                    updateLogs("[✔] EXPORTED TO: $fileName")
                    refreshExplorer()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { updateLogs("![ERR] EXPORT FAILED: ${e.message}") }
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
                    updateLogs("![ERR] FAILED TO OPEN FILE: ${e.message}")
                }
            }
        }
    }

    fun runTriage() {
        val allText = logs.joinToString("\n")
        val ports = parsePorts(allText)
        val recommendations = mutableListOf<Pair<String, String>>()
        ports.forEach { port -> recommendations.addAll(getRecommendations(port)) }
        if (target.isNotEmpty()) {
            recommendations.add("Whois Info" to "whois $target")
            recommendations.add("IP Info (CURL)" to "curl ipinfo.io/$target")
        }
        triageResults = recommendations.distinct()
        showTriageDialog = true
    }

    private fun parsePorts(text: String): List<Int> {
        val ports = mutableSetOf<Int>()
        val regex = Regex("""(\d{1,5})/(tcp|udp)""")
        regex.findAll(text).forEach { match ->
            match.groupValues[1].toIntOrNull()?.let { if (it in 1..65535) ports.add(it) }
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
        }
        return recs
    }
}
