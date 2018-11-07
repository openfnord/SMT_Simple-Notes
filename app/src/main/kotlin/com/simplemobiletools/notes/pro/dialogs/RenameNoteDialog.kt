package com.simplemobiletools.notes.pro.dialogs

import android.content.DialogInterface.BUTTON_POSITIVE
import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.notes.pro.R
import com.simplemobiletools.notes.pro.activities.SimpleActivity
import com.simplemobiletools.notes.pro.extensions.notesDB
import com.simplemobiletools.notes.pro.models.Note
import kotlinx.android.synthetic.main.dialog_new_note.view.*
import java.io.File

class RenameNoteDialog(val activity: SimpleActivity, val note: Note, val callback: (note: Note) -> Unit) {

    init {
        val view = activity.layoutInflater.inflate(R.layout.dialog_rename_note, null)
        view.note_name.setText(note.title)

        AlertDialog.Builder(activity)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, null)
                .create().apply {
                    activity.setupDialogStuff(view, this, R.string.rename_note) {
                        showKeyboard(view.note_name)
                        getButton(BUTTON_POSITIVE).setOnClickListener {
                            val title = view.note_name.value
                            Thread {
                                newTitleConfirmed(title, this)
                            }.start()
                        }
                    }
                }
    }

    private fun newTitleConfirmed(title: String, dialog: AlertDialog) {
        when {
            title.isEmpty() -> activity.toast(R.string.no_title)
            activity.notesDB.getNoteIdWithTitle(title) != null -> activity.toast(R.string.title_taken)
            else -> {
                note.title = title
                val path = note.path
                if (path.isEmpty()) {
                    activity.notesDB.insertOrUpdate(note)
                    activity.runOnUiThread {
                        dialog.dismiss()
                        callback(note)
                    }
                } else {
                    if (title.isEmpty()) {
                        activity.toast(R.string.filename_cannot_be_empty)
                        return
                    }

                    val file = File(path)
                    val newFile = File(file.parent, title)
                    if (!newFile.name.isAValidFilename()) {
                        activity.toast(R.string.invalid_name)
                        return
                    }

                    activity.renameFile(file.absolutePath, newFile.absolutePath) {
                        if (it) {
                            note.path = newFile.absolutePath
                            activity.notesDB.insertOrUpdate(note)
                            activity.runOnUiThread {
                                dialog.dismiss()
                                callback(note)
                            }
                        } else {
                            activity.toast(R.string.rename_file_error)
                            return@renameFile
                        }
                    }
                }
            }
        }
    }
}