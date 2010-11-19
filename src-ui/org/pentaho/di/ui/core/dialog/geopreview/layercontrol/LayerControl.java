package org.pentaho.di.ui.core.dialog.geopreview.layercontrol;

import java.util.Arrays;
//import org.eclipse.jface.viewers.CellEditor;
//import org.eclipse.jface.viewers.CheckStateChangedEvent;
//import org.eclipse.jface.viewers.CheckboxCellEditor;
//import org.eclipse.jface.viewers.CheckboxTableViewer;
//import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelection;
//import org.eclipse.jface.viewers.IStructuredContentProvider;
//import org.eclipse.jface.viewers.TextCellEditor;
//import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
//import org.eclipse.swt.widgets.Event;
//import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Tree;
//import org.eclipse.swt.widgets.Text;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ColorCellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Color;
import org.pentaho.di.ui.core.dialog.geopreview.Layer;
import org.pentaho.di.ui.core.dialog.geopreview.LayerCollection;
//import org.pentaho.di.ui.core.dialog.geopreview.layercontrol.CheckBoxTreeViewerExample.MyTreeContentProvider;
import org.pentaho.di.ui.core.dialog.geopreview.Symbolisation;

/**
*
* @author mouattara & tbadard
* @since 22-03-2009
*/
public class LayerControl 
{
	// ---------------- Constants ----------------------
	//private final String COLUMN_0_TITLE = "Visible";
	private final String COLUMN_0_TITLE = "";
	private final String COLUMN_1_TITLE	= "Name";
	private final String COLUMN_2_TITLE	= "Style";
	private final int COLUMN_0_WIDTH = 40;
	private final int COLUMN_1_WIDTH = 150;
	private final int COLUMN_2_WIDTH = 50;
	// --------------------------------------------------


	
	// ---------------- Variables ----------------------
	// Top control of the layerControl (for FormLayout)
	Control topControl;
	// The table
	private Tree table;
	// The table wrapper
	private CheckboxTreeViewer tableViewer;
	
	// ArrayList of Layer
	LayerCollection layerList;	// Collection of layers
	
	// Set column names
	private String[] columnNames = new String[] {
										this.COLUMN_0_TITLE, 
										this.COLUMN_1_TITLE,
										this.COLUMN_2_TITLE
										};
	// --------------------------------------------------


	/**
	 * Constructor
	 * 
	 * @param parent Composite to which the control will be added
	 * 
	 */
	public LayerControl(Composite parent, LayerCollection layerList, Control topControl) 
	{
		this.layerList = layerList;
		this.topControl = topControl;
		
		this.addChildControls(parent);		
	}

	
	/**
	 * Create a new shell, add the widgets, open the shell
	 *
	 * @param parent Composite to which the control will be added
	 * 
	 */
	private void addChildControls(Composite composite) 
	{
		// Create the table 
		createTable(composite);
		
		// Create and setup the TableViewer
		createTableViewer();
		this.table.selectAll();
	}

	
	/**
	 * Create the Table
	 * 
	 * @param parent Composite to which the control will be added
	 * 
	 */
	private void createTable(Composite parent) 
	{
		int style = SWT.SINGLE | SWT.BORDER |  
					SWT.FULL_SELECTION | SWT.CHECK | SWT.HIDE_SELECTION;

		this.table = new Tree(parent, style);
		
        FormData fdLayerList = new FormData();
        fdLayerList.left = new FormAttachment(0, 0);
        fdLayerList.top = new FormAttachment(this.topControl, 0);
        fdLayerList.right = new FormAttachment(30, 0);
        fdLayerList.bottom = new FormAttachment(100, -50);
        this.table.setLayoutData(fdLayerList);
					
		//table.setLinesVisible(true);
		this.table.setHeaderVisible(true);
		
			
		// TODO
		// Add listener to column so they can be sorted by name when clicked 
		/*column.addSelectionListener(new SelectionAdapter() {
       	
			public void widgetSelected(SelectionEvent e) {
				tableViewer.setSorter(new ExampleTaskSorter(ExampleTaskSorter.NAME));
			}
		});*/
	}

	
	static class MyEditingSupport extends EditingSupport {
		private CellEditor editor;
		private Viewer myViewer;
		public MyEditingSupport(ColumnViewer viewer) {
		super(viewer);
		myViewer = viewer;
		editor = new TextCellEditor((Composite)viewer.getControl());
		}
		protected boolean canEdit(Object element) { return false; }
		protected CellEditor getCellEditor(Object element) { return editor; }
		protected Object getValue(Object element) {
			Layer current = (Layer)element;
		return current.getName();
		}
		protected void setValue(Object element, Object value) {
			Layer current = (Layer)element;
			current.setName((String)value);
			myViewer.refresh();
		}
	}
	
	static class MyEditingSupportStyle extends EditingSupport {
		private CellEditor editor;
		private Viewer myViewer;
		public MyEditingSupportStyle(ColumnViewer viewer) {
			super(viewer);
			myViewer = viewer;
			editor = new ColorCellEditor((Composite)viewer.getControl());
		}
		protected boolean canEdit(Object element) { return true; }
		protected CellEditor getCellEditor(Object element) { 

			if (!(element instanceof Symbolisation))
				return null;
			Symbolisation s = (Symbolisation)element;

			if (s.getStyleUsage()==Symbolisation.LineStroke){
				return new TextCellEditor((Composite)myViewer.getControl());
			} 
			if(s.getStyleUsage()==Symbolisation.Opacite){
				return new TextCellEditor((Composite)myViewer.getControl());
			}

			if((s.getStyleUsage()==Symbolisation.BackGroundColor)||(s.getStyleUsage()==Symbolisation.LineForeColor)){
				return editor;
			}
			else
				return null; 
		}
		protected Object getValue(Object element) {
			Symbolisation current = (Symbolisation)element;
			return current.getFeatureStyle();
		}
		protected void setValue(Object element, Object value) {
			double val=0;
			Symbolisation current = (Symbolisation)element;
			if (current.getStyleUsage()==Symbolisation.Opacite){
				try{
					val=Double.parseDouble((String)value);

					if (val>1)
						value=this.getValue(current); 
				}catch(Exception e){
					value=this.getValue(current);
				}
			}
			
			if (current.getStyleUsage()==Symbolisation.LineStroke){
				try{
					Integer.parseInt((String)value);
				}catch(Exception e){
					value=this.getValue(current);
				}
			}
			current.setFeatureStyle(value);
			current.getLayerParent().setVisible(current.getLayerParent().isVisible());
			myViewer.refresh();
		
		}
		
	}
	
		
	
	/**
	 * Create the TableViewer
	 * 
	 */
	private void createTableViewer() 
	{
		// Create the tableViewer
		this.tableViewer = new CheckboxTreeViewer(this.table);
		this.tableViewer.setUseHashlookup(true);
		//tableViewer.setColumnProperties(this.columnNames);
		
		
		TreeViewerColumn column = new TreeViewerColumn(tableViewer, SWT.CENTER);
		
		column.setLabelProvider(new ColumnLabelProvider() {
			public String getText(Object element) {
				if (element instanceof Layer) {
					Layer current = (Layer)element;
					
					return current.getName();
				} else { 
					return null;//element.toString();				
				}
			}
			
			
		
		});
		// TODO Require i18n
		column.getColumn().setText("Geometry columns");
		
		column.setEditingSupport(new MyEditingSupport(tableViewer));
		
		column = new TreeViewerColumn(tableViewer, SWT.CENTER);
		column.setLabelProvider(new ColumnLabelProvider() {
			public String getText(Object element) {
				if (element instanceof Symbolisation) {
					Symbolisation current = (Symbolisation)element;
					return current.getFeatureStyle().toString();
				} else {
					return null;
				}
			}
		});
		// TODO Require i18n
		column.getColumn().setText("Legend");
		column.setEditingSupport(new MyEditingSupportStyle(tableViewer));
		
		
		for (int i = 0, n = table.getColumnCount(); i < n; i++) {
			table.getColumn(i).setWidth(200);
		}
		// CheckStateListener
		this.tableViewer.addCheckStateListener(new LayerCheckStateListener()); 
		
		// TODO
		// Set the default sorter for the viewer 
		// tableViewer.setSorter(new ExampleTaskSorter(ExampleTaskSorter.DESCRIPTION));

		// ContentProvider
		this.tableViewer.setContentProvider(new MyTreeContentProvider(layerList, this.tableViewer));
		
		// Set the label provider 
		// Since the table will contains layer, this is a LayerLabelProvider
		//this.tableViewer.setLabelProvider(new LayerLabelProvider());
		
		// The input for the table viewer is the instance of LayerList
		this.tableViewer.setInput(this.layerList);
		
	}

	
	/**
	 * Return the column names in a collection
	 * 
	 * @return List  containing column names
	 * 
	 */
	public java.util.List getColumnNames() {
		return Arrays.asList(this.columnNames);
	}

	

	/**
	 * @return currently selected item
	 * 
	 */
	public ISelection getSelection() {
		return this.tableViewer.getSelection();
	}


	/**
	 * Return the parent composite
	 * 
	 */
	public Control getControl() {
		return this.table;
	}

}