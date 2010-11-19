package org.pentaho.di.ui.core.dialog.geopreview;

import java.util.Observable;


/**
 * d�fintion d'une classe permettant de personnaliser les �lements de la carte
 * � savoir la couleur et le style des lignes; ainsi que la couleur de remplissage 
 * des polygones.
 */
/**
 * This class allows the personalisation of colors and opacity (backgound and lines) and of line styles (strokewidth)
 * 
 * @author mouattara & tbadard
 * @since 22-03-2009
 */
public class Symbolisation {
	public static int LineForeColor=0;
	public static int LineStroke=1;
	public static int BackGroundColor=2;
	public static int Opacite=3;
	
	private int styleUsage;
	
	private Object featureStyle;//
	
	private Layer layerParent;
	
	public void setStyleUsage(int usage){
		this.styleUsage=usage;
	}
	public void setFeatureStyle(Object o){
		this.featureStyle=o;		
	}
	public int getStyleUsage(){
		return this.styleUsage;
	}
	public Object getFeatureStyle(){
		return this.featureStyle;
	}
	public void setLayerParent(Layer l){
		this.layerParent=l;
	}
	
	public Symbolisation(int usage,Object lineC) {
		this.styleUsage=usage;
		this.featureStyle=lineC;		
	}
	public Layer getLayerParent(){
		return this.layerParent;
	}
	
}
