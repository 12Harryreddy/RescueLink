package com.example.rescuelink

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rescuelink.adapters.ContactAdapter
import com.example.rescuelink.models.Contact

class ContactsFragment : Fragment() {
    companion object {
        private const val PREF_NAME = "contacts_pref"
        private const val KEY_CONTACTS = "contact_numbers"
    }

    private lateinit var contactAdapter: ContactAdapter
    private val contactList = mutableListOf<Contact>()

    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout
        val view = inflater.inflate(R.layout.fragment_contacts, container, false)


        // Set up the adapter
        val prefs = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val savedNumbers = prefs.getStringSet(KEY_CONTACTS, emptySet()) ?: emptySet()
        contactList.clear()
        contactList.addAll(savedNumbers.map { Contact(it, it) })

        contactAdapter = ContactAdapter(contactList) { contactToDelete ->
            deleteContact(contactToDelete)
        }

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.alertsRecycleView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = contactAdapter

        // Set up add button
        val btnAdd = view.findViewById<Button>(R.id.btn_add_contact)
        val editContact = view.findViewById<EditText>(R.id.edit_contact)

        btnAdd.setOnClickListener {
            val phone = editContact.text.toString().trim()
            if (phone.isNotEmpty()) {
                val contact = Contact(phone, phone)

                if (contactList.any { it.phone == phone }) {
                    Toast.makeText(requireContext(), "Contact already exists", Toast.LENGTH_SHORT)
                        .show()
                    return@setOnClickListener
                }

                contactAdapter.addContact(contact) // This should internally update contactList

                editContact.text.clear()

                val prefs = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                val editor = prefs.edit()
                val updatedSet = contactAdapter.getContacts().map { it.phone }.toSet()
                editor.putStringSet(KEY_CONTACTS, updatedSet)
                editor.apply()

            } else {
                Toast.makeText(requireContext(), "Please enter a number", Toast.LENGTH_SHORT).show()
            }
        }



        return view
    }

    private fun deleteContact(contact: Contact) {
        contactAdapter.removeContact(contact)
        val prefs = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        val updatedSet = contactAdapter.getContacts().map { it.phone }.toSet()
        editor.putStringSet(KEY_CONTACTS, updatedSet)
        editor.apply()

        Toast.makeText(requireContext(), "${contact.name} deleted", Toast.LENGTH_SHORT).show()
    }
}
