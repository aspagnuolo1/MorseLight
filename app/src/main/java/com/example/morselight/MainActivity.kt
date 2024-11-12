package com.example.morselight
import android.Manifest
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private lateinit var cameraManager: CameraManager
    private var cameraId: String? = null
    private var useTorch = false
    private var msgTV: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        try{
            cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
            cameraId = cameraManager.cameraIdList[0]
        } catch (e : CameraAccessException){
            e.printStackTrace()
        }


        val inputText = findViewById<EditText>(R.id.inputText)
        val morseCodeText = findViewById<TextView>(R.id.morseCodeText)
        val convertButton = findViewById<Button>(R.id.convertButton)
        val torchButton = findViewById<Button>(R.id.torchButton)

        convertButton.setOnClickListener {
            val morseCode = convertToMorseCode(inputText.text.toString())
            morseCodeText.text = morseCode
        }

        torchButton.setOnClickListener {
            checkPermissions()
        }

        // Find the TextView with ID idTVMsg
        msgTV = findViewById(R.id.idTVMsg)
        msgTV?.text = "Morse Torch is not active"

        //MobileAds.initialize(this);
        
    }

    private fun convertToMorseCode(text: String): String {
        val morseCodeDictionary = mapOf(
                'a' to ".-", 'b' to "-...", 'c' to "-.-.", 'd' to "-..",
                'e' to ".", 'f' to "..-.", 'g' to "--.", 'h' to "....",
                'i' to "..", 'j' to ".---", 'k' to "-.-", 'l' to ".-..",
                'm' to "--", 'n' to "-.", 'o' to "---", 'p' to ".--.",
                'q' to "--.-", 'r' to ".-.", 's' to "...", 't' to "-",
                'u' to "..-", 'v' to "...-", 'w' to ".--", 'x' to "-..-",
                'y' to "-.--", 'z' to "--.."
        )
        flashMorseCode(text.lowercase().map { morseCodeDictionary[it] ?: "" }.joinToString(" "))
        return text.lowercase().map { morseCodeDictionary[it] ?: "" }.joinToString(" ")
    }

    private fun flashMorseCode(morseCode: String) {
        if(useTorch){
            val handler = Handler(Looper.getMainLooper())
            var delay = 0L

            for (symbol in morseCode) {
                when (symbol) {
                    '.' -> {
                        handler.postDelayed({ toggleTorch(true) }, delay)
                        delay += 200 // Durata del punto
                        handler.postDelayed({ toggleTorch(false) }, delay)
                    }
                    '-' -> {
                        handler.postDelayed({ toggleTorch(true) }, delay)
                        delay += 600 // Durata del trattino
                        handler.postDelayed({ toggleTorch(false) }, delay)
                    }
                    ' ' -> {
                        delay += 600 // Spazio tra le lettere
                    }
                }
                delay += 200 // Spazio tra i simboli
            }
        } else{
            return
        }
    }

    private fun checkPermissions() {
        useTorch = ! useTorch
        if(useTorch)
            msgTV?.text = "Morse Torch is active"
        else
            msgTV?.text = "Morse Torch is not active"
        if(useTorch)
            Toast.makeText(this, "MorseLight abilitata", Toast.LENGTH_SHORT).show()
        else
            Toast.makeText(this, "MorseLight disabilitata", Toast.LENGTH_SHORT).show()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {

            // Se il permesso non è stato concesso, richiedilo all'utente
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_REQUEST_CODE)
        } else {
            // Il permesso è già stato concesso
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                useTorch = if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Il permesso è stato concesso
                    true
                } else {
                    false
                    // Il permesso è stato negato. Disabilita la funzionalità che dipende da questo permesso.
                }
                return
            }
            // Controlla altri permessi se necessario
        }
    }

    companion object {
        private const val CAMERA_REQUEST_CODE = 1
    }

    private fun toggleTorch(on: Boolean) {
        useTorch = !useTorch
        if(useTorch)
            msgTV?.text = "Morse Torch is active"
        else
            msgTV?.text = "Morse Torch is not active"
        try {
            cameraId?.let {
                cameraManager.setTorchMode(it, on)
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }
}
