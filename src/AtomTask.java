package scenarioGenerator;

/**
 * This class represents a concrete instance of an action (i.e. a task which cannot be decomposed 
 * any further) that is (or can be) added to the scenario.
 */
public class AtomTask extends Task{

	public AtomTask(AtomTaskDescription a){
		super(a);
	}
	
	/**
	 * @return the type of action this instance represents
	 */
	public AtomTaskDescription actionDesc(){
		return (AtomTaskDescription) taskDesc();
	}
}
