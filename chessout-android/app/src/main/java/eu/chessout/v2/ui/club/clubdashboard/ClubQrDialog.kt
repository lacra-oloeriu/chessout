package eu.chessout.v2.ui.club.clubdashboard

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.BarcodeEncoder
import eu.chessout.v2.R
import eu.chessout.v2.util.GlideApp

class ClubQrDialog(val mView: View, val clubId: String) : DialogFragment() {

    // private lateinit var mView: View

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)
        val qrImage = mView.findViewById<ImageView>(R.id.qrImage)
        val builder = AlertDialog.Builder(requireContext())
        builder.setView(mView)
        builder.setMessage("Show this code to someone interested to join this club")
        builder.setPositiveButton("OK") { _, _ ->
            dismiss()
        }

        // qr code section
        val multiFormatWriter = MultiFormatWriter()
        val bitMatrix = multiFormatWriter.encode(
            clubId,
            BarcodeFormat.QR_CODE, 200, 200
        )
        val encoder = BarcodeEncoder()
        val bitMap = encoder.createBitmap(bitMatrix)

        GlideApp.with(requireActivity())
            .load(bitMap)
            .into(qrImage)



        builder.setPositiveButton("OK") { _, _ -> dismiss() }
        return builder.create()
    }
}