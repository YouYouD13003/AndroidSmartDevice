package fr.isen.abbes.androidsmartdevicefr.isen.abbes.myapplicationble

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

class ScanActivity : ComponentActivity() {

    private val bluetoothLeScanner: BluetoothLeScanner? by lazy {
        (getSystemService(BLUETOOTH_SERVICE) as BluetoothManager).adapter?.bluetoothLeScanner
    }

    private val devices = mutableStateListOf<ScanResult>()
    private val permissions = mutableListOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            add(Manifest.permission.BLUETOOTH_SCAN)
            add(Manifest.permission.BLUETOOTH_CONNECT)
        }
    }

    private val requestPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissionsResult ->
            val allGranted = permissionsResult.all { it.value }
            if (allGranted) {
                startScan()
            } else {
                Toast.makeText(this, "Permissions nécessaires refusées.", Toast.LENGTH_SHORT).show()
            }
        }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            result.device?.let { device ->
                // Get the device name
                val deviceName = result.scanRecord?.deviceName ?: device.name
                if (!deviceName.isNullOrEmpty() && deviceName != "Inconnu") {
                    // Add only devices with a valid name
                    if (!devices.any { it.device.address == device.address }) {
                        devices.add(result)
                    }
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e("ScanActivity", "Scan échoué : code $errorCode")
            Toast.makeText(this@ScanActivity, "Échec du scan BLE", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ScanScreen()
        }
    }

    @Composable
    fun ScanScreen() {
        var isScanning by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "Recherche BLE")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                if (isScanning) {
                    stopScan()
                } else {
                    checkPermissionsAndStartScan()
                }
                isScanning = !isScanning
            }) {
                Text(text = if (isScanning) "Arrêter le scan" else "Lancer le scan")
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn {
                items(devices) { device ->
                    val deviceName = device.scanRecord?.deviceName ?: device.device.name ?: "Inconnu"
                    val deviceAddress = device.device.address

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                navigateToDeviceDetails(device.device)
                            },
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "Nom : $deviceName")
                                Text(text = "Adresse : $deviceAddress")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun navigateToDeviceDetails(device: BluetoothDevice) {
        val intent = Intent(this, DeviceDetailsActivity::class.java).apply {
            putExtra("BLUETOOTH_DEVICE", device)
        }
        startActivity(intent)
    }

    private fun checkPermissionsAndStartScan() {
        val missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            requestPermissionsLauncher.launch(missingPermissions.toTypedArray())
        } else {
            startScan()
        }
    }

    private fun startScan() {
        try {
            val bluetoothAdapter = (getSystemService(BLUETOOTH_SERVICE) as BluetoothManager).adapter
            if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
                Toast.makeText(this, "Bluetooth non activé ou non disponible.", Toast.LENGTH_LONG).show()
                return
            }

            devices.clear() // Clear previous scan results
            bluetoothLeScanner?.startScan(scanCallback)
            Toast.makeText(this, "Scan démarré", Toast.LENGTH_SHORT).show()
        } catch (e: SecurityException) {
            Log.e("ScanActivity", "Permissions manquantes pour le scan : ${e.message}")
        }
    }

    private fun stopScan() {
        try {
            bluetoothLeScanner?.stopScan(scanCallback)
            Toast.makeText(this, "Scan arrêté", Toast.LENGTH_SHORT).show()
        } catch (e: SecurityException) {
            Log.e("ScanActivity", "Permissions manquantes pour arrêter le scan : ${e.message}")
        }
    }
}
