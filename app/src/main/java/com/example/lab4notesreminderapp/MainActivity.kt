package com.example.lab4notesreminderapp

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    //Mainactivity
    private lateinit var db: NotesDatabase
    private lateinit var adapter: MyAdapter
    private val notes = mutableListOf<Note>()

    private lateinit var edtTitle: EditText
    private lateinit var edtContent: EditText
    private lateinit var btnAdd: Button
    private lateinit var btnStartService: Button
    private lateinit var rvNotes: RecyclerView

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (!granted) {
                Toast.makeText(this, "Notification permission denied!", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = NotesDatabase.getDatabase(this)

        edtTitle = findViewById(R.id.edtTitle)
        edtContent = findViewById(R.id.edtContent)
        btnAdd = findViewById(R.id.btnAdd)
        btnStartService = findViewById(R.id.btnStartService)
        rvNotes = findViewById(R.id.rvNotes)

        adapter = MyAdapter(
            notes,
            onItemClick = { note, pos -> showEditDialog(note, pos) },
            onItemLongClick = { note, pos -> deleteNoteWithUndo(note, pos) }
        )

        rvNotes.layoutManager = LinearLayoutManager(this)
        rvNotes.adapter = adapter

        loadNotesFromDb()

        btnAdd.setOnClickListener {
            val title = edtTitle.text.toString().trim()
            val content = edtContent.text.toString().trim()
            if (title.isEmpty() && content.isEmpty()) {
                Toast.makeText(this, "Enter title or content", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val note = Note(title = title, content = content)
            addNoteToDb(note)
        }

        btnStartService.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

                val permission = android.Manifest.permission.POST_NOTIFICATIONS

                when {
                    checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED -> {
                        startService(Intent(this, ReminderService::class.java))
                        Toast.makeText(this, "Reminder service started", Toast.LENGTH_SHORT).show()
                    }
                    shouldShowRequestPermissionRationale(permission) -> {
                        requestNotificationPermission.launch(permission)
                    }
                    else -> {
                        requestNotificationPermission.launch(permission)
                    }
                }

            } else {
                startService(Intent(this, ReminderService::class.java))
                Toast.makeText(this, "Reminder service started", Toast.LENGTH_SHORT).show()
            }


        }
    }

    private fun loadNotesFromDb() {
        lifecycleScope.launch {
            val list = withContext(Dispatchers.IO) { db.noteDao().getAllNotes() }
            notes.clear()
            notes.addAll(list)
            adapter.notifyDataSetChanged()
        }
    }

    private fun addNoteToDb(note: Note) {
        lifecycleScope.launch {
            val id = withContext(Dispatchers.IO) { db.noteDao().insert(note) }
            // update id from DB
            note.id = id.toInt()
            notes.add(0, note) // show newest on top
            adapter.notifyItemInserted(0)
            rvNotes.scrollToPosition(0)
            edtTitle.text.clear()
            edtContent.text.clear()
            Toast.makeText(this@MainActivity, "Note added", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteNoteWithUndo(note: Note, position: Int) {
        // remove locally & database
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                db.noteDao().delete(note)
            }
            val removedPos = position
            val removedNote = note
            notes.removeAt(removedPos)
            adapter.notifyItemRemoved(removedPos)

            val snack = Snackbar.make(rvNotes, "Note deleted", Snackbar.LENGTH_LONG)
            snack.setAction("UNDO") {
                // re-insert
                lifecycleScope.launch {
                    val newId = withContext(Dispatchers.IO) { db.noteDao().insert(removedNote) }
                    removedNote.id = newId.toInt()
                    notes.add(removedPos, removedNote)
                    adapter.notifyItemInserted(removedPos)
                }
            }
            snack.show()
        }
    }

    private fun showEditDialog(note: Note, position: Int) {
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.dialog_edit_note, null)
        val etTitle = view.findViewById<EditText>(R.id.etDialogTitle)
        val etContent = view.findViewById<EditText>(R.id.etDialogContent)
        etTitle.setText(note.title)
        etContent.setText(note.content)
        builder.setView(view)
            .setTitle("Edit Note")
            .setPositiveButton("Save") { dialog, _ ->
                val newTitle = etTitle.text.toString().trim()
                val newContent = etContent.text.toString().trim()
                note.title = newTitle
                note.content = newContent
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        db.noteDao().update(note)
                    }
                    notes[position] = note
                    adapter.notifyItemChanged(position)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { d, _ -> d.dismiss() }
            .show()
    }


}