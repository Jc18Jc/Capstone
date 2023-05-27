package com.example.myapplication.Chat

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.WindowManager
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil.setContentView
import com.example.myapplication.databinding.CalculationDialogBinding
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.text.DecimalFormat

class CalculationDialog(context: Context, val destination: String,
                        val nowPeople:String,
                        val hostName2: String) : AlertDialog(context) {
    private lateinit var editText: EditText
    private lateinit var binding: CalculationDialogBinding
    val firestore = FirebaseFirestore.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private var currentUser: String? = ""            // 현재 닉네임
    private var roomName: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= CalculationDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)

        editText = binding.feeInputEditText
        var pointNumStr = "";
        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // 숫자가 입력됐을 때 makeCommaNumber 호출
                if (!TextUtils.isEmpty(p0.toString()) && !p0.toString().equals(pointNumStr)) {
                    pointNumStr = makeCommaNumber(Integer.parseInt(p0.toString().replace(",", "")))
                    editText.setText(pointNumStr)
                    editText.setSelection(pointNumStr.length)  //커서를 오른쪽 끝으로 보냄
                }
            }
        })

        var hostQr = ""
        var hostName = ""

        Log.d("ChatRoomActivity_f", hostName2)

        /** 일단 먼저 Room의 host에 대한 정보 가져옴 */
        db.collection("Room")
            .document(destination)
            .get()
            .addOnSuccessListener { documentSnapshot  ->
                if (documentSnapshot.exists()) {
                    hostName = documentSnapshot.getString("host").toString()
                    Log.d("ChatFragmen_in Room", "$hostName")

                    db.collection("User")
                        .document(hostName2)
                        .get()
                        .addOnSuccessListener { documentSnapshot  ->
                            if (documentSnapshot.exists()) {
                                hostQr = documentSnapshot.getString("qr").toString()
                            } else {
                                /** 일단 이거부터 못불러옴 */
                                Log.d("ChatFragment", "document is not exist")
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.d("ChatFragment", "$e")
                        }

                } else {
                    Log.d("ChatFragment", "document is not exist")
                }
            }
            .addOnFailureListener { e ->
                Log.d("ChatFragment", "$e")
            }

        Log.d("ChatFragment_Room", hostName2)

        /** 그 다음으로 host의 qr정보를 가져옴 */
        db.collection("User")
            .document(hostName2)
            .get()
            .addOnSuccessListener { documentSnapshot  ->
                if (documentSnapshot.exists()) {
                    hostQr = documentSnapshot.getString("qr").toString()
                } else {
                    Log.d("ChatFragment", "document is not exist")
                }
            }
            .addOnFailureListener { e ->
                Log.d("ChatFragment", "$e")
            }


        binding.correctButton.setOnClickListener {
            Log.d("CalcDialog", "buttonCorrect is Clicked")


            // 입력한 값을 콤마(,)를 없애고 cost에 담음
            db.collection("User")
                .document(hostName2)
                .get()
                .addOnSuccessListener { documentSnapshot  ->
                    if (documentSnapshot.exists()) {
                        hostQr = documentSnapshot.getString("qr").toString()

                        // 방 이름으로 알맞은 document를 찾음
                        val cost = editText.text.toString().replace(",", "")
                        val calculateCost = calculateResult(cost, nowPeople)

                        Log.d("calc_cost_nowPeople", "$nowPeople")
                        Log.d("calc_cost", cost.toString())
                        Log.d("calc_cost_",calculateCost.toString())


                        val costInfo = mapOf("cost" to cost)
                        val collectionRef = firestore?.collection("Chat")
                            ?.document(destination)
                            ?.collection("Cost")
                            ?.document("cost")

                        collectionRef?.set(costInfo)?.addOnSuccessListener {
                            val data = hashMapOf(
                                "nickname" to "관리자",
                                "contents" to "택시 탑승이 완료되었습니다.\n\n" +
                                        "총 결제금액은 $cost 원입니다.\n" +
                                        "발급된 qr코드로 $calculateCost 원을 송금해주세요.\n\n" +
                                        "$hostQr",
                                "time" to Timestamp.now()
                            )
                            if (data != null) {
                                FirebaseFirestore.getInstance().collection("Chat")
                                    .document(destination)
                                    .collection("ChatList")
                                    .add(data)
                                    .addOnSuccessListener {
                                        Log.w("ChatFragment", "Document added: $it")
                                    }
                                    .addOnFailureListener { e ->
                                        Log.w("ChatFragment", "Error occurs: $e")
                                    }
                            } else {
                                Log.w("ChatFragment", "Data is null. Cannot save to Firestore.")
                            }


                            dismiss()
                        }?.addOnFailureListener {e ->
                            Log.d("Calculatoin", "$e")
                        }

                    } else {
                        Log.d("ChatFragment_2", "document is not exist")
                    }
                }
                .addOnFailureListener { e ->
                    Log.d("ChatFragment", "$e")
                }


        }
    }

    // 1000단위로 콤마(,) 찍기
    fun makeCommaNumber(input: Int): String {
        val formatter = DecimalFormat("###,###")
        return formatter.format(input)
    }

    fun calculateResult(cost: String, people: String): Double {
        val costValue = cost.toDoubleOrNull()
        val peopleValue = people.toDoubleOrNull()

        if (costValue != null && peopleValue != null && peopleValue != 0.0) {
            val result = costValue / peopleValue
            val formattedResult = String.format("%.3f", result)
            return formattedResult.toDouble()
        }

        return 0.0
    }
}