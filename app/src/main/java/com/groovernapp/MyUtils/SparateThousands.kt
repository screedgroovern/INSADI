package com.groovernapp.MyUtils

import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import java.text.DecimalFormat

class SparateThousands(ed: EditText, val Bwithcurrency: Boolean, val Scurrency: String = "Rp ",
                       val currside: String = "START", val Ballowdectype: Boolean = false): TextWatcher {
    private val editText = ed
    private var cursorPosition = 0
    private var txtbefore = ""

    override fun afterTextChanged(s: Editable?) {
        try {
            editText.removeTextChangedListener(this)
            var value = editText.text.toString().let {edstr ->
                if (edstr.count { decimalMarker.contains(it) } > 1){
                    txtbefore
                }else {
                    getOriginalString(edstr, Ballowdectype)
                }
            }
            if (value != "") {
                if (value.startsWith(decimalMarker)) {
                    value = "0$decimalMarker"
                }
                if (value.startsWith("0") && !value.startsWith("0$decimalMarker")) {
                    var index = 0
                    while (index < value.length && value[index] == '0') {
                        index++
                    }
                    var newValue = value[0].toString()
                    if (index != 0) {
                        newValue = value[0].toString() + value.substring(index)
                    }
                    value = newValue
                }
                Log.e(javaClass.simpleName, "finalvalue : $value")
                editText.setText(getDecimalFormattedString(value, Bwithcurrency, Scurrency, currside))
                editText.setSelection(editText.text.toString().length)
            }else {
                editText.setText("")
            }
            //setting the cursor back to where it was
            editText.setSelection(editText.text.toString().length - cursorPosition)
            editText.addTextChangedListener(this)
        } catch (ex: Exception) {
            ex.printStackTrace()
            editText.addTextChangedListener(this)
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        txtbefore = getOriginalString(s.toString(), Ballowdectype)
        if (currside == "START" && editText.selectionStart in 1..Scurrency.length){
            editText.setSelection(Scurrency.length)
        }else if (currside == "END" && editText.selectionStart in editText.length()..editText.length()+Scurrency.length){
            editText.setSelection(s!!.filter { it.isDigit() }.length)
        }
        cursorPosition = editText.text.toString().length - editText.selectionStart
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    companion object{
        private val thousandSeparator = "."//df.getDecimalFormatSymbols().getGroupingSeparator().toString()
        private val decimalMarker = ","//df.getDecimalFormatSymbols().getDecimalSeparator().toString()

        fun getDecimalFormattedString(int: Int, bwithcurrency: Boolean = false, scurrency: String = "Rp ",
                                      currside: String = "START"): String =
            getDecimalFormattedString(int.toString(), bwithcurrency, scurrency, currside)

        fun getDecimalFormattedString(value: String, bwithcurrency: Boolean = false, scurrency: String = "Rp ",
                                      currside: String = "START"): String {
            val splitValue = value.split(decimalMarker).toTypedArray()
            var beforeDecimal = value.replace("-", "")
            var afterDecimal: String? = null
            var finalResult = ""
            if (splitValue.size == 2) {
                beforeDecimal = splitValue[0]
                afterDecimal = splitValue[1]
            }

            val minussymbol = try {
                if (value.toInt() < 0) "- "
                else ""
            }catch (e: Exception){
                Log.e(this::class.java.simpleName, "not integer exception -> $e")
                ""
            }
            var count = 0
            for (i in beforeDecimal.length - 1 downTo 0) {
                finalResult = beforeDecimal[i].toString() + finalResult
                count++
                if (count == 3 && i > 0) {
                    finalResult = thousandSeparator + finalResult
                    count = 0
                }
            }
            if (afterDecimal != null) {
                finalResult = finalResult + decimalMarker + afterDecimal
            }
            return if (bwithcurrency){
                when (currside){
                    "START" -> "$minussymbol$scurrency$finalResult"
                    "END" -> "$minussymbol$finalResult$scurrency"
                    else -> "$minussymbol$scurrency$finalResult"
                }
            } else "$minussymbol$finalResult"
        }

        /* Returns the string after removing all the thousands separators. */
        fun getOriginalString(string: String, ballowdectype: Boolean = false): String {
            return if (ballowdectype) string.filter { it.isDigit() || it == ','} else string.filter { it.isDigit() }
        }
    }

}