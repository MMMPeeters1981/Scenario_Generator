package scenarioGenerator;

import java.io.File;
import java.util.ArrayList;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Domain parser that can parse xml files that are the output of Protege
 *
 */
public class ProtegeDomainParser implements DomainParser{
	
	private TreeMap<String, TaskDescription> _taskDescriptions;
	private TreeMap<String, ServiceImplementation> _serviceImplementations;
	private TreeMap<String, ArrayList<String>> _scenarioTemplates;
	private TreeMap<String, AbstractSmartObjectDescription>_smartObjects;
	private TreeMap<String, AbstractSmartObjectDescription> _agents;
	private TreeMap<String, DecompositionSchema> _decompSchemas;
	private TreeMap<String, Service> _services;
	private TreeMap<String, String> _settings;

	public ProtegeDomainParser(){
		_taskDescriptions = new TreeMap<String, TaskDescription>();
		_serviceImplementations = new TreeMap<String, ServiceImplementation>();
		_scenarioTemplates = new TreeMap<String, ArrayList<String>>();
		_smartObjects = new TreeMap<String, AbstractSmartObjectDescription>();
		_agents = new TreeMap<String, AbstractSmartObjectDescription>();
		_decompSchemas = new TreeMap<String, DecompositionSchema>();
		_services = new TreeMap<String, Service>();
		_settings = new TreeMap<String, String>();
	}
	
	public boolean parseDomain(String file) {
		try {
			// setup document
			File xmlFile =  new File(file);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(xmlFile);
			doc.getDocumentElement().normalize();
			
			// parse instances
			NodeList instanceList = doc.getElementsByTagName("simple_instance");
			// loop through all instances to parse services and settings 
			// (do not require information from other items)
			for(int i=0; i<instanceList.getLength(); i++){
				Node curNode = instanceList.item(i);
				NodeList childNodes = curNode.getChildNodes();
				for(int j=0; j<childNodes.getLength(); j++){
					if(childNodes.item(j).getNodeName().equalsIgnoreCase("type")){
						//determine instance class and parse accordingly
						String type = childNodes.item(j).getFirstChild().getNodeValue();
						if(type.equalsIgnoreCase("Service")){
							parseService(curNode);
						}else if(type.equalsIgnoreCase("Setting")){
							parseSetting(curNode);
						}else if(type.equalsIgnoreCase("AtomTask")){
							parseAtomTask(curNode);
						}else if(type.equalsIgnoreCase("Task")){
							parseTask(curNode);
						}else if(type.equalsIgnoreCase("SmartObject")){
							parseSmartObject(curNode);
						}else if(type.equalsIgnoreCase("Agent")){
							parseAgent(curNode);
						}else if(type.equalsIgnoreCase("SmartObjectType")){
								parseSmartObjectType(curNode);
						}else if(type.equalsIgnoreCase("AgentType")){
							parseAgentType(curNode);
						}else if(type.equalsIgnoreCase("ServiceImplementation")){
							parseServiceImplementation(curNode);
						}else if(type.equalsIgnoreCase("DecompositionSchema")){
							parseDecompositionSchema(curNode);
						}else if(type.equalsIgnoreCase("ScenarioTemplate")){
							parseScenarioTemplate(curNode);
						}else
							break;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * Helper function to parse the specified slot values of a specific node
	 * @param node
	 * @param slotNames
	 * @return
	 */
	private ArrayList<ArrayList<String>> parseSlotValues(Node node, String...slotNames){
		ArrayList<ArrayList<String>> values = new ArrayList<ArrayList<String>>(slotNames.length);
		for(int i=0; i<slotNames.length; i++){
			values.add(new ArrayList<String>());
		}
		// find all slot nodes
		if(node.getNodeType() == Node.ELEMENT_NODE){
			Element el = (Element) node;
			NodeList slots = el.getElementsByTagName("own_slot_value");
			// for each slot
			for(int i=0; i<slots.getLength(); i++){
				if(slots.item(i).getNodeType() == Node.ELEMENT_NODE){
					Element slot = (Element) slots.item(i);
					// determine slot type
					NodeList names = slot.getElementsByTagName("slot_reference");
					int typeIndex = -1;
					for(int j=0; j<slotNames.length; j++){
						if(names.item(0).getFirstChild().getNodeValue().equalsIgnoreCase(slotNames[j])){
							typeIndex = j;
						}
					}
					// get value(s)
					if(typeIndex >= 0){
						NodeList slotValues = slot.getElementsByTagName("value");
						for(int j=0; j<slotValues.getLength(); j++){
							values.get(typeIndex).add(slotValues.item(j).getFirstChild().getNodeValue());
						}
					}
				}
			}
		}
		return values;
	}
	
	private String parseId(Node node){
		if(node.getNodeType() == Node.ELEMENT_NODE){
			Element el = (Element) node;
			return el.getElementsByTagName("name").item(0).getFirstChild().getNodeValue();
		}
		return null;
	}
	
	private boolean parseService(Node node){
		// parse id
		String id = parseId(node);
		// parse slots
		String[] slots = {"name", "service_parent", "service_children"};
		ArrayList<ArrayList<String>> slotValues = parseSlotValues(node, slots);
		String name = slotValues.get(0).get(0);
		String parent = "";
		if(slotValues.get(1).size() > 0)
			parent = slotValues.get(1).get(0);
		ArrayList<String> childrenIds = slotValues.get(2);
		
		// if not all required information found return null
		if(name.isEmpty() || id.isEmpty())
			return false;
		Service service = new Service(id, name, parent, childrenIds);
		_services.put(id, service);
		return true;
	}
	
	private boolean parseSetting(Node node){
		// parse id
		String id = parseId(node);
		// parse slots
		String[] slots = {"name"};
		ArrayList<ArrayList<String>> slotValues = parseSlotValues(node, slots);
		String name = slotValues.get(0).get(0);
		
		// if not all required information found return null
		if(name.isEmpty() || id.isEmpty())
			return false;
		
		_settings.put(id, name);
		return true;
	}
	
	private boolean parseScenarioTemplate(Node node){
		// parse id
		String id = parseId(node);
		// parse slots
		String[] slots = {"name", "tasks"};
		ArrayList<ArrayList<String>> slotValues = parseSlotValues(node, slots);
		String name = slotValues.get(0).get(0);
		ArrayList<String> tasks = slotValues.get(1);
		
		// if not all required information found return null
		if(name.isEmpty() || id.isEmpty())
			return false;
		
		_scenarioTemplates.put(name, tasks);
		return true;
	}
	
	private boolean parseSmartObject(Node node){
		// parse id
		String id = parseId(node);
		// parse slots
		String[] slots = {"name", "offers", "parents", "expected_settings", "unexpected_settings"};
		ArrayList<ArrayList<String>> slotValues = parseSlotValues(node, slots);
		String name = slotValues.get(0).get(0);
		ArrayList<String> serviceIds = slotValues.get(1);
		ArrayList<String> parentIds = slotValues.get(2);
		ArrayList<String> eSettings = slotValues.get(3);
		ArrayList<String> uSettings = slotValues.get(4);
		
		// if not all required information found return null
		if(name.isEmpty() || id.isEmpty())
			return false;
		
		// create object from information
		SmartObjectDescription obj = new SmartObjectDescription(id, name, serviceIds, parentIds);
		for(String eSetting : eSettings){
			obj.addSettingFit(eSetting, 1);
		}
		for(String uSetting : uSettings){
			obj.addSettingFit(uSetting, -1);
		}
		
		_smartObjects.put(id, obj);
		return true;
	}
	
	private boolean parseAgent(Node node){
		// parse id
		String id = parseId(node);
		// parse slots
		String[] slots = {"name", "offers", "parents", "expected_settings", "unexpected_settings", "type"};
		ArrayList<ArrayList<String>> slotValues = parseSlotValues(node, slots);
		String name = slotValues.get(0).get(0);
		ArrayList<String> serviceIds = slotValues.get(1);
		ArrayList<String> parentIds = slotValues.get(2);
		ArrayList<String> eSettings = slotValues.get(3);
		ArrayList<String> uSettings = slotValues.get(4);
		String type = slotValues.get(5).get(0);
		
		// if not all required information found return null
		if(name.isEmpty() || id.isEmpty())
			return false;
		
		// create object from information
		AgentDescription agent = new AgentDescription(id, name, serviceIds, parentIds, type);
		for(String eSetting : eSettings){
			agent.addSettingFit(eSetting, 1);
		}
		for(String uSetting : uSettings){
			agent.addSettingFit(uSetting, -1);
		}

		_smartObjects.put(id, agent);
		_agents.put(id, agent);
		return true;
	}
	
	private boolean parseSmartObjectType(Node node){
		// parse id
		String id = parseId(node);
		// parse slots
		String[] slots = {"name", "offers", "parents", "children"};
		ArrayList<ArrayList<String>> slotValues = parseSlotValues(node, slots);
		String name = slotValues.get(0).get(0);
		ArrayList<String> serviceIds = slotValues.get(1);
		ArrayList<String> parentIds = slotValues.get(2);
		ArrayList<String> childrenIds = slotValues.get(3);

		
		// if not all required information found return null
		if(name.isEmpty() || id.isEmpty())
			return false;
		
		// create object from information
		SmartObjectTypeDescription obj = new SmartObjectTypeDescription(id, name, serviceIds, parentIds, childrenIds);

		_smartObjects.put(id, obj);
		return true;
	}
	
	private boolean parseAgentType(Node node){
		// parse id
		String id = parseId(node);
		// parse slots
		String[] slots = {"name", "offers", "parents", "children", "type"};
		ArrayList<ArrayList<String>> slotValues = parseSlotValues(node, slots);
		String name = slotValues.get(0).get(0);
		ArrayList<String> serviceIds = slotValues.get(1);
		ArrayList<String> parentIds = slotValues.get(2);
		ArrayList<String> childrenIds = slotValues.get(3);
		String type = slotValues.get(4).get(0);

		
		// if not all required information found return null
		if(name.isEmpty() || id.isEmpty())
			return false;
		
		// create object from information
		AgentTypeDescription agentType = new AgentTypeDescription(id, name, serviceIds, parentIds, childrenIds, type);

		_smartObjects.put(id, agentType);
		_agents.put(id, agentType);
		return true;
	}
	
	private boolean parseServiceImplementation(Node node){
		// parse id
		String id = parseId(node);
		// parse slots
		String[] slots = {"name", "implements", "smartObject", "requires", "actors", "constraints", "min_difficulty", "max_difficulty"};
		ArrayList<ArrayList<String>> slotValues = parseSlotValues(node, slots);
		String name = slotValues.get(0).get(0);
		String service = slotValues.get(1).get(0);
		String objId = slotValues.get(2).get(0);
		ArrayList<String> actIds = slotValues.get(3);
		ArrayList<String> actorTypes = slotValues.get(4);	
		ArrayList<String> constraints = slotValues.get(5);
		// create ServiceImplementation Object
		if(objId != null && service != null){
			ServiceImplementation si = new ServiceImplementation(id, name, service, objId, actIds, actorTypes, constraints, Integer.parseInt(slotValues.get(6).get(0)), Integer.parseInt(slotValues.get(7).get(0)));
			_serviceImplementations.put(id, si);
			return true;
		}
		return false;
	}
	
	private boolean parseDecompositionSchema(Node node){
		// parse id
		String id = parseId(node);
		// parse slots
		String[] slots = {"name", "decomposition", "precondition_services", "precondition_objects", "task", "min_difficulty", "max_difficulty"};
		ArrayList<ArrayList<String>> slotValues = parseSlotValues(node, slots);
		String name = slotValues.get(0).get(0);
		ArrayList<String> decomposition = slotValues.get(1);
		ArrayList<String> preconServices = slotValues.get(2);
		ArrayList<String> preconObjects = slotValues.get(3);
		String  task = slotValues.get(4).get(0);
				
		// create ServiceImplementation Object
		if(id != null && name != null){
			DecompositionSchema schema = new DecompositionSchema(id, name, task, decomposition, preconServices, preconObjects, Integer.parseInt(slotValues.get(5).get(0)), Integer.parseInt(slotValues.get(6).get(0)));
			_decompSchemas.put(id, schema);
			return true;
		}
		return false;
	}
	
	private boolean parseAtomTask(Node node){
		// parse id
		String id = parseId(node);
		// parse slots
		String[] slots = {"name", "uses"};
		ArrayList<ArrayList<String>> slotValues = parseSlotValues(node, slots);
		String name = slotValues.get(0).get(0);
		ArrayList<String> serviceIds = slotValues.get(1);
		
		// create Action object
		if(name != null){
			AtomTaskDescription action = new AtomTaskDescription(id, name, serviceIds);
			_taskDescriptions.put(id, action);
			return true;
		}
		return false;
	}
	
	private boolean parseTask(Node node){
		// parse id
		String id = parseId(node);
		// parse slots
		String[] slots = {"name", "decompositions", "uses"};
		ArrayList<ArrayList<String>> slotValues = parseSlotValues(node, slots);
		String name = slotValues.get(0).get(0);
		ArrayList<String> decompIds = slotValues.get(1);
		ArrayList<String> serviceIds = slotValues.get(2);
		
		// create Action object
		if(name != null){
			TaskDescription task = new TaskDescription(id, name, decompIds, serviceIds);
			_taskDescriptions.put(id, task);
			return true;
		}
		return false;
	}
	
	@Override
	public ArrayList<ServiceImplementation> getAllServiceImplementations() {
		return new ArrayList<ServiceImplementation>(_serviceImplementations.values());
	}
	
	@Override
	public ServiceImplementation getServiceImplById(String id) {
		return _serviceImplementations.get(id);
	}
	
	@Override
	public ArrayList<TaskDescription> getScenarioTemplateByName(String name) {
		ArrayList<TaskDescription> tasks = new ArrayList<TaskDescription>();
		if(_scenarioTemplates.get(name) == null)
			return null;
		for(String id : _scenarioTemplates.get(name)){
			tasks.add(_taskDescriptions.get(id));
		}
		return tasks;
	}
	
	@Override
	public String getTaskIdByName(String name){
		for(TaskDescription task : _taskDescriptions.values()){
			if(task.name().equalsIgnoreCase(name))
				return task.id();
		}
		return null;
	}
	
	
	public String getSettingByName(String name){
		for(String settingId : _settings.keySet()){
			if(_settings.get(settingId).equalsIgnoreCase(name))
				return settingId;
		}
		return null;
			
	}
	@Override
	public TaskDescription getTaskDescById(String id) {
		return _taskDescriptions.get(id);
	}

	@Override
	public AtomTaskDescription getAtomTaskDescById(String id) {
		TaskDescription task = _taskDescriptions.get(id);
		if(task != null && !task.isComplexTask())
			return (AtomTaskDescription) task;
		return null;
	}

	@Override
	public AbstractSmartObjectDescription getSmartObjById(String id) {
		return _smartObjects.get(id);
	}

	@Override
	public Service getServiceById(String id) {
		return _services.get(id);
	}

	@Override
	public String getSettingName(String id) {
		return _settings.get(id);
	}

	@Override
	public DecompositionSchema getDecompSchemaById(String id) {
		return _decompSchemas.get(id);
	}

	@Override
	public ArrayList<ServiceImplementation> getServiceImplByObj(String smartObjId, String serviceId){
		ArrayList<ServiceImplementation> implementations = new ArrayList<ServiceImplementation>();
		AbstractSmartObjectDescription so = _smartObjects.get(smartObjId);
		Service service = getServiceById(serviceId);
		if(so != null && service != null){
			for(String implId : so.serviceImplementationIds()){
				ServiceImplementation si = _serviceImplementations.get(implId);
				if(si.service().equalsIgnoreCase(serviceId))
					implementations.add(si);
				else if(isServiceChild(service, si.service())){
					implementations.add(si);
				}
			}
			for(String parentId : so.parentIds()){
				implementations.addAll(getServiceImplByObj(parentId, serviceId));
			}
		}
		return implementations;
	}

	@Override
	public boolean isServiceChild(Service parent, String childId){
		for(String serviceId : parent.childIds()){
			if(serviceId.equalsIgnoreCase(childId))
				return true;
			else{
				Service service = getServiceById(serviceId);
				if(isServiceChild(service, childId))
					return true;
			}
		}
		return false;
	}

	@Override
	public ArrayList<AgentDescription> getAgentDescByType(String actorType) {
		ArrayList<AgentDescription> descriptions = new ArrayList<AgentDescription>();
		for(AbstractSmartObjectDescription obj : _agents.values()){
			if(obj instanceof AgentDescription){
				if(checkParentDescriptions(obj, actorType))
					descriptions.add((AgentDescription)obj);
			}
		}
		return descriptions;
	}
	
	public boolean checkParentDescriptions(AbstractSmartObjectDescription obj, String actorType){
		//check object
		if(obj instanceof AgentTypeDescription){
			if(((AgentTypeDescription) obj).type().equalsIgnoreCase(actorType))
				return true;
		}
		else if(obj instanceof AgentDescription){
			if(((AgentDescription) obj).type().equalsIgnoreCase(actorType))
				return true;
		}
		//check parents
		for(String aId : obj.parentIds()){
			if(_agents.get(aId) == null)
				continue;
			if(checkParentDescriptions(_agents.get(aId), actorType))
				return true;
		}
		return false;
	}

}
