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

public class ErrorDialogBox extends Dialog {

	protected Object result;
	protected Shell shell;
	private Text textBig;
	private Exception error;
	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public ErrorDialogBox(Shell parent, int style) {
		super(parent, style);
		setText("Edit Text");
	}
	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open(Exception err) {
		error=err;
		err.printStackTrace();
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
		shell.setSize(600, 500);
		shell.setText(getText());
		shell.setLayout(new GridLayout(1, false));
		
		SashForm sashForm_1 = new SashForm(shell, SWT.NONE);
		
		Label lblPleaseReportThis = new Label(sashForm_1, SWT.NONE);
		lblPleaseReportThis.setText("Please report this exception to vineet.dav@outlook.com");
		sashForm_1.setWeights(new int[] {1});
		
		textBig = new Text(shell, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		textBig.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		StackTraceElement ste[]=error.getStackTrace();
		String err=error.getMessage();
		for (StackTraceElement stackTraceElement : ste) {
			err+="\n"+stackTraceElement.toString();
		}
		textBig.setText(err);
		SashForm sashForm = new SashForm(shell, SWT.NONE);
		sashForm.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		
		Button btnNewButton = new Button(sashForm, SWT.NONE);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				shell.dispose();
			}
		});
		btnNewButton.setText("OK");
		sashForm.setWeights(new int[] {1});

	}

}
