package com.trada.app.utils

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

// 🎨 Visual formatter to match format: X XX XX XX XX (e.g., 6 12 34 56 78)
class PhoneSpacingTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text.filter { it.isDigit() }
        var formatted = ""
        for (i in digits.indices) {
            formatted += digits[i]
            if (i % 2 == 0 && i != digits.lastIndex) formatted += " "
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 0) return 0
                val validOffset = offset.coerceAtMost(digits.length)
                val spaces = validOffset / 2
                return (validOffset + spaces).coerceAtMost(formatted.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 0) return 0
                val validOffset = offset.coerceAtMost(formatted.length)
                val spaces = (validOffset + 1) / 3
                return (validOffset - spaces).coerceAtMost(text.text.length)
            }
        }
        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}