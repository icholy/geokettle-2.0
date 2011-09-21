package org.pentaho.di.ui.trans.steps.spatialanalysis;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
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

public class SpatialAnalysisDialog extends BaseStepDialog implements StepDialogInterface{
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
	
	private Label       		wlSide;
	private CCombo      		wSide;
	private FormData    		fdlSide, fdSide;
	
	private Label       		wlCap;
	private CCombo      		wCap;
	private FormData    		fdlCap, fdCap;
	
	private Label       		wlJoin;
	private CCombo      		wJoin;
	private FormData    		fdlJoin, fdJoin;

	private SpatialAnalysisMeta input;
	
	public SpatialAnalysisDialog(Shell parent, Object in, TransMeta tr, String sname){
		super(parent, (BaseStepMeta)in, tr, sname);
		input=(SpatialAnalysisMeta)in;
    }
	
	public void displayFields(){ //enable/disable appropriate fields
		if (wAnalysis.getText().equals(Messages.getString("SpatialAnalysisMeta.AnalysisType.BUFFER"))) {
    		setDualOptions(false);
    		wDistField.setEnabled(true);
    		wCap.setEnabled(true);
    		wSide.setEnabled(true);
    		wJoin.setEnabled(true);
        }else if (wAnalysis.getText().equals(Messages.getString("SpatialAnalysisMeta.AnalysisType.INT_POINT")))
        	setDualOptions(false);
        else if (wAnalysis.getText().equals(Messages.getString("SpatialAnalysisMeta.AnalysisType.ENVELOPE")))
        	setDualOptions(false);
        else if (wAnalysis.getText().equals(Messages.getString("SpatialAnalysisMeta.AnalysisType.CENTROID")))
        	setDualOptions(false);
        else if (wAnalysis.getText().equals(Messages.getString("SpatialAnalysisMeta.AnalysisType.BOUNDARY")))
        	setDualOptions(false);
        else if (wAnalysis.getText().equals(Messages.getString("SpatialAnalysisMeta.AnalysisType.CONVEX_HULL")))
        	setDualOptions(false);
        else if (wAnalysis.getText().equals(Messages.getString("SpatialAnalysisMeta.AnalysisType.REVERSE"))) 
        	setDualOptions(false);
        else{
        	setDualOptions(true);  
        	wDistField.setEnabled(false);
        }
	}
	
	public void setDualOptions(boolean bool){
		if(!bool){
	    	wCompare.removeAll();
	    	wCompareField.removeAll();
	    	wCompress.setSelection(bool);
	    	wOneRow.setSelection(bool);
    	}
		wDistField.setEnabled(false);
		wCap.setEnabled(false);
		wSide.setEnabled(false);
		wJoin.setEnabled(false);
		wCompare.setEnabled(bool);
    	wCompareField.setEnabled(bool);
    	wOneRow.setEnabled(bool); 
    	wCompress.setEnabled(bool);
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
		fdDistField.right = new FormAttachment(50, 0);
		wDistField.setLayoutData(fdDistField);
        
		//Buffer side
		wlSide=new Label(shell, SWT.RIGHT);
		wlSide.setText(Messages.getString("SpatialAnalysisDialog.Side.Label")); //$NON-NLS-1$
 		props.setLook(wlSide);
		fdlSide=new FormData();
		fdlSide.left = new FormAttachment(middle, 75);
		fdlSide.right = new FormAttachment(middle, 200);
		fdlSide.top = new FormAttachment(wAnalysis, margin*2);
		wlSide.setLayoutData(fdlSide);
		wSide=new CCombo(shell, SWT.BORDER );
		wSide.setEditable(false);
 		props.setLook(wSide);
		
		wSide.addModifyListener(lsMod);
		fdSide=new FormData();
		fdSide.left = new FormAttachment(wlSide, margin);
		fdSide.top = new FormAttachment(wAnalysis, margin);
		fdSide.right = new FormAttachment(100, 0);
		wSide.setLayoutData(fdSide);
		wSide.setItems(SpatialAnalysisMeta.bufferSides);
		
		//Buffer cap style
		wlCap=new Label(shell, SWT.RIGHT);
		wlCap.setText(Messages.getString("SpatialAnalysisDialog.Cap.Label")); //$NON-NLS-1$
 		props.setLook(wlCap);
		fdlCap=new FormData();
		fdlCap.left = new FormAttachment(0, 0);
		fdlCap.right= new FormAttachment(middle, -margin);
		fdlCap.top  = new FormAttachment(wDistField, margin*2);
		wlCap.setLayoutData(fdlCap);
		wCap=new CCombo(shell, SWT.BORDER );
		wCap.setEditable(false);
 		props.setLook(wCap);
		
		wCap.addModifyListener(lsMod);
		fdCap=new FormData();
		fdCap.left = new FormAttachment(middle, 0);
		fdCap.top  = new FormAttachment(wDistField, margin);
		fdCap.right= new FormAttachment(50, 0);
		wCap.setLayoutData(fdCap);
		wCap.setItems(SpatialAnalysisMeta.bufferCaps);
		
		//Buffer Join style
		wlJoin=new Label(shell, SWT.RIGHT);
		wlJoin.setText(Messages.getString("SpatialAnalysisDialog.Join.Label")); //$NON-NLS-1$
 		props.setLook(wlJoin);
		fdlJoin=new FormData();
		fdlJoin.left = new FormAttachment(middle, 75);
		fdlJoin.right = new FormAttachment(middle, 200);
		fdlJoin.top = new FormAttachment(wDistField, margin*2);
		wlJoin.setLayoutData(fdlJoin);
		wJoin=new CCombo(shell, SWT.BORDER );
		wJoin.setEditable(false);
 		props.setLook(wJoin);
		
		wJoin.addModifyListener(lsMod);
		fdJoin=new FormData();
		fdJoin.left = new FormAttachment(wlJoin, margin);
		fdJoin.top = new FormAttachment(wDistField, margin);
		fdJoin.right = new FormAttachment(100, 0);
		wJoin.setLayoutData(fdJoin);
		wJoin.setItems(SpatialAnalysisMeta.bufferJoins);
       		
		//reference step
		wlReference=new Label(shell, SWT.RIGHT);
		wlReference.setText(Messages.getString("SpatialAnalysisDialog.Reference.Label")); //$NON-NLS-1$
 		props.setLook(wlReference);
		fdlReference=new FormData();
		fdlReference.left = new FormAttachment(0, 0);
		fdlReference.right= new FormAttachment(middle, -margin);
		fdlReference.top  = new FormAttachment(wJoin, margin*5);
		wlReference.setLayoutData(fdlReference);
		wReference=new CCombo(shell, SWT.BORDER );
		wReference.setEditable(false);
 		props.setLook(wReference);
		
		wReference.addModifyListener(lsMod);
		fdReference=new FormData();
		fdReference.left = new FormAttachment(middle, 0);
		fdReference.top  = new FormAttachment(wJoin, margin*4);
		fdReference.right= new FormAttachment(100, 0);
		wReference.setLayoutData(fdReference);
		wReference.addFocusListener(new FocusListener(){
			public void focusLost(FocusEvent e){}       
			public void focusGained(FocusEvent e){
				setStepNames(wReference);
			}
		});
		
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
		wReferenceField.addFocusListener(new FocusListener(){
			public void focusLost(FocusEvent e){}       
			public void focusGained(FocusEvent e){
				setReferenceField();
			}
		});
		
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
        
		wCompare.addModifyListener(lsMod);
		fdCompare=new FormData();
        fdCompare.top  = new FormAttachment(wReferenceField, margin*4);
		fdCompare.left = new FormAttachment(middle, 0);
		fdCompare.right= new FormAttachment(100, 0);
		wCompare.setLayoutData(fdCompare);	
		wCompare.addFocusListener(new FocusListener(){
			public void focusLost(FocusEvent e){}       
			public void focusGained(FocusEvent e){
				setStepNames(wCompare);
			}
		});
		
		//compare field
		wlCompareField=new Label(shell, SWT.RIGHT);
		wlCompareField.setText(Messages.getString("SpatialAnalysisDialog.CompareField.Label")); //$NON-NLS-1$
 		props.setLook(wlCompareField);
		FormData fdlCompareField = new FormData();
		fdlCompareField.left = new FormAttachment(0, 0);
		fdlCompareField.right= new FormAttachment(middle, -margin);
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
		wCompareField.addFocusListener(new FocusListener(){
			public void focusLost(FocusEvent e){}       
			public void focusGained(FocusEvent e){
				setCompareField();
			}
		});
		
        // Using compression for temporary files?
		wCompress=new Button(shell, SWT.CHECK);
		wCompress.setToolTipText(null);
	    props.setLook(wCompress);
	    fdCompress=new FormData();
	    fdCompress.right  = new FormAttachment(100, 0);
	    fdCompress.top   = new FormAttachment(wCompareField, margin*4);
	    wCompress.setLayoutData(fdCompress);
        wlCompress=new Label(shell, SWT.RIGHT);
        wlCompress.setText(Messages.getString("SpatialAnalysisDialog.Compress.Label"));
        props.setLook(wlCompress);
        fdlCompress=new FormData();
        fdlCompress.right = new FormAttachment(wCompress, -margin);
        fdlCompress.top  = new FormAttachment(wCompareField, margin*5);
        wlCompress.setLayoutData(fdlCompress);
        
		// Execute analysis on the whole set (rowset 2)?		        
        wOneRow=new Button(shell, SWT.CHECK);
		wOneRow.setToolTipText(null);
	    props.setLook(wOneRow);
	    fdOneRow=new FormData();
	    fdOneRow.right  = new FormAttachment(100, 0);
	    fdOneRow.top   = new FormAttachment(wCompress, margin);
	    wOneRow.setLayoutData(fdOneRow);
	    wlOneRow=new Label(shell, SWT.RIGHT);
        wlOneRow.setText(Messages.getString("SpatialAnalysisDialog.OneRow.Label"));
        props.setLook(wlOneRow);
        fdlOneRow=new FormData();
        fdlOneRow.right = new FormAttachment(wOneRow, -margin);
        fdlOneRow.top  = new FormAttachment(wCompress, margin*2);
        wlOneRow.setLayoutData(fdlOneRow);
		
		// Result line...
        wResult=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wResult);
		wResult.addModifyListener(lsMod);
		fdResult=new FormData();
		fdResult.left = new FormAttachment(70, 0);
		fdResult.top  = new FormAttachment(wOneRow, margin*4);
		fdResult.right= new FormAttachment(100, 0);
		wResult.setLayoutData(fdResult);
		wlResult=new Label(shell, SWT.RIGHT);
		wlResult.setText(Messages.getString("SpatialAnalysisDialog.Result.Label")); //$NON-NLS-1$
 		props.setLook(wlResult);
		fdlResult=new FormData();
		fdlResult.right= new FormAttachment(wResult, -margin);
		fdlResult.top  = new FormAttachment(wOneRow, margin*5);
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
	
	public void setStepNames(CCombo combo){
 		List<StepMeta> prevSteps = transMeta.findPreviousSteps(transMeta.findStep(stepname));
		for (StepMeta prevStep : prevSteps){
			combo.add(prevStep.getName());
		}
	}
	
	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData(){
		wAnalysis.select(input.getSpatialAnalysisByDesc());
		if (input.getDistField()!=null) 
			wDistField.setText(input.getDistField());
		
		if (input.getSide() != null) 
			wSide.setText(input.getSide());
		else
			wSide.setText(SpatialAnalysisMeta.bufferSides[2]);
		
		if (input.getJoin() != null) 
			wJoin.setText(input.getJoin());
		else
			wJoin.setText(SpatialAnalysisMeta.bufferJoins[2]);
		
		if (input.getCap() != null) 
			wCap.setText(input.getCap());
		else
			wCap.setText(SpatialAnalysisMeta.bufferCaps[1]);
		
		if (input.getResultFieldName()!=null) 
			wResult.setText(input.getResultFieldName());
		if (input.getReferenceStepName() != null) 
			wReference.setText(input.getReferenceStepName());
		if (input.getCompareStepName() != null)
			wCompare.setText(input.getCompareStepName());
		wCompress.setSelection(input.getCompressFiles());
		wOneRow.setSelection(input.getOneRow());
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
		if (!Const.isEmpty(wStepname.getText())){
			input.setReferenceStepMeta(transMeta.findStep(wReference.getText()));
			input.setCompareStepMeta(transMeta.findStep(wCompare.getText()));                  
			input.setReferenceField(wReferenceField.getText());
			input.setCompareField(wCompareField.getText());		
			input.setCap(wCap.getText());
			input.setSide(wSide.getText());
			input.setJoin(wJoin.getText());		
		    input.setResultFieldName(wResult.getText());
		    input.setDistField(wDistField.getText());
		    input.setCompressFiles(wCompress.getSelection());
		    input.setOneRow(wOneRow.getSelection());
			input.setSpatialAnalysisType(wAnalysis.getSelectionIndex()<0?0:wAnalysis.getSelectionIndex()); 
			stepname = wStepname.getText(); // return value
			dispose();
		}
	}
    
	private void setCompareField(){
		try{
			wCompareField.removeAll();
			if(!Const.isEmpty(wCompare.getText())){							
				RowMetaInterface r = transMeta.getStepFields(wCompare.getText());
				if (r!=null){
					String [] fieldNames = r.getFieldNames();
					for (int i=0;i<fieldNames.length;i++){	
						wCompareField.add(fieldNames[i]);									
					}
				}	
			}				
		}catch(KettleException ke){
			new ErrorDialog(shell, Messages.getString("SpatialAnalysisDialog.ErrorGettingFields.DialogTitle"), Messages.getString("SpatialAnalysisDialog.ErrorGettingFields.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	
	private void setReferenceField(){
		try{
			wReferenceField.removeAll();
			if(!Const.isEmpty(wReference.getText())){							
				RowMetaInterface r = transMeta.getStepFields(wReference.getText());
				if (r!=null){
					String [] fieldNames = r.getFieldNames();
					for (int i=0;i<fieldNames.length;i++){	
						wReferenceField.add(fieldNames[i]);									
					}
				}	
			}				
		}catch(KettleException ke){
			new ErrorDialog(shell, Messages.getString("SpatialAnalysisDialog.ErrorGettingFields.DialogTitle"), Messages.getString("SpatialAnalysisDialog.ErrorGettingFields.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
}