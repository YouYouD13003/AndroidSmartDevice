package fr.isen.abbes.androidsmartdevicefr.isen.abbes.myapplicationble

import android.annotation.SuppressLint
import android.bluetooth.*
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@SuppressLint("MissingPermission")
class DeviceDetailsActivity : ComponentActivity() {

    private var bluetoothGatt: BluetoothGatt? = null
    private var connectionStatus by mutableStateOf("Non connecté")
    private var services: List<BluetoothGattService>? = null

    private var button1ClickCount by mutableStateOf(0)
    private var button3ClickCount by mutableStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val device: BluetoothDevice? = intent.getParcelableExtra("BLUETOOTH_DEVICE")
        if (device != null) {
            connectToDevice(device)
        } else {
            Toast.makeText(this, "Aucun appareil trouvé", Toast.LENGTH_SHORT).show()
        }

        setContent {
            DeviceDetailsScreen(
                connectionStatus = connectionStatus,
                button1ClickCount = button1ClickCount,
                button3ClickCount = button3ClickCount,
                onLedAction = { ledValue -> writeToLEDCharacteristic(ledValue) },
                onSubscribe = { subscribeToButtonNotifications() },
                onDisconnect = { disconnectFromDevice() },
                onExit = { finish() }
            )
        }
    }

    private fun connectToDevice(device: BluetoothDevice) {
        connectionStatus = "Connexion en cours..."
        bluetoothGatt = device.connectGatt(this, false, gattCallback)
    }

    private fun disconnectFromDevice() {
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
        connectionStatus = "Déconnecté"
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    connectionStatus = "Connecté à ${gatt?.device?.name}"
                    Log.i("DeviceDetailsActivity", "Connecté à ${gatt?.device?.name}")
                    bluetoothGatt?.discoverServices()
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    connectionStatus = "Déconnecté"
                    Log.i("DeviceDetailsActivity", "Déconnecté")
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                services = gatt?.services
                Log.i("DeviceDetailsActivity", "Services découverts : ${services?.size ?: 0}")
            } else {
                Log.e("DeviceDetailsActivity", "Erreur lors de la découverte des services")
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            characteristic?.value?.let { value ->
                Log.i("DeviceDetailsActivity", "Notification reçue : ${value.joinToString()}")
                runOnUiThread {
                    when (characteristic) {
                        services?.getOrNull(2)?.characteristics?.getOrNull(1) -> {
                            // Increment button 1 counter
                            button1ClickCount++
                            Toast.makeText(this@DeviceDetailsActivity, "Bouton 1: $button1ClickCount clics", Toast.LENGTH_SHORT).show()
                        }
                        services?.getOrNull(3)?.characteristics?.getOrNull(0) -> {
                            // Increment button 3 counter
                            button3ClickCount++
                            Toast.makeText(this@DeviceDetailsActivity, "Bouton 3: $button3ClickCount clics", Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            Toast.makeText(this@DeviceDetailsActivity, "Notification inconnue", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun writeToLEDCharacteristic(ledValue: Byte) {
        val characteristic = services?.getOrNull(2)?.characteristics?.getOrNull(0)
        if (characteristic != null) {
            characteristic.value = byteArrayOf(ledValue)
            bluetoothGatt?.writeCharacteristic(characteristic)
            Log.i("DeviceDetailsActivity", "LED $ledValue allumée")
        } else {
            Log.e("DeviceDetailsActivity", "Impossible d'écrire sur la caractéristique des LEDs")
        }
    }

    private fun subscribeToButtonNotifications() {
        val button1Characteristic = services?.getOrNull(2)?.characteristics?.getOrNull(1)
        val button3Characteristic = services?.getOrNull(3)?.characteristics?.getOrNull(0)

        if (button1Characteristic != null) {
            enableNotification(button1Characteristic)
            Log.i("DeviceDetailsActivity", "Abonné aux notifications du bouton 1")
        } else {
            Log.e("DeviceDetailsActivity", "Impossible de s'abonner au bouton 1")
        }

        if (button3Characteristic != null) {
            enableNotification(button3Characteristic)
            Log.i("DeviceDetailsActivity", "Abonné aux notifications du bouton 3")
        } else {
            Log.e("DeviceDetailsActivity", "Impossible de s'abonner au bouton 3")
        }
    }

    private fun enableNotification(characteristic: BluetoothGattCharacteristic) {
        bluetoothGatt?.setCharacteristicNotification(characteristic, true)
        characteristic.descriptors.forEach { descriptor ->
            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            bluetoothGatt?.writeDescriptor(descriptor)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disconnectFromDevice()
    }

    @Composable
    fun DeviceDetailsScreen(
        connectionStatus: String,
        button1ClickCount: Int,
        button3ClickCount: Int,
        onLedAction: (Byte) -> Unit,
        onSubscribe: () -> Unit,
        onDisconnect: () -> Unit,
        onExit: () -> Unit
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "État de la connexion : $connectionStatus")
            Spacer(modifier = Modifier.height(16.dp))

            // LED Control Buttons
            Text(text = "Contrôle des LEDs")
            Row {
                Button(onClick = { onLedAction(0x01) }) { Text(text = "LED 1") }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { onLedAction(0x02) }) { Text(text = "LED 2") }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { onLedAction(0x03) }) { Text(text = "LED 3") }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Button Counters
            Text(text = "Compteurs des boutons")
            Text(text = "Bouton 1: $button1ClickCount clics")
            Text(text = "Bouton 3: $button3ClickCount clics")

            Spacer(modifier = Modifier.height(16.dp))

            // Subscription Button
            Button(onClick = onSubscribe) { Text(text = "S'abonner aux boutons") }

            Spacer(modifier = Modifier.height(16.dp))

            // Disconnect and Exit
            Button(onClick = onDisconnect) { Text(text = "Déconnecter") }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onExit) { Text(text = "Quitter") }
        }
    }
}