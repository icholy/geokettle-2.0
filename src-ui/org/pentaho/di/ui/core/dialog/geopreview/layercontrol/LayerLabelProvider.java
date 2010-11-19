package org.pentaho.di.ui.core.dialog.geopreview.layercontrol;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;

import org.pentaho.di.ui.core.dialog.geopreview.Layer;

/**
 * Label provider for an object of type Layer
 *
 * @author mouattara & tbadard
 * @since 22-03-2009
 */
public class LayerLabelProvider extends LabelProvider implements ITableLabelProvider, ICheckedStateProvider {

	// For the checkbox images
	private static ImageRegistry imageRegistry = new ImageRegistry();

	/**
	 * Note: An image registry owns all of the image objects registered with it,
	 * and automatically disposes of them the SWT Display is disposed.
	 */ 
	static {
		/*String iconPath = ""; 
		imageRegistry.put(CHECKED_IMAGE, ImageDescriptor.createFromFile(
				LayerControl.class, 
				iconPath + "sample.gif"
				)
			);
		imageRegistry.put(UNCHECKED_IMAGE, ImageDescriptor.createFromFile(
				LayerControl.class, 
				iconPath + "sample.gif"
				)
			);*/	
	}
	
	/**
	 * Returns the image with the given key, or <code>null</code> if not found.
	 * 
	 */
	private Image getImage(boolean isSelected) {
		//String key = isSelected ? CHECKED_IMAGE : UNCHECKED_IMAGE;
		//return  imageRegistry.get(key);
		return null;
	}


	/**
	 * Returns the label text for the given column of the given element.
	 * 
	 */
	public String getColumnText(Object element, int columnIndex) 
	{
		if (columnIndex == 1)
			return ((Layer)element).getName();
		//if (columnIndex == 2)
			//return "chaine";
		
		return "";
	}

	
	
	/**
	 * Returns the label image for the given column of the given element.
	 * 
	 */
	public Image getColumnImage(Object element, int columnIndex) 
	{
		
		//img.setBackground(d.getSystemColor(org.eclipse.swt.SWT.COLOR_BLACK));		
		if (columnIndex == 2){			
			org.eclipse.swt.widgets.Display d =  org.eclipse.swt.widgets.Display.getCurrent();
			Layer layer=(Layer)element;
			Color maCouleur=null;
			//Color maCouleur = (Color)layer.getStyle();
			//If the style of the layer is not defined, a new color is given.
			if (maCouleur==null){
				maCouleur=d.getSystemColor(SWT.COLOR_BLUE);				
			}
			
			//RGB coul2=(RGB)ch;
			
			//layer.setStyle(maCouleur);
			PaletteData paletteData = new PaletteData(new RGB[] {
					 maCouleur.getRGB() , new RGB(255,255,255)
					});
			ImageData sourceData = new ImageData(10,10,1,paletteData);
			
			Image img= new Image(d,sourceData); 
			return img;
		}			
		else
		return null;
		//return (columnIndex == 0) ? getImage(true) : getImage(false);
		//return null;
    }
	

	/**
	 * Returns the checked state for the given element
	 * 
	 */
	public boolean getChecked(Object element)
	{
		if (element instanceof Layer)
			return ((Layer)element).isVisible();
		else
			return true;
	}
	
	
	/**
	 * Returns the grayed state for the given element
	 * NOT SUPPORTED YET
	 * 
	 */
	public boolean getGrayed(Object element)
	{
		// NOT SUPPORTED, RETURN ALWAYS FALSE
		return false;
	}

}
