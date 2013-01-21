package org.pentaho.di.ui.trans.steps.ogrfileoutput;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.gdal.ogr.Driver;
import org.gdal.ogr.ogr;
import org.gdal.ogr.ogrConstants;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.ogrfileoutput.Messages;
import org.pentaho.di.trans.steps.ogrfileoutput.OGRFileOutputMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

public class OGRFileOutputDialog extends BaseStepDialog implements StepDialogInterface{	
	private String[] ogrFormats = { };
	private int[] ogrGeomTypes = {ogrConstants.wkbUnknown, ogrConstants.wkbPoint, ogrConstants.wkbLineString, ogrConstants.wkbPolygon, ogrConstants.wkbMultiPoint, ogrConstants.wkbMultiLineString, ogrConstants.wkbMultiPolygon, ogrConstants.wkbGeometryCollection};

	
	private Label		 wlStepformat;
	private Label		 wlGeomtype;
	private Combo		 wcbStepformat;
	private Combo		 wcbGeomtype;
	private FormData     fdlStepformat, fdcbStepformat, fdlOptions, fdOptions, fdlGeomtype, fdcbGeomtype;
	private Label		 wlOptions;
	private TextVar		 wOptions;

	public static final List<String> sourceTypes = new ArrayList<String>(2) {
		private static final long serialVersionUID = 1L;{
			add(Messages.getString("OGRFileOutputDialog.Source.Type.File"));
			add(Messages.getString("OGRFileOutputDialog.Source.Type.ConnectionString"));
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
	
	private Label        wlLayerName;
	private TextVar      wLayerName;
	private FormData     fdlLayerName, fdLayerName;
	
	private Label 		 wlLayerField;
	private Button 		 wLayerField;
	private FormData 	 fdlLayerField,fdLayerField;
	
	private Label 		 wlLayerNameField;
	private CCombo 		 wLayerNameField;
	private FormData 	 fdLayerNameField,fdlLayerNameField;
	
	private Label		 wlOgrMode;
	private Combo		 wcbOgrMode;
	private FormData     fdlOgrMode, fdcbOgrMode;
	private String[]     ogrModes;

	private Label		 wlFIDField;
	private CCombo		 wcbFIDField;
	private FormData     fdlFIDField, fdcbFIDField;

	private Button		 wbbFIDField;
	private FormData     fdbbFIDField;
	
	//private Label        wlEncoding;
    //private CCombo       wEncoding;
    //private FormData     fdlEncoding, fdEncoding;

	private Group wgSource, wgLayer, wgOptParam;
	private FormData fdgSource, fdgLayer, fdgOptParam;
	
	private OGRFileOutputMeta Output;
	private RowMetaInterface prevStepFields;
	private boolean backupChanged;
	//private boolean gotEncodings = false;

	/**
	 * 
	 * @param format
	 * @return
	 */
	public boolean isOGRWritableFormat(String format) {
		String[] readOnlyFormats = {"AeronavFAA", "ArcObjects","AVCBin","AVCE00","ARCGEN","DODS","DWG","EDIGEO","FileGDB","SDE","FMEObjects Gateway","Geomedia","GRASS","HTF","Idrisi","IDB","INGRES","MDB","MSSQLSpatial","MySQL","NAS","OCI","ODBC","OGDI","OpenAir","PCIDSK","PDS","PGeo","PostgreSQL/PostGIS","REC","S57","SDTS","SEGUKOOA","SEGY","SOSI","SUA","SVG","UK .NTF","TIGER","VFK","VRT","XLS","XPlane"};
		//String[] readOnlyFormats = {"AeronavFAA", "ArcObjects","AVCBin","AVCE00","ARCGEN","DODS","DWG","EDIGEO","SDE","FMEObjects Gateway","Geomedia","GRASS","HTF","Idrisi","MDB","MySQL","NAS","ODBC","OGDI","OpenAir","PCIDSK","PDS","PGeo","REC","S57","SDTS","SEGUKOOA","SEGY","SOSI","SUA","SVG","UK .NTF","TIGER","VFK","VRT","XLS","XPlane"};
		for (int i=0;i<readOnlyFormats.length;i++) {
			if (format.equals(readOnlyFormats[i]))
				return false;
		}
		return true;
	}

	public boolean isOGRWritableFormat(Driver driver) {
		return driver.TestCapability( ogr.ODrCCreateDataSource );
	}
	
	public OGRFileOutputDialog(Shell parent, Object out, TransMeta tr, String sname){
		super(parent, (BaseStepMeta)out, tr, sname);
		ogr.RegisterAll();

		ArrayList<String> ogrf = new ArrayList<String>();

		for(int i = 0; i < ogr.GetDriverCount(); i++){
			Driver d = ogr.GetDriver(i);
			if (isOGRWritableFormat(d.getName()))
				ogrf.add(d.getName());
		}
		ogrFormats = (String[]) ogrf.toArray(ogrFormats);
		Arrays.sort(ogrFormats);
		
		ogrModes = new String[5];
		ogrModes[0]=Messages.getString("OGRFileOutputDialog.Dialog.OgrMode.Creation.Label");
		ogrModes[1]=Messages.getString("OGRFileOutputDialog.Dialog.OgrMode.Override.Label");
		ogrModes[2]=Messages.getString("OGRFileOutputDialog.Dialog.OgrMode.Update.Label");
		ogrModes[3]=Messages.getString("OGRFileOutputDialog.Dialog.OgrMode.Append.Label");
		ogrModes[4]=Messages.getString("OGRFileOutputDialog.Dialog.OgrMode.Delete.Label");

		Output=(OGRFileOutputMeta)out;
	}

	public String open(){
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
		props.setLook(shell);
		setShellImage(shell, Output);

		ModifyListener lsMod = new ModifyListener(){
			public void modifyText(ModifyEvent e){
				Output.setChanged();
			}
		};

		/*ModifyListener lsUpdateEncoding = new ModifyListener(){
			public void modifyText(ModifyEvent e){
				updateEncoding();
			}
		};*/
		
		ModifyListener lsModFIDField = new ModifyListener(){
			public void modifyText(ModifyEvent e){
				if (wcbFIDField.getSelectionIndex()==0) {
					wbbFIDField.setEnabled(false);
					wbbFIDField.setSelection(false);
				}
				else wbbFIDField.setEnabled(true);
				Output.setChanged();
			}
		};

		SelectionListener lsCheck = new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				
			}
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				Output.setChanged();
			}			
		};

		backupChanged = Output.hasChanged();

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(Messages.getString("OGRFileOutputDialog.Dialog.Title")); //$NON-NLS-1$

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
				.getString("OGRFileOutputDialog.Source.Group.Label"));
		FormLayout generalGroupLayout = new FormLayout();
		generalGroupLayout.marginWidth = 5;
		generalGroupLayout.marginHeight = 5;
		wgSource.setLayout(generalGroupLayout);
		
		// Stepformat line
		wlStepformat = new Label(wgSource, SWT.RIGHT);
		wlStepformat.setText(Messages.getString("OGRFileOutputDialog.Dialog.DataFormat"));
		props.setLook(wlStepformat);
		fdlStepformat=new FormData();
		fdlStepformat.left = new FormAttachment(0, 0);
		fdlStepformat.right= new FormAttachment(middle, -margin);
		fdlStepformat.top  = new FormAttachment(wStepname, margin*2);
		wlStepformat.setLayoutData(fdlStepformat);
		wcbStepformat = new Combo(wgSource, 
				SWT.DROP_DOWN | SWT.MULTI | 
				SWT.V_SCROLL | SWT.H_SCROLL);
		wcbStepformat.removeAll();
		for (int i = 0; i < ogrFormats.length; i++) {
			wcbStepformat.add(ogrFormats[i]);
		}
		wcbStepformat.select(0);
		props.setLook(wcbStepformat);
		wcbStepformat.addModifyListener(lsMod);
		//wcbStepformat.addModifyListener(lsUpdateEncoding);
		fdcbStepformat = new FormData();
		fdcbStepformat.left = new FormAttachment(middle, margin);
		fdcbStepformat.top  = new FormAttachment(wStepname, margin);
		fdcbStepformat.right= new FormAttachment(100, 0);
		wcbStepformat.setLayoutData(fdcbStepformat);

		// Source is a file or connectionString?
		wSourceType = new Button[2];
		wSourceType[0] = new Button(wgSource, SWT.RADIO);
		wSourceType[0].setText(sourceTypes.get(0));
		fdSourceType2 = new FormData();
		fdSourceType2.left = new FormAttachment(middle, margin);
		fdSourceType2.top = new FormAttachment(wcbStepformat, 2 * margin);
		wSourceType[0].setLayoutData(fdSourceType2);

		wSourceType[1] = new Button(wgSource, SWT.RADIO);
		wSourceType[1].setText(sourceTypes.get(1));
		fdSourceType1 = new FormData();
		fdSourceType1.left = new FormAttachment(wSourceType[0], 4 * margin);
		fdSourceType1.top = new FormAttachment(wcbStepformat, 2 * margin);
		wSourceType[1].setLayoutData(fdSourceType1);

		wlSourceType = new Label(wgSource, SWT.RIGHT);
		wlSourceType.setText(Messages.getString("OGRFileOutputDialog.SourceType.Label"));
		props.setLook(wlSourceType);
		fdlSourceType = new FormData();
		fdlSourceType.left = new FormAttachment(0, 0);
		fdlSourceType.top = new FormAttachment(wcbStepformat, margin * 2);
		fdlSourceType.right = new FormAttachment(middle, -margin);
		wlSourceType.setLayoutData(fdlSourceType);
		
		// Source line
		wlSource=new Label(wgSource, SWT.RIGHT);
		wlSource.setText(Messages.getString("OGRFileOutputDialog.Source.Label")); //$NON-NLS-1$
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
		fdSource.right= new FormAttachment(wbSource, -margin * 2);
		fdSource.top  = new FormAttachment(wSourceType[0], margin);
		wSource.setLayoutData(fdSource);
		
		wSourceType[0].addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent arg0){
				if(!wSourceInField.getSelection())
					wbSource.setEnabled(wSourceType[0].getSelection());
				Output.setChanged();
			}
		});
		
		//Is Source defined in a Field				        
		wlSourceInField=new Label(wgSource, SWT.RIGHT);
		wlSourceInField.setText(Messages.getString("OGRFileOutputDialog.SourceInField.Label"));
		props.setLook(wlSourceInField);
		fdlSourceInField=new FormData();
		fdlSourceInField.left = new FormAttachment(0, 0);
		fdlSourceInField.right= new FormAttachment(middle, -margin);
		fdlSourceInField.top  = new FormAttachment(wSource, margin*2);
		wlSourceInField.setLayoutData(fdlSourceInField);
		wSourceInField=new Button(wgSource, SWT.CHECK);
		wSourceInField.setToolTipText(Messages.getString("OGRFileOutputDialog.SourceInField.Tooltip"));
		props.setLook(wSourceInField);
		fdSourceInField=new FormData();
		fdSourceInField.top   = new FormAttachment(wSource, margin * 2);
		fdSourceInField.left   = new FormAttachment(middle, margin);
		wSourceInField.setLayoutData(fdSourceInField);
		wSourceInField.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent arg0){
				activeSourceInField();
				Output.setChanged();
			}
		});
		
		// Source field
		wlSourceField=new Label(wgSource, SWT.RIGHT);
		wlSourceField.setText(Messages.getString("OGRFileOutputDialog.SourceField.Label"));
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
				setComboFields(wSourceField);
				shell.setCursor(null);
				busy.dispose();
			}
		});
	
		fdgSource = new FormData();
		fdgSource .left = new FormAttachment(0, margin);
		fdgSource .top = new FormAttachment(wStepname, margin);
		fdgSource .right = new FormAttachment(100, -margin);
		wgSource .setLayoutData(fdgSource );
		
		wgOptParam = new Group(shell, SWT.SHADOW_NONE);
		props.setLook(wgOptParam);
		wgOptParam.setText(Messages
				.getString("OGRFileOutputDialog.OptParam.Group.Label"));
		FormLayout optParamGroupLayout = new FormLayout();
		optParamGroupLayout.marginWidth = 10;
		optParamGroupLayout.marginHeight = 10;
		wgOptParam.setLayout(optParamGroupLayout);
		

		wgLayer = new Group(wgOptParam, SWT.SHADOW_NONE);
		props.setLook(wgLayer);
		wgLayer.setText(Messages
				.getString("OGRFileOutputDialog.Layer.Group.Label"));
		FormLayout layerGroupLayout = new FormLayout();
		layerGroupLayout.marginWidth = 10;
		layerGroupLayout.marginHeight = 10;
		wgLayer.setLayout(layerGroupLayout);
		
		// Optional layer name
		wlLayerName=new Label(wgLayer, SWT.RIGHT);
		wlLayerName.setText(Messages.getString("OGRFileOutputDialog.Dialog.LayerName.Label")); //$NON-NLS-1$
		props.setLook(wlLayerName);
		fdlLayerName=new FormData();
		fdlLayerName.left = new FormAttachment(0, 0);
		fdlLayerName.right= new FormAttachment(middle, -margin);
		fdlLayerName.top  = new FormAttachment(wgSource, margin);
		wlLayerName.setLayoutData(fdlLayerName);
		wLayerName=new TextVar(transMeta, wgLayer, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		//wLayerName.setText(LayerName);
		props.setLook(wLayerName);
		wLayerName.addModifyListener(lsMod);
		fdLayerName=new FormData();
		fdLayerName.left = new FormAttachment(middle, margin);
		fdLayerName.top  = new FormAttachment(wgSource, margin);
		fdLayerName.right= new FormAttachment(100, 0);
		wLayerName.setLayoutData(fdLayerName);		

		//Is Layername defined in a Field				        
		wlLayerField=new Label(wgLayer, SWT.RIGHT);
		wlLayerField.setText(Messages.getString("OGRFileOutputDialog.LayernameInField.Label"));
		props.setLook(wlLayerField);
		fdlLayerField=new FormData();
		fdlLayerField.left = new FormAttachment(0, 0);
		fdlLayerField.right= new FormAttachment(middle, -margin);
		fdlLayerField.top  = new FormAttachment(wLayerName, margin * 2);
		wlLayerField.setLayoutData(fdlLayerField);

		wLayerField=new Button(wgLayer, SWT.CHECK);
		wLayerField.setToolTipText(Messages.getString("OGRFileOutputDialog.LayernameInField.Tooltip"));
		props.setLook(wLayerField);
		fdLayerField=new FormData();
		fdLayerField.top   = new FormAttachment(wLayerName, margin * 2);
		fdLayerField.left   = new FormAttachment(middle, margin);
		wLayerField.setLayoutData(fdLayerField);
		wLayerField.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent arg0){
				activeLayerField();
				Output.setChanged();
			}
		});		        
		
		// ConnName field
		wlLayerNameField=new Label(wgLayer, SWT.RIGHT);
		wlLayerNameField.setText(Messages.getString("OGRFileOutputDialog.LayernameField.Label"));
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
				setComboFields(wLayerNameField);
				shell.setCursor(null);
				busy.dispose();
			}
		});
		
		fdgLayer = new FormData();
		fdgLayer.left = new FormAttachment(0, margin);
		fdgLayer.top = new FormAttachment(wgOptParam, margin);
		fdgLayer.right = new FormAttachment(100, -margin);
		wgLayer.setLayoutData(fdgLayer);
		
		//GDAL/OGR options line
		wlOptions=new Label(wgOptParam, SWT.RIGHT);
		wlOptions.setText(Messages.getString("OGRFileOutputDialog.Dialog.OGROptions")); //$NON-NLS-1$
		props.setLook(wlOptions);
		fdlOptions=new FormData();
		fdlOptions.left = new FormAttachment(0, 0);
		fdlOptions.right= new FormAttachment(middle, -margin);
		fdlOptions.top  = new FormAttachment(wgLayer, margin*2);
		wlOptions.setLayoutData(fdlOptions);
		wOptions=new TextVar(transMeta, wgOptParam, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wOptions.setText("");
		props.setLook(wOptions);
		wOptions.addModifyListener(lsMod);
		fdOptions=new FormData();
		fdOptions.left = new FormAttachment(middle, margin);
		fdOptions.top  = new FormAttachment(wgLayer, margin);
		fdOptions.right= new FormAttachment(100, 0);
		wOptions.setLayoutData(fdOptions);

		// OGR mode line
		wlOgrMode = new Label(wgOptParam, SWT.RIGHT);
		wlOgrMode.setText(Messages.getString("OGRFileOutputDialog.Dialog.OgrMode.Label"));
		props.setLook(wlOgrMode);
		fdlOgrMode=new FormData();
		fdlOgrMode.left = new FormAttachment(0, 0);
		fdlOgrMode.right= new FormAttachment(middle, -margin);
		fdlOgrMode.top  = new FormAttachment(wOptions, margin*2);
		wlOgrMode.setLayoutData(fdlOgrMode);
		wcbOgrMode = new Combo(wgOptParam, 
				SWT.DROP_DOWN | SWT.MULTI | 
				SWT.V_SCROLL | SWT.H_SCROLL);
		wcbOgrMode.removeAll();
		for (int i = 0; i < ogrModes.length; i++) {
			wcbOgrMode.add(ogrModes[i]);
		}
		wcbOgrMode.select(0);
		props.setLook(wcbOgrMode);
		wcbOgrMode.addModifyListener(lsMod);
		fdcbOgrMode = new FormData();
		fdcbOgrMode.left = new FormAttachment(middle, margin);
		fdcbOgrMode.top  = new FormAttachment(wOptions, margin);
		fdcbOgrMode.right= new FormAttachment(100, 0);
		wcbOgrMode.setLayoutData(fdcbOgrMode);


		// FID selection line
		wlFIDField = new Label(wgOptParam, SWT.RIGHT);
		wlFIDField.setText(Messages.getString("OGRFileOutputDialog.Dialog.FIDField.Label"));
		props.setLook(wlFIDField);
		fdlFIDField=new FormData();
		fdlFIDField.left = new FormAttachment(0, 0);
		fdlFIDField.right= new FormAttachment(middle, -margin);
		fdlFIDField.top  = new FormAttachment(wcbOgrMode, margin*2);
		wlFIDField.setLayoutData(fdlFIDField);
		wcbFIDField = new CCombo(wgOptParam, SWT.BORDER | SWT.READ_ONLY);
		wcbFIDField.addFocusListener(new FocusListener(){
			public void focusLost(org.eclipse.swt.events.FocusEvent e){}       
			public void focusGained(org.eclipse.swt.events.FocusEvent e){
				Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
				shell.setCursor(busy);
				setComboFields(wcbFIDField);
				shell.setCursor(null);
				busy.dispose();
			}
		});
		
		props.setLook(wcbFIDField);
		wcbFIDField.addModifyListener(lsModFIDField);
		fdcbFIDField = new FormData();
		fdcbFIDField.left = new FormAttachment(middle, margin);
		fdcbFIDField.top  = new FormAttachment(wcbOgrMode, margin);
		fdcbFIDField.right= new FormAttachment(63, 0);
		wcbFIDField.setLayoutData(fdcbFIDField);
		wbbFIDField = new Button(wgOptParam, SWT.CHECK);
		wbbFIDField.setText(Messages.getString("OGRFileOutputDialog.Dialog.FIDField.Checkbox.Label"));
		wbbFIDField.setEnabled(false);
		props.setLook(wbbFIDField);
		wbbFIDField.addSelectionListener(lsCheck);
		fdbbFIDField = new FormData();
		fdbbFIDField.left = new FormAttachment(67, 0);
		fdbbFIDField.top  = new FormAttachment(wcbOgrMode, 2*margin);
		fdbbFIDField.right= new FormAttachment(100, 0);
		wbbFIDField.setLayoutData(fdbbFIDField);

		// Geometry type line
		wlGeomtype = new Label(wgOptParam, SWT.RIGHT);
		wlGeomtype.setText(Messages.getString("OGRFileOutputDialog.Dialog.GeometryType"));
		props.setLook(wlGeomtype);
		fdlGeomtype=new FormData();
		fdlGeomtype.left = new FormAttachment(0, 0);
		fdlGeomtype.right= new FormAttachment(middle, -margin);
		fdlGeomtype.top  = new FormAttachment(wcbFIDField, margin*2);
		wlGeomtype.setLayoutData(fdlGeomtype);
		wcbGeomtype = new Combo(wgOptParam, 
				SWT.DROP_DOWN | SWT.MULTI | 
				SWT.V_SCROLL | SWT.H_SCROLL);
		wcbGeomtype.removeAll();
		for (int i = 0; i < ogrGeomTypes.length; i++) {
			wcbGeomtype.add(org.gdal.ogr.ogr.GeometryTypeToName(ogrGeomTypes[i]));
		}
		wcbGeomtype.select(0);
		props.setLook(wcbGeomtype);
		wcbGeomtype.addModifyListener(lsMod);
		fdcbGeomtype = new FormData();
		fdcbGeomtype.left = new FormAttachment(middle, margin);
		fdcbGeomtype.top  = new FormAttachment(wcbFIDField, margin);
		fdcbGeomtype.right= new FormAttachment(100, 0);
		wcbGeomtype.setLayoutData(fdcbGeomtype);

		//Encoding
        /*wlEncoding=new Label(shell, SWT.RIGHT);
	    wlEncoding.setText(Messages.getString("OGRFileOutputDialog.Encoding.Label"));
	    props.setLook(wlEncoding);
	    fdlEncoding=new FormData();
	    fdlEncoding.left = new FormAttachment(0, 0);
	    fdlEncoding.top  = new FormAttachment(wcbGeomtype, margin*2);
	    fdlEncoding.right= new FormAttachment(middle, -margin);
	    wlEncoding.setLayoutData(fdlEncoding);
	    wEncoding=new CCombo(shell, SWT.BORDER | SWT.READ_ONLY);
	    wEncoding.setEditable(true);
	    props.setLook(wEncoding);
	    setEncodings();
	    wEncoding.addModifyListener(lsMod);
	    fdEncoding=new FormData();
	    fdEncoding.left = new FormAttachment(middle, 0);
	    fdEncoding.top  = new FormAttachment(wcbGeomtype, margin);
	    fdEncoding.right= new FormAttachment(100, 0);
	    wEncoding.setLayoutData(fdEncoding);*/
		
		fdgOptParam = new FormData();
		fdgOptParam.left = new FormAttachment(0, margin);
		fdgOptParam.top = new FormAttachment(wgSource, margin);
		fdgOptParam.right = new FormAttachment(100, -margin);
		wgOptParam.setLayoutData(fdgOptParam);

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
					
					dialog.setFilterNames(new String[] { Messages.getString("System.FileType.AllFiles") }); //$NON-NLS-1$ //$NON-NLS-2$

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
		Output.setChanged(changed);

		// Set the shell size, based upon previous time...
		setSize();

		shell.open();
		while (!shell.isDisposed()){
			if (!display.readAndDispatch()) 
				display.sleep();
		}
		return stepname;
	}

	/**
	 * Copy information from the meta-data Output to the dialog fields.
	 */ 
	public void getData(){
		wSourceType[Output.isFileSource() ? 0 : 1].setSelection(true);
		if(!Const.isEmpty(Output.getSource()))
			wSource.setText(Output.getSource());
		if(Output.isSourceInField())
			wSourceInField.setSelection(true);
		if(!Const.isEmpty(Output.getSourceField()))
			wSourceField.setText(Output.getSourceField());
		if(!Const.isEmpty(Output.getLayerName()))
			wLayerName.setText(Output.getLayerName());
		if(Output.isLayerNameInField())
			wLayerField.setSelection(true);
		if(!Const.isEmpty(Output.getLayerNameField()))
			wLayerNameField.setText(Output.getLayerNameField());
		
		String outputFormat = Output.getOgrOutputFormat();
		if (outputFormat != null){
			for (int i=0; i < ogrFormats.length; i++) {
				if (ogrFormats[i].equalsIgnoreCase(outputFormat)) {
					wcbStepformat.select(i);
					break;
				}
			}
		}

		if (Output.getOgrOptions() != null) 
			wOptions.setText(Output.getOgrOptions());

		wcbOgrMode.select(Output.getOgrWriteMode());

		int geomtype = Output.getOgrGeomType();
		for (int i=0; i < ogrGeomTypes.length; i++) {
			if (ogrGeomTypes[i] == geomtype) {
				wcbGeomtype.select(i);
				break;
			}
		}

		if(!Const.isEmpty(Output.getOgrFIDField()))
			wcbFIDField.setText(Output.getOgrFIDField());

		if (Output.isPreserveFIDField())
			wbbFIDField.setSelection(true);
		else
			wbbFIDField.setSelection(false);		

		wStepname.selectAll();
	}

	/*private void setEncodings(){
	      // Encoding of the shapefile:
	      if (!gotEncodings){
	          gotEncodings = true;
	          
	          wEncoding.removeAll();
	          List<Charset> values = new ArrayList<Charset>(Charset.availableCharsets().values());
	          for (int i=0;i<values.size();i++){
	              Charset charSet = (Charset)values.get(i);
	              wEncoding.add( charSet.displayName() );
	          }
	          
	          // Now select the default!
	          String defEncoding;
			  if (Output.getEncoding()!=null)
				defEncoding = Output.getEncoding();
			  else defEncoding = Const.getEnvironmentVariable("file.encoding", "UTF-8");
	          int idx = Const.indexOfString(defEncoding, wEncoding.getItems() );
	          if (idx>=0) wEncoding.select( idx );
	     }
	}
	
	private void updateEncoding(){
		//Some formats already have vendor options to encode data, if so, user should use those.
		boolean isFormatEncodable = true;
		int length = ogrEncodableFormats.length;
		for(int i = 0 ; i < length ; i++){		
			if(wcbStepformat.getSelectionIndex() > -1 && ogrEncodableFormats[i].equals(ogrFormats[wcbStepformat.getSelectionIndex()])){
				isFormatEncodable = false;
				break;
			}				
		}		
		if(isFormatEncodable)
			wEncoding.setEnabled(true);	
		else{
			//wEncoding.clearSelection();
			//wEncoding.setEnabled(false);
			wEncoding.setEnabled(true);
		}
	}*/
	
	private void cancel(){
		stepname=null;
		Output.setChanged(backupChanged);
		dispose();
	}

	public void getInfo(OGRFileOutputMeta meta) throws KettleStepException{
		// copy info to Meta class (Output)		
		meta.setIsFileSource(wSourceType[0].getSelection() );
		meta.setSource( wSource.getText() );
		meta.setSourceInField(wSourceInField.getSelection());
		meta.setLayerNameInField(wLayerField.getSelection());
		meta.setSourceField(wSourceField.getText());
		meta.setLayerNameField(wLayerNameField.getText());
		meta.setLayerName(wLayerName.getText() );
		meta.setOgrOutputFormat(ogrFormats[wcbStepformat.getSelectionIndex()]);
		meta.setOgrOptions( wOptions.getText() );	
		meta.setOgrWriteMode(wcbOgrMode.getSelectionIndex());
		meta.setOgrGeomType(ogrGeomTypes[wcbGeomtype.getSelectionIndex()]);
		meta.setOgrFIDField(wcbFIDField.getText());
		meta.setPreserveFIDField(wbbFIDField.getSelection());
		//meta.setEncoding(wEncoding.getEnabled()?wEncoding.getText():"");

		if (!wSourceInField.getSelection() && Const.isEmpty(meta.getSource()))
			throw new KettleStepException(Messages.getString("OGRFileOutputDialog.Exception.SpecifyAFileToUse")); //$NON-NLS-1$		
	}

	private void activeSourceInField(){
		wlSourceField.setEnabled(wSourceInField.getSelection());
		wSourceField.setEnabled(wSourceInField.getSelection());			
		wlSource.setEnabled(!wSourceInField.getSelection());		
		wSource.setEnabled(!wSourceInField.getSelection());
		wbSource.setEnabled(!wSourceInField.getSelection());
		
		if(!wSourceInField.getSelection())
			wbSource.setEnabled(wSourceType[0].getSelection());
		else
			wbSource.setEnabled(false);
	}
	
	private void setComboFields(CCombo c){
		c.removeAll();
		getPrevStepFields();
		if (prevStepFields!=null){
			prevStepFields.getFieldNames();
			for (int i=0;i<prevStepFields.getFieldNames().length;i++){	
				c.add(prevStepFields.getFieldNames()[i]);									
			}
		}	
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
	
	private void activeLayerField(){		
		wlLayerNameField.setEnabled(wLayerField.getSelection());
		wLayerNameField.setEnabled(wLayerField.getSelection());	
		wlLayerName.setEnabled(!wLayerField.getSelection());		
		wLayerName.setEnabled(!wLayerField.getSelection());
	}
	
	private void ok(){
		try{
			stepname = wStepname.getText(); // return value
			getInfo(Output);
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
}
