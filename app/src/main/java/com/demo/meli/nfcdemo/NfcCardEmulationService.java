package com.demo.meli.nfcdemo;

import android.app.Service;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.os.IBinder;

import java.util.Arrays;

public class NfcCardEmulationService extends HostApduService {
    public NfcCardEmulationService() {

    }

    private final static byte[] SELECT_APP = new byte[] {(byte)0x00, (byte)0xa4, (byte)0x04, (byte)0x00,
            (byte)0x07, (byte)0xd2, (byte)0x76, (byte)0x00, (byte)0x00, (byte)0x85, (byte)0x01, (byte)0x01,
            (byte)0x00,
    };

    private final static byte[] SELECT_CC_FILE = new byte[] {(byte)0x00, (byte)0xa4, (byte)0x00, (byte)0x0c,
            (byte)0x02, (byte)0xe1, (byte)0x03,
    };

    private final static byte[] SELECT_NDEF_FILE = new byte[] {(byte)0x00, (byte)0xa4, (byte)0x00, (byte)0x0c,
            (byte)0x02, (byte)0xe1, (byte)0x04,
    };

    private final static byte[] SUCCESS_SW = new byte[] {
            (byte)0x90, (byte)0x00,
    };

    private final static byte[] FAILURE_SW = new byte[] {
            (byte)0x6a, (byte)0x82,
    };

    private final static byte[] CC_FILE = new byte[] {
            0x00, 0x0f, // CCLEN
            0x20, // Mapping Version
            0x00, 0x3b, // Maximum R-APDU data size
            0x00, 0x34, // Maximum C-APDU data size
            0x04, 0x06, // Tag & Length
            (byte)0xe1, 0x04, // NDEF File Identifier
            0x00, 0x32, // Maximum NDEF size
            0x00, // NDEF file read access granted
            (byte)0xff, // NDEF File write access denied
    };

    private static final String URL = "melinfc://compraloquequieras.com";

    private byte[] mNdefRecordFile;

    private boolean mAppSelected;

    private boolean mCcSelected;

    private boolean mNdefSelected;

    @Override
    public void onCreate() {
        super.onCreate();

        mAppSelected = false;
        mCcSelected = false;
        mNdefSelected = false;

        NdefRecord record = NdefRecord.createUri(URL);
        NdefMessage ndefMessage = new NdefMessage(record);

        int nlen = ndefMessage.getByteArrayLength();

        mNdefRecordFile = new byte[nlen + 2];

        mNdefRecordFile[0] = (byte)((nlen & 0xff00) / 256);
        mNdefRecordFile[1] = (byte)(nlen & 0xff);
        System.arraycopy(ndefMessage.toByteArray(), 0, mNdefRecordFile, 2, ndefMessage.getByteArrayLength());
    }

    @Override
    public byte[] processCommandApdu(byte[] commandApdu, Bundle extras) {
        if (Arrays.equals(SELECT_APP, commandApdu)) {
            mAppSelected = true;
            mCcSelected = false;
            mNdefSelected = false;
            return SUCCESS_SW; // 成功
        } else if (mAppSelected && Arrays.equals(SELECT_CC_FILE, commandApdu)) {
            mCcSelected = true;
            mNdefSelected = false;
            return SUCCESS_SW; // 成功
        } else if (mAppSelected && Arrays.equals(SELECT_NDEF_FILE, commandApdu)) {
            mCcSelected = false;
            mNdefSelected = true;
            return SUCCESS_SW; // 成功
        } else if (commandApdu[0] == (byte)0x00 && commandApdu[1] == (byte)0xb0) {
            int offset = (0x00ff & commandApdu[2]) * 256 + (0x00ff & commandApdu[3]);
            int le = 0x00ff & commandApdu[4];

            byte[] responseApdu = new byte[le + SUCCESS_SW.length];

            if (mCcSelected && offset == 0 && le == CC_FILE.length) {
                System.arraycopy(CC_FILE, offset, responseApdu, 0, le);
                System.arraycopy(SUCCESS_SW, 0, responseApdu, le, SUCCESS_SW.length);

                return responseApdu;
            } else if (mNdefSelected) {
                if (offset + le <= mNdefRecordFile.length) {
                    System.arraycopy(mNdefRecordFile, offset, responseApdu, 0, le);
                    System.arraycopy(SUCCESS_SW, 0, responseApdu, le, SUCCESS_SW.length);

                    return responseApdu;
                }
            }
        }

        return FAILURE_SW;
    }

    @Override
    public void onDeactivated(int reason) {
        mAppSelected = false;
        mCcSelected = false;
        mNdefSelected = false;
    }
}