package org.pentaho.di.ui.trans.steps.spatialanalysis;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.spatialanalysis.Messages;
import org.pentaho.di.trans.steps.spatialanalysis.SpatialAnalysisMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

public class SpatialAnalysisDialog extends BaseStepDialog implements StepDialogInterface
{
	private Label       		wlReference;
	private CCombo      		wReference;
	private FormData    		fdlReference, fdReference;

	private Label 				wlReferenceField;
	private CCombo 				wReferenceField;
	
	private Label       		wlCompare;
	private CCombo      		wCompare;
	private FormData    		fdlCompare, fdCompare;
	
	private Label       		wlCompress;
    private Button      		wCompress;
    private FormData     		fdlCompress, fdCompress;
    
    private Label       		wlOneRow;
    private Button      		wOneRow;
    private FormData     		fdlOneRow, fdOneRow;
    
    private Label       		wlAttributes;
    private Button      		wAttributes;
    private FormData     		fdlAttributes, fdAttributes;
	
	private Label 				wlCompareField;
	private CCombo 				wCompareField;
    	
	private Label       		wlAnalysis;
	private CCombo      		wAnalysis;
	private FormData    		fdlAnalysis, fdAnalysis;    
    
    private Label       		wlResult;
	private Text        		wResult;
	private FormData    		fdlResult, fdResult;
	
	private Label       		wlDistField;
	private Text       			wDistField;
	private FormData    		fdlDistField, fdDistField;

	private SpatialAnalysisMeta input;
	
	public SpatialAnalysisDialog(Shell parent, Object in, TransMeta tr, String sname){
		super(parent, (BaseStepMeta)in, tr, sname);
		input=(SpatialAnalysisMeta)in;
    }
	
	public String [] getPreviousSteps(){
		return transMeta.getPrevStepNames(stepname);
	}
	
	public void displayFields(){ //enable/disable appropriate fields
		if (wAnalysis.getText().equals(Messages.getString("SpatialAnalysisMeta.AnalysisType.BUFFER"))) {
    		setDualOptions(false);
    		wDistField.setEnabled(true);
        }else if (wAnalysis.getText().equals(Messages.getString("SpatialAnalysisMeta.AnalysisType.INT_POINT"))) {
        	setDualOptions(false);
        }else if (wAnalysis.getText().equals(Messages.getString("SpatialAnalysisMeta.AnalysisType.ENVELOPE"))) {
        	setDualOptions(false);
        }else if (wAnalysis.getText().equals(Messages.getString("SpatialAnalysisMeta.AnalysisType.CENTROID"))) {
        	setDualOptions(false);
        }else if (wAnalysis.getText().equals(Messages.getString("SpatialAnalysisMeta.AnalysisType.BOUNDARY"))) {
        	setDualOptions(false);
        }else if (wAnalysis.getText().equals(Messages.getString("SpatialAnalysisMeta.AnalysisType.CONVEX_HULL"))) {
        	setDualOptions(false);
        }else if (wAnalysis.getText().equals(Messages.getString("SpatialAnalysisMeta.AnalysisType.REVERSE"))) {
        	setDualOptions(false);
        }else{
        	setDualOptions(true);  
        	wDistField.setEnabled(false);
        	checkPreviousSteps();
        }
	}
	
	public void setDualOptions(boolean bool){
		wDistField.setEnabled(bool);
		wCompare.setEnabled(bool);
    	wCompareField.setEnabled(bool);
    	wOneRow.setEnabled(bool);
		wAttributes.setEnabled(bool);
	}
	
	public void checkPreviousSteps(){ //clear fields if step/hop has been deteled/disabled
		String previousSteps[] = getPreviousSteps();
		boolean refStepFound = false;
		boolean compStepFound = false;
		for(int i = 0;i<previousSteps.length;i++){
			if(wCompare.getText().equals(previousSteps[i]))
				compStepFound = true;			
			if(wReference.getText().equals(previousSteps[i]))
				refStepFound = true;
		}
		
		if(!compStepFound){			
			wCompare.removeAll();
			wCompareField.removeAll();
			wCompare.setItems( previousSteps );
			getCompareFields();
		}
				
		if(!refStepFound){
			wReference.removeAll();
			wReferenceField.removeAll();	
			wReference.setItems( previousSteps );
			getReferenceFields();
		}
	}
	
	public String open(){
		Shell parent = getParent();
		Display display = parent.getDisplay();

        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX );
 		props.setLook(shell);
        setShellImage(shell, input);
		
		ModifyListener lsMod = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				input.setChanged();
			}
		};
		
		ModifyListener lsModRef = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				input.setChanged();
				getReferenceFields();
			}
		};
		
		ModifyListener lsModCom = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				input.setChanged();
				getCompareFields();
			}
		};
		
		backupChanged = input.hasChanged();
			
		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(Messages.getString("SpatialAnalysisDialog.Shell.Label")); //$NON-NLS-1$
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(Messages.getString("SpatialAnalysisDialog.Stepname.Label")); //$NON-NLS-1$
 		props.setLook(wlStepname);
		fdlStepname=new FormData();
		fdlStepname.left = new FormAttachment(0, 0);
		fdlStepname.right= new FormAttachment(middle, -margin);
		fdlStepname.top  = new FormAttachment(0, margin*2);
		wlStepname.setLayoutData(fdlStepname);
		wStepname=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wStepname.setText(stepname);
 		props.setLook(wStepname);
		wStepname.addModifyListener(lsMod);
		fdStepname=new FormData();
		fdStepname.left = new FormAttachment(middle, 0);
		fdStepname.top  = new FormAttachment(0, margin);
		fdStepname.right= new FormAttachment(100, 0);
		wStepname.setLayoutData(fdStepname);

        // Get the previous steps...
        String previousSteps[] = getPreviousSteps();
               
        // Analysis
        wlAnalysis = new Label(shell, SWT.RIGHT);
        wlAnalysis.setText(Messages.getString("SpatialAnalysisDialog.Analysis.Label"));
        props.setLook(wlAnalysis);
        fdlAnalysis = new FormData();
        fdlAnalysis.left = new FormAttachment(0, 0);
        fdlAnalysis.right = new FormAttachment(middle, -margin);
        fdlAnalysis.top = new FormAttachment(wStepname, margin*2);
        wlAnalysis.setLayoutData(fdlAnalysis);
        wAnalysis = new CCombo(shell, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
        wAnalysis.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(SelectionEvent event){
            	displayFields();
            }
        });
        wAnalysis.setEditable(false);
        wAnalysis.add(Messages.getString("SpatialAnalysisMeta.AnalysisType.UNION"));
        wAnalysis.add(Messages.getString("SpatialAnalysisMeta.AnalysisType.INTERSECTION"));
        wAnalysis.add(Messages.getString("SpatialAnalysisMeta.AnalysisType.BUFFER"));
        wAnalysis.add(Messages.getString("SpatialAnalysisMeta.AnalysisType.SYM_DIFF"));
        wAnalysis.add(Messages.getString("SpatialAnalysisMeta.AnalysisType.INT_POINT"));
        wAnalysis.add(Messages.getString("SpatialAnalysisMeta.AnalysisType.ENVELOPE"));
        wAnalysis.add(Messages.getString("SpatialAnalysisMeta.AnalysisType.CENTROID"));
        wAnalysis.add(Messages.getString("SpatialAnalysisMeta.AnalysisType.BOUNDARY"));
        wAnalysis.add(Messages.getString("SpatialAnalysisMeta.AnalysisType.DIFFERENCE"));
        wAnalysis.add(Messages.getString("SpatialAnalysisMeta.AnalysisType.CONVEX_HULL"));
        wAnalysis.add(Messages.getString("SpatialAnalysisMeta.AnalysisType.REVERSE"));
        wAnalysis.select(0); 
        props.setLook(wAnalysis);
        fdAnalysis = new FormData();
        fdAnalysis.left = new FormAttachment(middle, 0);
        fdAnalysis.top = new FormAttachment(wStepname, margin);
        fdAnalysis.right = new FormAttachment(100, 0);
        wAnalysis.setLayoutData(fdAnalysis);	

		// Distance line...
		wlDistField=new Label(shell, SWT.RIGHT);
		wlDistField.setText(Messages.getString("SpatialAnalysisDialog.DistField.Label")); //$NON-NLS-1$
 		props.setLook(wlDistField);
		fdlDistField=new FormData();
		fdlDistField.left = new FormAttachment(0, 0);
		fdlDistField.right= new FormAttachment(middle, -margin);
		fdlDistField.top  = new FormAttachment(wAnalysis, margin*2);
		wlDistField.setLayoutData(fdlDistField);
		wDistField=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wDistField);
		wDistField.addModifyListener(lsMod);
		wDistField.setEnabled(false);
		fdDistField=new FormData();
		fdDistField.left = new FormAttachment(middle, 0);
		fdDistField.top  = new FormAttachment(wAnalysis, margin);
		fdDistField.right= new FormAttachment(50, 0);
		wDistField.setLayoutData(fdDistField);
        
        // Using compression for temporary files?
		wCompress=new Button(shell, SWT.CHECK);
		wCompress.setToolTipText(null);
	    props.setLook(wCompress);
	    fdCompress=new FormData();
	    fdCompress.right  = new FormAttachment(100, 0);
	    fdCompress.top   = new FormAttachment(wAnalysis, margin);
	    wCompress.setLayoutData(fdCompress);
        wlCompress=new Label(shell, SWT.RIGHT);
        wlCompress.setText(Messages.getString("SpatialAnalysisDialog.Compress.Label"));
        props.setLook(wlCompress);
        fdlCompress=new FormData();
        fdlCompress.right = new FormAttachment(wCompress, -margin);
        fdlCompress.top  = new FormAttachment(wAnalysis, margin*2);
        wlCompress.setLayoutData(fdlCompress);
       		
		//reference step
		wlReference=new Label(shell, SWT.RIGHT);
		wlReference.setText(Messages.getString("SpatialAnalysisDialog.Reference.Label")); //$NON-NLS-1$
 		props.setLook(wlReference);
		fdlReference=new FormData();
		fdlReference.left = new FormAttachment(0, 0);
		fdlReference.right= new FormAttachment(middle, -margin);
		fdlReference.top  = new FormAttachment(wDistField, margin*5);
		wlReference.setLayoutData(fdlReference);
		wReference=new CCombo(shell, SWT.BORDER );
		wReference.setEditable(false);
 		props.setLook(wReference);

		if (previousSteps!=null)
			wReference.setItems( previousSteps );
		
		wReference.addModifyListener(lsModRef);
		fdReference=new FormData();
		fdReference.left = new FormAttachment(middle, 0);
		fdReference.top  = new FormAttachment(wDistField, margin*4);
		fdReference.right= new FormAttachment(100, 0);
		wReference.setLayoutData(fdReference);

		//reference field
		wlReferenceField=new Label(shell, SWT.RIGHT);
		wlReferenceField.setText(Messages.getString("SpatialAnalysisDialog.ReferenceField.Label")); //$NON-NLS-1$
 		props.setLook(wlReferenceField);
		FormData fdlReferenceField = new FormData();
		fdlReferenceField.left = new FormAttachment(0, 0);
		fdlReferenceField.right= new FormAttachment(middle, -margin);
		fdlReferenceField.top  = new FormAttachment(wReference, margin*2);
		wlReferenceField.setLayoutData(fdlReferenceField);
		wReferenceField=new CCombo(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wReferenceField.addModifyListener(lsMod);
		wReferenceField.setEditable(false);
 		props.setLook(wReferenceField);
		FormData fdReferenceField = new FormData();
		fdReferenceField.left = new FormAttachment(middle, 0);
		fdReferenceField.right= new FormAttachment(100, 0);
		fdReferenceField.top  = new FormAttachment(wReference, margin);
		wReferenceField.setLayoutData(fdReferenceField);
		
		//compare step
		wlCompare=new Label(shell, SWT.RIGHT);
		wlCompare.setText(Messages.getString("SpatialAnalysisDialog.Compare.Label")); //$NON-NLS-1$
 		props.setLook(wlCompare);
		fdlCompare=new FormData();
		fdlCompare.left = new FormAttachment(0, 0);
		fdlCompare.right= new FormAttachment(middle, -margin);
		fdlCompare.top  = new FormAttachment(wReferenceField, margin*5);
		wlCompare.setLayoutData(fdlCompare);
		wCompare=new CCombo(shell, SWT.BORDER );
		wCompare.setEditable(false);
 		props.setLook(wCompare);

        if (previousSteps!=null)
            wCompare.setItems( previousSteps );	
        
		wCompare.addModifyListener(lsModCom);
		fdCompare=new FormData();
        fdCompare.top  = new FormAttachment(wReferenceField, margin*4);
		fdCompare.left = new FormAttachment(middle, 0);
		fdCompare.right= new FormAttachment(100, 0);
		wCompare.setLayoutData(fdCompare);
		
		//compare field
		wlCompareField=new Label(shell, SWT.RIGHT);
		wlCompareField.setText(Messages.getString("SpatialAnalysisDialog.CompareField.Label")); //$NON-NLS-1$
 		props.setLook(wlCompareField);
		FormData fdlCompareField = new FormData();
		fdlCompareField.left = new FormAttachment(0, 0);
		fdlCompareField.right= new FormAttachment(middle, 0);
		fdlCompareField.top  = new FormAttachment(wCompare, margin*2);
		wlCompareField.setLayoutData(fdlCompareField);
		wCompareField=new CCombo(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wCompareField.addModifyListener(lsMod);
		wCompareField.setEditable(false);
 		props.setLook(wCompareField);
		FormData fdCompareField = new FormData();
		fdCompareField.left = new FormAttachment(middle, 0);
		fdCompareField.right= new FormAttachment(100, 0);
		fdCompareField.top  = new FormAttachment(wCompare, margin);
		wCompareField.setLayoutData(fdCompareField);
		
		// Execute analysis on the whole set (rowset 2)?		        
        wOneRow=new Button(shell, SWT.CHECK);
		wOneRow.setToolTipText(null);
	    props.setLook(wOneRow);
	    fdOneRow=new FormData();
	    fdOneRow.right  = new FormAttachment(100, 0);
	    fdOneRow.top   = new FormAttachment(wCompareField, margin*4);
	    wOneRow.setLayoutData(fdOneRow);
	    wlOneRow=new Label(shell, SWT.RIGHT);
        wlOneRow.setText(Messages.getString("SpatialAnalysisDialog.OneRow.Label"));
        props.setLook(wlOneRow);
        fdlOneRow=new FormData();
        fdlOneRow.right = new FormAttachment(wOneRow, -margin);
        fdlOneRow.top  = new FormAttachment(wCompareField, margin*5);
        wlOneRow.setLayoutData(fdlOneRow);
        
		// Keep attributes of compared rows?		        
        wAttributes=new Button(shell, SWT.CHECK);
		wAttributes.setToolTipText(null);
	    props.setLook(wAttributes);
	    fdAttributes=new FormData();
	    fdAttributes.right  = new FormAttachment(100, 0);
	    fdAttributes.top   = new FormAttachment(wOneRow, margin);
	    wAttributes.setLayoutData(fdAttributes);
	    wlAttributes=new Label(shell, SWT.RIGHT);
        wlAttributes.setText(Messages.getString("SpatialAnalysisDialog.Attributes.Label"));
        props.setLook(wlAttributes);
        fdlAttributes=new FormData();
        fdlAttributes.right = new FormAttachment(wAttributes, -margin);
        fdlAttributes.top  = new FormAttachment(wOneRow, margin*2);
        wlAttributes.setLayoutData(fdlAttributes);
		
		// Result line...
        wResult=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wResult);
		wResult.addModifyListener(lsMod);
		fdResult=new FormData();
		fdResult.left = new FormAttachment(70, 0);
		fdResult.top  = new FormAttachment(wAttributes, margin*4);
		fdResult.right= new FormAttachment(100, 0);
		wResult.setLayoutData(fdResult);
		wlResult=new Label(shell, SWT.RIGHT);
		wlResult.setText(Messages.getString("SpatialAnalysisDialog.Result.Label")); //$NON-NLS-1$
 		props.setLook(wlResult);
		fdlResult=new FormData();
		fdlResult.right= new FormAttachment(wResult, -margin);
		fdlResult.top  = new FormAttachment(wAttributes, margin*5);
		wlResult.setLayoutData(fdlResult);
        
		// Some buttons
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(Messages.getString("System.Button.OK")); //$NON-NLS-1$
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(Messages.getString("System.Button.Cancel")); //$NON-NLS-1$

		setButtonPositions(new Button[] { wOK, wCancel }, margin, null);

		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		
		wCancel.addListener(SWT.Selection, lsCancel);
		wOK.addListener    (SWT.Selection, lsOK    );
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

		// Set the shell size, based upon previous time...
		setSize();
		
		//fill entries
		getData();
		
		//display appropriate fields
		displayFields();
		
		input.setChanged(backupChanged);
		
		shell.open();
		while (!shell.isDisposed()){
				if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}
	
	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData(){
		wAnalysis.select(input.getSpatialAnalysisByDesc());
		if (input.getDistField()!=null) wDistField.setText(input.getDistField());
		if (input.getResultFieldName()!=null) wResult.setText(input.getResultFieldName());
		if (input.getReferenceStepName() != null) wReference.setText(input.getReferenceStepName());
		if (input.getCompareStepName() != null) wCompare.setText(input.getCompareStepName());
		wCompress.setSelection(input.getCompressFiles());
		wOneRow.setSelection(input.getOneRow());
		wAttributes.setSelection(input.getAttributes());
		wReferenceField.setText(Const.NVL(input.getReferenceField(), ""));
		wCompareField.setText(Const.NVL(input.getCompareField(), ""));      
        wStepname.selectAll();
	}
	
	private void cancel(){
		
		stepname=null;
		input.setChanged(backupChanged);
		dispose();
	}
	
	private void ok(){		
		if (Const.isEmpty(wStepname.getText())) return;

		input.setReferenceStepMeta(transMeta.findStep(wReference.getText()));
		input.setCompareStepMeta(transMeta.findStep(wCompare.getText()));                  
		input.setReferenceField(wReferenceField.getText());
		input.setCompareField(wCompareField.getText());
        input.setResultFieldName(wResult.getText());
        input.setDistField(wDistField.getText());
        input.setCompressFiles(wCompress.getSelection());
        input.setOneRow(wOneRow.getSelection());
        input.setAttributes(wAttributes.getSelection());
        if(wAnalysis.getSelectionIndex()<0)
			input.setSpatialAnalysisType(0); 
		else
			input.setSpatialAnalysisType(wAnalysis.getSelectionIndex());
        
		stepname = wStepname.getText(); // return value
		
		dispose();
	}
    
    private void getReferenceFields(){    	
    	try{
            StepMeta stepMeta = transMeta.findStep(wReference.getText());
            if (stepMeta!=null){
            	RowMetaInterface prev = transMeta.getStepFields(stepMeta);
            	if (prev!=null){
	            	String[] fieldNames = prev.getFieldNames();
	        		wReferenceField.setItems(fieldNames);
                } 
            }
        }
        catch(KettleException e){
            new ErrorDialog(shell, Messages.getString("SpatialAnalysisDialog.ErrorGettingFields.DialogTitle"), Messages.getString("SpatialAnalysisDialog.ErrorGettingFields.DialogMessage"), e); //$NON-NLS-1$ //$NON-NLS-2$
        } 
    }
    
    
    private void getCompareFields(){
	    try{
            StepMeta stepMeta = transMeta.findStep(wCompare.getText());
            if (stepMeta!=null){
            	RowMetaInterface prev = transMeta.getStepFields(stepMeta);
            	if (prev!=null){
	            	String[] fieldNames = prev.getFieldNames();
	        		wCompareField.setItems(fieldNames);	        			        		
                } 
            }
        }
        catch(KettleException e){
            new ErrorDialog(shell, Messages.getString("SpatialAnalysisDialog.ErrorGettingFields.DialogTitle"), Messages.getString("SpatialAnalysisDialog.ErrorGettingFields.DialogMessage"), e); //$NON-NLS-1$ //$NON-NLS-2$
        }    	
    }
}