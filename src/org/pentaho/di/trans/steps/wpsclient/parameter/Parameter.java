package org.pentaho.di.trans.steps.wpsclient.parameter;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.trans.steps.wpsclient.parameter.format.AbstractFormat;

public class Parameter implements Cloneable{
	private List<AbstractFormat> supportedFormats;	
	private AbstractFormat defaultFormat;	
	
	private boolean isMandatory;
	private boolean hasLiteralFormat;
	private boolean hasBoundingBoxFormat;
	
	private String id;
	private String _abstract;
	private String value;
	private String fieldName;
	
	public Parameter(boolean isMandatory, String id, String abs){
		this.isMandatory = isMandatory;
		this.id = id;
		this._abstract = abs;
		supportedFormats = new ArrayList<AbstractFormat>();
		this.hasLiteralFormat = false;
		this.hasBoundingBoxFormat = false;
		value = "";
		fieldName = "";
	}
	
    @Override
    public Object clone() {     
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {            
            return null;
        }
    }
	
	public String getValue(){
		return value;
	}
	
	public String getFieldName(){
		return fieldName;
	}
	
	public void setValue(String value){
		this.value = value;
	}
	
	public void setFieldName(String fieldName){
		this.fieldName = fieldName;
	}
	
	public String getAbstract(){
		return _abstract;
	}
	
	public String getId(){
		return id;
	}
	
	public boolean isMandatory(){
		return isMandatory;
	}
	
	public boolean hasLiteralFormat(){
		return hasLiteralFormat;
	}
	
	public boolean hasBoundingBoxFormat(){
		return hasBoundingBoxFormat;
	}
	
	public void setHasLiteralFormat(boolean hasLiteralFormat){
		this.hasLiteralFormat = hasLiteralFormat;
	}
	
	public void setHasBoundingBoxFormat(boolean hasBoundingBoxFormat){
		this.hasBoundingBoxFormat = hasBoundingBoxFormat;
	}
	
	public List<AbstractFormat> getSupportedFormats() {
		return supportedFormats;
	}
	
	public void addSupportedFormat(AbstractFormat supportedFormat) {
		this.supportedFormats.add(supportedFormat);
	}
	
	public AbstractFormat getDefaultFormat() {
		return defaultFormat;
	}
	
	public void setDefaultFormat(AbstractFormat defaultFormat) {
		this.defaultFormat = defaultFormat;
	}
}
