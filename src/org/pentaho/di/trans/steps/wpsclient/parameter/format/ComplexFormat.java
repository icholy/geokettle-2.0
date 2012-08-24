package org.pentaho.di.trans.steps.wpsclient.parameter.format;

public class ComplexFormat extends AbstractFormat{
	public static final String MIMETYPE = "Mime Type";
	public static final String SCHEMA = "Schema";
	public static final String ENCODING = "Encoding";
	
	public ComplexFormat(String mimeType, String encoding, String schema){
		super(3);
		properties.put(MIMETYPE, mimeType);
		properties.put(ENCODING, encoding);	
		properties.put(SCHEMA, schema);		
	}
}
