package com.dpm.palabraenigma.model

class Palabra(private var palabra: String) {
    constructor() : this("")

    private lateinit var definicion: String

    fun obtenerPalabra(): String = palabra
    fun obtenerDefinicion(): String = definicion

    fun setDefinicion(definicion: String){
        this.definicion = definicion
    }

    /**
     * Desordena los carácteres los mete en una lista, los mezcla y los introduce en la string
     */
     fun desordenar(): String {
        var palabraDesordenada = palabra.toList().shuffled().joinToString("")

        if (palabraDesordenada.equals(palabra)){
            desordenar()
        }

        return palabraDesordenada
    }

    fun obtenerPrimerCaracter() : String{
        return palabra[0].toString()
    }

    override fun toString(): String {
        return palabra
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true  // Si son la misma referencia, son iguales
        if (other !is Palabra) return false  // Si no es del tipo Palabra, no son iguales
        return palabra.contains(other.palabra, ignoreCase = true)  // Comparación insensible a mayúsculas/minúsculas
    }

    override fun hashCode(): Int {
        return palabra.lowercase().hashCode()  // Usamos el contenido en minúsculas para consistencia
    }
}