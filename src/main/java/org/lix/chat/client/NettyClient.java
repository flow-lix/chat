package org.lix.chat.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.lix.chat.handler.SecureChatClientInitializer;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class NettyClient {

    public static final String ADDRESS = System.getProperty("host", "localhost");
    public static final int PORT = Integer.parseInt(System.getProperty("port", "8081"));

    public static void main(String[] args) throws Exception {
        NettyClient client = new NettyClient();
        client.start();
    }

    public void start() throws Exception {
        NioEventLoopGroup group = new NioEventLoopGroup();
        SslContext sslContext = SslContextBuilder
                .forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                .build();

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new SecureChatClientInitializer(sslContext));

            Channel channel = bootstrap.connect(ADDRESS, PORT).sync().channel();

            ChannelFuture lastChannelFuture = null;
            try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
                for (; ; ) {
                    String msg = br.readLine();
                    if (msg == null) {
                        break;
                    }
                    lastChannelFuture = channel.writeAndFlush(msg + "\r\n");
                    if ("bye".equals(msg.toLowerCase())) {
                        channel.closeFuture().sync();
                        break;
                    }
                }
            }

            if (lastChannelFuture != null) {
                lastChannelFuture.sync();
            }
        } finally {
            group.shutdownGracefully();
        }
    }
}
