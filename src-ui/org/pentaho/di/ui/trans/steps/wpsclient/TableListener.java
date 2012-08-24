package org.pentaho.di.ui.trans.steps.wpsclient;

import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.pentaho.di.core.Const;
import org.pentaho.di.trans.steps.wpsclient.parameter.Parameter;

public class TableListener implements Listener{	
	private Table table;
	private Display display;
	private Shell shell;
	private Shell tip;
	private Label label;
	private Map<String, Parameter> parameters;
	private Map<String, Control> controls;
	private String selectedParamName;
	
	public TableListener(Table table, Display display, Shell shell, Map<String, Parameter> parameters){
		this.shell = shell;
		this.display = display;
		this.table = table;
		this.tip = null;
		this.label = null;
		selectedParamName = "";
	}
	
	public void setControls(Map<String, Control> controls){
		this.controls = controls;
	}
	
	public void setParameters(Map<String, Parameter> parameters){
		this.parameters = parameters;
	}
	
	@Override
	public void handleEvent(Event event){
		Listener labelListener = new Listener(){
		    public void handleEvent(Event event){
		        Label label = (Label) event.widget;
		        Shell shell = label.getShell();
		        switch (event.type) {
			        case SWT.MouseDown:
			          Event e = new Event();
			          e.item = (TableItem) label.getData("_TABLEITEM");
			          table.setSelection(new TableItem[]{(TableItem) e.item });
			          table.notifyListeners(SWT.Selection, e);
			          shell.dispose();
			          table.setFocus();
			          break;
			        case SWT.MouseExit:
			          shell.dispose();
			          break;
			        default:
						break;
		        }
		    }
		};
		      
		switch (event.type){
			case SWT.MouseHover:
				TableItem item = table.getItem(new Point(event.x, event.y));
				if (item != null && !Const.isEmpty(item.getText(1))){
					Parameter param = parameters.get(item.getText(1));
					if(param!=null && !Const.isEmpty(param.getAbstract())) {						
	        	  		if (tip != null && !tip.isDisposed())
	            			tip.dispose();
	        			tip = new Shell(shell, SWT.ON_TOP | SWT.TOOL);
	        			tip.setLayout(new FillLayout());
	        			label = new Label(tip, SWT.NONE);
	        			label.setForeground(display.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
	        			label.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
	        			label.setData("_TABLEITEM", item);
	        			label.setText(parameters.get(item.getText(1)).getAbstract());
	        			label.addListener(SWT.MouseExit, labelListener);
	        			label.addListener(SWT.MouseDown, labelListener);
	        			Point size = tip.computeSize(SWT.DEFAULT, SWT.DEFAULT);
	        			Rectangle rect = item.getBounds(0);
	        			Point pt = table.toDisplay(rect.x, rect.y);
	        			tip.setBounds(pt.x, pt.y, size.x, size.y);
	        			tip.setVisible(true);
					}
      		    }
				break;
			case SWT.KeyDown:
				if(event.keyCode == SWT.DEL && !Const.isEmpty(selectedParamName)){
					parameters.remove(selectedParamName);
					if(controls.containsKey(selectedParamName)){
						controls.get(selectedParamName).dispose();
						controls.remove(selectedParamName);						
					}
					selectedParamName = "";
				}				
				break;
			default:
				break;
		}
	}
	
	public void setSelection(String selectedParamName){
		this.selectedParamName = selectedParamName;
	}
}

