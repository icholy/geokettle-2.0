package org.pentaho.di.trans.steps.wpsclient.parameter.format;

import java.util.List;

public class BoundingBoxFormat extends AbstractFormat{
	public static final String DEFAULT_CRS = "Default_CRS";
	public static final String SUPPORTED_CRSS = "Supported_CRSs";
	
	public BoundingBoxFormat(List<String> supCRSs, String defCRS) {	
		super(2);
		properties.put(SUPPORTED_CRSS, supCRSs);
		properties.put(DEFAULT_CRS, defCRS);
	}
}