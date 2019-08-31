import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.StringTokenizer;
/**
 * The class is for connecting handler and do all request operations
 * 
 * @author 180007800
 *
 */
public class ConnectionHandler extends Thread {
	// TODO Auto-generated method stub
	private Socket client; // socket representing TCP/IP connection to Client
	private String root;
	private int contentLength = 0;
	private String boundary = null; // multipart/form-data submit post fengefu

	public ConnectionHandler(Socket client, String root) {
		// TODO Auto-generated method stub
		this.client = client;
		this.root = root;
	}

	public void run() {
		// TODO Auto-generated method stub
		try {
			InputStream is = client.getInputStream(); // get data from client on this input stream
			OutputStream os = client.getOutputStream(); // to send data back to the client on this stream
			BufferedReader reader = new BufferedReader(new InputStreamReader(is)); // use buffered reader to read client
																					// data
			String line = reader.readLine(); // read requests from web client
			System.out.println("line = " + line);
			String resource = line.substring(line.indexOf('/'), line.lastIndexOf('/') - 5);
			String path = root + resource;
			System.out.println("the resource you request is: " + path);
			resource = URLDecoder.decode(resource, "UTF-8");
			String method = new StringTokenizer(line).nextElement().toString();
			System.out.println("method = " + method);
			if (!"GET".equals(method) && !"HEAD".equals(method) && !"POST".equals(method) && !"PUT".equals(method)
					&& !"DELETE".equals(method) && !"CONNECT".equals(method) && !"OPTIONS".equals(method)
					&& !"TRACE".equals(method)) {
				PrintStream writer = new PrintStream(os, true);
				writer.println("HTTP/1.1 501 Not Implemented"); // return respond and finish
				writer.println(); // finish header
				writer.close();
				LogJava logjava = new LogJava(" ", "HTTP/1.1 501 Not Implemented");
				logjava.logjava();
				cleanup(reader, is);
			}
			while ((line = reader.readLine()) != null) {
				if (line.equals("")) {
					break;
				}
				System.out.println("the Http Header is: " + line);
			}
			if ("get".equals(method.toLowerCase())) {
				doGet(reader, os, path);
			}
			if ("head".equals(method.toLowerCase())) {
				doHead(os, path);
			}
			if ("post".equals(method.toLowerCase())) {
				doPost(reader, os);
			}
			if (resource.endsWith(".mkv") || resource.endsWith(".jpg") || resource.endsWith(".jpeg")
					|| resource.endsWith(".rmvb")) {
				transferFileHandle(resource, client, os);
				cleanup(reader, is);
			} else {
				PrintStream writer = new PrintStream(os, true);
				writer.println("HTTP/1.1 404 Not found"); // return respond and finish
				writer.println(); // finish header
				writer.close();
				LogJava logjava = new LogJava(" HTTP NOT FOUND ", "HTTP/1.1 404 Not found");
				logjava.logjava();
				cleanup(reader, is);
			}
		} catch (IOException e) {
			System.out.println(e.getLocalizedMessage());
		}
	}

	private void doHead(OutputStream os, String path) {
		// TODO Auto-generated method stub
		File fileToSend = new File(path);
		if (fileToSend.exists() && !fileToSend.isDirectory()) {
			try {
				os = client.getOutputStream(); // to send data back to the client on this stream
				PrintStream writer = new PrintStream(os);
				writer.println("HTTP/1.1 200 OK"); // respond and finish
				writer.println("Content-Type: text/html");
				writer.println("Content-Length: " + fileToSend.length()); // return bytes
				writer.println(); // finish header
				writer.close();
				LogJava logjava = new LogJava("HEAD", "HTTP/1.1 200 OK");
				logjava.logjava();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			PrintStream writer = new PrintStream(os, true);
			writer.println("HTTP/1.1 404 Not Found"); // return respond and finish
			writer.println(); // finish header
			writer.close();
			LogJava logjava = new LogJava(" File not exits", "HTTP/1.1 404 Not Found");
			try {
				logjava.logjava();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void doGet(BufferedReader br, OutputStream os, String path) {
		// TODO Auto-generated method stub
		File fileToSend = new File(path);
		if (fileToSend.exists() && !fileToSend.isDirectory()) {
			try {
				os = client.getOutputStream(); // to send data back to the client on this stream
				PrintStream writer = new PrintStream(os);
				writer.println("HTTP/1.1 200 OK"); // respond and finish
				writer.println("Content-Type: text/html");
				writer.println("Content-Length: " + fileToSend.length()); // return bytes
				writer.println(); // finish header
				FileInputStream fis = new FileInputStream(fileToSend);
				byte[] buf = new byte[fis.available()];
				fis.read(buf);
				writer.write(buf);
				writer.flush();
				writer.close();
				fis.close();
				LogJava logjava = new LogJava("GET", "HTTP/1.1 200 OK");
				logjava.logjava();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else {
			PrintStream writer = new PrintStream(os, true);
			writer.println("HTTP/1.1 404 Not Found"); // return respond and finish
			writer.println(); // finish header
			writer.close();
			LogJava logjava = new LogJava(" File not exites", "HTTP/1.1 404 Not Found");
			try {
				logjava.logjava();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void doPost(BufferedReader reader, OutputStream os) {
		// TODO Auto-generated method stub
		try {
			String line = reader.readLine();
			while (line != null) {
				System.out.println(line);
				line = reader.readLine();
				if ("".equals(line)) {
					break;
				} else if (line.indexOf("Content-Length") != -1) {
					contentLength = Integer.parseInt(line.substring(line.indexOf("Content-Length") + 16));
				}
				// attach file
				else if (line.indexOf("multipart/form-data") != -1) {
					boundary = line.substring(line.indexOf("boundary") + 9);
					doMultiPart(reader, os);
					return;
				}
			}
			// continue get normal data(no attached file)
			System.out.println("begin reading posted data......");
			byte[] buf = {};
			int size = 0;
			if (contentLength != 0) {
				buf = new byte[contentLength];
				while (size < contentLength) {
					int c = reader.read();
					buf[size++] = (byte) c;
				}
				System.out.println("The data user posted: " + new String(buf, 0, size));
			}
			// send to browser
			PrintStream writer = new PrintStream(os);
			writer.println("HTTP/1.1 200 OK"); // respond and finish
			writer.println("Server: Sunpache 1.0");
			writer.println("Content-Type: text/html");
			writer.println("Accept-ranges: bytes");
			writer.println(); // finish header
			String body = "<html><head><title>test server</title></head><body><p>post ok:</p>"
					+ new String(buf, 0, size) + "</body></html>";
			System.out.println(body);
			writer.println(body);
			writer.flush();
			reader.close();
			writer.close();
			LogJava logjava = new LogJava("POST", "HTTP/1.1 200 OK");
			logjava.logjava();
			System.out.println("request complete.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void doMultiPart(BufferedReader reader, OutputStream os) {
		// TODO Auto-generated method stub
		try {
			DataInputStream reader1 = new DataInputStream(client.getInputStream());
			System.out.println("doMultiPart......");
			String line = reader.readLine();
			while (line != null) {
				System.out.println(line);
				line = reader.readLine();
				if ("".equals(line)) {
					break;
				} else if (line.indexOf("Content-Length") != -1) {
					contentLength = Integer.parseInt(line.substring(line.indexOf("Content-Length") + 16));
					System.out.println("contentLength: " + contentLength);
				} else if (line.indexOf("boundary") != -1) {
					// get multipart fengefu
					boundary = line.substring(line.indexOf("boundary") + 9);
				}
			}
			System.out.println("begin get data......");
			if (contentLength != 0) {
				// store all the content(attached file and others) to buf
				byte[] buf = new byte[contentLength];
				int totalRead = 0;
				int size = 0;
				while (totalRead < contentLength) {
					size = reader1.read(buf, totalRead, contentLength - totalRead);
					totalRead += size;
				}
				// use buf to create a string, use the string to compute the location of
				// attached file
				String dataString = new String(buf, 0, totalRead);
				System.out.println("the data use posted:/n" + dataString);
				int pos = dataString.indexOf(boundary);
				// the location of first attached file
				pos = dataString.indexOf("/n", pos) + 1;
				pos = dataString.indexOf("/n", pos) + 1;
				pos = dataString.indexOf("/n", pos) + 1;
				pos = dataString.indexOf("/n", pos) + 1;
				// the start location of attached file
				int start = dataString.substring(0, pos).getBytes().length;
				pos = dataString.indexOf(boundary, pos) - 4;
				// the end location of attached file
				int end = dataString.substring(0, pos).getBytes().length;
				// find filename
				int fileNameBegin = dataString.indexOf("filename") + 10;
				int fileNameEnd = dataString.indexOf("/n" + fileNameBegin);
				String fileName = dataString.substring(fileNameBegin, fileNameEnd);
				if (fileName.lastIndexOf("//") != -1) {
					fileName = fileName.substring(fileName.lastIndexOf("//") + 1);
				}
				fileName = fileName.substring(0, fileName.length() - 2);
				OutputStream fileOut = new FileOutputStream("c://" + fileName);
				fileOut.write(buf, start, end - start);
				fileOut.close();
				fileOut.close();
			}
			PrintStream writer = new PrintStream(os);
			writer.println("HTTP/1.1 200 OK"); // respond and finish
			writer.println("Server: Sunpache 1.0");
			writer.println("Content-Type: text/html");
			writer.println("Accept-ranges: bytes");
			writer.println(); // finish header
			String body = "<html><head><title>test server</title></head><body><p>post ok:</p></body></html>";
			writer.println(body);
			writer.flush();
			reader.close();
			LogJava logjava = new LogJava("POST", "HTTP/1.1 200 OK");
			logjava.logjava();
			System.out.println("request complete. ");
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void transferFileHandle(String path, Socket clinet, OutputStream os) {
		File fileToSend = new File(path);
		if (fileToSend.exists() && !fileToSend.isDirectory()) {
			try {
				os = client.getOutputStream(); // to send data back to the client on this stream
				PrintStream writer = new PrintStream(os);
				writer.println("HTTP/1.1 200 OK"); // respond and finish
				writer.println("Content-Type:application/binary");
				writer.println("Content-Length: " + fileToSend.length()); // return bytes
				writer.println(); // finish header
				writer.close();
				LogJava logjava = new LogJava("GET Binary Images", "HTTP/1.1 200 OK");
				logjava.logjava();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void cleanup(BufferedReader reader, InputStream is) {
		// TODO Auto-generated method stub
		System.out.println("ConnectionHandler: ... cleaning up and exiting ... ");
		try {
			reader.close();
			is.close();
			client.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
}
