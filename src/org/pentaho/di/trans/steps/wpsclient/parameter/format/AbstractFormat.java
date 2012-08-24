package org.pentaho.di.trans.steps.wpsclient.parameter.format;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractFormat {
	Map<String, Object> properties;
	
	public AbstractFormat(int size){
		properties = new HashMap<String, Object>(size);
	}
	
	public Map<String, Object> getProperties(){
		return properties;
	}
}
