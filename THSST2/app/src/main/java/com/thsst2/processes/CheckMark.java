package com.trial;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class CheckMark {

	private Mat srcOrig;
	
	public void readFile(String filename) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		try {
			File fileSrc = new File(filename);
			BufferedImage bufferedSrc = ImageIO.read(fileSrc);
		
			this.srcOrig = new Mat(bufferedSrc.getHeight(), bufferedSrc.getWidth(), CvType.CV_8UC4);
			this.srcOrig = this.bufferedImageToMat(bufferedSrc);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private Mat bufferedImageToMat(BufferedImage bi) {
		Mat mat = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC3);
		
		byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
		mat.put(0, 0, data);
		
		return mat;
	}
	
	public Mat getCheckMark() {
		return this.srcOrig;
	}
	
}
