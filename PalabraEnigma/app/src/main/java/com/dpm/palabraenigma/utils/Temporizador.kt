package com.dpm.palabraenigma.utils

import android.content.Context
import android.os.Handler
import android.util.Log
import android.widget.Button
import android.widget.TextView

/**
 * Clase que implementa un temporizador.
 * El temporizador actualiza un botón de la interfaz de usuario (UI) con el tiempo restante
 * y permite pausar y reanudar el conteo. También controla la lógica cuando el tiempo se agota.
 *
 * @param textView El texto de la interfaz que se actualizará con el tiempo restante.
 * @param tiempo El tiempo inicial (en segundos) que el jugador tiene disponible.
 */
class Temporizador(
    private val textView: TextView,
    private val tiempo:Int,
    private val context: Context,
    private val btComprobar: Button // Añadimos el botón
    ) : Runnable {
    private var seconds: Int = tiempo
    private val uiHandler: Handler = Handler() // Para modificar la UI
    private var paused: Boolean = false //False para que inicie nada más empiece la partida, CAMBIAR
    private var stopThread: Boolean = false
    private var sinTiempo: Boolean = false

    init {
        updateUI() // Actualiza el botón con el tiempo inicial
    }

    /**
     * Método ejecutado cuando el temporizador comienza a correr en un hilo separado.
     * Actualiza el tiempo cada segundo, disminuyendo los segundos si no está pausado.
     * Si el tiempo se agota, detiene el temporizador y lo marca como pausado.
     */
    override fun run() {
        try {
            while (!stopThread) {
                Thread.sleep(1000)
                if (!paused) {
                    if (seconds > 0) {
                        seconds--
                        updateUI() // Actualiza el texto del botón
                    } else {
                        sinTiempo = true
                        uiHandler.post {
                            btComprobar.performClick() // Simulamos el clic al botón
                            btComprobar.isEnabled = false // Desactiva el botón
                        }
                        paused = true
                        Log.i("Temporizador", "Se ha se ha quedado sin tiempo.")
                        updateUIFinish()
                    }
                }
            }
        } catch (e: InterruptedException) {
            Log.i("Temporizador", "Error en el temporizador: ${e.message}")
        }
    }

    /**
     * Alterna el estado de pausa del temporizador.
     * Si está pausado, lo reanuda; si está corriendo, lo pausa.
     */
    fun togglePause() {
        paused = !paused
    }

    /**
     * Verifica si el temporizador está en funcionamiento (no pausado).
     *
     * @return `true` si el temporizador está corriendo, `false` si está pausado.
     */
    fun isRunning(): Boolean {
        return !paused
    }


    /**
     * Actualiza la interfaz de usuario con el tiempo formateado restante.
     * Utiliza un Handler para asegurarse de que la UI se modifique en el hilo principal.
     */
    private fun updateUI() {
        val tiempoFormateado = Ayudante.formatearTiempo(seconds)

        // Actualiza el texto del botón
        uiHandler.post {
            textView.text = tiempoFormateado
        }
    }

    private fun updateUIFinish() {

    }

    /**
     * Establece un nuevo tiempo para el temporizador.
     *
     * @param tiempo Nuevo tiempo en segundos.
     */
    fun setTiempo(tiempo: Int) {
        seconds = tiempo
        sinTiempo = false
    }

    fun aumentarTiempo(tiempo: Int){
        seconds += tiempo
        sinTiempo = false
    }

    fun obtenerSinTiempo() : Boolean = sinTiempo
}



