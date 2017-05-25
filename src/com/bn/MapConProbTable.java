package com.bn;

import java.util.Map;
import java.util.HashMap;

public class MapConProbTable implements ConditionalProbTable {

	private Map<String, Double> conProbTable;
	private Arity arity;
	private int[] allAttributes; // the first element is the current attribute
									// index
	private int probNum;

	public MapConProbTable(Arity a, int[] allAttr) {
		allAttributes = allAttr;
		initialise(a, allAttr);
	}

	public MapConProbTable(Arity a, int curAttr, int[] conAttr) {
		int[] allAttr = new int[conAttr.length + 1];
		allAttr[0] = curAttr;
		for (int i = 0; i < conAttr.length; i++)
			allAttr[i + 1] = conAttr[i];
		allAttributes = allAttr;
		initialise(a, allAttr);
	}

	private void initialise(Arity a, int[] allAttr) {
		arity = a;
		conProbTable = new HashMap<String, Double>();
		probNum = 1;
		for (int eachAttr : allAttr)
			probNum *= arity.values(eachAttr);

		CartesianProduct cartProd = new CartesianProduct(arity, allAttr);
		for (int[] eachCondition : cartProd)
			conProbTable.put(convertCondition(eachCondition), 0.0);
	}

	private String convertCondition(int[] condition) {
		// a condition has at least two attributes
		if (condition.length < 2)
			return null;

		String conStr = "";
		for (int eachCondition : condition)
			conStr = conStr + String.valueOf(eachCondition) + ",";
		return conStr;
	}

	@Override
	public ProbTabType getTabType() {
		return ProbTabType.ConditionalTab;
	}

	@Override
	public int getProbNum() {
		return probNum;
	}

	@Override
	public int getCurAttribute() {
		return allAttributes[0];
	}

	@Override
	public int[] getConAttributes() {
		int[] conAttr = new int[allAttributes.length - 1];
		for (int i = 1; i < allAttributes.length; i++)
			conAttr[i - 1] = allAttributes[i];
		return conAttr;
	}

	@Override
	public boolean setConProb(int curValue, int[] conValue, double conProb) {
		
		int[] allValue = new int[conValue.length + 1];
		allValue[0] = curValue;
		for (int i = 0; i < conValue.length; i++)
			allValue[i + 1] = conValue[i];
		return setConProb(allValue, conProb);
	}

	@Override
	public boolean setConProb(int[] allValue, double conProb) {
		if (conProb < 0 || conProb > 1)
			return false;
		String condition = convertCondition(allValue);
		//System.out.println("************************************************");
		//System.out.println("condition = "+condition+",probab="+conProb);
		//System.out.println("************************************************");
		if (conProbTable.containsKey(condition)) {
			conProbTable.put(condition, conProb);
			return true;
		} else
			return false;
	}

	@Override
	public double getConProb(int curValue, int[] conValue) {
		int[] allValue = new int[conValue.length + 1];
		allValue[0] = curValue;
		for (int i = 0; i < conValue.length; i++)
			allValue[i + 1] = conValue[i];
		return getConProb(allValue);
	}

	@Override
	public double getConProb(int[] allValue) {
		String condition = convertCondition(allValue);
		if (conProbTable.containsKey(condition))
			return conProbTable.get(condition);
		else
			return -1;
	}

	@Override
	public String AllProb() {
		String str = "";
		CartesianProduct cartProd = new CartesianProduct(arity, allAttributes);
		for (int[] eachCondition : cartProd) {
			String conditionStr = "P(a_" + String.valueOf(allAttributes[0])
					+ "=" + String.valueOf(eachCondition[0]);
			boolean firstVar = true; // used for generating '|' in the condition
			for (int i = 1; i < eachCondition.length; i++) {
				conditionStr = conditionStr + (firstVar ? " | a_" : ", a_")
						+ String.valueOf(allAttributes[i]) + "="
						+ String.valueOf(eachCondition[i]);
				firstVar = false;
			}
			conditionStr = conditionStr
					+ ") = "
					+ String.valueOf(conProbTable
							.get(convertCondition(eachCondition)));
			//System.out.println(conditionStr);
			str += conditionStr+"\n";
		}
		return str;
	}

	@Override
	public boolean isValid() {

		return false;
	}
}
