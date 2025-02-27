package com.botirovka.libraryapp.bookList

import android.content.Context
import android.graphics.Canvas
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.botirovka.libraryapp.R
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator

abstract class SwipeGesture(context: Context) : ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

    val deleteColor = ContextCompat.getColor(context, R.color.red)
    val favoriteColor = ContextCompat.getColor(context, R.color.orange_light)
    val deleteIcon = R.drawable.ic_delete
    val favoriteIcon = R.drawable.ic_borrowed_books

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {

        RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            .addSwipeLeftBackgroundColor(favoriteColor)
            .addSwipeLeftActionIcon(favoriteIcon)
            .addSwipeRightBackgroundColor(deleteColor)
            .addSwipeRightActionIcon(deleteIcon)
            .create()
            .decorate()
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }
}