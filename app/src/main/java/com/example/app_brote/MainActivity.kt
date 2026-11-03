package com.example.app_brote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app_brote.ui.theme.App_BroteTheme

class MainActivity : ComponentActivity() {

    // ViewModel que guarda las metas
    private val viewModel: MetasViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            App_BroteTheme {                 // aplica los colores de la app
                PantallaPrincipal(viewModel) // mostramos la pantalla principal
            }
        }
    }
}


//Barra inferior
@Composable
fun PantallaPrincipal(vm: MetasViewModel) {

    // Qué pestaña está activa: 0 = Metas, 1 = Jardín
    var pestana by remember { mutableStateOf(0) }

    // Se está mostrando la ventanita de "Nueva meta" (sí / no)
    var mostrarCrear by remember { mutableStateOf(false) }

    // Barra de abajo, botón flotante, contenido
    Scaffold(
        // Barra de navegación de abajo
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = pestana == 0,
                    onClick = { pestana = 0 },        // al tocar, cambia a Metas
                    icon = { Text("") },
                    label = { Text("Metas") }
                )
                NavigationBarItem(
                    selected = pestana == 1,
                    onClick = { pestana = 1 },        // al tocar, cambia a Jardín
                    icon = { Text("") },
                    label = { Text("Jardín") }
                )
            }
        },
        // Botón que solo se ve en la pestaña Metas
        floatingActionButton = {
            if (pestana == 0) {
                FloatingActionButton(onClick = { mostrarCrear = true }) {
                    Text("+", fontSize = 28.sp)
                }
            }
        }
    ) { padding ->
        // Según la pestaña, mostramos una pantalla u otra
        Box(modifier = Modifier.padding(padding)) {
            if (pestana == 0) {
                PantallaMetas(vm)
            } else {
                PantallaJardin(vm)
            }
        }
    }

    //Aparece la ventanita para crear una meta
    if (mostrarCrear) {
        DialogoCrearMeta(
            onCerrar = { mostrarCrear = false },
            onCrear = { nombre, objetivo ->
                vm.crearMeta(nombre, objetivo)   // guardamos la meta
                mostrarCrear = false             // cerramos la ventanita
            }
        )
    }
}


//Lista de metas que aún no terminan

@Composable
fun PantallaMetas(vm: MetasViewModel) {

    // Solo las metas NO completadas
    val activas = vm.metas.filter { !it.completada }

    // Guarda el id de la meta a la que vamos a aportar
    var idAporte by remember { mutableStateOf<Long?>(null) }

    if (activas.isEmpty()) {
        // Si no hay metas, mostramos un mensaje en el centro
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Aún no tienes metas.\nToca + para crear la primera", textAlign = TextAlign.Center)
        }
    } else {
        // Si hay metas, las mostramos en una lista con scroll
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(activas) { meta ->
                TarjetaMeta(
                    meta = meta,
                    onAportar = { idAporte = meta.id },       // abre ventanita de aporte
                    onEliminar = { vm.eliminarMeta(meta.id) } // borra la meta
                )
            }
        }
    }

    // Si hay una meta elegida para aportar, mostramos la ventanita de aporte
    if (idAporte != null) {
        DialogoAporte(
            onCerrar = { idAporte = null },
            onGuardar = { monto ->
                vm.agregarAporte(idAporte!!, monto) // sumamos el aporte
                idAporte = null                     // cerramos la ventanita
            }
        )
    }
}


// planta + datos + progreso

@Composable
fun TarjetaMeta(meta: Meta, onAportar: () -> Unit, onEliminar: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Fila: planta a la izquierda, textos a la derecha
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(plantaPara(meta.progreso), fontSize = 48.sp)  // la planta
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(meta.nombre, style = MaterialTheme.typography.titleLarge)
                    Text("Ahorrado: ${dinero(meta.ahorrado)} de ${dinero(meta.objetivo)}")
                    Text("Faltan: ${dinero(meta.restante)}")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Barra de progreso y porcentaje
            LinearProgressIndicator(
                progress = { meta.progreso },
                modifier = Modifier.fillMaxWidth()
            )
            Text("${(meta.progreso * 100).toInt()}%")

            // Botones
            Row {
                Button(onClick = onAportar) { Text("Agregar aporte") }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = onEliminar) { Text("Eliminar") }
            }
        }
    }
}


//JARDÍN : metas completadas

@Composable
fun PantallaJardin(vm: MetasViewModel) {

    // Solo las metas completadas
    val completadas = vm.metas.filter { it.completada }

    if (completadas.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Tu jardín está vacío.\nCompleta una meta para ver tu primera flor", textAlign = TextAlign.Center)
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(completadas) { meta ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("", fontSize = 48.sp)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(meta.nombre, style = MaterialTheme.typography.titleLarge)
                            Text("Meta lograda: ${dinero(meta.objetivo)}")
                        }
                    }
                }
            }
        }
    }
}

//VENTANITA "NUEVA META": pide nombre y monto

@Composable
fun DialogoCrearMeta(onCerrar: () -> Unit, onCrear: (String, Double) -> Unit) {

    // Lo que el usuario va escribiendo
    var nombre by remember { mutableStateOf("") }
    var objetivo by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }   // mensaje de error (vacío = sin error)

    AlertDialog(
        onDismissRequest = onCerrar,               // si toca afuera, se cierra
        title = { Text("Nueva meta") },
        text = {
            Column {
                // Campo para el nombre
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre de la meta") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Campo para el monto (teclado de números)
                OutlinedTextField(
                    value = objetivo,
                    onValueChange = { objetivo = it },
                    label = { Text("Monto objetivo") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                // Si hay error, lo mostramos en rojo
                if (error != "") {
                    Text(error, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val monto = objetivo.toDoubleOrNull()   // convierte texto a número (o null si no se puede)

                // VALIDACIONES
                if (nombre.isBlank()) {
                    error = "Escribe un nombre para la meta"
                } else if (monto == null || monto <= 0) {
                    error = "El monto debe ser un número mayor a 0"
                } else {
                    onCrear(nombre.trim(), monto)       //todo bien: creamos la meta
                }
            }) { Text("Crear") }
        },
        dismissButton = {
            TextButton(onClick = onCerrar) { Text("Cancelar") }
        }
    )
}


//VENTANITA "APORTE": pide cuánto ahorraste

@Composable
fun DialogoAporte(onCerrar: () -> Unit, onGuardar: (Double) -> Unit) {

    var texto by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text("Agregar aporte") },
        text = {
            Column {
                OutlinedTextField(
                    value = texto,
                    onValueChange = { texto = it },
                    label = { Text("¿Cuánto ahorraste?") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                if (error != "") {
                    Text(error, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val monto = texto.toDoubleOrNull()

                // VALIDACIÓN
                if (monto == null || monto <= 0) {
                    error = "Escribe un valor mayor a 0"
                } else {
                    onGuardar(monto)
                }
            }) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onCerrar) { Text("Cancelar") }
        }
    )
}