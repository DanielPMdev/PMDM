package com.dpm.chessclock

/**
 * El objeto Ayudante proporciona utilidades relacionadas con la gestión y formateo de tiempo.
 *
 * Este objeto es un singleton, lo que significa que solo se crea una única instancia del mismo
 * durante la ejecución del programa. Dado que no necesita almacenar estado, se utiliza para
 * ofrecer funciones que no dependen de instancias específicas.
 */
object Ayudante {

    /**
     * Formatea una cantidad de tiempo en segundos a un formato de cadena "MM:SS".
     *
     * @param segundos La cantidad de tiempo en segundos que se desea formatear.
     * @return Una cadena en formato "MM:SS", donde los minutos y segundos se
     *         representan siempre con dos dígitos. Ejemplo: 75 segundos se formatearán como "01:15".
     */
    public fun formatearTiempo(segundos: Int): String{
        val minutes = segundos / 60
        val segundosRestantes = segundos % 60
        return  String.format("%02d:%02d", minutes, segundosRestantes)
    }

}