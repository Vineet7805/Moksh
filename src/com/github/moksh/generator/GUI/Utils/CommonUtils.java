package com.github.moksh.generator.GUI.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.moksh.generator.GUI.ErrorDialogBox;
import com.github.moksh.generator.core.ClassMetaData;
import com.github.moksh.generator.core.ClassMetaData.API;
import com.github.moksh.generator.core.ClassMetaData.Function;
import com.github.moksh.generator.core.ClassMetaData.Property;
import com.github.moksh.generator.core.CodeGen;
import com.github.moksh.generator.core.ImportJSON;
import com.github.moksh.generator.core.JPOP;
import com.github.moksh.generator.core.JsonPatcher;

public class CommonUtils {
	private static int MAX_ELEMENTS=-5;
	public static void enableTreeSchemaEditor(Tree tree, Map<String, Integer> columnNameIndexMap) {
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);
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
				if (colInd == columnNameIndexMap.get("Type")) {
					Combo typeCombo = new Combo(tree, SWT.READ_ONLY);
					if(item.getText(colIndex).toLowerCase().contains("array") && item.getItemCount()==0) {
						typeCombo.setItems(new String[] { "array<string>", "array<number>", "array<boolean>",
								"array<integer>", "array<object>", "object" });
					} else if (item.getItemCount() == 0)
						typeCombo.setItems(
								new String[] { "string", "number", "boolean", "integer", "object", "array<object>" });
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
							if (colInd == columnNameIndexMap.get("Name")) {

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
				new ErrorDialogBox(tree.getShell(), tree.getShell().getStyle()).open(e);
			}
			}
			

		});
		Font treeFont = new Font(tree.getDisplay(), new FontData("Segoe UI", 8, SWT.NORMAL));
		tree.setFont(treeFont);
	}

	public static void createColumns(Tree tree, Map<String, Integer> columnNameIndexMap) {
		String keys[] = columnNameIndexMap.keySet().toArray(new String[columnNameIndexMap.keySet().size()]);
		Map<String, TreeColumn> treeColumnMap = new HashMap<String, TreeColumn>();
		for (int i = 0; i < keys.length; i++) {
			String clmName = getKeyByValue(columnNameIndexMap, i);
			TreeColumn trclmn = new TreeColumn(tree, SWT.LEFT_TO_RIGHT);
			trclmn.setWidth(165);
			trclmn.setText(clmName);
			treeColumnMap.put(clmName, trclmn);
		}
	}

	private static String getKeyByValue(Map<String, Integer> columnNameIndexMap, Integer index) {
		String keys[] = columnNameIndexMap.keySet().toArray(new String[columnNameIndexMap.keySet().size()]);
		// Map<String,TreeColumn> treeColumnMap=new HashMap<String, TreeColumn>();
		for (String key : keys) {
			if (index == columnNameIndexMap.get(key))
				return key;
		}
		return null;
	}

	public static void loadMokshSchema(String schema, Tree tree, Map<String, Integer> columnNameIndexMap) {
		try {
			ImportJSON importJson = ImportJSON.getInstance();
			String result[] = importJson.generatePayload(schema);
			schema = result[0];
			String payload = result[1];
			Map<String, Object> map = importJson.schemaMap;
			// tree.clearAll(false);
			tree.removeAll();
			loadSchemaEditor(map, tree, columnNameIndexMap);
		} catch (Exception e) {
			new ErrorDialogBox(tree.getShell(), tree.getShell().getStyle()).open(e);
		}
	}

	public static void addSchema(String schema, TreeItem treeItem, Map<String, Integer> columnNameIndexMap)
			throws Exception {
		//System.out.println("Add this schema---------------------------");
		//System.out.println(schema);
		ImportJSON importJson = ImportJSON.getInstance();
		String result[] = importJson.generatePayload(schema);
		schema = result[0];
		String payload = result[1];
		Map<String, Object> map = importJson.schemaMap;
		// tree.clearAll(false);
		TreeItem[] treeItems = treeItem.getItems();
		List<TreeItem> dispose = new ArrayList<TreeItem>();
		for (TreeItem item : treeItems) {
			if (!item.getText(columnNameIndexMap.get("Name")).contains("[")) {
				dispose.add(item);
			}
		}
		for (TreeItem item : dispose) {
			//System.out.println("Disposing:" + item.getText(0));

			item.removeAll();
			item.dispose();
		}
		// treeItem.removeAll();
		loadSchemaEditor(map, treeItem, columnNameIndexMap);
		// treeItem.
	}

	public static void loadMokshSchema(File file, Tree tree, Map<String, Integer> columnNameIndexMap) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode node = mapper.readTree(file);
			String schema = node.toPrettyString();
			loadMokshSchema(schema, tree, columnNameIndexMap);
		} catch (Exception e) {
			new ErrorDialogBox(tree.getShell(), tree.getShell().getStyle()).open(e);
		}
	}

	private static void loadSchemaEditor(Map<String, Object> map, TreeItem ti, Map<String, Integer> columnNameIndexMap)
			throws Exception {
		Object[] keys = map.keySet().toArray();
		int itemCount = keys.length;
		ti.setText(columnNameIndexMap.get("Type"), (map.get("type") + "").replace("null", ""));
		for (int i = 0; i < itemCount; i++) {
			if (map.get(keys[i]) instanceof Map) {
				TreeItem item = new TreeItem(ti, SWT.FULL_SELECTION);
				item.setText(columnNameIndexMap.get("Name"), keys[i].toString());// +"<"+(map.get("type")+"").replace("null",// "")+">");
				loadSchemaEditor((Map) map.get(keys[i]), item, columnNameIndexMap);
			}
		}
	}

	private static void loadSchemaEditor(Map<String, Object> map, Tree tree, Map<String, Integer> columnNameIndexMap)
			throws Exception {
		Object[] keys = map.keySet().toArray();
		int itemCount = keys.length;
		for (int i = 0; i < itemCount; i++) {
			TreeItem item = new TreeItem(tree, SWT.FULL_SELECTION);
			item.setText(columnNameIndexMap.get("Name"), keys[i].toString());
			// item.getCl
			if (map.get(keys[i]) instanceof Map)
				loadSchemaEditor((Map) map.get(keys[i]), item, columnNameIndexMap);
		}
	}

	public static void enableJsonEditor(Tree tree, String schema, Map<String, Integer> columnNameIndexMap,
			String payload, boolean isEditable) {
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);
		if(isEditable)
		tree.addListener(SWT.MouseDoubleClick, new Listener() {
			public void handleEvent(Event event) {
				if(tree.getSelection().length==0)
					return;
				final TreeItem ti = tree.getSelection()[0];
				if ((ti.getText(columnNameIndexMap.get("Type")).contains("array<") || ti.getText(columnNameIndexMap.get("Type")).contains("object"))) {
					return;
				}
				if(!isEligible(ti, columnNameIndexMap)) {
					return;
				}
				final TreeEditor editor = new TreeEditor(tree);
				editor.horizontalAlignment = SWT.LEFT;
				editor.grabHorizontal = true;
				// System.out.println("Double clicked...............................and is
				// eligilbe:" + isEligible);// +":"+isEligible(treeItem,
				
					// System.out.println("Double clicked...............................");
					final Text text = new Text(tree, SWT.NONE);
					int colInd = columnNameIndexMap.get("Value");
					text.setText(ti.getText(colInd));
					text.addKeyListener(new KeyListener() {
						
						@Override
						public void keyReleased(KeyEvent arg0) {
							// TODO Auto-generated method stub
							ti.setText(colInd,text.getText());
						}
						
						@Override
						public void keyPressed(KeyEvent arg0) {
							// TODO Auto-generated method stub
							
						}
					});
					editor.setEditor(text, ti, colInd);
					ti.addListener(SWT.Dispose, new Listener() {
						public void handleEvent(Event event) {
							
							ti.removeListener(SWT.Dispose, this);
							editor.dispose();
						}
					});
			}
		});
		if(isEditable)
			enableJsonEditor(tree, null, schema, columnNameIndexMap, payload,isEditable,schema);
		else
			loadJsonData(tree, null, schema, columnNameIndexMap, payload,isEditable,schema);
	}

	public static String getXPathForJson(TreeItem item, int nameIndex) {
		String xPath = ("/" + item.getText(nameIndex));
		if (item.getText(nameIndex).contains("["))
			xPath = xPath.replace(item.getText(nameIndex).split(Pattern.quote("["))[0], "").replace("/[", "/")
					.replace("]", "");
		try {
			while (item != null) {
				item = item.getParentItem();
				String xP = "/" + item.getText(nameIndex);
				//System.out.println("xP-------------------------");
				//System.out.println(xP);
				//System.out.println("---------------------------");
				if (item.getText(nameIndex).contains("[")) {
					xP = xP.replace(item.getText(nameIndex).split(Pattern.quote("["))[0], "").replace("/[", "/");
				}
				xPath = xP + xPath;
			}
		} catch (Exception e) {
			// e.printStackTrace();
		}
		return xPath.replace("]/", "/");
	}
	
	public static String getXPath(TreeItem item,int nameIndex,int typeIndex) {
		String xPath = getXPath(item, nameIndex, typeIndex, true);
		return xPath;
	}
	
	private static String getXPath(TreeItem item,int nameIndex,int typeIndex, boolean isLeaf) {
		String xPath = "/" + item.getText(nameIndex);
		if (item.getText(typeIndex).contains("array<")) {
			if(isLeaf) {
				xPath = xPath+"/*";
				isLeaf=false;
			}else
				xPath = xPath+"/0";
		}
		try {
			TreeItem itemP = item.getParentItem();
			String xP = getXPath(itemP,nameIndex,typeIndex,isLeaf);// + xPath;
			xPath=xP + xPath;
		} catch (Exception e) {
			// TODO: handle exception
		}
		return xPath;
	}

	public static String getSchemaXPath(TreeItem item,int nameIndex,int typeIndex) {
		String xPath = "/" + item.getText(nameIndex);
		//System.out.println("Name: "+xPath);
		try {
			TreeItem itemP = item.getParentItem();
			if (item.getText(typeIndex).contains("array<") || item.getText(typeIndex).equals("object"))
				xPath = "/properties" + xPath;
			if (itemP.getText(typeIndex).contains("array<"))
				xPath = "/items" + xPath;
			xPath = getSchemaXPath(itemP,nameIndex,typeIndex) + xPath;
		} catch (Exception e) {
			// TODO: handle exception
		}
		//System.out.println("Before modifying: "+xPath);
		if(item.getText(nameIndex).contains("[")) {
			xPath=xPath.replace("/properties/"+item.getText(nameIndex), "");
		}
		//System.out.println(xPath);
		return xPath;
	}

	public static boolean isEligible(TreeItem treeItem, Map<String, Integer> columnNameIndexMap) {
		if (treeItem.getText(columnNameIndexMap.get("Name")).contains("[")
				&& !(treeItem.getText(columnNameIndexMap.get("Type")).contains("array")
						|| treeItem.getText(columnNameIndexMap.get("Type")).equals("object")))
			return true;
		if (treeItem.getParentItem() == null)
			return true;
		treeItem = treeItem.getParentItem();
		String pName = treeItem.getText(columnNameIndexMap.get("Name"));
		String pType = treeItem.getText(columnNameIndexMap.get("Type"));
		;
		if (pName.contains("["))
			return true;
		else if (pType.equals("object"))
			return isEligible(treeItem, columnNameIndexMap);
		return false;
	}

	public static void addJsonArrayElement(Tree tree, TreeItem treeItem, String schema,
			Map<String, Integer> columnNameIndexMap, String payload, boolean isEditable,String mainSchema) {
		try {

			ObjectMapper om = new ObjectMapper();
			JsonNode node = om.readTree(mainSchema);
			//String path = getSchemaXPath(treeItem,columnNameIndexMap.get("Name"),columnNameIndexMap.get("Type"));
			//System.out.println("Schema xPath------------");
			//System.out.println(path);
			//System.out.println(node.toPrettyString());
			//System.out.println("==========================================");
			String schemaXP=getSchemaXPath(treeItem,columnNameIndexMap.get("Name"),columnNameIndexMap.get("Type"));
			node = node.at(schemaXP);
			//System.out.println(schemaXP);
			int count = 0;
			for (TreeItem item : treeItem.getItems()) {
				if (item.getText(columnNameIndexMap.get("Name")).contains("["))
					count=treeItem.getItems().length;
				else
					count=0;
				break;
			}
			String temp = "";
			String newSchemaElemet = "";
			if (treeItem.getText(columnNameIndexMap.get("Type")).contains("object")) {
//				temp = (node.toPrettyString().replaceFirst(Pattern.quote("type"), "")
//						.replaceFirst(Pattern.quote(":"), "").replaceFirst(Pattern.quote("array"), "")
//						.replaceFirst(Pattern.quote(","), "").replaceFirst(Pattern.quote("items"), "")
//						.replaceFirst(Pattern.quote(":"), "").replaceFirst(Pattern.quote("{"), "") + "END-OF-SCHEMA")
//								.replaceFirst(Pattern.quote("}END-OF-SCHEMA"), "");
//				temp = temp.replace("\"\"", "");
				//System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++");
				//System.out.println(node.toPrettyString());
				node=node.at("/items");
				temp=node.toPrettyString();
				//System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++");
				//System.out.println(temp);
				//System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++");
				

			} else {
				temp = "{\"type\":\""
						+ treeItem.getText(columnNameIndexMap.get("Type")).replace("array<", "").replace(">", "")
						+ "\"}";
			}
			newSchemaElemet = "{\"type\":\"object\", \"properties\":{\""
					+ treeItem.getText(columnNameIndexMap.get("Name")) + "[" + count + "]\":" + temp + "}}";
			// System.out.println("--------------Modified-------------------");
			// System.out.println(temp);
			// System.out.println("-----------------------------------------");
			// "{\"type\":\"object\",
			// \"properties\":{\""+treeItem.getText(columnNameIndexMap.get("Name"))+"["+count+"]\":"+temp+"}}";

			String type = treeItem.getText(1);
			//System.out.println(newSchemaElemet);
			addSchema(newSchemaElemet, treeItem, columnNameIndexMap);
			treeItem.setText(1, type);
			int size = treeItem.getItemCount() - 1;
			if(isEditable)
				enableJsonEditor(tree, treeItem.getItems(), newSchemaElemet, columnNameIndexMap, payload,isEditable,mainSchema);
		} catch (Exception er) {
			new ErrorDialogBox(tree.getShell(), tree.getShell().getStyle()).open(er);
		}
	}

	private static void addJsonData(Tree tree, TreeItem treeItem, String schema,
			Map<String, Integer> columnNameIndexMap, String payload, boolean isEditable,String mainSchema) throws Exception {
		if (payload != null) {
			ObjectMapper om = new ObjectMapper();
			JsonNode node = om.readTree(payload);
			String xPath = getXPathForJson(treeItem, columnNameIndexMap.get("Name"));
			//System.out.println("Payload xPath:---------------------\n" + xPath);
			node = node.at(xPath);
			//System.out.println("-: "+node.size());
			if (node != null) {
				for (int loop = 0; loop < node.size(); loop++) {
					
					addJsonArrayElement(tree, treeItem, schema, columnNameIndexMap, payload,isEditable,mainSchema);
					
				}
			}
		}
	}
	
	public static JsonNode applyPatch(String jsonPatch,JsonNode jnTarget) throws Exception{
		ObjectMapper om=new ObjectMapper();
		//JsonNode jPatchNode=om.readTree(jsonPatch);
		jsonPatch=jsonPatch.replaceAll("\n", "").replaceAll("\r", "");
		//System.out.println("----------------------------------------------------------------------------");
		//System.out.println(jsonPatch);
		List<JPOP> Ops = om.readValue(jsonPatch,new TypeReference<List<JPOP>>() {});
		Map<String, List<JPOP>> nestedOpMap=new HashMap<String, List<JPOP>>();
		for (JPOP jpop : Ops) {
			if(jpop.getFollow()!=null && jpop.getFollow().trim().length()>0) {
				List<JPOP> list=nestedOpMap.get(jpop.getFollow());
				if(list==null)
					list=new ArrayList<JPOP>();
				list.add(jpop);
				nestedOpMap.put(jpop.getFollow(), list);
			}
		}
		for (JPOP jpop : Ops) {
			if(jpop.getFollow().trim().length()<=0)
				jnTarget = JsonPatcher.apply(jpop, jnTarget,nestedOpMap);
		}
		return jnTarget;
	}

	private static void loadJsonData(Tree tree, TreeItem ti[], String schema, Map<String, Integer> columnNameIndexMap,
			String payload,boolean isEditable,String mainSchema) {
		try {
			int counter=0;
			TreeItem treeItems[] = ti;
			if (ti == null)
				treeItems = tree.getItems();
			//System.out.println("Number of elements to be populated: "+treeItems.length);
			for (TreeItem treeItem : treeItems) {
				if(counter++>MAX_ELEMENTS && MAX_ELEMENTS>0) {
					//System.out.println("-------------------Break-----------------------");
					break;
				}
				String type = treeItem.getText(columnNameIndexMap.get("Type"));
				boolean isEligible = isEligible(treeItem, columnNameIndexMap);
				
				
				Runnable run=new Runnable() {
					
					@Override
					public void run() {
						try {
							//System.out.println(".");
						if (type.toLowerCase().contains("array<") && isEligible) {
							//System.out.println("-");
							addJsonData(tree, treeItem, schema, columnNameIndexMap, payload,isEditable,mainSchema);
							//System.out.println("+");
							loadJsonData(tree, treeItem.getItems(), schema, columnNameIndexMap, payload,isEditable,mainSchema);
							//System.out.println("=");
						} else if (type.toLowerCase().equals("object")) {
							//System.out.println("++");
							loadJsonData(tree, treeItem.getItems(), schema, columnNameIndexMap, payload,isEditable,mainSchema);
							//System.out.println("==");
						}
						else {
							if (payload != null && isEligible) {
								String val = "";
								ObjectMapper om = new ObjectMapper();
								JsonNode node = om.readTree(payload);
								String xPath = getXPathForJson(treeItem, columnNameIndexMap.get("Name"));
								//System.out.println("Payload xPath:---------------------\n" + xPath);
								//String type=treeItem.getText(columnNameIndexMap.get("Type")).toLowerCase();
								if (node != null) {
									node = node.at(xPath);
									if(type.toLowerCase().contains("integer"))
										val = node.asLong(0)+"";
									else if(type.toLowerCase().contains("number"))
										val = node.asDouble(0.00)+"";
									else
										val = node.textValue();
									if (val != null)
										treeItem.setText(columnNameIndexMap.get("Value"), val);
								} else
									System.out.println("Node not found");
							}
						}
						}catch (Exception e) {
							e.printStackTrace();
						}
					}
				};
				Display.getDefault().asyncExec(run);
				
			}
		} catch (Exception e) {
			new ErrorDialogBox(tree.getShell(), tree.getShell().getStyle()).open(e);
		}
	}

	public static void enableJsonEditor(Tree tree, TreeItem ti[], String schema,
			Map<String, Integer> columnNameIndexMap, String payload, boolean isEditable,String mainSchema) {
		try {
			TreeItem treeItems[] = ti;
			if (ti == null)
				treeItems = tree.getItems();
			for (TreeItem treeItem : treeItems) {
				String type = treeItem.getText(columnNameIndexMap.get("Type"));
				final TreeEditor editor = new TreeEditor(tree);
				editor.horizontalAlignment = SWT.LEFT;
				editor.grabHorizontal = true;
				boolean isEligible = isEligible(treeItem, columnNameIndexMap);

				// System.out.println("Adding text listener on " + treeItem.getText(0));
				if (type.toLowerCase().contains("array<") && isEligible && isEditable) {
					// System.out.println("setting Add button for "+treeItem.getText(0));
					Button btnAddElement = new Button(tree, SWT.NONE);
					btnAddElement.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							addJsonArrayElement(tree, treeItem, schema, columnNameIndexMap, payload,isEditable,mainSchema);
							tree.deselectAll();
						}
					});

					btnAddElement.setText("Add");
					// System.out.println("Adding dispose listener to "+treeItem.getText(0));
					treeItem.addListener(SWT.Dispose, new Listener() {
						public void handleEvent(Event event) {
							// table.remove (table.getSelectionIndex());
							// tree.notifyListeners(SWT.Resize, new Event());
							btnAddElement.setVisible(false);
							// System.out.println("======================dispose this
							// button===================");
							btnAddElement.dispose();
						}
					});
					editor.setEditor(btnAddElement, treeItem, 2);
					enableJsonEditor(tree, treeItem.getItems(), schema, columnNameIndexMap, payload,isEditable,mainSchema);
				} else if (type.toLowerCase().equals("object")) {
					enableJsonEditor(tree, treeItem.getItems(), schema, columnNameIndexMap, payload,isEditable,mainSchema);
				} else {
					if (payload != null && isEligible) {
						String val = "";
						ObjectMapper om = new ObjectMapper();
						JsonNode node = om.readTree(payload);
						String xPath = getXPathForJson(treeItem, columnNameIndexMap.get("Name"));
						//System.out.println("Payload xPath:---------------------\n" + xPath);
						if (node != null) {
							node = node.at(xPath);
							val = node.textValue();
							if (val != null)
								treeItem.setText(columnNameIndexMap.get("Value"), val);
						} else
							System.out.println("Node not found");
					}
				}
			}
		} catch (Exception e) {
			new ErrorDialogBox(tree.getShell(), tree.getShell().getStyle()).open(e);
		}
	}

	public static String generateJsonPayload(Tree tree, Map<String, Integer> columnNameIndexMap) {
		TreeItem[] treeItems = tree.getItems();
		String payload = generateJsonPayload(treeItems, columnNameIndexMap);
		String jsonPayload = "{" + payload + "}";
		jsonPayload = jsonPayload.replace(",END-OF-JSON-####-##-####", "").replace("END-OF-JSON-####-##-####", "");
		return jsonPayload;
	}
	
	public static String generateJsonSchema(Tree tree, Map<String, Integer> columnNameIndexMap) {
		if(tree.getItemCount()<=0)
			return "";
		jsonWorkspace="";
		appendln("{");		
		appendln("\"$schema\": \"http://json-schema.org/draft-04/schema#\",");
		appendln("\"type\": \"object\", ");
		appendln("\"properties\": {");
		String json=jsonWorkspace;
		jsonWorkspace="";
		json+=generateJSchema(tree, null, columnNameIndexMap)+"}}";
		json=json.replaceAll(","+identifier, "").replaceAll(identifier, "");
		return json;
	}
	
	public static TreeItem generateAPIDefaults(Tree tree,String rootName,String payloadRootName,Map<String, Integer> columnNameIndexMap) {
		if(tree.getItemCount()>0)
			return null;
		TreeItem item = new TreeItem(tree, SWT.FULL_SELECTION);
		item.setText(columnNameIndexMap.get("Name"), rootName);
		item.setText(columnNameIndexMap.get("Type"), "object");
		item = new TreeItem(item, SWT.FULL_SELECTION);
		item.setText(columnNameIndexMap.get("Name"), payloadRootName);
		item.setText(columnNameIndexMap.get("Type"), "object");
		return item;
	}
	
	private static String generateJSchema(Tree tree,TreeItem[] ti, Map<String, Integer> columnNameIndexMap) {
		TreeItem[] treeItems = ti;
		if(ti==null)
			treeItems = tree.getItems();
		for (TreeItem treeItem : treeItems) {
			String name=treeItem.getText(columnNameIndexMap.get("Name"));
			String type=treeItem.getText(columnNameIndexMap.get("Type"));
			boolean primitive=true;
			if(type.contains("object"))
				primitive=false;
			if(primitive) {
				if(type.contains("array")) {
					appendln("\""+name+"\":{");
					appendln("\"type\" : \"array\",");
					appendln("\"items\": [{");
					appendln("\"type\" : \""+type.replace("array<", "").replace(">", "")+"\"");
					appendln("}]");
					append("},");
				}else {
					appendln("\""+name+"\":{");
					appendln("\"type\" : \""+type+"\"");
					append("},");
				}
			}else {
				if(type.contains("array<")) {
					appendln("\""+name+"\":{");
					appendln("\"type\":\"array\",");
					appendln("\"items\": {");
					appendln("\"type\":\"object\",");
					appendln("\"properties\":{");
					appendln(generateJSchema(tree, treeItem.getItems(), columnNameIndexMap));
					appendln("}");
					appendln("}");
					append("},");
				}else {
					appendln("\""+name+"\":{");
					appendln("\"type\":\"object\",");
					appendln("\"properties\":{");
					appendln(generateJSchema(tree, treeItem.getItems(), columnNameIndexMap));
					appendln("}");
					append("},");
				}
			}
			
		}
		appendln(identifier);
		String tempJson=jsonWorkspace;
		jsonWorkspace="";
		return tempJson;
	}
	
	
	public static void setJsonPointerValue(ObjectNode node, JsonPointer pointer, JsonNode value, ObjectMapper mapper) {
	    JsonPointer parentPointer = pointer.head();
	    JsonNode parentNode = node.at(parentPointer);
	    String fieldName = pointer.last().toString().substring(1);

	    if (parentNode.isMissingNode() || parentNode.isNull()) {
	        parentNode = StringUtils.isNumeric(fieldName) ? mapper.createArrayNode() : mapper.createObjectNode();
	        setJsonPointerValue(null,parentPointer, parentNode,mapper); // recursively reconstruct hierarchy
	    }

	    if (parentNode.isArray()) {
	        ArrayNode arrayNode = (ArrayNode) parentNode;
	        int index = Integer.valueOf(fieldName);
	        // expand array in case index is greater than array size (like JavaScript does)
	        for (int i = arrayNode.size(); i <= index; i++) {
	            arrayNode.addNull();
	        }
	        arrayNode.set(index, value);
	    } else if (parentNode.isObject()) {
	        ((ObjectNode) parentNode).set(fieldName, value);
	    } else {
	        throw new IllegalArgumentException("`" + fieldName + "` can't be set for parent node `"
	                + parentPointer + "` because parent is not a container but " + parentNode.getNodeType().name());
	    }
	}
	
	public static ClassMetaData generateClasses(Tree requestDocTree, Tree responseDocTree, Text pacakgeName, Text importedPackages,
			Text serviceImplementation, Text servicePrivateCode, String operationName, ClassMetaData cls,Map<String, Integer> columnNameIndexMap,CodeGen CG) throws Exception {
		
		Map<String, ClassMetaData> classes;
		TreeItem[] requestDoc=requestDocTree.getItems();
		TreeItem[] responseDoc=responseDocTree.getItems();
			ClassMetaData service=createRoot(operationName, requestDoc, responseDoc,cls,CG,columnNameIndexMap);
			API api=service.getAPI();
			api.code=Base64.getEncoder().encodeToString(serviceImplementation.getText().getBytes());
			api.imports=Base64.getEncoder().encodeToString(importedPackages.getText().getBytes());
			api.custom=Base64.getEncoder().encodeToString(servicePrivateCode.getText().getBytes());
			classes=CG.classes;
			geneateFunction(classes, service,operationName);
			return service;
	}
	
	private static void newSchema(TreeItem treeItem,int pad,ClassMetaData cls,Map<String, Integer> columnNameIndexMap,CodeGen CG) throws Exception{
		TreeItem items[]=items=treeItem.getItems();	
		for (TreeItem item : items) {
			processItem(item,pad,cls,columnNameIndexMap,CG);
		}
	}
	
	private static void processItem(TreeItem item,int pad,ClassMetaData cls,Map<String, Integer> columnNameIndexMap, CodeGen CG) throws Exception{
		String type=item.getText(columnNameIndexMap.get("Type"));//type of object
		String clsName=item.getText(columnNameIndexMap.get("Name"));	
		if(type.equalsIgnoreCase("object")) {
			if(cls==null) {
				cls=CG.createEntityClass(clsName,"Local",clsName);
			}else {
				Property prop=cls.addProperty("private",clsName, clsName.toLowerCase(),false);
				cls=CG.createEntityClass(clsName,"Local",cls.getRoot()+"."+clsName);
			}
    		newSchema(item, pad+2,cls,columnNameIndexMap,CG);
		}
		else if(type.toLowerCase().contains("array")) {
			//String clsName=item.getText(columnNameIndexMap.get("Name"));
			String subType=type.toLowerCase().replace("array<", "").replace(">", "");
			if(subType.equalsIgnoreCase("object")) {
				if(cls==null) {
					cls=CG.createEntityClass(clsName,"Local",clsName);
				}
				else {
					cls.addImport("import java.util.List;");
					Property prop=cls.addProperty("private","List<"+clsName+">", CodeGen.getPlural(clsName.toLowerCase()),false);
					cls=CG.createEntityClass(clsName,"Local",cls.getRoot()+"."+clsName);
				}
			}else {
				if(cls==null) {
					cls=CG.createEntityClass(clsName,"Local",clsName);
					Property prop=null;
					if(subType.equalsIgnoreCase("string"))
						prop=cls.addProperty("private ","String[]", CodeGen.getPlural(clsName.toLowerCase()),true);
					else
						prop=cls.addProperty("private ",subType+"[]", CodeGen.getPlural(clsName.toLowerCase()),true);
				}
				else {
					Property prop=null;
					if(subType.equalsIgnoreCase("string"))
						prop=cls.addProperty("private ","String[]", CodeGen.getPlural(clsName.toLowerCase()),true);
					else
						prop=cls.addProperty("private ",subType+"[]", CodeGen.getPlural(clsName.toLowerCase()),true);
					cls=CG.createEntityClass(clsName,"Local",cls.getRoot()+"."+clsName);
				}
			}
			newSchema(item, pad+3,cls,columnNameIndexMap,CG);
		}
		else {
			Property prop=null;
			if(type.equalsIgnoreCase("string"))
				prop=cls.addProperty("private ","String ", (clsName.toLowerCase()),true);
			else
				prop=cls.addProperty("private ",type+" ", (clsName.toLowerCase()),true);
		}
	}
	
	private static ClassMetaData createRoot(String name,TreeItem[] requestDoc,TreeItem[] responseDoc, ClassMetaData cls, CodeGen CG,Map<String, Integer> columnNameIndexMap) throws Exception{
		//String apiMethod=type.toLowerCase().replace("api<", "").replace(">", "");
	int pad=1;
	//String type="object";
		String clsName=name;
		cls=CG.createAPIClass(clsName);
		API api=cls.getAPI();
		api.method="GET";
		cls.setScope("Global");
		if(requestDoc!=null)
		for (TreeItem item : requestDoc) {
			processItem(item,pad,cls,columnNameIndexMap,CG);
		}
		if(responseDoc!=null)
		for (TreeItem item : responseDoc) {
			processItem(item,pad,cls,columnNameIndexMap,CG);
		}
		return cls;
}
	
	private static void geneateFunction(Map<String, ClassMetaData> classes,ClassMetaData clazz,String resourcePath) throws Exception {
		ClassMetaData resource=classes.get(clazz.getName()+"Resource");
		API api=clazz.getAPI();
		Set<String> imports=api.getImportList();
		if(imports!=null)
		for (String imprt : imports) {
			resource.addImport(imprt);
		}
		
		//resource.addProperty(access, type, name, primitive)
		ClassMetaData payload=classes.get(clazz.getName()+".Payload");
		String requestBody="@RequestBody(required = false)";
		Function apiActionImpl=null;
		Function functionJsonWrapper=null;
		if((payload.getProperty("requestpayloads")!=null && payload.getProperty("requestpayloads").required) || (payload.getProperty("requestpayload")!=null && payload.getProperty("requestpayload").required))
			requestBody="@RequestBody(required = true)";
		if(payload.getProperty("responsepayload")!=null) {
			apiActionImpl=resource.addFunction("public", "ResponsePayload", clazz.getName().toLowerCase());
			functionJsonWrapper=resource.addFunction("public", "String", clazz.getName().toLowerCase()+"Wrapper");
		}
		else {
			apiActionImpl=resource.addFunction("public", "List<ResponsePayload>", clazz.getName().toLowerCase()+"s");
			functionJsonWrapper=resource.addFunction("public", "String", clazz.getName().toLowerCase()+"s");
		}
		functionJsonWrapper.addParam("", "String", "jsonPayload");
		functionJsonWrapper.exceptions.add("Exception");
		
		if(payload.getProperty("requestpayload")!=null)
			apiActionImpl.addParam(requestBody,"RequestPayload" , "requestPayload");
		else
			apiActionImpl.addParam(requestBody,"List<RequestPayload>" , "requestPayloads");
		String method=api.method.toLowerCase();
		method=method.substring(0, 1).toUpperCase()+method.substring(1);
		List<Property> queryStringProps= clazz.getProperties();
		ClassMetaData requestheaders=classes.get(clazz.getRoot()+".Headers"+"."+"RequestHeaders");
		
		if(requestheaders!=null) {
		List<Property> properties=requestheaders.getProperties();
		if(properties!=null && properties.size()>0) {
			resource.addImport("import org.springframework.web.bind.annotation.RequestHeader;");
			requestheaders.addAnnotation("@AllArgsConstructor");
			requestheaders.addImport("import lombok.AllArgsConstructor;");
			requestheaders.addAnnotation("@NoArgsConstructor");
			requestheaders.addImport("import lombok.NoArgsConstructor;");
			for (Property property : properties) {
				apiActionImpl.addParam("@RequestHeader(\""+property.name+"\")", property.type, property.name);
			}
		}
		}
		//@PostMapping(path = "/entrys",params= {"projectId","timesheetId"})
		//@RequestParam long timesheetId
		//@PathVariable long id
		String params="";
		functionJsonWrapper.codeLines.add("ObjectMapper om=new ObjectMapper();");
		functionJsonWrapper.codeLines.add("om.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);");
		functionJsonWrapper.codeLines.add("RequestPayload req=om.readValue(jsonPayload.getBytes(), RequestPayload.class);");
		functionJsonWrapper.codeLines.add("String ret=om.writeValueAsString("+apiActionImpl.name+"(req));");
		functionJsonWrapper.codeLines.add("return ret;");
		resource.addImport("import com.fasterxml.jackson.databind.ObjectMapper;");
		resource.addImport("import com.fasterxml.jackson.databind.MapperFeature;");
		
		api.path=resourcePath;//.replace(";", newChar);
		
		apiActionImpl.annotations.add("@"+method+"Mapping(path=\""+api.path+"\")");
		apiActionImpl.codeLines.add(new String(Base64.getDecoder().decode(api.code)));
		resource.setCustomCode(new String(Base64.getDecoder().decode(api.custom)));
	}
	private static String jsonWorkspace;
	private static final String identifier="-~-~-~-010-~-~-~";
	private static void appendln(String data) {
		jsonWorkspace+=data+"\n";
		//System.out.println(data.replaceAll(","+identifier, "").replaceAll(identifier, ""));
	}
	private static void append(String data) {
		jsonWorkspace+=data;
		//System.out.println(data.replaceAll(","+identifier, "").replaceAll(identifier, ""));
	}
	private static String generateSchema(ClassMetaData service, Map<String, ClassMetaData> classes, String opetrationName) {
		API api=service.getAPI();
		appendln("{");		
		appendln("\"$schema\": \"http://json-schema.org/draft-04/schema#\",");
		appendln("\"import\": \""+api.imports+"\", ");
		appendln("\"code\": \""+api.code+"\", ");
		appendln("\"custom\": \""+api.custom+"\", ");
		appendln("\"operation\": \""+opetrationName+"\", ");
		appendln("\"type\": \"object\", ");
		
		appendln("\"properties\": {");
		appendln(service.toJsonSchema(classes));
		appendln("}");
		appendln("}");
		jsonWorkspace=jsonWorkspace.replaceAll(","+identifier, "").replaceAll(identifier, "");
		String temp=jsonWorkspace;
		jsonWorkspace="";
		return temp;
	}

	private static String generateJsonPayload(TreeItem[] treeItems, Map<String, Integer> columnNameIndexMap) {
		String jsonPayload = "";
		for (TreeItem item : treeItems) {
			String type = item.getText(columnNameIndexMap.get("Type"));
			String name = item.getText(columnNameIndexMap.get("Name"));
			//System.out.println("Type:"+type+" and Name: "+name);
			if (type.contains("array<object>")) {
				String payload = generateJsonPayload(item.getItems(), columnNameIndexMap);
				if(!payload.startsWith("{"))
					payload="{"+payload+"}";
				payload = "\"" + name + "\":[" + payload + "],";
				jsonPayload += payload;
			} else if (type.contains("array<")) {
				String payload = "\"" + name + "\":[";
				TreeItem[] items = item.getItems();
				for (TreeItem treeItem : items) {
					payload += "\"" + treeItem.getText(columnNameIndexMap.get("Value")) + "\",";
				}
				payload += "END-OF-JSON-####-##-####";
				jsonPayload += payload + "],";
			} else if (type.contains("object")) {
				String payload = generateJsonPayload(item.getItems(), columnNameIndexMap);
				if (name.contains("["))
					payload = "{" + payload + "},";
				else
					payload = "\"" + name + "\":{" + payload + "},";
				jsonPayload += payload;
			} else {
				//System.out.println("Value index: "+columnNameIndexMap.get("Value"));
				//System.out.println("Value: "+item.getText(columnNameIndexMap.get("Value")));
				String payload = "\"" + name + "\":\"" + item.getText(columnNameIndexMap.get("Value")) + "\",";
				jsonPayload += payload;
			}
		}
		jsonPayload += "END-OF-JSON-####-##-####";
		return jsonPayload;
	}
	
	public static String decodeBase64(String string) {
		if(string==null)
			return "";
		   try {
			   return new String(Base64.getDecoder().decode(string));
		    } catch(Exception e) { 
		    	e.printStackTrace();
		    	return string;
		    }
		}
	public static String encodeBase64(String data) {
		try {
		return Base64.getEncoder().encodeToString(data.getBytes());
		}catch (Exception e) {
			e.printStackTrace();
		}
		return data;
	}
	
	public static TreeItem geTreeItemAt(String xPath,Tree tree) {
		TreeItem ti[]=tree.getItems();
		String elem[]=xPath.replace("/0", "").replace("/*", "").split(Pattern.quote("/"));
		TreeItem item=null;
		int loc=1;
		item=at(ti, elem,loc);
		return item;
	}
	
	private static TreeItem at(TreeItem ti[], String elem[], int loc) {
		if(ti==null || ti.length==0)
			return null;
		TreeItem item=null;
		for (TreeItem treeItem : ti) {
			//System.out.println("Looking for elem: "+loc+", Length: "+elem.length+", Last elem:"+elem[elem.length-1]);
			if(treeItem.getText(0).equalsIgnoreCase(elem[loc])) {
				//System.out.println("Tree Item("+loc+"): "+treeItem.getText(0));
				if(loc==elem.length-1) 
					return treeItem;			
				else {
					item=at(treeItem.getItems(), elem,loc+1);
					break;
				}
			}
		}
		return item;
	}

	public static String openFileDialog(Shell shell) {
		String selected = null;
		try {
			FileDialog fd = new FileDialog(shell, SWT.OPEN);
			fd.setText("Open");
			String[] filterExt = { "*.json;*.JSON" };
			String[] filterNames = { "TXT files" };
			fd.setFilterExtensions(filterExt);
			fd.setFilterNames(filterNames);
			selected = fd.open();
		} catch (Exception e) {
			new ErrorDialogBox(shell, shell.getStyle()).open(e);
		}
		return selected;
	}
	
	public static String saveFileDialog(Shell shell,String filename) {
		String selected = null;
		try {
			FileDialog fd = new FileDialog(shell, SWT.SAVE);
			fd.setText("Open");
			fd.setFileName(filename);
			String[] filterExt = { "*.json;*.JSON" };
			String[] filterNames = { "TXT files" };
			fd.setFilterExtensions(filterExt);
			fd.setFilterNames(filterNames);
			selected = fd.open();
		} catch (Exception e) {
			new ErrorDialogBox(shell, shell.getStyle()).open(e);
		}
		return selected;
	}
	
	
	
	public static String resolveJavaType(String typ, String format) {
		String javaType=null;
		if(typ!=null)
		switch (typ.trim()) {
		case "string":
			javaType="String";
			if("date".equals(format))
				javaType="java.util.Date";
			break;
		case "integer":
			javaType="Integer";
			break;
		case "boolean":
			javaType="boolean";
			break;
		default:
			System.out.println("Type conversion requested for "+typ.trim());
			javaType="Double";
		}
		return javaType;
	}
	
	public static String resolveGetter(Property prop) {
		String getter=".get";
		if(prop!=null && prop.primitive && "boolean".equalsIgnoreCase(prop.type)) {
			getter=".is";
		}
		return getter;
	}
	
	public static boolean evaluateCondition(String condition, JsonNode jn,ScriptEngine engine) throws Exception{
		//System.out.println(condition);
		String params[]=condition.split(Pattern.quote("}"));
		for (String param : params) {
			if(param.contains("#{")) {
				param=param.split(Pattern.quote("#{"))[1];//replace("#{", "");
				JsonNode jnn=jn.at(param);
				String value=jnn.asText();
				if(jn.isTextual())
					condition=condition.replace("#{"+param+"}", "'"+value+"'");//cond=evaluatedParam+"='"+value+"'";
				else
					condition=condition.replace("#{"+param+"}", value);//cond=evaluatedParam+"="+value;
			}
		}
		return (boolean) engine.eval(condition);
	}
	
	public static String placeXPathValue(String xPaths, JsonNode jn) throws Exception{
		//System.out.println(xPaths);
		String xPathValues=xPaths;
		String params[]=xPaths.split(Pattern.quote("}"));
		for (String param : params) {
			if(param.contains("#{")) {
				param=param.split(Pattern.quote("#{"))[1];//replace("#{", "");
				JsonNode jnn=jn.at(param);
				String value=jnn.asText();
				if(jn.isTextual())
					xPathValues=xPathValues.replace("#{"+param+"}", "'"+value+"'");//cond=evaluatedParam+"='"+value+"'";
				else
					xPathValues=xPathValues.replace("#{"+param+"}", value);//cond=evaluatedParam+"="+value;
			}
		}
		return xPathValues;
	}
	
	public static String toLowerFirst(String word) {
		word=word.substring(0, 1).toLowerCase()+word.substring(1,word.length());
		return word;
	}
	
	public static String toUpperFirst(String word) {
		word=word.substring(0, 1).toUpperCase()+word.substring(1,word.length());
		return word;
	}
	
public static void main1(String[] args) throws Exception{
	String json="{\"meta\": {\r\n" + 
			"    \"pagination\": {\r\n" + 
			"      \"total\": 858,\r\n" + 
			"      \"pages\": 43,\r\n" + 
			"      \"limit\": 20,\r\n" + 
			"      \"page\": 1\r\n" + 
			"    }\r\n" + 
			"  }}";
	String condition="#{/meta/pagination/total}>#{/meta/pagination/limit} && #{/meta/pagination/total}<900";
	ObjectMapper om=new ObjectMapper();
	JsonNode jn=om.readTree(json);
	ScriptEngineManager factory = new ScriptEngineManager();
    ScriptEngine engine = factory.getEngineByName("JavaScript");
    engine.eval("if(2==2)throw new Error(\"Error reported from ssdsdssdsd inside script\");");
	//System.out.println(evaluateCondition(condition, jn,engine));
}
public static void main(String[] args) {
	System.out.println(toLowerFirst("AccessPrivilages"));
}
}
