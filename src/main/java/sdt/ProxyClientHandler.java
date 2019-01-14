package sdt;

import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.EventExecutorGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by xuzhe on 2019/1/7.
 */
public class ProxyClientHandler extends ChannelInboundHandlerAdapter {
    static Logger logger = LoggerFactory.getLogger(ProxyClientHandler.class);
    private final Channel clientChannel;
    private Channel channel;

    public ProxyClientHandler(Channel clientChannel) {
        this.clientChannel = clientChannel;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.channel = ctx.channel();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpResponse) {
            DefaultHttpResponse response = (DefaultHttpResponse) msg;
            Iterator<Map.Entry<String, String>> it = response.headers().iteratorAsString();
            while (it.hasNext()) {
                Map.Entry<String, String> entry = it.next();
                logger.debug("{} = {}", entry.getKey(), entry.getValue());
            }
            clientChannel.write(msg);
        } else if (msg instanceof HttpContent) {
//            ReferenceCountUtil.retain(msg);
            if (msg instanceof LastHttpContent) {
                logger.debug("last http content");

                clientChannel.writeAndFlush(msg).addListener(new ChannelFutureListener() {

                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        logger.debug("channel closed!");
                        clientChannel.close();
                        channel.close();
                    }
                });

            } else {
                DefaultHttpContent httpContent = (DefaultHttpContent) msg;
                clientChannel.writeAndFlush(httpContent);
//                logger.debug("readableBytes: {}", httpContent.content().readableBytes());

            }
        }
//        logger.debug(msg.toString());
    }
}
