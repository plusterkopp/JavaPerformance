package _07_04_MemoryLeaks;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.concurrent.atomic.*;

public abstract class AbstractServer {

	protected static class ClientSession {
		private static final AtomicInteger	clientCnt	= new AtomicInteger( 1);

		private final int					myId		= clientCnt.getAndIncrement();
		private volatile int				byteCnt;

		public boolean handleInput( ByteBuffer buf, int len) {
			if ( len >= 0) {
				byteCnt += len;
				return true;
			}
			buf.clear();
			System.out.println( "received " + byteCnt + " bytes from client " + myId + " by "
					+ Thread.currentThread().getName());
			System.out.flush();
			return false;
		}

		public void handleFailure() {
			System.out.println( "*** failure ***  received " + byteCnt + " bytes from client "
					+ myId + " by " + Thread.currentThread().getName());
		}

		public String toString() {
			return ( "ClientSession " + myId);
		}
	}

	protected final AsynchronousServerSocketChannel	serverSocketChannel;

	public AbstractServer( int serverPort) throws IOException {
		serverSocketChannel = AsynchronousServerSocketChannel.open();
		serverSocketChannel.bind( new InetSocketAddress( serverPort));
	}

	public abstract void doAccepting();

}
