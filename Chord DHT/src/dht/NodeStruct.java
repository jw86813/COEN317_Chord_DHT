package dht;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;


public interface NodeStruct<V> extends Remote {
	
	public String getKey() throws RemoteException;

	public NodeStruct<V> getSuccessor() throws RemoteException;
	public NodeStruct<V> getPredecessor() throws RemoteException;
	
	public void setAsSuccessor(NodeStruct<V> succ) throws RemoteException;
	public void setAsPredecessor(NodeStruct<V> pred) throws RemoteException;
	
	public Map<String, V> Reallocate(String oldPredKey, String newPredKey) throws RemoteException;	
	public void updateFingerTable(List<NodeStruct<V>> nodes) throws RemoteException;
//	public void probe(String key, int count) throws RemoteException;
	
	public NodeStruct<V> searchNodeFromKey(String key) throws RemoteException;
	public V getValueByKey(String key) throws RemoteException;
	public void addKeyValue(String key, V value) throws RemoteException;
	public void removeKeyValue(String key) throws RemoteException;
	public List<V> getAllValues() throws RemoteException;
	
	
}