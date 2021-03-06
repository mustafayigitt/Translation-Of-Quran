package com.ramo.quran.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import com.ramo.quran.R
import com.ramo.quran.dataAccess.LocalSqliteHelper
import com.ramo.quran.dataAccess.abstr.SqliteResponse
import com.ramo.quran.helper.showError
import com.ramo.quran.helper.showSuccess
import com.ramo.quran.model.Config
import com.ramo.quran.model.Language
import com.ramo.quran.ui.MainActivity
import kotlinx.android.synthetic.main.fragment_settings.*

class SettingFragment: Fragment() {

    private lateinit var db: LocalSqliteHelper
    private lateinit var languageList: List<Language>
    private lateinit var config: Config

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let {
            db = LocalSqliteHelper(it)
        }
        initUi()
    }

    private fun initUi() {
        (activity as? MainActivity)?.supportActionBar?.title = getString(R.string.settings)

        buttonSave.setOnClickListener { onSaveClick() }

        db.getAllLanguages(object :SqliteResponse<List<Language>>{
            override fun onSuccess(response: List<Language>) {
                languageList = response
                activity?.let {
                    val arrayAdapter = ArrayAdapter(it,android.R.layout.simple_spinner_dropdown_item,
                        response.map { it.name }.toTypedArray()
                    )
                    spinnerLanguage.adapter = arrayAdapter
                }
            }

            override fun onFail(failMessage: String) {
                activity?.showError()
            }
        })

        db.getAllConfig(object: SqliteResponse<Config>{
            override fun onSuccess(response: Config) {
                spinnerSetSelected(spinnerTextSize, response.textSize.toString())
                spinnerSetSelected(spinnerLanguage, response.language.name)
            }
            override fun onFail(failMessage: String) {
                activity?.showError()
            }
        })

    }

    private fun spinnerSetSelected(spinner: Spinner, item:String){
        for (i in 0 until spinner.count) {
            if (spinner.getItemAtPosition(i).equals(item)) {
                spinner.setSelection(i)
                break
            }
        }
    }

    private fun onSaveClick() {
        val config = Config (
            textSize = spinnerTextSize.selectedItem.toString().toInt(),
            language = languageList[spinnerLanguage.selectedItemPosition]
        )

        db.updateConfig(config)
        activity?.let {
            it.showSuccess()
            (it as MainActivity).recreate()
        }
    }
}