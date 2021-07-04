package com.feng.server.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class IOUtils {

    private static final int EOF = -1;
    private static final int BUFFER_SIZE = 1024*4; //4k read write

    public byte[] toByteArray(InputStream in){
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()){
            byte[] bytes = new byte[BUFFER_SIZE];
            while (EOF != in.read(bytes)) {
                out.write(bytes);
            }
            out.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
