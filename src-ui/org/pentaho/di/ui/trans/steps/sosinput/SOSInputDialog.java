package org.pentaho.di.ui.trans.steps.sosinput;

/*
 * Created on 18-february-2010
 * 
 * by: jmathieu
 *
 */

import java.net.URL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.geospatial.SOSReader;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.sosinput.Messages;
import org.pentaho.di.trans.steps.sosinput.SOSInputMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.ComboVar;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

public class SOSInputDialog extends BaseStepDialog implements StepDialogInterface{	
	
	private Label        			wlUrl;
	private TextVar      			wUrl;
	private FormData     			fdlUrl, fdUrl;
	
	private Label        			wlDifferentUrlObs;
    private Button       			wDifferentUrlObs;
    private FormData     			fdlDifferentUrlObs, fdDifferentUrlObs;
    
	private Label        			wlUrlObs;
	private TextVar      			wUrlObs;
	private FormData     			fdlUrlObs, fdUrlObs;
	
	private Label        			wlTime1;
	private TextVar      			wTime1;
	private FormData     			fdlTime1, fdTime1;
	
	private Label        			wlTime1InField;
    private Button       			wTime1InField;
    private FormData     			fdlTime1InField, fdTime1InField;
	
	private Label        			wlTime1Field;
	private ComboVar     			wTime1Field;
	private FormData     			fdlTime1Field, fdTime1Field;
	
	private Label        			wlTime2;
	private TextVar      			wTime2;
	private FormData     			fdlTime2, fdTime2;
	
	private Label        			wlTime2InField;
    private Button       			wTime2InField;
    private FormData     			fdlTime2InField, fdTime2InField;
	
	private Label        			wlTime2Field;
	private ComboVar     			wTime2Field;
	private FormData     			fdlTime2Field, fdTime2Field;
	
    private Label       			wlRequest;
    private ComboVar				wRequest;
    private FormData    			fdlRequest, fdRequest;  
    
    private Label       			wlMethod;
    private Button[]				wMethod;
    private FormData    			fdlMethod, fdMethod, fdMethod2; 
    private Listener				lsMethod;
    
    private Label       			wlVersion;
    private ComboVar				wVersion;
    private FormData    			fdlVersion, fdVersion; 
    
    private Button					wGetOfferings;
    private Label       			wlOfferings;
    private ComboVar				wOfferings;
    private FormData    			fdlOfferings, fdOfferings, fdGetOfferings;   
    private Listener				lsGetOfferings;
    
    private Button					wGetProcedures;
    private Label        			wlProcedures;
	private TableView    			wProcedures;
	private FormData     			fdlProcedures, fdProcedures, fdGetProcedures;
	private Listener				lsGetProcedures;
	
	private Button					wGetObservedProperties;
	private Label        			wlObservedProperties;
	private TableView    			wObservedProperties;
	private FormData     			fdlObservedProperties, fdObservedProperties, fdGetObservedProperties;
	private Listener				lsGetObservedProperties;     
    
	private SOSInputMeta 			input;   
	
    private Group 					wGetObservation, wGeneral;    
    private FormData 				fdGetObservation, fdGeneral;
       
    private boolean 				gotPreviousFields=false;
       
    private Map<String, Integer> 	inputFields;
    private String 					fieldNames[];
    
    private String					method;
    private String[] 				offerings;
    private String[] 				observedProperties;
    private String[] 				procedures;
    
	public SOSInputDialog(Shell parent, Object in, TransMeta transMeta, String sname){
		super(parent, (BaseStepMeta)in, transMeta, sname);
		input=(SOSInputMeta)in;
		inputFields =new HashMap<String, Integer>();
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
		
		ModifyListener lsClearGetObsCombos = new ModifyListener(){
			public void modifyText(ModifyEvent e){				
				clearObservedProperties();
				clearProcedures();
			}
		};
        
		changed = input.hasChanged();

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(Messages.getString("SOSInputDialog.Shell.Title")); //$NON-NLS-1$
		
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
 		
		wGeneral = new Group(shell, SWT.SHADOW_NONE);
		props.setLook(wGeneral);
		wGeneral.setText(Messages.getString("SOSInputDialog.General.Tab"));
		FormLayout GeneralgroupLayout = new FormLayout();
		GeneralgroupLayout.marginWidth = 10;
		GeneralgroupLayout.marginHeight = 10;
		wGeneral.setLayout(GeneralgroupLayout);
		
		wlUrl=new Label(wGeneral, SWT.RIGHT);
		wlUrl.setText(Messages.getString("SOSInputDialog.URL.Label")); //$NON-NLS-1$
 		props.setLook(wlUrl);
		fdlUrl=new FormData();
		fdlUrl.left = new FormAttachment(0, 0);
		fdlUrl.top  = new FormAttachment(wStepname, 0);
		fdlUrl.right= new FormAttachment(middle, -margin);	
		wlUrl.setLayoutData(fdlUrl);

		wUrl=new TextVar(transMeta, wGeneral, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wUrl);
		wUrl.addModifyListener(lsMod);
		fdUrl=new FormData();
		fdUrl.left = new FormAttachment(middle, 0);
		fdUrl.top  = new FormAttachment(wStepname, -margin);
		fdUrl.right= new FormAttachment(100, -margin);
		wUrl.setLayoutData(fdUrl);     
        
		//method		
		wMethod = new Button[2];
		wMethod[0] = new Button(wGeneral, SWT.RADIO);
		wMethod[0].setText("GET");
		fdMethod2 = new FormData();
		fdMethod2.left = new FormAttachment(middle, 0);
		fdMethod2.top = new FormAttachment(wUrl,2* margin);
		wMethod[0].setLayoutData(fdMethod2);
		lsMethod = new Listener()  {public void handleEvent(Event e){input.setChanged();setMethod();}	};
		wMethod[0].addListener(SWT.Selection, lsMethod);
		wMethod[1] = new Button(wGeneral, SWT.RADIO);
		wMethod[1].setText("POST");
		fdMethod = new FormData();
		fdMethod.left   = new FormAttachment(wMethod[0], 4*margin);
		fdMethod.top = new FormAttachment(wUrl,2*margin);
		wMethod[1].setLayoutData(fdMethod);		
		wMethod[1].addListener(SWT.Selection, lsMethod);
		
		wlMethod=new Label(wGeneral, SWT.RIGHT);
        wlMethod.setText(Messages.getString("SOSInputDialog.Method.Label"));
        props.setLook(wlMethod);
        fdlMethod=new FormData();
        fdlMethod.left = new FormAttachment(0, 0);
        fdlMethod.top  = new FormAttachment(wUrl, margin*3);
        fdlMethod.right= new FormAttachment(middle, -2*margin);
        wlMethod.setLayoutData(fdlMethod);

        //Version
        wlVersion=new Label(wGeneral, SWT.RIGHT);
        wlVersion.setText(Messages.getString("SOSInputDialog.Version.Label"));
        props.setLook(wlVersion);
        fdlVersion=new FormData();
        fdlVersion.left = new FormAttachment(0, 0);
        fdlVersion.top  = new FormAttachment(wMethod[0], margin*3);
        fdlVersion.right= new FormAttachment(middle, -2*margin);
        wlVersion.setLayoutData(fdlVersion);
        wVersion=new ComboVar(transMeta, wGeneral, SWT.BORDER | SWT.READ_ONLY);
        wVersion.setEditable(false);
        props.setLook(wVersion);
        fdVersion=new FormData();
        fdVersion.left = new FormAttachment(middle, 0);
        fdVersion.top  = new FormAttachment(wMethod[0], margin*2);
        fdVersion.right= new FormAttachment(100, -margin);
        wVersion.setLayoutData(fdVersion);
        wVersion.add("1.0.0");
        wVersion.addModifyListener(lsMod);
        
        //request
        wlRequest=new Label(wGeneral, SWT.RIGHT);
        wlRequest.setText(Messages.getString("SOSInputDialog.Request.Label"));
        props.setLook(wlRequest);
        fdlRequest=new FormData();
        fdlRequest.left = new FormAttachment(0, 0);
        fdlRequest.top  = new FormAttachment(wVersion, margin*2);
        fdlRequest.right= new FormAttachment(middle, -2*margin);
        wlRequest.setLayoutData(fdlRequest);
        wRequest=new ComboVar(transMeta, wGeneral, SWT.BORDER | SWT.READ_ONLY);
        wRequest.setEditable(false);
        props.setLook(wRequest);
        fdRequest=new FormData();
        fdRequest.left = new FormAttachment(middle, 0);
        fdRequest.top  = new FormAttachment(wVersion, margin);
        fdRequest.right= new FormAttachment(100, -margin);
        wRequest.setLayoutData(fdRequest);
        wRequest.add("GetObservation");
        wRequest.addModifyListener(lsMod);
        wRequest.addModifyListener( new ModifyListener() {
            public void modifyText(ModifyEvent arg0) {              
            	if (wRequest.getText().equals("GetObservation")){
            		wGetObservation.setEnabled(true);
            	}else{
            		wGetObservation.setEnabled(false);
            	}
            }
        });
 
		fdGeneral = new FormData();
		fdGeneral.left  = new FormAttachment(0, margin);
		fdGeneral.top   = new FormAttachment(wStepname, margin);
		fdGeneral.right = new FormAttachment(100, -margin);
		wGeneral.setLayoutData(fdGeneral);
				
		//////////////////////////
		// Start OF GetObs Group   ///
		//////////////////////////				
       		
		wGetObservation = new Group(shell, SWT.SHADOW_NONE);
		props.setLook(wGetObservation);
		wGetObservation.setText(Messages.getString("SOSInputDialog.GetObservation.Tab"));
		FormLayout GetObservationgroupLayout = new FormLayout();
		GetObservationgroupLayout.marginWidth = 10;
		GetObservationgroupLayout.marginHeight = 10;
		wGetObservation.setLayout(GetObservationgroupLayout);
		
		//Different URL
        wlDifferentUrlObs=new Label(wGetObservation, SWT.RIGHT);
        wlDifferentUrlObs.setText(Messages.getString("SOSInputDialog.DifferentUrlObs.Label"));
        props.setLook(wlDifferentUrlObs);
        fdlDifferentUrlObs=new FormData();
        fdlDifferentUrlObs.left = new FormAttachment(0, 0);
        fdlDifferentUrlObs.top  = new FormAttachment(wRequest, 0);
        fdlDifferentUrlObs.right= new FormAttachment(middle, -margin);
        wlDifferentUrlObs.setLayoutData(fdlDifferentUrlObs);
        wDifferentUrlObs=new Button(wGetObservation, SWT.CHECK );
        props.setLook(wDifferentUrlObs);
        fdDifferentUrlObs=new FormData();
        fdDifferentUrlObs.left = new FormAttachment(middle, 0);
        fdDifferentUrlObs.top  = new FormAttachment(wRequest, -margin);
        fdDifferentUrlObs.right= new FormAttachment(100, -margin);
        wDifferentUrlObs.setLayoutData(fdDifferentUrlObs);
        wDifferentUrlObs.addSelectionListener(new SelectionAdapter() 
            {
                public void widgetSelected(SelectionEvent e) 
                {
                	input.setChanged();
                	activeDifferentUrlObs();
                }
            }
        );
        
		//GetObservation URL	 
		wlUrlObs=new Label(wGetObservation, SWT.RIGHT);
		wlUrlObs.setText(Messages.getString("SOSInputDialog.URLObs.Label")); //$NON-NLS-1$
		props.setLook(wlUrlObs);
		fdlUrlObs=new FormData();
		fdlUrlObs.left = new FormAttachment(0, 0);
        fdlUrlObs.top  = new FormAttachment(wDifferentUrlObs, 2*margin);
        fdlUrlObs.right= new FormAttachment(middle, -margin);
		wlUrlObs.setLayoutData(fdlUrlObs);
		wUrlObs=new TextVar(transMeta, wGetObservation, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wUrlObs.setToolTipText(Messages.getString("SOSInputDialog.URLObs.Tooltip")); //$NON-NLS-1$
		props.setLook(wUrlObs);
		wUrlObs.addModifyListener(lsMod);
		fdUrlObs=new FormData();
		fdUrlObs.left = new FormAttachment(middle, 0);
        fdUrlObs.top  = new FormAttachment(wDifferentUrlObs, margin);
        fdUrlObs.right= new FormAttachment(100, -margin);
		wUrlObs.setLayoutData(fdUrlObs);  
		
		//offerings		
		wGetOfferings = new Button(wGetObservation, SWT.PUSH);
        wGetOfferings.setText(Messages.getString("SOSInputDialog.GetOfferings.Button")); //$NON-NLS-1$
		fdGetOfferings = new FormData();
		fdGetOfferings.right   = new FormAttachment(100, -margin);
		fdGetOfferings.top = new FormAttachment(wUrlObs,2*margin);
		wGetOfferings.setLayoutData(fdGetOfferings);
		lsGetOfferings = new Listener()  {public void handleEvent(Event e){setOfferings();}	};
		wGetOfferings.addListener(SWT.Selection, lsGetOfferings);
		
		wlOfferings=new Label(wGetObservation, SWT.RIGHT);
        wlOfferings.setText(Messages.getString("SOSInputDialog.Offerings.Label"));
        props.setLook(wlOfferings);
        fdlOfferings=new FormData();
        fdlOfferings.left = new FormAttachment(0, 0);
        fdlOfferings.top  = new FormAttachment(wUrlObs, 3*margin);
        fdlOfferings.right= new FormAttachment(middle, -margin);
        wlOfferings.setLayoutData(fdlOfferings);
        wOfferings=new ComboVar(transMeta, wGetObservation, SWT.BORDER | SWT.READ_ONLY);
        wOfferings.setEditable(false);
        props.setLook(wOfferings);
        wOfferings.addModifyListener(lsMod);
        wOfferings.addModifyListener(lsClearGetObsCombos);
        fdOfferings=new FormData();
        fdOfferings.left = new FormAttachment(middle, 0);
        fdOfferings.top  = new FormAttachment(wUrlObs, 2*margin);
        fdOfferings.right= new FormAttachment(wGetOfferings, -margin);
        wOfferings.setLayoutData(fdOfferings);
		
        //observedProperties
        wGetObservedProperties = new Button(wGetObservation, SWT.PUSH);
        wGetObservedProperties.setToolTipText(Messages.getString("SOSInputDialog.GetObservedProperties.Tooltip"));
        wGetObservedProperties.setText(Messages.getString("SOSInputDialog.GetObservedProperties.Button")); //$NON-NLS-1$
		fdGetObservedProperties = new FormData();
		fdGetObservedProperties.right   = new FormAttachment(100, -margin);
		fdGetObservedProperties.top = new FormAttachment(wOfferings, 2*margin);
		wGetObservedProperties.setLayoutData(fdGetObservedProperties);
		lsGetObservedProperties = new Listener()  {public void handleEvent(Event e){setObservedProperties();}	};
		wGetObservedProperties.addListener(SWT.Selection, lsGetObservedProperties);
		
		wlObservedProperties=new Label(wGetObservation, SWT.RIGHT);
		wlObservedProperties.setText(Messages.getString("SOSInputDialog.ObservedProperties.Label"));
 		props.setLook(wlObservedProperties);
		fdlObservedProperties=new FormData();
		fdlObservedProperties.left = new FormAttachment(0, 0);
		fdlObservedProperties.top  = new FormAttachment(wOfferings, 3*margin);
		fdlObservedProperties.right = new FormAttachment(middle, -margin);
		wlObservedProperties.setLayoutData(fdlObservedProperties);
		
		ColumnInfo[] colinfObservedProperties=new ColumnInfo[1];
		colinfObservedProperties[0]=new ColumnInfo(Messages.getString("SOSInputDialog.ObservedProperties.Label"),  
				ColumnInfo.COLUMN_TYPE_TEXT,null, false);
		wObservedProperties=new TableView(transMeta,wGetObservation,
							  SWT.BORDER | SWT.MULTI, 
							  colinfObservedProperties, 
							  1,  
							  lsMod,
							  props
							  );
		fdObservedProperties=new FormData();
		fdObservedProperties.left  = new FormAttachment(middle, 0);
		fdObservedProperties.top   = new FormAttachment(wOfferings, 2*margin);
		fdObservedProperties.right = new FormAttachment(wGetObservedProperties, -margin);
		wObservedProperties.setLayoutData(fdObservedProperties);
        
        //Procedures
        wGetProcedures = new Button(wGetObservation, SWT.PUSH);
        wGetProcedures.setToolTipText(Messages.getString("SOSInputDialog.GetProcedures.Tooltip"));
        wGetProcedures.setText(Messages.getString("SOSInputDialog.GetProcedures.Button")); //$NON-NLS-1$
		fdGetProcedures = new FormData();
		fdGetProcedures.right   = new FormAttachment(100, -margin);
		fdGetProcedures.top = new FormAttachment(wObservedProperties, 2*margin);
		wGetProcedures.setLayoutData(fdGetProcedures);
		lsGetProcedures = new Listener()  {public void handleEvent(Event e){setProcedures();}	};
		wGetProcedures.addListener(SWT.Selection, lsGetProcedures);
			
		wlProcedures=new Label(wGetObservation, SWT.RIGHT);
		wlProcedures.setText(Messages.getString("SOSInputDialog.Procedures.Label"));
 		props.setLook(wlProcedures);
		fdlProcedures=new FormData();
		fdlProcedures.left = new FormAttachment(0, 0);
		fdlProcedures.top  = new FormAttachment(wObservedProperties, 3*margin);
		fdlProcedures.right = new FormAttachment(middle, -margin);
		wlProcedures.setLayoutData(fdlProcedures);
		
		ColumnInfo[] colinfProcedures=new ColumnInfo[1];
		colinfProcedures[0]=new ColumnInfo(Messages.getString("SOSInputDialog.Procedures.Label"),  
				ColumnInfo.COLUMN_TYPE_TEXT,null, false);
		wProcedures=new TableView(transMeta,wGetObservation,
							  SWT.BORDER | SWT.MULTI, 
							  colinfProcedures, 
							  1,  
							  lsMod,
							  props
							  );
		fdProcedures=new FormData();
		fdProcedures.left  = new FormAttachment(middle, 0);
		fdProcedures.top   = new FormAttachment(wObservedProperties, 2*margin);
		fdProcedures.right = new FormAttachment(wGetProcedures, -margin);
		wProcedures.setLayoutData(fdProcedures);
		
        //time bound 1
		wlTime1=new Label(wGetObservation, SWT.RIGHT);
		wlTime1.setText(Messages.getString("SOSInputDialog.Time1.Label")); //$NON-NLS-1$
 		props.setLook(wlTime1);
		fdlTime1=new FormData();
		fdlTime1.left = new FormAttachment(0, 0);
		fdlTime1.right= new FormAttachment(middle, -margin);
		fdlTime1.top  = new FormAttachment(wProcedures, margin*3);
		wlTime1.setLayoutData(fdlTime1);
		wTime1=new TextVar(transMeta, wGetObservation, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		wTime1.setToolTipText(Messages.getString("SOSInputDialog.Time1.ToolTip"));
		props.setLook(wTime1);
		wTime1.addModifyListener(lsMod);
		fdTime1=new FormData();
		fdTime1.left = new FormAttachment(middle, 0);
		fdTime1.top  = new FormAttachment(wProcedures, margin*2);
		fdTime1.right= new FormAttachment(100, -margin);
		wTime1.setLayoutData(fdTime1);
				
		// Time1InField
        wlTime1InField=new Label(wGetObservation, SWT.RIGHT);
        wlTime1InField.setText(Messages.getString("SOSInputDialog.Time1InField.Label"));
        props.setLook(wlTime1InField);
        fdlTime1InField=new FormData();
        fdlTime1InField.left = new FormAttachment(0, 0);
        fdlTime1InField.top  = new FormAttachment(wTime1, margin*2);
        fdlTime1InField.right= new FormAttachment(middle, -margin);
        wlTime1InField.setLayoutData(fdlTime1InField);
        wTime1InField=new Button(wGetObservation, SWT.CHECK );
        props.setLook(wTime1InField);
        fdTime1InField=new FormData();
        fdTime1InField.left = new FormAttachment(middle, 0);
        fdTime1InField.top  = new FormAttachment(wTime1, margin);
        fdTime1InField.right= new FormAttachment(100, 0);
        wTime1InField.setLayoutData(fdTime1InField);
        wTime1InField.addSelectionListener(new SelectionAdapter() 
            {
                public void widgetSelected(SelectionEvent e) 
                {
                	input.setChanged();
                	activeTime1Infield();
                }
            }
        );

		// Time1Field
		wlTime1Field=new Label(wGetObservation, SWT.RIGHT);
		wlTime1Field.setText(Messages.getString("SOSInputDialog.Time1Field.Label")); //$NON-NLS-1$
 		props.setLook(wlTime1Field);
		fdlTime1Field=new FormData();
		fdlTime1Field.left = new FormAttachment(0, 0);
		fdlTime1Field.right= new FormAttachment(middle, -margin);
		fdlTime1Field.top  = new FormAttachment(wTime1InField, margin*2);
		wlTime1Field.setLayoutData(fdlTime1Field);
		
        wTime1Field=new ComboVar(transMeta, wGetObservation, SWT.BORDER | SWT.READ_ONLY);
        wTime1Field.setEditable(true);
        props.setLook(wTime1Field);
        wTime1Field.addModifyListener(lsMod);
        fdTime1Field=new FormData();
        fdTime1Field.left = new FormAttachment(middle, 0);
        fdTime1Field.top  = new FormAttachment(wTime1InField, margin);
        fdTime1Field.right= new FormAttachment(100, -margin);
        wTime1Field.setLayoutData(fdTime1Field);
        wTime1Field.addFocusListener(new FocusListener(){
                public void focusLost(org.eclipse.swt.events.FocusEvent e){
                }
            
                public void focusGained(org.eclipse.swt.events.FocusEvent e){
                    Cursor busy = new Cursor(wGetObservation.getDisplay(), SWT.CURSOR_WAIT);
                    wGetObservation.setCursor(busy);
                    setStreamFields();
                    wGetObservation.setCursor(null);
                    busy.dispose();
                }
            }
        ); 
        
		//time bound 2
		wlTime2=new Label(wGetObservation, SWT.RIGHT);
		wlTime2.setText(Messages.getString("SOSInputDialog.Time2.Label")); //$NON-NLS-1$
 		props.setLook(wlTime2);
		fdlTime2=new FormData();
		fdlTime2.left = new FormAttachment(0, 0);
		fdlTime2.right= new FormAttachment(middle, -margin);
		fdlTime2.top  = new FormAttachment(wTime1Field, margin*3);
		wlTime2.setLayoutData(fdlTime2);
		wTime2=new TextVar(transMeta, wGetObservation, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wTime2.setToolTipText(Messages.getString("SOSInputDialog.Time2.ToolTip"));
		props.setLook(wTime2);
		wTime2.addModifyListener(lsMod);
		fdTime2=new FormData();
		fdTime2.left = new FormAttachment(middle, 0);
		fdTime2.top  = new FormAttachment(wTime1Field, margin*2);
		fdTime2.right= new FormAttachment(100, -margin);
		wTime2.setLayoutData(fdTime2);
		
		// Time2InField
        wlTime2InField=new Label(wGetObservation, SWT.RIGHT);
        wlTime2InField.setText(Messages.getString("SOSInputDialog.Time2InField.Label"));
        props.setLook(wlTime2InField);
        fdlTime2InField=new FormData();
        fdlTime2InField.left = new FormAttachment(0, 0);
        fdlTime2InField.top  = new FormAttachment(wTime2, margin*2);
        fdlTime2InField.right= new FormAttachment(middle, -margin);
        wlTime2InField.setLayoutData(fdlTime2InField);
        wTime2InField=new Button(wGetObservation, SWT.CHECK );
        props.setLook(wTime2InField);
        fdTime2InField=new FormData();
        fdTime2InField.left = new FormAttachment(middle, 0);
        fdTime2InField.top  = new FormAttachment(wTime2, margin);
        fdTime2InField.right= new FormAttachment(100, 0);
        wTime2InField.setLayoutData(fdTime2InField);
        wTime2InField.addSelectionListener(new SelectionAdapter() 
            {
                public void widgetSelected(SelectionEvent e) 
                {
                	input.setChanged();
                	activeTime2Infield();
                }
            }
        );

		// Time2Field
		wlTime2Field=new Label(wGetObservation, SWT.RIGHT);
		wlTime2Field.setText(Messages.getString("SOSInputDialog.Time2Field.Label")); //$NON-NLS-1$
 		props.setLook(wlTime2Field);
		fdlTime2Field=new FormData();
		fdlTime2Field.left = new FormAttachment(0, 0);
		fdlTime2Field.right= new FormAttachment(middle, -margin);
		fdlTime2Field.top  = new FormAttachment(wTime2InField, margin*2);
		wlTime2Field.setLayoutData(fdlTime2Field);
		
        wTime2Field=new ComboVar(transMeta, wGetObservation, SWT.BORDER | SWT.READ_ONLY);
        wTime2Field.setEditable(true);
        props.setLook(wTime2Field);
        wTime2Field.addModifyListener(lsMod);
        fdTime2Field=new FormData();
        fdTime2Field.left = new FormAttachment(middle, 0);
        fdTime2Field.top  = new FormAttachment(wTime2InField, margin);
        fdTime2Field.right= new FormAttachment(100, -margin);
        wTime2Field.setLayoutData(fdTime2Field);
        wTime2Field.addFocusListener(new FocusListener(){
                public void focusLost(org.eclipse.swt.events.FocusEvent e){
                }
            
                public void focusGained(org.eclipse.swt.events.FocusEvent e){
                    Cursor busy = new Cursor(wGetObservation.getDisplay(), SWT.CURSOR_WAIT);
                    wGetObservation.setCursor(busy);
                    setStreamFields();
                    wGetObservation.setCursor(null);
                    busy.dispose();
                }
            }
        );
        
		fdGetObservation = new FormData();
		fdGetObservation.left = new FormAttachment(0, margin);
		fdGetObservation.top = new FormAttachment(wGeneral, margin);
		fdGetObservation.right = new FormAttachment(100, -margin);
		wGetObservation.setLayoutData(fdGetObservation);
       		
		// ///////////////////////////////////////////////////////////
		// / END OF GetObs Group
		// ///////////////////////////////////////////////////////////					
	
        // Search the fields in the background      
        final Runnable runnable = new Runnable(){
            public void run(){
                StepMeta stepMeta = transMeta.findStep(stepname);
                if (stepMeta!=null){
                    try{
                    	RowMetaInterface row = transMeta.getPrevStepFields(stepMeta);
                        
                        // Remember these fields...
                        for (int i=0;i<row.size();i++){
                        	inputFields.put(row.getValueMeta(i).getName(), Integer.valueOf(i));
                        }                       
                        getFieldNames();
                    }
                    catch(KettleException e){
                    	log.logError(toString(), Messages.getString("System.Dialog.GetFieldsFailed.Message"));
                    }
                }
            }
        };
        new Thread(runnable).start();
        
		// THE BUTTONS
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(Messages.getString("System.Button.OK"));
		
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(Messages.getString("System.Button.Cancel"));
		
		setButtonPositions(new Button[] { wOK, wCancel }, margin, wGetObservation);

		// Add listeners
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel();     } };
		
		wOK.addListener     (SWT.Selection, lsOK     );
		wCancel.addListener (SWT.Selection, lsCancel );
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
        wUrl.addSelectionListener( lsDef );
		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );
		
		// Set the shell size, based upon previous time...
		setSize();	
		getData();
		setMethod();
		activeDifferentUrlObs();
		activeTime1Infield();
		activeTime2Infield();
		input.setChanged(changed);

		shell.open();
		while (!shell.isDisposed()){
			if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}

	public void clearObservedProperties(){
		observedProperties=null;
		wObservedProperties.removeAll();				
	}
	
	public void clearProcedures(){
		procedures=null;
		wProcedures.removeAll();				
	}
	
	public void getData(){		
		if (!Const.isEmpty(input.getUrl()))      wUrl.setText(input.getUrl().toString());
		if (!Const.isEmpty(input.getMethod())) {
			if (input.getMethod().equals("GET")) wMethod[0].setSelection(true);
			if (input.getMethod().equals("POST")) wMethod[1].setSelection(true);
		}
		if (!Const.isEmpty(input.getSOSVersion()))      wVersion.setText(input.getSOSVersion());
		if (!Const.isEmpty(input.getRequest()))      wRequest.setText(input.getRequest());
		if (!Const.isEmpty(input.getOffering()))      wOfferings.setText(input.getOffering());
		if (input.getObservedProperties()!=null){
			observedProperties = input.getObservedProperties();
			wObservedProperties.removeAll();
	        for(int i = 0; i < observedProperties.length; i++){
	        		wObservedProperties.add(observedProperties[i]);
	        }       
	        wObservedProperties.removeEmptyRows();
	        wObservedProperties.setRowNums();	
		}
		if (input.getProcedures()!=null){
			procedures = input.getProcedures();
			wProcedures.removeAll();
	        for(int i = 0; i < procedures.length; i++){
	        		wProcedures.add(procedures[i]);
	        }       
	        wProcedures.removeEmptyRows();
	        wProcedures.setRowNums();	
		}
		wDifferentUrlObs.setSelection(input.isDifferentUrlObs());
		if (!Const.isEmpty(input.getUrlObs())) wUrlObs.setText(input.getUrlObs());
		if (!Const.isEmpty(input.getTime1()))      wTime1.setText(input.getTime1());
		wTime1InField.setSelection(input.isTime1InField());
		if (!Const.isEmpty(input.getTime1Field())) wTime1Field.setText(input.getTime1Field());
		if (!Const.isEmpty(input.getTime2()))      wTime2.setText(input.getTime2());
		wTime2InField.setSelection(input.isTime2InField());
		if (!Const.isEmpty(input.getTime2Field())) wTime2Field.setText(input.getTime2Field());

		wStepname.selectAll();
	}

	private void cancel(){
		stepname=null;
		input.setChanged(changed);
		dispose();
	}
	
	public boolean checkGeneralParameters(){
		return Const.isEmpty(wUrl.getText()) || method == null || Const.isEmpty(wVersion.getText()) || Const.isEmpty(wRequest.getText()) ? false:true;
	}
	
	public boolean checkRequestParameters(){
		if (wRequest.getText().equals(Messages.getString("SOSInputDialog.Request.GetObservation"))){
			if (Const.isEmpty(wOfferings.getText()) || wObservedProperties.table.getItemCount()==0 || (wObservedProperties.getItemCount()==1 && Const.isEmpty(wObservedProperties.getItem(0)[0])))				
				return false;
		}
		return true;
	}
	
	public boolean checkOfferingParameters(){
		return Const.isEmpty(wUrl.getText()) || method == null || Const.isEmpty(wVersion.getText()) ? false:true;
	}
	
	public boolean checkObservedPropertiesParameters(){
		return Const.isEmpty(wUrl.getText()) || method == null || Const.isEmpty(wVersion.getText()) || Const.isEmpty(wOfferings.getText()) ? false:true;
	}
	
	public boolean checkProceduresParameters(){
		return Const.isEmpty(wUrl.getText()) || method == null || Const.isEmpty(wVersion.getText()) || Const.isEmpty(wOfferings.getText()) ? false:true;
	}
	
	public void getOfferings(){
		SOSReader reader;		
    	try {
			reader = new SOSReader(new URL(wUrl.getText()), method, wVersion.getText());
			offerings = reader.getOfferings();
    	} catch (Exception e) {
			new ErrorDialog(shell, Messages.getString("SOSInputDialog.ErrorGetCapabilities.DialogTitle"), Messages.getString("SOSInputDialog.ErrorGetCapabilities.DialogMessage"), e);
		}
	}
	
	public void setMethod(){
		if (wMethod[0].getSelection() || wMethod[1].getSelection()){
			if (wMethod[0].getSelection()) method = "GET";
			if (wMethod[1].getSelection()) method = "POST";
		}else
			method = null;		
	}
	
	public void getObservedProperties(){
		SOSReader reader;		
    	try {
			reader = new SOSReader(new URL(wUrl.getText()), method, wVersion.getText());
			observedProperties = reader.getObservedProperties(wOfferings.getText());		
    	} catch (Exception e) {
			new ErrorDialog(shell, Messages.getString("SOSInputDialog.ErrorGetCapabilities.DialogTitle"), Messages.getString("SOSInputDialog.ErrorGetCapabilities.DialogMessage"), e);
		}
	}	
	
	public void getProcedures(){
		SOSReader reader;		
    	try {
			reader = new SOSReader(new URL(wUrl.getText()), method, wVersion.getText());
			procedures = reader.getProcedures(wOfferings.getText());		
    	} catch (Exception e) {
			new ErrorDialog(shell, Messages.getString("SOSInputDialog.ErrorGetCapabilities.DialogTitle"), Messages.getString("SOSInputDialog.ErrorGetCapabilities.DialogMessage"), e);
		}
	}
	
	private void setOfferings(){		
		if (!checkOfferingParameters()){			
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);
			mb.setMessage(Messages.getString("SOSInputDialog.ErrorRequiredInformationsOfferings.DialogMessage")); //$NON-NLS-1$
			mb.setText(Messages.getString("SOSInputDialog.ErrorRequiredInformations.DialogTitle")); //$NON-NLS-1$
			mb.open();
			return;
		}
        getOfferings();       		        
        wOfferings.removeAll();
        for(int i = 0; i < offerings.length; i++){
        	wOfferings.add(offerings[i]);
        }
        wOfferings.select(0);
	}
	
	private void setProcedures(){		
		if (!checkProceduresParameters()){			
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);
			mb.setMessage(Messages.getString("SOSInputDialog.ErrorRequiredInformationsProcedures.DialogMessage")); //$NON-NLS-1$
			mb.setText(Messages.getString("SOSInputDialog.ErrorRequiredInformations.DialogTitle")); //$NON-NLS-1$
			mb.open();
			return;
		}
        getProcedures();       		        
        wProcedures.removeAll();
        for(int i = 0; i < procedures.length; i++){
        		wProcedures.add(procedures[i]);
        }       
        wProcedures.removeEmptyRows();
        wProcedures.setRowNums();
	}
	
	private void setObservedProperties(){    
		if (!checkObservedPropertiesParameters()){			
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);
			mb.setMessage(Messages.getString("SOSInputDialog.ErrorRequiredInformationsObservedProperties.DialogMessage")); //$NON-NLS-1$
			mb.setText(Messages.getString("SOSInputDialog.ErrorRequiredInformations.DialogTitle")); //$NON-NLS-1$
			mb.open();
			return;
		}
		getObservedProperties();
		wObservedProperties.removeAll();
		for(int i = 0; i < observedProperties.length; i++){
     		wObservedProperties.add(observedProperties[i]);
	    }       
	    wObservedProperties.removeEmptyRows();
	    wObservedProperties.setRowNums();
	}
	
	private void activeTime1Infield(){
		wlTime1Field.setEnabled(wTime1InField.getSelection());
		wTime1Field.setEnabled(wTime1InField.getSelection());
		wlTime1.setEnabled(!wTime1InField.getSelection());
		wTime1.setEnabled(!wTime1InField.getSelection());       
	}
	
	private void activeTime2Infield(){
		wlTime2Field.setEnabled(wTime2InField.getSelection());
		wTime2Field.setEnabled(wTime2InField.getSelection());
		wlTime2.setEnabled(!wTime2InField.getSelection());
		wTime2.setEnabled(!wTime2InField.getSelection());       
	}
	
	private void activeDifferentUrlObs(){
		wlUrlObs.setEnabled(wDifferentUrlObs.getSelection());
		wUrlObs.setEnabled(wDifferentUrlObs.getSelection());   
	}
	
	private void setStreamFields(){
		if(!gotPreviousFields){
			wTime1Field.removeAll();
			wTime1Field.setItems(fieldNames);								
			wTime2Field.removeAll();
			wTime2Field.setItems(fieldNames);
			 
			gotPreviousFields=true;
		}
	}
	
	protected void getFieldNames(){
        // Something was changed in the row.
        //
        final Map<String, Integer> fields = new HashMap<String, Integer>();
        
        // Add the currentMeta fields...
        fields.putAll(inputFields);
        
        Set<String> keySet = fields.keySet();
        List<String> entries = new ArrayList<String>(keySet);
        
        fieldNames = (String[]) entries.toArray(new String[entries.size()]);

        Const.sortStrings(fieldNames);
    }
	
	private void ok(){
		if (Const.isEmpty(wStepname.getText())) return;
		
		if (!checkGeneralParameters()){
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);
			mb.setMessage(Messages.getString("SOSInputDialog.ErrorRequiredInformationsGeneral.DialogMessage")); //$NON-NLS-1$
			mb.setText(Messages.getString("SOSInputDialog.ErrorRequiredInformations.DialogTitle")); //$NON-NLS-1$
			mb.open();
		}
			
		if (!checkRequestParameters()){
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);
			mb.setMessage(Messages.getString("SOSInputDialog.ErrorRequiredInformations"+wRequest.getText()+".DialogMessage")); //$NON-NLS-1$
			mb.setText(Messages.getString("SOSInputDialog.ErrorRequiredInformations.DialogTitle")); //$NON-NLS-1$
			mb.open();
		}
			
		input.setUrl( wUrl.getText() );
		input.setMethod(method);
		input.setSOSVersion(wVersion.getText());
		input.setRequest(wRequest.getText());
		input.setUrlObs( wUrlObs.getText() );
		input.setObservedProperties(wObservedProperties.getItems(0));
		
		if (wProcedures.getItemCount()==0 || (wProcedures.getItemCount()==1 && Const.isEmpty(wProcedures.getItem(0)[0])))
			input.setProcedures(null);
		else
			input.setProcedures(wProcedures.getItems(0));
		
		input.setOffering(wOfferings.getText());
		input.setUrlObs(wUrlObs.getText());
		input.setDifferentUrlObs(wDifferentUrlObs.getSelection());
		input.setTime1(wTime1.getText());
		input.setTime1Field(wTime1Field.getText());
		input.setTime1InField(wTime1InField.getSelection());
		input.setTime2(wTime2.getText());
		input.setTime2Field(wTime2Field.getText());
		input.setTime2InField(wTime2InField.getSelection());
		stepname = wStepname.getText();

		dispose();
	}
}