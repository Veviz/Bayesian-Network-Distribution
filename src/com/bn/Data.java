package com.bn;

import java.util.Arrays;

public class Data {
	
	private int data[][];            //used for storing data
	private int node[];              //x=node[0] means that there are x count when node 0 get the 0 value
	private long datasize;            //total records
	private int dataCount;
	private int nodeSize;
	private int dataDimension;
	private Arity arity;
	
	
	
	
	
	public Arity getArity() {
		return arity;
	}

	public void setArity(Arity arity) {
		this.arity = arity;
	}

	public int[][] getData() 
	{
		return data;
	}
	
	public Data(int dimension,int counts,long size)//counts means after reducing all data become counts line
	{
		data = new int [counts][dimension+1];
		for(int i=0;i<counts;i++)
			for(int j=0;j<=dimension;j++)
				data[i][j] =-1; 
		node = new int [dimension*2];
		for(int i=0;i<dimension*2;i++)
			node[i] = -1;
		datasize = size;
		dataCount = 0;
		dataDimension = dimension+1;
		nodeSize = dimension*2;
	}
	
	public void setDataCount(int count)
	{
		dataCount = count;
	}
	
	public int getDataCount()
	{
		return dataCount;
	}
	
	public void setData(int i,int j,int value) {
		data[i][j] = value;
	}
	
	
	public int getData(int i,int j) {
		return data[i][j];
	}
	
	public int getNode(int i) {
		return node[i];
	}
	public void setNode(int i,int value) {
		node[i] = value;
	}
	public long getDatasize() {
		return datasize;
	}
	public void setDatasize(int datasize) {
		this.datasize = datasize;
	}
    
	
	public int getDataDimension() {
		return dataDimension;
	}

	public void setDataDimension(int dataDimension) {
		this.dataDimension = dataDimension;
	}

	public void output()
	{
		String info = "";
		for(int i=0;i<dataCount;i++)
		{	
			for(int j=0;j<dataDimension;j++)
				info += data[i][j]+" ";
			info += "\n";
		}
		for(int i=0;i<nodeSize;i++)
			info += node[i]+" ";
		info += "\n";
		info += "dataset size :"+datasize+"\n";
		info += arity.toString();
		System.out.println(info);
	}

}
