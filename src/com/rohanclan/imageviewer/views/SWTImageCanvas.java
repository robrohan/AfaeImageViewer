/*******************************************************************************
* Copyright (c) 2004 Chengdong Li : cdli@ccs.uky.edu
* All rights reserved. This program and the accompanying materials 
* are made available under the terms of the Common Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/cpl-v10.html
*******************************************************************************/
package com.rohanclan.imageviewer.views;

import java.awt.geom.AffineTransform;
//import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
//import org.eclipse.swt.graphics.Color;
//import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
//import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.ScrollBar;

import com.rohanclan.imageviewer.ImageViewerPlugin;

/**
 * A scrollable image canvas that extends org.eclipse.swt.graphics.Canvas.
 * <p/>
 * It requires Eclipse (version >= 2.1) on Win32/win32; Linux/gtk; MacOSX/carbon.
 * <p/>
 * This implementation using the pure SWT, no UI AWT package is used. For 
 * convenience, I put everything into one class. However, the best way to
 * implement this is to use inheritance to create multiple hierarchies.
 * 
 * @author Chengdong Li: cli4@uky.edu
 */
public class SWTImageCanvas extends Canvas 
{
	private static final String FALLBACK_IMAGE = "icons/misc/fileerror.png";
	
	/* zooming rates in x and y direction are equal.*/
	static final float ZOOMIN_RATE = 3.0f; /* zoomin rate */
	static final float ZOOMOUT_RATE = 0.3f; /* zoomout rate */
	
	private Image sourceImage; /* original image */
	private Image screenImage; /* screen image */
	
	private AffineTransform transform = new AffineTransform();

	private boolean hadloadError = false;
	private String errorhint = "";
	
	private String filename = null;
	
	
	//private String currentDir = ""; /* remembering file open directory */
	//private Color BGColor;
	
	public SWTImageCanvas(final Composite parent) 
	{
		this(parent, SWT.NULL);
		//BGColor = new Color((Device)parent.getDisplay(),91,91,91);
	}

	/**
	 * Constructor for ScrollableCanvas.
	 * @param parent the parent of this control.
	 * @param style the style of this control.
	 */
	public SWTImageCanvas(final Composite parent, int style) 
	{	
		super(parent,style|SWT.BORDER|SWT.V_SCROLL|SWT.H_SCROLL); //|SWT.NO_BACKGROUND
		
		/* resize listener. */
		addControlListener(new ControlAdapter() { 
			public void controlResized(ControlEvent event) {
				syncScrollBars();
			}
		});
		
		/* paint listener. */
		addPaintListener(new PaintListener() {
			public void paintControl(final PaintEvent event) {
				paint(event.gc);
			}
		});
		
		initScrollBars();
	}

	/**
	 * Dispose the garbage here
	 */
	public void dispose() 
	{
		if (sourceImage != null && !sourceImage.isDisposed()) {
			sourceImage.dispose();
		}
		
		if (screenImage != null && !screenImage.isDisposed()) {
			screenImage.dispose();
		}
		
		//BGColor.dispose();
	}

	/**
	 *  Paint function 
	 */
	private void paint(GC gc) 
	{
		//Canvas' painting area
		Rectangle clientRect = getClientArea(); 
		if (sourceImage != null) 
		{
			Rectangle imageRect = SWT2Dutil.inverseTransformRect(transform, clientRect);
			
			//find a better start point to render
			int gap = 2; 
			
			imageRect.x -= gap; 
			imageRect.y -= gap;
			imageRect.width += 2 * gap; 
			imageRect.height += 2 * gap;
			//imageRect.width += gap >> 1; 
			//imageRect.height += gap >> 1;

			Rectangle imageBound = sourceImage.getBounds();
			imageRect = imageRect.intersection(imageBound);
			Rectangle destRect = SWT2Dutil.transformRect(transform, imageRect);

			if (screenImage != null)
				screenImage.dispose();
			
			screenImage = new Image(
				getDisplay(), clientRect.width, clientRect.height
			);
			
			GC newGC = new GC(screenImage);
			
			newGC.setBackground(getShell().getDisplay().getSystemColor(SWT.COLOR_GRAY));
			newGC.setForeground(getShell().getDisplay().getSystemColor(SWT.COLOR_GRAY));
			newGC.fillGradientRectangle(0, 0, clientRect.width, clientRect.height, true);
			
			//fill the whole background with a gradient so the image background can
			//be seen a bit eaiser
			newGC.setForeground(getShell().getDisplay().getSystemColor(SWT.COLOR_WHITE));
			newGC.setBackground(getShell().getDisplay().getSystemColor(SWT.COLOR_GRAY));
			//newGC.fillGradientRectangle(0, 0, clientRect.width, clientRect.height, true);
			newGC.fillGradientRectangle(destRect.x-1, destRect.y-1, destRect.width+1, destRect.height+1, true);
			
			newGC.setForeground(getShell().getDisplay().getSystemColor(SWT.COLOR_BLACK));
			//draw a littl box around the image
			newGC.setLineStyle(SWT.LINE_DASHDOTDOT);
			newGC.drawRectangle(destRect.x-1, destRect.y-1, destRect.width+1, destRect.height+1);
			
			newGC.setClipping(clientRect);
			newGC.drawImage(
				sourceImage,
				imageRect.x, imageRect.y,
				imageRect.width, imageRect.height,
				destRect.x,	destRect.y,
				destRect.width,	destRect.height
			);
			newGC.dispose();

			gc.drawImage(screenImage, 0, 0);
		} 
		else 
		{
			gc.setClipping(clientRect);
			gc.fillRectangle(clientRect);
			initScrollBars();
		}
	}

	/**
	 *  Initalize the scrollbar and register listeners. 
	 */
	private void initScrollBars() 
	{
		ScrollBar horizontal = getHorizontalBar();
		horizontal.setEnabled(false);
		
		horizontal.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				scrollHorizontally((ScrollBar) event.widget);
			}
		});
		
		ScrollBar vertical = getVerticalBar();
		vertical.setEnabled(false);
		vertical.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				scrollVertically((ScrollBar) event.widget);
			}
		});
	}

	/**
	 * Scroll horizontally 
	 */
	private void scrollHorizontally(ScrollBar scrollBar) 
	{
		if (sourceImage == null)
			return;
		
		AffineTransform af = transform;
		double tx = af.getTranslateX();
		double select = -scrollBar.getSelection();
		af.preConcatenate(AffineTransform.getTranslateInstance(select - tx, 0));
		transform = af;
		syncScrollBars();
	}

	/**
	 *  Scroll vertically 
	 */
	private void scrollVertically(ScrollBar scrollBar) 
	{
		if (sourceImage == null)
			return;

		AffineTransform af = transform;
		double ty = af.getTranslateY();
		double select = -scrollBar.getSelection();
		af.preConcatenate(AffineTransform.getTranslateInstance(0, select - ty));
		transform = af;
		syncScrollBars();
	}

	/**
	 * Source image getter.
	 * @return sourceImage.
	 */
	public Image getSourceImage() {
		return sourceImage;
	}

	/**
	 * Synchronize the scrollbar with the image. If the transform is out
	 * of range, it will correct it. This function considers only following
	 * factors :<b> transform, image size, client area</b>.
	 */
	public void syncScrollBars() {
		if (sourceImage == null) {
			redraw();
			return;
		}

		AffineTransform af = transform;
		double sx = af.getScaleX(), sy = af.getScaleY();
		double tx = af.getTranslateX(), ty = af.getTranslateY();
		if (tx > 0) tx = 0;
		if (ty > 0) ty = 0;

		ScrollBar horizontal = getHorizontalBar();
		horizontal.setIncrement((int) (getClientArea().width / 100));
		horizontal.setPageIncrement(getClientArea().width);
		Rectangle imageBound = sourceImage.getBounds();
		int cw = getClientArea().width, ch = getClientArea().height;
		
		//image is wider than client area
		if (imageBound.width * sx > cw) { 
			horizontal.setMaximum((int) (imageBound.width * sx));
			horizontal.setEnabled(true);
			if (((int) - tx) > horizontal.getMaximum() - cw)
				tx = -horizontal.getMaximum() + cw;
		} else {
			//image is narrower than client area
			horizontal.setEnabled(false);
			
			//center if too small.
			tx = (cw - imageBound.width * sx) / 2; 
		}
		
		horizontal.setSelection((int) (-tx));
		horizontal.setThumb((int) (getClientArea().width));

		ScrollBar vertical = getVerticalBar();
		vertical.setIncrement((int) (getClientArea().height / 100));
		vertical.setPageIncrement((int) (getClientArea().height));
		
		//image is higher than client area
		if (imageBound.height * sy > ch) { 
			vertical.setMaximum((int) (imageBound.height * sy));
			vertical.setEnabled(true);
			if (((int) - ty) > vertical.getMaximum() - ch)
				ty = -vertical.getMaximum() + ch;
		//image is less higher than client area
		} else { 
			vertical.setEnabled(false);
			//center if too small.
			ty = (ch - imageBound.height * sy) / 2; 
		}
		vertical.setSelection((int) (-ty));
		vertical.setThumb((int) (getClientArea().height));

		//update transform.
		af = AffineTransform.getScaleInstance(sx, sy);
		af.preConcatenate(AffineTransform.getTranslateInstance(tx, ty));
		transform = af;

		redraw();
	}

	/**
	 * Reload image from a file
	 * @param filename image file
	 * @return swt image created from image file
	 */
	public Image loadImage(String filename) 
	{
		if (sourceImage != null && !sourceImage.isDisposed()) 
		{
			sourceImage.dispose();
			sourceImage = null;
		}
		
		try
		{
			this.filename = filename;
			sourceImage = new Image(getDisplay(), filename);
			this.hadloadError = false;
		}
		catch(SWTException swte)
		{
			//if there is an error load the error image
			sourceImage = new Image(getDisplay(), getFallbackImage());
			this.hadloadError = true;
			this.errorhint = swte.getMessage();
		}
		
		showOriginal();
		return sourceImage;
	}

	private String getFallbackImage()
	{
		try 
		{
			URL installURL = ImageViewerPlugin.getDefault().getBundle().getEntry("/");
			//URL snipsdir = Platform.resolve(new URL(installURL, FALLBACK_IMAGE));
			URL snipsdir = FileLocator.resolve(new URL(installURL, FALLBACK_IMAGE));
			return snipsdir.getFile();
		}
		catch (MalformedURLException e) 
		{
			e.printStackTrace();
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		return null;
	}
		
	/**
	 * Call back funtion of button "open". Will open a file dialog, and choose
	 * the image file. It supports image formats supported by Eclipse.
	 */
	/* public void onFileOpen() 
	{
		FileDialog fileChooser = new FileDialog(getShell(), SWT.OPEN);
		fileChooser.setText("Open image file");
		
		fileChooser.setFilterPath(currentDir);
		
		fileChooser.setFilterExtensions(
			new String[] { "*.gif; *.jpg; *.png; *.ico; *.bmp" }
		);
		
		fileChooser.setFilterNames(
			new String[] { "SWT image" + " (gif, jpeg, png, ico, bmp)" }
		);
		
		String filename = fileChooser.open();
		if (filename != null)
		{
			loadImage(filename);
			currentDir = fileChooser.getFilterPath();
		}
	} */

	/**
	 * Get the image data. (for future use only)
	 * @return image data of canvas
	 */
	public ImageData getImageData() 
	{
		return sourceImage.getImageData();
	}

	/**
	 * Reset the image data and update the image
	 * @param data image data to be set
	 */
	public void setImageData(ImageData data) 
	{
		if (sourceImage != null)
			sourceImage.dispose();
		
		if (data != null)
			sourceImage = new Image(getDisplay(), data);
		
		syncScrollBars();
	}

	/**
	 * Call this method to load an image from within the IDE
	 * @param file
	 */
	public void loadImageFromFile(String file)
	{
		filename = file;
		sourceImage = loadImage(file); 
		setImageData(getImageData());		
	}
	
	/**
	 * 
	 * @return
	 */
	public String getFileName()
	{
		return filename;
	}
	
	/**
	 * Fit the image onto the canvas
	 */
	public void fitCanvas() 
	{
		if (sourceImage == null)
			return;
		
		Rectangle imageBound = sourceImage.getBounds();
		Rectangle destRect = getClientArea();
		
		double sx = (double) destRect.width / (double) imageBound.width;
		double sy = (double) destRect.height / (double) imageBound.height;
		double s = Math.min(sx, sy);
		double dx = 0.5 * destRect.width;
		double dy = 0.5 * destRect.height;
		
		centerZoom(dx, dy, s, new AffineTransform());
	}

	/**
	 * Show the image with the original size
	 */
	public void showOriginal() 
	{
		if (sourceImage == null)
			return;
		
		transform = new AffineTransform();
		syncScrollBars();
	}

	/**
	 * Perform a zooming operation centered on the given point
	 * (dx, dy) and using the given scale factor. 
	 * The given AffineTransform instance is preconcatenated.
	 * @param dx center x
	 * @param dy center y
	 * @param scale zoom rate
	 * @param af original affinetransform
	 */
	public void centerZoom(double dx, double dy, double scale, AffineTransform af) 
	{
		af.preConcatenate(AffineTransform.getTranslateInstance(-dx, -dy));
		af.preConcatenate(AffineTransform.getScaleInstance(scale, scale));
		af.preConcatenate(AffineTransform.getTranslateInstance(dx, dy));
		
		transform = af;
		syncScrollBars();
	}

	/**
	 * Zoom in around the center of client Area.
	 */
	public void zoomIn() 
	{
		if (sourceImage == null)
			return;
		
		Rectangle rect = getClientArea();
		int w = rect.width, h = rect.height;
		
		double dx = (double)(w << 1);
		double dy = (double)(h << 1);
		
		centerZoom(dx, dy, ZOOMIN_RATE, transform);
	}

	/**
	 * Zoom out around the center of client Area.
	 */
	public void zoomOut() 
	{
		if (sourceImage == null)
			return;
		
		Rectangle rect = getClientArea();
		int w = rect.width, h = rect.height;
		
		double dx = (double)(w << 1);
		double dy = (double)(h << 1);
		
		centerZoom(dx, dy, ZOOMOUT_RATE, transform);
	}

	/**
	 * True if this canvas had a loading error. It will display an error image
	 * if it cant load the passed image so this is the only way to tell if the 
	 * load went ok.
	 * @return
	 */
	public boolean hadloadError() 
	{
		return hadloadError;
	}

	/**
	 * If there was an error this will show the exception
	 * @return
	 */
	public String getErrorhint() 
	{
		return errorhint;
	}
}