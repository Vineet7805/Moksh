package com.github.moksh.generator.GUI;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Combo;

public class MapStepGUI extends Composite {
	public Text textCondition;
	public Text textFunction;
	public Text textComment;
	public boolean delete=false;
	private Text txtValue;
	public Text txtOrder;
	public Text txtNested;
	public Text txtName;

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public MapStepGUI(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(1, false));
		
		Composite composite = new Composite(this, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		
		Composite composite_5 = new Composite(composite, SWT.NONE);
		composite_5.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		composite_5.setLayout(new GridLayout(6, false));
		
		Label lblUniqueName = new Label(composite_5, SWT.NONE);
		lblUniqueName.setToolTipText("Not a required field. ");
		lblUniqueName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblUniqueName.setText("Unique Name:");
		
		txtName = new Text(composite_5, SWT.BORDER);
		txtName.setToolTipText("Not a required field. ");
		txtName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblNested = new Label(composite_5, SWT.NONE);
		lblNested.setToolTipText("Not a required field. Use this field to make this operation nested to any other operation. Value must be a unique name that is already assigned to another operation.");
		lblNested.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNested.setText("Nested:");
		
		txtNested = new Text(composite_5, SWT.BORDER);
		txtNested.setToolTipText("Not a required field. Use this field to make this operation nested to any other operation. Value must be a unique name that is already assigned to another operation.");
		txtNested.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblOrder = new Label(composite_5, SWT.NONE);
		lblOrder.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblOrder.setText("Order:");
		
		txtOrder = new Text(composite_5, SWT.BORDER);
		txtOrder.setToolTipText("Not a required field. Operations will be executed in assending order if specified.");
		txtOrder.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblWriteCondition = new Label(composite, SWT.NONE);
		lblWriteCondition.setToolTipText("*Use javascript syntex. Condition must return true to execute copy operation.");
		lblWriteCondition.setText("Copy condition");
		
		textCondition = new Text(composite, SWT.BORDER);
		textCondition.setToolTipText("*Use javascript syntex. Condition must return true to execute copy operation.");
		textCondition.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		
		Composite composite_1 = new Composite(this, SWT.NONE);
		composite_1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1));
		composite_1.setLayout(new GridLayout(1, false));
		
		Label lblFunctionvalue = new Label(composite_1, SWT.NONE);
		lblFunctionvalue.setText("function enrich(json){");
		
		textFunction = new Text(composite_1, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
		GridData gd_textFunction = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_textFunction.heightHint = 108;
		textFunction.setLayoutData(gd_textFunction);
		
		Composite composite_4 = new Composite(composite_1, SWT.NONE);
		composite_4.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		composite_4.setLayout(new GridLayout(10, false));
		
		Label lblReturnValue = new Label(composite_4, SWT.NONE);
		lblReturnValue.setText("return json;\r\n}");
		new Label(composite_4, SWT.NONE);
		new Label(composite_4, SWT.NONE);
		new Label(composite_4, SWT.NONE);
		new Label(composite_4, SWT.NONE);
		new Label(composite_4, SWT.NONE);
		new Label(composite_4, SWT.NONE);
		new Label(composite_4, SWT.NONE);
		new Label(composite_4, SWT.NONE);
		
		Combo comboInbuilt = new Combo(composite_4, SWT.READ_ONLY);
		comboInbuilt.setItems(new String[] {"choose-inbuilt", "trim", "toUpperCase", "toLowerCase", "noSpecialChars", "validateEmail"});
		comboInbuilt.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, true, 1, 1));
		comboInbuilt.setText("choose-inbuilt");
		String trim="value=json.object.trim();\r\n" + 
				"json.object=value;";
		String toUpper="value=json.object.toUpperCase();\r\n" + 
				"json.value=value;";
		String toLower="value=json.object.toLowerCase();\r\n" + 
				"json.value=value;";
		String noSpecial="value=json.object.replace(/[^a-zA-Z]/g, \"\");\r\n" + 
				"json.value=value;";
		String validateEmail="email=json.object;\r\n" + 
				"isEmail= /^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$/.test(email);\r\n" + 
				"if(!isEmail)\r\n" + 
				"	throw new Error(\"Invalid email id\");";
		comboInbuilt.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
				System.out.println(comboInbuilt.getText());
				if(comboInbuilt.getText().equals("trim")) {
					textFunction.setText(trim);
				}else if(comboInbuilt.getText().equals("toUpperCase")) {
					textFunction.setText(toUpper);
				}else if(comboInbuilt.getText().equals("toLowerCase")) {
					textFunction.setText(toLower);
				}else if(comboInbuilt.getText().equals("noSpecialChars")) {
					textFunction.setText(noSpecial);
				}else if(comboInbuilt.getText().equals("validateEmail")) {
					textFunction.setText(validateEmail);
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		
		Composite composite_2 = new Composite(this, SWT.NONE);
		composite_2.setLayout(new GridLayout(1, false));
		composite_2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Label lblComment = new Label(composite_2, SWT.NONE);
		lblComment.setText("Comment:");
		
		textComment = new Text(composite_2, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
		GridData gd_textComment = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_textComment.heightHint = 98;
		textComment.setLayoutData(gd_textComment);
		
		Composite composite_3 = new Composite(this, SWT.NONE);
		composite_3.setLayout(new GridLayout(4, false));
		composite_3.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Button btnNewButton = new Button(composite_3, SWT.NONE);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
				ScriptEngineManager factory = new ScriptEngineManager();
		        ScriptEngine engine = factory.getEngineByName("JavaScript");
		        engine.eval("function enrich(json){\r\n"+textFunction.getText()+"\r\n return json;}");
		        //String jsonCompatible="Java.asJSONCompatible(" + txtValue.getText() + ")";
		        //System.out.println(jsonCompatible);
		        //engine.eval("json="+txtValue.getText()+";");
		        String json=txtValue.getText();
		        if(!json.contains(":"))
					json="\""+json+"\"";
		        String value=(String)engine.eval("JSON.stringify(enrich({\"object\":"+json+"}));");
		        new PromptDialogBox(getShell(), getStyle()).open(value);
				} catch (Exception er) {
					new ErrorDialogBox(getShell(), getStyle()).open(er);
				}
			}
		});
		btnNewButton.setText("Test enrich");
		
		txtValue = new Text(composite_3, SWT.BORDER);
		txtValue.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(composite_3, SWT.NONE);
		
		Button btnDeleteThisMap = new Button(composite_3, SWT.CHECK);
		btnDeleteThisMap.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String dlt=btnDeleteThisMap.getSelection()+"";
				System.out.println(dlt);
				if(btnDeleteThisMap.getSelection())
					delete=true;
			}
		});
		btnDeleteThisMap.setText("Delete this map");
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
}
