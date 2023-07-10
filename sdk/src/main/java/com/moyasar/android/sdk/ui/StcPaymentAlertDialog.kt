package com.moyasar.android.sdk.ui

import android.R
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment


class StcPaymentAlertDialog : DialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .setMessage(arguments?.getString("message"))
            .setTitle(arguments?.getString("title"))
            .setPositiveButton(getString(R.string.ok)) { _, _ -> }
            .create()

    override fun onPause() {
        super.onPause()

    }

    companion object {
        const val TAG = "PaymentAlertDialog"
    }

}


