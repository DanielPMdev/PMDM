package com.dpm.chessclock

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
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
    private var pulsadoPause = false //Boton play = false | boton pause = true
    private var jugadorActivo = 1 //Que jugador esta activo??
    private var primerMovimiento = true //Valorar si es el primer movimiento

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
                        funcionamientoJugadores(
                            jugadorActivo = 1,
                            txMoves = txMoves01,
                            timerActivo = player1Timer,
                            timerPausado = player2Timer,
                            botonActivo = player1Button,
                            botonPausado = player2Button
                        )
                    }

                    R.id.player02 -> {
                        funcionamientoJugadores(
                            jugadorActivo = 2,
                            txMoves = txMoves02,
                            timerActivo = player2Timer,
                            timerPausado = player1Timer,
                            botonActivo = player2Button,
                            botonPausado = player1Button
                        )
                    }
                }
            }

            is ImageButton -> {
                when (view?.id) {
                    R.id.timerButton -> {
                        mostrarDialogo() //Muestra el diálogo
                    }

                    R.id.pauseButton -> {
                        //Si esta el icono de play
                        if (!pulsadoPause) {
                            // Cambia el icono del botón a pausa y marca que está en estado de pausa
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
                        } else { //Si esta el icono de pause
                            // Cambia el icono del botón a play y llama a la función pausarBotones para detener los temporizadores
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

            formatearTiempo()

            //Caso que el tiempo haya acabado
            if (!player2Button.isEnabled && !player1Button.isEnabled) {
                player1Button.isEnabled = true
                player2Button.isEnabled = true

                player1Button.setBackgroundResource(R.drawable.btstyle01)
                player2Button.setBackgroundResource(R.drawable.btstyle01)
            }

            resetearContadores()

            //Poner el boton de pausa con el icono del play
            btPause.setImageResource(R.drawable.play)
            btPause.isEnabled = true
            pulsadoPause = false
            primerMovimiento = true
        }
    }

    /**
     * Pausa los temporizadores de los jugadores y activa el botón correspondiente.
     * Si el temporizador del jugador 1 está corriendo, lo pausa y habilita el botón del jugador 2.
     * Si el temporizador del jugador 2 está corriendo, lo pausa y habilita el botón del jugador 1.
     */
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

    /**
     * Resetea los contadores de movimientos de ambos jugadores a 0.
     * También actualiza los textos de los TextViews que muestran el número de movimientos.
     */
    private fun resetearContadores(){
        //Resetear Contadores
        contadorPlayer01 = 0
        contadorPlayer02 = 0
        txMoves01.text = getString(R.string.movesTxt) + " " + contadorPlayer01
        txMoves02.text = getString(R.string.movesTxt) + " " + contadorPlayer02
    }

    /**
     * Formatea el tiempo para mostrarlo en los botones de ambos jugadores.
     * Usa la clase Ayudante para convertir el tiempo en un formato legible y luego actualiza
     * tanto los temporizadores como el texto de los botones con el tiempo formateado.
     */
    private fun formatearTiempo(){
        //Formatear el Tiempo
        val tiempoFormateado = Ayudante.formatearTiempo(tiempo)
        player1Timer.setTiempo(tiempo)
        player1Button.text = tiempoFormateado
        player2Timer.setTiempo(tiempo)
        player2Button.text = tiempoFormateado
    }

    /**
     * Gestiona el funcionamiento de los jugadores, controlando los contadores, temporizadores y botones
     * de acuerdo con el jugador activo actual.
     *
     * @param jugadorActivo El jugador que está realizando la acción (1 para jugador 1, 2 para jugador 2).
     * @param txMoves El TextView que muestra el número de movimientos del jugador.
     * @param timerActivo El temporizador del jugador que está actualmente en turno.
     * @param timerPausado El temporizador del jugador que está en pausa.
     * @param botonActivo El botón del jugador que está en turno y que se deshabilitará.
     * @param botonPausado El botón del jugador que está en pausa y que se habilitará.
     */
    private fun funcionamientoJugadores(
        jugadorActivo: Int,
        txMoves: TextView,
        timerActivo: ChessTimer,
        timerPausado: ChessTimer,
        botonActivo: Button,
        botonPausado: Button
    ) {
        // Actualiza el icono de pausa y marca que el botón de pausa ha sido pulsado
        btPause.setImageResource(R.drawable.pause)
        pulsadoPause = true

        // Incrementa el contador de movimientos del jugador activo si no es el primer movimiento
        if (!primerMovimiento && this.jugadorActivo == jugadorActivo) {
            if (jugadorActivo == 1) {
                contadorPlayer01++ // Incrementa el contador del jugador 1
                txMoves.text = getString(R.string.movesTxt) + " " + contadorPlayer01
            } else {
                contadorPlayer02++ // Incrementa el contador del jugador 2
                txMoves.text = getString(R.string.movesTxt) + " " + contadorPlayer02
            }
        }

        // Marca que ya no es el primer movimiento
        primerMovimiento = false

        // Cambia el jugador activo al siguiente (de 1 a 2, o de 2 a 1)
        this.jugadorActivo = if (jugadorActivo == 1)
            2 else 1

        // Pausa el temporizador del jugador activo y reanuda el temporizador del jugador en pausa
        if (timerActivo.isRunning()) {
            timerActivo.togglePause()  // Pausa el temporizador del jugador activo
        }

        // Deshabilita el botón del jugador activo y habilita el botón del jugador pausado
        botonActivo.isEnabled = false
        botonPausado.isEnabled = true

        // Reanuda el temporizador del jugador pausado
        timerPausado.togglePause()
    }
}


