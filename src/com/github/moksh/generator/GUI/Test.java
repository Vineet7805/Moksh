package com.github.moksh.generator.GUI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
	private static String getSampleData() {
		Random rand = new Random();
		Map<String, Object> rowMap = null;
		List<Map<String, Object>> tabledata = new ArrayList<Map<String, Object>>();
		int duration[] = new int[32];
		String projects[] = { "SOW17", "SOW18", "SOW19" };
		String description[] = { "Design and discussion", "Implementing Rate API", "Testing services",
				"Deployment and support" };

		for (int i = 0; i < 10; i++) {
			rowMap = new HashMap<String, Object>();
			rowMap.put('"' + "project" + '"', '"' + projects[rand.nextInt(3)] + '"');
			rowMap.put('"' + "describe" + '"', '"' + description[rand.nextInt(4)] + '"');
			rowMap.put('"' + "billable" + '"', true);
			if (rand.nextInt(i + 10) % 2 != 0) {
				rowMap.put('"' + "billable" + '"', false);
				rowMap.put('"' + "describe" + '"', '"' + "Standup meetings" + '"');
			}
			for (int j = 1; j <= 31; j++) {
				int randm = rand.nextInt(8) + 1;
				String val = "";
				if (duration[j] < 8 && randm % 3 == 0) {
					val = randm + "s";
					duration[j] += randm;
				}
				rowMap.put('"' + "day" + j + '"', '"' + val + '"');
				val=j+"s";
				rowMap.put('"' + "ID_" + j + '"', '"' + val + '"');
			}
			tabledata.add(rowMap);
		}
		return (tabledata.toString().replace("=", ":"));
	}
	public static void main(String[] args)
	{
		System.out.println(getSampleData());
		//
		/*for(int i=1;i<=31;i++) {
			String row="{\"title\":\""+i+"\", field:\"day"+i+"\", editor:\"input\",\"headerSort\":false},\r\n" + 
					   "{\"title\":\"Entry_"+i+"\", \"field\":\"ID_"+i+"\", \"editor\":\"input\",\"headerSort\":false,\"visible\":false},";
			System.out.println(row);
		}
		*/
		//
		//
		System.exit(0);
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
