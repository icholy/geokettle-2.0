/* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/

package org.pentaho.di.ui.core.dialog;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolderAdapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.DragDetectEvent;
import org.eclipse.swt.events.DragDetectListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.dialog.geopreview.Layer;
import org.pentaho.di.ui.core.dialog.geopreview.LayerCollection;
import org.pentaho.di.ui.core.dialog.geopreview.MapMediator;
import org.pentaho.di.ui.core.dialog.geopreview.canvas.CanvasResizeListener;
import org.pentaho.di.ui.core.dialog.geopreview.canvas.CanvasViewer;
import org.pentaho.di.ui.core.dialog.geopreview.canvas.GeoMouseListener;
import org.pentaho.di.ui.core.dialog.geopreview.canvas.GeoPreviewPaintListener;
import org.pentaho.di.ui.core.dialog.geopreview.layercontrol.LayerControl;
import org.pentaho.di.ui.core.dialog.geopreview.toolbar.ToolbarListener;
import org.pentaho.di.ui.core.dialog.geopreview.toolbar.ToolbarStates;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.step.BaseStepDialog;


/**
 * Displays an ArrayList of rows in a TableView.
 * 
 * @author Matt, mouattara & tbadard
 * @since 19-06-2003
 */
public class PreviewRowsDialog extends Dialog
{
    public static final int MAX_BINARY_STRING_PREVIEW_SIZE = 1000000;

	private String stepname;

    private Label wlFields;
    private TableView wFields;
    private FormData fdlFields, fdFields;
    // -- Begin GeoKettle modification --
    private FormData fdCanvas;
    // -- End GeoKettle modification --

    private Button wClose;
    private Button wStop;
    private Button wNext;
    private Button wLog;

    private Shell shell;
    private List<Object[]> buffer;
    private PropsUI props;
    private String title, message;

    private Rectangle bounds;
    private int hscroll, vscroll;
    private int hmax, vmax;
    private String loggingText;
    
    private boolean proposingToGetMoreRows;
    private boolean proposingToStop;

    private boolean askingForMoreRows;
    
    private boolean askingToStop;

    private RowMetaInterface rowMeta;
    
    private VariableSpace variables;

    // -- Begin GeoKettle modification --
    private CTabFolder tabFolder;
    private Composite composite;
    private Composite compositeB;
    private ToolBar toolBar;
    private LayerCollection layers;
    
    private final String FILENAME_NOSELECTION_ICON = "ui/images/no_selection.png";
    private final String FILENAME_PAN_ICON = "ui/images/pan_16x16.png";
    private final String FILENAME_ZOOMIN_ICON = "ui/images/zoom_in_16x16.png";
    private final String FILENAME_ZOOMOUT_ICON = "ui/images/zoom_out_16x16.png";
    private final String FILENAME_GETINFOS_ICON = "ui/images/getInfos_16x16.png";
    
    //TODO Require i18n support here!
    private final String TOOLTIP_PAN = "Pan";
    private final String TOOLTIP_ZOOMIN = "Zoom In";
    private final String TOOLTIP_ZOOMOUT = "Zoom Out";
    private final String TOOLTIP_GETINFOS = "Information";
    // -- End GeoKettle modification --

    
    public PreviewRowsDialog(Shell parent, VariableSpace space, int style, String stepName, RowMetaInterface rowMeta, List<Object[]> rowBuffer)
    {
        this(parent, space, style, stepName, rowMeta, rowBuffer, null);
        // -- Begin GeoKettle modification --
        this.layers = new LayerCollection();
        // -- End GeoKettle modification --
    }

    public PreviewRowsDialog(Shell parent, VariableSpace space, int style, String stepName, RowMetaInterface rowMeta, List<Object[]> rowBuffer, String loggingText)
    {
        super(parent, style);
        this.stepname = stepName;
        this.buffer = rowBuffer;
        this.loggingText = loggingText;
        this.rowMeta = rowMeta;
        this.variables = space;

        props = PropsUI.getInstance();
        bounds = null;
        hscroll = -1;
        vscroll = -1;
        title = null;
        message = null;
        // -- Begin GeoKettle modification --
        this.layers = new LayerCollection();
        // -- End GeoKettle modification --
    }

    public void setTitleMessage(String title, String message)
    {
        this.title = title;
        this.message = message;
    }

    public void open()
    {
        Shell parent = getParent();
        Display display = parent.getDisplay();

        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX);
        props.setLook(shell);
		shell.setImage(GUIResource.getInstance().getImageSpoon());

        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth = Const.FORM_MARGIN;
        formLayout.marginHeight = Const.FORM_MARGIN;
        
        // -- Begin GeoKettle modification --
        tabFolder = new CTabFolder(shell, SWT.BORDER);
        tabFolder.setBorderVisible(true);
        tabFolder.addCTabFolderListener(new CTabFolderAdapter() {
            public void itemClosed(CTabFolderEvent event) {}
          });
       
        
        FormData fdTab = new FormData();
        fdTab.left = new FormAttachment(0, 0);
        fdTab.top = new FormAttachment(0, Const.MARGIN);
        fdTab.right = new FormAttachment(100, 0);
        fdTab.bottom = new FormAttachment(100, -50);
        tabFolder.setLayoutData(fdTab);

        // A standard tab and a geographic view tab
        CTabItem standardItem = new CTabItem(tabFolder, SWT.NULL);
        standardItem.setText("Standard view");
        CTabItem geographicTab = new CTabItem(tabFolder, SWT.NULL);
        geographicTab.setText("Geographic view");
        
        composite = new Composite(tabFolder, SWT.NONE);
		composite.setLayout(formLayout);
		standardItem.setControl(composite);        

        compositeB = new Composite(tabFolder, SWT.NONE);
		compositeB.setLayout(formLayout);
		geographicTab.setControl(compositeB);        

		Composite compositeToolbar = new Composite(compositeB, SWT.BORDER);
		compositeToolbar.setLayout(formLayout);        
        // -- End GeoKettle modification --

        if (title == null)
            title = Messages.getString("PreviewRowsDialog.Title");
        if (message == null)
            message = Messages.getString("PreviewRowsDialog.Header", stepname);

        if(buffer!=null)   	message+=" "+Messages.getString("PreviewRowsDialog.NrRows",""+buffer.size());
        
        shell.setLayout(formLayout);
        shell.setText(title);

        // int middle = props.getMiddlePct();
        int margin = Const.MARGIN;

        // -- Begin GeoKettle modification --
        //wlFields = new Label(shell, SWT.LEFT);
        wlFields = new Label(composite, SWT.LEFT);
        // -- End GeoKettle modification --
        wlFields.setText(message);
        props.setLook(wlFields);
        fdlFields = new FormData();
        fdlFields.left = new FormAttachment(0, 0);
        fdlFields.right = new FormAttachment(100, 0);
        fdlFields.top = new FormAttachment(0, margin);
        wlFields.setLayoutData(fdlFields);
        
        // -- Begin GeoKettle modification --
        if (buffer.size() > 0)
		{
			//row = (Row) buffer.get(0);

	        for (int i = 0; i < rowMeta.size(); i++)
	        {
	        	ValueMetaInterface v = rowMeta.getValueMeta(i);
	            
	            if (v.isGeometry() == true)
	            {
	            	Layer layer = new Layer(v.getName());
	            	this.layers.addLayer(i, layer);
	            }
	        }
		}
		LayerControl layerControl = new LayerControl(compositeB, this.layers, compositeToolbar);
		
		final CanvasViewer canvas = new CanvasViewer(compositeB, SWT.NO_REDRAW_RESIZE | SWT.BORDER);
        fdCanvas = new FormData();
        fdCanvas.left = new FormAttachment(layerControl.getControl(), 5);
        fdCanvas.top = new FormAttachment(compositeToolbar, 0);
        fdCanvas.right = new FormAttachment(100, 0);
        fdCanvas.bottom = new FormAttachment(87, -50);
        canvas.setLayoutData(fdCanvas);
        MapMediator mapContext = new MapMediator(this.layers, canvas);
        mapContext.addObserver(canvas);
        //this.layers.addObserver(mapContext);
        canvas.addPaintListener(new GeoPreviewPaintListener(canvas, canvas.getDisplay(), mapContext));
        canvas.addListener(SWT.Resize, new CanvasResizeListener(mapContext));
        
        canvas.addMouseListener(new GeoMouseListener(mapContext));
        
        canvas.addDragDetectListener(new DragDetectListener(){
        	public void dragDetected(DragDetectEvent event) 
        	{
        		// TODO Here is the place to add code in order to have the image visible during drag operations
        	}
        	
        });
        this.layers.addLayerListViewer(mapContext);
        
        

        //Toolbar for the geographic pan
		toolBar = new ToolBar(compositeToolbar, SWT.HORIZONTAL | SWT.WRAP | SWT.RIGHT);
		toolBar.setSize(300, 65);

		final ToolItem radioItem1 = new ToolItem(toolBar, SWT.RADIO);
		//Image iconPan = new Image(shell.getDisplay(), getClass().getResourceAsStream(Const.IMAGE_DIRECTORY + this.FILENAME_PAN_ICON));
		Image iconPan = GUIResource.getInstance().getImage(this.FILENAME_PAN_ICON);
	    radioItem1.setImage(iconPan);
	    radioItem1.setData("id", ToolbarStates.PAN);
	    radioItem1.setToolTipText(this.TOOLTIP_PAN);
	    
	    new ToolItem(toolBar, SWT.SEPARATOR);
		
		final ToolItem radioItem2 = new ToolItem(toolBar, SWT.RADIO);
	    //Image iconZoomIn = new Image(shell.getDisplay(), getClass().getResourceAsStream(Const.IMAGE_DIRECTORY + this.FILENAME_ZOOMIN_ICON));
		Image iconZoomIn = GUIResource.getInstance().getImage(this.FILENAME_ZOOMIN_ICON);
		radioItem2.setImage(iconZoomIn);
	    radioItem2.setData("id", ToolbarStates.ZOOMIN);
	    radioItem2.setToolTipText(this.TOOLTIP_ZOOMIN);
	    
	    new ToolItem(toolBar, SWT.SEPARATOR);
	    
	    final ToolItem radioItem3 = new ToolItem(toolBar, SWT.RADIO);
	    //Image iconZoomOut = new Image(shell.getDisplay(), getClass().getResourceAsStream(Const.IMAGE_DIRECTORY +  this.FILENAME_ZOOMOUT_ICON));
	    Image iconZoomOut = GUIResource.getInstance().getImage(this.FILENAME_ZOOMOUT_ICON);
	    radioItem3.setImage(iconZoomOut);
	    radioItem3.setData("id", ToolbarStates.ZOOMOUT);
	    radioItem3.setToolTipText(this.TOOLTIP_ZOOMOUT);
	    
	    new ToolItem(toolBar, SWT.SEPARATOR);
	    // Add a GetInfo button
	    final ToolItem radioItem4 = new ToolItem(toolBar, SWT.RADIO);
	    //Image iconInfos = new Image(shell.getDisplay(), getClass().getResourceAsStream(Const.IMAGE_DIRECTORY +  this.FILENAME_GETINFOS_ICON));
	    Image iconInfos = GUIResource.getInstance().getImage(this.FILENAME_GETINFOS_ICON);
	    radioItem4.setImage(iconInfos);
	    radioItem4.setData("id", ToolbarStates.GETINFOS);
	    radioItem4.setToolTipText(this.TOOLTIP_GETINFOS);
	    
	    new ToolItem(toolBar, SWT.SEPARATOR);
	    
	    final ToolItem radioItem5 = new ToolItem(toolBar, SWT.RADIO);
	    //Image iconNoselect = new Image(shell.getDisplay(), getClass().getResourceAsStream(Const.IMAGE_DIRECTORY +  this.FILENAME_NOSELECTION_ICON));
	    Image iconNoselect = GUIResource.getInstance().getImage(this.FILENAME_NOSELECTION_ICON);
	    radioItem5.setImage(iconNoselect);
	    radioItem5.setData("id", ToolbarStates.NOSELECTION);
	    radioItem5.setToolTipText(this.TOOLTIP_GETINFOS);

	    
	    ToolbarListener listener = new ToolbarListener();
	    listener.addObserver(mapContext);
	    
	    radioItem5.addListener(SWT.Selection, listener);
	    radioItem1.addListener(SWT.Selection, listener);
	    radioItem2.addListener(SWT.Selection, listener);
	    radioItem3.addListener(SWT.Selection, listener);
	    radioItem4.addListener(SWT.Selection, listener);
	    
	    toolBar.pack();
        // -- End GeoKettle modification --
        
        // Mmm, if we don't get any rows in the buffer: show a dialog box.
        if (buffer == null || buffer.size() == 0)
        {
            ShowMessageDialog dialog = new ShowMessageDialog(shell, SWT.OK | SWT.ICON_WARNING, Messages.getString("PreviewRowsDialog.NoRows.Text"), Messages.getString("PreviewRowsDialog.NoRows.Message"));
            dialog.open();
            shell.dispose();
            return;
        }

        ColumnInfo[] colinf = new ColumnInfo[rowMeta.size()];
        for (int i = 0; i < rowMeta.size(); i++)
        {
            ValueMetaInterface v = rowMeta.getValueMeta(i);
            colinf[i] = new ColumnInfo(v.getName(), ColumnInfo.COLUMN_TYPE_TEXT, v.isNumeric());
            colinf[i].setToolTip(v.toStringMeta());
            colinf[i].setValueMeta(v);
        }

        // -- Begin GeoKettle modification --
        //wFields = new TableView(variables, shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colinf, 0, null, props);
        wFields = new TableView(variables, composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colinf, 0, null, props);
        // -- End GeoKettle modification --
        
        fdFields = new FormData();
        fdFields.left = new FormAttachment(0, 0);
        fdFields.top = new FormAttachment(wlFields, margin);
        fdFields.right = new FormAttachment(100, 0);
        fdFields.bottom = new FormAttachment(100, -50);
        wFields.setLayoutData(fdFields);
        
        List<Button> buttons = new ArrayList<Button>();
        
        wClose = new Button(shell, SWT.PUSH);
        wClose.setText(Messages.getString("System.Button.Close"));
        wClose.addListener(SWT.Selection, new Listener() { public void handleEvent(Event e) { close(); } });
        buttons.add(wClose);
        
        if (!Const.isEmpty(loggingText)) {
	        wLog = new Button(shell, SWT.PUSH);
	        wLog.setText(Messages.getString("PreviewRowsDialog.Button.ShowLog"));
	        wLog.addListener(SWT.Selection, new Listener() { public void handleEvent(Event e) { log(); } });
	        buttons.add(wLog);
        }

        if (proposingToStop) {
	        wStop = new Button(shell, SWT.PUSH);
	        wStop.setText(Messages.getString("PreviewRowsDialog.Button.Stop.Label"));
	        wStop.setToolTipText(Messages.getString("PreviewRowsDialog.Button.Stop.ToolTip"));
	        wStop.addListener(SWT.Selection, new Listener() { public void handleEvent(Event e) { askingToStop=true; close(); } });
	        buttons.add(wStop);
        }

        if (proposingToGetMoreRows) {
	        wNext = new Button(shell, SWT.PUSH);
	        wNext.setText(Messages.getString("PreviewRowsDialog.Button.Next.Label"));
	        wNext.setToolTipText(Messages.getString("PreviewRowsDialog.Button.Next.ToolTip"));
	        wNext.addListener(SWT.Selection, new Listener() { public void handleEvent(Event e) { askingForMoreRows=true; close(); } });
	        buttons.add(wNext);
        }
        
        if (proposingToGetMoreRows || proposingToStop) {
	        wClose.setText(Messages.getString("PreviewRowsDialog.Button.Close.Label"));
	        wClose.setToolTipText(Messages.getString("PreviewRowsDialog.Button.Close.ToolTip"));
        }

        // Position the buttons...
        //
        BaseStepDialog.positionBottomButtons(shell, buttons.toArray(new Button[buttons.size()]), margin, null);

        // Detect X or ALT-F4 or something that kills this window...
        shell.addShellListener( new ShellAdapter() { public void shellClosed(ShellEvent e) { close(); } } );

        getData();
        
        // -- Begin GeoKettle modification --

        FormData frmData=new FormData();
        frmData.left = new FormAttachment(layerControl.getControl(),5);
        //frmData.top = new FormAttachment(compositeToolbar,400);
        frmData.top = new FormAttachment(canvas,5);
        frmData.right = new FormAttachment(100, 0);
        frmData.bottom = new FormAttachment(100, -50);
        
        Table infoTable= new Table(compositeB,SWT.BORDER);
        infoTable.setHeaderVisible(true);
        infoTable.setLayoutData(frmData);
               
        for (int i = 0; i < rowMeta.size(); i++){
        	
        	TableColumn col=new TableColumn(infoTable,SWT.BORDER);
        	ValueMetaInterface v = rowMeta.getValueMeta(i);
        	col.setText(v.getName());
        	col.setWidth(100);
        }
        //bt.setLayoutData(frmData);

        // -- End GeoKettle modification --

        BaseStepDialog.setSize(shell);

        shell.open();

        // -- Begin GeoKettle modification --
        boolean stopAffiche=false;
        // -- End GeoKettle modification --        
        while (!shell.isDisposed())
        {
            if (!display.readAndDispatch()) display.sleep();
            
            // -- Begin GeoKettle modification --            
            if ((stopAffiche==false)&&!(wFields.isDisposed())){
            	
            	//System.out.println("#######"+mapContext.getFeaturePosition());
            	if (mapContext.getFeaturePosition()!=0){            	
            		//stopAffiche=true;
            		String[] chaineInfos=wFields.getItem(mapContext.getFeaturePosition()-1);
            		TableItem tbItem=new TableItem(infoTable,SWT.NONE);            		
            		tbItem=infoTable.getItem(0);
            		tbItem.setText(chaineInfos);            		
            	}
            }
            // -- End GeoKettle modification --
        }
    }

    public void dispose()
    {
        props.setScreen(new WindowProperty(shell));
        bounds = shell.getBounds();
        hscroll = wFields.getHorizontalBar().getSelection();
        vscroll = wFields.getVerticalBar().getSelection();
        shell.dispose();
    }

    /**
     * Copy information from the meta-data input to the dialog fields.
     */
    private void getData()
    {
        shell.getDisplay().asyncExec(new Runnable()
        {
            public void run()
            {
            	int nrErrors = 0;
                for (int i = 0; i < buffer.size(); i++)
                {
                    TableItem item;
                    if (i==0) item = wFields.table.getItem(i);
                    else item = new TableItem(wFields.table, SWT.NONE);
                    
                    // Display the correct line item...
                    //
                    String strNr;
                    try {
						strNr = wFields.getNumberColumn().getValueMeta().getString(new Long(i+1));
					} catch (Exception e) {
						strNr = Integer.toString(i+1);
					}
					item.setText(0, strNr);
                    
                    Object[] row = (Object[]) buffer.get(i);

                    for (int c = 0; c < rowMeta.size(); c++)
                    {
                        ValueMetaInterface v = rowMeta.getValueMeta(c);
                        String show;
                        try
                        {
                            show = v.getString(row[c]);
                            
                            // -- Begin GeoKettle modification --
                            
                            // byte[] valeur=v.getBinary(row[c]);
                            // Geometry p=convertBinaryToGeometry(valeur);
                             if (v.isGeometry())
                             {
                             	try {
                             		layers.addGeometryToLayer(v.getGeometry(row[c]), v.getName(), c, true);
                             		
                             	}
                             	catch (Exception exception)
                             	{
                             		exception.printStackTrace();
                             		return;
                             	}
                             	
                             }
                            
                            // -- End GeoKettle modification --
                            
                            if (v.isBinary() && show!=null && show.length()>MAX_BINARY_STRING_PREVIEW_SIZE)
                            {
                            	// We want to limit the size of the strings during preview to keep all SWT widgets happy.
                            	//
                            	show = show.substring(0, MAX_BINARY_STRING_PREVIEW_SIZE);
                            }
                        }
                        catch (KettleValueException e)
                        {
                        	nrErrors++;
                        	if (nrErrors<25)
                        	{
	                            LogWriter.getInstance().logError(toString(), Const.getStackTracker(e));
                        	}
                            show=null;
                        }
                        catch (ArrayIndexOutOfBoundsException e)
                        {
                        	nrErrors++;
                        	if (nrErrors<25)
                        	{
                        		LogWriter.getInstance().logError(toString(), Const.getStackTracker(e));
                        	}
                            show=null;
                        }

                        if (show != null)
                        {
                            item.setText(c + 1, show);
                        }
                    }
                }
                if (!wFields.isDisposed()) wFields.optWidth(true, 200);
            }
        });
    }

    private void close()
    {
        stepname = null;
        dispose();
    }

    /**
     * Show the logging of the preview (in case errors occurred
     */
    private void log()
    {
        if (loggingText != null)
        {
            EnterTextDialog etd = new EnterTextDialog(shell, Messages.getString("PreviewRowsDialog.ShowLogging.Title"), Messages.getString("PreviewRowsDialog.ShowLogging.Message"), loggingText);
            etd.open();
        }
    };

    public boolean isDisposed()
    {
        return shell.isDisposed();
    }

    public Rectangle getBounds()
    {
        return bounds;
    }

    public void setBounds(Rectangle b)
    {
        bounds = b;
    }

    public int getHScroll()
    {
        return hscroll;
    }

    public void setHScroll(int s)
    {
        hscroll = s;
    }

    public int getVScroll()
    {
        return vscroll;
    }

    public void setVScroll(int s)
    {
        vscroll = s;
    }

    public int getHMax()
    {
        return hmax;
    }

    public void setHMax(int m)
    {
        hmax = m;
    }

    public int getVMax()
    {
        return vmax;
    }

    public void setVMax(int m)
    {
        vmax = m;
    }

    /**
     * @return true if the user is asking to grab the next rows with preview
     */
	public boolean isAskingForMoreRows() {
		return askingForMoreRows;
	}

	/**
	 * @return true if the dialog is proposing to ask for more rows
	 */
	public boolean isProposingToGetMoreRows() {
		return proposingToGetMoreRows;
	}

	/**
	 * @param proposingToGetMoreRows Set to true if you want to display a button asking for more preview rows.
	 */
	public void setProposingToGetMoreRows(boolean proposingToGetMoreRows) {
		this.proposingToGetMoreRows = proposingToGetMoreRows;
	}

	/**
	 * @return the askingToStop
	 */
	public boolean isAskingToStop() {
		return askingToStop;
	}

	/**
	 * @return the proposingToStop
	 */
	public boolean isProposingToStop() {
		return proposingToStop;
	}

	/**
	 * @param proposingToStop the proposingToStop to set
	 */
	public void setProposingToStop(boolean proposingToStop) {
		this.proposingToStop = proposingToStop;
	}
}
