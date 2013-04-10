package com.sismics.reader.core.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Utility to check MIME types.
 *
 * @author jtremeaux
 */
public class MimeTypeUtil {
    /**
     * Try to guess the MIME type of a file by its magic number (header).
     * 
     * @param file File to inspect
     * @return MIME type
     * @throws Exception
     */
    public static String guessMimeType(File file) throws Exception {
        InputStream is = null;
        try {
            is = new FileInputStream(file);
            byte[] headerBytes = new byte[64];
            int readCount = is.read(headerBytes, 0, headerBytes.length);
            if (readCount <= 0) {
                throw new Exception("Cannot read input file");
            }
            String header = new String(headerBytes, "US-ASCII");
            
            if (header.startsWith("GIF87a") || header.startsWith("GIF89a")) {
                return "image/gif";
            } else  if (headerBytes[0] == ((byte) 0xff) && headerBytes[1] == ((byte) 0xd8)) {
                return "image/jpeg";
            } else if (headerBytes[0] == ((byte) 0x89) && headerBytes[1] == ((byte) 0x50) && headerBytes[2] == ((byte) 0x4e) && headerBytes[3] == ((byte) 0x47) &&
                    headerBytes[4] == ((byte) 0x0d) && headerBytes[5] == ((byte) 0x0a) && headerBytes[6] == ((byte) 0x1a) && headerBytes[7] == ((byte) 0x0a)) {
                return "image/png";
            } else if (headerBytes[0] == ((byte) 0x00) && headerBytes[1] == ((byte) 0x00) && headerBytes[2] == ((byte) 0x01) && headerBytes[3] == ((byte) 0x00)) {
                return "image/x-icon";
            }
        } finally {
            if (is != null) {
                is.close();
            }
        }
        return null;
    }
}
