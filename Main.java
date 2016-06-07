package scenarioGenerator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;


public class Main {
	
	/**
	 * Entry point of program
	 * @param args
	 * - domain knowledge file name
	 * - scenario template name
	 * - critical task name
	 * - critical task difficulty
	 * - setting
	 */
	public static void main(String[] args) {
		
		// parse domain knowledge
		ProtegeDomainParser parser = new ProtegeDomainParser();
		if(!parser.parseDomain("SO_ontology.xml")){
			System.err.println("Error occured during domain parsing");
			System.exit(1);
		}
		
		// hard coded input for quick testing
		String[] scenarioTemplates = {"basic", "basic", "basic", "basic", "basic", "basic", "basic", "basic", "basic", "basic", "basic", "basic"};
		String[] criticalTasks = {"calm_person", "calm_person", "calm_person", "calm_person", "treat_burn", "treat_burn", "treat_burn", "treat_burn", "ensure_ABC", "ensure_ABC","ensure_ABC","ensure_ABC"};
		int[] difficulties = {2, 2, 1, 1, 2, 2, 1, 1, 2, 2, 1, 1};
		String[] settings = {"Park", "Home", "Restaurant", "Laboratorium", "Laboratorium", "Restaurant", "Park", "Home","Home", "Restaurant", "Laboratorium", "Park"};
		
		String[] output = new String[60];
		
		// generate scenarios
		for(int scenarioNo=0; scenarioNo<scenarioTemplates.length; scenarioNo++){
			// additional loop to create 5 scenarios for every input combination
			for(int r=0; r<5; r++){
			
				// init program classes
				TimeLine timeLine = new TimeLine(parser);
				SmartObjectSelector objSelector = new SmartObjectSelector(timeLine, parser);
				TaskDecomposer decomposer = new TaskDecomposer(timeLine, objSelector, parser);
				
				// get scenario template
				//ArrayList<TaskDescription> template = parser.getScenarioTemplateByName(args[1]);
				ArrayList<TaskDescription> template = parser.getScenarioTemplateByName(scenarioTemplates[scenarioNo]);
				if(template == null){
					System.err.println("Scenario template not recognised");
					System.exit(1);
				}
				ArrayList<Task> tasks = timeLine.addTemplate(template);
				
				// parse critical task
				//String taskId = parser.getTaskIdByName(args[2]);
				String taskId = parser.getTaskIdByName(criticalTasks[scenarioNo]);
				if(taskId == null){
					System.err.println("Critical task not recognised");
					System.exit(1);
				}
				
				// parse difficulty level
				int difficulty = 0;
				try{
					 //difficulty = Integer.parseInt(args[3]);
					difficulty = difficulties[scenarioNo];
				}
				catch(NumberFormatException e){
					System.err.println("Could not parse difficulty level");
					System.exit(1);
				}
				// parse setting
				String setting = parser.getSettingByName(settings[scenarioNo]);
				if(setting ==  null){
					System.err.println("Could not parse setting level");
					System.exit(1);
				}
				
				// fit critical task into scenario template
				int cIndex = -1;
				ArrayList<DecompositionSchema> decomp = new ArrayList<DecompositionSchema>();
					// check all top level tasks from scenario template
				for(int i=0; i<tasks.size(); i++){
					if(tasks.get(i).taskDesc().id().equalsIgnoreCase(taskId)){
						cIndex = i;
						break;
					}
				}
					// else check all decomposition trees of top level tasks
				if(cIndex < 0){
					decomp = findDecompCriticalTask(parser, tasks, taskId, decomposer, difficulty);
					if(decomp == null){
						System.err.println("Could not fit task into scenario template");
						System.exit(1);
					}
				}
				if(decomp.size() == 0 && cIndex < 0){
					System.err.println("Could not fit task into scenario template");
					System.exit(1);
				}
				if(decomp.size() > 0){
					for(int i=0; i<tasks.size(); i++){
						if(tasks.get(i).taskDesc().id().equalsIgnoreCase(decomp.get(0).getTaskId())){
							cIndex = i;
							break;
						}
					}
				}
				if(cIndex < 0){
					System.err.println("Could not fit task into scenario template");
					System.exit(1);
				}
				
				// decompose critical task
				if(decomp.size() > 0)
					decomposer.decomposeTaskFixed(tasks.get(cIndex), difficulty, setting, decomp);
				else
					decomposer.decomposeTask(tasks.get(cIndex), difficulty, setting, false);
				
				// decompose additional tasks from scenario template
				for(int i=cIndex-1; i>=0; i--){
					decomposer.decomposeTask(tasks.get(i), difficulty, setting, true);
				}
				for(int i=cIndex+1; i<tasks.size(); i++){
					decomposer.decomposeTask(tasks.get(i), difficulty, setting, true);
				}
				String heading = "Task to train: "+criticalTasks[scenarioNo]+" in the "+parser.getSettingName(setting)+" at level: "+difficulties[scenarioNo]+"\r\n";
				output[scenarioNo*5 + r] = heading + timeLine.printScenario() +"\r\n -------------------------------------------------- \r\n";
			}
		}
			
		int[] randomnr = new int[12];
		Random randomGenerator = new Random();
		for(int r=0; r<12; r++){
			randomnr[r] = randomGenerator.nextInt(5);
		}
		// print output
		try {
			FileWriter fstream = new FileWriter("scenario.txt");
			BufferedWriter out = new BufferedWriter(fstream);
			// write selected scenarios
			out.write("---------------------------------------------------------------\r\n Selected scenarios: \r\n -------------------------------------------------------\r\n");
			for(int index=0; index<randomnr.length; index++){
				out.write("ScenarioNo: "+index + " iteration: "+ randomnr[index] + " " + output[index*5 + randomnr[index]]);
			}
			// write all scenarios
			out.write("---------------------------------------------------------------\r\n All scenarios: \r\n --------------------------------------------------------------- \r\n");
			for(String s : output){
				out.write(s);
			}
			// write random numbers for selection check
			for(int i : randomnr){
				out.write(i+"\r\n");
			}
			out.close();
		}catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	
	
	/**
	 * Brute force fitting of the target task
	 * @param parser, reference to domain parse
	 * @param template, scenario template
	 * @param taskId, target task id
	 * @param decomposer, reference to decomposer class
	 * @param difficulty, desired difficulty for scenario
	 * @return
	 */
	private static ArrayList<DecompositionSchema> findDecompCriticalTask(DomainParser parser, ArrayList<Task> template, String taskId, TaskDecomposer decomposer,int difficulty){
		MultiValueTreeMap<String, DecompositionSchema> decompMap = new MultiValueTreeMap<String, DecompositionSchema>();
		ArrayList<DecompositionSchema> decomps = new ArrayList<DecompositionSchema>();
		ArrayList<String> taskIds = new ArrayList<String>();
		for(Task t : template){
			taskIds.add(t.taskDesc().id());
		}
		ArrayList<String> temp = new ArrayList<String>();
		boolean taskFound = false;
		//build lookup table of all possible decompositions of the template tasks
		while(!taskIds.isEmpty()){
			for(String tId : taskIds){
				if(tId.equalsIgnoreCase(taskId)){
					taskFound = true;
					continue;
				}
				else{
					ArrayList<String> decomp = parser.getTaskDescById(tId).getAllTaskDecompositions(); 
					for(String d : decomp){
						DecompositionSchema schema = parser.getDecompSchemaById(d);
						ArrayList<String> decompTasks = schema.getDecomposition();
						for(String dTask : decompTasks){
							temp.add(dTask);
							decompMap.put(dTask, schema);
						}
					}
				}
			}
			taskIds = temp;
			temp = new ArrayList<String>();
		}
		// find target task in lookup table and follow pointers back to original template task
		if(taskFound){
			// track decomps to reach critical task
			String tId = taskId;
			while(decompMap.get(tId) != null){
				ArrayList<DecompositionSchema> schemas = decompMap.get(tId);
				Collections.shuffle(schemas);
				decomposer.sortDecompositions(schemas, difficulty);
				decomps.add(schemas.get(0));
				tId = schemas.get(0).getTaskId();
			}
			Collections.reverse(decomps);
			return decomps;
		}
		return null;
	}


}
