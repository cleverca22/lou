package com.angeldsis.loudb;

import java.io.IOException;

public class loudb {
	public static class Record {

		protected int totalMade;
		protected long totalSize;
		protected String desc;

	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DbLink.init();
		ThreadPool.init();
		ThreadPool p = ThreadPool.getInstance();
		ConnectionPool.init();
		Logger.init();
		Worlds.init();
/*		Transaction t = new Transaction() {
			@Override
			void internalRun() throws SQLException {
				Statement s = link.con.createStatement();
				ResultSet r = s.executeQuery("SELECT playerid,name FROM player limit 10");
				while (r.next()) {
					System.out.println("id:"+r.getInt(1));
					System.out.println("name:"+r.getString(2));
				}
			}};*/
		CactiGraph cg = new CactiGraph(27969);
		cg.start();
		/*final HashMap<String,Record> records = new HashMap<String,Record>();
		AllocationRecorder.addSampler(new Sampler() {
			private int count = 0;
			@Override
			public void sampleAllocation(int count, String desc, Object newObj, long size) {
				Record rec = records.get(desc);
				if (rec == null) {
					rec = new Record();
					rec.desc = desc;
					records.put(desc, rec);
				}
				count += size;
				rec.totalMade++;
				rec.totalSize += size;
				//System.out.println("I just allocated the object of type " + desc + " whose size is " + size);
				if (count > (1024 * 32)) {
					System.out.println("\ndoing a dump");
					Iterator<Record> i = records.values().iterator();
					while (i.hasNext()) {
						rec = i.next();
						if (rec.totalSize < (1024*8)) continue;
						System.out.println(rec.totalMade+"\t"+rec.totalSize+"\t"+(rec.totalSize/rec.totalMade)+"\t"+rec.desc);
					}
					count = 0;
				}
				//if (count != -1) { System.out.println("It's an array of size " + count); }
			}
		});*/
		try {
			Server s = new Server();
			s.addGraphs(cg);
			s.run();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		p.shutdown();
	}
}
