package dht;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Scanner;

public class Chord {
	
	private DHT<String> new_dht;
	
	public Chord(String dht_name) {
		try {
			new_dht = new NodeImpl<>(dht_name);
			new NodeImpl<>("Node1", (Node<String>)new_dht);
			new NodeImpl<>("Node2", (Node<String>)new_dht);
			new NodeImpl<>("Node3", (Node<String>)new_dht);
			handle_msg();
		} catch (RemoteException e) {
			System.err.println("Fail to create the Chord.");
		}
	}
	
	public Chord(String name, String rName, String host, int port) {
		try {
			new_dht = new NodeImpl<>(name, host, port, rName);
			handle_msg();
		} catch (RemoteException e) {
			System.err.println("RemoteException");
//			e.printStackTrace();
		} catch (NotBoundException e) {
			System.err.println("RMI Registry error.");
//			e.printStackTrace();
		}
	}
	
	/**
	 * A simple console-based UI to perform some operations on the DHT.
	 */
	private void handle_msg() {
		Scanner scan = new Scanner(System.in);
		String input = scan.nextLine();
		while(!input.equals("exit")) {
			try {
				String[] inputs = input.split(" ");
				switch(inputs[0]) {
				case "put":
					String key = inputs[1];
					String value = inputs[2];
					new_dht.put(key, value);
					break;
				case "get":
					key = inputs[1];
					System.out.println("Got from dht: " + new_dht.get(key));
					break;
				case "remove":
					key = inputs[1];
					new_dht.remove(key);
					System.out.println("Removed " + key);
					break;
				case "bench":
					Benchmark.bench(Integer.parseInt(inputs[1]), Integer.parseInt(inputs[2]));
					break;
				case "benchc":
					Benchmark.benchConcurrency(Integer.parseInt(inputs[1]));
					break;
				case "list":
					System.out.println("Items:");
					for(String s : new_dht.listAll())
						System.out.println(s);
					break;
				default:
					System.out.println("Usage:");
					System.out.println("put key value");
					System.out.println("get key");
					System.out.println("remove key");
					System.out.println("bench nodes values");
					System.out.println("benchc nodes");
					System.out.println("list");
					System.out.println("exit");
				}
			} catch (Exception e) {
				System.out.println("Something went wrong.. a bad input?");
			}
			input = scan.nextLine();
		}
		new_dht.leave();
		scan.close();
	}
	
	/**
	 * Create/join a DHT and let the user interact with it.
	 * @param args - A single argument [name] will create a local dht. Arguments [localname, remotename, remotehost, remoteport] is used to connect to an existing dht.
	 */
	public static void main(String[] args) {
		if(args.length == 1) {
			String name = args[0];
			new Chord(name);
		}
		else if(args.length == 4) {
			String name = args[0];
			String rName = args[1];
			String rHost = args[2];
			int port = Integer.parseInt(args[3]);
			new Chord(name, rName, rHost, port);
		}
		else
			System.out.println("Expected arguments: [localname] or [localname, remotename, remotehost, remoteport]");
	}
}
