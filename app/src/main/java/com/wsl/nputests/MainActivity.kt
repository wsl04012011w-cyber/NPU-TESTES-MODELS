package com.wsl.nputests

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    LabScreen(this)
                }
            }
        }
    }
}

@Composable
private fun LabScreen(context: Context) {
    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val memory = ActivityManager.MemoryInfo().also(activityManager::getMemoryInfo)
    val abiList = Build.SUPPORTED_ABIS.joinToString()

    var cpuResult by remember { mutableStateOf<BenchmarkResult?>(null) }
    var gpuReport by remember { mutableStateOf("Ainda não executado") }
    var qnnReport by remember { mutableStateOf("Ainda não executado") }
    var running by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Snapdragon AI Lab", style = MaterialTheme.typography.headlineMedium)
        Text("Build 0.2.0 • Device Inspector + CPU + Vulkan/QNN diagnostics")

        InfoCard("Dispositivo") {
            InfoLine("Fabricante", Build.MANUFACTURER)
            InfoLine("Modelo", Build.MODEL)
            InfoLine("Android", "${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
            InfoLine("Hardware", Build.HARDWARE)
            InfoLine("Board", Build.BOARD)
            InfoLine("ABIs", abiList)
        }

        InfoCard("Memória") {
            InfoLine("RAM total", formatMb(memory.totalMem / (1024 * 1024)))
            InfoLine("RAM disponível agora", formatMb(memory.availMem / (1024 * 1024)))
            InfoLine("Low memory", if (memory.lowMemory) "Sim" else "Não")
            Text(
                "A RAM disponível muda constantemente e não representa memória garantida para o app.",
                style = MaterialTheme.typography.bodySmall
            )
        }

        InfoCard("CPU • benchmark") {
            Text("Carga determinística de contagem de primos. É uma linha de base, não uma comparação direta com GPU/NPU.")
            Button(
                enabled = !running,
                onClick = {
                    running = true
                    scope.launch {
                        cpuResult = withContext(Dispatchers.Default) {
                            BenchmarkEngine.runCpuPrimeBenchmark()
                        }
                        running = false
                    }
                }
            ) { Text(if (running) "Executando..." else "Executar benchmark CPU") }
            cpuResult?.let {
                InfoLine("Tempo", "${it.elapsedMs} ms")
                InfoLine("Limite analisado", it.operations.toString())
                InfoLine("Desempenho", "%.1f primos/s".format(it.score))
            }
        }

        InfoCard("GPU Adreno • Vulkan") {
            Text("Enumera dispositivos Vulkan, procura uma fila COMPUTE e tenta criar um dispositivo lógico.")
            Button(
                enabled = !running,
                onClick = {
                    running = true
                    scope.launch {
                        gpuReport = withContext(Dispatchers.IO) {
                            AcceleratorDiagnostics.runGpuProbe()
                        }
                        running = false
                    }
                }
            ) { Text("Analisar GPU / Vulkan") }
            Text(gpuReport, style = MaterialTheme.typography.bodySmall)
            Text(
                "Esta etapa valida acesso à fila de compute; ainda não mede execução de shader nem throughput.",
                style = MaterialTheme.typography.bodySmall
            )
        }

        InfoCard("NPU Hexagon • QNN / HTP") {
            Text("Verifica se as bibliotecas centrais do runtime QNN podem ser carregadas pelo app.")
            Button(
                enabled = !running,
                onClick = {
                    running = true
                    scope.launch {
                        qnnReport = withContext(Dispatchers.IO) {
                            AcceleratorDiagnostics.runQnnProbe()
                        }
                        running = false
                    }
                }
            ) { Text("Verificar runtime QNN") }
            Text(qnnReport, style = MaterialTheme.typography.bodySmall)
            Text(
                "Biblioteca carregável não significa HTP inicializado. A confirmação da NPU exige inicializar o backend e executar um grafo QNN.",
                style = MaterialTheme.typography.bodySmall
            )
        }

        Text("Próxima etapa: integrar as APIs QNN licenciadas e executar um modelo pequeno com confirmação do backend.")
    }
}

@Composable
private fun InfoCard(title: String, content: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
    }
}

private fun formatMb(value: Long) = String.format("%,d MB", value)
