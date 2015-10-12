package org.openda.model_RRMDA_Themi;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.openda.blackbox.interfaces.IoObjectInterface;
import org.openda.interfaces.IPrevExchangeItem;
import org.openda.model_RRMDA_Themi.Ewrapper;
import org.openda.utils.OpenDaTestSupport;

import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;

import junit.framework.TestCase;

public class EwrapperTest extends TestCase {

	// Use openDA test suite.
	private File testRunDataDir;
    private OpenDaTestSupport testData;

    private String fileName = "testE.mat";
    private String[] args = {};
    

    // Methods.
    protected void setUp() throws Exception {
		
    	// Set up oda test environment. 
		testData = new OpenDaTestSupport(EwrapperTest.class, "model_RRMDA_Themi");
		testRunDataDir = testData.getTestRunDataDir();
		
		// Write .mat file for testing.
		int numberOfStates = 64;
		int numberOfReplicates = 100;
		double[] E = new double[numberOfStates * numberOfReplicates]; 
		for (int i=0; i<numberOfReplicates; i++) {
			for (int j=0; j<numberOfStates; j++) {
				E[i*numberOfStates+j] = j+i*0.01;
			}
		}
		MLDouble mlDouble = new MLDouble( "E", E, 64 ); // Store test in 64 rows.
        ArrayList<MLArray> list = new ArrayList<MLArray>();
        list.add( mlDouble );
        
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
		
		// Expected values.
		File file = new File(testRunDataDir, fileName);
		double refdate = 0;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		try {
			refdate = org.openda.exchange.timeseries.TimeUtils.date2Mjd(sdf.format(file.lastModified()),"yyyyMMdd");
		} catch (ParseException e) {
			e.printStackTrace();
		}
		double[] expectedTime = new double[] {refdate};
		
		int numberOfStates = 64;
		int numberOfReplicates = 100;
		double[] expectedE = new double[numberOfStates * numberOfReplicates]; 
		for (int i=0; i<numberOfReplicates; i++) {
			for (int j=0; j<numberOfStates; j++) {
				expectedE[i*numberOfStates+j] = j+i*0.01;
			}
		}
		
		String[] expectedIDs = new String[] {"Q1","S1","G1","ETact1","a11","a21","a31","Smax1",
				                             "Q2","S2","G2","ETact2","a12","a22","a32","Smax2",
				                             "Q3","S3","G3","ETact3","a13","a23","a33","Smax3",
				                             "Q4","S4","G4","ETact4","a14","a24","a34","Smax4",
				                             "Q5","S5","G5","ETact5","a15","a25","a35","Smax5",
				                             "Q6","S6","G6","ETact6","a16","a26","a36","Smax6",
				                             "Q7","S7","G7","ETact7","a17","a27","a37","Smax7",
				                             "Q8","S8","G8","ETact8","a18","a28","a38","Smax8"};

		// Create wrapper object.
		IoObjectInterface ewrapper = new Ewrapper();
		// Read file.
		ewrapper.initialize(testRunDataDir, fileName, args);
		// Get exchange items.
		IPrevExchangeItem[] exchangeItems = ewrapper.getExchangeItems();
		
		assertEquals(numberOfStates,exchangeItems.length);
		
		// Test what is read.
		for (int item = 0; item < exchangeItems.length; item++) {
			if (exchangeItems[item].getId().equalsIgnoreCase(expectedIDs[item])) {
				IPrevExchangeItem readExchangeItem = exchangeItems[item];
				
				// Name of exchange item.
				String readId = readExchangeItem.getId();
				assertEquals(expectedIDs[item], readId);
				
				//System.out.println("Testing " + readId + ".");
				
				// Times and values.
				double[] readTimes = readExchangeItem.getTimes();
				double[] readValues = readExchangeItem.getValuesAsDoubles();
				assertEquals(expectedTime.length,readTimes.length);
				assertEquals(expectedTime[0],readTimes[0]);
				
				// Item is the index of the id.
				assertEquals(readId +": ExpectedE[" + item + "] = " + expectedE[item] +",readValues[0] = " + readValues[0] + ".",
						     expectedE[item],readValues[0]);
			}
		}
		
		
		
	}

	public void testFinish() {
		
		// Setup test.
		File file = new File(testRunDataDir, fileName);
		double refdate = 0;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		try {
			refdate = org.openda.exchange.timeseries.TimeUtils.date2Mjd(sdf.format(file.lastModified()),"yyyyMMdd");
		} catch (ParseException e) {
			e.printStackTrace();
		}
		double[] expectedTime = new double[] {refdate};
		
		int numberOfStates = 64;
		int numberOfReplicates = 100;
		double[] expectedE = new double[numberOfStates * numberOfReplicates]; 
		for (int i=0; i<numberOfReplicates; i++) {
			for (int j=0; j<numberOfStates; j++) {
				expectedE[i*numberOfStates+j] = j+i*0.01;
			}
		}
		
		String[] expectedIDs = new String[] {"Q1","S1","G1","ETact1","a11","a21","a31","Smax1",
				                             "Q2","S2","G2","ETact2","a12","a22","a32","Smax2",
				                             "Q3","S3","G3","ETact3","a13","a23","a33","Smax3",
				                             "Q4","S4","G4","ETact4","a14","a24","a34","Smax4",
				                             "Q5","S5","G5","ETact5","a15","a25","a35","Smax5",
				                             "Q6","S6","G6","ETact6","a16","a26","a36","Smax6",
				                             "Q7","S7","G7","ETact7","a17","a27","a37","Smax7",
				                             "Q8","S8","G8","ETact8","a18","a28","a38","Smax8"};

		// Ewrapper magic.
		IoObjectInterface ewrapper = new Ewrapper();
		ewrapper.initialize(testRunDataDir, fileName, args);
		//IPrevExchangeItem[] exchangeItems = ewrapper.getExchangeItems();
		ewrapper.finish();
		
		// Reading file again.
		// Create wrapper object.
		IoObjectInterface readingewrapper = new Ewrapper();
		// Read file.
		readingewrapper.initialize(testRunDataDir, fileName, args);
		// Get exchange items.
		IPrevExchangeItem[] exchangeItems = readingewrapper.getExchangeItems();
				
		assertEquals(numberOfStates,exchangeItems.length);
				
		// Test what is read.
		for (int item = 0; item < exchangeItems.length; item++) {
			if (exchangeItems[item].getId().equalsIgnoreCase(expectedIDs[item])) {
				IPrevExchangeItem readExchangeItem = exchangeItems[item];
				
				// Name of exchange item.
				String readId = readExchangeItem.getId();
				assertEquals(expectedIDs[item], readId);
				
				//System.out.println("Testing " + readId + ".");
				
				// Times and values.
				double[] readTimes = readExchangeItem.getTimes();
				double[] readValues = readExchangeItem.getValuesAsDoubles();
				assertEquals(expectedTime.length,readTimes.length);
				assertEquals(expectedTime[0],readTimes[0]);
					
				// Item is the index of the id.
				assertEquals(readId +": ExpectedE[" + item + "] = " + expectedE[item] +",readValues[0] = " + readValues[0] + ".",
						     expectedE[item],readValues[0]);
			}
		}
	}

}
