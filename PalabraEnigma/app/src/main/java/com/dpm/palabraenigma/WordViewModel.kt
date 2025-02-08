package com.dpm.palabraenigma

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.jsoup.Jsoup

class WordViewModel: ViewModel() {

    fun checkWordInWiktionary(word: String) = liveData(Dispatchers.IO) {
        val client = OkHttpClient()
        val url = "https://es.wiktionary.org/w/api.php"
        val urlWithParameters = "$url?action=parse&page=$word&prop=text&format=json"

        val request = Request.Builder()
            .url(urlWithParameters)
            .build()

        try {
            val response = client.newCall(request).execute()
            response.use { // Se asegura de cerrar el recurso automáticamente
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val jsonObject = JSONObject(responseBody ?: "")
                    val parse = jsonObject.optJSONObject("parse")
                    val text = parse?.optJSONObject("text")
                    val htmlContent = text?.optString("*")

                    if (!htmlContent.isNullOrEmpty()) {
                        val doc = Jsoup.parse(htmlContent)
                        val definitions = doc.select("dl dd") // Selecciona las definiciones
                        val result = if (definitions.isNotEmpty()) {
                            definitions.first()?.text() ?: "Definición no disponible."
                        } else {
                            "No se encontró una definición específica."
                        }
                        emit(result)
                    } else {
                        emit("No se encontró contenido HTML en la respuesta.")
                    }
                } else {
                    emit("Error al realizar la solicitud: ${response.code}")
                }
            }
        } catch (e: Exception) {
            emit("Error al conectar: ${e.message ?: "Excepción desconocida"}")
        }
    }
}