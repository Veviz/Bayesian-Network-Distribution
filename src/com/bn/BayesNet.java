package com.bn;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

public class BayesNet {

	private String name; // The name of the BN
	private int dimension; // the dimension of the BN
	private Arity arity; // to record the information of each record
	private boolean bnAdjMatrix[][]; // AdjMatrix,to record if there is a arc
										// between certain nodes
	private ProbabilityTable probTable[]; // either prior prob. table or
											// conditional prob. table
	private double marginalProbTable[][]; // saves the current probabilities of
											// all nodes
	private int[] topoSequence; // the topological sequence of the network

	public BayesNet(String nam, int dimen, Arity ari) // construct function
	{
		this.name = nam;
		this.dimension = dimen;
		this.arity = ari;
		bnAdjMatrix = new boolean[dimension][dimension];
		// initialise the adjacency matrix
		for (int i = 0; i < dimension; i++) {
			for (int j = 0; j < dimension; j++)
				bnAdjMatrix[i][j] = false;
		}
		// initialise the probTable array
		probTable = new ProbabilityTable[dimension];
		for (int i = 0; i < dimension; i++)
			probTable[i] = new ArrayPriorProbTable(arity, i);

		// initialise the current probability table
		marginalProbTable = new double[dimension][];
		for (int i = 0; i < dimension; i++) {
			marginalProbTable[i] = new double[arity.values(i)];
			double defaultProb = 1.0 / arity.values(i);
			for (int j = 0; j < arity.values(i); j++) {
				marginalProbTable[i][j] = defaultProb;
			}
		}

		// initialise the topological sequence
		topoSequence = new int[dimension];
		for (int i = 0; i < dimension; i++)
			topoSequence[i] = -1;
	}

	public boolean addEdges(int parent, int child) // this function will just
													// change bnAdjMatrix,and
													// won't change the CPT
	{
		if (parent >= 0 && parent < dimension && child >= 0
				&& child < dimension && parent != child
				&& bnAdjMatrix[child][parent] == false) {
			bnAdjMatrix[parent][child] = true;
			return true;
		} else
			return false;
	}

	public boolean deleteEdges(int parent,int child)
	{
		if (parent >= 0 && parent < dimension && child >= 0
				&& child < dimension && parent != child
				&& bnAdjMatrix[child][parent] == true) {
			bnAdjMatrix[parent][child] = false;
			return true;
		} else
			return false;
	}
	
	public void updateConditionalProbability(int [] parentList,int child,int parentCount,Data data)//when child node add the parent node,how CPT changes
	{
		//先构造child节点的条件概率表的结构
		probTable[child] = new MapConProbTable(arity,child,parentList);//此时probTable[child] 的类型为MapConProbTable
		//并设置probTable[child]的成员conProbTable 为HashMao<String,double>,根据child父节点的组合融入child的取值后组成的节点取值组合，构造笛卡尔积
		//for(int i=0;i<parentList.length;i++)
			//System.out.print(parentList[i]+" ");
		QuerySet qs = new QuerySet(arity,parentList);
		for(int [] eachQuery : qs)
		{
			//System.out.println("==============================================================");
			//for(int i=0;i<dimension;i++)
			     //System.out.print("eachQuery ="+eachQuery[i]+" ");
			//System.out.println();
			 int[] condition = new int[parentCount];
			 int countTmp = 0;
			 for (int i=0; i<eachQuery.length; i++)
			 {	 
				 if(eachQuery[i] != -1)
				 {
					 condition[countTmp++] = eachQuery[i];
					 //System.out.print(condition[countTmp]+" ");
				 }
			 }
			 //System.out.println();
			 double buf = count(eachQuery,data);//在数据集中统计child节点的父节点的组合为eachQuery的记录数量
			 //System.out.println("buf="+buf);
			 if(buf != 0)
			 {
				 for(int value=0;value<arity.values(child);value++)
				 {
					 eachQuery[child] = value;
					 double buf2 = count(eachQuery,data);      //统计child节点的父节点的组合为eachQuery时child取值为value的记录数量
					 //System.out.println("buf2="+buf2);
					 //System.out.println(buf2/buf);
					 this.setConProb(child, value, condition, buf2/buf);
				 }
			 }else   //若父节点的组合情况在数据集中不存在，则默认child节点取值的机会均等
			 {
				 for(int value=0;value<arity.values(child);value++)
				 {
					 this.setConProb(child, value, condition, 1.0/arity.values(child));
				 }
			 }
		}
	}
	
	public double count(int [] array,Data data)
	{
		//for(int i=0;i<dimension;i++)
			// System.out.print(array[i]+" ");
		//data.output();
		double num = 0;
		//System.out.println(data.getDataDimension());
		for(int i=0;i<data.getDataCount();i++)
		{
			boolean arrayMatch = true;
			for(int j=0;j<data.getDataDimension()-1;j++)
			{
				//System.out.println("array[j]="+array[j]+" data[i][j]="+data.getData(i, j));
				if(array[j] != -1 && array[j] != data.getData(i, j))
				{	
					//System.out.println("this is the "+i+"line of data");
					arrayMatch = false;
					break;
				}
				//System.out.print(data.getData(i, j)+" ");
			}
			if(arrayMatch)
			{	
				//System.out.println(data.getData(i, data.getDataDimension()-1));
				num += data.getData(i, data.getDataDimension()-1);
			}
		}
		//System.out.println(num);
		return num;
	}
	
	public int[] getParentList(int node) {// get parents of a node given a node
		if (node >= 0 && node < dimension) {
			List<Integer> parentList = new ArrayList<Integer>();
			for (int i = 0; i < dimension; i++)
				if (bnAdjMatrix[i][node])
					parentList.add(i);
			int parentArray[] = new int[parentList.size()];
			for (int i = 0; i < parentList.size(); i++)
				parentArray[i] = parentList.get(i).intValue();
			return parentArray;
		} else
			return null;
	}

	/*the bnInformation will records all information of the BN as following:
	 * name,dimension,arity,all the edges of the BN,and CPT of each node
	 * */
	public String outputInfo()
	{
		String bnInformation = "";
		bnInformation += "Name: "+name+"\n";
		bnInformation += "Dimension: "+dimension+"\n";
		bnInformation += "The nodes in BN are following: \n";
		for(int i=0;i<dimension;i++)
			bnInformation += arity.names(i)+"\t";
		bnInformation += "\n";
		bnInformation += "The egdes in BN are following:\n";
		for(int i=0;i<dimension;i++)
			for(int j=0;j<dimension;j++)
			{
				if(bnAdjMatrix[i][j])
					bnInformation += arity.names(i)+"->"+arity.names(j)+"\n";
					//bnInformation += i+"->"+j+"\n";
			}
		bnInformation += "\nthe marginal probability of each node are following:\n";
		for(int i=0;i<dimension;i++)
		{
			bnInformation += arity.names(i)+"\t";
		}
		bnInformation += "\n";
		for(int j=0;j<2;j++)
		{   
			for(int i=0;i<dimension;i++)
			    bnInformation += j+":"+marginalProbTable[i][j]+"\t";
			bnInformation += "\n";
		}
		//System.out.println(bnInformation);
		return bnInformation;
	}
	
	public void combine(String stru)//给该bn增加结构stru
	{
		String [] splitBuf = stru.split("\\|");
		int parent = -1;
		int child = -1;
		for(int i=0;i<splitBuf.length;i++)
		{
			String [] splitBufTmp = splitBuf[i].split("->");
			parent = Integer.parseInt(splitBufTmp[0]);
			child = Integer.parseInt(splitBufTmp[1]);
			this.setBnAdjMatrix(parent, child, true);
		}
	}
	
	public boolean getBnAdjMatrix(int i, int j) {
		return bnAdjMatrix[i][j];
	}

	public void setBnAdjMatrix(int i, int j, boolean status) {
		this.bnAdjMatrix[i][j] = status;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getDimension() {
		return dimension;
	}

	public void setDimension(int dimension) {
		this.dimension = dimension;
	}

	public Arity getArity() {
		return arity;
	}

	public void setArity(Arity arity) {
		this.arity = arity;
	}

	/*
	 * Update the marginal probabilities of a specific node according to the
	 * prior and conditional probabilities
	 * 
	 * @param node the attribute index of a node
	 * 
	 * @return true if updates successfully, otherwise return false
	 */
	public boolean updateMarginalProb(int node) {
		if (node < 0 && node >= dimension)
			return false;

		//System.out.println("update marginal prob. of node_" + node);
		// obtain the parents list of the node
		int[] parentList = getParentList(node);

		if (parentList.length == 0) {
			// set the marginal probabilities as the prior probabilities
			for (int i = 0; i < arity.values(node); i++)
				marginalProbTable[node][i] = getPriorProb(node, i);
			return true;
		} else {
			// calculate the marginal probabitlies for each P(node=eachCurValue)
			// where the eachCurValue ranges from 0 to the arity.values(node)-1
			for (int eachCurValue = 0; eachCurValue < arity.values(node); eachCurValue++) {
				marginalProbTable[node][eachCurValue] = 0.0; // reset the marginal prob.
				// calculate the cartesian product for the parents nodes
				// namely, calculate all the possible value combinations of the conditions
				CartesianProduct conditionCartProd = new CartesianProduct(arity, parentList);
				for (int[] eachConditions : conditionCartProd) {
					// calculate the contribution for each P(node=eachCurValue |
					// conditions)
					// set the contribution as conditional prob. initially
					double eachContribution = getConProb(node, eachCurValue, eachConditions);
					// parentList[i] is the i_th parent node's attribute index
					// eachConditions[i] is the i_th parent node's current value
					for (int i = 0; i < eachConditions.length; i++)
						eachContribution *= getMarginalProb(parentList[i], eachConditions[i]);
					// System.out.println(eachContribution);
					marginalProbTable[node][eachCurValue] += eachContribution;
				}
			}
			return true;
		}
	}

	/*
	 * @param node the attribute index of a node
	 * 
	 * @param attrValue the specific possible value of the node
	 * 
	 * @return the marginal probability: P(node = attrValue), return -1 if the
	 * node or attrValue are invalid
	 */
	public double getMarginalProb(int node, int attrValue) {
		if (node >= 0 && node < dimension // the node number must be valid
				&& attrValue >= 0 && attrValue < arity.values(node)) // the attribute value must  be valid
			return marginalProbTable[node][attrValue];
		else
			return -1;
	}
    
	// get the probability of node when it get value
	public double getMarginalProbTable(int node, int value) {
		return marginalProbTable[node][value];
	}

	// set the probability of node when it get value
	public void setMarginalProbTable(int node, int value, double probability) {
		this.marginalProbTable[node][value] = probability;
	}
	
	/**
     * Set the prior probability: P(node = attrValue) = priorProb
     * @param node the attribute index of a node
     * @param attrValue the specific possible value of the node
     * @param priorProb the prior probability
     * @return true if the prior probability is set successfully, return false if the node or attrValue are invalid
     */
	public boolean setPriorProb(int node, int attrValue, double priorProb) {
		if (node >= 0 && node < dimension // the node number must be valid
				&& attrValue >= 0 && attrValue < arity.values(node)) { 
			// the attribute value must be valid
			// return false if the node has parents which cannot have prior
			// probabilities
			for (int i = 0; i < dimension; i++)
				if (bnAdjMatrix[i][node] == true)
					return false;

			marginalProbTable[node][attrValue] = priorProb;
			if (probTable[node] != null) {
				PriorProbTable priorProbTable = (PriorProbTable) probTable[node];
				priorProbTable.setPriorProb(attrValue, priorProb);
			}
			return true;
		} else
			return false;
	}
	
    /**
     * @param node the attribute index of a node
     * @param attrValue the specific possible value of the node
     * @return the prior probability: P(node = attrValue), return -1 if the node or attrValue are invalid
     */
    public double getPriorProb(int node, int attrValue) {
	if (node >=0 && node < dimension // the node number must be valid
	    && attrValue >= 0 && attrValue < arity.values(node)) { // the attribute value must be valid
	    // return false if the node has parents which cannot have prior probabilities
	    for (int i=0; i<dimension; i++)
		if (bnAdjMatrix[i][node] == true)
		    return -1;

	    if (probTable[node] == null)
		return -1;

	    PriorProbTable priorProbTable = (PriorProbTable)probTable[node];
	    return priorProbTable.getPriorProb(attrValue);
	} else
	    return -1;
    }
	
    /**
     * @param node the attribute index of a node
     * @param allValue the array containing all the values [v1, v2, v3] of attributes x, y, z in P(x=v1 | y=v2, z=v3)
     * @param conProb the conditional probability of P(x=v1 | y=v2, z=v3)
     * @return true if the conditional probability is set successfully, return false if the input condition or conProb is invalid
     */
	public boolean setConProb(int node, int[] allValue, double conProb) {
		if (probTable[node] == null)
			probTable[node] = new MapConProbTable(arity, allValue);
		ConditionalProbTable conProbTable = (ConditionalProbTable) probTable[node];
		return conProbTable.setConProb(allValue, conProb);
	}
    
	
	/**
     * @param node the attribute index of a node
     * @param curValue the value v1 of the attribute x in P(x=v1 | y=v2, z=v3)
     * @param conValue the array containing all the values [v2, v3] of attributes y, z in P(x=v1 | y=v2, z=v3)
     * @param conProb the conditional probability of P(x=v1 | y=v2, z=v3)
     * @return true if the conditional probability is set successfully, return false if the input condition or conProb is invalid
     */
	public boolean setConProb(int node, int curValue, int[] conValue,
			double conProb) {
		if (probTable[node] == null)
			probTable[node] = new MapConProbTable(arity, curValue, conValue);
		ConditionalProbTable conProbTable = (ConditionalProbTable) probTable[node];
		return conProbTable.setConProb(curValue, conValue, conProb);
	}
	

    /**
     * @param node the attribute index of a node
     * @param allValue the array containing all the values [v1, v2, v3] of attributes x, y, z in P(x=v1 | y=v2, z=v3)
     * @return the conditional probability, return -1 if the input condition is invalid
     */
	public double getConProb(int node, int[] allValue) {
		if (probTable[node] == null)
			return -1;
		else {
			ConditionalProbTable conProbTable = (ConditionalProbTable) probTable[node];
			return conProbTable.getConProb(allValue);
		}
	}

    /**
     * @param node the attribute index of a node
     * @param curValue the value v1 of the attribute x in P(x=v1 | y=v2, z=v3)
     * @param conValue the array containing all the values [v2, v3] of attributes y, z in P(x=v1 | y=v2, z=v3). The conValue only contains the values of parent nodes.
     * @return the conditional probability, return -1 if the input condition is invalid
     */
	public double getConProb(int node, int curValue, int[] conValue) {
		if (probTable[node] == null)
			return -1;
		else {
			ConditionalProbTable conProbTable = (ConditionalProbTable) probTable[node];
			return conProbTable.getConProb(curValue, conValue);
		}
	}
    
	public String printNodesInfo() {
		String info = "";
		info += "************************************\n";
		info += "*     Bayesnet nodes infomation    *\n";
		info += "************************************\n";
		info += "* nodes:\n";
		//System.out.println("************************************");
		//System.out.println("*     Bayesnet nodes infomation    *");
		//System.out.println("************************************");
		//System.out.println("* nodes:");
		for (int i = 0; i < dimension; i++)
		{	
			//System.out.println("node_" + i + ": [" + arity.names(i)+ "]\tarity: " + arity.values(i));
			info += "node_" + i + ": [" + arity.names(i)+ "]\tarity: " + arity.values(i)+"\n";
		}
		info += "\n* edges:\n";
		//System.out.println("\n* edges:");
		for (int i = 0; i < dimension; i++)
			for (int j = 0; j < dimension; j++)
				if (bnAdjMatrix[i][j])
				{	
					info += arity.names(i)+"->"+arity.names(j)+"\n";
					//info += i+"->"+j+"\n";
					//System.out.println(i+"->"+j);
				}
		info += "\n* probability tables:";
		//System.out.println("\n* probability tables:");
		for (int i = 0; i < dimension; i++) 
		{
			info += "* node_" + i + " marginal probability:\n";
			//System.out.println("* node_" + i + " marginal probability:");
			for (int j = 0; j < arity.values(i); j++) 
			{
				String str = String.format("\tp(a_%d=%d)=%.4f", i, j,marginalProbTable[i][j]);
				//System.out.println(str);
				info += str+"\n";
			}

			if (probTable[i] != null) {
				//System.out.println((probTable[i].getTabType() == com.bn.ProbabilityTable.ProbTabType.PriorTab ? "prior"
						//: "conditional")+ " probabilities:");
				if(probTable[i].getTabType() == com.bn.ProbabilityTable.ProbTabType.PriorTab)
					info += "prior probabilities\n";
				else
					info += "conditional probabilities\n";
				info += "prob. number: " + probTable[i].getProbNum()+"\n";
				//System.out.println("prob. number: " + probTable[i].getProbNum());
				info += probTable[i].AllProb(); // prints all prob. tables, could
												// be a lot
			}
			//System.out.println();
			info += "\n";
		}

		info += "************************************\n";
		info += "* End of Bayesnet nodes infomation\n";
		info += "************************************\n";
		//System.out.println("************************************");
		//System.out.println("* End of Bayesnet nodes infomation *");
		//System.out.println("************************************");
		return info;
	}
	
	public int[] topologicalSort() {
		boolean adjMatrixCopy[][];
		adjMatrixCopy = new boolean[dimension][dimension];
		for (int i = 0; i < dimension; i++)
			for (int j = 0; j < dimension; j++)
				adjMatrixCopy[i][j] = bnAdjMatrix[i][j];
		for (int i = 0; i < dimension; i++) {
			boolean isLeader;
			for (int j = 0; j < dimension; j++) {
				boolean sorted = false;
				for (int k = 0; k < i; k++) {
					if (j == topoSequence[k]) {
						sorted = true;
						break;
					}
				}
				if (sorted)
					continue;
				isLeader = true;
				for (int k = 0; k < dimension; k++) {
					if (adjMatrixCopy[k][j] == true) {
						isLeader = false;
						break;
					}
				}
				if (isLeader) {
					topoSequence[i] = j;
					for (int k = 0; k < dimension; k++)
						adjMatrixCopy[j][k] = false;
					break;
				}
			}
		}
		int[] topoSequenceCopy = new int[dimension];
		for (int i = 0; i < dimension; i++)
			topoSequenceCopy[i] = topoSequence[i];
		return topoSequenceCopy;
	}

	public int  getTopoSequence(int index) {
		return topoSequence[index];
	}

	public void setTopoSequence(int[] topoSequence) {
		this.topoSequence = topoSequence;
	}

	
}
