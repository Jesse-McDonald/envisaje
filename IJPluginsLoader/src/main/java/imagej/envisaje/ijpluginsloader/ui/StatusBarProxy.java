//
// SwingStatusBar.java
//

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2010, ImageJDev.org.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the names of the ImageJDev.org developers nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package imagej.envisaje.ijpluginsloader.ui;

import imagej.event.EventSubscriber;
import imagej.event.Events;
import imagej.event.StatusEvent;
import imagej.ui.StatusBar;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;
import javax.swing.JProgressBar;

/**
 * StatusBar proxy for NBPlatform
 *
 * @author Curtis Rueden
 * @author Grant Harris
 */
public class StatusBarProxy extends JPanel
	implements StatusBar, EventSubscriber<StatusEvent>
{

	private final JProgressBar progressBar;

	public StatusBarProxy() {
		progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		setLayout(new BorderLayout());
		add(progressBar, BorderLayout.CENTER);
		setStatus("");
		Events.subscribe(StatusEvent.class, this);

		progressBar.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(final MouseEvent evt) {
				System.gc();
				Events.publish(new StatusEvent(getInfoString()));
			}
		});
	}

	// -- StatusBar methods --

	@Override
	public void setStatus(final String message) {
		progressBar.setString(message == null ? "" : message);
	}

	@Override
	public void setProgress(final int val, final int max) {
		if (val >= 0 && val < max) {
			progressBar.setValue(val);
			progressBar.setMaximum(max);
		}
		else {
			progressBar.setValue(0);
			progressBar.setMaximum(1);
		}
	}

	// -- EventSubscriber methods --

	@Override
	public void onEvent(final StatusEvent event) {
		final String message = event.getStatusMessage();
		final int val = event.getProgressValue();
		final int max = event.getProgressMaximum();
		setStatus(message);
		setProgress(val, max);
	}

	// -- Helper methods --

	protected String getInfoString() {
		final String javaVersion = System.getProperty("java.version");
		final String osArch = System.getProperty("os.arch");
		final long maxMem = Runtime.getRuntime().maxMemory();
		final long totalMem = Runtime.getRuntime().totalMemory();
		final long freeMem = Runtime.getRuntime().freeMemory();
		final long usedMem = totalMem - freeMem;
		final long usedMB = usedMem / 1048576;
		final long maxMB = maxMem / 1048576;
		return "ImageJ 2.0.0-alpha1; Java " + javaVersion +
			" [" + osArch + "]; " + usedMB + "MB of " + maxMB + "MB";
	}

}
