package com.dpm.palabraenigma.logic

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import androidx.core.content.ContextCompat
import androidx.gridlayout.widget.GridLayout
import com.dpm.palabraenigma.R
import com.dpm.palabraenigma.model.Palabra

class ButtonCreator(private val context: Context, private val grid: GridLayout) {
    private var miGrid: GridLayout = grid
    private lateinit var miPalabra: String
    private lateinit var miPalabraDesordenada: String
    var onButtonClicked: ((CharSequence?, View) -> Unit)? = null // Callback

    private var miListaBotones: MutableList<Button> = mutableListOf()

    fun obtenerListaBotones(): MutableList<Button> = miListaBotones

    private var currentIndex = 0 // Índice para el carácter que se debe pulsar actualmente

    fun setMiPalabra(palabra: Palabra) {
        this.miPalabra = palabra.toString()
        this.miPalabraDesordenada = palabra.desordenar()
    }

    fun recorrer() {
        //val miGrid = findViewById<GridLayout>(R.id.grid01)

        for (i in 0 until miGrid.childCount) {
            val v = miGrid.getChildAt(i)
            if (v is Button) {
                v.setBackgroundColor(Color.rgb(i * 10, i * 50, i * 30))
            }
        }
    }

    fun añadeHijos() {
        //val miGrid = findViewById<GridLayout>(R.id.grid01)

        miGrid.viewTreeObserver.addOnGlobalLayoutListener {
            val anchoBoton =
                miGrid.width / 5 //Ajusta el divisor según el número de columnas deseadas

            for (i in 0 until miGrid.childCount) {
                val vista = miGrid.getChildAt(i)
                if (vista is Button) {
                    vista.layoutParams = GridLayout.LayoutParams().apply {
                        width = anchoBoton
                        height = ViewGroup.LayoutParams.WRAP_CONTENT
                    }
                }
            }
        }

        //Crea y añade botones al GridLayout
        for (i in miPalabraDesordenada.indices) {
            val boton = Button(context).apply {
                id = View.generateViewId()
                text = miPalabraDesordenada[i].toString() //AÑADIR AQUI LOS CARACTERES
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )

                // Establece el fondo usando el selector de estilo
                background = ContextCompat.getDrawable(context, R.drawable.button_styles)

                //setOnClickListener {onClick(it)}
                setOnClickListener { view ->
                    val textoPulsado = onClick(view)
                    if (view != null) {
                        onButtonClicked?.invoke(
                            textoPulsado,
                            view
                        ) // Pasamos texto y botón al callback
                    }
                }
            }

            miGrid.addView(boton, i)


            miListaBotones.add(boton)
        }

        // Agrega un ImageButton al final del GridLayout
        miGrid.post { // Asegura que el tamaño del GridLayout ya está definido
            val imageButton = ImageButton(context).apply {
                id = View.generateViewId()
                setImageResource(R.drawable.backspace_32) // Cambia este recurso a tu imagen
                layoutParams = GridLayout.LayoutParams().apply {
                    width = miGrid.width / 5
                    height = ViewGroup.LayoutParams.WRAP_CONTENT
                }

                // Establece el fondo usando el selector de estilo
                background = ContextCompat.getDrawable(context, R.drawable.button_styles)

                //setOnClickListener {onClick(it)}
                setOnClickListener { view ->
                    val textoPulsado = onClick(view)
                    if (view != null) {
                        onButtonClicked?.invoke(
                            textoPulsado,
                            view
                        ) // Pasamos texto y botón al callback
                    }
                }
            }
            miGrid.addView(imageButton) // Añade el ImageButton al GridLayout
        }
    }

    fun limpiarGrid() {
        miGrid.removeAllViews()
        miListaBotones.clear()
    }

    private fun onClick(it: View?): CharSequence? {
        return when (it) {
            is Button -> it.text
            is ImageButton -> null
            else -> null
        }
    }
}




//  private fun resetGame() {
//        currentIndex = 0
//        // Reinicia los colores y los estados de los botones
//        for ((index, boton) in botones.withIndex()) {
//            boton.background = botonesBackground[index] // Cambia el color al original (puedes personalizarlo)
//        }
//        // Configura una nueva palabra si es necesario
//    }
//
//    private fun onClick(it: View?): Boolean {
//        if (it is Button) {
//            // Obtén el carácter que debería pulsarse actualmente
//            val currentChar = miPalabra[currentIndex].toString()
//
//            if (it.text == currentChar) {
//                // El botón correcto fue pulsado
//                it.setBackgroundColor(Color.GREEN) // Cambia el color del botón a verde
//                currentIndex++ // Avanza al siguiente carácter
//
//                // Si se alcanzó el final de la palabra
//                if (currentIndex == miPalabra.length) {
//                    // Reinicia el juego o muestra un mensaje de victoria
//                    println("HAS GANADO")
//                    return true
//                }
//            } else {
//                // El botón incorrecto fue pulsado
//                it.setBackgroundColor(Color.RED) // Cambia el color del botón a rojo
//                // Introduce un retraso de un segundo antes de reiniciar
//                Handler(Looper.getMainLooper()).postDelayed({
//                    resetGame()
//                }, 500) // 1000 milisegundos = 1 segundo
//                return false
//            }
//        }
//        return false
//    }