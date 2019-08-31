import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
/**
 * The class uses socket to connect to the server
 * 
 * @author 180007800
 *
 */
public class WebServerMain {
	// TODO Auto-generated method stub
	private static final int NUM_THREADS = 50 ; //线程池线程数量
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		if (args.length < 2) {
			System.out.println("Usage: java WebServerMain <document_root> <port>");
			System.exit(1);
		}
		String root = args[0];
		int port = Integer.parseInt(args[1]);

		ServerSocket server = null;
		Socket socket = null;

		try {
			server = new ServerSocket(port);
			while (true) {
				socket = server.accept();
				ConnectionHandler ch = new ConnectionHandler(socket, root);
				
				ch.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
