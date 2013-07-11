package com.angeldsis.loudb;

import java.sql.SQLException;
import java.util.ArrayList;

public class ConnectionPool {
	static ArrayList<DbLink> idle_links;
	static ArrayList<DbLink> busy_links = new ArrayList<DbLink>();
	static private final int min_spare = 5;
	static private boolean checking = false;
	static private Runnable checker = new Runnable() {
		@Override
		public void run() {
			System.out.println("checking!");
			checking = false;
			checkLimits(true);
		}};
	public synchronized static DbLink getLink() throws SQLException {
		if (idle_links.size() == 0) checkLimits(false); // emergency catch
		DbLink link = idle_links.remove(0);
		// if this connection is bad, simply drop the references, letting GC clean it up
		// this can only recurse up to max_space times, unfinished code keeps idle_links that small
		if (link.con.isClosed()) return getLink();
		busy_links.add(link);
		//System.out.println(String.format("sockets idle:%d busy:%d checking:%b",idle_links.size(),busy_links.size(),checking));
		postCheckLimits();
		return link;
	}
	public synchronized static int getIdleCount() {
		return idle_links.size();
	}
	private synchronized static void postCheckLimits() {
		if (checking) return;
		if (idle_links.size() > (min_spare - 2)) return;
		checking = true;
		ThreadPool.getInstance().post(checker);
	}
	public static void restore(DbLink link) {
		idle_links.add(link);
		busy_links.remove(link);
	}
	public static void init() {
		idle_links = new ArrayList<DbLink>();
		checkLimits(false);
	}
	private static void checkLimits(boolean background) {
		try {
			while (idle_links.size() < min_spare) {
				long start = System.currentTimeMillis();
				DbLink link = new DbLink();
				idle_links.add(link);
				System.out.println("creating new link#"+link.id);
				long end = System.currentTimeMillis();
				long diff = end-start;
				if (!background) {
					Exception e = new Exception();
					e.printStackTrace();
					System.out.println("spent:"+diff);
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
