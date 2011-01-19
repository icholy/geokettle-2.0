package org.pentaho.di.ui.core.dialog.geopreview.layercontrol;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.pentaho.di.ui.core.dialog.geopreview.ILayerListViewer;
import org.pentaho.di.ui.core.dialog.geopreview.Layer;
import org.pentaho.di.ui.core.dialog.geopreview.LayerCollection;
import org.pentaho.di.ui.core.dialog.geopreview.Symbolisation;

/**
 * 
 * 
 * @author mouattara, jmathieu & tbadard
 * @since 22-03-2009
 */
public class LayerTreeContentProvider implements ITreeContentProvider,ILayerListViewer {

	private ArrayList<LayerCollection> layerList;
	private CheckboxTreeViewer tableViewer;
	
	public LayerTreeContentProvider(ArrayList<LayerCollection> layerList, CheckboxTreeViewer tableViewer){
		this.layerList = layerList;
		this.tableViewer = tableViewer;
	}
	
	@SuppressWarnings("unchecked")
	public Object[] getElements(Object element){
		ArrayList<LayerCollection> currentPersonWorks = new ArrayList<LayerCollection>();
		Iterator<LayerCollection> it = ((ArrayList<LayerCollection>) element).iterator();
		while(it.hasNext()){
			currentPersonWorks.add(it.next());				
		}
		return currentPersonWorks.toArray();
	}
	
	@SuppressWarnings("unchecked")
	public void inputChanged(Viewer v, Object oldInput, Object newInput){
		if (newInput != null){
			Iterator<LayerCollection> it = ((ArrayList<LayerCollection>) newInput).iterator();
			while (it.hasNext()) it.next().addLayerListViewer(this);
		}
		if (oldInput != null){
			Iterator<LayerCollection> it = ((ArrayList<LayerCollection>) oldInput).iterator();
			while (it.hasNext()) it.next().removeLayerListViewer(this);
		}
	}

	public Object[] getChildren(Object element){
		if(element instanceof Layer)
			return ((Layer) element).getStyle().toArray();
		if(element instanceof LayerCollection)
			return ((LayerCollection) element).getLayers().toArray();
		return null;
	}

	public Object getParent(Object element){
		if(element instanceof Symbolisation)
			return ((Symbolisation)element).getLayerParent();
		if(element instanceof Layer)
			return ((Layer)element).getLayerCollectionParent();
		return null;		
	}

	public boolean hasChildren(Object element){
		if(element instanceof LayerCollection)
			return !(((LayerCollection) element).getLayers().size()==0);
		if(element instanceof Layer && ((Layer)element).getGeometryCount()!=0)
			return !(((Layer) element).getStyle().size()==0);		
		return false;			
	}
		
	public void dispose(){
		Iterator<LayerCollection> it = layerList.iterator();
		while(it.hasNext()){
			it.next().removeLayerListViewer(this);
		}
	}
	
	public void addLayerCollectionEvent(LayerCollection lc){
		tableViewer.add(lc,null);
	}
	
	public void addLayerEvent(Layer layer){
		tableViewer.add(layer,null);
	}

	public void removeLayerEvent(Layer layer){
		tableViewer.remove(layer);
	}
	
	public void removeLayerCollectionEvent(LayerCollection lc){
		tableViewer.remove(lc);
	}

	public void updateLayerCollectionEvent(LayerCollection lc){
		tableViewer.update(lc, null);
	}
	
	public void updateLayerEvent(Layer layer){
		tableViewer.update(layer, null);
	}
}