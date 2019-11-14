package scenarioGenerator;

import java.util.ArrayList;

/**
 * Stores the domain knowledge regarding a specific instance of the AtomTask concept.
 */
public class AtomTaskDescription extends TaskDescription{
	
	private ArrayList<String> _serviceIds;

	public AtomTaskDescription(String id, String name, ArrayList<String> serviceIds) {
		super(id, name, new ArrayList<String>(), serviceIds);
		_serviceIds = serviceIds;
	}

	public ArrayList<String> getRequiredServiceIds(){
		return _serviceIds;
	}

}
