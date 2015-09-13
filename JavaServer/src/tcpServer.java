import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.io.File;




public class tcpServer {

	private static ServerSocket serverSock;  // store server socket
	private static Socket socket; // store client socket
	private static Thread sendThr;
	private static List<Socket> s = new ArrayList<Socket>();  //store all clients socket
	private static List<BufferedReader> ins = new ArrayList<BufferedReader>();  //store all clients socket
	private static List<PrintWriter> outs = new ArrayList<PrintWriter>();  //store all clients socket
	private static BufferedReader infile;
	private static int len;
	private static final int BUF_SIZE = 0;
	private static HashSet<PrintWriter> writers = new HashSet<PrintWriter>();
	
	
	public static void main(String[] args) {

		int clientCount = 0;
		int serverPort = Integer.parseInt(args[0]);
		int maxClients = Integer.parseInt(args[1]);

		try {
			serverSock = new ServerSocket(serverPort);
			String addr = serverSock.toString();
			System.out.println(addr);
			System.out.println( "The server is ready..." ) ;
		} catch (IOException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}

		try {
			while (clientCount < maxClients) {
				socket = serverSock.accept();
				// Connect to a new client
				//new sendThread(socket).start();
				clientCount++;

				s.add(socket);  // Add to global array list

				PrintWriter out =
				        new PrintWriter(socket.getOutputStream(), true);
				outs.add(out);

				// Declare input stream
				BufferedReader in = new BufferedReader(
						new InputStreamReader(socket.getInputStream()));
				ins.add(in);

	/*			
				File f = new File("./abc.mp3");

				// returns the length in bytes
				len = (int)f.length();

				// Open the mp3 file
				infile = new BufferedReader(new FileReader("./abc.mp3"));
*/
				new sendThread(socket).start();
				//sendThr.start();
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	private static class sendThread extends Thread {
		

			String message = "Hello from Server!";
			String recvMesg = null;
			int index = 0;
			private Socket socket;
			
			public sendThread(Socket socket) {
	            this.socket = socket;
	        }
			public void run() {
			try{
				for (int i = 0; i < s.size(); i++)
				{
					if(s.get(i) == socket)
					{
						index = i;
						break;
					}
				}

				outs.get(index).println(message);

				// Send the mp3 file
/*
				int sent = 0;
				int wbyte = 0;
				char[] cbuf = new char[BUF_SIZE];
		         // reads characters to buffer, offset , size
				while(sent < len)
				{
					if((len - sent) > BUF_SIZE)
						wbyte = BUF_SIZE;
					else
						wbyte = len - sent;

					infile.read(cbuf, sent, wbyte);
					sent += wbyte;
					outs.get(index).write(cbuf);
				}
				// MAke sure to send out the whole file
				outs.get(index).flush();
				outs.get(index).close();
	*/			
	
				// Declare input stream
//				BufferedReader inpFromClient = new BufferedReader(
//						new InputStreamReader(socket.getInputStream()));

				
	//			BufferedReader in;
				
				while (true) {
					//System.out.println(index);
					recvMesg = ins.get(index).readLine();
					//System.out.println("hello");
					//System.out.println(recvMesg);
					
					if(recvMesg.equalsIgnoreCase("\\disconnect")) {
						System.out.println("Socket closed!");
						break;
					} else if (recvMesg.equals("pause")){ 
						// Server wants to broadcast pause message from one client
						// Current client is socket
						System.out.println("get pause");

						for (int i = 0; i < s.size(); i++)
						{
							if(s.get(i) != socket)
							{
								// Replay the pause messages
								//out = new PrintWriter(s.get(i).getOutputStream(), true);
								message = "pause";
								outs.get(i).println(message);
								System.out.println("Send out a pause to client");
							}
						}
					} else if (recvMesg.equals("restart")){ 
						// Server wants to broadcast pause or restart messages from one client
						// Current client is socket
						System.out.println("get restart");
						
						for (int i = 0; i < s.size(); i++)
						{
							if(s.get(i) != socket)
							{
								// Replay the restart messages
								//out = new PrintWriter(s.get(i).getOutputStream(), true);
								message = "restart";
								outs.get(i).println(message);
								System.out.println("Send out a restart to client");
							}
						}
					} else if (recvMesg.equals("start")){ 
						// Server wants to broadcast pause or restart messages from one client
						// Current client is socket
						
						System.out.println("get start");
						for (int i = 0; i < s.size(); i++)
						{
							//if(s.get(i) != socket)
							{
								// Replay the restart messages
								//out = new PrintWriter(s.get(i).getOutputStream(), true);
								message = "start";
								outs.get(i).println(message);
								System.out.println("Send out a start to client");
							}
						}
					}
					else {
						System.out.println(socket.getInetAddress() + " : "
								+ socket.getPort() + " : " + recvMesg);
					}
					
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
