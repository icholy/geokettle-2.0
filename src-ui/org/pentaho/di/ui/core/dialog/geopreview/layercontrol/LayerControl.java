package org.pentaho.di.ui.core.dialog.geopreview.layercontrol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ColorCellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;
import org.pentaho.di.ui.core.dialog.Messages;
import org.pentaho.di.ui.core.dialog.geopreview.Layer;
import org.pentaho.di.ui.core.dialog.geopreview.LayerCollection;
import org.pentaho.di.ui.core.dialog.geopreview.Symbolisation;
import org.pentaho.di.ui.core.util.geo.renderer.swt.LayerFactory;

/**
*
* @author mouattara, jmathieu & tbadard
* @since 22-03-2009
*/
public class LayerControl 
{	
	private Tree table;
	private FormData fd;
	
	private CheckboxTreeViewer tableViewer;
	
	private ArrayList<LayerCollection> layerList;
	
	private String[] columnNames = new String[] {"", Messages.getString("PreviewRowsDialog.LayerTreeViewer.ColumnName.Title"), Messages.getString("PreviewRowsDialog.LayerTreeViewer.ColumnStyle.Title")};

	public LayerControl(Composite parent, ArrayList<LayerCollection> layerList, FormData fd) {
		this.layerList = layerList;
		this.fd = fd;
		addChildControls(parent);		
	}
	
	private void addChildControls(Composite composite) {
		createTable(composite, fd);		
		createTableViewer();
		table.selectAll();
	}

	private void createTable(Composite parent, FormData fd) {
		table = new Tree(parent, SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION | SWT.CHECK | SWT.HIDE_SELECTION);		
        table.setLayoutData(fd);							
		table.setHeaderVisible(true);
	}

	static class StyleEditingSupport extends EditingSupport {
		private Viewer viewer;
		public StyleEditingSupport(ColumnViewer viewer) {
			super(viewer);
			this.viewer = viewer;
		}		
		protected boolean canEdit(Object element) { return true; }		
		protected CellEditor getCellEditor(Object element) { 
			if (!(element instanceof Symbolisation)) return null;
			
			Symbolisation s = (Symbolisation)element;
			if (s.getStyleUsage()==Symbolisation.LineStrokeWidth || s.getStyleUsage()==Symbolisation.PolygonStrokeWidth)
				return new TextCellEditor((Composite)viewer.getControl());			 
			if(s.getStyleUsage()==Symbolisation.PointOpacity || s.getStyleUsage()==Symbolisation.LineOpacity || s.getStyleUsage()==Symbolisation.PolygonOpacity || s.getStyleUsage()==Symbolisation.CollectionOpacity)
				return new TextCellEditor((Composite)viewer.getControl());			
			if(s.getStyleUsage()==Symbolisation.Radius)
				return new TextCellEditor((Composite)viewer.getControl());			
			if((s.getStyleUsage()==Symbolisation.PointColor)||(s.getStyleUsage()==Symbolisation.PolygonFillColor)||(s.getStyleUsage()==Symbolisation.LineStrokeColor) ||(s.getStyleUsage()==Symbolisation.PolygonStrokeColor) ||(s.getStyleUsage()==Symbolisation.CollectionColor))			
				return new ColorCellEditor((Composite)viewer.getControl());;							
			return null; 
		}
		protected Object getValue(Object element) {
			return ((Symbolisation)element).getFeatureStyle();
		}		
		protected void setValue(Object element, Object value) {
			double val=0;
			Symbolisation current = (Symbolisation)element;
			if (current.getStyleUsage()==Symbolisation.PolygonOpacity || current.getStyleUsage()==Symbolisation.PointOpacity || current.getStyleUsage()==Symbolisation.LineOpacity || current.getStyleUsage()==Symbolisation.CollectionOpacity){
				try{
					val=Double.parseDouble((String)value);
					if (val>1) value=this.getValue(current); 
				}catch(Exception e){
					value=this.getValue(current);
				}
			}			
			if (current.getStyleUsage()==Symbolisation.LineStrokeWidth || current.getStyleUsage()==Symbolisation.PolygonStrokeWidth || current.getStyleUsage()==Symbolisation.Radius){
				try{
					Integer.parseInt((String)value);
				}catch(Exception e){
					value=this.getValue(current);
				}
			}			
			if(!current.isCustom()){
				current.setIsCustom(true);
				((CheckboxTreeViewer)viewer).setChecked(current, current.isCustom());
			}
			current.setFeatureStyle(value);						
			current.updateParent();
			viewer.refresh();	
		}		
	}

	private void createTableViewer() {
		tableViewer = new CheckboxTreeViewer(table);
		tableViewer.setUseHashlookup(true);				
		
		TreeViewerColumn cNames = new TreeViewerColumn(tableViewer, SWT.CENTER);		
		cNames.getColumn().setText(Messages.getString("PreviewRowsDialog.LayerTreeViewer.GeometryFields"));	

		TreeViewerColumn cLegend = new TreeViewerColumn(tableViewer, SWT.CENTER);
		cLegend.getColumn().setText(Messages.getString("PreviewRowsDialog.LayerTreeViewer.Legend"));
		cLegend.setEditingSupport(new StyleEditingSupport(tableViewer));
			
		for (int i = 0, n = table.getColumnCount(); i < n; i++) {
			table.getColumn(i).setWidth(200);
		}

		tableViewer.addCheckStateListener(new ICheckStateListener(){
			public void checkStateChanged(CheckStateChangedEvent event) {
				if (event.getElement() instanceof LayerCollection) {
			    	((LayerCollection)event.getElement()).setVisible(event.getChecked(),true);
				}	
				if (event.getElement() instanceof Layer){ 
					((Layer)event.getElement()).setVisible(event.getChecked());
					if(event.getChecked()){
						LayerCollection lc = ((Layer)event.getElement()).getLayerCollectionParent();
						if(!lc.isVisible()){
							lc.setVisible(event.getChecked(), false);
							tableViewer.setChecked(lc, event.getChecked());
						}
					}
				}
				if (event.getElement() instanceof Symbolisation) {	
					Symbolisation sym = (Symbolisation)event.getElement();
					sym.setIsCustom(event.getChecked()); 
					if(!event.getChecked()){
						if(Symbolisation.usage[sym.getStyleUsage()].equals(Symbolisation.STROKECOLOR)||Symbolisation.usage[sym.getStyleUsage()].equals(Symbolisation.FILLCOLOR)||Symbolisation.usage[sym.getStyleUsage()].equals(Symbolisation.COLOR))
							sym.setFeatureStyle((Object)LayerFactory.getDefaultColor());
						if(Symbolisation.usage[sym.getStyleUsage()].equals(Symbolisation.STROKEWIDTH))
							sym.setFeatureStyle(LayerFactory.DEFAULT_STROKE_WIDTH);
						if(Symbolisation.usage[sym.getStyleUsage()].equals(Symbolisation.RADIUS))
							sym.setFeatureStyle(LayerFactory.DEFAULT_RADIUS);
						if(Symbolisation.usage[sym.getStyleUsage()].equals(Symbolisation.OPACITY))
							sym.setFeatureStyle(LayerFactory.DEFAULT_OPACITY);						
					}	
					sym.updateParent();
					tableViewer.refresh();
				}			
			}
		}); 
		tableViewer.setContentProvider(new LayerTreeContentProvider(layerList, tableViewer));
		tableViewer.setLabelProvider(new LayerLabelProvider(tableViewer));
		tableViewer.setInput(layerList);
		tableViewer.setAllChecked(true);
	}

	public List<String> getColumnNames() {
		return Arrays.asList(columnNames);
	}

	public ISelection getSelection() {
		return tableViewer.getSelection();
	}

	public Control getControl() {
		return table;
	}
}