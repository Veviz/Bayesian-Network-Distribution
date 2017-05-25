package com.bn;

import java.util.Iterator;

public class QuerySet extends CartesianProduct implements Iterable<int[]> {

    public class Queryiter extends CartesianProductiter implements Iterator<int[]> {
	@Override
	public int[] next() {
	    index++;
	    int[] newQuery = new int[arity.getDimension()];
	    int indexCopy = index;
	    int i = 0;
	    for (int j=0; j<arity.getDimension(); j++) {
		if (i<attributeList.length && attributeList[i] == j) {
		    newQuery[j] = indexCopy % arity.values(attributeList[i]);
		    indexCopy /= arity.values(attributeList[i]);
		    i++;
		} else {
		    newQuery[j] = -1;
		}
	    }
	    return newQuery;
	}
	
	@Override
	public void remove() {
	    //System.out.println("Function remove() does not make any sence for making a new query, nothing is done here.");
	}
    }
    
    public QuerySet(Arity a, int[] attrList) {
	super(a, attrList);
    }
	
    @Override
    public Iterator<int[]> iterator() {
	Iterator<int[]> queryiter = new Queryiter();
	return queryiter;
    }
    

}
