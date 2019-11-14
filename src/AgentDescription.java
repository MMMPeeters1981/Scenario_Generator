package scenarioGenerator;

import java.util.ArrayList;

/**
 * This class describes a type of agent stored in the domain knowledge.
 *
 */
public class AgentDescription extends SmartObjectDescription{
	
	private String _type;

	public AgentDescription(String id, String name, ArrayList<String> serviceIds, ArrayList<String> parentIds, String type) {
		super(id, name, serviceIds, parentIds);
		_type = type;
	}
	
	public String type(){
		return _type;
	}

}
