package com.deng.drpc.server;

import io.vertx.core.Vertx;

/**
 * 基于Vertx的Web服务器
 */
public class VertxHttpServer implements HttpServer{
    @Override
    public void doStart(int port) {
        //创建Vert.x实例
        Vertx vertx = Vertx.vertx();

        //创建HTTP服务器
        io.vertx.core.http.HttpServer server = vertx.createHttpServer();

//        server.requestHandler(httpServerRequest -> {
//            //处理HTTP请求
//            System.out.println("Received request:" + httpServerRequest.method() + "" + httpServerRequest.uri());
//
//            //发送HTTP响应
//            httpServerRequest.response()
//                    .putHeader("content-type", "text/plain")
//                    .end("Hello from Vert.x Http server!");
//        });
        //监听端口并处理请求
        server.requestHandler(new HttpServerHandler());

        //启动HTTP服务器并监听接口
        server.listen(port,result -> {
            if(result.succeeded()){
                System.out.println("Server succeeded"+port);
            }else{
                System.err.println("Failed to listen"+result.cause());
            }
        });
    }
}
