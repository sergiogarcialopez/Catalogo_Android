package com.sergiogarcialopez.catalogo.ui.catalogo

import android.app.Activity
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.sergiogarcialopez.catalogo.MainActivity
import com.sergiogarcialopez.catalogo.ui.producto.ProductoFragment

class CatalogoAdapter(
    private val _activity: Activity? = null,
    private val dataSet: Array<String>,
    private val imagenes: Array<String>? = null,
    private val ids: Array<String>? = null,
    private val precios: Array<String>? = null,
    private val unidadesDisponibles: Array<String>? = null,
    private val categorias: Array<String>? = null) :
    RecyclerView.Adapter<CatalogoAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView
        val imageButton: ImageButton

        init {
            // Define click listener for the ViewHolder's View
            textView = view.findViewById<TextView>(com.sergiogarcialopez.catalogo.R.id.text_view)
            imageButton = view.findViewById<ImageButton>(com.sergiogarcialopez.catalogo.R.id.image_button)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CatalogoAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(com.sergiogarcialopez.catalogo.R.layout.recycler_view_item, parent, false)

        return ViewHolder(view)    }

    fun iniciarProductoFragment(position: Int) {
        // CAMBIAR FRAGMENT
        //https://developer.android.com/guide/fragments/fragmentmanager
        var fragmentTransaction = (_activity as MainActivity).supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(
            com.sergiogarcialopez.catalogo.R.id.nav_host_fragment_activity_main,
            ProductoFragment(arrayOf(dataSet[position], imagenes!![position], ids!![position], precios!![position], unidadesDisponibles!![position], categorias!![position]))
        )
        fragmentTransaction.addToBackStack("Modificar producto")
        fragmentTransaction.commit()
        _activity.esFragmentoProductoModificado = true
        // OCULTAR BARRA
        (_activity as MainActivity).binding.navView.visibility = View.GONE
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: CatalogoAdapter.ViewHolder, position: Int) {
        if (imagenes != null){
            val imagen_byte_array = android.util.Base64.decode(imagenes[position], android.util.Base64.DEFAULT)
            //https://stackoverflow.com/questions/7620401/how-to-convert-image-file-data-in-a-byte-array-to-a-bitmap
            val image_bitmap = BitmapFactory.decodeByteArray(imagen_byte_array, 0, imagen_byte_array.size)
            holder.imageButton.setImageBitmap(image_bitmap)//Bitmap.createScaledBitmap(image_bitmap, 200, 200, false))
            holder.imageButton.setOnClickListener {
             iniciarProductoFragment(position)
            }
        }
        holder.textView.text = dataSet[position] + "\n" + if (precios != null) "$${precios[position]}" else ""
        holder.textView.focusable = View.FOCUSABLE
        holder.textView.isClickable = true
        holder.textView.setOnClickListener {
            iniciarProductoFragment(position)
            if (ids != null) {
            }
//            Firebase.firestore.collection("catalogo")
//                .get()
//                .addOnSuccessListener { result ->
//                    for (document in result) {
//                        Log.d("FIRESTORE", "${document.data}")
//                    }
//                }
//                .addOnFailureListener { exception ->
//                    Log.w("FIRESTORE", "$exception")
//                }
        }
    }


    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

}

