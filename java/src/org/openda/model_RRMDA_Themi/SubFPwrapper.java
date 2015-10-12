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
 * The file sub_FP.mat stores forecast data for 8 sub-catchments in the Themi basin
 * for a forecast period of 5 days. The time steps are stored in mjd.
 * 
 * This is an optional wrapper if stochastic boundary conditions are to be used in 
 * the model. This wrapper is under construction. 
 * 
 * @author Beatrice Marti, hydrosolutions ltd., marti@hydrosolutions.ch
 *
 * Copyright (c) 2015, hydrosolutions ltd.
 * 
 */
public class SubFPwrapper implements IoObjectInterface {

	@Override
	public void initialize(File workingDir, String fileName, String[] arguments) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IPrevExchangeItem[] getExchangeItems() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub
		
	}

	
	
}
