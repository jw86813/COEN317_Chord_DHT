package dht;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class NodeClass<E> extends UnicastRemoteObject implements NodeStruct<E>, KeyValueTable<E> {

//	private static final long serialVersionUID = 7837010474371220959L;
	
	private String node_name;
	private String node_id;
	
	private NodeStruct<E> predecessor;
	private NodeStruct<E> successor;

	private HashMap<String, E> key_value_table = new HashMap<>();
	private Map<String, NodeStruct<E>> finger_table = new LinkedHashMap<>();
	
	private static final int N = 1048576; //1MB
	public static final int DEFAULT_PORT = 8001;
	
	public NodeClass(String node_name) throws RemoteException {
		
		this.node_name = node_name;
		node_id = generateID(node_name, N);
		successor = this;
		predecessor = this;
		Registry registry;
		
		try {
			registry = LocateRegistry.createRegistry(DEFAULT_PORT);
			System.out.println("Create Registry");
		} catch (Exception e) {
			registry = LocateRegistry.getRegistry(DEFAULT_PORT);
		}
		registry.rebind(node_name, this);
	}
	
	public NodeClass(String node_name, NodeStruct<E> existed_node) throws RemoteException {
		this(node_name);
		join(existed_node);
	}
	
	@SuppressWarnings("unchecked")
	public NodeClass(String name, String ip, int port, String existed_name) throws RemoteException, NotBoundException {
		this(name);
		Registry registry = LocateRegistry.getRegistry(ip, port);
		NodeStruct<E> existed_node = (NodeStruct<E>) registry.lookup(existed_name);
		join(existed_node);
	}
	
	@Override
	public void join(NodeStruct<E> existed_node) {
		try {
			boolean joined = false;
			while(!joined) {
				
				NodeStruct<E> pred = existed_node.getPredecessor();
				String curr_key = existed_node.getKey();
				String pred_key = pred.getKey();
				
				if(isBetween(node_id, pred_key, curr_key)) {
					pred.setAsSuccessor(this);
					existed_node.setAsPredecessor(this);
					setAsSuccessor(existed_node);
					setAsPredecessor(pred);
					
					Map<String, E> get_from_pred = successor.Reallocate(pred_key, node_id);
					for(String k : get_from_pred.keySet())
						key_value_table.put(k, get_from_pred.get(k));
					
					System.out.println("Node joined into right position.");
					System.out.println("Current Node ID: " + node_id);
					System.out.println("Predecessor's Node ID: " + pred_key);
					System.out.println("Successor's Node ID: " + curr_key);
					joined = true;
					
				} else
					existed_node = existed_node.getSuccessor();
			}
			
			updateRouting();
			System.out.println("Routing Table Updated.\nNode List: " + getAllNodes());
			
		} catch(RemoteException e) {
			System.err.println("Joining Error on Node " + existed_node);
			e.printStackTrace();
		}
	}
	
	public void leave() {
		try {
			for(String k : key_value_table.keySet())
				successor.addKeyValue(k, key_value_table.get(k));
			System.out.println("Leaving Node " + node_name + ". Success Passing its data to it's successor.");
			
			successor.setAsPredecessor(predecessor);
			predecessor.setAsSuccessor(successor);
			successor = this;
			predecessor = this;
			updateRouting();
			System.out.println("Node left successfully");
			
		} catch(RemoteException e) {
			System.err.println("Error leaving.");
			e.printStackTrace();
		}
	}

	@Override
	public Map<String, E> Reallocate(String pred_key, String new_pred_key) throws RemoteException {
		Map<String, E> trans_map = new LinkedHashMap<>();
		List<String> key_list = new ArrayList<String>(key_value_table.keySet());
		for(String k : key_list)
			if(isBetween(k, pred_key, new_pred_key))
				trans_map.put(k, key_value_table.remove(k));
		// successor store data from pred_id to its id, now transfer data id from pred_id to new_pred_id for new_pred to take care
		return trans_map;
	}
	
	public static String generateID(String name, int space) {
		String sha1 = null;
		try {
			MessageDigest md = MessageDigest.getInstance("SHA1");
			byte[] result = md.digest(name.getBytes());
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < result.length; i++) {
				sb.append(Integer.toString(result[i], 2).substring(1));
			}
			sha1 = sb.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		int characters = (int) (Math.log(space)/Math.log(2));
		characters = Math.min(characters, sha1.length());
		return sha1.substring(sha1.length()-characters-1, sha1.length());
	}
	
	@Override
	public String toString() {
		return node_name;
	}
	
	@Override
	public String getKey() throws RemoteException {
		return node_id;
	}

	@Override
	public NodeStruct<E> getSuccessor() throws RemoteException {
		return successor;
	}

	@Override
	public NodeStruct<E> getPredecessor() throws RemoteException {
		return predecessor;
	}

	@Override
	public void setAsSuccessor(NodeStruct<E> new_succ) throws RemoteException {
		successor = new_succ;
	}

	@Override
	public void setAsPredecessor(NodeStruct<E> new_pred) throws RemoteException {
		predecessor = new_pred;
	}
	
	@Override
	public boolean equals(Object compare_node) {
		if(compare_node instanceof NodeStruct<?>)
			try {
				return node_id.equals(((NodeStruct<?>) compare_node).getKey());
			} catch (RemoteException e) {
				e.printStackTrace();
				return false;
			}
		else
			return false;
	}
	
	@Override
	public void updateRouting() {
		try {
			List<NodeStruct<E>> node_list = getAllNodes();
			for(NodeStruct<E> node : node_list)
				node.updateFingerTable(node_list);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

//	@Override
//	public void probe(String key, int count) throws RemoteException {
//		if(this.node_id.equals(key) && count > 0) {
//			System.out.println("Probe returned after " + count + " hops.");
//		} else {
//			System.out.println(node_name + ": Forwarding probe to " + successor);
//			successor.probe(key, count+1);
//		}
//	}

	@Override
	public NodeStruct<E> searchNodeFromKey(String key) throws RemoteException {
		String pred_key = predecessor.getKey();
		if(isBetween(key, pred_key, getKey())) {
			return this;
		}
		else {
			String[] key_list = {};
			key_list = finger_table.keySet().toArray(key_list);
			for(int i=0; i<(key_list.length-1); i++) {
				String curr_key = key_list[i];
				String next_key = key_list[i+1];
				if(isBetween(key, curr_key, next_key)) {
					NodeStruct<E> curr_node = finger_table.get(curr_key);
					NodeStruct<E> target_node = curr_node.getSuccessor();
					return target_node.searchNodeFromKey(key);
				}
			}
			return finger_table.get(key_list[key_list.length-1]).getSuccessor().searchNodeFromKey(key);
		}
	}
	
	@Override
	public E getValueByKey(String key) throws RemoteException {
		return key_value_table.get(key);
	}
	
	@Override
	public void addKeyValue(String key, E value) throws RemoteException {
		key_value_table.put(key, value);
	}
	
	@Override
	public void removeKeyValue(String key) throws RemoteException {
		key_value_table.remove(key);
	}

	@Override
	public E search(String key) {
		try {
			String query_id = generateID(key, N);
			NodeStruct<E> node = searchNodeFromKey(query_id);
			return node.getValueByKey(query_id);
		} catch (RemoteException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void create(String key, E value) {
		try {
			String key_id = generateID(key, N);
			NodeStruct<E> target_node = searchNodeFromKey(key_id);
			target_node.addKeyValue(key_id, value);
			NodeStruct<E> rep_node = target_node.getSuccessor();
			int rep_level = 2;
			while (!rep_node.equals(target_node) && rep_level > 0) {
				rep_node.addKeyValue(key_id, value);
				rep_node = rep_node.getSuccessor();
				rep_level --;
			}
			
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void delete(String key) {
		try {
			String key_id = generateID(key, N);
			NodeStruct<E> node = searchNodeFromKey(key_id);
			node.removeKeyValue(key_id);
			node = node.getSuccessor();
			node.removeKeyValue(key_id);
			node = node.getSuccessor();
			node.removeKeyValue(key_id);
		} catch (RemoteException e) {
//			e.printStackTrace();
		}
	}

	@Override
	public List<String> listAllKeyValue() {
		NodeStruct<E> curr_node = this;
		ArrayList<String> all_data_in_node = new ArrayList<>();
		try {
			do {
				for(E values : curr_node.getAllValues())
					all_data_in_node.add(values.toString());
				curr_node = curr_node.getSuccessor();
			} while(!this.equals(curr_node));
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return all_data_in_node;
	}

	@Override
	public List<E> getAllValues() throws RemoteException {
		ArrayList<E> value_lists = new ArrayList<>(key_value_table.values());
		return value_lists;
	} // better way to combine with upper

	
	private List<NodeStruct<E>> getAllNodes() {
		ArrayList<NodeStruct<E>> node_list = new ArrayList<>();
		try {
			NodeStruct<E> curr_node = this;
			do {
				node_list.add(curr_node);
				curr_node = curr_node.getSuccessor();
			} while(!this.equals(curr_node));
		} catch(RemoteException e){
			System.err.println("Error finding all nodes.");
		}
		Collections.sort(node_list, new Comparator<NodeStruct<E>>() {
			@Override
			public int compare(NodeStruct<E> node_1, NodeStruct<E> node_2) {
				try {
					int key_1 = Integer.parseInt(node_1.getKey(), 2);
					int key_2 = Integer.parseInt(node_2.getKey(), 2);
					
					if(key_1 > key_2) {
						return 1;
					}
					else if(key_1 < key_2) {
						return -1;
					}
					else {
						return 0;
					}
				}catch(RemoteException e) {
					return 0;
				}
			}
		});
		return node_list;
	}
	
	public void updateFingerTable(List<NodeStruct<E>> nodes) {
		Map<String, NodeStruct<E>> temp_ft = new LinkedHashMap<>();
		temp_ft.put(node_id, this);
		try {
			int my_index = nodes.indexOf(this);
			
			for(int i=1; i<nodes.size(); i = i*2) {
				int node_index = (my_index + i) % nodes.size();
				NodeStruct<E> n = nodes.get(node_index);
				temp_ft.put(n.getKey(), n);
			}
		} catch(RemoteException e) {
			e.printStackTrace();
		}
		this.finger_table = temp_ft;
	}
	
	public static boolean isBetween(String key_id, String upbound, String lowbound) {
		float key = Float.parseFloat(key_id);
		float upper = Float.parseFloat(upbound);
		float lower = Float.parseFloat(lowbound);
		
		if(upper > lower) {
			return key > upper || key <= lower;
		} else if(upper < lower) {
			return key > upper && key <= lower;
		}
		else {
			return true;
		}
	}
}
	

