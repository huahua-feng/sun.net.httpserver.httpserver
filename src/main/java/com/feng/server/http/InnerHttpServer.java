package com.feng.server.http;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.List;

public class InnerHttpServer {
    public static void main(String[] args) throws IOException {
            // 创建 http 服务器, 绑定本地 8080 端口
            HttpServer httpServer = HttpServer.create(new InetSocketAddress(8383), 0);
            httpServer.createContext("/upload", new HttpHandler() {
                @Override
                public void handle(HttpExchange httpExchange) throws IOException {
//                    System.out.println(IOUtils.toString(httpExchange.getRequestBody()));
                    System.out.println("url: " + httpExchange.getRequestURI().getQuery());
                    Headers headers = httpExchange.getRequestHeaders();
                    int length = Integer.parseInt(headers.getFirst("Content-length"));
                    InputStream in = httpExchange.getRequestBody();
                    byte[] body = IOUtils.toByteArray(in, length);
                    List<HttpRequestFormOtherResolver.ParamItem> params = HttpRequestFormOtherResolver.resolveForm(headers, body);
                    for (HttpRequestFormOtherResolver.ParamItem paramItem : params) {
                        if (paramItem.getType().equals("text")) {
                            System.out.println(paramItem);
                        } else {
                            //write file
                            File file = new File("/Users/huahua/Desktop/upload/"+paramItem.getFilename());
                            if (file.exists()) {
                                file.delete();
                            }
                            file.createNewFile();
                            FileOutputStream fileOutputStream = new FileOutputStream(file);
                            fileOutputStream.write(body, paramItem.getStartIndex(), paramItem.getEndIndex()- paramItem.getStartIndex());
                            fileOutputStream.close();
                        }
                    }


                    httpExchange.sendResponseHeaders(200, "hello".length());
                    OutputStream out = httpExchange.getResponseBody();
                    out.write("hello".getBytes());
                    in.close();
                    out.flush();
                    out.close();
                }
            });
            /*httpServer.createContext("/download", new HttpHandler() {
                @Override
                public void handle(HttpExchange httpExchange) throws IOException {
                    System.out.println("---url: " + httpExchange.getRequestURI().getQuery());
                    File file = new File("/Users/huahua/Downloads/closeSmps.jpg");
                    OutputStream out = httpExchange.getResponseBody();
                    try (FileInputStream in = new FileInputStream(file)){

                        httpExchange.getResponseHeaders().add("Content-Disposition", "attachment;filename="+file.getName());
                        httpExchange.sendResponseHeaders(200, file.length());
                        byte[] fileBytes = new byte[(int) file.length()];
                        in.read(fileBytes);
                        out.write(fileBytes);
                    } finally {
                        out.flush();
                        out.close();
                    }
                }
            });*/
            httpServer.start();
//            httpServer.stop(0);
    }
}
