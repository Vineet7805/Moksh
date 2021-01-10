package com.github.moksh.generator.GUI;

import java.util.ArrayList;
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
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import com.github.moksh.generator.core.ImportJSON;
import com.github.moksh.generator.core.JsonPatchMap;

public class InvokeGUI extends Composite {
	private ImportJSON importJsonRight = null;
	private ImportJSON importJsonLeft = null;
	private Tree treeLeft = null;
	private Tree treeRight = null;
	private TreeItem itemLeftSelected = null;
	private TreeItem itemRightSelected = null;
	private List<JsonPatchMap> jsonPatchMaps = null;
	private GC gc = null;
	int nameIndex=0,typeIndex=1;
	public InvokeGUI(Composite parent, int style) {
		super(parent, style);
		importJsonRight = ImportJSON.getInstance();
		importJsonLeft = ImportJSON.getInstance();
		jsonPatchMaps = new ArrayList<JsonPatchMap>();
		setLayout(new FillLayout(SWT.HORIZONTAL));
		Composite composite_4 = new Composite(this, SWT.BORDER_SOLID);
		final InvokeGUI myself=this;
		this.setSize(620,200);
		// composite_4.setLayout(new GridLayout(6, false));
		Shell shell = parent.getShell();
		shell.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				Rectangle clientArea = shell.getClientArea();
				e.gc.drawLine(0, 0, clientArea.width, clientArea.height);
			}
		});
		composite_4.setLayout(new GridLayout(1, false));
		//GridData gd_leftJsonPayload = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		//gd_leftJsonPayload.heightHint = 232;
		//leftJsonPayload.setLayoutData(gd_leftJsonPayload);

		Composite composite_5 = new Composite(composite_4, SWT.NONE);
		composite_5.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		composite_5.setLayout(new GridLayout(1, true));
		//Layout gd=myself.getLayout();

		ScrolledComposite scrolledComposite = new ScrolledComposite(composite_5,
				SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		GridData gd_scrolledComposite = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_scrolledComposite.heightHint = 125;
		gd_scrolledComposite.widthHint = 250;
		scrolledComposite.setLayoutData(gd_scrolledComposite);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		Composite composite_6 = new Composite(scrolledComposite, SWT.NONE);
		composite_6.setLayout(new GridLayout(3, false));
		//Font treeFont = new Font( parent.getDisplay(), new FontData( "Segoe UI", 10, SWT.NORMAL ) );
		treeLeft = new Tree(composite_6, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		GridData gd_treeLeft = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_treeLeft.widthHint = 250;
		treeLeft.setLayoutData(gd_treeLeft);

		// tree = new Tree(editorComposite, SWT.MULTI | SWT.FULL_SELECTION);
		// treeLeft.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		treeLeft.setHeaderVisible(true);
		treeLeft.setLinesVisible(true);
	//	treeLeft.setFont(treeFont);
		// treeLeft.setSize(250, height);

		TreeColumn trclmnLeftName = new TreeColumn(treeLeft, SWT.LEFT_TO_RIGHT);
		trclmnLeftName.setWidth(165);
		trclmnLeftName.setText("Name");
		
		TreeColumn trclmnLeftType = new TreeColumn(treeLeft, SWT.LEFT_TO_RIGHT);
		trclmnLeftType.setWidth(100);
		trclmnLeftType.setText("type");
		
		Button btnTest = new Button(composite_6, SWT.NONE);
		btnTest.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnTest.setText("Test");
		treeRight = new Tree(composite_6, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		GridData gd_treeRight = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_treeRight.widthHint = 250;
		treeRight.setLayoutData(gd_treeRight);
		// treeRight.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		treeRight.setOrientation(SWT.LEFT_TO_RIGHT);
		treeRight.setHeaderVisible(true);
		treeRight.setLinesVisible(true);
		//treeRight.setFont(treeFont);
		TreeColumn trclmnRightName = new TreeColumn(treeRight, SWT.LEFT_TO_RIGHT);
		trclmnRightName.setWidth(165);
		trclmnRightName.setText("Name");
		
		TreeColumn trclmnRightType = new TreeColumn(treeRight, SWT.LEFT_TO_RIGHT);
		trclmnRightType.setWidth(100);
		trclmnRightType.setText("type");

		scrolledComposite.setContent(composite_6);
		scrolledComposite.setMinSize(composite_6.computeSize(SWT.DEFAULT, SWT.DEFAULT));

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

	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
