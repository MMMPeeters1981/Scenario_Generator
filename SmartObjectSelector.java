package scenarioGenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeMap;


/**
 * This class represents the Object Selector and is responsible for fulfilling the services as
 * requested by the TaskDecomposer.
 */
public class SmartObjectSelector {

	private MultiValueTreeMap<String, ServiceImplementation> _smap;
	private ServiceImplementationComparator _comp;
	private DomainParser _parser;
	
	public SmartObjectSelector(TimeLine time, DomainParser parser){
		_parser = parser;
		_smap = new MultiValueTreeMap<String, ServiceImplementation>();
		for(ServiceImplementation si: _parser.getAllServiceImplementations()){
			addServiceImplementation(si);
		}
		_comp = new ServiceImplementationComparator();
	}
	
	/**
	 * Adds the specified ServiceImplementation to its internal data structure. This data structure
	 * stores all ServiceImplementation based on the most id of the most high-level ancestor of the
	 * Service specified in the ServiceImplementation.
	 * @param si the ServiceImplementation to add
	 */
	public void addServiceImplementation(ServiceImplementation si){
		_smap.put(si.service(), si);
		Service service = _parser.getServiceById(si.service());
		while(!service.parent().isEmpty()){
			service = _parser.getServiceById(service.parent());
			if(service == null)
				break;
			_smap.put(service.id(), si);
		}
	}
	
	/**
	 * Ensures all services that are required by the specified actions are present in the scenario.
	 * @param timeLine reference storing the scenario generated so far
	 * @param action reference that needs to be enabled
	 * @param difficulty of the scenario
	 * @param setting of the scenario
	 * @return true if all services required by the action have been fulfilled.
	 */
	public boolean enableAction(TimeLine timeLine, AtomTask action, int difficulty, String setting){
		//support backtracking
		TimeLine _timeLine = timeLine.clone();
		
		TreeMap<String, SmartObject> implementations = new TreeMap<String, SmartObject>();
		ArrayList<String> services = action.actionDesc().getRequiredServiceIds();
		// find smart objects for all required services
		for(String service : services){
			SmartObject si = fillService(_timeLine, service, difficulty, setting);
			if(si == null){
				System.err.println("Could not offer "+service+" for action " + action.actionDesc().name() );
				return false;
			}
			implementations.put(service, si);
		}
		// add smart objects to time line
		for(String service : implementations.keySet()){
			SmartObject obj = implementations.get(service);
			if(_timeLine.containsObject(obj.id())){
				_timeLine.addActionExsistingObject(action.id(), service, obj);
			}
			else{
				System.err.println("Object "+ obj.objDesc().name() +" not yet added to timeline");
				return false;
			}
		}
		// no backtracking required
		timeLine.merge(_timeLine);
		return true;
		
	}
	
	/**
	 * Ensures a specific service is offered by the game world
	 * @param timeLine reference to the scenario generated so far
	 * @param service that needs to be offered
	 * @param difficulty of the scenario
	 * @param setting of the scenario
	 * @return reference to the SmartObject that fulfills the specified service
	 */
	public SmartObject fillService(TimeLine timeLine, String service, int difficulty, String setting){
		// support backtracking
		TimeLine _timeLine = timeLine.clone();
		// check if service is already available
		SmartObject so = _timeLine.isServiceOffered(service);
		if(so != null){
			return so;
		}
		ArrayList<ServiceImplementation> implementations = new ArrayList<ServiceImplementation>();
		so = _timeLine.couldOfferService(service);
		if(so != null){
			implementations.addAll(_parser.getServiceImplByObj(so.objDesc().id(), service));
			ArrayList<ServiceImplementation> easyImplements = findNoCostImplementations(implementations, _timeLine, setting, difficulty);
			if(!easyImplements.isEmpty())
				implementations = easyImplements;
			sortImplementations(implementations, difficulty, setting);
		}
		
		// find all objects that perform required service
		ArrayList<ServiceImplementation> tempList = _smap.get(service);
		if(tempList == null)
			return null;
		
		ArrayList<ServiceImplementation> easyImplements = findNoCostImplementations(tempList, _timeLine, setting, difficulty);
		if(!easyImplements.isEmpty())
			tempList = easyImplements;
		// sort available implementations based on difficulty and setting
		sortImplementations(tempList, difficulty, setting);
		
		implementations.addAll(tempList);
		
		// loop through implementations from best to worst until one is found for which
		// all requirements can be met
		for(int i=0; i<implementations.size(); i++){
			boolean applicable = true;
			ServiceImplementation si = implementations.get(i);
			
			// enable all actions required by this implementation
			ArrayList<Integer> actionIds = new ArrayList<Integer>();
			for(String actionId : si.actionIds()){
				AtomTask a = _timeLine.addStoryAction(_parser.getAtomTaskDescById(actionId), si.service());
				actionIds.add(a.id());
				if(!enableAction(_timeLine, a, difficulty, setting)){
					applicable = false;
					_timeLine = timeLine.clone();
					break;
				}
			}
			// if all required actions could be enabled add action and obj to timeline 
			if(applicable){
				// check if there already exists an object following the description
				boolean newObj = false;
				SmartObject obj = null;
				ArrayList<SmartObject> objects = _timeLine.containsObjectDescription(_parser.getSmartObjById(si.objectId()));
				if(!objects.isEmpty()){
					Collections.shuffle(objects);
					for(SmartObject o : objects){
						if(_timeLine.satisfiesConstraints(o.id(), si)){
							obj = o;
							break;
						}
					}
					if(obj == null){
						System.out.println("Existing objects did not satisfy constraints failed to fill service "+_parser.getServiceById(service).name()+" using "+si.name());
						continue;
					}
				}
				// if no object exists create a new object (or agent)
				if(obj == null){
					newObj = true;
					SmartObjectDescription sod = findBestObject(_parser.getSmartObjById(si.objectId()), setting);
					if(sod instanceof AgentDescription)
						obj = new Agent((AgentDescription)sod);
					else
						obj = new SmartObject(sod);
				}
				// for each action determine actor
				for(int j=0; j<si.actionIds().size(); j++){
					int actionId = actionIds.get(j);
					String actorType = si.actorTypes().get(j);
					if(actorType.equalsIgnoreCase("Self")){
						if(obj instanceof Agent )
							_timeLine.addActionExistingActor(actionId, (Agent)obj);
					}else{
						Agent actor = _timeLine.getAgentByType(actorType);
						if(actor != null){
							_timeLine.addActionExistingActor(actionId, actor);
						}else{
							ArrayList<AgentDescription> possibleActors = _parser.getAgentDescByType(actorType);
							Agent act = new Agent(possibleActors.get(0)); //TODO add action distribution mechanism
							_timeLine.addActionNewActor(actionId, act);
						}
					}
				}
				// if a new object has been created, add the object to the TimeLine
				if(newObj){
					boolean init = (obj instanceof Agent) ? false : true;
					_timeLine.addNewObject(obj, init );
				}
				int diff = matchDifficulty(difficulty, si);
				String sid = service;
				if(_parser.isServiceChild(_parser.getServiceById(service), si.service()))
						sid = si.service();
				_timeLine.addServiceObject(sid, si.id(), obj, diff);
				// success, no backtracking required
				timeLine.merge(_timeLine);
				return obj;
			}
		}
		
		return null;
	}
	
	/**
	 * Enable all actions required by the specified ServiceImplementations.
	 * @param timeLine, stores the scenario generated so far
	 * @param si, the ServiceImplementation that must be enables
	 * @param service, that needs to be fulfilled, can be parent of service specified in ServiceImplementation
	 * so must be specified explicitly
	 * @param setting of the scenario
	 * @param difficulty of the scenario
	 * @return
	 */
	public SmartObject enableServiceImplementation(TimeLine timeLine, ServiceImplementation si, String service, String setting, int difficulty){
		//support backtracking
		TimeLine _timeLine = timeLine.clone();
		boolean applicable = true;
		
		// enable all actions required by this implementation
		ArrayList<Integer> actionIds = new ArrayList<Integer>();
		for(String actionId : si.actionIds()){
			AtomTask a = _timeLine.addStoryAction(_parser.getAtomTaskDescById(actionId), si.service());
			actionIds.add(a.id());
			if(!enableAction(_timeLine, a, difficulty, setting)){
				applicable = false;
				_timeLine = timeLine.clone();
				break;
			}
		}
		// if all required actions could be enabled add action and obj to timeline 
		if(applicable){
			// check if there already exists an object following the description
			boolean newObj = false;
			SmartObject obj = null;
			ArrayList<SmartObject> objects = _timeLine.containsObjectDescription(_parser.getSmartObjById(si.objectId()));
			if(!objects.isEmpty()){
				Collections.shuffle(objects);
				for(SmartObject o : objects){
					if(_timeLine.satisfiesConstraints(o.id(), si)){
						obj = o;
						break;
					}
				}
				if(obj == null){
					System.out.println("Existing objects did not satisfy constraints failed to fill service "+_parser.getServiceById(service).name()+" using "+si.name());
					return null;
				}
			}
			// if not create new object
			if(obj == null){
				newObj = true;
				SmartObjectDescription sod = findBestObject(_parser.getSmartObjById(si.objectId()), setting);
				if(sod instanceof AgentDescription)
					obj = new Agent((AgentDescription)sod);
				else
					obj = new SmartObject(sod);
			}
			// for each action determine actor
			for(int j=0; j<si.actionIds().size(); j++){
				int actionId = actionIds.get(j);
				String actorType = si.actorTypes().get(j);
				if(actorType.equalsIgnoreCase("Self")){
					if(obj instanceof Agent )
						_timeLine.addActionExistingActor(actionId, (Agent)obj);
				}else{
					Agent actor = _timeLine.getAgentByType(actorType);
					if(actor != null){
						_timeLine.addActionExistingActor(actionId, actor);
					}else{
						ArrayList<AgentDescription> possibleActors = _parser.getAgentDescByType(actorType);
						Agent act = new Agent(possibleActors.get(0)); //TODO add action distribution mechanism
						_timeLine.addActionNewActor(actionId, act);
					}
				}
			}
			if(newObj){
				boolean init = (si.actionIds().size() > 0) ? false : true;
				_timeLine.addNewObject(obj, init );
			}
			int diff = matchDifficulty(difficulty, si);
			String sid = service;
			if(_parser.isServiceChild(_parser.getServiceById(service), si.service()))
					sid = si.service();
			_timeLine.addServiceObject(sid, si.id(), obj, diff);
			timeLine.merge(_timeLine);
			return obj;
		}
		
		return null;
	}
	
	/**
	 * Computes the distance between the requested difficulty level and the closest approximation 
	 * of this level the ServiceImplementation can offer
	 * @param requestedDifficulty
	 * @param si
	 * @return distance
	 */
	private int matchDifficulty(int requestedDifficulty, ServiceImplementation si){
		int max = si.maxDifficulty();
		int min = si.minDifficulty();
		if(requestedDifficulty <= max && requestedDifficulty >= min){
			//requested difficulty falls within range of difficutlies service implementation can offer
			return requestedDifficulty; 
		}else{
			//find closest match
			int maxDifference = Math.abs(requestedDifficulty-max);
			int minDifference = Math.abs(requestedDifficulty-min);
			if(maxDifference < minDifference)
				return max;
			return min;
		}
	}
	
	/**
	 * Finds the concrete ObjectDescription that inherits from the specified AbstractSmartObjectDescription
	 * and that fits the Setting
	 * @param asotd the AbstractSmartObjectDescription for which a concrete object has to be found
	 * @param setting of the scenario
	 * @return reference to selected SmartObjectDescription
	 */
	public SmartObjectDescription findBestObject(AbstractSmartObjectDescription asotd, String setting){
		if(asotd instanceof SmartObjectDescription){
			return (SmartObjectDescription) asotd;
		}
		else {
			SmartObjectTypeDescription sotd = (SmartObjectTypeDescription) asotd;
			int score = Integer.MIN_VALUE;
			SmartObjectDescription sod = null;
			ArrayList<String> childIds = new ArrayList<String>();
			for(String child : sotd.childrenIds()){
				childIds.add(child);
			}
			Collections.shuffle(childIds);
			for(String child : childIds){
				SmartObjectDescription obj = findBestObject(_parser.getSmartObjById(child), setting);
				if(obj == null)
					return null;
				int tempScore = obj.fitsInSetting(setting);
				if(tempScore > score){
					score = tempScore;
					sod = obj;
					if(score == 1)
						break;
				}
			}
			return sod;
		}
	}
	
	/**
	 * Checks if the current scenario could offer a specific Service without having to add any
	 * new objects
	 * @param timeLine stores the scenario generated so far
	 * @param service that needs to be offered
	 * @param setting of the scenario
	 * @param difficulty of the scenario
	 * @return map storing which services will have to be offered by which objects in order to
	 * offer specified service. If the Service cannot be offered without adding objects null is returned.
	 */
	public MultiValueTreeMap<String, ServiceImplementation> couldServiceBeOffered(TimeLine timeLine, String service, String setting, int difficulty){
		if(timeLine.isServiceOffered(service) != null){
			return new MultiValueTreeMap<String, ServiceImplementation>();
		}
		ArrayList<ServiceImplementation> implementations = new ArrayList<ServiceImplementation>();
		SmartObject so = timeLine.couldOfferService(service);
		if(so != null){
			implementations.addAll(_parser.getServiceImplByObj(so.objDesc().id(), service));
			ArrayList<ServiceImplementation> easyImplements = findNoCostImplementations(implementations, timeLine, setting, difficulty);
			if(!easyImplements.isEmpty())
				implementations = easyImplements;
			sortImplementations(implementations, difficulty, setting);
		}
		
		// check if any of these implementations are applicable without adding objects
		for(ServiceImplementation imp: implementations){
			MultiValueTreeMap<String, ServiceImplementation> bindings = new MultiValueTreeMap<String, ServiceImplementation>();
			boolean applicable = true;
			for(String actionId : imp.actionIds()){
				AtomTaskDescription action = _parser.getAtomTaskDescById(actionId);
				for(String serviceId : action.getRequiredServiceIds()){
					MultiValueTreeMap<String, ServiceImplementation> temp = couldServiceBeOffered(timeLine, serviceId, setting, difficulty);
					if(temp == null){
						applicable = false;
						break;
					}
					else
						bindings.putAll(temp);
					if(!applicable)
						break;
				}
				if(!applicable)
					break;
			}
			if(applicable){
				bindings.put(service, imp);
				return bindings;
			}
		}
		return null;		
	}
	
	/**
	 * Sorts the list of ServiceImplementations based on how well the can approximate the desired 
	 * difficulty level and how well the associated object fits the desired setting. Implementations
	 * that score the same regarding these qualifications are shuffled randomly.
	 * @param implementations
	 * @param difficulty of the scenario
	 * @param setting of the scenario
	 */
	private void sortImplementations(ArrayList<ServiceImplementation> implementations, int difficulty, String setting){
		
		// sort based on difficulty and setting
		_comp.initialise(difficulty, setting, _parser);		
		Collections.sort(implementations, _comp);
		// reverse order so list is ordered from best to worst
		Collections.reverse(implementations);
	
		// shuffle implementations with equal score to promote variability
		int start = 0; ServiceImplementation temp1, temp2;
		for(int i=0; i<implementations.size()-1; i++){
			temp1 = implementations.get(i);
			temp2 = implementations.get(i+1);
			 // if current and next different shuffle from start to i (inclusive)
			if(_comp.compare(temp1, temp2) != 0 ){
				Collections.shuffle(implementations.subList(start, i+1));
				start = i+1;
			}// else if there is no next after this round shuffle from start to i+1 (inclusive)
			else if((i+1) >= implementations.size()-1){
				Collections.shuffle(implementations.subList(start, i+2));
			}
		}
	}
	
	/**
	 * Selects the ServiceImplementations that can be offered by the objects already added
	 * to the scenario without requiring any new objects having to be added.
	 * @param implementations from which to select
	 * @param time reference to the TimeLine storing the scenario generated so far
	 * @param setting of the scenario
	 * @param difficulty of the scenario
	 * @return 
	 */
	private ArrayList<ServiceImplementation> findNoCostImplementations(ArrayList<ServiceImplementation> implementations, TimeLine time, String setting, int difficulty){
		ArrayList<ServiceImplementation> result = new ArrayList<ServiceImplementation>();
		//consider all ServiceImplementation
		for(ServiceImplementation si : implementations){
			boolean applicable = true;
			// if the ServiceImplementation currently under consideration has no preconditions
			// it is immediately added to the result set 
			if(si.actionIds().isEmpty())
				continue;
			// if not, check if the preconditions can be fulfilled without adding any new objects
			for(String actionId : si.actionIds()){
				for(String serviceId : _parser.getAtomTaskDescById(actionId).getRequiredServiceIds()){
					if(couldServiceBeOffered(time, serviceId, setting, difficulty) == null){
						applicable = false;
						break;
					}
				}
				if(!applicable)
					break;
			}
			if(applicable)
				result.add(si);
		}
		return result;
	}
	
}
