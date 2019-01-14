package sdt;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.EventExecutorGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * Created by xuzhe on 2019/1/7.
 */
public class ProxyServerHandler extends ChannelInboundHandlerAdapter {
    static Logger logger = LoggerFactory.getLogger(ProxyServerHandler.class);
    private Channel channel;
    private ChannelFuture serverChannelFuture;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.channel = ctx.channel();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, final Object msg) throws Exception {
        logger.debug(msg.toString());
//        if (msg instanceof HttpRequest) {
//            DefaultHttpRequest request = (DefaultHttpRequest) msg;
//            logger.debug("url:{}", request.uri());
//            ctx.close();
//        ReferenceCountUtil.retain(msg);
        if (serverChannelFuture == null) {
            serverChannelFuture = getChannel(new InetSocketAddress("localhost", 80)
                    , ctx.channel().eventLoop());
        }

        serverChannelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
//                        DefaultHttpRequest request1 = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/");
                    Object request1 = msg;
                    logger.debug("{}", request1);
                    future.channel().write(request1);
                    future.channel().flush();
                } else {
                    logger.debug("{}", future.cause());
                }
            }
        });
//        } else {


//        }
    }

    public ChannelFuture getChannel(SocketAddress addr, EventLoopGroup eventLoopGroup) {
        Bootstrap b = new Bootstrap();
        b.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ch.pipeline().addLast(new HttpClientCodec());
                        ch.pipeline().addLast(new ProxyClientHandler(channel));
                    }
                });
        ChannelFuture future = b.connect(addr);
        return future;
    }
}
