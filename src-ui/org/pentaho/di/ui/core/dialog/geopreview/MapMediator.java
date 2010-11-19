package org.pentaho.di.ui.core.dialog.geopreview;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.geotools.factory.FactoryRegistryException;
import org.geotools.feature.DefaultFeatureCollection;

import org.opengis.feature.simple.SimpleFeature;

import org.opengis.feature.simple.SimpleFeatureType;
import org.geotools.feature.FeatureCollection;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.DefaultMapLayer;
import org.geotools.map.MapContext;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.geometry.MismatchedDimensionException;
import org.pentaho.di.ui.core.dialog.geopreview.canvas.CanvasViewer;
import org.pentaho.di.ui.core.dialog.geopreview.toolbar.ToolbarListener;
import org.pentaho.di.ui.core.dialog.geopreview.toolbar.ToolbarStates;
import org.pentaho.di.ui.core.util.geo.GeometryConverter;
import org.pentaho.di.ui.core.util.geo.renderer.swt.LayerFactory;
//import org.geotools.feature.FeatureCollections;

//import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
//import java.util.List;
//import java.util.ArrayList;

/**
 * 	From the book "Design Patterns - Elements of Reusable Object-Oriented Software"
 *  Define an object that encapsulates how a set of objects interact. Mediator promotes
 * 	loose coupling by keeping objects from referring to each other explicitly, and
 * 	it lets you vary their interaction independently.
 * 
 * @author mouattara & tbadard
 * @since 22-03-2009
 */
public class MapMediator extends Observable implements ILayerListViewer, Observer 
{
	// -------------- Variables ------------
	private int canvasWidth;
	private int canvasHeight;
	private ReferencedEnvelope envelope;
	private LayerCollection layerList;
	
	private ToolbarStates toolbarState;

	private FeatureCollection<SimpleFeatureType, SimpleFeature> pointFeatures;
	private FeatureCollection<SimpleFeatureType, SimpleFeature> lineFeatures;
	private FeatureCollection<SimpleFeatureType, SimpleFeature> polygonFeatures;
	private FeatureCollection<SimpleFeatureType, SimpleFeature> allFeatures;
	
	private int featurePosition;
	
	 
	private MapContext mapContext;
	// -------------------------------------
	
	// ---------------Constants ------------
	private final double ZOOM_OUT_FACTOR = 2.0; 
	private final double ZOOM_IN_FACTOR = 0.5;

	private final String FEATURE_COLLECTION_ID = "1";
	
	// -------------------------------------
	
	
	/**
	 * Constructor
	 * 
	 * @param layerList The list of layer to encapsulate
	 * @canvasViewer The canvas displaying those maps
	 * 
	 */
	public MapMediator(LayerCollection layerList, CanvasViewer canvasViewer)
	{
		this.layerList = layerList;
		this.toolbarState = ToolbarStates.NOSELECTION; 
	}
	

	/**
	 * Method to set the width and the height of the canvas
	 * 
	 * @param width Width of the canvas
	 * @param height Height of the canvas
	 * 
	 */
	public void setCanvasSize(int width, int height)
	{
		this.canvasWidth = width;
		this.canvasHeight = height;
		
		// Notify observers that something changed
		this.setChanged();
		this.notifyObservers();
	}
	
	
	/**
	 * Move the map depending on the difference between the starting point and
	 * ending point on the screen (first click - drag - release click)
	 *  
	 * @param diffXOnScreen Difference on x axis
	 * @param diffYOnScreen Difference on y axis
	 * 
	 */
	public void setMove(int diffXOnScreen, int diffYOnScreen)
	{
		double diffX; 
		double diffY;

		// Calculate the difference in the coordinate system
		diffX = (this.envelope.getMaxX() - this.envelope.getMinX()) * diffXOnScreen / this.getCanvasWidth(); 
		diffY = -(this.envelope.getMaxY() - this.envelope.getMinY()) * diffYOnScreen / this.getCanvasHeight();
		
		// Create the new envelope
		this.envelope = new ReferencedEnvelope(this.envelope.getMinX()-diffX, this.envelope.getMaxX()-diffX, this.envelope.getMinY()-diffY, this.envelope.getMaxY()-diffY, DefaultGeographicCRS.WGS84);
		
		// Notify observers that the map data have changed
		this.setChanged();
		this.notifyObservers();
	}


	/**
	 * Return the canvas width
	 * 
	 * @return the canvas width
	 * 
	 */
	public int getCanvasWidth()
	{
		return this.canvasWidth;
	}


	/**
	 * Return the canvas height
	 * 
	 * @return The canvas height
	 * 
	 */
	public int getCanvasHeight()
	{
		return this.canvasHeight;
	}
	

	/**
	 * Return the ReferencedEnvelope
	 * 
	 * @return The referenced envelope
	 * 
	 */
	public ReferencedEnvelope getEnvelope()
	{
		this.updateGeoFeatures();
		return this.envelope;
	}

	
	/**
	 * Return the number of geometries to be drawn (depending on the number of visible layers)
	 * 
	 * @return The number of geometries
	 * 
	 */
	public int getGeoFeaturesSize()
	{
		this.updateGeoFeatures();
	
		if (this.allFeatures == null){
			return -1;
		}
		else {
			return this.allFeatures.size();
		}
	}
	
	
	/**
	 * Return the mapContext (based on the visible layers)
	 * 
	 * @return The map context
	 * 
	 */
	public MapContext getMapContext() 
	{
		this.updateGeoFeatures();
		
		return mapContext;
	}

	/**
	 * 
	 */
	public int getFeaturePosition(){
		return this.featurePosition;
	}
	/**
	 * Return the geoFeatures collection
	 * 
	 */
	// @SuppressWarnings("unchecked")
	public void updateGeoFeatures()
	{
		LayerFactory layerFactory;
		GeometryWrapper geom;
    	DefaultMapLayer allFeatureLayer;
    	SimpleFeature feature;//modifier
    	String couleurHTMLFeatureStyle=""; 
    	String couleurBackGroundHTMLFeatureStyle="";
    	String opacityValue="";
    	int lineStrokeWidth=0;
    	long begin;
    	
    	
    	
        begin = System.currentTimeMillis();
    	
		layerFactory = new LayerFactory();
    	allFeatureLayer = null;
		
		this.allFeatures = new DefaultFeatureCollection(this.FEATURE_COLLECTION_ID, GeometryConverter.featureType);
		
		// Populate the feature collection
		// for (Layer layer : this.getLayerList())
		
		for(Iterator<Layer> i = this.getLayerList().iterator() ; i.hasNext() ; )
		{
			Layer layer = (Layer) i.next();
			List<Symbolisation> mesFeatureSymbolisation=layer.getStyle();
			Iterator<Symbolisation> itSymbol=mesFeatureSymbolisation.iterator();
						 
			while (itSymbol.hasNext()){
				Symbolisation s=itSymbol.next();
				
				if (s.getStyleUsage()==Symbolisation.LineForeColor){
					RGB monRGBLineColor=(RGB)s.getFeatureStyle();
					
					java.awt.Color coulAWT=new java.awt.Color(monRGBLineColor.red,monRGBLineColor.green,monRGBLineColor.blue);
					
					couleurHTMLFeatureStyle= "#"+Integer.toHexString(coulAWT.getRGB()).substring(2);
					
				}
				
				if (s.getStyleUsage()==Symbolisation.BackGroundColor){
					RGB monRGBLineColor=(RGB)s.getFeatureStyle();
					java.awt.Color coulBackgroundAWT=new java.awt.Color(monRGBLineColor.red,monRGBLineColor.green,monRGBLineColor.blue);
					
					couleurBackGroundHTMLFeatureStyle= "#"+Integer.toHexString(coulBackgroundAWT.getRGB()).substring(2);
				}
				
				if (s.getStyleUsage()==Symbolisation.LineStroke){
					lineStrokeWidth=Integer.parseInt((String)s.getFeatureStyle());
				}
				
				if(s.getStyleUsage()==Symbolisation.Opacite){
					opacityValue=(String)s.getFeatureStyle();
				}
			}
			
			//
			
			if (layer.isVisible() == true)
			{
	    		for (int x = 0; x < layer.getGeometriesNumber(); x++)
	    		{
	    			// Get one geometry
	    			
	    			geom = layer.getGeometry(x);
	    			
	    			if (geom.getJTSGeom() instanceof MultiPoint || geom.getJTSGeom() instanceof Point)
	    			{
	    				if (this.pointFeatures == null)
	    					this.pointFeatures = new DefaultFeatureCollection(this.FEATURE_COLLECTION_ID, GeometryConverter.featureType);

    					feature = geom.getGeotoolsFeature();
    					
    					if (feature != null)
    					{
    						
    						this.pointFeatures.add(feature);
    						this.allFeatures.add(feature);
    					}
	    			}
	    			else if (geom.getJTSGeom() instanceof MultiLineString || geom.getJTSGeom() instanceof LineString)
	    			{
	    				if (this.lineFeatures == null)
	    					this.lineFeatures = new DefaultFeatureCollection(this.FEATURE_COLLECTION_ID, GeometryConverter.featureType);

    					feature = geom.getGeotoolsFeature();
    					
    					if (feature != null)
    					{
    						this.lineFeatures.add(feature);
    						this.allFeatures.add(feature);
    					}
	    			}
	    			else if (geom.getJTSGeom() instanceof MultiPolygon || geom.getJTSGeom() instanceof Polygon)
	    			{
	    				if (this.polygonFeatures == null)
	    					this.polygonFeatures = new DefaultFeatureCollection(this.FEATURE_COLLECTION_ID, GeometryConverter.featureType);
	    				
    					feature = geom.getGeotoolsFeature();
    					
    					if (feature != null)
    					{
    						
    						this.polygonFeatures.add(feature);
    						this.allFeatures.add(feature);
    						    						
    					}
	    			}
	    		}
	    		
			}
		}
    	
    	// If no geometry to render, return immediately
    	if ( (this.pointFeatures == null || this.pointFeatures.features().hasNext() == false) && 
    		 (this.lineFeatures == null || this.lineFeatures.features().hasNext() == false) &&
    		 (this.polygonFeatures == null || this.polygonFeatures.features().hasNext() == false) )
    	{
    		System.out.println("Returning - No Geometries");
        	//System.out.print("MapMediator.updateGeoFeatures done in " + String.valueOf((System.currentTimeMillis()-begin)) + " ms\n");
    		return;
    	}

    	// Create a layer with the geometries
    	try 
    	{
            mapContext = layerFactory.createMapContext();

            //DefaultMapLayer mapLayer = new DefaultMapLayer(this.geoFeatures, layerFactory.createDefaultPolygonLayerStyle(this.DEFAULT_POLYGON_STROKE_COLOR, this.DEFAULT_POLYGON_STROKE_WIDTH, this.DEFAULT_POLYGON_FILL_COLOR));
    		//DefaultMapLayer mapLayer = new DefaultMapLayer(this.geoFeatures, layerFactory.createDefaultLineLayerStyle());
    		// DefaultMapLayer mapLayer = new DefaultMapLayer(this.geoFeatures, layerFactory.createDefaultPointLayerStyle());
    		if (this.pointFeatures != null && 
    			this.pointFeatures.features().hasNext() == true)
    		{
    			DefaultMapLayer pointLayer = new DefaultMapLayer(this.pointFeatures, layerFactory.createDefaultPointLayerStyle(lineStrokeWidth));
    			mapContext.addLayer(pointLayer);
    		}

    		if (this.lineFeatures != null &&
    			this.lineFeatures.features().hasNext() == true)
    		{
    			DefaultMapLayer lineLayer = new DefaultMapLayer(this.lineFeatures, layerFactory.createDefaultLineLayerStyle(lineStrokeWidth, couleurHTMLFeatureStyle));
    			mapContext.addLayer(lineLayer);
    		}
    		
    		if (this.polygonFeatures != null &&
    			this.polygonFeatures.features().hasNext() == true)
    		{
    			DefaultMapLayer polygonLayer = new DefaultMapLayer(this.polygonFeatures, layerFactory.createDefaultPolygonLayerStyle(couleurHTMLFeatureStyle, lineStrokeWidth, couleurBackGroundHTMLFeatureStyle,opacityValue));
    			mapContext.addLayer(polygonLayer);
    		}

    		if (this.allFeatures != null &&
    			this.allFeatures.features().hasNext() == true)
    		{
    			allFeatureLayer = new DefaultMapLayer(this.allFeatures, layerFactory.createDefaultLayerStyles());
    		}
    			
            
            // If no envelope defined yet
            if (this.envelope == null)
            	this.envelope = new ReferencedEnvelope(allFeatureLayer.getFeatureSource().getBounds(), DefaultGeographicCRS.WGS84);//.transform(layerFactory.getDefaultCRS(), true);
            
        } 
    	catch (FactoryRegistryException ex) {
            ex.printStackTrace();
            return;
        } 
    	catch (IOException ex) {
            ex.printStackTrace();
            return;
        } 
    	catch (MismatchedDimensionException ex) {
            ex.printStackTrace();
            return;
        }   

    	//System.out.print("MapMediator.updateGeoFeatures done in " + String.valueOf((System.currentTimeMillis()-begin)) + " ms\n");
	}

	
	/**
	 * Notification that a layer have been added to the collection of layers
	 * 
	 * @param layer The layer that have been added
	 * 
	 */
	public void addLayerEvent(Layer layer) 
	{
		this.updateGeoFeatures();
		
		this.setChanged();
		this.notifyObservers();
	}


	/**
	 * Notification that a layer have been removed to the collection of layers
	 * 
	 * @param layer The layer that have been removed
	 * 
	 */
	public void removeLayerEvent(Layer Layer) 
	{
		this.updateGeoFeatures();
		
		this.setChanged();
		this.notifyObservers();
	}

	
	/**
	 * Notification that a layer have been updated to the collection of layers
	 * 
	 * @param layer The layer that have been updated
	 * 
	 */
	public void updateLayerEvent(Layer layer) 
	{
		this.updateGeoFeatures();
		
		this.setChanged();
		this.notifyObservers();
	}
	

	/**
	 * Notification from an observervable that something changed
	 * 
	 * @param observable The observable who send the notification
	 * @param the object concern by the notification
	 * 
	 */
	public void update(Observable observable, Object arg)
	{
		// Update the ToolbarState
		if (observable instanceof ToolbarListener) {
			this.toolbarState = ((ToolbarListener)observable).getToolbarState();
		}

		// Update the geoFeatures based on the new changed
		this.updateGeoFeatures();
	}	

	
	/**
	 * Return a collection of layer
	 * 
	 * @return The collection of layer
	 * 
	 */
	// public Collection<Layer> getLayerList()
	public Collection getLayerList()
	{
		return this.layerList.getLayers();
	}

	
	/**
	 * Return the toolbar state (which button is pressed)
	 * 
	 * @return the toolbar state
	 * 
	 */
	public ToolbarStates getToolbarState()
	{
		return this.toolbarState;
	}
	
	
	/**
	 * Apply a zoomin to the ReferencedEnvelope
	 * 
	 * @param x The x-coord on screen
	 * @param y The y-coord on screen
	 * 
	 */
	public void zoomInOnPoint(int x, int y)
	{
		double newEnvelopewidth;
		double newEnvelopeHeight;
		double diffX;
		double diffY;
		
		// Recenter the map
		this.recenterCalculator(x, y);
		
		newEnvelopewidth = this.envelope.getWidth() * this.ZOOM_IN_FACTOR;
		newEnvelopeHeight = this.envelope.getHeight() * this.ZOOM_IN_FACTOR;
		diffX = (this.envelope.getWidth() - newEnvelopewidth) / 2;
		diffY = (this.envelope.getHeight() - newEnvelopeHeight) / 2;
		
		// The new envelope
		this.envelope = new ReferencedEnvelope(this.envelope.getMinX()+diffX, this.envelope.getMaxX()-diffX, this.envelope.getMinY()+diffY, this.envelope.getMaxY()-diffY, DefaultGeographicCRS.WGS84);
		
		// Notify the observers that something changed
		this.setChanged();
		this.notifyObservers();
	}
	
	
	/**
	 * Apply a zoomout to the ReferencedEnvelope
	 * 
	 * @param x The x-coord on screen
	 * @param y The y-coord on screen
	 * 
	 */
	public void zoomOutOnPoint(int x, int y)
	{
		double newEnvelopewidth;
		double newEnvelopeHeight;
		double diffX;
		double diffY;
		
		// Recenter the map
		this.recenterCalculator(x, y);
		
		newEnvelopewidth = this.envelope.getWidth() * this.ZOOM_OUT_FACTOR;
		newEnvelopeHeight = this.envelope.getHeight() * this.ZOOM_OUT_FACTOR;
		diffX = (this.envelope.getWidth() - newEnvelopewidth) / 2;
		diffY = (this.envelope.getHeight() - newEnvelopeHeight) / 2;
		
		// new envelope
		this.envelope = new ReferencedEnvelope(this.envelope.getMinX()+diffX, this.envelope.getMaxX()-diffX, this.envelope.getMinY()+diffY, this.envelope.getMaxY()-diffY, DefaultGeographicCRS.WGS84);
		
		// Notify the observers that something changed
		this.setChanged();
		this.notifyObservers();
	}
	
	
	/**
	 * Calculator to recenter a map
	 * 
	 * @param x x-coord on the screen
	 * @param y y-coord on the screen
	 */
	private void recenterCalculator(int x, int y)
	{
		double newX;
		double newY;
		double pixelValueX;
		double pixelValueY;
		

		// Proportion between the screen and the referenced envelope
		pixelValueX = this.envelope.getWidth() / this.getCanvasWidth();
		pixelValueY = this.envelope.getHeight() / this.getCanvasHeight();
		
		// x-coord and y-coord in the referenced system
		newX = this.envelope.getMinX() + x * pixelValueX;
		newY = this.envelope.getMinY() + (this.canvasHeight-y) * pixelValueY;
		
		// the new envelope
		this.envelope =new ReferencedEnvelope(newX-this.envelope.getWidth()/2, newX+this.envelope.getWidth()/2, newY-this.envelope.getHeight()/2, newY+this.envelope.getHeight()/2, DefaultGeographicCRS.WGS84);
	}

	/**
	 * Allow to retrieve a feature near the mouse pointer when mouse is clicked.
	 * Mouse coordinates are captured and a bounding box is built to encompass this point.
	 * This extent is a point when fixedValue is set to 0. 
	 */ 
	public void getFeatureInformations(int x, int y){
		
		double newX;
		double newY;
		double pixelValueX;
		double pixelValueY;
		double fixedValueX=this.envelope.getWidth()/2;
		double fixedValueY=this.envelope.getHeight()/2;
		
		//this.recenterCalculator(x, y);
		// Proportion between the screen and the referenced envelope
		pixelValueX = this.envelope.getWidth() / this.getCanvasWidth();
		pixelValueY = this.envelope.getHeight() / this.getCanvasHeight();
		
		// x-coord and y-coord in the referenced system
		newX = this.envelope.getMinX() + x * pixelValueX;
		newY = this.envelope.getMinY() + (this.canvasHeight-y) * pixelValueY;
		
		
		
		//this.recenterCalculator(x, y);
		
		/*double newEnvelopewidth = this.envelope.getWidth() * this.ZOOM_IN_FACTOR;
		double newEnvelopeHeight = this.envelope.getHeight() * this.ZOOM_IN_FACTOR;
		double diffX = (this.envelope.getWidth() - newEnvelopewidth) / 2;
		double diffY = (this.envelope.getHeight() - newEnvelopeHeight) / 2;
		*/
		
		// The new envelope
		//this.envelope = new ReferencedEnvelope(this.envelope.getMinX()+diffX, this.envelope.getMaxX()-diffX, this.envelope.getMinY()+diffY, this.envelope.getMaxY()-diffY, DefaultGeographicCRS.WGS84);

		
		ReferencedEnvelope myEnvelope=new ReferencedEnvelope(newX-10, newX+10, newY-10, newY+10, DefaultGeographicCRS.WGS84);
		
		//ReferencedEnvelope myEnvelope=new ReferencedEnvelope(newX-fixedValueX, newX+fixedValueX, newY-fixedValueY, newY+fixedValueY, DefaultGeographicCRS.WGS84);
		
		//System.out.println("MinX->"+myEnvelope.getMinX()+" MaxX->"+myEnvelope.getMaxX()+" MinY->"+myEnvelope.getMinY()+" MaxY->"+myEnvelope.getMaxY());
		//System.out.println("=======================================================");
		int cpt=0;
	    for( Iterator<SimpleFeature> i=allFeatures.iterator(); i.hasNext();){
	    	 cpt++;
	    	 SimpleFeature feature =(SimpleFeature) i.next();
	          
	    	 double milieuX=this.envelope.getMinX()+feature.getBounds().getWidth()/2;
	    	 double milieuY=this.envelope.getMinY()+feature.getBounds().getHeight()/2;
	    	 
	    	 
	    	 //if ((newX>=feature.getBounds().getMinX())&&(newX<=feature.getBounds().getMaxX())&&((newY>=feature.getBounds().getMinY())&&(newY<=feature.getBounds().getMaxY()))){
	    	// if ((newX>=envTemp.getMinX())&&(newX<=envTemp.getMaxX())&&((newY>=envTemp.getMinY())&&(newY<=envTemp.getMaxY()))){
	    	 //System.out.println("Max X ->"+ feature.getBounds().getMaxX() +" Min X ->"+ feature.getBounds().getMinX()+" Max Y ->"+feature.getBounds().getMaxY());	
	    	 //System.out.println("##########################################");
	    	 
	    	 //if (myEnvelope.contains(feature.getBounds())){
	    	 
	    	 
	    	 //
	    	 //System.out.println("center=========="+envTemp.centre().toString());
	    	 	    	 //
	    	 if( feature.getBounds().contains(myEnvelope.centre().x, myEnvelope.centre().y)){
	        	  
	        	  //unBoolean=feature.getBounds().contains(newX, newY);
	        	  //System.out.println(""+ feature.getBounds().getMinX() +";"+ feature.getBounds().getMaxX()+";"+feature.getBounds().getMinY()+";"+feature.getBounds().getMaxY());
	        	  System.out.println("feature Id "+cpt+" -->"+feature.getID());
	        	  this.featurePosition=cpt;
	        	  break;
	          }
	     }
	    
		     
	}
	
	
	/**
	 * Recenter a map based on the x-coord and y-coord from the screen
	 * 
	 * @param x x-coord on the screen
	 * @param y y-coord on the screen
	 */
	public void recenter(int x, int y)
	{
		// Calculation
		this.recenterCalculator(x, y);
		
		// Notify the observer that something changed
		this.setChanged();
		this.notifyObservers();
	}
	
}
