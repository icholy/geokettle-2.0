package org.pentaho.di.ui.core.dialog.geopreview;

import org.pentaho.di.ui.core.dialog.geopreview.layercontrol.Messages;

/**
 * This class allows the personalisation of colors and opacity (backgound and lines) and of line styles (strokewidth)
 * 
 * @author mouattara, jmathieu & tbadard
 * @since 22-03-2009
 */
public class Symbolisation {
	public static final int LineStrokeColor=0;
	public static final int PolygonStrokeColor=1;
	public static final int LineStrokeWidth=2;
	public static final int PolygonStrokeWidth=3;
	public static final int PolygonFillColor=4;
	public static final int PointColor=5;
	public static final int PointOpacity=6;
	public static final int LineOpacity=7;
	public static final int PolygonOpacity=8;
	public static final int Radius=9;
	
	public static final String STROKECOLOR=Messages.getString("layerControl.style.strokeColor");
	public static final String STROKEWIDTH=Messages.getString("layerControl.style.strokeWidth");
	public static final String FILLCOLOR=Messages.getString("layerControl.style.fillColor");
	public static final String OPACITY=Messages.getString("layerControl.style.opacity");
	public static final String RADIUS=Messages.getString("layerControl.style.radius");
	public static final String COLOR=Messages.getString("layerControl.style.color");	
	public static final String COLLECTIONLINESTROKECOLOR=Messages.getString("layerControl.style.collectionLineStrokeColor");
	public static final String COLLECTIONPOLYGONSTROKECOLOR=Messages.getString("layerControl.style.collectionPolygonStrokeColor");
	public static final String COLLECTIONLINESTROKEWIDTH=Messages.getString("layerControl.style.collectionLineStrokeWidth");
	public static final String COLLECTIONPOLYGONSTROKEWIDTH=Messages.getString("layerControl.style.collectionPolygonStrokeWidth");
	public static final String COLLECTIONPOLYGONFILLCOLOR=Messages.getString("layerControl.style.collectionPolygonfillColor");
	public static final String COLLECTIONPOINTCOLOR=Messages.getString("layerControl.style.collectionPointColor");
	public static final String COLLECTIONPOINTOPACITY=Messages.getString("layerControl.style.collectionPointOpacity");
	public static final String COLLECTIONLINEOPACITY=Messages.getString("layerControl.style.collectionLineOpacity");
	public static final String COLLECTIONPOLYGONOPACITY=Messages.getString("layerControl.style.collectionPolygonOpacity");
	public static final String COLLECTIONPOINTRADIUS=Messages.getString("layerControl.style.collectionPointRadius");
	
	public static final String[] usage = new String[]{STROKECOLOR, STROKECOLOR, STROKEWIDTH, STROKEWIDTH, FILLCOLOR, COLOR, OPACITY, OPACITY, OPACITY, RADIUS, COLLECTIONLINESTROKECOLOR, COLLECTIONPOLYGONSTROKECOLOR, COLLECTIONLINESTROKEWIDTH, COLLECTIONPOLYGONSTROKEWIDTH,
		COLLECTIONPOLYGONFILLCOLOR, COLLECTIONPOINTCOLOR, COLLECTIONPOINTOPACITY, COLLECTIONLINEOPACITY, COLLECTIONPOLYGONOPACITY,
		COLLECTIONPOINTRADIUS};
	
	private int styleUsage;
	
	private Object featureStyle;//
	private Object lastFeatureStyle;//
	
	private Layer layerParent;
	
	public boolean isCustom;
	
	public void setStyleUsage(int usage){
		this.styleUsage = usage;
	}
	
	public void setFeatureStyle(Object o){
		this.featureStyle = o;
	}
	
	public void setLastFeatureStyle(Object o){
		this.lastFeatureStyle = o;
	}
	
	public int getStyleUsage(){
		return styleUsage;
	}
	
	public Object getFeatureStyle(){
		return featureStyle;
	}
	
	public Object getLastFeatureStyle(){
		return lastFeatureStyle;
	}
	
	public void setLayerParent(Layer l){
		this.layerParent=l;
	}
	
	public Symbolisation(int usage, Object fs) {
		this.styleUsage = usage;
		this.featureStyle = fs;
    	setLastFeatureStyle(fs);
		isCustom = true;
	}
	
	public void setIsCustom(boolean isCustom){
		this.isCustom = isCustom;
	}
	
	public boolean isCustom(){
		return isCustom;
	}
	
	public Layer getLayerParent(){
		return layerParent;
	}
	
	public void updateParent(){
		layerParent.update();
	}
	
	public String getUsage(int styleUsage){
		return usage[styleUsage];
	}
}