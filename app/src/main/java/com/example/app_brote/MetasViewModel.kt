package com.example.app_brote

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel

class MetasViewModel(app: Application) : AndroidViewModel(app) {

    private val almacen = Almacen(app)

    // Lista observable de metas
    val metas = mutableStateListOf<Meta>()

    // Lista observable de flores desbloqueadas en el jardín
    val flores = mutableStateListOf<FlorDesbloqueada>()

    // Página actualmente visible en el jardín
    var paginaActual by mutableIntStateOf(0)

    // Flor recién desbloqueada para mostrar feedback visual al usuario
    var florRecienDesbloqueada by mutableStateOf<FlorDesbloqueada?>(null)

    init {
        metas.addAll(almacen.cargar())
        val cargadas = almacen.cargarFlores()

        // Sincronizar nombres de metas para flores cargadas si vinieran vacías
        val floresActualizadas = cargadas.map { f ->
            if (f.metaNombre.isEmpty()) {
                val nombre = metas.find { it.id == f.metaId }?.nombre ?: "Flor lograda"
                f.copy(metaNombre = nombre)
            } else {
                f
            }
        }
        flores.addAll(floresActualizadas)
        verificarDesbloqueosAutomaticos()
    }

    // Calcula cuántas páginas de jardín existen actualmente (mínimo 1)
    val totalPaginas: Int
        get() = maxOf(1, (flores.maxOfOrNull { it.pagina } ?: 0) + 1)

    // Flores asignadas a la página actualmente seleccionada
    fun floresDePagina(pagina: Int): List<FlorDesbloqueada> {
        return flores.filter { it.pagina == pagina }
    }

    fun crearMeta(nombre: String, objetivo: Double) {
        val nuevoId = System.currentTimeMillis()
        val defaultFlorTipo = ((Math.abs(nuevoId) % FLORES_DISPONIBLES.size) + 1).toInt()
        metas.add(
            Meta(
                id = nuevoId,
                nombre = nombre,
                objetivo = objetivo,
                florTipo = defaultFlorTipo
            )
        )
        almacen.guardar(metas)
        verificarDesbloqueosAutomaticos()
    }

    fun agregarAporte(id: Long, monto: Double) {
        val posicion = metas.indexOfFirst { it.id == id }
        if (posicion >= 0) {
            val meta = metas[posicion]
            // Regla estricta: No se puede exceder lo pactado en la meta
            val montoMaximo = meta.restante
            val montoReal = minOf(monto, montoMaximo)
            if (montoReal > 0) {
                metas[posicion] = meta.copy(ahorrado = meta.ahorrado + montoReal)
                almacen.guardar(metas)
                verificarDesbloqueosAutomaticos()
            }
        }
    }

    fun eliminarMeta(id: Long) {
        metas.removeAll { it.id == id }
        almacen.guardar(metas)
    }

    // Navegación entre páginas del jardín
    fun irAPaginaAnterior() {
        if (paginaActual > 0) {
            paginaActual--
        }
    }

    fun irAPaginaSiguiente() {
        if (paginaActual < totalPaginas - 1) {
            paginaActual++
        }
    }

    fun cerrarAvisoFlor() {
        florRecienDesbloqueada = null
    }

    // Verificación segura e idempotente de desbloqueo de flores
    private fun verificarDesbloqueosAutomaticos() {
        val completadas = metas.filter { it.completada }
        var huboCambios = false

        for (meta in completadas) {
            // Regla de idempotencia: cada meta solo puede desbloquear 1 flor
            val yaDesbloqueada = flores.any { it.metaId == meta.id }
            if (!yaDesbloqueada) {
                val (paginaLibre, parcelaLibre) = buscarSiguienteParcelaLibre()

                val nuevaFlor = FlorDesbloqueada(
                    id = System.currentTimeMillis() + flores.size,
                    metaId = meta.id,
                    florTipo = meta.florTipo,
                    florResId = recursoFlorPorTipo(meta.florTipo),
                    pagina = paginaLibre,
                    parcela = parcelaLibre,
                    fechaDesbloqueo = System.currentTimeMillis(),
                    metaNombre = meta.nombre
                )

                flores.add(nuevaFlor)
                florRecienDesbloqueada = nuevaFlor
                huboCambios = true
            }
        }

        if (huboCambios) {
            almacen.guardarFlores(flores)
        }
    }

    // Encuentra la primera parcela desocupada; si una página se llena (12 parcelas),
    // crea automáticamente la siguiente página comenzando en la parcela 0.
    private fun buscarSiguienteParcelaLibre(): Pair<Int, Int> {
        val ocupadas = flores.map { it.pagina to it.parcela }.toSet()
        var p = 0
        while (true) {
            for (parcela in 0 until PARCELAS_POR_PAGINA) {
                if (!ocupadas.contains(p to parcela)) {
                    return p to parcela
                }
            }
            p++ // La página actual está llena, avanzar a la siguiente página
        }
    }
}
