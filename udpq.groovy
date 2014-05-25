@Grapes( 
@Grab(group='io.netty', module='netty-all', version='4.0.19.Final') 
)

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.FixedRecvByteBufAllocator;
import io.netty.util.CharsetUtil;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.nio.file.Path
import java.nio.file.WatchKey
import java.nio.file.WatchEvent
import java.nio.file.WatchService
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.StandardWatchEventKinds

public class UDPQServer {
    public static ExecutorService threadPool = Executors.newFixedThreadPool(1);
		
    static String postUrl = System.getProperty("url","http://localhost:81/mb66/post.php");

    private final int port;

    public UDPQServer(int port) {
        this.port = port;
    }

    public void run() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
             .channel(NioDatagramChannel.class)
             .option(ChannelOption.SO_BROADCAST, true)
             .option(ChannelOption.SO_RCVBUF, 8192)
             .option(ChannelOption.SO_SNDBUF, 8192)
             .option(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator(8192))
             .handler(new QuoteOfTheMomentServerHandler());

			println "starting server at port: ${port}";
            b.bind(port).sync().channel().closeFuture().await();
            
        } finally {
            group.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
    	String strport = System.getProperty("port","8888")
      int port = 8888;
      try{
      	port = Integer.parseInt(strport)
      }catch(Exception e)
      {
      	println "invalid port specified"
      }
		final Thread mainThread = Thread.currentThread()
		Runtime.getRuntime().addShutdownHook(
			new Thread(){
				public void run(){
					UDPQServer.threadPool.shutdown();
					int i=0;
					while(!UDPQServer.threadPool.isTerminated()&&i++<5)
					{
						Thread.sleep(2000);
					}
					UDPQServer.threadPool.shutdownNow();
					//mainThread.join()
				}
			});
		Thread stopWatcher = new Thread(new StopWatcherRun(mainThread))
		stopWatcher.start()
		try{
        	new UDPQServer(port).run();
		}catch(InterruptedException e){
			println "interrupted, quitting..."
			System.err.println("interrupted, quitting...");
			System.exit(0)
		}
		
    }
}
class StopWatcherRun implements Runnable {
	private Thread mainThread;
	private boolean keepRunning = true

    public StopWatcherRun(Thread mainThread)
	{
		this.mainThread = mainThread;
	}

	public void run(){
		final Path path = FileSystems.getDefault().getPath(".");
		println "watching ${path} for stop.txt file";
		final WatchService watchService = FileSystems.getDefault().newWatchService();
		final WatchKey watchKey = path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
		while(keepRunning) {
			final WatchKey wk = watchService.take();
			for(WatchEvent event: wk.pollEvents())
			{
				final Path changed = (Path) event.context();
				if(changed.endsWith("stop.txt")) {
					println "stop.now detected, quitting..."
					Files.delete(changed)
					this.keepRunning = false;
					mainThread.interrupt();
				}
			}
			boolean valid = wk.reset();
			if(!valid) {
				println "key has been unregisted"
			}
		}
	}
}
class PosterTask implements Runnable{
	private String content
	
	public PosterTask(String content)
	{
		this.content = content
	}
	
	public void run(){
		String rtn = sendPost('k', content)
		//println "server rtn:${rtn}"
		try{
			Thread.sleep(200);
		}catch(Exception e){
			println "task sleep interval interrupted!"
		}
	}
	
	public String sendPost(String k, String v){
		return sendPostParams(getURL(), [k:v])
	}
	
	public String sendPostParams(url, paramMap)
	{
		println "begin post"
		def HttpURLConnection connection = url.openConnection()
		connection.setDoOutput(true)
		connection.outputStream.withWriter { Writer writer ->
			paramMap.each{k, v->
				//println "writing $k=$v%"
				writer << "$k=$v&"
			}
		}
		String response = connection.inputStream.withReader { Reader reader -> reader.text }
		println "  posted data with return: ${response}" 
		return response
	}
	
	public URL getURL()
	{
		return UDPQServer.postUrl.toURL()
		//return new URL(UDPQServer.postUrl);
	}
}

public class QuoteOfTheMomentServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    public void messageReceived(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {
        //System.err.println(packet);
        //logger.info("received: {}", packet.content().toString(CharsetUtil.UTF_8));
        //System.out.printf("%s ", new Date())
        printFilter( packet.content().toString(CharsetUtil.UTF_8) )
        PosterTask task = new PosterTask(packet.content().toString(CharsetUtil.UTF_8));
        UDPQServer.threadPool.submit(task);
        
        ctx.write(new DatagramPacket(
              Unpooled.copiedBuffer("QUEUED", CharsetUtil.UTF_8), packet.sender()));
       
    }

	  public void printFilter(String msg)
	  {
	  	def pmsg = "${msg}                  "
	  	println pmsg[0..15]
	  }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(
            ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        cause.printStackTrace();
        // We don't close the channel because we can keep serving requests.
    }

	@Override
	protected void channelRead0(ChannelHandlerContext arg0, DatagramPacket arg1)
			throws Exception {
		// TODO Auto-generated method stub
		messageReceived(arg0, arg1);
	}
}
