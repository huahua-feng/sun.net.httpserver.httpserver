package com.feng.server.http;

import com.sun.net.httpserver.Headers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class HttpRequestFormOtherResolver {

    private static final int PARAM_INDEX = "Content-Disposition: form-data; ".length();

    public static List<ParamItem> resolveForm(Headers headers, byte[] body) throws IOException {
        String contentType = headers.getFirst("Content-type");
        String boundary = contentType.substring(contentType.indexOf("=") + 1);
        boundary = "--" + boundary; //size 52
        byte[] boundaryBytes = boundary.getBytes(StandardCharsets.UTF_8);

        byte[] sep = "\r\n".getBytes(StandardCharsets.UTF_8);
        List<ParamItem> paramItems = boundaryIndex(body, boundaryBytes, sep);
        return paramItems;
    }

    private static ParamItem resolveParam(String lineStr) {
        lineStr = lineStr.substring(PARAM_INDEX);
        String[] kVs = lineStr.split(";");
        ParamItem paramItem = new ParamItem();
        paramItem.setType("text");
        for (String kV : kVs) {
            String[] k_v = kV.trim().split("=");
            if ("name".equals(k_v[0])) {
                paramItem.setName(k_v[1].replace("\"", ""));
            } else if ("filename".equals(k_v[0])) {
                paramItem.setFilename(k_v[1].replace("\"", ""));
                paramItem.setType("file");
            }
        }
        return paramItem;
    }

    private static boolean isLineBlank(byte[] line) {
        if (line.length == 0) {
            return true;
        }
        byte[] sep = "\r\n".getBytes(StandardCharsets.UTF_8);
        if (line.length == 2) {
            if (line[0] == sep[0] && line[1] == sep[1]) {
                return true;
            }
        }
        return false;
    }

    private static List<ParamItem> boundaryIndex(byte[] body, byte[] boundary, byte[] sep) {
        int count = 0;
        int sep_count = 0;
        List<ParamItem> list = new ArrayList<>();
        int length = body.length;
        int boundaryLen = boundary.length;
        int cursor = boundaryLen + 2;
        ParamItem paramItem = null;
        boolean paramStart = false;
        boolean paramEnd = false;

        for (int i = 0; i < length; i++) {
            for (int j = 0; j < boundaryLen; j++) {
                if (i + j == length) {
                    return list;
                }
                if (body[i + j] == boundary[j]) {
                    count++;
                } else {
                    break;
                }
            }
            if (count == boundaryLen) {
                if (i > 0)
                    paramEnd = true;
                paramStart = true;
                i += boundaryLen + 2 - 1;
            }

            if (paramStart) {
                for (int j = 0; j < 2; j++) {
                    if (body[i + j] == sep[j]) {
                        sep_count++;
                    } else {
                        break;
                    }
                }

                if (sep_count == 2) {
                    byte[] line = new byte[i - cursor];
                    System.arraycopy(body, cursor, line, 0, i - cursor);

                    if (isLineBlank(line)) {
                        paramStart = false;
                        paramEnd = false;
                        cursor = i + 2;
                    } else {
                        String lineStr = new String(line);
                        if (lineStr.startsWith("Content-Disposition: form-data; ")) {
                            paramItem = resolveParam(lineStr);
                        }
                        cursor = i;
                    }
                    i += 1;
                }
                sep_count = 0;
            }

            if (paramEnd) {
                if (paramItem == null) {
                    return null;
                }
                if (paramItem.getType().equals("text")) {
                    byte[] val = new byte[i - cursor - boundaryLen - 1 - 2];
                    System.arraycopy(body, cursor, val, 0, i - cursor - boundaryLen - 1 - 2);
                    paramItem.setVal(new String(val));
                } else {
                    paramItem.setStartIndex(cursor);
                    paramItem.setEndIndex(i - boundaryLen - 1 - 2);
                }
                list.add(paramItem);

                cursor = i+1;
                paramEnd = false;
            }

            count = 0;
        }

        return list;
    }

    public static class ParamItem {
        private String type;//text file
        private String name;
        private String filename;
        private String val;
        private int startIndex;
        private int endIndex;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }

        public String getVal() {
            return val;
        }

        public void setVal(String val) {
            this.val = val;
        }

        public int getStartIndex() {
            return startIndex;
        }

        public void setStartIndex(int startIndex) {
            this.startIndex = startIndex;
        }

        public int getEndIndex() {
            return endIndex;
        }

        public void setEndIndex(int endIndex) {
            this.endIndex = endIndex;
        }

        @Override
        public String toString() {
            return "ParamItem{" +
                    "type='" + type + '\'' +
                    ", name='" + name + '\'' +
                    ", filename='" + filename + '\'' +
                    ", val='" + val + '\'' +
                    ", startIndex=" + startIndex +
                    ", endIndex=" + endIndex +
                    '}';
        }
    }
}
