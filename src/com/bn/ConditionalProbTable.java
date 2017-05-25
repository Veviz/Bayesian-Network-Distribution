package com.bn;

public interface ConditionalProbTable extends ProbabilityTable {
   
    /**
     * @return the current attribute index
     */
    public int getCurAttribute();

    /**
     * @return the indexs of all condintional attributes
     */
    public int[] getConAttributes();

    /**
     * @param allValue the array containing all the values [v1, v2, v3] of attributes x, y, z in P(x=v1 | y=v2, z=v3)
     * @param conProb the conditional probability of P(x=v1 | y=v2, z=v3)
     * @return true if the conditional probability is set successfully, return false if the input condition or conProb is invalid
     */
    public boolean setConProb(int[] allValue, double conProb);

    /**
     * @param curValue the value v1 of the attribute x in P(x=v1 | y=v2, z=v3)
     * @param conValue the array containing all the values [v2, v3] of attributes y, z in P(x=v1 | y=v2, z=v3)
     * @param conProb the conditional probability of P(x=v1 | y=v2, z=v3)
     * @return true if the conditional probability is set successfully, return false if the input condition or conProb is invalid
     */
    public boolean setConProb(int curValue, int[] conValue, double conProb);

    /**
     * @param allValue the array containing all the values [v1, v2, v3] of attributes x, y, z in P(x=v1 | y=v2, z=v3)
     * @return the conditional probability, return -1 if the input condition is invalid
     */
    public double getConProb(int[] allValue);

    /**
     * @param curValue the value v1 of the attribute x in P(x=v1 | y=v2, z=v3)
     * @param conValue the array containing all the values [v2, v3] of attributes y, z in P(x=v1 | y=v2, z=v3)
     * @return the conditional probability, return -1 if the input condition is invalid
     */
    public double getConProb(int curValue, int[] conValue);

}
