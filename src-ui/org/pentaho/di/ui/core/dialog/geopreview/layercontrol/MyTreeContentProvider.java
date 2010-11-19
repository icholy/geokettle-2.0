package org.pentaho.di.ui.core.dialog.geopreview.layercontrol;

import java.util.List;

import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.pentaho.di.ui.core.dialog.geopreview.ILayerListViewer;
import org.pentaho.di.ui.core.dialog.geopreview.Layer;
import org.pentaho.di.ui.core.dialog.geopreview.LayerCollection;
import org.pentaho.di.ui.core.dialog.geopreview.Symbolisation;

/**
 * 
 * 
 * @author mouattara & tbadard
 * @since 22-03-2009
 */
public class MyTreeContentProvider implements ITreeContentProvider,ILayerListViewer {

	private LayerCollection layerList;
	private CheckboxTreeViewer tableViewer;
	
	public MyTreeContentProvider(LayerCollection layerList, CheckboxTreeViewer tableViewer)
	{
		this.layerList = layerList;
		this.tableViewer = tableViewer;
	}
	
	@SuppressWarnings("unchecked")
	public Object[] getElements(Object element) {
		final Object[] currentPersonWorks = ((LayerCollection) element).getLayers()
				.toArray();
		return currentPersonWorks;
	}

	
	
	public void inputChanged(Viewer v, Object oldInput, Object newInput) 
	{
		if (newInput != null)
			((LayerCollection) newInput).addLayerListViewer(this);
		if (oldInput != null)
			((LayerCollection) oldInput).removeLayerListViewer(this);
	}
	//public void inputChanged(Viewer arg0, Object arg1, Object arg2) {
	//}

	public Object[] getChildren(Object element) {
		final Layer currentPersonWork = (Layer) element;
		return currentPersonWork.getStyle().toArray();
	}

	public Object getParent(Object element) {
		if (element instanceof Symbolisation) {
			
			return ((Symbolisation)element).getLayerParent();
		} else {
			return null;
		}
	}

	public boolean hasChildren(Object element) {
		if (element instanceof Layer) {
			Layer current = (Layer) element;
			return !(current.getStyle().size()==0);
		} else {
			return false;
		}
	}
	
	
	public void dispose() 
	{
		layerList.removeLayerListViewer(this);
	}
	
	public boolean getChecked(Object element)
	{
		//if (element instanceof Layer)
			//return ((Layer)element).isVisible();
		//else
			return true;
	}
	public void addLayerEvent(Layer layer) 
	{
		tableViewer.add(layer,null);
	}

	
	/* (non-Javadoc)
	 * @see ILayerListViewer#removeLayer(Layer)
	 */
	public void removeLayerEvent(Layer layer) 
	{
		tableViewer.remove(layer);
	}

	
	/* (non-Javadoc)
	 * @see ILayerListViewer#updateLayer(ExampleLayer)
	 */
	public void updateLayerEvent(Layer layer) 
	{
		tableViewer.update(layer, null);
	}
}