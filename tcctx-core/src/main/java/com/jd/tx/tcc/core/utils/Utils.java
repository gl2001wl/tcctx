package com.jd.tx.tcc.core.utils;

import java.io.*;

/**
 * @author Leon Guo
 *         Creation Date: 2016/4/8
 */
public class Utils {

    public final static int DEFAULT_BUFFER_SIZE = 1024 * 4;

    public static byte[] readByteArrayFromResource(String resource) throws IOException {
        try (InputStream in =Thread.currentThread().getContextClassLoader().getResourceAsStream(resource)) {
            if (in == null) {
                return null;
            }

            return readByteArray(in);
        }
    }

    public static String readFromResource(String resource) throws IOException {
        try (InputStream in =
                     Thread.currentThread().getContextClassLoader().getResourceAsStream(resource) == null ?
                             Utils.class.getResourceAsStream(resource) : Thread.currentThread().getContextClassLoader().getResourceAsStream(resource)) {
            if (in == null) {
                return null;
            }

            String text = Utils.read(in);
            return text;
        }
    }

    public static String read(InputStream in) {
        InputStreamReader reader;
        try {
            reader = new InputStreamReader(in, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        return read(reader);
    }

    public static String read(Reader reader) {
        try {

            StringWriter writer = new StringWriter();

            char[] buffer = new char[DEFAULT_BUFFER_SIZE];
            int n;
            while (-1 != (n = reader.read(buffer))) {
                writer.write(buffer, 0, n);
            }

            return writer.toString();
        } catch (IOException ex) {
            throw new IllegalStateException("read error", ex);
        }
    }

    public static byte[] readByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        copy(input, output);
        return output.toByteArray();
    }

    public static long copy(InputStream input, OutputStream output) throws IOException {
        final int EOF = -1;

        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];

        long count = 0;
        int n = 0;
        while (EOF != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

}
