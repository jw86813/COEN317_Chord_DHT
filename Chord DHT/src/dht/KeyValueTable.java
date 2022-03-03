package dht;
import java.util.List;


public interface KeyValueTable<V> {
	
	public V search(String key);
	public void create(String key, V value);
	public void delete(String key);
	public void join(NodeStruct<V> existed_node);
	public void leave();
	public List<String> listAllKeyValue();
	public void updateRouting();
	
}
