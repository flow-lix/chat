package org.lix.chat.handler;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.ssl.SslContext;
import org.lix.chat.client.NettyClient;

public class SecureChatClientInitializer extends ChannelInitializer<SocketChannel> {

    private SslContext sslContext;

    public SecureChatClientInitializer(SslContext sslContext) {
        this.sslContext = sslContext;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline()
                .addLast(sslContext.newHandler(ch.alloc(), NettyClient.ADDRESS, NettyClient.PORT))
                .addLast(new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()))
                .addLast(new StringDecoder())
                .addLast(new StringEncoder())
                .addLast(new SecureChatClientHandler());
    }
}
