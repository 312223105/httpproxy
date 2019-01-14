package sdt;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

/**
 * Created by xuzhe on 2019/1/7.
 */
public class ServerChannelInitializer extends ChannelInitializer<SocketChannel> {
    public ServerChannelInitializer() {
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline().addLast(new HttpResponseEncoder());
        ch.pipeline().addLast(new HttpRequestDecoder());
//        ch.pipeline().addLast(new HttpObjectAggregator(1024*1024*1024));
        ch.pipeline().addLast(new ProxyServerHandler());
    }
}
