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
import org.eclipse.swt.widgets.Composite;

public class EmptyDialogBox extends Dialog {

	protected Object result;
	protected Shell shell;
	private String input;
	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	TreeItem item=null;
	private Text textItem=null;
	private String value=null;
	int index=0;
	private Runnable runOnOk=null;
	private Runnable runOnCancel=null;
	public EmptyDialogBox(Shell parent, int style) {
		super(parent, style);
		setText("Editor");
		shell = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.PRIMARY_MODAL);
		composite = new Composite(shell, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
	}
	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open(Composite composite,Runnable runOnOk,Runnable runOnCancel, Runnable init) {
		createContents(composite);
		this.runOnOk=runOnOk;
		this.runOnCancel=runOnCancel;
		if(init!=null)
			init.run();
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
	public Composite composite = null;
	private void createContents(Composite composite_1) {
		
		shell.setSize(560, 600);
		shell.setText(getText());
		shell.setLayout(new GridLayout(1, false));
		
		//Composite composite_1 = new Composite(composite, SWT.NONE);
		composite_1.setLayout(new GridLayout(1, false));
		composite_1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		SashForm sashForm = new SashForm(shell, SWT.NONE);
		sashForm.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		
		Button btnNewButton = new Button(sashForm, SWT.NONE);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(runOnOk!=null)
					runOnOk.run();
				shell.dispose();
			}
		});
		btnNewButton.setText("OK");
		
		Button btnCancel = new Button(sashForm, SWT.CENTER);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(runOnCancel!=null)
					runOnCancel.run();
				shell.dispose();
			}
		});
		btnCancel.setText("Cancel");
		sashForm.setWeights(new int[] {1, 1});

	}

}
