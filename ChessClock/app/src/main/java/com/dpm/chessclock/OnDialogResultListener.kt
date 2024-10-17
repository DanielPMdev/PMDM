package com.dpm.chessclock

interface OnDialogResultListener {
    fun onResult(result: Int) //TIEMPO EN SEGUNDOS
    fun onResult(result: Boolean) //Si o No del RELOAD
}