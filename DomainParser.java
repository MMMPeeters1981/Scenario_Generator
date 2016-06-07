package scenarioGenerator;

import java.util.ArrayList;

/**
 * Interface describing the functionalities a domain parser should offer.
 *
 */
/**
 * @author Gwen
 *
 */
interface DomainParser {
	
	/**
	 * Parses the domain, storing the domain knowledge in the form of Descriptions into internal
	 * structures, ready to be accessed by the program.
	 * @param file name of input file
	 * @return true if the domain was successfully parsed, false otherwise.
	 */
	public boolean parseDomain(String file);
	
	/**
	 * @param id, of the TaskDescription
	 * @return reference to the TaskDescription
	 */
	public TaskDescription getTaskDescById(String id);
	
	/**
	 * @param id, of the AtomTask
	 * @return reference to the AtomTaskDescription
	 */
	public AtomTaskDescription getAtomTaskDescById(String id);
	
	/**
	 * @param id, of ServiceImplementation
	 * @return reference to the ServiceImplementation
	 */
	public ServiceImplementation getServiceImplById(String id);
	/**
	 * @param smartObjId id of the SmartObjectDescription
	 * @param serviceId, id of the Service that this SmartObject offers
	 * @return references to the ServiceImplementations that detail how the inputted Service is 
	 * offered by this SmartObject
	 */
	public ArrayList<ServiceImplementation> getServiceImplByObj(String smartObjId, String serviceId);
	
	/**
	 * @param id, of DecompositionSchema
	 * @return reference to the DecompositionSchema
	 */
	public DecompositionSchema getDecompSchemaById(String id);
	
	/**
	 * @param id of the SmartObjectDescription
	 * @return reference to the SmartObjectDescription
	 */
	public AbstractSmartObjectDescription getSmartObjById(String id);
	
	/**
	 * @return all ServiceImplementations parsed from the domain knowledge
	 */
	public ArrayList<ServiceImplementation> getAllServiceImplementations();
	
	/**
	 * @param name of the Scenario Template
	 * @return tasks that constitute the Scenario Template
	 */
	public ArrayList<TaskDescription> getScenarioTemplateByName(String name);
	
	/**
	 * @param name of the TaskDescription
	 * @return id of the TaskDescription
	 */
	public String getTaskIdByName(String name);
	
	/**
	 * @param id of the Service
	 * @return reference to the Service
	 */
	public Service getServiceById(String id);

	/**
	 * @param id of the Setting
	 * @return name of the Setting
	 */
	public String getSettingName(String id); 
	
	/**
	 * @param actorType, type of the agent
	 * @return reference to AgentDescriptions that have this type
	 */
	public ArrayList<AgentDescription> getAgentDescByType(String actorType);
	
	/**
	 * @param parent Service that is thought to be the parent of the child
	 * @param childId id of the Service that is thought to be the child
	 * @return true, if the child Service inherits from the parent Service, false otherwise
	 */
	public boolean isServiceChild(Service parent, String childId);
	
	/**
	 * @param obj 
	 * @param actorType
	 * @return true if the SmartObject or any of its parents is of the type specified, false otherwise
	 */
	public boolean checkParentDescriptions(AbstractSmartObjectDescription obj, String actorType);
	
}
