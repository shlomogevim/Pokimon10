package com.example.pokimon10

import android.content.Context
import android.widget.Toast
import com.google.ar.core.AugmentedImage
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.animation.ModelAnimator
import com.google.ar.sceneform.collision.Box
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import kotlin.math.max

class AugmentedImageNode(val context: Context) :
    AnchorNode() {   //so we dont need to create anchore node
    var image: AugmentedImage? = null
    private var modelCompleatableFuture = ModelRenderable.builder()
        .setSource(context, R.raw.beedrill)
        .build()

    private lateinit var renderable: ModelRenderable

    fun setAugmentedImage(image: AugmentedImage) {
        this.image = image
        if (!modelCompleatableFuture.isDone) {
            modelCompleatableFuture.thenAccept {
                setAugmentedImage(image) //call again to the same function till ..Futher Done
            }.exceptionally {
                Toast.makeText(context, "Error creating renderable", Toast.LENGTH_LONG).show()
                null
            }
            return
        }
        renderable = modelCompleatableFuture.get()
        anchor =
            image.createAnchor(image.centerPose)  // we can do it directly because we inherite from AnchorNode class
        val modelNode = Node().apply {
            setParent(this@AugmentedImageNode)
            renderable = this@AugmentedImageNode.renderable
        }

        val renderableBox = renderable.collisionShape as Box
        val maxEdgeSize = max(renderableBox.size.x, renderableBox.size.z)
        val maxImageEdge = max(image.extentX, image.extentZ)
        val modelScale = (maxImageEdge / maxEdgeSize) / 2f
        modelNode.localScale = Vector3(modelScale, modelScale, modelScale)
        modelNode.localScale = Vector3(modelScale, modelScale, modelScale)
        startAnimation()
    }

    private fun startAnimation() {
        if (renderable.animationDataCount == 0) { // if there is no animation then escape
            return
        }
        val animationData = renderable.getAnimationData("Beedrill_Animation")
        ModelAnimator(animationData, renderable).apply {
            repeatCount = ModelAnimator.INFINITE
            start()
        }
    }
}

