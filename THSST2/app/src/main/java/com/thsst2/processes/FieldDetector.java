package com.thsst2.processes;

import android.graphics.Bitmap;

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

import java.util.ArrayList;
import java.util.List;

public class FieldDetector {

	private Mat srcOrig;
	private Mat srcGray;
	private Mat output_mat;
	private Bitmap output_bmp;
	private int page;

	public FieldDetector(int pageNo) {
		this.setPageNo(pageNo);
	}

	public Bitmap analyze(Mat source) {
		this.srcOrig = source;
		this.srcGray = this.srcOrig.clone();
		this.extract();

		this.output_bmp = Bitmap.createBitmap(this.output_mat.cols(), this.output_mat.rows(), Bitmap.Config.ARGB_8888);
		Utils.matToBitmap(this.output_mat, this.output_bmp);
		return this.output_bmp;
	}

	private void extract() {
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Mat hierarchy = new Mat();
		Imgproc.findContours(srcGray, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

		this.detectChecks();
	}

	private void detectChecks() {
		//Apply adaptive threshold at the bitwise_not of gray
		Mat bw = this.srcGray.clone();

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
		Imgproc.drawContours(colored, contours, -1, new Scalar(212, 173, 70));

		FieldManager fm = PaperFormManager.getInstance().getPage(this.page);
		int size = fm.getFieldList().size();

		for(int i = 0; i < size; i++) {
			double x = fm.getField(i).getX()*this.srcOrig.width();
			double y = fm.getField(i).getY()*this.srcOrig.height();
			double width = fm.getField(i).getWidth()*this.srcOrig.width();
			double height = fm.getField(i).getHeight()*this.srcOrig.height();

			Rect roi = new Rect(new Point(x, y), new Point(x+width, y+height));
			Mat roi_submat = srcGray.submat(roi);

			int nonzero = Core.countNonZero(roi_submat);
			PaperFormManager.getInstance().getPage(this.page).getField(i).setNonzero_pixels(nonzero);
		}

		this.output_mat = srcGray;
	}

	public void setPageNo(int pageNo) {
		this.page = pageNo;
	}
}
