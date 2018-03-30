package com.trial;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.opencv.core.Core;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.photo.Photo;

public class FieldDetector {
	
	private Mat srcOrig;
	private Mat srcGray;
	private Mat template;
	
	public void readFile(String filename, String destname) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		try {
			File fileSrc = new File(filename);
			BufferedImage bufferedSrc = ImageIO.read(fileSrc);
		
			this.srcOrig = new Mat(bufferedSrc.getHeight(), bufferedSrc.getWidth(), CvType.CV_8UC3);
			this.srcOrig = this.bufferedImageToMat(bufferedSrc);
			
			this.extract(destname);
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
	
	private void extract(String destname) {
		//---Do this once the app starts
		this.template = Imgcodecs.imread("C:/Users/gyra/Desktop/checkmarks/Check1.jpg");
		Imgproc.cvtColor(template, template, Imgproc.COLOR_BGR2GRAY);
		//--------------------------------
		
		this.srcGray = new Mat();
		
		Imgproc.cvtColor(srcOrig, srcGray, Imgproc.COLOR_BGR2GRAY);
		Photo.fastNlMeansDenoising(srcGray, srcGray);
		Imgproc.adaptiveThreshold(srcGray, srcGray, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 21, 2);	
		
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Mat hierarchy = new Mat();
		Imgproc.findContours(srcGray, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

		this.detectChecks();
		System.out.println("Done.");
	}
	
	private void detectChecks() {
		for(int i = 0; i < 15; i++) {
			
			double x = FieldManager.getInstance().getField(i).getX()*this.srcOrig.width();
			double y = FieldManager.getInstance().getField(i).getY()*this.srcOrig.height();
			double width = FieldManager.getInstance().getField(i).getWidth()*this.srcOrig.width();
			double height = FieldManager.getInstance().getField(i).getHeight()*this.srcOrig.height();
			
			Rect roi = new Rect(new Point(x, y), new Point(x+width, y+height));
			Mat roi_submat = srcGray.submat(roi);
			int result_cols = roi_submat.cols() - this.template.cols() + 1;
			int result_rows = roi_submat.rows() - this.template.rows() + 1;
			Mat result = new Mat(result_rows, result_cols, CvType.CV_8U);
			
			Imgproc.matchTemplate(roi_submat, this.template, result, Imgproc.TM_SQDIFF);
			Core.normalize(result, result, 0, 1, Core.NORM_MINMAX, -1, new Mat());
			MinMaxLocResult mmr = Core.minMaxLoc(result);				
			Point matchLoc = mmr.minLoc;
			Rect new_roi = new Rect(new Point(matchLoc.x, matchLoc.y), new Point(matchLoc.x+template.cols(), matchLoc.y+template.rows()));
			roi_submat = roi_submat.submat(new_roi);	
			
			List<MatOfPoint> template_contours = new ArrayList<MatOfPoint>();
			Mat template_hierarchy = new Mat();
			Imgproc.findContours(this.template, template_contours, template_hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
			
			List<MatOfPoint> roi_contours = new ArrayList<MatOfPoint>();
			Mat roi_hierarchy = new Mat();
			Imgproc.findContours(roi_submat, roi_contours, roi_hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
			
			double similarity = Imgproc.matchShapes(template, roi_submat, Imgproc.CV_CONTOURS_MATCH_I1, 0) * 100;
			
			Imgproc.cvtColor(roi_submat, roi_submat, Imgproc.COLOR_GRAY2BGR);
			Imgproc.drawContours(roi_submat, roi_contours, -1, new Scalar(0, 0, 255));
			Imgcodecs.imwrite("Test"+(i+1)+".jpg", roi_submat);
		
		}
	}
}
