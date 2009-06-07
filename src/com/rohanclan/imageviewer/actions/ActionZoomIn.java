package com.rohanclan.imageviewer.actions;

import org.eclipse.jface.action.IAction;
import com.rohanclan.imageviewer.views.SWTImageCanvas;

public class ActionZoomIn extends ImageActionDelegate {

	public void run(IAction action) {
		SWTImageCanvas imageCanvas = view.imageCanvas;

		if (imageCanvas.getSourceImage() == null)
			return;

		imageCanvas.zoomIn();
		return;
	}
}
