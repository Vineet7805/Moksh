package com.github.moksh.generator.GUI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.moksh.generator.GUI.Utils.CommonUtils;
import com.github.moksh.generator.core.ClassMetaData;
import com.github.moksh.generator.core.ClassMetaData.API;
import com.github.moksh.generator.core.ClassMetaData.Function;
import com.github.moksh.generator.core.ClassMetaData.Property;
import com.github.moksh.generator.core.CodeGen;
import com.github.moksh.generator.core.ImportJSON;
import com.github.moksh.generator.core.JsonPatchMap;

public class FunctionEditorGUI extends Composite {

	/**
	 * Create the composite.
	 * 
	 * @param parent
	 * @param style
	 */
	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	/**
	 * Create the panel.
	 */
	private Text leftJsonSchema;
	private Text leftJsonPayload;
	private Text rightJsonSchema;
	private Text rightJsonPayload;
	private ImportJSON importJsonRight = null;
	private ImportJSON importJsonLeft = null;
	private Tree treeLeft = null;
	private Tree treeRight = null;
	private Tree parameterTree = null;
	private TreeItem itemLeftSelected = null;
	private TreeItem itemRightSelected = null;
	private List<JsonPatchMap> jsonPatchMaps = null;
	private GC gc = null;
	// int columnNameIndexMap.get("Name")=0,columnNameIndexMap.get("Type")=1;
	Map<String, Integer> columnNameIndexMap = null;
	private Text txtWriteYourPrivate;
	private Text txtImportPackages;
	private Text txtImplementYourMain;
	private Text packageName;
	private Combo methodCombo = null;
	public FunctionEditorGUI(Composite parent, int style) {
		super(parent, style);
		importJsonRight = ImportJSON.getInstance();
		importJsonLeft = ImportJSON.getInstance();
		jsonPatchMaps = new ArrayList<JsonPatchMap>();
		columnNameIndexMap = new HashMap<String, Integer>();
		columnNameIndexMap.put("Name", 0);
		columnNameIndexMap.put("Type", 1);
		setLayout(new FillLayout(SWT.HORIZONTAL));
		Composite composite_4 = new Composite(this, SWT.NONE);
		this.setSize(parent.getSize());
		// composite_4.setLayout(new GridLayout(6, false));
		Shell shell = parent.getShell();
		shell.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				Rectangle clientArea = shell.getClientArea();
				e.gc.drawLine(0, 0, clientArea.width, clientArea.height);
			}
		});
		composite_4.setLayout(new GridLayout(1, false));

		Composite composite_8 = new Composite(composite_4, SWT.NONE);
		GridData gd_composite_8 = new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1);
		gd_composite_8.exclude = true;
		gd_composite_8.widthHint = 1;
		composite_8.setLayoutData(gd_composite_8);
		composite_8.setLayout(new GridLayout(1, true));

		// SashForm sashForm_9 = new SashForm(composite_8, SWT.NONE);
		// Image img=SWTResourceManager.getImage(JSchemaGeneratorGUI.class,
		// "/com/github/moksh/images/reload.png");
		// btnReload.setImage(img);
		// sashForm_9.setWeights(new int[] { });

		leftJsonSchema = new Text(composite_8, SWT.BORDER | SWT.MULTI);
		// GridData gd_leftJsonSchema = new GridData(SWT.FILL, SWT.FILL, true, true, 1,
		// 1);
		// gd_leftJsonSchema.heightHint = 172;
		leftJsonSchema.setVisible(false);// .setLayoutData(gd_leftJsonSchema);

		// SashForm sashForm_10 = new SashForm(composite_8, SWT.NONE);
		// sashForm_10.setWeights(new int[] { });

		leftJsonPayload = new Text(composite_8, SWT.BORDER | SWT.MULTI);
		leftJsonPayload.setVisible(false);
		// GridData gd_leftJsonPayload = new GridData(SWT.FILL, SWT.FILL, true, true, 1,
		// 1);
		// gd_leftJsonPayload.heightHint = 232;
		// leftJsonPayload.setLayoutData(gd_leftJsonPayload);

		Composite composite_5 = new Composite(composite_4, SWT.NONE);
		composite_5.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		composite_5.setLayout(new GridLayout(1, false));

		ScrolledComposite scrolledComposite = new ScrolledComposite(composite_5,
				SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		GridData gd_scrolledComposite = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_scrolledComposite.heightHint = 125;
		gd_scrolledComposite.widthHint = 269;
		scrolledComposite.setLayoutData(gd_scrolledComposite);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		Composite composite_6 = new Composite(scrolledComposite, SWT.NONE);
		composite_6.setLayout(new GridLayout(3, false));
		Font treeFont = new Font(parent.getDisplay(), new FontData("Segoe UI", 10, SWT.NORMAL));
		treeLeft = new Tree(composite_6, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		treeLeft.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true, 1, 1));

		// tree = new Tree(editorComposite, SWT.MULTI | SWT.FULL_SELECTION);
		// treeLeft.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		treeLeft.setHeaderVisible(true);
		treeLeft.setLinesVisible(true);
		treeLeft.setFont(treeFont);
		enableEditor(treeLeft);
		// treeLeft.setSize(250, height);

		TreeColumn trclmnLeftName = new TreeColumn(treeLeft, SWT.LEFT_TO_RIGHT);
		trclmnLeftName.setWidth(165);
		trclmnLeftName.setText("Name");

		TreeColumn trclmnLeftType = new TreeColumn(treeLeft, SWT.LEFT_TO_RIGHT);
		trclmnLeftType.setWidth(100);
		trclmnLeftType.setText("Type");

		Composite mappingArea = new Composite(composite_6, SWT.NONE);
		mappingArea.setLayout(new GridLayout(1, false));
		mappingArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		gc = new GC(mappingArea);
		treeRight = new Tree(composite_6, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		treeRight.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1));
		// treeRight.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		treeRight.setOrientation(SWT.LEFT_TO_RIGHT);
		treeRight.setHeaderVisible(true);
		treeRight.setLinesVisible(true);
		treeRight.setFont(treeFont);
		enableEditor(treeRight);
		TreeColumn trclmnRightName = new TreeColumn(treeRight, SWT.LEFT_TO_RIGHT);
		trclmnRightName.setWidth(165);
		trclmnRightName.setText("Name");

		TreeColumn trclmnRightType = new TreeColumn(treeRight, SWT.LEFT_TO_RIGHT);
		trclmnRightType.setWidth(100);
		trclmnRightType.setText("Type");

		scrolledComposite.setContent(composite_6);
		scrolledComposite.setMinSize(composite_6.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		ScrolledComposite scrolledComposite_1 = new ScrolledComposite(composite_5,
				SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		GridData gd_scrolledComposite_1 = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gd_scrolledComposite_1.widthHint = 214;
		gd_scrolledComposite_1.heightHint = 114;
		scrolledComposite_1.setLayoutData(gd_scrolledComposite_1);
		scrolledComposite_1.setExpandHorizontal(true);
		scrolledComposite_1.setExpandVertical(true);

		Composite composite_1 = new Composite(scrolledComposite_1, SWT.NONE);
		composite_1.setLayout(new GridLayout(3, false));

		Composite composite_2 = new Composite(composite_1, SWT.NONE);
		composite_2.setLayout(new GridLayout(1, false));
		GridData gd_composite_2 = new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1);
		gd_composite_2.heightHint = 109;
		gd_composite_2.widthHint = 286;
		composite_2.setLayoutData(gd_composite_2);

		Label lblNewLabel_3 = new Label(composite_2, SWT.NONE);
		lblNewLabel_3.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		lblNewLabel_3.setText("Json functions:");

		Composite composite_7 = new Composite(composite_2, SWT.NONE);
		composite_7.setLayout(new GridLayout(3, false));
		GridData gd_composite_7 = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_composite_7.heightHint = 34;
		composite_7.setLayoutData(gd_composite_7);

		Button generateSchemaLeft = new Button(composite_7, SWT.CENTER);
		generateSchemaLeft.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		generateSchemaLeft.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					CustomDialogBox cdb = new CustomDialogBox(parent.getShell(), SWT.NONE);
					cdb.open(leftJsonPayload);
					String retStr[] = importJsonLeft.generateSchema(leftJsonPayload.getText());
					leftJsonSchema.setText(retStr[0]);
					leftJsonPayload.setText(retStr[1]);
					importJsonLeft.generatePayload(leftJsonSchema.getText());
					Map<String, Object> map = importJsonLeft.schemaMap;
					treeLeft.removeAll();
					loadSchemaEditor(map, treeLeft);
				} catch (Exception e1) {
					// e1.printStackTrace();
					if (leftJsonPayload.getText() == null || leftJsonPayload.getText().length() == 0) {
						new InfoDialogBox(parent.getShell(), style).open(
								"To generate JSON Schema you need to load JSON Payload first.\n1. Use \"Browse JSON Payload\" button to select JSON Payload file.\nOr\n2. Paste JSON Payload text into \"JSON Payload\" text box.");
					} else
						new ErrorDialogBox(parent.getShell(), style).open(e1);
				}
			}
		});
		generateSchemaLeft.setText("Load payload");
		generateSchemaLeft.setOrientation(SWT.RIGHT_TO_LEFT);

		Button generatePayloadLeft = new Button(composite_7, SWT.CENTER);
		generatePayloadLeft.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		generatePayloadLeft.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					CustomDialogBox cdb = new CustomDialogBox(parent.getShell(), SWT.NONE);
					cdb.open(leftJsonSchema);
					String[] text = importJsonLeft.generatePayload(leftJsonSchema.getText());
					leftJsonSchema.setText(text[0]);
					leftJsonPayload.setText(text[1]);
					Map<String, Object> map = importJsonLeft.schemaMap;
					// tree.clearAll(false);
					treeLeft.removeAll();
					loadSchemaEditor(map, treeLeft);
				} catch (Exception e1) {
					if (leftJsonSchema.getText() == null || leftJsonSchema.getText().length() == 0) {
						new InfoDialogBox(parent.getShell(), getStyle()).open(
								"To generate JSON Payload you need to load JSON Schema first.\n1. Use \"Browse JSON Schema\" button to select JSON Schema file.\nOr\n2. Paste JSON Schema text into \"JSON Schema\" text box.");
					} else
						new ErrorDialogBox(parent.getShell(), getStyle()).open(e1);
				}
			}
		});
		generatePayloadLeft.setOrientation(SWT.RIGHT_TO_LEFT);
		generatePayloadLeft.setText("Load Schema");

		Button btnLoadDocumentLeft = new Button(composite_7, SWT.NONE);
		btnLoadDocumentLeft.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		btnLoadDocumentLeft.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Map<String, Object> map = importJsonLeft.schemaMap;
				treeLeft.removeAll();
				try {
					loadSchemaEditor(map, treeLeft);
				} catch (Exception e1) {
					if (leftJsonPayload.getText() == null || leftJsonPayload.getText().length() == 0) {
						new InfoDialogBox(parent.getShell(), style).open(
								"To generate JSON Schema you need to load JSON Payload first.\n1. Use \"Browse JSON Payload\" button to select JSON Payload file.\nOr\n2. Paste JSON Payload text into \"JSON Payload\" text box.");
					} else
						new ErrorDialogBox(parent.getShell(), style).open(e1);
				}
			}
		});
		btnLoadDocumentLeft.setText("Refresh");

		Label lblNewLabel_1 = new Label(composite_2, SWT.NONE);
		lblNewLabel_1.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		lblNewLabel_1.setText("Editor functions:");

		Composite composite_10 = new Composite(composite_2, SWT.NONE);
		composite_10.setLayout(new GridLayout(2, false));
		GridData gd_composite_10 = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_composite_10.heightHint = 34;
		composite_10.setLayoutData(gd_composite_10);

		Button btnAddItemLeft = new Button(composite_10, SWT.NONE);
		btnAddItemLeft.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		btnAddItemLeft.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					if (treeLeft.getSelection().length <= 0) {
						TreeItem rootItem = generateAPIRequestPayloadDefaults(treeLeft);
						TreeItem item = new TreeItem(rootItem, SWT.FULL_SELECTION);
						item.setText(columnNameIndexMap.get("Name"), "newElement");
						item.setText(columnNameIndexMap.get("Type"), "string");
					} else {
						final TreeItem selItem = treeLeft.getSelection()[0];
						String type = selItem.getText(columnNameIndexMap.get("Type"));
						if (type.equalsIgnoreCase("array<object>") || type.equalsIgnoreCase("array<array>")
								|| type.equalsIgnoreCase("object")) {
							TreeItem item = new TreeItem(selItem, SWT.FULL_SELECTION);
							item.setText(columnNameIndexMap.get("Name"), "newElement");
							item.setText(columnNameIndexMap.get("Type"), "string");
						} else {
							TreeItem item = null;
							if (selItem.getParentItem() != null)
								item = new TreeItem(selItem.getParentItem(), SWT.FULL_SELECTION);
							else
								item = new TreeItem(treeLeft, SWT.FULL_SELECTION);
							item.setText(columnNameIndexMap.get("Name"), "newElement");
							item.setText(columnNameIndexMap.get("Type"), "string");
						}
					}
				} catch (Exception e2) {
					new ErrorDialogBox(shell, style).open(e2);
				}

			}
		});
		btnAddItemLeft.setText("Add element");

		Button btnRemoveItemLeft = new Button(composite_10, SWT.NONE);
		btnRemoveItemLeft.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		btnRemoveItemLeft.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					if (treeLeft.getSelection().length == 0)
						new InfoDialogBox(shell, style).open(
								"Please select the row you want to remove. Selected row will be highlighted with red color.\n\nNote:-\nYou can't undo remove action.");
					else {
						final TreeItem item = treeLeft.getSelection()[0];
						removeElement(item, mappingArea);
						item.dispose();
					}
				} catch (Exception e2) {
					new ErrorDialogBox(shell, style).open(e2);
				}
			}
		});
		btnRemoveItemLeft.setText("Delete element");

		txtWriteYourPrivate = new Text(composite_1, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL | SWT.MULTI);
		txtWriteYourPrivate.setText(
				"//Use this space as your service private workspace. You can write your private functions and variables here. ");
		txtWriteYourPrivate.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		Composite composite_3 = new Composite(composite_1, SWT.NONE);
		composite_3.setLayout(new GridLayout(1, false));
		GridData gd_composite_3 = new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1);
		gd_composite_3.heightHint = 113;
		gd_composite_3.widthHint = 285;
		composite_3.setLayoutData(gd_composite_3);

		Label lblNewLabel_4 = new Label(composite_3, SWT.NONE);
		lblNewLabel_4.setText("JSON functions:");

		Composite composite_11 = new Composite(composite_3, SWT.NONE);
		composite_11.setLayout(new GridLayout(3, false));
		GridData gd_composite_11 = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_composite_11.heightHint = 34;
		composite_11.setLayoutData(gd_composite_11);

		Button generateSchemaRight = new Button(composite_11, SWT.CENTER);
		generateSchemaRight.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		generateSchemaRight.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					CustomDialogBox cdb = new CustomDialogBox(shell, SWT.NONE);
					cdb.open(rightJsonPayload);
					String retStr[] = importJsonRight.generateSchema(rightJsonPayload.getText());
					rightJsonSchema.setText(retStr[0]);
					rightJsonPayload.setText(retStr[1]);
					importJsonRight.generatePayload(rightJsonSchema.getText());
					Map<String, Object> map = importJsonRight.schemaMap;
					treeRight.removeAll();
					loadSchemaEditor(map, treeRight);
				} catch (Exception e1) {
					if (rightJsonPayload.getText() == null || rightJsonPayload.getText().length() == 0) {
						new InfoDialogBox(parent.getShell(), style).open(
								"To generate JSON Schema you need to load JSON Payload first.\n1. Use \"Browse JSON Payload\" button to select JSON Payload file.\nOr\n2. Paste JSON Payload text into \"JSON Payload\" text box.");
					} else
						new ErrorDialogBox(parent.getShell(), style).open(e1);
				}
			}
		});
		generateSchemaRight.setText("Load payload");
		generateSchemaRight.setOrientation(SWT.RIGHT_TO_LEFT);

		Button generatePayloadRight = new Button(composite_11, SWT.CENTER);
		generatePayloadRight.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		generatePayloadRight.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					CustomDialogBox cdb = new CustomDialogBox(shell, SWT.NONE);
					cdb.open(rightJsonSchema);
					String[] text = importJsonRight.generatePayload(rightJsonSchema.getText());
					rightJsonSchema.setText(text[0]);
					rightJsonPayload.setText(text[1]);
					Map<String, Object> map = importJsonRight.schemaMap;
					// tree.clearAll(false);
					treeRight.removeAll();
					loadSchemaEditor(map, treeRight);
				} catch (Exception e1) {
					if (rightJsonSchema.getText() == null || rightJsonSchema.getText().length() == 0) {
						new InfoDialogBox(parent.getShell(), getStyle()).open(
								"To generate JSON Payload you need to load JSON Schema first.\n1. Use \"Browse JSON Schema\" button to select JSON Schema file.\nOr\n2. Paste JSON Schema text into \"JSON Schema\" text box.");
					} else
						new ErrorDialogBox(parent.getShell(), getStyle()).open(e1);
				}
			}
		});
		generatePayloadRight.setText("Load schema");
		generatePayloadRight.setOrientation(SWT.RIGHT_TO_LEFT);

		Button btnLoadDocumentRight = new Button(composite_11, SWT.NONE);
		btnLoadDocumentRight.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		btnLoadDocumentRight.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Map<String, Object> map = importJsonRight.schemaMap;
				treeRight.removeAll();
				try {
					loadSchemaEditor(map, treeRight);
				} catch (Exception e1) {
					if (rightJsonPayload.getText() == null || rightJsonPayload.getText().length() == 0) {
						new InfoDialogBox(parent.getShell(), style).open(
								"To generate JSON Schema you need to load JSON Payload first.\n1. Use \"Browse JSON Payload\" button to select JSON Payload file.\nOr\n2. Paste JSON Payload text into \"JSON Payload\" text box.");
					} else
						new ErrorDialogBox(parent.getShell(), style).open(e1);
				}
			}
		});
		btnLoadDocumentRight.setText("Refresh");

		Label lblNewLabel_5 = new Label(composite_3, SWT.NONE);
		lblNewLabel_5.setText("Editor functions:");

		Composite composite_12 = new Composite(composite_3, SWT.NONE);
		composite_12.setLayout(new GridLayout(2, false));
		GridData gd_composite_12 = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_composite_12.heightHint = 34;
		composite_12.setLayoutData(gd_composite_12);

		Button btnAddRight = new Button(composite_12, SWT.NONE);
		btnAddRight.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		btnAddRight.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					if (treeRight.getSelection().length <= 0) {
						TreeItem rootItem = generateAPIResponsePayloadDefaults(treeRight);
						TreeItem item = new TreeItem(rootItem, SWT.FULL_SELECTION);
						item.setText(columnNameIndexMap.get("Name"), "newElement");
						item.setText(columnNameIndexMap.get("Type"), "string");
					} else {
						final TreeItem selItem = treeRight.getSelection()[0];
						String type = selItem.getText(columnNameIndexMap.get("Type"));
						if (type.equalsIgnoreCase("array<object>") || type.equalsIgnoreCase("array<array>")
								|| type.equalsIgnoreCase("object")) {
							TreeItem item = new TreeItem(selItem, SWT.FULL_SELECTION);
							item.setText(columnNameIndexMap.get("Name"), "newElement");
							item.setText(columnNameIndexMap.get("Type"), "string");
						} else {
							TreeItem item = null;
							if (selItem.getParentItem() != null)
								item = new TreeItem(selItem.getParentItem(), SWT.FULL_SELECTION);
							else
								item = new TreeItem(treeRight, SWT.FULL_SELECTION);
							item.setText(columnNameIndexMap.get("Name"), "newElement");
							item.setText(columnNameIndexMap.get("Type"), "string");
						}
					}
				} catch (Exception e2) {
					new ErrorDialogBox(shell, style).open(e2);
				}
			}
		});
		btnAddRight.setText("Add element");

		Button btnRemoveRight = new Button(composite_12, SWT.NONE);
		btnRemoveRight.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		btnRemoveRight.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					if (treeRight.getSelection().length == 0)
						new InfoDialogBox(shell, style).open(
								"Please select the row you want to remove. Selected row will be highlighted with red color.\n\nNote:-\nYou can't undo remove action.");
					else {
						final TreeItem item = treeRight.getSelection()[0];
						removeElement(item, mappingArea);
						item.dispose();
					}
				} catch (Exception e2) {
					new ErrorDialogBox(shell, style).open(e2);
				}

			}
		});
		btnRemoveRight.setText("Remove element");
		scrolledComposite_1.setContent(composite_1);
		scrolledComposite_1.setMinSize(composite_1.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		Composite composite_9 = new Composite(composite_4, SWT.NONE);
		GridData gd_composite_9 = new GridData(SWT.CENTER, SWT.CENTER, false, true, 1, 1);
		gd_composite_9.exclude = true;
		gd_composite_9.widthHint = 1;
		composite_9.setLayoutData(gd_composite_9);
		composite_9.setLayout(new GridLayout(1, true));

//		SashForm sashForm_11 = new SashForm(composite_9, SWT.NONE);
//		sashForm_11.setWeights(new int[] { });

		rightJsonSchema = new Text(composite_9, SWT.BORDER | SWT.MULTI);
		rightJsonSchema.setVisible(false);
		// rightJsonSchema.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1,
		// 1));

//		SashForm sashForm_12 = new SashForm(composite_9, SWT.NONE);
//		sashForm_12.setWeights(new int[] { });

		rightJsonPayload = new Text(composite_9, SWT.BORDER | SWT.MULTI);
		rightJsonPayload.setVisible(false);

		treeLeft.addListener(SWT.EraseItem, new Listener() {
			public void handleEvent(Event event) {
				if ((event.detail & SWT.SELECTED) != 0) {
					GC gc = event.gc;
					Rectangle area = treeLeft.getClientArea();
					int columnCount = treeLeft.getColumnCount();
					if (event.index == columnCount - 1 || columnCount == 0) {
						int width = area.x + area.width + event.x;
						if (width > 0) {
							Region region = new Region();
							gc.getClipping(region);
							region.add(event.x, event.y, width, event.height);
							gc.setClipping(region);
							region.dispose();
						}
					}
					gc.setAdvanced(true);
					if (gc.getAdvanced())
						gc.setAlpha(128);
					Rectangle rect = event.getBounds();
					Color foreground = gc.getForeground();
					Color background = gc.getBackground();
					gc.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_RED));
					gc.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
					gc.fillGradientRectangle(0, rect.y, shell.getSize().x, rect.height, false);
					// restore colors for subsequent drawing
					gc.setForeground(foreground);
					gc.setBackground(background);
					event.detail &= ~SWT.SELECTED;

					Point point = new Point(event.x, event.y);
					itemLeftSelected = treeLeft.getItem(point);
				}
			}
		});
		treeRight.addListener(SWT.EraseItem, new Listener() {
			public void handleEvent(Event event) {
				if ((event.detail & SWT.SELECTED) != 0) {
					GC gc = event.gc;
					Rectangle area = treeRight.getClientArea();
					int columnCount = treeRight.getColumnCount();
					if (event.index == columnCount - 1 || columnCount == 0) {
						int width = area.x + area.width + event.x;
						if (width > 0) {
							Region region = new Region();
							gc.getClipping(region);
							region.add(event.x, event.y, width, event.height);
							gc.setClipping(region);
							region.dispose();
						}
					}
					gc.setAdvanced(true);
					if (gc.getAdvanced())
						gc.setAlpha(128);
					Rectangle rect = event.getBounds();
					Color foreground = gc.getForeground();
					Color background = gc.getBackground();
					gc.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_RED));
					gc.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
					gc.fillGradientRectangle(0, rect.y, shell.getSize().x, rect.height, false);
					// restore colors for subsequent drawing
					gc.setForeground(foreground);
					gc.setBackground(background);
					event.detail &= ~SWT.SELECTED;

					Point point = new Point(event.x, event.y);
					itemRightSelected = treeRight.getItem(point);
				}
			}
		});

		treeLeft.addListener(SWT.Expand, new Listener() {
			public void handleEvent(Event e) {

				parent.getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						redrawComposite(mappingArea);
						try {
							Thread.sleep(150);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						for (JsonPatchMap jpOP : jsonPatchMaps) {
							drawMap(jpOP, mappingArea);
						}

					}
				});
			}
		});

		treeRight.addListener(SWT.Expand, new Listener() {
			public void handleEvent(Event e) {
				// mappingArea.redraw();
				parent.getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						redrawComposite(mappingArea);
						try {
							Thread.sleep(150);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						for (JsonPatchMap jpOP : jsonPatchMaps) {
							drawMap(jpOP, mappingArea);
						}

					}
				});

			}
		});

		treeLeft.addListener(SWT.Collapse, new Listener() {
			public void handleEvent(Event e) {

				parent.getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						redrawComposite(mappingArea);
						try {
							Thread.sleep(150);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						for (JsonPatchMap jpOP : jsonPatchMaps) {
							drawMap(jpOP, mappingArea);
						}

					}
				});
			}
		});

		treeRight.addListener(SWT.Collapse, new Listener() {
			public void handleEvent(Event e) {
				// mappingArea.redraw();
				parent.getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						redrawComposite(mappingArea);
						try {
							Thread.sleep(150);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						for (JsonPatchMap jpOP : jsonPatchMaps) {
							drawMap(jpOP, mappingArea);
						}

					}
				});
			}
		});

		GC gc = new GC(mappingArea);

		Composite composite_13 = new Composite(mappingArea, SWT.NONE);
		composite_13.setLayout(new GridLayout(8, false));
		GridData gd_composite_13 = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gd_composite_13.heightHint = 34;
		composite_13.setLayoutData(gd_composite_13);

		Label lblPackage = new Label(composite_13, SWT.NONE);
		lblPackage.setAlignment(SWT.CENTER);
		GridData gd_lblPackage = new GridData(SWT.FILL, SWT.CENTER, false, true, 1, 1);
		gd_lblPackage.widthHint = 52;
		lblPackage.setLayoutData(gd_lblPackage);
		lblPackage.setText("Package:");

		packageName = new Text(composite_13, SWT.BORDER);
		packageName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblOperationName = new Label(composite_13, SWT.NONE);
		lblOperationName.setAlignment(SWT.CENTER);
		GridData gd_lblOperationName = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		gd_lblOperationName.widthHint = 99;
		lblOperationName.setLayoutData(gd_lblOperationName);
		lblOperationName.setText("Operation name:");

		txtOperation = new Text(composite_13, SWT.BORDER);
		txtOperation.setText("Name");
		txtOperation.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

		Label lblPath = new Label(composite_13, SWT.NONE);
		lblPath.setAlignment(SWT.CENTER);
		GridData gd_lblPath = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		gd_lblPath.widthHint = 38;
		lblPath.setLayoutData(gd_lblPath);
		lblPath.setText("Path:");

		txtPath = new Text(composite_13, SWT.BORDER);
		txtPath.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblVersion = new Label(composite_13, SWT.NONE);
		lblVersion.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblVersion.setText("Version:");

		txtVersion = new Text(composite_13, SWT.BORDER);
		txtVersion.setText("v1.0.0");
		txtVersion.addKeyListener(new KeyListener() {

			@Override
			public void keyReleased(KeyEvent arg0) {
				// TODO Auto-generated method stub
				String version = txtVersion.getText();
				if (version != null && !version.toLowerCase().startsWith("v")) {
					txtVersion.setText("v" + version);
				}
			}

			@Override
			public void keyPressed(KeyEvent arg0) {
				// TODO Auto-generated method stub

			}
		});
		GridData gd_txtVersion = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_txtVersion.widthHint = 40;
		txtVersion.setLayoutData(gd_txtVersion);

		Composite composite_14 = new Composite(mappingArea, SWT.NONE);
		composite_14.setLayout(new GridLayout(2, true));
		GridData gd_composite_14 = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gd_composite_14.heightHint = 253;
		composite_14.setLayoutData(gd_composite_14);

		Composite composite_18 = new Composite(composite_14, SWT.NONE);
		GridData gd_composite_18 = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_composite_18.heightHint = 104;
		composite_18.setLayoutData(gd_composite_18);
		composite_18.setLayout(new GridLayout(1, false));

		Label lblImports = new Label(composite_18, SWT.NONE);
		lblImports.setText("imports:");

		txtImportPackages = new Text(composite_18, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
		txtImportPackages.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		Composite composite_15 = new Composite(composite_14, SWT.NONE);
		composite_15.setLayout(new GridLayout(1, false));
		composite_15.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

		Tree tree = new Tree(composite_15, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		parameterTree = tree;
		tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		tree.setOrientation(SWT.LEFT_TO_RIGHT);
		CommonUtils.enableTreeSchemaEditor(tree, columnNameIndexMap);
		TreeColumn trclmnName = new TreeColumn(tree, SWT.LEFT_TO_RIGHT);
		trclmnName.setWidth(120);
		trclmnName.setText("Parameter");
		TreeColumn trclmnType = new TreeColumn(tree, SWT.LEFT_TO_RIGHT);
		trclmnType.setWidth(120);
		trclmnType.setText("Type");
		TreeColumn trclmnDesc = new TreeColumn(tree, SWT.LEFT_TO_RIGHT);
		trclmnDesc.setWidth(120);
		trclmnDesc.setText("Description");

		Composite composite_16 = new Composite(composite_14, SWT.NONE);
		composite_16.setLayout(new GridLayout(1, false));

		composite_16.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		
		Label lblGenerateCodeTo = new Label(composite_16, SWT.NONE);
		lblGenerateCodeTo.setText("Click on \"Test\" button to generate the function signature.");
		Composite composite_17 = new Composite(composite_14, SWT.NONE);
		composite_17.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		composite_17.setLayout(new GridLayout(7, false));

		Button btnAdd = new Button(composite_17, SWT.NONE);
		btnAdd.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					if (tree.getSelection().length <= 0) {
						TreeItem rootItem = generateAPIParameterDefaults(tree);
					} else {
						final TreeItem selItem = tree.getSelection()[0];
						String type = selItem.getText(columnNameIndexMap.get("Type"));
						if (type.equalsIgnoreCase("array<object>") || type.equalsIgnoreCase("array<array>")
								|| type.equalsIgnoreCase("object")) {
							TreeItem item = new TreeItem(selItem, SWT.FULL_SELECTION);
							item.setText(columnNameIndexMap.get("Name"), "newElement");
							item.setText(columnNameIndexMap.get("Type"), "string");
						} else {
							TreeItem item = null;
							if (selItem.getParentItem() != null)
								item = new TreeItem(selItem.getParentItem(), SWT.FULL_SELECTION);
							else
								item = new TreeItem(tree, SWT.FULL_SELECTION);
							item.setText(columnNameIndexMap.get("Name"), "newElement");
							item.setText(columnNameIndexMap.get("Type"), "string");
						}
					}
				} catch (Exception e2) {
					new ErrorDialogBox(shell, style).open(e2);
				}
			}
		});
		btnAdd.setText("Add");

		Button btnDelete = new Button(composite_17, SWT.NONE);
		btnDelete.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					if (tree.getSelection().length == 0)
						new InfoDialogBox(shell, style).open(
								"Please select the row you want to remove. Selected row will be highlighted with red color.\n\nNote:-\nYou can't undo remove action.");
					else {
						final TreeItem item = tree.getSelection()[0];
						removeElement(item, mappingArea);
						item.dispose();
					}
				} catch (Exception e2) {
					new ErrorDialogBox(shell, style).open(e2);
				}
			}
		});
		btnDelete.setText("Delete");

		Label apiMethod = new Label(composite_17, SWT.NONE);
		apiMethod.setText("Method:");

		Combo methodCombo = new Combo(composite_17, SWT.NONE);
		this.methodCombo = methodCombo;
		methodCombo.setItems(new String[] { "GET", "POST", "PUT", "PATCH", "DELETE" });
		GridData gd_methodCombo = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_methodCombo.widthHint = 41;
		methodCombo.setLayoutData(gd_methodCombo);
		methodCombo.setText("GET");

		Button btnSave = new Button(composite_17, SWT.NONE);
		btnSave.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ClassMetaData service = generateClasses(treeLeft, treeRight, parameterTree, packageName.getText(),
						txtImportPackages.getText(), txtImplementYourMain.getText(), txtWriteYourPrivate.getText(),
						txtVersion.getText(), txtOperation.getText(), txtPath.getText(), methodCombo.getText());
				String jsonWorkspace = generateSchema(service, CG.classes);
				save(jsonWorkspace, service, CG.classes);
				CG.classes.clear();
			}
		});
		btnSave.setText("Generate");

		Button btnTest = new Button(composite_17, SWT.NONE);
		btnTest.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					EmptyDialogBox edb = new EmptyDialogBox(shell, style);
					JPayloadEditorGUI jpeGUI = new JPayloadEditorGUI(edb.composite, style);
					if (treeLeft.getItemCount() > 0) {

						ClassMetaData service = generateClasses(treeLeft, treeRight, parameterTree,
								packageName.getText(), txtImportPackages.getText(), txtImplementYourMain.getText(),
								txtWriteYourPrivate.getText(), txtVersion.getText(), txtOperation.getText(),
								txtPath.getText(), methodCombo.getText());
						String jsonWorkspace = generateSchema(service, CG.classes);
						CG.classes.clear();

						ObjectMapper mapper = new ObjectMapper();
						// System.out.println(jsonWorkspace);
						JsonNode nodeL = mapper.readTree(jsonWorkspace);
						JsonNode leftNode = ((ObjectNode) nodeL.get("properties").get("Payload").get("properties"));// ;
						String imports = nodeL.get("import").textValue();
						String code = nodeL.get("code").textValue();
						String custom = nodeL.get("custom").textValue();
						String operation = nodeL.get("operation").textValue();

						if (imports != null && imports.trim().length() > 0)
							txtImportPackages.setText(new String(Base64.getDecoder().decode(imports)));
						if (code != null && code.trim().length() > 0)
							txtImplementYourMain.setText(new String(Base64.getDecoder().decode(code)));
						if (custom != null && custom.trim().length() > 0)
							txtWriteYourPrivate.setText(new String(Base64.getDecoder().decode(custom)));
						if (operation != null && operation.trim().length() > 0)
							txtOperation.setText(operation);
						((ObjectNode) leftNode).remove("ResponsePayload");
						JsonNode nodeR = mapper.readTree(jsonWorkspace);
						JsonNode rightNode = ((ObjectNode) nodeR.get("properties").get("Payload").get("properties"));// .remove("RequestPayload");//node.at("/Payload/RequestPayload");
						((ObjectNode) rightNode).remove("RequestPayload");
						leftJsonSchema.setText(nodeL.toPrettyString());
						String[] text = importJsonLeft.generatePayload(leftJsonSchema.getText());
						leftJsonSchema.setText(text[0]);
						leftJsonPayload.setText(text[1]);

						if (leftJsonSchema != null && leftJsonSchema.getText().trim().length() > 0)
							jpeGUI.loadSchema(leftJsonSchema.getText());
					}
					edb.open(jpeGUI,null,null,null);
				} catch (Exception er) {
					new ErrorDialogBox(shell, shell.getStyle()).open(er);
				}
			}
		});
		btnTest.setText("Test");

		Button btnOpen = new Button(composite_17, SWT.NONE);
		btnOpen.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					openMokshSchema();
					ClassMetaData service = generateClasses(treeLeft, treeRight, parameterTree, packageName.getText(),
							txtImportPackages.getText(), txtImplementYourMain.getText(), txtWriteYourPrivate.getText(),
							txtVersion.getText(), txtOperation.getText(), txtPath.getText(), methodCombo.getText());
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		btnOpen.setText("Open");
		
		Composite composite = new Composite(mappingArea, SWT.NONE);
		composite.setLayout(new FillLayout(SWT.HORIZONTAL));
		GridData gd_composite = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gd_composite.heightHint = 37;
		composite.setLayoutData(gd_composite);
		
		lblOperationSig = new Text(composite, SWT.BORDER | SWT.READ_ONLY | SWT.MULTI);
		lblOperationSig.setEnabled(false);
		lblOperationSig.setText("\r\n{");

		txtImplementYourMain = new Text(mappingArea, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL | SWT.MULTI);
		txtImplementYourMain.setText("//Implement your main service here.");
		txtImplementYourMain.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Composite composite_19 = new Composite(mappingArea, SWT.NONE);
		composite_19.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		composite_19.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		text_1 = new Text(composite_19, SWT.BORDER);
		text_1.setEnabled(false);
		text_1.setText("}");
		text_1.setEditable(false);
		gc.setLineWidth(2);
		gc.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_BLUE));
		gc.drawLine(0, 0, 100, 200);
		gc.dispose();
	}

	CodeGen CG = CodeGen.getInstance();
	String jsonWorkspace = "";
	ClassMetaData cls = null;
	public String identifier = "-~-~-~-010-~-~-~";
	private Text txtPath;
	private Text txtOperation;
	private Text txtVersion;
	private Text lblOperationSig;
	private Text text_1;

	private void appendln(String data) {
		jsonWorkspace += data + "\n";
		// System.out.println(data.replaceAll(","+identifier, "").replaceAll(identifier,
		// ""));
	}

	private void openMokshSchema() throws Exception {
		String filePath = openFileDialog();
		if (filePath == null || filePath.trim().length() == 0)
			return;
		// schemaMap.clear();
		String basePackageName = (new File(filePath).getName().toLowerCase() + "_").replace(".json_", "");
		packageName.setText(basePackageName);

		System.out.println("You chose to open this file: " + filePath);
		ObjectMapper mapper = new ObjectMapper();
		JsonNode nodeL = mapper.readTree(new File(filePath));
		if(!nodeL.at("/properties/Headers/properties/ResponseHeaders").isMissingNode())
			((ObjectNode) nodeL.get("properties").get("Headers").get("properties")).remove("ResponseHeaders");
		if(!nodeL.at("/properties/Parameter").isMissingNode())
			((ObjectNode) nodeL.get("properties")).remove("Parameter");
		JsonNode leftNode = ((ObjectNode) nodeL.get("properties").get("Payload").get("properties"));// ;

		if(leftNode.get("ResponsePayload")!=null)
			((ObjectNode) leftNode).remove("ResponsePayload");

		JsonNode nodeR = mapper.readTree(new File(filePath));
		if(!nodeR.at("/properties/Headers/properties/RequestHeaders").isMissingNode())
			((ObjectNode) nodeR.get("properties").get("Headers").get("properties")).remove("RequestHeaders");
		if(!nodeR.at("/properties/Parameter").isMissingNode())
			((ObjectNode) nodeR.get("properties")).remove("Parameter");
		JsonNode rightNode = ((ObjectNode) nodeR.get("properties").get("Payload").get("properties"));// .remove("RequestPayload");//node.at("/Payload/RequestPayload");
		if(rightNode.get("RequestPayload")!=null)
			((ObjectNode) rightNode).remove("RequestPayload");
		
		leftJsonSchema.setText(nodeL.toPrettyString());

		String[] text = importJsonLeft.generatePayload(leftJsonSchema.getText());
		leftJsonSchema.setText(text[0]);
		leftJsonPayload.setText(text[1]);
		Map<String, Object> map = importJsonLeft.schemaMap;
		// tree.clearAll(false);
		treeLeft.removeAll();
		loadSchemaEditor(map, treeLeft);

		rightJsonSchema.setText(nodeR.toPrettyString());
		text = importJsonRight.generatePayload(rightJsonSchema.getText());
		rightJsonSchema.setText(text[0]);
		rightJsonPayload.setText(text[1]);
		map = importJsonRight.schemaMap;
		// tree.clearAll(false);
		treeRight.removeAll();
		loadSchemaEditor(map, treeRight);

		String imports = nodeL.get("import").textValue();
		String code = nodeL.get("code").textValue();
		String custom = nodeL.get("custom").textValue();
		String operation = nodeL.get("operation").textValue();
		String version = nodeL.get("version").textValue();
		//String packageName = nodeL.get("packageName").textValue();
		String path = nodeL.get("path").textValue();
		if (imports != null && imports.trim().length() > 0)
			txtImportPackages.setText(new String(Base64.getDecoder().decode(imports)));
		if (code != null && code.trim().length() > 0)
			txtImplementYourMain.setText(new String(Base64.getDecoder().decode(code)));
		if (custom != null && custom.trim().length() > 0)
			txtWriteYourPrivate.setText(new String(Base64.getDecoder().decode(custom)));
		if (operation != null && operation.trim().length() > 0)
			txtOperation.setText(operation);
		if (version != null && version.trim().length() > 0)
			txtVersion.setText(version);
		if (path != null && path.trim().length() > 0)
			txtPath.setText(path);

		JsonNode nodeParemeter = mapper.readTree(new File(filePath));
		((ObjectNode) nodeParemeter.get("properties")).remove("Payload");
		((ObjectNode) nodeParemeter.get("properties")).remove("Headers");
		ImportJSON importJsonParameter = new ImportJSON();
		importJsonParameter.generatePayload(nodeParemeter.toPrettyString());
		map = importJsonParameter.schemaMap;
		parameterTree.removeAll();
		loadSchemaEditor(map, parameterTree);
	}

	private String openFileDialog() throws Exception {
		FileDialog fd = new FileDialog(getShell(), SWT.OPEN);
		fd.setText("Open");
		String[] filterExt = { "*.json;*.JSON" };
		String[] filterNames = { "TXT files" };
		fd.setFilterExtensions(filterExt);
		fd.setFilterNames(filterNames);
		/*
		 * String lastPath = System.getInstance().getString(Config.LAST_OPEN_TEXT_PATH);
		 * if (lastPath != null && !lastPath.isEmpty()) fd.setFileName(lastPath);
		 */
		String selected = fd.open();
		return selected;
	}

	private void append(String data) {
		jsonWorkspace += data;
		// System.out.print(data.replaceAll(","+identifier, "").replaceAll(identifier,
		// ""));
	}

	protected ClassMetaData generateClasses(Tree requestDocTree, Tree responseDocTree, Tree paramTree,
			String packageName, String importedPackages, String serviceImplementation, String servicePrivateCode,
			String version, String operationName, String path, String method) {

		Map<String, ClassMetaData> classes;
		TreeItem[] requestDoc = requestDocTree.getItems();
		TreeItem[] responseDoc = responseDocTree.getItems();
		TreeItem[] paramDoc = paramTree.getItems();
		try {
			ClassMetaData service = createRoot(operationName, requestDoc, responseDoc, paramDoc);
			API api = service.getAPI();
			api.version = version;
			api.code = Base64.getEncoder().encodeToString(serviceImplementation.getBytes());
			api.imports = Base64.getEncoder().encodeToString(importedPackages.getBytes());
			api.custom = Base64.getEncoder().encodeToString(servicePrivateCode.getBytes());
			api.path = path;
			api.method = method;
			api.packageName = packageName;
			classes = CG.classes;
			geneateFunction(classes, service);
			return service;
		} catch (Exception e) {
			new ErrorDialogBox(getShell(), getShell().getStyle()).open(e);
		}
		return null;
	}

	private String generateSchema(ClassMetaData service, Map<String, ClassMetaData> classes) {
		API api = service.getAPI();
		appendln("{");
		appendln("\"$schema\": \"http://json-schema.org/draft-04/schema#\",");
		appendln("\"import\": \"" + api.imports + "\", ");
		appendln("\"code\": \"" + api.code + "\", ");
		appendln("\"custom\": \"" + api.custom + "\", ");
		appendln("\"path\": \"" + api.path + "\", ");
		appendln("\"version\": \"" + api.version + "\", ");
		appendln("\"operation\": \"" + api.operationName + "\", ");
		appendln("\"type\": \"object\", ");

		appendln("\"properties\": {");
		appendln(service.toJsonSchema(classes));
		appendln("}");
		appendln("}");
		jsonWorkspace = jsonWorkspace.replaceAll("," + identifier, "").replaceAll(identifier, "");
		String temp = jsonWorkspace;
		jsonWorkspace = "";
		return temp;
	}

	private void save(String jsonWorkspace, ClassMetaData service, Map<String, ClassMetaData> classes) {
		try {
			API api = service.getAPI();
			String versionWithoutSpecialChars=(api.version.replaceAll("[^a-zA-Z0-9]", ""));
			String filename=api.packageName + ".JSON";
			String dirPath = CommonUtils.saveFileDialog(getShell(), filename);//(getShell());
			
			if(dirPath==null)
				return;
			dirPath=dirPath.replace("/"+filename, "").replace("\\"+filename, "");
			System.out.println(dirPath);
			if (!dirPath.replaceAll("[^a-zA-Z0-9]", "").contains(api.packageName.replaceAll("[^a-zA-Z0-9]", ""))) {
				throw new Exception("Directory path should match package name '" + api.packageName + "'."
						+ "\nPackage path:\n" + api.packageName + "\nFolder Path:\n" + dirPath);
			}
			if(!dirPath.endsWith(versionWithoutSpecialChars)) {
				dirPath += "/" + versionWithoutSpecialChars;
			}
			if (new File(dirPath).exists()) {
				String val = new PromptDialogBox(getShell(), getStyle())
						.open("Version(" + api.version + ") already exists. \nOverwrite existing version?");
				if (val == null)
					return;
			}

			service.exportAPI(dirPath, api.packageName + "." + versionWithoutSpecialChars, classes);
			ClassMetaData resource = classes.get(service.getName() + "Resource");
			resource.exportJava(dirPath, api.packageName + "."+api.version.replaceAll("[^a-zA-Z0-9]", ""));
			File dir = new File(dirPath);
			if (!dir.exists())
				dir.mkdirs();
			FileOutputStream fos = new FileOutputStream(
					new File(dirPath + "/" + api.packageName + ".JSON"));
			fos.write(jsonWorkspace.getBytes());
			fos.flush();
			fos.close();
			new PromptDialogBox(getShell(), getStyle()).open("Saved!");
		} catch (Exception e) {
			new ErrorDialogBox(getShell(), getShell().getStyle()).open(e);
		}
	}

	private void geneateFunction(Map<String, ClassMetaData> classes, ClassMetaData clazz) throws Exception {
		ClassMetaData resource = classes.get(clazz.getName() + "Resource");
		API api = clazz.getAPI();
		Set<String> imports = api.getImportList();
		if (imports != null)
			for (String imprt : imports) {
				resource.addImport(imprt);
			}

		// resource.addProperty(access, type, name, primitive)
		ClassMetaData payload = classes.get(clazz.getName() + ".Payload");
		String requestBody = "@RequestBody(required = false)";
		Function apiActionImpl = null;
//		Function functionJsonWrapper = null;
		if ((payload.getProperty("requestpayloads") != null && payload.getProperty("requestpayloads").required)
				|| (payload.getProperty("requestpayload") != null && payload.getProperty("requestpayload").required))
			requestBody = "@RequestBody(required = true)";
		if (payload.getProperty("responsepayload") != null) {
			apiActionImpl = resource.addFunction("public", "ResponsePayload", clazz.getName().toLowerCase());
//			functionJsonWrapper = resource.addFunction("public", "String", clazz.getName().toLowerCase() + "Wrapper");
		} else {
			apiActionImpl = resource.addFunction("public", "List<ResponsePayload>",
					clazz.getName().toLowerCase() + "s");
//			functionJsonWrapper = resource.addFunction("public", "String", clazz.getName().toLowerCase() + "s");
		}
//		functionJsonWrapper.addParam("", "String", "jsonPayload");
//		functionJsonWrapper.exceptions.add("Exception");

		if (payload.getProperty("requestpayload") != null) {
			
		if(payload.getProperty("requestpayload").type.startsWith("List<"))
			apiActionImpl.addParam(requestBody, "List<RequestPayload>", "requestPayload");
		else
			apiActionImpl.addParam(requestBody, "RequestPayload", "requestPayload");
		}
		String method = api.method.toLowerCase();
		method = method.substring(0, 1).toUpperCase() + method.substring(1);// making first alphabet capital and others
																			// small
		ClassMetaData parameter = classes.get(clazz.getRoot() + ".Parameter");
		List<Property> queryStringProps = null;
		if(parameter!=null)
			queryStringProps=parameter.getProperties();
		ClassMetaData requestheaders = classes.get(clazz.getRoot() + ".Headers" + "." + "RequestHeaders");
		// ClassMetaData responseheaders = classes.get(clazz.getRoot() + ".Headers" +
		// "." + "ResponseHeaders");
		if (requestheaders != null) {
			List<Property> properties = requestheaders.getProperties();
			if (properties != null && properties.size() > 0) {
				resource.addImport("import org.springframework.web.bind.annotation.RequestHeader;");
				requestheaders.addAnnotation("@AllArgsConstructor");
				requestheaders.addImport("import lombok.AllArgsConstructor;");
				requestheaders.addAnnotation("@NoArgsConstructor");
				requestheaders.addImport("import lombok.NoArgsConstructor;");
				for (Property property : properties) {
					apiActionImpl.addParam("@RequestHeader(\"" + property.name + "\")", property.type, property.name);
				}
			}
		}
		// @PostMapping(path = "/entrys",params= {"projectId","timesheetId"})
		// @RequestParam long timesheetId
		// @PathVariable long id
		String params = "";
		if(queryStringProps!=null)
		for (Property property : queryStringProps) {
			if (property.primitive && !property.name.equalsIgnoreCase("path")) {
				if (!api.path.contains("{" + property.name + "}")) {
					apiActionImpl.addParam("@RequestParam", property.type, property.name);
					params += "\"" + property.name + "\",";
				} else {
					apiActionImpl.addParam("@PathVariable", property.type, property.name);
				}
			}
		}
		if (params.trim().length() > 0)
			params = (params + ",").replace(",,", "");

//		functionJsonWrapper.codeLines.add("ObjectMapper om=new ObjectMapper();");
//		functionJsonWrapper.codeLines.add("om.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);");
//		functionJsonWrapper.codeLines
//				.add("RequestPayload req=om.readValue(jsonPayload.getBytes(), RequestPayload.class);");
//		functionJsonWrapper.codeLines.add("String ret=om.writeValueAsString(" + apiActionImpl.name + "(req));");
//		functionJsonWrapper.codeLines.add("return ret;");
		resource.addImport("import com.fasterxml.jackson.databind.ObjectMapper;");
		resource.addImport("import com.fasterxml.jackson.databind.MapperFeature;");

		// apiActionImpl.annotations.add("@" + method + "Mapping(path=\"" + api.path +
		// "\")");
		apiActionImpl.annotations.add("@" + method + "Mapping(path=\"/" + api.path+"/"+api.version + "\", params={" + params + "})");
		apiActionImpl.codeLines.add(new String(Base64.getDecoder().decode(api.code)));
		resource.setCustomCode(new String(Base64.getDecoder().decode(api.custom)));
		//System.out.println(apiActionImpl.toString());
		lblOperationSig.setText(apiActionImpl.toString());//.replace("{", "").replace("}", ""));
	}

/*	private String openFolderDialog() throws Exception {
		DirectoryDialog fd = new DirectoryDialog(this.getShell(), SWT.OPEN);
		fd.setText("Open");

		String selected = fd.open();
		return selected;
	}*/

	private ClassMetaData createRoot(String name, TreeItem[] requestDoc, TreeItem[] responseDoc, TreeItem paramDoc[])
			throws Exception {
		// String apiMethod=type.toLowerCase().replace("api<", "").replace(">", "");
		int pad = 1;
		// String type="object";
		String clsName = name;
		cls = CG.createAPIClass(clsName);
		API api = cls.getAPI();
		api.operationName = name;
		cls.setScope("Global");
		if (requestDoc != null)
			for (TreeItem item : requestDoc) {
				processItem(item, pad, cls);
			}
		if (responseDoc != null)
			for (TreeItem item : responseDoc) {
				processItem(item, pad, cls);
			}
		if (paramDoc != null)
			for (TreeItem item : paramDoc) {
				System.out.println("Parameter" + item.getText());
				processItem(item, pad, cls);
			}
		return cls;
	}

	private void newSchema(TreeItem treeItem, int pad, ClassMetaData cls) throws Exception {
		TreeItem items[] = items = treeItem.getItems();
		for (TreeItem item : items) {
			processItem(item, pad, cls);
		}
	}

	private void processItem(TreeItem item, int pad, ClassMetaData cls) throws Exception {
		String type = item.getText(columnNameIndexMap.get("Type"));// type of object
		String clsName = item.getText(columnNameIndexMap.get("Name"));
		if (type.equalsIgnoreCase("object")) {
			if (cls == null) {
				cls = CG.createEntityClass(clsName, "Local", clsName);
			} else {
				Property prop = cls.addProperty("private", clsName, clsName.toLowerCase(), false);
				cls = CG.createEntityClass(clsName, "Local", cls.getRoot() + "." + clsName);
			}
			newSchema(item, pad + 2, cls);
		} else if (type.toLowerCase().contains("array")) {
			// String clsName=item.getText(columnNameIndexMap.get("Name"));
			String subType = type.toLowerCase().replace("array<", "").replace(">", "");
			if (subType.equalsIgnoreCase("object")) {
				if (cls == null) {
					cls = CG.createEntityClass(clsName, "Local", clsName);
				} else {
					cls.addImport("import java.util.List;");
					Property prop = cls.addProperty("private", "List<" + clsName + ">",
							CodeGen.getPlural(clsName.toLowerCase()), false);
					cls = CG.createEntityClass(clsName, "Local", cls.getRoot() + "." + clsName);
				}
			} else {
				if (cls == null) {
					cls = CG.createEntityClass(clsName, "Local", clsName);
					Property prop = null;
					if (subType.equalsIgnoreCase("string"))
						prop = cls.addProperty("private ", "String[]", CodeGen.getPlural(clsName.toLowerCase()), true);
					else
						prop = cls.addProperty("private ", subType + "[]", CodeGen.getPlural(clsName.toLowerCase()),
								true);
				} else {
					Property prop = null;
					if (subType.equalsIgnoreCase("string"))
						prop = cls.addProperty("private ", "String[]", CodeGen.getPlural(clsName.toLowerCase()), true);
					else
						prop = cls.addProperty("private ", CommonUtils.resolveJavaType(subType,null) + "[]", CodeGen.getPlural(clsName.toLowerCase()),
								true);
					cls = CG.createEntityClass(clsName, "Local", cls.getRoot() + "." + clsName);
				}
			}
			newSchema(item, pad + 3, cls);
		} else {
			Property prop = null;
			if (type.equalsIgnoreCase("string"))
				prop = cls.addProperty("private ", "String ", (clsName.toLowerCase()), true);
			else
				prop = cls.addProperty("private ", CommonUtils.resolveJavaType(type,null) + " ", (clsName.toLowerCase()), true);
		}
	}

	public void redrawComposite(Composite composite) {
		composite.redraw();
		// composite.pack(false);
		composite.update();
		gc.dispose();
		gc = new GC(composite);
	}

	private void drawMap(JsonPatchMap op, Composite mappingArea) {
		int ly = op.itemLeftSelected.getBounds(0).y;
		int ry = op.itemRightSelected.getBounds(0).y;
		TreeItem ti = op.itemLeftSelected;

		try {
			while (ly <= 0) {
				ti = ti.getParentItem();
				ly = ti.getBounds(0).y;
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		ti = op.itemRightSelected;
		try {
			while (ry <= 0) {
				ti = ti.getParentItem();
				ry = ti.getBounds(0).y;
			}
		} catch (Exception e) {
			// TODO: handle exception
		}

		gc.drawLine(0, ly + 32, mappingArea.getBounds().width, ry + 32);
		// System.out.println("repaint");
	}

	private void loadSchemaEditor(Map<String, Object> map, Tree tree) throws Exception {
		Object[] keys = map.keySet().toArray();
		int itemCount = keys.length;
		for (int i = 0; i < itemCount; i++) {
			TreeItem item = new TreeItem(tree, SWT.FULL_SELECTION);
			item.setText(columnNameIndexMap.get("Name"), keys[i].toString());
			// item.getCl
			if (map.get(keys[i]) instanceof Map)
				loadSchemaEditor((Map) map.get(keys[i]), item);
		}
	}

	private void loadSchemaEditor(Map<String, Object> map, TreeItem ti) throws Exception {
		Object[] keys = map.keySet().toArray();
		int itemCount = keys.length;
//	    ti.setText(descIndex, (map.get("description")+"").replace("null", ""));
//	    ti.setText(titleIndex, (map.get("title")+"").replace("null", ""));
		ti.setText(columnNameIndexMap.get("Type"), (map.get("type") + "").replace("null", ""));
//	    ti.setText(egIndex, (map.get("example")+"").replace("null", ""));
//	    ti.setText(formatIndex, (map.get("format")+"").replace("null", ""));
//	    ti.setText(enumIndex, (map.get("enum")+"").replace("null", ""));
//	    ti.setText(scopeIndex, (map.get("scope")+"").replace("null", ""));
//	    ti.setText(requiredIndex, (map.get("required")+"").replace("null", "")); 

		for (int i = 0; i < itemCount; i++) {
			if (map.get(keys[i]) instanceof Map) {
				TreeItem item = new TreeItem(ti, SWT.FULL_SELECTION);
				item.setText(columnNameIndexMap.get("Name"), keys[i].toString());// +"<"+(map.get("type")+"").replace("null",
																					// "")+">");
				loadSchemaEditor((Map) map.get(keys[i]), item);
			}
		}
	}

	private void enableEditor(Tree tree) {
		CommonUtils.enableTreeSchemaEditor(tree, columnNameIndexMap);
	}

	private void removeElement(TreeItem item, Composite mappingArea) {
		List<JsonPatchMap> removeList = new ArrayList<JsonPatchMap>();
		for (JsonPatchMap jsonPatchOP : jsonPatchMaps) {
			if (jsonPatchOP.itemLeftSelected == item || jsonPatchOP.itemRightSelected == item)
				removeList.add(jsonPatchOP);
		}
		if (removeList.size() > 0) {
			jsonPatchMaps.removeAll(removeList);
			redrawComposite(mappingArea);
			for (JsonPatchMap jOP : jsonPatchMaps) {
				drawMap(jOP, mappingArea);
			}
		}
	}

	private TreeItem generateAPIRequestPayloadDefaults(Tree tree) {
		if (tree.getItemCount() > 0)
			return null;
		TreeItem item = new TreeItem(tree, SWT.FULL_SELECTION);
		item.setText(columnNameIndexMap.get("Name"), "Payload");
		item.setText(columnNameIndexMap.get("Type"), "object");

		item = new TreeItem(item, SWT.FULL_SELECTION);
		item.setText(columnNameIndexMap.get("Name"), "RequestPayload");
		item.setText(columnNameIndexMap.get("Type"), "object");

		TreeItem requestPayload = item;

		item = new TreeItem(tree, SWT.FULL_SELECTION);
		item.setText(columnNameIndexMap.get("Name"), "Headers");
		item.setText(columnNameIndexMap.get("Type"), "object");

		item = new TreeItem(item, SWT.FULL_SELECTION);
		item.setText(columnNameIndexMap.get("Name"), "RequestHeaders");
		item.setText(columnNameIndexMap.get("Type"), "object");

		item = new TreeItem(item, SWT.FULL_SELECTION);
		item.setText(columnNameIndexMap.get("Name"), "requestID");
		item.setText(columnNameIndexMap.get("Type"), "integer");

		return requestPayload;
	}

	private TreeItem generateAPIResponsePayloadDefaults(Tree tree) {
		if (tree.getItemCount() > 0)
			return null;
		TreeItem item = new TreeItem(tree, SWT.FULL_SELECTION);
		item.setText(columnNameIndexMap.get("Name"), "Payload");
		item.setText(columnNameIndexMap.get("Type"), "object");

		item = new TreeItem(item, SWT.FULL_SELECTION);
		item.setText(columnNameIndexMap.get("Name"), "ResponsePayload");
		item.setText(columnNameIndexMap.get("Type"), "object");

		TreeItem responsePayload = item;

		item = new TreeItem(tree, SWT.FULL_SELECTION);
		item.setText(columnNameIndexMap.get("Name"), "Headers");
		item.setText(columnNameIndexMap.get("Type"), "object");

		item = new TreeItem(item, SWT.FULL_SELECTION);
		item.setText(columnNameIndexMap.get("Name"), "ResponseHeaders");
		item.setText(columnNameIndexMap.get("Type"), "object");

		item = new TreeItem(item, SWT.FULL_SELECTION);
		item.setText(columnNameIndexMap.get("Name"), "status");
		item.setText(columnNameIndexMap.get("Type"), "string");

		TreeItem status = new TreeItem(responsePayload, SWT.FULL_SELECTION);
		status.setText(columnNameIndexMap.get("Name"), "Status");
		status.setText(columnNameIndexMap.get("Type"), "object");

		item = new TreeItem(status, SWT.FULL_SELECTION);
		item.setText(columnNameIndexMap.get("Name"), "message");
		item.setText(columnNameIndexMap.get("Type"), "string");

		item = new TreeItem(status, SWT.FULL_SELECTION);
		item.setText(columnNameIndexMap.get("Name"), "details");
		item.setText(columnNameIndexMap.get("Type"), "string");

		return responsePayload;
	}

	private TreeItem generateAPIParameterDefaults(Tree tree) {
		if (tree.getItemCount() > 0)
			return null;
		TreeItem item = new TreeItem(tree, SWT.FULL_SELECTION);
		item.setText(columnNameIndexMap.get("Name"), "Parameter");
		item.setText(columnNameIndexMap.get("Type"), "object");
		TreeItem param = item;
		item = new TreeItem(item, SWT.FULL_SELECTION);
		item.setText(columnNameIndexMap.get("Name"), "version");
		item.setText(columnNameIndexMap.get("Type"), "integer");
		return param;
	}
}
