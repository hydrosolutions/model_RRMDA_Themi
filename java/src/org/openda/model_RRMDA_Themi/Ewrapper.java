package org.openda.model_RRMDA_Themi;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;

import org.openda.blackbox.interfaces.IoObjectInterface;
import org.openda.exchange.timeseries.TimeSeries;
import org.openda.interfaces.IExchangeItem;
import org.openda.interfaces.IPrevExchangeItem;

import com.jmatio.io.MatFileReader;
import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;

/**
 * Allows reading of a matlab .mat file and storing read data in <code>oda</code> 
 * exchange items. 
 * <p>
 * The file read here is the initial states and parameter file E.mat produced by 
 * the rainfall-runoff model by hydrosolutions ltd. In the case of the Themi model 
 * it reads 8 states and parameters for 8 sub-catchments so a total of 64 states 
 * and parameters. They are read to individual exchange items in order to allow 
 * maximum flexibility for assigning different levels of uncertainty to all of them.
 * The order in which the states and paramters are stored in E.mat is: 
 * <p>
 * [ Q; S; G; ETact; alpha1; alpha2; alpha3 (d); Smax ]
 * <p>
 * <p>
 * The data in E.mat comes without time reference. The time reference has to be 
 * figured out in this wrapper. The file E.mat is written on a daily basis. For the 
 * current run yesterdays file is read, modified and stored again. Therefore, the 
 * time reference of the data can be read from the modified date of the file E.mat.
 * This should also work if the file has been modified today. 
 * <p>
 * E.mat contains 100 replicates of the states, all perturbed. The matlab rainfall
 * runoff model (RRM) uses the first 21 of them to compute stochastic model forecasts.
 * A workaround is needed here to make RRM compatible. The first row (= the central 
 * model) is read into exchange items. The rows containing the stochastic replicates
 * of the central model are ignored because openDA accounts for them. When writing, 
 * the wrapper copies the current replicate of E to the number of columns of E.mat 
 * that have been read initially. Thereby it is assured that E.mat has the format it 
 * needs to have to be read by RRMDA_Themi. A clean up routine is needed after each 
 * call to openDA to copy the contents of E.mat (among others) in the openDA working 
 * directories to the original E.mat such that the following routines in RRMDA_Themi 
 * can run successfully. 
 *  
 * @author Beatrice Marti, hydrosolutions ltd., marti@hydrosolutions.ch
 *
 * Copyright (c) 2015, hydrosolutions ltd.
 */
public class Ewrapper implements IoObjectInterface {

		
	// Class specific values
	File workingDir;
	String configString;
	String fileName = null;
	int[] readDataDim = {0,0};
	HashMap<String, TimeSeries> items = new LinkedHashMap<String, TimeSeries>();
	HashMap<String, IExchangeItem> map = new HashMap<>();
	String[] exchangeItemIDs = new String[] {"Q1","S1","G1","ETact1","a11","a21","a31","Smax1",
			 								 "Q2","S2","G2","ETact2","a12","a22","a32","Smax2",
											 "Q3","S3","G3","ETact3","a13","a23","a33","Smax3",
											 "Q4","S4","G4","ETact4","a14","a24","a34","Smax4",
											 "Q5","S5","G5","ETact5","a15","a25","a35","Smax5",
											 "Q6","S6","G6","ETact6","a16","a26","a36","Smax6",
											 "Q7","S7","G7","ETact7","a17","a27","a37","Smax7",
											 "Q8","S8","G8","ETact8","a18","a28","a38","Smax8"};
	
	/**
	 * Initialize the IoObject. Reads the content of a .mat file (fileName) in
	 * directory (workingDir) with given arguments.
	 * 
	 * @param workingDir
	 *            Working directory
	 * @param fileName
	 *            The name of the file containing the data (relative to the
	 *            working directory.)
	 * @param arguments
	 *            Additional arguments (may be null zero-length)
	 */
	public void initialize(File workingDir, String fileName, String[] arguments) {
		
		this.workingDir = workingDir;
		this.fileName = fileName;
		
		double refdate = 0;
		double[] values;
		double[] time = new double[1];
		double[] value = new double[1];
		double[][] d;
		String id;
		
		File file = new File(workingDir, fileName);
		if (!file.exists()) {
			throw new RuntimeException("Ewrapper.initialize(): input file "
					+ file.getAbsolutePath() + " does not exist");
		}
		
		
		try {
			
			// Read the .mat file.
			MatFileReader matfilereader = new MatFileReader( file );
			MLArray readData = matfilereader.getMLArray("E");
			
			// Parse the MLArray to an array of doubles. E contains an ensemble of states 
			// and parameters. We're interested here only in the central model: The first column.
			readDataDim = readData.getDimensions(); // e.g. [number of rows, number of columns] for 2D arrays. 
			int readDataType = readData.getType();
			if (readDataType != 6) {
				throw new RuntimeException("Ewrapper.initialize(): input file " + file.getAbsolutePath() + 
						" problem reading file type.\n   Expected " + MLArray.typeToString(6) + " but got " + 
						MLArray.typeToString(readDataType));
			}
			
			// Cast to MLDouble only after verifying that the type of readData is indeed a double array. 
			d = ((MLDouble) readData).getArray(); 
			
			// Read the modified date from the file E.mat.
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			try {
				refdate = org.openda.exchange.timeseries.TimeUtils.date2Mjd(sdf.format(file.lastModified()),"yyyyMMdd");
			} catch (ParseException e) {
				e.printStackTrace();
			}	
			//System.out.println(sdf.format(file.lastModified()) + " , " + refdate);
			
			// Store the central model states and parameters in an array. 
			values = new double[readDataDim[0]];
			for (int i = 0; i<readDataDim[0]; i++) {
				values[i] = d[i][0];
			}
			
			time[0] = refdate;
			
			TimeSeries temp;
			// Store data in exchange items.
			for (int i = 0; i<readDataDim[0]; i++) {
				value[0] = values[i];
				id = exchangeItemIDs[i];
				temp = new TimeSeries(time,value);
				temp.setId(id);
				this.items.put(id,temp);
				IExchangeItem x = new TimeSeries(temp);
				map.put(id, x);
			}
		    
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	   
		
		
	}

	/**
	 * Ask which elements can be accessed
	 * 
	 * 
	 * @return The list of element identifiers that can be accessed
	 */
	public IPrevExchangeItem[] getExchangeItems() {
		// Get the number of items.
		int n = this.items.size();
		Set<String> keys = this.items.keySet();
		IExchangeItem[] result = new IExchangeItem[n];
		int i = 0;
		for (String key : keys) {
			result[i] = this.items.get(key);
			i++;
		}
		return result;
	}

	public void finish() {
		
		class Local {};
        String methodName = Local.class.getEnclosingMethod().getName();
        String className = this.getClass().getName();
		
		// Iterate over hash map items and store data in a double array.
		double[] data = new double[readDataDim[0]*readDataDim[1]];
		if (items.size() != readDataDim[0]) {
			throw new RuntimeException(className + "." + methodName + ": Problem with the dimension of read data.");
		}
		for (int i = 0; i<items.size(); i++) {
			IPrevExchangeItem ei = map.get(exchangeItemIDs[i]);
			data[i] = ei.getValuesAsDoubles()[0];
		}
		// Store the same data in the same number of columns that has been read from E.mat.
		for (int j=1; j<readDataDim[1]; j++) {
			for (int i=0;i<readDataDim[0];i++) {
				data[i+j*readDataDim[0]] = data[i];
			}
		}
		
		// Writing.
		System.out.println(className + "." + methodName + ": Writing to file: "+
                           this.workingDir + System.getProperty("line.separator") + this.fileName);
		try{
			File outputFile = new File(this.workingDir,this.fileName);
			try{
				if(outputFile.isFile()){
					outputFile.delete();
				}
			}catch (Exception e) {
				System.out.println(className + "." + methodName + ": trouble removing file "+ fileName);
			}
			try {
				
				MLDouble mlDouble = new MLDouble( "E", data, 64 ); // Store in 64 rows.
		        ArrayList<MLArray> list = new ArrayList<MLArray>();
		        list.add( mlDouble );
		        
		        new MatFileWriter( outputFile, list );
				
			} catch (Exception e) {
				throw new RuntimeException(className + "." + methodName + ": Problem writing to file " + 
						                   fileName+" : " + System.getProperty("line.separator") + e.getMessage());
			}
		}catch (Exception e) {
			System.out.println(className + "." + methodName + ": Problem creating file.");
		}
		
	}

	

}
