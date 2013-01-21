package org.pentaho.di.ui.trans.steps.ogrfileinput;

import java.util.ArrayList;
import java.util.List;

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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransPreviewFactory;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.ogrfileinput.Messages;
import org.pentaho.di.trans.steps.ogrfileinput.OGRFileInputMeta;
import org.pentaho.di.ui.core.dialog.EnterNumberDialog;
import org.pentaho.di.ui.core.dialog.EnterTextDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.dialog.PreviewRowsDialog;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.dialog.TransPreviewProgressDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

public class OGRFileInputDialog extends BaseStepDialog implements StepDialogInterface{
	public static final List<String> sourceTypes = new ArrayList<String>(2) {
		private static final long serialVersionUID = 1L;{
			add(Messages.getString("OGRFileInputDialog.Source.Type.File"));
			add(Messages.getString("OGRFileInputDialog.Source.Type.ConnectionString"));
		}
	};
	
	private Label wlSourceType;
	private Button[] wSourceType;
	private FormData fdlSourceType, fdSourceType1, fdSourceType2;
	
	private Label        wlSource;
	private Button       wbSource;
	private TextVar      wSource;
	private FormData     fdlSource, fdbSource, fdSource;
	
	private Label 		 wlSourceInField;
	private Button 		 wSourceInField;
	private FormData 	 fdlSourceInField,fdSourceInField;
	
	private Label 		 wlSourceField;
	private CCombo 		 wSourceField;
	private FormData 	 fdSourceField,fdlSourceField;
	
	private Label        wlPassThruFields;
	private Button       wPassThruFields;
	private FormData     fdlPassThruFields, fdPassThruFields;
	
	private Label        wlLayerName;
	private TextVar      wLayerName;
	private FormData     fdlLayerName, fdLayerName;
	
	private Label 		 wlLayerField;
	private Button 		 wLayerField;
	private FormData 	 fdlLayerField,fdLayerField;
	
	private Label 		 wlLayerNameField;
	private CCombo 		 wLayerNameField;
	private FormData 	 fdLayerNameField,fdlLayerNameField;

	private Label        wlSpatialFilter;
	private TextVar      wSpatialFilter;
	private FormData     fdlSpatialFilter, fdSpatialFilter;
	
	private Label        wlAttributeFilter;
	private TextVar      wAttributeFilter;
	private FormData     fdlAttributeFilter, fdAttributeFilter;
	
	private Label        wlSkipFailure;
	private Button       wSkipFailure;
	private FormData     fdlSkipFailure,fdSkipFailure;
	
	private Label        wlLimit;
	private Text         wLimit;
	private FormData     fdlLimit, fdLimit;
	
	private Label        wlAddRownr;
	private Button       wAddRownr;
	private FormData     fdlAddRownr, fdAddRownr;

	private Label        wlFieldRownr;
	private Text         wFieldRownr;
	private FormData     fdlFieldRownr, fdFieldRownr;
	
	private Group wgSource, wgLayer, wgOptParam;
	private FormData fdgSource, fdgLayer, fdgOptParam;
    
	private OGRFileInputMeta input;
	
	private RowMetaInterface prevStepFields;
	
	private boolean backupChanged, backupAddRownr, backupSkipFailure;

	public OGRFileInputDialog(Shell parent, Object in, TransMeta tr, String sname){
		super(parent, (BaseStepMeta)in, tr, sname);
		input=(OGRFileInputMeta)in;
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
		
		backupChanged = input.hasChanged();
		backupSkipFailure = input.isSkipFailureAdded();
		backupAddRownr = input.isRowNrAdded();

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(Messages.getString("OGRFileInputDialog.Dialog.Title")); //$NON-NLS-1$
		
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
		fdStepname.left = new FormAttachment(middle, margin);
		fdStepname.top  = new FormAttachment(0, margin);
		fdStepname.right= new FormAttachment(100, 0);
		wStepname.setLayoutData(fdStepname);
	
		
		wgSource = new Group(shell, SWT.SHADOW_NONE);
		props.setLook(wgSource);
		wgSource.setText(Messages
				.getString("OGRFileInputDialog.Source.Group.Label"));
		FormLayout generalGroupLayout = new FormLayout();
		generalGroupLayout.marginWidth = 5;
		generalGroupLayout.marginHeight = 5;
		wgSource.setLayout(generalGroupLayout);
		
		// Source is a file or connectionString?
		wSourceType = new Button[2];
		wSourceType[0] = new Button(wgSource, SWT.RADIO);
		wSourceType[0].setText(sourceTypes.get(0));
		fdSourceType2 = new FormData();
		fdSourceType2.left = new FormAttachment(middle, margin);
		fdSourceType2.top = new FormAttachment(wStepname, 2 * margin);
		wSourceType[0].setLayoutData(fdSourceType2);

		wSourceType[1] = new Button(wgSource, SWT.RADIO);
		wSourceType[1].setText(sourceTypes.get(1));
		fdSourceType1 = new FormData();
		fdSourceType1.left = new FormAttachment(wSourceType[0], 4 * margin);
		fdSourceType1.top = new FormAttachment(wStepname, 2 * margin);
		wSourceType[1].setLayoutData(fdSourceType1);

		wlSourceType = new Label(wgSource, SWT.RIGHT);
		wlSourceType.setText(Messages.getString("OGRFileInputDialog.SourceType.Label"));
		props.setLook(wlSourceType);
		fdlSourceType = new FormData();
		fdlSourceType.left = new FormAttachment(0, 0);
		fdlSourceType.top = new FormAttachment(wStepname, margin * 2);
		fdlSourceType.right = new FormAttachment(middle, -margin);
		wlSourceType.setLayoutData(fdlSourceType);
		
		// Source line
		wlSource=new Label(wgSource, SWT.RIGHT);
		wlSource.setText(Messages.getString("OGRFileInputDialog.Source.Label")); //$NON-NLS-1$
 		props.setLook(wlSource);
		fdlSource=new FormData();
		fdlSource.left = new FormAttachment(0, 0);
		fdlSource.top  = new FormAttachment(wSourceType[0], margin*2);
		fdlSource.right= new FormAttachment(middle, -margin);
		wlSource.setLayoutData(fdlSource);
		
		wbSource=new Button(wgSource, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbSource);
		wbSource.setText(Messages.getString("System.Button.Browse")); //$NON-NLS-1$
		fdbSource=new FormData();
		fdbSource.right= new FormAttachment(100, 0);
		fdbSource.top  = new FormAttachment(wSourceType[0], margin);
		wbSource.setLayoutData(fdbSource);

		wSource=new TextVar(transMeta, wgSource, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wSource);
		wSource.addModifyListener(lsMod);
		fdSource=new FormData();
		fdSource.left = new FormAttachment(middle, margin);
		fdSource.right= new FormAttachment(wbSource, -margin);
		fdSource.top  = new FormAttachment(wSourceType[0], margin);
		wSource.setLayoutData(fdSource);
		
		wSourceType[0].addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent arg0){
				if(!wSourceInField.getSelection())
					wbSource.setEnabled(wSourceType[0].getSelection());
				input.setChanged();
			}
		});
		
		//Is Source defined in a Field				        
		wlSourceInField=new Label(wgSource, SWT.RIGHT);
		wlSourceInField.setText(Messages.getString("OGRFileInputDialog.SourceInField.Label"));
		props.setLook(wlSourceInField);
		fdlSourceInField=new FormData();
		fdlSourceInField.left = new FormAttachment(0, 0);
		fdlSourceInField.right= new FormAttachment(middle, -margin);
		fdlSourceInField.top  = new FormAttachment(wSource, margin*2);
		wlSourceInField.setLayoutData(fdlSourceInField);
		wSourceInField=new Button(wgSource, SWT.CHECK);
		wSourceInField.setToolTipText(Messages.getString("OGRFileInputDialog.SourceInField.Tooltip"));
		props.setLook(wSourceInField);
		fdSourceInField=new FormData();
		fdSourceInField.top   = new FormAttachment(wSource, margin * 2);
		fdSourceInField.left   = new FormAttachment(middle, margin);
		wSourceInField.setLayoutData(fdSourceInField);
		wSourceInField.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent arg0){
				activeSourceInField();
				input.setChanged();
			}
		});
		
		// Source field
		wlSourceField=new Label(wgSource, SWT.RIGHT);
		wlSourceField.setText(Messages.getString("OGRFileInputDialog.SourceField.Label"));
		props.setLook(wlSourceField);
		fdlSourceField=new FormData();
		fdlSourceField.top  = new FormAttachment(wSource, margin * 2);
		fdlSourceField.left   = new FormAttachment(wSourceInField, margin * 6);	
		wlSourceField.setLayoutData(fdlSourceField);

		wSourceField=new CCombo(wgSource, SWT.BORDER | SWT.READ_ONLY);
		wSourceField.setEditable(true);
		props.setLook(wSourceField);
		wSourceField.addModifyListener(lsMod);
		fdSourceField=new FormData();
		fdSourceField.top  = new FormAttachment(wSource, margin);
		fdSourceField.right= new FormAttachment(100, 0);
		fdSourceField.left   = new FormAttachment(wlSourceField, margin);	
		wSourceField.setLayoutData(fdSourceField);
		wSourceField.addFocusListener(new FocusListener(){
			public void focusLost(org.eclipse.swt.events.FocusEvent e){}       
			public void focusGained(org.eclipse.swt.events.FocusEvent e){
				Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
				shell.setCursor(busy);
				setSourceInField();
				shell.setCursor(null);
				busy.dispose();
			}
		});
		
		fdgSource = new FormData();
		fdgSource.left = new FormAttachment(0, margin);
		fdgSource.top = new FormAttachment(wStepname, margin);
		fdgSource.right = new FormAttachment(100, -margin);
		wgSource .setLayoutData(fdgSource);
		
		wgOptParam = new Group(shell, SWT.SHADOW_NONE);
		props.setLook(wgOptParam);
		wgOptParam.setText(Messages
				.getString("OGRFileInputDialog.OptParam.Group.Label"));
		FormLayout optParamGroupLayout = new FormLayout();
		optParamGroupLayout.marginWidth = 10;
		optParamGroupLayout.marginHeight = 10;
		wgOptParam.setLayout(optParamGroupLayout);
		
		wgLayer = new Group(wgOptParam, SWT.SHADOW_NONE);
		props.setLook(wgLayer);
		wgLayer.setText(Messages
				.getString("OGRFileInputDialog.Layer.Group.Label"));
		FormLayout layerGroupLayout = new FormLayout();
		layerGroupLayout.marginWidth = 10;
		layerGroupLayout.marginHeight = 10;
		wgLayer.setLayout(layerGroupLayout);
		
		// Optional layer name
		wlLayerName=new Label(wgLayer, SWT.RIGHT);
		wlLayerName.setText(Messages.getString("OGRFileInputDialog.Dialog.LayerName.Label")); //$NON-NLS-1$
		props.setLook(wlLayerName);
		fdlLayerName=new FormData();
		fdlLayerName.left = new FormAttachment(0, 0);
		fdlLayerName.right= new FormAttachment(middle, -margin);
		fdlLayerName.top  = new FormAttachment(wgLayer, margin);
		wlLayerName.setLayoutData(fdlLayerName);
		wLayerName=new TextVar(transMeta, wgLayer, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		//wLayerName.setText(LayerName);
		props.setLook(wLayerName);
		wLayerName.addModifyListener(lsMod);
		fdLayerName=new FormData();
		fdLayerName.left = new FormAttachment(middle, margin);
		fdLayerName.top  = new FormAttachment(wgLayer, margin);
		fdLayerName.right= new FormAttachment(100, 0);
		wLayerName.setLayoutData(fdLayerName);		

		//Is Layername defined in a Field				        
		wlLayerField=new Label(wgLayer, SWT.RIGHT);
		wlLayerField.setText(Messages.getString("OGRFileInputDialog.LayernameInField.Label"));
		props.setLook(wlLayerField);
		fdlLayerField=new FormData();
		fdlLayerField.left = new FormAttachment(0, 0);
		fdlLayerField.right= new FormAttachment(middle, -margin);
		fdlLayerField.top  = new FormAttachment(wLayerName, margin * 2);
		wlLayerField.setLayoutData(fdlLayerField);

		wLayerField=new Button(wgLayer, SWT.CHECK);
		wLayerField.setToolTipText(Messages.getString("OGRFileInputDialog.LayernameInField.Tooltip"));
		props.setLook(wLayerField);
		fdLayerField=new FormData();
		fdLayerField.top   = new FormAttachment(wLayerName, margin * 2);
		fdLayerField.left   = new FormAttachment(middle, margin);
		wLayerField.setLayoutData(fdLayerField);
		wLayerField.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent arg0){
				activeLayerField();
				input.setChanged();
			}
		});
		
		// ConnName field
		wlLayerNameField=new Label(wgLayer, SWT.RIGHT);
		wlLayerNameField.setText(Messages.getString("OGRFileInputDialog.LayernameField.Label"));
		props.setLook(wlLayerNameField);
		fdlLayerNameField=new FormData();
		fdlLayerNameField.top  = new FormAttachment(wLayerName, margin*2);
		fdlLayerNameField.left   = new FormAttachment(wLayerField, margin * 6);
		wlLayerNameField.setLayoutData(fdlLayerNameField);

		wLayerNameField=new CCombo(wgLayer, SWT.BORDER | SWT.READ_ONLY);
		wLayerNameField.setEditable(true);
		props.setLook(wLayerNameField);
		wLayerNameField.addModifyListener(lsMod);
		fdLayerNameField=new FormData();
		fdLayerNameField.top  = new FormAttachment(wLayerName, margin);
		fdLayerNameField.right= new FormAttachment(100, 0);
		fdLayerNameField.left   = new FormAttachment(wlLayerNameField, margin);
		wLayerNameField.setLayoutData(fdLayerNameField);
		wLayerNameField.addFocusListener(new FocusListener(){
			public void focusLost(org.eclipse.swt.events.FocusEvent e){}       
			public void focusGained(org.eclipse.swt.events.FocusEvent e){
				Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
				shell.setCursor(busy);
				setLayerField();
				shell.setCursor(null);
				busy.dispose();
			}
		});
		
		fdgLayer = new FormData();
		fdgLayer.left = new FormAttachment(0, margin);
		fdgLayer.top = new FormAttachment(wgOptParam, margin);
		fdgLayer.right = new FormAttachment(100, -margin);
		wgLayer.setLayoutData(fdgLayer);
		
		wlPassThruFields=new Label(wgOptParam, SWT.RIGHT);
		wlPassThruFields.setText(Messages.getString("OGRFileInputDialog.PassThruFields.Label"));
		props.setLook(wlPassThruFields);
		fdlPassThruFields=new FormData();
		fdlPassThruFields.top  = new FormAttachment(wgLayer, margin);
		fdlPassThruFields.left = new FormAttachment(0, 0);
		fdlPassThruFields.right= new FormAttachment(middle, -margin);
		wlPassThruFields.setLayoutData(fdlPassThruFields);

		wPassThruFields=new Button(wgOptParam, SWT.CHECK);
		wPassThruFields.setToolTipText(Messages.getString("OGRFileInputDialog.PassThruFields.Tooltip"));
		props.setLook(wPassThruFields);
		fdPassThruFields=new FormData();
		fdPassThruFields.top  = new FormAttachment(wgLayer, margin);
		fdPassThruFields.left = new FormAttachment(middle, margin);
		fdPassThruFields.right= new FormAttachment(100, 0);
		wPassThruFields.setLayoutData(fdPassThruFields);
		
		// Spatial filter line
		wlSpatialFilter=new Label(wgOptParam, SWT.RIGHT);
		wlSpatialFilter.setText(Messages.getString("OGRFileInputDialog.Dialog.SpatialFilter.Label")); //$NON-NLS-1$
 		props.setLook(wlSpatialFilter);
		fdlSpatialFilter=new FormData();
		fdlSpatialFilter.left = new FormAttachment(0, 0);
		fdlSpatialFilter.right= new FormAttachment(middle, -margin);
		fdlSpatialFilter.top  = new FormAttachment(wPassThruFields, margin*2);
		wlSpatialFilter.setLayoutData(fdlSpatialFilter);
		wSpatialFilter=new TextVar(transMeta, wgOptParam, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		//wSpatialFilter.setText(SpatialFilter);
 		props.setLook(wSpatialFilter);
		wSpatialFilter.addModifyListener(lsMod);
		fdSpatialFilter=new FormData();
		fdSpatialFilter.left = new FormAttachment(middle, margin);
		fdSpatialFilter.top  = new FormAttachment(wPassThruFields, margin);
		fdSpatialFilter.right= new FormAttachment(100, 0);
		wSpatialFilter.setLayoutData(fdSpatialFilter);		

		// Where clause line
		wlAttributeFilter=new Label(wgOptParam, SWT.RIGHT);
		wlAttributeFilter.setText(Messages.getString("OGRFileInputDialog.Dialog.WhereClause.Label")); //$NON-NLS-1$
 		props.setLook(wlAttributeFilter);
		fdlAttributeFilter=new FormData();
		fdlAttributeFilter.left = new FormAttachment(0, 0);
		fdlAttributeFilter.right= new FormAttachment(middle, -margin);
		fdlAttributeFilter.top  = new FormAttachment(wSpatialFilter, margin*2);
		wlAttributeFilter.setLayoutData(fdlAttributeFilter);
		wAttributeFilter=new TextVar(transMeta, wgOptParam, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		//wAttributeFilter.setText(AttributeFilter);
 		props.setLook(wAttributeFilter);
		wAttributeFilter.addModifyListener(lsMod);
		fdAttributeFilter=new FormData();
		fdAttributeFilter.left = new FormAttachment(middle, margin);
		fdAttributeFilter.top  = new FormAttachment(wSpatialFilter, margin);
		fdAttributeFilter.right= new FormAttachment(100, 0);
		wAttributeFilter.setLayoutData(fdAttributeFilter);		
		
		//Skip failures
		wlSkipFailure=new Label(wgOptParam, SWT.RIGHT);
		wlSkipFailure.setText(Messages.getString("OGRFileInputDialog.Dialog.SkipFailure.Label")); //$NON-NLS-1$
 		props.setLook(wlSkipFailure);
		fdlSkipFailure=new FormData();
		fdlSkipFailure.left = new FormAttachment(0, 0);
		fdlSkipFailure.top  = new FormAttachment(wAttributeFilter, margin*2);
		fdlSkipFailure.right= new FormAttachment(middle, -margin);
		wlSkipFailure.setLayoutData(fdlSkipFailure);
		wSkipFailure=new Button(wgOptParam, SWT.CHECK );
 		props.setLook(wSkipFailure);
		wSkipFailure.setToolTipText(Messages.getString("OGRFileInputDialog.SkipFailure.Tooltip")); //$NON-NLS-1$
		fdSkipFailure=new FormData();
		fdSkipFailure.left = new FormAttachment(middle, margin);
		fdSkipFailure.top  = new FormAttachment(wAttributeFilter, margin*2);
		wSkipFailure.setLayoutData(fdSkipFailure);
		wSkipFailure.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { input.setChanged(); setFlags(); } } );
		
		// Limit input ...
		wlLimit=new Label(wgOptParam, SWT.RIGHT);
		wlLimit.setText(Messages.getString("OGRFileInputDialog.LimitSize.Label")); //$NON-NLS-1$
 		props.setLook(wlLimit);
		fdlLimit=new FormData();
		fdlLimit.left = new FormAttachment(wSkipFailure, margin *6 );
		fdlLimit.top  = new FormAttachment(wAttributeFilter, margin*2);
		wlLimit.setLayoutData(fdlLimit);
		wLimit=new Text(wgOptParam, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wLimit);
		wLimit.addModifyListener(lsMod);
		fdLimit=new FormData();
		fdLimit.left = new FormAttachment(wlLimit, margin);
		fdLimit.top  = new FormAttachment(wAttributeFilter, margin);
		fdLimit.right= new FormAttachment(100, 0);
		wLimit.setLayoutData(fdLimit);

		// Add rownr (1...)?
		wlAddRownr=new Label(wgOptParam, SWT.RIGHT);
		wlAddRownr.setText(Messages.getString("OGRFileInputDialog.AddRowNr.Label")); //$NON-NLS-1$
 		props.setLook(wlAddRownr);
		fdlAddRownr=new FormData();
		fdlAddRownr.left = new FormAttachment(0, 0);
		fdlAddRownr.top  = new FormAttachment(wLimit, margin*2);
		fdlAddRownr.right= new FormAttachment(middle, -margin);
		wlAddRownr.setLayoutData(fdlAddRownr);
		wAddRownr=new Button(wgOptParam, SWT.CHECK );
 		props.setLook(wAddRownr);
		wAddRownr.setToolTipText(Messages.getString("OGRFileInputDialog.AddRowNr.Tooltip")); //$NON-NLS-1$
		fdAddRownr=new FormData();
		fdAddRownr.left = new FormAttachment(middle, margin);
		fdAddRownr.top  = new FormAttachment(wLimit, margin*2);
		wAddRownr.setLayoutData(fdAddRownr);
		wAddRownr.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { input.setChanged(); setFlags(); } } );

		// FieldRownr input ...
		wlFieldRownr=new Label(wgOptParam, SWT.LEFT);
		wlFieldRownr.setText(Messages.getString("OGRFileInputDialog.FieldnameOfRowNr.Label")); //$NON-NLS-1$
 		props.setLook(wlFieldRownr);
		fdlFieldRownr=new FormData();
		fdlFieldRownr.left = new FormAttachment(wAddRownr, margin * 6);
		fdlFieldRownr.top  = new FormAttachment(wLimit, margin*2);
		wlFieldRownr.setLayoutData(fdlFieldRownr);
		wFieldRownr=new Text(wgOptParam, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wFieldRownr);
		wFieldRownr.addModifyListener(lsMod);
		fdFieldRownr=new FormData();
		fdFieldRownr.left = new FormAttachment(wlFieldRownr, margin);
		fdFieldRownr.top  = new FormAttachment(wLimit, margin);
		fdFieldRownr.right= new FormAttachment(100, 0);
		wFieldRownr.setLayoutData(fdFieldRownr);
		
		fdgOptParam = new FormData();
		fdgOptParam.left = new FormAttachment(0, margin);
		fdgOptParam.top = new FormAttachment(wgSource, margin);
		fdgOptParam.right = new FormAttachment(100, -margin);
		wgOptParam.setLayoutData(fdgOptParam);
		
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
		
		lsDef=new SelectionAdapter() { 
			public void widgetDefaultSelected(SelectionEvent e) { 
				ok(); 
			} 
		};
		
		wStepname.addSelectionListener( lsDef );
		wLimit.addSelectionListener( lsDef );
		wFieldRownr.addSelectionListener( lsDef );
		
		wSource.addModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent arg0){
				wSource.setToolTipText(transMeta.environmentSubstitute(wSource.getText()));
			}
		});
		
		wbSource.addSelectionListener(
			new SelectionAdapter(){
				public void widgetSelected(SelectionEvent e){
					FileDialog dialog = new FileDialog(shell, SWT.OPEN);

					if (wSource.getText()!=null)
						dialog.setFileName(wSource.getText());
											
					dialog.setFilterNames(new String[] {Messages.getString("OGRFileInputDialog.Filter.SHPFiles"), Messages.getString("System.FileType.AllFiles")}); //$NON-NLS-1$ //$NON-NLS-2$
					
					if (dialog.open()!=null){
						String str = dialog.getFilterPath()+Const.FILE_SEPARATOR+dialog.getFileName();
						wSource.setText(str);
					}
				}
			}
		);

		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );
		
		getData();
		activeSourceInField();
		activeLayerField();
		
		input.setChanged(changed);

		// Set the shell size, based upon previous time...
		setSize();
		
		shell.open();
		while (!shell.isDisposed()){
				if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}
	
	protected void setFlags(){
        wlFieldRownr.setEnabled( wAddRownr.getSelection() );
        wFieldRownr.setEnabled( wAddRownr.getSelection() );
    }
	
	private void getPrevStepFields(){
		if(prevStepFields == null){
			try {
				prevStepFields = transMeta.getPrevStepFields(stepMeta);
			} catch (KettleStepException e) {
				new ErrorDialog(shell, Messages.getString("OGRFileInputDialog.FailedToGetFields.DialogTitle"), Messages.getString("OGRFileInputDialog.FailedToGetFields.DialogMessage"), e); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
	}
	
	/**
	 * Copy information from the meta-data input to the dialog fields.
	 * @throws KettleStepException 
	 */ 
	public void getData(){
		wSourceType[input.isFileSource() ? 0 : 1].setSelection(true);
		if(!Const.isEmpty(input.getSource()))
			wSource.setText(input.getSource());
		if(input.isSourceInField())
			wSourceInField.setSelection(true);
		if(!Const.isEmpty(input.getSourceField()))
			wSourceField.setText(input.getSourceField());
		if(!Const.isEmpty(input.getLayerName()))
			wLayerName.setText(input.getLayerName());
		if(input.isLayerNameInField())
			wLayerField.setSelection(true);
		if(!Const.isEmpty(input.getLayerNameField()))
			wLayerNameField.setText(input.getLayerNameField());
		wPassThruFields.setSelection(input.isPassingThruFields());

		if (input.getSpatialFilter() != null)
			wSpatialFilter.setText(input.getSpatialFilter());

		if (input.getAttributeFilter() != null)
			wAttributeFilter.setText(input.getAttributeFilter());
		
		wSkipFailure.setSelection(input.isSkipFailureAdded());
		wLimit.setText(Integer.toString(input.getRowLimit())); //$NON-NLS-1$
		wAddRownr.setSelection(input.isRowNrAdded());
		if (input.getRowNrField()!=null) wFieldRownr.setText(input.getRowNrField());
		
        setFlags();
		
		wStepname.selectAll();
	}

	private void cancel(){
		stepname=null;
		input.setRowNrAdded( backupSkipFailure );
		input.setRowNrAdded( backupAddRownr );
		input.setChanged(backupChanged);
		dispose();
	}
	
	public void getInfo(OGRFileInputMeta meta) throws KettleStepException{
		// copy info to Meta class (input)
		meta.setIsFileSource(wSourceType[0].getSelection() );
		meta.setSource( wSource.getText() );
		meta.setSourceInField(wSourceInField.getSelection());
		meta.setLayerNameInField(wLayerField.getSelection());
		meta.setPassingThruFields( wPassThruFields.getSelection() );
		meta.setSourceField(wSourceField.getText());
		meta.setLayerNameField(wLayerNameField.getText());
		meta.setLayerName(wLayerName.getText() );
		meta.setSpatialFilter(wSpatialFilter.getText() );
		meta.setAttributeFilter(wAttributeFilter.getText() );
		meta.setSkipFailureAdded( wSkipFailure.getSelection() );
		meta.setRowLimit( Const.toInt(wLimit.getText(), 0 ) );
        meta.setRowNrAdded( wAddRownr.getSelection() );
		meta.setRowNrField( wFieldRownr.getText() );

		if ((!wSourceInField.getSelection() && Const.isEmpty(meta.getSource())))
			throw new KettleStepException(Messages.getString("OGRFileInputDialog.Exception.SpecifyASourceToUse")); //$NON-NLS-1$
	}
	
	private void ok(){
		try{
			stepname = wStepname.getText(); // return value
			getInfo(input);
			dispose();
		}catch(KettleStepException e){
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage(e.toString());
			mb.setText(Messages.getString("System.Warning")); //$NON-NLS-1$
			mb.open();
			
			// Close anyway!
			dispose();
		}
	}
	
	private void setSourceInField(){
		wSourceField.removeAll();						
		getPrevStepFields();
		if (prevStepFields!=null){
			prevStepFields.getFieldNames();
			for (int i=0;i<prevStepFields.getFieldNames().length;i++){	
				wSourceField.add(prevStepFields.getFieldNames()[i]);									
			}
		}								
	}

	private void activeSourceInField(){
		wlSourceField.setEnabled(wSourceInField.getSelection());
		wSourceField.setEnabled(wSourceInField.getSelection());			
		wlSource.setEnabled(!wSourceInField.getSelection());		
		wSource.setEnabled(!wSourceInField.getSelection());
		wbSource.setEnabled(!wSourceInField.getSelection());
		activePassThrough();		
		if(!wSourceInField.getSelection())
			wbSource.setEnabled(wSourceType[0].getSelection());
		else
			wbSource.setEnabled(false);
	}
	
	private void activePassThrough(){
		boolean enabled = wSourceInField.getSelection() || wLayerField.getSelection() ? true : false;
		wlPassThruFields.setEnabled(enabled);
		wPassThruFields.setEnabled(enabled);
	}

	private void setLayerField(){
		wLayerNameField.removeAll();
		getPrevStepFields();
		if (prevStepFields!=null){
			prevStepFields.getFieldNames();
			for (int i=0;i<prevStepFields.getFieldNames().length;i++){	
				wLayerNameField.add(prevStepFields.getFieldNames()[i]);									
			}
		}	
	}

	private void activeLayerField(){		
		wlLayerNameField.setEnabled(wLayerField.getSelection());
		wLayerNameField.setEnabled(wLayerField.getSelection());	
		wlLayerName.setEnabled(!wLayerField.getSelection());		
		wLayerName.setEnabled(!wLayerField.getSelection());	
		activePassThrough();
	}
	
    // Preview the data
    private void preview(){
        // Create the XML input step
    	try{
	        OGRFileInputMeta oneMeta = new OGRFileInputMeta();
	        getInfo(oneMeta);
	        
	        if (oneMeta.isSourceInField()){
				MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION);
				mb.setMessage(Messages.getString("OGRFileInputDialog.Dialog.SpecifyASampleFile.Message"));
				mb.setText(Messages.getString("OGRFileInputDialog.Dialog.SpecifyASampleFile.Title"));
				mb.open();
				return;
			}else if (oneMeta.isLayerNameInField()){
				MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION);
				mb.setMessage(Messages.getString("OGRFileInputDialog.Dialog.SpecifyASampleLayer.Message"));
				mb.setText(Messages.getString("OGRFileInputDialog.Dialog.SpecifyASampleLayer.Title"));
				mb.open();
				return;
			}
            
            TransMeta previewMeta = TransPreviewFactory.generatePreviewTransformation(transMeta, oneMeta, wStepname.getText());
	        
	        EnterNumberDialog numberDialog = new EnterNumberDialog(shell, props.getDefaultPreviewSize(), Messages.getString("OGRFileInputDialog.PreviewSize.DialogTitle"), Messages.getString("OGRFileInputDialog.PreviewSize.DialogMessage")); //$NON-NLS-1$ //$NON-NLS-2$
	        int previewSize = numberDialog.open();
	        if (previewSize>0){
	            TransPreviewProgressDialog progressDialog = new TransPreviewProgressDialog(shell, previewMeta, new String[] { wStepname.getText() }, new int[] { previewSize } );
	            progressDialog.open();
	
	            Trans trans = progressDialog.getTrans();
	            String loggingText = progressDialog.getLoggingText();
	
	            if (!progressDialog.isCancelled()){
	                if (trans.getResult()!=null && trans.getResult().getNrErrors()>0){
	                	EnterTextDialog etd = new EnterTextDialog(shell, Messages.getString("System.Dialog.PreviewError.Title"),   //$NON-NLS-1$
	                			Messages.getString("System.Dialog.PreviewError.Message"), loggingText, true ); //$NON-NLS-1$
	                	etd.setReadOnly();
	                	etd.open();
	                }
	            }
	            
	            PreviewRowsDialog prd =new PreviewRowsDialog(shell, transMeta, SWT.NONE, wStepname.getText(), progressDialog.getPreviewRowsMeta(wStepname.getText()), progressDialog.getPreviewRows(wStepname.getText()), loggingText);
	            prd.open();
	        }
    	}catch(Exception e){
    		new ErrorDialog(shell, Messages.getString("System.Dialog.PreviewError.Title"),  //$NON-NLS-1$
    				Messages.getString("System.Dialog.PreviewError.Message"), e); //$NON-NLS-1$
    	}
    }
}
