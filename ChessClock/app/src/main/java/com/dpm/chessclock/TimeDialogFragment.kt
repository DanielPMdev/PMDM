package com.dpm.chessclock

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment

/**
 * Un fragmento de diálogo personalizado que permite al usuario seleccionar una cantidad de tiempo predefinida.
 * Esta clase muestra una lista de opciones (en segundos o minutos) y devuelve el tiempo seleccionado
 * a un listener implementado en el contexto que crea el diálogo.
 */
class TimeDialogFragment : DialogFragment() {

    // Listener para devolver el resultado al contexto que lo implementa
    private var listener: OnDialogResultListener? = null

    /**
     * Método llamado cuando el fragmento se adjunta al contexto.
     * Asegura que el contexto que usa este fragmento implemente la interfaz OnDialogResultListener.
     *
     * @param context El contexto que adjunta este fragmento (generalmente, una actividad).
     */
    override fun onAttach(context: Context) {
        super.onAttach(context)
        //Asegurate de que el contexto sea una instancia de OnDialogResultListener
        listener = context as? OnDialogResultListener
    }

    /**
     * Crea el diálogo cuando se inicializa el fragmento.
     * Presenta una lista de opciones de tiempo para que el usuario seleccione, y devuelve la opción
     * seleccionada al listener implementado por el contexto.
     *
     * @param savedInstanceState Estado del fragmento previamente guardado, si existe.
     * @return Un diálogo configurado con un título, opciones y botones de aceptar/cancelar.
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val opciones = arrayOf("20 segundos", "1 minuto", "5 minutos", "10 minutos")
        var elegida = 0 // Opción predeterminada (índice de la opción seleccionada)

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Selecciona el Tiempo del Reloj")
            .setSingleChoiceItems(opciones, elegida) {  _, which ->
                elegida = which // Actualizar la opción seleccionada
        }
        builder.setPositiveButton("Aceptar", object : DialogInterface.OnClickListener{
            override fun onClick(p0: DialogInterface?, p1: Int) {
                val tiempoEnSegundos = when (elegida) {
                    0 -> 20 // 20 segundos
                    1 -> 60 // 1 minuto
                    2 -> 300 // 5 minutos
                    3 -> 600 // 10 minutos
                    else -> {20}
                }
                listener?.onResult(tiempoEnSegundos) //Devuelve el tiempo al Listener
            }
        })
        builder.setNegativeButton("CANCELAR", object : DialogInterface.OnClickListener{
            override fun onClick(p0: DialogInterface?, p1: Int) {
                //listener?.onResult() //Devuelve el tiempo al Listener
                dismiss()
            }
        })

        return builder.create()
    }
}