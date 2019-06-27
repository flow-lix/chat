package org.lix.chat.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.net.InetAddress;

/**
 * Handlers a Server-side channel
 */
public class SecureChatServerHandler extends SimpleChannelInboundHandler<String> {

    static final ChannelGroup activatedChannels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.pipeline().get(SslHandler.class).handshakeFuture().addListener(future -> {
            ctx.writeAndFlush("Welcome to " + InetAddress.getLocalHost().getHostAddress() +
                    " secure chat server!\n");
            ctx.writeAndFlush("You session is protected by " +
                    ctx.pipeline().get(SslHandler.class).engine().getSession().getCipherSuite() + " cipher suite!\n");
            activatedChannels.add(ctx.channel());
        });
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("inactive..");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        for (Channel channel : activatedChannels) {
            if (channel == ctx.channel()) {
                channel.writeAndFlush("[You]: " + msg + "\n");
            } else {
                channel.writeAndFlush("[" + ctx.channel().remoteAddress() + "]: " + msg + "\n");
            }
        }
        if ("bye".equals(msg.toLowerCase())) {
            ctx.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
