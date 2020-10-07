package aima.core.logic.propositional.algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import aima.core.logic.propositional.parsing.PEParser;
import aima.core.logic.propositional.parsing.ast.BinarySentence;
import aima.core.logic.propositional.parsing.ast.Sentence;
import aima.core.logic.propositional.parsing.ast.Symbol;
import aima.core.logic.propositional.parsing.ast.SymbolComparator;
import aima.core.logic.propositional.parsing.ast.UnarySentence;
import aima.core.logic.propositional.visitors.CNFClauseGatherer;
import aima.core.logic.propositional.visitors.CNFTransformer;
import aima.core.logic.propositional.visitors.SymbolClassifier;
import aima.core.util.Converter;
import aima.core.util.SetOps;

/**
 * @author Ravi Mohan
 * 
 */
public class PLResolution {

	public boolean plResolution(KnowledgeBase kb, String alpha) {
		return plResolution(kb, (Sentence) new PEParser().parse(alpha));
	}

	private void dumpClauses(Set<Sentence> clauses){
		//Get and print clauses
		int i = 1;
		Iterator<Sentence> iter = clauses.iterator();
		System.out.println("List of clauses from the input sentence:");
		while(iter.hasNext()){
			Sentence aClause = iter.next();
			String line = "(" + i + ") " + aClause.toString();
			System.out.println(line);
			i++;
		}
		
	}

	public MyOutput plMyResolution(KnowledgeBase kb, Sentence alpha) {
		MyOutput output  = new MyOutput();
		
		Sentence kBAndNotAlpha = new BinarySentence("AND", kb.asSentence(),
				new UnarySentence(alpha));
		Set<Sentence> clauses = new CNFClauseGatherer()
				.getClausesFrom(new CNFTransformer().transform(kBAndNotAlpha));
		clauses = filterOutClausesWithTwoComplementaryLiterals(clauses);
		System.out.println("Clauses--------------: ");
		dumpClauses(clauses);
		output.setNumOfClauses(clauses.size());
		
		Set<Sentence> newClauses = new HashSet<Sentence>();
		while (true) {
			List<List<Sentence>> pairs = getCombinationPairs(new Converter<Sentence>()
					.setToList(clauses));
			
			System.out.println("Nof pairs: " + pairs.size());

			for (int i = 0; i < pairs.size(); i++) {
				List<Sentence> pair = pairs.get(i);
				// System.out.println("pair number" + i+" of "+pairs.size());
				Set<Sentence> resolvents = plResolve(pair.get(0), pair.get(1));
				resolvents = filterOutClausesWithTwoComplementaryLiterals(resolvents);

				if (resolvents.contains(new Symbol("EMPTY_CLAUSE"))) {
					output.setResult(true);
					return output;
				}
				newClauses = SetOps.union(newClauses, resolvents);
				// System.out.println("clauseslist size = " +clauses.size());

			}
			if (SetOps.intersection(newClauses, clauses).size() == newClauses
					.size()) {// subset test
				output.setResult(false);
				return output;
			}
			clauses = SetOps.union(newClauses, clauses);
			clauses = filterOutClausesWithTwoComplementaryLiterals(clauses);
		}

	}

	public boolean plResolution(KnowledgeBase kb, Sentence alpha) {
		Sentence kBAndNotAlpha = new BinarySentence("AND", kb.asSentence(),
				new UnarySentence(alpha));
		Set<Sentence> clauses = new CNFClauseGatherer()
				.getClausesFrom(new CNFTransformer().transform(kBAndNotAlpha));
		clauses = filterOutClausesWithTwoComplementaryLiterals(clauses);
		Set<Sentence> newClauses = new HashSet<Sentence>();
		while (true) {
			List<List<Sentence>> pairs = getCombinationPairs(new Converter<Sentence>()
					.setToList(clauses));

			for (int i = 0; i < pairs.size(); i++) {
				List<Sentence> pair = pairs.get(i);
				// System.out.println("pair number" + i+" of "+pairs.size());
				Set<Sentence> resolvents = plResolve(pair.get(0), pair.get(1));
				resolvents = filterOutClausesWithTwoComplementaryLiterals(resolvents);

				if (resolvents.contains(new Symbol("EMPTY_CLAUSE"))) {
					return true;
				}
				newClauses = SetOps.union(newClauses, resolvents);
				// System.out.println("clauseslist size = " +clauses.size());

			}
			if (SetOps.intersection(newClauses, clauses).size() == newClauses
					.size()) {// subset test
				return false;
			}
			clauses = SetOps.union(newClauses, clauses);
			clauses = filterOutClausesWithTwoComplementaryLiterals(clauses);
		}

	}

		
	public Set<Sentence> plResolve(Sentence clause1, Sentence clause2) {
		Set<Sentence> resolvents = new HashSet<Sentence>();
		ClauseSymbols cs = new ClauseSymbols(clause1, clause2);
		Iterator iter = cs.getComplementedSymbols().iterator();
		while (iter.hasNext()) {
			Symbol symbol = (Symbol) iter.next();
			resolvents.add(createResolventClause(cs, symbol));
		}

		return resolvents;
	}

	public boolean plResolution(String kbs, String alphaString) {
		KnowledgeBase kb = new KnowledgeBase();
		kb.tell(kbs);
		Sentence alpha = (Sentence) new PEParser().parse(alphaString);
		return plResolution(kb, alpha);
	}
	
	//Modified by LTSACH
	public MyOutput plResolution(String premises[], String ec) {
		KnowledgeBase kb = new KnowledgeBase();
		kb.tellAll(premises);
		Sentence alpha = (Sentence) new PEParser().parse(ec);
		return plMyResolution(kb, alpha);
	}

	//END

	//
	// PRIVATE METHODS
	//

	private Set<Sentence> filterOutClausesWithTwoComplementaryLiterals(
			Set<Sentence> clauses) {
		Set<Sentence> filtered = new HashSet<Sentence>();
		SymbolClassifier classifier = new SymbolClassifier();
		Iterator iter = clauses.iterator();
		while (iter.hasNext()) {
			Sentence clause = (Sentence) iter.next();
			Set<Symbol> positiveSymbols = classifier
					.getPositiveSymbolsIn(clause);
			Set<Symbol> negativeSymbols = classifier
					.getNegativeSymbolsIn(clause);
			if ((SetOps.intersection(positiveSymbols, negativeSymbols).size() == 0)) {
				filtered.add(clause);
			}
		}
		return filtered;
	}

	private Sentence createResolventClause(ClauseSymbols cs, Symbol toRemove) {
		List<Symbol> positiveSymbols = new Converter<Symbol>().setToList(SetOps
				.union(cs.clause1PositiveSymbols, cs.clause2PositiveSymbols));
		List<Symbol> negativeSymbols = new Converter<Symbol>().setToList(SetOps
				.union(cs.clause1NegativeSymbols, cs.clause2NegativeSymbols));
		if (positiveSymbols.contains(toRemove)) {
			positiveSymbols.remove(toRemove);
		}
		if (negativeSymbols.contains(toRemove)) {
			negativeSymbols.remove(toRemove);
		}

		Collections.sort(positiveSymbols, new SymbolComparator());
		Collections.sort(negativeSymbols, new SymbolComparator());

		List<Sentence> sentences = new ArrayList<Sentence>();
		for (int i = 0; i < positiveSymbols.size(); i++) {
			sentences.add(positiveSymbols.get(i));
		}
		for (int i = 0; i < negativeSymbols.size(); i++) {
			sentences.add(new UnarySentence(negativeSymbols.get(i)));
		}
		if (sentences.size() == 0) {
			return new Symbol("EMPTY_CLAUSE"); // == empty clause
		} else {
			return LogicUtils.chainWith("OR", sentences);
		}

	}

	private List<List<Sentence>> getCombinationPairs(List<Sentence> clausesList) {
		int odd = clausesList.size() % 2;
		int midpoint = 0;
		if (odd == 1) {
			midpoint = (clausesList.size() / 2) + 1;
		} else {
			midpoint = (clausesList.size() / 2);
		}

		List<List<Sentence>> pairs = new ArrayList<List<Sentence>>();
		for (int i = 0; i < clausesList.size(); i++) {
			for (int j = i; j < clausesList.size(); j++) {
				List<Sentence> pair = new ArrayList<Sentence>();
				Sentence first = clausesList.get(i);
				Sentence second = clausesList.get(j);

				if (!(first.equals(second))) {
					pair.add(first);
					pair.add(second);
					pairs.add(pair);
				}
			}
		}
		return pairs;
	}

	class ClauseSymbols {
		Set<Symbol> clause1Symbols, clause1PositiveSymbols,
				clause1NegativeSymbols;

		Set<Symbol> clause2Symbols, clause2PositiveSymbols,
				clause2NegativeSymbols;

		Set<Symbol> positiveInClause1NegativeInClause2,
				negativeInClause1PositiveInClause2;

		public ClauseSymbols(Sentence clause1, Sentence clause2) {

			SymbolClassifier classifier = new SymbolClassifier();

			clause1Symbols = classifier.getSymbolsIn(clause1);
			clause1PositiveSymbols = classifier.getPositiveSymbolsIn(clause1);
			clause1NegativeSymbols = classifier.getNegativeSymbolsIn(clause1);

			clause2Symbols = classifier.getSymbolsIn(clause2);
			clause2PositiveSymbols = classifier.getPositiveSymbolsIn(clause2);
			clause2NegativeSymbols = classifier.getNegativeSymbolsIn(clause2);

			positiveInClause1NegativeInClause2 = SetOps.intersection(
					clause1PositiveSymbols, clause2NegativeSymbols);
			negativeInClause1PositiveInClause2 = SetOps.intersection(
					clause1NegativeSymbols, clause2PositiveSymbols);

		}

		public Set<Symbol> getComplementedSymbols() {
			return SetOps.union(positiveInClause1NegativeInClause2,
					negativeInClause1PositiveInClause2);
		}

	}
}