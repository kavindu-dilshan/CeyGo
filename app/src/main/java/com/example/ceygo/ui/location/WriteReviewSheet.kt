package com.example.ceygo.ui.location

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import androidx.core.os.bundleOf
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.example.ceygo.R

class WriteReviewSheet : BottomSheetDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        return inflater.inflate(R.layout.sheet_write_review, c, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val id = requireArguments().getString(ARG_ID)!!

        val rating = view.findViewById<RatingBar>(R.id.ratingInput)
        val input  = view.findViewById<TextInputEditText>(R.id.etReview)

        view.findViewById<View>(R.id.btnSend).setOnClickListener {
            val stars = rating.rating.toInt().coerceIn(1, 5)
            val text  = input.text?.toString()?.trim().orEmpty()
            if (text.isEmpty()) {
                input.error = "Please write something"
                return@setOnClickListener
            }
            parentFragmentManager.setFragmentResult(
                RESULT_KEY,
                bundleOf("locationId" to id, "rating" to stars, "text" to text)
            )
            dismiss()
        }
    }

    companion object {
        const val RESULT_KEY = "write_review_result"
        private const val ARG_ID = "id"
        fun newInstance(locationId: String) = WriteReviewSheet().apply {
            arguments = bundleOf(ARG_ID to locationId)
        }
    }
}