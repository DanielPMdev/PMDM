package com.dpm.palabraenigma.logic

import android.view.View
import android.widget.Button
import android.widget.EditText
import com.dpm.palabraenigma.model.Jugador
import com.dpm.palabraenigma.model.Palabra
import com.dpm.palabraenigma.data.Diccionario
import com.dpm.palabraenigma.utils.Temporizador

class Controlador(
    private val jugador: Jugador,  // Objeto Jugador para gestionar los puntos, intentos y nivel
    private val diccionario: Diccionario,  // Objeto Diccionario para obtener las palabras aleatorias
    private val buttonCreator: ButtonCreator, //Objeto para crear botones
    private val player1Timer : Temporizador //Objeto para controlar el tiempo
) {
    private var palabraActual: Palabra? = null  // Palabra actual a adivinar
    private var palabraAnterior: Palabra? = null
    private val listaAcertadas: MutableList<Palabra> =
        mutableListOf()  // Lista de palabras ya acertadas

    // Palabra que el usuario introduce como respuesta
    var palabraUsuario: String = ""
    private var rachaAciertos: Int = 0
    private var doblePuntos = false

    fun obtenerPalabraActual(): Palabra? = palabraActual
    fun obtenerPalabraAnterior(): Palabra? = palabraAnterior
    fun obtenerDiccionario(): Diccionario = diccionario
    fun obtenerJugador(): Jugador = jugador
    fun obtenerButtonCreator(): ButtonCreator = buttonCreator
    fun obtenerDoblePuntos(): Boolean = doblePuntos

    fun setDoblePuntos(boolean: Boolean) {
        doblePuntos = boolean
    }

    /**
     * Inicia el juego seleccionando la primera palabra de manera aleatoria.
     * Llama al método 'seleccionarNuevaPalabra()' para asignar una palabra al jugador.
     */
    fun iniciarJuego() {
        seleccionarNuevaPalabra()
    }

    /**
     * Verifica si la palabra ingresada por el usuario es correcta.
     * Si es correcta, se suman puntos al jugador, y se selecciona una nueva palabra.
     * Si es incorrecta, se resta un intento al jugador.
     *
     * @return true si la palabra es correcta, false si es incorrecta.
     */
    fun verificarPalabra(): Boolean {
        // Valida que la palabra actual no sea nula
        val palabraCorrecta = palabraActual ?: run {
            println("Error: palabraActual es nula")
            return false
        }

        val palabraUsuarioP = Palabra(palabraUsuario)

        // Verifica si la lista contiene la palabra introducida
        // y si la palabra seleccionada contiene los caracteres introducidos
        val listaPalabras = diccionario.obtenerListaPalabras()
        if (palabraUsuarioP in listaPalabras &&
            sonAnagramas(palabraCorrecta.obtenerPalabra(), palabraUsuarioP.obtenerPalabra()) &&
            palabraUsuarioP !in listaAcertadas
        ) {

            sumarPuntos(palabraUsuarioP.obtenerPalabra().length)
            doblePuntos = false
            rachaAciertos++
            reiniciarIntentos()


            // Añade la palabra actual a la lista de acertadas
            listaAcertadas.add(palabraUsuarioP)

            // Guarda la palabra actual en otra variable antes de seleccionarla de nuevo
            palabraAnterior = palabraCorrecta

            seleccionarNuevaPalabra()
            return true
        } else {
            // Resta un intento si la respuesta es incorrecta
            restarIntento()
            return false
        }
    }

    // Función auxiliar para verificar si dos palabras son anagramas
    private fun sonAnagramas(palabra1: String, palabra2: String): Boolean {
        return palabra1.lowercase().toList().sorted() == palabra2.lowercase().toList().sorted()
    }

    fun verificarIntentos(): Boolean {
        // Valida que la palabra actual no sea nula
        val palabraCorrecta = palabraActual ?: run {
            println("Error: palabraActual es nula")
            return false
        }

        if (jugador.obtenerIntentos() == 0) {
            // Añade la palabra actual a la lista de acertadas
            listaAcertadas.add(palabraCorrecta)

            // Guarda la palabra actual en otra variable antes de seleccionarla de nuevo
            palabraAnterior = palabraCorrecta

            rachaAciertos = 0
            doblePuntos = false

            seleccionarNuevaPalabra()

            reiniciarIntentos()

            return true
        }
        return false
    }

    fun verificarSinTiempo() : Boolean{
        // Valida que la palabra actual no sea nula
        val palabraCorrecta = palabraActual ?: run {
            println("Error: palabraActual es nula")
            return false
        }

        //PASARLE EL TIMER
        if (player1Timer.obtenerSinTiempo()) {
            // Añade la palabra actual a la lista de acertadas
            listaAcertadas.add(palabraCorrecta)

            // Guarda la palabra actual en otra variable antes de seleccionarla de nuevo
            palabraAnterior = palabraCorrecta

            rachaAciertos = 0
            doblePuntos = false

            seleccionarNuevaPalabra()

            reiniciarIntentos()

            return true
        }
        return false
    }

    /**
     * Selecciona una nueva palabra aleatoria que no haya sido acertada previamente.
     * Si no quedan palabras disponibles, muestra un mensaje de finalización.
     */
    private fun seleccionarNuevaPalabra() {
        do {
            palabraActual =
                diccionario.seleccionarPalabraAleatoria()  // Obtiene una palabra aleatoria del diccionario
        } while (palabraActual != null && listaAcertadas.contains(palabraActual))  // Verifica que la palabra no haya sido acertada antes

        if (palabraActual == null) {
            println("¡No quedan más palabras disponibles!")  // Mensaje si no quedan más palabras
        } else {
            println("Nueva palabra seleccionada. ¡A jugar!")  // Mensaje si se ha seleccionado una nueva palabra
        }
    }

    /**
     * Suma puntos al jugador por acertar una palabra.
     * Los puntos son fijos y no dependen de la dificultad de la palabra.
     */
    private fun sumarPuntos(longitudPalabra: Int) {
        var puntosGanados = 20  // Puntos fijos por palabra acertada

        puntosGanados += (longitudPalabra * 2)

        if (rachaAciertos in 3..7) {
            puntosGanados += 5
        } else if (rachaAciertos > 7) {
            puntosGanados += 15
        }

        if (doblePuntos) {
            puntosGanados *= 2
        }

        jugador.incrementarPuntos(puntosGanados)  // Suma los puntos al jugador

        println("Puntos actuales: ${jugador.obtenerPuntos()}")  // Muestra los puntos actuales del jugador
        println("Nivel actual: ${jugador.obtenerNivel()}")  // Muestra el nivel actual del jugador
    }

    /**
     * Resta un intento al jugador cuando la respuesta es incorrecta.
     * Si el jugador se queda sin intentos, muestra un mensaje de fin del juego.
     */
    private fun restarIntento() {
        jugador.decrementarIntentos()  // Resta un intento al jugador
        if (jugador.obtenerIntentos() <= 0) {
            println("El jugador ${jugador} se quedó sin intentos. Fin del juego.")  // Mensaje si se acaban los intentos
        } else {
            println("Intentos restantes: ${jugador.obtenerIntentos()}")  // Muestra los intentos restantes
        }
    }

    fun reiniciarIntentos() {
        jugador.setIntentos(3)
    }

    fun recargarDificultad(){

    }

    /*
    fun verificarBotones(inputTextView: EditText) {
        buttonCreator.onButtonClicked = { texto, view ->
            if (texto == null) {
                // Si el texto es null, elimina el último carácter
                val currentText = inputTextView.text.toString()
                if (currentText.isNotEmpty()) {
                    inputTextView.setText(currentText.dropLast(1))
                }
            } else {
                // Si el texto no es null, añádelo al InputTextView
                inputTextView.append(texto)
            }
        }
    }
     */

    // Lista para almacenar los botones deshabilitados correspondientes a los caracteres
    val botonesDeshabilitados = mutableListOf<Pair<CharSequence, Button>>()

    // Lista para almacenar los caracteres añadidos al input (no necesariamente borrados)
    val caracteresEnElTexto = mutableListOf<CharSequence>()

    fun obtenerBotonesDes(): MutableList<Pair<CharSequence, Button>> = botonesDeshabilitados
    fun obtenerCaracteresText(): MutableList<CharSequence> = caracteresEnElTexto

    fun añadirCaracteresText(charSequence: CharSequence){
        caracteresEnElTexto.add(0, charSequence)
    }

    fun habilitarTodosLosBotones() {
        val botonesDeshabilitados = obtenerBotonesDes()
        for ((_, boton) in botonesDeshabilitados) {
            boton.isEnabled = true
        }
    }

    fun limpiarListasBotones(){
        botonesDeshabilitados.clear()
        caracteresEnElTexto.clear()
    }

    fun verificarBotones(inputTextView: EditText) {
        buttonCreator.onButtonClicked = { caracter, view ->
            if (caracter == null) { //Boton de borrar
                val currentText = inputTextView.text.toString()
                if (currentText.isNotEmpty()) {
                    val nuevoTexto = currentText.dropLast(1)
                    inputTextView.setText(nuevoTexto)

                    // Recuperar el último carácter borrado y desbloquear el botón correspondiente
                    if (caracteresEnElTexto.isNotEmpty()) {
                        val ultimoCaracterBorrado = caracteresEnElTexto.
                        removeAt(caracteresEnElTexto.size - 1)
                        // Eliminar y obtener el último carácter borrado

                        // Buscar el botón cuyo texto coincida con el último carácter borrado
                        val botonRecuperado = botonesDeshabilitados.find{
                            it.first == ultimoCaracterBorrado }?.second
                        botonRecuperado?.isEnabled = true // Habilitar el botón que tiene el texto borrado
                        // Eliminarlo de la lista de botones deshabilitados
                        botonesDeshabilitados.removeFirst { it.first == ultimoCaracterBorrado }
                    }
                }
            } else {
                // Si el texto no es null, añádelo al InputTextView
                inputTextView.append(caracter)

                // Registrar el carácter en el input y deshabilitar el botón correspondiente
                caracteresEnElTexto.add(caracter)  // Guardar el carácter que se añadió
                val boton = view as? Button  // Asegurarse de que view es un Button
                boton?.isEnabled = false  // Deshabilitar el botón después de usarlo
                boton?.let { botonesDeshabilitados.add(Pair(caracter, it)) }  // Guardar el texto y el botón deshabilitado
            }
        }
    }

    //El mutable list no reconococia el it
    private inline fun <T> MutableList<T>.removeFirst(predicate: (T) -> Boolean): Boolean {
        val index = indexOfFirst(predicate) // Encuentra el índice del primer elemento que cumple la condición
        return if (index != -1) {
            removeAt(index) // Elimina el elemento en el índice encontrado
            true
        } else {
            false // Devuelve false si no se encontró ningún elemento
        }
    }

}


    /*
    fun verificarBotones(inputTextView: EditText) {
        buttonCreator.onButtonClicked = { texto, view ->
            if (texto == null) {
                // Si el texto es null, elimina el último carácter
                val currentText = inputTextView.text.toString()
                if (currentText.isNotEmpty()) {
                    inputTextView.setText(currentText.dropLast(1))
                }
            } else {
                // Si el texto no es null, añádelo al InputTextView
                inputTextView.append(texto)
                view.isEnabled = false
            }
        }
    }

    // VERSIÓN QUE BORRA EL TEXTO Y HABILITA TODOS LOS BOTONES

    // VERSIÓN QUE VA HABILITANDO LOS CARACTERES QUE RECOGE

    // Lista interna para almacenar botones deshabilitados
    val botonesDeshabilitados = mutableListOf<Button>()

    fun verificarBotones(inputTextView: EditText) {
        buttonCreator.onButtonClicked = { texto, view ->
            if (texto == null) {
                // Si el texto es null, elimina el último carácter
                val currentText = inputTextView.text.toString()
                if (currentText.isNotEmpty()) {
                    inputTextView.setText(currentText.dropLast(1))
                }

                inputTextView.setText("")
                habilitarBotones(botonesDeshabilitados)  // Habilitar todos los botones
                botonesDeshabilitados.clear()  // Limpiar la lista de botones deshabilitados

            } else {
                // Si el texto no es null, añádelo al InputTextView
                inputTextView.append(texto)
                view.isEnabled = false

                // Añadir el botón deshabilitado a la lista interna
                if (view is Button) {
                    botonesDeshabilitados.add(view)
                }
            }
        }
    }

    // Función para habilitar todos los botones
    fun habilitarBotones(botones: List<Button>) {
        for (boton in botones) {
            boton.isEnabled = true
        }
    }
     */
