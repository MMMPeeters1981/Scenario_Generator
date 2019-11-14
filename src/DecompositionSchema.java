package scenarioGenerator;

import java.util.ArrayList;

/**
 * Represents a decomposition schema or HTN method that can be applied to decompose a high level
 * task into subtasks.
 */
public class DecompositionSchema extends DomainConcept {

	private ArrayList<String> _decomposition;
	private ArrayList<String> _precond_services;
	private ArrayList<String> _precond_objects;
	private String _taskId;
	private int _minDifficutly;
	private int _maxDifficulty;
	
	public DecompositionSchema(String id, String name, String taskId, ArrayList<String> decomposition, ArrayList<String> preServiceIds, ArrayList<String> preObjectIds, int minDif, int maxDif) {
		super(id, name);
		_taskId = taskId;
		_decomposition = decomposition;
		_precond_services = preServiceIds;
		_precond_objects = preObjectIds;
		_minDifficutly = minDif;
		_maxDifficulty = maxDif;
	}
	
	/**
	 * @return subtasks into which the task is decomposed
	 */
	public ArrayList<String> getDecomposition(){
		ArrayList<String> copy = new ArrayList<String>();
		for(String t : _decomposition){
			copy.add(t);
		}
		return copy;
	}
	
	/**
	 * @return task to which DecompositionSchema can be applied
	 */
	public String getTaskId(){
		return _taskId;
	}
	
	/**
	 * @return services that are required before this schema can be applied
	 */
	public ArrayList<String> getAllServicePreconds(){
		ArrayList<String> copy = new ArrayList<String>();
		for(String t : _precond_services){
			copy.add(t);
		}
		return copy;
	}
	
	/**
	 * @return objects that are required before this schema can be applied
	 */
	public ArrayList<String> getAllObjectPreconds(){
		ArrayList<String> copy = new ArrayList<String>();
		for(String t : _precond_objects){
			copy.add(t);
		}
		return copy;
	}
	
	/**
	 * @return min value of the difficulty range of the schema
	 */
	public int minDifficulty(){
		return _minDifficutly;
	}
	
	/**
	 * @return max value of the difficulty range of the schema
	 */
	public int maxDifficulty(){
		return _maxDifficulty;
	}


}
