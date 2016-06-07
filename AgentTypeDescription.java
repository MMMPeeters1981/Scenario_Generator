package scenarioGenerator;

import java.util.ArrayList;

/**
 * Similar to SmartObjectTypeDescriptions this class supports inheritance between Agents.
 */
public class AgentTypeDescription extends SmartObjectTypeDescription{
	
	private String _type;

	public AgentTypeDescription(String id, String name, ArrayList<String> serviceIds, ArrayList<String> parentIds, ArrayList<String> childrenIds, String type) {
		super(id, name, serviceIds, parentIds, childrenIds);
		_type = type;
	}
	
	public String type(){
		return _type;
	}
}
