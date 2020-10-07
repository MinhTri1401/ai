package aima.core.logic.propositional.visitors;

import java.util.HashSet;
import java.util.Set;

import aima.core.logic.propositional.parsing.ast.BinarySentence;
import aima.core.logic.propositional.parsing.ast.Sentence;
import aima.core.logic.propositional.parsing.ast.Symbol;
import aima.core.logic.propositional.parsing.ast.UnarySentence;

/**
 * @author Ravi Mohan
 * 
 */
public class CNFClauseGatherer extends BasicTraverser {
	AndDetector detector;

	public CNFClauseGatherer() {
		detector = new AndDetector();
	}

	@Override
	public Object visitBinarySentence(BinarySentence bs, Object args) {

		Set<Sentence> soFar = (Set<Sentence>) args;

		Sentence first = bs.getFirst();
		Sentence second = bs.getSecond();
		processSubTerm(second, processSubTerm(first, soFar));

		return soFar;

	}

	public Set<Sentence> getClausesFrom(Sentence sentence) {
//		Set<Sentence> set = new HashSet<Sentence>();
//		if (sentence instanceof Symbol) {
//			set.add(sentence);
//		} else if (sentence instanceof UnarySentence) {
//			set.add(sentence);
//		} else {
//			set = (Set<Sentence>) sentence.accept(this, set);
//		}
//		return set;
	
		//Modify by LTSACH -----------------------MAY 15, 2011
		Set<Sentence> set = new HashSet<Sentence>();
		if (sentence instanceof Symbol) {
			set.add(sentence);
		} else if (sentence instanceof UnarySentence) {
			set.add(sentence);
		} else if(detector.containsEmbeddedAnd(sentence)){
			set = (Set<Sentence>) sentence.accept(this, set);
		}
		else{
			set.add(sentence);
		}
		return set;
		//END-Modify by LTSACH ----------------------- 
	}

	//
	// PRIVATE METHODS
	//
	private Set<Sentence> processSubTerm(Sentence s, Set<Sentence> soFar) {
		if (detector.containsEmbeddedAnd(s)) {
			return (Set<Sentence>) s.accept(this, soFar);
		} else {
			soFar.add(s);
			return soFar;
		}
	}
}