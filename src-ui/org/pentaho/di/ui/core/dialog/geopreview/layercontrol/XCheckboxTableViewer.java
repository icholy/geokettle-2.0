package org.pentaho.di.ui.core.dialog.geopreview.layercontrol;

import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Widget;

/**
 * 
 * This class inherits from TableViewer and allows to have one listener for
 * the triggered event when an user clicks on a checkbox
 * 
 * @author mouattara & tbadard
 * @since 22-03-2009
 *  
 */
public class XCheckboxTableViewer extends CheckboxTableViewer 
{
	
	public XCheckboxTableViewer(Table table) 
    {
        super(table);
    }

    
	protected void preservingSelection(Runnable updateCode) 
    {
        updateCode.run();
    }

	protected void doUpdateItem(Widget widget, Object element, boolean bool) 
	{
        super.doUpdateItem(widget, element, bool);
        
        IBaseLabelProvider baseProvider = getLabelProvider();
        
        if (baseProvider instanceof ICheckedStateProvider) 
        {
            ICheckedStateProvider provider = (ICheckedStateProvider)baseProvider;
            this.setChecked(element, provider.getChecked(element));
            this.setGrayed(element, provider.getGrayed(element));
        }
    }	
}
