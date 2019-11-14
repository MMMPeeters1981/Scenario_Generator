package scenarioGenerator;

import java.util.ArrayList;

/**
 * This class supports the hierarchical ordering of Smart Objects. A Smart ObjectType 
 * represents a class of objects that all support a specific service. Any object inheriting
 * from this object automatically also supports this service.
 *
 */
public class SmartObjectTypeDescription extends AbstractSmartObjectDescription{
	
	private ArrayList<String> _childrenIds;

	public SmartObjectTypeDescription(String id, String name, ArrayList<String> serviceIds, ArrayList<String> parentIds, ArrayList<String> childrenIds) {
		super(id, name, serviceIds, parentIds);
		_childrenIds = childrenIds;	
	}
	
	public ArrayList<String> childrenIds(){
		ArrayList<String> copy = new ArrayList<String>();
		for(String id: _childrenIds){
			copy.add(id);
		}
		return copy;
	}

}
