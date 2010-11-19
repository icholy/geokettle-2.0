package org.pentaho.di.ui.core.dialog.geopreview.layercontrol;

/**
 * Interface for a checkable element
 * 
 * @author mouattara & tbadard
 * @since 22-03-2009
 */
public interface ICheckedStateProvider 
{
    /**
     * Return the check state of the checkable element
     * 
     * @param element The element
     * @return true if checked, false if not
     * 
     */
	public boolean getChecked(Object element);

	
    /**
     * Return the grayed state of the checkable element
     * 
     * @param element The element
     * @return true if grayed, false if not
     * 
     */
	public boolean getGrayed(Object element);
}