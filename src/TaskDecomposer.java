package scenarioGenerator;

import java.util.ArrayList;
import java.util.Collections;

/**
 * This class is responsible for decomposing the high-level tasks from the scenario template
 * into concrete actions based on the required difficulty level of the scenario *
 */
public class TaskDecomposer {
	
	private TimeLine _timeline;
	private SmartObjectSelector _objectSelector;
	private DecompositionComparator _comp;
	private DomainParser _parser;
	private MultiValueTreeMap<String, ServiceImplementation> _bindings;
	
	public TaskDecomposer(TimeLine time, SmartObjectSelector objSelector, DomainParser parser){
		_timeline = time;
		_objectSelector = objSelector;
		_comp = new DecompositionComparator();
		_parser = parser;
		_bindings = new MultiValueTreeMap<String, ServiceImplementation>();
		
	}
	
	/**
	 * Selects the DecompositionSchema that matches best given the difficulty and applies it to the
	 * specified task. If the matchPreconditions flag is on, only schema that do not require the 
	 * addition of new objects to the world are considered.
	 * @param task to be decomposed
	 * @param difficulty level desired for scenario
	 * @param setting of scenario
	 * @param matchPreconditions, flag to indicate if new objects may be added to the scenario
	 * @return true if the task was successfully decomposed, false otherwise
	 */
	public boolean decomposeTask(Task task, int difficulty, String setting, boolean matchPreconditions){
		// get all possible decompositions
		ArrayList<String> decompIds = _parser.getTaskDescById(task.taskDesc().id()).getAllTaskDecompositions();
		ArrayList<DecompositionSchema> decomps = new ArrayList<DecompositionSchema>();
		for(String id : decompIds){
			decomps.add(_parser.getDecompSchemaById(id));
		}
		if(decomps.size() <= 0)
			return false;
		
		// sort possible decompositions on difficulty range
		sortDecompositions(decomps, difficulty);
		
		DecompositionSchema schema = null;
		// select best possible option
		// if flag is off, all schemas are acceptable, therefore select first
		if(!matchPreconditions){
			for(DecompositionSchema d : decomps){
				if(enablePreconditions(d, difficulty, setting)){
					schema = d;
					break;
				}
			}
			if(schema == null)
				System.err.println("Could not enable preconditions of any schema for task "+ task.taskDesc().name());
		}
		else{
			for(DecompositionSchema d : decomps){// check all options, since ordered by suitability select first possible option
				if(holdPreconditions(d, setting, difficulty)){
					schema = d;
					break;
				}
			}
		}
		// decompose task recursively depth first
		if(schema != null){
			_bindings.clear();
			ArrayList<Task> tasks = _timeline.decomposeTask(task.id(), schema);
			for(Task t: tasks){
				if(!t.taskDesc().isComplexTask()){
					AtomTask action = (AtomTask) t;
						_objectSelector.enableAction(_timeline, action, difficulty, setting);
				}else{
					decomposeTask(t, difficulty, setting, matchPreconditions);
				}
				
			}
			return true;
		}
		return false;
	}
	
	/**
	 * This function decomposes a high level task according using the DecompositionSchemas specified.
	 * This function should be called only for decomposing the template task to which the target task
	 * was fitted. When the target task is reached and no more schemas are specified the function will
	 * call the decomposeTask function to choose appropriate DecompositionSchemas to apply to the
	 * remaining tasks.
	 * @param task to be decomposed
	 * @param difficulty level desired for scenario
	 * @param setting of the scenario
	 * @param decomps DecompositionSchemas to use
	 * once the method is free to choose the most appropriate DecompositionSchema
	 * @return true if task successfully decomposed, false otherwise
	 */
	public boolean decomposeTaskFixed(Task task, int difficulty, String setting, ArrayList<DecompositionSchema> decomps){
		DecompositionSchema schema = decomps.get(0);
		if(schema.getTaskId().equalsIgnoreCase(task.taskDesc().id())){
			// enable preconditions and add required services
			enablePreconditions(schema, difficulty, setting);
			// decompose task
			ArrayList<Task> tasks = _timeline.decomposeTask(task.id(), schema);
			decomps.remove(0);
			// if no more decomposition schemas available to decompose the subtasks choose freely
			if(decomps.isEmpty()){
				for(int i=0; i<tasks.size(); i++){
					Task t = tasks.get(i);
					if(t.taskDesc().isComplexTask()){
						if(!decomposeTask(t, difficulty, setting, false))
							return false;
					}
					else{
						AtomTask action = (AtomTask) t;
						if(!_objectSelector.enableAction(_timeline, action, difficulty, setting))
							return false;
					}
					
				}
			}
			else{
				// find critical subtask
				int cIndex = -1;
				for(int i=0; i<tasks.size(); i++){
					if(tasks.get(i).taskDesc().id().equalsIgnoreCase(decomps.get(0).getTaskId())){
						cIndex = i;
						break;
					}
				}
				if(cIndex >= 0){
					Task cTask = tasks.get(cIndex);
					// decompose critical subtask
					if(!decomposeTaskFixed(cTask, difficulty, setting, decomps))
						return false;
					// decompose additional subtasks choosing freely
					for(int i=cIndex-1; i>=0; i--){
						if(!decomposeTask(tasks.get(i), difficulty, setting, true))
							return false;
					}
					for(int i=cIndex+1; i<tasks.size(); i++){
						if(!decomposeTask(tasks.get(i), difficulty, setting, true))
							return false;
					}
				}
				else{
					System.err.println("Mismatch schema, trying to use schema "+ decomps.get(0).name()+" but task not availble");
					return false;
				}
			}
		}
		else{
			System.err.println("Mismatch schema, trying to decompose task: "+task.taskDesc().name() +" with: "+schema.name());
			return false;
		}
		return true;
	}
	
	/**
	 * Helper function to determine if all preconditions of a DecompositionSchema are fulfilled or
	 * can be fulfilled without adding any new objects to the scenario
	 * @param schema
	 * @param setting of the scenario
	 * @param difficulty level desired for the scenario
	 * @return
	 */
	private boolean holdPreconditions(DecompositionSchema schema, String setting, int difficulty){
		for(String service : schema.getAllServicePreconds()){
			// check if service is already offered by an object in the time line
			if(_timeline.isServiceOffered(service) == null){
				// check if service could be offered without adding new objects
				MultiValueTreeMap<String, ServiceImplementation> bind = _objectSelector.couldServiceBeOffered(_timeline, service, setting, difficulty);
				if(bind == null)
					return false;
				else{
					_bindings.putAll(bind);
				}
			}
		}
		for(String obj : schema.getAllObjectPreconds()){
			if(_timeline.containsObjectDescription(_parser.getSmartObjById(obj)).size() == 0)
				return false;
		}
		return true;
	}
	
	/**
	 * Tries to ensure that the specified service is offered by the game world. First checks if the
	 * Service is already offered and if not tries to find an object that could offer the Service.
	 * @param serviceId id of the Service that has to be offered
	 * @param difficulty level desired for the scenario
	 * @param setting of the scenario
	 * @return true if the Service is offered, false otherwise
	 */
	private boolean findServiceObject(String serviceId, int difficulty, String setting){
		if(_timeline.isServiceOffered(serviceId) == null){	
			SmartObject so = _objectSelector.fillService(_timeline, serviceId, difficulty, setting);
			if(so == null)
				return false;
				
		}
		return true;
	}
	
	/**
	 * Tries to enable the service and object preconditions of the specified DecompositionSchema
	 * @param schema
	 * @param difficulty level desired for the scenario
	 * @param setting of the scenario
	 * @return true if all the preconditions are fulfilled, false otherwise
	 */
	private boolean enablePreconditions(DecompositionSchema schema, int difficulty, String setting){
		for(String service : schema.getAllServicePreconds()){
			if(!findServiceObject(service, difficulty, setting))
				return false;			
		}
		for(String obj : schema.getAllObjectPreconds()){
			if(_timeline.containsObjectDescription(_parser.getSmartObjById(obj)) == null){
				SmartObjectDescription sod = _objectSelector.findBestObject(_parser.getSmartObjById(obj), setting);
				SmartObject so;
				if(sod instanceof AgentDescription)
					so = new Agent((AgentDescription)sod);
				else 
					so = new SmartObject(sod);
				_timeline.addNewObject(so, false);
			}
				
		}
		return true;
	}
	
	/**
	 * Sorts the supplied DecompositionSchemas based on how closely they can approximate the
	 * desired difficulty level of the scenario. Schemas with the same score are shuffled randomly.
	 * @param decomps DecompositionSchemas to sort
	 * @param difficulty level desired for the scenario
	 */
	public void sortDecompositions(ArrayList<DecompositionSchema> decomps, int difficulty){
		// sort based on difficulty and setting
		_comp.initialise(difficulty);		
		Collections.sort(decomps, _comp);
		// reverse order so list is ordered from best to worst
		Collections.reverse(decomps);
	
		// shuffle implementations with equal score to promote variability
		int start = 0; DecompositionSchema temp1, temp2;
		for(int i=0; i<decomps.size()-1; i++){
			temp1 = decomps.get(i);
			temp2 = decomps.get(i+1);
			 // if current and next different shuffle from start to i (inclusive)
			if(_comp.compare(temp1, temp2) != 0 ){
				Collections.shuffle(decomps.subList(start, i+1));
				start = i+1;
			}// else if there is no next after this round shuffle from start to i+1 (inclusive)
			else if((i+1) >= decomps.size()-1){
				Collections.shuffle(decomps.subList(start, i+2));
			}
		}
	}
	
	
	
	
	
}
