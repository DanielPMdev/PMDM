package com.dpm.chessclock

import android.content.Context
import android.os.Handler
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import androidx.core.content.ContextCompat

/**
 * Clase que implementa un temporizador de ajedrez para un jugador.
 * El temporizador actualiza un botón de la interfaz de usuario (UI) con el tiempo restante
 * y permite pausar y reanudar el conteo. También controla la lógica cuando el tiempo se agota.
 *
 * @param playerName Nombre del jugador asociado a este temporizador.
 * @param button El botón de la interfaz que se actualizará con el tiempo restante.
 * @param tiempo El tiempo inicial (en segundos) que el jugador tiene disponible.
 */
class ChessTimer(private val playerName: String, private val button: Button, private val tiempo:Int, private val context: Context) : Runnable {
    private var seconds: Int = tiempo
    private val uiHandler: Handler = Handler() // Para modificar la UI
    private var paused: Boolean = true
    private var stopThread: Boolean = false

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
                        paused = true
                        Log.i("ChessTimer", "$playerName se ha quedado sin tiempo.")
                        updateUIFinish()
                    }
                }
            }
        } catch (e: InterruptedException) {
            Log.i("ChessTimer", "Error en el temporizador de $playerName: ${e.message}")
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
            button.text = tiempoFormateado
        }
    }

    private fun updateUIFinish() {
        // Actualiza el boton cuando se a acabado el tiempo
        uiHandler.post {
            button.setBackgroundColor(ContextCompat.getColor(context, R.color.rojochillon))
            button.isEnabled = false
        }
        if (context is MainActivity) {
            val mainActivity = context as MainActivity
            val btPause = mainActivity.findViewById<ImageButton>(R.id.pauseButton)
            btPause.isEnabled = false
            btPause.setImageResource(R.drawable.play)
        }
    }

    /**
     * Establece un nuevo tiempo para el temporizador.
     *
     * @param tiempo Nuevo tiempo en segundos.
     */
    fun setTiempo(tiempo: Int) {
        seconds = tiempo
    }
}



