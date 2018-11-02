package TerranBot;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.csvreader.CsvReader;

public class DicisionTree {	  
		public static Object testTree(String n1, String n2, String n3) throws Exception {
		    String[] attrNames = new String[] { "Assimilator", "Protoss Cybernetics Core", "Gateway"};
		    // Read dataset and create samples
		    Map<Object, List<Sample>> samples = readSamples(attrNames);
		    // generate decision tree
		    Object decisionTree = generateDecisionTree(samples, attrNames);
		    // print decision tree
		    outputDecisionTree(decisionTree, 0, null);
		    Object[] testData = new Object[] {n1, n2, n3, ""};
		    Sample testSample = new Sample();
		    for (int i=0; i < testData.length - 1; i++)
		    	testSample.setAttribute(attrNames[i], testData[i]);
		    testSample.setCategory(testData[testData.length - 1]);
		    
		    if (decisionTree instanceof Tree) {
		    	//System.out.println(((Tree) decisionTree).sampleMatch(testSample));
		    	return ((Tree) decisionTree).sampleMatch(testSample);
	    	}else {
	    		//System.out.println("Not a tree");
	    		throw new Exception("Not a tree");
	    	}
		    //System.out.println(testSample.getAttribute(attrNames[0]));
		}
		//Get directories of all csv files
	    public static ArrayList<String> getFileList(String dirPath){
	        File dir = new File(dirPath);
	        File[] fileList = dir.listFiles();
	        ArrayList<String> strList = new ArrayList<String>();
	        for(File f:fileList){	                            
	            if((f.isFile()) 
	                    && (".csv".equals(
	                            f.getName().
	                            substring(
	                                    f.getName().lastIndexOf("."), 
	                                    f.getName().length())))){
	                strList.add(f.getAbsolutePath());
	                
	            }
	        }       
	        return strList;
	    }
	  
	  //Read a csv file and generate a result array.
	  //The result is in format {ASSIMILATOR, PCC, Gateway, Plan}
	  public static String[] readcsv(String dileDir){
		  	int COLUMN_NUM = 6;
		  	int ROW_NUM = 14;
		  	
		  	/*
		  	int COLUMN_CYCLE = 0;
		  	int COLUMN_ASSIMILATOR = 1;
		  	int COLUMN_PCC = 2;
		  	int COLUMN_DRAGOON = 3;
		  	int COLUMN_GATEWAY = 4;
		  	int COLUMN_ZEALOT = 5;
		  	*/
		  	int COLUMN_ASSIMILATOR = 0;
		  	int COLUMN_CC = 1;
		  	int COLUMN_GATEWAY = 2;
		  	//int COLUMN_ZEALOT_DRAGOON = 3;
		  	int ENEMY_PLAN = 3;
		  	
		  	
		  	int i = 0;
		  	int j = 0;
	        String filePath = dileDir;
	        String scoutCycle = "4320";
	        String resultCycle = "10080";
	        //String[][] currCol = new String[ROW_NUM][];
	        String[] result = new String[4];
	        try {
	            // new a csv reader
	            CsvReader csvReader = new CsvReader(filePath);

	            // read headers
	            csvReader.readHeaders();
	            while (csvReader.readRecord()){
	                //Read a row
	                //System.out.println(csvReader.getRawRecord());
	            	
	            	//get all elements in an array.
	            	/*
	            	currCol[i] = csvReader.getRawRecord().split(",");
	            	System.out.println(currCol[i][0]);
	            	i++;
	            	*/
	            	
	                //Get necessary elements at the row time is the 3rd mins: 
	            	//1.number of assimilator
	            	//2.number of PCC
	            	//3.number of gateway
	            	
	                //System.out.println(csvReader.get("cycle"));
	            	
	            	if (csvReader.get("cycle").equals(scoutCycle)) {
	            		result[COLUMN_ASSIMILATOR] = csvReader.get("Protoss Assimilator");
	            		result[COLUMN_CC] = csvReader.get("Protoss Cybernetics Core");
	            		result[COLUMN_GATEWAY] = csvReader.get("Protoss Gateway");
	            		//System.out.println("Find 4320");
	            	}
	            	
	            	//If there are more Zealots than Dragoons in the 7th minute, we consider it is a Zealot Rush.
	            	if (csvReader.get("cycle").equals(resultCycle)) {
	            		if (Integer.parseInt(csvReader.get("Protoss Zealot")) > Integer.parseInt(csvReader.get("Protoss Dragoon"))) {
	            			//System.out.println("Zealot Rush");
	            			result[ENEMY_PLAN] = "Zealot Rush";
	            		}
	            		else {
	            			result[ENEMY_PLAN] = "Dragoon";          			
	            		}
	            	}
	            }

	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	        //System.out.println(Arrays.toString(result));
	        return result;
	    }
	  
	  //Read all csv files and generate datas for creating decision tree.
	  public static String[][] getAllCsvData(String dir) throws IOException{
	        //ArrayList<String> list = new ArrayList<String>();
	        ArrayList<String> csvList = getFileList(dir);
	        
	        String[][] result = new String[csvList.size()][];
	        for(int i = 0; i < csvList.size(); i ++){
	            
	        	result[i] = (readcsv(csvList.get(i)));
	                
	        }
	        return result;
	    }
	  
	  /**
	 * @throws IOException 
	   */
	  static Map<Object, List<Sample>> readSamples(String[] attrNames) throws IOException {
		// Map<category, Samples>. Create Sample for each csv file, and classify all Samples according to category.
		// Category is the value of plan (Zealot Rush or Dragoon)
		  
		  //All csv files are under dataSet folders
		  String[][] rawData = getAllCsvData("dataSet");
		    /*for(int i = 0; i < s.length; i ++) {
		    	System.out.println(Arrays.toString(s[i]));
		    }*/
	    Map<Object, List<Sample>> ret = new HashMap<Object, List<Sample>>();
	    
	    //For each row(csv file), create a sample object.
	    for (Object[] row : rawData) {
	      Sample sample = new Sample();
	      int i = 0;
	      for (int n = row.length - 1; i < n; i++)
	        sample.setAttribute(attrNames[i], row[i]);
	      
	      //Category is the value of plan (Zealot Rush or Dragoon)
	      sample.setCategory(row[i]);
	      
	      //Put all samples that have the same category into a list. 
	      //If the list of a category is empty, then create the list. Else add the sample into the existing list.
	      List<Sample> samples = ret.get(row[i]);
	      if (samples == null) {
	        samples = new LinkedList<Sample>();
	        ret.put(row[i], samples);
	      }
	      samples.add(sample);
	    }
	    return ret;
	  }
	  /**
	   * generate decision tree
	   */
	  static Object generateDecisionTree(
	      Map<Object, List<Sample>> categoryToSamples, String[] attrNames) {
		// If there is only one category, return this category.
	    if (categoryToSamples.size() == 1)
	      return categoryToSamples.keySet().iterator().next();
	    // If there is no available attribute left, choose the category that has the most samples.
	    if (attrNames.length == 0) {
	      int max = 0;
	      Object maxCategory = null;
	      for (Entry<Object, List<Sample>> entry : categoryToSamples
	          .entrySet()) {
	        int cur = entry.getValue().size();
	        if (cur > max) {
	          max = cur;
	          maxCategory = entry.getKey();
	        }
	      }
	      return maxCategory;
	    }
	    // Choose the best attribute for this tree.
	    Object[] rst = chooseBestTestAttribute(categoryToSamples, attrNames);
	    // Create the root node and set rst as its attribute
	    Tree tree = new Tree(attrNames[(Integer) rst[0]]);
	    // delete the attribute of root node in attribute set.
	    String[] subA = new String[attrNames.length - 1];
	    for (int i = 0, j = 0; i < attrNames.length; i++)
	      if (i != (Integer) rst[0])
	        subA[j++] = attrNames[i];
	    // each split is a child tree
	    @SuppressWarnings("unchecked")
	    Map<Object, Map<Object, List<Sample>>> splits =
	    /* NEW LINE */(Map<Object, Map<Object, List<Sample>>>) rst[2];
	    for (Entry<Object, Map<Object, List<Sample>>> entry : splits.entrySet()) {
	      Object attrValue = entry.getKey();
	      Map<Object, List<Sample>> split = entry.getValue();
	      Object child = generateDecisionTree(split, subA);
	      tree.setChild(attrValue, child);
	    }
	    return tree;
	  }
	  //Choose the best attribute for a node. Need to provide sample set classified by 
	  //category and attrNames. Return an array containing index of the best attribute,
	  //minValue(maybe useless) and the best split plan that is map(attribute -> map(category -> list of samples))
	  static Object[] chooseBestTestAttribute(
	      Map<Object, List<Sample>> categoryToSamples, String[] attrNames) {
	    int minIndex = -1; // index of the best attribute
	    double minValue = Double.MAX_VALUE; // minValue(maybe useless)
	    Map<Object, Map<Object, List<Sample>>> minSplits = null; // the best split plan
	   
	    // For each attribute, compute total required info value of all branches.
	    // The best one has the minValue. (max benefit)
	    for (int attrIndex = 0; attrIndex < attrNames.length; attrIndex++) {
	      int allCount = 0; // total number of samples
	      
	      Map<Object, Map<Object, List<Sample>>> curSplits =
	      /* NEW LINE */new HashMap<Object, Map<Object, List<Sample>>>();
	      for (Entry<Object, List<Sample>> entry : categoryToSamples
	          .entrySet()) {
	        Object category = entry.getKey();
	        List<Sample> samples = entry.getValue();
	        for (Sample sample : samples) {
	          Object attrValue = sample
	              .getAttribute(attrNames[attrIndex]);
	          Map<Object, List<Sample>> split = curSplits.get(attrValue);
	          if (split == null) {
	            split = new HashMap<Object, List<Sample>>();
	            curSplits.put(attrValue, split);
	          }
	          List<Sample> splitSamples = split.get(category);
	          if (splitSamples == null) {
	            splitSamples = new LinkedList<Sample>();
	            split.put(category, splitSamples);
	          }
	          splitSamples.add(sample);
	        }
	        allCount += samples.size();
	      }	      
	      // For current attribute, compute total required info value of all branches.
	      double curValue = 0.0; // total value
	      for (Map<Object, List<Sample>> splits : curSplits.values()) {
	        double perSplitCount = 0;
	        for (List<Sample> list : splits.values())
	          perSplitCount += list.size(); //  accumulate number of samples for current branch 
	        double perSplitValue = 0.0; // value for current branch
	        for (List<Sample> list : splits.values()) {
	          double p = list.size() / perSplitCount;
	          perSplitValue -= p * (Math.log(p) / Math.log(2));
	        }
	        curValue += (perSplitCount / allCount) * perSplitValue;
	      }
	      // The best one has the minValue. (max benefit)
	      if (minValue > curValue) {
	        minIndex = attrIndex;
	        minValue = curValue;
	        minSplits = curSplits;
	      }
	    }
	    return new Object[] { minIndex, minValue, minSplits };
	  }
	  /**
	   * Print the tree
	   */
	  static void outputDecisionTree(Object obj, int level, Object from) {
	    for (int i = 0; i < level; i++)
	      System.out.print("|-----");
	    if (from != null)
	      System.out.printf("(%s):", from);
	    if (obj instanceof Tree) {
	      Tree tree = (Tree) obj;
	      String attrName = tree.getAttribute();
	      System.out.printf("[%s = ?]\n", attrName);
	      for (Object attrValue : tree.getAttributeValues()) {
	        Object child = tree.getChild(attrValue);
	        outputDecisionTree(child, level + 1, attrName + " = "
	            + attrValue);
	      }
	    } else {
	      System.out.printf("[CATEGORY = %s]\n", obj);
	    }
	  }
	  /**
	   * definition of sample. containing attributes and one category
	   */
	  static class Sample {
	    private Map<String, Object> attributes = new HashMap<String, Object>();
	    private Object category;
	    public Object getAttribute(String name) {
	      return attributes.get(name);
	    }
	    public void setAttribute(String name, Object value) {
	      attributes.put(name, value);
	    }
	    public Object getCategory() {
	      return category;
	    }
	    public void setCategory(Object category) {
	      this.category = category;
	    }
	    public String toString() {
	      return attributes.toString();
	    }
	  }
	  /**
	   * definition of tree. Every branch non-leaf node can be considered as the root node of a (sub)decision tree.
	   * Root node has one attribute. Every non-root&&non-leaf node has an attribute and a value of parent node's attribute. 
	   */
	  static class Tree {
	    private String attribute;
	    private Map<Object, Object> children = new HashMap<Object, Object>();
	    public Tree(String attribute) {
	      this.attribute = attribute;
	    }
	    public String getAttribute() {
	      return attribute;
	    }
	    public Object getChild(Object attrValue) {
	      return children.get(attrValue);
	    }
	    public void setChild(Object attrValue, Object child) {
	      children.put(attrValue, child);
	    }
	    public Set<Object> getAttributeValues() {
	      return children.keySet();
	    }
	    public Object sampleMatch(Sample sample) {
	    	
	    	Object nextNode = this.getChild(sample.getAttribute(attribute));	    	
	    	if (nextNode instanceof Tree) {
	    		return ((Tree) nextNode).sampleMatch(sample);
	    	}else {
	    		return nextNode;
	    	}
	    	//this.getChild(sample.getAttribute(attribute));
	    	//return "error";
	    }
	  }
	}

/*public class DicisionTree {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("123");
	}

}*/

