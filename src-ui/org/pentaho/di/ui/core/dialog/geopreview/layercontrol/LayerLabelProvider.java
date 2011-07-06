package org.pentaho.di.ui.core.dialog.geopreview.layercontrol;

import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.pentaho.di.ui.core.dialog.geopreview.Layer;
import org.pentaho.di.ui.core.dialog.geopreview.LayerCollection;
import org.pentaho.di.ui.core.dialog.geopreview.Symbolisation;

/**
 * Label provider for an object of type Layer
 *
 * @author mouattara, jmathieu & tbadard
 * @since 22-03-2009
 */
public class LayerLabelProvider extends LabelProvider implements ITableLabelProvider{
	
	private CheckboxTreeViewer tableViewer;
	
	public LayerLabelProvider(CheckboxTreeViewer tableViewer){
		this.tableViewer=tableViewer;
	}
	
	public String getColumnText(Object element, int columnIndex) {				
		if(columnIndex == 0){
			if(element instanceof LayerCollection)
				return ((LayerCollection)element).getName()+" ("+((LayerCollection)element).getGeometryCount()+")";
			if(element instanceof Layer){
				tableViewer.setChecked(element, ((Layer)element).isVisible());
				if (((Layer)element).getType()==Layer.POINT_LAYER)
					return ((Layer)element).labels[Layer.POINT_LAYER]+" ("+((Layer)element).getSingleGeometryCount()+")";
				if (((Layer)element).getType()==Layer.COLLECTION_LAYER)
					return ((Layer)element).labels[Layer.COLLECTION_LAYER]+" ("+((Layer)element).getGeometryCount()+")";
				if (((Layer)element).getType()==Layer.LINE_LAYER)
					return ((Layer)element).labels[Layer.LINE_LAYER]+" ("+((Layer)element).getSingleGeometryCount()+")";
				if (((Layer)element).getType()==Layer.POLYGON_LAYER)
					return ((Layer)element).labels[Layer.POLYGON_LAYER]+" ("+((Layer)element).getSingleGeometryCount()+")";		
			}
			if (element instanceof Symbolisation){
				tableViewer.setChecked(element, ((Symbolisation)element).isCustom());
				return ((Symbolisation)element).getUsage(((Symbolisation)element).getStyleUsage());
			}
			return "";
		}else if(columnIndex == 1){
			if (element instanceof Symbolisation)
				return ((Symbolisation)element).getFeatureStyle().toString();
			return "";
		}			
		return "";
	}

	public Image getColumnImage(Object element, int columnIndex) {			
		if (columnIndex == 0){	
			if (element instanceof Symbolisation){
				if((((Symbolisation)element).getStyleUsage()==Symbolisation.PointColor)||(((Symbolisation)element).getStyleUsage()==Symbolisation.PolygonFillColor)||(((Symbolisation)element).getStyleUsage()==Symbolisation.LineStrokeColor) ||(((Symbolisation)element).getStyleUsage()==Symbolisation.PolygonStrokeColor) ||(((Symbolisation)element).getStyleUsage()==Symbolisation.CollectionColor)){
					Display d =  Display.getCurrent();							
					PaletteData paletteData = new PaletteData(new RGB[] {(RGB)((Symbolisation)element).getFeatureStyle(), (RGB)((Symbolisation)element).getFeatureStyle()});
					ImageData sourceData = new ImageData(10,10,1,paletteData);			
					return new Image(d,sourceData);
				}
				return null;
			}
			return null;
		}			
		return null;
    }

	public boolean getChecked(Object element){
		if(element instanceof LayerCollection)
			return ((LayerCollection)element).isVisible();
		if(element instanceof Layer)
			return ((Layer)element).isVisible();
		if(element instanceof Symbolisation)
			return ((Symbolisation)element).isCustom();
		return true;		
	}	
	
	public boolean getGrayed(Object element){
		return false;
	}
}