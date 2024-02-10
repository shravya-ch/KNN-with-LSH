package assignment_2;
//ReadMe file has the instructions to run the program
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Scanner;

public class nn_search {
	//list of permutations
	static List<int[]> pi = new ArrayList<int[]>();
	//sorted lists of permutations
	static HashMap<Integer,List<int[]>> T = new HashMap<>();
	//list  of input arrays 
	static List<double[]> in =new ArrayList<double[]>();
	//list of input arrays with reduced dimensions
	static List<int[]> t =new ArrayList<int[]>();

	public static void main(String[] args) throws  ClassNotFoundException, IOException {
		System.out.print("Enter the program and parameters ");
		Scanner sc= new Scanner(System.in);	
		//reading inputs from the given path
		String[] inputs = sc.nextLine().split("\\s");
		sc.close();
		if(inputs[0].equals("nn_search")) {
			//reading the inputs
			String search_file = inputs[1];
			int k = Integer.parseInt(inputs[2]);
			int C = Integer.parseInt(inputs[3]);
			
			ObjectInputStream in1 = new ObjectInputStream(new FileInputStream("T.ser"));
			T= (HashMap<Integer, List<int[]>>) in1.readObject();
			in1.close();
			
			ObjectInputStream in2 = new ObjectInputStream(new FileInputStream("in.ser"));
			in = (ArrayList<double[]>)in2.readObject();
			in2.close();
			
			ObjectInputStream in3 = new ObjectInputStream(new FileInputStream("t.ser"));
			t = (ArrayList<int[]>) in3.readObject();
			in3.close();
			
			Doc_Names(search_file,k,C);}
		else {
			System.out.println("enter inputs correctly");}
	}
	
	public static void Doc_Names(String search_file, int k, int c) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(search_file));
		String line;
		//reading the queries line by line
		List<double[]> queries =new ArrayList<double[]>();
		while (((line = reader.readLine()) != null)) {
			// if the line is  a comment, is  empty or is a
			// kind of metadata
			if (line.isEmpty() == true ||
				line.charAt(0) == '#' || line.charAt(0) == '%'
				|| line.charAt(0) == '@') {
				continue;}
			String[] lineSplited = line.split(" ");
			double[] input_vector = new double[lineSplited.length];
			for (int i = 0; i < lineSplited.length; i++) { 
				input_vector[i] = Double.parseDouble(lineSplited[i]);
			}
			queries.add(input_vector);
		}
		reader.close(); 
		
		//generating the unit vectors from Gaussian same as in index program 
		int d = queries.get(0).length ;
		
		int D = T.get(0).get(0).length;
		List<double[]> u = new ArrayList<double[]>();
		for (int j=0;j<D;j++) {
			Random r = new Random(j);
			double[] unit_vector = new double[d];
			double norm=0.0;
			for (int i=0 ;i<d ;i++) {
				unit_vector[i] = r.nextGaussian();
				norm += (unit_vector[i]*unit_vector[i]);
			}
			for (int i=0;i<d;i++) {
				unit_vector[i] = (unit_vector[i]/norm);
			}
			u.add(unit_vector);
		}
		
		//modifying the query vectors to {0,1} of D dimensions
		List<int[]> queries_D =new ArrayList<int[]>();
		for (int i =0;i<queries.size();i++) {
			int[] map_vector = new int[u.size()];
			for (int j =0 ;j<u.size();j++) {
				//u.get(j);
				double sum =0.0;
				for (int p =0;p<queries.get(i).length;p++){
					sum += queries.get(i)[p] * u.get(j)[p];
				}
			if (sum >= 0) {map_vector[j]=1;}
			else {map_vector[j]=0;}
			}
			queries_D.add(map_vector);
		}
		
		int L = T.size();
		//generating random permutations same as in  index program
		int[] perm = new int[D];
		for(int i = 0; i < D; i++) {
	        perm[i] = i;}
		for (int i=0;i<L ;i++) {
			Random r = new Random(i);
			int[] permutation = Arrays.copyOf(perm, D);
			for(int j = 0; j < D; j++){
		        int ran = j + r.nextInt (D-j);
		        int temp = permutation[j];
		        permutation[j] = permutation[ran];
		        permutation[ran] = temp;
		    }
			pi.add(permutation); 
		}
		
		for (int i=0;i<queries_D.size();i++) {
			//get each query
			List<int[]> Q1 = new ArrayList<int[]>();
			for (int m=0;m<pi.size();m++) {
				//get each permutation
				int[] tp1 = new int[D];
				for(int j=0;j<D;j++) {
					tp1[j] = queries_D.get(i)[pi.get(m)[j]];
				}
				//add list of permuted arrays of each query 
				Q1.add(tp1);
			}
			
			knn_elements(Q1,i,c,k,queries.get(i));
		}
	}
	 
	public static void knn_elements(List<int[]> ex,int query_number,int c,int k,double[] queries) {
		//bounds_list has the pointers of nearest numbers 
		List<int[]> bounds_list = new ArrayList<int[]>();
		for (int i=0;i<ex.size();i++) {
			//ex.get(i);
			
			List<int[]> t = T.get(i);
			
			int first =0 ;
			int last = t.size()-1;
			int mid = (first+last)/2;
			boolean flag = false;
			//binary search
			while (first<=last) {
				int count = 0;
				for (int j=0;j<t.get(mid).length;j++) {
					if(t.get(mid)[j]==ex.get(i)[j]) {count++ ;if(count== t.get(mid).length){flag=true;break;}
													else {continue;}}
					else if (t.get(mid)[j]>ex.get(i)[j]) {
					        last = mid-1;break;}
					else  {first = mid+1;break;}
				}
				if (flag) {break;}
				else {mid=(first+last)/2;}
			}
			//perfect match
			if (flag) {
				if((mid-1) >=0) {last= mid-1;}
				else {last =mid;}
				if(mid+1 < t.size()) {first=mid+1;}
				else {first=mid;}}
			//setting the bounds for last and first not to exceed the size of lists
			if (last < 0) {last =0;first=1;}
			if (first >= t.size()) {first=t.size()-1;last=t.size()-2;}
			int[] bounds = {last,first};
			bounds_list.add(bounds);
		}	
		
		List<int[]> F = new ArrayList<int[]>();
		//collecting 2L+C elements to the list 
		while (F.size() <= (2*ex.size()) + c ) {
			for (int i=0;i<ex.size();i++) {
			int[] q = ex.get(i);
			
			List<int[]> t = T.get(i);
			int count1 =0; int count2 =0;
			int[] first = t.get(bounds_list.get(i)[0]); 
			int[] last= t.get(bounds_list.get(i)[1]);
			for (int j=0;j<ex.get(i).length;j++){
				if(first[j]== q[j]) {count1++;}
				if(last[j]== q[j]) {count2++;}
				if (count1 != count2) {break;}
			}
			//getting element with maximum prefix match
			int[] origin = new int[pi.get(i).length] ;
			if (count1 > count2) {
				for (int j=0;j<pi.get(i).length;j++) {
					origin[pi.get(i)[j]] = first[j];
				}
				//adding the original binary vector from permuted t array
				F.add(origin);
				if((bounds_list.get(i)[0]-1) >=0) 
				{bounds_list.get(i)[0] = bounds_list.get(i)[0]-1;}
				else {bounds_list.get(i)[0] = bounds_list.get(i)[0];}
			}
			else {
				for (int j=0;j<pi.get(i).length;j++) {
					origin[pi.get(i)[j]] = last[j];
				}
				//adding the original binary vector from permuted t array
				F.add(origin);
				if((bounds_list.get(i)[1]+1) <t.size()) 
					bounds_list.get(i)[1] = bounds_list.get(i)[1]+1;
				else {bounds_list.get(i)[1] = bounds_list.get(i)[1];}
			}
			if(F.size() == (2*ex.size()) + c) {break;}
		}
		if(F.size() == (2*ex.size()) + c) {break;}
	}
		//finding the original index from t for bounds and calculating the 
		//cosine distance from the actual input array .Saved to hashmap dist
		//with index of input array and cosine distance
	Map<Integer,Double> dist = new HashMap<Integer,Double>();
	for (int[] f : F) {
		for(int i=0;i<t.size();i++) {
			if(Arrays.equals(f, t.get(i))) {
				double sum =0.0;
				for(int j=0;j<in.get(i).length;j++) {sum += in.get(i)[j]*queries[j];} //queries
				sum = ((Math.acos(sum))/Math.PI);
				dist.put(i,sum );
				break;
			};
		}
	}
	
	// Create a list from elements of HashMap 
    List<Map.Entry<Integer,Double> > list = 
           new LinkedList<Map.Entry<Integer,Double> >(dist.entrySet()); 

    // Sort the list according to the cosine distance
    Collections.sort(list, new Comparator<Map.Entry<Integer,Double> >() { 
        public int compare(Map.Entry<Integer,Double> o1,  
                           Map.Entry<Integer,Double> o2) 
        { 
            return (o1.getValue()).compareTo(o2.getValue()); 
        } 
    }); 
    
    //print the KNN Arrays from input file for each query with number starting from zero
    int final_count =0;
    System.out.println("Query"+query_number);
    for (Entry<Integer, Double> a : list) { 
    	final_count++;
       System.out.println(Arrays.toString( in.get(a.getKey())));
       if(final_count==k) {break;}
    } 
		
	}
	
}
