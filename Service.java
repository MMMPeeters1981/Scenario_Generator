package scenarioGenerator;

import java.util.ArrayList;

/**
 * Represents a Service or interaction possibility that a SmartObject can offer to the trainee
 * or other virtual agents. Services are hierarchically ordered.
 */
public class Service extends DomainConcept{
	
	private String _parentId;
	private ArrayList<String> _childIds;
	
	public Service(String id, String name, String parentId, ArrayList<String> childIds) {
		super(id, name);
		_parentId = parentId;
		_childIds = childIds;
	}
	
	/**
	 * @return id of the Service from which this Service inherits, null if the Service has no
	 * parent 
	 */
	public String parent(){
		return _parentId;
	}
	
	/**
	 * @return IDs of the Services that inherit from this Service
	 */
	public ArrayList<String> childIds(){
		ArrayList<String> copy = new ArrayList<String>();
		copy.addAll(_childIds);
		return copy;
	}

}
