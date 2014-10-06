/* Copyright (c) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sismics.reader.core.dao.file.rss;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.io.ByteStreams;

/**
 * Text reader, which uses a BOM (Byte Order Mark) to identify the encoding to
 * be used. This also has the side effect of removing the BOM from the input
 * stream (when present). If no BOM is present, the encoding will be searched
 * from the XML header.
 * 
 * @author bgamard
 */
public class XmlReader extends Reader {

    private final InputStreamReader internalInputStreamReader;

    /**
     * Bytes to read from the beginning of the XML.
     * We need to read the full XML head tag (to get the encoding).
     */
    private static final int HEADER_SIZE = 128;

    /**
     * @param in Input stream
     * @param defaultEnc Default encoding
     * @throws IOException If an I/O error occurs
     */
    public XmlReader(InputStream in, String defaultEnc) throws IOException {
        // Read ahead four bytes and check for BOM marks. Extra bytes are unread
        // back to the stream; only BOM bytes are skipped.
        String encoding = defaultEnc;
        byte header[] = new byte[HEADER_SIZE];
        int n, unread;

        
        PushbackInputStream pushbackStream = new PushbackInputStream(in, HEADER_SIZE);
        n = ByteStreams.read(in, header, 0, header.length);

        if ((header[0] == (byte) 0xEF) && (header[1] == (byte) 0xBB) && (header[2] == (byte) 0xBF)) {
            encoding = "UTF-8";
            unread = n - 3;
        } else if ((header[0] == (byte) 0xFE) && (header[1] == (byte) 0xFF)) {
            encoding = "UTF-16BE";
            unread = n - 2;
        } else if ((header[0] == (byte) 0xFF) && (header[1] == (byte) 0xFE)) {
            encoding = "UTF-16LE";
            unread = n - 2;
        } else if ((header[0] == (byte) 0x00) && (header[1] == (byte) 0x00) && (header[2] == (byte) 0xFE) && (header[3] == (byte) 0xFF)) {
            encoding = "UTF-32BE";
            unread = n - 4;
        } else if ((header[0] == (byte) 0xFF) && (header[1] == (byte) 0xFE) && (header[2] == (byte) 0x00) && (header[3] == (byte) 0x00)) {
            encoding = "UTF-32LE";
            unread = n - 4;
        } else {
            // Unicode BOM mark not found, unread all bytes and search in the XML header
            unread = n;
            Pattern pattern = Pattern.compile("encoding=\"(.*?)\"");
            Matcher matcher = pattern.matcher(new String(header));
            if (matcher.find()) {
                String enc = matcher.group(1);
                try {
                    Charset.forName(enc);
                    encoding = enc;
                } catch (Exception e) {
                    // Fallback to default encoding if the encoding in the XML header is invalid
                }
            }
        }
        
        if (unread > 0) {
            pushbackStream.unread(header, (n - unread), unread);
        } else if (unread < -1) {
            pushbackStream.unread(header, 0, 0);
        }

        // Use given encoding
        internalInputStreamReader = new InputStreamReader(pushbackStream, encoding);
    }

    @Override
    public void close() throws IOException {
        internalInputStreamReader.close();
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        return internalInputStreamReader.read(cbuf, off, len);
    }
}