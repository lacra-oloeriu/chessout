package eu.chessout.v2.ui.club.joinclubbyqrcode

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.util.isNotEmpty
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import eu.chessout.shared.Constants
import eu.chessout.v2.R
import eu.chessout.v2.util.ImageUtil
import kotlinx.android.synthetic.main.join_club_by_qr_code_fragment.*

class JoinClubByQrCodeFragment : Fragment() {

    companion object {
        fun newInstance() = JoinClubByQrCodeFragment()
    }

    private val viewModel: JoinClubByQrCodeViewModel by viewModels()
    private lateinit var cameraSource: CameraSource
    private lateinit var detector: BarcodeDetector

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.join_club_by_qr_code_fragment, container, false)
        viewModel.initModel(requireActivity().getPreferences(Context.MODE_PRIVATE).edit())
        viewModel.clubLocated.observe(viewLifecycleOwner, Observer {
            if (it) {
                Toast.makeText(
                    requireContext(), "Club successfully set as default club",
                    Toast.LENGTH_SHORT
                ).show()
                requireActivity().onBackPressed()
            }
        })
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (ImageUtil.askForCameraPermissions(requireActivity())) {
            setupControls()
        } else {
            textScanResult.text = "No permissions available yet. Please go back and try again :)"
        }
    }


    private fun setupControls() {
        Log.d(Constants.LOG_TAG, "Setting up controls")
        detector = BarcodeDetector.Builder(requireContext()).build()
        cameraSource = CameraSource.Builder(requireContext(), detector)
            .setAutoFocusEnabled(true)
            .build()
        cameraSurfaceView.holder.addCallback(surfaceCallBack)
        detector.setProcessor(processor)
    }

    private val surfaceCallBack = object : SurfaceHolder.Callback {
        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            // nothing to implement
        }

        override fun surfaceDestroyed(holder: SurfaceHolder) {
            cameraSource.stop()
        }

        @SuppressLint("MissingPermission")
        override fun surfaceCreated(holder: SurfaceHolder) {
            try {
                cameraSource.start(holder)
            } catch (exception: Exception) {
                Toast.makeText(
                    requireContext(), "Camera problems ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private val processor = object : Detector.Processor<Barcode> {
        override fun release() {
        }

        override fun receiveDetections(detections: Detector.Detections<Barcode>?) {
            if (detections != null && detections.detectedItems.isNotEmpty()) {
                val qrCodes: SparseArray<Barcode> = detections.detectedItems
                val code = qrCodes.valueAt(0)
                textScanResult.text = code.displayValue
                viewModel.checkClub(code.displayValue)
            } else {
                textScanResult.text = ""
            }
        }
    }
}
