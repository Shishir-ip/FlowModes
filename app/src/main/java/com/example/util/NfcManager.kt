package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import java.nio.charset.Charset

object NfcManager {

    fun isNfcSupported(context: Context): Boolean {
        val adapter = NfcAdapter.getDefaultAdapter(context)
        return adapter != null
    }

    fun isNfcEnabled(context: Context): Boolean {
        val adapter = NfcAdapter.getDefaultAdapter(context)
        return adapter?.isEnabled == true
    }

    fun parseTagId(intent: Intent): String? {
        val idBytes = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID) ?: return null
        return idBytes.joinToString(":") { "%02X".format(it) }
    }

    fun parseNdefPayload(intent: Intent): String? {
        val rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES) ?: return null
        for (raw in rawMessages) {
            val msg = raw as? NdefMessage ?: continue
            for (record in msg.records) {
                // Check URI record
                if (record.toUri() != null) {
                    return record.toUri().toString()
                }
                // Check Text / Mime record
                try {
                    val payload = String(record.payload, Charset.forName("UTF-8"))
                    if (payload.isNotBlank()) return payload
                } catch (_: Exception) {}
            }
        }
        return null
    }

    fun createModeNdefMessage(modeId: String): NdefMessage {
        val uri = Uri.parse("flowmodes://mode/$modeId")
        val uriRecord = NdefRecord.createUri(uri)
        return NdefMessage(arrayOf(uriRecord))
    }

    fun writeNdefMessageToTag(tag: Tag, message: NdefMessage): Pair<Boolean, String> {
        return try {
            val ndef = Ndef.get(tag)
            if (ndef != null) {
                ndef.connect()
                if (!ndef.isWritable) {
                    ndef.close()
                    return Pair(false, "NFC Tag is write-protected")
                }
                if (ndef.maxSize < message.byteArrayLength) {
                    ndef.close()
                    return Pair(false, "Tag capacity is too small (${ndef.maxSize} bytes vs ${message.byteArrayLength} bytes)")
                }
                ndef.writeNdefMessage(message)
                ndef.close()
                Pair(true, "Successfully wrote Focus Mode to NFC tag!")
            } else {
                val formatable = NdefFormatable.get(tag)
                if (formatable != null) {
                    formatable.connect()
                    formatable.format(message)
                    formatable.close()
                    Pair(true, "Formatted & wrote Focus Mode to NFC tag!")
                } else {
                    Pair(false, "Tag does not support NDEF format")
                }
            }
        } catch (e: Exception) {
            Pair(false, "Failed to write NFC tag: ${e.localizedMessage}")
        }
    }
}
