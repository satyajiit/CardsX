package com.argonlabs.cardsx.ui.fragments

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.DatePicker.OnDateChangedListener
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.argonlabs.cardsx.R
import com.argonlabs.cardsx.adapters.ExpensesAdapter
import com.argonlabs.cardsx.base.BaseApplication.Companion.getmInstance
import com.argonlabs.cardsx.databinding.FragmentTrackerBinding
import com.argonlabs.cardsx.managers.DialogCreator
import com.argonlabs.cardsx.models.ExpenseModel
import com.argonlabs.cardsx.utils.Locker
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class TrackerFragment : Fragment() {
    var activity: Activity? = null
    private var _binding: FragmentTrackerBinding? = null
    private val binding get() = _binding!!
    var locker: Locker? = null
    private lateinit var picker: DatePicker
    private var date_report_edit_text: TextInputEditText? = null
    var firebaseAuth = FirebaseAuth.getInstance()
    var sessionKey: String? = null
    private val db = FirebaseFirestore.getInstance()
    private val expenseList: MutableList<ExpenseModel> = ArrayList()
    private var expenseEventListener: ListenerRegistration? = null
    private var totalSpentListener: ListenerRegistration? = null
    private var lastVisible: DocumentSnapshot? = null
    private val isFirstPageFirstLoad = true
    var expensesAdapter: ExpensesAdapter? = null
    var emptyList: LinearLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity = getmInstance()!!.activity
        sessionKey = getmInstance()!!.sessionKey
        locker = Locker(sessionKey)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        _binding = FragmentTrackerBinding.inflate(inflater, container, false)
        emptyList = binding.root.findViewById(R.id.emptyList)
        setUpListeners()
        setUpExpenseRecycler()
        loadTrackedData()
        setUpValueListener()
        return binding.root
    }

    private fun setUpListeners() {
        binding.addToTrackFab.setOnClickListener { showAddDataDialog() }
    }

    private fun setUpExpenseRecycler() {
        val mLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager(activity)
        binding.expenseRecycler.layoutManager = mLayoutManager
        binding.expenseRecycler.itemAnimator = DefaultItemAnimator()
        expensesAdapter = ExpensesAdapter(expenseList, activity!!)
        binding.expenseRecycler.layoutManager = mLayoutManager
        binding.expenseRecycler.adapter = expensesAdapter

    }

    private fun loadTrackedData() {
        expenseList.clear()
        val firstQuery = db.collection("/data/" + firebaseAuth.uid + "/expenseData")
                .orderBy("expenseTime", Query.Direction.DESCENDING)

        expenseEventListener = firstQuery.addSnapshotListener { documentSnapshots: QuerySnapshot?, e: FirebaseFirestoreException? ->
            if (documentSnapshots == null) return@addSnapshotListener
            if (documentSnapshots.documents.size == 0) {
                emptyList!!.visibility = View.VISIBLE
                return@addSnapshotListener
            }
            lastVisible = documentSnapshots.documents[documentSnapshots.size() - 1]
            for (doc in documentSnapshots.documentChanges) {
                if (doc.type == DocumentChange.Type.ADDED) {
                    val document = doc.document
                    expenseList.add(loadExpensesModel(document))
                    if (expenseList.size > 0) {
                        emptyList!!.visibility = View.GONE
                    } else {
                        emptyList!!.visibility = View.VISIBLE
                    }
                    expensesAdapter!!.notifyDataSetChanged()
                }
            }
        }
    }

    private fun loadExpensesModel(document: QueryDocumentSnapshot): ExpenseModel {
        val tempModel = ExpenseModel()
        tempModel.timeStamp = document.getLong("expenseTime")!!
        tempModel.cost = locker!!.decryptDataForShow(document[FieldPath.of(locker!!.encryptDataForUpload("cost"))].toString())
        tempModel.description = locker!!.decryptDataForShow(document[FieldPath.of(locker!!.encryptDataForUpload("description"))].toString())
        tempModel.mainTitle = locker!!.decryptDataForShow(document[FieldPath.of(locker!!.encryptDataForUpload("companyName"))].toString())
        return tempModel
    }

//    private fun loadMoreData() {
//        val nextQuery = db.collection("/data/" + firebaseAuth.uid + "/expenseData")
//                .orderBy("expenseTime", Query.Direction.DESCENDING)
//                .startAfter(lastVisible)
//                .limit(5)
//        nextQuery.addSnapshotListener(getActivity()!!) { documentSnapshots: QuerySnapshot?, e: FirebaseFirestoreException? ->
//            if (!documentSnapshots!!.isEmpty) {
//                lastVisible = documentSnapshots.documents[documentSnapshots.size() - 1]
//                for (doc in documentSnapshots.documentChanges) {
//                    if (doc.type == DocumentChange.Type.ADDED) {
//                        expenseList.add(loadExpensesModel(doc.document))
//                        expensesAdapter!!.notifyDataSetChanged()
//                    }
//                }
//            }
//        }
//    }

    private fun showAddDataDialog() {
        val dialog = Dialog(activity!!)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setContentView(R.layout.dialog_add_tracker_data)
        dialog.window!!.attributes.windowAnimations = R.style.slide_dialog
        date_report_edit_text = dialog.findViewById(R.id.date_report_edit_text)
        dialog.findViewById<View>(R.id.cancel_button).setOnClickListener { view: View? -> dialog.dismiss() }
        val date_report_edit_text: TextInputEditText = dialog.findViewById(R.id.date_report_edit_text)
        (dialog.findViewById<View>(R.id.date_report_text_input) as TextInputLayout).setEndIconOnClickListener { view: View? -> showDateDialog() }
        date_report_edit_text.setOnClickListener { view: View? -> showDateDialog() }
        val mainTitle: TextInputEditText = dialog.findViewById(R.id.etMainTitle)
        val mainDes: TextInputEditText = dialog.findViewById(R.id.etDescription)
        val moneySpent: TextInputEditText = dialog.findViewById(R.id.etMoney)
        dialog.findViewById<View>(R.id.sure_button).setOnClickListener { view: View? -> if (mainTitle.text.toString().length < 2) showToast("Invalid Title Given") else if (mainDes.text.toString().length < 5) showToast("Description too short!") else if (moneySpent.text.toString().length < 1 || moneySpent.text.toString().toInt() < 1) showToast("Invalid Expense Given!") else if (date_report_edit_text.text.toString().length < 8) showToast("Invalid Title Given") else uploadData(mainTitle.text.toString(), mainDes.text.toString(), moneySpent.text.toString(), dialog) }
        dialog.show()
    }

    private fun showDateDialog() {
        val dialog = Dialog(activity!!)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setContentView(R.layout.dialog_date_picker)
        picker = dialog.findViewById(R.id.datePicker)
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        picker.init(calendar[Calendar.YEAR], calendar[Calendar.MONTH], calendar[Calendar.DAY_OF_MONTH], OnDateChangedListener { _: DatePicker?, year: Int, month: Int, dayOfMonth: Int ->
            date_report_edit_text!!.setText(currentDate)
            dialog.dismiss()
        })
        dialog.show()
    }

    private fun uploadData(companyName: String, des: String, cost: String, dialog: Dialog) {
        val dialogCreator = DialogCreator(activity!!)
        dialogCreator.showLoadingDialog()
        dialogCreator.updateDialogText("Adding Your Expense Data...")
        val year = picker.year
        val month = picker.month
        val day = picker.dayOfMonth
        val calendar = Calendar.getInstance()
        calendar[Calendar.YEAR] = year
        calendar[Calendar.MONTH] = month
        calendar[Calendar.DAY_OF_MONTH] = day
        val timestamp = calendar.timeInMillis
        db.collection("data/" + firebaseAuth.uid + "/expenseData")
                .add(createDataObj(companyName, des, cost, timestamp))
                .addOnSuccessListener { documentReference: DocumentReference? ->
                    showToast("Data Added Successful!")
                    dialog.dismiss()
                    dialogCreator.dialog.dismiss()
                    incrementExpenseCounter(cost)
                    loadTrackedData()
                }
                .addOnFailureListener { e: Exception? ->
                    showToast("Failed. Error Code 109")
                    dialogCreator.dialog.dismiss()
                }
    }

    private fun setUpValueListener() {
        binding.spentTotalTv.text = "Loading..."
        totalSpentListener =  db.collection("data/" + firebaseAuth.uid + "/totalExpense")
                .document("TOTAL_EXPENSE")
                .addSnapshotListener { documentSnapshot: DocumentSnapshot?, e: FirebaseFirestoreException? ->

                    if (documentSnapshot != null && documentSnapshot.getLong("Expense") == null) {
                        binding.spentTotalTv.text = getString(R.string.rs_cost).replace("%cost%", "0")
                        return@addSnapshotListener
                    }
                    binding.spentTotalTv.text = getString(R.string.rs_cost).replace("%cost%", "" + documentSnapshot?.getLong("Expense"))
                }
    }

    private fun incrementExpenseCounter(cost: String) {
        val expData: MutableMap<String, Any> = HashMap()
        expData["Expense"] = FieldValue.increment(cost.toLong())
        db.collection("data/" + firebaseAuth.uid + "/totalExpense")
                .document("TOTAL_EXPENSE")[expData] = SetOptions.merge()
    }

    private fun createDataObj(companyName: String, des: String, cost: String, timestamp: Long): Any {
        val expData: MutableMap<String, Any> = HashMap()
        expData["expenseTime"] = timestamp
        expData[locker!!.encryptDataForUpload("companyName")] = locker!!.encryptDataForUpload(companyName)
        expData[locker!!.encryptDataForUpload("description")] = locker!!.encryptDataForUpload(des)
        expData[locker!!.encryptDataForUpload("cost")] = locker!!.encryptDataForUpload(cost)
        return expData
    }

    //month is 0 based
    private val currentDate: String
        get() = picker.dayOfMonth.toString() + "/" +
                (picker.month + 1) + "/" +  //month is 0 based
                picker.year

    private fun showToast(msg: String) {
        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        expenseEventListener?.remove()
        totalSpentListener?.remove()
       // _binding = null
    }

}