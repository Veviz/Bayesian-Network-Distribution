package com.bn;


public class ArrayConProbTable implements ConditionalProbTable {

    private double[] conProbTable;
    private Arity arity;
    private int currentAttr;
    private int[] conditionalAttr;
    private int entryNum;

    public void MapConProbTable(Arity a, int curAttr, int[] conAttr) {
	arity = a;
	currentAttr = curAttr;
	conditionalAttr = conAttr;
	entryNum = arity.values(curAttr);
	for (int eachArity : conAttr)
	    entryNum *= arity.values(eachArity);
	conProbTable = new double[entryNum];

	for (int conNum = 0; conNum < entryNum; conNum++)
	    conProbTable[conNum] = 0.0;
    }

    public int getEntryNum() {
	return entryNum;
    }

    @Override
    public boolean setConProb(int curValue, int[] conValue, double conProb) {
	if (conProb < 0 || conProb > 1)
	    return false;


	    return false;
    }

    @Override
    public double getConProb(int curValue, int[] conValue) {
	return -1;
    }

    public void printAllEntries() {
    //    	for (int conNum = 0; conNum < entryNum; conNum++) {
    // 	    int conNumCopy = conNum;
    // 	    int curValue = conNumCopy % arity.values(currentAttr);
    // 	    String condition = String.valueOf(curValue);
    // 	    boolean firstVar = true; // used for generating '|' in the condition
    // 	    conNumCopy /= arity.values(currentAttr);
    // 	    for (int eachAttr : conditionalAttr) {
    // 		condition = condition + (firstVar ? "|" : ",") + conNumCopy % arity.values(eachAttr);
    // 		firstVar = false;
    // 		conNumCopy /= arity.values(eachAttr);
    // 	    }
    // 	    System.out.println(conNum + ": P(" + condition + ") = " + conProbTable.get(condition));
    // 	}	
    // }
    }

	@Override
	public ProbTabType getTabType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getProbNum() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String  AllProb() {
		// TODO Auto-generated method stub
		String str = "";
		return str;
	}

	@Override
	public boolean isValid() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getCurAttribute() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int[] getConAttributes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean setConProb(int[] allValue, double conProb) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public double getConProb(int[] allValue) {
		// TODO Auto-generated method stub
		return 0;
	}
    
}
