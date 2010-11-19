package org.pentaho.di.ui.core.dialog.geopreview.toolbar;

import java.util.Observable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ToolItem;
import org.pentaho.di.core.Const;
import org.pentaho.di.ui.core.dialog.PreviewRowsDialog;
import org.pentaho.di.ui.core.gui.GUIResource;

/**
 * Listen to the click on the toolbar. Keep also a track of the state of the toolbar (which
 * button is clicked) and notify observer when a change	occurs on the state.
 * 
 * @author mouattara & tbadard
 * @since 22-03-2009
 */
public class ToolbarListener extends Observable implements Listener
{
	// State of the toolbar
	private ToolbarStates state;
	private final String FILENAME_GETINFOS_ICON = "ui/images/getInfos_16x16.png";
	private final String FILENAME_PAN_ICON = "ui/images/pan_16x16.png";
	private final String FILENAME_ZOOMIN_ICON = "ui/images/zoom_in_16x16.png";
	private final String FILENAME_ZOOMOUT_ICON = "ui/images/zoom_out_16x16.png";
	private final String FILENAME_NOSELECTION_ICON = "ui/images/no_selection.png";
	
	/**
	 * Constructor
	 * 
	 */
	public ToolbarListener()
	{
		this.state = ToolbarStates.NOSELECTION;
	}

	
	/**
	 * Return the current state of the toolbar
	 * 
	 * @return The state of the toolbar
	 * 
	 */
	public ToolbarStates getToolbarState()
	{
		return this.state;
	}

	
	/**
	 * Method to handle event thrown by the toolbar
	 * 
	 * @param event The event
	 * 
	 */
	public void handleEvent(Event event) 
	{
		ToolItem item = null;
		
		try {

		    item = (ToolItem)event.widget;
		    
			
		}
		catch (Exception e)	{
			e.printStackTrace();
			return;
		}

		// Get the new state and notify the observers
		if (item != null && item.getSelection() == true)
		{
			if ((ToolbarStates)item.getData("id") != this.state)
			{
				//String imagePath=null;
				Image iconInfos = null;
				Cursor cursor=null;
				this.state = (ToolbarStates)item.getData("id");
				ToolbarStates temp=(ToolbarStates)item.getData("id");
				
				if (this.state==ToolbarStates.GETINFOS){
					//imagePath=Const.IMAGE_DIRECTORY + this.FILENAME_GETINFOS_ICON;
					iconInfos = GUIResource.getInstance().getImage(this.FILENAME_GETINFOS_ICON);
				}
				if (this.state==ToolbarStates.PAN){
					//imagePath=Const.IMAGE_DIRECTORY + this.FILENAME_PAN_ICON;
					iconInfos = GUIResource.getInstance().getImage(this.FILENAME_PAN_ICON);
				}
				if (this.state==ToolbarStates.ZOOMIN){
					//imagePath=Const.IMAGE_DIRECTORY + this.FILENAME_ZOOMIN_ICON;
					iconInfos = GUIResource.getInstance().getImage(this.FILENAME_ZOOMIN_ICON);
				}
				if (this.state==ToolbarStates.ZOOMOUT){
					//imagePath=Const.IMAGE_DIRECTORY + this.FILENAME_ZOOMOUT_ICON;
					iconInfos = GUIResource.getInstance().getImage(this.FILENAME_ZOOMOUT_ICON);
				}
				if (this.state==ToolbarStates.NOSELECTION){
					//imagePath=Const.IMAGE_DIRECTORY + this.FILENAME_NOSELECTION_ICON;
					iconInfos = GUIResource.getInstance().getImage(this.FILENAME_NOSELECTION_ICON);
					cursor = new Cursor(item.getDisplay(), SWT.CURSOR_ARROW);
						
				}
				//Image iconInfos = new Image(item.getDisplay(), getClass().getResourceAsStream(imagePath));
				if (this.state!=ToolbarStates.NOSELECTION)
					cursor = new Cursor(item.getDisplay(), iconInfos.getImageData(), 0, 0);
			    //Cursor cursor = new Cursor(item.getDisplay(), SWT.CURSOR_HAND);
			    item.getParent().getParent().getParent().setCursor(cursor);
				
				this.setChanged();
				this.notifyObservers();
			}
		}
	}
}
