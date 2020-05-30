package com.argonlabs.cardsx.ui.fragments

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.argonlabs.cardsx.R
import com.argonlabs.cardsx.adapters.CardsAdapter
import com.argonlabs.cardsx.base.BaseApplication.Companion.getmInstance
import com.argonlabs.cardsx.base.BaseFragment
import com.argonlabs.cardsx.databinding.FragmentWalletBinding
import com.argonlabs.cardsx.models.CardsModel
import com.argonlabs.cardsx.utils.Locker
import com.google.firebase.firestore.*
import java.util.*

class WalletFragment : BaseFragment() {
    var activity: Activity? = null
    private lateinit var binding: FragmentWalletBinding
    var cardsAdapter: CardsAdapter? = null
    private val cardsList: MutableList<CardsModel> = ArrayList()
    private var cardsEvenListener: ListenerRegistration? = null
    private var lastVisible: DocumentSnapshot? = null
    private var isFirstPageFirstLoad = true
    var locker: Locker? = null
    var emptyList: LinearLayout? = null
    var tempR: RecyclerView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity = getmInstance()?.activity
        locker = Locker(getmInstance()?.sessionKey)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentWalletBinding.inflate(inflater, container, false)
        emptyList = binding.root.findViewById(R.id.emptyList)
        tempR = binding.root.findViewById(R.id.cardsRecycler)
        setUpClickListeners()
        setupRecyclerView()
        loadCardsFromDb()
        return binding.root
    }

    private fun setUpClickListeners() {
        binding.addNewCardBtn.setOnClickListener {
            val addNewCardDialogFragment = AddNewCardDialogFragment()
            addNewCardDialogFragment.show(fragmentManager!!, "ADD_CARD")
        }
        binding.addCardTv.setOnClickListener {
            val addNewCardDialogFragment = AddNewCardDialogFragment()
            addNewCardDialogFragment.show(fragmentManager!!, "ADD_CARD")
        }
    }

    private fun setupRecyclerView() {
        val mLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager(activity)
        binding.cardsRecycler.layoutManager = mLayoutManager
        binding.cardsRecycler.itemAnimator = DefaultItemAnimator()
        cardsAdapter = CardsAdapter(cardsList, activity!!)
        binding.cardsRecycler.layoutManager = mLayoutManager
        binding.cardsRecycler.adapter = cardsAdapter
//        binding.cardsRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                super.onScrolled(recyclerView, dx, dy)
//                val reachedBottom = !recyclerView.canScrollVertically(1)
//                if (reachedBottom) {
//                    loadMoreCards()
//                }
//            }
//        })
    }

    private fun loadCardsFromDb() {
        cardsList.clear()
        val firstQuery = db.collection("/data/" + mAuth.uid + "/cardsData")
                .orderBy("addedOnTimestamp", Query.Direction.DESCENDING)
              //  .limit(10)
        cardsEvenListener = firstQuery.addSnapshotListener { documentSnapshots: QuerySnapshot?, e: FirebaseFirestoreException? ->
            if (documentSnapshots == null) return@addSnapshotListener
            if (documentSnapshots.documents.size == 0) {
                emptyList!!.visibility = View.VISIBLE
                binding.loadingTv.visibility = View.GONE
                if (cardsList.size == 1) {
                    cardsList.removeAt(0)
                    cardsAdapter!!.notifyItemRemoved(0)
                    return@addSnapshotListener
                }
                return@addSnapshotListener
            }
            if (isFirstPageFirstLoad) {
                lastVisible = documentSnapshots.documents[documentSnapshots.size() - 1]
            }
            for (doc in documentSnapshots.documentChanges) {
                if (doc.type == DocumentChange.Type.ADDED) {
                    val document = doc.document
                    if (isFirstPageFirstLoad) {
                        cardsList.add(loadCardsModel(document))
                    } else {
                        cardsList.add(0, loadCardsModel(document))
                        cardsAdapter!!.notifyItemInserted(0)
                    }
                    if (cardsList.size > 0) {
                        emptyList!!.visibility = View.GONE
                    } else {
                        emptyList!!.visibility = View.VISIBLE
                    }
                    cardsAdapter!!.notifyDataSetChanged()
                } else if (doc.type == DocumentChange.Type.REMOVED) {
                    val document = doc.document
                    val posToRemove = getPositionOfDoc(document.id)
                    cardsList.removeAt(posToRemove)
                    cardsAdapter!!.notifyItemRemoved(posToRemove)
                    if (cardsList.size > 0) {
                        emptyList!!.visibility = View.GONE
                    } else {
                        emptyList!!.visibility = View.VISIBLE
                    }
                }
            }
            isFirstPageFirstLoad = false
            binding.loadingTv.visibility = View.GONE
        }
    }

    private fun getPositionOfDoc(doc: String): Int {

        //Search
        for (i in cardsList.indices) {
            if (cardsList[i].docID == doc) return i
        }
        return -1
    }

//    fun loadMoreCards() {
//        val nextQuery = db.collection("/data/" + mAuth.uid + "/cardsData")
//                .orderBy("addedOnTimestamp", Query.Direction.DESCENDING)
//                .startAfter(lastVisible)
//                .limit(5)
//        nextQuery.addSnapshotListener(getActivity()!!) { documentSnapshots: QuerySnapshot?, _: FirebaseFirestoreException? ->
//            if (!documentSnapshots!!.isEmpty) {
//                lastVisible = documentSnapshots.documents[documentSnapshots.size() - 1]
//                for (doc in documentSnapshots.documentChanges) {
//                    if (doc.type == DocumentChange.Type.ADDED) {
//                        cardsList.add(loadCardsModel(doc.document))
//                        cardsAdapter!!.notifyDataSetChanged()
//                    }
//                }
//            }
//        }
//    }

    private fun loadCardsModel(document: QueryDocumentSnapshot): CardsModel {
        val tempModel = CardsModel()
        tempModel.addedOnTimestamp = document.getLong("addedOnTimestamp")!!
        tempModel.docID = document.id
        tempModel.cardColor = document.getString("cardColor")!!.toInt()
        tempModel.cardType = locker?.decryptDataForShow(document[FieldPath.of(locker?.encryptDataForUpload("cardType"))].toString())
        tempModel.bankName = locker?.decryptDataForShow(document[FieldPath.of(locker?.encryptDataForUpload("bankName"))].toString())
        tempModel.cardNumber = locker?.decryptDataForShow(document[FieldPath.of(locker?.encryptDataForUpload("cardNumber"))].toString())
        tempModel.cardHolderName = locker?.decryptDataForShow(document[FieldPath.of(locker?.encryptDataForUpload("cardHolderName"))].toString())
        tempModel.expiryDate = locker?.decryptDataForShow(document[FieldPath.of(locker?.encryptDataForUpload("expiryDate"))].toString())
        tempModel.cardCVV = locker?.decryptDataForShow(document[FieldPath.of(locker?.encryptDataForUpload("cardCVV"))].toString())
        return tempModel
    }
}