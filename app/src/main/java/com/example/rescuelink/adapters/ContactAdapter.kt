package com.example.rescuelink.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.rescuelink.R
import com.example.rescuelink.models.Contact

class ContactAdapter(
    private val contacts: MutableList<Contact>,
    private val onDelete: (Contact) -> Unit
) : RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

    inner class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvContactName)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_contact, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contacts[position]
        holder.tvName.text = contact.name

        holder.btnDelete.setOnClickListener {
            onDelete(contact)
        }
    }

    override fun getItemCount(): Int = contacts.size

    fun addContact(contact: Contact) {
        contacts.add(contact)
        notifyItemInserted(contacts.size - 1)
    }

    fun removeContact(contact: Contact) {
        val index = contacts.indexOf(contact)
        if (index != -1) {
            contacts.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    fun getContacts(): List<Contact> = contacts

}
