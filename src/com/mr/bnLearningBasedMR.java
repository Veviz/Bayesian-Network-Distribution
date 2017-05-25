package com.mr;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

import com.bn.*;

public class bnLearningBasedMR {

	//该map函数就是为了处理数据的，没有其他功能
	public static class DataMapper // split the dataset
			extends Mapper<Object, Text, Text, IntWritable> {
		
		private final static IntWritable one = new IntWritable(1);
		private Text word = new Text();
		private int totalRecord = 0;
		private int dimension;

		private MultipleOutputs<Text, IntWritable> mos;
		
		private int mapCount = 1;
		public void map(Object key, Text value, Context context) throws IOException, InterruptedException 
		{
			totalRecord ++;
			StringTokenizer itr = new StringTokenizer(value.toString());
			while (itr.hasMoreTokens()) 
			{
				word.set(itr.nextToken());
				context.write(word, one);
		    }
			String str = new String(value.toString());
			String[] splitBuf;
			splitBuf = str.split(",");
			int i = 0;
			for (i = 0; i < splitBuf.length; i++) 
			{
				String tmp = "N";
				tmp += i;
				if (splitBuf[i].equals("0"))
				{	
					tmp += "=0";
					context.write(new Text(tmp), one);
				}
				else if(splitBuf[i].equals("1"))
				{
					tmp += "=1";
					context.write(new Text(tmp), one);
				}
		    }
		}
			
		protected void setup(Context context) throws IOException,
				InterruptedException {
			mos = new MultipleOutputs<Text, IntWritable>(context);// ////////////////////////////////
		}
		
		protected void cleanup(Context context) throws IOException,
				InterruptedException {
			mos.close();// ////////////////////////////////
		}
		
		public void run(Context context) throws IOException, InterruptedException 
		{
			mapCount ++;
			 setup(context);// 初始化
			    try 
			    {
				    context.nextKeyValue();
				    String str = new String(context.getCurrentValue().toString());
				    String [] splitBuf = str.split(",");
				    dimension = splitBuf.length;
				    context.write(new Text(str), new IntWritable(1));
				    context.write(new Text("##dimension"), new IntWritable(dimension));
				    while (context.nextKeyValue()) 
				    { 
					      map(context.getCurrentKey(), context.getCurrentValue(), context);// 对该K-V对进行map处理
				    }
			   } finally 
			   {
				   String str;
				   for(int curNode = 0;curNode < dimension; curNode ++)
				   {
					   str = String.valueOf(curNode);
					   mos.write(str, new Text(str), new Text("1"));
				   }
				   context.write(new Text("##totalRecords"), new IntWritable(totalRecord));
				   cleanup(context);// 结束处理
			   }
		}
	}

	
	public static class DataReducer extends Reducer<Text, IntWritable, Text, IntWritable> 
	{
		private IntWritable result = new IntWritable();
		private Data data;
		private Arity arity;
		private int nodeCount = 0;
		private int dataRow = 0;
		private int dataCount = 0;
		private int dimension = 0;
		private int totalRecords = 0;
		
		public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException 
		{
			String str = key.toString();		
			int sum = 0;
			for (IntWritable val : values)
			{
				sum += val.get();
			}
			if(str.length() == 4)
			{	
				data.setNode(nodeCount, sum);
				nodeCount ++;
			}
			else if(str.length() == (dimension*2 - 1))
			{
				String [] strBuf = str.split(",");
				for(int i=0;i<strBuf.length;i++)
					data.setData(dataRow,i, Integer.parseInt(strBuf[i]));
				data.setData(dataRow, strBuf.length, sum);
				dataRow ++;
				dataCount ++;
			}else
			{
				String [] splitBuf = str.split(",");
				for(int i=0;i<dimension;i++)
				{	
					arity.setName(i, splitBuf[i]);
					arity.setValues(i, 2);
				}
			}
		}
		
		public void cleanup(Context context)throws IOException, InterruptedException 
		{
			FileOutputStream fos = new FileOutputStream("/home/hadoop/file/file1.txt");
	        ObjectOutputStream oos = new ObjectOutputStream(fos);
	        String tmp = "";
	        tmp += dimension+"@";
	        tmp += totalRecords+"@";
	        tmp += dataCount+"@";
	        for(int i=0;i<dataCount;i++)
	        {	
	        	tmp += data.getData(i, 0);
	        	for(int j=1;j<=dimension;j++)
	        	   tmp += ","+data.getData(i, j);
	            if(i+1 < dataCount)
	        	   tmp += "#";
	        }
	        tmp += "+";
	        tmp += data.getNode(0);
	        for(int i=1;i<nodeCount;i++)
	        {
	        	tmp += ","+data.getNode(i);
	        }
	        tmp += "+";
	        tmp += data.getArity().names(0);
	        for(int i=1;i<dimension;i++)
	        	tmp += "*"+data.getArity().names(i);
	        oos.writeObject(tmp);
	        oos.close();
	        fos.close();
		}
		
		 public void run(Context context) throws IOException, InterruptedException 
		 {
			    setup(context);
			    try
			    {
			    	context.nextKey();
			    	for(IntWritable val:context.getValues())
			    		dimension += val.get();
			    	context.nextKey();
			    	for(IntWritable val:context.getValues())
			    		totalRecords += val.get();
			    	data = new Data(dimension,100,totalRecords);
			    	arity = new Arity(dimension);
			        while (context.nextKey()) 
			        {
			             reduce(context.getCurrentKey(), context.getValues(), context);
			        }
			        data.setArity(arity);
			    } finally 
			    {
			      cleanup(context);
			    }
	    }
	}

	/*
	 * For each node,construct a candidate network and then calculate the BIC
	 * score of this network,and write <node,network&score> pair to the
	 * intermediate store
	 */
	public static class StructureMapper extends
			Mapper<Object, Text, Text, Text> {
	

		private Data data;
		private Arity arity;
		private int dimension;
		private long totalRecords;
		private int dataCount;
		
		private int curNode;
		
		public void map(Object key,Text value,Context context)throws IOException, InterruptedException
		{
			String str = value.toString();
			StringTokenizer st = new StringTokenizer(str);
			curNode = Integer.parseInt(st.nextToken().toString());
			int [] parenList;
			int count = 0;
			parenList = new int [dimension];
			for(int i=0;i<dimension;i++)
				parenList[i] = -1;
			boolean keepAdding = true;
			BayesNet curBn = new BayesNet("curBn",dimension,arity);
			double curBicScore = Double.NEGATIVE_INFINITY;
			while(keepAdding)//every time we just produce one candidate BN structure at one node
			{
				//record the maxScore of temp BN after adding the maxParent to the currentNode's parent list
				double maxScore = Double.NEGATIVE_INFINITY;
				int maxParent = -1;
				for(int parent = 0;parent < curNode;parent ++)//这个循环，只是为了得到是bn评分最大的父节点和其评分
			    //by the sequence of node,we try each node to see if it can be currentNode's parent
				{
					//if the node has existed in the parent list of current node,we can ignore it
					if(isExisting(curBn.getParentList(curNode),parent))
						continue;
					BayesNet bnTmp = new BayesNet("bnTmp",dimension,arity);
					for(int i=0;parenList[i] != -1;i++)
						bnTmp.addEdges(parenList[i], curNode);
					//we add a edge for the temp BN structure to calculate score
					bnTmp.addEdges(parent, curNode);
					bicScore bic = new bicScore();
					double bicScoreTmp = bic.getBicScore(data, bnTmp);
					if(bicScoreTmp > maxScore)
					{
						maxScore = bicScoreTmp;
						maxParent = parent;
					}
				}
				//System.out.println("now the best bn score is:"+maxScore+" while edge is "+maxParent+"->"+currentNode);
				//if the score of temp BN increased,we can add the maxParent to the currentNode's parent list
				//and continue to find if there is another node which suit to be currentNode's parent
				if(maxScore > curBicScore)
				{	
					curBicScore = maxScore;
					curBn.addEdges(maxParent, curNode);
					parenList[count++] = maxParent;
				}
				else//otherwise,we stop finding the currentNode's parent
					keepAdding = false;
			}
			
			String structure = "";
			boolean isFirst = true;
			for(int i=0;i<dimension;i++)
				for(int j=0;j<dimension;j++)
				{
					if(curBn.getBnAdjMatrix(i, j))
					{
						if(!isFirst)
							structure += "|";
						structure += i+"->"+j;
						isFirst = false;
					}
				}
			String valueToReduce = String.valueOf(curNode)+" ";
			if(structure.length() > 0)
			{
				valueToReduce += structure + "&"+curBicScore;
			}
			else
			{
				valueToReduce += "null";
			}
			context.write(new Text(valueToReduce), new Text(valueToReduce));
		}

		public boolean isExisting(int array[], int element) {
			boolean result = false;
			for (int i = 0; i < array.length; i++) {
				if (element == array[i])
					result = true;
			}
			return result;
		}
		
		public void setup(Context context)throws IOException, InterruptedException
		{
			FileInputStream fis = new FileInputStream("/home/hadoop/file/file1.txt");
	        ObjectInputStream ois = new ObjectInputStream(fis);
	        try {
				String information = (String)ois.readObject();
				String [] splitBuf = information.split("@");
				dimension = Integer.parseInt(splitBuf[0]);
				totalRecords = Long.parseLong(splitBuf[1]);
				dataCount = Integer.parseInt(splitBuf[2]);
				data = new Data(dimension,dataCount,totalRecords);
				arity = new Arity(dimension);
				data.setDataCount(dataCount);
				
				String [] split1 = splitBuf[3].split("\\+");
				//split1[0] represents the array data
				String [] split1Buf = split1[0].split("#");//按照#将数据分割成每行
				for(int i=0;i<split1Buf.length;i++)
				{
					String [] split1Buf1 = split1Buf[i].split(",");//按照“，”将每行数据单独分割开
					for(int j=0;j<split1Buf1.length;j++)
					{	
						data.setData(i, j, Integer.parseInt(split1Buf1[j]));
					}
				}
				//split1[1] represents the array node
				String [] split1Buff = split1[1].split(",");
				for(int i=0;i<split1Buff.length;i++)
					data.setNode(i, Integer.parseInt(split1Buff[i]));
				//split1[2] represents the arity
				String [] split1Buffer = split1[2].split("\\*");
				for(int i=0;i<split1Buffer.length;i++)
				{
					arity.setName(i, split1Buffer[i]);
					arity.setValues(i,2);
				}
				data.setArity(arity);
				//data.output();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		public void run(Context context) throws IOException, InterruptedException
		{
			 setup(context);// 初始化
			    try 
			    {
			    	while (context.nextKeyValue()) 
					 { 
						   map(context.getCurrentKey(), context.getCurrentValue(), context);// 对该K-V对进行map处理
					 }
				} finally
				{
					cleanup(context);// 结束处理
			    }
		}
	}

	/*
	 * For each node,find the highest score network and then compare this score
	 * (marked A)with the BIC score of the current network(marked B),if A >
	 * B,then combine the A network with current network. After all of the node
	 * calculating,we will get a global network,and calculate the CPT of this BN
	 * Structure
	 */
	public static class StructureReducer extends
			Reducer<Text, Text, Text, Text> {

		private int dimension;               //record the dimension of the BN
		private long totalRecords;
		private int dataCount;
		private Arity arity;
		private Data data;
		
		
		BayesNet bnFinal;                       //reduce阶段全局最优的网络结构
		bicScore bicOri;                        //上面的网络结构对应的评分
		double finalBic;
		
		public void reduce(Text key, Text value, Context context)
				throws IOException, InterruptedException {
			
			StringTokenizer st = new StringTokenizer(value.toString());

			int curNode = Integer.parseInt(st.nextToken().toString());  //获得当前节点
			String str = st.nextToken().toString();
			if (!str.equals("null")) 
			{
				String[] splitBuf = str.split("&");

				String localStruc = splitBuf[0]; // 获得该节点对应的局部最优网络结构
				double localScore = Double.parseDouble(splitBuf[1]);

				
				BayesNet bnTmp = new BayesNet("bnTmp", dimension, arity);
				copy(bnFinal, bnTmp);
				bnTmp.combine(localStruc);
				bicScore bic = new bicScore();
				double bicTmp = bic.getBicScore(data, bnTmp);
				if (bicTmp < finalBic) {
					finalBic = bicTmp;
					bnFinal.combine(localStruc);
				}
			}
		}

		public void setup(Context context)throws IOException, InterruptedException
		{
			FileInputStream fis = new FileInputStream("/home/hadoop/file/file1.txt");
	        ObjectInputStream ois = new ObjectInputStream(fis);
	        try {
				String information = (String)ois.readObject();
				String [] splitBuf = information.split("@");
				dimension = Integer.parseInt(splitBuf[0]);
				totalRecords = Long.parseLong(splitBuf[1]);
				dataCount = Integer.parseInt(splitBuf[2]);
				data = new Data(dimension,dataCount,totalRecords);
				arity = new Arity(dimension);
				data.setDataCount(dataCount);
				
				String [] split1 = splitBuf[3].split("\\+");
				//split1[0] represents the array data
				String [] split1Buf = split1[0].split("#");//按照#将数据分割成每行
				for(int i=0;i<split1Buf.length;i++)
				{
					String [] split1Buf1 = split1Buf[i].split(",");//按照“，”将每行数据单独分割开
					for(int j=0;j<split1Buf1.length;j++)
					{	
						data.setData(i, j, Integer.parseInt(split1Buf1[j]));
					}
				}
				//split1[1] represents the array node
				String [] split1Buff = split1[1].split(",");
				for(int i=0;i<split1Buff.length;i++)
					data.setNode(i, Integer.parseInt(split1Buff[i]));
				//split1[2] represents the arity
				String [] split1Buffer = split1[2].split("\\*");
				for(int i=0;i<split1Buffer.length;i++)
				{
					arity.setName(i, split1Buffer[i]);
					arity.setValues(i,2);
				}
				data.setArity(arity);
				//data.output();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		// overwrite the run function
		public void run(Context context) throws IOException,
				InterruptedException {
			setup(context);// 获取压缩数据，得到类Data 所需要的全部数据
			try {
				
				bnFinal = new BayesNet("Final BN",dimension,arity);
				bicOri = new bicScore();
				finalBic = bicOri.getBicScore(data, bnFinal);//Double.NEGATIVE_INFINITY;			
				while (context.nextKeyValue()) {// 每次读取一行，得到一个节点以及对应的网络结构
					reduce(context.getCurrentKey(), context.getCurrentValue(), context);// 对该K-V对进行map处理
				}
			} finally {

				//1.get the prior probability
				for(int node = 0;node < dimension;node++)
				{
					for(int value=0;value<arity.values(node);value ++)
					{
						long tt = data.getNode(node*2+value);
						double prob = (double)tt/data.getDatasize();
						bnFinal.setPriorProb(node, value,prob );
					}
				}
				//bnFinal.printNodesInfo();
				//2.update conditional probability of nodes those which have parent
				int  [] parentTmp;
				parentTmp = new int[dimension];
				for(int child=0;child<dimension;child++)
				{
					int parentCount = 0;
					for(int parent=0;parent<dimension;parent++)
					{
						if(bnFinal.getBnAdjMatrix(parent, child))    //just for those nodes which have parents
						{	
							parentTmp[parentCount++] = parent;
						}
					}
					int [] parentList;
					parentList = new int[parentCount];
					if(parentCount == 0)
					{
						continue;
					}

					//System.out.print(child+"th node's parents are:");
					for(int i=0;i<parentCount;i++)
					{
						parentList[i] = parentTmp[i];
					}
					bnFinal.updateConditionalProbability(parentList,child,parentCount,data);  //当前节点child的父节点为parentList，更新其条件概率
				}
				//3.update the marginal probability of each node
				bnFinal.topologicalSort();
				for(int i=0;i<dimension;i++)
				{
					int node = bnFinal.getTopoSequence(i);
					bnFinal.updateMarginalProb(node);
				}
				//bnFinal.printNodesInfo();
				
				String bnInformation = "";
				bnInformation = bnFinal.printNodesInfo();
				context.write(new Text(bnInformation), null);
				cleanup(context);// 结束处理
			}
		}
		
		public void copy(BayesNet source,BayesNet copy)//将source 的bnAdjMatrix 拷贝给copy
		{
			for(int i =0;i<source.getDimension();i++)
				for(int j=0;j<source.getDimension();j++)
				{
					copy.setBnAdjMatrix(i, j, source.getBnAdjMatrix(i, j));
				}
		}
	}

	public static void main(String[] args) throws Exception {
		
		
		long preTime = System.currentTimeMillis();
		
		Configuration conf = new Configuration();
		Job job = new Job(conf, "Dataset getting");
		job.setJarByClass(bnLearningBasedMR.class);
		job.setMapperClass(DataMapper.class);
		job.setReducerClass(DataReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);
		
		String str;
		for(int i=0;i<8;i++)
		{
			str = String.valueOf(i);
			MultipleOutputs.addNamedOutput(job, str, TextOutputFormat.class, Text.class, Text.class);
		}
		
		
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		job.waitForCompletion(true);

		
		
		Configuration conf2 = new Configuration();
		Job job2 = new Job(conf2, "BN Structure clnonstructing");
		job2.setJarByClass(bnLearningBasedMR.class);
		job2.setMapperClass(StructureMapper.class);
		job2.setReducerClass(StructureReducer.class);
		job2.setOutputKeyClass(Text.class);
		job2.setOutputValueClass(Text.class);
		FileInputFormat.addInputPath(job2, new Path(args[2]));
		FileOutputFormat.setOutputPath(job2, new Path(args[3]));
		job2.waitForCompletion(true);
		
		long afterTime = System.currentTimeMillis();
		System.out.println("time used:"+(afterTime - preTime));
		
	}
}
