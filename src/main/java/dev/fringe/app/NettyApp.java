package dev.fringe.app;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

@Data
@Configuration
//@Import({LocalDevToolsAutoConfiguration.class, DevToolsDataSourceAutoConfiguration.class}) not working
// hotswap??
@ComponentScan
@Log4j2
public class NettyApp implements InitializingBean {
	static final boolean SSL = System.getProperty("ssl") != null;
	static final int PORT = Integer.parseInt(System.getProperty("port", SSL ? "8443" : "8081"));

	final NettyServerHandler nettyServerHandler;
	
	@SneakyThrows
	public void afterPropertiesSet() {
		final SslContext ssl = this.buildSslContext();
		EventLoopGroup boss = new NioEventLoopGroup(1);
		EventLoopGroup worker = new NioEventLoopGroup();
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.option(ChannelOption.SO_BACKLOG, 1024);
			b.group(boss, worker)
				.channel(NioServerSocketChannel.class)
				.handler(new LoggingHandler(LogLevel.INFO))
				.childHandler(new ChannelInitializer<Channel>() {
					protected void initChannel(Channel c) {
			            ChannelPipeline p = c.pipeline();
			            if (ssl != null) {
			                p.addLast(ssl.newHandler(c.alloc()));
			            }
			            p.addLast(new HttpServerCodec());
//			            p.addLast(new HttpContentCompressor((CompressionOptions[]) null));
//			            p.addLast(new HttpServerExpectContinueHandler());
			            p.addLast(nettyServerHandler);
					}
				});
			Channel c = b.bind(PORT).sync().channel();
			log.error("Open your web browser and navigate to " + (SSL ? "https" : "http") + "://127.0.0.1:" + PORT + '/'); //log setting need
			c.closeFuture().sync();
		} finally {
			boss.shutdownGracefully();
			worker.shutdownGracefully();
		}
	}
	
    @SneakyThrows
    public SslContext buildSslContext(){
        if (!SSL) {
            return null;
        }
        SelfSignedCertificate c = new SelfSignedCertificate();
        return SslContextBuilder.forServer(c.certificate(), c.privateKey()).build();
    }
    
}
