package com.thsst2.processes;

import android.graphics.Bitmap;
import android.util.Log;

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
//		//Apply adaptive threshold at the bitwise_not of gray
		Mat bw = this.srcGray.clone();
		Core.bitwise_not(this.srcGray, this.srcGray);
		Imgproc.adaptiveThreshold(this.srcGray, bw, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 15, -2);
		Log.e("CameraOverlay", "Adaptive Threshold.");

//		//Create containers for horizontal and vertical lines
		Mat horizontal = bw.clone();
		Mat vertical = bw.clone();
		Log.e("CameraOverlay", "Containers.");
//
//		//Horizontal+Vertical Lines
		int horizontal_size = horizontal.cols() / 80;
		int vertical_size = vertical.cols() / 80;
		Mat horizontalStructure = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(horizontal_size, 1));
		Mat verticalStructure = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(1, vertical_size));
		Log.e("CameraOverlay", "Morphological Transform.");
//
//		//Apply morphological operations
		Imgproc.erode(horizontal, horizontal, horizontalStructure);
		Imgproc.dilate(horizontal, horizontal, horizontalStructure);
		Log.e("CameraOverlay", "Erode/Dilate1.");
//
		Imgproc.erode(vertical, vertical, verticalStructure);
		Imgproc.dilate(vertical, vertical, verticalStructure);
		Log.e("CameraOverlay", "Erode/Dilate2.");
//
		Mat cleaned = new Mat();
		Core.add(horizontal, vertical, cleaned);
		Mat kernel = Mat.ones(new Size(10, 10), CvType.CV_8U);
		Imgproc.morphologyEx(cleaned, cleaned, Imgproc.MORPH_CLOSE, kernel);
		Log.e("CameraOverlay", "Cleaned.");

		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Mat hierarchy = new Mat();
		Imgproc.findContours(srcGray, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
		Log.e("CameraOverlay", "Contours.");

//		Mat colored = new Mat(srcGray.size(), CvType.CV_8UC3, new Scalar(0, 0, 0));
//		Imgproc.cvtColor(srcGray, colored, Imgproc.COLOR_GRAY2BGR);
//		Imgproc.drawContours(colored, contours, -1, new Scalar(212, 173, 70));

		FieldManager fm = PaperFormManager.getInstance().getPage(this.page);
		int size = fm.getFieldList().size();
		Log.e("CameraOverlay", "Field List.");

		for(int i = 0; i < size; i++) {
			Log.e("CameraOverlay", "Submat "+i);
			double x = fm.getField(i).getX()*this.srcOrig.width();
			double y = fm.getField(i).getY()*this.srcOrig.height();
			double width = fm.getField(i).getWidth()*this.srcOrig.width();
			double height = fm.getField(i).getHeight()*this.srcOrig.height();

			Rect roi = new Rect(new Point(x, y), new Point(x+width, y+height));
			int index = 0;
			double min_dist = Double.MAX_VALUE;

			for(int j = 0; j < contours.size(); j++) {
				double distance = computeDistance(roi.tl(), Imgproc.boundingRect(contours.get(j)).tl());

				if(distance < min_dist) {
					min_dist = distance;
					index = j;
				}
			}
			Log.e("CameraOverlay", "Closest contour "+i);

			double x_midpoint = width/2;
			double y_midpoint = height/2;

			Rect selected = Imgproc.boundingRect(contours.get(index));
			Rect roi_crop = new Rect(new Point(Math.abs(x_midpoint-34), Math.abs(y_midpoint-23)), new Point(x_midpoint+34, y_midpoint+23));
			Mat roi_submat = bw.submat(selected);
			Mat cropped = roi_submat.clone();

			if(roi_crop.x >= 0 && roi_crop.y >= 0 &&
					roi_crop.width + roi_crop.x < roi_submat.cols() &&
					roi_crop.height+roi_crop.y < roi_submat.rows())
				cropped = new Mat(roi_submat, roi_crop);
			Log.e("CameraOverlay", "Cropped "+i);

			Mat structure = Imgproc.getStructuringElement(Imgproc.MORPH_CROSS, new Size(3, 3));
			Mat skeleton = new Mat(cropped.size(), CvType.CV_8UC1, new Scalar(0, 0, 0));
			boolean isDone = false;

			while(!isDone) {
				Log.e("CameraOverlay", "Skeleton "+i);
				Mat eroded = new Mat();
				Mat temp = new Mat();

				Imgproc.erode(cropped, eroded, structure);
				Imgproc.dilate(eroded, temp, structure);
				Core.subtract(cropped, temp, temp);
				Core.bitwise_or(temp, skeleton, skeleton);
				cropped = eroded.clone();

				isDone = (Core.countNonZero(cropped) == 0);
			}
			Log.e("CameraOverlay", "Skeleton "+i);

//			Mat skeleton_color = new Mat(skeleton.size(), CvType.CV_8UC3, new Scalar(0, 0, 0));
//			Imgproc.cvtColor(skeleton, skeleton_color, Imgproc.COLOR_GRAY2BGR);
//			Log.e("CameraOverlay", "Submat " + i);

			int count = 0;
			double pixel[];
			ArrayList<Integer> coords = new ArrayList<Integer>();

			for(int i_ = 1; i_ < skeleton.rows()-1; i_++) {
				for(int j_ = 1; j_ < skeleton.cols()-1; j_++) {
					pixel = skeleton.get(i_, j_);

					if(pixel[0] == 0)
						continue;

					count = 0;

					for(int y_ = -1; y_ <= 1; y_++) {
						for(int x_ = -1; x_ <= 1; x_++) {
							pixel = skeleton.get(i_+y_, j_+x_);

							if(pixel[0] != 0)
								count++;
						}
					}

					if(count < 4) {
						coords.add(i_);
						coords.add(j_);
					}
				}
			}
			Log.e("CameraOverlay", "Skeleton "+i);

			//TL
			int tl_x = Integer.MAX_VALUE;
			int tl_y = Integer.MAX_VALUE;
			int tl_x_index = 0;
			int tl_y_index = 0;

			//TR
			int tr_x = Integer.MIN_VALUE;
			int tr_x_index = 0;
			int tr_y_index = 0;

			//BR
			int br_y = Integer.MIN_VALUE;
			int br_x_index = 0;
			int br_y_index = 0;

			//BL
			int bl_x = Integer.MAX_VALUE;
			int bl_y = Integer.MIN_VALUE;
			int bl_x_index = 0;
			int bl_y_index = 0;

			for(int m = 0; m < coords.size()/2; m++) {
				if(tl_x > coords.get(2*m+1) && tl_y > coords.get(2*m)) {
					tl_x = coords.get(2*m+1);
					tl_y = coords.get(2*m);
					tl_x_index = 2*m+1;
					tl_y_index = 2*m;
				}
				else if(tr_x < coords.get(2*m+1)) {
					tr_x = coords.get(2*m+1);
					tr_x_index = 2*m+1;
					tr_y_index = 2*m;
				}
				else if(br_y < coords.get(2*m)) {
					br_y = coords.get(2*m);
					br_x_index = 2*m+1;
					br_y_index = 2*m;
				}
				else if(bl_x > coords.get(2*m+1) && bl_y < coords.get(2*m)) {
					bl_x = coords.get(2*m+1);
					bl_y = coords.get(2*m);
					bl_x_index = 2*m+1;
					bl_y_index = 2*m;
				}
			}
			Log.e("CameraOverlay", "4 points "+i);

			if(coords.size() > 0) {
				double dist_tl_tr = computeDistance(new Point(coords.get(tl_x_index), coords.get(tl_y_index)), new Point(coords.get(tr_x_index), coords.get(tr_y_index)));
				double dist_tr_br = computeDistance(new Point(coords.get(tr_x_index), coords.get(tr_y_index)), new Point(coords.get(br_x_index), coords.get(br_y_index)));
				double dist_br_bl = computeDistance(new Point(coords.get(br_x_index), coords.get(br_y_index)), new Point(coords.get(bl_x_index), coords.get(bl_y_index)));
				double dist_bl_tl = computeDistance(new Point(coords.get(bl_x_index), coords.get(bl_y_index)), new Point(coords.get(tl_x_index), coords.get(tl_y_index)));

				double sum = dist_tl_tr + dist_tr_br + dist_br_bl + dist_bl_tl;
				double s = sum / 2.0;

				double s_tl_tr = s - dist_tl_tr;
				double s_tr_br = s - dist_tr_br;
				double s_br_bl = s - dist_br_bl;
				double s_bl_tl = s - dist_bl_tl;

				double area = Math.sqrt(s_tl_tr*s_tr_br*s_br_bl*s_bl_tl);

				if(area >= 20 && area < 500) {
					PaperFormManager.getInstance().getAllPages().get(page).getField(i).setSelected(true);
				}
			}
			Log.e("CameraOverlay", "Added "+i);
//
//			Mat roi_submat = srcGray.submat(roi);
//
//			Mat submat_clone = roi_submat.clone();
//			Mat submat_kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
//			Imgproc.erode(submat_clone, submat_clone, submat_kernel);
//
//			List<MatOfPoint> sub_contours = new ArrayList<MatOfPoint>();
//			Mat sub_hierarchy = new Mat();
//			Imgproc.findContours(submat_clone, sub_contours, sub_hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
//
//			Mat submat_colored = new Mat(submat_clone.size(), CvType.CV_8UC3, new Scalar(0, 0, 0));
//			Imgproc.cvtColor(submat_clone, submat_colored, Imgproc.COLOR_GRAY2BGR);
//
//			int nonzero = Core.countNonZero(roi_submat);
//			PaperFormManager.getInstance().getPage(this.page).getField(i).setNonzero_pixels(nonzero);
		}

		this.output_mat = srcGray;
		Log.e("CameraOverlay", "Done processing.");
	}

	public void setPageNo(int pageNo) {
		this.page = pageNo;
	}

	private double computeDistance(Point p1, Point p2) {
		double x = Math.pow(p1.x-p2.x, 2);
		double y = Math.pow(p1.y-p2.y, 2);

		return Math.sqrt(x+y);
	}
}
