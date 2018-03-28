package com.thsst2.processes;

import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gyra on 03/25/2018.
 */
public class FormDetector {

    private Mat srcOrig;
    private Mat srcGray;
    private Mat output_mat;
    private Bitmap output_bmp;

    public Bitmap extract(Bitmap source) {
        this.convertToMat(source);
        this.preprocess();
        this.drawRectangle();

        this.output_bmp = Bitmap.createBitmap(this.output_mat.cols(), this.output_mat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(this.output_mat, this.output_bmp);
        return this.output_bmp;
    }

    private void convertToMat(Bitmap source) {
        Bitmap copy = source.copy(Bitmap.Config.ARGB_8888, true);
        this.srcOrig = new Mat(source.getHeight(), source.getWidth(), CvType.CV_8UC1);
        Utils.bitmapToMat(copy, this.srcOrig);

        this.srcGray = this.srcOrig.clone();
        this.output_mat = this.srcOrig.clone();
    }

    private void preprocess() {
        Imgproc.cvtColor(this.srcOrig, this.srcGray, Imgproc.COLOR_BGR2GRAY); //Convert to grayscale
        Imgproc.adaptiveThreshold(this.srcGray, this.srcGray, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 21, 2); //Apply binary threshold
    }

    private void drawRectangle() {
        //Find all contours
        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(this.srcGray, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
        Log.e("FormDetector", "Contours "+contours.size());

        //Get candidate contours
        List<Rect> detected = new ArrayList<Rect>();
        detected = this.detectRectangles(contours);
        Log.e("FormDetecor", "Detected " + detected.size());

        //Identify four corner points
        List<Point> rectangle = this.getFourCornerPts(detected);
        Log.e("FormDetector", "Rectangle "+rectangle.size());

        //Apply four-point transform
        this.output_mat = this.getFourPointTransform(rectangle, this.output_mat);
        this.output_mat.convertTo(this.output_mat, -1, 1.5, 1);
        Log.e("FormDetector", "Transformed");
    }

    private List<Rect> detectRectangles(List<MatOfPoint> c) {
        List<Rect> rectGroup = new ArrayList<Rect>();
        MatOfPoint approx = new MatOfPoint();
        MatOfPoint2f approx2f = new MatOfPoint2f();

        for(MatOfPoint contour : c) {
            MatOfPoint2f contour2f = new MatOfPoint2f();
            contour.convertTo(contour2f, CvType.CV_32FC2);

            double perimeter = Imgproc.arcLength(contour2f, true);
            Imgproc.approxPolyDP(contour2f, approx2f, perimeter*0.04, true);
            approx2f.convertTo(approx2f, CvType.CV_32S);

            if(approx.size().height >= 4 || approx.size().height < 6) {
                Rect rect = Imgproc.boundingRect(contour);

                if(rect.width < rect.height) {
                    float ratio_width = (float)rect.width/this.srcOrig.width();
                    if(ratio_width <= 0.099)
                        rectGroup.add(rect);
                }
            }
        }

        return rectGroup;
    }

    private List<Point> getFourCornerPts(List<Rect> rectGroup) {
        List<Point> corners = new ArrayList<Point>();

        int tl = Integer.MAX_VALUE; int br = Integer.MIN_VALUE;
        int tr = Integer.MIN_VALUE; int bl = Integer.MAX_VALUE;

        int tl_index = 0;   int br_index = 0;
        int tr_index = 0;   int bl_index = 0;

        int index = 0;

        for(Rect r : rectGroup) {
            int sum = r.x+r.y;
            int diff = r.x-r.y;

            if(tl > sum) {
                tl = sum;
                tl_index = index;
            }

            if(br < sum) {
                br = sum;
                br_index = index;
            }

            if(tr < diff) {
                tr = diff;
                tr_index = index;
            }

            if(bl > diff) {
                bl = diff;
                bl_index = index;
            }

            index++;
        }

        Point topLeft = new Point(rectGroup.get(tl_index).x+rectGroup.get(tl_index).width, rectGroup.get(tl_index).y+rectGroup.get(tl_index).height);
        Point topRight = new Point(rectGroup.get(tr_index).x, rectGroup.get(tr_index).y+rectGroup.get(tr_index).height);
        Point bottomRight = new Point(rectGroup.get(br_index).x+rectGroup.get(br_index).width, rectGroup.get(br_index).y);
        Point bottomLeft = new Point(rectGroup.get(bl_index).x, rectGroup.get(bl_index).y);

        corners.add(topLeft);
        corners.add(topRight);
        corners.add(bottomRight);
        corners.add(bottomLeft);

        return corners;
    }

    private Mat getFourPointTransform(List<Point> points, Mat srcImage) {
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

}
