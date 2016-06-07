package scenarioGenerator;
import java.util.ArrayList;

/**
 * Stores information on how a specific SmartObjectDescription can offer a specific service
 *
 */
public class ServiceImplementation extends DomainConcept {
	
	private String _serviceId;
	private String _objectId;
	private ArrayList<String> _actionIds;
	private ArrayList<String> _actorTypes;
	private ArrayList<String> _constraints;
	private int _minDifficulty, _maxDifficulty;
	
	public ServiceImplementation(String id, String name, String service, String objectId, ArrayList<String> actionIds, ArrayList<String> actorTypes, ArrayList<String> constraints, int minDifficulty, int maxDifficulty) {
		super(id, name);
		_objectId = objectId;
		_actionIds = actionIds;		
		_actorTypes = actorTypes;
		_constraints = constraints;
		_minDifficulty = minDifficulty;
		_maxDifficulty = maxDifficulty;
		_serviceId = service;
	}
	
	/**
	 * @return the id of the service that is offered
	 */
	public String service(){
		return _serviceId;
	}
	
	/**
	 * @return the id of the SmartObjectDescription that offers the service
	 */
	public String objectId(){
		return _objectId;
	}
	
	/**
	 * @return the IDs of the actions that have to be executed before the service can be offered
	 */
	public ArrayList<String> actionIds(){
		return _actionIds;
	}
	
	/**
	 * @return the type of the agents that have to execute the actions specified as preconditions
	 * before the service can be offered. The index of the type should match the index of the
	 * action.
	 */
	public ArrayList<String> actorTypes(){
		return _actorTypes;
	}
	
	/**
	 * @return the IDs of ServiceImplementations that cannot be offered by a specific instance of 
	 * the SmartObjectDescription if it already offering this ServiceImplementation
	 */
	public ArrayList<String> constraints(){
		return _constraints;
	}
	
	/**
	 * @return the min value of the difficulty range at which the SmartObjectDescription can offer
	 * the Service
	 */
	public int minDifficulty(){
		return _minDifficulty;
	}
	
	/**
	 * @return the max value of the difficulty range at which the SmartObjectDescription can offer
	 * the Service
	 */
	public int maxDifficulty(){
		return _maxDifficulty;
	}

}
