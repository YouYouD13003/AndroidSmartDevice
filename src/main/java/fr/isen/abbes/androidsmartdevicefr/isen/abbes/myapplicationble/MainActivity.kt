package fr.isen.abbes.androidsmartdevicefr.isen.abbes.myapplicationble

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainScreen()
        }
    }
}

@Composable
fun MainScreen() {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Barre supérieure avec le titre (toujours tout en haut)
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

        // Contenu principal (centré verticalement)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Afficher l'image au centre
            Image(
                painter = painterResource(id = R.drawable.ble), // Nom de l'image dans drawable
                contentDescription = "BLE Logo",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(200.dp) // Taille de l'image
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Bouton pour démarrer l'activité ScanActivity
            Button(onClick = {
                val intent = Intent(context, ScanActivity::class.java)
                context.startActivity(intent)
            }) {
                Text(text = "Commencer")
            }
        }
    }
}
