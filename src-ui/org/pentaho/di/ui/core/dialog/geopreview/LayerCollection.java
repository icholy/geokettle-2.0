package org.pentaho.di.ui.core.dialog.geopreview;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * This class holds all layer represented in the cartographic preview tab.
 * 
 * @author mouattara, jmathieu & tbadard
 * @since 22-03-2009
 */
public class LayerCollection implements Observer 
{		
	private final int NRLAYERS = 4;
	
	private ArrayList<Layer> layers;
	private ArrayList<ArrayList<Object>> featureIndexes;
	
	private Set<ILayerListViewer> layerListViewers; // Set of observers

	private String name;

	public boolean isVisible;
	
	public LayerCollection(String name) {
		super();
		this.name=name;
		isVisible=true;
		layers = new ArrayList<Layer>(NRLAYERS);
		featureIndexes = new ArrayList<ArrayList<Object>>(NRLAYERS);
		layerListViewers = new HashSet<ILayerListViewer>();
		for(int i = 0;i<NRLAYERS;i++){
			featureIndexes.add(new ArrayList<Object>());
			addLayer(name, i);
		}				
	}

	public ArrayList<Layer> getLayers(){
		return layers;
	}
	
	public String getName(){
		return name;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public  ArrayList<ArrayList<Object>> getFeatureIndexes(){
		return featureIndexes;
	}
	
	public int getGeometryCount(){
		int size = 0;
		size+=layers.get(Layer.POINT_LAYER).getSingleGeometryCount();
		size+=layers.get(Layer.LINE_LAYER).getSingleGeometryCount();
		size+=layers.get(Layer.POLYGON_LAYER).getSingleGeometryCount();
		size+=layers.get(Layer.COLLECTION_LAYER).getGeometryCount();
		return size;
	}

	private void addLayer(String name, int layerType) {
		Layer layer = new Layer(name, layerType, this);
		layer.addObserver(this);
		layers.add(layer);
		Iterator<ILayerListViewer> iterator = layerListViewers.iterator();
		while (iterator.hasNext())
			((ILayerListViewer) iterator.next()).addLayerEvent(layer);
	}

	public void addGeometryToCollection(Geometry geom, boolean batchMode, int featureIndex){	
		if (geom!=null){
			int type = -1;
			if(geom instanceof Point || geom instanceof MultiPoint){
				type = Layer.POINT_LAYER;
				layers.get(type).addGeometry((Geometry)geom, batchMode);
			}else if(geom instanceof LineString || geom instanceof MultiLineString){
				type = Layer.LINE_LAYER;
				layers.get(type).addGeometry((Geometry)geom, batchMode);
			}else if(geom instanceof Polygon || geom instanceof MultiPolygon){
				type = Layer.POLYGON_LAYER;
				layers.get(type).addGeometry((Geometry)geom, batchMode);
			}else if(geom instanceof GeometryCollection){
				type = Layer.COLLECTION_LAYER;
				layers.get(type).addGeometry((Geometry)geom, batchMode);
			}
			featureIndexes.get(type).add(featureIndex);
		}
	}

	public void removeLayer(Layer layer) {
		layers.remove(layer);		
		Iterator<ILayerListViewer> iterator = layerListViewers.iterator();
		while (iterator.hasNext())
			((ILayerListViewer) iterator.next()).removeLayerEvent(layer);
	}

	public void layerChanged(Layer layer) {
		Iterator<ILayerListViewer> iterator = layerListViewers.iterator();
		while (iterator.hasNext())
			((ILayerListViewer) iterator.next()).updateLayerEvent(layer);
	}

	public void removeLayerListViewer(ILayerListViewer viewer) {
		layerListViewers.remove(viewer);
	}

	public void addLayerListViewer(ILayerListViewer viewer) {
		layerListViewers.add(viewer);
	}

	public void update (Observable observable, Object object){
		if (observable instanceof Layer) layerChanged((Layer) object);		
	}
	
	public boolean isVisible(){
		return isVisible;
	}
	
	public void setVisible(boolean isVisible, boolean changeLayersVisibility){
		this.isVisible=isVisible;
		if(changeLayersVisibility){
			Iterator<Layer> it = getLayers().iterator();
			while(it.hasNext())it.next().setVisible(isVisible);
		}
	}
}