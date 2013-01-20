/*
 * @(#) jqIRC	0.4	08/12/2001
 *
 * Copyright (c), 2000 by jqIRC, Inc. ^-^
 *
 * License: We grant you this piece of source code to play with
 * as you wish provided that 1) you buy us a drink when we meet
 * somewhere someday. 2) Incase you don't want to fullfill the
 * first condition, you just buy something for one of your
 * beloved friends. 3) Attach below messages somewhere. ^-^
 *
 *                To some people, a friend
 *            is practically anyone they know.
 *                To me, friendship means
 *              a much closer relationship,
 *            one in which you take the time
 *            to really understand each other.
 *         A friend is someone you trust enough
 *              to share a part of yourself
 *         the rest of the world may never see.
 *               That kind of friendship
 *            doesn't come along everyday...
 *            but that's the way it should be.
 *
 */

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Test extends JApplet {
	private ConstraintsPanel cp = new ConstraintsPanel();
	private JPanel buttonsPanel = new JPanel();  

	private JButton showButton = new JButton("show dialog ..."),
					okButton = new JButton("OK"),
					applyButton = new JButton("Apply"),
					cancelButton = new JButton("Cancel");

	private JButton[] buttons = new  JButton[] {
		okButton, applyButton, cancelButton,
	};

	private JDialog dialog = new JDialog(null, // owner
								"Constraints Dialog", // title
								true); // modal

	public Test() {
		Container contentPane = getContentPane();
		Container dialogContentPane = dialog.getContentPane();

		contentPane.setLayout(new FlowLayout());
		contentPane.add(showButton);

		dialogContentPane.add(cp, BorderLayout.CENTER);
		dialogContentPane.add(buttonsPanel, BorderLayout.SOUTH);
		dialog.pack();

		// setLocationRelativeTo must be called after pack(),
		// because dialog placement is based on dialog size.
		// Because the applet is not yet showing, calling
		// setLocationRelativeTo() here causes the dialog to be
		// shown centered on the screen.
		//
		// If setLocationRelativeTo() is not invoked, the dialog
		// will be located at (0,0) in screen coordinates.
		//dialog.setLocationRelativeTo(this);

		for(int i=0; i < buttons.length; ++i) {
			buttonsPanel.add(buttons[i]);
		}
		addButtonListeners();
	}
	private void addButtonListeners() {
		showButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// calling setLocationRelativeTo() here causes
				// the dialog ito be centered over the applet.
				dialog.setLocationRelativeTo(Test.this);
				dialog.show();
			}
		});
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showStatus("OK button Activated");
				dialog.dispose();
			}
		});
		applyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showStatus("Apply button Activated");
			}
		});
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showStatus("Cancel button Activated");
				dialog.dispose();
			}
		});
	}
}
