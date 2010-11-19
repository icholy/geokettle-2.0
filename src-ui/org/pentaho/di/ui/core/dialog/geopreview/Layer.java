package org.pentaho.di.ui.core.dialog.geopreview;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.pentaho.di.ui.core.util.geo.GeometryConverter;


import com.vividsolutions.jts.geom.Geometry;

/**
 * Encapsulates data related to a layer. This class allows to define layers which appear when a geometry column is previewed. 
 * 
 * @author mouattara & tbadard
 * @since 22-03-2009
 */
public class Layer extends Observable
{
	
	// --------------- Constants ------------
	private final int DEFAULT_SIZE = 50;
	private final boolean DEFAULT_VISIBLE = true;
	// -------------------------------------

	
	// --------------- Variables ------------
	//the name of the layer
	private String name;
	//this attribut is used to set the layer visibility
	private boolean visible; 
	//used to represent the style of the layer
	private List<Symbolisation> style; 
	// private ArrayList<GeometryWrapper> geodata;
	private ArrayList geodata;
	// -------------------------------------
	
	
	/**
	 * Constructor
	 * 
	 * @param name Name of the layer
	 */
	public Layer(String name)
	{
		Symbolisation s;
		this.name = name;
		this.visible = this.DEFAULT_VISIBLE;
		//setVisible(this.DEFAULT_VISIBLE);
		// this.geodata = new ArrayList<GeometryWrapper>(this.DEFAULT_SIZE);
		this.geodata = new ArrayList(this.DEFAULT_SIZE);
		this.style= new ArrayList<Symbolisation>();

		//Set initial value for the color of lines
		s=new Symbolisation(Symbolisation.LineForeColor,(Object)new RGB((int)(Math.random()*255),(int)(Math.random()*255),(int)(Math.random()*255)));
		s.setLayerParent(this);
		this.style.add(s);
		
		//Set initial value for background color
		s=new Symbolisation(Symbolisation.BackGroundColor,(Object)new RGB((int)(Math.random()*255),(int)(Math.random()*255),(int)(Math.random()*255)));
		s.setLayerParent(this);
		this.style.add(s);
		
		//Set initial value for strokewidth
		s=new Symbolisation(Symbolisation.LineStroke,"1" );
		s.setLayerParent(this);
		this.style.add(s);
		
		//Set initial value for opacity of the layer
		s=new Symbolisation(Symbolisation.Opacite,"1.0");
		s.setLayerParent(this);
		this.style.add(s);
		
	}

	
	/**
	 * Return the name of the layer
	 * 
	 * @return The name of the layer
	 */
	public String getName()
	{
		return this.name;
	}
	/**
	 * Return the style of the layer
	 * 
	 * @return the style of the layer
	 */
	public List<Symbolisation> getStyle()
	{
		return this.style;
	}
	
	
	/**
	 * Set the name of the layer
	 * 
	 * @param name The name of the layer
	 */
	public void setName(String name)
	{
		this.name = name;

		this.setChanged();
		this.notifyObservers(this);
	}
	/**
	 * set the style of the layer with un object of SWT Color type
	 * 
	 * @param style Style of the layer
	 */
	public void setStyle(List<Symbolisation> style)
	{
		this.style = style;

		this.setChanged();
		this.notifyObservers(this);
	}

	/**
	 * If the layer should be visible in the canvas
	 * 
	 * @return true: the layer should be visible
	 * 		   false: the layer should not be visible
	 */
	public boolean isVisible() {
		return visible;
	}


	/**
	 * If the layer should be visible in the canvas
	 * 
	 * @param visible true: the layer will be visible, false: the layer will not be visible
	 * 
	 */
	public void setVisible(boolean visible) 
	{
		this.visible = visible;
		this.setChanged();
		this.notifyObservers(this);
	}

	
	/**
	 * Add a geometry to the layer
	 * 
	 * @param geom The geometry to add
	 */
	public void addGeometry(Geometry geom, boolean batchMode)
	{
		GeometryWrapper geometryWrapper;
		

		geometryWrapper = new GeometryWrapper(geom);
		
		this.geodata.add(geometryWrapper);
		
		
		if (batchMode == false)
		{
			this.setChanged();
			this.notifyObservers(this);
		}
	}
	
	
	/**
	 * Return the number of geometries contained in the layer
	 * 
	 * @return The number of geometries contained in the layer
	 */
	public int getGeometriesNumber()
	{
		return this.geodata.size();
	}
	
	
	/**
	 * Return the geometry corresponding to the index
	 * 
	 * @param index The index of the geometry to return
	 * @return The geometry
	 */
	public GeometryWrapper getGeometry(int index)
	{
		// return this.geodata.get(index);
		return (GeometryWrapper) this.geodata.get(index);
	}
}
