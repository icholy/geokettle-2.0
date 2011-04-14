/**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** Kettle, from version 2.2 on, is released into the public domain   **
 ** under the Lesser GNU Public License (LGPL).                       **
 **                                                                   **
 ** For more details, please read the document LICENSE.txt, included  **
 ** in this project                                                   **
 **                                                                   **
 ** http://www.kettle.be                                              **
 ** info@kettle.be                                                    **
 **                                                                   **
 **********************************************************************/

 
/*
 * Created on 2008-01-27
 * jmathieu, edube
 *
 */

package org.pentaho.di.ui.trans.steps.ogrfileoutput;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
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
import org.gdal.ogr.ogr;
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
	
	private Label        wlFilename;
	private Button       wbFilename;
	private Label		 wlStepformat;
	private Combo		 wcbStepformat;
	private TextVar      wFilename;
	private FormData     fdlFilename, fdbFilename, fdFilename, fdlStepformat, fdcbStepformat;

	private OGRFileOutputMeta Output;
	private boolean backupChanged;
	
	public boolean isOGRWritableFormat(String format) {
		String[] readOnlyFormats = {"AeronavFAA", "ArcObjects","AVCBin","AVCE00","DODS","EDIGEO","PGeo","SDE","FMEObjects Gateway","Geomedia","GRASS","HTF","MDB","MySQL","NAS","ODBC","OGDI","OpenAir","PCIDSK","PDS","REC","S57","SDTS","SOSI","SUA","SVG","UK .NTF","TIGER","VFK","VRT","XPlane"};
		for (int i=0;i<readOnlyFormats.length;i++) {
			if (format.equals(readOnlyFormats[i]))
				return false;
		}
		return true;
	}

	public OGRFileOutputDialog(Shell parent, Object out, TransMeta tr, String sname)
	{
		super(parent, (BaseStepMeta)out, tr, sname);
		ogr.RegisterAll();
		ogrFormats = new String[ogr.GetDriverCount()];
		for(int i = 0; i < ogr.GetDriverCount(); i++)
        {
			ogrFormats[i] = ogr.GetDriver(i).getName();
        }
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
		
		// Stepformat line
		wlStepformat = new Label(shell, SWT.RIGHT);
		// TODO This string should be externalised !
		wlStepformat.setText("OGR data format");
		props.setLook(wlStepformat);
		fdlStepformat=new FormData();
		fdlStepformat.left = new FormAttachment(0, 0);
		fdlStepformat.right= new FormAttachment(middle, -margin);
		fdlStepformat.top  = new FormAttachment(wStepname, margin);
		wlStepformat.setLayoutData(fdlStepformat);
		wcbStepformat = new Combo(shell, 
                SWT.DROP_DOWN | SWT.MULTI | 
                SWT.V_SCROLL | SWT.H_SCROLL);
//		wcbFormats.addSelectionListener(
//				new SelectionListener() {
//				});
		wcbStepformat.removeAll();
	    for (int i = 0; i < ogrFormats.length; i++) {
	    	if (isOGRWritableFormat(ogrFormats[i]))
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
		//fdlFilename.top  = new FormAttachment(wStepname, margin);
		fdlFilename.top  = new FormAttachment(wcbStepformat, margin);
		fdlFilename.right= new FormAttachment(middle, -margin);
		wlFilename.setLayoutData(fdlFilename);
		
		wbFilename=new Button(shell, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbFilename);
		wbFilename.setText(Messages.getString("System.Button.Browse")); //$NON-NLS-1$
		fdbFilename=new FormData();
		fdbFilename.right= new FormAttachment(100, 0);
		//fdbFilename.top  = new FormAttachment(wStepname, margin);
		fdbFilename.top  = new FormAttachment(wcbStepformat, margin);
		wbFilename.setLayoutData(fdbFilename);

		wFilename=new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wFilename);
		wFilename.addModifyListener(lsMod);
		fdFilename=new FormData();
		fdFilename.left = new FormAttachment(middle, 0);
		fdFilename.right= new FormAttachment(wbFilename, -margin);
		//fdFilename.top  = new FormAttachment(wStepname, margin);
		fdFilename.top  = new FormAttachment(wcbStepformat, margin);
		wFilename.setLayoutData(fdFilename);
		
		// Some buttons
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(Messages.getString("System.Button.OK")); //$NON-NLS-1$
        // wPreview=new Button(shell, SWT.PUSH);
        // wPreview.setText(Messages.getString("System.Button.Preview")); //$NON-NLS-1$
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(Messages.getString("System.Button.Cancel")); //$NON-NLS-1$
		
		setButtonPositions(new Button[] { wOK, wCancel }, margin, null);

		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
        // lsPreview  = new Listener() { public void handleEvent(Event e) { preview(); } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		
		wCancel.addListener(SWT.Selection, lsCancel);
        // wPreview.addListener (SWT.Selection, lsPreview);
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
					//dialog.setFilterExtensions(OGRFILE_FILTER_EXT); //$NON-NLS-1$ //$NON-NLS-2$
					if (wFilename.getText()!=null)
					{
						dialog.setFileName(wFilename.getText());
					}
						
					dialog.setFilterNames(new String[] {Messages.getString("OGRFileOutputDialog.Filter.SHPFiles"), Messages.getString("System.FileType.AllFiles")}); //$NON-NLS-1$ //$NON-NLS-2$
					
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

		if (Const.isEmpty(meta.getGisFileName()) /* && !meta.isAcceptingFilenames() */)
		{
			throw new KettleStepException(Messages.getString("OGRFileOutputDialog.Exception.SpecifyAFileToUse")); //$NON-NLS-1$
		}
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
