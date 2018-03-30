package com.trial;

import java.util.List;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.photo.Photo;

public class Transform {
	
	private Mat srcOrig;
	
	private Point ref_tl;
	private Point ref_tr;
	private Point ref_br;
	private Point ref_bl;
	
	public void fourPointTransform(String filename, String destname) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		try {
			File fileSrc = new File(filename);
			BufferedImage bufferedSrc = ImageIO.read(fileSrc);
		
			this.srcOrig = new Mat(bufferedSrc.getHeight(), bufferedSrc.getWidth(), CvType.CV_8UC4);
			this.srcOrig = this.bufferedImageToMat(bufferedSrc);
			
			this.ref_tl = new Point(0, 0);
			this.ref_tr = new Point(this.srcOrig.cols(), 0);
			this.ref_br = new Point(this.srcOrig.cols(), this.srcOrig.rows());
			this.ref_bl = new Point(0, this.srcOrig.rows());
			
			this.transform(destname);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void transform(String destname) {
		Mat scratch = this.srcOrig.clone();
		Mat mask = new Mat();
		
		Core.inRange(srcOrig, new Scalar(0,0,0), new Scalar(100, 100, 100), mask);
	    Photo.fastNlMeansDenoising(mask, mask);
	    
        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(mask, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
        
        List<Rect> detected = new ArrayList<Rect>();
        detected = this.detectRectangles(contours, scratch);
        List<Point> rectangle = this.getFourCornerPts(detected, scratch);
        
        Mat destImage = this.getFourpointTransform(rectangle, scratch);
	    Imgcodecs.imwrite(destname, destImage);
  
        System.out.println("Done");
	}
	
	private List<Rect> detectRectangles(ArrayList<MatOfPoint> c, Mat scratch) {
		List<Rect> rectGroup = new ArrayList<Rect>();
		MatOfPoint approx = new MatOfPoint();
		MatOfPoint2f approx2f = new MatOfPoint2f();
		
		for(MatOfPoint contour : c) {
			MatOfPoint2f contour2f = new MatOfPoint2f();
			contour.convertTo(contour2f, CvType.CV_32FC2);
			
			double perimeter = Imgproc.arcLength(contour2f, true);
			Imgproc.approxPolyDP(contour2f, approx2f, perimeter*0.04, true);
			approx2f.convertTo(approx2f, CvType.CV_32S);
			
			if(approx.size().height >= 4 || approx.size().height <= 7) {
				Rect rect = Imgproc.boundingRect(contour);
				rectGroup.add(rect);
			}
		}
		return rectGroup;
	}
	
	private List<Point> getFourCornerPts(List<Rect> rectGroup, Mat scratch) {
		List<Point> corners = new ArrayList<Point>();
		
		double curr_distance_tl = Integer.MAX_VALUE;
		double curr_distance_tr = Integer.MAX_VALUE;
		double curr_distance_br = Integer.MAX_VALUE;
		double curr_distance_bl = Integer.MAX_VALUE;
		
		int curr_index_tl = 0;
		int curr_index_tr = 0;
		int curr_index_br = 0;
		int curr_index_bl = 0;
		
		int new_index = 0;
		
		for(Rect r : rectGroup) {
			double new_distance_tl = this.computeDistance(this.ref_tl, r.tl());
			double new_distance_tr = this.computeDistance(this.ref_tr, new Point(r.x+r.width, r.y));
			double new_distance_br = this.computeDistance(this.ref_br, r.br());
			double new_distance_bl = this.computeDistance(this.ref_bl, new Point(r.x, r.y+r.height));
			
			if(new_distance_tl < curr_distance_tl) {
				curr_distance_tl = new_distance_tl;
				curr_index_tl = new_index;
			}
			
			if(new_distance_tr < curr_distance_tr) {
				curr_distance_tr = new_distance_tr;
				curr_index_tr = new_index;
			}
			
			if(new_distance_br < curr_distance_br) {
				curr_distance_br = new_distance_br;
				curr_index_br = new_index;
			}
			
			if(new_distance_bl < curr_distance_bl) {
				curr_distance_bl = new_distance_bl;
				curr_index_bl = new_index;
			}
			
			new_index++;
		}
		
		Point top_left = new Point(rectGroup.get(curr_index_tl).x+rectGroup.get(curr_index_tl).width, rectGroup.get(curr_index_tl).y+rectGroup.get(curr_index_tl).height);
		Point top_right = new Point(rectGroup.get(curr_index_tr).x, rectGroup.get(curr_index_tr).height-rectGroup.get(curr_index_tr).y);
		Point bottom_right = new Point(rectGroup.get(curr_index_br).x, rectGroup.get(curr_index_br).y);
		Point bottom_left = new Point(rectGroup.get(curr_index_bl).x+rectGroup.get(curr_index_bl).width, rectGroup.get(curr_index_bl).y);
		
		corners.add(top_left);
		corners.add(top_right);
		corners.add(bottom_right);
		corners.add(bottom_left);
		
		return corners;
	}
	
	private Mat getFourpointTransform(List<Point> points, Mat srcImage) {
		Mat src = new MatOfPoint2f(points.get(0), points.get(1), points.get(2), points.get(3));
		
		double aWidth = computeDistance(points.get(0), points.get(1));
		double bWidth = computeDistance(points.get(2), points.get(3));
		double maxWidth = (aWidth > bWidth) ? aWidth : bWidth;
		
		double aHeight = computeDistance(points.get(0), points.get(3));
		double bHeight = computeDistance(points.get(1), points.get(2));
		double maxHeight = (aHeight > bHeight) ? aHeight : bHeight;
		
		Mat dst = new MatOfPoint2f(new Point(0, 0), new Point(maxWidth-1, 0), new Point(maxWidth-1, maxHeight-1), new Point(0, maxHeight-1));
		Mat destImage = new Mat((int)maxHeight, (int)maxWidth, srcImage.type());
		Mat transform = Imgproc.getPerspectiveTransform(src, dst);
		Imgproc.warpPerspective(srcImage, destImage, transform, destImage.size());
		
		return destImage;
	}
	
	private double computeDistance(Point p1, Point p2) {
		double x = Math.pow(p1.x-p2.x, 2);
		double y = Math.pow(p1.y-p2.y, 2);
		
		return Math.sqrt(x+y);
	}
	
	private Mat bufferedImageToMat(BufferedImage bi) {
		Mat mat = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC3);
		
		byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
		mat.put(0, 0, data);
		
		return mat;
	}
	
}
