package com.example.app_brote

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
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

// Barra inferior y navegación de 3 pestañas (Inicio, Metas, Jardín)
@Composable
fun PantallaPrincipal(vm: MetasViewModel) {
    // 0 = Inicio, 1 = Metas, 2 = Jardín
    var pestana by remember { mutableIntStateOf(0) }
    var mostrarCrear by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = pestana == 0,
                    onClick = { pestana = 0 },
                    icon = { Text("🌱", fontSize = 20.sp) },
                    label = { Text("Inicio") }
                )
                NavigationBarItem(
                    selected = pestana == 1,
                    onClick = { pestana = 1 },
                    icon = { Text("🎯", fontSize = 20.sp) },
                    label = { Text("Metas") }
                )
                NavigationBarItem(
                    selected = pestana == 2,
                    onClick = { pestana = 2 },
                    icon = { Text("🌸", fontSize = 20.sp) },
                    label = { Text("Jardín") }
                )
            }
        },
        floatingActionButton = {
            if (pestana == 1) {
                FloatingActionButton(
                    onClick = { mostrarCrear = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape
                ) {
                    Text("+", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (pestana) {
                0 -> PantallaInicio(
                    vm = vm,
                    onIrAMetas = { pestana = 1 },
                    onIrAJardin = { pestana = 2 },
                    onNuevaMeta = { mostrarCrear = true }
                )
                1 -> PantallaMetas(vm)
                else -> PantallaJardin(vm)
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
            title = {
                Text(
                    text = "¡Flor desbloqueada!",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleLarge
                )
            },
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
                        text = "¡Felicidades! Completaste la meta \"${florReciente.metaNombre.ifEmpty { "Meta lograda" }}\" y esta flor se ha incorporado a tu jardín.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        vm.cerrarAvisoFlor()
                        pestana = 2 // Cambiar a la pestaña Jardín para verla
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Ir al Jardín")
                }
            },
            dismissButton = {
                TextButton(onClick = { vm.cerrarAvisoFlor() }) {
                    Text("Aceptar")
                }
            },
            shape = RoundedCornerShape(22.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

// Pantalla de Inicio basada en fondoinicio.jpg
@Composable
fun PantallaInicio(
    vm: MetasViewModel,
    onIrAMetas: () -> Unit,
    onIrAJardin: () -> Unit,
    onNuevaMeta: () -> Unit
) {
    val totalAhorrado = vm.metas.sumOf { it.ahorrado }
    val metasActivas = vm.metas.count { !it.completada }
    val floresLogradas = vm.flores.size

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val availableWidth = maxWidth
        val availableHeight = maxHeight

        // Proporción exacta de fondoinicio.jpg (606 x 1200 px)
        val imageAspectRatio = 606f / 1200f
        val containerAspectRatio = availableWidth / availableHeight

        val (contentWidth, contentHeight) = if (containerAspectRatio > imageAspectRatio) {
            (availableHeight * imageAspectRatio) to availableHeight
        } else {
            availableWidth to (availableWidth / imageAspectRatio)
        }

        Box(
            modifier = Modifier.size(contentWidth, contentHeight)
        ) {
            // Fondo ilustrado sin deformarse
            Image(
                bitmap = ImageBitmap.imageResource(id = R.drawable.fondoinicio),
                contentDescription = "Fondo de Inicio Brote",
                contentScale = ContentScale.Fit,
                filterQuality = FilterQuality.None,
                modifier = Modifier.fillMaxSize()
            )

            // Contenido ubicado en el espacio central libre (y ≈ 18% a 78%)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = contentHeight * 0.17f,
                        bottom = contentHeight * 0.25f,
                        start = 22.dp,
                        end = 22.dp
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Cabecera de bienvenida
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                    shadowElevation = 4.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🌱 Brote",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Tu jardín de ahorro y constancia",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Resumen del ahorro y progreso
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    shadowElevation = 5.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Total Ahorrado",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = dinero(totalAhorrado),
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.weight(1f).padding(end = 6.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("🎯 $metasActivas", style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        text = "Metas activas",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.weight(1f).padding(start = 6.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("🌸 $floresLogradas", style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        text = "Flores logradas",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Botones de acción rápida
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onIrAMetas,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Ver mis metas 🎯", style = MaterialTheme.typography.labelLarge)
                    }

                    OutlinedButton(
                        onClick = onIrAJardin,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.90f)
                        ),
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Text(
                            text = "Explorar mi jardín 🌸",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

// Lista de metas activas con el mismo fondo de inicio
@Composable
fun PantallaMetas(vm: MetasViewModel) {
    val activas = vm.metas.filter { !it.completada }
    var metaParaAporte by remember { mutableStateOf<Meta?>(null) }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val availableWidth = maxWidth
        val availableHeight = maxHeight

        val imageAspectRatio = 606f / 1200f
        val containerAspectRatio = availableWidth / availableHeight

        val (contentWidth, contentHeight) = if (containerAspectRatio > imageAspectRatio) {
            (availableHeight * imageAspectRatio) to availableHeight
        } else {
            availableWidth to (availableWidth / imageAspectRatio)
        }

        Box(
            modifier = Modifier.size(contentWidth, contentHeight)
        ) {
            // Mismo fondo de la pantalla de inicio
            Image(
                bitmap = ImageBitmap.imageResource(id = R.drawable.fondoinicio),
                contentDescription = "Fondo de Metas",
                contentScale = ContentScale.Fit,
                filterQuality = FilterQuality.None,
                modifier = Modifier.fillMaxSize()
            )

            // Contenedor de contenido dentro de la zona central de la ilustración
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = contentHeight * 0.16f,
                        bottom = contentHeight * 0.12f,
                        start = 18.dp,
                        end = 18.dp
                    )
            ) {
                // Título de la sección
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                    shadowElevation = 3.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Mis Metas",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (activas.isEmpty()) "Sin metas en curso" else "${activas.size} en crecimiento",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (activas.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            shadowElevation = 3.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Aún no tienes metas activas",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Toca el botón + para comenzar a ahorrar y ver crecer tu primera planta.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(activas, key = { it.id }) { meta ->
                            TarjetaMeta(
                                meta = meta,
                                onAportar = { metaParaAporte = meta },
                                onEliminar = { vm.eliminarMeta(meta.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (metaParaAporte != null) {
        DialogoAporte(
            meta = metaParaAporte!!,
            onCerrar = { metaParaAporte = null },
            onGuardar = { monto ->
                vm.agregarAporte(metaParaAporte!!.id, monto)
                metaParaAporte = null
            }
        )
    }
}

// Tarjeta de meta con etapa de crecimiento de la planta
@Composable
fun TarjetaMeta(meta: Meta, onAportar: () -> Unit, onEliminar: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Contenedor visual de la planta según su etapa
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(68.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (meta.completada) {
                            val bitmap = rememberFlorSinFondo(meta.florResId)
                            Image(
                                bitmap = bitmap,
                                contentDescription = "Flor desarrollada",
                                filterQuality = FilterQuality.None,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.size(44.dp)
                            )
                        } else {
                            Text(meta.etapa.icono, fontSize = 32.sp)
                        }
                        Text(
                            text = meta.etapa.nombre,
                            style = MaterialTheme.typography.labelSmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = meta.nombre,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Ahorrado: ${dinero(meta.ahorrado)} de ${dinero(meta.objetivo)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Faltan: ${dinero(meta.restante)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Barra de progreso estilizada
            LinearProgressIndicator(
                progress = { meta.progreso },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = if (meta.completada) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primaryContainer
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${(meta.progreso * 100).toInt()}% alcanzado",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (meta.completada) "¡Completada!" else "En crecimiento",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (meta.completada) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onEliminar,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onAportar,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("+ Aporte")
                }
            }
        }
    }
}

// Pantalla del Jardín con fondojardin, parcelas elevadas, nombres de metas y paginación
@Composable
fun PantallaJardin(vm: MetasViewModel) {
    val floresPagina = vm.floresDePagina(vm.paginaActual)
    val totalPaginas = vm.totalPaginas
    var florSeleccionada by remember { mutableStateOf<FlorDesbloqueada?>(null) }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val availableWidth = maxWidth
        val availableHeight = maxHeight

        // Proporción exacta de fondojardin (602 x 1200 px)
        val imageAspectRatio = 602f / 1200f
        val containerAspectRatio = availableWidth / availableHeight

        val (gardenWidth, gardenHeight) = if (containerAspectRatio > imageAspectRatio) {
            (availableHeight * imageAspectRatio) to availableHeight
        } else {
            availableWidth to (availableWidth / imageAspectRatio)
        }

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
                    val nombreMeta = flor.metaNombre.ifEmpty {
                        vm.metas.find { it.id == flor.metaId }?.nombre ?: "Meta lograda"
                    }

                    // Contenedor interactivo de la flor y su nombre de meta
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .offset(x = left, y = top)
                            .width(florSize)
                            .clickable { florSeleccionada = flor }
                    ) {
                        Image(
                            bitmap = bitmap,
                            contentDescription = "Flor de $nombreMeta",
                            filterQuality = FilterQuality.None,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.size(florSize)
                        )

                        // Cartel con el nombre de la meta lograda
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            shadowElevation = 3.dp,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text(
                                text = nombreMeta,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Estado vacío si no hay flores en toda la app
            if (vm.flores.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                    shadowElevation = 6.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier
                        .padding(24.dp)
                        .align(Alignment.Center)
                ) {
                    Column(
                        modifier = Modifier.padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🌱", fontSize = 44.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Tu jardín está esperando su primer brote",
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Completa una meta al 100% para desbloquear y plantar tu primera flor.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                shadowElevation = 5.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    IconButton(
                        onClick = { vm.irAPaginaAnterior() },
                        enabled = vm.paginaActual > 0
                    ) {
                        Text("◀", fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                    }

                    Text(
                        text = "Jardín ${vm.paginaActual + 1} / $totalPaginas",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    IconButton(
                        onClick = { vm.irAPaginaSiguiente() },
                        enabled = vm.paginaActual < totalPaginas - 1
                    ) {
                        Text("▶", fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }

    // Modal de detalle al tocar una flor del jardín
    if (florSeleccionada != null) {
        val flor = florSeleccionada!!
        val nombreMeta = flor.metaNombre.ifEmpty {
            vm.metas.find { it.id == flor.metaId }?.nombre ?: "Meta lograda"
        }

        AlertDialog(
            onDismissRequest = { florSeleccionada = null },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    text = "Flor de \"$nombreMeta\"",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val bitmap = rememberFlorSinFondo(flor.florResId)
                    Image(
                        bitmap = bitmap,
                        contentDescription = "Flor de $nombreMeta",
                        filterQuality = FilterQuality.None,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(90.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "¡Meta cumplida con éxito!",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Ubicada en el Jardín ${flor.pagina + 1}, Parcela ${flor.parcela + 1}.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { florSeleccionada = null },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Cerrar")
                }
            }
        )
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
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Nueva meta de ahorro",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Define el nombre y el monto objetivo que deseas alcanzar.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = {
                        nombre = it
                        if (error.isNotEmpty()) error = ""
                    },
                    label = { Text("Nombre de la meta") },
                    placeholder = { Text("Ej: Bicicleta, Vacaciones") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = objetivo,
                    onValueChange = {
                        objetivo = it
                        if (error.isNotEmpty()) error = ""
                    },
                    label = { Text("Monto objetivo") },
                    placeholder = { Text("Ej: 1000") },
                    prefix = { Text("$ ") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                if (error.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val monto = objetivo.toDoubleOrNull()
                    if (nombre.isBlank()) {
                        error = "Escribe un nombre para tu meta"
                    } else if (monto == null || monto <= 0) {
                        error = "Ingresa un monto objetivo mayor a 0"
                    } else {
                        onCrear(nombre.trim(), monto)
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Crear meta")
            }
        },
        dismissButton = {
            TextButton(onClick = onCerrar) {
                Text("Cancelar")
            }
        }
    )
}

// Diálogo para agregar ahorro con tope estricto y chips de sugerencia
@Composable
fun DialogoAporte(meta: Meta, onCerrar: () -> Unit, onGuardar: (Double) -> Unit) {
    var texto by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onCerrar,
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Agregar ahorro",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Meta: ${meta.nombre}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column {
                // Tarjeta informativa con el estado actual y lo que falta
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Ahorrado: ${dinero(meta.ahorrado)} / ${dinero(meta.objetivo)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${(meta.progreso * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Falta para completar: ${dinero(meta.restante)}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Chips rápidos de sugerencia
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (meta.restante > 100) {
                        SuggestionChip(
                            onClick = {
                                texto = "100"
                                error = ""
                            },
                            label = { Text("+$100") }
                        )
                    }
                    if (meta.restante > 500) {
                        SuggestionChip(
                            onClick = {
                                texto = "500"
                                error = ""
                            },
                            label = { Text("+$500") }
                        )
                    }
                    SuggestionChip(
                        onClick = {
                            texto = meta.restante.toLong().toString()
                            error = ""
                        },
                        label = { Text("Completar (${dinero(meta.restante)})") },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = texto,
                    onValueChange = {
                        texto = it
                        val valor = it.toDoubleOrNull()
                        error = when {
                            valor != null && valor > meta.restante ->
                                "No puedes exceder lo pactado. El máximo permitido es ${dinero(meta.restante)}."
                            valor != null && valor <= 0 ->
                                "Ingresa un monto mayor a 0."
                            else -> ""
                        }
                    },
                    label = { Text("Monto a aportar") },
                    placeholder = { Text("Máximo: ${dinero(meta.restante)}") },
                    prefix = { Text("$ ") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(14.dp),
                    isError = error.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                )

                if (error.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val monto = texto.toDoubleOrNull()
                    when {
                        monto == null || monto <= 0 -> {
                            error = "Escribe un valor mayor a 0"
                        }
                        monto > meta.restante -> {
                            error = "No puedes ingresar más de lo que falta (${dinero(meta.restante)})"
                        }
                        else -> {
                            onGuardar(monto)
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Guardar aporte")
            }
        },
        dismissButton = {
            TextButton(onClick = onCerrar) {
                Text("Cancelar")
            }
        }
    )
}

