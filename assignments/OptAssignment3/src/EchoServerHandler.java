import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.Bootstrap;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

public class EchoServerHandler extends SimpleChannelUpstreamHandler {

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
		ChannelBuffer buf = (ChannelBuffer)e.getMessage();
		
		while (buf.readable()) {
			if((buf.readChar() == '\n') || (buf.readChar() == '\r'))
				System.out.println(buf.readChar());
			
			System.out.flush();
		}		
	}
	
	@Override
	public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e)
			throws Exception {
		
		if (e instanceof ChannelStateEvent) {
			System.out.println("Channel state changed: " + e);
		}
		
		super.handleUpstream(ctx, e);		
	}
	
	@Override
	public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {
		
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
		e.getCause().printStackTrace();
		System.out.println(e.toString());
		Channel ch = e.getChannel();
		ch.close();
	}
	
	public static class NettyServer {
		public void startServer() throws Exception {
			ChannelFactory factory = new NioServerSocketChannelFactory(
					Executors.newCachedThreadPool(),
					Executors.newCachedThreadPool());
			
			ServerBootstrap bootstrap = new ServerBootstrap(factory);
			bootstrap.bind(new InetSocketAddress(80));
		}		
	}
	
	public static void main(String[] args) throws Exception {
		NettyServer ns = new NettyServer();
		ns.startServer();
	}
}
