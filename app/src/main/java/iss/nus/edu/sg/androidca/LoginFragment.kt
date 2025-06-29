package iss.nus.edu.sg.androidca

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import iss.nus.edu.sg.androidca.databinding.FragmentLoginBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle
import java.net.HttpURLConnection
import java.net.URL

class LoginFragment: Fragment() {
    private var _binding: FragmentLoginBinding ?= null
    private val binding get() = _binding!!
    private lateinit var username: TextInputEditText
    private lateinit var password: TextInputEditText
    private lateinit var loginButton: MaterialButton
    private var isLoggedIn = false
    private var isPaid = false
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) : View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        initUI()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loginButton.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val usernameText = username.text.toString()
                    val passwordText = password.text.toString()
                    val url = URL("${getString(R.string.base_url)}/Login")
                    val conn = url.openConnection() as HttpURLConnection
                    conn.requestMethod = "POST"
                    conn.setRequestProperty("Content-Type", "application/json")
                    conn.doOutput = true

                    val json = JSONObject()
                    json.put("username", usernameText)
                    json.put("password", passwordText)

                    conn.outputStream.use {
                        it.write(json.toString().toByteArray())
                    }

                    val response = conn.inputStream.bufferedReader().use { it.readText() }
                    val result = JSONObject(response)

                    isLoggedIn = result.getBoolean("isLoggedIn")
                    isPaid = result.getBoolean("isPaid")
                    if (!isLoggedIn){
                        withContext(Dispatchers.Main) {
                            MotionToast.createToast(requireActivity(),
                                "Warning",
                                getString(R.string.warning_message),
                                MotionToastStyle.WARNING,
                                MotionToast.GRAVITY_BOTTOM,
                                MotionToast.SHORT_DURATION,
                                ResourcesCompat.getFont(requireActivity(),R.font.normal))
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            (activity as MainActivity).isLoginSuccessful(usernameText, isPaid)
                        }
                    }
                } catch (_: Exception) {
                    withContext(Dispatchers.Main) {
                        MotionToast.createToast(requireActivity(),
                            "Error",
                            getString(R.string.error_message),
                            MotionToastStyle.ERROR,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.SHORT_DURATION,
                            ResourcesCompat.getFont(requireActivity(),R.font.normal))
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun initUI() {
        username = binding.usernameInput
        password = binding.passwordInput
        loginButton = binding.loginButton
    }
}