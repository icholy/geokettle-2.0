/**
 * 
 */
package org.pentaho.di.ui.trans.steps.cswoutput;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.jdom.Element;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.cswoutput.CSWOutputMeta;
import org.pentaho.di.trans.steps.cswoutput.CSWWriter;
import org.pentaho.di.trans.steps.cswoutput.Messages;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.ComboVar;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

/**
 * @author LiberT
 *
 */
public class CSWOutputDialog extends BaseStepDialog implements
		StepDialogInterface {

	
	private CSWOutputMeta input;
	private Group wGeneral;
	private Label wlUrl;
	private FormData fdlUrl;
	private TextVar wUrl;
	private FormData fdUrl;
	private CSWWriter cswwriter;
	private FormData fdGeneral;
	private Label wlLoginURL;
	private FormData fdlwlLoginURL;
	private TextVar wLoginUrl;
	private FormData fdLoginUrl;
	private Group wLoginGroup;
	private FormData fdLoginGroup;
	private Label wlUser;
	private FormData fdwlUser;
	private TextVar wUser;
	private FormData fdwUser;
	private Label wlPassword;
	private FormData fdwlPassword;
	private TextVar wPassword;
	private FormData fdwPassword;
	private Label wlSchemaLabel;
	private FormData fdwlSchemaLabel;
	private ComboVar wSchemaLabel;
	private FormData fdwSchemaLabel;
	private TableView wQueryElement;
	private FormData fdwQueryElement;
	private Button wGetQueryElements;
	private Listener lsGGetQueryElements;
	private FormData fdGGetQueryElements;
	private Group wMappingColumnGroup;
	private FormData fdMappingGroup;
	

	/**
	 * @param parent
	 * @param baseStepMeta
	 * @param transMeta
	 * @param stepname
	 */


		public CSWOutputDialog(Shell parent, Object in, TransMeta tr, String sname){
		super(parent, (BaseStepMeta)in, tr, sname);
		input=(CSWOutputMeta)in;
		 
	}

	/* (non-Javadoc)
	 * @see org.pentaho.di.trans.step.StepDialogInterface#open()
	 */
	@Override
	public String open() {

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
		shell.setText(Messages.getString("CSWOutputDialog.Shell.Title")); //$NON-NLS-1$
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(Messages.getString("CSWOutputDialog.Stepname.Label")); //$NON-NLS-1$
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
		wGeneral.setText(Messages.getString("CSWOutputDialog.General.Tab"));
		FormLayout GeneralgroupLayout = new FormLayout();
		GeneralgroupLayout.marginWidth = 10;
		GeneralgroupLayout.marginHeight = 10;
		wGeneral.setLayout(GeneralgroupLayout);
		
		wlUrl=new Label(wGeneral, SWT.LEFT);
		wlUrl.setText(Messages.getString("CSWOutputDialog.URL.Label")); //$NON-NLS-1$
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
		
		

        /**
         * Login parameters
         * **/
        		
        wLoginGroup = new Group(wGeneral, SWT.SHADOW_NONE);
        
		props.setLook(wLoginGroup);
		wLoginGroup.setText(Messages.getString("CSWOutputDialog.Login.Group"));
		FormLayout LoginGroupLayout = new FormLayout();
		LoginGroupLayout.marginWidth = 10;
		LoginGroupLayout.marginHeight = 10;
		wLoginGroup.setLayout(LoginGroupLayout);
		
		fdLoginGroup=new FormData();
		fdLoginGroup.left = new FormAttachment(0, margin);
		fdLoginGroup.top  = new FormAttachment(wUrl, 3*margin);
		fdLoginGroup.right= new FormAttachment(100, -1*margin);
		wLoginGroup.setLayoutData(fdLoginGroup); 
		
		wlLoginURL=new Label(wLoginGroup, SWT.LEFT);
		wlLoginURL.setText(Messages.getString("CSWOutputDialog.LoginURL.Label"));
        props.setLook(wlLoginURL);
        fdlwlLoginURL=new FormData();
        fdlwlLoginURL.left = new FormAttachment(0, margin);
        fdlwlLoginURL.top  = new FormAttachment(0, margin);        
        wlLoginURL.setLayoutData(fdlwlLoginURL);
		
		wLoginUrl=new TextVar(transMeta, wLoginGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wLoginUrl);
 		
		fdLoginUrl=new FormData();
		fdLoginUrl.left = new FormAttachment(wlLoginURL, margin);
		fdLoginUrl.top  = new FormAttachment(0, margin);
		fdLoginUrl.right= new FormAttachment(100, -20*margin);
		wLoginUrl.setLayoutData(fdLoginUrl); 
		
		
		wlUser= new Label(wLoginGroup, SWT.LEFT);
		wlUser.setText(Messages.getString("CSWOutputDialog.Username.Label"));
 		props.setLook(wlUser);
 		
		fdwlUser=new FormData();
		fdwlUser.left = new FormAttachment(0, margin);
		fdwlUser.top  = new FormAttachment(wLoginUrl, margin);		
		wlUser.setLayoutData(fdwlUser); 
		
		wUser=new TextVar(transMeta, wLoginGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wUser);
 		wUser.addModifyListener(lsMod);
		fdwUser=new FormData();
		fdwUser.left = new FormAttachment(wlUser, margin);
		fdwUser.top  = new FormAttachment(wLoginUrl, margin);
		fdwUser.right= new FormAttachment(100, -70*margin);
		wUser.setLayoutData(fdwUser); 
		
		wlPassword= new Label(wLoginGroup, SWT.LEFT);
		wlPassword.setText(Messages.getString("CSWOutputDialog.Password.Label"));
 		props.setLook(wlPassword);
 		
		fdwlPassword=new FormData();
		fdwlPassword.left = new FormAttachment(wUser, 4*margin);
		fdwlPassword.top  = new FormAttachment(wLoginUrl, margin);		
		wlPassword.setLayoutData(fdwlPassword); 
		
		wPassword=new TextVar(transMeta, wLoginGroup, SWT.SINGLE | SWT.PASSWORD |SWT.LEFT | SWT.BORDER);
 		props.setLook(wPassword);
 		wPassword.addModifyListener(lsMod);
		fdwPassword=new FormData();
		fdwPassword.left = new FormAttachment(wlPassword, margin);
		fdwPassword.top  = new FormAttachment(wLoginUrl, margin);
		fdwPassword.right= new FormAttachment(100, -4*margin);
		wPassword.setLayoutData(fdwPassword); 
		
		
		///
		wlSchemaLabel=new Label(wGeneral, SWT.LEFT);
		wlSchemaLabel.setText(Messages.getString("CSWOutputDialog.Schema.Label"));
		props.setLook(wlSchemaLabel);
		fdwlSchemaLabel=new FormData();
		fdwlSchemaLabel.left = new FormAttachment(0, margin);
		fdwlSchemaLabel.top  = new FormAttachment(wLoginGroup, 2*margin);		
		wlSchemaLabel.setLayoutData(fdwlSchemaLabel);
 		
		wSchemaLabel= new ComboVar(transMeta, wGeneral, SWT.BORDER ); 			 
		props.setLook(wSchemaLabel);
		wSchemaLabel.setEditable(false);
 		fdwSchemaLabel=new FormData();
 		fdwSchemaLabel.left = new FormAttachment(wlSchemaLabel, margin);
 		fdwSchemaLabel.top  = new FormAttachment(wLoginGroup, 2*margin);
 		fdwSchemaLabel.right= new FormAttachment(75, -1*margin);
 		wSchemaLabel.setLayoutData(fdwSchemaLabel);
 		wSchemaLabel.add(Messages.getString("CSWOutputDialog.Schema.CSWRECORD"));
 		wSchemaLabel.add(Messages.getString("CSWOutputDialog.Schema.MD_METADATA"));
		
        
        fdGeneral = new FormData();
		fdGeneral.left  = new FormAttachment(0, margin);
		fdGeneral.top   = new FormAttachment(wStepname, margin);
		fdGeneral.right = new FormAttachment(100, -margin);
		wGeneral.setLayoutData(fdGeneral);
		
		
		
		/**mapping columns*/
		
		 wMappingColumnGroup = new Group(shell, SWT.SHADOW_NONE);
	        
			props.setLook(wMappingColumnGroup);
			wMappingColumnGroup.setText(Messages.getString("CSWOutputDialog.MappingColumn.Group"));
			FormLayout MappingGroupLayout = new FormLayout();
			MappingGroupLayout.marginWidth = 10;
			MappingGroupLayout.marginHeight = 10;
			wMappingColumnGroup.setLayout(MappingGroupLayout);
			
			fdMappingGroup=new FormData();
			fdMappingGroup.left = new FormAttachment(0, margin);
			fdMappingGroup.top  = new FormAttachment(wGeneral, 3*margin);
			fdMappingGroup.right= new FormAttachment(100, -1*margin);
			wMappingColumnGroup.setLayoutData(fdMappingGroup); 
		
		ColumnInfo[] colinfQueryElement=new ColumnInfo[3];
 		colinfQueryElement[0]=new ColumnInfo(Messages.getString("CSWOutputDialog.SchemaColumn"),  
				ColumnInfo.COLUMN_TYPE_TEXT,null, false);
 		colinfQueryElement[1]=new ColumnInfo(Messages.getString("CSWOutputDialog.PreviousStepColumn"),  
				ColumnInfo.COLUMN_TYPE_TEXT,null, false);
 		
 		colinfQueryElement[2]=new ColumnInfo(Messages.getString("CSWOutputDialog.DefaultValueColumn"),  
				ColumnInfo.COLUMN_TYPE_TEXT,null, false);
 		
		wQueryElement=new TableView(transMeta,wMappingColumnGroup,
							  SWT.BORDER | SWT.MULTI | SWT.V_SCROLL, 
							  colinfQueryElement, 
							  5,  
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
		
		
		
 		wGetQueryElements=new Button(wMappingColumnGroup, SWT.PUSH);
 		wGetQueryElements.setText(Messages.getString("CSWOutputDialog.Button.GetColumn"));
        lsGGetQueryElements = new Listener()  {

		public void handleEvent(Event e){
			getMappingInformation();
			
		}

		
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
        
       
		// Some buttons
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(Messages.getString("System.Button.OK")); //$NON-NLS-1$
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(Messages.getString("System.Button.Cancel")); //$NON-NLS-1$

		setButtonPositions(new Button[] { wOK, wCancel }, margin, wMappingColumnGroup);

		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		lsOK       = new Listener() { public void handleEvent(Event e) { 
			ok();     
			} };
		
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
	
	private void getMappingInformation(){
		String str=CSWWriter.CSWBRIEF_XML;
		ArrayList<String> schemacolumList=new ArrayList<String>(); 
		CSWWriter cswwriter=new CSWWriter();
        Element element=null;
		
        if (wSchemaLabel.getText().equalsIgnoreCase("MD_METADATA")){
        	str=CSWWriter.MD_METADATA_XML;
        }
        
		StepMeta stepMeta = transMeta.findStep(stepname);
        wQueryElement.removeAll();
        
        
		try {
			element = cswwriter.fromStringToJDOMDocument(str).getRootElement();
			Iterator<Element> it=cswwriter.getColumns(element).iterator();
			//int rownr=0;
			while (it.hasNext()){
				Element c=it.next();
				if (wSchemaLabel.getText().equalsIgnoreCase("MD_METADATA")){
					schemacolumList.add(c.getParentElement().getName());
		        }else
				schemacolumList.add(c.getName());

			}
		} catch (KettleException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
         catch (ServletException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        
        if (stepMeta!=null)
        {
            try
            {
            	RowMetaInterface row = transMeta.getPrevStepFields(stepMeta);
            	int colSize=schemacolumList.size();
            	int prevColSize=row.size();
            	int taille=prevColSize;
            	if (colSize>prevColSize){
            		taille=colSize;
            	}            		
            	
            	
                // Remember these fields...
                for (int i=0;i<taille;i++)
                {
                	String colName=null;
                	String prevColName=null;
                	if (i<colSize){
                		colName=schemacolumList.get(i);
                	}
                	if (i<prevColSize){
                		prevColName=row.getValueMeta(i).getName();
                	}                		
                	
                    wQueryElement.add(colName,prevColName);
                	
                }
                wQueryElement.remove(0);
                wQueryElement.setRowNums(); 
                
                ColumnInfo col=new ColumnInfo(Messages.getString("CSWOutputDialog.SchemaColumn"),  
    					ColumnInfo.COLUMN_TYPE_CCOMBO,schemacolumList.toArray(new String[schemacolumList.size()]), false);
                wQueryElement.setColumnInfo(0, col);
                col=new ColumnInfo(Messages.getString("CSWOutputDialog.PreviousStepColumn"),  
    					ColumnInfo.COLUMN_TYPE_CCOMBO,row.getFieldNames(), false);
    			wQueryElement.setColumnInfo(1, col);
                
                //setComboBoxes();
            }
            catch(KettleException e)
            {
            	log.logError(toString(), Messages.getString("System.Dialog.GetFieldsFailed.Message"));
            }
        }
	}

	private void getData() {
		// 
		CSWWriter cswwriter=input.getCSWwriter();
		if (!Const.isEmpty(cswwriter.getCswUrl().toString())){
			wUrl.setText(cswwriter.getCswUrl().toString());
		}
		if (!Const.isEmpty(cswwriter.getLoginUrl().toString())){
			wLoginUrl.setText(cswwriter.getLoginUrl().toString());
		}
		if (!Const.isEmpty(cswwriter.getUsername())){
			wUser.setText(cswwriter.getUsername());
		}
		if (!Const.isEmpty(cswwriter.getPassword())){
			wPassword.setText(cswwriter.getPassword());
		}
		if (!Const.isEmpty(cswwriter.getSchema())){
			wSchemaLabel.setText(cswwriter.getSchema());
		}
		
		ArrayList<String[]> mapColList=cswwriter.getMappingColumns();
		if (mapColList!=null){
			wQueryElement.removeAll();
			for(String[]s:mapColList){
				wQueryElement.add(s);
			}
			wQueryElement.remove(0);
			wQueryElement.setRowNums();
		}
		  ColumnInfo col=new ColumnInfo(Messages.getString("CSWOutputDialog.SchemaColumn"),  
					ColumnInfo.COLUMN_TYPE_CCOMBO,wQueryElement.getItems(0), false);
          wQueryElement.setColumnInfo(0, col);
          col=new ColumnInfo(Messages.getString("CSWOutputDialog.PreviousStepColumn"),  
					ColumnInfo.COLUMN_TYPE_CCOMBO,wQueryElement.getItems(1), false);
			wQueryElement.setColumnInfo(1, col);
		
	}
	private void cancel()
	{
		stepname=null;
		input.setChanged(changed);
		
		dispose();
	}
	private void ok() 
	{
		cswwriter=new CSWWriter();
		try {
			cswwriter.setCswUrl(this.wUrl.getText());
			cswwriter.setLoginUrl(this.wLoginUrl.getText());
			cswwriter.setUsername(this.wUser.getText());
			cswwriter.setPassword(this.wPassword.getText());
			cswwriter.setSchema(this.wSchemaLabel.getText());
			
			wQueryElement.removeEmptyRows();
			ArrayList<String[]> mappingColList=new ArrayList<String[]>();		
			for (int i=0;i<wQueryElement.getItemCount();i++){
				String []s=wQueryElement.getItem(i);
				mappingColList.add(s);		
			}
			cswwriter.setMappingColumns(mappingColList);
			
			if (Const.isEmpty(wStepname.getText())) return;
			stepname=wStepname.getText();
		} catch (MalformedURLException e) {
			
			e.printStackTrace();
		}
				
		input.setCSWwriter(cswwriter);
			
		dispose();
	}

}
