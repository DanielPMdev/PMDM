package com.dpm.somosamigos

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // VARIABLES DE LOS WIDGETS
        val bt_01: Button = findViewById(R.id.btAmigo)
        val bt_02: Button = findViewById(R.id.btLimpiar)
        val txInput01: TextInputEditText = findViewById(R.id.textInput01)
        val txInput02: TextInputEditText = findViewById(R.id.textInput02)
        val txView01: TextView = findViewById(R.id.textAmigo)
        val imgView01: ImageView = findViewById (R.id.imageView)

        bt_01.setOnClickListener {
            val input1 = txInput01.text.toString().trim() //Uso trim para evitar posibles errores
            val input2 = txInput02.text.toString().trim() //Aunque teniendo los digits establecidos no deberían poder

            //Comprobamos que no esta vacio para que no se "ROMPA" la app
            if (input1.isEmpty()) {
                txInput01.error = "Este campo no puede estar vacío"
            } else if (input2.isEmpty()) {
                txInput02.error = "Este campo no puede estar vacío"
            } else {
                // Solo convierto a entero cuando los campos no están vacíos
                val num1 = input1.toInt()
                val num2 = input2.toInt()

                if (num1 == num2){
                    val errorIguales = "Los numeros {$num1, $num2} son iguales"
                    txView01.text = errorIguales

                } else{
                    val resultado = somosNumerosAmigos(num1, num2)
                    var textoMostrar = "Los numeros {$num1, $num2} "

                    if (resultado) {
                        //Mostramos una "alerta", cambiamos el texto y ponemos la imagen
                        Toast.makeText(this, "Son números amigos", Toast.LENGTH_SHORT).show()
                        textoMostrar += getString(R.string.amigosTrue)
                        txView01.text = textoMostrar
                        imgView01.setImageResource(R.drawable.numerosamigos)


                    } else {
                        Toast.makeText(this, "No son números amigos", Toast.LENGTH_SHORT).show()
                        textoMostrar += getString(R.string.amigosFalse)
                        txView01.text = textoMostrar
                        imgView01.setImageResource(R.drawable.numerospelea)
                    }
                }
            }
        }

        bt_02.setOnClickListener {
            //Limpiamos los dos inputs, el texto y la imagen
            txInput01.setText("")
            txInput02.setText("")
            txView01.text = ""
            imgView01.setImageResource(android.R.color.transparent);
        }
    }

    /**
     * Determina si dos números son números amigos.
     * Los números amigos son dos números donde la suma de los divisores de uno
     * es igual al otro numero y viceversa.
     *
     * @param num1 El primer número.
     * @param num2 El segundo número.
     * @return `true` si los números son amigos, `false` en caso contrario.
     */
    private fun somosNumerosAmigos(num1: Int, num2: Int): Boolean{
        var suma1 = 0
        var suma2 = 0

        for (i in 1 until  num1){ //Bucle desde 1 hasta el numero NO INCLUIDO
            if (num1 % i == 0){ //Comprobamos si el resto es 0 (Divisible)
                suma1 += i
            }
        }
        for (i in 1 until num2){
            if (num2 % i == 0){
                suma2 += i
            }
        }

        return suma1 == num2 && suma2 == num1
    }
}