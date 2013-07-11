package com.angeldsis.loudb;

import java.util.ArrayList;

public class ThreadPool {
	private static ThreadPool self;
	private ArrayList<MyThread> threads;
	//private ArrayList<MyThread> busy = new ArrayList<MyThread>();
	private static int min_size = 5;
	ThreadPool() {
		threads = new ArrayList<MyThread>();
		checkLimits();
	}
	public int getCount() {
		return threads.size();
	}
	private void checkLimits() {
		while (threads.size() < min_size) threads.add(new MyThread());
	}
	public static ThreadPool getInstance() {
		return self;
	}
	public void post(Runnable r) {
		if (threads.size() == 0) {
			System.out.println("first pass failed, total threads "+threads.size());
			Exception e = new Exception();
			e.printStackTrace();
			MyThread t = new MyThread();
			synchronized(t) {
				//busy.add(t);
				t.queue.add(r);
				t.interrupt();
				return;
			}
		}
		// first pass, any idle thread
		synchronized(this) {
			MyThread t = threads.remove(0);
			//System.out.println(String.format("threads idle:%d posting to thread "+t.getId(),threads.size()));
			t.queue.add(r);
			//busy.add(t);
			t.interrupt();
			return;
		}
	}
	public static void init() {
		self = new ThreadPool();
	}
	class MyThread extends Thread {
		boolean cont;
		ArrayList<Runnable> queue;
		MyThread() {
			cont = true;
			queue = new ArrayList<Runnable>();
			start();
		}
		@Override public void run() {
			while (cont) {
				//System.out.println(this.getId()+" start");
				try {
					synchronized (ThreadPool.this) {
						if (!threads.contains(this)) threads.add(this);
					}
					sleep(60000);
				} catch (InterruptedException e) {
					//System.out.println("who woke me!");
				}
				Runnable r;
				while (queue.size() > 0) {
					r = queue.remove(0);
					r.run();
				}
				//System.out.println(this.getId()+" stop");
			}
		}
	}
	public void shutdown() {
		for (MyThread t : threads) {
			t.cont = false;
			t.interrupt();
		}
	}
}
