package com.example.pokimon10

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.ar.core.AugmentedImageDatabase
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.sceneform.ux.ArFragment
import java.io.IOException

private const val REQUEST_CODE_CHOOSE_IMAGE = 0

class ImageArFragment():ArFragment() {
    override fun onAttach(context: Context) {
        super.onAttach(context)
        chooseNewImage()
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        planeDiscoveryController.hide()
        planeDiscoveryController.setInstructionView(null) //white hand
        arSceneView.planeRenderer.isEnabled=false //white dots
    }

    override fun getSessionConfiguration(session: Session?): Config {
        val config= super.getSessionConfiguration(session)
        config.focusMode=Config.FocusMode.AUTO
        return config                              // Its just focus camera to identify image
    }

    private fun chooseNewImage() {
        Intent(Intent.ACTION_GET_CONTENT).run {  //open the gallery to choose image
            type = "image/*"
            startActivityForResult(this, REQUEST_CODE_CHOOSE_IMAGE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)  // now this fun get image from the gallery
        if(resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_CHOOSE_IMAGE) {
            val imageUri = data?.data ?: return          //it data==null then return
            val session = arSceneView.session ?: return //it session==null then return
            val config = getSessionConfiguration(session)
            val database = createAugmentedImageDatabaseWithSingleImage(session, imageUri)
            config.augmentedImageDatabase = database
            config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE  // to get the update fram
            session.configure(config)        // here its the end of manipulate and create ArFragment with data basethat include one image from the gallery
        }                                   // in the end we create database with image in this session
    }

    private fun createAugmentedImageDatabaseWithSingleImage(session: Session, imageUri: Uri): AugmentedImageDatabase {
        val database = AugmentedImageDatabase(session)
        val bmp = loadAugmentedImageBitmap(imageUri)
        database.addImage("myImage.jpg", bmp)
        return database
    }

    private fun loadAugmentedImageBitmap(imageUri: Uri): Bitmap? {
        return try {
            context?.contentResolver?.openInputStream(imageUri)?.use {
                BitmapFactory.decodeStream(it)
            }
        } catch(e: IOException) {
            Log.e("ImageArFragment", "IOException while loading augmented image from storage", e)
            null
        }
    }
}