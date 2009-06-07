/*
 * Created on Jan 30, 2005
 *
 * The MIT License
 * Copyright (c) 2004 Rob Rohan, Oliver Tupman
 *
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the "Software"), 
 * to deal in the Software without restriction, including without limitation 
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons to whom the Software 
 * is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in 
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, 
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE 
 * SOFTWARE.
 */
package com.rohanclan.imageviewer.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import com.rohanclan.imageviewer.editor.ImageViewEditor;
import com.rohanclan.imageviewer.views.SWTImageCanvas;


/**
 * @author Rob Rohan 
 */
public class ImageActionDelegate implements IEditorActionDelegate {

	public static final String ID_ZOOMIN = "ImageViewer.toolbar.zoomin";
	public static final String ID_ZOOMOUT = "ImageViewer.toolbar.zoomout";
	public static final String ID_ORIGINAL = "ImageViewer.toolbar.original";
	public static final String ID_FIT = "ImageViewer.toolbar.fit";
	public static final String ID_ROTATE = "ImageViewer.toolbar.rotate";
	
	/** pointer to image view */	
	public ImageViewEditor view = null;
	/** Action id of this delegate */
	public String id;
	
    /**
	 * 
	 */
	public ImageActionDelegate() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorActionDelegate#setActiveEditor(org.eclipse.jface.action.IAction, org.eclipse.ui.IEditorPart)
	 */
	public void setActiveEditor(IAction action, IEditorPart targetEditor) 
	{	
		if(targetEditor instanceof ImageViewEditor){
			this.view = (ImageViewEditor)targetEditor;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	//TODO: Break these out into actions like with ZoomOut ZoomIn
	public void run(IAction action) 
	{
		String id = action.getId();
		//System.out.println("running " + id.toString());
		SWTImageCanvas imageCanvas = view.imageCanvas;
		
		if (imageCanvas.getSourceImage() == null) 
			return;
		
		if (id.equals(ID_ZOOMIN))
		{
			imageCanvas.zoomIn();
			return;
		}
		else if (id.equals(ID_ZOOMOUT))
		{
			imageCanvas.zoomOut();
			return;
		}
		else if (id.equals(ID_FIT))
		{
			imageCanvas.fitCanvas();
			return;
		}
		else if (id.equals(ID_ROTATE))
		{
			/* rotate image anti-clockwise */
			ImageData src = imageCanvas.getImageData();
			if(src == null) 
				return;
			PaletteData srcPal = src.palette;
			PaletteData destPal;
			ImageData dest;
			
			/* construct a new ImageData */
			if(srcPal.isDirect){
				destPal = new PaletteData(
					srcPal.redMask, srcPal.greenMask, srcPal.blueMask
				);
			}else{
				destPal = new PaletteData(srcPal.getRGBs());
			}
			
			dest = new ImageData(src.height, src.width, src.depth, destPal);
			
			/* rotate by rearranging the pixels */
			for(int i=0; i < src.width; i++)
			{
				for(int j=0; j < src.height; j++)
				{
					int pixel = src.getPixel(i,j);
					dest.setPixel(j,src.width-1-i,pixel);
				}
			}
			
			imageCanvas.setImageData(dest);
			return;
			
		}
		else if (id.equals(ID_ORIGINAL))
		{
			imageCanvas.showOriginal();
			return;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {;}
}
