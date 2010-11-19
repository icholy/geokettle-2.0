package org.pentaho.di.ui.core.dialog.geopreview;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import com.vividsolutions.jts.geom.Geometry;
import org.opengis.feature.simple.SimpleFeature; 

/**
 * This class holds all layer represented in the cartographic preview tab.
 * 
 * @author mouattara & tbadard
 * @since 22-03-2009
 */
public class LayerCollection implements Observer 
{

	private final int COUNT = 100;  // Initial number of object that the hashSet could handle
	
	// private HashMap<Integer, Layer> layers; // The hashset containing the layer
	
	//layer represents a associative table in which keys are integers and
	//values correspond to the name of the layer
	private HashMap layers; // The hashset containing the layer
	
	// private Set<ILayerListViewer> layerListViewers; // Set of observers
	private Set layerListViewers; // Set of observers


	/**
	 * Constructor
	 * 
	 */
	public LayerCollection() 
	{
		super();

		// this.layers = new HashMap<Integer, Layer>(COUNT*3);
		//initialisation of the associative table
		this.layers = new HashMap(COUNT*3);
		// this.layerListViewers = new HashSet<ILayerListViewer>();
		//The set is initialised
		this.layerListViewers = new HashSet();
		this.initData();
	}
	
	
	/**
	 * Initialize the table data.
	 * 
	 */
	private void initData() 
	{
	}

	
	/**
	 * Return the collection of layers
	 * 
	 */
	// public Collection<Layer> getLayers() 
	public Collection getLayers()
	{
		return layers.values();
	}
	
	
	/**
	 * Add a new layer to the collection of layers
	 * 
	 * @param index The unique key of the klayer
	 * @param name Name of the layer
	 * 
	 */
	public void addLayer(int index, String name) 
	{
		Layer layer;
		
		// Create and add the layer to the collection
		layer = new Layer(name);
		layer.addObserver(this);
		// layers.put(index, layer);
		layers.put(new Integer(index), layer);

		// Notify the observers that a layer have been added
		Iterator iterator = layerListViewers.iterator();
		while (iterator.hasNext())
			((ILayerListViewer) iterator.next()).addLayerEvent(layer);
	}

	
	/**
	 * Add a new layer to the collection of layers
	 * 
	 * @param index The unique key of the klayer
	 * @param layer The layer to add
	 * 
	 */
	public void addLayer( int index, Layer layer) 
	{
		// Add the layer to the collection
		layer.addObserver(this);
		// layers.put(index, layer);
		layers.put(new Integer(index), layer);
		
		// Notify the observers that a layer have been added
		Iterator iterator = layerListViewers.iterator();
		while (iterator.hasNext())
			((ILayerListViewer) iterator.next()).addLayerEvent(layer);
	}

	
	/**
	 * Add a geometry to the layer
	 * 
	 * @param geom The geom to add 
	 * @param layerName layerName of the layer to which the geometry should be added
	 * @param index The unique key of the layer to which the geometry should be added
	 * @param batchMode If in batch mode, doesn't call the layerChanged, accelerate the treatment.
	 * 
	 */
	public void addGeometryToLayer(Geometry geom, String layerName, int index, boolean batchMode)
	{
		Layer layer;
		
		// Get the layer based on the index
		// layer = this.layers.get(index);
		layer = (Layer) this.layers.get(new Integer(index));
		
		// Add the geometry and call the method layerChanged
		if (layer.getName().equalsIgnoreCase(layerName) == true)
		{
			layer.addGeometry((Geometry)geom, batchMode);
		}
	}
	
	/**
	 * Remove the layer from the collection of layers 
	 * 
	 * @param layer The layer to remove
	 * 
	 */
	public void removeLayer(Layer layer) 
	{
		// Remove the layer
		layers.remove(layer);
		
		// Notify the observers that the layer have been removed 
		Iterator iterator = layerListViewers.iterator();
		while (iterator.hasNext())
			((ILayerListViewer) iterator.next()).removeLayerEvent(layer);
	}

	
	/**
	 * Notify the changeListeners that a layer just changed
	 * 
	 * @param layer Layer that changed
	 * 
	 */
	public void layerChanged(Layer layer) 
	{
		Iterator iterator = this.layerListViewers.iterator();
		while (iterator.hasNext())
			((ILayerListViewer) iterator.next()).updateLayerEvent(layer);
	}
	

	/**
	 * Remove a changeListener
	 * 
	 * @param viewer The LayerListViewer to remove
	 * 
	 */
	public void removeLayerListViewer(ILayerListViewer viewer) 
	{
		layerListViewers.remove(viewer);
	}

	
	/**
	 * Add a viewer
	 * 
	 * @param viewer The LayerListViewer to add
	 * 
	 */
	public void addLayerListViewer(ILayerListViewer viewer) 
	{
		layerListViewers.add(viewer);
	}
	
	
	/**
	 * update method of Observer
	 * 
	 */
	public void update (Observable observable, Object object)
	{
		if (observable instanceof Layer)
		{
			this.layerChanged((Layer) object);
		}
	}
}
