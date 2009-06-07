/*
 * Copyright (c) 2005 RohanClan and others.
 *
 * Contributors:
 *    IBM - Initial API and implementation   
 */
/*******************************************************************************
 * Copyright (c) 2004 Chengdong Li : cdli@ccs.uky.edu
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/
package com.rohanclan.imageviewer.editor;

//import java.net.URL;

import java.net.URI;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.SWT; //import org.eclipse.swt.graphics.Color;
//import org.eclipse.swt.graphics.Device;
//import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label; //import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.editors.text.ILocationProvider;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.part.EditorPart;

import com.rohanclan.imageviewer.views.SWTImageCanvas;

/**
 * An integrated Image viewer, defined as an editor to make better use of the
 * desktop.
 */
public class ImageViewEditor extends EditorPart {
	public static final String IMAGE_VIEW_EDITOR_ID = "com.rohanclan.imageviewer.editor";

	protected String initialURL;
	protected int fileSize = 0;

	public SWTImageCanvas imageCanvas;
	protected Label infoLabel;
	protected Label sizeLabel;

	/**
	 * ImageViewEditor constructor comment.
	 */
	public ImageViewEditor() {
		super();
	}

	/**
	 * Creates the SWT controls for this workbench part.
	 * <p>
	 * Clients should not call this method (the workbench calls this method at
	 * appropriate times).
	 * </p>
	 * <p>
	 * For implementors this is a multi-step process:
	 * <ol>
	 * <li>Create one or more controls within the parent.</li>
	 * <li>Set the parent layout as needed.</li>
	 * <li>Register any global actions with the <code>IActionService</code>.</li>
	 * <li>Register any popup menus with the <code>IActionService</code>.</li>
	 * <li>Register a selection provider with the <code>ISelectionService</code>
	 * (optional).</li>
	 * </ol>
	 * </p>
	 * 
	 * @param parent
	 *            the parent control
	 */
	public void createPartControl(Composite parent) {
		// parent.setBackground(new
		// Color((Device)parent.getDisplay(),91,91,91));

		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.verticalSpacing = 2;
		layout.marginWidth = 0;
		layout.marginHeight = 2;
		parent.setLayout(layout);

		// layout the image viewer
		GridData layoutData = new GridData();
		layoutData.grabExcessHorizontalSpace = true;
		layoutData.grabExcessVerticalSpace = true;
		layoutData.horizontalAlignment = GridData.FILL;
		layoutData.verticalAlignment = GridData.FILL;

		imageCanvas = new SWTImageCanvas(parent, SWT.NULL);
		imageCanvas.loadImage(initialURL);

		imageCanvas.setLayoutData(layoutData);

		Composite container1 = new Composite(parent, SWT.NULL);

		GridLayout thisLayout = new GridLayout();
		container1.setLayout(thisLayout);
		{
			DimLabelName = new Label(container1, SWT.NONE);
			DimLabelName.setText("Dimensions:");
		}
		{
			DimLabelValue = new Label(container1, SWT.NONE);
			// DimLabelValue.setText("123x123");
		}
		{
			CDepthLabelName = new Label(container1, SWT.NONE);
			CDepthLabelName.setText("Color Depth:");
		}
		{
			CDepthLabelValue = new Label(container1, SWT.NONE);
			// CDepthLabelValue.setText("24");
		}
		{
			// SizeLabelName = new Label(container1, SWT.NONE);
			// SizeLabelName.setText("Size:");
		}
		{
			// SizeLabelValue = new Label(container1, SWT.NONE);
			// SizeLabelValue.setText("300k");
		}
		thisLayout.numColumns = 6;

		if (!imageCanvas.hadloadError()) {
			DimLabelValue.setText(imageCanvas.getImageData().width + "x"
					+ imageCanvas.getImageData().height);
			CDepthLabelValue.setText(imageCanvas.getImageData().depth + "");
			// SizeLabelValue.setText(imageCanvas.getImageData().data.length
			// +" Bytes");
		} else {
			DimLabelValue.setText(imageCanvas.getErrorhint());
			CDepthLabelValue.setText(imageCanvas.getErrorhint());
			// SizeLabelValue.setText(imageCanvas.getErrorhint());
			setPartName("Load Error");
			setTitleToolTip(imageCanvas.getErrorhint());
		}
	}

	private Label DimLabelName;
	private Label DimLabelValue;
	private Label CDepthLabelName;
	// private Label SizeLabelValue;
	// private Label SizeLabelName;
	private Label CDepthLabelValue;

	/**
	 * Initializes the editor part with a site and input.
	 * <p>
	 * Subclasses of <code>EditorPart</code> must implement this method. Within
	 * the implementation subclasses should verify that the input type is
	 * acceptable and then save the site and input. Here is sample code:
	 * </p>
	 * 
	 * <pre>
	 * if (!(input instanceof IFileEditorInput))
	 * 	throw new PartInitException(&quot;Invalid Input: Must be IFileEditorInput&quot;);
	 * setSite(site);
	 * setInput(editorInput);
	 * </pre>
	 */
	public void init(IEditorSite site, IEditorInput input) {
		String filename = null;
		String dspname = null;

		if (input instanceof IPathEditorInput) {
			// this gets called when you open an external file
			filename = ((IPathEditorInput) input).getPath().toOSString();
			dspname = ((IPathEditorInput) input).getName();
		} else if (input instanceof ILocationProvider) {
			// System.err.println("ilocation");
			IPath path = ((ILocationProvider) input).getPath(input);
			filename = path.toString();
			dspname = filename;
		} else if (input instanceof IStorageEditorInput) {
			// System.err.println("istorage");
			try {
				filename = ((IStorageEditorInput) input).getStorage().getName();
				dspname = filename;
			} catch (CoreException e) {
				e.printStackTrace(System.err);
			}
		} else if (input instanceof IFileEditorInput) {
			// System.err.println("ifileeditor");
			// this gets called when you open a file from the project
			IFileEditorInput fei = (IFileEditorInput) input;
			IFile file = fei.getFile();
			filename = file.getRawLocation().toOSString();
			dspname = file.getName();
		} else if (input instanceof FileStoreEditorInput) {
			URI fileuri = ((FileStoreEditorInput)input).getURI();
			filename = new Path(fileuri.getPath()).toOSString();
		}

		try {
			initialURL = filename;

			setPartName(dspname);
			setTitleToolTip(filename);
		} catch (Exception e) {
			// Trace.trace(Trace.SEVERE, "Error getting URL to file");
			e.printStackTrace();
		}

		setSite(site);
		setInput(input);
	}

	/*
	 * (non-Javadoc) Returns whether the contents of this editor have changed
	 * since the last save operation. <p> Subclasses must override this method
	 * to implement the open-save-close lifecycle for an editor. For greater
	 * details, see <code>IEditorPart</code> </p>
	 * 
	 * @see IEditorPart
	 */
	public boolean isDirty() {
		return false;
	}

	/*
	 * (non-Javadoc) Returns whether the "save as" operation is supported by
	 * this editor. <p> Subclasses must override this method to implement the
	 * open-save-close lifecycle for an editor. For greater details, see
	 * <code>IEditorPart</code> </p>
	 * 
	 * @see IEditorPart
	 */
	public boolean isSaveAsAllowed() {
		return false;
	}

	/**
	 * Asks this part to take focus within the workbench.
	 * <p>
	 * Clients should not call this method (the workbench calls this method at
	 * appropriate times).
	 * </p>
	 */
	public void setFocus() {
		imageCanvas.setFocus();
	}

	/**
	 * Close the editor correctly.
	 */
	protected void closeEditor() {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				getEditorSite().getPage().closeEditor(ImageViewEditor.this,
						false);
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.
	 * IProgressMonitor)
	 */
	public void doSave(IProgressMonitor monitor) {
		;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.EditorPart#doSaveAs()
	 */
	public void doSaveAs() {
		;
	}

	public void dispose() {
		imageCanvas.dispose();
		super.dispose();
	}
}