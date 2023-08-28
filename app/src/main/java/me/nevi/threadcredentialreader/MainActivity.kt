package me.nevi.threadcredentialreader

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.gms.threadnetwork.ThreadNetwork
import com.google.android.gms.threadnetwork.ThreadNetworkCredentials
import me.nevi.threadcredentialreader.ui.theme.ThreadCredentialReaderTheme

class MainActivity : ComponentActivity() {
    private lateinit var preferredCredentialsLauncher: ActivityResultLauncher<IntentSenderRequest>

    private var allThreadCredentials: MutableList<ThreadNetworkCredentials> = mutableListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ThreadCredentialReaderTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column {
                        Button(onClick = {
//                        this@MainActivity.getAllThreadNetworkCredentials()
                            this@MainActivity.getPreferredThreadNetworkCredentials()
                        }) {
                            Text(text = "Get All Credentials")
                        }
                        ThreadNetworkCredentialsList(allThreadCredentials)
                    }
                }
            }
        }

        preferredCredentialsLauncher =
            registerForActivityResult(
                ActivityResultContracts.StartIntentSenderForResult()
            ) { result: ActivityResult ->
                if (result.resultCode == RESULT_OK) {
                    val threadNetworkCredentials = ThreadNetworkCredentials.fromIntentSenderResultData(result.data!!)
                    this.allThreadCredentials.add(threadNetworkCredentials)
                    Log.d("debug", threadNetworkCredentials.networkName)
                    Log.d("debug", threadNetworkCredentials.networkKey.toHex())
                    Log.d("debug", threadNetworkCredentials.pskc.toHex())
                    Log.d("debug", threadNetworkCredentials.panId.toString())
                    Log.d("debug", threadNetworkCredentials.extendedPanId.toHex())
                    Log.d("debug", threadNetworkCredentials.activeOperationalDataset.toHex())
                } else {
                    Log.d("debug", "User denied request.")
                }
            }
    }

    private fun getPreferredThreadNetworkCredentials() {
        ThreadNetwork.getClient(this)
            .preferredCredentials
            .addOnSuccessListener { intentSenderResult ->
                intentSenderResult.intentSender?.let {
                    preferredCredentialsLauncher.launch(IntentSenderRequest.Builder(it).build())
                } ?: Log.d("debug", "No preferred credentials found.")
            }
            .addOnFailureListener { e: Exception -> Log.d("MainActivity", "ERROR: [${e}]") }
    }

    private fun getAllThreadNetworkCredentials() {
        ThreadNetwork.getClient(this)
            .allCredentials
            .addOnSuccessListener { credentials ->
                this.allThreadCredentials = credentials
                Log.w("MainActivity", "Found ${credentials.size} credentials")
            }
            .addOnFailureListener { e: Exception -> Log.d("MainActivity", "ERROR: [${e}]") }
    }
}

@Composable
fun ThreadNetworkCredentialsList(credentials: List<ThreadNetworkCredentials>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        credentials.forEach{ credential ->
            ThreadNetworkCredentialItem(credential = credential)
        }
    }
}

@Composable
fun ThreadNetworkCredentialItem(credential: ThreadNetworkCredentials) {
    Column {
        Text(credential.networkName)
        Text(credential.activeOperationalDataset.toHex())
    }
}

fun ByteArray.toHex(): String = joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }