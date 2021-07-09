package com.DeepaliVerma.heyya

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import androidx.core.view.isVisible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import kotlinx.android.synthetic.main.activity_otp.*
import java.util.concurrent.TimeUnit

const val PHONE_NUMBER="phoneNumber"
class OtpActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var auth: FirebaseAuth
    lateinit var callbacks:PhoneAuthProvider.OnVerificationStateChangedCallbacks
    var phoneNumber: String? = null
    private lateinit var progressDialog: ProgressDialog
    var mVerificationId:String?=null
    private var mCounterDown: CountDownTimer? = null
    var mResendToken:PhoneAuthProvider.ForceResendingToken?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp)
        auth=FirebaseAuth.getInstance()
        initView()
        startVerify()
    }
    private fun startVerify() {
startPhoneNumberVerification(phoneNumber!!)
        showTimer(60000)
        progressDialog = createProgressDialog("sending a verification code", false)
        progressDialog.show()
    }
    private fun showTimer(milliSecInFuture: Long) {
resendBtn.isEnabled=false
        object: CountDownTimer(milliSecInFuture,1000){
            override fun onTick(millisUntilFinished: Long) {
                counterTv.isVisible=true
                counterTv.text=getString(R.string.secondsRemaining,millisUntilFinished/1000)
            }

            override fun onFinish() {
                resendBtn.isEnabled=true
                counterTv.isVisible=false
            }

        }.start()
    }
    override fun onDestroy() {
 super.onDestroy()
        if (mCounterDown != null) {
            mCounterDown!!.cancel()
        }
    }
    @SuppressLint("StringFormatInvalid")
    private fun initView() {
        phoneNumber=intent.getStringExtra(PHONE_NUMBER)
        verifyTv.text= getString(R.string.verify_number,phoneNumber)
        setSpannableString()
        verificationBtn.setOnClickListener(this)
        resendBtn.setOnClickListener(this)

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                if (::progressDialog.isInitialized) {
                    progressDialog.dismiss()
                }
               val smsCode:String?=credential.smsCode
                if(!smsCode.isNullOrBlank()){
                    sendcodeEt.setText(smsCode)
                }

               signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                if (::progressDialog.isInitialized) {
                    progressDialog.dismiss()
                }

                Log.w(TAG, "onVerificationFailed", e)

                if (e is FirebaseAuthInvalidCredentialsException) {

                } else if (e is FirebaseTooManyRequestsException) {

                }
               Log.e("Error_Firebase",e.localizedMessage)
                notifyUserAndRetry("Your Phone Number might be wrong or cnnection error.Retry again!!")
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                if (::progressDialog.isInitialized) {
                    progressDialog.dismiss()
                }
                mVerificationId = verificationId
                mResendToken = token
            }
        }
    }
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {

        val mAuth = FirebaseAuth.getInstance()
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {

                    if (::progressDialog.isInitialized) {
                        progressDialog.dismiss()
                    }
                    showSignUpActivity()

                } else {

                    if (::progressDialog.isInitialized) {
                        progressDialog.dismiss()
                    }

                    notifyUserAndRetry("Your Phone Number Verification is failed.Retry again!")
                }
            }
    }

    private fun showSignUpActivity() {
        startActivity(
            Intent(this, signupActivity::class.java)
        )
        finish()
    }

    fun setSpannableString() {
        val span=SpannableString(getString(R.string.waiting_text,phoneNumber))
        val clickableSpan=object:ClickableSpan(){
            override fun onClick(widget: View) {
             showLoginActivity()
            }

            override fun updateDrawState(ds: TextPaint) {
                ds.isUnderlineText=false
                ds.color=ds.linkColor
            }

        }
        span.setSpan(clickableSpan,span.length -13,span.length,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        waitingTv.text=span
        waitingTv.movementMethod=LinkMovementMethod.getInstance()
    }
    fun showLoginActivity() {
        startActivity(
            Intent(this, LoginActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )

    }

    override fun onBackPressed() {

    }
    fun notifyUserAndRetry(message: String) {
        MaterialAlertDialogBuilder(this).apply {
            setMessage(message)
            setPositiveButton("Ok") { _, _ ->
                showLoginActivity()
            }

            setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            setCancelable(false)
            create()
            show()
        }
    }
    override fun onClick(v: View?) {
        when(v){
            verificationBtn->{
                val code=sendcodeEt.text.toString()
                if(code.isNotEmpty()){
                    progressDialog=createProgressDialog("Please wait...",false)
                    progressDialog.show()
                    val credential=PhoneAuthProvider.getCredential(mVerificationId!!,code)
                    signInWithPhoneAuthCredential(credential)
                }
            }
            resendBtn->{
                val code=sendcodeEt.text.toString()
                if(mResendToken!=null){
                    resendVerificationCode(phoneNumber.toString(), mResendToken)
                    showTimer(60000)
                    progressDialog=createProgressDialog("Sending a Verification Code",false)
                    progressDialog.show()
                }
            }
        }
    }

    private fun resendVerificationCode(
        phoneNumber: String,
        mResendToken: PhoneAuthProvider.ForceResendingToken?
    ) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber, // Phone number to verify
            60, // Timeout duration
            TimeUnit.SECONDS, // Unit of timeout
            this, // Activity (for callback binding)
            callbacks, // OnVerificationStateChangedCallbacks
            mResendToken
        ) // ForceResendingToken from callbacks
    }



    private fun startPhoneNumberVerification(phoneNumber: String) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber,      // Phone number to verify
            60,               // Timeout duration
            TimeUnit.SECONDS, // Unit of timeout
            this,            // Activity (for callback binding)
            callbacks
        ) // OnVerificationStateChangedCallbacks
    }

}
fun Context.createProgressDialog(message:String, isCancelable:Boolean):ProgressDialog{
    return ProgressDialog(this).apply {
        setMessage(message)
        setCancelable(false)
        setCanceledOnTouchOutside(false)
    }
}