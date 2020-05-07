package com.example.pokimon10

import android.media.CamcorderProfile
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Toast
import com.google.ar.core.AugmentedImage
import com.google.ar.core.Frame
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.ux.ArFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity()
{
    private lateinit var arFragment: ArFragment
    private val augmentedImageMap = HashMap<AugmentedImage, AugmentedImageNode>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        arFragment=fragment as ArFragment

        arFragment.arSceneView.scene.addOnUpdateListener {
            val curFrame = arFragment.arSceneView.arFrame
            if(curFrame != null && curFrame.camera.trackingState == TrackingState.TRACKING) {
                updateTrackedImages(curFrame)
            }
        }
        setupFab()
    }

    private fun updateTrackedImages(frame: Frame) {
        val imageList = frame.getUpdatedTrackables(AugmentedImage::class.java)
//HasMap that trace and collect only augmentedImage
        for(image in imageList) {                // check in all existing AugmentedImage imageList
            if(image.trackingState == TrackingState.TRACKING) {  // if the image in state of tracking
                if(!augmentedImageMap.containsKey(image)) {      // if this image not existing in our map
                           // we create new item to our list
                    AugmentedImageNode(this).apply {
                        setAugmentedImage(image)
                        augmentedImageMap[image] = this
                        arFragment.arSceneView.scene.addChild(this)
                    }
                }
            } else if(image.trackingState == TrackingState.STOPPED) {
                augmentedImageMap.remove(image)
            }
        }
    }
    private fun setupFab() {
       /* private lateinit var photoSaver: PhotoSaver
        private lateinit var videoRecorder: VideoRecorder
        private var isRecording = false*/
        var isRecording = false
        val photoSaver = PhotoSaver(this)
        val videoRecorder = VideoRecorder(this).apply {
            sceneView = arFragment.arSceneView
            setVideoQuality(CamcorderProfile.QUALITY_1080P, resources.configuration.orientation)
        }
        fab.setOnClickListener {
            if (!isRecording) {
                eliminateDot()
                photoSaver.takePhoto(arFragment.arSceneView)
            }
        }
        fab.setOnLongClickListener {
            eliminateDot()
            isRecording = videoRecorder.toggleRecordingState()
            true
        }
        fab.setOnTouchListener { view, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_UP && isRecording) {
                isRecording = videoRecorder.toggleRecordingState()
                Toast.makeText(this, "Saved video to gallery!", Toast.LENGTH_LONG).show()
                true
            } else false
        }
    }

    private fun eliminateDot() {
        arFragment.arSceneView.planeRenderer.isVisible = false
        arFragment.planeDiscoveryController.hide()
        arFragment.planeDiscoveryController.setInstructionView(null)
    }
}
