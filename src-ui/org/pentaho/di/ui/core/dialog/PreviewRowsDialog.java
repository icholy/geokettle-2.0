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
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
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
import org.pentaho.di.ui.core.dialog.geopreview.GeoFeaturesManager;
import org.pentaho.di.ui.core.dialog.geopreview.LayerCollection;
import org.pentaho.di.ui.core.dialog.geopreview.canvas.GeoCanvas;
import org.pentaho.di.ui.core.dialog.geopreview.layercontrol.LayerControl;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.util.geo.renderer.swt.SWTMapRenderer;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

/**
 * Displays an ArrayList of rows in a TableView.
 * 
 * @author Matt, mouattara, jmathieu & tbadard
 * @since 19-06-2003
 */
public class PreviewRowsDialog extends Dialog
{
    public static final int MAX_BINARY_STRING_PREVIEW_SIZE = 1000000;

	private String stepname;

    private Label wlFields;
    private TableView wFields;
    private FormData fdlFields, fdFields;

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

    private CTabFolder tabFolder;
    
    private Composite standardTab;
    private Composite geoTab;
    
    private ToolBar  wGeoToolBar;
    private ToolItem wToolItemPan; 
    private ToolItem wToolItemZoomIn;
    private ToolItem wToolItemZoomOut;
    private ToolItem wToolItemGetInfos;
    private ToolItem wToolItemZoomToLayersExtent;
    
    private Label wlX;
    private FormData fdlX;
    private Label wlCoordX;
    private FormData fdlCoordX;
    private Label wlY;
    private FormData fdlY;
    private Label wlCoordY;
    private FormData fdlCoordY;
    
    private LayerControl wLayerControl;
    
    private GeoCanvas wGeoCanvas;
    private FormData fdGeoCanvas;
    
    private Table wInfoTable;
    private FormData fdInfoTable;
    
    private GeoFeaturesManager geoFeaturesManager;
    
    private ArrayList<LayerCollection> layers;
    
    private final String PAN_ICON = "ui/images/pan.png";
    private final String ZOOMIN_ICON = "ui/images/zoomIn.png";
    private final String ZOOMOUT_ICON = "ui/images/zoomOut.png";
    private final String GETINFOS_ICON = "ui/images/getInfos.png";
    private final String GETINFOSCURSOR_ICON = "ui/images/getInfosCursor.png";
    private final String ZOOMTOLAYERSEXTENT_ICON = "ui/images/zoomToLayersExtent.png";

    public PreviewRowsDialog(Shell parent, VariableSpace space, int style, String stepName, RowMetaInterface rowMeta, List<Object[]> rowBuffer){
        this(parent, space, style, stepName, rowMeta, rowBuffer, null);
        layers = new ArrayList<LayerCollection>();
    }

    public PreviewRowsDialog(Shell parent, VariableSpace space, int style, String stepName, RowMetaInterface rowMeta, List<Object[]> rowBuffer, String loggingText){
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
        
        layers = new ArrayList<LayerCollection>();
    }

    public void setTitleMessage(String title, String message){
        this.title = title;
        this.message = message;
    }

    public void open(){
        Shell parent = getParent();
        Display display = parent.getDisplay();

        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX);
        props.setLook(shell);
		shell.setImage(GUIResource.getInstance().getImageSpoon());

        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth = Const.FORM_MARGIN;
        formLayout.marginHeight = Const.FORM_MARGIN;
        
        if (title == null)
            title = Messages.getString("PreviewRowsDialog.Title");
        if (message == null)
            message = Messages.getString("PreviewRowsDialog.Header", stepname);

        if(buffer!=null)   	message+=" "+Messages.getString("PreviewRowsDialog.NrRows",""+buffer.size());
        
        shell.setLayout(formLayout);
        shell.setText(title);      
        
        int margin = Const.MARGIN;
        
        tabFolder = new CTabFolder(shell, SWT.BORDER);
        tabFolder.setBorderVisible(true);      
        
        FormData fdTab = new FormData();
        fdTab.left = new FormAttachment(0, 0);
        fdTab.top = new FormAttachment(0, Const.MARGIN);
        fdTab.right = new FormAttachment(100, 0);
        fdTab.bottom = new FormAttachment(100, -50);
        tabFolder.setLayoutData(fdTab);

        //Standard Tab
        CTabItem standardItem = new CTabItem(tabFolder, SWT.NULL);
        standardItem.setText("Standard view");
        
        standardTab = new Composite(tabFolder, SWT.NONE);
        standardTab.setLayout(formLayout);
		standardItem.setControl(standardTab); 
		
		wlFields = new Label(standardTab, SWT.LEFT);
        wlFields.setText(message);
        props.setLook(wlFields);
        fdlFields = new FormData();
        fdlFields.left = new FormAttachment(0, 0);
        fdlFields.right = new FormAttachment(100, 0);
        fdlFields.top = new FormAttachment(0, margin);
        wlFields.setLayoutData(fdlFields);
	      
        // Mmm, if we don't get any rows in the buffer: show a dialog box.
        if (buffer == null || buffer.size() == 0){
            ShowMessageDialog dialog = new ShowMessageDialog(shell, SWT.OK | SWT.ICON_WARNING, Messages.getString("PreviewRowsDialog.NoRows.Text"), Messages.getString("PreviewRowsDialog.NoRows.Message"));
            dialog.open();
            shell.dispose();
            return;
        }

        ColumnInfo[] colinf = new ColumnInfo[rowMeta.size()];
        for (int i = 0; i < rowMeta.size(); i++){
            ValueMetaInterface v = rowMeta.getValueMeta(i);
            colinf[i] = new ColumnInfo(v.getName(), ColumnInfo.COLUMN_TYPE_TEXT, v.isNumeric());
            colinf[i].setToolTip(v.toStringMeta());
            colinf[i].setValueMeta(v);
        }

        wFields = new TableView(variables, standardTab, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colinf, 0, null, props);
        fdFields = new FormData();
        fdFields.left = new FormAttachment(0, 0);
        fdFields.top = new FormAttachment(wlFields, margin);
        fdFields.right = new FormAttachment(100, 0);
        fdFields.bottom = new FormAttachment(100, -50);
        wFields.setLayoutData(fdFields);
               
        ///////////////
        //Geographic tab
        //////////////////
        geoTab = new Composite(tabFolder, SWT.NONE);
		geoTab.setLayout(formLayout);
		
        CTabItem geographicTab = new CTabItem(tabFolder, SWT.NULL);
        geographicTab.setText("Geographic view");                     		
		geographicTab.setControl(geoTab);        

        //ToolBar the geographic tools
		wGeoToolBar = new ToolBar(geoTab, SWT.HORIZONTAL | SWT.WRAP | SWT.RIGHT);

		SelectionAdapter lsToolbarItemSelect = new SelectionAdapter(){
			public void widgetSelected(SelectionEvent event) {
				deselectGeoToolBarItems();
				((ToolItem)event.widget).setSelection(true);
			}
		};
		
		SelectionAdapter lsZoomToLayersExtent = new SelectionAdapter(){
			public void widgetSelected(SelectionEvent event) {
				deselectGeoToolBarItems();
				geoFeaturesManager.zoomToLayersExtent();
			}
		};
		
		wToolItemPan = new ToolItem(wGeoToolBar, SWT.RADIO);
		wToolItemPan.setImage(GUIResource.getInstance().getImage(PAN_ICON));		
		wToolItemPan.setData("id", "PAN");
		wToolItemPan.setToolTipText(Messages.getString("PreviewRowsDialog.Pan.Tooltip"));	    
		wToolItemPan.addSelectionListener(lsToolbarItemSelect);
		wToolItemPan.setSelection(true);
		
		wToolItemZoomIn = new ToolItem(wGeoToolBar, SWT.RADIO);
		wToolItemZoomIn.setImage(GUIResource.getInstance().getImage(ZOOMIN_ICON));
		wToolItemZoomIn.setData("id", "ZOOMIN");
		wToolItemZoomIn.setToolTipText(Messages.getString("PreviewRowsDialog.ZoomIn.Tooltip"));
		wToolItemZoomIn.addSelectionListener(lsToolbarItemSelect);
		
		wToolItemZoomOut = new ToolItem(wGeoToolBar, SWT.RADIO);
		wToolItemZoomOut.setImage(GUIResource.getInstance().getImage(ZOOMOUT_ICON));
		wToolItemZoomOut.setData("id", "ZOOMOUT");
		wToolItemZoomOut.setToolTipText(Messages.getString("PreviewRowsDialog.ZoomOut.Tooltip"));
	    wToolItemZoomOut.addSelectionListener(lsToolbarItemSelect);
	    
	    wToolItemZoomToLayersExtent = new ToolItem(wGeoToolBar, SWT.RADIO);
	    wToolItemZoomToLayersExtent.setImage(GUIResource.getInstance().getImage(ZOOMTOLAYERSEXTENT_ICON));
	    wToolItemZoomToLayersExtent.setData("id", "ZOOMTOLAYERSEXTENT");
	    wToolItemZoomToLayersExtent.setToolTipText(Messages.getString("PreviewRowsDialog.ZoomToLayersExtent.Tooltip"));
	    wToolItemZoomToLayersExtent.addSelectionListener(lsZoomToLayersExtent);
	    
	    wToolItemGetInfos = new ToolItem(wGeoToolBar, SWT.RADIO);
	    wToolItemGetInfos.setImage(GUIResource.getInstance().getImage(GETINFOS_ICON));
	    wToolItemGetInfos.setData("id", "GETINFOS");
	    wToolItemGetInfos.setToolTipText(Messages.getString("PreviewRowsDialog.GetInfos.Tooltip"));
	    wToolItemGetInfos.addSelectionListener(lsToolbarItemSelect);	    	
	    	    
	    wGeoToolBar.pack();
	    
        if (buffer.size() > 0){
	        for (int i = 0; i < rowMeta.size(); i++){
	        	ValueMetaInterface v = rowMeta.getValueMeta(i);            
	            if (v.isGeometry())
	            	layers.add(new LayerCollection(v.getName()));	            
	        }
			for (int i = 0; i < buffer.size(); i++){                       
			    Object[] row = (Object[]) buffer.get(i);
			    for (int c = 0; c < rowMeta.size(); c++){			    	
			        ValueMetaInterface v = rowMeta.getValueMeta(c);                        
		            if (v.isGeometry()){
		             	try {
		             		Iterator<LayerCollection> it = layers.iterator();
		                    while(it.hasNext()){
		                    	LayerCollection lc = it.next();
		                    	if(lc.getName().equals(v.getName()))
		                    		lc.addGeometryToCollection(v.getGeometry(row[c]), true, i);		                    	
		                    }                    		
		             	}catch (Exception exception){
		             		exception.printStackTrace();
		             		return;
		             	}                             	
		            }		        
			    }
			}
		}               
         
        FormData fd = new FormData();
        fd.left = new FormAttachment(0, 0);
        fd.top = new FormAttachment(wGeoToolBar, margin);
        fd.right = new FormAttachment(30, 0);
        fd.bottom = new FormAttachment(100, -margin);
        wLayerControl = new LayerControl(geoTab, layers, fd);             
        
		wGeoCanvas = new GeoCanvas(geoTab, SWT.BORDER);
        fdGeoCanvas = new FormData();
        fdGeoCanvas.left = new FormAttachment(wLayerControl.getControl(), margin);
        fdGeoCanvas.top = new FormAttachment(wGeoToolBar, margin);
        fdGeoCanvas.right = new FormAttachment(100, 0);
        fdGeoCanvas.bottom = new FormAttachment(87, -30);
        wGeoCanvas.setLayoutData(fdGeoCanvas);
        
        geoFeaturesManager = new GeoFeaturesManager(layers);
        geoFeaturesManager.addObserver(wGeoCanvas);
        
        Listener lsMouseListener = new Listener(){  
        	private Point startingPoint;
        	private boolean pan = false;
			public void handleEvent(Event event) {
		        switch (event.type) {
		            case SWT.MouseDown: 
		            	if(event.button ==1){
			        		if (wToolItemPan.getSelection()){
			        			pan = true;
			        			startingPoint = new Point(event.x, event.y);
			        		}else if (wToolItemZoomIn.getSelection())
			        			geoFeaturesManager.zoomInOnPoint(event.x, event.y);
			        		else if (wToolItemZoomOut.getSelection())
			        			geoFeaturesManager.zoomOutOnPoint(event.x, event.y);
			        		else if (wToolItemGetInfos.getSelection()){   
			        			int rowIndex = geoFeaturesManager.getFeatureIndex(event.x, event.y);
		        				wInfoTable.removeAll();
		        				TableItem tbItem = new TableItem(wInfoTable, SWT.NONE);
			        			if(rowIndex>=0)tbItem.setText(wFields.getItem(rowIndex));  	        						                    		            					                    				                    	
			        		}
		            	}
		            	break;
		            case SWT.MouseUp:
		            	if(pan){
		            		pan = false;
		            		startingPoint = null;
		            	}
		            	break;
		            case SWT.MouseMove:	            	
		            	if(pan){
		            		geoFeaturesManager.move(event.x - startingPoint.x, event.y - startingPoint.y);
		            		startingPoint = new Point(event.x, event.y);
		            	}
		            	String x = Double.toString(geoFeaturesManager.getX(event.x));
		            	wlCoordX.setText(x.substring(0,x.length()>10?10:x.length()));
		            	String y = Double.toString(geoFeaturesManager.getY(event.y));
		            	wlCoordY.setText(y.substring(0,y.length()>10?10:y.length()));
		            	break;
		            case SWT.MouseDoubleClick:
		            	if(wToolItemPan.getSelection())geoFeaturesManager.recenter(event.x, event.y);
		            	break;
		            case SWT.MouseEnter:
		            	if(wToolItemPan.getSelection()){
		            		ImageData icon = GUIResource.getInstance().getImage(PAN_ICON).getImageData();
		            		wGeoCanvas.setCursor(new Cursor(wGeoCanvas.getDisplay(), icon, icon.width/2, icon.height/2));
		            	}else if(wToolItemZoomIn.getSelection()){
		            		ImageData icon = GUIResource.getInstance().getImage(ZOOMIN_ICON).getImageData();
		            		wGeoCanvas.setCursor(new Cursor(wGeoCanvas.getDisplay(), icon, icon.width/3, icon.height/3));
		            	}else if(wToolItemZoomOut.getSelection()){
		            		ImageData icon = GUIResource.getInstance().getImage(ZOOMOUT_ICON).getImageData();
		            		wGeoCanvas.setCursor(new Cursor(wGeoCanvas.getDisplay(), icon, icon.width/3, icon.height/3));
		            	}else if(wToolItemGetInfos.getSelection()){
		            		ImageData icon = GUIResource.getInstance().getImage(GETINFOSCURSOR_ICON).getImageData();
		            		wGeoCanvas.setCursor(new Cursor(wGeoCanvas.getDisplay(), icon, 0, 0));
		            	}
		            	break;
		            case SWT.MouseExit:
		            	wlCoordX.setText("                              ");  
		            	wlCoordY.setText("                              ");  
		            	pan = false;
		            	startingPoint = null;
		            	wGeoCanvas.setCursor(new Cursor(wGeoCanvas.getDisplay(), SWT.CURSOR_ARROW));
		            	break;
		        }			     
			}        	         
        };
        
        wGeoCanvas.addListener(SWT.MouseDown, lsMouseListener);
        wGeoCanvas.addListener(SWT.MouseUp, lsMouseListener);
        wGeoCanvas.addListener(SWT.MouseDoubleClick, lsMouseListener);
        wGeoCanvas.addListener(SWT.MouseEnter, lsMouseListener);
        wGeoCanvas.addListener(SWT.MouseExit, lsMouseListener);
        wGeoCanvas.addListener(SWT.MouseMove, lsMouseListener);
        		
        wGeoCanvas.addListener(SWT.Paint, new Listener(){   	
			public void handleEvent(Event event) {
	        	if (geoFeaturesManager.getGeometryCount() <= 0)return;
	        	Rectangle clientArea = event.display.getClientArea();
	        	event.gc.setClipping(clientArea);	            
	        	SWTMapRenderer mapRenderer = new SWTMapRenderer(event.gc);
	            mapRenderer.render(geoFeaturesManager.getMapContext(), geoFeaturesManager.getEnvelope());
			}        	
        });
        
        wGeoCanvas.addListener(SWT.Resize, new Listener(){   	
			public void handleEvent(Event event) {
				geoFeaturesManager.setCanvasSize(((Canvas)event.widget).getSize().x, ((Canvas)event.widget).getSize().y);
			}        	
        });    
        
        Iterator<LayerCollection> it = layers.iterator();
        while(it.hasNext())it.next().addLayerListViewer(geoFeaturesManager);
                 
        wlX = new Label(geoTab, SWT.NONE);
        wlX.setText("X:");      
        props.setLook(wlX);
        fdlX = new FormData();
        fdlX.left = new FormAttachment(wLayerControl.getControl(), margin);
        fdlX.top = new FormAttachment(wGeoCanvas, margin);
        wlX.setLayoutData(fdlX);
        
        wlCoordX = new Label(geoTab, SWT.LEFT);
        wlCoordX.setText("                              ");      
        props.setLook(wlCoordX);
        fdlCoordX = new FormData();
        fdlCoordX.left = new FormAttachment(wlX, margin);
        fdlCoordX.top = new FormAttachment(wGeoCanvas, margin);
        wlCoordX.setLayoutData(fdlCoordX);
        
        wlY = new Label(geoTab, SWT.NONE);
        wlY.setText("Y:");      
        props.setLook(wlY);
        fdlY = new FormData();
        fdlY.left = new FormAttachment(wlCoordX, 20);
        fdlY.top = new FormAttachment(wGeoCanvas, margin);
        wlY.setLayoutData(fdlY);
        
        wlCoordY = new Label(geoTab, SWT.LEFT);
        wlCoordY.setText("                              ");      
        props.setLook(wlCoordY);
        fdlCoordY = new FormData();
        fdlCoordY.left = new FormAttachment(wlY, margin);
        fdlCoordY.top = new FormAttachment(wGeoCanvas, margin);
        wlCoordY.setLayoutData(fdlCoordY);     
        
        wInfoTable = new Table(geoTab,SWT.BORDER);
        wInfoTable.setHeaderVisible(true);
        fdInfoTable =new FormData();
        fdInfoTable.left = new FormAttachment(30,margin);
        fdInfoTable.top = new FormAttachment(wlX,margin);
        fdInfoTable.right = new FormAttachment(100, 0);
        fdInfoTable.bottom = new FormAttachment(100, -margin);       
        wInfoTable.setLayoutData(fdInfoTable);
               
        for (int i = 0; i < rowMeta.size(); i++){       	
        	TableColumn col = new TableColumn(wInfoTable,SWT.BORDER);
        	ValueMetaInterface v = rowMeta.getValueMeta(i);
        	col.setText(v.getName());
        	col.setWidth(100);
        }
        /////////////////
	    // End of geographic Tab
	    ////////////////////
	    
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
		
        BaseStepDialog.setSize(shell);

        shell.open();
     
        while (!shell.isDisposed()){
            if (!display.readAndDispatch()) display.sleep();
        }
    }

    public void deselectGeoToolBarItems(){
    	for (int i=0;i<wGeoToolBar.getItemCount();i++){
    		wGeoToolBar.getItem(i).setSelection(false);
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
