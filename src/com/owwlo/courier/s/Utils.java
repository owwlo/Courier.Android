package com.owwlo.courier.s;

import java.util.Random;

public class Utils {
    private static final String TAG = Utils.class.getSimpleName();

    public static String byteArrayToHexString(byte[] array) {
        StringBuffer hexString = new StringBuffer();
        for (byte b : array) {
            int intVal = b & 0xff;
            if (intVal < 0x10)
                hexString.append("0");
            hexString.append(Integer.toHexString(intVal));
        }
        return hexString.toString();
    }

    public static char generateRandomAuthChar() {
        Random random = new Random();
        int pickIndex = random.nextInt(36);
        if (pickIndex < 10) {
            return (char) ('0' + pickIndex);
        } else {
            return (char) ('A' + pickIndex - 10);
        }
    }
}
