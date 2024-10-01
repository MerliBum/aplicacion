package com.example.aplicacion.ui

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.aplicacion.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MenuActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)


        auth = FirebaseAuth.getInstance()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        if (!checkLocationPermission()) {
            requestLocationPermission()
        } else {
            obtenerUbicacionYGuardar()
        }


        val totalCompraInput = findViewById<EditText>(R.id.et_monto_compra)
        val calcularButton = findViewById<Button>(R.id.btn_calcular_despacho)
        val resultadoTextView = findViewById<TextView>(R.id.tv_resultado_despacho)

        calcularButton.setOnClickListener {
            val totalCompra = totalCompraInput.text.toString().toDoubleOrNull()

            if (totalCompra != null) {
                val distancia = 15.0 // Aquí debes obtener la distancia real. Se ha puesto 15 como valor de ejemplo.
                val costoDespacho = calcularCostoDespacho(totalCompra, distancia)
                resultadoTextView.text = "Costo del despacho: $$costoDespacho"
            } else {
                Toast.makeText(this, "Por favor, ingrese un monto válido", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun checkLocationPermission(): Boolean {
        val fineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarseLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)

        return fineLocationPermission == PackageManager.PERMISSION_GRANTED && coarseLocationPermission == PackageManager.PERMISSION_GRANTED
    }


    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 1)
    }

    // Función que se llama después de conceder o rechazar los permisos
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                obtenerUbicacionYGuardar() // Si los permisos son concedidos, obtener la ubicación
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun obtenerUbicacionYGuardar() {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    if (location != null) {

                        val database = FirebaseDatabase.getInstance()
                        val userLocationRef = database.getReference("users/${auth.currentUser?.uid}/location")

                        val locationData = mapOf(
                            "latitude" to location.latitude,
                            "longitude" to location.longitude
                        )


                        CoroutineScope(Dispatchers.IO).launch {
                            userLocationRef.setValue(locationData).addOnCompleteListener { task ->
                                // Volver al hilo principal para mostrar el Toast
                                CoroutineScope(Dispatchers.Main).launch {
                                    if (task.isSuccessful) {
                                        Toast.makeText(this@MenuActivity, "Ubicación guardada correctamente", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(this@MenuActivity, "Error al guardar la ubicación", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    } else {
                        Toast.makeText(this, "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } catch (e: SecurityException) {
            Toast.makeText(this, "Error de seguridad al obtener la ubicación", Toast.LENGTH_SHORT).show()
        }
    }


    private fun calcularCostoDespacho(totalCompra: Double, distanciaKm: Double): Double {
        return when {
            totalCompra >= 50000 -> {

                if (distanciaKm <= 20) 0.0 else distanciaKm * 300
            }
            totalCompra in 25000.0..49999.0 -> {
                // Cobro de $150 por kilómetro
                distanciaKm * 150
            }
            else -> {
                // Cobro de $300 por kilómetro
                distanciaKm * 300
            }
        }
    }
}
