package com.github.moksh.generator.GUI;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.layout.GridLayout;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.moksh.generator.GUI.Utils.CommonUtils;
import com.github.moksh.generator.core.ImportJSON;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class JPayloadEditorGUI extends Composite {

	/**
	 * Create the composite.
	 * 
	 * @param parent
	 * @param style
	 */
	Map<String, Integer> columnNameIndexMap = null;
	Tree tree = null;
	String schema = null;
	String payload = null;

	public JPayloadEditorGUI(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(1, false));
		columnNameIndexMap = new HashMap<String, Integer>();
		columnNameIndexMap.put("Name", 0);
		columnNameIndexMap.put("Type", 1);
		columnNameIndexMap.put("Value", 2);
		Composite composite = new Composite(this, SWT.V_SCROLL);
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		tree = new Tree(composite, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
		tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		// CommonUtils.enableTreeSchemaEditor(tree, columnNameIndexMap);
		CommonUtils.createColumns(tree, columnNameIndexMap);

		Composite composite_1 = new Composite(this, SWT.NONE);
		composite_1.setLayout(new GridLayout(4, false));
		composite_1.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, false, 1, 1));

		Button btnDelete = new Button(composite_1, SWT.NONE);
		btnDelete.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final TreeItem item = tree.getSelection()[0];
				//removeElement(item,mappingArea);
				item.dispose();
			}
		});
		btnDelete.setText("Delete");

		Button btnLoadJson = new Button(composite_1, SWT.NONE);
		btnLoadJson.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					CustomDialogBox cdb = new CustomDialogBox(parent.getShell(), SWT.NONE);
					payload = cdb.open(payload);
					//System.out.println("Payload-------------------------");
					//System.out.println(payload);
					loadPayload(payload);
				} catch (Exception er) {
					new ErrorDialogBox(getShell(), getStyle()).open(er);
				}
			}
		});
		btnLoadJson.setText("Load JSON");

		Button btnSave = new Button(composite_1, SWT.NONE);
		btnSave.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					String json = CommonUtils.generateJsonPayload(tree, columnNameIndexMap);
					ObjectMapper om = new ObjectMapper();
					JsonNode node = om.readTree(json);
					CustomDialogBox cdb = new CustomDialogBox(parent.getShell(), SWT.NONE);
					payload = cdb.open(node.toPrettyString());
					// System.out.println(node.toPrettyString());
				} catch (Exception er) {
					new ErrorDialogBox(getShell(), getShell().getStyle()).open(er);
				}
			}
		});
		btnSave.setText("Show JSON");
		
		Button btnIsEditable = new Button(composite_1, SWT.NONE);
		btnIsEditable.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(payload==null || payload.trim().length()<=0)
					new CustomDialogBox(getShell(), getStyle()).open("Please load JSON payload first. Click 'Load Json' button to load JSON");
				else {
					//tree.removeAll();
					//CommonUtils.loadMokshSchema(schema, tree, columnNameIndexMap);
					CommonUtils.enableJsonEditor(tree, schema, columnNameIndexMap, payload,true);
				}
			}
		});
		btnIsEditable.setText("Edit JSON");
	}
	
	public void loadPayload(String payload)throws Exception {
		if(payload==null || payload.trim().length()<=0)
			return;
		tree.removeAll();
		
			String result[] = ImportJSON.getInstance().generateSchema(payload);
			schema = result[0];
			//payload = result[1];
			this.payload=payload;
		CommonUtils.loadMokshSchema(schema, tree, columnNameIndexMap);
		CommonUtils.enableJsonEditor(tree, schema, columnNameIndexMap, payload,false);
		
	}
	
	public String getSchema() {
		return schema;
	}

	public String getJsonPayload() throws Exception{
		String json = CommonUtils.generateJsonPayload(tree, columnNameIndexMap);
		ObjectMapper om = new ObjectMapper();
		JsonNode node = om.readTree(json);
		//System.out.println(json);
		return json;
	}

	public void loadSchema(String schema) {
		// System.out.println(schema);
		this.schema = schema;
		this.payload=null;
		CommonUtils.loadMokshSchema(schema, tree, columnNameIndexMap);
		CommonUtils.enableJsonEditor(tree, schema, columnNameIndexMap, payload,true);
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
}
