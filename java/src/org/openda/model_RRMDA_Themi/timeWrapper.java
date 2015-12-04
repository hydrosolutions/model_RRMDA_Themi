package org.openda.model_RRMDA_Themi;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.openda.blackbox.interfaces.IoObjectInterface;
import org.openda.exchange.DoubleExchangeItem;
import org.openda.interfaces.IExchangeItem;
import org.openda.interfaces.IPrevExchangeItem;

import com.jmatio.io.MatFileReader;
import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;

public class timeWrapper implements IoObjectInterface{

	// Define a private class to cache the exchange items.
	private class TimeWrapperExchangeItem {
		
		public IExchangeItem exchangeItem;

		TimeWrapperExchangeItem(IExchangeItem exchangeItem) {
			this.exchangeItem = exchangeItem;
		}
	}
	
	// Class specific values
	File workingDir;
	String configString;
	String fileName = null;
	HashMap<String, TimeWrapperExchangeItem> map = new HashMap<String, TimeWrapperExchangeItem>();
		
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
			
		double startTime = 0;
		double endTime = 0;
		double step = 0;
		double[][] timetemp;
			
		File file = new File(workingDir, fileName);
		if (!file.exists()) {
			throw new RuntimeException("timeWrapper.initialize(): input file "
					+ file.getAbsolutePath() + " does not exist");
		}
			
			
		try {
				
			// Read the .mat file.
			MatFileReader matfilereader = new MatFileReader( file );
			MLArray readStartTime = matfilereader.getMLArray("startTime");
			MLArray readEndTime = matfilereader.getMLArray("endTime");
			MLArray readStep = matfilereader.getMLArray("step");
				
			// Parse the MLArray to an array of doubles. E contains an ensemble of states 
			// and parameters. We're interested here only in the central model: The first column.
				
			timetemp = ((MLDouble) readStartTime).getArray();
			startTime = timetemp[0][0];
			timetemp = ((MLDouble) readEndTime).getArray();
			endTime = timetemp[0][0];
			timetemp = ((MLDouble) readStep).getArray();
			step = timetemp[0][0];
				
			IExchangeItem startTimeExchangeItem = new DoubleExchangeItem("startTime", startTime);
			this.map.put("startTime", new TimeWrapperExchangeItem(startTimeExchangeItem));
			    
			IExchangeItem endTimeExchangeItem = new DoubleExchangeItem("endTime", endTime);
			this.map.put("endTime", new TimeWrapperExchangeItem(endTimeExchangeItem));
			
			IExchangeItem stepExchangeItem = new DoubleExchangeItem("step", step);
			this.map.put("step", new TimeWrapperExchangeItem(stepExchangeItem));
			
			
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
		int n = this.map.size();
		Set<String> keys = this.map.keySet();
		IExchangeItem[] result = new IExchangeItem[n];
		int i = 0;
		for (String key : keys) {
			result[i] = this.map.get(key).exchangeItem;
			i++;
		}
		return result;
	}

	public void finish() {
			
		class Local {};
	    String methodName = Local.class.getEnclosingMethod().getName();
	    String className = this.getClass().getName();
		
		// Iterate over hash map items and store data in a double array.
		double[] startTime = this.map.get("startTime").exchangeItem.getValuesAsDoubles();
		double[] endTime = this.map.get("endTime").exchangeItem.getValuesAsDoubles();
		double[] step = this.map.get("step").exchangeItem.getValuesAsDoubles();
			
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
				
				MLDouble mlstart = new MLDouble( "startTime", startTime, 1 ); // Store in 1 row.
				MLDouble mlend = new MLDouble("endTime",endTime,1);
				MLDouble mlstep = new MLDouble("step",step,1);
		        ArrayList<MLArray> list = new ArrayList<MLArray>();
		        list.add( mlstart );
		        list.add(mlend);
		        list.add(mlstep);
		        
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
