package scenarioGenerator;

/**
 * Stores information on a concrete instantiation of a SmartObjectDescription that is (or can be) added to the
 * scenario.
 */
public class SmartObject {
	
	public static int OBJECTCOUNTER;
	private int _objId;
	private SmartObjectDescription _objDesc;
	
	public SmartObject(SmartObjectDescription sd){
		_objId = OBJECTCOUNTER;
		OBJECTCOUNTER++;
		_objDesc = sd;
	}
	
	/**
	 * @return id of this specific instance of the SmartObjectDescription
	 */
	public int id(){
		return _objId;
	}
	
	/**
	 * @return reference to the SmartObjectDescription of which this SmartObject is an instantiation
	 */
	public SmartObjectDescription objDesc(){
		return _objDesc;
	}
	
	@Override
	public boolean equals(Object o){
		if(o instanceof SmartObject){
			SmartObject other = (SmartObject) o;
			return _objDesc.equals(other.objDesc()) && (this.id() == other.id());
		}
		return false;
	}

	@Override
	public int hashCode(){
		return _objDesc.hashCode()^this.id();
	}
}
