package scenarioGenerator;

/**
 * Instantiation of a TaskDescription that is (or can be) added to the scenario and has a unique
 * id.
 */
public class Task {
	
	public static int TASKCOUNTER;
	private int _taskId;
	
	private TaskDescription _tDescription;
	
	public Task(TaskDescription d){
		_taskId = TASKCOUNTER;
		TASKCOUNTER++;
		_tDescription = d;
	}
	
	/**
	 * @return unique id
	 */
	public int id(){
		return _taskId;
	}
	
	/**
	 * @return reference to TaskDescription from which this Task is an instantiation
	 */
	public TaskDescription taskDesc(){
		return _tDescription;
	}
	
	
	@Override
	public boolean equals(Object o){
		if(o instanceof Task){
			Task other = (Task) o;
			return _tDescription.equals(other.taskDesc()) && (this.id() == other.id());
		}
		return false;
	}

	@Override
	public int hashCode(){
		return _tDescription.hashCode()^this.id();
	}
}
