package com.example.lab4notesreminderapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MyAdapter(
    private val items: MutableList<Note>,
    private val onItemClick: (note: Note, position: Int) -> Unit,
    private val onItemLongClick: (note: Note, position: Int) -> Unit
) : RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvContent: TextView = view.findViewById(R.id.tvContent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_note, parent, false)
        return MyViewHolder(v)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val note = items[position]
        holder.tvTitle.text = note.title
        holder.tvContent.text = note.content

        holder.itemView.setOnClickListener {
            onItemClick(note, position)
        }

        holder.itemView.setOnLongClickListener {
            onItemLongClick(note, position)
            true
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateList(newList: List<Note>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }

    fun removeAt(position: Int) {
        items.removeAt(position)
        notifyItemRemoved(position)
    }

    fun insertAt(position: Int, note: Note) {
        items.add(position, note)
        notifyItemInserted(position)
    }
}