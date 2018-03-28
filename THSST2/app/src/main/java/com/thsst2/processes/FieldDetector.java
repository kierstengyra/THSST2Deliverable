package com.thsst2.processes;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class FieldDetector {
	
	private Mat srcOrig;
	
	public void readFile(String filename) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		try {
			File fileSrc = new File(filename);
			BufferedImage bufferedSrc = ImageIO.read(fileSrc);
		
			this.srcOrig = new Mat(bufferedSrc.getHeight(), bufferedSrc.getWidth(), CvType.CV_8UC4);
			this.srcOrig = this.bufferedImageToMat(bufferedSrc);
			
			this.extract();
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
	
	private void extract() {
		Mat srcGray = new Mat();
		Mat scratch = this.srcOrig.clone();
		
		Imgproc.cvtColor(srcOrig, srcGray, Imgproc.COLOR_BGR2GRAY);
		Imgproc.adaptiveThreshold(srcGray, srcGray, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 2);
		this.drawSquares(scratch);
		Imgcodecs.imwrite("FD6.jpg", scratch);	
	
		System.out.println("Done.");
	}
	
	private void drawSquares(Mat scratch) {
		for(int i = 0; i < 15; i++) {
			double x = FieldManager.getInstance().getField(i).getX()*this.srcOrig.width();
			double y = FieldManager.getInstance().getField(i).getY()*this.srcOrig.height();
			
			double width = FieldManager.getInstance().getField(i).getWidth()*this.srcOrig.width();
			double height = FieldManager.getInstance().getField(i).getHeight()*this.srcOrig.height();
			
			Imgproc.rectangle(scratch, new Point(x,y), new Point(x+width,y+height), new Scalar(0, 0, 255));
		}
	}
}
