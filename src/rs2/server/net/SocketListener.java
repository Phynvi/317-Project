package rs2.server.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import rs2.server.Server;

/**
 * @author John (deathschaos9)
 * 
 */
public class SocketListener {
	private ServerSocket serverSocket = null;
	private Socket clientSocket = null;

	public SocketListener(int i) {
		try {
			System.out.print("#pre#Starting connection listener... ");
			serverSocket = new ServerSocket(i, 1, null);
			System.out.print("#new#done.");
			do {
				clientSocket = serverSocket.accept();
				clientSocket.setTcpNoDelay(true);
				String hostName = clientSocket.getInetAddress().getHostName();
				int port = clientSocket.getPort();
				System.out.println("#pre#Connection accepted from " + hostName + ":" + port);
				Server.getHandler().newPlayer(clientSocket, hostName);
				try {
					Thread.sleep(100);
				} catch (Exception e) {
					
				}
			} while (true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
