package com.rohanclan.imageviewer.actions;

/*
 * Copyright (c) 2005 Rob Rohan
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

import com.rohanclan.images.ImageResize;
import com.rohanclan.imageviewer.editor.ImageViewEditor;

/**
 * @author Rob
 * 
 */
public class ResizeImageAction implements IEditorActionDelegate {
	// protected ITextEditor editor = null;
	protected ImageViewEditor editor = null;

	// protected ImageResize imageresize = null;

	/**
	 * the actual color control, this might not be good as static on all OSs but
	 * works pretty well on the Mac... keep an eye...
	 */
	// protected static ColorDialog colordialog = null;
	/** if they do #ffffff this would be # so we can re add the # */
	// protected String preColorString = "";
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		if (targetEditor instanceof ImageViewEditor) {
			editor = (ImageViewEditor) targetEditor;
		}
	}

	/**
	 * this gets called for every action
	 */
	public void run(IAction action) {
		if (editor != null) {
			//ID will look like id="I.S_.5"
			String id = action.getId();
			//get the size multiplier
			String resizecommand = id.substring(id.indexOf("_") + 1);

			//System.err.println(resizecommand);

			if (resizecommand.indexOf("x") > 0) {
				String[] wh = resizecommand.split("x");

				//System.err.println(Integer.parseInt(wh[0]) + " " + Integer.parseInt(wh[1]));

				this.resizeAndSaveNewImage(Integer.parseInt(wh[0]), Integer.parseInt(wh[1]));
			} else {
				float mult = Float.parseFloat(resizecommand);
				this.resizeAndSaveNewImage(mult);
			}
			
			//refresh the file view to see the new file
			IResource myresource = ResourcesPlugin.getWorkspace().getRoot().findMember(
				new File(
					editor.imageCanvas.getFileName()
				).getParent()
			);
			//on Mac the SWT AWT bit calls another thread so this might
			//not work right away. For a hoot call refresh 3 times.
			try {
				myresource.refreshLocal(IResource.DEPTH_INFINITE, null);
			} catch (CoreException e) {
				e.printStackTrace();
			}
			
		}
	}

	public void resizeAndSaveNewImage(int w, int h) {
		this.resizeImage(1, w, h);
	}

	public void resizeAndSaveNewImage(float multiplier) {
		this.resizeImage(multiplier, 0, 0);
	}

	private void resizeImage(float mult, int w, int h) {
		int height = h;
		int width = w;

		// get the current files place on disk
		String origfilename = editor.imageCanvas.getFileName();
		File f = new File(origfilename);

		//System.err.println("Going to try: " + f.getParent() + " " + mult);
		
		ImageResize imageresize = new ImageResize();
		imageresize.openImage(origfilename);

		if (width == 0 && height == 0) {
			width = editor.imageCanvas.getImageData().width;
			height = editor.imageCanvas.getImageData().height;
		}
		//System.err.println("Going to show the dialog...");
		
		InputDialog inputDialog = new InputDialog(
			editor.getSite().getShell(), 
			"New File Name",
			"Please enter a new file name", 
			f.getName(), null
		);
		
		//System.err.println("showed the dialog...");
		
		if (inputDialog.open() == InputDialog.OK) {
			String result = inputDialog.getValue();
			imageresize.resize((int) (width * mult), (int) (height * mult));
			try{
				imageresize.saveAsGuessType(f.getParent() + File.separator + result);
			}catch(IOException e){
				e.printStackTrace();
			}
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {;}
}
