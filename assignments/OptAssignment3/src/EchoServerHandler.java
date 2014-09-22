import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

/*
 * Create an EchoServerHandler that inherits from SimpleChannelUpstreamHandler 
 * and implement its messageReceived() hook method so that it echos back the client's input 
 * either (a) a "chunk" at a time or (b) a "line" at a time (i.e., until the symbols "\n", "\r", or "\r\n" are read), 
 * rather than a character at a time.   
 * You can additionally override methods such as channelOpen for example to show logging information.
 */
public class EchoServerHandler extends SimpleChannelUpstreamHandler {

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
		ChannelBuffer buf = (ChannelBuffer)e.getMessage();
		
		Channel ch = e.getChannel();
		ch.write(e.getMessage());		
		
		while (buf.readable()) {
			char c = (char)buf.readByte();
			System.out.println(c);
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
//	
//	@Override
//	public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e)
//			throws Exception {
//		
//	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
		e.getCause().printStackTrace();
		System.out.println(e.toString());
		Channel ch = e.getChannel();
		ch.close();
	}
	
	public static class NettyServer {
		public void startServer() throws Exception {
			// Configure the ServerSocketChannelFactory that inherits from ServerChannelFactory.
			ChannelFactory factory = new NioServerSocketChannelFactory(
					Executors.newCachedThreadPool(),
					Executors.newCachedThreadPool());
			
			// Configure the ServerBootstrap class with appropriate thread pools to run the events
			ServerBootstrap bootstrap = new ServerBootstrap(factory);
			
			// Configure the bootstrap object with a PipelineFactory
			bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
				
				/*
				 * Create a ChannelPipelineFactory which sets up a pipeline for processing the inbound messages.
				 * @see org.jboss.netty.channel.ChannelPipelineFactory#getPipeline()
				 */
				@Override
				public ChannelPipeline getPipeline() throws Exception {
					/*
					 * When a client connection request arrives, the ServerChannelFactory and ChannelPipeline 
					 * work together to pass events to the EchoServerHandler.
					 */
					return Channels.pipeline(new EchoServerHandler());					
				}
			});
			
			bootstrap.setOption("tcpNoDelay", true);
			bootstrap.setOption("keepAlive", true);
			
			// binds the ServerBootstrap class to an appropriate InetSocketAddress
			bootstrap.bind(new InetSocketAddress(80));
		}		
	}
	
	public static void main(String[] args) throws Exception {
		NettyServer ns = new NettyServer();
		ns.startServer();
	}
}
