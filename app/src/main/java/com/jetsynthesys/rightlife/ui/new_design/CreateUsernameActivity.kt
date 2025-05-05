package com.jetsynthesys.rightlife.ui.new_design

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.apimodel.userdata.Userdata
import com.jetsynthesys.rightlife.ui.CommonAPICall
import com.jetsynthesys.rightlife.ui.utility.AppConstants
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import com.jetsynthesys.rightlife.ui.utility.Utils
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CreateUsernameActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setChildContentView(R.layout.activity_create_username)


        var username = intent.getStringExtra("USERNAME_KEY")
        var email = intent.getStringExtra("EMAIL")


        val edtUsername = findViewById<EditText>(R.id.edt_username)
        val sharedPreferenceManager = SharedPreferenceManager.getInstance(this)

        if (!username.isNullOrEmpty()) {
            edtUsername.setText(username) // Set the username to the EditText
        } else if (!sharedPreferenceManager.displayName.isNullOrEmpty()) {
            username = sharedPreferenceManager.userName
            edtUsername.setText(sharedPreferenceManager.displayName) // Set the username to the EditText
            email = sharedPreferenceManager.email
        }

        val colorStateListSelected = ContextCompat.getColorStateList(this, R.color.menuselected)
        val colorStateList = ContextCompat.getColorStateList(this, R.color.rightlife)

        val tvCharLeft = findViewById<TextView>(R.id.tv_char_left)
        val btnContinue = findViewById<Button>(R.id.btn_continue)
        val tvError = findViewById<TextView>(R.id.tv_username_error)
        val charLeft = edtUsername.text.length
        if (validateUsername(edtUsername.text.toString())) {
            tvError.visibility = GONE
            btnContinue.backgroundTintList = colorStateListSelected
            btnContinue.isEnabled = true
        } else {
            tvError.visibility = VISIBLE
            btnContinue.backgroundTintList = colorStateList
            btnContinue.isEnabled = false
        }

        "$charLeft/20 ch".also { tvCharLeft.text = it }

        edtUsername.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, count: Int) {
                val c = edtUsername.text.length
                "$c/20 ch".also { tvCharLeft.text = it }
                if (validateUsername(p0.toString())) {
                    tvError.visibility = GONE
                    btnContinue.backgroundTintList = colorStateListSelected
                    btnContinue.isEnabled = true
                } else {
                    tvError.visibility = VISIBLE
                    btnContinue.backgroundTintList = colorStateList
                    btnContinue.isEnabled = false
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })

        btnContinue.setOnClickListener {
            Utils.hideSoftKeyboard(this@CreateUsernameActivity)
            sharedPreferenceManager.userName = edtUsername.text.toString()
            val userdata = Userdata()
            userdata.firstName = username
            userdata.email = email
            //updateUserData(userdata)
            sharedPreferenceManager.createUserName = true
            val intent = Intent(this, HappyToHaveYouActivity::class.java)
            startActivity(intent)
        }
    }

    fun validateUsername(username: String): Boolean {
        // Check if the username only contains alphabetic characters
        val regex = "^[A-Za-z]+$".toRegex()

        return when {
            !username.matches(regex) -> false
            else -> true
        }
    }

    private fun updateUserData(userdata: Userdata) {
        val call: Call<ResponseBody> = apiService.updateUser(sharedPreferenceManager.accessToken, userdata)
        call.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                if (response.isSuccessful && response.body() != null) {
                    Log.d("AAAA", "Response = " + response.body())
                } else {
                    Toast.makeText(
                        this@CreateUsernameActivity,
                        "Server Error: " + response.code(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                CommonAPICall.updateChecklistStatus(
                    this@CreateUsernameActivity,
                    "profile",
                    AppConstants.CHECKLIST_INPROGRESS
                )
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                handleNoInternetView(t)
            }
        })
    }
}