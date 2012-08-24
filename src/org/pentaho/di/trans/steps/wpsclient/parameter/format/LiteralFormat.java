package org.pentaho.di.trans.steps.wpsclient.parameter.format;

import java.util.List;

public class LiteralFormat extends AbstractFormat{
	public static final String DATATYPE = "Datatype";
	public static final String SUPPORTED_UOMS = "Supported_UOMs";
	public static final String DEFAULT_UOM = "Default_UOM";
	public static final String DEFAULT_VALUE = "Default_Value";
	
	public LiteralFormat(String datatype, List<String> supUOMs, String defUOM, String defValue) {	
		super(4);
		properties.put(DATATYPE, datatype);	
		properties.put(SUPPORTED_UOMS, supUOMs);
		properties.put(DEFAULT_UOM, defUOM);
		properties.put(DEFAULT_VALUE, defValue);
	}
}
