package com.arunscs.filament3dviewer

import android.opengl.Matrix
import android.os.Bundle
import android.view.Choreographer
import android.view.SurfaceView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.arunscs.filament3dviewer.ui.theme._3dViewerTheme
import com.google.android.filament.utils.KtxLoader
import com.google.android.filament.utils.ModelViewer
import com.google.android.filament.utils.Utils
import java.nio.ByteBuffer

class MainActivity : ComponentActivity() {
    private lateinit var surfaceView: SurfaceView
    private lateinit var choreographer: Choreographer
    private lateinit var modelViewer: ModelViewer
    private lateinit var frameCallback:Choreographer.FrameCallback
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        Utils.init()

        surfaceView = SurfaceView(this).apply { setContentView(this) }
        choreographer = Choreographer.getInstance()
        modelViewer = ModelViewer(surfaceView)
        surfaceView.setOnTouchListener(modelViewer)


        frameCallback = object : Choreographer.FrameCallback {
            override fun doFrame(currentTime: Long) {
                choreographer.postFrameCallback(this)
                modelViewer.render(currentTime)
            }
        }

        loadGlb("Park")
        loadEnvironment("env")
    }

    private fun loadGlb(name:String){
        val buffer = readAsset("models/${name}.glb")
        modelViewer.loadModelGlb(buffer)
        //modelViewer.transformToUnitCube()
        val transformManager = modelViewer.engine.transformManager
        val root = modelViewer.asset?.root ?:0
        val ti = transformManager.getInstance(root)
        val matrix = FloatArray(16)
        Matrix.setIdentityM(matrix,0)
        Matrix.scaleM(matrix,0,0.25f,0.25f,0.25f)
        Matrix.translateM(matrix,0,0f,-1f,-8.0f)
        Matrix.rotateM(matrix, 0, 15f, 0f, 1f, 0f)
        transformManager.setTransform(ti,matrix)

    }

    private fun  loadEnvironment(ibl:String){
        var buffer = readAsset("envs/environment/${ibl}_ibl.ktx")
        KtxLoader.createIndirectLight(modelViewer.engine,buffer).apply {
            intensity = 50_000f
            modelViewer.scene.indirectLight=this
        }

        buffer = readAsset("envs/environment/${ibl}_skybox.ktx")
        KtxLoader.createSkybox(modelViewer.engine,buffer).apply {
            modelViewer.scene.skybox = this
        }
    }

    private fun readAsset(assetName:String):ByteBuffer{
        val input = assets.open(assetName)
        val bytes = ByteArray(input.available())
        input.read(bytes)
        return ByteBuffer.wrap(bytes)
    }

    override fun onResume() {
        super.onResume()
        choreographer.postFrameCallback(frameCallback)
    }

    override fun onPause() {
        super.onPause()
        choreographer.removeFrameCallback(frameCallback)
    }

    override fun onDestroy() {
        super.onDestroy()
        choreographer.removeFrameCallback(frameCallback)
    }
}