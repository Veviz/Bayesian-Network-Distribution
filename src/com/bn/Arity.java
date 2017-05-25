package com.bn;

public class Arity {

	private int dimension;
	private String[] arityNames;
	private int[] arityValues;

	public Arity(int d) {
		dimension = d;
		arityNames = new String[dimension];
		arityValues = new int[dimension];

		for (int i = 0; i < dimension; i++) {
			arityNames[i] = "";
			arityValues[i] = 0;
		}
	}

	public Arity(int d, String[] names, int[] values) {
		dimension = d;
		arityNames = names;
		arityValues = values;
	}

	public int getDimension() {
		return dimension;
	}

	public boolean setName(int i, String name) {
		if (i < dimension && i >= 0) {
			arityNames[i] = name;
			return true;
		} else {
			return false;
		}
	}

	public boolean setValues(int i, int value) {
		if (i < dimension && i >= 0) {
			arityValues[i] = value;
			return true;
		} else {
			return false;
		}
	}

	public String names(int i) {
		return (i < dimension && i >= 0) ? arityNames[i] : null;
	}

	public int values(int i) {
		return (i < dimension && i >= 0) ? arityValues[i] : 0;
	}

	@Override
	public String toString() {
		String str = "";
		for (int i = 0; i < dimension; i++)
			str = str + "[" + arityNames[i] + "]\t" + arityValues[i] + "\n";
		return str;
	}
}
