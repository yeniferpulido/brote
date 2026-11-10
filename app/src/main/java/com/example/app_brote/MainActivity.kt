package com.example.app_brote

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.app_brote.ui.theme.App_BroteTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MetasViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            App_BroteTheme {
                PantallaPrincipal(viewModel)
            }
        }
    }
}

// Carga la flor, convierte los píxeles blancos del fondo JPG en transparentes
// y conserva intactos los píxeles pixel art originales.
@Composable
fun rememberFlorSinFondo(resId: Int): ImageBitmap {
    val context = LocalContext.current
    return remember(resId) {
        val drawable = ContextCompat.getDrawable(context, resId)
        val bitmap = (drawable as? BitmapDrawable)?.bitmap
            ?: BitmapFactory.decodeResource(context.resources, resId)

        val mutable = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val width = mutable.width
        val height = mutable.height
        val pixels = IntArray(width * height)
        mutable.getPixels(pixels, 0, width, 0, 0, width, height)

        // Fondo blanco o casi blanco del JPG se vuelve transparente
        for (i in pixels.indices) {
            val color = pixels[i]
            val r = (color shr 16) and 0xFF
            val g = (color shr 8) and 0xFF
            val b = color and 0xFF
            if (r >= 235 && g >= 235 && b >= 235) {
                pixels[i] = 0 // Transparencia total
            }
        }
        mutable.setPixels(pixels, 0, width, 0, 0, width, height)
        mutable.asImageBitmap()
    }
}

// Barra inferior y navegación de pestañas
@Composable
fun PantallaPrincipal(vm: MetasViewModel) {
    // 0 = Metas, 1 = Jardín
    var pestana by remember { mutableStateOf(0) }
    var mostrarCrear by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = pestana == 0,
                    onClick = { pestana = 0 },
                    icon = { Text("🎯") },
                    label = { Text("Metas") }
                )
                NavigationBarItem(
                    selected = pestana == 1,
                    onClick = { pestana = 1 },
                    icon = { Text("🌸") },
                    label = { Text("Jardín") }
                )
            }
        },
        floatingActionButton = {
            if (pestana == 0) {
                FloatingActionButton(onClick = { mostrarCrear = true }) {
                    Text("+", fontSize = 28.sp)
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (pestana == 0) {
                PantallaMetas(vm)
            } else {
                PantallaJardin(vm)
            }
        }
    }

    // Ventana para crear una meta
    if (mostrarCrear) {
        DialogoCrearMeta(
            onCerrar = { mostrarCrear = false },
            onCrear = { nombre, objetivo ->
                vm.crearMeta(nombre, objetivo)
                mostrarCrear = false
            }
        )
    }

    // Aviso de flor recién desbloqueada
    val florReciente = vm.florRecienDesbloqueada
    if (florReciente != null) {
        AlertDialog(
            onDismissRequest = { vm.cerrarAvisoFlor() },
            title = { Text("¡Flor desbloqueada! 🌸", textAlign = TextAlign.Center) },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val bitmap = rememberFlorSinFondo(florReciente.florResId)
                    Image(
                        bitmap = bitmap,
                        contentDescription = "Nueva flor desbloqueada",
                        filterQuality = FilterQuality.None,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(100.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "¡Felicidades! Completaste tu meta de ahorro y esta flor se ha incorporado a tu jardín.",
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    vm.cerrarAvisoFlor()
                    pestana = 1 // Cambiar a la pestaña Jardín para verla
                }) {
                    Text("Ir al Jardín")
                }
            },
            dismissButton = {
                TextButton(onClick = { vm.cerrarAvisoFlor() }) {
                    Text("Aceptar")
                }
            }
        )
    }
}

// Lista de metas activas
@Composable
fun PantallaMetas(vm: MetasViewModel) {
    val activas = vm.metas.filter { !it.completada }
    var idAporte by remember { mutableStateOf<Long?>(null) }

    if (activas.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                "Aún no tienes metas activas.\nToca + para crear una nueva meta de ahorro",
                textAlign = TextAlign.Center
            )
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(activas, key = { it.id }) { meta ->
                TarjetaMeta(
                    meta = meta,
                    onAportar = { idAporte = meta.id },
                    onEliminar = { vm.eliminarMeta(meta.id) }
                )
            }
        }
    }

    if (idAporte != null) {
        DialogoAporte(
            onCerrar = { idAporte = null },
            onGuardar = { monto ->
                vm.agregarAporte(idAporte!!, monto)
                idAporte = null
            }
        )
    }
}

// Tarjeta de meta con etapa de crecimiento de la planta
@Composable
fun TarjetaMeta(meta: Meta, onAportar: () -> Unit, onEliminar: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Representación de la planta según la etapa de crecimiento
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(64.dp)
                ) {
                    if (meta.completada) {
                        val bitmap = rememberFlorSinFondo(meta.florResId)
                        Image(
                            bitmap = bitmap,
                            contentDescription = "Flor desarrollada",
                            filterQuality = FilterQuality.None,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.size(52.dp)
                        )
                    } else {
                        Text(meta.etapa.icono, fontSize = 42.sp)
                    }
                    Text(
                        text = meta.etapa.nombre,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
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
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("${(meta.progreso * 100).toInt()}%")
                Text(
                    text = if (meta.completada) "¡Meta cumplida!" else "En crecimiento",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row {
                Button(onClick = onAportar) { Text("Agregar aporte") }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = onEliminar) { Text("Eliminar") }
            }
        }
    }
}

// Pantalla del Jardín con fondojardin, parcelas y paginación
@Composable
fun PantallaJardin(vm: MetasViewModel) {
    val floresPagina = vm.floresDePagina(vm.paginaActual)
    val totalPaginas = vm.totalPaginas

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val availableWidth = maxWidth
        val availableHeight = maxHeight

        // Proporción exacta de fondojardin.jpg (602 x 1200 px)
        val imageAspectRatio = 602f / 1200f
        val containerAspectRatio = availableWidth / availableHeight

        val (gardenWidth, gardenHeight) = if (containerAspectRatio > imageAspectRatio) {
            (availableHeight * imageAspectRatio) to availableHeight
        } else {
            availableWidth to (availableWidth / imageAspectRatio)
        }

        // Contenedor ajustado a la proporción del fondo
        Box(
            modifier = Modifier.size(gardenWidth, gardenHeight)
        ) {
            // Imagen de fondo del jardín
            Image(
                bitmap = ImageBitmap.imageResource(id = R.drawable.fondojardin),
                contentDescription = "Fondo del Jardín",
                contentScale = ContentScale.Fit,
                filterQuality = FilterQuality.None,
                modifier = Modifier.fillMaxSize()
            )

            // Flores superpuestas en las parcelas correspondientes
            for (parcela in PARCELAS_JARDIN) {
                val flor = floresPagina.find { it.parcela == parcela.id }
                if (flor != null) {
                    val florSize = gardenWidth * 0.22f
                    val left = (gardenWidth * parcela.xNorm) - (florSize / 2f)
                    val top = (gardenHeight * parcela.yNorm) - (florSize / 2f)

                    val bitmap = rememberFlorSinFondo(flor.florResId)
                    Image(
                        bitmap = bitmap,
                        contentDescription = "Flor en parcela ${parcela.id + 1}",
                        filterQuality = FilterQuality.None,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .offset(x = left, y = top)
                            .size(florSize)
                    )
                }
            }

            // Estado vacío si no hay flores en toda la app
            if (vm.flores.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .padding(24.dp)
                        .align(Alignment.Center)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🌱", fontSize = 44.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Tu jardín está esperando su primer brote",
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Completa una meta al 100% para desbloquear y plantar tu primera flor.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Barra de controles de paginación
            Surface(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .align(Alignment.TopCenter),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.90f),
                shadowElevation = 6.dp
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    IconButton(
                        onClick = { vm.irAPaginaAnterior() },
                        enabled = vm.paginaActual > 0
                    ) {
                        Text("◀", fontSize = 16.sp)
                    }

                    Text(
                        text = "Jardín ${vm.paginaActual + 1} / $totalPaginas",
                        style = MaterialTheme.typography.titleMedium
                    )

                    IconButton(
                        onClick = { vm.irAPaginaSiguiente() },
                        enabled = vm.paginaActual < totalPaginas - 1
                    ) {
                        Text("▶", fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

// Diálogo para crear nueva meta
@Composable
fun DialogoCrearMeta(onCerrar: () -> Unit, onCrear: (String, Double) -> Unit) {
    var nombre by remember { mutableStateOf("") }
    var objetivo by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text("Nueva meta") },
        text = {
            Column {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre de la meta") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = objetivo,
                    onValueChange = { objetivo = it },
                    label = { Text("Monto objetivo") },
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
                val monto = objetivo.toDoubleOrNull()
                if (nombre.isBlank()) {
                    error = "Escribe un nombre para la meta"
                } else if (monto == null || monto <= 0) {
                    error = "El monto debe ser un número mayor a 0"
                } else {
                    onCrear(nombre.trim(), monto)
                }
            }) { Text("Crear") }
        },
        dismissButton = {
            TextButton(onClick = onCerrar) { Text("Cancelar") }
        }
    )
}

// Diálogo para ingresar un aporte
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
