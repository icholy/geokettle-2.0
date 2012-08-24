package org.pentaho.di.ui.trans.steps.wpsclient;

import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.TableItem;

public class ComboSelectionListener extends SelectionAdapter{	
	private CCombo combo;
	private TableItem item;
	private int index;
	
	public ComboSelectionListener(CCombo combo, int index, TableItem item){
		this.combo = combo;
		this.index = index;
		this.item = item;
	}
	
	public void widgetSelected(SelectionEvent evt) {
		item.setText(index, combo.getText());
	}
}
