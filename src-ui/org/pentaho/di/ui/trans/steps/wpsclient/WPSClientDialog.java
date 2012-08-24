package org.pentaho.di.ui.trans.steps.wpsclient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.wpsclient.Messages;
import org.pentaho.di.trans.steps.wpsclient.WPSClientMeta;
import org.pentaho.di.trans.steps.wpsclient.parameter.Parameter;
import org.pentaho.di.trans.steps.wpsclient.parameter.ParameterBuilder;
import org.pentaho.di.trans.steps.wpsclient.parameter.format.AbstractFormat;
import org.pentaho.di.trans.steps.wpsclient.parameter.format.BoundingBoxFormat;
import org.pentaho.di.trans.steps.wpsclient.parameter.format.ComplexFormat;
import org.pentaho.di.trans.steps.wpsclient.parameter.format.LiteralFormat;
import org.pentaho.di.trans.steps.wpsclient.process.ProcessBuilder;
import org.pentaho.di.trans.steps.wpsclient.requestbuilder.DescribeProcessRequestBuilder;
import org.pentaho.di.trans.steps.wpsclient.requestbuilder.GetCapabilitiesRequestBuilder;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.ComboVar;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

public class WPSClientDialog extends BaseStepDialog implements StepDialogInterface{	
	private static final String 	GET = "GET";
	private static final String 	POST = "POST";
	
	private static final String 	CRS = "Crs";
	private static final String 	ANY = "---------";
	
	private static final String 	WPS_VERSION = "1.0.0";
	
	private Label        			wlUrl;
	private TextVar      			wUrl;
	private FormData     			fdlUrl, fdUrl;	
    
	private Map<String, String>		urls;
	
	private Label        			wlBoldInput, wlBoldOutput;
	private FormData     			fdlBoldInput, fdlBoldOutput;	
	
    private Label       			wlMethod;
    private Button[]				wMethod;
    private FormData    			fdlMethod, fdMethod1, fdMethod2; 
    private Listener				lsMethod;
    
    private Label       			wlVersion;
    private ComboVar				wVersion;
    private FormData    			fdlVersion, fdVersion;   
    
    private Button					wbGetProcesses;
    private Label       			wlProcess;
    private ComboVar				wProcess;
    private FormData    			fdlProcess, fdProcess, fdGetProcesses;   
    private Listener				lsGetProcesses;
    
	private TableView 				wInputParameters, wOutputParameters;
	private FormData 				fdInputParameters, fdOutputParameters, fdGetParameters;
	private Button 					wGetParameters;
	
	private Label        			wlResultFieldName;
	private TextVar      			wResultFieldName;
	private FormData     			fdlResultFieldName, fdResultFieldName;	
		
	private WPSClientMeta 			input;   
	
	private Group 					wgGeneral, wgParameters, wgInputParameters, wgOutputParameters;    
    private FormData 				fdgGeneral, fdgParameters, fdgInputParameters, fdgOutputParameters;  
    
    private Label       			wlOutputType;
    private Button[]				wOutputType;
    private FormData    			fdlOutputType, fdOutputType1, fdOutputType2; 
    private Listener				lsOutputType;
    
    private Map<String, Integer> 	inputFields;
    private String 					fieldNames[];
    
    private String					method;
    private Map<String, String>     processes;
    private Map<String, Parameter>  inputParameters;
    private Map<String, Parameter>  outputParameters;
	
	private TableListener inputTableListener; 
	private TableListener outputTableListener;
	
	private ColumnInfo[] colinfInputParameters;
	private ColumnInfo[] colinfOutputParameters;
	
	private Map<String, Control> inputControls;
	private Map<String, Control> outputControls;
  
	public WPSClientDialog(Shell parent, Object in, TransMeta transMeta, String sname){
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(WPSClientMeta)in;
		inputFields =new HashMap<String, Integer>();
		inputControls = new HashMap<String, Control>();
		outputControls = new HashMap<String, Control>();
	}

	public String open(){
		Shell parent = getParent();
		Display display = parent.getDisplay();
		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
 		props.setLook(shell);
        setShellImage(shell, input);

		ModifyListener lsMod = new ModifyListener(){
			public void modifyText(ModifyEvent e){
				input.setChanged();
			}
		};
        
		changed = input.hasChanged();

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(Messages.getString("WPSClientDialog.Shell.Title")); //$NON-NLS-1$
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(Messages.getString("System.Label.StepName")); //$NON-NLS-1$
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
 		
		wgGeneral = new Group(shell, SWT.SHADOW_NONE);
		props.setLook(wgGeneral);
		wgGeneral.setText(Messages.getString("WPSClientDialog.General.Group.Label"));
		FormLayout generalGroupLayout = new FormLayout();
		generalGroupLayout.marginWidth = 5;
		generalGroupLayout.marginHeight = 5;
		wgGeneral.setLayout(generalGroupLayout);
		
		wlUrl=new Label(wgGeneral, SWT.RIGHT);
		wlUrl.setText(Messages.getString("WPSClientDialog.URL.Label")); //$NON-NLS-1$
 		props.setLook(wlUrl);
		fdlUrl=new FormData();
		fdlUrl.left = new FormAttachment(0, 0);
		fdlUrl.top  = new FormAttachment(wStepname, 0);
		fdlUrl.right= new FormAttachment(middle, -margin);	
		wlUrl.setLayoutData(fdlUrl);

		wUrl=new TextVar(transMeta, wgGeneral, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wUrl);
		wUrl.addModifyListener(lsMod);
		fdUrl=new FormData();
		fdUrl.left = new FormAttachment(middle, 0);
		fdUrl.top  = new FormAttachment(wStepname, -margin);
		fdUrl.right= new FormAttachment(100, -margin);
		wUrl.setLayoutData(fdUrl);     
        		
		wMethod = new Button[2];		
		wMethod[0] = new Button(wgGeneral, SWT.RADIO);
		wMethod[0].setText(GET);
		fdMethod2 = new FormData();
		fdMethod2.left = new FormAttachment(middle, 0);
		fdMethod2.top = new FormAttachment(wUrl,2* margin);
		wMethod[0].setLayoutData(fdMethod2);
		lsMethod = new Listener()  {public void handleEvent(Event e){input.setChanged();setMethod();}	};
		wMethod[0].addListener(SWT.Selection, lsMethod);
		
		wMethod[1] = new Button(wgGeneral, SWT.RADIO);
		wMethod[1].setText(POST);
		fdMethod1 = new FormData();
		fdMethod1.left   = new FormAttachment(wMethod[0], 4*margin);
		fdMethod1.top = new FormAttachment(wUrl,2*margin);
		wMethod[1].setLayoutData(fdMethod1);		
		wMethod[1].addListener(SWT.Selection, lsMethod);
		
		wlMethod=new Label(wgGeneral, SWT.RIGHT);
        wlMethod.setText(Messages.getString("WPSClientDialog.Method.Label"));
        props.setLook(wlMethod);
        fdlMethod=new FormData();
        fdlMethod.left = new FormAttachment(0, 0);
        fdlMethod.top  = new FormAttachment(wUrl, margin*3);
        fdlMethod.right= new FormAttachment(middle, -2*margin);
        wlMethod.setLayoutData(fdlMethod);

        wlVersion=new Label(wgGeneral, SWT.RIGHT);
        wlVersion.setText(Messages.getString("WPSClientDialog.Version.Label"));
        props.setLook(wlVersion);
        fdlVersion=new FormData();
        fdlVersion.left = new FormAttachment(75, 0);
        fdlVersion.top  = new FormAttachment(wUrl, margin*3);
        wlVersion.setLayoutData(fdlVersion);
        wVersion=new ComboVar(transMeta, wgGeneral, SWT.BORDER | SWT.READ_ONLY);
        wVersion.setEditable(false);
        props.setLook(wVersion);
        fdVersion=new FormData();
        fdVersion.left = new FormAttachment(wlVersion, margin*2);
        fdVersion.top  = new FormAttachment(wUrl, margin);
        fdVersion.right= new FormAttachment(100, -margin);
        wVersion.setLayoutData(fdVersion);
        wVersion.add(WPS_VERSION);
        wVersion.addModifyListener(lsMod);
        
		lsGetProcesses = new Listener(){
			public void handleEvent(Event e){
				if(!checkGeneralParameters()){
					getProcesses();
					setProcesses();
					wProcess.select(0);
				}
			}	
		};
		
		wbGetProcesses = new Button(wgGeneral, SWT.PUSH);
		wbGetProcesses.setText(Messages.getString("WPSClientDialog.GetProcesses.Button")); //$NON-NLS-1$
		fdGetProcesses = new FormData();
		fdGetProcesses.right   = new FormAttachment(100, -margin);
		fdGetProcesses.top = new FormAttachment(wVersion, 2*margin);
		wbGetProcesses.setLayoutData(fdGetProcesses);
		wbGetProcesses.addListener(SWT.Selection, lsGetProcesses);
		
		wlProcess = new Label(wgGeneral, SWT.RIGHT);
		wlProcess.setText(Messages.getString("WPSClientDialog.Process.Label"));
        props.setLook(wlProcess);
        fdlProcess = new FormData();
        fdlProcess.left = new FormAttachment(0, 0);
        fdlProcess.top  = new FormAttachment(wVersion, 3*margin);
        fdlProcess.right= new FormAttachment(middle, -margin);
        wlProcess.setLayoutData(fdlProcess);
        
        wProcess=new ComboVar(transMeta, wgGeneral, SWT.BORDER | SWT.READ_ONLY);
        wProcess.setEditable(false);
        props.setLook(wProcess);
        wProcess.addModifyListener(lsMod);
        fdProcess=new FormData();
        fdProcess.left = new FormAttachment(middle, 0);
        fdProcess.top  = new FormAttachment(wVersion, 2*margin);
        fdProcess.right= new FormAttachment(wbGetProcesses, -margin);
        wProcess.setLayoutData(fdProcess);
 
		fdgGeneral = new FormData();
		fdgGeneral.left  = new FormAttachment(0, margin);
		fdgGeneral.top   = new FormAttachment(wStepname, margin);
		fdgGeneral.right = new FormAttachment(100, -margin);
		wgGeneral.setLayoutData(fdgGeneral);		
		
		wgParameters = new Group(shell, SWT.SHADOW_NONE);
		props.setLook(wgParameters);
		wgParameters.setText(Messages.getString("WPSClientDialog.Parameters.Group.Label"));
		FormLayout parametersGroupLayout = new FormLayout();
		parametersGroupLayout.marginWidth = 10;
		parametersGroupLayout.marginHeight = 10;
		wgParameters.setLayout(parametersGroupLayout);
		
		wgInputParameters = new Group(wgParameters, SWT.SHADOW_NONE);
		props.setLook(wgInputParameters);
		wgInputParameters.setText(Messages.getString("WPSClientDialog.InputParameters.Group.Label"));
		FormLayout inputGroupLayout = new FormLayout();
		inputGroupLayout.marginWidth = 10;
		inputGroupLayout.marginHeight = 10;
		wgInputParameters.setLayout(inputGroupLayout);
		    
		colinfInputParameters = new ColumnInfo[5];
		colinfInputParameters[0] = new ColumnInfo(Messages.getString("WPSClientDialog.ParameterColumn.Label"),  
				ColumnInfo.COLUMN_TYPE_TEXT, null, false);
		colinfInputParameters[1] = new ColumnInfo(Messages.getString("WPSClientDialog.FormatColumn.Label"),  
				ColumnInfo.COLUMN_TYPE_TEXT, null, false);
		colinfInputParameters[2] = new ColumnInfo(Messages.getString("WPSClientDialog.UomColumn.Label"),  
				ColumnInfo.COLUMN_TYPE_TEXT, null, false);
		colinfInputParameters[3] = new ColumnInfo(Messages.getString("WPSClientDialog.ValueColumn.Label"),  
				ColumnInfo.COLUMN_TYPE_TEXT, null, false); 		
		colinfInputParameters[4] = new ColumnInfo(Messages.getString("WPSClientDialog.PreviousStepFieldColumn.Label"),  
				ColumnInfo.COLUMN_TYPE_CCOMBO, null, false);
		
		wInputParameters = new TableView(transMeta, wgInputParameters,
							  SWT.BORDER | SWT.V_SCROLL, 
							  colinfInputParameters, 
							  4,  
							  lsMod,
							  props
							  );
		
		wInputParameters.setSortable(false);
		final Table inputTable = wInputParameters.getTable();
		inputTableListener = new TableListener(inputTable, display, shell, inputParameters);
		inputTable.addListener(SWT.MouseHover, inputTableListener);
		inputTable.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				inputTableListener.setSelection(inputTable.getSelection()[0].getText(1));				
			}
	    });
		
		fdInputParameters = new FormData();
		fdInputParameters.left  = new FormAttachment(0, 0);
		fdInputParameters.top   = new FormAttachment(wgInputParameters, 2*margin);
		fdInputParameters.right = new FormAttachment(100, -margin);		
		wInputParameters.setLayoutData(fdInputParameters);
		
		wlBoldInput = new Label(wgInputParameters, SWT.RIGHT);
		wlBoldInput.setText(Messages.getString("WPSClientDialog.Bold.Label"));
        props.setLook(wlBoldInput);
        fdlBoldInput = new FormData();
        fdlBoldInput.left = new FormAttachment(0, margin);
        fdlBoldInput.top  = new FormAttachment(wInputParameters, margin);
        wlBoldInput.setLayoutData(fdlBoldInput);
		
		fdgInputParameters = new FormData();
		fdgInputParameters.left  = new FormAttachment(0, margin);
		fdgInputParameters.top   = new FormAttachment(wgGeneral, margin);
		fdgInputParameters.right = new FormAttachment(100, -margin);
		wgInputParameters.setLayoutData(fdgInputParameters);	
				
		wgOutputParameters = new Group(wgParameters, SWT.SHADOW_NONE);
		props.setLook(wgOutputParameters);
		wgOutputParameters.setText(Messages.getString("WPSClientDialog.OutputParameters.Group.Label"));
		FormLayout outputGroupLayout = new FormLayout();
		outputGroupLayout.marginWidth = 10;
		outputGroupLayout.marginHeight = 10;
		wgOutputParameters.setLayout(outputGroupLayout);

		wlOutputType=new Label(wgOutputParameters, SWT.RIGHT);
        wlOutputType.setText(Messages.getString("WPSClientDialog.OutputType.Label"));
        props.setLook(wlOutputType);
        fdlOutputType=new FormData();
        fdlOutputType.left = new FormAttachment(0, 0);
        fdlOutputType.top  = new FormAttachment(wgOutputParameters, 2*margin);        
        wlOutputType.setLayoutData(fdlOutputType);
        
        wOutputType = new Button[2];		
		wOutputType[0] = new Button(wgOutputParameters, SWT.RADIO);
		wOutputType[0].setText(Messages.getString("WPSClientDialog.OutputTypes.ResponseDocument.Label"));
		wOutputType[0].setToolTipText(Messages.getString("WPSClientDialog.OutputTypes.ResponseDocument.Tooltip"));
		fdOutputType2 = new FormData();
		fdOutputType2.left = new FormAttachment(wlOutputType, 4*margin);
		fdOutputType2.top = new FormAttachment(wgOutputParameters,margin);
		wOutputType[0].setLayoutData(fdOutputType2);
		lsOutputType = new Listener() {public void handleEvent(Event e){input.setChanged();}};
		wOutputType[0].addListener(SWT.Selection, lsOutputType);
		wOutputType[0].setSelection(true);
		
		wOutputType[1] = new Button(wgOutputParameters, SWT.RADIO);
		wOutputType[1].setText(Messages.getString("WPSClientDialog.OutputTypes.RawDataOutput.Label"));
		wOutputType[1].setToolTipText(Messages.getString("WPSClientDialog.OutputTypes.RawDataOutput.Tooltip"));
		fdOutputType1 = new FormData();
		fdOutputType1.left = new FormAttachment(wOutputType[0], 2*margin);
		fdOutputType1.top = new FormAttachment(wgOutputParameters,margin);
		wOutputType[1].setLayoutData(fdOutputType1);		
		wOutputType[1].addListener(SWT.Selection, lsOutputType);

		colinfOutputParameters = new ColumnInfo[4];
		colinfOutputParameters[0] = new ColumnInfo(Messages.getString("WPSClientDialog.ParameterColumn.Label"),  
				ColumnInfo.COLUMN_TYPE_TEXT, null, false);
		colinfOutputParameters[1] = new ColumnInfo(Messages.getString("WPSClientDialog.FormatColumn.Label"),  
				ColumnInfo.COLUMN_TYPE_TEXT, null, false);
		colinfOutputParameters[2] = new ColumnInfo(Messages.getString("WPSClientDialog.UomColumn.Label"),  
				ColumnInfo.COLUMN_TYPE_TEXT, null, false);
		colinfOutputParameters[3] = new ColumnInfo(Messages.getString("WPSClientDialog.ValueColumn.Label"),  
				ColumnInfo.COLUMN_TYPE_TEXT, null, false); 	

 		
		wOutputParameters = new TableView(transMeta, wgOutputParameters,
							  SWT.BORDER | SWT.V_SCROLL,
							  colinfOutputParameters, 
							  3,  
							  lsMod,
							  props
							  );
		
		wOutputParameters.setSortable(false);	

		final Table outputTable = wOutputParameters.getTable();		
		outputTableListener = new TableListener(outputTable, display, shell, outputParameters);
		outputTable.addListener(SWT.MouseHover, outputTableListener);
		outputTable.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				outputTableListener.setSelection(outputTable.getSelection()[0].getText(1));					
			}
	    });
		
		fdOutputParameters = new FormData();
		fdOutputParameters.left  = new FormAttachment(0, 0);
		fdOutputParameters.top   = new FormAttachment(wOutputType[0], 2*margin);
		fdOutputParameters.right = new FormAttachment(100, -margin);		
		wOutputParameters.setLayoutData(fdOutputParameters);
		
		wlBoldOutput = new Label(wgOutputParameters, SWT.RIGHT);
		wlBoldOutput.setText(Messages.getString("WPSClientDialog.Bold.Label"));
        props.setLook(wlBoldOutput);
        fdlBoldOutput = new FormData();
        fdlBoldOutput.left = new FormAttachment(0, margin);
        fdlBoldOutput.top  = new FormAttachment(wOutputParameters, margin);
        wlBoldOutput.setLayoutData(fdlBoldOutput);
        
		fdgOutputParameters = new FormData();
		fdgOutputParameters.left  = new FormAttachment(0, margin);
		fdgOutputParameters.top   = new FormAttachment(wgInputParameters, margin);
		fdgOutputParameters.right = new FormAttachment(100, -margin);
		wgOutputParameters.setLayoutData(fdgOutputParameters);	
		
		Listener lsGetParameters = new Listener(){
			public void handleEvent(Event e){
				if(!checkGeneralParameters() && !checkProcess()){
					getParameters();
					setParameters(true);
				}
			}	
		};
		
 		wGetParameters=new Button(wgParameters, SWT.PUSH);
 		wGetParameters.setText(Messages.getString("WPSClientDialog.GetParameters.Label"));		  
        fdGetParameters = new FormData();
        fdGetParameters.left = new FormAttachment(0, margin);
        fdGetParameters.top = new FormAttachment(wgOutputParameters, 2*margin);
        wGetParameters.setLayoutData(fdGetParameters);  
		wGetParameters.addListener(SWT.Selection, lsGetParameters);
		
		fdgParameters = new FormData();
		fdgParameters.left  = new FormAttachment(0, margin);
		fdgParameters.top   = new FormAttachment(wgGeneral, margin);
		fdgParameters.right = new FormAttachment(100, -margin);
		wgParameters.setLayoutData(fdgParameters);
		
		wlResultFieldName=new Label(shell, SWT.RIGHT);
		wlResultFieldName.setText(Messages.getString("WPSClientDialog.ResultFieldName.Label")); //$NON-NLS-1$
 		props.setLook(wlResultFieldName);
		fdlResultFieldName=new FormData();
		fdlResultFieldName.left = new FormAttachment(0, margin);
		fdlResultFieldName.top  = new FormAttachment(wgParameters, 3*margin);	
		wlResultFieldName.setLayoutData(fdlResultFieldName);

		wResultFieldName=new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wResultFieldName);
		wResultFieldName.addModifyListener(lsMod);
		fdResultFieldName=new FormData();
		fdResultFieldName.left = new FormAttachment(wlResultFieldName, margin);
		fdResultFieldName.top  = new FormAttachment(wgParameters, 2*margin);
		fdResultFieldName.right= new FormAttachment(middle, -margin);
		wResultFieldName.setLayoutData(fdResultFieldName);    
	
        // Search the fields in the background      
        final Runnable runnable = new Runnable(){
            public void run(){
                StepMeta stepMeta = transMeta.findStep(stepname);
                if (stepMeta!=null){
                    try{
                    	RowMetaInterface row = transMeta.getPrevStepFields(stepMeta);               
                        for (int i=0;i<row.size();i++){
                        	inputFields.put(row.getValueMeta(i).getName(), Integer.valueOf(i));
                        }                       
                        getFieldNames();
                    }catch(KettleException e){
                    	log.logError(toString(), Messages.getString("System.Dialog.GetFieldsFailed.Message"));
                    }
                }
            }
        };
        
        new Thread(runnable).start();
        
		// THE BUTTONS
		wOK = new Button(shell, SWT.PUSH);
		wOK.setText(Messages.getString("System.Button.OK"));
		
		wCancel = new Button(shell, SWT.PUSH);
		wCancel.setText(Messages.getString("System.Button.Cancel"));
		
		setButtonPositions(new Button[] { wOK, wCancel }, margin, wResultFieldName);

		// Add listeners
		lsOK = new Listener(){ 
			public void handleEvent(Event e){ 
				ok();
			} 
		};
		
		lsCancel = new Listener(){ 
			public void handleEvent(Event e){ 
				cancel();
			} 
		};
		
		wOK.addListener     (SWT.Selection, lsOK     );
		wCancel.addListener (SWT.Selection, lsCancel );
		
		lsDef = new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
        wUrl.addSelectionListener( lsDef );
		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );
		
		// Set the shell size, based upon previous time...
		setSize();	
		getData();
		setMethod();
		
		input.setChanged(changed);

		shell.open();
		while (!shell.isDisposed()){
			if (!display.readAndDispatch()) 
				display.sleep();
		}
		return stepname;
	}
	
	protected void setFieldCombos() {
		Set<String> keySet = inputFields.keySet();
		List<String> entries = new ArrayList<String>(keySet);
		String[] fieldNames = (String[]) entries.toArray(new String[entries.size()]);
		Const.sortStrings(fieldNames);		
		colinfInputParameters[4].setComboValues(fieldNames);
	}

	public void getData(){		
		if (!Const.isEmpty(input.getUrl()))      
			wUrl.setText(input.getUrl().toString());
		
		if (!Const.isEmpty(input.getMethod())) {
			method = input.getMethod();
			if (method.equals(GET))
				wMethod[0].setSelection(true);
			else
				wMethod[1].setSelection(true);			
		}
		
		if (!Const.isEmpty(input.getVersion()))      
			wVersion.setText(input.getVersion());
		
		if (!Const.isEmpty(input.getProcess()) && !Const.isEmpty(input.getProcessId())){
			wProcess.setText(input.getProcess());
			processes = new HashMap<String, String>(1);
			processes.put(input.getProcess(), input.getProcessId());
			setProcesses();
		}
		
		if (!Const.isEmpty(input.getProcess()))
			wProcess.setText(input.getProcess());
		
		if(input.getInputParameters() != null)
			inputParameters = input.getInputParameters();		
		
		if(input.getOutputParameters() != null)
			outputParameters = input.getOutputParameters();	
		
		if(!Const.isEmpty(input.getResultFieldName()))
			wResultFieldName.setText(input.getResultFieldName());
		
		urls = input.getUrls();			

		wOutputType[1].setSelection(input.hasRawDataOutput());
		wOutputType[0].setSelection(!wOutputType[1].getSelection());					
		
		setParameters(false);
		
		wStepname.selectAll();
	}

	private void cancel(){
		stepname=null;
		input.setChanged(changed);
		dispose();
	}
	
	public boolean checkGeneralParameters(){
		boolean error = false;
		if(Const.isEmpty(wUrl.getText()) || method == null || Const.isEmpty(wVersion.getText())){
			error = true;
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);
			mb.setMessage(Messages.getString("WPSClientDialog.ErrorRequiredInformationsGeneral.DialogMessage")); //$NON-NLS-1$
			mb.setText(Messages.getString("WPSClientDialog.ErrorRequiredInformations.DialogTitle")); //$NON-NLS-1$
			mb.open();			
		}
		return error;
	}
	
	public boolean checkProcess(){
		boolean error = false;
		if(Const.isEmpty(wProcess.getText())){
			error = true;
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);
			mb.setMessage(Messages.getString("WPSClientDialog.ErrorRequiredProcess.DialogMessage")); //$NON-NLS-1$
			mb.setText(Messages.getString("WPSClientDialog.ErrorRequiredProcess.DialogTitle")); //$NON-NLS-1$
			mb.open();			
		}
		return error;
	}
	
	public void setMethod(){
		method = wMethod[0].getSelection()?GET:POST;	
	}
    
	public void getProcesses(){
		try {
			ProcessBuilder processBuilder = new ProcessBuilder();
			processes = processBuilder.parseProcesses(new GetCapabilitiesRequestBuilder(wUrl.getText(), method, wVersion.getText()).sendRequest());	
			urls = processBuilder.getUrls();
		} catch (KettleException e) {
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);
			mb.setMessage(Messages.getString("WPSClientDialog.ErrorCannotReadProcesses.DialogMessage"));
			mb.setText(Messages.getString("WPSClientDialog.ErrorCannotReadProcesses.DialogTitle"));
			mb.open();
		}
	}
	
	public void getParameters(){
		try {
			String process = processes.get(wProcess.getText());
			if(!Const.isEmpty(process)){
				String response = new DescribeProcessRequestBuilder(urls, method, wVersion.getText(), process).sendRequest();
				inputParameters = new ParameterBuilder(true).parseParameters(response);
				outputParameters = new ParameterBuilder(false).parseParameters(response);
			}		
		} catch (KettleException e) {
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);
			mb.setMessage(e.getMessage());
			mb.setText(Messages.getString("WPSClientDialog.ErrorGettingParameters.DialogTitle")); //$NON-NLS-1$
			mb.open();
		}
	}
	
	private void updateTooltips(){
		inputTableListener.setParameters(inputParameters);
		outputTableListener.setParameters(outputParameters);
	}
	
	private void setParameters(boolean getNewValues){	
		removeControls(true);
		removeControls(false);
		setParameters(true, getNewValues);//Setting both input and output parameters
		setParameters(false, getNewValues);//Setting both input and output parameters
		setFieldCombos();
		updateTooltips();
	}
	
	private void removeControls(boolean isInput){
		Map<String, Control> controls = isInput?inputControls:outputControls;
		for(String parameterName : controls.keySet()){
			controls.get(parameterName).dispose();//remove all previously added editor-controls
		}
	}
	
	private void setParameters(boolean isInput, boolean getNewValues){
		TableView tableView = isInput?wInputParameters:wOutputParameters;
		TableListener listener = isInput?inputTableListener:outputTableListener;		
		tableView.setRedraw(false);
		tableView.removeAll();
		Map<String, Parameter> parameters = isInput?inputParameters:outputParameters;	
		if(parameters != null && !parameters.isEmpty()){			
			Table table = tableView.getTable();
			table.setItemCount(parameters.size());
			int i = 0;
			for(String name : parameters.keySet()){
				TableItem item = table.getItem(i);
				Parameter parameter = parameters.get(name);
				Boolean isMandatory = parameter.isMandatory();
				item.setText(0, String.valueOf(i));
				item.setText(1, name);
				item.setText(4, !Const.isEmpty(parameter.getValue())?parameter.getValue():"");
				item.setText(5, !Const.isEmpty(parameter.getFieldName())?parameter.getFieldName():"");
				if(isMandatory)
					item.setFont(1, getMandatoryFont());
		        displayParameterInfo(parameter, name, i, isInput, isMandatory, getNewValues);
		        i++;
		    } 
			tableView.setEnabled(true);
			table.addListener(SWT.KeyDown, listener);
		}else
			tableView.setEnabled(false);
		tableView.setRedraw(true);
		listener.setControls(isInput?inputControls:outputControls);
	}
	
	@SuppressWarnings("unchecked")
	private void displayParameterInfo(Parameter parameter, String parameterName, int i, boolean isInput, boolean isMandatory, boolean getNewValues){	
		TableView tableView = isInput?wInputParameters:wOutputParameters;
		Map<String, Map<String, String>> values = getNewValues? null:(isInput?input.getInputValues():input.getOutputValues());
		Map<String, Control> controls = isInput?inputControls:outputControls;	
		Table table = tableView.getTable();
		AbstractFormat defaultFormat = parameter.getDefaultFormat();
		TableItem item = table.getItem(i);
		TableEditor editor = new TableEditor(table);
		editor.grabHorizontal = true;
		int index = 0;
		if(parameter.hasLiteralFormat()){	
			if(defaultFormat != null){
				Map<String, Object> properties = defaultFormat.getProperties();
				List<String> uoms = (List<String>) properties.get(LiteralFormat.SUPPORTED_UOMS);				
			
				Object datatypeObj = properties.get(LiteralFormat.DATATYPE);
				if(datatypeObj != null)
					item.setText(2, datatypeObj.toString());	
					
				if(uoms != null && !uoms.isEmpty() && !(uoms.size() == 1 && Const.isEmpty(uoms.get(0)))){
					CCombo combo = new CCombo(table, SWT.DROP_DOWN | SWT.READ_ONLY);					
					for(String uom : uoms){
						combo.add(uom);
					}
					
					String value = null;
					if(values!= null && values.containsKey(parameterName) && values.get(parameterName).containsKey(LiteralFormat.SUPPORTED_UOMS))				
						value = values.get(parameterName).get(LiteralFormat.SUPPORTED_UOMS);
					else
						value = uoms.get(uoms.indexOf(properties.get(LiteralFormat.DEFAULT_UOM)));																								
					
					if(!Const.isEmpty(value)){
						combo.setText(value);
						item.setText(3, value);
					}
					
					combo.addSelectionListener(new ComboSelectionListener(combo, 3, item));
					
					editor.setEditor(combo, item, 3);
					controls.put(parameterName, combo);
				}else
					item.setText(3, ANY);
				
				Object defaultValueObj = properties.get(LiteralFormat.DEFAULT_VALUE);
				if(isInput){
					if(defaultValueObj != null && Const.isEmpty(item.getText(4)))
						item.setText(4, defaultValueObj.toString());				
				}else
					item.setText(4, ANY);
			}
		}else{ 
			if(parameter.hasBoundingBoxFormat()){
				if(defaultFormat != null){		
					item.setText(2, CRS);
					Map<String, Object> properties = defaultFormat.getProperties();	
					List<String> crss = (List<String>) properties.get(BoundingBoxFormat.SUPPORTED_CRSS);														
					
					if(crss != null && !crss.isEmpty() && !(crss.size() == 1 && Const.isEmpty(crss.get(0)))){
						CCombo combo = new CCombo(table, SWT.NONE);					
						for(String supported_crs : crss){
							combo.add(supported_crs);
						}
						
						String value = null;
						if(values!= null && values.containsKey(parameterName) && values.get(parameterName).containsKey(BoundingBoxFormat.SUPPORTED_CRSS))
							value = values.get(parameterName).get(BoundingBoxFormat.SUPPORTED_CRSS);
						else
							value = crss.get(crss.indexOf(properties.get(BoundingBoxFormat.DEFAULT_CRS)));
						
							
						
						if(!Const.isEmpty(value)){
							combo.setText(value);
							item.setText(4, value);
						}
						
						combo.addSelectionListener(new ComboSelectionListener(combo, 4, item));
						
						editor.setEditor(combo, item, 4);
						controls.put(parameterName, combo);
					}else{
						item.setText(4, ANY);
					}	
				}
			}else{
				List<AbstractFormat> formats = parameter.getSupportedFormats();
				if(defaultFormat!=null){
					Map<String, Object> properties = defaultFormat.getProperties();
					boolean alreadyExists = false;
					Object schemaObj = properties.get(ComplexFormat.SCHEMA);
					Object mimeTypeObj = properties.get(ComplexFormat.MIMETYPE);
					Object encodingObj = properties.get(ComplexFormat.ENCODING);
					for(AbstractFormat format: formats){					
						if(schemaObj!=null && format.getProperties().get(ComplexFormat.SCHEMA)!=null && mimeTypeObj!=null && format.getProperties().get(ComplexFormat.MIMETYPE)!=null && encodingObj !=null && format.getProperties().get(ComplexFormat.ENCODING)!=null){							
							if(schemaObj.toString().equals(format.getProperties().get(ComplexFormat.SCHEMA)) && mimeTypeObj.toString().equals(format.getProperties().get(ComplexFormat.MIMETYPE)) && encodingObj.toString().equals(format.getProperties().get(ComplexFormat.ENCODING))){
								alreadyExists = true;
								index = formats.indexOf(format);
								break;
							}
						}					
					}
					if(!alreadyExists)
						formats.add(0, defaultFormat);
				}
				if(formats != null && !formats.isEmpty()){
					CCombo combo = new CCombo(table, SWT.NONE);
					for(AbstractFormat format : formats){	
						combo.add(concatComplexProps(format.getProperties()));
					}
					String value = concatComplexProps((values!= null && values.containsKey(parameterName))?values.get(parameterName):formats.get(index).getProperties());
										
					if(!Const.isEmpty(value)){
						combo.setText(value);
						item.setText(2, value);
					}
					
					combo.addSelectionListener(new ComboSelectionListener(combo, 2, item));

					editor.setEditor(combo, item, 2);	
					controls.put(parameterName, combo);
				}
				
				if(!isInput)
					item.setText(4, ANY);
			}
			item.setText(3, ANY);
		}	
	}
	
	private String concatComplexProps(Map<String, ?> props){
		Object mimetype_obj = props.get(ComplexFormat.MIMETYPE);
		Object schema_obj = props.get(ComplexFormat.SCHEMA);						
		Object encoding_obj = props.get(ComplexFormat.ENCODING);
		StringBuffer sb = new StringBuffer();
		sb.append(mimetype_obj!=null&&!Const.isEmpty(mimetype_obj.toString())?"MimeType: "+mimetype_obj.toString()+", ":"");
		sb.append(schema_obj!=null&&!Const.isEmpty(schema_obj.toString())?"Schema: " + schema_obj.toString()+", ":"");
		sb.append(encoding_obj!=null&&!Const.isEmpty(encoding_obj.toString())?"Encoding: " + encoding_obj.toString():"");
		if(sb.indexOf(",")==sb.length()-2)
			sb.delete(sb.length()-2, sb.length());
		return sb.toString();
	}
	
	private void getSelectedValue(Map<String, Map<String, String>> values, String paramName, String propertyName, Object propertyValue){
		if(propertyValue!=null && !propertyValue.equals(ANY)){
			if(values.containsKey(paramName))
				values.get(paramName).put(propertyName, propertyValue.toString());
			else{
				Map<String, String> properties = new HashMap<String, String>();
				properties.put(propertyName, propertyValue.toString());
				values.put(paramName, properties);			
			}
		}
	}
	
	private Map<String, Map<String, String>> getSelectedValues(boolean isInput){
		Map<String, Map<String, String>> values = new HashMap<String, Map<String, String>>();
		TableView tableView = isInput?wInputParameters:wOutputParameters;
		Map<String, Parameter> parameters = isInput?inputParameters:outputParameters;	
		TableItem[] items = tableView.getTable().getItems();
		for(int i = 0 ; i < items.length ; i++){
			TableItem item = items[i];
			String paramName = item.getText(1);
			if(parameters.containsKey(paramName)){
				Parameter param = parameters.get(paramName);
				if(param.hasLiteralFormat())
					getSelectedValue(values, paramName, LiteralFormat.SUPPORTED_UOMS, item.getText(3));				
				else if(param.hasBoundingBoxFormat())
					getSelectedValue(values, paramName, BoundingBoxFormat.SUPPORTED_CRSS, item.getText(4));	
				else{
					String mimetype = "";
					String schema = "";
					String encoding= "";
					
					String format = item.getText(2);
					if(!Const.isEmpty(format)){
						
						boolean hasMimetype = false;
						boolean hasSchema = false;
						boolean hasEncoding = false;
						if(format.indexOf("MimeType")>-1)
							hasMimetype = true;
						if(format.indexOf("Schema")>-1)
							hasSchema = true;
						if(format.indexOf("Encoding")>-1)
							hasEncoding = true;
						
						int begIndex;
						int endIndex;
						
						if(hasMimetype){
							begIndex = 10;
							endIndex = hasSchema || hasEncoding ? format.indexOf(","):format.length();
							mimetype = format.substring(begIndex, endIndex);
						}
						
						if(hasSchema){
							begIndex = hasMimetype ? format.indexOf(",")+10:8;
							endIndex = hasEncoding ? format.lastIndexOf(","):format.length();
							schema = format.substring(begIndex, endIndex);
						}
						
						if(hasEncoding){
							begIndex = hasMimetype || hasSchema ? format.lastIndexOf(",")+12:10;
							endIndex = format.length();
							encoding = format.substring(begIndex, endIndex);
						}
					}
					
					getSelectedValue(values, paramName, ComplexFormat.MIMETYPE, mimetype);
					getSelectedValue(values, paramName, ComplexFormat.SCHEMA, schema);
					getSelectedValue(values, paramName, ComplexFormat.ENCODING, encoding);
				}
			}						
		}
		return values;
	}
	
	private Font getMandatoryFont(){
		Font font = Display.getCurrent().getSystemFont();
		FontData[] fds = font.getFontData();
		for (FontData fd : fds){
		    fd.setStyle(SWT.BOLD);
		}
		return new Font(font.getDevice(), fds);
	}
	
	private void setProcesses(){	            		        
		wProcess.removeAll();
		if(processes != null && !processes.isEmpty()){
			for(String process : processes.keySet()){
		        wProcess.add(process);
		    } 
		}
		String[] items = wProcess.getItems();
        Arrays.sort(items);
        wProcess.setItems(items);
	}
	
	protected void getFieldNames(){
        final Map<String, Integer> fields = new HashMap<String, Integer>();        
        fields.putAll(inputFields);      
        List<String> entries = new ArrayList<String>(fields.keySet());        
        fieldNames = (String[]) entries.toArray(new String[entries.size()]);
        Const.sortStrings(fieldNames);
    }
	
	private void setParametersValue(boolean isInput){
		Map<String, Parameter> parameters = isInput?inputParameters:outputParameters;
		TableView tableView = isInput?wInputParameters:wOutputParameters;
		for(String paramName: parameters.keySet()){			
			boolean paramFound = false;
			for(TableItem item: tableView.getTable().getItems()){
				if(paramName.equals(item.getText(1))){
					paramFound = true;		
					Parameter param = parameters.get(paramName);
					String value = item.getText(4);
					param.setValue(!Const.isEmpty(value) && !value.equals(ANY)?value:"");
					String fieldName = item.getText(5);
					param.setFieldName(!Const.isEmpty(fieldName) && !fieldName.equals(ANY)?fieldName:"");
					break;
				}			
			}
			if(!paramFound){
				parameters.remove(paramName);
			}
		}				
	}
	
	private void ok(){
		if (Const.isEmpty(wStepname.getText())) 
			return;			
		input.setUrl(wUrl.getText());
		input.setMethod(method);
		input.setVersion(wVersion.getText());
		if(!Const.isEmpty(wProcess.getText())){
			input.setProcess(wProcess.getText());
			input.setProcessId(processes.get(wProcess.getText()));
		}

		if(inputParameters != null){
			setParametersValue(true);
			input.setInputParameters(inputParameters);
			input.setInputValues(getSelectedValues(true));
		}

		if(outputParameters != null){
			setParametersValue(false);
			input.setOutputParameters(outputParameters);
			input.setOutputValues(getSelectedValues(false));
		}
		
		if(!Const.isEmpty(wResultFieldName.getText()))
			input.setResultFieldName(wResultFieldName.getText());
		
		input.setUrls(urls);
		
		input.setHasRawDataOutput(wOutputType[1].getSelection());
		
		stepname = wStepname.getText();

		dispose();
	}
}
