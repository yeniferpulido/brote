package com.example.app_brote

import org.junit.Assert.*
import org.junit.Test

class JardinLogicaTest {

    @Test
    fun testEtapasDeCrecimiento() {
        val metaSemilla = Meta(id = 1L, nombre = "Ahorro 1", objetivo = 1000.0, ahorrado = 0.0)
        assertEquals(EtapaCrecimiento.SEMILLA, metaSemilla.etapa)
        assertFalse(metaSemilla.completada)

        val metaBrote = Meta(id = 2L, nombre = "Ahorro 2", objetivo = 1000.0, ahorrado = 300.0)
        assertEquals(EtapaCrecimiento.BROTE, metaBrote.etapa)
        assertFalse(metaBrote.completada)

        val metaHojas = Meta(id = 3L, nombre = "Ahorro 3", objetivo = 1000.0, ahorrado = 700.0)
        assertEquals(EtapaCrecimiento.PLANTA_CON_HOJAS, metaHojas.etapa)
        assertFalse(metaHojas.completada)

        val metaFlor = Meta(id = 4L, nombre = "Ahorro 4", objetivo = 1000.0, ahorrado = 1000.0)
        assertEquals(EtapaCrecimiento.FLOR, metaFlor.etapa)
        assertTrue(metaFlor.completada)
    }

    @Test
    fun testParcelasJardinValidas() {
        assertEquals(12, PARCELAS_JARDIN.size)
        assertEquals(PARCELAS_POR_PAGINA, PARCELAS_JARDIN.size)

        // Verificar que todas las coordenadas normalizadas están dentro del rango (0f..1f)
        for (parcela in PARCELAS_JARDIN) {
            assertTrue("xNorm debe estar entre 0 y 1", parcela.xNorm in 0f..1f)
            assertTrue("yNorm debe estar entre 0 y 1", parcela.yNorm in 0f..1f)
        }

        // Verificar que no hay identificadores de parcela duplicados
        val ids = PARCELAS_JARDIN.map { it.id }
        assertEquals(12, ids.toSet().size)
    }

    @Test
    fun testAsignacionDePaginasYParcelasSinColisiones() {
        val flores = mutableListOf<FlorDesbloqueada>()

        fun buscarSiguienteLibre(): Pair<Int, Int> {
            val ocupadas = flores.map { it.pagina to it.parcela }.toSet()
            var p = 0
            while (true) {
                for (parcela in 0 until PARCELAS_POR_PAGINA) {
                    if (!ocupadas.contains(p to parcela)) {
                        return p to parcela
                    }
                }
                p++
            }
        }

        // Simular el llenado de la primera página (12 parcelas)
        for (i in 0 until 12) {
            val (pagina, parcela) = buscarSiguienteLibre()
            assertEquals(0, pagina)
            assertEquals(i, parcela)
            flores.add(
                FlorDesbloqueada(
                    id = i.toLong(),
                    metaId = i.toLong(),
                    florTipo = 1,
                    pagina = pagina,
                    parcela = parcela
                )
            )
        }

        // La flor número 13 debe pasar automáticamente a la página 1, parcela 0
        val (pagina13, parcela13) = buscarSiguienteLibre()
        assertEquals(1, pagina13)
        assertEquals(0, parcela13)
    }

    @Test
    fun testIdempotenciaDesbloqueo() {
        val meta = Meta(id = 100L, nombre = "Bicicleta", objetivo = 500.0, ahorrado = 500.0)
        val flores = mutableListOf<FlorDesbloqueada>()

        fun intentarDesbloquear(m: Meta) {
            if (m.completada && flores.none { it.metaId == m.id }) {
                flores.add(
                    FlorDesbloqueada(
                        id = System.currentTimeMillis() + flores.size,
                        metaId = m.id,
                        florTipo = m.florTipo,
                        pagina = 0,
                        parcela = 0
                    )
                )
            }
        }

        // Intentar desbloquear múltiples veces la misma meta completada
        intentarDesbloquear(meta)
        intentarDesbloquear(meta)
        intentarDesbloquear(meta)

        assertEquals(1, flores.size)
        assertEquals(100L, flores[0].metaId)
    }

    @Test
    fun testFormatoValoresNumericosConPuntos() {
        assertEquals("$0", dinero(0.0))
        assertEquals("$100", dinero(100.0))
        assertEquals("$1.000", dinero(1000.0))
        assertEquals("$15.000", dinero(15000.0))
        assertEquals("$250.000", dinero(250000.0))
        assertEquals("$2.000.000", dinero(2000000.0))

        assertEquals("1.000", formatearNumero(1000.0))
        assertEquals("50.000", formatearNumero(50000.0))
        assertEquals("1.500.000", formatearNumero(1500000.0))

        assertEquals(1000.0, limpiarMonto("1.000"))
        assertEquals(250000.0, limpiarMonto("250.000"))
        assertEquals(2000000.0, limpiarMonto("$2.000.000"))
    }
}

