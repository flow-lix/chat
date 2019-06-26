package org.lix.chat.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import org.lix.chat.handler.SecureChatInitializer;

public class NettyServer {

    private static NettyServer nettyServer;

    private NettyServer() {
    }

    private EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    private EventLoopGroup workerGroup = new NioEventLoopGroup();

    public void start() throws Exception {
        ServerBootstrap bootstrap = new ServerBootstrap();

        SelfSignedCertificate certificate = new SelfSignedCertificate();
        SslContext sslContext = SslContextBuilder
                .forServer(certificate.certificate(), certificate.privateKey())
                .build();

        try {
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.DEBUG))
                    .childHandler(new SecureChatInitializer(sslContext));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static NettyServer getNettyServer() {
        return NettyServerHolder.INSTANCE;
    }

    private static class NettyServerHolder {
        static final NettyServer INSTANCE = initNettyServer();

        private static NettyServer initNettyServer() {
            return new NettyServer();
        }
    }
}
