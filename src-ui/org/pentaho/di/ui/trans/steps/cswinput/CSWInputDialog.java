/**
 * 
 */
package org.pentaho.di.ui.trans.steps.cswinput;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.servlet.ServletException;

import org.eclipse.swt.SWT;

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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;


import org.eclipse.swt.widgets.Text;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.cswinput.CSWInputMeta;
import org.pentaho.di.trans.steps.cswinput.CSWReader;
import org.pentaho.di.trans.steps.cswinput.Messages;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.ComboVar;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;

/**
 * @author O.Mamadou
 *
 */
public class CSWInputDialog extends BaseStepDialog implements StepDialogInterface
{
	
	private CSWInputMeta input;
	private Group wGeneral;
	private Label wlUrl;
	private TextVar wUrl;
	private FormData fdUrl;
	private Button[] wMethod;
	private Label wlMethod;
	private FormData fdlMethod;
	private Label wlVersion;
	private FormData fdlVersion;
	private ComboVar wVersion;
	private FormData fdVersion;
	private FormData fdGeneral;
	private FormData fdlUrl;
	
	private ComboVar wMethodCSW;
	private FormData fdwMethod;
	
	private Group wRequestGroup;
	private FormData fdRequestGroup;
	
	private TextVar wReqText;
	private FormData fdReqText;
	private Label wlReqLabel;
	private FormData fdReqTextLabel;
	
	private Label wlOutputSchemaLabel;
	private FormData fdwlOutputSchemaLabel;
	private ComboVar wOutputSchemaLabel;
	private FormData fdwOutputSchemaLabel;
	private Group wLoginGroup;
	private TextVar wUser;
	private FormData fdwUser;
	private FormData fdLoginGroup;
	private TextVar wPassword;
	private FormData fdwPassword;
	private Label wlUser;
	private FormData fdwlUser;
	private Label wlPassword;
	private FormData fdwlPassword;
	private TextVar wLoginUrl;
	private FormData fdLoginUrl;
	private Label wlLoginURL;
	private FormData fdlwlLoginURL;
	private Group wOutputGroup;
	private FormData fdOutputGroup;
	private Button wChkAdvanced;
	private FormData fdChkAdvanced;
	private Group wAdvancedGroup;
	private FormData fdAvancedGroup;
	private TextVar wTitle;
	private FormData fdTitle;
	private Label wlTitle;
	private FormData fdwlTitle;
	private Label wlDateDeb;
	private FormData fdwlDateDeb;
	private TextVar wDateDeb;
	private FormData fdwDateDeb;
	private Label wlDateFin;
	private FormData fdwlDateFin;
	private TextVar wDateFin;
	private FormData fdwDateFin;
	private Group wSpatialGroup;
	private FormData fdSpatialGroup;
	private TextVar wBoxNorth;
	private FormData fdwBoxNorth;
	private TextVar wBoxWest;
	private FormData fdwBoxWest;
	private TextVar wBoxEast;
	private FormData fdwBoxEast;
	private TextVar wBoxSouth;
	private FormData fdwBoxSouth;
	/*private Button wGetRecordButton;
	private FormData fdwGetRecordButton;*/
	private Button wOptResultType;
	private FormData fdwOptResultType;
	private Button wOptResultTypeBrief;
	private FormData fdwOptResultTypeBrief;
	private Button wOptResultTypeFull;
	private FormData fdwOptResultTypeFull;
	private Group ElementSetGroup;
	private FormData fdElementSetGroup;
	private CSWReader cswParam;
	private ArrayList<String> outSchemaContent;
	private ComboVar wConstrainteLanguage;
	private FormData fdwConstrainteLanguage;
	private Label wlConstrainteLanguage;
	private FormData fdlConstrainteLanguage;
	private Group wRecordsGroup;
	private FormData fdRecordGroup;
	private Label wlStartPosition;
	private FormData fdwlStartPosition;
	private TextVar wStartPosition;
	private FormData fdwStartPosition;
	private Label wlMaxRecord;
	private FormData fdwlMaxRecord;
	private TextVar wMaxRecord;
	private FormData fdwMaxRecord;
	private Group wDateGroup;
	private FormData fdDateGroup;
	private TableView wQueryElement;
	private FormData fdwQueryElement;
	private String output;
	private Button wGetOutputSchemaButton;
	private Listener lsGetOutputSchema;
	private FormData fdGetOutputSchema;
	private Button wGetQueryElements;
	private Listener lsGGetQueryElements;
	private FormData fdGGetQueryElements;
	
	public void setMethod(){
		if (wMethod[0].getSelection() || wMethod[1].getSelection()){
			if (wMethod[0].getSelection()) {
			}
			if (wMethod[1].getSelection()) {
			}
		} else {
		}		
	}

	public CSWInputDialog(Shell parent, Object in, TransMeta tr, String sname)
	{
		super(parent, (BaseStepMeta)in, tr, sname);
		input=(CSWInputMeta)in;
	}

	public String open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX);
 		props.setLook(shell);
 		setShellImage(shell, input);
        
		ModifyListener lsMod = new ModifyListener() 
		{
			public void modifyText(ModifyEvent e) 
			{
				input.setChanged();
			}
		};
		changed = input.hasChanged();

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(Messages.getString("CSWInputDialog.Shell.Title")); //$NON-NLS-1$
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(Messages.getString("CSWInputDialog.Stepname.Label")); //$NON-NLS-1$
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
		wGeneral.setText(Messages.getString("CSWInputDialog.General.Tab"));
		FormLayout GeneralgroupLayout = new FormLayout();
		GeneralgroupLayout.marginWidth = 10;
		GeneralgroupLayout.marginHeight = 10;
		wGeneral.setLayout(GeneralgroupLayout);
		
		wlUrl=new Label(wGeneral, SWT.LEFT);
		wlUrl.setText(Messages.getString("CSWInputDialog.URL.Label")); //$NON-NLS-1$
 		props.setLook(wlUrl);
 		fdlUrl=new FormData();
		fdlUrl.left = new FormAttachment(0, 0);
		fdlUrl.top  = new FormAttachment(wStepname,margin);			
		wlUrl.setLayoutData(fdlUrl);
		
 		wUrl=new TextVar(transMeta, wGeneral, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wUrl);
		wUrl.addModifyListener(lsMod);
		fdUrl=new FormData();
		fdUrl.left = new FormAttachment(wlUrl, margin);
		fdUrl.top  = new FormAttachment(wStepname, margin);
		fdUrl.right= new FormAttachment(100, -1*margin);
		wUrl.setLayoutData(fdUrl);
		
		

		//method
		
		 wMethodCSW=new ComboVar(transMeta, wGeneral, SWT.BORDER | SWT.READ_ONLY);
        wMethodCSW.setEditable(false);
        props.setLook(wMethodCSW);
	        
		
		
		//
		
		wlMethod=new Label(wGeneral, SWT.LEFT);
        wlMethod.setText(Messages.getString("CSWInputDialog.Method.Label"));
        props.setLook(wlMethod);
        fdlMethod=new FormData();
        fdlMethod.left = new FormAttachment(0, margin);
        fdlMethod.top  = new FormAttachment(wUrl, margin*3);        
        wlMethod.setLayoutData(fdlMethod);
        
        fdwMethod=new FormData();
        fdwMethod.left = new FormAttachment(wlMethod, 2*margin);
        fdwMethod.top  = new FormAttachment(wUrl, margin*3);
        //fdwMethod.right= new FormAttachment(100, -85*margin);
        wMethodCSW.setLayoutData(fdwMethod);
        wMethodCSW.add("POST");
        wMethodCSW.add("GET");
        wMethodCSW.add("SOAP");
        
       

        //Version
        wlVersion=new Label(wGeneral, SWT.LEFT);
        wlVersion.setText(Messages.getString("CSWInputDialog.Version.Label"));
        props.setLook(wlVersion);
        fdlVersion=new FormData();
        fdlVersion.left = new FormAttachment(wMethodCSW, 3*margin);
        fdlVersion.top  = new FormAttachment(wUrl, margin*3);
        //
        wlVersion.setLayoutData(fdlVersion);
        wVersion=new ComboVar(transMeta, wGeneral, SWT.BORDER | SWT.READ_ONLY);
        wVersion.setEditable(false);
        props.setLook(wVersion);
        fdVersion=new FormData();
        fdVersion.left = new FormAttachment(wlVersion, 3*margin);
        fdVersion.top  = new FormAttachment(wUrl, margin*3);
        //fdVersion.right= new FormAttachment(100, -75*margin);
        wVersion.setLayoutData(fdVersion);
        wVersion.add("1.0.0");
        wVersion.add("2.0.0");
        wVersion.add("2.0.1");
        wVersion.add("2.0.2");
        
        wVersion.addModifyListener(lsMod);
        
        
        //constraint languague
        
      //
        wlConstrainteLanguage=new Label(wGeneral, SWT.LEFT);
        wlConstrainteLanguage.setText(Messages.getString("CSWInputDialog.ConstraintLanguage.Label"));
        props.setLook(wlConstrainteLanguage);
        fdlConstrainteLanguage=new FormData();
        fdlConstrainteLanguage.left = new FormAttachment(wVersion, 2*margin);
        fdlConstrainteLanguage.top  = new FormAttachment(wUrl, margin*3);
        //
        wlConstrainteLanguage.setLayoutData(fdlConstrainteLanguage);
        
        wConstrainteLanguage=new ComboVar(transMeta, wGeneral, SWT.BORDER | SWT.READ_ONLY);
        wConstrainteLanguage.setEditable(false);
        props.setLook(wConstrainteLanguage);
        fdwConstrainteLanguage=new FormData();
        fdwConstrainteLanguage.left = new FormAttachment(wlConstrainteLanguage, 2*margin);
        fdwConstrainteLanguage.top  = new FormAttachment(wUrl, margin*3);
        fdwConstrainteLanguage.right= new FormAttachment(100, -1*margin);
        wConstrainteLanguage.setLayoutData(fdwConstrainteLanguage);
        wConstrainteLanguage.add("FILTER");
        wConstrainteLanguage.add("CQL_TEXT");
        
        
        
        
        
        /**
         * Login parameters
         * **/
        
      ///
		wlLoginURL=new Label(wGeneral, SWT.LEFT);
		wlLoginURL.setText(Messages.getString("CSWInputDialog.LoginURL.Label"));
        props.setLook(wlLoginURL);
        fdlwlLoginURL=new FormData();
        fdlwlLoginURL.left = new FormAttachment(0, margin);
        fdlwlLoginURL.top  = new FormAttachment(wMethodCSW, margin*3);        
        wlLoginURL.setLayoutData(fdlwlLoginURL);
		
		
		wLoginUrl=new TextVar(transMeta, wGeneral, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wLoginUrl);
 		
		fdLoginUrl=new FormData();
		fdLoginUrl.left = new FormAttachment(wlLoginURL, margin);
		fdLoginUrl.top  = new FormAttachment(wMethodCSW, margin*3);
		fdLoginUrl.right= new FormAttachment(100, -1*margin);
		wLoginUrl.setLayoutData(fdLoginUrl); 
		
		
        wLoginGroup = new Group(wGeneral, SWT.SHADOW_NONE);
        
		props.setLook(wLoginGroup);
		wLoginGroup.setText(Messages.getString("CSWInputDialog.Login.Group"));
		FormLayout LoginGroupLayout = new FormLayout();
		LoginGroupLayout.marginWidth = 10;
		LoginGroupLayout.marginHeight = 10;
		wLoginGroup.setLayout(LoginGroupLayout);
		
		fdLoginGroup=new FormData();
		fdLoginGroup.left = new FormAttachment(0, margin);
		fdLoginGroup.top  = new FormAttachment(wLoginUrl, 3*margin);
		fdLoginGroup.right= new FormAttachment(100, -1*margin);
		wLoginGroup.setLayoutData(fdLoginGroup); 
		
		wlUser= new Label(wLoginGroup, SWT.LEFT);
		wlUser.setText(Messages.getString("CSWInputDialog.Username.Label"));
 		props.setLook(wlUser);
 		
		fdwlUser=new FormData();
		fdwlUser.left = new FormAttachment(0, margin);
		fdwlUser.top  = new FormAttachment(0, margin);		
		wlUser.setLayoutData(fdwlUser); 
		
		wUser=new TextVar(transMeta, wLoginGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wUser);
 		wUser.addModifyListener(lsMod);
		fdwUser=new FormData();
		fdwUser.left = new FormAttachment(wlUser, margin);
		fdwUser.top  = new FormAttachment(0, margin);
		fdwUser.right= new FormAttachment(middle, -1*margin);
		wUser.setLayoutData(fdwUser); 
		
		wlPassword= new Label(wLoginGroup, SWT.LEFT);
		wlPassword.setText(Messages.getString("CSWInputDialog.Password.Label"));
 		props.setLook(wlPassword);
 		
		fdwlPassword=new FormData();
		fdwlPassword.left = new FormAttachment(wUser, 10*margin);
		fdwlPassword.top  = new FormAttachment(0, margin);		
		wlPassword.setLayoutData(fdwlPassword); 
		
		wPassword=new TextVar(transMeta, wLoginGroup, SWT.SINGLE | SWT.PASSWORD |SWT.LEFT | SWT.BORDER);
 		props.setLook(wPassword);
 		wPassword.addModifyListener(lsMod);
		fdwPassword=new FormData();
		fdwPassword.left = new FormAttachment(wlPassword, margin);
		fdwPassword.top  = new FormAttachment(0, margin);
		fdwPassword.right= new FormAttachment(100, -30*margin);
		wPassword.setLayoutData(fdwPassword); 
		
		
        
        /**getCapabilities button
         * 
         * 
        wGetCapabilitiesButton=new Button(wGeneral, SWT.PUSH );
        wGetCapabilitiesButton.setVisible(false);
        wGetCapabilitiesButton.setText(Messages.getString("CSWInputDialog.Button.GetCapabilities"));
        lsGetCapabilities = new Listener()  {

		public void handleEvent(Event e){getCapabilities();}

		private void getCapabilities() {
			cswParam=new CSWReader();
			if (wVersion.getText().trim().length()==0){
				MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);
				mb.setMessage(Messages.getString("CSWInputDialog.VersionRequired.DialogMessage")); //$NON-NLS-1$
				mb.setText(Messages.getString("CSWInputDialog.VersionRequired.DialogMessage")); //$NON-NLS-1$
				mb.open();
				//e.printStackTrace();
				return;
			}
			
			cswParam.setVersion(wVersion.getText());
			
			if (wMethodCSW.getText().trim().length()==0){
				MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);
				mb.setMessage(Messages.getString("CSWInputDialog.MethodRequired.DialogMessage")); //$NON-NLS-1$
				mb.setText(Messages.getString("CSWInputDialog.MethodRequired.DialogMessage")); //$NON-NLS-1$
				mb.open();
				//e.printStackTrace();
				return;
			}
			cswParam.setMethod(wMethodCSW.getText());
			
			try {
				cswParam.setCatalogUrl(wUrl.getText());
			} catch (MalformedURLException e) {
				
				MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);
				mb.setMessage(Messages.getString("CSWInputDialog.ErrorRequiredWellFormedCSWURL.DialogMessage")); //$NON-NLS-1$
				mb.setText(Messages.getString("CSWInputDialog.ErrorRequiredWellFormedCSWURL.DialogMessage")); //$NON-NLS-1$
				mb.open();
				//e.printStackTrace();
				return;
				// TODO Auto-generated catch block
				
			}
			try {					
				
				//output=cswParam.getCapabilitiesDoc();
				if (output==null){
					output=cswParam.GetCapabilities();
					//output=cswParam.getCapabilitiesDoc();
					System.out.println("nouvelle recharge");
				}
					
				String[] queryElement=cswParam.getQueryableElement(cswParam.fromStringToJDOMDocument(output));
				String[] comparisonOps=cswParam.getComparisonOperator(cswParam.fromStringToJDOMDocument(output));				
				ColumnInfo col=new ColumnInfo(Messages.getString("CSWInputDialog.QueryElement.Column1"),  
						ColumnInfo.COLUMN_TYPE_CCOMBO,queryElement, true);
				wQueryElement.setColumnInfo(0, col);
				
				col=new ColumnInfo(Messages.getString("CSWInputDialog.QueryElement.Column2"),  
						ColumnInfo.COLUMN_TYPE_CCOMBO,comparisonOps, true);
				wQueryElement.setColumnInfo(1, col);
				
				outSchemaContent=cswParam.extractOutputSchemaFromCapabilitiesDocument(output);
				//
				Iterator<String> contentIT=outSchemaContent.iterator();
				while (contentIT.hasNext()){
					String item=contentIT.next();
					wOutputSchemaLabel.add(item);
				}
				
				//System.out.println(output);
			} catch (KettleException e) {				
				//e.printStackTrace();
				MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);
				mb.setMessage(e.getMessage()); //$NON-NLS-1$
				mb.setText(Messages.getString("CSWInputDialog.ErrorRetrievingOutSchema.DialogMessage")); //$NON-NLS-1$
				mb.open();
				return;
			} catch (ServletException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	};
		wGetCapabilitiesButton.addListener(SWT.Selection, lsGetCapabilities);

        
        fdGetCapabilitiesButton = new FormData();
        fdGetCapabilitiesButton.left = new FormAttachment(wUrl, 3*margin);
        fdGetCapabilitiesButton.top = new FormAttachment(wStepname, margin);
        //
        wGetCapabilitiesButton.setLayoutData(fdGetCapabilitiesButton);
 */
		fdGeneral = new FormData();
		fdGeneral.left  = new FormAttachment(0, margin);
		fdGeneral.top   = new FormAttachment(wStepname, margin);
		fdGeneral.right = new FormAttachment(100, -margin);
		wGeneral.setLayoutData(fdGeneral);
		
		
		/**
		 * 
		 * */
		wRequestGroup = new Group(shell, SWT.SHADOW_NONE);
		props.setLook(wRequestGroup);
		wRequestGroup.setText(Messages.getString("CSWInputDialog.Query.Tab"));
		FormLayout RequestGroupLayout = new FormLayout();
		RequestGroupLayout.marginWidth = 5;
		RequestGroupLayout.marginHeight = 5;
		wRequestGroup.setLayout(RequestGroupLayout);
		
		fdRequestGroup = new FormData();
		fdRequestGroup.left  = new FormAttachment(0, margin);
		fdRequestGroup.top   = new FormAttachment(wGeneral, 1*margin);
		fdRequestGroup.right = new FormAttachment(100, -margin);
		wRequestGroup.setLayoutData(fdRequestGroup);
		
		
		
 		
 		
		
		/***Request Text and Label
		 * */
		
		wlReqLabel=new Label(wRequestGroup, SWT.LEFT);
		wlReqLabel.setText(Messages.getString("CSWInputDialog.Request.Label"));
		props.setLook(wlReqLabel);
		fdReqTextLabel=new FormData();
		fdReqTextLabel.left = new FormAttachment(0, 0);
		fdReqTextLabel.top  = new FormAttachment(margin, margin);		
		wlReqLabel.setLayoutData(fdReqTextLabel);
 		
		wReqText=new TextVar(transMeta, wRequestGroup, SWT.BORDER | SWT.SINGLE); 
		props.setLook(wReqText);
 		fdReqText=new FormData();
 		fdReqText.left = new FormAttachment(wlReqLabel, margin);
 		fdReqText.top  = new FormAttachment(margin, margin);
 		fdReqText.right= new FormAttachment(100, -60*margin);
 		wReqText.setLayoutData(fdReqText);
 		
 	
        //
        
        /**
         * Start Position and maxRecord
         * */
        wRecordsGroup = new Group(wRequestGroup, SWT.SHADOW_NONE);
		props.setLook(wRecordsGroup);
		wRecordsGroup.setText(Messages.getString("CSWInputDialog.RecordSetting.Group"));
		FormLayout RecordGroupLayout = new FormLayout();
		RecordGroupLayout.marginWidth = 10;
		RecordGroupLayout.marginHeight = 10;
		wRecordsGroup.setLayout(LoginGroupLayout);
		//mmml
		
		fdRecordGroup=new FormData();
		fdRecordGroup.left = new FormAttachment(wReqText, margin);
		fdRecordGroup.top  = new FormAttachment(0, 0);
		fdRecordGroup.right= new FormAttachment(100, -1*margin);
		wRecordsGroup.setLayoutData(fdRecordGroup); 
		
		
		wlStartPosition= new Label(wRecordsGroup, SWT.LEFT);
		wlStartPosition.setText(Messages.getString("CSWInputDialog.StartPosition.Label"));
 		props.setLook(wlStartPosition);
 		
		fdwlStartPosition=new FormData();
		fdwlStartPosition.left = new FormAttachment(0, margin);
		fdwlStartPosition.top  = new FormAttachment(0, margin);	
		//fdwlStartPosition.right= new FormAttachment(middle, -margin);
		wlStartPosition.setLayoutData(fdwlStartPosition); 
		
		wStartPosition=new TextVar(transMeta, wRecordsGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wStartPosition);
 		
		fdwStartPosition=new FormData();
		fdwStartPosition.left = new FormAttachment(wlStartPosition, margin);
		fdwStartPosition.top  = new FormAttachment(0, margin);
		//fdwStartPosition.right= new FormAttachment(60, -50*margin);
		wStartPosition.setLayoutData(fdwStartPosition); 
		
		wlMaxRecord= new Label(wRecordsGroup, SWT.LEFT);
		wlMaxRecord.setText(Messages.getString("CSWInputDialog.MaxRecord.Label"));
 		props.setLook(wlMaxRecord);
 		
		fdwlMaxRecord=new FormData();
		fdwlMaxRecord.left = new FormAttachment(0,margin);
		fdwlMaxRecord.top  = new FormAttachment(wStartPosition, margin);		
		wlMaxRecord.setLayoutData(fdwlMaxRecord); 
		
		wMaxRecord=new TextVar(transMeta, wRecordsGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wMaxRecord);
 		
		fdwMaxRecord=new FormData();
		fdwMaxRecord.left = new FormAttachment(wlMaxRecord, 2*margin);
		fdwMaxRecord.top  = new FormAttachment(wStartPosition, margin);
		//fdwMaxRecord.right= new FormAttachment(80, -50*margin);
		wMaxRecord.setLayoutData(fdwMaxRecord); 
		/**
		 * 
		 * */
        
 		
 		//checkbox advanced query
 		wChkAdvanced= new Button(wRequestGroup, SWT.CHECK);
 		props.setLook(wChkAdvanced);
 		wChkAdvanced.setText("Advanced search");
 		fdChkAdvanced=new FormData();
 		fdChkAdvanced.left = new FormAttachment(0, margin);
 		fdChkAdvanced.top  = new FormAttachment(wReqText, 2*margin);
 		fdChkAdvanced.right= new FormAttachment(100, -1*margin);
 		wChkAdvanced.setLayoutData(fdChkAdvanced);
 		//listener
 		wChkAdvanced.addSelectionListener(new SelectionAdapter() 
        {
            public void widgetSelected(SelectionEvent e) 
            {
            	input.setChanged();
            	if (wChkAdvanced.getSelection()==true){
            		wAdvancedGroup.setEnabled(true);
            	}else
            	if (wChkAdvanced.getSelection()==false){
            		wAdvancedGroup.setEnabled(false);
            	}           	
            		
            }
        }
 		);
 		
 		/**
 		 * advanced query group
 		 * **/
 		wAdvancedGroup = new Group(wRequestGroup, SWT.SHADOW_NONE);
 		
 		//
 		props.setLook(wAdvancedGroup);
 		wAdvancedGroup.setText(Messages.getString("CSWInputDialog.AdvancedQuery.Tab"));
		FormLayout AdvancedGroupLayout = new FormLayout();
		AdvancedGroupLayout.marginWidth = 4;
		AdvancedGroupLayout.marginHeight = 4;
		wAdvancedGroup.setLayout(AdvancedGroupLayout);
		
		fdAvancedGroup = new FormData();
		fdAvancedGroup.left  = new FormAttachment(0, margin);
		fdAvancedGroup.top   = new FormAttachment(wChkAdvanced, 4*margin);
		fdAvancedGroup.right = new FormAttachment(100, -margin);
		wAdvancedGroup.setLayoutData(fdAvancedGroup);
		
		
////
 		
 		ColumnInfo[] colinfQueryElement=new ColumnInfo[3];
 		colinfQueryElement[0]=new ColumnInfo(Messages.getString("CSWInputDialog.QueryElement.Column1"),  
				ColumnInfo.COLUMN_TYPE_TEXT,null, false);
 		colinfQueryElement[1]=new ColumnInfo(Messages.getString("CSWInputDialog.QueryElement.Column2"),  
				ColumnInfo.COLUMN_TYPE_TEXT,null, true);
 		colinfQueryElement[2]=new ColumnInfo(Messages.getString("CSWInputDialog.QueryElement.Column3"),  
				ColumnInfo.COLUMN_TYPE_TEXT,null, false);
		wQueryElement=new TableView(transMeta,wAdvancedGroup,
							  SWT.BORDER | SWT.MULTI | SWT.V_SCROLL, 
							  colinfQueryElement, 
							  3,  
							  lsMod,
							  props
							  );
		fdwQueryElement=new FormData();
		fdwQueryElement.left  = new FormAttachment(0, 0);
		fdwQueryElement.top   = new FormAttachment(0, 2*margin);
		fdwQueryElement.right = new FormAttachment(100, -margin);
		
		wQueryElement.setLayoutData(fdwQueryElement);
		
		/**
		 * queryable Element button
		 * */
		
		
		
 		wGetQueryElements=new Button(wAdvancedGroup, SWT.PUSH);
 		wGetQueryElements.setText(Messages.getString("CSWInputDialog.Button.GetQueryElement"));
        lsGGetQueryElements = new Listener()  {

		public void handleEvent(Event e){getInformationFromCapabilitieDocument();}

		
	};
		wGetQueryElements.addListener(SWT.Selection, lsGGetQueryElements);

        
        fdGGetQueryElements = new FormData();
        fdGGetQueryElements.left = new FormAttachment(middle+middle, 2*margin);
        fdGGetQueryElements.top = new FormAttachment(wQueryElement, 1*margin);
        //
        wGetQueryElements.setLayoutData(fdGGetQueryElements);
		
		/**
		 * end queryable Element button
		 * */
 		
		//title
		wlTitle=new Label(wAdvancedGroup, SWT.LEFT); 
		wlTitle.setText(Messages.getString("CSWInputDialog.AdvancedGroup.Title"));
		props.setLook(wlTitle);
 		fdwlTitle=new FormData();
 		fdwlTitle.left = new FormAttachment(0, margin);
 		fdwlTitle.top  = new FormAttachment(wQueryElement, 2*margin); 		
 		wlTitle.setLayoutData(fdwlTitle);
 		wlTitle.setVisible(false);
 		
 		
		wTitle=new TextVar(transMeta, wAdvancedGroup, SWT.BORDER | SWT.SINGLE); 
		props.setLook(wTitle);
 		fdTitle=new FormData();
 		fdTitle.left = new FormAttachment(wlTitle, margin);
 		fdTitle.top  = new FormAttachment(wQueryElement, 2*margin);
 		fdTitle.right= new FormAttachment(100, -1*margin);
 		wTitle.setLayoutData(fdTitle);
 		wTitle.setVisible(false);
		
 		/**
 		 * 
 		 * */
 		wDateGroup = new Group(wAdvancedGroup, SWT.SHADOW_NONE);
 		props.setLook(wDateGroup);
 		wDateGroup.setText(Messages.getString("CSWInputDialog.DateSearchGroup.Title"));
		FormLayout DateGroupLayout = new FormLayout();
		DateGroupLayout.marginWidth = 5;
		DateGroupLayout.marginHeight = 5;
		wDateGroup.setLayout(DateGroupLayout);
		
		fdDateGroup = new FormData();
		fdDateGroup.left  = new FormAttachment(0, margin);
		fdDateGroup.top   = new FormAttachment(wTitle, margin);
		fdDateGroup.right = new FormAttachment(middle, -margin);
		wDateGroup.setLayoutData(fdDateGroup);
 		
 		
 		//date deb et date fin
 		wlDateDeb=new Label(wDateGroup, SWT.LEFT); 
 		wlDateDeb.setText(Messages.getString("CSWInputDialog.AdvancedGroup.DateDeb"));
		props.setLook(wlDateDeb);
 		fdwlDateDeb=new FormData();
 		fdwlDateDeb.left = new FormAttachment(0, margin);
 		fdwlDateDeb.top  = new FormAttachment(0, margin); 		
 		wlDateDeb.setLayoutData(fdwlDateDeb);
 		
		wDateDeb=new TextVar(transMeta, wDateGroup, SWT.BORDER | SWT.SINGLE); 
		props.setLook(wDateDeb);
 		fdwDateDeb=new FormData();
 		fdwDateDeb.left = new FormAttachment(wlDateDeb, margin);
 		fdwDateDeb.top  = new FormAttachment(0, margin);
 		//fdwDateDeb.right= new FormAttachment(50, -1*margin);
 		wDateDeb.setLayoutData(fdwDateDeb);
 		
 		//
 		wlDateFin=new Label(wDateGroup, SWT.LEFT); 
 		wlDateFin.setText(Messages.getString("CSWInputDialog.AdvancedGroup.DateFin"));
		props.setLook(wlDateFin);
 		fdwlDateFin=new FormData();
 		fdwlDateFin.left = new FormAttachment(0, margin);
 		fdwlDateFin.top  = new FormAttachment(wDateDeb, margin); 		
 		wlDateFin.setLayoutData(fdwlDateFin);
 		
 		wDateFin=new TextVar(transMeta, wDateGroup, SWT.BORDER | SWT.SINGLE); 
		props.setLook(wDateFin);
 		fdwDateFin=new FormData();
 		fdwDateFin.left = new FormAttachment(wlDateFin, margin);
 		fdwDateFin.top  = new FormAttachment(wDateDeb, margin);
 		//fdwDateFin.right= new FormAttachment(100, -1*margin);
 		wDateFin.setLayoutData(fdwDateFin);
 		
 		/**
 		 * spatial search
 		 * **/
 		wSpatialGroup = new Group(wAdvancedGroup, SWT.SHADOW_NONE);
 		props.setLook(wSpatialGroup);
 		wSpatialGroup.setText(Messages.getString("CSWInputDialog.SpatialSearchGroup.Title"));
		FormLayout SpatialGroupLayout = new FormLayout();
		SpatialGroupLayout.marginWidth = 5;
		SpatialGroupLayout.marginHeight = 5;
		wSpatialGroup.setLayout(SpatialGroupLayout);
		
		fdSpatialGroup = new FormData();
		fdSpatialGroup.left  = new FormAttachment(middle, 3*margin);
		fdSpatialGroup.top   = new FormAttachment(wTitle, margin);
		fdSpatialGroup.right = new FormAttachment(100, -margin);
		wSpatialGroup.setLayoutData(fdSpatialGroup);
		
		wBoxNorth=new TextVar(transMeta, wSpatialGroup, SWT.BORDER | SWT.SINGLE); 
		props.setLook(wBoxNorth);
 		fdwBoxNorth=new FormData();
 		fdwBoxNorth.left = new FormAttachment(middle, margin);
 		fdwBoxNorth.top  = new FormAttachment(0, margin);
 		//fdwBoxNorth.right= new FormAttachment(100, -margin);
 		wBoxNorth.setLayoutData(fdwBoxNorth);
 		
 		wBoxWest=new TextVar(transMeta, wSpatialGroup, SWT.BORDER | SWT.SINGLE); 
		props.setLook(wBoxWest);
 		fdwBoxWest=new FormData();
 		fdwBoxWest.left = new FormAttachment(0, margin);
 		fdwBoxWest.top  = new FormAttachment(wBoxNorth, margin);
 		fdwBoxWest.right= new FormAttachment(middle, -margin);
 		wBoxWest.setLayoutData(fdwBoxWest);
 		
 		wBoxEast=new TextVar(transMeta, wSpatialGroup, SWT.BORDER | SWT.SINGLE); 
		props.setLook(wBoxEast);
 		fdwBoxEast=new FormData();
 		fdwBoxEast.left = new FormAttachment(wBoxNorth, margin);
 		fdwBoxEast.top  = new FormAttachment(wBoxNorth, margin);
 		fdwBoxEast.right= new FormAttachment(100, -margin);
 		wBoxEast.setLayoutData(fdwBoxEast);
 		
 		wBoxSouth=new TextVar(transMeta, wSpatialGroup, SWT.BORDER | SWT.SINGLE); 
		props.setLook(wBoxSouth);
 		fdwBoxSouth=new FormData();
 		fdwBoxSouth.left = new FormAttachment(middle, margin);
 		fdwBoxSouth.top  = new FormAttachment(wBoxEast, margin);
 		//fdwBoxSouth.right= new FormAttachment(100, -50*margin);
 		wBoxSouth.setLayoutData(fdwBoxSouth);
 		
 		//outputSchema and resultType (brief,Summary,full)
 		
 		wOutputGroup = new Group(wRequestGroup, SWT.SHADOW_NONE);
 		props.setLook(wOutputGroup);
 		wOutputGroup.setText(Messages.getString("CSWInputDialog.Output.Group"));
		FormLayout OutputGroupLayout = new FormLayout();
		OutputGroupLayout.marginWidth = 5;
		OutputGroupLayout.marginHeight = 5;
		wOutputGroup.setLayout(OutputGroupLayout);
		
		fdOutputGroup = new FormData();
		fdOutputGroup.left  = new FormAttachment(0, margin);
		fdOutputGroup.top   = new FormAttachment(wAdvancedGroup, margin);
		fdOutputGroup.right = new FormAttachment(100, -margin);
		wOutputGroup.setLayoutData(fdOutputGroup);
 		
 		
		
		ElementSetGroup = new Group(wOutputGroup, SWT.SHADOW_NONE);
 		props.setLook(ElementSetGroup);
 		ElementSetGroup.setText(Messages.getString("CSWInputDialog.ElementSet.Group"));
		FormLayout ElementSetGroupLayout = new FormLayout();
		ElementSetGroupLayout.marginWidth = 5;
		ElementSetGroupLayout.marginHeight = 5;
		ElementSetGroup.setLayout(ElementSetGroupLayout);
		
		fdElementSetGroup = new FormData();
		fdElementSetGroup.left  = new FormAttachment(0, margin);
		fdElementSetGroup.top   = new FormAttachment(0, margin);
		fdElementSetGroup.right = new FormAttachment(100, -margin);
		ElementSetGroup.setLayoutData(fdElementSetGroup);
		
		///
		
 		
 		

 		//
 		wOptResultType= new Button(ElementSetGroup, SWT.RADIO);
 		props.setLook(wOptResultType);
 		wOptResultType.setText(Messages.getString("CSWInputDialog.ElementSet.Summary"));
 		fdwOptResultType=new FormData();
 		fdwOptResultType.left = new FormAttachment(0, 20*margin);
 		fdwOptResultType.top  = new FormAttachment(0, margin); 		
 		wOptResultType.setLayoutData(fdwOptResultType);
 		
 		wOptResultTypeBrief= new Button(ElementSetGroup, SWT.RADIO);
 		props.setLook(wOptResultTypeBrief);
 		wOptResultTypeBrief.setText(Messages.getString("CSWInputDialog.ElementSet.Brief"));
 		fdwOptResultTypeBrief=new FormData();
 		fdwOptResultTypeBrief.left = new FormAttachment(wOptResultType, 20*margin);
 		fdwOptResultTypeBrief.top  = new FormAttachment(0, margin);
 		
 		wOptResultTypeBrief.setLayoutData(fdwOptResultTypeBrief);
 		
 		wOptResultTypeFull= new Button(ElementSetGroup, SWT.RADIO);
 		props.setLook(wOptResultTypeFull);
 		wOptResultTypeFull.setText(Messages.getString("CSWInputDialog.ElementSet.Full"));
 		fdwOptResultTypeFull=new FormData();
 		fdwOptResultTypeFull.left = new FormAttachment(wOptResultTypeBrief, 20*margin);
 		fdwOptResultTypeFull.top  = new FormAttachment(0, margin);
 		
 		wOptResultTypeFull.setLayoutData(fdwOptResultTypeFull);
 		
 		//
 		
 		
 		/**
 		 * output schema
 		 * **/
 		wlOutputSchemaLabel=new Label(wOutputGroup, SWT.LEFT);
 		wlOutputSchemaLabel.setText(Messages.getString("CSWInputDialog.OutputSchema.Label"));
		props.setLook(wlOutputSchemaLabel);
		fdwlOutputSchemaLabel=new FormData();
		fdwlOutputSchemaLabel.left = new FormAttachment(0, margin);
		fdwlOutputSchemaLabel.top  = new FormAttachment(ElementSetGroup, 3*margin);		
		wlOutputSchemaLabel.setLayoutData(fdwlOutputSchemaLabel);
 		
		wOutputSchemaLabel= new ComboVar(transMeta, wOutputGroup, SWT.BORDER ); 			 
		props.setLook(wOutputSchemaLabel);
		
		
 		fdwOutputSchemaLabel=new FormData();
 		fdwOutputSchemaLabel.left = new FormAttachment(wlOutputSchemaLabel, margin);
 		fdwOutputSchemaLabel.top  = new FormAttachment(ElementSetGroup, 3*margin);
 		fdwOutputSchemaLabel.right= new FormAttachment(75, -1*margin);
 		wOutputSchemaLabel.setLayoutData(fdwOutputSchemaLabel);
 		
 		/**
 		 * Get output schema button
 		 * */
 		wGetOutputSchemaButton=new Button(wOutputGroup, SWT.PUSH);
 		wGetOutputSchemaButton.setText(Messages.getString("CSWInputDialog.Button.GetOuputSchema"));
        lsGetOutputSchema = new Listener()  {

		public void handleEvent(Event e){getInformationFromCapabilitieDocument();}		
	};
	wGetOutputSchemaButton.addListener(SWT.Selection, lsGetOutputSchema);

        
        fdGetOutputSchema = new FormData();
        fdGetOutputSchema.left = new FormAttachment(wOutputSchemaLabel, 2*margin);
        fdGetOutputSchema.top = new FormAttachment(ElementSetGroup, 3*margin);
        //
        wGetOutputSchemaButton.setLayoutData(fdGetOutputSchema);
        
        ///
 		
 		
		
		
		
		
		
		// Some buttons
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(Messages.getString("System.Button.OK")); //$NON-NLS-1$
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(Messages.getString("System.Button.Cancel")); //$NON-NLS-1$

		setButtonPositions(new Button[] { wOK, wCancel }, margin, wRequestGroup);

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
		
		getData();
		input.setChanged(changed);
	
		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}
	
	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
		wStepname.selectAll();
		if (!Const.isEmpty(input.getCswParam().getKeyword()))      
			wReqText.setText(input.getCswParam().getKeyword());
		
		if (!Const.isEmpty(input.getCswParam().getVersion())){
			wVersion.setText(input.getCswParam().getVersion());
		}
		if (!Const.isEmpty(input.getCswParam().getCatalogUrl().toString())){
			wUrl.setText(input.getCswParam().getCatalogUrl().toString());
		}
		if (!Const.isEmpty(input.getCswParam().getMethod())){
			wMethodCSW.setText(input.getCswParam().getMethod());
		}
		if (!Const.isEmpty(input.getCswParam().getStartDate())){
			wDateDeb.setText(input.getCswParam().getStartDate());
		}
		if (!Const.isEmpty(input.getCswParam().getEndDate())){
			wDateFin.setText(input.getCswParam().getEndDate());
		}
		if (!Const.isEmpty(input.getCswParam().getUsername())){
			wUser.setText(input.getCswParam().getUsername());
		}
		if (!Const.isEmpty(input.getCswParam().getPassword())){
			wPassword.setText(input.getCswParam().getPassword());
		}
		if (!Const.isEmpty(input.getCswParam().getLoginServiceUrl())){
			wLoginUrl.setText(input.getCswParam().getLoginServiceUrl());
		}
		if (!Const.isEmpty(input.getCswParam().getOutputSchema())){
			wOutputSchemaLabel.setText(input.getCswParam().getOutputSchema());
		}
		if (!Const.isEmpty(input.getCswParam().getTitle())){
			wTitle.setText(input.getCswParam().getTitle());
		}
		
		if (!Const.isEmpty(input.getCswParam().getConstraintLanguage())){
			wConstrainteLanguage.setText(input.getCswParam().getConstraintLanguage());
		}
		
		if (!Const.isEmpty(input.getCswParam().getStartPosition().toString())){
			wStartPosition.setText(input.getCswParam().getStartPosition().toString());
		}
		if (!Const.isEmpty(input.getCswParam().getMaxRecords().toString())){
			wMaxRecord.setText(input.getCswParam().getMaxRecords().toString());
		}
		
		
		wChkAdvanced.setSelection(input.getCswParam().isSimpleSearch());
		
		wAdvancedGroup.setEnabled(wChkAdvanced.getSelection());
		
			
		if (!Const.isEmpty(input.getCswParam().getElementSet())){
			String value=input.getCswParam().getElementSet();
			if (value.equalsIgnoreCase(wOptResultType.getText())){
				wOptResultType.setSelection(true);
			}else
			if (value.equalsIgnoreCase(wOptResultTypeBrief.getText())){
				wOptResultTypeBrief.setSelection(true);
			}else
			if (value.equalsIgnoreCase(wOptResultTypeFull.getText())){
				wOptResultTypeFull.setSelection(true);
			}
	
		}//end element set
		wBoxNorth.setText(input.getCswParam().getBBOX().get("NORTH").toString());
		wBoxSouth.setText(input.getCswParam().getBBOX().get("SOUTH").toString());
		wBoxEast.setText(input.getCswParam().getBBOX().get("EAST").toString());
		wBoxWest.setText(input.getCswParam().getBBOX().get("WEST").toString());
		
		ArrayList<String[]> advancedElementArrayList=input.getCswParam().getAdvancedRequestParam();
		if (advancedElementArrayList!=null){
			wQueryElement.removeAll();
			for(String[]s:advancedElementArrayList){
				wQueryElement.add(s);
			}
			wQueryElement.remove(0);
			wQueryElement.setRowNums();
		}
		this.output=input.getCswParam().getCapabilitiesDoc();
	
		
	}
	
	private void getInformationFromCapabilitieDocument() {
		//if (cswParam==null){
			cswParam=new CSWReader();
		//}
		
		if (wVersion.getText().trim().length()==0){
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);
			mb.setMessage(Messages.getString("CSWInputDialog.VersionRequired.DialogMessage")); //$NON-NLS-1$
			mb.setText(Messages.getString("CSWInputDialog.VersionRequired.DialogMessage")); //$NON-NLS-1$
			mb.open();
			//e.printStackTrace();
			return;
		}
		
		cswParam.setVersion(wVersion.getText());
		
		if (wMethodCSW.getText().trim().length()==0){
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);
			mb.setMessage(Messages.getString("CSWInputDialog.MethodRequired.DialogMessage")); //$NON-NLS-1$
			mb.setText(Messages.getString("CSWInputDialog.MethodRequired.DialogMessage")); //$NON-NLS-1$
			mb.open();
			//e.printStackTrace();
			return;
		}
		cswParam.setMethod(wMethodCSW.getText());
		
		try {
			cswParam.setCatalogUrl(wUrl.getText());
		} catch (MalformedURLException e) {
			
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);
			mb.setMessage(Messages.getString("CSWInputDialog.ErrorRequiredWellFormedCSWURL.DialogMessage")); //$NON-NLS-1$
			mb.setText(Messages.getString("CSWInputDialog.ErrorRequiredWellFormedCSWURL.DialogMessage")); //$NON-NLS-1$
			mb.open();
			//e.printStackTrace();
			return;
			// TODO Auto-generated catch block
			
		}
		try {					
			
			//output=cswParam.getCapabilitiesDoc();
			//if (output==null){
				output=cswParam.GetCapabilities();
				//output=cswParam.getCapabilitiesDoc();
				//System.out.println("nouvelle recharge");
		//	}
				
			String[] queryElement=cswParam.getQueryableElement(cswParam.fromStringToJDOMDocument(output));
			String[] comparisonOps=cswParam.getComparisonOperator(cswParam.fromStringToJDOMDocument(output));				
			ColumnInfo col=new ColumnInfo(Messages.getString("CSWInputDialog.QueryElement.Column1"),  
					ColumnInfo.COLUMN_TYPE_CCOMBO,queryElement, true);
			wQueryElement.setColumnInfo(0, col);
			
			col=new ColumnInfo(Messages.getString("CSWInputDialog.QueryElement.Column2"),  
					ColumnInfo.COLUMN_TYPE_CCOMBO,comparisonOps, true);
			wQueryElement.setColumnInfo(1, col);
			
			outSchemaContent=cswParam.extractOutputSchemaFromCapabilitiesDocument(output);
			//
			Iterator<String> contentIT=outSchemaContent.iterator();
			while (contentIT.hasNext()){
				String item=contentIT.next();
				wOutputSchemaLabel.add(item);
			}
			
			//System.out.println(output);
		} catch (KettleException e) {				
			//e.printStackTrace();
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);
			mb.setMessage(e.getMessage()); //$NON-NLS-1$
			mb.setText(Messages.getString("CSWInputDialog.ErrorRetrievingOutSchema.DialogMessage")); //$NON-NLS-1$
			mb.open();
			return;
		} catch (ServletException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void cancel()
	{
		stepname=null;
		input.setChanged(changed);
		
		dispose();
	}
	
	private void ok()
	{
		String elementSet;
		if (wOptResultType.getSelection()==true){
			elementSet=wOptResultType.getText();
		}else{
			if (wOptResultTypeBrief.getSelection()==true){
				elementSet=wOptResultTypeBrief.getText();
			}else
				elementSet=wOptResultTypeFull.getText();
		}
		
		cswParam=new CSWReader();
		cswParam.setVersion(wVersion.getText());
		cswParam.setMethod(wMethodCSW.getText());
		cswParam.setStartDate(wDateDeb.getText());
		cswParam.setEndDate(wDateFin.getText());
		cswParam.setSimpleSearch(wChkAdvanced.getSelection());
		cswParam.setKeyword(wReqText.getText());
		cswParam.setUsername(wUser.getText());
		cswParam.setPassword(wPassword.getText());
		cswParam.setLoginServiceUrl(wLoginUrl.getText());
		cswParam.setOutputSchema(wOutputSchemaLabel.getText());
		cswParam.setTitle(wTitle.getText());
		cswParam.setElementSet(elementSet);
		cswParam.setConstraintLanguage(wConstrainteLanguage.getText());
		cswParam.setStartPosition(Integer.parseInt(wStartPosition.getText()));
		cswParam.setMaxRecords(Integer.parseInt(wMaxRecord.getText()));
		
		HashMap<String, Double> bbox=new HashMap<String, Double>();
		bbox.put("NORTH", Double.parseDouble(wBoxNorth.getText()));
		bbox.put("SOUTH", Double.parseDouble(wBoxSouth.getText()));
		bbox.put("EAST", Double.parseDouble(wBoxEast.getText()));
		bbox.put("WEST", Double.parseDouble(wBoxWest.getText()));
		cswParam.setBBOX(bbox);
		cswParam.setCapabilitiesDoc(this.output);
		
		
		
		/**
		 * */
		wQueryElement.removeEmptyRows();
		ArrayList<String[]> advancedElementArrayList=new ArrayList<String[]>();		
		for (int i=0;i<wQueryElement.getItemCount();i++){
			String []s=wQueryElement.getItem(i);
			advancedElementArrayList.add(s);		
		}
		cswParam.setAdvancedRequestParam(advancedElementArrayList);
		
		/**
		 * */
		
		try {
			cswParam.setCatalogUrl(wUrl.getText());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		input.setCswParam(cswParam);
		
		if (Const.isEmpty(wStepname.getText())) return;
		stepname = wStepname.getText(); // return value
		
		
		
		//input.setKeyword(wReqText.getText());
		
		dispose();
	}
}
