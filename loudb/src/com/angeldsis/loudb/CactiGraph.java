package com.angeldsis.loudb;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class CactiGraph extends Thread {
	public interface GraphElement {
		int getValue();
	}
	Map<String,GraphElement> elements = new HashMap<String,GraphElement>();
	private int port;
	public CactiGraph(int i) {
		port = i;
	}
	@Override public void run() {
		try {
			ServerSocket in = new ServerSocket(port);
			while (true) {
				Socket request = in.accept();
				DataOutputStream out = new DataOutputStream(request.getOutputStream());
				try {
					Runtime rt = Runtime.getRuntime();
					if (elements.size() > 0) {
						Iterator<String> i = elements.keySet().iterator();
						while (i.hasNext()) {
							String key = i.next();
							out.writeBytes(String.format("%s:%d ",key,elements.get(key).getValue()));
						}
					}
					out.writeBytes(String.format("total:%d max:%d free:%d\n",rt.totalMemory(),rt.maxMemory(),rt.freeMemory()));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				out.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void add(String string, GraphElement graphElement) {
		elements.put(string, graphElement);
	}
}
