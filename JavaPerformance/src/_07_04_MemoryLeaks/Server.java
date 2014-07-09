package _07_04_MemoryLeaks;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.*;

public class Server extends AbstractServer {

	private final Map<ClientSession, AsynchronousSocketChannel>	sessionToChannel	= new ConcurrentHashMap<ClientSession, AsynchronousSocketChannel>();

	public Server( int serverPort) throws IOException {
		super( serverPort);
	}

	public void doAccepting() {
		serverSocketChannel.accept( null,
			new CompletionHandler<AsynchronousSocketChannel, Object>() {

				@Override
				public void completed( AsynchronousSocketChannel channel, Object attachment) {

					ClientSession session = new ClientSession();
					sessionToChannel.put( session, channel);
					final ByteBuffer buf = ByteBuffer.allocateDirect( 256);
					channel.read( buf, session, new CompletionHandler<Integer, ClientSession>() {

						@Override
						public void completed( Integer len, ClientSession clSession) {
							AsynchronousSocketChannel channel = sessionToChannel.get( clSession);
							if ( clSession.handleInput( buf, len)) {
								buf.clear();
								channel.read( buf, clSession, this);
							} else {
								try {
									channel.close();
									// clear session entry
									sessionToChannel.remove( clSession);
								} catch ( IOException e) { /* ignore */
								}
							}
						}

						@Override
						public void failed( Throwable exc, ClientSession clSession) {
							System.out.println( "client connection " + clSession
									+ " socket problem with: " + exc);
							exc.printStackTrace();

							clSession.handleFailure();

							AsynchronousSocketChannel channel = sessionToChannel.get( clSession);
							try {
								channel.close();
								// clear session entry
								sessionToChannel.remove( clSession);
							} catch ( IOException e) { /* ignore */
							}
						}
					});
					serverSocketChannel.accept( null, this);
				}

				@Override
				public void failed( Throwable exc, Object attachment) {
					System.out.println( "server socket problem: " + exc);
					exc.printStackTrace();

					serverSocketChannel.accept( null, this);
				}
			});
	}
}
