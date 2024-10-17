package com.dpm.chessclock

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment

class ReloadDialogFragment : DialogFragment(){

    private var listener: OnDialogResultListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        //Asegurate de que el contexto sea una instancia de OnDialogResultListener
        listener = context as? OnDialogResultListener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("¿Quieres resetar los temporizadores?")
        builder.setPositiveButton("SI", object : DialogInterface.OnClickListener {
            override fun onClick(p0: DialogInterface?, p1: Int) {
                listener?.onResult(true)
                dismiss()
            }
        })
        builder.setNegativeButton("NO", object : DialogInterface.OnClickListener {
            override fun onClick(p0: DialogInterface?, p1: Int) {
                listener?.onResult(false)
                dismiss()
            }
        })

        return builder.create()
    }
}