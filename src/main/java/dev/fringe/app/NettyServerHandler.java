package dev.fringe.app;

import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderValues.CLOSE;
import static io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE;
import static io.netty.handler.codec.http.HttpHeaderValues.TEXT_PLAIN;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpUtil;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Component
@Sharable //i got some error. fix
@AllArgsConstructor
@Log4j2
public class NettyServerHandler extends SimpleChannelInboundHandler<HttpObject> {
	
	final ApplicationContext applicationContext;
	
	private static final byte[] CONTENT = "안녕하세요. 테스트입니다.".getBytes();

	public void channelReadComplete(ChannelHandlerContext c) {
		c.flush();
	}

	public void channelRead0(ChannelHandlerContext c, HttpObject m) {
		if (m instanceof HttpRequest) {
			HttpRequest req = (HttpRequest) m;
			boolean keepAlive = HttpUtil.isKeepAlive(req);
			FullHttpResponse res = new DefaultFullHttpResponse(req.protocolVersion(), OK, Unpooled.wrappedBuffer(CONTENT));
			res.headers().set(CONTENT_TYPE, TEXT_PLAIN+ "; charset=UTF-8").setInt(CONTENT_LENGTH, res.content().readableBytes());
			if (keepAlive) {
				log.error(applicationContext);//log setting need
				if (!req.protocolVersion().isKeepAliveDefault()) {
					res.headers().set(CONNECTION, KEEP_ALIVE);
				}
			} else {
				res.headers().set(CONNECTION, CLOSE);
			}
			ChannelFuture f = c.write(res);
			if (!keepAlive) {
				f.addListener(ChannelFutureListener.CLOSE);
			}
		}
	}

	public void exceptionCaught(ChannelHandlerContext c, Throwable t) {
		t.printStackTrace();
		c.close();
	}
}