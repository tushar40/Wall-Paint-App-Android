package com.example.paintapp

import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc

class WatershedSegmenter
{
    var markerss = Mat()

    public fun setMarkers(markerImage: Mat)
    {
        markerImage.convertTo(markerss, CvType.CV_32SC1);
    }

    public fun process(image: Mat): Mat
    {
        var newImg = Mat()
        Imgproc.cvtColor(image,newImg,Imgproc.COLOR_RGBA2RGB)

        Imgproc.watershed(newImg,markerss);
        markerss.convertTo(markerss, CvType.CV_8U);
        return markerss;
    }
}