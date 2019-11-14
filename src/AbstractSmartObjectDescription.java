package scenarioGenerator;

import java.util.ArrayList;

/**
 * This class is an encompassing class for all SmartObject types and instantiations.
 */
public class AbstractSmartObjectDescription extends DomainConcept {
	
	private ArrayList<String> _serviceImplementationIds;
	private ArrayList<String> _parentIds;
	
	
	public AbstractSmartObjectDescription(String id, String name, ArrayList<String> serviceIds, ArrayList<String> parentIds) {
		super(id, name);
		_serviceImplementationIds = serviceIds;
		_parentIds = parentIds;
	}
	
	/**
	 * @return service implementations offered by this (type) of object
	 */
	public ArrayList<String> serviceImplementationIds(){
		ArrayList<String> copy = new ArrayList<String>();
		for(String id: _serviceImplementationIds){
			copy.add(id);
		}
		return copy;
	}
	
	/**
	 * @return IDs of abstractSmartObjectDescriptions from which this instance inherits
	 */
	public ArrayList<String> parentIds(){
		ArrayList<String> copy = new ArrayList<String>();
		for(String id: _parentIds){
			copy.add(id);
		}
		return copy;
	}

}
