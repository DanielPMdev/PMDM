package com.dpm.palabraenigma.data

import android.content.Context
import com.dpm.palabraenigma.model.Palabra
import java.io.File

class Diccionario () {

    private lateinit var listaPalabras: List<Palabra>
    //private lateinit var definicion: String

    fun obtenerListaPalabras(): List<Palabra> = listaPalabras

    fun cargarDatos(context: Context, nombreArchivo: String) {
        if (!::listaPalabras.isInitialized || listaPalabras.isEmpty()) { //:: Este operador permite acceder a los metadatos
            listaPalabras = emptyList()
        }

        // Leer el archivo desde assets y cargar las palabras
        val palabrasNuevas = context.assets.open(nombreArchivo).bufferedReader().use {
            it.readLines().map { linea -> Palabra(linea) }
        }
        listaPalabras = palabrasNuevas
    }

    // Seleccionar una palabra aleatoria
    fun seleccionarPalabraAleatoria(): Palabra? {
        return if (listaPalabras.isNotEmpty()) {
            listaPalabras.random()
        } else {
            println("No hay palabras disponibles en el diccionario.")
            null
        }
    }

    fun eliminarPalabra(palabra: Palabra){

    }

    /**
     * @param rutaFichero Ruta del Fichero que se utilizara para importar el diccionario
     * @return Devuelve la lista de Palabras que obtenemos del fichero
     */
    private fun leerFichero(rutaFichero: String): List<Palabra> {
        val listaPalabras = mutableListOf<Palabra>()
        try {
            val archivo = File(rutaFichero)
            archivo.forEachLine { linea ->
                if (linea.isNotBlank()) {
                    listaPalabras.add(Palabra(linea.trim()))
                }
            }
        } catch (e: Exception) {
            println("Error al leer el fichero: ${e.message}")
        }
        return listaPalabras
    }

}