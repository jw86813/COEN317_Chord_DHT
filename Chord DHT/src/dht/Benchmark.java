package dht;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class Benchmark {
	
	/**
	 * Do some concurrency tests. Concurrently create n nodes, put n values in each node, and get n values from each node.
	 * @param n
	 */
	@SuppressWarnings("unchecked")
	public static void benchConcurrency(int n) {
		try {
			long start, end;
			KeyValueTable<String> dht = new NodeClass<>("table");
			
			System.out.println("Creating " + n + " nodes.");
			start = System.currentTimeMillis();
			List<KeyValueTable<String>> nodes = createNodes(n, (NodeStruct<String>) dht);
			end = System.currentTimeMillis();
			System.out.println("Time: " + (int)(end-start));
			
			System.out.println("Putting " + n + " values.");
			start = System.currentTimeMillis();
			List<String> keys = benchPut(nodes);
			end = System.currentTimeMillis();
			System.out.println("Time: " + (int)(end-start));
			
			System.out.println("Getting " + keys.size() + " random values from DHT");
			start = System.currentTimeMillis();
			benchGet(nodes, keys);
			end = System.currentTimeMillis();
			System.out.println("Time: " + (int)(end-start));
			
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Create n nodes that will be linked to the dht network.
	 * @param n
	 * @param dht
	 * @return The list of nodes.
	 * @throws RemoteException
	 */
	private static List<KeyValueTable<String>> createNodes(int n, final NodeStruct<String> dht) throws RemoteException {
		final List<KeyValueTable<String>> nodes = new ArrayList<>();
		for(int i=0; i<n; i++) {
//			nodes.add(new Node<>("node"+i, dht));
		}
		return nodes;
	}
	
	/**
	 * Concurrently put one value in each node.
	 * @param dhts - A list of nodes/dhts to insert into.
	 * @return a list of keys to the values inserted in the table.
	 * @throws InterruptedException
	 */
	private static List<String> benchPut(List<KeyValueTable<String>> dhts) throws InterruptedException {
		Random rand = new Random();
		final List<String> keys = new ArrayList<>();
		final Lock write = new ReentrantLock();
		
		final AtomicInteger finnishedWork = new AtomicInteger();
		int workStarted = 0;
		
		for(final KeyValueTable<String> dht : dhts) {
			final String key = "key" + rand.nextInt();
			new Thread(){
				public void run() {
					dht.create(key, "v");
					write.lock();
					keys.add(key);
					finnishedWork.incrementAndGet();
					write.unlock();
				}
			}.start();
			workStarted++;
		}
		while(finnishedWork.get() != workStarted);
//			System.out.println("workStarted=" + workStarted + ", finnishedWork=" + finnishedWork);
//		System.out.println("Done! workStarted=" + workStarted + ", finnishedWork=" + finnishedWork);
		return keys;
	}
	
	/**
	 * Concurrently get a random value from each node/dht.
	 * @param dhts - A list of nodes/dhts in the network.
	 * @param keys - A list of keys to randomly chose from.
	 */
	private static void benchGet(List<KeyValueTable<String>> dhts, List<String> keys) {
		Random rand = new Random();
		
		final AtomicInteger finnishedWork = new AtomicInteger();
		int workStarted = 0;
		
		for(final KeyValueTable<String> dht : dhts) {
			final String key = keys.get(rand.nextInt(keys.size()));
			new Thread() {
				public void run() {					
					dht.search(key);
					finnishedWork.incrementAndGet();
				}
			}.start();
			workStarted++;
		}
		
		while(finnishedWork.get() != workStarted);
	}

	/**
	 * Do some standard performance tests. Create nodes, insert and get n values.
	 * @param nodes - Number of nodes in the network to evaluate.
	 * @param n - Number of values to put/get.
	 */
	@SuppressWarnings("unchecked")
	public static void bench(int nodes, int n) {
		try {
			long start, end;
			String[] keys = new String[n];
			for(int i=0; i<n; i++)
				keys[i] = "key" + i;
			
			System.out.println("Starting test with " + nodes + " nodes and " + n + " operations.");
			
			start = System.currentTimeMillis();
			KeyValueTable<String> dht = new NodeClass<>("dht");
			for(int i=0; i<nodes; i++)
				new NodeClass<>("dht"+i, (NodeClass<String>)dht);
			end = System.currentTimeMillis();
			System.out.println("Create time: " + (int)(end-start));
			
			
			start = System.currentTimeMillis();
			for (String key : keys)
				dht.create(key, "value");
			end = System.currentTimeMillis();
			System.out.println("Put time: " + (int)(end-start));
			
			
			start = System.currentTimeMillis();
			for (String key : keys)
				dht.search(key);
			end = System.currentTimeMillis();
			System.out.println("Get time: " + (int)(end-start));
			
			
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

}
