package com.thsst2.processes;

import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.photo.Photo;

import java.util.ArrayList;
import java.util.List;

public class FieldDetector {
	
	private Mat srcOrig;
	private Mat srcGray;
	private Mat output_mat;
	private Bitmap output_bmp;

	private double finalScore;

	public Bitmap analyze(Bitmap source) {
		this.convertToMat(source);
		this.extract();

		this.output_bmp = Bitmap.createBitmap(this.output_mat.cols(), this.output_mat.rows(), Bitmap.Config.ARGB_8888);
		Utils.matToBitmap(this.output_mat, this.output_bmp);
		return this.output_bmp;
	}

	private void convertToMat(Bitmap source) {
		Bitmap copy = source.copy(Bitmap.Config.ARGB_8888, true);
		this.srcOrig = new Mat(source.getHeight(), source.getWidth(), CvType.CV_8UC1);
		Utils.bitmapToMat(copy, this.srcOrig);

		this.srcGray = this.srcOrig.clone();
	}

	private void extract() {
		this.srcGray = new Mat();

		Imgproc.cvtColor(srcOrig, srcGray, Imgproc.COLOR_BGR2GRAY);
		Photo.fastNlMeansDenoising(srcGray, srcGray);
		Imgproc.adaptiveThreshold(srcGray, srcGray, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 41, 2);

		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Mat hierarchy = new Mat();
		Imgproc.findContours(srcGray, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

		this.detectChecks();
		this.finalScore = FieldManager.getInstance().computeScore();

		Log.e("FieldDetector", "Score: "+finalScore);
	}

	private void detectChecks() {
		Mat bw = this.srcGray.clone();
		Core.bitwise_not(srcGray, srcGray);
		Imgproc.adaptiveThreshold(srcGray, bw, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 15, -2);

		//Create containers for horizontal and vertical lines
		Mat horizontal = bw.clone();
		Mat vertical = bw.clone();

		//Horizontal+Vertical Lines
		int horizontal_size = horizontal.cols() / 30;
		int vertical_size = vertical.cols() / 30;
		Mat horizontalStructure = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(horizontal_size, 1));
		Mat verticalStructure = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(1, vertical_size));

		//Apply morphological operations
		Imgproc.erode(horizontal, horizontal, horizontalStructure);
		Imgproc.dilate(horizontal, horizontal, horizontalStructure);
		Imgproc.erode(vertical, vertical, verticalStructure);
		Imgproc.dilate(vertical, vertical, verticalStructure);

		Mat cleaned = new Mat();
		Core.add(horizontal, vertical, cleaned);
		Mat kernel = Mat.ones(new Size(10, 10), CvType.CV_8U);
		Imgproc.morphologyEx(cleaned, cleaned, Imgproc.MORPH_CLOSE, kernel);

		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Mat hierarchy = new Mat();
		Imgproc.findContours(srcGray, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

		Mat colored = new Mat(cleaned.size(), CvType.CV_8UC3, new Scalar(0, 0, 0));
		Imgproc.cvtColor(srcGray, colored, Imgproc.COLOR_GRAY2BGR);

		Log.e("FieldDetector", "Field List: "+FieldManager.getInstance().getFieldList().size());
		for(int i = 15; i < FieldManager.getInstance().getFieldList().size(); i++) {
			double x = FieldManager.getInstance().getField(i).getX() * this.srcOrig.width();
			double y = FieldManager.getInstance().getField(i).getY() * this.srcOrig.height();
			double width = FieldManager.getInstance().getField(i).getWidth() * this.srcOrig.width();
			double height = FieldManager.getInstance().getField(i).getHeight() * this.srcOrig.height();

			Rect roi = new Rect(new Point(x, y), new Point(x + width, y + height));
			Mat roi_submat = srcGray.submat(roi);

			int nonzero = Core.countNonZero(roi_submat);
			Log.e("FieldDetector", "Nonzero: " + nonzero);
			FieldManager.getInstance().getField(i).setNonzeroPixels(nonzero);
		}

		this.output_mat = srcGray;
	}

	public double getScore() {
		return this.finalScore;
	}

//	public void readFile(String filename, String destname) {
//
//		try {
//			File fileSrc = new File(filename);
//			BufferedImage bufferedSrc = ImageIO.read(fileSrc);
//
//			this.srcOrig = new Mat(bufferedSrc.getHeight(), bufferedSrc.getWidth(), CvType.CV_8UC3);
//			this.srcOrig = this.bufferedImageToMat(bufferedSrc);
//
//			this.extract(destname);
//		}
//		catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
	
//	private Mat bufferedImageToMat(BufferedImage bi) {
//		Mat mat = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC3);
//
//		byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
//		mat.put(0, 0, data);
//
//		return mat;
//	}
//
//	private void extract(String destname) {
//		//---Do this once the app starts
//		this.template = Imgcodecs.imread("C:/Users/gyra/Desktop/checkmarks/Check1.jpg");
//		Imgproc.cvtColor(template, template, Imgproc.COLOR_BGR2GRAY);
//		//--------------------------------
//
//		this.srcGray = new Mat();
//
//		Imgproc.cvtColor(srcOrig, srcGray, Imgproc.COLOR_BGR2GRAY);
//		Photo.fastNlMeansDenoising(srcGray, srcGray);
//		Imgproc.adaptiveThreshold(srcGray, srcGray, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 21, 2);
//
//		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
//		Mat hierarchy = new Mat();
//		Imgproc.findContours(srcGray, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
//
//		this.detectChecks();
//		System.out.println("Done.");
//	}
	
//	private void detectChecks() {
//		for(int i = 0; i < 15; i++) {
//
//			double x = FieldManager.getInstance().getField(i).getX()*this.srcOrig.width();
//			double y = FieldManager.getInstance().getField(i).getY()*this.srcOrig.height();
//			double width = FieldManager.getInstance().getField(i).getWidth()*this.srcOrig.width();
//			double height = FieldManager.getInstance().getField(i).getHeight()*this.srcOrig.height();
//
//			Rect roi = new Rect(new Point(x, y), new Point(x+width, y+height));
//			Mat roi_submat = srcGray.submat(roi);
//			int result_cols = roi_submat.cols() - this.template.cols() + 1;
//			int result_rows = roi_submat.rows() - this.template.rows() + 1;
//			Mat result = new Mat(result_rows, result_cols, CvType.CV_8U);
//
//			Imgproc.matchTemplate(roi_submat, this.template, result, Imgproc.TM_SQDIFF);
//			Core.normalize(result, result, 0, 1, Core.NORM_MINMAX, -1, new Mat());
//			MinMaxLocResult mmr = Core.minMaxLoc(result);
//			Point matchLoc = mmr.minLoc;
//			Rect new_roi = new Rect(new Point(matchLoc.x, matchLoc.y), new Point(matchLoc.x+template.cols(), matchLoc.y+template.rows()));
//			roi_submat = roi_submat.submat(new_roi);
//
//			List<MatOfPoint> template_contours = new ArrayList<MatOfPoint>();
//			Mat template_hierarchy = new Mat();
//			Imgproc.findContours(this.template, template_contours, template_hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
//
//			List<MatOfPoint> roi_contours = new ArrayList<MatOfPoint>();
//			Mat roi_hierarchy = new Mat();
//			Imgproc.findContours(roi_submat, roi_contours, roi_hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
//
//			double similarity = Imgproc.matchShapes(template, roi_submat, Imgproc.CV_CONTOURS_MATCH_I1, 0) * 100;
//
//			Imgproc.cvtColor(roi_submat, roi_submat, Imgproc.COLOR_GRAY2BGR);
//			Imgproc.drawContours(roi_submat, roi_contours, -1, new Scalar(0, 0, 255));
//			Imgcodecs.imwrite("Test"+(i+1)+".jpg", roi_submat);
//
//		}
//	}
}
