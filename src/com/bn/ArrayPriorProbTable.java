package com.bn;


public class ArrayPriorProbTable implements PriorProbTable {

    private double[] priorProbTable;
    private Arity arity;
    private int curAttribute;

    public ArrayPriorProbTable(Arity a, int curAttr) {
	arity = a;
	curAttribute = curAttr;
	priorProbTable = new double[arity.values(curAttr)];
	double defaultProb = 1.0/arity.values(curAttr);
	for (int i=0; i<arity.values(curAttr); i++)
	    priorProbTable[i] = defaultProb;
    }

    @Override
    public ProbTabType getTabType() {
	return ProbTabType.PriorTab;
    }

    @Override
    public int getProbNum() {
	return arity.values(curAttribute);
    }

	@Override
	public String AllProb() {
		String str = "";
		for (int i = 0; i < arity.values(curAttribute); i++) 
		{
			String priorStr = "P(a_" + String.valueOf(curAttribute) + "=" + String.valueOf(i) + ")";
			priorStr = priorStr + " = " + String.valueOf(priorProbTable[i]);
			//System.out.println(String.valueOf(i + 1) + ": " + priorStr);
			str += String.valueOf(i + 1) + ": " + priorStr+"\n";
		}
		return str;
	}

    @Override
    public boolean setPriorProb(int curValue, double priorProb) {
	if (curValue >= 0 && curValue < arity.values(curAttribute)) {
	    priorProbTable[curValue] = priorProb;
	    return true;
	} else
	    return false;
    }

    @Override
    public double getPriorProb(int curValue) {
	if (curValue >= 0 && curValue < arity.values(curAttribute))
	    return priorProbTable[curValue];
	else
	    return -1;
    }
    
    @Override
    public boolean isValid() {
	double sum = 0;
	for (double eachProb : priorProbTable)
	    sum += eachProb;

	if (sum >= 0.9999 && sum <= 1.0001)
	    return true;
	else
	    return false;
    }

}
