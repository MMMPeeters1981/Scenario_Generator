package scenarioGenerator;

import java.util.ArrayList;
import java.util.TreeMap;

/**
 * Helper class representing a TreeMap that can store several values under one key
 *
 * @param <K> key type
 * @param <V> value type
 */
public class MultiValueTreeMap<K,V> {
	
	private TreeMap<K, ArrayList<V>> _map;
	
	public MultiValueTreeMap(){
		_map = new TreeMap<K, ArrayList<V>>();
	}
	
	/**
	 * Stores value under the specified key. If the key already existed, the value is added to
	 * the list of existing values, otherwise the key is created
	 * @param key
	 * @param value
	 */
	public void put(K key, V value){
		if(_map.containsKey(key)){
			_map.get(key).add(value);
		}
		else{
			ArrayList<V> list = new ArrayList<V>();
			list.add(value);
			_map.put(key, list);
		}
	}
	
	/**
	 * Stores all values under the specified key. If the key already existed the values are added
	 * to the list of existing values, otherwise the key is created.
	 * @param key
	 * @param values
	 */
	public void put(K key, ArrayList<V> values){
		if(_map.containsKey(key)){
			_map.get(key).addAll(values);
		}
		else{
			_map.put(key, values);
		}
	}
	
	/**
	 * @param key
	 * @return all values stored under the specified key
	 */
	public ArrayList<V> get(K key){
		return _map.get(key);
	}
	
	/**
	 * @param key
	 * @return true if the map contains the key, false otherwise
	 */
	public boolean containsKey(K key){
		return _map.containsKey(key);
	}
	
	/**
	 * @return all keys in the map
	 */
	public ArrayList<K> keySet(){
		return new ArrayList<K>(_map.keySet());
	}
	
	/**
	 * @return number of keys in the map
	 */
	public int size(){
		return _map.size();
	}
	
	/**
	 * @param value
	 * @return true if the map contains the value, false otherwise
	 */
	public boolean containsValue(V value){
		for(ArrayList<V> list: _map.values()){
			for(V v:list){
				if(v.equals(value))
					return true;
			}
		}
		return false;
	}
	
	/**
	 * @param value
	 * @return all keys associated with a specific value
	 */
	public ArrayList<K> getKeys(V value){
		ArrayList<K> keys = new ArrayList<K>();
		for(K key: _map.keySet()){
			for(V v: _map.get(key)){
				if(v.equals(value))
					keys.add(key);
			}
		}
		return keys;
	}
	
	/**
	 * @return all values stored in the map
	 */
	public ArrayList<V> values(){
		ArrayList<V> values = new ArrayList<V>();
		for(ArrayList<V> list: _map.values()){
			values.addAll(list);
		}
		return values;
	}
	
	/**
	 * Stores all keys and associated values from the specified map in this map. If a key already
	 * existed the values from the specified map are added to the list of already existing values.
	 * @param map
	 */
	public void putAll(MultiValueTreeMap<K,V> map){
		TreeMap<K, ArrayList<V>> m = map._map;
		for(K key : m.keySet()){
			ArrayList<V> values = m.get(key);
			for(V value : values){
				put(key, value);
			}
		}
	}
	
	/**
	 * Empty the map.
	 */
	public void clear(){
		_map.clear();
	}
}
