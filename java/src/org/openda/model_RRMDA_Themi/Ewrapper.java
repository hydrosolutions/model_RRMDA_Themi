package org.openda.model_RRMDA_Themi;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;

import org.openda.blackbox.interfaces.IoObjectInterface;
import org.openda.exchange.timeseries.TimeSeries;
import org.openda.interfaces.IExchangeItem;
import org.openda.interfaces.IPrevExchangeItem;

import com.jmatio.io.MatFileFilter;
import com.jmatio.io.MatFileReader;
import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLChar;
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
 *  
 * @author Beatrice Marti, hydrosolutions ltd., marti@hydrosolutions.ch
 *
 */
public class Ewrapper implements IoObjectInterface {

		
	// Class specific values
	File workingDir;
	String configString;
	String fileName = null;
	HashMap<String, TimeSeries> items = new LinkedHashMap<String, TimeSeries>();

	
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
			int[] readDataDim = readData.getDimensions(); // e.g. [number of rows, number of columns] for 2D arrays. 
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
			
			// Store data in exchange items.
			value[0] = values[0];
			TimeSeries tsc1Q = new TimeSeries(time,value);
			id = "Q1";
			tsc1Q.setId(id);
			this.items.put(id,tsc1Q);
			@SuppressWarnings("unused")
			IExchangeItem c1Q = new TimeSeries(tsc1Q);
			
			value[0] = values[1];
			TimeSeries tsc1S = new TimeSeries(time,value);
			id = "S1";
			tsc1S.setId(id);
			this.items.put(id,tsc1S);
			@SuppressWarnings("unused")
			IExchangeItem c1S = new TimeSeries(tsc1S);
			
			value[0] = values[2];
			TimeSeries tsc1G = new TimeSeries(time,value);
			id = "G1";
			tsc1G.setId(id);
			this.items.put(id,tsc1G);
			@SuppressWarnings("unused")
			IExchangeItem c1G = new TimeSeries(tsc1G);
			
			value[0] = values[3];
			TimeSeries tsc1ETact = new TimeSeries(time,value);
			id = "ETact1";
			tsc1ETact.setId(id);
			this.items.put(id,tsc1ETact);
			@SuppressWarnings("unused")
			IExchangeItem c1ETact = new TimeSeries(tsc1ETact);
			
			value[0] = values[4];
			TimeSeries tsc1a1 = new TimeSeries(time,value);
			id = "a11";
			tsc1a1.setId(id);
			this.items.put(id,tsc1a1);
			@SuppressWarnings("unused")
			IExchangeItem c1a1 = new TimeSeries(tsc1a1);
			
			value[0] = values[5];
			TimeSeries tsc1a2 = new TimeSeries(time,value);
			id = "a21";
			tsc1a2.setId(id);
			this.items.put(id,tsc1a2);
			@SuppressWarnings("unused")
			IExchangeItem c1a2 = new TimeSeries(tsc1a2);
			
			value[0] = values[6];
			TimeSeries tsc1a3 = new TimeSeries(time,value);
			id = "a31";
			tsc1a3.setId(id);
			this.items.put(id,tsc1a3);
			@SuppressWarnings("unused")
			IExchangeItem c1a3 = new TimeSeries(tsc1a3);

			value[0] = values[7];
			TimeSeries tsc1Smax = new TimeSeries(time,value);
			id = "Smax1";
			tsc1Smax.setId(id);
			this.items.put(id,tsc1Smax);
			@SuppressWarnings("unused")
			IExchangeItem c1Smax = new TimeSeries(tsc1Smax);
			
			value[0] = values[8];
			TimeSeries tsc2Q = new TimeSeries(time,value);
			id = "Q2";
			tsc2Q.setId(id);
			this.items.put(id,tsc2Q);
			@SuppressWarnings("unused")
			IExchangeItem c2Q = new TimeSeries(tsc2Q);
			
			value[0] = values[9];
			TimeSeries tsc2S = new TimeSeries(time,value);
			id = "S2";
			tsc2S.setId(id);
			this.items.put(id,tsc2S);
			@SuppressWarnings("unused")
			IExchangeItem c2S = new TimeSeries(tsc2S);
			
			value[0] = values[10];
			TimeSeries tsc2G = new TimeSeries(time,value);
			id = "G2";
			tsc2G.setId(id);
			this.items.put(id,tsc2G);
			@SuppressWarnings("unused")
			IExchangeItem c2G = new TimeSeries(tsc2G);
			
			value[0] = values[11];
			TimeSeries tsc2ETact = new TimeSeries(time,value);
			id = "ETact2";
			tsc2ETact.setId(id);
			this.items.put(id,tsc2ETact);
			@SuppressWarnings("unused")
			IExchangeItem c2ETact = new TimeSeries(tsc2ETact);
			
			value[0] = values[12];
			TimeSeries tsc2a1 = new TimeSeries(time,value);
			id = "a12";
			tsc2a1.setId(id);
			this.items.put(id,tsc2a1);
			@SuppressWarnings("unused")
			IExchangeItem c2a1 = new TimeSeries(tsc2a1);
			
			value[0] = values[13];
			TimeSeries tsc2a2 = new TimeSeries(time,value);
			id = "a22";
			tsc2a2.setId(id);
			this.items.put(id,tsc2a2);
			@SuppressWarnings("unused")
			IExchangeItem c2a2 = new TimeSeries(tsc2a2);
			
			value[0] = values[14];
			TimeSeries tsc2a3 = new TimeSeries(time,value);
			id = "a32";
			tsc2a3.setId(id);
			this.items.put(id,tsc2a3);
			@SuppressWarnings("unused")
			IExchangeItem c2a3 = new TimeSeries(tsc2a3);

			value[0] = values[15];
			TimeSeries tsc2Smax = new TimeSeries(time,value);
			id = "Smax2";
			tsc2Smax.setId(id);
			this.items.put(id,tsc2Smax);
			@SuppressWarnings("unused")
			IExchangeItem c2Smax = new TimeSeries(tsc2Smax);
			
			// 3rd subcatchment.
			value[0] = values[16];
			TimeSeries tsc3Q = new TimeSeries(time,value);
			id = "Q3";
			tsc3Q.setId(id);
			this.items.put(id,tsc3Q);
			@SuppressWarnings("unused")
			IExchangeItem c3Q = new TimeSeries(tsc3Q);
			
			value[0] = values[17];
			TimeSeries tsc3S = new TimeSeries(time,value);
			id = "S3";
			tsc3S.setId(id);
			this.items.put(id,tsc3S);
			@SuppressWarnings("unused")
			IExchangeItem c3S = new TimeSeries(tsc3S);
			
			value[0] = values[18];
			TimeSeries tsc3G = new TimeSeries(time,value);
			id = "G3";
			tsc3G.setId(id);
			this.items.put(id,tsc3G);
			@SuppressWarnings("unused")
			IExchangeItem c3G = new TimeSeries(tsc3G);
			
			value[0] = values[19];
			TimeSeries tsc3ETact = new TimeSeries(time,value);
			id = "ETact3";
			tsc3ETact.setId(id);
			this.items.put(id,tsc3ETact);
			@SuppressWarnings("unused")
			IExchangeItem c3ETact = new TimeSeries(tsc3ETact);
			
			value[0] = values[20];
			TimeSeries tsc3a1 = new TimeSeries(time,value);
			id = "a13";
			tsc3a1.setId(id);
			this.items.put(id,tsc3a1);
			@SuppressWarnings("unused")
			IExchangeItem c3a1 = new TimeSeries(tsc3a1);
			
			value[0] = values[21];
			TimeSeries tsc3a2 = new TimeSeries(time,value);
			id = "a23";
			tsc3a2.setId(id);
			this.items.put(id,tsc3a2);
			@SuppressWarnings("unused")
			IExchangeItem c3a2 = new TimeSeries(tsc3a2);
			
			value[0] = values[22];
			TimeSeries tsc3a3 = new TimeSeries(time,value);
			id = "a33";
			tsc3a3.setId(id);
			this.items.put(id,tsc3a3);
			@SuppressWarnings("unused")
			IExchangeItem c3a3 = new TimeSeries(tsc3a3);

			value[0] = values[23];
			TimeSeries tsc3Smax = new TimeSeries(time,value);
			id = "Smax3";
			tsc3Smax.setId(id);
			this.items.put(id,tsc3Smax);
			@SuppressWarnings("unused")
			IExchangeItem c3Smax = new TimeSeries(tsc3Smax);
			
			// 4th subcatchment.
			value[0] = values[24];
			TimeSeries tsc4Q = new TimeSeries(time,value);
			id = "Q4";
			tsc4Q.setId(id);
			this.items.put(id,tsc4Q);
			@SuppressWarnings("unused")
			IExchangeItem c4Q = new TimeSeries(tsc4Q);
			
			value[0] = values[25];
			TimeSeries tsc4S = new TimeSeries(time,value);
			id = "S4";
			tsc4S.setId(id);
			this.items.put(id,tsc4S);
			@SuppressWarnings("unused")
			IExchangeItem c4S = new TimeSeries(tsc4S);
			
			value[0] = values[26];
			TimeSeries tsc4G = new TimeSeries(time,value);
			id = "G4";
			tsc4G.setId(id);
			this.items.put(id,tsc4G);
			@SuppressWarnings("unused")
			IExchangeItem c4G = new TimeSeries(tsc4G);
			
			value[0] = values[27];
			TimeSeries tsc4ETact = new TimeSeries(time,value);
			id = "ETact4";
			tsc4ETact.setId(id);
			this.items.put(id,tsc4ETact);
			@SuppressWarnings("unused")
			IExchangeItem c4ETact = new TimeSeries(tsc4ETact);
			
			value[0] = values[28];
			TimeSeries tsc4a1 = new TimeSeries(time,value);
			id = "a14";
			tsc4a1.setId(id);
			this.items.put(id,tsc4a1);
			@SuppressWarnings("unused")
			IExchangeItem c4a1 = new TimeSeries(tsc4a1);
			
			value[0] = values[29];
			TimeSeries tsc4a2 = new TimeSeries(time,value);
			id = "a24";
			tsc4a2.setId(id);
			this.items.put(id,tsc4a2);
			@SuppressWarnings("unused")
			IExchangeItem c4a2 = new TimeSeries(tsc4a2);
			
			value[0] = values[30];
			TimeSeries tsc4a3 = new TimeSeries(time,value);
			id = "a34";
			tsc4a3.setId(id);
			this.items.put(id,tsc4a3);
			@SuppressWarnings("unused")
			IExchangeItem c4a3 = new TimeSeries(tsc4a3);

			value[0] = values[34];
			TimeSeries tsc4Smax = new TimeSeries(time,value);
			id = "Smax4";
			tsc4Smax.setId(id);
			this.items.put(id,tsc4Smax);
			@SuppressWarnings("unused")
			IExchangeItem c4Smax = new TimeSeries(tsc4Smax);
			
			// 5th subcatchment.
			value[0] = values[32];
			TimeSeries tsc5Q = new TimeSeries(time,value);
			id = "Q5";
			tsc5Q.setId(id);
			this.items.put(id,tsc5Q);
			@SuppressWarnings("unused")
			IExchangeItem c5Q = new TimeSeries(tsc5Q);
			
			value[0] = values[33];
			TimeSeries tsc5S = new TimeSeries(time,value);
			id = "S5";
			tsc5S.setId(id);
			this.items.put(id,tsc5S);
			@SuppressWarnings("unused")
			IExchangeItem c5S = new TimeSeries(tsc5S);
			
			value[0] = values[34];
			TimeSeries tsc5G = new TimeSeries(time,value);
			id = "G5";
			tsc5G.setId(id);
			this.items.put(id,tsc5G);
			@SuppressWarnings("unused")
			IExchangeItem c5G = new TimeSeries(tsc5G);
			
			value[0] = values[35];
			TimeSeries tsc5ETact = new TimeSeries(time,value);
			id = "ETact5";
			tsc5ETact.setId(id);
			this.items.put(id,tsc5ETact);
			@SuppressWarnings("unused")
			IExchangeItem c5ETact = new TimeSeries(tsc5ETact);
			
			value[0] = values[36];
			TimeSeries tsc5a1 = new TimeSeries(time,value);
			id = "a15";
			tsc5a1.setId(id);
			this.items.put(id,tsc5a1);
			@SuppressWarnings("unused")
			IExchangeItem c5a1 = new TimeSeries(tsc5a1);
			
			value[0] = values[37];
			TimeSeries tsc5a2 = new TimeSeries(time,value);
			id = "a25";
			tsc5a2.setId(id);
			this.items.put(id,tsc5a2);
			@SuppressWarnings("unused")
			IExchangeItem c5a2 = new TimeSeries(tsc5a2);
			
			value[0] = values[38];
			TimeSeries tsc5a3 = new TimeSeries(time,value);
			id = "a35";
			tsc5a3.setId(id);
			this.items.put(id,tsc5a3);
			@SuppressWarnings("unused")
			IExchangeItem c5a3 = new TimeSeries(tsc5a3);

			value[0] = values[39];
			TimeSeries tsc5Smax = new TimeSeries(time,value);
			id = "Smax5";
			tsc5Smax.setId(id);
			this.items.put(id,tsc5Smax);
			@SuppressWarnings("unused")
			IExchangeItem c5Smax = new TimeSeries(tsc5Smax);
			
			// 6th subcatchment
			value[0] = values[40];
			TimeSeries tsc6Q = new TimeSeries(time,value);
			id = "Q6";
			tsc6Q.setId(id);
			this.items.put(id,tsc6Q);
			@SuppressWarnings("unused")
			IExchangeItem c6Q = new TimeSeries(tsc6Q);
			
			value[0] = values[46];
			TimeSeries tsc6S = new TimeSeries(time,value);
			id = "S6";
			tsc6S.setId(id);
			this.items.put(id,tsc6S);
			@SuppressWarnings("unused")
			IExchangeItem c6S = new TimeSeries(tsc6S);
			
			value[0] = values[42];
			TimeSeries tsc6G = new TimeSeries(time,value);
			id = "G6";
			tsc6G.setId(id);
			this.items.put(id,tsc6G);
			@SuppressWarnings("unused")
			IExchangeItem c6G = new TimeSeries(tsc6G);
			
			value[0] = values[43];
			TimeSeries tsc6ETact = new TimeSeries(time,value);
			id = "ETact6";
			tsc6ETact.setId(id);
			this.items.put(id,tsc6ETact);
			@SuppressWarnings("unused")
			IExchangeItem c6ETact = new TimeSeries(tsc6ETact);
			
			value[0] = values[44];
			TimeSeries tsc6a1 = new TimeSeries(time,value);
			id = "a16";
			tsc6a1.setId(id);
			this.items.put(id,tsc6a1);
			@SuppressWarnings("unused")
			IExchangeItem c6a1 = new TimeSeries(tsc6a1);
			
			value[0] = values[45];
			TimeSeries tsc6a2 = new TimeSeries(time,value);
			id = "a26";
			tsc6a2.setId(id);
			this.items.put(id,tsc6a2);
			@SuppressWarnings("unused")
			IExchangeItem c6a2 = new TimeSeries(tsc6a2);
			
			value[0] = values[46];
			TimeSeries tsc6a3 = new TimeSeries(time,value);
			id = "a36";
			tsc6a3.setId(id);
			this.items.put(id,tsc6a3);
			@SuppressWarnings("unused")
			IExchangeItem c6a3 = new TimeSeries(tsc6a3);

			value[0] = values[47];
			TimeSeries tsc6Smax = new TimeSeries(time,value);
			id = "Smax6";
			tsc6Smax.setId(id);
			this.items.put(id,tsc6Smax);
			@SuppressWarnings("unused")
			IExchangeItem c6Smax = new TimeSeries(tsc6Smax);
			
			// 7th subcatchment
						value[0] = values[48];
						TimeSeries tsc7Q = new TimeSeries(time,value);
						id = "Q7";
						tsc7Q.setId(id);
						this.items.put(id,tsc7Q);
						@SuppressWarnings("unused")
						IExchangeItem c7Q = new TimeSeries(tsc7Q);
						
						value[0] = values[49];
						TimeSeries tsc7S = new TimeSeries(time,value);
						id = "S7";
						tsc7S.setId(id);
						this.items.put(id,tsc7S);
						@SuppressWarnings("unused")
						IExchangeItem c7S = new TimeSeries(tsc7S);
						
						value[0] = values[50];
						TimeSeries tsc7G = new TimeSeries(time,value);
						id = "G7";
						tsc7G.setId(id);
						this.items.put(id,tsc7G);
						@SuppressWarnings("unused")
						IExchangeItem c7G = new TimeSeries(tsc7G);
						
						value[0] = values[51];
						TimeSeries tsc7ETact = new TimeSeries(time,value);
						id = "ETact7";
						tsc7ETact.setId(id);
						this.items.put(id,tsc7ETact);
						@SuppressWarnings("unused")
						IExchangeItem c7ETact = new TimeSeries(tsc7ETact);
						
						value[0] = values[52];
						TimeSeries tsc7a1 = new TimeSeries(time,value);
						id = "a17";
						tsc7a1.setId(id);
						this.items.put(id,tsc7a1);
						@SuppressWarnings("unused")
						IExchangeItem c7a1 = new TimeSeries(tsc7a1);
						
						value[0] = values[53];
						TimeSeries tsc7a2 = new TimeSeries(time,value);
						id = "a27";
						tsc7a2.setId(id);
						this.items.put(id,tsc7a2);
						@SuppressWarnings("unused")
						IExchangeItem c7a2 = new TimeSeries(tsc7a2);
						
						value[0] = values[54];
						TimeSeries tsc7a3 = new TimeSeries(time,value);
						id = "a37";
						tsc7a3.setId(id);
						this.items.put(id,tsc7a3);
						@SuppressWarnings("unused")
						IExchangeItem c7a3 = new TimeSeries(tsc7a3);

						value[0] = values[55];
						TimeSeries tsc7Smax = new TimeSeries(time,value);
						id = "Smax7";
						tsc7Smax.setId(id);
						this.items.put(id,tsc7Smax);
						@SuppressWarnings("unused")
						IExchangeItem c7Smax = new TimeSeries(tsc7Smax);
						
						// 6th subcatchment
						value[0] = values[56];
						TimeSeries tsc8Q = new TimeSeries(time,value);
						id = "Q8";
						tsc8Q.setId(id);
						this.items.put(id,tsc8Q);
						@SuppressWarnings("unused")
						IExchangeItem c8Q = new TimeSeries(tsc8Q);
						
						value[0] = values[57];
						TimeSeries tsc8S = new TimeSeries(time,value);
						id = "S8";
						tsc8S.setId(id);
						this.items.put(id,tsc8S);
						@SuppressWarnings("unused")
						IExchangeItem c8S = new TimeSeries(tsc8S);
						
						value[0] = values[58];
						TimeSeries tsc8G = new TimeSeries(time,value);
						id = "G8";
						tsc8G.setId(id);
						this.items.put(id,tsc8G);
						@SuppressWarnings("unused")
						IExchangeItem c8G = new TimeSeries(tsc8G);
						
						value[0] = values[59];
						TimeSeries tsc8ETact = new TimeSeries(time,value);
						id = "ETact8";
						tsc8ETact.setId(id);
						this.items.put(id,tsc8ETact);
						@SuppressWarnings("unused")
						IExchangeItem c8ETact = new TimeSeries(tsc8ETact);
						
						value[0] = values[60];
						TimeSeries tsc8a1 = new TimeSeries(time,value);
						id = "a18";
						tsc8a1.setId(id);
						this.items.put(id,tsc8a1);
						@SuppressWarnings("unused")
						IExchangeItem c8a1 = new TimeSeries(tsc8a1);
						
						value[0] = values[61];
						TimeSeries tsc8a2 = new TimeSeries(time,value);
						id = "a28";
						tsc8a2.setId(id);
						this.items.put(id,tsc8a2);
						@SuppressWarnings("unused")
						IExchangeItem c8a2 = new TimeSeries(tsc8a2);
						
						value[0] = values[62];
						TimeSeries tsc8a3 = new TimeSeries(time,value);
						id = "a38";
						tsc8a3.setId(id);
						this.items.put(id,tsc8a3);
						@SuppressWarnings("unused")
						IExchangeItem c8a3 = new TimeSeries(tsc8a3);

						value[0] = values[63];
						TimeSeries tsc8Smax = new TimeSeries(time,value);
						id = "Smax8";
						tsc8Smax.setId(id);
						this.items.put(id,tsc8Smax);
						@SuppressWarnings("unused")
						IExchangeItem c8Smax = new TimeSeries(tsc8Smax);
			
			
		    
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
		// Updates initial states file.
				double[] currentTimeCache = this.items.get("soilMoisture").getTimes();
				double[] soilMoistureCache = this.items.get("soilMoisture").getValuesAsDoubles();
				double[] gwStorageCache = this.items.get("gwStorage").getValuesAsDoubles();
				
				//write to file
				System.out.println("InitialStatesWrapper.finish(): Writing to file: "+this.workingDir+"/"+this.fileName);
				File outputFile = new File(this.workingDir,this.fileName);
				try{
					if(outputFile.isFile()){
						outputFile.delete();
					}
				}catch (Exception e) {
					System.out.println("InitialStatesWrapper.finish(): trouble removing file "+ fileName);
				}
				try {
					FileWriter writer = new FileWriter(outputFile);
					BufferedWriter out = new BufferedWriter(writer);

					/**
					 * Write initial states values with noise from noise model propagated to the next time step.
					 */
					out.write("% currentTime = " + currentTimeCache[0] + ", finalTime = " + currentTimeCache[1] + System.getProperty("line.separator"));
					out.write("soilMoisture = " + soilMoistureCache[1] + "; " + System.getProperty("line.separator"));
					out.write("gwStorage = " + gwStorageCache[1] + "; " + System.getProperty("line.separator"));
					
					out.flush(); // Force java to write buffered stream to disk.
					out.close();
					writer.close();

				} catch (Exception e) {
					throw new RuntimeException("InitialStatesWrapper.finish(): Problem writing to file "+fileName+" : " + System.getProperty("line.separator") + e.getMessage());
				}
	}

	

}
