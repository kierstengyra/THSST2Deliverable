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

	private ArrayList<Mat> checkfields;
	private ArrayList<Integer> checkfieldIndices;

	public FieldDetector(int pageNo) {
		this.setPageNo(pageNo);
	}

	public void analyze(Mat source) {
		this.checkfields = new ArrayList<Mat>();
		this.checkfieldIndices = new ArrayList<Integer>();

		this.srcOrig = source;
		this.srcGray = this.srcOrig.clone();
		Imgproc.cvtColor(this.srcGray, srcGray, Imgproc.COLOR_BGR2GRAY);
		this.detectChecks();

		this.output_bmp = Bitmap.createBitmap(this.output_mat.cols(), this.output_mat.rows(), Bitmap.Config.ARGB_8888);
		Utils.matToBitmap(this.output_mat, this.output_bmp);
//		return this.output_bmp;
	}

	private void detectChecks() {
		Mat bw = this.srcGray.clone();
		Core.bitwise_not(this.srcGray, this.srcGray);
		Imgproc.adaptiveThreshold(this.srcGray, bw, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 15, -2);
		Log.e("CameraOverlay", "Adaptive Threshold.");

		Mat horizontal = bw.clone();
		Mat vertical = bw.clone();
		Log.e("CameraOverlay", "Containers.");

		int horizontal_size = horizontal.rows() / 150; // change to higher values
		int horizontal_size_2 = horizontal.rows() / 150;

		int vertical_size = vertical.cols() / 180;
		int vertical_size_2 = vertical.cols() / 180;

		Mat horizontalStructure = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(horizontal_size, 1));
		Mat horizontalStructure2 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(horizontal_size_2, 1));

		Mat verticalStructure = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(1, vertical_size));
		Mat verticalStructure2 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(1, vertical_size_2));
		Log.e("CameraOverlay", "Morphological Transform.");

		Imgproc.erode(horizontal, horizontal, horizontalStructure);
		Imgproc.dilate(horizontal, horizontal, horizontalStructure2);
		Log.e("CameraOverlay", "Erode/Dilate1.");

		Imgproc.erode(vertical, vertical, verticalStructure);
		Imgproc.dilate(vertical, vertical, verticalStructure2);
		Log.e("CameraOverlay", "Erode/Dilate2.");

		Mat cleaned = new Mat();
		Core.add(horizontal, vertical, cleaned);

		Mat kernel = Mat.ones(new Size(20, 20), CvType.CV_8U);
		Imgproc.morphologyEx(cleaned, cleaned, Imgproc.MORPH_CLOSE, kernel);
		Log.e("CameraOverlay", "Cleaned.");

		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Mat hierarchy = new Mat();
		Imgproc.findContours(cleaned, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
		Log.e("CameraOverlay", "Contours.");

		ArrayList<MatOfPoint> filtered = new ArrayList<MatOfPoint>();
		for(int i = 0; i < contours.size(); i++) {
			double area = Imgproc.contourArea(contours.get(i));

			if(area > 200)
				filtered.add(contours.get(i));
		}

		ArrayList<MatOfPoint> filtered2 = new ArrayList<MatOfPoint>();
		for(int i = 0; i < filtered.size(); i++) {
			Rect roi = Imgproc.boundingRect(filtered.get(i));

			if(roi.width > roi.height) {
				filtered2.add(filtered.get(i));
			}
		}

		FieldManager fm = PaperFormManager.getInstance().getPage(this.page);
		int size = fm.getFieldList().size();
		Log.e("CameraOverlay", "Field List.");

		for(int i = 0; i < PaperFormManager.getInstance().getQuestionList().size(); i++) {
			Log.e("CameraOverlay", "Question_"+i);
			int q_page = PaperFormManager.getInstance().getQuestion(i).getPage();

			if(q_page == this.page) {
				Question q = PaperFormManager.getInstance().getQuestion(i);
				double x = q.getX()*cleaned.width();
				double y = q.getY()*cleaned.height();
				double width = q.getWidth()*cleaned.width();
				double height = q.getHeight()*cleaned.height();

				Rect roi = new Rect(new Point(x, y), new Point(x+width, y+height));

				int index = 0;
				double min_dist = Double.MAX_VALUE;

				for(int j = 0; j < filtered2.size(); j++) {
					Log.e("CameraOverlay", "Filtered2_"+i);
					double distance_tl = computeDistance(roi.tl(), Imgproc.boundingRect(filtered2.get(j)).tl());

					if(distance_tl < min_dist) {
						min_dist = distance_tl;
						index = j;
					}
				}

				Rect raw = Imgproc.boundingRect(filtered2.get(index));
				Rect selected = new Rect(raw.tl(), roi.br());

				Mat block = cleaned.submat(selected);
				Mat block2 = bw.submat(selected);

				int top_bottom = (int) (0.05*block.rows());

				Mat dst = new Mat();
				Mat dst2 = new Mat();

				Core.copyMakeBorder(block, dst, top_bottom, top_bottom, 0, 0, Core.BORDER_CONSTANT, new Scalar(255, 255, 255));
				Core.copyMakeBorder(block2, dst2, top_bottom, top_bottom, 0, 0, Core.BORDER_CONSTANT);

				Mat block_colored = new Mat(dst.size(), CvType.CV_8UC3, new Scalar(0, 0, 0));
				Imgproc.cvtColor(dst, block_colored, Imgproc.COLOR_GRAY2BGR);

				List<MatOfPoint> selected_contours = new ArrayList<MatOfPoint>();
				Mat selected_hierarchy = new Mat();
				Imgproc.findContours(dst, selected_contours, selected_hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

				int selectedCtr = 0;

				for(int k = 0; k < PaperFormManager.getInstance().getPage(this.page).getFieldList().size(); k++) {
					Log.e("CameraOverlay", "Field_"+k+"_"+i);
					int f_question = PaperFormManager.getInstance().getPage(this.page).getField(k).getQuestion();

					if(f_question == q.getNumber()) {
						Field f = PaperFormManager.getInstance().getPage(this.page).getField(k);
						double f_x = f.getX()*dst.width();
						double f_y = f.getY()*dst.height()-top_bottom;
						double f_width = f.getWidth()*dst.width();
						double f_height = f.getHeight()*dst.height();

						Rect f_roi = new Rect(new Point(f_x, f_y), new Point(f_x+f_width, f_y+f_height));
						Imgproc.rectangle(block_colored, f_roi.tl(), f_roi.br(), new Scalar(255, 0, 255), 5); //Purple for reference

						int sel_index = 0;
						double sel_min_dist = Double.MAX_VALUE;

						for(int j = 0; j < selected_contours.size(); j++) {
							double distance_tl = computeDistance(f_roi.tl(), Imgproc.boundingRect(selected_contours.get(j)).tl());
							if(distance_tl < sel_min_dist) {
								sel_min_dist = distance_tl;
								sel_index = j;
							}
						}

						Rect fieldRect = Imgproc.boundingRect(selected_contours.get(sel_index));
						Mat field = dst2.submat(fieldRect);

						double x_midpoint = f_width/2;
						double y_midpoint = f_height/2;
						Rect roi_crop = new Rect(new Point(Math.abs(x_midpoint-32), Math.abs(y_midpoint-20)), new Point(x_midpoint+32, y_midpoint+20));
						Mat cropped = field.clone();

						if(roi_crop.x >= 0 && roi_crop.y >= 0 &&
								roi_crop.width + roi_crop.x < field.cols() &&
								roi_crop.height+roi_crop.y < field.rows())
							cropped = new Mat(field, roi_crop);

						if(Core.countNonZero(cropped) != 0) {
							selectedCtr++;
							checkfields.add(cropped);
							checkfieldIndices.add(k);
						}
					}
				}

				if(selectedCtr > 1) {
					for(int iterator = 0; iterator < checkfields.size(); iterator++) {
						Mat cropped = checkfields.get(iterator);

						Mat structure = Imgproc.getStructuringElement(Imgproc.MORPH_CROSS, new Size(3, 3));
						Mat skeleton = new Mat(cropped.size(), CvType.CV_8UC1, new Scalar(0, 0, 0));
						boolean isDone = false;

						while(!isDone) {
							Mat eroded = new Mat();
							Mat temp = new Mat();

							Imgproc.erode(cropped, eroded, structure);
							Imgproc.dilate(eroded, temp, structure);
							Core.subtract(cropped, temp, temp);
							Core.bitwise_or(temp, skeleton, skeleton);
							cropped = eroded.clone();

							isDone = (Core.countNonZero(cropped) == 0);
							Log.e("CameraOverlay", "Skeleton_"+i+"_"+Core.countNonZero(cropped));
						}

						int count = 0;
						double pixel[];
						ArrayList<Integer> coords = new ArrayList<Integer>();
						for(int xSkel = 1; xSkel < skeleton.rows()-1; xSkel++) {
							for(int ySkel = 1; ySkel < skeleton.cols()-1; ySkel++) {
								pixel = skeleton.get(xSkel, ySkel);

								if(pixel[0] == 0)
									continue;

								count = 0;

								for(int a = -1; a <= 1; a++) {
									for(int b = -1; b <= 1; b++) {
										pixel = skeleton.get(xSkel+a, ySkel+b);

										if(pixel[0] != 0)
											count++;
									}
								}

								if(count > 1) {
									coords.add(xSkel);
									coords.add(ySkel);
								}
							}
						}

						int xPrev = 0;
						int yPrev = 0;
						int prevDiff = Integer.MAX_VALUE;

						boolean isBreak = false;

						for(int m = (coords.size()/2)-1; m >= 0; m--) {
							int xCurr = coords.get(2*m+1);
							int yCurr = coords.get(2*m);

							if(m != (coords.size()/2)-1) {
								if(yPrev == yCurr) {
									int diff = Math.abs(xPrev-xCurr);

									if(diff > 10) {
										if(prevDiff > diff && prevDiff-diff > 1) {
											isBreak = true;
										}
									}

									prevDiff = diff;
								}
							}

							xPrev = xCurr;
							yPrev = yCurr;
						}

						if(isBreak)
							Log.e("CameraOverlay", "Cross");
						else {
							PaperFormManager.getInstance().getPage(this.page).getField(iterator).setSelected(true);
							Log.e("CameraOverlay", "Check");
						}
					}
				}
				else if(selectedCtr == 1)
					PaperFormManager.getInstance().getPage(this.page).getField(checkfieldIndices.get(0)).setSelected(true);
			}
		}
		this.output_mat = cleaned;
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
