//package fr.isen.abbes.androidsmartdevicefr.isen.abbes.myapplicationble
//
//import android.content.Intent
//import android.os.Bundle
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.material3.Button
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//
//class MainActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContent {
//            MainScreen()
//        }
//    }
//}
//
//@Composable
//fun MainScreen() {
//    val context = LocalContext.current
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//    ) {
//        // Barre supérieure avec le titre (toujours tout en haut)
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .background(Color.Blue)
//                .padding(16.dp),
//            contentAlignment = Alignment.Center
//        ) {
//            Text(
//                text = "ABBES Ayoub BLE APP",
//                color = Color.White,
//                fontWeight = FontWeight.Bold,
//                fontSize = 20.sp
//            )
//        }
//
//        // Contenu principal (centré verticalement)
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(16.dp),
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.Center
//        ) {
//            Spacer(modifier = Modifier.height(20.dp))
//
//            // Afficher l'image au centre
//            Image(
//                painter = painterResource(id = R.drawable.ble), // Nom de l'image dans drawable
//                contentDescription = "BLE Logo",
//                contentScale = ContentScale.Fit,
//                modifier = Modifier
//                    .size(200.dp) // Taille de l'image
//            )
//
//            Spacer(modifier = Modifier.height(40.dp))
//
//            // Bouton pour démarrer l'activité ScanActivity
//            Button(onClick = {
//                val intent = Intent(context, ScanActivity::class.java)
//                context.startActivity(intent)
//            }) {
//                Text(text = "Commencer")
//            }
//        }
//    }
//}
package fr.isen.abbes.androidsmartdevicefr.isen.abbes.myapplicationble

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    // Initializing the Bluetooth Adapter
    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        BluetoothAdapter.getDefaultAdapter()
    }

    // Launcher for enabling Bluetooth
    private val enableBtLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                // Bluetooth has been enabled
                startScanActivity()
            } else {
                // User denied enabling Bluetooth
                Toast.makeText(this, "Bluetooth doit être activé pour continuer", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainScreen { checkBluetoothAndStartScan() }
        }
    }

    /**
     * Checks if Bluetooth is enabled and starts the ScanActivity.
     * If Bluetooth is disabled, requests the user to enable it.
     */
    private fun checkBluetoothAndStartScan() {
        val adapter = bluetoothAdapter // Store in a local variable
        if (adapter == null || !adapter.isEnabled) {
            // If Bluetooth is disabled or null, prompt the user to enable it
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            enableBtLauncher.launch(enableBtIntent)
        } else {
            // If Bluetooth is enabled, start the ScanActivity
            startScanActivity()
        }
    }

    /**
     * Starts the ScanActivity.
     */
    private fun startScanActivity() {
        val intent = Intent(this, ScanActivity::class.java)
        startActivity(intent)
    }
}

@Composable
fun MainScreen(onStartScanClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Top bar with title
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Blue)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "ABBES Ayoub BLE APP",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }

        // Main content (vertically centered)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Display an image in the center
            Image(
                painter = painterResource(id = R.drawable.ble), // Image name in drawable
                contentDescription = "BLE Logo",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(200.dp) // Image size
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Button to start the ScanActivity
            Button(onClick = onStartScanClick) {
                Text(text = "Commencer")
            }
        }
    }
}
