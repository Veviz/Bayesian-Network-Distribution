package com.bn;

public interface PriorProbTable extends ProbabilityTable {

    /**
     * @param curValue the value v of the attribute x in P(x=v)
     * @param priorProb the prior probability of P(x=v)
     * @return true if the prior probability is set successfully, return false if the input curValue is invalid
     */
    public boolean setPriorProb(int curValue, double priorProb);

    /**
     * @param curValue the value v of the attribute x in P(x=v)
     * @return the prior probability, return -1 if the input curValue is invalid
     */
    public double getPriorProb(int curValue);
}
