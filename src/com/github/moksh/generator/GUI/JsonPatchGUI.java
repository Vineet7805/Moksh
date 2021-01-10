package com.github.moksh.generator.GUI;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.zjsonpatch.JsonPatch;
import com.github.moksh.generator.GUI.Utils.CommonUtils;
import com.github.moksh.generator.core.ImportJSON;
import com.github.moksh.generator.core.JPOP;
import com.github.moksh.generator.core.JsonPatchMap;

public class JsonPatchGUI extends Composite {

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
	private TreeItem itemLeftSelected = null;
	private TreeItem itemRightSelected = null;
	private TreeItem itemLastSelected = null;
	private List<JsonPatchMap> jsonPatchCopyList = null;
	private List<JsonPatchMap> jsonPatchDropList = null;
	private List<JsonPatchMap> jsonPatchInvokeList = null;
	private List<JsonPatchMap> jsonPatchAddList = null;
	private GC gc = null;
	int nameIndex = 0, typeIndex = 1;
	Map<String, Integer> columnNameIndexMap = null;
	private Text jsonPatch;
	Button btnDropParam = null;
	Composite mappingArea = null;
	JPayloadEditorGUI leftPayloadEditorGUI = null;
	JPayloadEditorGUI rightPayloadEditorGUI = null;

	public JsonPatchGUI(Composite parent, int style) {
		super(parent, style);
		importJsonRight = ImportJSON.getInstance();
		importJsonLeft = ImportJSON.getInstance();
		jsonPatchCopyList = new ArrayList<JsonPatchMap>();
		jsonPatchDropList = new ArrayList<JsonPatchMap>();
		jsonPatchInvokeList = new ArrayList<JsonPatchMap>();
		jsonPatchAddList = new ArrayList<JsonPatchMap>();
		columnNameIndexMap = new HashMap<String, Integer>();
		columnNameIndexMap.put("Name", nameIndex);
		columnNameIndexMap.put("Type", typeIndex);
		columnNameIndexMap.put("Value", 2);
		columnNameIndexMap.put("Title", 2);
		columnNameIndexMap.put("Description", 3);
		setLayout(new FillLayout(SWT.HORIZONTAL));
		Composite composite_4 = new Composite(this, SWT.NONE);
		this.setSize(1400, 800);

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
		GridData gd_composite_8 = new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1);
		gd_composite_8.exclude = true;
		gd_composite_8.widthHint = 50;
		composite_8.setLayoutData(gd_composite_8);
		composite_8.setLayout(new GridLayout(1, true));

		SashForm sashForm_9 = new SashForm(composite_8, SWT.NONE);

		Button generateSchemaLeft = new Button(sashForm_9, SWT.CENTER);
		generateSchemaLeft.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					String retStr[] = importJsonLeft.generateSchema(leftJsonPayload.getText());
					leftJsonSchema.setText(retStr[0]);
					leftJsonPayload.setText(retStr[1]);
					importJsonLeft.generatePayload(leftJsonSchema.getText());
					Map<String, Object> map = importJsonLeft.schemaMap;
					treeLeft.removeAll();
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
		generateSchemaLeft.setText("Generate Schema");
		generateSchemaLeft.setOrientation(SWT.RIGHT_TO_LEFT);
		// Image img=SWTResourceManager.getImage(JSchemaGeneratorGUI.class,
		// "/com/github/moksh/images/reload.png");
		// btnReload.setImage(img);
		sashForm_9.setWeights(new int[] { 1 });

		leftJsonSchema = new Text(composite_8, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL | SWT.MULTI);
		leftJsonSchema.setText(
				"{\r\n\"$schema\": \"http://json-schema.org/draft-04/schema#\",\r\n\"type\": \"object\", \r\n\"properties\": {\r\n\"Payload\":{\r\n\"type\":\"object\",\r\n\"properties\":{\r\n\"RequestPayload\":{\r\n\"type\":\"object\",\r\n\"properties\":{\r\n\"Color\":{\r\n\"type\":\"array\",\r\n\"items\": {\r\n\"type\":\"object\",\r\n\"properties\":{\r\n\"RGB\":{\r\n\"type\" : \"string\"\r\n}\r\n\r\n}\r\n}\r\n}\r\n\r\n}\r\n}\r\n\r\n}\r\n}\r\n}}");
		GridData gd_leftJsonSchema = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_leftJsonSchema.heightHint = 172;
		leftJsonSchema.setLayoutData(gd_leftJsonSchema);

		SashForm sashForm_10 = new SashForm(composite_8, SWT.NONE);

		Button generatePayloadLeft = new Button(sashForm_10, SWT.CENTER);
		generatePayloadLeft.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
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
		generatePayloadLeft.setText("Generate Payload");
		sashForm_10.setWeights(new int[] { 1 });

		leftJsonPayload = new Text(composite_8, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL | SWT.MULTI);
		GridData gd_leftJsonPayload = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_leftJsonPayload.heightHint = 15;
		leftJsonPayload.setLayoutData(gd_leftJsonPayload);

		Composite composite_5 = new Composite(composite_4, SWT.NONE);
		composite_5.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		composite_5.setLayout(new GridLayout(1, false));

		ScrolledComposite scrolledComposite = new ScrolledComposite(composite_5,
				SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		GridData gd_scrolledComposite = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_scrolledComposite.heightHint = 345;
		gd_scrolledComposite.widthHint = 269;
		scrolledComposite.setLayoutData(gd_scrolledComposite);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		Composite composite_6 = new Composite(scrolledComposite, SWT.NONE);
		GridLayout gl_composite_6 = new GridLayout(3, false);
		gl_composite_6.verticalSpacing = 0;
		gl_composite_6.marginHeight = 0;
		composite_6.setLayout(gl_composite_6);
		
		treeLeft = new Tree(composite_6, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		GridData gd_treeLeft = new GridData(SWT.LEFT, SWT.FILL, false, true, 1, 1);
		gd_treeLeft.widthHint = 400;
		treeLeft.setLayoutData(gd_treeLeft);

		// tree = new Tree(editorComposite, SWT.MULTI | SWT.FULL_SELECTION);
		// treeLeft.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		treeLeft.setHeaderVisible(true);
		treeLeft.setLinesVisible(true);
		//treeLeft.setFont(treeFont);
		CommonUtils.enableTreeSchemaEditor(treeLeft, columnNameIndexMap);//Editor(treeLeft);
		final ScrollBar vLBar=treeLeft.getVerticalBar();
		vLBar.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
				refreshMappingArea(mappingArea);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		// treeLeft.setSize(250, height);

		TreeColumn trclmnLeftName = new TreeColumn(treeLeft, SWT.LEFT_TO_RIGHT);
		trclmnLeftName.setWidth(165);
		trclmnLeftName.setText("Name");

		TreeColumn trclmnLeftType = new TreeColumn(treeLeft, SWT.LEFT_TO_RIGHT);
		trclmnLeftType.setWidth(100);
		trclmnLeftType.setText("Type");

		/*TreeColumn trclmnLeftTitle = new TreeColumn(treeLeft, SWT.LEFT_TO_RIGHT);
		trclmnLeftTitle.setWidth(100);
		trclmnLeftTitle.setText("Title");*/

		TreeColumn trclmnLeftDescription = new TreeColumn(treeLeft, SWT.LEFT_TO_RIGHT);
		trclmnLeftDescription.setWidth(150);
		trclmnLeftDescription.setText("Description");

		mappingArea = new Composite(composite_6, SWT.NONE);
		GridLayout gl_mappingArea = new GridLayout(3, false);
		gl_mappingArea.marginHeight = 0;
		gl_mappingArea.verticalSpacing = 0;
		mappingArea.setLayout(gl_mappingArea);
		mappingArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		gc = new GC(mappingArea);

		treeRight = new Tree(composite_6, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		GridData gd_treeRight = new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1);
		gd_treeRight.widthHint = 400;
		treeRight.setLayoutData(gd_treeRight);
		// treeRight.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		treeRight.setOrientation(SWT.LEFT_TO_RIGHT);
		treeRight.setHeaderVisible(true);
		treeRight.setLinesVisible(true);
		//treeRight.setFont(treeFont);
		CommonUtils.enableTreeSchemaEditor(treeRight, columnNameIndexMap);
		final ScrollBar vRBar=treeRight.getVerticalBar();
		vRBar.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
				refreshMappingArea(mappingArea);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		TreeColumn trclmnRightName = new TreeColumn(treeRight, SWT.LEFT_TO_RIGHT);
		trclmnRightName.setWidth(165);
		trclmnRightName.setText("Name");

		TreeColumn trclmnRightType = new TreeColumn(treeRight, SWT.LEFT_TO_RIGHT);
		trclmnRightType.setWidth(100);
		trclmnRightType.setText("Type");

		/*TreeColumn trclmnRightTitle = new TreeColumn(treeRight, SWT.LEFT_TO_RIGHT);
		trclmnRightTitle.setWidth(100);
		trclmnRightTitle.setText("Title");*/

		TreeColumn trclmnRightDescription = new TreeColumn(treeRight, SWT.LEFT_TO_RIGHT);
		trclmnRightDescription.setWidth(150);
		trclmnRightDescription.setText("Description");

		scrolledComposite.setContent(composite_6);
		scrolledComposite.setMinSize(composite_6.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		Composite composite_12 = new Composite(composite_5, SWT.NONE);
		GridData gd_composite_12 = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gd_composite_12.heightHint = 31;
		composite_12.setLayoutData(gd_composite_12);
		GridLayout gl_composite_12 = new GridLayout(3, false);
		gl_composite_12.verticalSpacing = 0;
		gl_composite_12.marginHeight = 0;
		composite_12.setLayout(gl_composite_12);

		Composite composite_3 = new Composite(composite_12, SWT.NONE);
		GridLayout gl_composite_3 = new GridLayout(1, false);
		gl_composite_3.marginHeight = 0;
		gl_composite_3.verticalSpacing = 0;
		composite_3.setLayout(gl_composite_3);
		GridData gd_composite_3 = new GridData(SWT.CENTER, SWT.FILL, false, false, 1, 1);
		gd_composite_3.widthHint = 385;
		composite_3.setLayoutData(gd_composite_3);

		Composite composite_13 = new Composite(composite_3, SWT.NONE);
		composite_13.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, true, 1, 1));
		composite_13.setSize(200, 31);
		composite_13.setLayout(new FillLayout(SWT.HORIZONTAL));

		Button btnRemoveItemLeft = new Button(composite_13, SWT.NONE);
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
					leftJsonSchema.setText(CommonUtils.generateJsonSchema(treeLeft, columnNameIndexMap));
					leftPayloadEditorGUI.loadSchema(leftJsonSchema.getText());
				} catch (Exception e2) {
					new ErrorDialogBox(shell, style).open(e2);
				}
			}
		});
		btnRemoveItemLeft.setText("Remove");

		Button btnLeftEditorUpdate = new Button(composite_13, SWT.ARROW | SWT.DOWN);
		btnLeftEditorUpdate.setToolTipText("Copy the schema from above to payload editor below.");
		btnLeftEditorUpdate.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					leftJsonSchema.setText(CommonUtils.generateJsonSchema(treeLeft, columnNameIndexMap));
					leftPayloadEditorGUI.loadSchema(leftJsonSchema.getText());
				} catch (Exception e2) {
					new ErrorDialogBox(shell, style).open(e2);
				}
			}
		});
		btnLeftEditorUpdate.setText("Update");

		Button btnArraow = new Button(composite_13, SWT.ARROW);
		btnArraow.setToolTipText("Copy the schema from below to the request schema builder above.");
		btnArraow.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					String[] text = importJsonLeft.generatePayload(leftPayloadEditorGUI.getSchema());
					leftJsonSchema.setText(leftPayloadEditorGUI.getSchema());
					leftJsonPayload.setText(leftPayloadEditorGUI.getJsonPayload());
					Map<String, Object> map = importJsonLeft.schemaMap;
					// tree.clearAll(false);
					treeLeft.removeAll();
					loadSchemaEditor(map, treeLeft);
				} catch (Exception e1) {
					if (leftJsonSchema.getText() == null || leftJsonSchema.getText().length() == 0) {
						new InfoDialogBox(parent.getShell(), getStyle()).open("Json schema not loaded");
					} else
						new ErrorDialogBox(parent.getShell(), getStyle()).open(e1);
				}
			}
		});
		btnArraow.setText("Arraow");

		Button btnAddItemLeft = new Button(composite_13, SWT.NONE);
		btnAddItemLeft.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					if (treeLeft.getSelection().length <= 0) {
						TreeItem rootItem = CommonUtils.generateAPIDefaults(treeLeft, "Payload", "RequestPayload",
								columnNameIndexMap);
						TreeItem item = new TreeItem(rootItem, SWT.FULL_SELECTION);
						item.setText(nameIndex, "rootElement");
						item.setText(typeIndex, "object");
					} else {
						final TreeItem selItem = treeLeft.getSelection()[0];
						String type = selItem.getText(typeIndex);
						if (type.equalsIgnoreCase("array<object>") || type.equalsIgnoreCase("array<array>")
								|| type.equalsIgnoreCase("object")) {
							TreeItem item = new TreeItem(selItem, SWT.FULL_SELECTION);
							item.setText(nameIndex, "newElement");
							item.setText(typeIndex, "string");
						} else {
							TreeItem item = null;
							if (selItem.getParentItem() != null)
								item = new TreeItem(selItem.getParentItem(), SWT.FULL_SELECTION);
							else
								item = new TreeItem(treeLeft, SWT.FULL_SELECTION);
							item.setText(nameIndex, "newElement");
							item.setText(typeIndex, "string");
						}
					}
					leftJsonSchema.setText(CommonUtils.generateJsonSchema(treeLeft, columnNameIndexMap));
					leftPayloadEditorGUI.loadSchema(leftJsonSchema.getText());
				} catch (Exception e2) {
					new ErrorDialogBox(shell, style).open(e2);
				}

			}
		});
		btnAddItemLeft.setText("Add");

		Composite composite_14 = new Composite(composite_12, SWT.NONE);
		GridLayout gl_composite_14 = new GridLayout(1, false);
		gl_composite_14.marginHeight = 0;
		gl_composite_14.verticalSpacing = 0;
		composite_14.setLayout(gl_composite_14);
		GridData gd_composite_14 = new GridData(SWT.CENTER, SWT.FILL, true, true, 1, 1);
		gd_composite_14.widthHint = 950;
		composite_14.setLayoutData(gd_composite_14);

		Composite composite = new Composite(composite_14, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, true, 1, 1));
		GridLayout gl_composite = new GridLayout(8, true);
		gl_composite.verticalSpacing = 0;
		gl_composite.marginHeight = 0;
		composite.setLayout(gl_composite);

		Button btnOpen = new Button(composite, SWT.NONE);
		btnOpen.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					String filePath = CommonUtils.openFileDialog(shell);
					if (filePath == null)
						return;
					jsonPatchAddList.clear();
					jsonPatchCopyList.clear();
					treeLeft.removeAll();
					treeRight.removeAll();
					ObjectMapper om = new ObjectMapper();
					JsonNode jnTransformer = om.readTree(new File(filePath));
					String requestSchema = jnTransformer.at("/transformer/request").toPrettyString();
					String[] text = importJsonLeft.generatePayload(requestSchema);
					leftJsonSchema.setText(text[0]);
					leftJsonPayload.setText(text[1]);
					Map<String, Object> map = importJsonLeft.schemaMap;
					treeLeft.removeAll();
					loadSchemaEditor(map, treeLeft);

					String responseSchema = jnTransformer.at("/transformer/response").toPrettyString();
					text = importJsonRight.generatePayload(responseSchema);
					rightJsonSchema.setText(text[0]);
					rightJsonPayload.setText(text[1]);
					map = importJsonRight.schemaMap;
					treeRight.removeAll();
					loadSchemaEditor(map, treeRight);
					JsonNode JsonPatch=jnTransformer.at("/transformer/JsonPatch");
					for (JsonNode jsonNode : JsonPatch) {
					 String op=jsonNode.get("op").textValue();
					 if("copy".equalsIgnoreCase(op)) {
						 JPOP jpop=om.readValue(jsonNode.toPrettyString(), new TypeReference<JPOP>() {});
						 jpop.setCondition(CommonUtils.decodeBase64(jpop.getCondition()));
							jpop.setComment(CommonUtils.decodeBase64(jpop.getComment()));
							jpop.setFunction(CommonUtils.decodeBase64(jpop.getFunction()));
							jpop.setValue(CommonUtils.decodeBase64(jpop.getValue()));
							JsonPatchMap jpm = new JsonPatchMap(jpop, treeLeft, treeRight);
							jsonPatchCopyList.add(jpm);
					 }else if("add".equalsIgnoreCase(op)) {
						 JPOP jpop=om.readValue(jsonNode.toPrettyString(), new TypeReference<JPOP>() {});
						 jpop.setCondition(CommonUtils.decodeBase64(jpop.getCondition()));
							jpop.setComment(CommonUtils.decodeBase64(jpop.getComment()));
							jpop.setFunction(CommonUtils.decodeBase64(jpop.getFunction()));
							jpop.setValue(CommonUtils.decodeBase64(jpop.getValue()));
							JsonPatchMap jpm = new JsonPatchMap(jpop, treeLeft, treeRight);
							jsonPatchAddList.add(jpm);
					 }
					}/*
					JsonNode jnCopyOps = jnTransformer.at("/transformer/copyOps");
					if (JsonPatch.toPrettyString().trim().length() > 0) {
						JPOP copyOp = om.readValue(jnTransformer.at("/transformer/copyOps").toPrettyString(),
								new TypeReference<List<JPOP>>() {
								});
						if (copyOps != null)
							for (JPOP jpop : copyOps) {
								jpop.setCondition(CommonUtils.decodeBase64(jpop.getCondition()));
								jpop.setComment(CommonUtils.decodeBase64(jpop.getComment()));
								jpop.setFunction(CommonUtils.decodeBase64(jpop.getFunction()));
								jpop.setValue(CommonUtils.decodeBase64(jpop.getValue()));
								JsonPatchMap jpm = new JsonPatchMap(jpop, treeLeft, treeRight);
								jsonPatchCopyList.add(jpm);
							}
					}
					
					
					// System.out.println(jnCopyOps.toPrettyString());
					if (jnCopyOps.toPrettyString().trim().length() > 0) {
						List<JPOP> copyOps = om.readValue(jnTransformer.at("/transformer/copyOps").toPrettyString(),
								new TypeReference<List<JPOP>>() {
								});
						if (copyOps != null)
							for (JPOP jpop : copyOps) {
								jpop.setCondition(CommonUtils.decodeBase64(jpop.getCondition()));
								jpop.setComment(CommonUtils.decodeBase64(jpop.getComment()));
								jpop.setFunction(CommonUtils.decodeBase64(jpop.getFunction()));
								jpop.setValue(CommonUtils.decodeBase64(jpop.getValue()));
								JsonPatchMap jpm = new JsonPatchMap(jpop, treeLeft, treeRight);
								jsonPatchCopyList.add(jpm);
							}
					}
					JsonNode jnAddOps = jnTransformer.at("/transformer/addOps");
					// System.out.println("Add ops -------------------------------");
					// System.out.println(jnAddOps.toPrettyString());
					if (jnAddOps.toPrettyString().trim().length() > 0) {
						List<JPOP> addOps = om.readValue(jnAddOps.toPrettyString(), new TypeReference<List<JPOP>>() {
						});
						if (addOps != null)
							for (JPOP jpop : addOps) {
								jpop.setCondition(CommonUtils.decodeBase64(jpop.getCondition()));
								jpop.setComment(CommonUtils.decodeBase64(jpop.getComment()));
								jpop.setFunction(CommonUtils.decodeBase64(jpop.getFunction()));
								jpop.setValue(CommonUtils.decodeBase64(jpop.getValue()));
								JsonPatchMap jpm = new JsonPatchMap(jpop, treeLeft, treeRight);
								jsonPatchAddList.add(jpm);
							}
					}*/
					refreshMappingArea(mappingArea);
				} catch (Exception er) {
					new ErrorDialogBox(shell, shell.getStyle()).open(er);
				}
			}
		});
		btnOpen.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		btnOpen.setText("Open");

		Button btnSave = new Button(composite, SWT.NONE);
		btnSave.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					String filePath = CommonUtils.openFileDialog(getShell());
					String root = "{\"transformer\":{";
					String schema = "\"request\":" + CommonUtils.generateJsonSchema(treeLeft, columnNameIndexMap)
							+ ",\"response\":" + CommonUtils.generateJsonSchema(treeRight, columnNameIndexMap);
					root += schema;
					String Ops=",\"JsonPatch\":[";
					if (jsonPatchCopyList.size() > 0) {
						//String Ops = ",\"copyOps\":[";
						for (JsonPatchMap jpMap : jsonPatchCopyList) {
							String patch = jpMap.getJsonPatchOP().toSaveString().replace("\n", "").replace("\r", "");
							Ops += patch + ",";
						}
						//Ops += "]";
						//Ops = Ops.replace("},]", "}]");
						//root += Ops;
					}

					if (jsonPatchAddList.size() > 0) {
						//String Ops = ",\"addOps\":[";
						for (JsonPatchMap jpMap : jsonPatchAddList) {
							String patch = jpMap.getJsonPatchOP().toSaveString().replace("\n", "").replace("\r", "");
							Ops += patch + ",";
						}
						
					}
					Ops += "]";
					Ops = Ops.replace("},]", "}]");
					root += Ops;
					root += "}}";
					File file = new File(filePath);
					FileOutputStream fos = new FileOutputStream(file);
					fos.write(root.getBytes());
					fos.flush();
					fos.close();
				} catch (Exception er) {
					new ErrorDialogBox(shell, shell.getStyle()).open(er);
				}
			}
		});
		btnSave.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		btnSave.setText("Save");

		Button testJsonPatchButton = new Button(composite, SWT.NONE);
		testJsonPatchButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		testJsonPatchButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				//refreshMappingArea(mappingArea);
				long st=System.currentTimeMillis();
				ObjectMapper objectMapper = new ObjectMapper();
				try {
					rightJsonPayload.setText(CommonUtils.generateJsonPayload(treeRight, columnNameIndexMap));
					JsonNode jnRight = objectMapper.readTree(rightJsonPayload.getText());
//					EmptyDialogBox edb = new EmptyDialogBox(shell, style);
//					JPayloadEditorGUI jpeGUI = new JPayloadEditorGUI(edb.composite, style);
//					jpeGUI.loadSchema(CommonUtils.generateJsonSchema(treeLeft, columnNameIndexMap));
//					

					JsonNode jnLeft = objectMapper.readTree(leftPayloadEditorGUI.getJsonPayload());
					String jPatchAdd = "[{\"op\":\"add\",\"path\":\"/Payload/RequestPayload\",\"value\":"
							+ jnLeft.at("/Payload/RequestPayload").toPrettyString() + "}]";
					String jPatchRemove = "[{\"op\":\"remove\",\"path\":\"/Payload/RequestPayload\"}]";
					// merge right left trees
					//System.out.println(jPatchAdd);
					//System.out.println(jnRight.toPrettyString());
					JsonNode target = JsonPatch.apply(objectMapper.readTree(jPatchAdd), jnRight);
					// apply all copy patches
					/*for (JsonPatchMap jpMap : jsonPatchCopyList) {
						System.out.println("Applying patch to " + jpMap.getJsonPatchOP().getOp());
						target = JsonPatcher.apply(jpMap.getJsonPatchOP(), target);
					}

					for (JsonPatchMap jpMap : jsonPatchAddList) {
						System.out.println("Applying patch to " + jpMap.getJsonPatchOP().getOp());
						target = JsonPatcher.apply(jpMap.getJsonPatchOP(), target);
					}*/
					
					target=CommonUtils.applyPatch(jsonPatch.getText(), target);

					target = JsonPatch.apply(objectMapper.readTree(jPatchRemove), target);
					rightJsonPayload.setText(target.toPrettyString());
					rightPayloadEditorGUI.loadPayload(rightJsonPayload.getText());
				} catch (Exception er) {
					new ErrorDialogBox(shell, shell.getStyle()).open(er);
				}
				System.out.println("Total time taken is "+(System.currentTimeMillis()-st)+"ms");
			}
		});
		testJsonPatchButton.setText("Test");

		Button addFunctionButton = new Button(composite, SWT.NONE);
		addFunctionButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		addFunctionButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addInvoke(mappingArea);
				refreshMappingArea(mappingArea);
				mappingArea.layout();
			}
		});
		addFunctionButton.setText("Add Service");

		Button btnMap = new Button(composite, SWT.NONE);
		btnMap.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		btnMap.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (itemRightSelected == null || itemLeftSelected == null)
					return;
				try {
					JsonPatchMap jpMap = findMapping(itemLeftSelected, itemRightSelected, jsonPatchCopyList);
					if (jpMap == null && !itemRightSelected.getText(1).equals(itemLeftSelected.getText(1)))
						new PromptDialogBox(getShell(), style)
								.open("Please ensure to write type casting function on the mapping.");
					EmptyDialogBox edb = new EmptyDialogBox(shell, style);
					MapStepGUI msGUI = new MapStepGUI(edb.composite, style);
					final JsonPatchMap temp = jpMap;
					Runnable init = new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							if (temp != null) {
								msGUI.textComment.setText(temp.getJsonPatchOP().getComment());
								msGUI.textCondition.setText(temp.getJsonPatchOP().getCondition());
								msGUI.textFunction.setText(temp.getJsonPatchOP().getFunction());
								
								msGUI.txtOrder.setText(temp.getJsonPatchOP().getOrder()==null?"":temp.getJsonPatchOP().getOrder()+"");
								msGUI.txtName.setText(temp.getJsonPatchOP().getName());
								msGUI.txtNested.setText(temp.getJsonPatchOP().getFollow());
							}
						}
					};
					final Map<String, String> map = new HashMap<String, String>();
					Runnable runOnOk = new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							map.put("comment", msGUI.textComment.getText());
							map.put("condition", msGUI.textCondition.getText());
							map.put("function", msGUI.textFunction.getText());
							map.put("delete", msGUI.delete + "");
							map.put("order", msGUI.txtOrder.getText());
							map.put("name", msGUI.txtName.getText());
							map.put("follow", msGUI.txtNested.getText());
						}
					};
					Runnable runOnCancel=new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							map.put("cancel","cancel");
						}
					};
					edb.open(msGUI, runOnOk, runOnCancel, init);
					if("cancel".equalsIgnoreCase(map.get("cancel")))
						return ;
					String comment = map.get("comment");
					String condition = map.get("condition");
					String function = map.get("function");
					String order = map.get("order");
					String name = map.get("name");
					String follow = map.get("follow");
					
					boolean delete = "true".equalsIgnoreCase(map.get("delete"));
					System.out.println(map.get("delete") + "===================================");
					if (!delete) {
						System.out.println("Connected from: " + itemLeftSelected.getBounds(0).y + " to "
								+ itemRightSelected.getBounds(0).y);
						System.out.println(itemLeftSelected.getText() + " to " + itemRightSelected.getText());
						if(jpMap==null) {
							jpMap = new JsonPatchMap(itemLeftSelected, itemRightSelected, "copy");
							jsonPatchCopyList.add(jpMap);
						}
						jpMap.getJsonPatchOP().setComment(comment);
						jpMap.getJsonPatchOP().setCondition(condition);
						jpMap.getJsonPatchOP().setFunction(function);
						if(order!=null && order.trim().length()>0) try {
							jpMap.getJsonPatchOP().setOrder(Integer.parseInt(order));
						}catch (Exception e2) {
							System.out.println("Map order must be integer vaalue");
							e2.printStackTrace();
						}
						jpMap.getJsonPatchOP().setName(name);
						jpMap.getJsonPatchOP().setFollow(follow);
						// drawMap(jpMap, mappingArea);
					} else if (jpMap != null) {
						jsonPatchCopyList.remove(jpMap);
					}
					refreshMappingArea(mappingArea);
				} catch (Exception er) {
					new ErrorDialogBox(shell, shell.getStyle()).open(er);
				}
			}
		});

		btnMap.setText("Map");

		Button btnSetVal = new Button(composite, SWT.NONE);
		btnSetVal.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		btnSetVal.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					String val = null;
					String xPath = "";
					JsonPatchMap jpm = null;
					String payload = null;
					String schemaPath = null;
					String targetJson = null;
					System.out.println("--------------------------------------------------");
					System.out.println(itemLastSelected);
					boolean eligible = CommonUtils.isEligible(itemLastSelected, columnNameIndexMap);
					System.out.println("============================"+eligible);
					if(!eligible) {
						new PromptDialogBox(getShell(), getStyle()).open("Can not set the values for array childs. Please set the value of top most array");
						return ;
					}
					List<JsonPatchMap> jpml = findMapping(itemLastSelected, jsonPatchAddList);

					if (jpml != null && jpml.size() > 0) {
						jpm = jpml.get(0);
						if (eligible && !(itemLastSelected.getText(1).contains("object")
								|| itemLastSelected.getText(1).contains("array<"))) {
							CustomDialogBox cdb = new CustomDialogBox(parent.getShell(), SWT.NONE);
							val = cdb.open(
									jpm.getJsonPatchOP().getValue() == null ? "" : jpm.getJsonPatchOP().getValue());
							if (val != null && val.trim().length() > 4)
								jpm.getJsonPatchOP().setValue(val);
							else
								jsonPatchAddList.remove(jpm);
							return;
						}
					}

					if (eligible && !(itemLastSelected.getText(1).contains("object")
							|| itemLastSelected.getText(1).contains("array<"))) {
						jpm = new JsonPatchMap(null, itemLastSelected, "add");
						CustomDialogBox cdb = new CustomDialogBox(parent.getShell(), SWT.NONE);
						val = cdb.open(jpm.getJsonPatchOP().getValue() == null ? "" : jpm.getJsonPatchOP().getValue());
						if (val != null && val.trim().length() > 0) {
							jpm.getJsonPatchOP().setValue(val);
							jsonPatchAddList.add(jpm);
						}
						return;
					}

					ObjectMapper om = new ObjectMapper();
					EmptyDialogBox edb = new EmptyDialogBox(shell, style);
					JPayloadEditorGUI jpeGUI = new JPayloadEditorGUI(edb.composite, style);
					final Map<String, String> map = new HashMap<String, String>();
					Runnable runOnOk = new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							try {
								String pl = jpeGUI.getJsonPayload();
								map.put("payload", pl);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						}
					};
					String fetchXPath = "/" + itemLastSelected.getText(0);

					if (jpm != null) {
						String value = jpm.getJsonPatchOP().getValue();
						payload = value;
//						if(itemLastSelected.getText(1).contains("array<object>")) {
//							payload="{\""+itemLastSelected.getText(0)+"\":"+ value+"}";
//						}
//						else if(itemLastSelected.getText(1).contains("array<")) {
//							payload="{\""+itemLastSelected.getText(0)+"\":"+ value+"}";
//						}else {
//							payload="{\""+itemLastSelected.getText(0)+"\":"+ value+"}";
//							fetchXPath=null;
//						}
						System.out.println("Payload:" + payload);
						if (value != null && value.trim().length() > 4) {
							jpeGUI.loadPayload(payload);
							edb.open(jpeGUI, runOnOk, null, null);
						} else {
							jsonPatchAddList.remove(jpm);
						}
					} else {
						jpm = new JsonPatchMap(null, itemLastSelected, "add");
						xPath = jpm.getJsonPatchOP().getPath();
						if (itemLastSelected == itemLeftSelected) {
							targetJson = CommonUtils.generateJsonSchema(treeLeft, columnNameIndexMap);
						}
						if (itemLastSelected == itemRightSelected) {
							targetJson = CommonUtils.generateJsonSchema(treeRight, columnNameIndexMap);
						}
						schemaPath = CommonUtils.getSchemaXPath(itemLastSelected, 0, 1);
						System.out.println("schemaPath:\n" + schemaPath);
						JsonNode jnTarget = om.readTree(targetJson).at(schemaPath);
						String schemaJson = jnTarget.toPrettyString();

						if (itemLastSelected.getText(1).contains("array<object>")) {
							schemaJson = "{\"type\":\"object\",\"properties\":{\"" + itemLastSelected.getText(0) + "\":"
									+ schemaJson + "}}";
						} else if (itemLastSelected.getText(1).contains("array<")) {
							String subType = itemLastSelected.getText(1).replace("array<", "").replace(">", "");
							schemaJson = "{\"type\":\"object\",\"properties\":{\"" + itemLastSelected.getText(0)
									+ "\":{\"type\":\"array\",\"items\":[{\"type\":\"" + subType + "\"}]}}}";
						} else
							fetchXPath = null;
						System.out.println("Json schema-------------------------");
						System.out.println(schemaJson);
						jpeGUI.loadSchema(schemaJson);
						edb.open(jpeGUI, runOnOk, null, null);
						payload = map.get("payload");
						if (payload == null)
							return;
						jsonPatchAddList.add(jpm);
					}
					payload = map.get("payload");
					if (payload == null)
						return;

					JsonNode jnt = null;
					if (fetchXPath != null) {
						jnt = om.readTree(payload);
						payload = jnt.at(fetchXPath).toPrettyString();
					}
					// System.out.println("Payload:\n"+payload);
					if (payload != null && payload.trim().length() > 4)
						jpm.getJsonPatchOP().setValue(payload);
					else
						jsonPatchAddList.remove(jpm);
					// System.out.println("Add operation:\n"+jpm.getJsonPatchOP().toString());
					refreshMappingArea(mappingArea);
				} catch (Exception er) {
					new ErrorDialogBox(shell, shell.getStyle()).open(er);
				}
			}
		});
		btnSetVal.setText("Set Value");

		btnDropParam = new Button(composite, SWT.NONE);
		btnDropParam.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		btnDropParam.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				JsonPatchMap jpm = null;
				if (itemLastSelected == itemLeftSelected)
					jpm = new JsonPatchMap(itemLeftSelected, null, "remove");
				if (itemLastSelected == itemRightSelected)
					jpm = new JsonPatchMap(null, itemRightSelected, "remove");
				jsonPatchDropList.add(jpm);
				refreshMappingArea(mappingArea);
				Color color = itemLastSelected.getForeground();
				Color newColor = parent.getDisplay().getSystemColor(SWT.COLOR_GRAY);
				if (color.equals(newColor))
					itemLastSelected.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_BLACK));
				else
					itemLastSelected.setForeground(newColor);
				// itemLastSelected.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
				/*
				 * GC gc=new GC(itemLastSelected.); gc.setAdvanced(true); if (gc.getAdvanced())
				 * gc.setAlpha(128); Rectangle rect = event.getBounds(); Color foreground =
				 * gc.getForeground(); Color background = gc.getBackground();
				 * gc.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_RED));
				 * gc.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND
				 * )); gc.fillGradientRectangle(0, rect.y, shell.getSize().x, rect.height,
				 * false); // restore colors for subsequent drawing
				 * gc.setForeground(foreground); gc.setBackground(background);
				 */
			}
		});
		btnDropParam.setText("Drop Param");
		Button btnRefresh = new Button(composite, SWT.NONE);
		btnRefresh.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1));
		btnRefresh.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				refreshMappingArea(mappingArea);
			}
		});
		btnRefresh.setText("Refresh");

		Composite composite_7 = new Composite(composite_12, SWT.NONE);
		GridLayout gl_composite_7 = new GridLayout(1, false);
		gl_composite_7.marginHeight = 0;
		gl_composite_7.verticalSpacing = 0;
		composite_7.setLayout(gl_composite_7);
		GridData gd_composite_7 = new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1);
		gd_composite_7.widthHint = 385;
		composite_7.setLayoutData(gd_composite_7);

		Composite composite_15 = new Composite(composite_7, SWT.NONE);
		composite_15.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, true, 1, 1));
		composite_15.setSize(200, 31);
		composite_15.setLayout(new FillLayout(SWT.HORIZONTAL));

		Button btnAddRight = new Button(composite_15, SWT.NONE);
		btnAddRight.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					if (treeRight.getSelection().length <= 0) {
						TreeItem rootItem = CommonUtils.generateAPIDefaults(treeRight, "Payload", "ResponsePayload",
								columnNameIndexMap);
						TreeItem item = new TreeItem(rootItem, SWT.FULL_SELECTION);
						item.setText(nameIndex, "rootElement");
						item.setText(typeIndex, "object");
					} else {
						final TreeItem selItem = treeRight.getSelection()[0];
						String type = selItem.getText(typeIndex);
						if (type.equalsIgnoreCase("array<object>") || type.equalsIgnoreCase("array<array>")
								|| type.equalsIgnoreCase("object")) {
							TreeItem item = new TreeItem(selItem, SWT.FULL_SELECTION);
							item.setText(nameIndex, "newElement");
							item.setText(typeIndex, "string");
						} else {
							TreeItem item = null;
							if (selItem.getParentItem() != null)
								item = new TreeItem(selItem.getParentItem(), SWT.FULL_SELECTION);
							else
								item = new TreeItem(treeRight, SWT.FULL_SELECTION);
							item.setText(nameIndex, "newElement");
							item.setText(typeIndex, "string");
						}
					}
					rightJsonSchema.setText(CommonUtils.generateJsonSchema(treeRight, columnNameIndexMap));
					rightPayloadEditorGUI.loadSchema(rightJsonSchema.getText());
					rightJsonPayload.setText(importJsonRight.generatePayload(rightJsonSchema.getText())[1]);
				} catch (Exception e2) {
					new ErrorDialogBox(shell, style).open(e2);
				}
			}
		});
		btnAddRight.setText("Add");

		Button btnRightEditorUpdate = new Button(composite_15, SWT.ARROW | SWT.DOWN);
		btnRightEditorUpdate.setToolTipText("Copy the schema from above to payload editor below.");
		btnRightEditorUpdate.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					rightJsonSchema.setText(CommonUtils.generateJsonSchema(treeRight, columnNameIndexMap));
					rightPayloadEditorGUI.loadSchema(rightJsonSchema.getText());
					rightJsonPayload.setText(importJsonRight.generatePayload(rightJsonSchema.getText())[1]);
				} catch (Exception e2) {
					new ErrorDialogBox(shell, style).open(e2);
				}
			}
		});
		btnRightEditorUpdate.setText("Update");

		Button button = new Button(composite_15, SWT.ARROW);
		button.setToolTipText("Copy the schema from below to the response schema builder above.");

		Button btnRemoveRight = new Button(composite_15, SWT.NONE);
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
					rightJsonSchema.setText(CommonUtils.generateJsonSchema(treeRight, columnNameIndexMap));
					rightPayloadEditorGUI.loadSchema(rightJsonSchema.getText());
					rightJsonPayload.setText(importJsonRight.generatePayload(rightJsonSchema.getText())[1]);
				} catch (Exception e2) {
					new ErrorDialogBox(shell, style).open(e2);
				}

			}
		});
		btnRemoveRight.setText("Remove");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					String[] text = importJsonRight.generatePayload(rightPayloadEditorGUI.getSchema());
					rightJsonSchema.setText(rightPayloadEditorGUI.getSchema());
					rightJsonPayload.setText(rightPayloadEditorGUI.getJsonPayload());
					Map<String, Object> map = importJsonRight.schemaMap;
					// tree.clearAll(false);
					treeRight.removeAll();
					loadSchemaEditor(map, treeRight);
				} catch (Exception e1) {
					if (leftJsonSchema.getText() == null || leftJsonSchema.getText().length() == 0) {
						new InfoDialogBox(parent.getShell(), getStyle()).open("Json schema not loaded");
					} else
						new ErrorDialogBox(parent.getShell(), getStyle()).open(e1);
				}
			}
		});

		ScrolledComposite scrolledComposite_1 = new ScrolledComposite(composite_5,
				SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		GridData gd_scrolledComposite_1 = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_scrolledComposite_1.widthHint = 214;
		gd_scrolledComposite_1.heightHint = 283;
		scrolledComposite_1.setLayoutData(gd_scrolledComposite_1);
		scrolledComposite_1.setExpandHorizontal(true);
		scrolledComposite_1.setExpandVertical(true);

		Composite composite_1 = new Composite(scrolledComposite_1, SWT.NONE);
		composite_1.setLayout(new GridLayout(3, false));

		leftPayloadEditorGUI = new JPayloadEditorGUI(composite_1, SWT.NONE);
		GridData gd_leftPayloadEditorGUI = new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1);
		gd_leftPayloadEditorGUI.widthHint = 500;
		leftPayloadEditorGUI.setLayoutData(gd_leftPayloadEditorGUI);

		Composite composite_2 = new Composite(composite_1, SWT.NONE);
		composite_2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		composite_2.setLayout(new GridLayout(1, false));
		
		TabFolder tabFolder = new TabFolder(composite_2, SWT.NONE);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		TabItem tbtmJsonpatch = new TabItem(tabFolder, SWT.NONE);
		tbtmJsonpatch.setText("JsonPatch");
		
		Composite composite_10 = new Composite(tabFolder, SWT.NONE);
		tbtmJsonpatch.setControl(composite_10);
		composite_10.setLayout(new FillLayout(SWT.HORIZONTAL));

		jsonPatch = new Text(composite_10, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL | SWT.MULTI);
		jsonPatch.setEditable(true);
		
		TabItem tbtmTree = new TabItem(tabFolder, SWT.NONE);
		tbtmTree.setText("Tree");
		
		Composite composite_11 = new Composite(tabFolder, SWT.NONE);
		tbtmTree.setControl(composite_11);

		rightPayloadEditorGUI = new JPayloadEditorGUI(composite_1, SWT.NONE);
		GridData gd_rightPayloadEditorGUI = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
		gd_rightPayloadEditorGUI.widthHint = 500;
		rightPayloadEditorGUI.setLayoutData(gd_rightPayloadEditorGUI);
		GridLayout gl_rightPayloadEditorGUI = (GridLayout) rightPayloadEditorGUI.getLayout();
		scrolledComposite_1.setContent(composite_1);
		scrolledComposite_1.setMinSize(composite_1.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		Composite composite_9 = new Composite(composite_4, SWT.NONE);
		GridData gd_composite_9 = new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1);
		gd_composite_9.exclude = true;
		composite_9.setLayoutData(gd_composite_9);
		composite_9.setLayout(new GridLayout(1, true));

		SashForm sashForm_11 = new SashForm(composite_9, SWT.NONE);

		Button generateSchemaRight = new Button(sashForm_11, SWT.CENTER);
		generateSchemaRight.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
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
		generateSchemaRight.setText("Generate Schema");
		generateSchemaRight.setOrientation(SWT.RIGHT_TO_LEFT);
		sashForm_11.setWeights(new int[] { 1 });

		rightJsonSchema = new Text(composite_9, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL | SWT.MULTI);
		rightJsonSchema.setText(
				"{\r\n\"$schema\": \"http://json-schema.org/draft-04/schema#\",\r\n\"type\": \"object\", \r\n\"properties\": {\r\n\"Payload\":{\r\n\"type\":\"object\",\r\n\"properties\":{\r\n\"ResponsePayload\":{\r\n\"type\":\"object\",\r\n\"properties\":{\r\n\"Pallet\":{\r\n\"type\":\"array\",\r\n\"items\": {\r\n\"type\":\"object\",\r\n\"properties\":{\r\n\"Banner\":{\r\n\"type\":\"array\",\r\n\"items\": {\r\n\"type\":\"object\",\r\n\"properties\":{\r\n\"color\":{\r\n\"type\" : \"string\"\r\n}\r\n\r\n}\r\n}\r\n}\r\n\r\n}\r\n}\r\n}\r\n\r\n}\r\n}\r\n\r\n}\r\n}\r\n}}");
		GridData gd_rightJsonSchema = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_rightJsonSchema.heightHint = 172;
		rightJsonSchema.setLayoutData(gd_rightJsonSchema);

		SashForm sashForm_12 = new SashForm(composite_9, SWT.NONE);

		Button generatePayloadRight = new Button(sashForm_12, SWT.CENTER);
		generatePayloadRight.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
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
		generatePayloadRight.setText("Generate Payload");
		generatePayloadRight.setOrientation(SWT.RIGHT_TO_LEFT);
		sashForm_12.setWeights(new int[] { 1 });

		rightJsonPayload = new Text(composite_9, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL | SWT.MULTI);
		GridData gd_rightJsonPayload = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_rightJsonPayload.heightHint = 232;
		rightJsonPayload.setLayoutData(gd_rightJsonPayload);

		treeLeft.addListener(SWT.MouseUp, new Listener() {
			@Override
			public void handleEvent(Event event) {
				// TODO Auto-generated method stub
				Point point = new Point(event.x, event.y);
				itemLeftSelected = treeLeft.getItem(point);
				itemLastSelected = itemLeftSelected;
				List<JsonPatchMap> selections = findMapping(itemLeftSelected, jsonPatchCopyList);
				if (selections.size() > 0 && itemRightSelected != selections.get(0).itemRightSelected) {
					itemRightSelected = selections.get(0).itemRightSelected;
					treeRight.deselectAll();
					treeRight.select(itemRightSelected);
				}
				itemLastSelected = itemLeftSelected;
				refreshMappingArea(mappingArea);
			}
		});
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
					if (itemLastSelected != null) {
						Color color = itemLastSelected.getForeground();
						Color newColor = parent.getDisplay().getSystemColor(SWT.COLOR_GRAY);
						if (color.equals(newColor))
							btnDropParam.setText("Undo Drop");
						else
							btnDropParam.setText("Drop Param");
					}

				}
			}
		});

		treeRight.addListener(SWT.MouseUp, new Listener() {
			@Override
			public void handleEvent(Event event) {
				// TODO Auto-generated method stub
				Point point = new Point(event.x, event.y);
				itemRightSelected = treeRight.getItem(point);
				itemLastSelected = itemRightSelected;
				List<JsonPatchMap> selections = findMapping(itemRightSelected, jsonPatchCopyList);
				if (selections.size() > 0 && itemLeftSelected != selections.get(0).itemLeftSelected) {
					itemLeftSelected = selections.get(0).itemLeftSelected;
					treeLeft.deselectAll();
					treeLeft.select(itemLeftSelected);
				}
				itemLastSelected = itemRightSelected;
				refreshMappingArea(mappingArea);
			}
		});
		treeRight.addListener(SWT.EraseItem, new Listener() {
			public void handleEvent(Event event) {
				try {
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

					if (itemLastSelected != null) {
						Color color = itemLastSelected.getForeground();
						Color newColor = parent.getDisplay().getSystemColor(SWT.COLOR_GRAY);
						if (color.equals(newColor))
							btnDropParam.setText("Undo Drop");
						else
							btnDropParam.setText("Drop Param");
					}
				}
			} catch (Exception er) {
				new ErrorDialogBox(shell, shell.getStyle()).open(er);
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
						refreshMappingArea(mappingArea);

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
						refreshMappingArea(mappingArea);

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
						refreshMappingArea(mappingArea);
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
						refreshMappingArea(mappingArea);
					}
				});

			}
		});

		// gc.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_BLUE));

	}

	private void addInvoke(Composite mappingArea) {
		// Composite serviceInvoke =null;
		Label leftSpacer = new Label(mappingArea, SWT.NONE);
		leftSpacer.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1));

		Composite invokeWrapper = new Composite(mappingArea, SWT.BORDER);
		GridData gd_invokeWrapper = new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1);
		gd_invokeWrapper.widthHint = 620;
		gd_invokeWrapper.heightHint = 250;
		gd_invokeWrapper.verticalIndent=0;
		gd_invokeWrapper.verticalSpan=0;
		invokeWrapper.setLayoutData(gd_invokeWrapper);
		invokeWrapper.setLayout(new GridLayout(1, false));

		Composite composite_3 = new Composite(invokeWrapper, SWT.NONE);
		composite_3.setLayout(new GridLayout(4, false));
		GridData gd_composite_3 = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_composite_3.heightHint = 30;
		gd_composite_3.verticalIndent=0;
		gd_composite_3.verticalSpan=0;
		composite_3.setLayoutData(gd_composite_3);

		Button btnToggle = new Button(composite_3, SWT.ARROW | SWT.DOWN);
		btnToggle.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1));

		btnToggle.setText("Toggle");

		Label lblServiceName = new Label(composite_3, SWT.NONE);
		lblServiceName.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1));
		lblServiceName.setText("Service name");

		Button btnDelete = new Button(composite_3, SWT.NONE);
		btnDelete.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1));
		btnDelete.setText("Delete");
		final Composite serviceInvoke = new Composite(invokeWrapper, SWT.NONE);
		serviceInvoke.setLayout(new FillLayout(SWT.HORIZONTAL));
		GridData gd_serviceInvoke = new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1);
		gd_serviceInvoke.widthHint = 620;
		gd_serviceInvoke.verticalIndent=0;
		gd_serviceInvoke.verticalSpan=0;
		serviceInvoke.setLayoutData(gd_serviceInvoke);
		serviceInvoke.setBounds(0, 0, 64, 40);
		btnToggle.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (serviceInvoke.getVisible()) {
					serviceInvoke.setVisible(false);
					gd_invokeWrapper.heightHint = 40;
					gd_invokeWrapper.widthHint = 200;
				} else {
					gd_invokeWrapper.heightHint = 250;
					gd_invokeWrapper.widthHint = 620;
					serviceInvoke.setVisible(true);
				}
				invokeWrapper.layout();
				mappingArea.layout();
				serviceInvoke.getShell().layout();
				refreshMappingArea(mappingArea);
			}
		});

		InvokeGUI invokeGUI = new InvokeGUI(serviceInvoke, SWT.NONE);

		Label rightSpacer = new Label(mappingArea, SWT.NONE);
		rightSpacer.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1));

		btnDelete.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// deleteServiceMap(mappingArea, invokeWrapper);
				leftSpacer.dispose();
				rightSpacer.dispose();
				invokeWrapper.dispose();
				// invokeWrapper.layout();
				// mappingArea.pack();
				mappingArea.layout();
				mappingArea.getShell().layout();
				// serviceInvoke.getShell().layout();
				refreshMappingArea(mappingArea);
			}
		});		
	}

	private void deleteServiceMap(Composite mappingArea, Composite invokeWrapper) {
		invokeWrapper.dispose();
		refreshMappingArea(mappingArea);
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
		Color color = gc.getForeground();

		if ((op.getJsonPatchOP().getCondition() != null && op.getJsonPatchOP().getCondition().trim().length() > 0) || (op.getJsonPatchOP().getFunction() != null && op.getJsonPatchOP().getFunction().trim().length() > 0))
			gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_BLUE));
		if (op.itemLeftSelected == itemLeftSelected && op.itemRightSelected == itemRightSelected)
			gc.setLineWidth(2);
		gc.drawLine(0, ly + 32, mappingArea.getBounds().width, ry + 32);
		gc.setForeground(color);
		gc.setLineWidth(1);
		// System.out.println("repaint");
	}

	private void drawCircle(JsonPatchMap op, Composite mappingArea) {
		int y = 0;
		int x = mappingArea.getBounds().width - 10;
		TreeItem ti = op.itemRightSelected;
		y = op.itemRightSelected.getBounds(0).y;
		if (op.getJsonPatchOP().getPath().contains("Request"))
			x = 0;

		try {
			while (y <= 0) {
				ti = ti.getParentItem();
				y = ti.getBounds(0).y;
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		gc.drawOval(x, y + 32, 10, 10);
		gc.fillOval(x, y + 32, 10, 10);
		Color newColor = getDisplay().getSystemColor(SWT.COLOR_DARK_GREEN);
		gc.setForeground(newColor);
		// System.out.println("repaint");
	}

	private void loadSchemaEditor(Map<String, Object> map, Tree tree) throws Exception {
		Object[] keys = map.keySet().toArray();
		int itemCount = keys.length;
		for (int i = 0; i < itemCount; i++) {
			TreeItem item = new TreeItem(tree, SWT.FULL_SELECTION);
			item.setText(nameIndex, keys[i].toString());
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
		ti.setText(typeIndex, (map.get("type") + "").replace("null", ""));
//	    ti.setText(egIndex, (map.get("example")+"").replace("null", ""));
//	    ti.setText(formatIndex, (map.get("format")+"").replace("null", ""));
//	    ti.setText(enumIndex, (map.get("enum")+"").replace("null", ""));
//	    ti.setText(scopeIndex, (map.get("scope")+"").replace("null", ""));
//	    ti.setText(requiredIndex, (map.get("required")+"").replace("null", "")); 

		for (int i = 0; i < itemCount; i++) {
			if (map.get(keys[i]) instanceof Map) {
				TreeItem item = new TreeItem(ti, SWT.FULL_SELECTION);
				item.setText(nameIndex, keys[i].toString());// +"<"+(map.get("type")+"").replace("null", "")+">");
				loadSchemaEditor((Map) map.get(keys[i]), item);
			}
		}
	}

	private void enableEditor1(Tree tree) {
		final TreeEditor editor = new TreeEditor(tree);
		editor.horizontalAlignment = SWT.LEFT;
		editor.grabHorizontal = true;
		tree.addListener(SWT.MouseDoubleClick, new Listener() {
			public void handleEvent(Event event) {
				try {
					if (tree.getItemCount() <= 0)
						return;
					final TreeItem item = tree.getSelection()[0];
					int colIndex = 0;
					// System.out.println(event.getBounds().x+","+event.getBounds().y);
					for (int i = 0; i < tree.getColumnCount(); i++) {
						// System.out.println(i);
						if (item.getBounds(i).contains(event.getBounds().x, event.getBounds().y))
							colIndex = i;
					}
					final int colInd = colIndex;
					if (colInd == typeIndex) {
						Combo typeCombo = new Combo(tree, SWT.READ_ONLY);
						if (item.getText(colIndex).toLowerCase().contains("array")) {
							typeCombo.setItems(new String[] { "array<string>", "array<number>", "array<boolean>",
									"array<integer>", "array<object>", "object" });
						} else if (item.getItemCount() == 0)
							typeCombo.setItems(new String[] { "string", "number", "boolean", "integer", "object",
									"array<object>"});
						else
							typeCombo.setItems(new String[] { "object", "array<object>" });
						// final Text text = new Text(tree, SWT.NONE);
						typeCombo.setText(item.getText(colIndex).toLowerCase());
						// text.selectAll();
						typeCombo.setFocus();
						typeCombo.addFocusListener(new FocusAdapter() {
							public void focusLost(FocusEvent event) {
								if (!item.getText(colInd).equalsIgnoreCase(typeCombo.getText())) {
									// item.setText(formatIndex,"");
									item.setText(colInd, typeCombo.getText());
									// item.setText(scopeIndex,"");
								}
								typeCombo.dispose();
							}
						});

						typeCombo.addKeyListener(new KeyAdapter() {
							public void keyPressed(KeyEvent event) {
								switch (event.keyCode) {
								case SWT.CR:
									item.setText(colInd, typeCombo.getText().toLowerCase());
								case SWT.ESC:
									typeCombo.dispose();
									break;
								}
							}
						});
						editor.setEditor(typeCombo, item, colInd);
					} else {
						final Text text = new Text(tree, SWT.NONE);
						text.setText(item.getText(colIndex));
						text.selectAll();
						text.setFocus();

						text.addFocusListener(new FocusAdapter() {
							public void focusLost(FocusEvent event) {
								if (colInd == nameIndex) {

								}
								item.setText(colInd, text.getText());
								text.dispose();
							}
						});

						text.addKeyListener(new KeyAdapter() {
							public void keyPressed(KeyEvent event) {
								switch (event.keyCode) {
								case SWT.CR:

									item.setText(colInd, text.getText());
									text.dispose();
								case SWT.ESC:
									text.dispose();
									break;
								}
							}
						});

						editor.setEditor(text, item, colInd);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		});
		Font treeFont = new Font(tree.getDisplay(), new FontData("Segoe UI", 8, SWT.NORMAL));
		tree.setFont(treeFont);
	}

	private void removeElement(TreeItem item, Composite mappingArea) {
		List<JsonPatchMap> removeList = new ArrayList<JsonPatchMap>();
		for (JsonPatchMap jsonPatchOP : jsonPatchCopyList) {
			if (jsonPatchOP.itemLeftSelected == item || jsonPatchOP.itemRightSelected == item)
				removeList.add(jsonPatchOP);
		}
		if (removeList.size() > 0) {
			jsonPatchCopyList.removeAll(removeList);
			redrawComposite(mappingArea);
			for (JsonPatchMap jpOPMap : jsonPatchCopyList) {
				drawMap(jpOPMap, mappingArea);
			}
		}
	}

	private List<JsonPatchMap> findMapping(TreeItem item, List<JsonPatchMap> list) {
		List<JsonPatchMap> jpmList = new ArrayList<JsonPatchMap>();
		for (JsonPatchMap jsonPatchMap : list) {
			if (jsonPatchMap.itemRightSelected == item || jsonPatchMap.itemLeftSelected == item) {
				jpmList.add(jsonPatchMap);
			}
		}
		return jpmList;
	}

	private JsonPatchMap findMapping(TreeItem itemLeft, TreeItem itemRight, List<JsonPatchMap> list) {
		for (JsonPatchMap jsonPatchMap : list) {
			if (jsonPatchMap.itemRightSelected == itemRight && jsonPatchMap.itemLeftSelected == itemLeft) {
				return jsonPatchMap;
			}
		}
		return null;
	}

	private void refreshMappingArea(Composite mappingArea){
		try {
		redrawComposite(mappingArea);
		mappingArea.layout();
		String jPatch = "[";
		for (JsonPatchMap jpMap : jsonPatchCopyList) {
			drawMap(jpMap, mappingArea);
			String patch = jpMap.getJsonPatchOP().toString();
			jPatch += "\n"+patch + ",";
		}
		for (JsonPatchMap jpMap : jsonPatchAddList) {
			drawCircle(jpMap, mappingArea);
			String patch = jpMap.getJsonPatchOP().toString();
			jPatch += "\n"+patch + ",";
		}
		if(jPatch.endsWith(",")) {
			jPatch=jPatch.substring(0, jPatch.length()-1);
		}
		jPatch += "\n]";
		jsonPatch.setText(jPatch);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
}
