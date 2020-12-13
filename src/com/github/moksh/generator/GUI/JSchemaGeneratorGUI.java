package com.github.moksh.generator.GUI;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.jsonschema2pojo.DefaultGenerationConfig;
import org.jsonschema2pojo.GenerationConfig;
import org.jsonschema2pojo.Jackson2Annotator;
import org.jsonschema2pojo.SchemaGenerator;
import org.jsonschema2pojo.SchemaMapper;
import org.jsonschema2pojo.SchemaStore;
import org.jsonschema2pojo.rules.RuleFactory;

import com.github.moksh.generator.core.ClassMetaData;
import com.github.moksh.generator.core.ClassMetaData.API;
import com.github.moksh.generator.core.ClassMetaData.Property;
import com.github.moksh.generator.core.CodeGen;
import com.github.moksh.generator.core.ImportJSON;
import com.sun.codemodel.JCodeModel;

import swing2swt.layout.BorderLayout;

public class JSchemaGeneratorGUI extends Composite {
	private Text jsonText;
	private Text schemaText;
	private Map<String, String> schemaMap;
	private Combo documentSelector=null;
	private CodeGen CG=CodeGen.CG;
	/**
	 * @wbp.nonvisual location=314,279
	 */
	public Display display = null;
	public Shell shell = new Shell(display);
	int nameIndex=0,titleIndex=2,typeIndex=3,descIndex=1,egIndex=4,enumIndex=5,formatIndex=6,requiredIndex=7,scopeIndex=8,codeIndex=9,customIndex=10, importIndex=11;
	public static void main(String[] args) {
		final Display display = new Display();
		final Shell shell = new Shell(display);
		shell.setMinimumSize(new Point(800, 600));
		shell.setLayout(new FillLayout(SWT.HORIZONTAL));
		JSchemaGeneratorGUI jschema=new JSchemaGeneratorGUI(shell, SWT.NONE);
		jschema.display=display;
		jschema.shell=shell;
		shell.setText("Moksh, A rapid prototyping tool to bring an end to the cycle of requirement-design discussion.");
		shell.pack();
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

	private final JFileChooser fileChooser = new JFileChooser();
	private Tree tree;
	private TableEditor editor;

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public int reloadSelector(Combo obj,String name) {
		Object[] arry=schemaMap.keySet().toArray();
		String strs[]=new String[arry.length];
		int index=0;
		for (int i = 0; i < arry.length; i++) {
			strs[i]=(String)arry[i];
			if(name.equals(strs[i])) {
				index=i;
			}
		}
		obj.setItems(strs);
		return index;
	}
	public boolean renameRootElement(String name,String newName,Combo obj) {
		String schema=schemaMap.get(newName);
		if(schema==null) {
			schemaMap.put(newName, schemaMap.get(name));
			schemaMap.remove(name);
			int index=reloadSelector(obj,newName);
			obj.select(index);
			return true;
		}
		return false;
	}
	public JSchemaGeneratorGUI(Composite parent, int style) {
		super(parent, style);
		schemaMap=new HashMap<String, String>();
		schemaMap.put("New Root", null);
		setLayout(new BorderLayout(0, 0));
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
			    "JSON files", "JSON");
		fileChooser.setFileFilter(filter);
		
		TabFolder tabFolder = new TabFolder(this, SWT.NONE);
		
		TabItem tbtmEditor = new TabItem(tabFolder, SWT.NONE);
		tbtmEditor.setText("Editor");
		
		Font treeFont = new Font( display, new FontData( "Segoe UI", 10, SWT.NORMAL ) );
		
		Composite editorComposite = new Composite(tabFolder, SWT.NONE);
		tbtmEditor.setControl(editorComposite);
		editorComposite.setLayout(new GridLayout(1, false));
		
		tree = new Tree(editorComposite, SWT.MULTI | SWT.FULL_SELECTION);
		//editor = new TableEditor(tree);
		tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);
		
		TreeColumn trclmnName = new TreeColumn(tree, SWT.LEFT_TO_RIGHT);
		trclmnName.setWidth(190);
		trclmnName.setText("Name");
		
		TreeColumn trclmnDescription = new TreeColumn(tree, SWT.LEFT_TO_RIGHT);
		trclmnDescription.setWidth(391);
		trclmnDescription.setText("Description");
		
		TreeColumn trclmnTitle = new TreeColumn(tree, SWT.LEFT_TO_RIGHT);
		trclmnTitle.setWidth(130);
		trclmnTitle.setText("Title");
		
		TreeColumn trclmnType = new TreeColumn(tree, SWT.LEFT_TO_RIGHT);
		trclmnType.setWidth(120);
		trclmnType.setText("Type");
		
		TreeColumn trclmnExample = new TreeColumn(tree, SWT.LEFT_TO_RIGHT);
		trclmnExample.setWidth(231);
		trclmnExample.setText("Value");
		
		TreeColumn trclmnEnum = new TreeColumn(tree, SWT.LEFT_TO_RIGHT);
		trclmnEnum.setWidth(100);
		trclmnEnum.setText("Enum");
		
	    
	    TreeColumn trclmnFormat = new TreeColumn(tree, SWT.LEFT_TO_RIGHT);
	    trclmnFormat.setText("Format");
	    trclmnFormat.setWidth(100);
	    
		TreeColumn trclmnRequied = new TreeColumn(tree, SWT.LEFT_TO_RIGHT);
		trclmnRequied.setWidth(100);
		trclmnRequied.setText("Required");
		
		TreeColumn trclmnScope = new TreeColumn(tree, SWT.LEFT_TO_RIGHT);
		trclmnScope.setWidth(100);
		trclmnScope.setText("Scope");
		
		TreeColumn trclmnCode = new TreeColumn(tree, SWT.LEFT_TO_RIGHT);
		trclmnCode.setWidth(100);
		trclmnCode.setText("Code");
		
		TreeColumn trclmnCustom = new TreeColumn(tree, SWT.LEFT_TO_RIGHT);
		trclmnCustom.setWidth(100);
		trclmnCustom.setText("Custom");
		
		TreeColumn trclmnImport = new TreeColumn(tree, SWT.LEFT_TO_RIGHT);
		trclmnImport.setWidth(100);
		trclmnImport.setText("Import");
		
		tree.setFont(treeFont);
		tree.addListener(SWT.EraseItem, new Listener() {
		      public void handleEvent(Event event) {
		        if ((event.detail & SWT.SELECTED) != 0) {
		          GC gc = event.gc;
		          Rectangle area = tree.getClientArea();
		          /*
		           * If you wish to paint the selection beyond the end of last column,
		           * you must change the clipping region.
		           */
		          int columnCount = tree.getColumnCount();
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
		          gc.setForeground(display.getSystemColor(SWT.COLOR_RED));
		          gc.setBackground(display.getSystemColor(SWT.COLOR_LIST_BACKGROUND));
		          gc.fillGradientRectangle(0, rect.y, shell.getSize().x, rect.height, false);
		          // restore colors for subsequent drawing
		          gc.setForeground(foreground);
		          gc.setBackground(background);
		          event.detail &= ~SWT.SELECTED;
		        }
		      }
		    });
		
	    final TreeEditor editor = new TreeEditor(tree);

	    
	    tree.addListener(SWT.MouseDoubleClick, new Listener() {
	        public void handleEvent(Event event) {
	          if(tree.getItemCount()<=0)
	        	  return;
	          final TreeItem item = tree.getSelection()[0];
	          int colIndex=0;
	          System.out.println(event.getBounds().x+","+event.getBounds().y);
	          for(int i=0;i<tree.getColumnCount();i++) {
	        	  //System.out.println(i);
	        	 if(item.getBounds(i).contains(event.getBounds().x,event.getBounds().y))
	        		 colIndex=i;
	          }
	          final int colInd=colIndex;
	          //System.out.println(colIndex);
	          if(colInd==requiredIndex) {
	        	  Combo requiredCombo = null;
	        	  //String requiredtype=item.getText(typeIndex);
	        	  requiredCombo=new Combo(tree, SWT.READ_ONLY);
	        	  requiredCombo.setItems(new String[] {"true", "false"});
	        	  requiredCombo.setText(item.getText(colIndex).toLowerCase());
		          //text.selectAll();
	        	  requiredCombo.setFocus();
	
		          final Combo fc=requiredCombo;
		          requiredCombo.addFocusListener(new FocusAdapter() {
		            public void focusLost(FocusEvent event) {
		              item.setText(colInd,fc.getText().toLowerCase());
		              fc.dispose();
		            }
		          });
	
		          requiredCombo.addKeyListener(new KeyAdapter() {
		            public void keyPressed(KeyEvent event) {
		              switch (event.keyCode) {
		              case SWT.CR:
		                item.setText(colInd,fc.getText().toLowerCase());
		              case SWT.ESC:
		                fc.dispose();
		                break;
		              }
		            }
		          });
		          editor.setEditor(requiredCombo, item,colInd);
	        	  
			  }else
				  if(colInd==scopeIndex) {
		        	  Combo scopeCombo = null;
		        	  String type=item.getText(typeIndex);
		        	  scopeCombo=new Combo(tree, SWT.READ_ONLY);
		        	  if(type.toLowerCase().startsWith("api<"))
		        		  scopeCombo.setItems(new String[] {"global"});
		        	  else {
		        		  switch (type) {
							case "number":
								scopeCombo=new Combo(tree, SWT.NONE);
								scopeCombo.setItems(new String[] {"","*ck<composite-key-name>","unique<Allow-NULL>", "unique<Not-NULL>"});
								break;
							case "string":
								scopeCombo=new Combo(tree, SWT.NONE);
								scopeCombo.setItems(new String[] {"","*ck<composite-key-name>","unique<Allow-NULL>", "unique<Not-NULL>"});
								break;
							case "integer":
								scopeCombo=new Combo(tree, SWT.NONE);
								scopeCombo.setItems(new String[] {"","*ck<composite-key-name>","unique<Allow-NULL>", "unique<Not-NULL>"});
								break;
							default:
								scopeCombo.setItems(new String[] {"","global", "persist","local"});
								break;
						  }
		        	  }
		        	  scopeCombo.setText(item.getText(colIndex).toLowerCase());
			          //text.selectAll();
		        	  scopeCombo.setFocus();
		
			          final Combo fc=scopeCombo;
			          scopeCombo.addFocusListener(new FocusAdapter() {
			            public void focusLost(FocusEvent event) {
			              item.setText(colInd,fc.getText());
			              fc.dispose();
			            }
			          });

			          scopeCombo.addKeyListener(new KeyAdapter() {
			            public void keyPressed(KeyEvent event) {
			              switch (event.keyCode) {
			              case SWT.CR:
			                item.setText(colInd,fc.getText().toLowerCase());
			              case SWT.ESC:
			                fc.dispose();
			                break;
			              }
			            }
			          });
			          editor.setEditor(scopeCombo, item,colInd);
		        	  
				  }else
	          if(colInd==formatIndex) {
	        	  Combo formatCombo = null;
	        	  String type=item.getText(typeIndex);
	        	  
	        	  switch (type) {
					case "number":
						formatCombo=new Combo(tree, SWT.READ_ONLY);
						formatCombo.setItems(new String[] {"", "double", "float"});
						break;
					case "string":
						formatCombo=new Combo(tree, SWT.READ_ONLY);
						formatCombo.setItems(new String[] {"", "binary", "byte", "password", "email", "uuid", "uri", "hostname", "ipv4", "ipv6","Date"});
						break;
					case "array<string>":
						formatCombo=new Combo(tree, SWT.READ_ONLY);
						formatCombo.setItems(new String[] {"", "binary", "byte", "password", "email", "uuid", "uri", "hostname", "ipv4", "ipv6","Date"});
						break;
					case "integer":
						formatCombo=new Combo(tree, SWT.READ_ONLY);
						formatCombo.setItems(new String[] {"", "int32", "int64"});
						break;
					case "array":
						formatCombo=new Combo(tree, SWT.READ_ONLY);
						formatCombo.setItems(new String[] {""});
						break;
					case "object":
						formatCombo=new Combo(tree, SWT.READ_ONLY);
						formatCombo.setItems(new String[] {""});
						break;
					case "boolean":
						formatCombo=new Combo(tree, SWT.READ_ONLY);
						formatCombo.setItems(new String[] {"","1/0", "true/false","TRUE/FALSE","yes/no","YES/NO"});
						break;
					case "date":
						formatCombo=new Combo(tree, SWT.NONE);
						formatCombo.setTouchEnabled(true);
						break;
					default:
						formatCombo=new Combo(tree, SWT.READ_ONLY);
						formatCombo.setItems(new String[] {"","Select Type first"});
						break;
				  }
	        	  
		          formatCombo.setText(item.getText(colIndex).toLowerCase());
		          //text.selectAll();
		          formatCombo.setFocus();
	
		          final Combo fc=formatCombo;
		          formatCombo.addFocusListener(new FocusAdapter() {
		            public void focusLost(FocusEvent event) {
		              item.setText(colInd,fc.getText().toLowerCase());
		              fc.dispose();
		            }
		          });
	
		          formatCombo.addKeyListener(new KeyAdapter() {
		            public void keyPressed(KeyEvent event) {
		              switch (event.keyCode) {
		              case SWT.CR:
		                item.setText(colInd,fc.getText().toLowerCase());
		              case SWT.ESC:
		                fc.dispose();
		                break;
		              }
		            }
		          });
		          editor.setEditor(formatCombo, item,colInd);
	          }else
	          
	          if(colInd==typeIndex) {
		          Combo typeCombo = new Combo(tree, SWT.READ_ONLY);
		          if(item.getText(colIndex).toLowerCase().contains("api")) {
		        	  typeCombo.setItems(new String[] {"API<GET>", "API<POST>", "API<PUT>", "API<DELETE>", "API<PATCH>", "object"});
		          }else
		          if(item.getText(colIndex).toLowerCase().contains("array")) {
		        	  typeCombo.setItems(new String[] {"array<string>", "array<number>", "array<boolean>", "array<integer>", "array<object>", "object"});
		          }else
		          if(item.getItemCount()==0)
		        	  typeCombo.setItems(new String[] {"string", "number", "boolean", "integer", "object", "array<object>", "API<GET>"});
		          else
		        	  typeCombo.setItems(new String[] {"object", "array<object>"});
		  		  //final Text text = new Text(tree, SWT.NONE);
		          typeCombo.setText(item.getText(colIndex).toLowerCase());
		          //text.selectAll();
		          typeCombo.setFocus();
		          typeCombo.addFocusListener(new FocusAdapter() {
		            public void focusLost(FocusEvent event) {
		              if(!item.getText(colInd).equalsIgnoreCase(typeCombo.getText())) {
		            	  item.setText(formatIndex,"");
			              item.setText(colInd,typeCombo.getText());
			              item.setText(scopeIndex,"");
			              if(item.getText(formatIndex).trim().length()==0) {
				              if(typeCombo.getText().equals("date"))
				                	 item.setText(formatIndex,"dd-MM-yyyy HH:mm:ss");
			              }
				          if(typeCombo.getText().contains("object")) {
				              item.setText(requiredIndex,"false");
				              item.setText(scopeIndex,"Persist");
				          }
				          else {
				        	  item.setText(scopeIndex,"");
				        	  item.setText(requiredIndex,"true");
				          }
				          if(typeCombo.getText().toLowerCase().contains("api<")) {
				        	  item.setText(scopeIndex,"Global");
				        	  generateAPIDefaults(item);
				          }
				            	  
		              }
		              typeCombo.dispose();
		            }
		          });
	
		          typeCombo.addKeyListener(new KeyAdapter() {
		            public void keyPressed(KeyEvent event) {
		              switch (event.keyCode) {
		              case SWT.CR:
		                item.setText(colInd,typeCombo.getText().toLowerCase());
		              case SWT.ESC:
		                typeCombo.dispose();
		                break;
		              }
		            }
		          });
		          editor.setEditor(typeCombo, item,colInd);
	          }else {
		    	  final Text text = new Text(tree, SWT.NONE);
		          text.setText(item.getText(colIndex));
		          text.selectAll();
		          text.setFocus();

		          text.addFocusListener(new FocusAdapter() {
		            public void focusLost(FocusEvent event) {
		            	if(colInd==nameIndex) {
		            		  Object obj=item.getParent();
		            		  if(obj!=null && obj instanceof Tree) {
		            			  renameRootElement(item.getText(colInd), text.getText(), documentSelector);
		            		  }
		            	  }
		              item.setText(colInd,text.getText());
		              text.dispose();
		            }
		          });
	
		          text.addKeyListener(new KeyAdapter() {
		            public void keyPressed(KeyEvent event) {
		              switch (event.keyCode) {
		              case SWT.CR:
		            	  if(colInd==nameIndex) {
		            		  Object obj=item.getParent();
		            		  if(obj!=null && obj instanceof Tree) {
		            			  renameRootElement(item.getText(colInd), text.getText(), documentSelector);
		            		  }
		            	  }
		            	  item.setText(colInd,text.getText());
		            	  text.dispose();
		              case SWT.ESC:
		            	  text.dispose();
		            	  break;
		              }
		            }
		          });
		          if(colInd==descIndex || colInd==egIndex || colInd==enumIndex || colInd==codeIndex || colInd==customIndex || colInd==importIndex) {
		        	  CustomDialogBox cdb=new CustomDialogBox(shell, SWT.NONE);
		        	  cdb.open(item,colInd);
		          }else
		        	  editor.setEditor(text, item,colInd);	 
	          }
	        }
	        
	      });	   
	    // */
	    
		SashForm sashForm_2 = new SashForm(editorComposite, SWT.NONE);
		sashForm_2.setSashWidth(2);
		sashForm_2.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		
		SashForm sashForm_5 = new SashForm(sashForm_2, SWT.NONE);
		sashForm_5.setSashWidth(1);
		
		Button btnMoksh = new Button(sashForm_5, SWT.NONE);
		btnMoksh.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					openMokshSchema();
				} catch (Exception e2) {
					new ErrorDialogBox(shell, style).open(e2);
				}
			}
		});
		btnMoksh.setText("Open");
		
		Label label_2 = new Label(sashForm_5, SWT.NONE);
		
		Button btnGenerateDatarest = new Button(sashForm_5, SWT.NONE);
		btnGenerateDatarest.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateSchema();
				try {
					String folderLoc=openFolderDialog();
					File file=new File(folderLoc+"/"+basePackage.getText().toString()+".JSON");
					FileOutputStream fos=new FileOutputStream(file);
					fos.write(jsonWorkspace.getBytes());
					fos.flush();
					fos.close();
					String val=CG.generateClasses(basePackage.getText().toString(),folderLoc, htmlText.getText(), javaScriptText.getText(), cssText.getText());
					if(val!=null)
						new InfoDialogBox(shell, style).open(val);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					new ErrorDialogBox(shell, style).open(e1);
				}
			}
		});
		btnGenerateDatarest.setText("Generate");
		
		Label label = new Label(sashForm_5, SWT.NONE);
		
		Label lblPakcage = new Label(sashForm_5, SWT.NONE);
		lblPakcage.setText("Pakcage: ");
		
		basePackage = new Text(sashForm_5, SWT.BORDER);
		
		Label lblSelectRootDocument = new Label(sashForm_5, SWT.NONE);
		lblSelectRootDocument.setText("Type:");
		lblSelectRootDocument.setVisible(false);
		documentSelector = new Combo(sashForm_5, SWT.READ_ONLY);
		documentSelector.setItems(new String[] {"Data-REST-API"});
		documentSelector.setVisible(false);
		documentSelector.select(0);
		
		documentSelector.addSelectionListener(new SelectionAdapter() {
		      public void widgetSelected(SelectionEvent e) {
		    	  loadSelectedSchema();
		        System.out.println(documentSelector.getText());
		      }
		});
		
		Label label_1 = new Label(sashForm_5, SWT.NONE);
		label_1.setText("");
		
		Button btnUpdateSchema = new Button(sashForm_5, SWT.NONE);
		btnUpdateSchema.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateSchema();
			}
		});
		btnUpdateSchema.setText("Update Schema");
		
		Label label_3 = new Label(sashForm_5, SWT.NONE);
		
		Button btnAdd = new Button(sashForm_5, SWT.NONE);
		btnAdd.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					if(tree.getSelection().length<=0) {
						TreeItem item = new TreeItem(tree, SWT.FULL_SELECTION);
						item.setText(nameIndex, "rootElement");
						item.setText(descIndex, "");
						item.setText(titleIndex, "");
						item.setText(typeIndex, "object");
						item.setText(egIndex, "");
						item.setText(enumIndex, "");
						item.setText(requiredIndex, "");
						item.setText(scopeIndex, "Persist");
						item.setText(codeIndex, "");
					}else {
						final TreeItem selItem = tree.getSelection()[0];
						String type=selItem.getText(typeIndex);
						
						if(type.equalsIgnoreCase("API<GET>") || type.equalsIgnoreCase("API<POST>") || type.equalsIgnoreCase("API<DELETE>") || type.equalsIgnoreCase("API<PUT>") || type.equalsIgnoreCase("API<PATCH>") ||  type.equalsIgnoreCase("API")) {
							TreeItem item = new TreeItem(selItem, SWT.FULL_SELECTION);
							item.setText(nameIndex, "newElement");
							item.setText(descIndex, "");
							item.setText(titleIndex, "");
							item.setText(typeIndex, "string");
							item.setText(egIndex, "");
							item.setText(enumIndex, "");
							item.setText(requiredIndex, "true");
							item.setText(scopeIndex, "Local");
							item.setText(codeIndex, "Code Editor");
						}else
						if(type.equalsIgnoreCase("array<object>") || type.equalsIgnoreCase("array<array>") ||  type.equalsIgnoreCase("object")) {
							TreeItem item = new TreeItem(selItem, SWT.FULL_SELECTION);
							item.setText(nameIndex, "newElement");
							item.setText(descIndex, "");
							item.setText(titleIndex, "");
							item.setText(typeIndex, "string");
							item.setText(egIndex, "");
							item.setText(enumIndex, "");
							item.setText(requiredIndex, "true");
							item.setText(scopeIndex, "Persist");
							item.setText(codeIndex, "");
						}else {
							TreeItem item = null;
							if(selItem.getParentItem()!=null)
								item=new TreeItem(selItem.getParentItem(), SWT.FULL_SELECTION);
							else
								item=new TreeItem(tree, SWT.FULL_SELECTION);
							item.setText(nameIndex, "newElement");
							item.setText(descIndex, "");
							item.setText(titleIndex, "");
							item.setText(typeIndex, "string");
							item.setText(egIndex, "");
							item.setText(enumIndex, "");
							item.setText(requiredIndex, "true");
							item.setText(scopeIndex, "");
							item.setText(codeIndex, "");
						}
					}
				} catch (Exception e2) {
					new ErrorDialogBox(shell, style).open(e2);
				}
				
			}
		});
		btnAdd.setText("Add");
		
		Button btnRemove = new Button(sashForm_5, SWT.NONE);
		btnRemove.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					if(tree.getSelection().length==0)
						new InfoDialogBox(shell, style).open("Please select the row you want to remove. Selected row will be highlighted with red color.\n\nNote:-\nYou can't undo remove action.");
					else {
						final TreeItem item = tree.getSelection()[0];
						item.dispose();
					}
				} catch (Exception e2) {
					new ErrorDialogBox(shell, style).open(e2);
				}
				
			}
		});
		btnRemove.setText("Remove");
		sashForm_5.setWeights(new int[] {84, 86, 136, 39, 73, 252, 0, 0, 22, 94, 86, 94, 156});
		sashForm_2.setWeights(new int[] {1});
		
		TabItem tbtmGenerator = new TabItem(tabFolder, SWT.NONE);
		tbtmGenerator.setText("Generator");
		
		Composite composite = new Composite(tabFolder, SWT.NONE);
		tbtmGenerator.setControl(composite);
		composite.setLayout(new GridLayout(2, false));
		
		SashForm sashForm_3 = new SashForm(composite, SWT.NONE);
		
		Label lblNewLabel = new Label(sashForm_3, SWT.NONE);
		lblNewLabel.setText("JSON Payload");
		sashForm_3.setWeights(new int[] {1});
	
		SashForm sashForm_4 = new SashForm(composite, SWT.NONE);
		
		Label lblNewLabel_1 = new Label(sashForm_4, SWT.NONE);
		lblNewLabel_1.setText("JSON Schema");
		sashForm_4.setWeights(new int[] {1});
		
		jsonText = new Text(composite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL | SWT.MULTI);
		jsonText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		schemaText = new Text(composite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL | SWT.MULTI);
		schemaText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		SashForm sashForm = new SashForm(composite, SWT.NONE);
		
		Button generatePayload = new Button(sashForm, SWT.NONE);
		generatePayload.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				generatePayloadFunction();
			}
		});
		generatePayload.setText("Generate JSON Payload");
		
		Button browseJSONPayloadFle = new Button(sashForm, SWT.NONE);
		browseJSONPayloadFle.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					String filePath=openFileDialog();
					if(filePath !=null) {
						   System.out.println("You chose to open this file: " + filePath);
							String[] text= ImportJSON.getInstance().loadPayload(filePath);
							schemaText.setText(text[0]);
							jsonText.setText(text[1]);
							ImportJSON.getInstance().generatePayload(schemaText.getText());
							Map<String, Object> map=ImportJSON.getInstance().schemaMap;
							//addToSchemaMap();
							tree.removeAll();
							loadSchemaEditor(map, tree);
					}
				} catch (Exception e2) {
					new ErrorDialogBox(shell, style).open(e2);
				}
			}
		});
		browseJSONPayloadFle.setText("Browse JSON Payload");
		SashForm sashForm_1 = new SashForm(composite, SWT.NONE);
		Button generateSchema = new Button(sashForm_1, SWT.NONE);
		generateSchema.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					String retStr[]=ImportJSON.getInstance().generateSchema(jsonText.getText());
					schemaText.setText(retStr[0]);
					jsonText.setText(retStr[1]);
					ImportJSON.getInstance().generatePayload(schemaText.getText());
					Map<String, Object> map=ImportJSON.getInstance().schemaMap;
					tree.removeAll();
					loadSchemaEditor(map, tree);
				} catch (Exception e1) {
					if(jsonText.getText()==null || jsonText.getText().length()==0) {
						new InfoDialogBox(shell, style).open("To generate JSON Schema you need to load JSON Payload first.\n1. Use \"Browse JSON Payload\" button to select JSON Payload file.\nOr\n2. Paste JSON Payload text into \"JSON Payload\" text box.");
					}else
					new ErrorDialogBox(shell, style).open(e1);
				}
			}
		});
		generateSchema.setText("Generate JSON Schema");
		
		Button browseSchemaFile = new Button(sashForm_1, SWT.NONE);
		browseSchemaFile.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					openMokshSchema();
				} catch (Exception e2) {
					new ErrorDialogBox(shell, style).open(e2);
				}
			}
		});
		browseSchemaFile.setText("Browse JSON Schema");
		
		Button buttonCreateJava = new Button(sashForm_1, SWT.NONE);
		buttonCreateJava.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					String folder=openFolderDialog();
					JCodeModel codeModel = new JCodeModel();

					GenerationConfig config = new DefaultGenerationConfig() {
					@Override
					public boolean isGenerateBuilders() { // set config option by overriding method
					return true;
					}
					};

					SchemaMapper mapper = new SchemaMapper(new RuleFactory(config, new Jackson2Annotator(config), new SchemaStore()), new SchemaGenerator());
					mapper.generate(codeModel, "Payload", basePackage.getText(), schemaText.getText());

					codeModel.build(new File(folder));
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
			}
		});
		buttonCreateJava.setText("Create Java");
		sashForm_1.setWeights(new int[] {1, 1, 1});
		
	    editor.horizontalAlignment = SWT.LEFT;
	    editor.grabHorizontal = true;
		
		TabItem tbtmApplication = new TabItem(tabFolder, SWT.NONE);
		tbtmApplication.setText("Application");
		
		Composite composite_3 = new Composite(tabFolder, SWT.NONE);
		tbtmApplication.setControl(composite_3);
		composite_3.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		pomText = new Text(composite_3, SWT.BORDER | SWT.MULTI);
		pomText.setText("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n\txsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd\">\r\n\t<modelVersion>4.0.0</modelVersion>\r\n\t<parent>\r\n\t\t<groupId>org.springframework.boot</groupId>\r\n\t\t<artifactId>spring-boot-starter-parent</artifactId>\r\n\t\t<version>2.4.0</version>\r\n\t\t<relativePath/> <!-- lookup parent from repository -->\r\n\t</parent>\r\n\t<groupId>com.lm</groupId>\r\n\t<artifactId>timesheet</artifactId>\r\n\t<version>0.0.1-SNAPSHOT</version>\r\n\t<name>timesheet</name>\r\n\t<description>Timesheet</description>\r\n\r\n\t<properties>\r\n\t\t<java.version>1.8</java.version>\r\n\t\t<mariadb-version>2.7.0</mariadb-version>\r\n\t</properties>\r\n\r\n\t<dependencies>\r\n\t\t<dependency>\r\n\t\t\t<groupId>org.springframework.boot</groupId>\r\n\t\t\t<artifactId>spring-boot-starter-data-jpa</artifactId>\r\n\t\t</dependency>\r\n\t\t<dependency>\r\n\t\t\t<groupId>org.springframework.boot</groupId>\r\n\t\t\t<artifactId>spring-boot-starter-web</artifactId>\r\n\t\t</dependency>\r\n\r\n\t\t<dependency>\r\n\t\t\t<groupId>org.springframework.boot</groupId>\r\n\t\t\t<artifactId>spring-boot-starter-test</artifactId>\r\n\t\t\t<scope>test</scope>\r\n\t\t</dependency>\r\n\t\t<dependency>\r\n\t\t\t<groupId>org.springframework.boot</groupId>\r\n\t\t\t<artifactId>spring-boot-devtools</artifactId>\r\n\t\t\t<scope>runtime</scope>\r\n\t\t\t<optional>true</optional>\r\n\t\t</dependency>\r\n\t\t<dependency>\r\n\t\t\t<groupId>org.mariadb.jdbc</groupId>\r\n\t\t\t<artifactId>mariadb-java-client</artifactId>\r\n\t\t\t<version>${mariadb-version}</version><!--$NO-MVN-MAN-VER$ -->\r\n\t\t</dependency>\r\n\t\t<dependency>\r\n\t\t\t<groupId>org.springdoc</groupId>\r\n\t\t\t<artifactId>springdoc-openapi-ui</artifactId>\r\n\t\t\t<version>1.5.0</version>\r\n\t\t</dependency>\r\n\t\t<dependency>\r\n\t\t\t<groupId>org.springdoc</groupId>\r\n\t\t\t<artifactId>springdoc-openapi-data-rest</artifactId>\r\n\t\t\t<version>1.5.0</version>\r\n\t\t</dependency>\r\n\t\t<dependency>\r\n\t\t\t<groupId>org.projectlombok</groupId>\r\n\t\t\t<artifactId>lombok</artifactId>\r\n\t\t\t<version>1.18.8</version>\r\n\t\t\t<scope>provided</scope>\r\n\t\t</dependency>\r\n\r\n\t\t<!-- https://mvnrepository.com/artifact/org.json/json -->\r\n\t\t<dependency>\r\n\t\t\t<groupId>org.json</groupId>\r\n\t\t\t<artifactId>json</artifactId>\r\n\t\t\t<version>20201115</version>\r\n\t\t</dependency>\r\n\r\n\r\n\t\t<dependency>\r\n\t\t\t<groupId>com.integralblue</groupId>\r\n\t\t\t<artifactId>log4jdbc-spring-boot-starter</artifactId>\r\n\t\t\t<version>2.0.0</version>\r\n\t\t</dependency>\r\n\t\t<dependency>\r\n\t\t\t<groupId>org.springframework.boot</groupId>\r\n\t\t\t<artifactId>spring-boot-starter-hateoas</artifactId>\r\n\t\t</dependency>\r\n\t</dependencies>\r\n\r\n\t<build>\r\n\t\t<plugins>\r\n\t\t\t<plugin>\r\n\t\t\t\t<groupId>org.springframework.boot</groupId>\r\n\t\t\t\t<artifactId>spring-boot-maven-plugin</artifactId>\r\n\t\t\t</plugin>\r\n\t\t</plugins>\r\n\t</build>\r\n\r\n</project>\r\n");
		
		applicationText = new Text(composite_3, SWT.BORDER | SWT.MULTI);
		applicationText.setText("#MySQL\r\nspring.datasource.url=jdbc:mariadb://localhost:3306/lm_timesheet\r\nspring.datasource.username=dev\r\nspring.datasource.password=manage\r\nspring.datasource.driver-class-name=org.mariadb.jdbc.Driver\r\nspring.jpa.database-platform=org.hibernate.dialect.MySQL5InnoDBDialect\r\nlogging.level.org.springframework=debug\r\n#JPA\r\nspring.jpa.hibernate.ddl-auto = update\r\n#Jackson\r\nspring.jackson.serialization.write-dates-as-timestamps=false");
		
		TabItem autoAdminGUI = new TabItem(tabFolder, SWT.NONE);
		autoAdminGUI.setText("Auto Admin GUI");
		
		Composite composite_2 = new Composite(tabFolder, SWT.NONE);
		autoAdminGUI.setControl(composite_2);
		composite_2.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		htmlText = new Text(composite_2, SWT.BORDER | SWT.MULTI);
		htmlText.setText("<script src=\"DynamicTableJs.js\"></script>\r\n<link href=\"DynamicTableCss.css\" rel=\"stylesheet\" />\r\n<link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.4.0/css/bootstrap.min.css\">\r\n<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/3.4.1/jquery.min.js\"></script>\r\n<script src=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.4.0/js/bootstrap.min.js\"></script>\r\n<div class=\"center\">\r\n    %navigation%\r\n  </div>\r\n<div class=\"container\"> \r\n    <div class=\"tables\" id=\"showData\"></div>\r\n</div>\r\n  \r\n<div class=\"modelpop\">\r\n</div>\r\n<div class=\"modal-processing\"></div> ");
		
		javaScriptText = new Text(composite_2, SWT.BORDER | SWT.MULTI);
		javaScriptText.setText("function dynamic_table(url, editUrl) {\r\n\t//alert(url);\r\n\t$body = $(\"body\");\r\n\r\n\t$(document).on({\r\n\t    ajaxStart: function() { $body.addClass(\"loading\");    },\r\n\t     ajaxStop: function() { $body.removeClass(\"loading\"); }    \r\n\t});\r\n\t$(\".modelpop\").html(\"\");\r\n\t$(\"#addMore\").remove();\r\n\t$(\"#refresh-link\").remove();\r\n\t$(\"#showData\").html(\"\");\r\n    window.editUrl = editUrl\r\n    window.entityUrl = url;\r\n    $.ajax({\r\n        url: url,\r\n        data: {\r\n            format: 'json'\r\n        },\r\n        error: function () {\r\n        \t$('body').removeClass(\"loading\");\r\n            $('#info').html('<p>An error has occurred</p>');\r\n        },\r\n        dataType: 'json',\r\n        success: function (data) {\r\n        \t\r\n            // EXTRACT VALUE FOR HTML HEADER.\r\n        \t//alert(data);\r\n        \tdata=JSON.stringify(data);\r\n        \tif(!data.startsWith(\"[\")){\r\n        \t\tdata=\"[\"+data+\"]\";\r\n        \t}\r\n        \tdata=JSON.parse(data);\r\n            var col = [];\r\n            window.cols = col;\r\n            for (var i = 0; i < data.length; i++) {\r\n                for (var key in data[i]) {\r\n                    if (col.indexOf(key) === -1) {\r\n                        col.push(key);\r\n                    }\r\n                }\r\n            }\r\n            col.push(\"Actions\");\r\n            // CREATE DYNAMIC TABLE.\r\n            var table = document.createElement(\"table\");\r\n            table.setAttribute('class', 'table blueTable');\r\n            // CREATE HTML TABLE HEADER ROW USING THE EXTRACTED HEADERS ABOVE.\r\n            window.addButton=true;\r\n            var tr = table.insertRow(-1);                   // TABLE ROW.\r\n            for (var i = 0; i < col.length; i++) {\r\n                var th = document.createElement(\"th\");      // TABLE HEADER.\r\n                if(col[i]=='_links')\r\n                \twindow.addButton=false;\r\n                th.innerHTML = col[i];\r\n                tr.appendChild(th);\r\n            }\r\n \r\n            // ADD JSON DATA TO THE TABLE AS ROWS.\r\n            //window.addButton=true;\r\n            if(data[0][col[0]]>0)\r\n            for (var i = 0; i < data.length; i++) {\r\n \r\n                tr = table.insertRow(-1);\r\n \r\n                for (var j = 0; j < col.length; j++) {\r\n                \t\r\n                \tvar tabCell = tr.insertCell(-1);\r\n                \t//alert(col[j]);\r\n                \tif(col[j]=='links'){//}(data[i][col[j]]!=\"undefined\" && j<col.length-1) && data[i][col[j]]!=null && data[i][col[j]][0]=='[object Object]'){\r\n                \t\tvar links=data[i][col[j]];\r\n                \t\t\r\n                \t\t//alert(JSON.stringify(links));\r\n                \t\ttabCell.innerHTML = '| ';\r\n                \t\tfor(var l=0;l<links.length;l++)\r\n                \t\t\ttabCell.innerHTML += '<a href=\"#\" onclick=\"dynamic_table(\\'' + links[l].href + '\\',\\''+links[l].href+'/\\' )\">'+links[l].rel+'</a> | ';// | <a href=\"#\" onclick=\"deleteRecord(' +\"'\"+editUrl+ data[i].id +\"'\"+ ')\">Delete</a></td>'\r\n                \t}else if(col[j]=='_links'){//}(data[i][col[j]]!=\"undefined\" && j<col.length-1) && data[i][col[j]]!=null && data[i][col[j]][0]=='[object Object]'){\r\n                \t\tvar links=data[i][col[j]];\r\n                \t\t\r\n                \t\t//alert(JSON.stringify(links));\r\n                \t\t//links=JSON.parse(links);\r\n                \t\ttabCell.innerHTML = '| ';\r\n                \t\tfor (var key in links)\r\n                \t\t//for(var l=0;l<links.length;l++)\r\n                \t\t\ttabCell.innerHTML += '<a href=\"#\" onclick=\"dynamic_table(\\'' + links[key].href + '\\',\\''+links[key].href+'/\\' )\">'+key+'</a> | ';// | <a href=\"#\" onclick=\"deleteRecord(' +\"'\"+editUrl+ data[i].id +\"'\"+ ')\">Delete</a></td>'\r\n                \t}else\r\n\t                    tabCell.innerHTML = data[i][col[j]];\r\n                }\r\n                var d = data[i].id;\r\n\r\n                \ttabCell.innerHTML = '<a href=\"#\" onclick=\"return EditTable(' + data[i].id + ' )\">Edit</a> | <a href=\"#\" onclick=\"deleteRecord(' +\"'\"+data[i].id +\"'\"+ ')\">Delete</a></td>'\r\n            }\r\n            // FINALLY ADD THE NEWLY CREATED TABLE WITH JSON DATA TO A CONTAINER.\r\n            var divContainer = document.getElementById(\"showData\");\r\n            divContainer.innerHTML = \"\";\r\n            divContainer.appendChild(table);\r\n            //alert(window.addButton);\r\n            if(window.addButton)\r\n            \tjQuery('.tables').before('<input id=\"addMore\" type=\"submit\" onclick=\"return CreateDynamicModelPopup(' +\"'\"+editUrl +\"'\"+ ');\" class=\"addme\" value=\"Add!\"></div>');\r\n            else\r\n            \tjQuery('.tables').before('<input id=\"addMore\" type=\"submit\" onclick=\"return CreateDynamicModelPopup(' +\"'\"+editUrl +\"'\"+ ');\" class=\"addme\" value=\"Add/Modify!\"></div>');\r\n            jQuery('.tables').before('<div id=\"refresh-link\" class=\"refresh-right\"><a href=\"#\" onclick=\"dynamic_table(\\'' + window.entityUrl + '\\',\\''+window.editUrl+'\\' )\">Refresh</a></div>');\r\n            $('body').removeClass(\"loading\");\r\n        },\r\n        type: 'GET'\r\n    });\r\n};\r\n \r\n \r\nfunction CreateDynamicModelPopup(editUrl) {\r\n    var d = window.cols;\r\n    var url=editUrl;\r\n    var pageTitle = $(this).attr('pageTitle');\r\n    var pageName = $(this).attr('pageName');\r\n \r\n    var modelbodystring = \"\";\r\n    modelbodystring += '<div class=\"modal fade\" tabindex=\"-1\" role=\"dialog\">'\r\n    modelbodystring += '<div class=\"modal-dialog\">'\r\n    modelbodystring += ' <form id=\"createForm\" action=\"\" method=\"POST\"><div class=\"modal-content\">'\r\n    modelbodystring += ' <div class=\"modal-header\">'\r\n    modelbodystring += '  <button type=\"button\" class=\"close\" data-dismiss=\"modal\" aria-label=\"Close\"><span aria-hidden=\"true\">?</span></button>'\r\n    modelbodystring += '<h4 class=\"modal-title\"></h4>'\r\n    modelbodystring += ' </div>'\r\n    modelbodystring += ' <div class=\"modal-body\">'\r\n    $.each(d, function (i, v) {\r\n        if (i !== 0 && !(v=='Actions' || v=='links' || v=='_links')) {\r\n            modelbodystring += '<div class=\"col-sm-12 modeldata\">'\r\n            modelbodystring += '<div class=\"col-sm-3\">'\r\n            modelbodystring += '<label>' + v + '</label>'\r\n            modelbodystring += '</div>'\r\n            modelbodystring += '<div class=\"col-sm-9\">'\r\n            if(v=='date')\r\n            \tmodelbodystring += '<input class=\"form-control\" type=\"date\" name=' + v + ' id=' + v + ' value=\"\"/>'\r\n            else\r\n            \tmodelbodystring += '<input class=\"form-control\" type=\"text\" name=' + v + ' id=' + v + ' value=\"\"/>'\r\n            modelbodystring += '</textarea >'\r\n            modelbodystring += '</div>'\r\n            modelbodystring += '</div>'\r\n        }\r\n    });\r\n    modelbodystring += '</div >'\r\n    modelbodystring += ' <div class=\"modal-footer\">'\r\n    modelbodystring += ' <button type=\"button\" class=\"btn btn-default\" data-dismiss=\"modal\">Close</button>'\r\n    if(window.addButton)\r\n    \tmodelbodystring += ' <button type=\"button\" class=\"btn btn-primary\" data-dismiss=\"modal\" onclick=\"return createRecord('+\"'\"+url+\"'\"+');\">Create New</button>'\r\n    modelbodystring += ' <button type=\"button\" class=\"btn btn-primary\" data-dismiss=\"modal\" onclick=\"return addRecord('+\"'\"+url+\"'\"+');\">Add Existing</button>'\r\n    modelbodystring += '</div>'\r\n    modelbodystring += '</div>'\r\n    modelbodystring += '</div>'\r\n    modelbodystring += '</div>'\r\n \r\n    $(\".modal .modal-title\").html(\"Create\");\r\n    $(\".modelpop\").html(modelbodystring);\r\n    $(\".modal\").modal(\"show\");\r\n   // $(\".modal .modal-body\").load(\"Create Data\");\r\n    //});\r\n};\r\n \r\nfunction EditTable(id) {\r\n    var d = window.editUrl;\r\n    if(!d.endsWith(id+\"/\"))\r\n    \td=d+id\r\n    $.ajax({\r\n        url: d ,\r\n        type: \"GET\",\r\n        contentType: \"application/json;charset=UTF-8\",\r\n        dataType: \"json\",\r\n        success: function (resultdata) {\r\n        \t\r\n            var modelbodystring = \"\";\r\n            modelbodystring += '<div class=\"modal fade\" tabindex=\"-1\" role=\"dialog\">'\r\n            modelbodystring += '<div class=\"modal-dialog\">'\r\n            modelbodystring += '<form id=\"updateForm\" action=\"\" method=\"PUT\"> <div class=\"modal-content\">'\r\n            modelbodystring += ' <div class=\"modal-header\">'\r\n            modelbodystring += '  <button type=\"button\" class=\"close\" data-dismiss=\"modal\" aria-label=\"Close\"><span aria-hidden=\"true\">?</span></button>'\r\n            modelbodystring += '<h4 class=\"modal-title\"></h4>'\r\n            modelbodystring += ' </div>'\r\n            modelbodystring += ' <div class=\"modal-body\">'\r\n \r\n            var loop = 0;\r\n            $.each(resultdata, function (i, v) {\r\n                if (loop == 0) {\r\n                    modelbodystring += '<div class=\"col-sm-12 modeldata\" style=\"display:none\">'\r\n                    modelbodystring += '<div class=\"col-sm-3\">'\r\n                    modelbodystring += '<label>' + i + '</label>'\r\n                    modelbodystring += '</div>'\r\n                    modelbodystring += '<div class=\"col-sm-9\">'\r\n                    if(i=='date')\r\n                       \tmodelbodystring += '<input class=\"form-control\" type=\"date\" name=' + i + ' id=' + i + ' value=\"' + v + '\"/>'\r\n                    else{\r\n                    if (v.length >= 20) {\r\n                        modelbodystring += '<textarea rows=\"4\" cols=\"50\" class=\"form-control\" type=\"text\" name=' + i + ' id=' + i + '/>'\r\n                        modelbodystring += v \r\n                        modelbodystring += '</textarea >'\r\n                    }\r\n                    else {\r\n                        modelbodystring += '<input class=\"form-control\" type=\"text\" name=' + i + ' id=' + i + ' value=\"' + v + '\"/>'\r\n                    }}\r\n                    modelbodystring += '</div>'\r\n                    modelbodystring += '</div>'\r\n                    loop++;\r\n                }\r\n                else {\r\n                \tif(i=='_links');else{\r\n                    modelbodystring += '<div class=\"col-sm-12 modeldata\">'\r\n                    modelbodystring += '<div class=\"col-sm-3\">'\r\n                    modelbodystring += '<label>' + i + '</label>'\r\n                    modelbodystring += '</div>'\r\n                    modelbodystring += '<div class=\"col-sm-9\">'\r\n                    \r\n                    if(v==null){\r\n                    \tv=\"\";\r\n                    }if(i=='date')\r\n                       \tmodelbodystring += '<input class=\"form-control\" type=\"date\" name=' + i + ' id=' + i + ' value=\"' + v + '\"/>'\r\n                        else{\r\n                    if (v.length >= 20) {\r\n                        modelbodystring += '<textarea rows=\"4\" cols=\"50\" class=\"form-control\" type=\"text\" name=' + i + ' id=' + i + '>'\r\n                        modelbodystring += v\r\n                        modelbodystring += '</textarea >'\r\n                    }\r\n                    else {\r\n                        modelbodystring += '<input class=\"form-control\" type=\"text\" name=' + i + ' id=' + i + ' value=\"' + v + '\"/>'\r\n                    }}\r\n                    modelbodystring += '</div>'\r\n                    modelbodystring += '</div>'\r\n                \t}\r\n                }\r\n            });\r\n            modelbodystring += '</div >'\r\n            modelbodystring += ' <div class=\"modal-footer\">'\r\n            modelbodystring += ' <button type=\"button\" class=\"btn btn-default\" data-dismiss=\"modal\">Close</button>'\r\n            modelbodystring += ' <button type=\"button\" class=\"btn btn-primary\" data-dismiss=\"modal\" onclick=\"return updateRecord('+\"'\"+id+\"'\"+');\">Ok</button>'\r\n            modelbodystring += '</div>'\r\n            modelbodystring += '</div></form>'\r\n            modelbodystring += '</div>'\r\n            modelbodystring += '</div>'\r\n \r\n            \t\r\n            $(\".modal .modal-title\").html(\"Edit\");\r\n            $(\".modelpop\").html(modelbodystring);\r\n            $(\".modal\").modal(\"show\");\r\n //           $(\".modal .modal-body\").load(\"Edit Data\");\r\n            $('body').removeClass(\"loading\"); \r\n        },\r\n        error: function (errormessage) {\r\n        \t$('body').removeClass(\"loading\");\r\n            alert(errormessage.responseText);\r\n        }\r\n    });\r\n};\r\n\r\n\r\nfunction updateRecord(id){\r\n\tvar formData = {};\r\n\t//var serialize = require('form-serialize');\r\n\t//var form = document.querySelector('#updateForm');\r\n\tformData=serialize(document.getElementById(\"updateForm\"),{ hash: true });//.serialize();\r\n\tformData=JSON.stringify(formData);\r\n\t//alert(formData);\r\n\tvar d = window.editUrl;\r\n\tif(!d.endsWith(id+\"/\"))\r\n    \td=d+id\r\n\t$.ajax({\r\n        url: d, // url where to submit the request\r\n        type : \"PUT\", // type of action POST || GET\r\n        contentType: 'application/json',\r\n        dataType : 'json', // data type\r\n        data : formData, // post data || get data\r\n        success : function(result) {\r\n        \t\r\n            // you can see the result from the console\r\n            // tab of the developer tools\r\n            console.log(result);\r\n            $('body').removeClass(\"loading\");\r\n        },\r\n        error: function(errormessage) {\r\n        \t$('body').removeClass(\"loading\");\r\n        \talert(errormessage.responseText);\r\n        }\r\n    })\r\n    $(\".modal-backdrop\").remove();\r\n    setTimeout('dynamic_table(\\'' + window.entityUrl + '\\',\\''+window.editUrl+'\\' );', 100);\r\n    \r\n\treturn false;\r\n}\r\n\r\nfunction addRecord(url){\r\n\tvar formData = {};\r\n\t//var serialize = require('form-serialize');\r\n\t//var form = document.querySelector('#updateForm');\r\n\tformData=serialize(document.getElementById(\"createForm\"),{ hash: true });//.serialize();\r\n\tformData=JSON.stringify(formData);\r\n\t//alert(formData);\r\n\tvar d = url;\r\n\t$.ajax({\r\n        url: d, // url where to submit the request\r\n        type : \"PATCH\", // type of action POST || GET\r\n        contentType: 'application/json',\r\n        dataType : 'json', // data type\r\n        data : formData, // post data || get data\r\n        success : function(result) {\r\n        \t\r\n            // you can see the result from the console\r\n            // tab of the developer tools\r\n            console.log(result);\r\n            $('body').removeClass(\"loading\");\r\n        },\r\n        error: function(errormessage) {\r\n        \t$('body').removeClass(\"loading\");\r\n        \talert(errormessage.responseText);\r\n        }\r\n    });\r\n\t$(\".modal-backdrop\").remove();\r\n    setTimeout('dynamic_table(\\'' + window.entityUrl + '\\',\\''+window.editUrl+'\\' );', 100);\r\n    \r\n\treturn false;\r\n}\r\n\r\nfunction createRecord(url){\r\n\tvar formData = {};\r\n\t//var serialize = require('form-serialize');\r\n\t//var form = document.querySelector('#updateForm');\r\n\tformData=serialize(document.getElementById(\"createForm\"),{ hash: true });//.serialize();\r\n\tformData=JSON.stringify(formData);\r\n\t//alert(formData);\r\n\tvar d = url;\r\n\t$.ajax({\r\n        url: d, // url where to submit the request\r\n        type : \"POST\", // type of action POST || GET\r\n        contentType: 'application/json',\r\n        dataType : 'json', // data type\r\n        data : formData, // post data || get data\r\n        success : function(result) {\r\n            // you can see the result from the console\r\n            // tab of the developer tools\r\n            console.log(result);\r\n            $('body').removeClass(\"loading\");\r\n        },\r\n        error: function(errormessage) {\r\n        \t$('body').removeClass(\"loading\");\r\n        \talert(errormessage.responseText);\r\n        }\r\n    });\r\n\t$(\".modal-backdrop\").remove();\r\n    setTimeout('dynamic_table(\\'' + window.entityUrl + '\\',\\''+window.editUrl+'\\' );', 100);\r\n    \r\n\treturn false;\r\n}\r\n\r\nfunction deleteRecord(id){\r\n\tvar d = window.editUrl;\r\n\tif(!d.endsWith(id+\"/\"))\r\n    \td=d+id\r\n\t$.ajax({\r\n        url: d, // url where to submit the request\r\n        type : \"DELETE\", // type of action POST || GET\r\n        contentType: 'application/json',\r\n        dataType : 'json', // data type\r\n        success : function(result) {\r\n            // you can see the result from the console\r\n            // tab of the developer tools\r\n           // console.log(result);\r\n        \t$('body').removeClass(\"loading\");\r\n        },\r\n        error: function(errormessage) {\r\n        \t$('body').removeClass(\"loading\");\r\n        \tif(errormessage.responseText!=\"\")\r\n        \t\talert(errormessage.responseText);\r\n        }\r\n    })\r\n    $(\".modal-backdrop\").remove();\r\n    setTimeout('dynamic_table(\\'' + window.entityUrl + '\\',\\''+window.editUrl+'\\' );', 100);\r\n    \r\n\treturn false;\r\n}\r\n\r\n//get successful control from form and assemble into object\r\n//http://www.w3.org/TR/html401/interact/forms.html#h-17.13.2\r\n\r\n//types which indicate a submit action and are not successful controls\r\n//these will be ignored\r\nvar k_r_submitter = /^(?:submit|button|image|reset|file)$/i;\r\n\r\n//node names which could be successful controls\r\nvar k_r_success_contrls = /^(?:input|select|textarea|keygen)/i;\r\n\r\n//Matches bracket notation.\r\nvar brackets = /(\\[[^\\[\\]]*\\])/g;\r\n\r\n//serializes form fields\r\n//@param form MUST be an HTMLForm element\r\n//@param options is an optional argument to configure the serialization. Default output\r\n//with no options specified is a url encoded string\r\n// - hash: [true | false] Configure the output type. If true, the output will\r\n// be a js object.\r\n// - serializer: [function] Optional serializer function to override the default one.\r\n// The function takes 3 arguments (result, key, value) and should return new result\r\n// hash and url encoded str serializers are provided with this module\r\n// - disabled: [true | false]. If true serialize disabled fields.\r\n// - empty: [true | false]. If true serialize empty fields\r\nfunction serialize(form, options) {\r\n if (typeof options != 'object') {\r\n     options = { hash: !!options };\r\n }\r\n else if (options.hash === undefined) {\r\n     options.hash = true;\r\n }\r\n\r\n var result = (options.hash) ? {} : '';\r\n var serializer = options.serializer || ((options.hash) ? hash_serializer : str_serialize);\r\n\r\n var elements = form && form.elements ? form.elements : [];\r\n\r\n //Object store each radio and set if it's empty or not\r\n var radio_store = Object.create(null);\r\n\r\n for (var i=0 ; i<elements.length ; ++i) {\r\n     var element = elements[i];\r\n\r\n     // ingore disabled fields\r\n     if ((!options.disabled && element.disabled) || !element.name) {\r\n         continue;\r\n     }\r\n     // ignore anyhting that is not considered a success field\r\n     if (!k_r_success_contrls.test(element.nodeName) ||\r\n         k_r_submitter.test(element.type)) {\r\n         continue;\r\n     }\r\n\r\n     var key = element.name;\r\n     var val = element.value;\r\n\r\n     // we can't just use element.value for checkboxes cause some browsers lie to us\r\n     // they say \"on\" for value when the box isn't checked\r\n     if ((element.type === 'checkbox' || element.type === 'radio') && !element.checked) {\r\n         val = undefined;\r\n     }\r\n\r\n     // If we want empty elements\r\n     if (options.empty) {\r\n         // for checkbox\r\n         if (element.type === 'checkbox' && !element.checked) {\r\n             val = '';\r\n         }\r\n\r\n         // for radio\r\n         if (element.type === 'radio') {\r\n             if (!radio_store[element.name] && !element.checked) {\r\n                 radio_store[element.name] = false;\r\n             }\r\n             else if (element.checked) {\r\n                 radio_store[element.name] = true;\r\n             }\r\n         }\r\n\r\n         // if options empty is true, continue only if its radio\r\n         if (val == undefined && element.type == 'radio') {\r\n             continue;\r\n         }\r\n     }\r\n     else {\r\n         // value-less fields are ignored unless options.empty is true\r\n         if (!val) {\r\n             continue;\r\n         }\r\n     }\r\n\r\n     // multi select boxes\r\n     if (element.type === 'select-multiple') {\r\n         val = [];\r\n\r\n         var selectOptions = element.options;\r\n         var isSelectedOptions = false;\r\n         for (var j=0 ; j<selectOptions.length ; ++j) {\r\n             var option = selectOptions[j];\r\n             var allowedEmpty = options.empty && !option.value;\r\n             var hasValue = (option.value || allowedEmpty);\r\n             if (option.selected && hasValue) {\r\n                 isSelectedOptions = true;\r\n\r\n                 // If using a hash serializer be sure to add the\r\n                 // correct notation for an array in the multi-select\r\n                 // context. Here the name attribute on the select element\r\n                 // might be missing the trailing bracket pair. Both names\r\n                 // \"foo\" and \"foo[]\" should be arrays.\r\n                 if (options.hash && key.slice(key.length - 2) !== '[]') {\r\n                     result = serializer(result, key + '[]', option.value);\r\n                 }\r\n                 else {\r\n                     result = serializer(result, key, option.value);\r\n                 }\r\n             }\r\n         }\r\n\r\n         // Serialize if no selected options and options.empty is true\r\n         if (!isSelectedOptions && options.empty) {\r\n             result = serializer(result, key, '');\r\n         }\r\n\r\n         continue;\r\n     }\r\n\r\n     result = serializer(result, key, val);\r\n }\r\n\r\n // Check for all empty radio buttons and serialize them with key=\"\"\r\n if (options.empty) {\r\n     for (var key in radio_store) {\r\n         if (!radio_store[key]) {\r\n             result = serializer(result, key, '');\r\n         }\r\n     }\r\n }\r\n\r\n return result;\r\n}\r\n\r\nfunction parse_keys(string) {\r\n var keys = [];\r\n var prefix = /^([^\\[\\]]*)/;\r\n var children = new RegExp(brackets);\r\n var match = prefix.exec(string);\r\n\r\n if (match[1]) {\r\n     keys.push(match[1]);\r\n }\r\n\r\n while ((match = children.exec(string)) !== null) {\r\n     keys.push(match[1]);\r\n }\r\n\r\n return keys;\r\n}\r\n\r\nfunction hash_assign(result, keys, value) {\r\n if (keys.length === 0) {\r\n     result = value;\r\n     return result;\r\n }\r\n\r\n var key = keys.shift();\r\n var between = key.match(/^\\[(.+?)\\]$/);\r\n\r\n if (key === '[]') {\r\n     result = result || [];\r\n\r\n     if (Array.isArray(result)) {\r\n         result.push(hash_assign(null, keys, value));\r\n     }\r\n     else {\r\n         // This might be the result of bad name attributes like \"[][foo]\",\r\n         // in this case the original `result` object will already be\r\n         // assigned to an object literal. Rather than coerce the object to\r\n         // an array, or cause an exception the attribute \"_values\" is\r\n         // assigned as an array.\r\n         result._values = result._values || [];\r\n         result._values.push(hash_assign(null, keys, value));\r\n     }\r\n\r\n     return result;\r\n }\r\n\r\n // Key is an attribute name and can be assigned directly.\r\n if (!between) {\r\n     result[key] = hash_assign(result[key], keys, value);\r\n }\r\n else {\r\n     var string = between[1];\r\n     // +var converts the variable into a number\r\n     // better than parseInt because it doesn't truncate away trailing\r\n     // letters and actually fails if whole thing is not a number\r\n     var index = +string;\r\n\r\n     // If the characters between the brackets is not a number it is an\r\n     // attribute name and can be assigned directly.\r\n     if (isNaN(index)) {\r\n         result = result || {};\r\n         result[string] = hash_assign(result[string], keys, value);\r\n     }\r\n     else {\r\n         result = result || [];\r\n         result[index] = hash_assign(result[index], keys, value);\r\n     }\r\n }\r\n\r\n return result;\r\n}\r\n\r\n//Object/hash encoding serializer.\r\nfunction hash_serializer(result, key, value) {\r\n var matches = key.match(brackets);\r\n\r\n // Has brackets? Use the recursive assignment function to walk the keys,\r\n // construct any missing objects in the result tree and make the assignment\r\n // at the end of the chain.\r\n if (matches) {\r\n     var keys = parse_keys(key);\r\n     hash_assign(result, keys, value);\r\n }\r\n else {\r\n     // Non bracket notation can make assignments directly.\r\n     var existing = result[key];\r\n\r\n     // If the value has been assigned already (for instance when a radio and\r\n     // a checkbox have the same name attribute) convert the previous value\r\n     // into an array before pushing into it.\r\n     //\r\n     // NOTE: If this requirement were removed all hash creation and\r\n     // assignment could go through `hash_assign`.\r\n     if (existing) {\r\n         if (!Array.isArray(existing)) {\r\n             result[key] = [ existing ];\r\n         }\r\n\r\n         result[key].push(value);\r\n     }\r\n     else {\r\n         result[key] = value;\r\n     }\r\n }\r\n\r\n return result;\r\n}\r\n\r\n//urlform encoding serializer\r\nfunction str_serialize(result, key, value) {\r\n // encode newlines as \\r\\n cause the html spec says so\r\n value = value.replace(/(\\r)?\\n/g, '\\r\\n');\r\n value = encodeURIComponent(value);\r\n\r\n // spaces should be '+' rather than '%20'.\r\n value = value.replace(/%20/g, '+');\r\n return result + (result ? '&' : '') + encodeURIComponent(key) + '=' + value;\r\n}\r\n\r\n");
		
		cssText = new Text(composite_2, SWT.BORDER | SWT.MULTI);
		cssText.setText("/* Start by setting display:none to make this hidden.\r\n   Then we position it in relation to the viewport window\r\n   with position:fixed. Width, height, top and left speak\r\n   for themselves. Background we set to 80% white with\r\n   our animation centered, and no-repeating */\r\n.modal-processing {\r\n    display:    none;\r\n    position:   fixed;\r\n    z-index:    1000;\r\n    top:        0;\r\n    left:       0;\r\n    height:     100%;\r\n    width:      100%;\r\n    background: rgba( 255, 255, 255, .8 ) \r\n                url('http://i.stack.imgur.com/FhHRx.gif') \r\n                50% 50% \r\n                no-repeat;\r\n}\r\n\r\n/* When the body has the loading class, we turn\r\n   the scrollbar off with overflow:hidden */\r\nbody.loading .modal-processing {\r\n    overflow: hidden;   \r\n}\r\n\r\n/* Anytime the body has the loading class, our\r\n   modal element will be visible */\r\nbody.loading .modal-processing {\r\n    display: block;\r\n}\r\n\r\n\r\n\r\n.center {\r\n  display: flex;\r\n  justify-content: center;\r\n  align-items: center;\r\n  height: 80px;\r\n  border: 2px solid grey;\r\nmargin:20px; \r\n}\r\n\r\n.refresh-right {\r\n  float: right;\r\n}\r\n\r\n.button {\r\n  border: none;\r\n  color: white;\r\n  padding: 14px 24px;\r\n  text-align: center;\r\n  text-decoration: none;\r\n  display: inline-block;\r\n  font-size: 16px;\r\n  margin: 4px 2px;\r\n  transition-duration: 0.4s;\r\n  cursor: pointer;\r\n  background-color: white; \r\n  color: black; \r\n  border: 2px solid #008CBA;\r\n}\r\n\r\n.button:hover {\r\n  background-color: #008CBA;\r\n  color: white;\r\n}\r\n\r\n.center button{\r\n\r\nmargin:10px;\r\n\r\n}\r\n\r\n.col-sm-12.modeldata {\r\n    padding-top: 19px;\r\n    padding-bottom: 7px;\r\n}\r\n \r\n.addme {\r\n    margin-bottom: 11px;\r\n    background-color: #d0e4f5;\r\n    border: 0px solid #ccc;\r\n    padding: 12px;\r\n    box-shadow: 0 0 1px black;\r\n}\r\n \r\n.modal-body {\r\n    padding: 0px !important;\r\n}\r\n \r\n.modal-header {\r\n    background: #a2c1dc;\r\n}\r\n \r\ntable.blueTable {\r\n    border: 1px solid #1C6EA4;\r\n    background-color: #EEEEEE;\r\n    width: 100%;\r\n    text-align: left;\r\n    border-collapse: collapse;\r\n}\r\n \r\n    table.blueTable td, table.blueTable th {\r\n        border: 1px solid #AAAAAA;\r\n        padding: 3px 2px;\r\n    }\r\n \r\n    table.blueTable tbody td {\r\n        font-size: 13px;\r\n    }\r\n \r\n    table.blueTable tr:nth-child(even) {\r\n        background: #D0E4F5;\r\n    }\r\n \r\n    table.blueTable thead {\r\n        background: #1C6EA4;\r\n        background: -moz-linear-gradient(top, #5592bb 0%, #327cad 66%, #1C6EA4 100%);\r\n        background: -webkit-linear-gradient(top, #5592bb 0%, #327cad 66%, #1C6EA4 100%);\r\n        background: linear-gradient(to bottom, #5592bb 0%, #327cad 66%, #1C6EA4 100%);\r\n        border-bottom: 2px solid #444444;\r\n    }\r\n \r\n        table.blueTable thead th {\r\n            font-size: 15px;\r\n            font-weight: bold;\r\n            color: #FFFFFF;\r\n            border-left: 2px solid #D0E4F5;\r\n        }\r\n \r\n            table.blueTable thead th:first-child {\r\n                border-left: none;\r\n            }\r\n \r\n    table.blueTable tfoot {\r\n        font-size: 14px;\r\n        font-weight: bold;\r\n        color: #FFFFFF;\r\n        background: #D0E4F5;\r\n        background: -moz-linear-gradient(top, #dcebf7 0%, #d4e6f6 66%, #D0E4F5 100%);\r\n        background: -webkit-linear-gradient(top, #dcebf7 0%, #d4e6f6 66%, #D0E4F5 100%);\r\n        background: linear-gradient(to bottom, #dcebf7 0%, #d4e6f6 66%, #D0E4F5 100%);\r\n        border-top: 2px solid #444444;\r\n    }\r\n \r\n        table.blueTable tfoot td {\r\n            font-size: 14px;\r\n        }\r\n \r\n        table.blueTable tfoot .links {\r\n            text-align: right;\r\n        }\r\n \r\n            table.blueTable tfoot .links a {\r\n                display: inline-block;\r\n                background: #1C6EA4;\r\n                color: #FFFFFF;\r\n                padding: 2px 8px;\r\n                border-radius: 5px;\r\n            }");
		
		TabItem tbtmAbout = new TabItem(tabFolder, SWT.NONE);
		tbtmAbout.setText("About");
		
		Composite composite_1 = new Composite(tabFolder, SWT.NONE);
		tbtmAbout.setControl(composite_1);
		composite_1.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		Browser browser = new Browser(composite_1, SWT.NONE);
		browser.setText("<!DOCTYPE html>\r\n<html>\r\n<title>W3.CSS</title>\r\n<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\r\n\r\n<head>\r\n<style>\r\n/* W3.CSS 4.13 June 2019 by Jan Egil and Borge Refsnes */\r\nhtml{box-sizing:border-box}*,*:before,*:after{box-sizing:inherit}\r\n/* Extract from normalize.css by Nicolas Gallagher and Jonathan Neal git.io/normalize */\r\nhtml{-ms-text-size-adjust:100%;-webkit-text-size-adjust:100%}body{margin:0}\r\narticle,aside,details,figcaption,figure,footer,header,main,menu,nav,section{display:block}summary{display:list-item}\r\naudio,canvas,progress,video{display:inline-block}progress{vertical-align:baseline}\r\naudio:not([controls]){display:none;height:0}[hidden],template{display:none}\r\na{background-color:transparent}a:active,a:hover{outline-width:0}\r\nabbr[title]{border-bottom:none;text-decoration:underline;text-decoration:underline dotted}\r\nb,strong{font-weight:bolder}dfn{font-style:italic}mark{background:#ff0;color:#000}\r\nsmall{font-size:80%}sub,sup{font-size:75%;line-height:0;position:relative;vertical-align:baseline}\r\nsub{bottom:-0.25em}sup{top:-0.5em}figure{margin:1em 40px}img{border-style:none}\r\ncode,kbd,pre,samp{font-family:monospace,monospace;font-size:1em}hr{box-sizing:content-box;height:0;overflow:visible}\r\nbutton,input,select,textarea,optgroup{font:inherit;margin:0}optgroup{font-weight:bold}\r\nbutton,input{overflow:visible}button,select{text-transform:none}\r\nbutton,[type=button],[type=reset],[type=submit]{-webkit-appearance:button}\r\nbutton::-moz-focus-inner,[type=button]::-moz-focus-inner,[type=reset]::-moz-focus-inner,[type=submit]::-moz-focus-inner{border-style:none;padding:0}\r\nbutton:-moz-focusring,[type=button]:-moz-focusring,[type=reset]:-moz-focusring,[type=submit]:-moz-focusring{outline:1px dotted ButtonText}\r\nfieldset{border:1px solid #c0c0c0;margin:0 2px;padding:.35em .625em .75em}\r\nlegend{color:inherit;display:table;max-width:100%;padding:0;white-space:normal}textarea{overflow:auto}\r\n[type=checkbox],[type=radio]{padding:0}\r\n[type=number]::-webkit-inner-spin-button,[type=number]::-webkit-outer-spin-button{height:auto}\r\n[type=search]{-webkit-appearance:textfield;outline-offset:-2px}\r\n[type=search]::-webkit-search-decoration{-webkit-appearance:none}\r\n::-webkit-file-upload-button{-webkit-appearance:button;font:inherit}\r\n/* End extract */\r\nhtml,body{font-family:Verdana,sans-serif;font-size:15px;line-height:1.5}html{overflow-x:hidden}\r\nh1{font-size:36px}h2{font-size:30px}h3{font-size:24px}h4{font-size:20px}h5{font-size:18px}h6{font-size:16px}.w3-serif{font-family:serif}\r\nh1,h2,h3,h4,h5,h6{font-family:\"Segoe UI\",Arial,sans-serif;font-weight:400;margin:10px 0}.w3-wide{letter-spacing:4px}\r\nhr{border:0;border-top:1px solid #eee;margin:20px 0}\r\n.w3-image{max-width:100%;height:auto}img{vertical-align:middle}a{color:inherit}\r\n.w3-table,.w3-table-all{border-collapse:collapse;border-spacing:0;width:100%;display:table}.w3-table-all{border:1px solid #ccc}\r\n.w3-bordered tr,.w3-table-all tr{border-bottom:1px solid #ddd}.w3-striped tbody tr:nth-child(even){background-color:#f1f1f1}\r\n.w3-table-all tr:nth-child(odd){background-color:#fff}.w3-table-all tr:nth-child(even){background-color:#f1f1f1}\r\n.w3-hoverable tbody tr:hover,.w3-ul.w3-hoverable li:hover{background-color:#ccc}.w3-centered tr th,.w3-centered tr td{text-align:center}\r\n.w3-table td,.w3-table th,.w3-table-all td,.w3-table-all th{padding:8px 8px;display:table-cell;text-align:left;vertical-align:top}\r\n.w3-table th:first-child,.w3-table td:first-child,.w3-table-all th:first-child,.w3-table-all td:first-child{padding-left:16px}\r\n.w3-btn,.w3-button{border:none;display:inline-block;padding:8px 16px;vertical-align:middle;overflow:hidden;text-decoration:none;color:inherit;background-color:inherit;text-align:center;cursor:pointer;white-space:nowrap}\r\n.w3-btn:hover{box-shadow:0 8px 16px 0 rgba(0,0,0,0.2),0 6px 20px 0 rgba(0,0,0,0.19)}\r\n.w3-btn,.w3-button{-webkit-touch-callout:none;-webkit-user-select:none;-khtml-user-select:none;-moz-user-select:none;-ms-user-select:none;user-select:none}   \r\n.w3-disabled,.w3-btn:disabled,.w3-button:disabled{cursor:not-allowed;opacity:0.3}.w3-disabled *,:disabled *{pointer-events:none}\r\n.w3-btn.w3-disabled:hover,.w3-btn:disabled:hover{box-shadow:none}\r\n.w3-badge,.w3-tag{background-color:#000;color:#fff;display:inline-block;padding-left:8px;padding-right:8px;text-align:center}.w3-badge{border-radius:50%}\r\n.w3-ul{list-style-type:none;padding:0;margin:0}.w3-ul li{padding:8px 16px;border-bottom:1px solid #ddd}.w3-ul li:last-child{border-bottom:none}\r\n.w3-tooltip,.w3-display-container{position:relative}.w3-tooltip .w3-text{display:none}.w3-tooltip:hover .w3-text{display:inline-block}\r\n.w3-ripple:active{opacity:0.5}.w3-ripple{transition:opacity 0s}\r\n.w3-input{padding:8px;display:block;border:none;border-bottom:1px solid #ccc;width:100%}\r\n.w3-select{padding:9px 0;width:100%;border:none;border-bottom:1px solid #ccc}\r\n.w3-dropdown-click,.w3-dropdown-hover{position:relative;display:inline-block;cursor:pointer}\r\n.w3-dropdown-hover:hover .w3-dropdown-content{display:block}\r\n.w3-dropdown-hover:first-child,.w3-dropdown-click:hover{background-color:#ccc;color:#000}\r\n.w3-dropdown-hover:hover > .w3-button:first-child,.w3-dropdown-click:hover > .w3-button:first-child{background-color:#ccc;color:#000}\r\n.w3-dropdown-content{cursor:auto;color:#000;background-color:#fff;display:none;position:absolute;min-width:160px;margin:0;padding:0;z-index:1}\r\n.w3-check,.w3-radio{width:24px;height:24px;position:relative;top:6px}\r\n.w3-sidebar{height:100%;width:200px;background-color:#fff;position:fixed!important;z-index:1;overflow:auto}\r\n.w3-bar-block .w3-dropdown-hover,.w3-bar-block .w3-dropdown-click{width:100%}\r\n.w3-bar-block .w3-dropdown-hover .w3-dropdown-content,.w3-bar-block .w3-dropdown-click .w3-dropdown-content{min-width:100%}\r\n.w3-bar-block .w3-dropdown-hover .w3-button,.w3-bar-block .w3-dropdown-click .w3-button{width:100%;text-align:left;padding:8px 16px}\r\n.w3-main,#main{transition:margin-left .4s}\r\n.w3-modal{z-index:3;display:none;padding-top:100px;position:fixed;left:0;top:0;width:100%;height:100%;overflow:auto;background-color:rgb(0,0,0);background-color:rgba(0,0,0,0.4)}\r\n.w3-modal-content{margin:auto;background-color:#fff;position:relative;padding:0;outline:0;width:600px}\r\n.w3-bar{width:100%;overflow:hidden}.w3-center .w3-bar{display:inline-block;width:auto}\r\n.w3-bar .w3-bar-item{padding:8px 16px;float:left;width:auto;border:none;display:block;outline:0}\r\n.w3-bar .w3-dropdown-hover,.w3-bar .w3-dropdown-click{position:static;float:left}\r\n.w3-bar .w3-button{white-space:normal}\r\n.w3-bar-block .w3-bar-item{width:100%;display:block;padding:8px 16px;text-align:left;border:none;white-space:normal;float:none;outline:0}\r\n.w3-bar-block.w3-center .w3-bar-item{text-align:center}.w3-block{display:block;width:100%}\r\n.w3-responsive{display:block;overflow-x:auto}\r\n.w3-container:after,.w3-container:before,.w3-panel:after,.w3-panel:before,.w3-row:after,.w3-row:before,.w3-row-padding:after,.w3-row-padding:before,\r\n.w3-cell-row:before,.w3-cell-row:after,.w3-clear:after,.w3-clear:before,.w3-bar:before,.w3-bar:after{content:\"\";display:table;clear:both}\r\n.w3-col,.w3-half,.w3-third,.w3-twothird,.w3-threequarter,.w3-quarter{float:left;width:100%}\r\n.w3-col.s1{width:8.33333%}.w3-col.s2{width:16.66666%}.w3-col.s3{width:24.99999%}.w3-col.s4{width:33.33333%}\r\n.w3-col.s5{width:41.66666%}.w3-col.s6{width:49.99999%}.w3-col.s7{width:58.33333%}.w3-col.s8{width:66.66666%}\r\n.w3-col.s9{width:74.99999%}.w3-col.s10{width:83.33333%}.w3-col.s11{width:91.66666%}.w3-col.s12{width:99.99999%}\r\n@media (min-width:601px){.w3-col.m1{width:8.33333%}.w3-col.m2{width:16.66666%}.w3-col.m3,.w3-quarter{width:24.99999%}.w3-col.m4,.w3-third{width:33.33333%}\r\n.w3-col.m5{width:41.66666%}.w3-col.m6,.w3-half{width:49.99999%}.w3-col.m7{width:58.33333%}.w3-col.m8,.w3-twothird{width:66.66666%}\r\n.w3-col.m9,.w3-threequarter{width:74.99999%}.w3-col.m10{width:83.33333%}.w3-col.m11{width:91.66666%}.w3-col.m12{width:99.99999%}}\r\n@media (min-width:993px){.w3-col.l1{width:8.33333%}.w3-col.l2{width:16.66666%}.w3-col.l3{width:24.99999%}.w3-col.l4{width:33.33333%}\r\n.w3-col.l5{width:41.66666%}.w3-col.l6{width:49.99999%}.w3-col.l7{width:58.33333%}.w3-col.l8{width:66.66666%}\r\n.w3-col.l9{width:74.99999%}.w3-col.l10{width:83.33333%}.w3-col.l11{width:91.66666%}.w3-col.l12{width:99.99999%}}\r\n.w3-rest{overflow:hidden}.w3-stretch{margin-left:-16px;margin-right:-16px}\r\n.w3-content,.w3-auto{margin-left:auto;margin-right:auto}.w3-content{max-width:980px}.w3-auto{max-width:1140px}\r\n.w3-cell-row{display:table;width:100%}.w3-cell{display:table-cell}\r\n.w3-cell-top{vertical-align:top}.w3-cell-middle{vertical-align:middle}.w3-cell-bottom{vertical-align:bottom}\r\n.w3-hide{display:none!important}.w3-show-block,.w3-show{display:block!important}.w3-show-inline-block{display:inline-block!important}\r\n@media (max-width:1205px){.w3-auto{max-width:95%}}\r\n@media (max-width:600px){.w3-modal-content{margin:0 10px;width:auto!important}.w3-modal{padding-top:30px}\r\n.w3-dropdown-hover.w3-mobile .w3-dropdown-content,.w3-dropdown-click.w3-mobile .w3-dropdown-content{position:relative}\t\r\n.w3-hide-small{display:none!important}.w3-mobile{display:block;width:100%!important}.w3-bar-item.w3-mobile,.w3-dropdown-hover.w3-mobile,.w3-dropdown-click.w3-mobile{text-align:center}\r\n.w3-dropdown-hover.w3-mobile,.w3-dropdown-hover.w3-mobile .w3-btn,.w3-dropdown-hover.w3-mobile .w3-button,.w3-dropdown-click.w3-mobile,.w3-dropdown-click.w3-mobile .w3-btn,.w3-dropdown-click.w3-mobile .w3-button{width:100%}}\r\n@media (max-width:768px){.w3-modal-content{width:500px}.w3-modal{padding-top:50px}}\r\n@media (min-width:993px){.w3-modal-content{width:900px}.w3-hide-large{display:none!important}.w3-sidebar.w3-collapse{display:block!important}}\r\n@media (max-width:992px) and (min-width:601px){.w3-hide-medium{display:none!important}}\r\n@media (max-width:992px){.w3-sidebar.w3-collapse{display:none}.w3-main{margin-left:0!important;margin-right:0!important}.w3-auto{max-width:100%}}\r\n.w3-top,.w3-bottom{position:fixed;width:100%;z-index:1}.w3-top{top:0}.w3-bottom{bottom:0}\r\n.w3-overlay{position:fixed;display:none;width:100%;height:100%;top:0;left:0;right:0;bottom:0;background-color:rgba(0,0,0,0.5);z-index:2}\r\n.w3-display-topleft{position:absolute;left:0;top:0}.w3-display-topright{position:absolute;right:0;top:0}\r\n.w3-display-bottomleft{position:absolute;left:0;bottom:0}.w3-display-bottomright{position:absolute;right:0;bottom:0}\r\n.w3-display-middle{position:absolute;top:50%;left:50%;transform:translate(-50%,-50%);-ms-transform:translate(-50%,-50%)}\r\n.w3-display-left{position:absolute;top:50%;left:0%;transform:translate(0%,-50%);-ms-transform:translate(-0%,-50%)}\r\n.w3-display-right{position:absolute;top:50%;right:0%;transform:translate(0%,-50%);-ms-transform:translate(0%,-50%)}\r\n.w3-display-topmiddle{position:absolute;left:50%;top:0;transform:translate(-50%,0%);-ms-transform:translate(-50%,0%)}\r\n.w3-display-bottommiddle{position:absolute;left:50%;bottom:0;transform:translate(-50%,0%);-ms-transform:translate(-50%,0%)}\r\n.w3-display-container:hover .w3-display-hover{display:block}.w3-display-container:hover span.w3-display-hover{display:inline-block}.w3-display-hover{display:none}\r\n.w3-display-position{position:absolute}\r\n.w3-circle{border-radius:50%}\r\n.w3-round-small{border-radius:2px}.w3-round,.w3-round-medium{border-radius:4px}.w3-round-large{border-radius:8px}.w3-round-xlarge{border-radius:16px}.w3-round-xxlarge{border-radius:32px}\r\n.w3-row-padding,.w3-row-padding>.w3-half,.w3-row-padding>.w3-third,.w3-row-padding>.w3-twothird,.w3-row-padding>.w3-threequarter,.w3-row-padding>.w3-quarter,.w3-row-padding>.w3-col{padding:0 8px}\r\n.w3-container,.w3-panel{padding:0.01em 16px}.w3-panel{margin-top:16px;margin-bottom:16px}\r\n.w3-code,.w3-codespan{font-family:Consolas,\"courier new\";font-size:16px}\r\n.w3-code{width:auto;background-color:#fff;padding:8px 12px;border-left:4px solid #4CAF50;word-wrap:break-word}\r\n.w3-codespan{color:crimson;background-color:#f1f1f1;padding-left:4px;padding-right:4px;font-size:110%}\r\n.w3-card,.w3-card-2{box-shadow:0 2px 5px 0 rgba(0,0,0,0.16),0 2px 10px 0 rgba(0,0,0,0.12)}\r\n.w3-card-4,.w3-hover-shadow:hover{box-shadow:0 4px 10px 0 rgba(0,0,0,0.2),0 4px 20px 0 rgba(0,0,0,0.19)}\r\n.w3-spin{animation:w3-spin 2s infinite linear}@keyframes w3-spin{0%{transform:rotate(0deg)}100%{transform:rotate(359deg)}}\r\n.w3-animate-fading{animation:fading 10s infinite}@keyframes fading{0%{opacity:0}50%{opacity:1}100%{opacity:0}}\r\n.w3-animate-opacity{animation:opac 0.8s}@keyframes opac{from{opacity:0} to{opacity:1}}\r\n.w3-animate-top{position:relative;animation:animatetop 0.4s}@keyframes animatetop{from{top:-300px;opacity:0} to{top:0;opacity:1}}\r\n.w3-animate-left{position:relative;animation:animateleft 0.4s}@keyframes animateleft{from{left:-300px;opacity:0} to{left:0;opacity:1}}\r\n.w3-animate-right{position:relative;animation:animateright 0.4s}@keyframes animateright{from{right:-300px;opacity:0} to{right:0;opacity:1}}\r\n.w3-animate-bottom{position:relative;animation:animatebottom 0.4s}@keyframes animatebottom{from{bottom:-300px;opacity:0} to{bottom:0;opacity:1}}\r\n.w3-animate-zoom {animation:animatezoom 0.6s}@keyframes animatezoom{from{transform:scale(0)} to{transform:scale(1)}}\r\n.w3-animate-input{transition:width 0.4s ease-in-out}.w3-animate-input:focus{width:100%!important}\r\n.w3-opacity,.w3-hover-opacity:hover{opacity:0.60}.w3-opacity-off,.w3-hover-opacity-off:hover{opacity:1}\r\n.w3-opacity-max{opacity:0.25}.w3-opacity-min{opacity:0.75}\r\n.w3-greyscale-max,.w3-grayscale-max,.w3-hover-greyscale:hover,.w3-hover-grayscale:hover{filter:grayscale(100%)}\r\n.w3-greyscale,.w3-grayscale{filter:grayscale(75%)}.w3-greyscale-min,.w3-grayscale-min{filter:grayscale(50%)}\r\n.w3-sepia{filter:sepia(75%)}.w3-sepia-max,.w3-hover-sepia:hover{filter:sepia(100%)}.w3-sepia-min{filter:sepia(50%)}\r\n.w3-tiny{font-size:10px!important}.w3-small{font-size:12px!important}.w3-medium{font-size:15px!important}.w3-large{font-size:18px!important}\r\n.w3-xlarge{font-size:24px!important}.w3-xxlarge{font-size:36px!important}.w3-xxxlarge{font-size:48px!important}.w3-jumbo{font-size:64px!important}\r\n.w3-left-align{text-align:left!important}.w3-right-align{text-align:right!important}.w3-justify{text-align:justify!important}.w3-center{text-align:center!important}\r\n.w3-border-0{border:0!important}.w3-border{border:1px solid #ccc!important}\r\n.w3-border-top{border-top:1px solid #ccc!important}.w3-border-bottom{border-bottom:1px solid #ccc!important}\r\n.w3-border-left{border-left:1px solid #ccc!important}.w3-border-right{border-right:1px solid #ccc!important}\r\n.w3-topbar{border-top:6px solid #ccc!important}.w3-bottombar{border-bottom:6px solid #ccc!important}\r\n.w3-leftbar{border-left:6px solid #ccc!important}.w3-rightbar{border-right:6px solid #ccc!important}\r\n.w3-section,.w3-code{margin-top:16px!important;margin-bottom:16px!important}\r\n.w3-margin{margin:16px!important}.w3-margin-top{margin-top:16px!important}.w3-margin-bottom{margin-bottom:16px!important}\r\n.w3-margin-left{margin-left:16px!important}.w3-margin-right{margin-right:16px!important}\r\n.w3-padding-small{padding:4px 8px!important}.w3-padding{padding:8px 16px!important}.w3-padding-large{padding:12px 24px!important}\r\n.w3-padding-16{padding-top:16px!important;padding-bottom:16px!important}.w3-padding-24{padding-top:24px!important;padding-bottom:24px!important}\r\n.w3-padding-32{padding-top:32px!important;padding-bottom:32px!important}.w3-padding-48{padding-top:48px!important;padding-bottom:48px!important}\r\n.w3-padding-64{padding-top:64px!important;padding-bottom:64px!important}\r\n.w3-left{float:left!important}.w3-right{float:right!important}\r\n.w3-button:hover{color:#000!important;background-color:#ccc!important}\r\n.w3-transparent,.w3-hover-none:hover{background-color:transparent!important}\r\n.w3-hover-none:hover{box-shadow:none!important}\r\n/* Colors */\r\n.w3-amber,.w3-hover-amber:hover{color:#000!important;background-color:#ffc107!important}\r\n.w3-aqua,.w3-hover-aqua:hover{color:#000!important;background-color:#00ffff!important}\r\n.w3-blue,.w3-hover-blue:hover{color:#fff!important;background-color:#2196F3!important}\r\n.w3-light-blue,.w3-hover-light-blue:hover{color:#000!important;background-color:#87CEEB!important}\r\n.w3-brown,.w3-hover-brown:hover{color:#fff!important;background-color:#795548!important}\r\n.w3-cyan,.w3-hover-cyan:hover{color:#000!important;background-color:#00bcd4!important}\r\n.w3-blue-grey,.w3-hover-blue-grey:hover,.w3-blue-gray,.w3-hover-blue-gray:hover{color:#fff!important;background-color:#607d8b!important}\r\n.w3-green,.w3-hover-green:hover{color:#fff!important;background-color:#4CAF50!important}\r\n.w3-light-green,.w3-hover-light-green:hover{color:#000!important;background-color:#8bc34a!important}\r\n.w3-indigo,.w3-hover-indigo:hover{color:#fff!important;background-color:#3f51b5!important}\r\n.w3-khaki,.w3-hover-khaki:hover{color:#000!important;background-color:#f0e68c!important}\r\n.w3-lime,.w3-hover-lime:hover{color:#000!important;background-color:#cddc39!important}\r\n.w3-orange,.w3-hover-orange:hover{color:#000!important;background-color:#ff9800!important}\r\n.w3-deep-orange,.w3-hover-deep-orange:hover{color:#fff!important;background-color:#ff5722!important}\r\n.w3-pink,.w3-hover-pink:hover{color:#fff!important;background-color:#e91e63!important}\r\n.w3-purple,.w3-hover-purple:hover{color:#fff!important;background-color:#9c27b0!important}\r\n.w3-deep-purple,.w3-hover-deep-purple:hover{color:#fff!important;background-color:#673ab7!important}\r\n.w3-red,.w3-hover-red:hover{color:#fff!important;background-color:#f44336!important}\r\n.w3-sand,.w3-hover-sand:hover{color:#000!important;background-color:#fdf5e6!important}\r\n.w3-teal,.w3-hover-teal:hover{color:#fff!important;background-color:#009688!important}\r\n.w3-yellow,.w3-hover-yellow:hover{color:#000!important;background-color:#ffeb3b!important}\r\n.w3-white,.w3-hover-white:hover{color:#000!important;background-color:#fff!important}\r\n.w3-black,.w3-hover-black:hover{color:#fff!important;background-color:#000!important}\r\n.w3-grey,.w3-hover-grey:hover,.w3-gray,.w3-hover-gray:hover{color:#000!important;background-color:#9e9e9e!important}\r\n.w3-light-grey,.w3-hover-light-grey:hover,.w3-light-gray,.w3-hover-light-gray:hover{color:#000!important;background-color:#f1f1f1!important}\r\n.w3-dark-grey,.w3-hover-dark-grey:hover,.w3-dark-gray,.w3-hover-dark-gray:hover{color:#fff!important;background-color:#616161!important}\r\n.w3-pale-red,.w3-hover-pale-red:hover{color:#000!important;background-color:#ffdddd!important}\r\n.w3-pale-green,.w3-hover-pale-green:hover{color:#000!important;background-color:#ddffdd!important}\r\n.w3-pale-yellow,.w3-hover-pale-yellow:hover{color:#000!important;background-color:#ffffcc!important}\r\n.w3-pale-blue,.w3-hover-pale-blue:hover{color:#000!important;background-color:#ddffff!important}\r\n.w3-text-amber,.w3-hover-text-amber:hover{color:#ffc107!important}\r\n.w3-text-aqua,.w3-hover-text-aqua:hover{color:#00ffff!important}\r\n.w3-text-blue,.w3-hover-text-blue:hover{color:#2196F3!important}\r\n.w3-text-light-blue,.w3-hover-text-light-blue:hover{color:#87CEEB!important}\r\n.w3-text-brown,.w3-hover-text-brown:hover{color:#795548!important}\r\n.w3-text-cyan,.w3-hover-text-cyan:hover{color:#00bcd4!important}\r\n.w3-text-blue-grey,.w3-hover-text-blue-grey:hover,.w3-text-blue-gray,.w3-hover-text-blue-gray:hover{color:#607d8b!important}\r\n.w3-text-green,.w3-hover-text-green:hover{color:#4CAF50!important}\r\n.w3-text-light-green,.w3-hover-text-light-green:hover{color:#8bc34a!important}\r\n.w3-text-indigo,.w3-hover-text-indigo:hover{color:#3f51b5!important}\r\n.w3-text-khaki,.w3-hover-text-khaki:hover{color:#b4aa50!important}\r\n.w3-text-lime,.w3-hover-text-lime:hover{color:#cddc39!important}\r\n.w3-text-orange,.w3-hover-text-orange:hover{color:#ff9800!important}\r\n.w3-text-deep-orange,.w3-hover-text-deep-orange:hover{color:#ff5722!important}\r\n.w3-text-pink,.w3-hover-text-pink:hover{color:#e91e63!important}\r\n.w3-text-purple,.w3-hover-text-purple:hover{color:#9c27b0!important}\r\n.w3-text-deep-purple,.w3-hover-text-deep-purple:hover{color:#673ab7!important}\r\n.w3-text-red,.w3-hover-text-red:hover{color:#f44336!important}\r\n.w3-text-sand,.w3-hover-text-sand:hover{color:#fdf5e6!important}\r\n.w3-text-teal,.w3-hover-text-teal:hover{color:#009688!important}\r\n.w3-text-yellow,.w3-hover-text-yellow:hover{color:#d2be0e!important}\r\n.w3-text-white,.w3-hover-text-white:hover{color:#fff!important}\r\n.w3-text-black,.w3-hover-text-black:hover{color:#000!important}\r\n.w3-text-grey,.w3-hover-text-grey:hover,.w3-text-gray,.w3-hover-text-gray:hover{color:#757575!important}\r\n.w3-text-light-grey,.w3-hover-text-light-grey:hover,.w3-text-light-gray,.w3-hover-text-light-gray:hover{color:#f1f1f1!important}\r\n.w3-text-dark-grey,.w3-hover-text-dark-grey:hover,.w3-text-dark-gray,.w3-hover-text-dark-gray:hover{color:#3a3a3a!important}\r\n.w3-border-amber,.w3-hover-border-amber:hover{border-color:#ffc107!important}\r\n.w3-border-aqua,.w3-hover-border-aqua:hover{border-color:#00ffff!important}\r\n.w3-border-blue,.w3-hover-border-blue:hover{border-color:#2196F3!important}\r\n.w3-border-light-blue,.w3-hover-border-light-blue:hover{border-color:#87CEEB!important}\r\n.w3-border-brown,.w3-hover-border-brown:hover{border-color:#795548!important}\r\n.w3-border-cyan,.w3-hover-border-cyan:hover{border-color:#00bcd4!important}\r\n.w3-border-blue-grey,.w3-hover-border-blue-grey:hover,.w3-border-blue-gray,.w3-hover-border-blue-gray:hover{border-color:#607d8b!important}\r\n.w3-border-green,.w3-hover-border-green:hover{border-color:#4CAF50!important}\r\n.w3-border-light-green,.w3-hover-border-light-green:hover{border-color:#8bc34a!important}\r\n.w3-border-indigo,.w3-hover-border-indigo:hover{border-color:#3f51b5!important}\r\n.w3-border-khaki,.w3-hover-border-khaki:hover{border-color:#f0e68c!important}\r\n.w3-border-lime,.w3-hover-border-lime:hover{border-color:#cddc39!important}\r\n.w3-border-orange,.w3-hover-border-orange:hover{border-color:#ff9800!important}\r\n.w3-border-deep-orange,.w3-hover-border-deep-orange:hover{border-color:#ff5722!important}\r\n.w3-border-pink,.w3-hover-border-pink:hover{border-color:#e91e63!important}\r\n.w3-border-purple,.w3-hover-border-purple:hover{border-color:#9c27b0!important}\r\n.w3-border-deep-purple,.w3-hover-border-deep-purple:hover{border-color:#673ab7!important}\r\n.w3-border-red,.w3-hover-border-red:hover{border-color:#f44336!important}\r\n.w3-border-sand,.w3-hover-border-sand:hover{border-color:#fdf5e6!important}\r\n.w3-border-teal,.w3-hover-border-teal:hover{border-color:#009688!important}\r\n.w3-border-yellow,.w3-hover-border-yellow:hover{border-color:#ffeb3b!important}\r\n.w3-border-white,.w3-hover-border-white:hover{border-color:#fff!important}\r\n.w3-border-black,.w3-hover-border-black:hover{border-color:#000!important}\r\n.w3-border-grey,.w3-hover-border-grey:hover,.w3-border-gray,.w3-hover-border-gray:hover{border-color:#9e9e9e!important}\r\n.w3-border-light-grey,.w3-hover-border-light-grey:hover,.w3-border-light-gray,.w3-hover-border-light-gray:hover{border-color:#f1f1f1!important}\r\n.w3-border-dark-grey,.w3-hover-border-dark-grey:hover,.w3-border-dark-gray,.w3-hover-border-dark-gray:hover{border-color:#616161!important}\r\n.w3-border-pale-red,.w3-hover-border-pale-red:hover{border-color:#ffe7e7!important}.w3-border-pale-green,.w3-hover-border-pale-green:hover{border-color:#e7ffe7!important}\r\n.w3-border-pale-yellow,.w3-hover-border-pale-yellow:hover{border-color:#ffffcc!important}.w3-border-pale-blue,.w3-hover-border-pale-blue:hover{border-color:#e7ffff!important}\r\n</style>\r\n</head>\r\n\r\n<body>\r\n\r\n<div class=\"w3-container\">\r\n  <h2>JSchemaTool</h2>\r\n  <p>Highly useful even if not perfect. Also it's free.</p>\r\n  <p>Supported fetures:</p>\r\n  <ul class=\"w3-ul w3-green w3-small\">\r\n  <li>Generate empty JSON payload from JSON schema.</li>\r\n  <li>Generate JSON schema from JSON payload.</li>\r\n  <li>Edit schema using GUI editor.</li>\r\n  <li>Create new schema.</li>\r\n</ul>\r\n  <p>Unsupported:</p>\r\n  <ul class=\"w3-ul w3-red w3-small\">\r\n  <li>Defenitions</li>\r\n  <li>$ref</li>\r\n  <li>Required</li>\r\n</ul>\r\n\r\n</div>\r\n\r\n\r\n</body>\r\n</html>");

	}
	private void generatePayloadFunction() {
		try {
			String[] text= ImportJSON.getInstance().generatePayload(schemaText.getText());
			schemaText.setText(text[0]);
			jsonText.setText(text[1]);
			Map<String, Object> map= ImportJSON.getInstance().schemaMap;
			//tree.clearAll(false);
			tree.removeAll();
			loadSchemaEditor(map, tree);
		} catch (Exception e1) {
			if(schemaText.getText()==null || schemaText.getText().length()==0) {
				new InfoDialogBox(shell, getStyle()).open("To generate JSON Payload you need to load JSON Schema first.\n1. Use \"Browse JSON Schema\" button to select JSON Schema file.\nOr\n2. Paste JSON Schema text into \"JSON Schema\" text box.");
			}else
			new ErrorDialogBox(shell, getStyle()).open(e1);
		}
	}
	private void loadSelectedSchema() {
		String name=documentSelector.getText();
  	  if(tree.getItemCount()>0) {
  		  TreeItem item=tree.getItem(0);
  		  updateSchema();
  		  schemaMap.put(item.getText(nameIndex), schemaText.getText());
	    	  item.dispose();
  	  }
  	  if(!name.equals("New Root")) {
	    	  String schema=schemaMap.get(name);
	    	  schemaText.setText(schema);
	    	  generatePayloadFunction();
  	  }else {
  		  schemaText.setText("");
  		  jsonText.setText("");
  	  }
	}
	private void updateSchema() {
		TreeItem items[]= tree.getItems();
		System.out.println(items.length+"");
		jsonWorkspace="";
		try {
			newSchema(1);
			jsonWorkspace=jsonWorkspace.replaceAll(","+identifier, "");
	        jsonWorkspace=jsonWorkspace.replaceAll(identifier, "");
	        //System.out.println(jsonWorkspace);
	        jsonWorkspace=ImportJSON.beautify(jsonWorkspace);
			schemaText.setText(jsonWorkspace);
		} catch (Exception e2) {
			new ErrorDialogBox(shell, getStyle()).open(e2);
		}
	}
	String jsonWorkspace="";
	public String identifier="-~-~-~-010-~-~-~";
	private Text basePackage;
	private Text applicationText;
	private Text pomText;
	private Text htmlText;
	private Text javaScriptText;
	private Text cssText;
	private void appendln(String data) {
		jsonWorkspace+=data+"\n";
		//System.out.println(data.replaceAll(","+identifier, "").replaceAll(identifier, ""));
	}
	
	private void append(String data) {
		jsonWorkspace+=data;
		//System.out.print(data.replaceAll(","+identifier, "").replaceAll(identifier, ""));
	}
	private void newSchema(int pad) throws Exception {
		int count=tree.getItemCount();
		TreeItem items[]=tree.getItems();
		appendln("{");		
		appendln(ImportJSON.padding(pad)+"\"$schema\": \"http://json-schema.org/draft-04/schema#\",");
		if(pomText.getText()!=null && pomText.getText().trim().length()>0)
			appendln(ImportJSON.padding(pad)+"\"pom\": \""+ImportJSON.encodeBase64(pomText.getText())+"\",");
		if(applicationText.getText()!=null && applicationText.getText().trim().length()>0)
			appendln(ImportJSON.padding(pad)+"\"application\": \""+ImportJSON.encodeBase64(applicationText.getText())+"\",");
		appendln(ImportJSON.padding(pad)+"\"type\": \"object\", ");
		appendln(ImportJSON.padding(pad)+"\"properties\": {");
		for (TreeItem item : items) {
			processItem(item,pad+1,null);
		}
		appendln(identifier);
		appendln(ImportJSON.padding(pad)+"}");
		appendln("}");
	}
	private void newSchema(TreeItem treeItem,int pad,ClassMetaData cls) throws Exception{
		//int count=treeItem.getItemCount();
		TreeItem items[]=items=treeItem.getItems();
		
		for (TreeItem item : items) {
			processItem(item,pad,cls);
		}
		appendln(identifier);
	}
	
	private void processItem(TreeItem item,int pad,ClassMetaData cls) throws Exception{
		String type=item.getText(typeIndex);//type of object
		//Map<String, Object> cls=null;
		if(type.toLowerCase().contains("api")) {
			String apiMethod=type.toLowerCase().replace("api<", "").replace(">", "");
			String clsName=item.getText(nameIndex);
			appendln("\n"+ImportJSON.padding(pad)+"\""+clsName+"\":{"); 
			cls=CG.createAPIClass(clsName);
			API api=cls.getAPI();
			api.method=apiMethod;
			if(api.code==null || api.code.trim().length()==0)
				api.code=ImportJSON.encodeBase64(item.getText(codeIndex));
			if(api.custom==null || api.custom.trim().length()==0)
				api.custom=ImportJSON.encodeBase64(item.getText(customIndex));
			if(api.imports==null || api.imports.trim().length()==0)
				api.imports=ImportJSON.encodeBase64(item.getText(importIndex));
			if(cls.getDescription()==null || cls.getDescription().trim().length()==0)
				cls.setDescription(item.getText(descIndex));
			if(cls.getTitle()==null || cls.getTitle().trim().length()==0)
				cls.setTitle(item.getText(titleIndex));
			cls.setScope(item.getText(scopeIndex));
			if("false".equalsIgnoreCase(item.getText(requiredIndex))) {
				cls.setRequired(false);
				CG.classes.get(clsName+"Resource").setRequired(false);
			}
    		appendln(ImportJSON.padding(pad+1)+"\"type\":\""+type+"\",");
    		appendln(ImportJSON.padding(pad+1)+"\"required\":\""+item.getText(requiredIndex)+"\",");
    		appendln(ImportJSON.padding(pad+1)+"\"scope\":\""+cls.getScope()+"\",");
    		appendln(ImportJSON.padding(pad+1)+"\"title\":\""+cls.getTitle()+"\",");
    		appendln(ImportJSON.padding(pad+1)+"\"code\":\""+cls.getAPI().code+"\",");
    		appendln(ImportJSON.padding(pad+1)+"\"custom\":\""+cls.getAPI().custom+"\",");
    		appendln(ImportJSON.padding(pad+1)+"\"import\":\""+cls.getAPI().imports+"\",");
    		appendln(ImportJSON.padding(pad+1)+"\"description\":\""+cls.getDescription()+"\",");
    		
    		appendln(ImportJSON.padding(pad+1)+"\"properties\":{");
    		newSchema(item, pad+2,cls);
    		appendln(ImportJSON.padding(pad+1)+"}");
    		append(ImportJSON.padding(pad)+"},");
		}else
		if(type.equalsIgnoreCase("object")) {
			String clsName=item.getText(nameIndex);
			appendln("\n"+ImportJSON.padding(pad)+"\""+clsName+"\":{");
			
			if(cls==null) {
				cls=CG.createEntityClass(clsName,item.getText(scopeIndex),clsName);
			}else {

				cls.addImport("import com.fasterxml.jackson.annotation.JsonIgnore;");
				cls.addImport("import org.springframework.data.rest.core.annotation.Description;");
				Property prop=cls.addProperty("private",clsName, clsName.toLowerCase(),false);
				if(!"local".equalsIgnoreCase(cls.getScope())) {
					cls.addImport("import javax.persistence.CascadeType;");
					cls.addImport("import javax.persistence.JoinColumn;");
					cls.addImport("import javax.persistence.ManyToOne;");
					cls.addImport("import javax.persistence.FetchType;");
					prop.annotations.add("@JsonIgnore");
					prop.annotations.add("@ManyToOne(fetch = FetchType.EAGER)");
				}
				if("true".equals(item.getText(requiredIndex))) {
						prop.required=true;
						if(!prop.primitive) {
							prop.annotations.add("@JoinColumn(name = \""+prop.name+"_id\",nullable=false)\r\n");
							cls.addImport("import javax.persistence.JoinColumn;");
						}else {
							prop.annotations.add("@Column(nullable = false)\r\n");
							cls.addImport("import javax.persistence.Column;");
						}
						cls.addImport("import javax.validation.constraints.NotEmpty;");
				}
				if(item.getText(descIndex)!=null) {
					prop.description=item.getText(descIndex);
					cls.addImport("import org.springframework.data.rest.core.annotation.Description;");
				}
				cls=CG.createEntityClass(clsName,item.getText(scopeIndex),cls.getRoot()+"."+clsName);
			}
			if(cls.getDescription()==null || cls.getDescription().trim().length()==0)
				cls.setDescription(item.getText(descIndex));
			if(cls.getTitle()==null || cls.getTitle().trim().length()==0)
				cls.setTitle(item.getText(titleIndex));
			
    		appendln(ImportJSON.padding(pad+1)+"\"type\":\"object\",");
    		appendln(ImportJSON.padding(pad+1)+"\"required\":\""+item.getText(requiredIndex)+"\",");
    		appendln(ImportJSON.padding(pad+1)+"\"scope\":\""+cls.getScope()+"\",");
    		appendln(ImportJSON.padding(pad+1)+"\"title\":\""+cls.getTitle()+"\",");
    		appendln(ImportJSON.padding(pad+1)+"\"description\":\""+cls.getDescription()+"\",");
    		
    		appendln(ImportJSON.padding(pad+1)+"\"properties\":{");
    		newSchema(item, pad+2,cls);
    		appendln(ImportJSON.padding(pad+1)+"}");
    		append(ImportJSON.padding(pad)+"},");
		}
		else if(type.toLowerCase().contains("array")) {
			String clsName=item.getText(nameIndex);
			String typ="array";
			String subType=type.toLowerCase().replace("array<", "").replace(">", "");
			//System.out.println(subType);
			appendln("\n"+ImportJSON.padding(pad)+"\""+clsName+"\":{");
			appendln(ImportJSON.padding(pad+1)+"\"type\":\"array\",");
			appendln(ImportJSON.padding(pad+1)+"\"required\":\""+item.getText(requiredIndex)+"\",");
			appendln(ImportJSON.padding(pad+1)+"\"title\":\""+item.getText(titleIndex)+"\",");
			appendln(ImportJSON.padding(pad+1)+"\"scope\":\""+cls.getScope()+"\",");
			appendln(ImportJSON.padding(pad+1)+"\"description\":\""+item.getText(descIndex)+"\",");
    		
    		//cls=addAnnots(cls, "@Description(value = \""+item.getText(descIndex)+"\")");
			if(subType.equalsIgnoreCase("object")) {
				appendln(ImportJSON.padding(pad+1)+"\"items\":{");
				appendln(ImportJSON.padding(pad+2)+"\"type\":\"object\",");
				appendln(ImportJSON.padding(pad+2)+"\"required\":\""+item.getText(requiredIndex)+"\",");
			}
			else {
				appendln(ImportJSON.padding(pad+1)+"\"items\":[");
				appendln(ImportJSON.padding(pad+2)+"{\"type\":\""+subType+"\"}");
				appendln(ImportJSON.padding(pad+2)+"\"required\":\""+item.getText(requiredIndex)+"\",");
			}
			
			if(subType.equalsIgnoreCase("object")) {
				appendln(ImportJSON.padding(pad+2)+"\"properties\":{");
				if(cls==null) {
					cls=CG.createEntityClass(clsName,item.getText(scopeIndex),clsName);
				}else {
					cls.addImport("import javax.persistence.OneToMany;");
					cls.addImport("import java.util.List;");
					cls.addImport("import com.fasterxml.jackson.annotation.JsonIgnore;");
					
					Property prop=cls.addProperty("private","List<"+clsName+">", CodeGen.getPlural(clsName.toLowerCase()),false);
					if(!"local".equalsIgnoreCase(cls.getScope())) {
						prop.annotations.add("@JsonIgnore");
						prop.annotations.add("@OneToMany(mappedBy = \""+cls.getName().toLowerCase()+"\")");
					}
					if("true".equals(item.getText(requiredIndex))) {
							prop.required=true;
							if(!prop.primitive) {
								prop.annotations.add("@JoinColumn(name = \""+prop.name+"_id\",nullable=false)\r\n");
								cls.addImport("import javax.persistence.JoinColumn;");
							}else {
								prop.annotations.add("@Column(nullable = false)\r\n");
								cls.addImport("import javax.persistence.Column;");
							}
					}
					if(item.getText(descIndex)!=null)
						prop.description=item.getText(descIndex);
					cls=CG.createEntityClass(clsName,item.getText(scopeIndex),cls.getRoot()+"."+clsName);
				}
			}
			newSchema(item, pad+3,cls);
			if(subType.equalsIgnoreCase("object")) {
				appendln(ImportJSON.padding(pad+2)+"}");
				appendln(ImportJSON.padding(pad+1)+"}");
			}else
				appendln(ImportJSON.padding(pad+1)+"]");
			append(ImportJSON.padding(pad)+"},");
		}
		else {
			String propName=item.getText(nameIndex);
			String typName=item.getText(typeIndex);
			String req=item.getText(requiredIndex);
			String format=item.getText(formatIndex);
			//System.out.println(propName+" is required:"+req);
			if(cls==null)
				cls=CG.createAPIClass("UndefinedRoot");
    		cls.addImport("import org.springframework.data.rest.core.annotation.Description;");
			Property prop=cls.addProperty("private",resolveJavaType(typName,format),propName,true);
			if("date".equals(format) && item.getText(formatIndex)!=null) {
				cls.addImport("import org.springframework.format.annotation.DateTimeFormat;");
				prop.annotations.add("@DateTimeFormat");//(style = \""+item.getText(formatIndex)+"\")");
			}
			
			if(item.getText(descIndex)!=null)
				prop.description=item.getText(descIndex);
			appendln("\n"+ImportJSON.padding(pad)+"\""+propName+"\":{");
    		append(ImportJSON.padding(pad+1)+"\"type\":\""+typName+"\",");
    		append(ImportJSON.padding(pad+1)+"\"required\":\""+req+"\",");
    		if(item.getText(titleIndex)!=null && item.getText(titleIndex).trim().length()>0)
    			append(ImportJSON.padding(pad+1)+"\n\"title\":\""+item.getText(titleIndex)+"\",");
    		if(item.getText(descIndex)!=null && item.getText(descIndex).trim().length()>0) {
    			append(ImportJSON.padding(pad+1)+"\n\"description\":\""+item.getText(descIndex)+"\",");
    		}
    		if(item.getText(formatIndex)!=null && item.getText(formatIndex).trim().length()>0)
    			append(ImportJSON.padding(pad+1)+"\n\"format\":\""+item.getText(formatIndex)+"\",");
    		if(item.getText(egIndex)!=null && item.getText(egIndex).trim().length()>0) {
    			prop.value=item.getText(egIndex);
    			append(ImportJSON.padding(pad+1)+"\n\"example\":\""+prop.value+"\",");
    		}
    		if(item.getText(scopeIndex)!=null && item.getText(scopeIndex).trim().length()>0) {
    			prop.scope=item.getText(scopeIndex);
    			cls.addImport("import javax.persistence.Column;");
    			if(prop.scope.contains("unique<Allow-NULL>"))
    				prop.annotations.add("@Column(unique = true, nullable = true)");
    			if(prop.scope.contains("unique<Not-NULL>"))
        			prop.annotations.add("@Column(unique = true, nullable = false)");
    			append(ImportJSON.padding(pad+1)+"\n\"scope\":\""+prop.scope+"\",");
    		}
    		if("true".equals(item.getText(requiredIndex))) {
				prop.required=true;
				if(prop.scope==null || !prop.scope.contains("unique<")) {
					if(!prop.primitive) {
						prop.annotations.add("@JoinColumn(name = \""+prop.name+"_id\",nullable=false)\r\n");
						cls.addImport("import javax.persistence.JoinColumn;");
					}else {
						prop.annotations.add("@Column(nullable = false)\r\n");
						cls.addImport("import javax.persistence.Column;");
					}
				}
			}
    		if(item.getText(enumIndex)!=null && item.getText(enumIndex).trim().length()>0)
    			append(ImportJSON.padding(pad+1)+"\n\"enum\":["+item.getText(enumIndex)+"],");
    		appendln(identifier);
    		append(ImportJSON.padding(pad)+"},");
		}
	}
	
	public static String resolveJavaType(String typ, String format) {
		String javaType=null;
		switch (typ) {
		case "string":
			javaType="String";
			if("date".equals(format))
				javaType="java.util.Date";
			break;
		case "integer":
			javaType="Integer";
			break;
		default:
			javaType="Double";
		}
		return javaType;
	}
	
	private void loadSchemaEditor(Map<String, Object> map, Tree tree) throws Exception{
		Object[] keys=map.keySet().toArray();
	    int itemCount = keys.length;
	    for (int i = 0; i < itemCount; i++) {
		    TreeItem item = new TreeItem(tree, SWT.FULL_SELECTION);
		    item.setText(nameIndex,keys[i].toString());
		    //item.getCl
		    if(map.get(keys[i]) instanceof Map)
		    	loadSchemaEditor((Map)map.get(keys[i]), item);
	    }
	}
	
	private void loadSchemaEditor(Map<String, Object> map, TreeItem ti) throws Exception{
		Object[] keys=map.keySet().toArray();
	    int itemCount = keys.length;
	    ti.setText(descIndex, (map.get("description")+"").replace("null", ""));
	    ti.setText(titleIndex, (map.get("title")+"").replace("null", ""));
	    ti.setText(typeIndex, (map.get("type")+"").replace("null", ""));
	    ti.setText(egIndex, (map.get("example")+"").replace("null", ""));
	    ti.setText(formatIndex, (map.get("format")+"").replace("null", ""));
	    ti.setText(enumIndex, (map.get("enum")+"").replace("null", ""));
	    ti.setText(scopeIndex, (map.get("scope")+"").replace("null", ""));
	    ti.setText(requiredIndex, (map.get("required")+"").replace("null", ""));
	    if(map.get("code")!=null && map.get("code").toString().trim().length()>0) {
	    	String code=map.get("code").toString();
	    	ti.setText(codeIndex, ImportJSON.decodeBase64(code));
	    }
	    
	    if(map.get("custom")!=null && map.get("custom").toString().trim().length()>0) {
	    	String code=map.get("custom").toString();
	    	ti.setText(customIndex, ImportJSON.decodeBase64(code));
	    }
	    
	    if(map.get("import")!=null && map.get("import").toString().trim().length()>0) {
	    	String code=map.get("import").toString();
	    	ti.setText(importIndex, ImportJSON.decodeBase64(code));
	    }
	    
	    for (int i = 0; i < itemCount; i++) {
	    	if(map.get(keys[i]) instanceof Map) {
		    	TreeItem item = new TreeItem(ti, SWT.FULL_SELECTION);
		    	item.setText(0,keys[i].toString());
		    	loadSchemaEditor((Map) map.get(keys[i]), item);
	    	}
	    }
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
	
	private String openFileDialog() throws Exception{
		FileDialog fd = new FileDialog(shell, SWT.OPEN);
		fd.setText("Open");
		String[] filterExt = { "*.json;*.JSON" };
		String[] filterNames = { "TXT files" };
		fd.setFilterExtensions(filterExt);
		fd.setFilterNames(filterNames);
		/*String lastPath = System.getInstance().getString(Config.LAST_OPEN_TEXT_PATH);
		if (lastPath != null && !lastPath.isEmpty())
			fd.setFileName(lastPath);
		*/
		String selected = fd.open();
		return selected;
	}
	
	private String openFolderDialog() throws Exception{
		DirectoryDialog fd = new DirectoryDialog(shell, SWT.OPEN);
		fd.setText("Open");
		/*String lastPath = System.getInstance().getString(Config.LAST_OPEN_TEXT_PATH);
		if (lastPath != null && !lastPath.isEmpty())
			fd.setFileName(lastPath);
		*/
		String selected = fd.open();
		return selected;
	}
	
	private void generateAPIDefaults(TreeItem selItem) {
		if(selItem.getItemCount()>0)
			return;
		TreeItem item = new TreeItem(selItem, SWT.FULL_SELECTION);
		item.setText(nameIndex, "path");
		item.setText(descIndex, "This is resource path. You can write the path in the Value cell");
		item.setText(titleIndex, "Path");
		item.setText(typeIndex, "string");
		item.setText(egIndex, "/books/{id}");
		item.setText(enumIndex, "");
		item.setText(requiredIndex, "");
		item.setText(scopeIndex, "");
		//item.setText(codeIndex, "Write your java code here.");
		
		item = new TreeItem(selItem, SWT.FULL_SELECTION);
		item.setText(nameIndex, "id");
		item.setText(descIndex, "This will be treated as path param as it has a refernce in path value section");
		item.setText(titleIndex, "ID");
		item.setText(typeIndex, "string");
		item.setText(egIndex, "sample value");
		item.setText(enumIndex, "");
		item.setText(requiredIndex, "true");
		item.setText(scopeIndex, "");
		//item.setText(codeIndex, "Write your java code here.");
		
		item = new TreeItem(selItem, SWT.FULL_SELECTION);
		item.setText(nameIndex, "version");
		item.setText(descIndex, "This is a querry param. If you reference it in path then it will be treated as path param");
		item.setText(titleIndex, "Version");
		item.setText(typeIndex, "string");
		item.setText(egIndex, "1.0.0");
		item.setText(enumIndex, "");
		item.setText(requiredIndex, "true");
		item.setText(scopeIndex, "");
		
		TreeItem headers = new TreeItem(selItem, SWT.FULL_SELECTION);
		headers.setText(nameIndex, "Headers");
		headers.setText(descIndex, "This is a header object. Define your request/response headers inside");
		headers.setText(titleIndex, "Header");
		headers.setText(typeIndex, "object");
		headers.setText(egIndex, "");
		headers.setText(enumIndex, "");
		headers.setText(requiredIndex, "true");
		headers.setText(scopeIndex, "Local");
		
		item = new TreeItem(headers, SWT.FULL_SELECTION);
		item.setText(nameIndex, "RequestHeaders");
		item.setText(descIndex, "Add your request header params inside");
		item.setText(titleIndex, "Request Headers");
		item.setText(typeIndex, "object");
		item.setText(egIndex, "");
		item.setText(enumIndex, "");
		item.setText(requiredIndex, "true");
		item.setText(scopeIndex, "Local");
		
		item = new TreeItem(headers, SWT.FULL_SELECTION);
		item.setText(nameIndex, "ResponseHeaders");
		item.setText(descIndex, "Add your response header params inside");
		item.setText(titleIndex, "Response Headers");
		item.setText(typeIndex, "object");
		item.setText(egIndex, "");
		item.setText(enumIndex, "");
		item.setText(requiredIndex, "true");
		item.setText(scopeIndex, "Local");
		
		
		TreeItem payloads = new TreeItem(selItem, SWT.FULL_SELECTION);
		payloads.setText(nameIndex, "Payload");
		payloads.setText(descIndex, "This is a payload object. Define your request/response payloads inside");
		payloads.setText(titleIndex, "Payload");
		payloads.setText(typeIndex, "object");
		payloads.setText(egIndex, "");
		payloads.setText(enumIndex, "");
		payloads.setText(requiredIndex, "true");
		payloads.setText(scopeIndex, "Local");
		
		item = new TreeItem(payloads, SWT.FULL_SELECTION);
		item.setText(nameIndex, "RequestPayload");
		item.setText(descIndex, "Add your request payload params inside");
		item.setText(titleIndex, "Request Payload");
		item.setText(typeIndex, "object");
		item.setText(egIndex, "");
		item.setText(enumIndex, "");
		item.setText(requiredIndex, "true");
		item.setText(scopeIndex, "Local");
		
		item = new TreeItem(payloads, SWT.FULL_SELECTION);
		item.setText(nameIndex, "ResponsePayload");
		item.setText(descIndex, "Add your response payload params inside");
		item.setText(titleIndex, "Payload Headers");
		item.setText(typeIndex, "object");
		item.setText(egIndex, "");
		item.setText(enumIndex, "");
		item.setText(requiredIndex, "true");
		item.setText(scopeIndex, "Local");

	}
	private void openMokshSchema() throws Exception {
		String filePath=openFileDialog();
		if(filePath==null || filePath.trim().length()==0)
			return;
		schemaMap.clear();
		String packageName=(new File(filePath).getName().toLowerCase()+"_").replace(".json_", "");
		basePackage.setText(packageName);
		if(filePath !=null) {
			   System.out.println("You chose to open this file: " + filePath);
				String[] text= ImportJSON.getInstance().loadSchema(filePath);
				if(ImportJSON.POM!=null)
					pomText.setText(ImportJSON.POM);
				if(ImportJSON.APP!=null)
					applicationText.setText(ImportJSON.APP);
				Map<String, Object> map=ImportJSON.getInstance().schemaMap;
				schemaText.setText(text[0]);
				jsonText.setText(text[1]);
				//tree.clearAll(false);
				tree.removeAll();
				loadSchemaEditor(map, tree);
		}
	}
}
