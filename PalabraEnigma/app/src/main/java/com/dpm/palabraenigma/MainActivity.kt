package com.dpm.palabraenigma

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.gridlayout.widget.GridLayout
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.dpm.palabraenigma.data.Diccionario
import com.dpm.palabraenigma.logic.ButtonCreator
import com.dpm.palabraenigma.logic.Controlador
import com.dpm.palabraenigma.model.Jugador
import com.dpm.palabraenigma.utils.InstruccionesDialogFragment
import com.dpm.palabraenigma.utils.Temporizador

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var controlador: Controlador
    private lateinit var jugador: Jugador
    private lateinit var diccionario: Diccionario
    private lateinit var buttonCreator: ButtonCreator

    private lateinit var player1Timer: Temporizador
    private lateinit var player1TextView: TextView

    private lateinit var lbDesordenadas : TextView
    private lateinit var txPuntos: TextView
    private lateinit var txIntentos: TextView
    private lateinit var txNivel: TextView
    private lateinit var txRespuesta: EditText

    private var tiempo = 90
    private var contadorPalabras = 1

    private val viewModel: WordViewModel by viewModels() // Instancia del ViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //Dificultad del Menu:
        val sharedPreferences = getSharedPreferences("configuracion", Context.MODE_PRIVATE)
        val dificultad = sharedPreferences.getString("dificultad", "Facil") // "Facil" es el valor por defecto

        // Mostrar la dificultad al usuario, se guarda de la última selección
        Toast.makeText(this, "Dificultad seleccionada: $dificultad", Toast.LENGTH_SHORT).show()


        lbDesordenadas = findViewById(R.id.lbDesordenadas)
        txRespuesta = findViewById(R.id.txetRespuesta)
        txRespuesta.isFocusable = false
        txRespuesta.isFocusableInTouchMode = false
        val txAviso: TextView = findViewById(R.id.txAvisos)
        val txPalabra: TextView = findViewById(R.id.txPalabra)
        val txDefinicion: TextView = findViewById(R.id.txDefinicion)
        val btComprobar: Button = findViewById(R.id.btComprobar)
        txPuntos = findViewById(R.id.txPuntos)
        txIntentos = findViewById(R.id.txIntentos)
        txNivel = findViewById(R.id.txNivel)

        val btDoublePoints: ImageButton = findViewById(R.id.doublePoints)
        val btMoreTime: ImageButton = findViewById(R.id.moreTime)
        val btUnlockLetter: ImageButton = findViewById(R.id.unlockLetter)

        btDoublePoints.setOnClickListener(this)
        btMoreTime.setOnClickListener(this)
        btUnlockLetter.setOnClickListener(this)


        //Crear botones dinámicos
        val grid: GridLayout = findViewById(R.id.grid01)

        // Crear el temporizador para el jugador
        player1TextView = findViewById(R.id.txTiempo)
        player1Timer = Temporizador(player1TextView, tiempo, this, btComprobar)

        // Crear el hilo para el jugador
        val player1Thread = Thread(player1Timer)

        // Iniciar el hilo
        player1Thread.start()

        jugador = Jugador()
        diccionario = Diccionario()
        buttonCreator = ButtonCreator(this, grid)
        controlador = Controlador(jugador, diccionario, buttonCreator, player1Timer)

        //controlador.obtenerDiccionario().cargarDatos(this, seleccionarDiccionario(dificultad))
        controlador.obtenerDiccionario().cargarDatos(this, "listado_pruebas.txt")

        controlador.iniciarJuego()

        txPuntos.actualizar()

        //SACO LA PRIMERA PALABRA
        println(controlador.obtenerPalabraActual())

        controlador.obtenerPalabraActual()?.let {controlador.obtenerButtonCreator().setMiPalabra(it)}
        controlador.obtenerButtonCreator().añadeHijos()
        lbDesordenadas.text = controlador.obtenerPalabraActual()?.desordenar()

        controlador.verificarBotones(txRespuesta)

        btComprobar.setOnClickListener {
            if (!btComprobar.isEnabled) return@setOnClickListener // Evitar múltiples clics

            val respuestaUsuario = txRespuesta.text.toString().trim()

            controlador.palabraUsuario = respuestaUsuario  // Asignar la palabra del usuario al controlador

            val resultado = controlador.verificarPalabra()  // Verificar si la palabra es correcta
            val sinIntentos = controlador.verificarIntentos() //Verifica si al jugador le quedan intentos
            val sinTiempo = controlador.verificarSinTiempo() //Verificamos si se agoto el tiempo

            if (resultado || sinIntentos || sinTiempo) {
                player1Timer.togglePause()

                if (resultado){
                    txAviso.text = "¡Felicidades! Has acertado la palabra."
                    txPalabra.text  = "La palabra era: ${respuestaUsuario}"
                    txPuntos.actualizar()
                    //DEFINICION
                    viewModel.checkWordInWiktionary(respuestaUsuario).observe(this) { result ->
                        txDefinicion.text = result
                    }

                } else if (sinTiempo){
                    txAviso.text = "¡Se acabó el tiempo!"
                    txPalabra.text = "La palabra era: ${controlador.obtenerPalabraAnterior().toString()}"
                    viewModel.checkWordInWiktionary(controlador.obtenerPalabraAnterior().toString()).observe(this) { result ->
                        txDefinicion.text = result
                    }

                } else {
                    txAviso.text = "Te quedaste sin intentos, suerte en la proxima palabra"
                    txPalabra.text  = "La palabra era: ${controlador.obtenerPalabraAnterior().toString()}"
                    //DEFINICION
                    viewModel.checkWordInWiktionary(controlador.obtenerPalabraAnterior().toString()).observe(this) { result ->
                        txDefinicion.text = result
                    }
                }
                contadorPalabras++

                // Mostrar la siguiente palabra después de una pausa de 3 segundos
                if (contadorPalabras < controlador.obtenerDiccionario().obtenerListaPalabras().size) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        controlador.obtenerButtonCreator().limpiarGrid()
                        controlador.limpiarListasBotones()
                        controlador.obtenerPalabraActual()?.let {controlador.obtenerButtonCreator().setMiPalabra(it)}
                        controlador.obtenerButtonCreator().añadeHijos()
                        lbDesordenadas.text = controlador.obtenerPalabraActual()?.desordenar()
                        player1Timer.setTiempo(tiempo) //Reseteamos el tiempo a 60 segundos
                        btComprobar.isEnabled = true
                        player1Timer.togglePause()
                        println(controlador.obtenerPalabraActual())
                        txAviso.text = ""
                        txRespuesta.text.clear()
                    }, 3000)

                } else {
                    txAviso.text = "¡Felicidades! Has completado todas las palabras."
                    btComprobar.isEnabled = false  // Deshabilitar botón cuando no quedan palabras
                }

            } else {
                txAviso.text = "Sigue probando, ¡la próxima vez lo acertarás!" +
                        " Te quedan ${controlador.obtenerJugador().obtenerIntentos()} intentos"
                txRespuesta.setText("")
                controlador.habilitarTodosLosBotones()
                controlador.limpiarListasBotones()
                txIntentos.actualizar()
            }

            // Limpiar el campo de texto
            //txRespuesta.text.clear()
        }
    }

    //LISTENERS
    override fun onClick(view: View?) {
        when(view){
            is ImageButton ->{
                when(view?.id){
                    R.id.doublePoints -> {
                        if (controlador.obtenerJugador().obtenerPuntos() > 15){
                            if (!controlador.obtenerDoblePuntos()){
                                controlador.obtenerJugador().restarPuntos(15)
                                txPuntos.actualizar()
                                controlador.setDoblePuntos(true)
                            } else{
                                Toast.makeText(applicationContext, "Ya has comprado esta ayuda, podrás volver" +
                                        " a comprarla en la siguiente palabra",
                                    Toast.LENGTH_SHORT).show()
                            }
                        } else{
                            Toast.makeText(applicationContext, "No hay puntos suficientes para comprar la ayuda",
                                Toast.LENGTH_SHORT).show()
                        }
                    }

                    R.id.moreTime -> {
                        if (controlador.obtenerJugador().obtenerPuntos() > 50){
                            controlador.obtenerJugador().restarPuntos(50)
                            player1Timer.aumentarTiempo(30)
                            txPuntos.actualizar()
                        } else{
                            Toast.makeText(applicationContext, "No hay puntos suficientes para comprar la ayuda",
                                Toast.LENGTH_SHORT).show()
                        }
                    }

                    R.id.unlockLetter -> {
                        if (controlador.obtenerJugador().obtenerPuntos() > 25) {
                            controlador.obtenerJugador().restarPuntos(25)
                            val primerCaracter = controlador.obtenerPalabraActual()?.obtenerPrimerCaracter()

                            // Actualizar la vista con el primer carácter
                            txRespuesta.setText("")
                            txRespuesta.setText(primerCaracter)

                            // Buscar el botón que contiene el primer carácter y deshabilitarlo
                            val botonDeshabilitar = buttonCreator.obtenerListaBotones().find { it.text.toString() == primerCaracter }

                            // Deshabilitar el botón y agregarlo a la lista de deshabilitados
                            val botonesDeshabilitados = controlador.obtenerBotonesDes()

                            botonDeshabilitar?.isEnabled = false
                            primerCaracter?.let { texto ->
                                botonDeshabilitar?.let { botonesDeshabilitados.add(Pair(texto, it)) }
                            }

                            if (primerCaracter != null) {
                                controlador.añadirCaracteresText(primerCaracter)
                            }

                            txPuntos.actualizar()

                        } else {
                            Toast.makeText(
                                applicationContext,
                                "No hay puntos suficientes para comprar la ayuda",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }

            is Button -> {
                when(view?.id){
                    //AÑADIR EL BTCOMPROBAR SI EN NECESARIO
                }
            }
        }
    }

    //MENU
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menudificultad, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val sharedPreferences = getSharedPreferences("configuracion", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        when (item.itemId) {
            R.id.itemFacil -> {
                editor.putString("dificultad", "Facil")
                recargarDificultad("listado_pruebas.txt")
                Toast.makeText(this, "Dificultad: Facil", Toast.LENGTH_SHORT).show()
            }
            R.id.itemMedio -> {
                editor.putString("dificultad", "Medio")
                recargarDificultad("listado_medio.txt")
                Toast.makeText(this, "Dificultad: Medio", Toast.LENGTH_SHORT).show()
            }
            R.id.itemDificil -> {
                editor.putString("dificultad", "Dificil")
                recargarDificultad("listado_dificil.txt")
                Toast.makeText(this, "Dificultad: Dificil", Toast.LENGTH_SHORT).show()
            }
            R.id.itemInstrucciones ->{
                val instruccionesDialog = InstruccionesDialogFragment()
                instruccionesDialog.show(supportFragmentManager, "InstruccionesDialog")
            }
        }
        editor.apply() // Guarda los cambios
        return true
    }

    private fun seleccionarDiccionario(dificultad :String?): String {
        var rutaFichero = "listado_general.txt"
        when (dificultad) {
            "Facil" -> {
                // Configuración para fácil
                //rutaFichero = "listado_comun.txt"
                rutaFichero = "listado_pruebas.txt"
            }
            "Medio" -> {
                // Configuración para medio
                rutaFichero = "listado_medio.txt"
            }
            "Dificil" -> {
                // Configuración para difícil
                rutaFichero = "listado_dificil.txt"
            }
        }
        return rutaFichero
    }

    fun TextView.actualizar() {
        // Actualiza el texto con los puntos del jugador
        txPuntos.setText(getString(R.string.puntos) + " " + controlador.obtenerJugador().obtenerPuntos())
        txIntentos.setText(getString(R.string.intentos) + " " + controlador.obtenerJugador().obtenerIntentos())
        txNivel.setText(getString(R.string.nivel) + " " + controlador.obtenerJugador().obtenerNivel())

    }

    fun recargarDificultad(rutaFichero : String){
        controlador.obtenerDiccionario().cargarDatos(this, rutaFichero)
        controlador.iniciarJuego()

        //SACO LA PRIMERA PALABRA
        println(controlador.obtenerPalabraActual())

        controlador.obtenerButtonCreator().limpiarGrid()
        controlador.limpiarListasBotones()

        controlador.obtenerPalabraActual()?.let {controlador.obtenerButtonCreator().setMiPalabra(it)}
        controlador.obtenerButtonCreator().añadeHijos()
        lbDesordenadas.text = controlador.obtenerPalabraActual()?.desordenar()
        player1Timer.setTiempo(tiempo)
    }

}
