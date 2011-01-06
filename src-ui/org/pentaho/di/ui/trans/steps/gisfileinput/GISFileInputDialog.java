package org.pentaho.di.ui.trans.steps.gisfileinput;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransPreviewFactory;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.gisfileinput.GISFileInputMeta;
import org.pentaho.di.trans.steps.gisfileinput.Messages;
import org.pentaho.di.ui.core.dialog.EnterNumberDialog;
import org.pentaho.di.ui.core.dialog.EnterTextDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.dialog.PreviewRowsDialog;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.dialog.TransPreviewProgressDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

public class GISFileInputDialog extends BaseStepDialog implements StepDialogInterface
{
	final static private String[] GISFILE_FILTER_EXT = new String[] {"*.shp;*.SHP", "*"};
	
	private Label        wlFileName;
	private Button       wbFileName;
	private TextVar      wFileName;
	private FormData     fdlFileName, fdbFileName, fdFileName;
	
	private Label wlFileField;
	private Button wFileField;
	private FormData fdlFileField,fdFileField;
      
    private Label wlFileNameField;
    private CCombo wFileNameField;
    private FormData fdFileNameField,fdlFileNameField;
    
	private Label        wlLimit;
	private Text         wLimit;
	private FormData     fdlLimit, fdLimit;
	
	private Label        wlAddRownr;
	private Button       wAddRownr;
	private FormData     fdlAddRownr, fdAddRownr;

	private Label        wlFieldRownr;
	private Text         wFieldRownr;
	private FormData     fdlFieldRownr, fdFieldRownr;

	private GISFileInputMeta input;
	private boolean backupChanged, backupAddRownr;

	public GISFileInputDialog(Shell parent, Object in, TransMeta tr, String sname)
	{
		super(parent, (BaseStepMeta)in, tr, sname);
		input=(GISFileInputMeta)in;
	}

	public String open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
 		props.setLook(shell);
        setShellImage(shell, input);

		ModifyListener lsMod = new ModifyListener() 
		{
			public void modifyText(ModifyEvent e) 
			{
				input.setChanged();
			}
		};
		backupChanged = input.hasChanged();
		backupAddRownr = input.isRowNrAdded();

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(Messages.getString("GISFileInputDialog.Dialog.Title")); //$NON-NLS-1$
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(Messages.getString("System.Label.StepName")); //$NON-NLS-1$
 		props.setLook(wlStepname);
		fdlStepname=new FormData();
		fdlStepname.left = new FormAttachment(0, 0);
		fdlStepname.right= new FormAttachment(middle, -margin);
		fdlStepname.top  = new FormAttachment(0, margin);
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
	
		// Filename line
		wlFileName=new Label(shell, SWT.RIGHT);
		wlFileName.setText(Messages.getString("System.Label.Filename")); //$NON-NLS-1$
 		props.setLook(wlFileName);
		fdlFileName=new FormData();
		fdlFileName.left = new FormAttachment(0, 0);
		fdlFileName.top  = new FormAttachment(wStepname, margin);
		fdlFileName.right= new FormAttachment(middle, -margin);
		wlFileName.setLayoutData(fdlFileName);
		
		wbFileName=new Button(shell, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbFileName);
		wbFileName.setText(Messages.getString("System.Button.Browse")); //$NON-NLS-1$
		fdbFileName=new FormData();
		fdbFileName.right= new FormAttachment(100, 0);
		fdbFileName.top  = new FormAttachment(wStepname, margin);
		wbFileName.setLayoutData(fdbFileName);

		wFileName=new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wFileName);
		wFileName.addModifyListener(lsMod);
		fdFileName=new FormData();
		fdFileName.left = new FormAttachment(middle, 0);
		fdFileName.right= new FormAttachment(wbFileName, -margin);
		fdFileName.top  = new FormAttachment(wStepname, margin);
		wFileName.setLayoutData(fdFileName);
        
		//Is FileName defined in a Field				        
		wlFileField=new Label(shell, SWT.RIGHT);
	    wlFileField.setText(Messages.getString("GISFileInputDialog.FilenameInField.Label"));
        props.setLook(wlFileField);
        fdlFileField=new FormData();
        fdlFileField.left = new FormAttachment(0, 0);
        fdlFileField.right = new FormAttachment(middle, -margin);
        fdlFileField.top  = new FormAttachment(wFileName, margin*2);
        wlFileField.setLayoutData(fdlFileField);
        
        wFileField=new Button(shell, SWT.CHECK);
        wFileField.setToolTipText(Messages.getString("GISFileInputDialog.FilenameInField.Tooltip"));
	    props.setLook(wFileField);
	    fdFileField=new FormData();
	    fdFileField.right  = new FormAttachment(100, 0);
	    fdFileField.top   = new FormAttachment(wFileName, margin);
	    fdFileField.left   = new FormAttachment(middle, 0);
	    wFileField.setLayoutData(fdFileField);
	    wFileField.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent arg0)
            {
            	activeFileField();
            	input.setChanged();
            }
        }
        );
        
		// FileName field
		wlFileNameField=new Label(shell, SWT.RIGHT);
        wlFileNameField.setText(Messages.getString("GISFileInputDialog.FilenameField.Label"));
        props.setLook(wlFileNameField);
        fdlFileNameField=new FormData();
        fdlFileNameField.left = new FormAttachment(0, 0);
        fdlFileNameField.top  = new FormAttachment(wFileField,2* margin);
        fdlFileNameField.right= new FormAttachment(middle, -margin);
        wlFileNameField.setLayoutData(fdlFileNameField);
              
        wFileNameField=new CCombo(shell, SWT.BORDER | SWT.READ_ONLY);
        wFileNameField.setEditable(true);
        props.setLook(wFileNameField);
        wFileNameField.addModifyListener(lsMod);
        fdFileNameField=new FormData();
        fdFileNameField.left = new FormAttachment(middle, 0);
        fdFileNameField.top  = new FormAttachment(wFileField, margin);
        fdFileNameField.right= new FormAttachment(100, -margin);
        wFileNameField.setLayoutData(fdFileNameField);
        wFileNameField.addFocusListener(new FocusListener()
            {
                public void focusLost(org.eclipse.swt.events.FocusEvent e){
                }
            
                public void focusGained(org.eclipse.swt.events.FocusEvent e){
                    Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
                    shell.setCursor(busy);
                    setFileField();
                    shell.setCursor(null);
                    busy.dispose();
                }
            }
        );   
		// Limit input ...
		wlLimit=new Label(shell, SWT.RIGHT);
		wlLimit.setText(Messages.getString("GISFileInputDialog.LimitSize.Label")); //$NON-NLS-1$
 		props.setLook(wlLimit);
		fdlLimit=new FormData();
		fdlLimit.left = new FormAttachment(0, 0);
		fdlLimit.right= new FormAttachment(middle, -margin);
		fdlLimit.top  = new FormAttachment(wFileNameField, margin*2);
		wlLimit.setLayoutData(fdlLimit);
		wLimit=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wLimit);
		wLimit.addModifyListener(lsMod);
		fdLimit=new FormData();
		fdLimit.left = new FormAttachment(middle, 0);
		fdLimit.top  = new FormAttachment(wFileNameField, margin*2);
		fdLimit.right= new FormAttachment(100, 0);
		wLimit.setLayoutData(fdLimit);

		// Add rownr (1...)?
		wlAddRownr=new Label(shell, SWT.RIGHT);
		wlAddRownr.setText(Messages.getString("GISFileInputDialog.AddRowNr.Label")); //$NON-NLS-1$
 		props.setLook(wlAddRownr);
		fdlAddRownr=new FormData();
		fdlAddRownr.left = new FormAttachment(0, 0);
		fdlAddRownr.top  = new FormAttachment(wLimit, margin);
		fdlAddRownr.right= new FormAttachment(middle, -margin);
		wlAddRownr.setLayoutData(fdlAddRownr);
		wAddRownr=new Button(shell, SWT.CHECK );
 		props.setLook(wAddRownr);
		wAddRownr.setToolTipText(Messages.getString("GISFileInputDialog.AddRowNr.Tooltip")); //$NON-NLS-1$
		fdAddRownr=new FormData();
		fdAddRownr.left = new FormAttachment(middle, 0);
		fdAddRownr.top  = new FormAttachment(wLimit, margin);
		wAddRownr.setLayoutData(fdAddRownr);
		wAddRownr.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { input.setChanged(); setFlags(); } } );

		// FieldRownr input ...
		wlFieldRownr=new Label(shell, SWT.LEFT);
		wlFieldRownr.setText(Messages.getString("GISFileInputDialog.FieldnameOfRowNr.Label")); //$NON-NLS-1$
 		props.setLook(wlFieldRownr);
		fdlFieldRownr=new FormData();
		fdlFieldRownr.left = new FormAttachment(wAddRownr, margin);
		fdlFieldRownr.top  = new FormAttachment(wLimit, margin);
		wlFieldRownr.setLayoutData(fdlFieldRownr);
		wFieldRownr=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wFieldRownr);
		wFieldRownr.addModifyListener(lsMod);
		fdFieldRownr=new FormData();
		fdFieldRownr.left = new FormAttachment(wlFieldRownr, margin);
		fdFieldRownr.top  = new FormAttachment(wLimit, margin);
		fdFieldRownr.right= new FormAttachment(100, 0);
		wFieldRownr.setLayoutData(fdFieldRownr);

		// Some buttons
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(Messages.getString("System.Button.OK")); //$NON-NLS-1$
        wPreview=new Button(shell, SWT.PUSH);
        wPreview.setText(Messages.getString("System.Button.Preview")); //$NON-NLS-1$
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(Messages.getString("System.Button.Cancel")); //$NON-NLS-1$
		
		setButtonPositions(new Button[] { wOK, wPreview, wCancel }, margin, null);

		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
        lsPreview  = new Listener() { public void handleEvent(Event e) { preview(); } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		
		wCancel.addListener(SWT.Selection, lsCancel);
        wPreview.addListener (SWT.Selection, lsPreview);
		wOK.addListener    (SWT.Selection, lsOK    );
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
		wLimit.addSelectionListener( lsDef );
		wFieldRownr.addSelectionListener( lsDef );
        // wAccField.addSelectionListener( lsDef );

		wFileName.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent arg0)
			{
				wFileName.setToolTipText(transMeta.environmentSubstitute(wFileName.getText()));
			}
		});
		
		wbFileName.addSelectionListener
		(
			new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e) 
				{
					FileDialog dialog = new FileDialog(shell, SWT.OPEN);
					dialog.setFilterExtensions(GISFILE_FILTER_EXT); //$NON-NLS-1$ //$NON-NLS-2$
					if (wFileName.getText()!=null)
					{
						dialog.setFileName(wFileName.getText());
					}
						
					dialog.setFilterNames(new String[] {Messages.getString("GISFileInputDialog.Filter.SHPFiles"), Messages.getString("System.FileType.AllFiles")}); //$NON-NLS-1$ //$NON-NLS-2$
					
					if (dialog.open()!=null)
					{
						String str = dialog.getFilterPath()+Const.FILE_SEPARATOR+dialog.getFileName();
						wFileName.setText(str);
					}
				}
			}
		);

		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );
		
		getData();
		input.setChanged(changed);

		// Set the shell size, based upon previous time...
		setSize();
		activeFileField();
		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}
	
	protected void setFlags()
    {
        wlFieldRownr.setEnabled( wAddRownr.getSelection() );
        wFieldRownr.setEnabled( wAddRownr.getSelection() );
    }
	
	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
		if (!input.isFileNameInField() ) {
			if (input.getFileName() != null){
				wFileName.setText(input.getFileName());
				wFileName.setToolTipText(transMeta.environmentSubstitute(input.getFileName()));
			}
		}else {
			wFileField.setSelection(true);
			if(input.getFileNameField() !=null)
				wFileNameField.setText(input.getFileNameField());		
		}
		wLimit.setText(Integer.toString(input.getRowLimit())); //$NON-NLS-1$
		wAddRownr.setSelection(input.isRowNrAdded());
		if (input.getRowNrField()!=null) wFieldRownr.setText(input.getRowNrField());
	
        setFlags();
		
		wStepname.selectAll();
	}
	
	private void cancel()
	{
		stepname=null;
		input.setRowNrAdded( backupAddRownr );
		input.setChanged(backupChanged);
		dispose();
	}
	
	private void setFileField(){
		try{
	        String field=  wFileNameField.getText();
			wFileNameField.removeAll();
				
			RowMetaInterface r = transMeta.getPrevStepFields(stepname);
			if (r!=null){
		    	r.getFieldNames();
			    for (int i=0;i<r.getFieldNames().length;i++){	
		        	wFileNameField.add(r.getFieldNames()[i]);									
				}
			}
			if(field!=null) wFileNameField.setText(field);		
		}catch(KettleException ke){
			new ErrorDialog(shell, Messages.getString("GISFileInputDialog.FailedToGetFields.DialogTitle"), Messages.getString("GISFileInputDialog.FailedToGetFields.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	
	private void activeFileField(){
		wlFileNameField.setEnabled(wFileField.getSelection());
		wFileNameField.setEnabled(wFileField.getSelection());	
		wlFileName.setEnabled(!wFileField.getSelection());		
		wFileName.setEnabled(!wFileField.getSelection());
		wbFileName.setEnabled(!wFileField.getSelection());		
	}
	
	public void getInfo(GISFileInputMeta oneMeta) throws KettleStepException
	{
		// copy info to Meta class (input)
		oneMeta.setFileName( wFileName.getText() );
		oneMeta.setFileNameInField(wFileField.getSelection());
		oneMeta.setFileNameField(wFileNameField.getText());	
		oneMeta.setRowLimit( Const.toInt(wLimit.getText(), 0 ) );
        oneMeta.setRowNrAdded( wAddRownr.getSelection() );
		oneMeta.setRowNrField( wFieldRownr.getText() );
	}
	
	private void ok()
	{
		try
		{
			stepname = wStepname.getText(); // return value
			getInfo(input);
			dispose();
		}
		catch(KettleStepException e)
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage(e.toString());
			mb.setText(Messages.getString("System.Warning")); //$NON-NLS-1$
			mb.open();
			
			// Close anyway!
			dispose();
		}
	}
	
    // Preview the data
    private void preview()
    {
        // Create the XML input step
    	try
    	{
	        GISFileInputMeta oneMeta = new GISFileInputMeta();
	        getInfo(oneMeta);
            
	        if (oneMeta.isFileNameInField())
            {
                MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION);
                mb.setMessage(Messages.getString("GISFileInputDialog.Dialog.SpecifyASampleFile.Message")); // Nothing found that matches your criteria
                mb.setText(Messages.getString("GISFileInputDialog.Dialog.SpecifyASampleFile.Title")); // Sorry!
                mb.open();
                return;
            }
	        
            TransMeta previewMeta = TransPreviewFactory.generatePreviewTransformation(transMeta, oneMeta, wStepname.getText());
	        
	        EnterNumberDialog numberDialog = new EnterNumberDialog(shell, props.getDefaultPreviewSize(), Messages.getString("GISFileInputDialog.PreviewSize.DialogTitle"), Messages.getString("GISFileInputDialog.PreviewSize.DialogMessage")); //$NON-NLS-1$ //$NON-NLS-2$
	        int previewSize = numberDialog.open();
	        if (previewSize>0)
	        {
	            TransPreviewProgressDialog progressDialog = new TransPreviewProgressDialog(shell, previewMeta, new String[] { wStepname.getText() }, new int[] { previewSize } );
	            progressDialog.open();
	
	            Trans trans = progressDialog.getTrans();
	            String loggingText = progressDialog.getLoggingText();
	
	            if (!progressDialog.isCancelled())
	            {
	                if (trans.getResult()!=null && trans.getResult().getNrErrors()>0)
	                {
	                	EnterTextDialog etd = new EnterTextDialog(shell, Messages.getString("System.Dialog.PreviewError.Title"),   //$NON-NLS-1$
	                			Messages.getString("System.Dialog.PreviewError.Message"), loggingText, true ); //$NON-NLS-1$
	                	etd.setReadOnly();
	                	etd.open();
	                }
	            }
	            
	            PreviewRowsDialog prd =new PreviewRowsDialog(shell, transMeta, SWT.NONE, wStepname.getText(), progressDialog.getPreviewRowsMeta(wStepname.getText()), progressDialog.getPreviewRows(wStepname.getText()), loggingText);
	            prd.open();
	        }
    	}
    	catch(Exception e)
    	{
    		new ErrorDialog(shell, Messages.getString("System.Dialog.PreviewError.Title"),  //$NON-NLS-1$
    				Messages.getString("System.Dialog.PreviewError.Message"), e); //$NON-NLS-1$
    	}
    }
}
