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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.scriptforlil.kindroidhealthsync.R
import com.scriptforlil.kindroidhealthsync.ui.theme.KindroidHealthTheme

class HealthConnectPrivacyPolicyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KindroidHealthTheme {
                PrivacyScreen()
            }
        }
    }
}

@Composable
private fun PrivacyScreen() {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(stringResource(R.string.privacy_title), style = MaterialTheme.typography.headlineSmall)
            Text(stringResource(R.string.privacy_body_1), style = MaterialTheme.typography.bodyLarge)
            Text(stringResource(R.string.privacy_body_2), style = MaterialTheme.typography.bodyMedium)
        }
    }
}
