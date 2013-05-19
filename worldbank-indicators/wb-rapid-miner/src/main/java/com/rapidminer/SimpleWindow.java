/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2009 by Rapid-I and the contributors
 *
 *  Complete list of developers available at our web site:
 *
 *       http://rapid-i.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package com.rapidminer;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.rapidminer.gui.tools.ResourceDockKey;
import com.vlsolutions.swing.docking.DockKey;
import com.vlsolutions.swing.docking.Dockable;

/**
 * A very simple example of a new dockable window.
 * @author Sebastian Land
 */
public class SimpleWindow extends JPanel implements Dockable {

	private static final long serialVersionUID = 1L;

	private final DockKey DOCK_KEY = new ResourceDockKey("tutorial.simple_window");
	
	private JLabel label = new JLabel("Hello user.");
	
	public SimpleWindow() {
		// adding content to this window
		setLayout(new BorderLayout());
		add(label, BorderLayout.CENTER);
	}

	public void setLabel(String labelText) {
		this.label.setText(labelText+"TEST");
		System.out.println(labelText);
		revalidate();
	}
	
	@Override
	public Component getComponent() {
		return this;
	}

	@Override
	public DockKey getDockKey() {
		return DOCK_KEY;
	}
}
