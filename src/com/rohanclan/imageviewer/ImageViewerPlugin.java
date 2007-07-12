/* 
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
package com.rohanclan.imageviewer;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

//import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * The main plugin class to be used in the desktop.
 */
public class ImageViewerPlugin extends AbstractUIPlugin {
	
	/** The bundle of resources for the plugin */
	private ResourceBundle resourceBundle;
	
//	The shared instance.
	private static ImageViewerPlugin plugin;
	
	/* public ImageViewerPlugin(IPluginDescriptor descriptor) {
		super(descriptor);
	} */
	
	public ImageViewerPlugin() 
	{
		super();
		plugin = this;
		try 
		{
			resourceBundle = ResourceBundle.getBundle("plugin");	
		} 
		catch (MissingResourceException x) 
		{
			x.printStackTrace(System.err);
			resourceBundle = null;
		}
	}
	
	public ResourceBundle getResourceBundle()
	{
		return resourceBundle;
	}
	
	/**
	 * Returns the shared instance.
	 */
	public static ImageViewerPlugin getDefault() 
	{
		return plugin;
	}
}
