package org.openda.model_RRMDA_Themi;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.openda.blackbox.interfaces.IoObjectInterface;
import org.openda.interfaces.IPrevExchangeItem;
import org.openda.model_RRMDA_Themi.timeWrapper;
import org.openda.utils.OpenDaTestSupport;

import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLChar;
import com.jmatio.types.MLDouble;

import junit.framework.TestCase;

public class timeWrapperTest extends TestCase {

	// Use openDA test suite.
	private File testRunDataDir;
    private OpenDaTestSupport testData;
    private String fileName = "testTime.mat";
    private String[] args = {};
	    

    // Methods.
    protected void setUp() throws Exception {
			
    	// Set up oda test environment. 
		testData = new OpenDaTestSupport(EwrapperTest.class, "model_RRMDA_Themi");
		testRunDataDir = testData.getTestRunDataDir();
			
		// Write .mat file for testing.
		String a = "201501010101";
		String b = "201501020101";
		double[] c = {1};
		MLChar mla = new MLChar("startTime",a); 
		MLChar mlb = new MLChar("endTime",b);
		MLDouble mlc = new MLDouble("step",c,1);
		ArrayList<MLArray> list = new ArrayList<MLArray>();
        list.add(mla);
        list.add(mlb);
        list.add(mlc);
	        
        //write arrays to file
        try {
        	File file = new File(testRunDataDir, fileName);
        	new MatFileWriter( file, list );
        } catch (IOException e) {
			e.printStackTrace();
		}
    }

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testInitialize() {
			
		// Create wrapper object.
		IoObjectInterface timewrapper = new timeWrapper();
		// Read file.
		timewrapper.initialize(testRunDataDir, fileName, args);
		// Get exchange items.
		IPrevExchangeItem[] exchangeItems = timewrapper.getExchangeItems();
		
		// Test what is read.
		for (int item = 0; item < exchangeItems.length; item++) {
			if (exchangeItems[item].getId().equalsIgnoreCase("startTime")) {
				double[] startTime = exchangeItems[item].getValuesAsDoubles();
				double aa = 0;
				try {
					aa = org.openda.exchange.timeseries.TimeUtils.date2Mjd("201501010101");
				} catch (Exception e) {
					e.printStackTrace();
				}
				assertEquals(aa,startTime[0]);
			}
			if (exchangeItems[item].getId().equalsIgnoreCase("endTime")) {
				double[] endTime = exchangeItems[item].getValuesAsDoubles();
				double bb = 0;
				try {
					bb = org.openda.exchange.timeseries.TimeUtils.date2Mjd("201501020101");
				} catch (Exception e) {
					e.printStackTrace();
				}
				assertEquals(bb,endTime[0]);
			}
			if (exchangeItems[item].getId().equalsIgnoreCase("step")) {
				double[] step = exchangeItems[item].getValuesAsDoubles();
				assertEquals(1.0,step[0]);
			}
		}
	}

	public void testFinish() {
	
		IoObjectInterface w = new timeWrapper();
		w.initialize(testRunDataDir,fileName,args);
		IPrevExchangeItem[] exchangeItems = w.getExchangeItems();
		for (int item = 0; item < exchangeItems.length; item++) {
			if (exchangeItems[item].getId().equalsIgnoreCase("startTime")) {
				double[] startTime = exchangeItems[item].getValuesAsDoubles();
				startTime[0] = startTime[0] + 10;
				exchangeItems[item].setValuesAsDoubles(startTime);
			}
			if (exchangeItems[item].getId().equalsIgnoreCase("endTime")) {
				double[] endTime = exchangeItems[item].getValuesAsDoubles();
				endTime[0] = endTime[0] + 10;
				exchangeItems[item].setValuesAsDoubles(endTime);
			}
			if (exchangeItems[item].getId().equalsIgnoreCase("step")) {
				double[] step = exchangeItems[item].getValuesAsDoubles();
				step[0] = 3.0;
				exchangeItems[item].setValuesAsDoubles(step);
			}
		}
		w.finish();
		
		w.initialize(testRunDataDir, fileName, args);
		exchangeItems = w.getExchangeItems();
		
		// Test what is read.
		for (int item = 0; item < exchangeItems.length; item++) {
			if (exchangeItems[item].getId().equalsIgnoreCase("startTime")) {
				double[] startTime = exchangeItems[item].getValuesAsDoubles();
				double aa = 0;
				try {
					aa = org.openda.exchange.timeseries.TimeUtils.date2Mjd("201501110101");
				} catch (Exception e) {
					e.printStackTrace();
				}
				assertEquals(aa,startTime[0]);
			}
			if (exchangeItems[item].getId().equalsIgnoreCase("endTime")) {
				double[] endTime = exchangeItems[item].getValuesAsDoubles();
				double bb = 0;
				try {
					bb = org.openda.exchange.timeseries.TimeUtils.date2Mjd("201501120101");
				} catch (Exception e) {
					e.printStackTrace();
				}
				assertEquals(bb,endTime[0]);
			}
			if (exchangeItems[item].getId().equalsIgnoreCase("step")) {
				double[] step = exchangeItems[item].getValuesAsDoubles();
				assertEquals(3.0,step[0]);
			}
		}
		
	}
	
	
}











