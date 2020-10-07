package aima.core.logic.propositional.algorithms;

public class MyOutput {
	public MyOutput(){
		this.setResult(false);
		this.setNumOfClauses(0);
	}
	public boolean isResult() {
		return result;
	}
	public void setResult(boolean result) {
		this.result = result;
	}
	public int getNumOfClauses() {
		return numOfClauses;
	}
	public void setNumOfClauses(int numOfClauses) {
		this.numOfClauses = numOfClauses;
	}
	
	private boolean result;
	private int numOfClauses;
}
