package scenarioGenerator;

import java.util.ArrayList;
import java.util.TreeMap;

/**
 * Stores information on a SmartObject as defined in the domain knowledge. Only one SmartObjectDescription
 * exists for every object defined in the domain knowledge. If the object had to be added
 * to the scenario an instantiation of the description is created in the form of a SmartObject.
 *
 */
public class SmartObjectDescription extends AbstractSmartObjectDescription{
	
	private TreeMap<String, Integer> _settingFit;

	public SmartObjectDescription(String id, String name, ArrayList<String> serviceIds, ArrayList<String> parentIds) {
		super(id, name, serviceIds, parentIds);
		_settingFit = new TreeMap<String, Integer>();
	}
	
	public void addSettingFit(String setting, int fit){
		_settingFit.put(setting, fit);
	}
	
	public int fitsInSetting(String setting){
		if(_settingFit.containsKey(setting))
			return _settingFit.get(setting);
		else
			return 0;
	}
}
