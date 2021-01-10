package com.github.moksh.generator.GUI;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;

public class PromptDialogBox extends Dialog {

	protected Object result;
	protected Shell shell;
	private Text textBig;
	private String input;
	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	TreeItem item=null;
	private Text textItem=null;
	private String value="";
	int index=0;
	public PromptDialogBox(Shell parent, int style) {
		super(parent, style);
		setText("Edit Text");
	}
	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open(TreeItem ti,int colIndx) {
		item=ti;
		index=colIndx;
		createContents();
		shell.open();
		shell.layout();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}
	
	
	public Object open(Text ti) {
		textItem=ti;
		createContents();
		Rectangle screenSize = shell.getDisplay().getPrimaryMonitor().getBounds();
		shell.setLocation((screenSize.width - shell.getBounds().width) / 2, (screenSize.height - shell.getBounds().height) / 2);
		shell.open();
		shell.layout();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}
	
	public String open(String str) {
		if(str!=null)
			value=str;
		createContents();
		shell.open();
		shell.layout();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return value;
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shell = new Shell(getParent(), getStyle());
		shell.setSize(200, 100);
		shell.setText(getText());
		shell.setLayout(new GridLayout(1, false));
		
		textBig = new Text(shell, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
		textBig.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		if(item!=null)
			textBig.setText(item.getText(index));
		else if(textItem!=null)
			textBig.setText(textItem.getText());
		else
			textBig.setText(value);
		SashForm sashForm = new SashForm(shell, SWT.NONE);
		sashForm.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		
		Button btnNewButton = new Button(sashForm, SWT.NONE);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(item!=null)
					item.setText(index, textBig.getText());
				else if(textItem!=null)
					textItem.setText(textBig.getText());
				else
					value=textBig.getText();
				shell.dispose();
			}
		});
		btnNewButton.setText("OK");
		
		Button btnCancel = new Button(sashForm, SWT.CENTER);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				value=null;
				shell.dispose();
			}
		});
		btnCancel.setText("Cancel");
		sashForm.setWeights(new int[] {1, 1});

	}

}
