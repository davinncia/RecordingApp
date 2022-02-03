package com.example.recordingapp

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.hardware.camera2.CameraCharacteristics
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import android.view.Display
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.example.recordingapp.databinding.ActivityMainBinding
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import android.hardware.camera2.CameraManager

class MainActivity : AppCompatActivity(), PoseImageAnalyser.PoseListener {
    private lateinit var viewBinding: ActivityMainBinding

    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null
    private var recordingState = RecordingState.STOPPED

    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        // Set up the listener for video capture button
        viewBinding.videoCaptureButton.setOnClickListener {
            if (recording != null) stopVideoCapture()
            else countDownStart(COUNT_DOWN_MANUAL_RECORDING).start()
        }

        cameraExecutor = Executors.newSingleThreadExecutor()

        setRecordingFrame(FrameState.DEFAULT)

    }

    private fun stopVideoCapture() {
        recording?.stop()
        recording = null
    }

    private fun captureVideo() {
        val videoCapture = this.videoCapture ?: return

        viewBinding.videoCaptureButton.isEnabled = false

        if (recording != null) {
            stopVideoCapture()
            return
        }

        // create and start a new recording session
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.getDefault())
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/CameraX-Video")
            }
        }

        val mediaStoreOutputOptions = MediaStoreOutputOptions
            .Builder(contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            .setContentValues(contentValues)
            .build()
        recording = videoCapture.output
            .prepareRecording(this, mediaStoreOutputOptions)
            .start(ContextCompat.getMainExecutor(this)) { recordEvent ->
                when(recordEvent) {
                    is VideoRecordEvent.Start -> {
                        recordingState = RecordingState.RECORDING
                        filmingDurationCountDown.start()
                        viewBinding.recordingIndicator.visibility = View.VISIBLE
                        viewBinding.videoCaptureButton.apply {
                            text = getString(R.string.stop_capture)
                            isEnabled = true
                        }
                    }
                    is VideoRecordEvent.Finalize -> {
                        if (!recordEvent.hasError()) {
                            val msg = "Video capture succeeded: " + "${recordEvent.outputResults.outputUri}"
                            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                            Log.d(TAG, msg)
                        } else {
                            recording?.close()
                            recording = null
                            Log.e(TAG, "Video capture ends with error: " + "${recordEvent.error}")
                        }
                        viewBinding.recordingIndicator.visibility = View.GONE
                        viewBinding.videoCaptureButton.apply {
                            text = getString(R.string.start_capture)
                            isEnabled = true
                        }
                        recordingState = RecordingState.STOPPED
                    }
                }
            }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
                }

            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.LOWEST))
                .build()
            videoCapture = VideoCapture.withOutput(recorder)

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            val imageAnalysis = getImageAnalysis()

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                if (analysisAvailable())
                    cameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis, preview, videoCapture)
                else
                    cameraProvider.bindToLifecycle(this, cameraSelector, preview, videoCapture)

            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    //----------------------------------- D E T E C T O R ----------------------------------------//

    private fun getImageAnalysis(): ImageAnalysis {
        // Detector
        val options = PoseDetectorOptions.Builder()
            .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
            .build()
        val poseDetector = PoseDetection.getClient(options)

        val wm = this.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display: Display = wm.defaultDisplay

        return ImageAnalysis.Builder()
            .setTargetResolution(Size(display.width, display.height))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also {
                it.setAnalyzer(
                    cameraExecutor,
                    PoseImageAnalyser(poseDetector, this)
                )
            }
    }

    override fun onPoseAnalysed(pose: Pose) {
        val skeleton = SkeletonDraw(this@MainActivity, pose)
        viewBinding.frameSkeleton.removeAllViews()
        viewBinding.frameSkeleton.addView(skeleton)
    }

    override fun fullBodyInFrame(inFrame: Boolean) {
        if (inFrame) {
            setRecordingFrame(FrameState.GOOD)
            if (recordingState == RecordingState.STOPPED) {
                // Launch automatic recording
                countDownStart(COUNT_DOWN_POSTURE_DETECTION).start()
            }
        }
        else setRecordingFrame(FrameState.WRONG)
    }

    //------------------------------------------ U I ---------------------------------------------//

    private var frameState: FrameState = FrameState.DEFAULT

    private fun setRecordingFrame(state: FrameState) {
        if (state == this.frameState) return

        this.frameState = state
        val frameDrawable = (viewBinding.frame.background as? GradientDrawable) ?: return
        frameDrawable.setStroke(state.widthPx, state.color)
    }

    private enum class FrameState(val widthPx: Int, val color: Int) {
        DEFAULT(20, Color.WHITE),
        HIDDEN(0, Color.TRANSPARENT),
        GOOD(20, Color.GREEN),
        WRONG(20, Color.RED)
    }

    private enum class RecordingState() {
        STOPPED, COUNT_DOWN, RECORDING
    }

    private fun countDownStart(time: Long): CountDownTimer {
        viewBinding.textCountdown.visibility = View.VISIBLE
        viewBinding.videoCaptureButton.text = ""
        viewBinding.videoCaptureButton.isEnabled = false
        recordingState = RecordingState.COUNT_DOWN

        return object : CountDownTimer(time + 1_000, 1_000L) {
            override fun onTick(p0: Long) {
                if (p0 < 1_000) {
                    viewBinding.textCountdown.text = "GO_"
                } else {
                    viewBinding.textCountdown.text = (p0 / 1_000L).toString()
                }
            }

            override fun onFinish() {
                viewBinding.textCountdown.visibility = View.GONE
                captureVideo()
            }
        }
    }

    private val filmingDurationCountDown: CountDownTimer =

        object : CountDownTimer(FILMING_DURATION, 1_000L) {
            override fun onTick(p0: Long) {}

            override fun onFinish() {
                stopVideoCapture()
            }
        }

    //--------------------------------- P E R M I S S I O N S ------------------------------------//

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    //--------------------------------------- U T I L S ------------------------------------------//

    private fun analysisAvailable(): Boolean {
        val manager = (getSystemService(CAMERA_SERVICE) as CameraManager)
        val frontCameraId = getFrontFacingCameraId(manager)
        frontCameraId ?: return false
        val characteristics = manager.getCameraCharacteristics(frontCameraId)

        return  (characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)
                != CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED)
    }

    private fun getFrontFacingCameraId(cManager: CameraManager): String? {
        for (cameraId in cManager.cameraIdList) {
            val characteristics = cManager.getCameraCharacteristics(cameraId)
            val cOrientation = characteristics.get(CameraCharacteristics.LENS_FACING)
            if (cOrientation == CameraCharacteristics.LENS_FACING_FRONT) return cameraId
        }
        return null
    }

    //----------------------------------- C O M P A N I O N --------------------------------------//

    companion object {
        private const val TAG = "CameraXApp"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"

        private const val COUNT_DOWN_POSTURE_DETECTION = 3_000L
        private const val COUNT_DOWN_MANUAL_RECORDING = 5_000L
        private const val FILMING_DURATION = 5_000L

        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf (Manifest.permission.CAMERA).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }
}