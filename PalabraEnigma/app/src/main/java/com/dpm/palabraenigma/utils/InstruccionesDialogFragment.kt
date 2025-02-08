package com.dpm.palabraenigma.utils

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment

class InstruccionesDialogFragment : DialogFragment() {

    private var listener: OnDialogResultListener? = null

    // Interfaz para comunicar el resultado al Activity o Fragment
    interface OnDialogResultListener {
        fun onDialogResult(result: Boolean)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Asegúrate de que el contexto implemente la interfaz
        listener = context as? OnDialogResultListener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Instrucciones")

        // Concatenar todas las secciones del mensaje
        val mensaje = """
        Clicka los botones para añadir las letras y formar la palabra.
    
        Si tienes puntos suficientes, podrás comprar pistas o ayudas.

        1. Estrella (15 puntos): 
        Duplica los puntos de la siguiente palabra.
        
        2. Reloj (50 puntos): 
        Añade 30 segundos al contador.
        
        3. Candado (25 puntos): 
        Desbloquea la primera letra.
        """.trimIndent()

        // Establecer el mensaje concatenado
        builder.setMessage(mensaje)



        builder.setPositiveButton("Cerrar") { _, _ ->
            listener?.onDialogResult(true) // Si el usuario cierra el diálogo
            dismiss()
        }

        return builder.create()
    }
}
