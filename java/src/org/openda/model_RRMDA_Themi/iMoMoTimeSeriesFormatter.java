package org.openda.model_RRMDA_Themi;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.openda.exchange.timeseries.TimeSeries;
import org.openda.exchange.timeseries.TimeSeriesFormatter;
import org.openda.interfaces.IExchangeItem;

import com.jmatio.io.MatFileReader;
import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLChar;
import com.jmatio.types.MLDouble;
import com.jmatio.types.MLStructure;

/**
 * Formatter that reads and writes time series from iMoMo.csv file. 
 * <p>
 * iMoMo.csv contains the observations collected for the input to the 
 * rainfall-runoff model RRMDA in a table. Each row entry in the 
 * csv file corresponds to one measurement and data pertaining to each
 * measurement is stored in the columns. For example if 3 measurements 
 * have been taken on a given day, iMoMo.csv for that given day has 3
 * entries rows.
 * <p>
 * The columns have the following headers: <br>
 *   dataValue (double), relevant <br>
 *   variableID (int), relevant <br>
 *   dateTimeUTC (string), relevant <br>
 *   latitude (double) <br>
 *   longitude (double) <br>
 *   siteID (int), relevant <br>
 *   userID (int) <br>
 *   siteName (string) <br>
 *   userName (cell) <br>
 * <p>
 * variableID 25 corresponds to discharge data. So only data with 
 * variableID 25 has to be read. The siteID then specifies to which 
 * sub-catchment the discharge measurement belongs. If several data 
 * points have been measured for a given sub-catchment at a given day
 * the last one is used for the data assimilation.
 * <p>
 * @author Beatrice Marti, hydrosolutions ltd., marti@hydrosolutions.ch
 * <p>
 * copyright hydrosolutions ltd. 2015 <br>
 * license   see LICENSE
 *
 */
public class iMoMoTimeSeriesFormatter extends TimeSeriesFormatter {

	// Local class iMoMoData to store the relevant stuff from iMoMo.csv.
	private class iMoMoData{
		
		public int variable_id;
		public int site_id;
		public double data_value;
		public String date;
		
		// method to fill the data.
		iMoMoData(int varialbeID, int siteID, double dataValue, String date){
			this.variable_id = varialbeID;
			this.site_id = siteID;
			this.data_value = dataValue;
			this.date = date;
		}
		
	}
	
	// 
	List<String> fileContent= new ArrayList<String>();
	
	private int getVariableID(iMoMoData imomodata) {
		return imomodata.variable_id;
	}
	
	private int getSiteID(iMoMoData imomodata) {
		return imomodata.site_id;
	}
	
	private double getValue(iMoMoData imomodata) {
		return imomodata.data_value;
	}
	
	private String getDate(iMoMoData imomodata) {
		return imomodata.date;
	}
	
	@Override
	public void write(OutputStream out, TimeSeries series) {
		write(new PrintWriter(out), series);
	}

	public void write(PrintWriter printer, TimeSeries series) {
		
	}
	
	
	@Override
	public TimeSeries read(InputStream in) {
		return read(new BufferedReader(new InputStreamReader(in)));
	}
	
	/**
	 * Read in data from iMoMo.csv
	 * <p>
	 * Read in the entire file and store each row as iMoMoData in a 
	 * hash map.
	 * 
	 * @param buff buffered reader with input stream.
	 * @return TimeSeries containing time and value.
	 */
	public TimeSeries read(BufferedReader buff) {
		
		List<iMoMoData> iMoMoDataList = new ArrayList<iMoMoData>();
		List<iMoMoData> dischargeData = new ArrayList<iMoMoData>();
		
		// Read in the file.
		boolean eof = false;
		String line;
		while (!eof) {
			try {
				line = buff.readLine();
			} catch (Exception e) {
				throw new RuntimeException("Problem reading line from file.");
			}
			if (line == null) {
				eof = true;
			} else { 
				// Add the line to list for later writing. 
				fileContent.add(line);
				String[] columns = line.split(","); // Split comma-separated line.
				int numberOfColumns = columns.length;
				for (int i=0; i<numberOfColumns; i++) {
					columns[i]=columns[i].trim();	// Remove superfluous spaces.				
				}
				// Skip lines that start with a character.
				if (columns[0].matches("^D.+$")) { 
					continue; // Go get the next line.
				} else {
					// Fill in iMoMoData with a unique identifier.
					iMoMoDataList.add(new iMoMoData(Integer.valueOf(columns[1]),Integer.valueOf(columns[5]),Double.valueOf(columns[0]),columns[2]));
				}
			}
		}
		
		// Now browse through the map for discharge measurements.
		
		for (int i=0; i<iMoMoDataList.size(); i++) {
			// Only need data from key starting with 25 which is discharge data. 
			iMoMoData data = iMoMoDataList.get(i);
			if (getVariableID(data) == 25) {
				// Put them to a new list.
				dischargeData.add(data);
			}
			// Only do the next step if the new list is not empty.
			if (dischargeData.isEmpty()==false) {
				// If we have more than one entry for discharge for a sub-catchment 
				// we have to sort through the date and choose the last one. 
				
			}			
		}
		
		TimeSeries result = new TimeSeries();
		return result;

	}

		
	/*
	 //array name
        //file name in which array will be stored
        String fileName = "mlstruct.mat";

        //test column-packed vector
        double[] src = new double[] { 1.3, 2.0, 3.0, 4.0, 5.0, 6.0 };
        
        //create 3x2 double matrix
        //[ 1.0 4.0 ;
        //  2.0 5.0 ;
        //  3.0 6.0 ]
        MLDouble mlDouble = new MLDouble( null, src, 3 );
        MLChar mlChar = new MLChar( null, "I am dummy" );
        
        
        MLStructure mlStruct = new MLStructure("str", new int[] {1,1} );
        mlStruct.setField("f1", mlDouble);
        mlStruct.setField("f2", mlChar);
        
        //write array to file
        ArrayList<MLArray> list = new ArrayList<MLArray>();
        list.add( mlStruct );
        
        //write arrays to file
        new MatFileWriter( fileName, list );
        
        //read array form file
        MatFileReader mfr = new MatFileReader( fileName );
        MLStructure mlArrayRetrived = (MLStructure)mfr.getMLArray( "str" );
        
        assertEquals(mlDouble, mlArrayRetrived.getField("f1") );
        assertEquals(mlChar, mlArrayRetrived.getField("f2") );
        
	 */
	
}
