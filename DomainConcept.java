package scenarioGenerator;

/**
 * General class that encompasses all concepts parsed from the domain knowledge. All concepts have
 * a name and an id and can be compared based on these properties.
 */
public abstract class DomainConcept {
	
	private String _id;
	private String _name;
	
	public DomainConcept(String id, String name){
		_id = id;
		_name = name;
	}
	
	public String name(){
		return _name;
	}
	
	public String id(){
		return _id;
	}
	
	@Override
	public boolean equals(Object o){
		if(o instanceof DomainConcept){
			DomainConcept other = (DomainConcept) o;
			return _name.equals(other.name()) && _id.equals(other.id());
		}
		return false;
	}

	@Override
	public int hashCode(){
		return _name.hashCode()^_id.hashCode();
	}
}
