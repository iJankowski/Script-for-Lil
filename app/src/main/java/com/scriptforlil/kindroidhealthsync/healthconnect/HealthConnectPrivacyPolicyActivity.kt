package com.scriptforlil.kindroidhealthsync.healthconnect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.scriptforlil.kindroidhealthsync.ui.theme.KindroidHealthTheme

class HealthConnectPrivacyPolicyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KindroidHealthTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Dlaczego prosimy o dostęp", style = MaterialTheme.typography.headlineSmall)
                        Text(
                            "Aplikacja odczytuje sen, kroki i tętno z Health Connect tylko po to, żeby zbudować krótką wiadomość kontekstową i wysłać ją do jednego Kindroida.",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            "Nie używamy tych danych do reklam ani sprzedaży. W tej wersji dane są przetwarzane lokalnie na telefonie, a na zewnątrz wychodzi wyłącznie finalna wiadomość wysyłana do Kindroid API.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}
