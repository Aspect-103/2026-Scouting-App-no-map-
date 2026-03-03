package com.mercury1089.Scouting_App_2026.qr;


import android.graphics.Bitmap;

import com.google.zxing.EncodeHintType;

import net.glxn.qrgen.android.QRCode;

public class QRUtils {
    //for QR code generator
    public final static int QRCodeSize = 500;
    public final static int QRCodeMargin = 1;
    public static Bitmap textToImageEncode(String qrString) {
        return QRCode.from(qrString).withSize(QRCodeSize, QRCodeSize)
                .withHint(EncodeHintType.MARGIN, QRCodeMargin)
                .bitmap();

    }
}
