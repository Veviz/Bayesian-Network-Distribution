package com.bn;

import java.util.Iterator;

public class CartesianProduct implements Iterable<int[]> {
    protected Arity arity;
    protected int[] attributeList;
    protected int entryNum;
	
    public class CartesianProductiter implements Iterator<int[]> {
	protected int index = -1;
	
	@Override
	public boolean hasNext() {
	    return (index < entryNum-1);
	}
	
	@Override
	public int[] next() {
	    index++;
	    int[] newProduct = new int[attributeList.length];
	    int indexCopy = index;
	    int i = 0;
	    for (int eachAttr : attributeList) {
		newProduct[i] = indexCopy % arity.values(eachAttr);
		indexCopy /= arity.values(eachAttr);
		i++;
	    }
	    return newProduct;
	}
	
	@Override
	public void remove() {
	    //System.out.println("Function remove() does not make any sence for making cartesian product, nothing is done here.");
	}
    }

    public CartesianProduct(Arity a, int[] attrList) {
	arity = a;
	attributeList = attrList;
	entryNum = 1;
	for (int eachAttr : attrList)
	    entryNum *= arity.values(eachAttr);
    }
	
    @Override
    public Iterator<int[]> iterator() {
	Iterator<int[]> cartesianProductiter = new CartesianProductiter();
	return cartesianProductiter;
    }

    public int size() {
	return entryNum;
    }
}
