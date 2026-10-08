package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.navigation.FlowAppContent
import com.example.ui.theme.FlowModesTheme
import com.example.ui.viewmodel.FlowViewModel
import com.example.util.DisplayRefreshRateHelper

class MainActivity : ComponentActivity() {

    private val viewModel: FlowViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        DisplayRefreshRateHelper.optimizeWindowForHighRefreshRate(this)
        handleNfcIntent(intent)

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            val oledBlack by viewModel.oledBlack.collectAsState()

            FlowModesTheme(
                themeMode = themeMode,
                oledBlack = oledBlack
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    FlowAppContent(viewModel = viewModel)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshPermissions()
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNfcIntent(intent)
    }

    private fun handleNfcIntent(intent: android.content.Intent?) {
        if (intent == null) return
        val action = intent.action ?: return
        if (action == android.nfc.NfcAdapter.ACTION_NDEF_DISCOVERED ||
            action == android.nfc.NfcAdapter.ACTION_TAG_DISCOVERED ||
            action == android.nfc.NfcAdapter.ACTION_TECH_DISCOVERED
        ) {
            val tagId = com.example.util.NfcManager.parseTagId(intent)
            val payload = com.example.util.NfcManager.parseNdefPayload(intent)
            viewModel.handleNfcTagScanned(tagId, payload)
        }
    }
}
