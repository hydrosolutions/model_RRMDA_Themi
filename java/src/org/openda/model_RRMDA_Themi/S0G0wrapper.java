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
 * The file read here is the initial states file S0G0.mat produced by the rainfall-
 * runoff model RRMDA by hydrosolutions ltd. In the case of the Themi model it
 * reads 2 states (soil moisture storage and groundwater storage) for 8 sub-catchments.
 * 3 variables are stored in S0G0.mat: 
 * <p>
 * S0: Initial soil moisture storage with size(S0)=[1,<number_of_subcatchments>].<br>
 * G0: Initial groundwater storage with size(G0)=[1,<number_of_subcatchments>].<br>
 * SG: 100 replicates of soil moisture storage and groundwater storage computed with 
 *     the rainfall-runoff model for each sub-catchment with size(SG)=[16,100]. <br>
 * <p>
 * S0 and G0 need to be read to initialize the rainfall-runoff model. They are stored 
 * in individual exchange items to allow full flexibility in assigning uncertainties 
 * to each state. SG contains the same values for soil moisture storage and groundwater
 * storage as E.mat and is read by the rainfall-runoff model but immediately overwritten 
 * with S0 and G0 so it doesn't have to be figured as extra exchange item. However it  
 * has to be written for the rainfall-runoff model to run successfully. Therefore SG 
 * is read to a class-owned variable and written from this variable. 
 * <p>
 * The data in S0G0.mat comes without time reference. The time reference has to be 
 * figured out in this wrapper. The file S0G0.mat is written on a daily basis. For the 
 * current run yesterdays file is read, modified and stored again. Therefore, the 
 * time reference of the data can be read from the modified date of the file S0G0.mat.
 * This should also work if the file has been modified today. 
 * <p>
 * The rainfall-runoff model does not offer the possibility to operate with stochastic 
 * initial values for soil moisture storage and groundwater storage. It can be done, 
 * however, with this implementation of <code>openDA</code>-wrapper. S0G0.mat does not 
 * need any fancy clean up method to be compatible with <code>openDA</code>.
 *  
 * @author Beatrice Marti, hydrosolutions ltd., marti@hydrosolutions.ch
 *
 * Copyright (c) 2015, hydrosolutions ltd.
 */

public class S0G0wrapper implements IoObjectInterface {

	// Class specific values
	File workingDir;
	String configString;
	String fileName = null;
	int[] readS0Dim = {0,0};
	int[] readG0Dim = {0,0};
	int[] readSGDim = {0,0};
	double[] valuesSG;
	HashMap<String, TimeSeries> items = new LinkedHashMap<String, TimeSeries>();
	HashMap<String, IExchangeItem> map = new HashMap<>();
	int numberOfStatesInSG = 16;
	
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
			
			class Local {};
	        String methodName = Local.class.getEnclosingMethod().getName();
	        String className = this.getClass().getName();
			
			this.workingDir = workingDir;
			this.fileName = fileName;
			
			double refdate = 0;
			double[] time = new double[1];
			double[] value = new double[1];
			double[][] sData;
			double[][] gData;
			double[][] sgData;
			String id;
			
			File file = new File(workingDir, fileName);
			if (!file.exists()) {
				throw new RuntimeException("Ewrapper.initialize(): input file "
						+ file.getAbsolutePath() + " does not exist");
			}
			
			
			try {
				
				// Read the .mat file.
				MatFileReader matfilereader = new MatFileReader( file );
				MLArray readS0 = matfilereader.getMLArray("S0");
				MLArray readG0 = matfilereader.getMLArray("G0");
				MLArray readSG = matfilereader.getMLArray("SG");
				
				// Parse the MLArray to an array of doubles. E contains an ensemble of states 
				// and parameters. We're interested here only in the central model: The first column.
				readS0Dim = readS0.getDimensions();
				readG0Dim = readG0.getDimensions();
				readSGDim = readSG.getDimensions();
				
				int readDataType = readS0.getType();
				if (readDataType != 6) {
					throw new RuntimeException(className+"."+methodName+"(): input file " + file.getAbsolutePath() + 
							" problem reading file type.\n   Expected " + MLArray.typeToString(6) + " but got " + 
							MLArray.typeToString(readDataType));
				}
				readDataType = readG0.getType();
				if (readDataType != 6) {
					throw new RuntimeException(className+"."+methodName+"(): input file " + file.getAbsolutePath() + 
							" problem reading file type.\n   Expected " + MLArray.typeToString(6) + " but got " + 
							MLArray.typeToString(readDataType));
				}
				readDataType = readSG.getType();
				if (readDataType != 6) {
					throw new RuntimeException(className+"."+methodName+"(): input file " + file.getAbsolutePath() + 
							" problem reading file type.\n   Expected " + MLArray.typeToString(6) + " but got " + 
							MLArray.typeToString(readDataType));
				}
				
				// Cast to MLDouble only after verifying that the type of readData is indeed a double array. 
				sData = ((MLDouble) readS0).getArray();
				gData = ((MLDouble) readG0).getArray();
				sgData = ((MLDouble) readSG).getArray();
				
				// Read the modified date from the file S0G0.mat.
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
				try {
					refdate = org.openda.exchange.timeseries.TimeUtils.date2Mjd(sdf.format(file.lastModified()),"yyyyMMdd");
				} catch (ParseException e) {
					e.printStackTrace();
				}	
				//System.out.println(sdf.format(file.lastModified()) + " , " + refdate);
				
				// Store the model states in an array. 
				valuesSG = new double[readSGDim[0]*readSGDim[1]];
				for (int j=0; j<readSGDim[1]; j++) {
					for (int i = 0; i<readSGDim[0]; i++) {
						valuesSG[j*readSGDim[0]+i] = sgData[i][j];						
					}
				}
				
				time[0] = refdate;
				
				TimeSeries temp;
				// Store data in exchange items.
				for (int i=0; i<readS0Dim[1]; i++) {
					value[0] = sData[0][i];
					id = "S0"+(i+1);
					temp = new TimeSeries(time,value);
					temp.setId(id);
					this.items.put(id,temp);
					IExchangeItem x = new TimeSeries(temp);
					map.put(id, x);
				}
				for (int i=0; i<readG0Dim[1]; i++) {
					value[0] = gData[0][i];
					id = "G0"+(i+1);
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
			double[] dataS0 = new double[readS0Dim[1]];
			for (int i = 0; i<readS0Dim[1]; i++) {
				IPrevExchangeItem ei = map.get("S0"+(i+1));
				dataS0[i] = ei.getValuesAsDoubles()[0];
			}
			double[] dataG0 = new double[readG0Dim[1]];
			for (int i = 0; i<readG0Dim[1]; i++) {
				IPrevExchangeItem ei = map.get("G0"+(i+1));
				dataG0[i] = ei.getValuesAsDoubles()[0];
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
					
					MLDouble mlDoubleS0 = new MLDouble( "S0", dataS0, 1 ); // Store test in 1 row.
					MLDouble mlDoubleG0 = new MLDouble( "G0", dataG0, 1);
					MLDouble mlDoubleSG = new MLDouble( "SG", valuesSG, numberOfStatesInSG);
			        ArrayList<MLArray> list = new ArrayList<MLArray>();
			        list.add( mlDoubleS0 );
			        list.add( mlDoubleG0 );
			        list.add( mlDoubleSG );
			        
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
