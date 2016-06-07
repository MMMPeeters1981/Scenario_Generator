package scenarioGenerator;

import java.util.Comparator;

/**
 * Comparator that compares the applicability of two DecompositionSchemas based on the desired
 * difficulty level. Must be instantiated with the difficulty level before comparisons can be 
 * made.
 */
public class DecompositionComparator implements Comparator<DecompositionSchema> {
	
	private int _requestedDifficulty;
	
	/**
	 * The comparator must be initialised with the desired difficulty level
	 * @param difficulty, desired difficulty level of schema
	 */
	public void initialise(int difficulty){
		_requestedDifficulty = difficulty;
	}

	@Override
	public int compare(DecompositionSchema decomp1, DecompositionSchema decomp2) {
		
		int si1Score; int si2Score;
		// first check difficulty
		si1Score = difficultyDifference(decomp1.minDifficulty(), decomp1.maxDifficulty());
		si2Score = difficultyDifference(decomp2.minDifficulty(), decomp2.maxDifficulty());
		if(si1Score > si2Score)
			return -1; // the difference is bigger therefore si1 is not as suitable as si2
		else if(si1Score < si2Score)
			return 1; // the difference is smaller therefore si1 is better suitable than si2 
		
		// there is no difference in suitability
		return 0;
	}
	
	/**
	 * Helper function to compute the distance between a specific difficulty value and
	 * a range of values.
	 * @param min value of the difficulty range
	 * @param max value of the difficulty range
	 * @return
	 */
	private int difficultyDifference(int min, int max){
		if(_requestedDifficulty <= max && _requestedDifficulty >= min)
			return 0; //requested difficulty falls within range
		else return Math.min(Math.abs(_requestedDifficulty - min), Math.abs(_requestedDifficulty - max));
	}
}

