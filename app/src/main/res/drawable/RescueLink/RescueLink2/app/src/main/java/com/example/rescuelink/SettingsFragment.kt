package com.example.rescuelink

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import java.util.Locale


class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private lateinit var spinner: Spinner
    private lateinit var saveButton: Button
    private lateinit var selectedLanguageCode: String

    val languagesMap = mapOf(
        "English" to "en",
        "हिंदी (Hindi)" to "hi",
        "తెలుగు (Telugu)" to "te",
        "ਪੰਜਾਬੀ (Punjabi)" to "pa",
        "Español (Spanish)" to "es",
        "Français (French)" to "fr",
        "Deutsch (German)" to "de",
        "中文 (Chinese)" to "zh",
        "日本語 (Japanese)" to "ja",
        "русский (Russian)" to "ru",
    )


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        spinner = view.findViewById(R.id.spinner_language)
        saveButton = view.findViewById(R.id.btn_save_language)

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, languagesMap.keys.toList())
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        val currentLangCode = getSavedLanguageCode()
        val selectedIndex = languagesMap.values.indexOf(currentLangCode)
        if (selectedIndex >= 0) spinner.setSelection(selectedIndex)

        saveButton.setOnClickListener {
            val selectedLang = spinner.selectedItem.toString()
            selectedLanguageCode = languagesMap[selectedLang] ?: "en"
            saveLanguageCode(selectedLanguageCode)
            setAppLocale(selectedLanguageCode)
            Toast.makeText(requireContext(), "Language changed to $selectedLang", Toast.LENGTH_SHORT).show()
            requireActivity().recreate()
        }
    }

    private fun getSavedLanguageCode(): String {
        val prefs = requireContext().getSharedPreferences("settings_pref", Context.MODE_PRIVATE)
        return prefs.getString("app_language", "en") ?: "en"
    }

    private fun saveLanguageCode(langCode: String) {
        val prefs = requireContext().getSharedPreferences("settings_pref", Context.MODE_PRIVATE)
        prefs.edit().putString("app_language", langCode).apply()
    }

    private fun setAppLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}
