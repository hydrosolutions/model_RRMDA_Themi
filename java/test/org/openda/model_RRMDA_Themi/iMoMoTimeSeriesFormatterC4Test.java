package org.openda.model_RRMDA_Themi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.openda.model_RRMDA_Themi.iMoMoTimeSeriesFormatterC1;
import org.openda.exchange.timeseries.TimeSeries;
import org.openda.exchange.timeseries.TimeSeriesFormatter;
import org.openda.utils.OpenDaTestSupport;

import junit.framework.TestCase;

public class iMoMoTimeSeriesFormatterC4Test extends TestCase {

	// Use openDA test suite.
	private File testRunDataDir;
    private OpenDaTestSupport testData;
       

    // Methods.
    protected void setUp() throws Exception {
			
    	// Set up oda test environment. 
		testData = new OpenDaTestSupport(EwrapperTest.class, "model_RRMDA_Themi");
		testRunDataDir = testData.getTestRunDataDir();
		
    }

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testRead_noDischargeData() throws FileNotFoundException {
		
		TimeSeriesFormatter iMoMoFormatter = new iMoMoTimeSeriesFormatterC1();
		File iMoMoDataFile = new File(testRunDataDir, "736239DLoad_noDischargeData.csv");
		
		if (iMoMoDataFile.exists()) {
			FileInputStream in = new FileInputStream(iMoMoDataFile.getAbsolutePath());
			TimeSeries dischargeSubcatchment1 = iMoMoFormatter.read(in);
			String location = dischargeSubcatchment1.getLocation();
			assertEquals("",location);
			iMoMoFormatter.writeToStandardOut(dischargeSubcatchment1);
		}
	}
	
	public void testRead_multipleDischargeData() throws FileNotFoundException {
		
		TimeSeriesFormatter iMoMoFormatter = new iMoMoTimeSeriesFormatterC1();
		File iMoMoDataFile = new File(testRunDataDir, "736239DLoad.csv");
		
		if (iMoMoDataFile.exists()) {
			FileInputStream in = new FileInputStream(iMoMoDataFile.getAbsolutePath());
			TimeSeries dischargeSubcatchment1 = iMoMoFormatter.read(in);
			String location = dischargeSubcatchment1.getLocation();
			assertEquals("subcatchment_4",location);
			double times[] = dischargeSubcatchment1.getTimesRef();
			assertEquals("times[0]", 57296.6, times[0], 0.5);
			double values[] = dischargeSubcatchment1.getValuesAsDoubles();
			assertEquals("values[0]", 0.3, values[0], 0.01);
		}
	}

}
