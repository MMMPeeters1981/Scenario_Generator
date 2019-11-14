package scenarioGenerator;

import java.util.Comparator;

/**
 * Compares ServiceImplementations based on how closely they match the desired difficulty level
 * an how well the associated SmartObjects fits in the specified Setting. Should be initialised with
 * the desired difficulty level and Setting before use.
 */
public class ServiceImplementationComparator implements Comparator<ServiceImplementation>{
	
	private int _requestedDifficulty;
	private String _setting; 
	private DomainParser _parser;
	
	public void initialise(int difficulty, String setting, DomainParser parser){
		_requestedDifficulty = difficulty;
		_setting = setting;
		_parser = parser;
	}

	
	@Override
	public int compare(ServiceImplementation si1, ServiceImplementation si2) {
		
		// check if comparator is initialised, if not all implementations are equally
		// suitable
		if(_setting == null)
			return 0;
		
		int si1Score; int si2Score;
		
		// first compare based on likelihood in setting
		int maxScore = 1;
		si1Score = bestFit(_parser.getSmartObjById(si1.objectId()), maxScore);
		si2Score = bestFit(_parser.getSmartObjById(si2.objectId()), maxScore);
		if(si1Score < si2Score)
			return -1; // si1 is not as suitable as si2
		else if(si1Score > si2Score)
			return 1; // si1 is better suitable than si2 
				
		// suitability based on setting equal, compare difficulty
		si1Score = difficultyDifference(si1.minDifficulty(), si1.maxDifficulty());
		si2Score = difficultyDifference(si2.minDifficulty(), si2.maxDifficulty());
		if(si1Score > si2Score)
			return -1; // the difference is bigger therefore si1 is not as suitable as si2
		else if(si1Score < si2Score)
			return 1; // the difference is smaller therefore si1 is better suitable than si2 
		
		// there is no difference in suitability
		return 0;
	}
	
	/**
	 * Helper function that determines the distance between a specific difficulty value and a range
	 * of difficulty values
	 * @param min value of the range of difficulty values
	 * @param max value of the range of difficulty values
	 * @return
	 */
	private int difficultyDifference(int min, int max){
		if(_requestedDifficulty <= max && _requestedDifficulty >= min)
			return 0; //requested difficulty falls within range
		else return Math.min(Math.abs(_requestedDifficulty - min), Math.abs(_requestedDifficulty - max));
	}
	
	/**
	 * Helper function that scores how well the SmartObject fits in the specified Setting
	 * @param asotd, the AbstractSmartObjectDescription of the ServiceImplementation
	 * @param maxScore, the best score that can be achieved by an object (1 in the current implementation)
	 * @return
	 */
	private int bestFit(AbstractSmartObjectDescription asotd, int maxScore){
		int score = Integer.MIN_VALUE;
		if(asotd instanceof SmartObjectDescription){
			score = ((SmartObjectDescription)asotd).fitsInSetting(_setting);
		}
		else {
			SmartObjectTypeDescription sotd = (SmartObjectTypeDescription) asotd;
			int tempScore;
			for(String child : sotd.childrenIds()){
				tempScore = bestFit(_parser.getSmartObjById(child), maxScore);
				if(tempScore > score){
					score = tempScore;
					if(score == maxScore)
						break;
				}
			}
		}
		return score;
		
	}
}



