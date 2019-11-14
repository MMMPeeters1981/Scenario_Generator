package scenarioGenerator;

import java.util.ArrayList;
import java.util.Collections;
//import java.util.Iterator;
//import java.util.Stack;
//import java.util.TreeSet;
import java.util.TreeMap;

/**
 * This class stores the scenario as generated so far. It contains all the objects, services and
 * actions that together make up the scenario plan.
 */
public class TimeLine {

	private ArrayList<Task> _planning;
	private ArrayList<AtomTask> _story;
	private ArrayList<SmartObject> _initObj;
	private ArrayList<SmartObject> _dynamicObj;
	private MultiValueTreeMap<Integer, SmartObject> _actionObj; 
	private MultiValueTreeMap<Integer, String> _actionService; 
	private TreeMap<Integer, Agent> _actionActor;
	private TreeMap<Integer, String> _actionGoal;
	private DomainParser _parser;
	private TreeMap<String, SmartObject> _services;
	private TreeMap<String, Integer> _serviceDifficulty;
	private MultiValueTreeMap<Integer, String> _serviceImplementations;
	
	public TimeLine(DomainParser parser){
		_parser = parser;
		_planning = new ArrayList<Task>();
		_story = new ArrayList<AtomTask>();
		_initObj = new ArrayList<SmartObject>();
		_dynamicObj = new ArrayList<SmartObject>();
		_actionObj = new MultiValueTreeMap<Integer, SmartObject>();
		_actionService = new MultiValueTreeMap<Integer, String>();
		_actionActor = new TreeMap<Integer, Agent>();
		_actionGoal = new TreeMap<Integer, String>();
		_services = new TreeMap<String, SmartObject>();
		_serviceDifficulty = new TreeMap<String, Integer>();
		_serviceImplementations = new MultiValueTreeMap<Integer, String>();
	}
	
	/**
	 * Checks if any of the objects currently available in the game world could offer the specified 
	 * Service.
	 * @param service
	 * @return reference to the object that can offer the Service, null if no object could be found
	 */
	public SmartObject couldOfferService(String service){
		ArrayList<SmartObject> allObj = new ArrayList<SmartObject>();
		allObj.addAll(_initObj);
		allObj.addAll(_dynamicObj);
		// consider all available objects
		for(SmartObject so : allObj){
			ArrayList<ServiceImplementation> options = _parser.getServiceImplByObj(so.objDesc().id(), service);
			// check if the ServiceImplementation is compatible with the implementations already
			// offered by this object
			for(ServiceImplementation si : options){
				if(satisfiesConstraints(so.id(), si))
					return so;
			}
		}
		return null;
	}
	
	/**
	 * Checks if the specified Service (or a child Service) is currently offered by the game world
	 * @param serviceId
	 * @return the object that offers the Service, null if the Service is not offered
	 */
	public SmartObject isServiceOffered(String serviceId){
		SmartObject so = _services.get(serviceId);
		if(so == null){
			Service service = _parser.getServiceById(serviceId);
			for(String sId : _services.keySet()){
				if(_parser.isServiceChild(service, sId))
					so = _services.get(sId);
			}
		}
		return so;
	}
	
	/**
	 * Initialises the action planning of the trainee with the tasks from the scenario template
	 * @param descriptions the TaskDescriptions that together form the scenario
	 * @return the instantiated Tasks that were added to the planning
	 */
	public ArrayList<Task> addTemplate(ArrayList<TaskDescription> descriptions){
		ArrayList<Task> tasks = new ArrayList<Task>();
		for(TaskDescription d: descriptions){
			Task task = new Task(d);
			tasks.add(task);
			_planning.add(task);
		}
		return tasks;
	}
	
	/**
	 * Stores the information that an object already present in the game world is required 
	 * to offer the specified service so it can be used by a specific action
	 * @param actionId
	 * @param service
	 * @param obj
	 */
	public void addActionExsistingObject(int actionId, String service, SmartObject obj){
		_actionObj.put(actionId, obj);
		_actionService.put(actionId, service);
	}
	
	/**
	 * Adds the object to the game world and records the action and the service that caused this
	 * object to be added
	 * @param actionId
	 * @param so
	 * @param service
	 * @param init flag to indicate if this is a static object or not
	 */
	public void addActionNewObject(int actionId, SmartObject so, String service, boolean init){
		if(init){
			_initObj.add(so);
		}
		else{
			_dynamicObj.add(so);
		}
		_actionObj.put(actionId, so);
		_actionService.put(actionId, service);
	}
	
	/**
	 * Adds a story action to the planning of the virtual agents, including the Service that this
	 * action is supposed to accomplish
	 * @param action
	 * @param service
	 * @return
	 */
	public AtomTask addStoryAction(AtomTaskDescription action, String service){
		AtomTask aTask = new AtomTask(action);
		_story.add(aTask);
		_actionGoal.put(aTask.id(), service);
		return aTask;
	}
	
	/**
	 * Adds an object to the game world
	 * @param so
	 * @param init flag to indicate whether the object is static or not
	 */
	public void addNewObject(SmartObject so, boolean init){
		if(init)
			_initObj.add(so);
		else
			_dynamicObj.add(so);
	}
	
	/**
	 * Checks if a specific object is present in the game world
	 * @param objId
	 * @return true if the object is present, false otherwise
	 */
	public boolean containsObject(int objId){
		ArrayList<SmartObject> allObj = new ArrayList<SmartObject>();
		allObj.addAll(_initObj);
		allObj.addAll(_dynamicObj);
		for(SmartObject so : allObj){
			if(so.id() == objId)
				return true;
		}
		return false; 
	}
	
	/**
	 * Checks if there exists any object in the game world that is an instantiation of the 
	 * specified AbstractSmartObjectDescription or any of its children
	 * @param objDesc
	 * @return all objects present that are instatiations of the description
	 */
	public ArrayList<SmartObject> containsObjectDescription(AbstractSmartObjectDescription objDesc){
		ArrayList<SmartObject> objects = new ArrayList<SmartObject>();
		ArrayList<SmartObject> allObj = new ArrayList<SmartObject>();
		allObj.addAll(_initObj);
		allObj.addAll(_dynamicObj);
		for(SmartObject so : allObj){
			if(so.objDesc().equals(objDesc))
				objects.add(so);
			else if(isParentObject(objDesc, so.objDesc()))
				objects.add(so);
		}
		return objects; 
	}
	
	/**
	 * Helper function that checks if the description specified as child actually inherits from
	 * the description specified as parent
	 * @param parent
	 * @param child
	 * @return true if there the descriptions are related by inheritance, false otherwise
	 */
	private boolean isParentObject(AbstractSmartObjectDescription parent, AbstractSmartObjectDescription child){
		for(String id : child.parentIds()){
			if(id.equalsIgnoreCase(parent.id()))
				return true;
		}
		for(String id : child.parentIds()){
			AbstractSmartObjectDescription obj = _parser.getSmartObjById(id);
			if(isParentObject(parent, obj))
				return true;
		}
		return false;
	}
	
	/**
	 * Replaces the specified task in the planning by the subtasks indicated in the DecompositionSchema
	 * @param taskID
	 * @param decomp
	 * @return the instantiations of the sub Tasks as added to the planning
	 */
	public ArrayList<Task> decomposeTask(int taskID, DecompositionSchema decomp){
		ArrayList<Task> tasks = new ArrayList<Task>();
		for(int i=0; i<_planning.size(); i++){
			if(_planning.get(i).id() == taskID){
				// remove task that is to be decomposed
				_planning.remove(i);
				// determine tasks to be added based on decomposition
				for(String id: decomp.getDecomposition()){
					TaskDescription desc = _parser.getTaskDescById(id);
					Task task;
					if(desc.isComplexTask()){
						task = new Task(_parser.getTaskDescById(id));
					}
					else{
						task = new AtomTask((AtomTaskDescription)desc);
					}
					tasks.add(task);
				}
				// make room for tasks
				int number = tasks.size();
				for(int j=0; j<number; j++){
					_planning.add(null);
				}
				for(int j=_planning.size()-(number+1); j>=i; j--){
					_planning.set(j+number, _planning.get(j));
				}
				// add tasks
				int taskIndex = 0;
				for(int j=i; j<i+number; j++){
					_planning.set(j, tasks.get(taskIndex) );
					taskIndex++;
				}
				return tasks;
			}
		}
		return null;
	}
	
	public TimeLine clone(){
		TimeLine clone = new TimeLine(_parser);
		for(Task t : _planning)
			clone._planning.add(t);
		for(AtomTask t: _story)
			clone._story.add(t);
		for(SmartObject obj : _initObj)
			clone._initObj.add(obj);
		for(SmartObject obj : _dynamicObj)
			clone._dynamicObj.add(obj);
		for(Integer key : _actionObj.keySet())
			clone._actionObj.put(key, _actionObj.get(key));
		for(Integer key : _actionService.keySet())
			clone._actionService.put(key, _actionService.get(key));
		for(Integer key : _actionActor.keySet())
			clone._actionActor.put(key, _actionActor.get(key));
		for(Integer key : _actionGoal.keySet())
			clone._actionGoal.put(key, _actionGoal.get(key));
		for(String key : _services.keySet())
			clone._services.put(key, _services.get(key));
		for(String key : _serviceDifficulty.keySet())
			clone._serviceDifficulty.put(key, _serviceDifficulty.get(key));
		for(Integer key : _serviceImplementations.keySet())
			clone._serviceImplementations.put(key, _serviceImplementations.get(key));
		return clone;
	}
	
	/**
	 * Adds all the information stored in the specified TimeLine to the current TimeLine
	 * @param other
	 */
	public void merge(TimeLine other){
		merge(_planning, other._planning);
		merge(_story, other._story);
		merge(_initObj, other._initObj);
		merge(_dynamicObj, other._dynamicObj);
		merge(_actionObj, other._actionObj);
		merge(_actionService, other._actionService);
		merge(_actionActor, other._actionActor);
		merge(_actionGoal, other._actionGoal);
		merge(_services, other._services);
		merge(_serviceDifficulty, other._serviceDifficulty);
		merge(_serviceImplementations, other._serviceImplementations);
	}
	
	/**
	 * Helper function to add all values from the other map to the goal map
	 * @param goal
	 * @param other
	 */
	private <K, V> void merge(MultiValueTreeMap<K, V> goal, MultiValueTreeMap<K, V> other){
		for(K key : other.keySet()){
			if(!goal.containsKey(key))
				goal.put(key, other.get(key));
			else{
				ArrayList<V> otherValues = other.get(key);
				ArrayList<V> ownValues = goal.get(key);
				for(V value : otherValues)
					if(!ownValues.contains(value))
						ownValues.add(value);
				}
		}
	}
	
	/**
	 * Helper function to add all values from the other list to the goal list
	 * @param goal
	 * @param other
	 */
	private <V> void merge(ArrayList<V> goal, ArrayList<V> other){
		for(V value : other){
			if(!goal.contains(value))
				goal.add(value);
		}
	}
	
	/**
	 * Helper function to add all values from the other map to the goal map
	 * @param goal
	 * @param other
	 */
	private <K,V> void merge(TreeMap<K,V> goal, TreeMap<K,V> other){
		for(K key : other.keySet()){
			goal.put(key, other.get(key));
		}
	}
	
	/**
	 * @return the scenario in a readable layout
	 */
	public String printScenario(){
		String scenario = "";
		scenario += "Planning: \r\n";
		for(Task t : _planning){
			String objects = "";
			if(_actionObj.get(t.id()) != null){
				objects+=" using ";
				for(int i=0; i<_actionObj.get(t.id()).size(); i++){
					SmartObject so = _actionObj.get(t.id()).get(i);
					String service = _parser.getServiceById(_actionService.get(t.id()).get(i)).name();
					objects += so.objDesc().name()+"("+so.id() +")"+" for "+ service +", ";
				}
			}
			scenario += t.taskDesc().name() +  objects + "\r\n";
		}
		scenario += "\r\n Story: \r\n";
		Collections.reverse(_story);
		for(AtomTask a : _story){
			String objects = "";
			if(_actionObj.get(a.id()) != null){
				objects += " using ";
				for(int i=0; i<_actionObj.get(a.id()).size(); i++){
					SmartObject so = _actionObj.get(a.id()).get(i);
					String service = _parser.getServiceById(_actionService.get(a.id()).get(i)).name();
					objects += so.objDesc().name()+"("+so.id() +")"+ " for "+ service +", ";
				}
			}
			String actor ="";
			if(_actionActor.get(a.id()) != null)
				actor += _actionActor.get(a.id()).getAgentDesc().name()+"("+_actionActor.get(a.id()).id() +")";
			String goal ="";
			if(_actionGoal.get(a.id()) != null)
				goal += " to obtain " +  _parser.getServiceById(_actionGoal.get(a.id())).name();
			scenario += actor + " performs " + a.actionDesc().name() + goal + objects + "\r\n";
		}
		scenario += "\r\n Init Objects: \r\n";
		for(SmartObject so : _initObj){
			scenario += so.objDesc().name() +"("+so.id()+")"+ "\r\n";
		}
		scenario += "\r\n Dynamic Objects: \r\n";
		for(SmartObject so : _dynamicObj){
			scenario += so.objDesc().name() +"("+so.id()+")"+ "\r\n";
		}
		scenario += "\r\n Services: \r\n";
		for(String serviceId : _services.keySet()){
			scenario += _services.get(serviceId).objDesc().name()+" offers "+ _parser.getServiceById(serviceId).name() +" at level: "+_serviceDifficulty.get(serviceId)+"\r\n";
		}
		return scenario;
	}

	/**
	 * Specifies an existing Agent as actor for the specified action
	 * @param actionId
	 * @param actor
	 */
	public void addActionExistingActor(int actionId, Agent actor) {
		_actionActor.put(actionId, actor);		
	}

	/**
	 * Adds an Agent to the game world along with the action he is expected to perform
	 * @param actionId
	 * @param actor
	 */
	public void addActionNewActor(int actionId, Agent actor) {
		_dynamicObj.add(actor);
		_actionActor.put(actionId, actor);
		
	}

	/**
	 * Tries to find an Agent present in the game world of the specified type
	 * @param actorType
	 * @return reference to an Agent of the correct type, null if there exists no Agent of the 
	 * correct type
	 */
	public Agent getAgentByType(String actorType) {
		ArrayList<SmartObject> allObj = new ArrayList<SmartObject>();
		allObj.addAll(_initObj);
		allObj.addAll(_dynamicObj);
		for(SmartObject so: allObj){
			if(so instanceof Agent){
				Agent agent = (Agent) so;
				if(agent.getAgentDesc().type().equalsIgnoreCase(actorType))
					return agent;
				else if(_parser.checkParentDescriptions(agent.getAgentDesc(), actorType))
					return agent;
			}
		}
		return null;
	}

	/**
	 * Stores the information that a service is offered by a specific object using the specified 
	 * ServiceImplementation at the specified difficulty level
	 * @param serviceId
	 * @param serviceImplId
	 * @param obj
	 * @param difficulty
	 */
	public void addServiceObject(String serviceId, String serviceImplId, SmartObject obj, int difficulty) {
		_services.put(serviceId, obj);
		_serviceDifficulty.put(serviceId, difficulty);
		_serviceImplementations.put(obj.id(), serviceImplId);
		
	}
	
	/**
	 * Checks if the specified ServiceImplementation is compatible with the ServiceImplementations
	 * already offered by the specified object
	 * @param objId
	 * @param si
	 * @return true if the ServiceImplementation is compatible, false otherwise
	 */
	public boolean satisfiesConstraints(int objId, ServiceImplementation si){
		ArrayList<String> simps = _serviceImplementations.get(objId);
		if( simps == null)
			return true;
		else{
			for(String constraint : si.constraints()){
				for(String simpId : simps){
					if(constraint.equalsIgnoreCase(simpId))
						return false;
				}
			}
			return true;
		}
	}
	
}
