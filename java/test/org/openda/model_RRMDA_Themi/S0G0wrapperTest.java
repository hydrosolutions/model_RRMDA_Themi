package org.openda.model_RRMDA_Themi;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.openda.blackbox.interfaces.IoObjectInterface;
import org.openda.interfaces.IPrevExchangeItem;
import org.openda.utils.OpenDaTestSupport;

import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;

import junit.framework.TestCase;

public class S0G0wrapperTest extends TestCase {

	// Use openDA test suite.
	private File testRunDataDir;
	private OpenDaTestSupport testData;

	private String fileName = "testS0G0.mat";
	private String[] args = {};
	    
	
	public S0G0wrapperTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		// Set up oda test environment. 
		testData = new OpenDaTestSupport(S0G0wrapperTest.class, "model_RRMDA_Themi");
		testRunDataDir = testData.getTestRunDataDir();
			
		// Write .mat file for testing.
		int numberOfStates = 8;
		int numberOfReplicates = 100;
		double[] s0 = new double[numberOfStates];
		double[] g0 = new double[numberOfStates];
		double[] sg = new double[numberOfStates*2 * numberOfReplicates]; 
		
		for (int i=0; i<numberOfStates; i++) {
			s0[i] = i;
			g0[i] = i+100;
		}
		
		for (int i=0; i<numberOfReplicates; i++) {
			for (int j=0; j<numberOfStates*2; j++) {
				sg[i*numberOfStates*2+j] = j+i*0.01;
			}
		}
		
		MLDouble mlDoubleS0 = new MLDouble( "S0", s0, 1);
		MLDouble mlDoubleG0 = new MLDouble( "G0", g0, 1);
		MLDouble mlDoubleSG = new MLDouble( "SG", sg, numberOfStates*2 ); // Store test in 64 rows.
		ArrayList<MLArray> list = new ArrayList<MLArray>();
		list.add( mlDoubleS0 );
		list.add( mlDoubleG0 );
		list.add( mlDoubleSG );
		        
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
				
		int numberOfStates = 8;
		double[] expectedS0 = new double[numberOfStates];
		double[] expectedG0 = new double[numberOfStates];
		
		for (int i=0; i<numberOfStates; i++) {
			expectedS0[i] = i;
			expectedG0[i] = i+100;
		}
		
		
		// Create wrapper object.
		IoObjectInterface wrapper = new S0G0wrapper();
		// Read file.
		wrapper.initialize(testRunDataDir, fileName, args);
		// Get exchange items.
		IPrevExchangeItem[] exchangeItems = wrapper.getExchangeItems();
				
		assertEquals(numberOfStates*2,exchangeItems.length);
				
		// Test what is read.
		for (int item = 0; item < exchangeItems.length; item++) {
			if (exchangeItems[item].getId().equalsIgnoreCase("S0"+(item+1))) {
				IPrevExchangeItem readExchangeItem = exchangeItems[item];
						
				// Name of exchange item.
				String readId = readExchangeItem.getId();
				assertEquals("S0"+(item+1), readId);
						
				//System.out.println("Testing " + readId + ".");
						
				// Times and values.
				double[] readTimes = readExchangeItem.getTimes();
				double[] readValues = readExchangeItem.getValuesAsDoubles();
				assertEquals(expectedTime.length,readTimes.length);
				assertEquals(expectedTime[0],readTimes[0]);
						
				// Item is the index of the id.
				assertEquals(readId +": ExpectedS0["+item+"] = " + expectedS0[item] +",readValues[0] = " + readValues[0] + ".",
								     expectedS0[item],readValues[0]);
			}
			
			if (exchangeItems[item].getId().equalsIgnoreCase("G0"+(item+1))) {
				IPrevExchangeItem readExchangeItem = exchangeItems[item];
				
				// Name of exchange item.
				String readId = readExchangeItem.getId();
				assertEquals("G0"+(item+1), readId);
						
				//System.out.println("Testing " + readId + ".");
						
				// Times and values.
				double[] readTimes = readExchangeItem.getTimes();
				double[] readValues = readExchangeItem.getValuesAsDoubles();
				assertEquals(expectedTime.length,readTimes.length);
				assertEquals(expectedTime[0],readTimes[0]);
						
				// Item is the index of the id.
				assertEquals(readId +": ExpectedG0["+item+"] = " + expectedG0[item] +",readValues[0] = " + readValues[0] + ".",
								     expectedG0[item],readValues[0]);
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
				
		int numberOfStates = 8;
		double[] expectedS0 = new double[numberOfStates];
		double[] expectedG0 = new double[numberOfStates];
		
		for (int i=0; i<numberOfStates; i++) {
			expectedS0[i] = i;
			expectedG0[i] = i+100;
		}
				
		// Ewrapper magic.
		IoObjectInterface wrapper = new S0G0wrapper();
		wrapper.initialize(testRunDataDir, fileName, args);
		//IPrevExchangeItem[] exchangeItems = ewrapper.getExchangeItems();
		wrapper.finish();
				
		// Reading file again.
		// Create wrapper object.
		IoObjectInterface readingWrapper = new S0G0wrapper();
		// Read file.
		readingWrapper.initialize(testRunDataDir, fileName, args);
		// Get exchange items.
		IPrevExchangeItem[] exchangeItems = readingWrapper.getExchangeItems();
						
		assertEquals(numberOfStates*2,exchangeItems.length);
						
		// Test what is read.
		for (int item = 0; item < exchangeItems.length; item++) {
			if (exchangeItems[item].getId().equalsIgnoreCase("S0"+(item+1))) {
				IPrevExchangeItem readExchangeItem = exchangeItems[item];
						
				// Name of exchange item.
				String readId = readExchangeItem.getId();
				assertEquals("S0"+(item+1), readId);
						
				//System.out.println("Testing " + readId + ".");
						
				// Times and values.
				double[] readTimes = readExchangeItem.getTimes();
				double[] readValues = readExchangeItem.getValuesAsDoubles();
				assertEquals(expectedTime.length,readTimes.length);
				assertEquals(expectedTime[0],readTimes[0]);
						
				// Item is the index of the id.
				assertEquals(readId +": ExpectedS0["+item+"] = " + expectedS0[item] +",readValues[0] = " + readValues[0] + ".",
								     expectedS0[item],readValues[0]);
			}
			
			if (exchangeItems[item].getId().equalsIgnoreCase("G0"+(item+1))) {
				IPrevExchangeItem readExchangeItem = exchangeItems[item];
				
				// Name of exchange item.
				String readId = readExchangeItem.getId();
				assertEquals("G0"+(item+1), readId);
						
				//System.out.println("Testing " + readId + ".");
						
				// Times and values.
				double[] readTimes = readExchangeItem.getTimes();
				double[] readValues = readExchangeItem.getValuesAsDoubles();
				assertEquals(expectedTime.length,readTimes.length);
				assertEquals(expectedTime[0],readTimes[0]);
						
				// Item is the index of the id.
				assertEquals(readId +": ExpectedG0["+item+"] = " + expectedG0[item] +",readValues[0] = " + readValues[0] + ".",
								     expectedG0[item],readValues[0]);
			}
		}
	}

}
