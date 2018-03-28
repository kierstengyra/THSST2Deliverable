package com.thsst2.processes;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import javax.imageio.ImageIO;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

public class BoxPositions {
	
	private Mat srcOrig;
	private ArrayList<Point> points;
	private ArrayList<Double> widths;
	private ArrayList<Double> heights;
	
	public BoxPositions() {
		this.points = new ArrayList<Point>();
		this.widths = new ArrayList<Double>();
		this.heights = new ArrayList<Double>();
	}
	
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
		
	    Imgproc.cvtColor(srcOrig, srcGray, Imgproc.COLOR_BGR2GRAY);
	    Imgproc.Canny(srcGray, srcGray, 50, 150);
	    
        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(srcGray, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
   
        Collections.reverse(contours);
        this.detectSquares(contours);
        
        this.writeToFile();
        System.out.println("Success");
	}
	
	private void detectSquares(ArrayList<MatOfPoint> c) {
		MatOfPoint2f approx = new MatOfPoint2f();
		MatOfPoint2f approx2f = new MatOfPoint2f();
		
		for(MatOfPoint contour : c) {
			MatOfPoint2f contour2f = new MatOfPoint2f();
			contour.convertTo(contour2f, CvType.CV_32FC2);
			
			double perimeter = Imgproc.arcLength(contour2f, false);
			Imgproc.approxPolyDP(contour2f, approx2f, perimeter*0.04, false);
			approx2f.convertTo(approx, CvType.CV_32S);
			
			if(approx.size().height == 4 || approx.size().height == 5) {
				Rect rect = Imgproc.boundingRect(contour);
				if(rect.width < 130 && rect.width >= 120 && rect.height < 100) {
					Point point = new Point((double)rect.x/this.srcOrig.width(), (double)rect.y/this.srcOrig.height());
					
					if(this.points.size() == 0 || !point.equals(this.points.get(points.size()-1))) {
						this.points.add(point);
						this.widths.add((double)rect.width/this.srcOrig.width());
						this.heights.add((double)rect.height/this.srcOrig.height());
					}
				}
			}
		}
	}
	
	private void writeToFile() {
		BufferedWriter bw = null;
		FileWriter fw = null;
		
		try {
			fw = new FileWriter("C:/Users/gyra/Desktop/Page4.csv");
			bw = new BufferedWriter(fw);
			int j = 0;
			
			for(int i = 0; i < this.points.size(); i++) {
				bw.write("Blob,");
				bw.write(this.points.get(i).x+",");
				bw.write(this.points.get(i).y+",");
				bw.write(this.widths.get(i)+",");
				bw.write(this.heights.get(i)+",");
				bw.write(j+1+"\n");

				if( (i+1) % 3 == 0)
					j++;
			}
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		finally {
			try {
				if(bw != null)
					bw.close();
				
				if(fw != null)
					fw.close();
			}
			catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	
}
