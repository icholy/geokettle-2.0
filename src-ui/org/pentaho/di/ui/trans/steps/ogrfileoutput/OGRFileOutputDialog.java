package org.pentaho.di.ui.trans.steps.ogrfileoutput;

import java.util.ArrayList;

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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
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
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.ogrfileoutput.Messages;
import org.pentaho.di.trans.steps.ogrfileoutput.OGRFileOutputMeta;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

public class OGRFileOutputDialog extends BaseStepDialog implements StepDialogInterface
{
	//final static private String[] OGRFILE_FILTER_EXT = new String[] {"*.shp;*.SHP", "*"};
	
	private String[] ogrFormats = { };
	private int[] ogrGeomTypes = {ogrConstants.wkbUnknown, ogrConstants.wkbPoint, ogrConstants.wkbLineString, ogrConstants.wkbPolygon, ogrConstants.wkbMultiPoint, ogrConstants.wkbMultiLineString, ogrConstants.wkbMultiPolygon, ogrConstants.wkbGeometryCollection};
	
	private Label        wlFilename;
	private Button       wbFilename;
	private Label		 wlStepformat;
	private Label		 wlGeomtype;
	private Combo		 wcbStepformat;
	private Combo		 wcbGeomtype;
	private TextVar      wFilename;
	private FormData     fdlFilename, fdbFilename, fdFilename, fdlStepformat, fdcbStepformat, fdlOptions, fdOptions, fdlGeomtype, fdcbGeomtype;
	private Label		 wlOptions;
	private Text		 wOptions;

	private OGRFileOutputMeta Output;
	private boolean backupChanged;
	
	/**
	 * 
	 * @param format
	 * @return
	 */
	public boolean isOGRWritableFormat(String format) {
		//TODO GPX, BNA, GPSTrackMaker are excluded this time but should be included by adding a dropbox in the step interface to select the geometry type
		//TODO Bug with Interlis has not been identified yt
		//TODO GPSBabel, WFS, GFT, MSSQLSpatial have not been tested and require a important change in the way paths are managed from the FileObject field ... Need to be able to handle URL, connection string, etc. and not only file path.
		//String[] readOnlyFormats = {"AeronavFAA", "ArcObjects","AVCBin","AVCE00","DODS","EDIGEO","PGeo","SDE","FMEObjects Gateway","Geomedia","GRASS","HTF","MDB","MySQL","NAS","ODBC","OGDI","OpenAir","PCIDSK","PDS","REC","S57","SDTS","SOSI","SUA","SVG","UK .NTF","TIGER","VFK","VRT","XPlane","GPX","BNA","Interlis 1","Interlis 2","GPSTrackMaker","GPSBabel","WFS","GFT","MSSQLSpatial"};
		String[] readOnlyFormats = {"AeronavFAA", "ArcObjects","AVCBin","AVCE00","DODS","EDIGEO","PGeo","SDE","FMEObjects Gateway","Geomedia","GRASS","HTF","MDB","MySQL","NAS","ODBC","OGDI","OpenAir","PCIDSK","PDS","REC","S57","SDTS","SOSI","SUA","SVG","UK .NTF","TIGER","VFK","VRT","XPlane","Interlis 1","Interlis 2","GPSBabel","WFS","GFT","MSSQLSpatial","CouchDB"};
		for (int i=0;i<readOnlyFormats.length;i++) {
			if (format.equals(readOnlyFormats[i]))
				return false;
		}
		return true;
	}

	public boolean isOGRWritableFormat(Driver driver) {
		return driver.TestCapability( ogr.ODrCCreateDataSource );
	}
	
	public OGRFileOutputDialog(Shell parent, Object out, TransMeta tr, String sname)
	{
		super(parent, (BaseStepMeta)out, tr, sname);
		ogr.RegisterAll();
		
		ArrayList<String> ogrf = new ArrayList<String>();
		
		for(int i = 0; i < ogr.GetDriverCount(); i++)
        {
			if (isOGRWritableFormat(ogr.GetDriver(i).getName()))
				ogrf.add(ogr.GetDriver(i).getName());
        }
		ogrFormats = (String[]) ogrf.toArray(ogrFormats);
		Output=(OGRFileOutputMeta)out;
	}

	public String open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
 		props.setLook(shell);
        setShellImage(shell, Output);

		ModifyListener lsMod = new ModifyListener() 
		{
			public void modifyText(ModifyEvent e) 
			{
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
		fdStepname.left = new FormAttachment(middle, 0);
		fdStepname.top  = new FormAttachment(0, margin);
		fdStepname.right= new FormAttachment(100, 0);
		wStepname.setLayoutData(fdStepname);
		
		// Stepformat line
		wlStepformat = new Label(shell, SWT.RIGHT);
		wlStepformat.setText(Messages.getString("OGRFileOutputDialog.Dialog.DataFormat"));
		props.setLook(wlStepformat);
		fdlStepformat=new FormData();
		fdlStepformat.left = new FormAttachment(0, 0);
		fdlStepformat.right= new FormAttachment(middle, -margin);
		fdlStepformat.top  = new FormAttachment(wStepname, margin*2);
		wlStepformat.setLayoutData(fdlStepformat);
		wcbStepformat = new Combo(shell, 
                SWT.DROP_DOWN | SWT.MULTI | 
                SWT.V_SCROLL | SWT.H_SCROLL);
		wcbStepformat.removeAll();
	    for (int i = 0; i < ogrFormats.length; i++) {
	    	wcbStepformat.add(ogrFormats[i]);
	    }
	    wcbStepformat.select(0);
	    props.setLook(wcbStepformat);
	    fdcbStepformat = new FormData();
	    fdcbStepformat.left = new FormAttachment(middle, 0);
	    fdcbStepformat.top  = new FormAttachment(wStepname, margin);
	    fdcbStepformat.right= new FormAttachment(100, 0);
		wcbStepformat.setLayoutData(fdcbStepformat);
	    
	    
		// Filename line
		wlFilename=new Label(shell, SWT.RIGHT);
		wlFilename.setText(Messages.getString("System.Label.Filename")); //$NON-NLS-1$
 		props.setLook(wlFilename);
		fdlFilename=new FormData();
		fdlFilename.left = new FormAttachment(0, 0);
		fdlFilename.top  = new FormAttachment(wcbStepformat, margin*2);
		fdlFilename.right= new FormAttachment(middle, -margin);
		wlFilename.setLayoutData(fdlFilename);
		
		wbFilename=new Button(shell, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbFilename);
		wbFilename.setText(Messages.getString("System.Button.Browse")); //$NON-NLS-1$
		fdbFilename=new FormData();
		fdbFilename.right= new FormAttachment(100, 0);
		fdbFilename.top  = new FormAttachment(wcbStepformat, margin);
		wbFilename.setLayoutData(fdbFilename);

		wFilename=new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wFilename);
		wFilename.addModifyListener(lsMod);
		fdFilename=new FormData();
		fdFilename.left = new FormAttachment(middle, 0);
		fdFilename.right= new FormAttachment(wbFilename, -margin);
		fdFilename.top  = new FormAttachment(wcbStepformat, margin);
		wFilename.setLayoutData(fdFilename);
		
		//GDAL/OGR options line
		wlOptions=new Label(shell, SWT.RIGHT);
		wlOptions.setText(Messages.getString("OGRFileOutputDialog.Dialog.OGROptions")); //$NON-NLS-1$
 		props.setLook(wlOptions);
		fdlOptions=new FormData();
		fdlOptions.left = new FormAttachment(0, 0);
		fdlOptions.right= new FormAttachment(middle, -margin);
		fdlOptions.top  = new FormAttachment(wFilename, margin*2);
		wlOptions.setLayoutData(fdlOptions);
		wOptions=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wOptions.setText("");
 		props.setLook(wOptions);
 		wOptions.addModifyListener(lsMod);
		fdOptions=new FormData();
		fdOptions.left = new FormAttachment(middle, 0);
		fdOptions.top  = new FormAttachment(wFilename, margin);
		fdOptions.right= new FormAttachment(100, 0);
		wOptions.setLayoutData(fdOptions);
		
		// Geometry type line
		wlGeomtype = new Label(shell, SWT.RIGHT);
		wlGeomtype.setText(Messages.getString("OGRFileOutputDialog.Dialog.GeometryType"));
		props.setLook(wlGeomtype);
		fdlGeomtype=new FormData();
		fdlGeomtype.left = new FormAttachment(0, 0);
		fdlGeomtype.right= new FormAttachment(middle, -margin);
		fdlGeomtype.top  = new FormAttachment(wOptions, margin*2);
		wlGeomtype.setLayoutData(fdlGeomtype);
		wcbGeomtype = new Combo(shell, 
                SWT.DROP_DOWN | SWT.MULTI | 
                SWT.V_SCROLL | SWT.H_SCROLL);
		wcbGeomtype.removeAll();
	    for (int i = 0; i < ogrGeomTypes.length; i++) {
	    	wcbGeomtype.add(org.gdal.ogr.ogr.GeometryTypeToName(ogrGeomTypes[i]));
	    }
	    wcbGeomtype.select(0);
	    props.setLook(wcbGeomtype);
	    fdcbGeomtype = new FormData();
	    fdcbGeomtype.left = new FormAttachment(middle, 0);
	    fdcbGeomtype.top  = new FormAttachment(wOptions, margin);
	    fdcbGeomtype.right= new FormAttachment(100, 0);
		wcbGeomtype.setLayoutData(fdcbGeomtype);

		
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

		wFilename.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent arg0)
			{
				wFilename.setToolTipText(transMeta.environmentSubstitute(wFilename.getText()));
			}
		});
		
		wbFilename.addSelectionListener
		(
			new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e) 
				{
					FileDialog dialog = new FileDialog(shell, SWT.OPEN);

					if (wFilename.getText()!=null)
					{
						dialog.setFileName(wFilename.getText());
					}
						
					dialog.setFilterNames(new String[] { Messages.getString("System.FileType.AllFiles") }); //$NON-NLS-1$ //$NON-NLS-2$
					
					if (dialog.open()!=null)
					{
						String str = dialog.getFilterPath()+Const.FILE_SEPARATOR+dialog.getFileName();
						wFilename.setText(str);
					}
				}
			}
		);

		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );
		
		getData();
		Output.setChanged(changed);

		// Set the shell size, based upon previous time...
		setSize();
		
		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}
	
	protected void setFlags()
    {
    }
	
	/**
	 * Copy information from the meta-data Output to the dialog fields.
	 */ 
	public void getData()
	{
		if (Output.getGisFileName() != null) 
		{
			wFilename.setText(Output.getGisFileName());
			wFilename.setToolTipText(transMeta.environmentSubstitute(Output.getGisFileName()));
		}
		
		String outputFormat = Output.getOgrOutputFormat();
		
		if (outputFormat != null) 
		{
			for (int i=0; i < ogrFormats.length; i++) {
				if (ogrFormats[i].equalsIgnoreCase(outputFormat)) {
					wcbStepformat.select(i);
					break;
				}
			}
		}
		
		if (Output.getOgrOptions() != null) 
		{
			wOptions.setText(Output.getOgrOptions());
		}

		int geomtype = Output.getOgrGeomType();
		
		for (int i=0; i < ogrGeomTypes.length; i++) {
			if (ogrGeomTypes[i] == geomtype) {
				wcbGeomtype.select(i);
				break;
			}
		}
		
        setFlags();
		
		wStepname.selectAll();
	}
	
	private void cancel()
	{
		stepname=null;
		Output.setChanged(backupChanged);
		dispose();
	}
	
	public void getInfo(OGRFileOutputMeta meta) throws KettleStepException
	{
		// copy info to Meta class (Output)
		meta.setGisFileName( wFilename.getText() );
		meta.setOgrOutputFormat(ogrFormats[wcbStepformat.getSelectionIndex()]);
		meta.setOgrOptions( wOptions.getText() );
		meta.setOgrGeomType(ogrGeomTypes[wcbGeomtype.getSelectionIndex()]);

		if (Const.isEmpty(meta.getGisFileName()))
			throw new KettleStepException(Messages.getString("OGRFileOutputDialog.Exception.SpecifyAFileToUse")); //$NON-NLS-1$		
	}
	
	private void ok()
	{
		try
		{
			stepname = wStepname.getText(); // return value
			getInfo(Output);
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
}
