package org.southasia.ghru.ui.camera


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import io.fotoapparat.Fotoapparat
import io.fotoapparat.error.CameraErrorListener
import io.fotoapparat.exception.camera.CameraException
import io.fotoapparat.facedetector.processor.FaceDetectorProcessor
import io.fotoapparat.log.logcat
import io.fotoapparat.log.loggers
import io.fotoapparat.parameter.ScaleType
import io.fotoapparat.preview.Frame
import io.fotoapparat.preview.FrameProcessor
import io.fotoapparat.result.BitmapPhoto
import io.fotoapparat.result.WhenDoneListener
import io.fotoapparat.result.transformer.scaled
import io.fotoapparat.selector.back
import org.jetbrains.annotations.NotNull
import org.southasia.ghru.R
import org.southasia.ghru.binding.FragmentDataBindingComponent
import org.southasia.ghru.databinding.CameraFragmentBinding
import org.southasia.ghru.di.Injectable
import org.southasia.ghru.event.BitmapRxBus
import org.southasia.ghru.util.hideKeyboard
import org.southasia.ghru.util.singleClick
import org.southasia.ghru.vo.SavedBitMap
import java.io.File
import javax.inject.Inject


class CameraFragment : Fragment(), Injectable {


    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var binding: CameraFragmentBinding

    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)

    private var fotoapparat: Fotoapparat? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dataBinding = DataBindingUtil.inflate<CameraFragmentBinding>(
            inflater,
            R.layout.camera_fragment,
            container,
            false
        )
        binding = dataBinding
        setHasOptionsMenu(true)
        val appCompatActivity = requireActivity() as AppCompatActivity
        appCompatActivity.setSupportActionBar(binding.detailToolbar)
        appCompatActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.root.hideKeyboard()
        return dataBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fotoapparat = createFotoapparat();

        binding.cameraView.visibility = View.VISIBLE

        binding.switchCamera.singleClick({

            val photoResult = fotoapparat?.autoFocus()?.takePicture()

            val tsLong = System.currentTimeMillis() / 1000
            val ts = tsLong.toString()

            photoResult?.saveToFile(
                File(
                    activity?.getExternalFilesDir("photos"),
                    "$ts.jpg"
                )
            )

            photoResult
                ?.toBitmap(scaled(0.1f))
                ?.whenDone(object : WhenDoneListener<BitmapPhoto> {
                    override fun whenDone(@Nullable bitmapPhoto: BitmapPhoto?) {
                        if (bitmapPhoto == null) {
                            Log.e("bitmapPhoto", "Couldn't capture photo.")
                            return
                        }
                        var path = activity?.getExternalFilesDir("photos")?.absolutePath
                        path = "$path/$ts.jpg"
                        val image: SavedBitMap = SavedBitMap(bitmapPhoto, path)
                        Log.d("Path ", path)

                        BitmapRxBus.getInstance().post(image)
                        navController().popBackStack()
                    }
                })

        })
    }

    private fun createFotoapparat(): Fotoapparat {
        return Fotoapparat
            .with(activity!!)
            .into(binding.cameraView)
            .focusView(binding.focusView)
            .previewScaleType(ScaleType.CenterCrop)
            .lensPosition(back())
            .frameProcessor(SampleFrameProcessor())
            .logger(
                loggers(
                    logcat()
                )
            )
            .cameraErrorCallback(object : CameraErrorListener {
                override fun onError(e: CameraException) {
                    //  Toast.makeText(this@ActivityJava, e.toString(), Toast.LENGTH_LONG).show()
                }
            })
            .build()
    }


    private inner class SampleFrameProcessor : FrameProcessor {
        override fun process(@NotNull frame: Frame) {
            FaceDetectorProcessor.with(activity)
                .listener { faces ->
                    Log.d("&&&", "Detected faces: " + faces.size)
                    // binding.rectanglesView.setRectangles(faces)
                }
                .build()
        }
    }


    override fun onStart() {
        super.onStart()
        fotoapparat?.start()

    }

    override fun onStop() {
        super.onStop()
        fotoapparat?.stop()

    }

    fun navController() = findNavController()
}