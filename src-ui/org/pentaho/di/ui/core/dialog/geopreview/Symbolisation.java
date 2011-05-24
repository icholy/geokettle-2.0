package org.pentaho.di.ui.core.dialog.geopreview;

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
	public static final int CollectionColor=6;
	public static final int PointOpacity=7;
	public static final int LineOpacity=8;
	public static final int PolygonOpacity=9;
	public static final int CollectionOpacity=10;
	public static final int Radius=11;
	
	public static final String STROKECOLOR="Stroke Color";
	public static final String STROKEWIDTH="Stroke Width";
	public static final String FILLCOLOR="Fill Color";
	public static final String OPACITY="Opacity";
	public static final String RADIUS="Radius";
	public static final String COLOR="Color";
	
	public static final String[] usage = new String[]{STROKECOLOR, STROKECOLOR, STROKEWIDTH, STROKEWIDTH, FILLCOLOR, COLOR, COLOR, OPACITY, OPACITY, OPACITY, OPACITY, RADIUS };
	
	private int styleUsage;
	
	private Object featureStyle;//
	private Object lastFeatureStyle;//
	
	private Layer layerParent;
	
	public boolean isCustom;
	
	public void setStyleUsage(int usage){
		this.styleUsage=usage;
	}
	
	public void setFeatureStyle(Object o){
		this.featureStyle=o;
	}
	
	public void setLastFeatureStyle(Object o){
		this.lastFeatureStyle=o;
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
		this.styleUsage=usage;
		this.featureStyle=fs;
    	setLastFeatureStyle(fs);
		isCustom=true;
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