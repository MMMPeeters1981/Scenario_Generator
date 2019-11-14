package scenarioGenerator;

/**
 * An instance of this class represents a concrete Agent that is (or can be) added to the scenario.
 */
public class Agent extends SmartObject{

	public Agent(AgentDescription ad) {
		super(ad);
	}
	
	public AgentDescription getAgentDesc(){
		return (AgentDescription) objDesc();
	}

}
