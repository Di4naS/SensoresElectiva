@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.tallerevaluativoelectiva

import android.R.attr.padding
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tallerevaluativoelectiva.ui.theme.TallerEvaluativoElectivaTheme
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tallerevaluativoelectiva.viewmodels.ProximityViewModel
import com.example.tallerevaluativoelectiva.viewmodels.MotionViewModel
import com.example.tallerevaluativoelectiva.models.ReportItem
import com.example.tallerevaluativoelectiva.viewmodels.ReportsViewModel
import androidx.compose.foundation.lazy.items
import com.example.tallerevaluativoelectiva.ui.theme.RojoLejos
import com.example.tallerevaluativoelectiva.ui.theme.VerdeCercano


// Actividad principal de la aplicación
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Definimos el contenido con Compose
        setContent {
            TallerEvaluativoElectivaTheme {
                // Controlador de navegación
                val navController = rememberNavController()
                // Configuración de navegación con 4 pantallas
                NavHost(navController = navController, startDestination = "Inicio") {
                    composable("Inicio") { HomeScreen(navController) }
                    composable("Proximidad") { ProximityScreen() }
                    composable("Movimiento") { MotionScreen() }
                    composable("Reportes") { ReportsScreen() }
                }
            }
        }
    }
}

@Composable
fun HomeScreen(navController: NavController) {
    // Pantalla de inicio con Scaffold (estructura base)
    Scaffold(
        topBar = {
            // Barra superior con título
            CenterAlignedTopAppBar(
                title = { Text("Taller Sensores") }
            )
        }
    ) { padding ->
        // Column organiza los botones en vertical
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center, // centra verticalmente
            horizontalAlignment = Alignment.CenterHorizontally // centra horizontalmente
        ) {
            // Botón para abrir la pantalla de proximidad
            Button(onClick = { navController.navigate("Proximidad") }) {
                Text("Monitorear Proximidad")
            }
            // Botón para abrir la pantalla de movimiento
            Button(onClick = { navController.navigate("Movimiento") }) {
                Text("Monitorear Movimiento")
            }
            // Botón para abrir la pantalla de reportes
            Button(onClick = { navController.navigate("Reportes") }) {
                Text("Ver Reportes")
            }
        }
    }
}

@Composable
fun ProximityScreen(vm: ProximityViewModel = viewModel()) {
    // Estado expuesto por el ViewModel (flujo del sensor de proximidad)
    val estado by vm.estado.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Monitoreo: Proximidad") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            // Card que cambia de color según el estado (verde / rojo)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (estado == "Objeto detectado cerca") VerdeCercano else RojoLejos
                )
            ) {
                Text(
                    text = "Estado: $estado",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }

            // Botones para iniciar/detener el sensor
            Button(
                onClick = { vm.startListening() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Iniciar Monitoreo")
            }

            Button(
                onClick = { vm.stopListening() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Detener Monitoreo")
            }
        }
    }
}

@Composable
fun MotionScreen(vm: MotionViewModel = viewModel()) {
    // Estados del sensor de movimiento
    val estado by vm.estadoMovimiento.collectAsState()    // texto: "estable" / "sacudida"
    val sacudidas by vm.contadorSacudidas.collectAsState() // número de sacudidas acumuladas

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Monitoreo: Movimiento") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            // Card que muestra el estado actual del sensor
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = "Estado: $estado",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }

            // Texto con número de sacudidas detectadas
            Text(
                text = "Sacudidas detectadas: $sacudidas",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(16.dp)
            )

            // Botones para iniciar/detener monitoreo del acelerómetro
            Button(
                onClick = { vm.startListening() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Iniciar Monitoreo")
            }

            Button(
                onClick = { vm.stopListening() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Detener Monitoreo")
            }
        }
    }
}

@Composable
fun ReportsScreen(vm: ReportsViewModel = viewModel()) {
    // Reportes combinados de proximidad y movimiento en tiempo real
    val reportes = vm.reportes.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Reportes Firebase") }
            )
        }
    ) { padding ->
        if (reportes.value.isEmpty()) {
            // Caso: no hay datos en Firebase
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center
            ) {
                Text("No hay eventos para mostrar", modifier = Modifier.padding(16.dp))
            }
        } else {
            // Lista de reportes usando LazyColumn
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(reportes.value, key = { it.key }) { reporte ->
                    ReportCard(reporte)
                }
            }
        }
    }
}

// Composable que muestra cada reporte como una tarjeta de Material Design
@Composable
private fun ReportCard(item: ReportItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("${item.tipo} • ${item.estado}", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(6.dp))
            Text(item.fecha, style = MaterialTheme.typography.bodySmall)
        }
    }
}