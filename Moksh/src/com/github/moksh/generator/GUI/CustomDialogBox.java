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

public class CustomDialogBox extends Dialog {

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
	int index=0;
	public CustomDialogBox(Shell parent, int style) {
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

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shell = new Shell(getParent(), getStyle());
		shell.setSize(700, 450);
		shell.setText(getText());
		shell.setLayout(new GridLayout(1, false));
		
		textBig = new Text(shell, SWT.BORDER | SWT.MULTI);
		textBig.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		textBig.setText(item.getText(index));
		SashForm sashForm = new SashForm(shell, SWT.NONE);
		sashForm.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		
		Button btnNewButton = new Button(sashForm, SWT.NONE);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				item.setText(index, textBig.getText());
				shell.dispose();
			}
		});
		btnNewButton.setText("OK");
		
		Button btnCancel = new Button(sashForm, SWT.CENTER);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				shell.dispose();
			}
		});
		btnCancel.setText("Cancel");
		sashForm.setWeights(new int[] {1, 1});

	}

}
