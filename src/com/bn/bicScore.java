package com.bn;

import java.lang.Math;

public class bicScore {
       
	private int dimension;
	private Arity arity;
	private double bicScore = 0;
	private double penalisation = 0;
	
	public double getBicScore(Data data,BayesNet bn)
	{
		dimension = bn.getDimension();
		arity = bn.getArity();
		for(int eachNode = 0;eachNode < dimension;eachNode ++)//
		{
			int [] parentList = bn.getParentList(eachNode);    //only there need the BN Structure
			
			//if the current node has no parent ,then calculate next one
			if(parentList.length == 0)
			{
				//System.out.println("the current node have no parent");
				continue;
			}
			/////////////////////////////////////////////////////////
			//System.out.println("the current node parent:");
			//for(int i=0;i<parentList.length;i++)
			//{
				//System.out.println(parentList[i]);
			//}
			/////////////////////////////////////////////////////////////
			QuerySet m1Queries = new QuerySet(arity, parentList);
			for(int[] eachm1Query : m1Queries)
			{
				//now we are going to calcullate Mij*
				int m1 = count(data,eachm1Query);    //we get the times the eachm1Query appears in the dataset
				//System.out.println("mij* = "+m1);
				int [] eachm2Query;
				for(int values = 0; values < arity.values(eachNode);values ++)
				{
					eachm2Query = new int[dimension];
					for(int i = 0;i < dimension; i++)   //calculate Mijk while k = values
					{
						if(i != eachNode)
						   eachm2Query[i] = eachm1Query[i];
						else
							eachm2Query[i] = values;     //at the position of eachNode,we set query with values
					}
					int m2 = count(data,eachm2Query);
					//System.out.println("mijk = "+m2+"while k = "+values);
					if(m1 == 0 || m2 == 0)
						continue;
					double ratio = (double)m2/m1;
					//System.out.println("m1 = "+m1);
					//System.out.println("m2 = "+m2);
					//System.out.println("m2/m1 = "+ratio);
					//System.out.println("log(m2/m1) = "+Math.log(ratio));
					//System.out.println("m2 * log(m2/m1) = "+m2 * Math.log(ratio));
					double firstPart = m2 * Math.log(ratio);
					//System.out.println(firstPart);
					bicScore += firstPart;
				}
			}
			double first = m1Queries.size() * arity.values((eachNode)-1);
			//System.out.println(first);
			penalisation += first /2 ;////
		}
		//System.out.println("bicScore = "+bicScore);
		//System.out.println("penalisation = "+penalisation);
		penalisation *=  Math.log(data.getDatasize());
		bicScore = bicScore - penalisation;
		return bicScore;
	}
	
	public  int count(Data data,int [] query)
	{
		//data.output();
		//for(int i=0;i<query.length;i++)
		//	System.out.print(query[i]+" ");
		int result = 0;
		for(int i = 0; i < data.getDataCount();i ++)
		{
			boolean queryMatch = true;
			for(int j = 0;j < dimension; j++)
			{
				if(query[j] != -1 && query[j] != data.getData(i, j))
				{	
					queryMatch = false;
				    break;
				}
			}
			if(queryMatch)
				result += data.getData(i, dimension);
		}
		return result;
	}
}
