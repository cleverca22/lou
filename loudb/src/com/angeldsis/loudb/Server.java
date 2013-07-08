package com.angeldsis.loudb;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocketFactory;

import com.angeldsis.louapi.Log;
import com.angeldsis.loudb.CactiGraph.GraphElement;

public class Server {
	private static final String TAG = "Server";
	ServerSocket socket;
	ArrayList<Client> clients;
	Server() throws IOException {
		//System.setProperty("javax.net.debug", "ssl");
		System.setProperty("javax.net.ssl.keyStore", "host.jks");
		System.setProperty("javax.net.ssl.keyStorePassword", "password");
		System.setProperty("sun.security.ssl.allowUnsafeRenegotiation", "true");
		System.setProperty("javax.net.ssl.trustStore", "host.jks");
		ServerSocketFactory ssocketFactory = SSLServerSocketFactory.getDefault();
		socket = ssocketFactory.createServerSocket(8080);
		clients = new ArrayList<Client>();
	}
	void run() {
		while (true) {
			try {
				Socket s = socket.accept();
				synchronized (clients) {
					clients.add(new Client(s,this));
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
			}
		}
	}
	public void removeClient(Client client) {
		synchronized (clients) {
			clients.remove(client);
			Log.v(TAG,String.format("clients: %d",clients.size()));
		}
	}
	public void addGraphs(CactiGraph cg) {
		cg.add("clients",new GraphElement() {
			public int getValue() {
				return clients.size();
			}
		});
		cg.add("threads",new GraphElement() {
			public int getValue() {
				return ThreadPool.getInstance().getCount();
			}
		});
		cg.add("idledbs",new GraphElement() {
			public int getValue() {
				return ConnectionPool.getIdleCount();
			}
		});
	}
}
