package dht;

import java.util.Scanner;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;


public class Chord {
	
	private KeyValueTable<String> new_dht;
	
	public Chord(String dht_name) {
		try {
			new_dht = new NodeClass<>(dht_name);
			new NodeClass<>("Node1", (NodeClass<String>)new_dht);
			new NodeClass<>("Node2", (NodeClass<String>)new_dht);
			new NodeClass<>("Node3", (NodeClass<String>)new_dht);
			System.out.println("Chord Create Success!");
			handle_msg();
		} catch (RemoteException e) {
			System.err.println("Fail to create the Chord.");
		}
	}
	
	public Chord(String node_name, String existed_node, String host, int port) {
		try {
			new_dht = new NodeClass<>(node_name, host, port, existed_node);
			System.out.println("Node Join Success!");
			handle_msg();
		} catch (RemoteException e) {
			System.err.println("Error: Remote Exception.");
		} catch (NotBoundException e) {
			System.err.println("Error: RMI Registry error.");
		}
	}
	

	private void handle_msg() {
		System.out.println("Please enter command line (add/search/del/listAll): ");
		Scanner scan = new Scanner(System.in);
		String input = scan.nextLine();
		
		while(input != "exit") {
			String[] cmd = input.split(" ");
			String action = cmd[0];
			try {
				switch(action) {
					case "add":
						String key = cmd[1];
						String value = cmd[2];
						new_dht.create(key, value);
						System.out.println("key/value: " + key + " / " + value + " has been added.");
						System.out.println("=============================");
						break;
					case "search":
						key = cmd[1];
						System.out.println("Search value for " + key + ": " + new_dht.search(key));
						System.out.println("=============================");
						break;
					case "del":
						key = cmd[1];
						new_dht.delete(key);
						System.out.println(key + " has been removed.");
						System.out.println("=============================");
						break;
	//				case "bench":
	//					Benchmark.bench(Integer.parseInt(inputs[1]), Integer.parseInt(inputs[2]));
	//					break;
	//				case "benchc":
	//					Benchmark.benchConcurrency(Integer.parseInt(inputs[1]));
	//					break;
					case "listAll":
						System.out.println("Stored Values:");
						for(String s : new_dht.listAllKeyValue())
							System.out.println(s);
						System.out.println("=============================");
						break;
					default:
						System.out.println("Please enter command line (new/search/delete/showAll): ");
				}
			} catch (Exception e) {
				System.out.println("Can't read command line, please try again.");
			}
			input = scan.nextLine();
		}
		new_dht.leave();
		scan.close();
	}
	
	public static void main(String[] args) {
		if(args.length == 1) {
			String dht_name = args[0];
			new Chord(dht_name);
		}
		else if(args.length == 4) {
			String node_name = args[0];
			String existed_node = args[1];
			String chord_ip = args[2];
			int chord_port = Integer.parseInt(args[3]);
			new Chord(node_name, existed_node, chord_ip, chord_port);
		}
		else
			System.out.println("Incorrect input args.");
	}
}
