package com.dpm.chessclock

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

/**
 * La actividad principal de la aplicación que implementa un temporizador de ajedrez para dos jugadores.
 * Esta actividad permite gestionar el tiempo de cada jugador, alternar entre ellos y modificar
 * el tiempo inicial a través de un diálogo de selección.
 */
class MainActivity : AppCompatActivity(), OnDialogResultListener, View.OnClickListener {
    private var tiempo = 60

    private var contadorPlayer01: Int = 0
    private var contadorPlayer02: Int = 0

    private lateinit var player1Timer: ChessTimer
    private lateinit var player2Timer: ChessTimer

    private lateinit var player1Button: Button
    private lateinit var player2Button: Button

    private lateinit var txMoves01: TextView
    private lateinit var txMoves02: TextView
    private lateinit var btPause: ImageButton

    private var resetear = false
    private var pulsadoPause = false //False para sin iniciar, true para iniciado
    private var jugadorActivo = 1
    private var primerMovimiento = true

    /**
     * Método llamado cuando se crea la actividad.
     * Configura la UI, los botones, los temporizadores y los hilos asociados a cada jugador.
     *
     * @param savedInstanceState Estado previamente guardado de la actividad.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Asignación de los botones desde el layout
        player1Button = findViewById(R.id.player01)
        player2Button = findViewById(R.id.player02)

        btPause = findViewById(R.id.pauseButton)
        val btTimer: ImageButton = findViewById(R.id.timerButton)
        val btReload: ImageButton = findViewById(R.id.reloadButton)

        // Asignación de los textView de los movimientos
        txMoves01 = findViewById(R.id.txvPlayer01)
        txMoves02 = findViewById(R.id.txvPlayer02)

        // Actualiza el texto para cada jugador usando la misma cadena de recursos
        txMoves01.text = getString(R.string.movesTxt) + " " + contadorPlayer01
        txMoves02.text = getString(R.string.movesTxt) + " " + contadorPlayer02

        // Crear los temporizadores para los dos jugadores
        player1Timer = ChessTimer("Jugador 1", player1Button, tiempo, this)
        player2Timer = ChessTimer("Jugador 2", player2Button, tiempo, this)

        // Crear hilos para los temporizadores de cada jugador
        val player1Thread = Thread(player1Timer)
        val player2Thread = Thread(player2Timer)

        // Iniciar los hilos, pero mantener ambos temporizadores pausados inicialmente
        player1Thread.start()
        player2Thread.start()

        //Escuchas de los botones
        player1Button.setOnClickListener(this)
        player2Button.setOnClickListener(this)
        btTimer.setOnClickListener(this)
        btPause.setOnClickListener(this)
        btReload.setOnClickListener(this)


    }

    override fun onClick(view: View?) {
        when (view) {
            is Button -> {
                when (view?.id) {
                    R.id.player01 -> {
                        btPause.setImageResource(R.drawable.pause)
                        pulsadoPause = true

                        //Contador
                        if(!primerMovimiento && jugadorActivo == 1){
                            contadorPlayer01++
                            txMoves01.text = getString(R.string.movesTxt) + " " + contadorPlayer01
                        }
                        primerMovimiento = false
                        jugadorActivo = 2

                        if (player1Timer.isRunning()) { // Si el temporizador del Jugador 1 está corriendo
                            player1Timer.togglePause()  // Pausar el temporizador del Jugador 1
                        }
                        player1Button.isEnabled = false; // Desabilitamos el boton 1
                        player2Button.isEnabled = true; // Habilitamos el boton 2
                        player2Timer.togglePause() // Reanudar el temporizador del Jugador 2

                    }

                    R.id.player02 -> {
                        btPause.setImageResource(R.drawable.pause)
                        pulsadoPause = true

                        //Contador
                        if(!primerMovimiento  && jugadorActivo == 2){
                            contadorPlayer02++
                            txMoves02.text = getString(R.string.movesTxt) + " " + contadorPlayer02
                        }
                        primerMovimiento = false
                        jugadorActivo = 1

                        if (player2Timer.isRunning()) { // Si el temporizador del Jugador 2 está corriendo
                            player2Timer.togglePause()  // Pausar el temporizador del Jugador 2
                        }
                        player2Button.isEnabled = false; // Desabilitamos el boton 2
                        player1Button.isEnabled = true; // Habilitamos el boton 2
                        player1Timer.togglePause() // Reanudar el temporizador del Jugador 1


                    }
                }
            }

            is ImageButton -> {
                when (view?.id) {
                    R.id.timerButton -> {
                        mostrarDialogo() //Muestra el diálogo
                    }

                    R.id.pauseButton -> {
                        if (!pulsadoPause) {
                            btPause.setImageResource(R.drawable.pause)
                            pulsadoPause = true
                            //Iniciar de Primeras el Boton de Arriba
                            if (jugadorActivo == 1) {
                                player1Timer.togglePause()
                                player2Button.isEnabled = false
                            } else {
                                player2Timer.togglePause()
                                player1Button.isEnabled = false
                            }
                        } else {
                            btPause.setImageResource(R.drawable.play)
                            pausarBotones()
                            pulsadoPause = false
                        }
                    }

                    R.id.reloadButton -> {
                        avisoReset()
                    }
                }
            }
        }
    }

    /**
     * Muestra el diálogo para seleccionar un tiempo predefinido.
     */
    private fun mostrarDialogo() {
        val timeDialogFragment = TimeDialogFragment()
        timeDialogFragment.show(supportFragmentManager, "Diálogo de elección de tiempo")
    }

    /**
     * Muestra el diálogo para resetar los timers.
     */
    private fun avisoReset() {
        val reloadDialogFragment = ReloadDialogFragment()
        reloadDialogFragment.show(supportFragmentManager, "Diálogo de reseteo de timers")
    }

    /**
     * Método que recibe el resultado del diálogo con el tiempo seleccionado.
     * Actualiza los temporizadores y botones con el nuevo tiempo.
     *
     * @param result El tiempo seleccionado en segundos.
     */
    override fun onResult(result: Int) { //DEL TIEMPO
        tiempo = result
        avisoReset()

        /*pausarBotones()
        val tiempoFormateado = Ayudante.formatearTiempo(tiempo)
        player1Timer.setTiempo(tiempo)
        player1Button.text = tiempoFormateado
        player2Timer.setTiempo(tiempo)
        player2Button.text = tiempoFormateado*/
    }

    /**
     * Método que recibe el resultado del diálogo de resetar los timers.
     * Resetea los timers si es true, si es false no hace nada.
     *
     * @param result True or false según haya seleccionado el usuario.
     */
    override fun onResult(result: Boolean) { //DEL RESETEAR
        resetear = result

        if (resetear) {
            //Parar los Botones
            pausarBotones()

            //Formatear el Tiempo
            val tiempoFormateado = Ayudante.formatearTiempo(tiempo)
            player1Timer.setTiempo(tiempo)
            player1Button.text = tiempoFormateado
            player2Timer.setTiempo(tiempo)
            player2Button.text = tiempoFormateado

            //Caso que el tiempo haya acabado
            if (!player2Button.isEnabled && !player1Button.isEnabled) {
                player1Button.isEnabled = true
                player2Button.isEnabled = true

                player1Button.setBackgroundResource(R.drawable.btstyle01)
                player2Button.setBackgroundResource(R.drawable.btstyle01)
            }

            //Resetear Contadores
            contadorPlayer01 = 0
            contadorPlayer02 = 0
            txMoves01.text = getString(R.string.movesTxt) + " " + contadorPlayer01
            txMoves02.text = getString(R.string.movesTxt) + " " + contadorPlayer02

            //Poner el boton de pausa con el icono del play
            btPause.setImageResource(R.drawable.play)
            pulsadoPause = false
            primerMovimiento = true
        }
    }

    private fun pausarBotones() {
        if (player1Timer.isRunning()) { //Si el timer del jugador 1 esta corriendo
            jugadorActivo = 1
            player1Timer.togglePause() //Lo ponemos en pausa
            player2Button.isEnabled = true; //Activamos el boton del jugador 2
        }
        if (player2Timer.isRunning()) {
            jugadorActivo = 2
            player2Timer.togglePause()
            player1Button.isEnabled = true;
        }
    }
}