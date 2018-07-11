/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.demo.meli.nfcdemo.seller;

import android.content.Context;
import android.content.Intent;
import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.demo.meli.nfcdemo.NfcApplication;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

/**
 * This HostApduService is implementing a totally custom NFC Tag protocol.
 *
 * It's necessary for the Android buyers (NFC Reader) whose are not able to
 * read the NFC Tag Type 4 implemented by {@link NfcCardEmulationService}
 */

public class NfcCustomCardEmulationService extends HostApduService {
    private static final String TAG = "NfcCustomCardEmulationService";

    // Custom AID for our card service.
    private static final String CUSTOM_CARD_AID = "F222222222";

    // ISO-DEP command HEADER for selecting an AID.
    // Format: [Class | Instruction | Parameter 1 | Parameter 2]
    private static final String SELECT_APDU_HEADER = "00A40400";
    // Format: [Class | Instruction | Parameter 1 | Parameter 2]
    private static final String GET_DATA_APDU_HEADER = "00CA0000";
    // "OK" status word sent in response to SELECT AID command (0x9000)
    private static final byte[] SELECT_OK_SW = HexStringToByteArray("9000");
    // "UNKNOWN" status word sent in response to invalid APDU command (0x0000)
    private static final byte[] UNKNOWN_CMD_SW = HexStringToByteArray("0000");
    private static final byte[] SELECT_APDU = BuildSelectApdu(CUSTOM_CARD_AID);
    private static final byte[] GET_DATA_APDU = BuildGetDataApdu();

    /*File IO Stuffs*/
    File sdcard = Environment.getExternalStorageDirectory();
    File file = new File(sdcard,"file.txt");
    StringBuilder text = new StringBuilder();
    int pointer;


    public static Intent newIntent(final Context context) {
        final Intent intent = new Intent(context, NfcCustomCardEmulationService.class);
        return intent;
    }


    @Override
    public void onDeactivated(int reason) { }

    @Override
    public byte[] processCommandApdu(byte[] commandApdu, Bundle extras) {
        Log.i(TAG, "Received APDU: " + ByteArrayToHexString(commandApdu));
        // If the APDU matches the SELECT AID command for this service,
        // send the payment url, followed by a SELECT_OK status trailer (0x9000).
        if (Arrays.equals(SELECT_APDU, commandApdu)) {
            String paymentUrl = ((NfcApplication) getApplication()).getPaymentUrl();
            byte[] accountBytes = paymentUrl.getBytes();
            Log.i(TAG, "Sending payment Url: " + paymentUrl);
            readFromFile();
            return ConcatArrays(accountBytes, SELECT_OK_SW);
        } else if ((Arrays.equals(GET_DATA_APDU, commandApdu))) {
            String stringToSend;
            try {
                stringToSend = text.toString().substring(pointer, pointer + 200);
            } catch (IndexOutOfBoundsException e) {
                Toast.makeText(this, "Reached the end of the file", Toast.LENGTH_SHORT).show();
                stringToSend = "END";
            }
            pointer += 200;byte[] accountBytes = stringToSend.getBytes();
            Log.i(TAG, "Sending substring, pointer : " + pointer + " , " + stringToSend);
            return ConcatArrays(accountBytes, SELECT_OK_SW);
        }

        else {
                return UNKNOWN_CMD_SW;
        }
    }

    public static byte[] BuildSelectApdu(String aid) {
        // Format: [CLASS | INSTRUCTION | PARAMETER 1 | PARAMETER 2 | LENGTH | DATA]
        return HexStringToByteArray(SELECT_APDU_HEADER + String.format("%02X",
                aid.length() / 2) + aid);
    }

    public static byte[] BuildGetDataApdu() {
        // Format: [CLASS | INSTRUCTION | PARAMETER 1 | PARAMETER 2 | LENGTH | DATA]
        return HexStringToByteArray(GET_DATA_APDU_HEADER + "0FFF");
    }

    public static String ByteArrayToHexString(byte[] bytes) {
        final char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
        char[] hexChars = new char[bytes.length * 2]; // Each byte has two hex characters (nibbles)
        int v;
        for (int j = 0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF; // Cast bytes[j] to int, treating as unsigned value
            hexChars[j * 2] = hexArray[v >>> 4]; // Select hex character from upper nibble
            hexChars[j * 2 + 1] = hexArray[v & 0x0F]; // Select hex character from lower nibble
        }
        return new String(hexChars);
    }

    public static byte[] HexStringToByteArray(String s) throws IllegalArgumentException {
        int len = s.length();
        if (len % 2 == 1) {
            throw new IllegalArgumentException("Hex string must have even number of characters");
        }
        byte[] data = new byte[len / 2]; // Allocate 1 byte per 2 hex characters
        for (int i = 0; i < len; i += 2) {
            // Convert each character into a integer (base-16), then bit-shift into place
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public static byte[] ConcatArrays(byte[] first, byte[]... rest) {
        int totalLength = first.length;
        for (byte[] array : rest) {
            totalLength += array.length;
        }
        byte[] result = Arrays.copyOf(first, totalLength);
        int offset = first.length;
        for (byte[] array : rest) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }

    private void readFromFile() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}