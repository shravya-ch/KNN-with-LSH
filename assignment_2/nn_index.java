package assignment_2;
//ReadMe file has the instructions to run the program
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Scanner;
import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class nn_index {
	//list of permutations
	static List<int[]> pi = new ArrayList<int[]>();
	//sorted lists of permutations
	static HashMap<Integer,List<int[]>> T = new HashMap<>();
	//list  of input arrays 
	static List<double[]> in =new ArrayList<double[]>();
	//list of input arrays with reduced dimensions
	static List<int[]> t =new ArrayList<int[]>();
	public static void main(String[] args) throws IOException {
		System.out.print("Enter the program and parameters ");
		Scanner sc= new Scanner(System.in);		
		String[] inputs = sc.nextLine().split("\\s");
		sc.close();
		
		if (inputs[0].equals("nn_index")) {
			//reading the given inputs
			File folder = new File(inputs[1]);
			int d =  Integer.parseInt(inputs[2]);
			int L =  Integer.parseInt(inputs[3]);
			int D =  Integer.parseInt(inputs[4]);
			listFilesForFolder(folder,d,L,D);}
		else {System.out.println("enter inputs correctly");}}

	public static void listFilesForFolder(File folder, int d, int L, int D) throws IOException {
		//generating the unit vectors from Gaussian
		List<double[]> u = new ArrayList<double[]>();
		for (int j=0;j<D;j++) {
			Random r = new Random(j);
			double[] unit_vector = new double[d];
			double norm=0.0;
			for (int i=0 ;i<d ;i++) {
				unit_vector[i] = (r.nextGaussian());
				norm += ((unit_vector[i]*unit_vector[i]));
			}
			for (int i=0;i<d;i++) {
				unit_vector[i] = (unit_vector[i]/norm);
			}
			u.add(unit_vector);
		}	
		
		BufferedReader reader = new BufferedReader(new FileReader(folder));
		String line;
		//reading the input arrays from the file line by line 
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
			in.add(input_vector);
		}
		reader.close(); 
		
		//modifying the input vectors to {0,1} of D dimensions 
		for (int i =0;i<in.size();i++) {
			//in.get(i);
			int[] map_vector = new int[u.size()];
			for (int j =0 ;j<u.size();j++) {
				//u.get(j);
				double sum =0.0;
				for (int p =0;p<in.get(i).length;p++){
					sum += in.get(i)[p] * u.get(j)[p];
				}
			if (sum >= 0) {map_vector[j]=1;}
			else {map_vector[j]=0;}
			}
			t.add(map_vector);
		}
		
		//generating random permutations 
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
			pi.add(permutation) ;
		}
		
		for (int k=0;k<pi.size();k++) {
		//get each permutation
			List<int[]> T1 = new ArrayList<int[]>();
			for (int i=0;i<t.size();i++) {
			//get each t input binary vector 
			int[] tp1 = new int[D];
			//permute it 
				for(int j=0;j<D;j++) {
					tp1[j] = t.get(i)[pi.get(k)[j]];
				}
				//add all the vectors to list
				T1.add(tp1);
			}
			
			//sort the vectors according to the lexicographic ordering 
			Collections.sort(T1,new Comparator<int[]>() {
			    public int compare(int[] a, int[] b) {
			    	int p =0;
			    	for(int i=0;i<a.length;i++){
				        if (a[i] == b[i]){continue;}
				        else {p=i;break;}
			    	}
			    	int compare = a[p]-b[p];
					return compare; 
			    }});
			//Map each set of permuted list of input binary D dimensional vectors to 
			//to hash map with integer key 
			T.put(k, T1);
			
		}
		//save the files 	
		
		ObjectOutputStream out_1 = new ObjectOutputStream(new FileOutputStream("T.ser"));
		out_1.writeObject(T);
		out_1.flush();
		out_1.close();
		
		ObjectOutputStream out_2 = new ObjectOutputStream(new FileOutputStream("in.ser"));
		out_2.writeObject(in);
		out_2.flush();
		out_2.close();
		
		ObjectOutputStream out_3 = new ObjectOutputStream(new FileOutputStream("t.ser"));
		out_3.writeObject(t);
		out_3.flush();
		out_3.close();
		System.out.println("Index Files are saved");
		//Memeory of the saved index 
		String dir = System.getProperty("user.dir");
		File file = new File (dir+"\\T.ser") ;
    	double total_mb = file.length()/(1024.0*1024.0);
    	System.out.printf("Total Storage space in MB %f",total_mb);
    	
    	int c = 50;
    	//k values for nearest neighbor
    	int[] k = {10,20,30};
    	int max_k =30;
    	List<int[]> knn = new ArrayList<int[]>();
    	for (int i=0;i<1000;i++) {
			//get each input
			List<int[]> Q1 = new ArrayList<int[]>();
			for (int m=0;m<pi.size();m++) {
				//get each permutation
				int[] tp1 = new int[D];
				for(int j=0;j<D;j++) {
					tp1[j] = t.get(i)[pi.get(m)[j]];
				}
				//list of permutations of each input vector is passed
				Q1.add(tp1);
			}
			
			knn.add(knn_elements(Q1,i,c,in.get(i),max_k));
		}
    	
    	List<int[]> knn_actual = new ArrayList<int[]>();
    	//for each input array upto 1000 vectors
    	for (int i=0;i<1000;i++) {
    		Map<Integer,Double> dist = new HashMap<Integer,Double>();
        	for(int j =0 ;j<1000;j++) {
        		//take all other vectors 
        		if(i==j) {continue;}
        		else {
        			//calculate distance for each input vector with others
        			dist.put(j,calculate_dist(in.get(i),in.get(j)));
        		}
        	}
        	//sort and add nearest k elements
        	knn_actual.add(sort_dist(dist,max_k));
        }
    	
    	double recall = 0.0;
    	for (int each :k ) {
    		//for maximum k finding intersection 
    		if(each == max_k) {
    			double sum=0.0;
    			for (int i=0;i<1000;i++) {
    				//find elements in common and calculate frequency
    				HashSet<Integer> set = new HashSet<>(); 
    				Integer[] arr1 = Arrays.stream(knn.get(i)).boxed().toArray(Integer[]::new);
    				Integer[] arr2 = Arrays.stream(knn_actual.get(i)).boxed().toArray(Integer[]::new);
    				
    		        set.addAll(Arrays.asList(arr1));
    		        set.retainAll(Arrays.asList(arr2));
    		        sum += (double)set.size()/each;
    			}
    			//calculating the recall for all 1000 vectors
    			recall +=((sum/1000.0)*100.0);
    		}
    		else {
    		//for lesser values of k (<max_k)
    			double sum =0.0;
    			for (int i=0;i<1000;i++) {
    				//trim the arrays and find elements in common
    				int[] a = Arrays.copyOf(knn.get(i), each);
    				int[] b = Arrays.copyOf(knn_actual.get(i), each);
    				HashSet<Integer> set = new HashSet<>(); 
    				Integer[] arr1 = Arrays.stream(a).boxed().toArray(Integer[]::new);
    				Integer[] arr2 = Arrays.stream(b).boxed().toArray(Integer[]::new);
    				
    		        set.addAll(Arrays.asList(arr1));
    		        set.retainAll(Arrays.asList(arr2));
    				sum += (double)set.size()/each;
    			}
    			//calculating the recall for all 1000 vectors
    			recall += ((sum/1000.0)*100.0);
    		}
    	}
    	
    	System.out.printf("\n recall percentage %f", recall/3);
	}
	
	private static int[] sort_dist(Map<Integer,Double> dist_map,int k) {
		List<Map.Entry<Integer,Double> > list = 
		           new LinkedList<Map.Entry<Integer,Double> >(dist_map.entrySet()); 

		    // Sort the list according to the cosine distance
		    Collections.sort(list, new Comparator<Map.Entry<Integer,Double> >() { 
		        public int compare(Map.Entry<Integer,Double> o1,  
		                           Map.Entry<Integer,Double> o2) 
		        { 
		            return (o1.getValue()).compareTo(o2.getValue()); 
		        } 
		    }); 
		    //return the nearest k neighbors
		    int final_count =0;
		    int[] temp =new int[k];
		    for (Entry<Integer, Double> a : list) { 
		    	temp[final_count] = a.getKey();
		    	final_count++;
		        if(final_count==k) {break;}
		    }
		    return temp;
	}

	public static double calculate_dist(double[] ds,double[] ds2) {
		//calculating cosine distance
		double sum=0.0;
		for(int j=0;j<ds.length;j++) {sum += ds[j]*ds2[j];} 
		sum = ((Math.acos(sum))/Math.PI);;
		return sum;	
	}

	
	
	public static int[] knn_elements(List<int[]> ex,int query_number,int c,double[] queries,int k) {
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
	    
	    int final_count =0;
	    int[] temp =new int[k];
	    for (Entry<Integer, Double> a : list) { 
	    	temp[final_count] = a.getKey();
	    	final_count++;
	        if(final_count==k) {break;}
	    }
	    //return the elements with least distor highest similarity
	    return temp;
	}
	
}

