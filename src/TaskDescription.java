package scenarioGenerator;

import java.util.ArrayList;

/**
 * Represents a task as described by the domain knowledge. If the task is complex it
 * can be decomposed using DecompositionlevelSchemas. If a specific task has to be added to the scenario a concrete instantiation
 * of the appropriate instance of this class is made in the form of a Task.
 *
 */
public class TaskDescription extends DomainConcept {

	private ArrayList<String> _serviceIds;
	private ArrayList<String> _decompIds;
	
	public TaskDescription(String id, String name, ArrayList<String> decompIds, ArrayList<String> serviceIds) {
		super(id, name);
		_decompIds = decompIds;
		_serviceIds = serviceIds;
	}
	
	/**
	 * @return true if the task can be decomposed into subtasks, false otherwise
	 */
	public boolean isComplexTask(){
		return _decompIds.size() > 0; 
	}
	
	/**
	 * @return all DecompositionSchemas that can be used to decompose this task
	 */
	public ArrayList<String> getAllTaskDecompositions(){
		ArrayList<String> copy = new ArrayList<String>();
		for(String t : _decompIds){
			copy.add(t);
		}
		return copy;
	}
	
	/**
	 * @return all precondtions for this task
	 */
	public ArrayList<String> getRequiredServiceIds(){
		return _serviceIds;
	}

}
