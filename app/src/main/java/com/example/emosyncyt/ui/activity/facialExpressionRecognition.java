package com.example.emosyncyt.ui.activity;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.util.Log;

import com.example.emosyncyt.R;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.gpu.GpuDelegate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class facialExpressionRecognition {
    private Interpreter interpreter; // TensorFlow Lite interpreter for model inference
    private int INPUT_SIZE; // Input size for the model
    private int height = 0; // Original frame height
    private int width = 0; // Original frame width
    private GpuDelegate gpuDelegate = null; // GPU delegate for performance improvement
    private CascadeClassifier cascadeClassifier; // Cascade classifier for face detection
    private String detectedEmotion = "Unknown"; // Detected emotion

    public facialExpressionRecognition(AssetManager assetManager, Context context, String modelPath, int inputSize) throws IOException {
        INPUT_SIZE = inputSize;

        // Set up GPU for interpreter
        Interpreter.Options options = new Interpreter.Options();
        gpuDelegate = new GpuDelegate();
        options.addDelegate(gpuDelegate);
        options.setNumThreads(4); // Configure number of threads for the interpreter
        interpreter = new Interpreter(loadModelFile(assetManager, modelPath), options); // Load the model
        Log.d("facial_Expression", "Model is loaded");

        // Load Haar cascade classifier
        try {
            InputStream is = context.getResources().openRawResource(R.raw.haarcascade_frontalface_alt);
            File cascadeDir = context.getDir("cascade", Context.MODE_PRIVATE);
            File mCascadeFile = new File(cascadeDir, "haarcascade_frontalface_alt");
            FileOutputStream os = new FileOutputStream(mCascadeFile);
            byte[] buffer = new byte[4096];
            int byteRead;

            // Write the classifier data to the file
            while ((byteRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, byteRead);
            }
            is.close();
            os.close();
            cascadeClassifier = new CascadeClassifier(mCascadeFile.getAbsolutePath()); // Load classifier
            Log.d("facial_Expression", "Classifier is loaded");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getDetectedEmotion() {
        return detectedEmotion; // Return detected emotion
    }

    public Mat recognizeImage(Mat mat_image) {
        // Process image for emotion recognition
        Core.flip(mat_image.t(), mat_image, 1); // Rotate image
        Mat grayscaleImage = new Mat();
        Imgproc.cvtColor(mat_image, grayscaleImage, Imgproc.COLOR_RGBA2GRAY); // Convert to grayscale
        height = grayscaleImage.height();
        width = grayscaleImage.width();

        int absoluteFaceSize = (int) (height * 0.1); // Minimum face size for detection
        MatOfRect faces = new MatOfRect();

        // Detect faces using the classifier
        if (cascadeClassifier != null) {
            cascadeClassifier.detectMultiScale(grayscaleImage, faces, 1.1, 2, 2,
                    new Size(absoluteFaceSize, absoluteFaceSize), new Size());
        }

        // Process detected faces
        Rect[] faceArray = faces.toArray();
        for (Rect face : faceArray) {
            Imgproc.rectangle(mat_image, face.tl(), face.br(), new Scalar(0, 255, 0, 255), 2); // Draw rectangle around face

            Rect roi = new Rect((int) face.tl().x, (int) face.tl().y,
                    (int) face.br().x - (int) face.tl().x,
                    (int) face.br().y - (int) face.tl().y);
            Mat cropped_rgba = new Mat(mat_image, roi); // Crop the detected face
            Bitmap bitmap = Bitmap.createBitmap(cropped_rgba.cols(), cropped_rgba.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(cropped_rgba, bitmap); // Convert to Bitmap

            // Resize and prepare for model input
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 48, 48, false);
            ByteBuffer byteBuffer = convertBitmapToByteBuffer(scaledBitmap);
            float[][] emotion = new float[1][1]; // Output array for predicted emotion

            // Run the model to predict emotion
            interpreter.run(byteBuffer, emotion);
            float emotion_v = emotion[0][0]; // Get predicted emotion value
            Log.d("facial_expression", "Output:  " + emotion_v);

            // Map the predicted value to emotion text
            String emotion_s = get_emotion_text(emotion_v);
            Imgproc.putText(mat_image, emotion_s + " (" + emotion_v + ")",
                    new Point((int) face.tl().x + 10, (int) face.tl().y + 20),
                    1, 1.5, new Scalar(0, 0, 255, 150), 2);
            detectedEmotion = emotion_s; // Update detected emotion
        }

        // Rotate image back to original orientation
        Core.flip(mat_image.t(), mat_image, 0);
        return mat_image; // Return processed image
    }

    private String get_emotion_text(float emotion_v) {
        // Map emotion value to emotion text
        if (emotion_v >= 0 && emotion_v < 0.5) {
            return "Surprise";
        } else if (emotion_v >= 0.5 && emotion_v < 1.5) {
            return "Fear";
        } else if (emotion_v >= 1.5 && emotion_v < 2.5) {
            return "Angry";
        } else if (emotion_v >= 2.5 && emotion_v < 3.5) {
            return "Neutral";
        } else if (emotion_v >= 3.5 && emotion_v < 4.5) {
            return "Sad";
        } else if (emotion_v >= 4.5 && emotion_v < 5.5) {
            return "Disgust";
        } else {
            return "Happy";
        }
    }

    private ByteBuffer convertBitmapToByteBuffer(Bitmap scaledBitmap) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * 1 * INPUT_SIZE * INPUT_SIZE * 3);
        byteBuffer.order(ByteOrder.nativeOrder());
        int[] intValues = new int[INPUT_SIZE * INPUT_SIZE];
        scaledBitmap.getPixels(intValues, 0, scaledBitmap.getWidth(), 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight());

        // Fill byte buffer with scaled image values
        int pixel = 0;
        for (int i = 0; i < INPUT_SIZE; ++i) {
            for (int j = 0; j < INPUT_SIZE; ++j) {
                final int val = intValues[pixel++];
                byteBuffer.putFloat((((val >> 16) & 0xFF)) / 255.0f); // Red
                byteBuffer.putFloat((((val >> 8) & 0xFF)) / 255.0f); // Green
                byteBuffer.putFloat((val & 0xFF) / 255.0f); // Blue
            }
        }
        return byteBuffer; // Return byte buffer for model input
    }

    private MappedByteBuffer loadModelFile(AssetManager assetManager, String modelPath) throws IOException {
        // Load the model file into a mapped byte buffer
        AssetFileDescriptor assetFileDescriptor = assetManager.openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(assetFileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();

        long startOffset = assetFileDescriptor.getStartOffset();
        long declaredLength = assetFileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength); // Return the mapped buffer
    }
}
