package com.example.paintapp

import android.Manifest
import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException

import org.opencv.imgproc.Imgproc
import org.opencv.android.Utils;
import org.opencv.core.*
import org.opencv.core.Scalar
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Rect
import android.util.DisplayMetrics
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import org.opencv.core.CvType
import yuku.ambilwarna.AmbilWarnaDialog
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

typealias Coordinates = Pair<Point, Point>

class MainActivity : AppCompatActivity() {

    companion object {
        init {
            System.loadLibrary("opencv_java")
        }
    }

    var touchCount = 0
    lateinit var tl: Point
    private lateinit var br: Point
    lateinit var bitmap: Bitmap
    var chosenColor = Color.RED
//        Scalar(200.0, 0.0, 0.0)

    private val TAG = MainActivity::class.java.simpleName


    private enum class LoadImage {
        PICK_FROM_CAMERA,
        PICK_FROM_GALLERY
    }
//    private val PICK_FROM_CAMERA = 1
//    private val PICK_FROM_GALLERY = 2

    private var texture = false

    private val PERMISSIONS = arrayOf<String>(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tl = Point()
        br = Point()

        openCamera()
//        openGallery()
    }

    private fun openGallery() {
        val i = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(i, LoadImage.PICK_FROM_GALLERY.ordinal)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.action_open_img -> {
                showImage()
            }
            R.id.action_process_image -> {
                showResultLayouts()
            }
            R.id.action_take_photo -> {
                openCamera()
            }
            R.id.action_get_gallery -> {
                openGallery()
            }
            R.id.action_get_color -> {
                chooseColor()
            }
            R.id.action_get_texture -> {
                chooseTexture()
            }
        }
        return  true
    }

    private fun chooseTexture() {
        texture = true
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            LoadImage.PICK_FROM_CAMERA.ordinal -> if (resultCode == Activity.RESULT_OK) {

                try {
//                    bitmap = data?.getExtras()?.get("data") as Bitmap
                    imageFromData.setImageURI(Uri.parse(imageFilePath))

                    bitmap = imageFromData.drawable.toBitmap()
                    bitmap = getResizedBitmap(bitmap,bitmap.width/5,bitmap.height/5)
                    showImage()
//                    imageFromData.setImageBitmap(bitmap)
//                    gaussianOutput(bitmap)
//                    processImage(bitmap)
//                    detectAndFillContours(bitmap)
//                    imageView.setImageBitmap(steptowatershed(bitmap))
//                    paintSelected(bitmap)
//                    extractForeGround(bitmap)
//                    makeContours(bitmap)
//                    Log.e(TAG, "Bitmap image: "+ bitmap)

                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            LoadImage.PICK_FROM_GALLERY.ordinal -> if (resultCode == Activity.RESULT_OK) {
                loadFromGallery(data)
            }
        }

        imageFromData.setOnTouchListener(object : View.OnTouchListener {

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                if (event.action == MotionEvent.ACTION_DOWN) {
                    if (touchCount == 0) {
                        tl.x = event.x.toDouble()
                        tl.y = event.y.toDouble()
//                        touchCount++

                        if(texture) {
                            applyTexture(bitmap, tl)
                        } else {
                            rpPaintHSV(bitmap,tl)
                        }
//                        rppaint(bitmap,tl)
//                        floodFill(bitmap,tl)

//                        br.x = event.x.toDouble()
//                        br.y = event.y.toDouble()

//                        val rectPaint = Paint()
//                        rectPaint.setARGB(255, 255, 0, 0)
//                        rectPaint.setStyle(Paint.Style.STROKE)
//                        rectPaint.setStrokeWidth(3.toFloat())
//                        val tmpBm = Bitmap.createBitmap(
//                            bitmap.width,
//                            bitmap.height, Bitmap.Config.RGB_565
//                        )
//                        val tmpCanvas = Canvas(tmpBm)
//
//                        tmpCanvas.drawBitmap(bitmap, 0.toFloat(), 0.toFloat(), null)
//                        tmpCanvas.drawRect(
//                            RectF(
//                                tl.x.toFloat(),
//                                tl.y.toFloat(),
//                                br.x.toFloat(),
//                                br.y.toFloat()
//                            ),
//                            rectPaint
//                        )
//                        imageView.setImageDrawable(BitmapDrawable(getResources(), tmpBm))
//
//                        targetChose = true
//                        touchCount = 0
//                        imageView.setOnTouchListener(null)
//                        paintSelected(bitmap)

                    }
                }

                return true
            }
        })

    }

    private fun loadFromGallery(data:Intent?) {
        val selectedImage = data?.data
        val filePathColumn: Array<String> = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = getContentResolver().query(selectedImage!!,filePathColumn, null, null, null)
        cursor?.moveToFirst()
        val columnIndex = cursor?.getColumnIndex(filePathColumn[0])
        val picturePath = cursor?.getString(columnIndex!!)
        cursor?.close()

        bitmap = BitmapFactory.decodeFile(picturePath)

        bitmap = getResizedBitmap(bitmap,bitmap.width/5,bitmap.height/5)
        showImage()
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            LoadImage.PICK_FROM_CAMERA.ordinal -> {

                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {

                    Log.e(TAG, "Permission has been denied by user")
                } else {
                    openCamera()
//                    val i =  Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
//                    startActivityForResult(i, PICK_FROM_GALLERY)
                    Log.e(TAG, "Permission has been granted by user")
                }
            }

        }
    }

    private lateinit var imageFilePath: String

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format( Date())
        val imageFileName = "IMG_" + timeStamp + "_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
        )

        imageFilePath = image.getAbsolutePath()
        return image;
    }

    private fun saveImage(image: Bitmap) {
        val pictureFile = createImageFile()
        if (pictureFile == null) {
            Log.e(TAG, "Error creating media file, check storage permissions: ")
            return
        }
        try {
            val fos = FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.PNG, 90, fos)
            fos.close()
        } catch (e: FileNotFoundException) {
            Log.e(TAG, "File not found: " + e.message)
        } catch (e: IOException) {
            Log.e(TAG, "Error accessing file: " + e.message)
        }
    }

    private fun openCamera() {
        if (ActivityCompat.checkSelfPermission(baseContext, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, PERMISSIONS, LoadImage.PICK_FROM_CAMERA.ordinal)

        } else {
            val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            var photoFile: File? = null
            try {
                photoFile = createImageFile()
            } catch (ex: IOException) {
                // Error occurred while creating the File
            }
            if (photoFile != null) {
                val photoURI = FileProvider.getUriForFile(this,"com.example.paintapp.provider", photoFile)
                captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
//                captureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                startActivityForResult(captureIntent, LoadImage.PICK_FROM_CAMERA.ordinal)
            }

        }
    }

    fun getResizedBitmap(bm: Bitmap, newWidth: Int, newHeight: Int): Bitmap {
        val width = bm.getWidth()
        val height = bm.getHeight()
        val scaleWidth = newWidth / width.toFloat()
        val scaleHeight = newHeight / height.toFloat()
        // CREATE A MATRIX FOR THE MANIPULATION
        val matrix =  Matrix()
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight)

        // "RECREATE" THE NEW BITMAP
        val resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true)

        return resizedBitmap
    }

//    private val coordinates: Coordinates = Coordinates(Point(-1.0, -1.0), Point(-1.0, -1.0))
//
//    private fun extractForeGround(bitmap: Bitmap) {
//
//        val img = Mat()
//        Utils.bitmapToMat(bitmap, img)
//        val r = img.rows()
//        val c = img.cols()
//
//        val p1 = Point(c / 5.0, r / 5.0)
//        val p2 = Point(c - c / 5.0, r - r / 8.0)
//
//        val rect = Rect(p1, p2)
//
//        val fgdModel = Mat()
//        fgdModel.setTo(Scalar(255.0, 255.0, 255.0))
//        val bgdModel = Mat()
//        bgdModel.setTo(Scalar(255.0, 255.0, 255.0))
//
//
//        val srcImage = Mat()
//        Imgproc.cvtColor(img, srcImage, Imgproc.COLOR_RGBA2RGB)
//
//
//        val iterations = 5
//
//        // Mask image where we specify which areas are background, foreground or probable background/foreground
//        val firstMask = Mat()
//
//        val source = Mat(1, 1, CvType.CV_8U, Scalar(Imgproc.GC_PR_FGD.toDouble()))
////        val rect = Rect(coordinates.first, coordinates.second)
//
//        // Run the grab cut algorithm with a rectangle (for subsequent iterations with touch-up strokes,
//        // flag should be Imgproc.GC_INIT_WITH_MASK)
//        Imgproc.grabCut(srcImage, firstMask, rect, bgdModel, fgdModel, iterations, Imgproc.GC_INIT_WITH_RECT)
//
//        // Create a matrix of 0s and 1s, indicating whether individual pixels are equal
//        // or different between "firstMask" and "source" objects
//        // Result is stored back to "firstMask"
//        Core.compare(firstMask, source, firstMask, Core.CMP_EQ)
//
//        // Create a matrix to represent the foreground, filled with white color
//        val foreground = Mat(srcImage.size(), CvType.CV_8UC3, Scalar(255.0, 255.0, 255.0))
//
//        // Copy the foreground matrix to the first mask
//        srcImage.copyTo(foreground, firstMask)
//
//        // Create a red color
//        val color = Scalar(255.0, 0.0, 0.0, 255.0)
//        // Draw a rectangle using the coordinates of the bounding box that surrounds the foreground
//        Imgproc.rectangle(srcImage, coordinates.first, coordinates.second, color)
//
//        // Create a new matrix to represent the background, filled with white color
//        val background = Mat(srcImage.size(), CvType.CV_8UC3, Scalar(255.0, 255.0, 255.0))
//
//        val mask = Mat(foreground.size(), CvType.CV_8UC1, Scalar(255.0, 255.0, 255.0))
//        // Convert the foreground's color space from BGR to gray scale
//        Imgproc.cvtColor(foreground, mask, Imgproc.COLOR_BGR2GRAY)
//
//        // Separate out regions of the mask by comparing the pixel intensity with respect to a threshold value
//        Imgproc.threshold(mask, mask, 254.0, 255.0, Imgproc.THRESH_BINARY_INV)
//
//        // Create a matrix to hold the final image
//        val dst = Mat()
//        // copy the background matrix onto the matrix that represents the final result
//        background.copyTo(dst)
//
//        val vals = Mat(1, 1, CvType.CV_8UC3, Scalar(0.0))
//        // Replace all 0 values in the background matrix given the foreground mask
//        background.setTo(vals, mask)
//
//        // Add the sum of the background and foreground matrices by applying the mask
//        Core.add(background, foreground, dst, mask)
//
//        Utils.matToBitmap(dst,bitmap)
//        inputImage.setImageBitmap(bitmap)
//
//        // Clean up used resources
//        firstMask.release()
//        source.release()
//        bgdModel.release()
//        fgdModel.release()
//        vals.release()
//        dst.release()
//
//    }
//
//
//    private fun paintSelected(bitmap: Bitmap) {
//
//        val img = Mat()
//        Utils.bitmapToMat(bitmap, img)
//
//        var background = Mat(
//            img.size(), CvType.CV_8UC3,
//            Scalar(255.0, 255.0, 255.0)
//        )
//        val firstMask = Mat()
//
//        val mask: Mat
//        val source = Mat(1, 1, CvType.CV_8U, Scalar(Imgproc.GC_PR_FGD.toDouble()))
//        val dst = Mat()
//
//
//        val displayMetrics = DisplayMetrics()
//        windowManager.defaultDisplay.getMetrics(displayMetrics)
//        val height = displayMetrics.heightPixels
//        val width = displayMetrics.widthPixels
//
//        val p1 = Point(tl.x*(252.0/width), tl.y*(189.0/height))
//        val p2 = Point(p1.x+50, p1.y+50)
//
////        val p1 = Point(0.0,0.0)
////        val p2 = Point(img.width()-1.0,img.height()-1.0)
//
//        val rect = Rect(p1, p2)
//
//        val fgModel = Mat()
////        fgModel.setTo(Scalar(255.0, 255.0, 255.0))
//        val bgModel = Mat()
////        bgModel.setTo(Scalar(255.0, 255.0, 255.0))
//
//        Imgproc.cvtColor(img, img, Imgproc.COLOR_RGBA2RGB)
//
//        Log.e(TAG,"("+img.rows()+","+img.cols()+")")
//
//        Imgproc.grabCut(
//            img, firstMask, rect, bgModel, fgModel,
//            5, Imgproc.GC_INIT_WITH_RECT
//        )
//
//        Core.compare(firstMask, source, firstMask, Core.CMP_EQ)
//
//        val foreground = Mat(
//            img.size(), CvType.CV_8UC3,
//            Scalar(255.0, 255.0, 255.0)
//        )
//        img.copyTo(foreground, firstMask)
//
//        val color = Scalar(255.0, 0.0, 0.0, 255.0)
//        Imgproc.rectangle(img, p1, p2, color)
//
//        val tmp = Mat()
//        Imgproc.resize(background, tmp, img.size())
//        background = tmp
//        mask = Mat(
//            foreground.size(), CvType.CV_8UC1,
//            Scalar(255.0, 255.0, 255.0)
//        )
//
//        Imgproc.cvtColor(foreground, mask, Imgproc.COLOR_BGR2GRAY)
//        Imgproc.threshold(mask, mask, 254.0, 255.0, Imgproc.THRESH_BINARY_INV)
//        val vals = Mat(1, 1, CvType.CV_8UC3, Scalar(0.0))
//        background.copyTo(dst)
//
//        background.setTo(vals, mask)
//
//        Core.add(background, foreground, dst, mask)
//
//        firstMask.release()
//        source.release()
//        bgModel.release()
//        fgModel.release()
//        vals.release()
//
//        Utils.matToBitmap(dst,bitmap)
//
//        inputImage.setImageBitmap(bitmap)
//    }
//
//
//
//    private fun makeContours(bitmap: Bitmap) {
//
//        var mRgbMat = Mat();
//        Utils.bitmapToMat(bitmap, mRgbMat);
//
//        var mGreyScaleMat = Mat()
//        Imgproc.cvtColor( mRgbMat, mGreyScaleMat, Imgproc.COLOR_RGB2GRAY, 3)
//
//        var finals = Mat()
//
//        Imgproc.blur(mGreyScaleMat,finals, Size(3.0,3.0))
//        Imgproc.GaussianBlur(mGreyScaleMat, finals,Size(1.0,1.0), 0.0)
//        Imgproc.Canny( finals, finals, 50.0, 150.0, 3 );
//
//        Imgproc.dilate( finals, finals, Mat());
////        Imgproc.dilate( finals, finals, Mat());
////        Imgproc.dilate( finals, finals, Mat());
//
//
//        var contours = ArrayList<MatOfPoint>()
//        var hierarchy = Mat()
//        Imgproc.findContours(finals, contours,Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)
//        for (contourIdx in contours.indices) {
//            Log.e(TAG,"contour size: "+contours[contourIdx].size().height)
//            if (contours[contourIdx].size().height > 100 )
//            {
//                val color = Scalar(Color.red(Color.RED).toDouble(), Color.green(Color.RED).toDouble(), Color.blue(Color.RED).toDouble())
//                Imgproc.drawContours(mRgbMat, contours, contourIdx, color, Imgproc.CV_WARP_FILL_OUTLIERS, 2, hierarchy)
//                Imgproc.fillPoly(mRgbMat, listOf(contours[contourIdx]), color)
//            }
//        }
//
//        Utils.matToBitmap(mRgbMat,bitmap)
//        inputImage.setImageBitmap(bitmap)
//
//    }
//
//    private fun detectEdges(bitmap: Bitmap) {
//        val rgba = Mat()
//        Utils.bitmapToMat(bitmap, rgba)
//
//        val edges = Mat(rgba.size(), CvType.CV_8UC1)
//        Imgproc.cvtColor(rgba, edges, Imgproc.COLOR_RGB2GRAY, 4)
//        Imgproc.Canny(edges, edges, 80.0, 100.0)
//
//        val resultBitmap = Bitmap.createBitmap(edges.cols(), edges.rows(), Bitmap.Config.ARGB_8888)
//        Utils.matToBitmap(edges, resultBitmap)
//        inputImage.setImageBitmap(resultBitmap)
//
//    }
//
//    private fun detectAndFillContours(bitmap: Bitmap) {
//
//        val imageIn = Mat()
//        Utils.bitmapToMat(bitmap, imageIn)
//        var imageOut = zeros(imageIn.rows(), imageIn.cols(), CvType.CV_8UC3);
//
//        Imgproc.cvtColor(imageIn, imageIn, Imgproc.COLOR_RGBA2GRAY)
//
//        var contours = ArrayList<MatOfPoint>()
//        var hierarchy = Mat()
//
//        Imgproc.findContours( imageIn, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE )
//
//        var idx = 0
//        while( idx >= 0)
//        {
//            val color = Scalar(Color.red(Color.RED).toDouble(), Color.green(Color.RED).toDouble(), Color.blue(Color.RED).toDouble())
//            Imgproc.drawContours( imageOut, contours, idx, color, Imgproc.CV_WARP_FILL_OUTLIERS, 8, hierarchy )
//            val idxArray = IntArray(1)
//            idxArray.set(0,idx)
//            idx = hierarchy[idxArray][0].toInt()
//        }
//
//    }
//
//    private fun steptowatershed(bitmap: Bitmap): Bitmap
//    {
//        val img = Mat()
//        Utils.bitmapToMat(bitmap, img)
//
//        var threeChannel =  Mat();
//
//        Imgproc.cvtColor(img, threeChannel, Imgproc.COLOR_RGBA2GRAY);
//        Imgproc.threshold(threeChannel, threeChannel, 100.0, 255.0, Imgproc.THRESH_BINARY);
//
//        var fg = Mat(img.size(),CvType.CV_8U);
//        Imgproc.erode(threeChannel,fg, Mat());
//
//        var bg = Mat(img.size(),CvType.CV_8U);
//        Imgproc.dilate(threeChannel,bg, Mat());
//        Imgproc.threshold(bg,bg,1.0, 128.0,Imgproc.THRESH_BINARY_INV);
//
//        var markers = Mat(img.size(),CvType.CV_8U,  Scalar(0.0));
//        Core.add(fg, bg, markers);
//        var result1 = Mat()
//        var segmenter =  WatershedSegmenter()
//        segmenter.setMarkers(markers);
//        result1 = segmenter.process(img)
//
//        Imgproc.cvtColor(img, img, Imgproc.COLOR_RGBA2RGB);
//        Imgproc.cvtColor(result1, result1, Imgproc.COLOR_GRAY2RGB);
//
//        var result = Mat()
//
//        Core.add(result1,img,result)
//
////        Core.multiply(result1,img,result)
//
//        Utils.matToBitmap(result,bitmap)
//
//        return bitmap;
//    }
//
//    private fun processImage(bitmap: Bitmap) {
//        val img = Mat()
//        Utils.bitmapToMat(bitmap, img)
//
//        val cvSize = img.size()
//
//        val hsvImage = Mat()
//        Imgproc.cvtColor(img,hsvImage,Imgproc.COLOR_RGB2HSV)
////            Mat(cvSize.height.toInt(), img.depth(), img.channels())
//
////        val hChannel = Mat(cvSize, img.depth(), Scalar(1.0))
////        val sChannel = Mat(cvSize, img.depth(), Scalar(1.0))
////        val vChannel = Mat(cvSize, img.depth(), Scalar(1.0))
//
//        var list = ArrayList<Mat>(4)
//        Core.split(hsvImage, list)
//
//
//        val cvInRange = Mat(cvSize, img.depth(), Scalar(1.0))
//        val source = Scalar(0.66, 72 / 2.0, 0.07 * 255, 0.0) //source color to replace
//        val from = source.`val`[0]
//        val to = source.`val`[1]
//
////        val white_lower = Scalar(0.0,0.0,255.0,0.0)
////        val white_upper = Scalar(255.0,255.0,255.0,0.0)
//
//        Core.inRange(hsvImage, Scalar(from), Scalar(to), cvInRange)
//
//        val dest = Mat(cvSize.height.toInt(), img.depth(), img.channels())
//
//        val temp = Mat(cvSize, img.depth()+5, Scalar(2.0))
//        Core.merge(listOf(list.get(0),list.get(1)), temp)
//
//        temp.setTo(Scalar(0.0, 0.0, 0.0, 0.0), cvInRange)// destination hue and sat
//
//        var list1 = ArrayList<Mat>(2)
//        Core.split(temp, list1)
//        Core.merge(listOf(list1.get(0),list1.get(1),list.get(2)), dest)
//        Imgproc.cvtColor(dest, dest, Imgproc.COLOR_HSV2BGR)
//
//        Utils.matToBitmap(dest,bitmap)
//
//        this.bitmap = bitmap
//        inputImage.setImageBitmap(this.bitmap)
//
//    }
//
//    private fun gaussianOutput(bitmap: Bitmap) {
//        var gaussian_output =  Mat()
//
//        val img = Mat()
//        Utils.bitmapToMat(bitmap, img)
//
//        Imgproc.cvtColor(img,img,Imgproc.COLOR_RGBA2RGB)
//
//        var mIntermediateMat = Mat()
//        Imgproc.Canny(img, mIntermediateMat, 80.0, 100.0);
//
////        Imgproc.cvtColor(mIntermediateMat, img, Imgproc.COLOR_YUV2RGBA_NV21, 4);
//        Imgproc.GaussianBlur(mIntermediateMat, gaussian_output, Size(5.0, 5.0), 5.0);
//        var contours =  ArrayList<MatOfPoint>()
//        Imgproc.findContours( gaussian_output, contours,  Mat(),Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE,  Point(0.0,0.0) );
//        var color =  Scalar(165.0, 30.0, 215.0);
//        // Imgproc.drawContours(gaussian_output, contours, -1, color, 3);
//        var hierarchy =  Mat()
//        // find contours:
//        Imgproc.findContours(gaussian_output, contours, hierarchy, Imgproc.RETR_TREE,Imgproc.CHAIN_APPROX_SIMPLE);
//        var contourIdx = 0
//        while (contourIdx < contours.size) {
//            Imgproc.drawContours(gaussian_output, contours, contourIdx,color, -1);
//            contourIdx += 1
//        }
//
//        Imgproc.cvtColor(gaussian_output,gaussian_output,Imgproc.COLOR_GRAY2RGB)
//
//        var result = Mat()
//        Core.add(gaussian_output,img,result)
//
//        Utils.matToBitmap(result,bitmap)
//
//        inputImage.setImageBitmap(bitmap)
//    }
//
//    private fun floodFill(bitmap1: Bitmap,p: Point) {
//
//        val p1 = Point(p.x*(bitmap.width/1080.0), p.y*(bitmap.height/1920.0))
//
//        val f = QueueLinearFloodFiller(bitmap)
//        f.setTargetColor(Color.BLUE)
//        f.floodFill(p1.x.toInt(),p1.y.toInt())
//        bitmap = f.image!!
//        inputImage.setImageBitmap(bitmap)
//    }
//
//    private fun rppaint(bitmap: Bitmap,p: Point) {
//
//        val cannyMinThres = 25.0
//        val ratio = 60/cannyMinThres
//
//        // show intermediate step results
//        // grid created here to do that
//        showResultLayouts()
//
//        val mRgbMat = Mat()
//        Utils.bitmapToMat(bitmap, mRgbMat)
//
//        showImage(mRgbMat,inputImage)
//
//        Imgproc.cvtColor(mRgbMat,mRgbMat,Imgproc.COLOR_RGBA2RGB)
//
//        val mask = Mat(Size(mRgbMat.width()/8.0, mRgbMat.height()/8.0), CvType.CV_8UC1, Scalar(0.0))
//        Imgproc.dilate(mRgbMat, mRgbMat,mask, Point(0.0,0.0), 5)
//
//        val img = Mat()
//        mRgbMat.copyTo(img)
//
//        // grayscale
//        val mGreyScaleMat = Mat()
//        Imgproc.cvtColor(mRgbMat, mGreyScaleMat, Imgproc.COLOR_RGB2GRAY, 3)
////        Imgproc.medianBlur(mGreyScaleMat,mGreyScaleMat,7)
//
//        showImage(mGreyScaleMat,greyScaleImage)
//
//        // canny
//        val cannyMat = Mat()
//        Imgproc.Canny(mRgbMat, cannyMat, cannyMinThres, cannyMinThres*ratio, 3)
//
//        showImage(cannyMat,cannyEdgeImage)
//
//
//        val displayMetrics = DisplayMetrics()
//        windowManager.defaultDisplay.getMetrics(displayMetrics)
//        val height = displayMetrics.heightPixels
//        val width = displayMetrics.widthPixels
//
//        val seedPoint = Point(p.x*(mRgbMat.width()/width.toDouble()), p.y*(mRgbMat.height()/height.toDouble()))
//
//        Imgproc.resize(cannyMat, cannyMat, Size(cannyMat.width() + 2.0, cannyMat.height() + 2.0))
//
//        val floodFillFlag = 8
//        Imgproc.floodFill(
//            mRgbMat,
//            cannyMat,
//            seedPoint,
//            Scalar(200.0, 0.0, 0.0),
//            Rect(),
//            Scalar(5.0, 5.0, 5.0),
//            Scalar(5.0, 5.0, 5.0),
//            floodFillFlag
//        )
//        showImage(mRgbMat,floodFillImage)
//
//        Imgproc.dilate(mRgbMat, mRgbMat, mask, Point(0.0,0.0), 5)
//
////        Imgproc.medianBlur(mRgbMat,mRgbMat,15)
//
//        showImage(mRgbMat,HSVImage)
//
//        val hsvImage = Mat()
//        Imgproc.cvtColor(img,hsvImage,Imgproc.COLOR_RGB2HSV)
//
//        //got the hsv values
//        val list = ArrayList<Mat>(3)
//        Core.split(hsvImage, list)
//
//        //got the hsv of the mask image
//        val rgbHsvImage = Mat()
//        Imgproc.cvtColor(mRgbMat,rgbHsvImage,Imgproc.COLOR_RGB2HSV)
//
//        val list1 = ArrayList<Mat>(3)
//        Core.split(rgbHsvImage, list1)
//
//        //merged the "v" of original image with mRgb mat
//        val result = Mat()
//        Core.merge(listOf(list1.get(0),list1.get(1),list.get(2)), result)
//
////        showImage(result,dilatedImage)
//
//        // converted to rgb
//        Imgproc.cvtColor(result, result, Imgproc.COLOR_HSV2RGB)
//
//        Core.addWeighted(result,0.8, img,0.2 ,0.0,result )
//
//        showImage(result,outputImage)
//    }

    private fun rpPaintHSV(bitmap: Bitmap, p: Point): Mat {
        val cannyMinThres = 30.0
        val ratio = 2.5

        // show intermediate step results
        // grid created here to do that
        showResultLayouts()

        val mRgbMat = Mat()
        Utils.bitmapToMat(bitmap, mRgbMat)

        showImage(mRgbMat,inputImage)

        Imgproc.cvtColor(mRgbMat,mRgbMat,Imgproc.COLOR_RGBA2RGB)

        val mask = Mat(Size(mRgbMat.width()/8.0, mRgbMat.height()/8.0), CvType.CV_8UC1, Scalar(0.0))
//        Imgproc.dilate(mRgbMat, mRgbMat,mask, Point(0.0,0.0), 5)

        val img = Mat()
        mRgbMat.copyTo(img)

        // grayscale
        val mGreyScaleMat = Mat()
        Imgproc.cvtColor(mRgbMat, mGreyScaleMat, Imgproc.COLOR_RGB2GRAY, 3)
        Imgproc.medianBlur(mGreyScaleMat,mGreyScaleMat,3)


        val cannyGreyMat = Mat()
        Imgproc.Canny(mGreyScaleMat, cannyGreyMat, cannyMinThres, cannyMinThres*ratio, 3)

        showImage(cannyGreyMat,greyScaleImage)

        //hsv
        val hsvImage = Mat()
        Imgproc.cvtColor(img,hsvImage,Imgproc.COLOR_RGB2HSV)

        //got the hsv values
        val list = ArrayList<Mat>(3)
        Core.split(hsvImage, list)

        val sChannelMat = Mat()
        Core.merge(listOf(list.get(1)), sChannelMat)
        Imgproc.medianBlur(sChannelMat,sChannelMat,3)
        showImage(sChannelMat,floodFillImage)

        // canny
        val cannyMat = Mat()
        Imgproc.Canny(sChannelMat, cannyMat, cannyMinThres, cannyMinThres*ratio, 3)
        showImage(cannyMat,HSVImage)

        Core.addWeighted(cannyMat,0.5, cannyGreyMat,0.5 ,0.0,cannyMat)
        Imgproc.dilate(cannyMat, cannyMat,mask, Point(0.0,0.0), 5)

        showImage(cannyMat,cannyEdgeImage)

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels
        val width = displayMetrics.widthPixels

        val seedPoint = Point(p.x*(mRgbMat.width()/width.toDouble()), p.y*(mRgbMat.height()/height.toDouble()))

        Imgproc.resize(cannyMat, cannyMat, Size(cannyMat.width() + 2.0, cannyMat.height() + 2.0))

        Imgproc.medianBlur(mRgbMat,mRgbMat,15)

        val floodFillFlag = 8
        Imgproc.floodFill(
            mRgbMat,
            cannyMat,
            seedPoint,
            Scalar(Color.red(chosenColor).toDouble(),Color.green(chosenColor).toDouble(),Color.blue(chosenColor).toDouble()),
            Rect(),
            Scalar(5.0, 5.0, 5.0),
            Scalar(5.0, 5.0, 5.0),
            floodFillFlag
        )
//        showImage(mRgbMat,floodFillImage)
        Imgproc.dilate(mRgbMat, mRgbMat, mask, Point(0.0,0.0), 5)

        //got the hsv of the mask image
        val rgbHsvImage = Mat()
        Imgproc.cvtColor(mRgbMat,rgbHsvImage,Imgproc.COLOR_RGB2HSV)

        val list1 = ArrayList<Mat>(3)
        Core.split(rgbHsvImage, list1)

        //merged the "v" of original image with mRgb mat
        val result = Mat()
        Core.merge(listOf(list1.get(0),list1.get(1),list.get(2)), result)

        // converted to rgb
        Imgproc.cvtColor(result, result, Imgproc.COLOR_HSV2RGB)

        Core.addWeighted(result,0.7, img,0.3 ,0.0,result )

        showImage(result,outputImage)
        return result
    }


    private fun showImage(image: Mat, view: ImageView) {
        val mBitmap = Bitmap.createBitmap(image.cols(), image.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(image, mBitmap)
        view.setImageBitmap(mBitmap)

        bitmap = mBitmap
        saveImage(bitmap)
    }

    private fun showResultLayouts() {
        imageFromData.visibility = View.GONE

        topLayout.visibility = View.VISIBLE
        middleLayout.visibility = View.VISIBLE
        bottomLayout.visibility = View.VISIBLE
    }

    private fun showImage() {
        imageFromData.visibility = View.VISIBLE

        topLayout.visibility = View.GONE
        middleLayout.visibility = View.GONE
        bottomLayout.visibility = View.GONE

        try {
            imageFromData.setImageBitmap(bitmap)
        } catch (e: Exception) {
            Toast.makeText(this@MainActivity, "No image selected",Toast.LENGTH_SHORT).show()
        }
    }

    private fun chooseColor() {
        texture = false

        val colorPicker = AmbilWarnaDialog(this@MainActivity, chosenColor, object: AmbilWarnaDialog.OnAmbilWarnaListener {

            override fun onCancel(dialog: AmbilWarnaDialog) {
            }

            override fun onOk(dialog: AmbilWarnaDialog ,color: Int) {
                chosenColor = color
            }
        })

        colorPicker.show()
    }


    private fun applyTexture(bitmap: Bitmap, p: Point) {
        val cannyMinThres = 30.0
        val ratio = 2.5

        // show intermediate step results
        // grid created here to do that
        showResultLayouts()

        val mRgbMat = Mat()
        Utils.bitmapToMat(bitmap, mRgbMat)

        showImage(mRgbMat,inputImage)

        Imgproc.cvtColor(mRgbMat,mRgbMat,Imgproc.COLOR_RGBA2RGB)

        val mask = Mat(Size(mRgbMat.width()/8.0, mRgbMat.height()/8.0), CvType.CV_8UC1, Scalar(0.0))
//        Imgproc.dilate(mRgbMat, mRgbMat,mask, Point(0.0,0.0), 5)

        val img = Mat()
        mRgbMat.copyTo(img)

        // grayscale
        val mGreyScaleMat = Mat()
        Imgproc.cvtColor(mRgbMat, mGreyScaleMat, Imgproc.COLOR_RGB2GRAY, 3)
        Imgproc.medianBlur(mGreyScaleMat,mGreyScaleMat,3)


        val cannyGreyMat = Mat()
        Imgproc.Canny(mGreyScaleMat, cannyGreyMat, cannyMinThres, cannyMinThres*ratio, 3)

        showImage(cannyGreyMat,greyScaleImage)

        //hsv
        val hsvImage = Mat()
        Imgproc.cvtColor(img,hsvImage,Imgproc.COLOR_RGB2HSV)

        //got the hsv values
        val list = ArrayList<Mat>(3)
        Core.split(hsvImage, list)

        val sChannelMat = Mat()
        Core.merge(listOf(list.get(1)), sChannelMat)
        Imgproc.medianBlur(sChannelMat,sChannelMat,3)
        showImage(sChannelMat,floodFillImage)

        // canny
        val cannyMat = Mat()
        Imgproc.Canny(sChannelMat, cannyMat, cannyMinThres, cannyMinThres*ratio, 3)
        showImage(cannyMat,HSVImage)

        Core.addWeighted(cannyMat,0.5, cannyGreyMat,0.5 ,0.0,cannyMat)
        Imgproc.dilate(cannyMat, cannyMat,mask, Point(0.0,0.0), 5)

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels
        val width = displayMetrics.widthPixels

        val seedPoint = Point(p.x*(mRgbMat.width()/width.toDouble()), p.y*(mRgbMat.height()/height.toDouble()))

        Imgproc.resize(cannyMat, cannyMat, Size(cannyMat.width() + 2.0, cannyMat.height() + 2.0))
        val cannyMat1 = Mat()
        cannyMat.copyTo(cannyMat1)

//        Imgproc.medianBlur(mRgbMat,mRgbMat,15)

        val wallMask = Mat(mRgbMat.size(),mRgbMat.type())

        val floodFillFlag = 8
        Imgproc.floodFill(
            wallMask,
            cannyMat,
            seedPoint,
            Scalar(255.0,255.0,255.0/*chosenColor.toDouble(),chosenColor.toDouble(),chosenColor.toDouble()*/),
            Rect(),
            Scalar(5.0, 5.0, 5.0),
            Scalar(5.0, 5.0, 5.0),
            floodFillFlag
        )
        showImage(wallMask,greyScaleImage)

        showImage(cannyMat,cannyEdgeImage)

        //second floodfill is not working 5
        Imgproc.floodFill(
            mRgbMat,
            cannyMat1,
            seedPoint,
            Scalar(0.0,0.0,0.0/*chosenColor.toDouble(),chosenColor.toDouble(),chosenColor.toDouble()*/),
            Rect(),
            Scalar(5.0, 5.0, 5.0),
            Scalar(5.0, 5.0, 5.0),
            floodFillFlag
        )
        showImage(mRgbMat,HSVImage)

        val texture = getTextureImage()

        val textureImgMat = Mat()
        Core.bitwise_and(wallMask ,texture,textureImgMat)

        showImage(textureImgMat,floodFillImage)

        val resultImage = Mat()
        Core.bitwise_or(textureImgMat,mRgbMat,resultImage)

        showImage(resultImage,outputImage)

        ////alpha blending

        //got the hsv of the mask image
        val rgbHsvImage = Mat()
        Imgproc.cvtColor(resultImage,rgbHsvImage,Imgproc.COLOR_RGB2HSV)

        val list1 = ArrayList<Mat>(3)
        Core.split(rgbHsvImage, list1)

        //merged the "v" of original image with mRgb mat
        val result = Mat()
        Core.merge(listOf(list1.get(0),list1.get(1),list.get(2)), result)

        // converted to rgb
        Imgproc.cvtColor(result, result, Imgproc.COLOR_HSV2RGB)

        Core.addWeighted(result,0.8, img,0.2 ,0.0,result )

        showImage(result,outputImage)
    }

    private fun getTextureImage(): Mat {
        var textureImage = BitmapFactory.decodeResource(getResources(), R.drawable.texture_small_brick_red)
        textureImage = getResizedBitmap(textureImage,bitmap.width,bitmap.height)
        val texture = Mat()
        Utils.bitmapToMat(textureImage,texture)
        Imgproc.cvtColor(texture,texture,Imgproc.COLOR_RGBA2RGB)
        return texture
    }

}
