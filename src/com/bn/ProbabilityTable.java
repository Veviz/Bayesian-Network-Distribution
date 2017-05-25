package com.bn;

public interface ProbabilityTable {

    public enum ProbTabType {PriorTab, ConditionalTab};

    /**
     * @return the probability table type, either PriorTab or ConditionalTab
     */
    public ProbTabType getTabType();

    /**
     * @return the number of possible conditional probabilities
     */
    public int getProbNum();

    /**
     * Print all the possible conditional probabilities in the table.
     */
    public String AllProb();

    /**
     * @return true if the summation of all probabilities is 1, otherwise return false 
     */
    public boolean isValid();

}
