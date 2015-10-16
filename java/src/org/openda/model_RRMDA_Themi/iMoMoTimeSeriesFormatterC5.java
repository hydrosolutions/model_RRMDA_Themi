package org.openda.model_RRMDA_Themi;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.openda.exchange.timeseries.TimeSeries;
import org.openda.exchange.timeseries.TimeSeriesFormatter;
import org.openda.exchange.timeseries.TimeUtils;

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
 * This class can be used to read in the data for sub-catchment 1.
 * <p>
 * @author Beatrice Marti, hydrosolutions ltd., marti@hydrosolutions.ch
 * <p>
 * copyright hydrosolutions ltd. 2015 <br>
 * license   see LICENSE
 *
 */
public class iMoMoTimeSeriesFormatterC5 extends TimeSeriesFormatter {

	// Local class iMoMoData to store the relevant stuff from iMoMo.csv.
	private class iMoMoData{
		
		public int variable_id;
		public int site_id;
		public double data_value;
		public String date;
		
		iMoMoData() {
			this.variable_id = 0;
			this.site_id = 0;
			this.data_value = 0.0;
			this.date = "";
		}
		
		iMoMoData(int varialbeID, int siteID, double dataValue, String date){
			this.variable_id = varialbeID;
			this.site_id = siteID;
			this.data_value = dataValue;
			this.date = date;
		}
		
		public int getVariableID() {
			return this.variable_id;
		}
		
		public int getSiteID() {
			return this.site_id;
		}
		
		public double getValue() {
			return this.data_value;
		}
		
		/**
		 * The date format of iMoMoData.date is 
		 * yyyy-MM-dd HH:MM:SS,S
		 * This needs to be transformed to the oda date string
		 * yyyyMMddHHMM
		 * 
		 * @return double newDate
		 */
		public String iMoMoDate2odaDateString() {
			
			// Take year, month, and day 
			String[] parts = this.date.split("-"); // yyyy, MM, dd HH:mm:SS.S
			String modifiedDate = parts[0]+parts[1]; // yyyy + MM
			
			// Take hours and minutes.
			String[] moreParts = parts[2].split(" "); // dd, HH:mm:SS.S
			modifiedDate = modifiedDate+moreParts[0]; // dd
			String[] evenMoreParts = moreParts[1].split(":"); // HH, mm, SS.S
			modifiedDate = modifiedDate + evenMoreParts[0] + evenMoreParts[1]; // HH + mm
			String[] finalParts = evenMoreParts[2].split("\\."); // SS, S
			modifiedDate = modifiedDate + finalParts[0]; // SS
			//System.out.println("modifiedDate : " + modifiedDate);
			
			return modifiedDate;
		}
		
	}
	
	
	// Global fields. 
	List<String> fileContent= new ArrayList<String>();
	
	// Public methods.
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
		List<iMoMoData> dischargeDataList = new ArrayList<iMoMoData>();
		List<iMoMoData> subcatchmentDataList = new ArrayList<iMoMoData>();
		
		TimeSeries result = new TimeSeries(); // Initialize TimeSeries for return.
		double[] value = new double[1];
		double[] time = new double[1];
		
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
			if (data.getVariableID() == 25) {
				// Put them to a new list.
				dischargeDataList.add(data);
			}			
		}
		
		// Only continue if there is discharge data.
		if (dischargeDataList.isEmpty() == false) {
			for (int i=0; i<dischargeDataList.size(); i++ ) {
				iMoMoData data = dischargeDataList.get(i);
				if (data.getSiteID() == 5) {
					subcatchmentDataList.add(data);
				}
			}
			// If there is data for the sub-catchment.
			if (subcatchmentDataList.isEmpty() == false) {
				// Check if there is more than one entry.
				if (subcatchmentDataList.size() > 1) {
					// Sort through the date and take the last one.
					double timeTemp = 0.0;
					double temp = 0.0;
					double valueTemp = 0.0;
					iMoMoData d = new iMoMoData();
					for (int i=0; i<subcatchmentDataList.size(); i++) {
						d = subcatchmentDataList.get(i);
						try {
							temp = TimeUtils.date2Mjd(d.iMoMoDate2odaDateString());
						} catch (ParseException e) {
							e.printStackTrace();
						}
						if (temp > timeTemp) {
							timeTemp = temp;
							valueTemp = d.getValue();
						}
					}
					time[0] = timeTemp;
					value[0] = valueTemp;
					
				} else {
					// Take the value and add it to the time series.
					iMoMoData d = subcatchmentDataList.get(0);
					value[0] = d.getValue();
					try {
						time[0] = Double.valueOf(d.iMoMoDate2odaDateString());
					} catch (Exception e) {
						throw new RuntimeException("Problem reading time: time[0] = " + time[0]);
					}
				}
				result.setData(time, value);
				result.setLocation("subcatchment_5");
			    result.setQuantity("mm");
			    result.setSource("measured");
			}
		} else {
			return result;
		}
		
		return result;

	}
	
}

