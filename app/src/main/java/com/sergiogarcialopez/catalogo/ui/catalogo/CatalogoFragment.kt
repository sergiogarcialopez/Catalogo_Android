package com.sergiogarcialopez.catalogo.ui.catalogo

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.sergiogarcialopez.catalogo.databinding.FragmentCatalogoBinding

data class Producto(
    val id: String,
    val nombre: String,
    val imagen: String,
    val precio: String,
    val unidadesDisponibles: String,
    val categoria: String
)

class CatalogoFragment : Fragment() {
    private var _binding: FragmentCatalogoBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val catalogoViewModel =
            ViewModelProvider(this).get(CatalogoViewModel::class.java)

        _binding = FragmentCatalogoBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.recyclerViewCatalogo.layoutManager = LinearLayoutManager(context)
        var datos = arrayOf("Cargando...") // i.e. Nombre
        var imagenes = arrayOf<String>()
        var ids = arrayOf<String>()
        var precios = arrayOf<String>()
        var unidadesDisponibles = arrayOf<String>()
        var categorias = arrayOf<String>()
        try {
            Firebase.firestore.collection("catalogo").get().addOnSuccessListener { result ->
                var _datos = arrayListOf<String>()
                var _imagenes = arrayListOf<String>()
                var _ids = arrayListOf<String>()
                var _precios = arrayListOf<String>()
                var _unidadesDisponibles = arrayListOf<String>()
                var _categorias = arrayListOf<String>()
                for (document in result) {
                    _datos.add("${document.data.get("nombre")}")
                    _imagenes.add("${document.data.get("imagen")}")
                    _ids.add("${document.id}")
                    _precios.add("${document.data.get("precio")}")
                    _unidadesDisponibles.add("${document.data.get("unidadesDisponibles")}")
                    _categorias.add("${document.data.get("categoria")}")
                }
                datos = _datos.toTypedArray()
                imagenes = _imagenes.toTypedArray()
                ids = _ids.toTypedArray()
                precios = _precios.toTypedArray()
                unidadesDisponibles = _unidadesDisponibles.toTypedArray()
                categorias = _categorias.toTypedArray()
                binding.recyclerViewCatalogo.adapter = CatalogoAdapter(
                    activity,
                    datos,
                    imagenes,
                    ids,
                    precios,
                    unidadesDisponibles,
                    categorias
                )
            }.addOnFailureListener {
                datos = arrayOf("Algo salió mal")
                binding.recyclerViewCatalogo.adapter = CatalogoAdapter(dataSet = datos)
            }
        } catch (e: Exception) {
        }
        binding.recyclerViewCatalogo.adapter = CatalogoAdapter(dataSet = datos)

        //val textView: TextView = binding.textCatalogo
        catalogoViewModel.text.observe(viewLifecycleOwner) {
            //textView.text = it
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}