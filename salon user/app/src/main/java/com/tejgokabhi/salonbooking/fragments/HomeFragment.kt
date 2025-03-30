package com.tejgokabhi.salonbooking.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.tejgokabhi.salonbooking.R
import com.tejgokabhi.salonbooking.UserLocationManager
import com.tejgokabhi.salonbooking.activities.CategoryActivity
import com.tejgokabhi.salonbooking.activities.PolicyActivity
import com.tejgokabhi.salonbooking.activities.ViewBookingsActivity
import com.tejgokabhi.salonbooking.adapter.CategoryAdapter
import com.tejgokabhi.salonbooking.adapter.ProductAdapter
import com.tejgokabhi.salonbooking.adapter.SliderAdapter
import com.tejgokabhi.salonbooking.databinding.DialogDesignBinding
import com.tejgokabhi.salonbooking.databinding.DialogExitBinding
import com.tejgokabhi.salonbooking.databinding.FragmentHomeBinding
import com.tejgokabhi.salonbooking.databinding.NavigationLayoutBinding
import com.tejgokabhi.salonbooking.model.ButtonModel
import com.tejgokabhi.salonbooking.model.CategoryModel
import com.tejgokabhi.salonbooking.model.ProductModel
import com.tejgokabhi.salonbooking.model.SliderModel
import com.tejgokabhi.salonbooking.preference.SharedPref
import com.tejgokabhi.salonbooking.utils.Constants
import com.tejgokabhi.salonbooking.utils.Utils


class HomeFragment : Fragment(), ProductAdapter.OnItemClickListener, SliderAdapter.OnItemClickListener, CategoryAdapter.OnItemClickListener {
    private val binding by lazy { FragmentHomeBinding.inflate(layoutInflater) }
    private lateinit var bottomSheet: BottomSheetDialog
    private lateinit var database: FirebaseDatabase
    private lateinit var viewPager2: ViewPager2
    private lateinit var pageChangeListener: ViewPager2.OnPageChangeCallback
    private lateinit var sliderList: ArrayList<SliderModel>
    private lateinit var categoryList: ArrayList<CategoryModel>
    private lateinit var favoriteList: ArrayList<ProductModel>
    private lateinit var productList: ArrayList<ProductModel>
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var productAdapter: ProductAdapter
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var buttonList: ArrayList<ButtonModel>
    private lateinit var progressDialog: AlertDialog
    private var userId = ""
    private var adminId: String = ""
    private var isListenerAdded = false

    private val params = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.WRAP_CONTENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
    ).apply {
        setMargins(0, 0, 8, 0)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bottomSheet = BottomSheetDialog(requireContext())

        viewPager2 = binding.viewPager2
        database = FirebaseDatabase.getInstance()
        adminId = Firebase.auth.currentUser!!.uid
        pageChangeListener = object : ViewPager2.OnPageChangeCallback() {}
        sliderList = ArrayList()
        productList = ArrayList()
        favoriteList = ArrayList()
        buttonList = ArrayList()
        categoryAdapter = CategoryAdapter(this@HomeFragment)
        productAdapter = ProductAdapter(favoriteList, this@HomeFragment, requireContext())
        categoryList = ArrayList()
        drawerToggle = ActionBarDrawerToggle(requireActivity(), binding.drawerLayout, R.string.open, R.string.close)
        userId = Firebase.auth.currentUser!!.uid
        val user = SharedPref.getUserData(requireContext())

        binding.drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        requestLocationAndFetchData()

        if (!isListenerAdded) {
            isListenerAdded = true  // ✅ Prevent duplicate listeners

            UserLocationManager.addListener { location ->
                Log.d("LocationCheck", "Received updated location: $location")
                fetchNearbySalons()
            }
        }

        val navigation = NavigationLayoutBinding.inflate(layoutInflater)
        binding.navDrawer.addView(navigation.root)

        binding.btNavDrawer.setOnClickListener {
            binding.drawerLayout.open()
        }

        binding.apply {
            btProfile.setOnClickListener {
                findNavController().navigate(R.id.action_navigation_home_to_navigation_Profile)
            }

            btNotification.setOnClickListener {
                findNavController().navigate(R.id.action_navigation_home_to_navigation_notification)
            }

        }


        navigation.apply {
            navFavorite.setOnClickListener {
                closeDrawer()
                findNavController().navigate(R.id.action_navigation_home_to_navigation_favorites)
            }

            navContactUs.setOnClickListener {
                Utils.openEmailApp(requireContext(), "appsupport@gmail.com")
            }

            navRate.setOnClickListener {
                closeDrawer()
            }

            btEditProfile.setOnClickListener {
                closeDrawer()
                findNavController().navigate(R.id.action_navigation_home_to_navigation_Profile)
            }

            navPolicy.setOnClickListener {
                closeDrawer()
                startActivity(Intent(requireActivity(), PolicyActivity::class.java))
            }

            navShareApp.setOnClickListener {
                closeDrawer()
                val app = "Download now https://play.google.com/store/apps/details?id=${requireActivity().packageName}"
                Utils.shareText(requireContext(), app)
            }

            btBookingsView.setOnClickListener {
                closeDrawer()
                startActivity(Intent(requireActivity(), ViewBookingsActivity::class.java))
            }

            btAboutUs.setOnClickListener {
                closeDrawer()
                val description = getString(R.string.aboutUs)
                val buttonText = "Close"
                val title = "About Us"
                Utils.showDialog(requireContext(), title, description, buttonText, false) {
                    val dialogDesign = DialogDesignBinding.inflate(layoutInflater)
                    dialogDesign.btConfirm.visibility = View.GONE
                }
            }
        }
        binding.apply {
            val userName = "Hi ${user?.firstName},"
            tvUserName.text = userName

        }

        loadFavorites()
        searchProduct()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                if(binding.drawerLayout.isOpen) {
                    closeDrawer()
                } else {
                    showExitDialog()
                }
            }

        })

    }
    private fun requestLocationAndFetchData() {
        Log.d("UserLocation", "User enabled GPS")

        // Show Loading Dialog
        progressDialog = AlertDialog.Builder(requireContext())
            .setView(R.layout.dialog_progress)
            .setCancelable(false)
            .create()
        progressDialog.show()

        UserLocationManager.addListener { area ->
            Log.d("UserLocation", "Location detected: $area")

            fetchNearbySalons()
        }
    }
    override fun onResume() {
        super.onResume()

        if (!UserLocationManager.userLocation.isNullOrEmpty() && !fetchingSalons) {
            Log.d("LocationCheck", "Location available in onResume: ${UserLocationManager.userLocation}")
            fetchNearbySalons()
        }
    }
    var fetchingSalons = false

    private fun searchProduct() {
        binding.searchView.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Always filter both categories and products independently
                filterCategories(newText)
                filterProducts(newText)
                return true
            }
        })
    }

    private fun filterCategories(newText: String?) {
        val filteredCategories = ArrayList<CategoryModel>()

        // Filter categories based on the search text
        for (category in categoryList) {
            if (category.categoryName.contains(newText.orEmpty(), ignoreCase = true)) {
                filteredCategories.add(category)
            }
        }

        // Update the category adapter with the filtered categories
        categoryAdapter.submitList(filteredCategories)
        binding.categoryRv.adapter = categoryAdapter
    }

    private fun filterProducts(newText: String?) {
        val filteredProducts = ArrayList<ProductModel>()

        // Filter products based on title or salon name
        for (product in productList) {
            if (product.title.contains(newText.orEmpty(), ignoreCase = true) ||
                product.salonName.contains(newText.orEmpty(), ignoreCase = true)) {
                filteredProducts.add(product)
            }
        }

        // Update the product adapter with the filtered products
        productAdapter.submitList(filteredProducts)
        binding.productRv.adapter = productAdapter
    }



    private fun closeDrawer() {
        binding.drawerLayout.closeDrawer(GravityCompat.START)
    }

    private fun fetchNearbySalons() {
        if (fetchingSalons) {
            Log.d("SalonFilter", "fetchNearbySalons() already running, skipping duplicate call")
            return
        }

        fetchingSalons = true
        Log.d("SalonFilter", "fetchNearbySalons() started")

        val userArea = UserLocationManager.userLocation
        if (userArea.isNullOrEmpty()) {
            Log.e("SalonFilter", "User location is not set!")
            progressDialog.dismiss()
            fetchingSalons = false
            return
        }

        val databaseRef = FirebaseDatabase.getInstance().getReference("Admins")

        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                fetchingSalons = false  // Reset flag when done
                Log.d("Firebase", "Fetched data from Firebase")

                val matchedAdminIds = mutableListOf<String>()

                for (adminSnapshot in snapshot.children) {
                    val salonName = adminSnapshot.child("salonName").getValue(String::class.java)
                    val adminId = adminSnapshot.key

                    if (!salonName.isNullOrEmpty() && adminId != null) {
                        if (salonName.contains(userArea, ignoreCase = true)) {
                            matchedAdminIds.add(adminId)
                        }
                    }
                }

                if (matchedAdminIds.isNotEmpty()) {
                    Log.d("SalonFilter", "Nearby salons found: $matchedAdminIds")
                    categoryList.clear()
                    productList.clear()
                    sliderList.clear()

                    for (adminId in matchedAdminIds) {
                        loadCategories(adminId)
                        getProducts(adminId)
                        fetchSliderData(adminId)
                    }
                } else {
                    Log.e("SalonFilter", "No salons found in $userArea")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                fetchingSalons = false // Reset flag on error
                Log.e("FirebaseError", "Failed to fetch salons: ${error.message}")
            }
        })
    }

    private fun loadCategories(adminId: String) {
        val categoryRef = database.reference.child(Constants.CATEGORY_REF).child(adminId)

        categoryRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d("FirebaseData", "Category Snapshot for Admin ($adminId): ${dataSnapshot.value}")

                // Use a HashSet to track already-added category names
                val uniqueCategoryNames = HashSet<String>()
                // Initialize the set with names already in categoryList
                categoryList.forEach { uniqueCategoryNames.add(it.categoryName.trim()) }

                for (categorySnapshot in dataSnapshot.children) {
                    val category = categorySnapshot.getValue(CategoryModel::class.java)
                    category?.categoryName?.trim()?.let { categoryName ->
                        if (uniqueCategoryNames.add(categoryName)) {  // Only add if not already present
                            categoryList.add(category)
                            progressDialog.dismiss()
                        }
                    } ?: Log.e("Error", "Failed to parse: ${categorySnapshot.value}")
                }

                categoryAdapter.submitList(ArrayList(categoryList))
                binding.categoryRv.adapter = categoryAdapter
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("FirebaseError", "Failed: ${databaseError.message}")
            }
        })
    }


    private fun loadFavorites() {
        database.reference.child(Constants.FAVORITE_REF).child(userId).addValueEventListener(object : ValueEventListener {
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

            }

            override fun onCancelled(databaseError: DatabaseError) {
                Utils.showMessage(requireContext(), "Failed")
            }
        })
    }


    private fun getProducts(adminId: String) {
        val productRef = database.getReference(Constants.PRODUCTS_REF).child(adminId)

        productRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d("FirebaseData", "Product Snapshot for Admin ($adminId): ${dataSnapshot.value}")

                // Use a HashSet to track already-added product IDs
                val uniqueProductIds = HashSet<String>()
                productList.forEach { uniqueProductIds.add(it.productId) }

                for (productSnapshot in dataSnapshot.children) {
                    val product = productSnapshot.getValue(ProductModel::class.java)
                    product?.let {
                        if (uniqueProductIds.add(it.productId)) {  // Only add if unique
                            productList.add(it)
                            progressDialog.dismiss()
                        }
                    } ?: Log.e("Error", "Failed to parse: ${productSnapshot.value}")
                }

                productAdapter.submitList(ArrayList(productList.shuffled()))
                binding.productRv.adapter = productAdapter
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("FirebaseError", "Failed: ${databaseError.message}")
                Utils.showMessage(requireContext(), "Failed to load products")
            }
        })
    }




    override fun onDestroy() {
        super.onDestroy()
        viewPager2.unregisterOnPageChangeCallback(pageChangeListener)
    }

    private fun fetchSliderData(adminId: String) {
        database.getReference(Constants.SLIDER_DOCUMENT)
            .orderByChild("adminId")
            .equalTo(adminId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (childSnapshot in dataSnapshot.children) {
                        val slider = childSnapshot.getValue(SliderModel::class.java)
                        if (slider != null) {
                            sliderList.add(slider)
                        }
                    }
                    if (sliderList.isNotEmpty()) {
                        createSlider()
                        binding.card.visibility = View.VISIBLE
                    } else {
                        binding.card.visibility = View.GONE
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("FirebaseError", "Failed to fetch slider data: ${databaseError.message}")
                }
            })
    }



    private fun createSlider() {
        val sliderAdapter = SliderAdapter(this@HomeFragment)
        viewPager2.adapter = sliderAdapter

        val dotImage = Array(sliderList.size) { ImageView(requireContext()) }

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

    private fun showExitDialog() {
        val bottomSheet = BottomSheetDialog(requireContext())
        val layout = DialogExitBinding.inflate(layoutInflater)
        bottomSheet.setContentView(layout.root)
        bottomSheet.setCanceledOnTouchOutside(true)
        layout.btExit.setOnClickListener {
            bottomSheet.dismiss()
            requireActivity().finish()
        }
        layout.btCancel.setOnClickListener {
            bottomSheet.dismiss()
        }
        bottomSheet.show()
    }

    override fun onItemClick(sliderModel: SliderModel) {

    }

    override fun onItemClick(categoryModel: CategoryModel) {
        val categoryName = categoryModel.categoryName?.trim() ?: ""

        if (categoryName.isEmpty()) {
            Utils.showMessage(requireContext(), "Category name is missing!")
            return
        }

        val intent = Intent(requireContext(), CategoryActivity::class.java)
        intent.putExtra("categoryName", categoryName)
        startActivity(intent)
    }


    override fun onItemClick(
        productModel: ProductModel,
        favBtn: ImageView,
        favList: ArrayList<ProductModel>
    ) {
        manageFavorite(productModel, favList, favBtn)
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