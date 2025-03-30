package com.tejgokabhi.salonbooking.activities

import android.content.Intent
import android.graphics.Paint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.tejgokabhi.salonbooking.R
import com.tejgokabhi.salonbooking.adapter.ProductImageAdapter
import com.tejgokabhi.salonbooking.adapter.SimilarProductAdapter
import com.tejgokabhi.salonbooking.adapter.SliderAdapter
import com.tejgokabhi.salonbooking.databinding.ActivityDetailsBinding
import com.tejgokabhi.salonbooking.model.ProductImageUrlModel
import com.tejgokabhi.salonbooking.model.ProductModel
import com.tejgokabhi.salonbooking.model.SliderModel
import com.tejgokabhi.salonbooking.utils.Constants
import com.tejgokabhi.salonbooking.utils.Utils

class DetailsActivity : AppCompatActivity(),SliderAdapter.OnItemClickListener, SimilarProductAdapter.OnItemClickListener, ProductImageAdapter.OnItemClickListener {
    private val binding by lazy { ActivityDetailsBinding.inflate(layoutInflater) }
    private lateinit var adapter: ProductImageAdapter
    private lateinit var similarAdapter: SimilarProductAdapter
    private lateinit var database: FirebaseDatabase
    private lateinit var favoriteList: ArrayList<ProductModel>
    private var productData: ProductModel? = null
    private lateinit var viewPager2: ViewPager2
    private lateinit var pageChangeListener: ViewPager2.OnPageChangeCallback
    private lateinit var sliderList: ArrayList<SliderModel>

    private val params = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.WRAP_CONTENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
    ).apply {
        setMargins(0, 0, 8, 0)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val product = intent.getStringExtra(Constants.PRODUCTS_REF)!!
        database = FirebaseDatabase.getInstance()
        adapter = ProductImageAdapter(this)
        productData = ProductModel()
        favoriteList = ArrayList()
        similarAdapter = SimilarProductAdapter(favoriteList, this@DetailsActivity)
        viewPager2 = binding.viewPager2
        database = FirebaseDatabase.getInstance()
        pageChangeListener = object : ViewPager2.OnPageChangeCallback() {}
        sliderList = ArrayList()


        getProductData(product)


        binding.apply {
            btDetaisBack.setOnClickListener {
                startActivity(Intent(this@DetailsActivity, HomeMainActivity::class.java))
                finish()
            }

            btDetailFavorite.setOnClickListener {
                if(productData != null) {
                    manageFavorite(productData!!, favoriteList, btDetailFavorite)
                } else {
                    Utils.showMessage(this@DetailsActivity, "Something went wrong")
                }
            }

            btShare.setOnClickListener {
                if(productData != null) {
                    val details = " ${productData!!.title} at price 0f ${productData!!.price}\nI found this product on ${getString(R.string.app_name)}"
                    Utils.shareText(this@DetailsActivity, details)
                } else {
                    Utils.showMessage(this@DetailsActivity, "Something went wrong")
                }

            }

            btBook.setOnClickListener {
                if(productData != null) {
                    val intent = Intent(this@DetailsActivity, BookingActivity::class.java)
                    intent.putExtra(Constants.PRODUCTS_REF, productData!!.productId)
                    intent.putExtra(Constants.PRICE, productData!!.fullPrice)
                    intent.putExtra(Constants.SERVICE, productData!!.title)
                    startActivity(intent)
                    finish()
                } else {
                    Utils.showMessage(this@DetailsActivity, "Something went wrong")
                }

            }
        }

    }
    override fun onDestroy() {
        super.onDestroy()
        viewPager2.unregisterOnPageChangeCallback(pageChangeListener)
    }

    private fun createSlider() {
        val sliderAdapter = SliderAdapter(this@DetailsActivity)
        viewPager2.adapter = sliderAdapter

        val dotImage = Array(sliderList.size) { ImageView(this@DetailsActivity) }

        binding.linearLay.removeAllViews()

        binding.card.visibility = View.VISIBLE
        dotImage.forEach {
            it.setImageResource(R.drawable.slide_unselected)
            binding.linearLay.addView(it, params)
        }

        dotImage[0].setImageResource(R.drawable.slide_selected)
        sliderAdapter.submitList(sliderList)

        val pageChangeListener = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                dotImage.mapIndexed { index, imageView ->
                    imageView.setImageResource(
                        if (position == index) R.drawable.slide_selected else R.drawable.slide_unselected
                    )
                }
                super.onPageSelected(position)
            }
        }
        viewPager2.registerOnPageChangeCallback(pageChangeListener)

        val handler = Handler(Looper.getMainLooper())


        val runnable = object : Runnable {
            override fun run() {
                val currentItem = viewPager2.currentItem
                val nextItem = if (currentItem < sliderList.size - 1) currentItem + 1 else 0
                viewPager2.setCurrentItem(nextItem, true)
                handler.postDelayed(this, 3500)
            }
        }
        handler.removeCallbacks(runnable)
        handler.postDelayed(runnable, 3500)
    }

    private fun loadFavorites() {
        database.reference.child(Constants.FAVORITE_REF).child(Firebase.auth.currentUser!!.uid).addValueEventListener(object : ValueEventListener {
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

                if(productData != null) {
                    val matchFound = favoriteList.any { it.productId == productData!!.productId }
                    binding.btDetailFavorite.setImageResource(if (matchFound) R.drawable.ic_fav_filled_red else R.drawable.ic_fav_outline)
                } else {
                    Utils.showMessage(this@DetailsActivity, "Something went wrong")
                }



            }

            override fun onCancelled(databaseError: DatabaseError) {
                Utils.showMessage(this@DetailsActivity, "Failed")
            }
        })
    }


    private fun manageFavorite(
        product: ProductModel,
        favList: ArrayList<ProductModel>,
        favBtn: ImageView
    ) {
        val databaseReference = database.reference.child(Constants.FAVORITE_REF).child(Firebase.auth.currentUser!!.uid).child(product.productId)

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
                            Utils.showMessage(this@DetailsActivity, "Something went wrong")
                        }
                } else {
                    databaseReference.setValue(product)
                        .addOnSuccessListener {
                            favBtn.setImageResource(R.drawable.ic_fav_filled_red)

                        }
                        .addOnFailureListener {
                            Utils.showMessage(this@DetailsActivity, "Something went wrong")
                        }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
    }

    private fun getProductSimilar(category: String) {
        val currentAdminId = Firebase.auth.currentUser?.uid ?: return
        val productRef = database.getReference(Constants.PRODUCTS_REF).child(currentAdminId) // Fetch products under the admin's node
        val query = productRef.orderByChild("selectCategory").equalTo(category)

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val productList = ArrayList<ProductModel>()

                for (productSnapshot in dataSnapshot.children) {
                    val productData = productSnapshot.getValue(ProductModel::class.java)
                    productData?.let {
                        productList.add(it)
                    }
                }

                if (productList.isNotEmpty()) {
                    similarAdapter.submitList(productList)
                    binding.similarProductRv.adapter = similarAdapter
                }

                Log.d("Product List", "getProductSimilar: $productList")
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("Firebase", "Error fetching data: ${databaseError.message}")
            }
        })
    }

    private fun getProductData(productId: String) {
        val productRef = database.getReference(Constants.PRODUCTS_REF)

        productRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (adminSnapshot in dataSnapshot.children) { // Loop through all admin nodes
                    val productSnapshot = adminSnapshot.child(productId)
                    if (productSnapshot.exists()) { // Found the product
                        productData = productSnapshot.getValue(ProductModel::class.java)
                        if (productData != null) {
                            setProductDetails(productData!!)
                            loadFavorites()
                            getProductSimilar(productData!!.selectCategory)
                            sliderList.clear()
                            for (i in productData!!.images) {
                                sliderList.add(SliderModel(i.imageId, i.imageUrl, "", 45))
                            }
                            createSlider()
                        } else {
                            Utils.showMessage(this@DetailsActivity, "Something went wrong")
                        }
                        return // Exit loop once product is found
                    }
                }
                Utils.showMessage(this@DetailsActivity, "Product not found")
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("Firebase", "Error fetching data: ${databaseError.message}")
            }
        })
    }


    private fun setProductDetails(product: ProductModel) {

        val title = "${product.title}"

        binding.tvCarTitle.text = title

        val discount = product.price.toDouble() - product.fullPrice.toDouble()

        val discountPercentage = (discount/ product.price.toDouble()) * 100
        binding.tvRating.text = product.rating
        binding.tvCarYear.text = "${discountPercentage.toInt()}% Off"
        binding.tvOriginalPrice.paintFlags = binding.tvOriginalPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        binding.tvDescription.text = product.description
        binding.tvCarPrice.text = product.fullPrice
        binding.tvOriginalPrice.text = "₹ ${product.price}"
        binding.tvRating.text = product.rating




        binding.btShare.setOnClickListener {
            val text = "${product.title} at the price of Rs${product.price}\nI found this product on ${getString(R.string.app_name)}\nDownload the app on PlayStore\nhttps://play.google.com/store/apps/details?id=$packageName"
            Utils.shareText(this@DetailsActivity, text)
        }
    }

    override fun onItemClick(imageUrlModel: ProductImageUrlModel) {

    }

    override fun onItemClick(productModel: ProductModel) {
        getProductData(productModel.productId)
    }

    override fun onItemClick(sliderModel: SliderModel) {
        TODO("Not yet implemented")
    }
}