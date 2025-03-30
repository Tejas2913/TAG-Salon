package com.tejgokabhi.salonbooking.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.tejgokabhi.salonbooking.R
import com.tejgokabhi.salonbooking.adapter.ProductAdapter
import com.tejgokabhi.salonbooking.databinding.FragmentFavoritesBinding
import com.tejgokabhi.salonbooking.model.ProductModel
import com.tejgokabhi.salonbooking.utils.Constants
import com.tejgokabhi.salonbooking.utils.Utils


class FavoritesFragment : Fragment(), ProductAdapter.OnItemClickListener {
    private val binding by lazy { FragmentFavoritesBinding.inflate(layoutInflater) }
    private lateinit var database: FirebaseDatabase
    private lateinit var favoriteList: ArrayList<ProductModel>
    private lateinit var adapter: ProductAdapter
    private var userId = ""


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = FirebaseDatabase.getInstance()
        favoriteList = ArrayList()
        adapter = ProductAdapter(favoriteList, this@FavoritesFragment, requireContext())
        userId = Firebase.auth.currentUser!!.uid

        binding.btBack.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_favorites_to_navigation_home)
        }

        loadFavorites()

    }

    private fun loadFavorites() {
        database.reference.child(Constants.FAVORITE_REF).child(userId).addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                favoriteList.clear()
                for (snapshot in dataSnapshot.children) {
                    val fav = snapshot.getValue(ProductModel::class.java)
                    if (fav != null) {
                        favoriteList.add(fav)
                    } else {
                        Log.e("Error", "Failed to convert data: ")
                    }
                }

                if(favoriteList.isNotEmpty()) {
                    binding.favoriteRv.visibility = View.VISIBLE
                    binding.tvFavStatus.visibility = View.GONE
                    adapter.submitList(favoriteList)
                    binding.favoriteRv.adapter = adapter
                } else {
                    binding.favoriteRv.visibility = View.GONE
                    binding.tvFavStatus.visibility = View.VISIBLE
                    val status = "No Favorites"
                    binding.tvFavStatus.text = status
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {
                Utils.showMessage(requireContext(), "Failed")
            }
        })
    }

    override fun onItemClick(
        productModel: ProductModel,
        favBtn: ImageView,
        favList: ArrayList<ProductModel>
    ) {
        manageFavorite(productModel, favoriteList, favBtn)
    }

    private fun manageFavorite(
        product: ProductModel,
        favList: ArrayList<ProductModel>,
        favBtn: ImageView
    ) {
        val databaseReference = database.reference.child(Constants.FAVORITE_REF).child(userId).child(product.productId)


        val matchFound = favList.any { it.productId == product.productId }
        favBtn.setImageResource(if (matchFound) R.drawable.ic_fav_filled_red else R.drawable.ic_fav_outline)

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    databaseReference.removeValue()
                        .addOnSuccessListener {
                            favBtn.setImageResource(R.drawable.ic_fav_outline)

                        }
                        .addOnFailureListener {
                            Utils.showMessage(requireContext(), "Something went wrong")
                        }
                } else {
                    databaseReference.setValue(product)
                        .addOnSuccessListener {
                            favBtn.setImageResource(R.drawable.ic_fav_filled_red)

                        }
                        .addOnFailureListener {
                            Utils.showMessage(requireContext(), "Something went wrong")
                        }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
    }

}