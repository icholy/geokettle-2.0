package org.pentaho.di.ui.trans.steps.wfsinput;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.wfs.WFSDataStoreFactory;
import org.opengis.feature.simple.SimpleFeatureType;
import org.pentaho.di.core.Const;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.wfsinput.Filter;
import org.pentaho.di.trans.steps.wfsinput.GTUtils;
import org.pentaho.di.trans.steps.wfsinput.Messages;
import org.pentaho.di.trans.steps.wfsinput.WFSInputMeta;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.ComboVar;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

public class WFSInputDialog extends BaseStepDialog implements
		StepDialogInterface {
	public static final List<String> methods = new ArrayList<String>(3) {
		private static final long serialVersionUID = 1L;
		{
			add("Get");
			add("Post");
		}
	};

	public static final String[] OPERATORS = new String[] { "",
			Messages.getString("WFSInputDialog.Operator.And.Label"),
			Messages.getString("WFSInputDialog.Operator.Or.Label") };

	private Label wlCapabilitiesUrl;
	private TextVar wCapabilitiesUrl;
	private FormData fdlCapabilitiesUrl, fdCapabilitiesUrl;

	private Label wlUsername;
	private TextVar wUsername;
	private FormData fdlUsername, fdUsername;

	private Label wlPassword;
	private TextVar wPassword;
	private FormData fdlPassword, fdPassword;

	private Label wlMethod;
	private Button[] wMethod;
	private FormData fdlMethod, fdMethod, fdMethod2;

	private Button wbGetLayernames;
	private Label wlLayername;
	private ComboVar wLayername;
	private FormData fdlLayername, fdLayername, fdGetLayernames;

	private Label wlMaxFeatures;
	private TextVar wMaxFeatures;
	private FormData fdlMaxFeatures, fdMaxFeatures;

	private Label wlSrs;
	private TextVar wSrs;
	private FormData fdlSrs, fdSrs;

	private Group wgGeneral, wgFilters, wgOptParam;
	private FormData fdgGeneral, fdgFilters, fdgOptParam;

	private TableView wFilters;
	private FormData fdFilters;

	private Label wlWKTInfo;
	private FormData fdlWKTInfo;

	private ColumnInfo[] colinfFilters;

	private WFSInputMeta input;

	private String[] layernames;

	private DataStore ds;

	private String[] attributes;

	public WFSInputDialog(Shell parent, Object in, TransMeta transMeta,
			String sname) {
		super(parent, (BaseStepMeta) in, transMeta, sname);
		input = (WFSInputMeta) in;
	}

	public String open() {
		Shell parent = getParent();
		Display display = parent.getDisplay();
		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX
				| SWT.MIN);
		props.setLook(shell);
		setShellImage(shell, input);

		ModifyListener lsMod = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				input.setChanged();
			}
		};

		ModifyListener lsModCapabilitiesUrl = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				layernames = null;
			}
		};

		Listener lsGetLayernames = new Listener() {
			public void handleEvent(Event e) {
				setLayernames();
			}
		};

		changed = input.hasChanged();

		FormLayout formLayout = new FormLayout();
		formLayout.marginWidth = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(Messages.getString("WFSInputDialog.Shell.Title")); //$NON-NLS-1$

		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname = new Label(shell, SWT.RIGHT);
		wlStepname.setText(Messages.getString("System.Label.StepName")); //$NON-NLS-1$
		props.setLook(wlStepname);
		fdlStepname = new FormData();
		fdlStepname.left = new FormAttachment(0, 0);
		fdlStepname.right = new FormAttachment(middle, -margin);
		fdlStepname.top = new FormAttachment(0, margin * 2);
		wlStepname.setLayoutData(fdlStepname);
		wStepname = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wStepname.setText(stepname);
		props.setLook(wStepname);
		wStepname.addModifyListener(lsMod);
		fdStepname = new FormData();
		fdStepname.left = new FormAttachment(middle, 0);
		fdStepname.top = new FormAttachment(0, margin);
		fdStepname.right = new FormAttachment(100, 0);
		wStepname.setLayoutData(fdStepname);

		wgGeneral = new Group(shell, SWT.SHADOW_NONE);
		props.setLook(wgGeneral);
		wgGeneral.setText(Messages
				.getString("WFSInputDialog.General.Group.Label"));
		FormLayout generalGroupLayout = new FormLayout();
		generalGroupLayout.marginWidth = 5;
		generalGroupLayout.marginHeight = 5;
		wgGeneral.setLayout(generalGroupLayout);

		wlCapabilitiesUrl = new Label(wgGeneral, SWT.RIGHT);
		wlCapabilitiesUrl.setText(Messages
				.getString("WFSInputDialog.CapabilitiesURL.Label"));
		props.setLook(wlCapabilitiesUrl);
		fdlCapabilitiesUrl = new FormData();
		fdlCapabilitiesUrl.left = new FormAttachment(0, 0);
		fdlCapabilitiesUrl.top = new FormAttachment(wStepname, 0);
		fdlCapabilitiesUrl.right = new FormAttachment(middle, -margin);
		wlCapabilitiesUrl.setLayoutData(fdlCapabilitiesUrl);

		wCapabilitiesUrl = new TextVar(transMeta, wgGeneral, SWT.SINGLE
				| SWT.LEFT | SWT.BORDER);
		props.setLook(wCapabilitiesUrl);
		wCapabilitiesUrl.addModifyListener(lsMod);
		wCapabilitiesUrl.addModifyListener(lsModCapabilitiesUrl);
		fdCapabilitiesUrl = new FormData();
		fdCapabilitiesUrl.left = new FormAttachment(middle, 0);
		fdCapabilitiesUrl.top = new FormAttachment(wStepname, -margin);
		fdCapabilitiesUrl.right = new FormAttachment(100, -margin);
		wCapabilitiesUrl.setLayoutData(fdCapabilitiesUrl);

		wlUsername = new Label(wgGeneral, SWT.RIGHT);
		wlUsername.setText(Messages.getString("WFSInputDialog.Username.Label"));
		props.setLook(wlUsername);
		fdlUsername = new FormData();
		fdlUsername.left = new FormAttachment(0, 0);
		fdlUsername.top = new FormAttachment(wCapabilitiesUrl, 3 * margin);
		fdlUsername.right = new FormAttachment(middle, -margin);
		wlUsername.setLayoutData(fdlUsername);

		wUsername = new TextVar(transMeta, wgGeneral, SWT.SINGLE | SWT.LEFT
				| SWT.BORDER);
		props.setLook(wUsername);
		wUsername.addModifyListener(lsMod);
		fdUsername = new FormData();
		fdUsername.left = new FormAttachment(middle, 0);
		fdUsername.right = new FormAttachment(100, -margin);
		fdUsername.top = new FormAttachment(wCapabilitiesUrl, 2 * margin);
		wUsername.setLayoutData(fdUsername);

		wlPassword = new Label(wgGeneral, SWT.RIGHT);
		wlPassword.setText(Messages.getString("WFSInputDialog.Password.Label"));
		props.setLook(wlPassword);
		fdlPassword = new FormData();
		fdlPassword.left = new FormAttachment(0, 0);
		fdlPassword.top = new FormAttachment(wUsername, 3 * margin);
		fdlPassword.right = new FormAttachment(middle, -margin);
		wlPassword.setLayoutData(fdlPassword);

		wPassword = new TextVar(transMeta, wgGeneral, SWT.SINGLE | SWT.LEFT | SWT.PASSWORD
				| SWT.BORDER);
		props.setLook(wPassword);
		wPassword.addModifyListener(lsMod);
		fdPassword = new FormData();
		fdPassword.left = new FormAttachment(middle, 0);
		fdPassword.right = new FormAttachment(100, -margin);
		fdPassword.top = new FormAttachment(wUsername, 2 * margin);
		wPassword.setLayoutData(fdPassword);

		wMethod = new Button[2];
		wMethod[0] = new Button(wgGeneral, SWT.RADIO);
		wMethod[0].setText(methods.get(0));
		fdMethod2 = new FormData();
		fdMethod2.left = new FormAttachment(middle, 0);
		fdMethod2.top = new FormAttachment(wPassword, 2 * margin);
		wMethod[0].setLayoutData(fdMethod2);

		wMethod[1] = new Button(wgGeneral, SWT.RADIO);
		wMethod[1].setText(methods.get(1));
		fdMethod = new FormData();
		fdMethod.left = new FormAttachment(wMethod[0], 4 * margin);
		fdMethod.top = new FormAttachment(wPassword, 2 * margin);
		wMethod[1].setLayoutData(fdMethod);

		wlMethod = new Label(wgGeneral, SWT.RIGHT);
		wlMethod.setText(Messages.getString("WFSInputDialog.Method.Label"));
		props.setLook(wlMethod);
		fdlMethod = new FormData();
		fdlMethod.left = new FormAttachment(0, 0);
		fdlMethod.top = new FormAttachment(wPassword, margin * 3);
		fdlMethod.right = new FormAttachment(middle, -2 * margin);
		wlMethod.setLayoutData(fdlMethod);

		wbGetLayernames = new Button(wgGeneral, SWT.PUSH);
		wbGetLayernames.setText(Messages
				.getString("WFSInputDialog.GetLayernames.Button")); //$NON-NLS-1$
		fdGetLayernames = new FormData();
		fdGetLayernames.right = new FormAttachment(100, -margin);
		fdGetLayernames.top = new FormAttachment(wMethod[0], 2 * margin);
		wbGetLayernames.setLayoutData(fdGetLayernames);
		wbGetLayernames.addListener(SWT.Selection, lsGetLayernames);

		wlLayername = new Label(wgGeneral, SWT.RIGHT);
		wlLayername.setText(Messages
				.getString("WFSInputDialog.Layernames.Label"));
		props.setLook(wlLayername);
		fdlLayername = new FormData();
		fdlLayername.left = new FormAttachment(0, 0);
		fdlLayername.top = new FormAttachment(wMethod[0], 3 * margin);
		fdlLayername.right = new FormAttachment(middle, -margin);
		wlLayername.setLayoutData(fdlLayername);

		wLayername = new ComboVar(transMeta, wgGeneral, SWT.BORDER
				| SWT.READ_ONLY);
		wLayername.setEditable(false);
		props.setLook(wLayername);
		wLayername.addModifyListener(lsMod);
		wLayername.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setAttributes();
			}
		});
		fdLayername = new FormData();
		fdLayername.left = new FormAttachment(middle, 0);
		fdLayername.top = new FormAttachment(wMethod[0], 2 * margin);
		fdLayername.right = new FormAttachment(wbGetLayernames, -margin);
		wLayername.setLayoutData(fdLayername);

		fdgGeneral = new FormData();
		fdgGeneral.left = new FormAttachment(0, margin);
		fdgGeneral.top = new FormAttachment(wStepname, margin);
		fdgGeneral.right = new FormAttachment(100, -margin);
		wgGeneral.setLayoutData(fdgGeneral);

		wgOptParam = new Group(shell, SWT.SHADOW_NONE);
		props.setLook(wgOptParam);
		wgOptParam.setText(Messages
				.getString("WFSInputDialog.OptParam.Group.Label"));
		FormLayout optParamGroupLayout = new FormLayout();
		optParamGroupLayout.marginWidth = 10;
		optParamGroupLayout.marginHeight = 10;
		wgOptParam.setLayout(optParamGroupLayout);

		wlSrs = new Label(wgOptParam, SWT.RIGHT);
		wlSrs.setText(Messages.getString("WFSInputDialog.Srs.Label"));
		props.setLook(wlSrs);
		fdlSrs = new FormData();
		fdlSrs.left = new FormAttachment(0, 0);
		fdlSrs.top = new FormAttachment(wgOptParam, 0);
		fdlSrs.right = new FormAttachment(middle, -margin);
		wlSrs.setLayoutData(fdlSrs);

		wSrs = new TextVar(transMeta, wgOptParam, SWT.SINGLE | SWT.LEFT
				| SWT.BORDER);
		props.setLook(wSrs);
		wSrs.addModifyListener(lsMod);
		fdSrs = new FormData();
		fdSrs.left = new FormAttachment(middle, 0);
		fdSrs.top = new FormAttachment(wgOptParam, -margin);
		fdSrs.right = new FormAttachment(100, -margin);
		wSrs.setLayoutData(fdSrs);

		wlMaxFeatures = new Label(wgOptParam, SWT.RIGHT);
		wlMaxFeatures.setText(Messages
				.getString("WFSInputDialog.MaxFeatures.Label"));
		props.setLook(wlMaxFeatures);
		fdlMaxFeatures = new FormData();
		fdlMaxFeatures.left = new FormAttachment(0, 0);
		fdlMaxFeatures.top = new FormAttachment(wSrs, 3 * margin);
		fdlMaxFeatures.right = new FormAttachment(middle, -margin);
		wlMaxFeatures.setLayoutData(fdlMaxFeatures);

		wMaxFeatures = new TextVar(transMeta, wgOptParam, SWT.SINGLE | SWT.LEFT
				| SWT.BORDER);
		props.setLook(wMaxFeatures);
		wMaxFeatures.addModifyListener(lsMod);
		fdMaxFeatures = new FormData();
		fdMaxFeatures.left = new FormAttachment(middle, 0);
		fdMaxFeatures.right = new FormAttachment(100, -margin);
		fdMaxFeatures.top = new FormAttachment(wSrs, 2 * margin);
		wMaxFeatures.setLayoutData(fdMaxFeatures);

		wgFilters = new Group(wgOptParam, SWT.SHADOW_NONE);
		props.setLook(wgFilters);
		wgFilters.setText(Messages
				.getString("WFSInputDialog.Filters.Group.Label"));
		FormLayout filtersGroupLayout = new FormLayout();
		filtersGroupLayout.marginWidth = 10;
		filtersGroupLayout.marginHeight = 10;
		wgFilters.setLayout(filtersGroupLayout);

		colinfFilters = new ColumnInfo[4];

		colinfFilters[0] = new ColumnInfo(
				Messages.getString("WFSInputDialog.Operator.Label"),
				ColumnInfo.COLUMN_TYPE_CCOMBO, null, false);
		colinfFilters[0].setComboValues(OPERATORS);

		colinfFilters[1] = new ColumnInfo(
				Messages.getString("WFSInputDialog.Attribute.Label"),
				ColumnInfo.COLUMN_TYPE_CCOMBO, null, false);

		colinfFilters[2] = new ColumnInfo(
				Messages.getString("WFSInputDialog.Condition.Label"),
				ColumnInfo.COLUMN_TYPE_CCOMBO, null, false);
		colinfFilters[2].setComboValues(GTUtils.FUNCTIONS);

		colinfFilters[3] = new ColumnInfo(
				Messages.getString("WFSInputDialog.Value.Label"),
				ColumnInfo.COLUMN_TYPE_TEXT, null, false);

		wFilters = new TableView(transMeta, wgFilters, SWT.BORDER
				| SWT.V_SCROLL, colinfFilters, 4, lsMod, props);

		wFilters.setSortable(false);

		fdFilters = new FormData();
		fdFilters.left = new FormAttachment(0, 0);
		fdFilters.right = new FormAttachment(100, -margin);
		fdFilters.top = new FormAttachment(wgFilters, 2 * margin);
		wFilters.setLayoutData(fdFilters);

		wlWKTInfo = new Label(wgFilters, SWT.RIGHT);
		wlWKTInfo.setText(Messages.getString("WFSInputDialog.WKTInfo.Label"));
		props.setLook(wlWKTInfo);
		fdlWKTInfo = new FormData();
		fdlWKTInfo.left = new FormAttachment(0, margin);
		fdlWKTInfo.top = new FormAttachment(wFilters, margin);
		wlWKTInfo.setLayoutData(fdlWKTInfo);

		fdgFilters = new FormData();
		fdgFilters.left = new FormAttachment(0, margin);
		fdgFilters.top = new FormAttachment(wMaxFeatures, margin);
		fdgFilters.right = new FormAttachment(100, -margin);
		wgFilters.setLayoutData(fdgFilters);

		fdgOptParam = new FormData();
		fdgOptParam.left = new FormAttachment(0, margin);
		fdgOptParam.top = new FormAttachment(wgGeneral, margin);
		fdgOptParam.right = new FormAttachment(100, -margin);
		wgOptParam.setLayoutData(fdgOptParam);

		// THE BUTTONS
		wOK = new Button(shell, SWT.PUSH);
		wOK.setText(Messages.getString("System.Button.OK"));

		wCancel = new Button(shell, SWT.PUSH);
		wCancel.setText(Messages.getString("System.Button.Cancel"));

		setButtonPositions(new Button[] { wOK, wCancel }, margin, wgOptParam);

		// Add listeners
		lsOK = new Listener() {
			public void handleEvent(Event e) {
				ok();
			}
		};

		lsCancel = new Listener() {
			public void handleEvent(Event e) {
				cancel();
			}
		};

		wOK.addListener(SWT.Selection, lsOK);
		wCancel.addListener(SWT.Selection, lsCancel);

		lsDef = new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent e) {
				ok();
			}
		};

		wStepname.addSelectionListener(lsDef);
		wCapabilitiesUrl.addSelectionListener(lsDef);

		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(new ShellAdapter() {
			public void shellClosed(ShellEvent e) {
				cancel();
			}
		});

		// Set the shell size, based upon previous time...
		setSize();
		getData();
		colinfFilters[1].setComboValues(attributes);

		input.setChanged(changed);

		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		return stepname;
	}

	public void getData() {
		if (!Const.isEmpty(input.getCapabilitiesUrl()))
			wCapabilitiesUrl.setText(input.getCapabilitiesUrl());
		if (!Const.isEmpty(input.getUsername()))
			wUsername.setText(input.getUsername());
		if (!Const.isEmpty(input.getPassword()))
			wPassword.setText(input.getPassword());
		wMethod[input.getMethod() ? 1 : 0].setSelection(true);
		if (!Const.isEmpty(input.getLayername()))
			wLayername.setText(input.getLayername());
		if (!Const.isEmpty(input.getMaxFeatures()))
			wMaxFeatures.setText(input.getMaxFeatures());
		if (!Const.isEmpty(input.getSrs()))
			wSrs.setText(input.getSrs());
		attributes = input.getAttributes();
		if(Const.isEmpty(input.getAttributes()) || !isArrayValid(attributes))
				attributes = new String[] { "" };
		buildFiltersFromInput();
		wStepname.selectAll();
	}

	private void cancel() {
		stepname = null;
		input.setChanged(changed);
		dispose();
	}

	public boolean isValid(String str, String parameter) {
		boolean isValid = true;
		if (Const.isEmpty(str)) {
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);
			mb.setMessage(Messages
					.getString("WFSInputDialog.ErrorRequiredInformations.DialogMessage1") + parameter + Messages.getString("WFSInputDialog.ErrorRequiredInformations.DialogMessage2")); //$NON-NLS-1$
			mb.setText(Messages
					.getString("WFSInputDialog.ErrorRequiredInformations.DialogTitle")); //$NON-NLS-1$
			mb.open();
			isValid = false;
		}
		return isValid;
	}

	private void getLayernames() {
		if (isValid(wCapabilitiesUrl.getText(),
				Messages.getString("WFSInputDialog.CapabilitiesURL.Label"))) {
			DataStore ds = buildDataStore();
			try {
				if (ds != null)
					layernames = ds.getTypeNames();
			} catch (IOException e) {
				MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
				mb.setMessage(Messages
						.getString("WFSInputDialog.ErrorGettingLayernames.DialogMessage"));
				mb.setText(Messages
						.getString("WFSInputDialog.Error.DialogTitle"));
				mb.open();
			}
		}
	}

	public void setLayernames() {
		wLayername.removeAll();
		if (Const.isEmpty(layernames))
			getLayernames();
		if (layernames != null) {
			for (String layername : layernames) {
				wLayername.add(layername);
			}
			String[] items = wLayername.getItems();
			if (!Const.isEmpty(items)) {
				Arrays.sort(items);
				wLayername.setItems(items);
				wLayername.layout();
			}
		}
	}

	private void getAttributes() {
		String layername = wLayername.getText();
		if (isValid(layername,
				Messages.getString("WFSInputDialog.Layernames.Label"))) {
			if (isValid(wCapabilitiesUrl.getText(),
					Messages.getString("WFSInputDialog.CapabilitiesURL.Label")))
				ds = buildDataStore();
			try {			
				SimpleFeatureType sft = ds.getSchema(layername);
				int size = sft.getAttributeCount();
				attributes = new String[size];
				for (int i = 0; i < size; i++) {
					attributes[i] = sft.getDescriptor(i).getLocalName();
				}
			} catch (Exception e) {
				attributes = new String[] { "" };
			}
		}
	}
	
	
	
	private void setAttributes() {
		getAttributes();
		colinfFilters[1].setComboValues(attributes);
	}

	private void buildFiltersFromInput() {
		List<Filter> filters = input.getFilters();
		if (filters != null && filters.size() > 0) {
			wFilters.setRedraw(false);
			wFilters.removeAll();
			Table table = wFilters.getTable();
			table.setItemCount(filters.size());
			int i = 0;
			for (Filter f : filters) {
				TableItem item = table.getItem(i);
				String operator = f.getOperator();
				String value = f.getValue();
				item.setText(0, Integer.toString(i));
				if (!Const.isEmpty(operator))
					item.setText(1, operator);
				item.setText(2, f.getAttribute());
				item.setText(3, f.getCondition());
				if (!Const.isEmpty(value))
					item.setText(4, value);
				i++;
			}
			wFilters.setRedraw(true);
		}
	}

	private DataStore buildDataStore() {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(WFSDataStoreFactory.URL.key, wCapabilitiesUrl.getText());
		parameters.put(WFSDataStoreFactory.PROTOCOL.key,
				wMethod[1].getSelection() ? Boolean.TRUE : Boolean.FALSE);
		parameters.put(WFSDataStoreFactory.TIMEOUT.key, GTUtils.TIMEOUT);
		if (!Const.isEmpty(wUsername.getText()))
			parameters.put(WFSDataStoreFactory.USERNAME.key,
					wUsername.getText());
		if (!Const.isEmpty(wPassword.getText()))
			parameters.put(WFSDataStoreFactory.PASSWORD.key,
					wPassword.getText());
		try {
			ds = DataStoreFinder.getDataStore(parameters);
		} catch (IOException e) {
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
			mb.setMessage(Messages
					.getString("WFSInputDialog.ErrorBuildingDataStore.DialogMessage"));
			mb.setText(Messages.getString("WFSInputDialog.Error.DialogTitle"));
			mb.open();
		}
		return ds;
	}

	private boolean setInputFilters() {
		List<Filter> filters = new ArrayList<Filter>(wFilters.getItemCount());
		TableItem[] items = wFilters.getTable().getItems();
		for (int i = 0; i < items.length; i++) {
			TableItem item = items[i];
			String attribute = item.getText(2);
			String condition = item.getText(3);
			String operator = item.getText(1);
			if (!Const.isEmpty(attribute) && !Const.isEmpty(condition)) {
				if (i > 0 && Const.isEmpty(operator))
					return false;
				else if (i == 0 && !Const.isEmpty(operator))
					return false;
				else
					filters.add(new Filter(attribute, condition, item
							.getText(4), operator));
			}
		}
		input.setFilters(filters);
		return true;
	}

	private void ok() {
		if (Const.isEmpty(wStepname.getText()))
			return;
		isValid(wCapabilitiesUrl.getText(),
				Messages.getString("WFSInputDialog.CapabilitiesURL.Label"));
		input.setCapabilitiesUrl(wCapabilitiesUrl.getText());
		input.setUsername(wUsername.getText());
		input.setPassword(wPassword.getText());
		input.setMethod(wMethod[0].getSelection() ? Boolean.FALSE
				: Boolean.TRUE);
		isValid(wLayername.getText(),
				Messages.getString("WFSInputDialog.Layernames.Label"));
		input.setLayername(wLayername.getText());
		if (!Const.isEmpty(wMaxFeatures.getText())) {
			try {
				if (Integer.parseInt(wMaxFeatures.getText()) <= 0)
					throw new Exception();
			} catch (Exception e) {
				MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);
				mb.setMessage(Messages
						.getString("WFSInputDialog.ErrorValueNotPositiveInteger.DialogMessage1")
						+ Messages
								.getString("WFSInputDialog.MaxFeatures.Label")
						+ Messages
								.getString("WFSInputDialog.ErrorValueNotPositiveInteger.DialogMessage2"));
				mb.setText(Messages
						.getString("WFSInputDialog.ErrorValueNotPositiveInteger.DialogTitle"));
				mb.open();
			}
		}
		input.setMaxFeatures(wMaxFeatures.getText());

		String srs = wSrs.getText();
		if (GTUtils.decode(srs))
			input.setSrs(srs);
		else {
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);
			mb.setMessage("\""
					+ srs
					+ Messages
							.getString("WFSInputDialog.ErrorSRSNotValid.DialogMessage"));
			mb.setText(Messages
					.getString("WFSInputDialog.ErrorSRSNotValid.DialogTitle"));
			mb.open();
		}

		input.setAttributes(!Const.isEmpty(attributes)? attributes : new String[] { "" });
		if (!setInputFilters()) {
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);
			mb.setMessage(Messages
					.getString("WFSInputDialog.ErrorFilterNotValid.DialogMessage"));
			mb.setText(Messages
					.getString("WFSInputDialog.ErrorFilterNotValid.DialogTitle"));
			mb.open();
		}
		stepname = wStepname.getText();
		dispose();
	}
	
	private boolean isArrayValid(Object[] array){
		boolean valid = false;
		for(int i = 0 ; i < array.length ; i++){
			if(array[i] != null){
				valid = true;
				break;
			}
		}
		return valid;
	}
}
