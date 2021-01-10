package com.github.moksh.generator.GUI;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

public class Test {
	public static void main(String[] args)
	{
	    Display display = new Display();
	    final Shell shell = new Shell(display);
	    shell.setText("StackOverflow");
	    shell.setLayout(new GridLayout(1, true));
	    
	    Button hideButton = new Button(shell, SWT.PUSH);
	    hideButton.setText("Toggle");

	    final Composite content = new Composite(shell, SWT.NONE);
	    content.setLayout(new GridLayout(3, false));

	    final GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
	    content.setLayoutData(data);

	    for(int i = 0; i < 10; i++)
	    {
	        new Label(content, SWT.NONE).setText("Label " + i);
	    }

	    hideButton.addListener(SWT.Selection, new Listener()
	    {
	        @Override
	        public void handleEvent(Event arg0)
	        {
	            data.exclude = !data.exclude;
	            content.setVisible(!data.exclude);
	            content.getParent().pack();
	        }
	    });

	    shell.pack();
	    shell.open();
	    while (!shell.isDisposed())
	    {
	        if (!display.readAndDispatch())
	            display.sleep();
	    }
	    display.dispose();
	}
}
