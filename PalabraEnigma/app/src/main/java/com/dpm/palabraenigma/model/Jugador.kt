package com.dpm.palabraenigma.model

data class Jugador(
    private var puntos: Int = 0,
    private var intentos: Int = 3 // Número de intentos iniciales
    ) {

    private val nivel: Int
        get() = calcularNivel()

    fun obtenerPuntos(): Int = puntos
    fun obtenerIntentos(): Int = intentos
    fun obtenerNivel(): Int = nivel

    fun setIntentos(intentos: Int){
        this.intentos = intentos
    }

    // Incrementar puntos al acertar
    fun incrementarPuntos(puntosGanados: Int) {
        puntos += puntosGanados
    }

    // Reducir intentos al fallar
    fun decrementarIntentos(){
        if (intentos > 0) {
            intentos--
        }
    }

    fun restarPuntos(cantidad : Int){
        puntos -= cantidad
    }

    // Calcular el nivel del jugador basado en puntos
    private fun calcularNivel(): Int {
        return when {
            puntos < 50 -> 1
            puntos < 100 -> 2
            puntos < 200 -> 3
            puntos < 300 -> 4
            puntos < 400 -> 5
            puntos < 800 -> 6
            puntos < 1200 -> 7
            puntos < 1600 -> 8
            puntos < 2000 -> 9
            else -> 10 // Nivel máximo o avanzado
        }
    }
}