package com.sergiogarcialopez.catalogo.ui.producto

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.sergiogarcialopez.catalogo.databinding.FragmentProductoBinding
import java.io.ByteArrayOutputStream
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import androidx.core.widget.addTextChangedListener

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.M)
fun isInternetAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}

class ProductoFragment(
    private val datos: Array<String>? = null
) : Fragment() {
    private var _binding: FragmentProductoBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    lateinit var imagen_default_drawable: android.graphics.drawable.Drawable
    lateinit var imagen_bitmap: Bitmap
    lateinit var last_imagen_bitmap: Bitmap
    val imagen_default_base64: String
        get() {
            val baos_default = ByteArrayOutputStream()
            last_imagen_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos_default)
            return android.util.Base64.encodeToString(
                baos_default.toByteArray(),
                android.util.Base64.DEFAULT
            )
        }
    var imagen_esta_vacia = true
    var producto_id: String? = datos?.get(2)

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val productoViewModel =
            ViewModelProvider(this).get(ProductoViewModel::class.java)

        _binding = FragmentProductoBinding.inflate(inflater, container, false)
        val root: View = binding.root
        //val textView: TextView = binding.textProducto
        productoViewModel.text.observe(viewLifecycleOwner) {
            //textView.text = it
        }

        imagen_default_drawable = binding.imagen.drawable as android.graphics.drawable.Drawable
        //Converting an android.graphics.drawable.Drawable to a Bitmap in Android involves drawing the Drawable onto a Canvas that is backed by a Bitmap.
        //Here is a common method for this conversion: Create a mutable Bitmap.
        imagen_bitmap = createBitmap(
            (imagen_default_drawable as android.graphics.drawable.Drawable).intrinsicWidth,
            (imagen_default_drawable).intrinsicHeight
        )
        val imagen_canvas = Canvas(imagen_bitmap)
        imagen_default_drawable.setBounds(0, 0, imagen_canvas.width, imagen_canvas.height)
        imagen_default_drawable.draw(imagen_canvas)
        imagen_esta_vacia = true
        last_imagen_bitmap = imagen_bitmap.scale(240, 240, false)

        var imagen_base64: String? = null
        val pickMedia = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                // Callback is invoked after the user selects a media item or closes the photo picker.
                if (uri != null) {
                    imagen_esta_vacia = false
                    binding.imagen.setImageURI(uri)
                    //https://stackoverflow.com/questions/3879992/how-to-get-bitmap-from-an-uri
                    var bmp: Bitmap =
                        MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
                    last_imagen_bitmap = bmp.scale(240, 240, false)
                    bmp = bmp.scale(240, 240, false)
                    //https://stackoverflow.com/questions/33210634/android-base64-decoding-encoding-image-to-string
                    val baos = ByteArrayOutputStream()
                    bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                    imagen_base64 = android.util.Base64.encodeToString(
                        baos.toByteArray(),
                        android.util.Base64.DEFAULT
                    )
                } else {
                    imagen_esta_vacia = true
                    binding.imagen.setImageBitmap(imagen_bitmap)
                    Toast.makeText(requireContext(), "Algo salió mal", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            TODO("VERSION.SDK_INT < KITKAT")
        }

        if (datos != null) {
            binding.nombre.setText(datos[0])
            binding.precio.setText(datos[3])
            binding.unidadesDisponibles.setText(datos[4])
            binding.categoria.setText(datos[5])
            val imagen_byte_array =
                android.util.Base64.decode(datos[1], android.util.Base64.DEFAULT)
            val image_bitmap =
                BitmapFactory.decodeByteArray(imagen_byte_array, 0, imagen_byte_array.size)
            binding.imagen.setImageBitmap(image_bitmap)
            last_imagen_bitmap = image_bitmap
            binding.eliminar.visibility = View.VISIBLE
            binding.eliminar.setOnClickListener {
                Firebase.firestore.collection("catalogo").document(datos[2]).delete().addOnSuccessListener {
                    Toast.makeText(context, "Producto eliminado", Toast.LENGTH_LONG).show()
                    binding.guardar.isEnabled = false
                    binding.guardar.visibility = View.GONE
                    binding.eliminar.isEnabled = false
                    binding.eliminar.visibility = View.GONE
                    binding.seleccionarImagen.isEnabled = false
                    binding.seleccionarImagen.visibility = View.GONE
                    binding.nombre.isEnabled = false
                    binding.categoria.isEnabled = false
                    binding.unidadesDisponibles.isEnabled = false
                    binding.precio.isEnabled = false
                }.addOnFailureListener {
                    Toast.makeText(context, "Algo salió mal", Toast.LENGTH_LONG).show()
                }
            }
        }

        binding.seleccionarImagen.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.guardar.setOnClickListener {
            // Es formulario para modificar producto
            if (datos != null) {
                Firebase.firestore.collection("catalogo").document(producto_id!!)
                    .update(
                        "nombre", "${binding.nombre.text}",
                        "precio", "${binding.precio.text}",
                        "unidadesDisponibles", "${binding.unidadesDisponibles.text}",
                        "categoria", "${binding.categoria.text}",
                        "imagen", imagen_default_base64
                    ).addOnSuccessListener {
                        Toast.makeText(context, "Producto modificado con éxito", Toast.LENGTH_LONG).show()
                    }.addOnFailureListener {
                        Toast.makeText(context, "Algo salió mal al modificar el producto", Toast.LENGTH_LONG).show()
                    }
            } else {// Es fragmento producto nuevo
                val hay_campos_vacios = (binding.nombre.text.isEmpty()) ||
                        (imagen_esta_vacia) || (binding.categoria.text.isEmpty()) ||
                        (binding.unidadesDisponibles.text.isEmpty()) ||
                        (binding.precio.text.isEmpty())
                if (hay_campos_vacios) {
                    Toast.makeText(context, "Hay campos vacíos", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
                val producto = hashMapOf(
                    "nombre" to "${binding.nombre.text}",
                    "imagen" to "${imagen_base64}",
                    "categoria" to "${binding.categoria.text}",
                    "precio" to "${binding.precio.text}",
                    "unidadesDisponibles" to "${binding.unidadesDisponibles.text}",
                )
                Firebase.firestore.collection("catalogo").add(producto).addOnSuccessListener {
                    Toast.makeText(context, "Producto nuevo añadido con éxito", Toast.LENGTH_LONG).show()
                    binding.nombre.text.clear()
                    binding.imagen.setImageBitmap(imagen_bitmap)
                    imagen_esta_vacia = true
                    binding.categoria.text.clear()
                    binding.precio.text.clear()
                    binding.unidadesDisponibles.text.clear()
                }.addOnFailureListener {
                    Toast.makeText(context, "Algo salió mal al añadir producto nuevo", Toast.LENGTH_LONG).show()
                }
            }

        }

        if (!isInternetAvailable(requireContext())) {
            binding.guardar.visibility = View.GONE
            binding.eliminar.visibility = View.GONE
            binding.seleccionarImagen.visibility = View.GONE
        }
        return root
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}