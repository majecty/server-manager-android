/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.observability.ui

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import androidx.lifecycle.Observer
import com.example.android.observability.Injection
import com.example.android.observability.R
import com.github.kittinunf.fuel.core.Parameters
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.fuel.livedata.liveDataResponseString
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_user.*

/**
 * Main screen of the app. Displays a user name and gives the option to update the user name.
 */
class UserActivity : AppCompatActivity() {

    private lateinit var viewModelFactory: ViewModelFactory

    private lateinit var viewModel: UserViewModel

    private val disposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        viewModelFactory = Injection.provideViewModelFactory(this)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(UserViewModel::class.java)
        update_user_button.setOnClickListener { updateUserName() }

        update_button.setOnClickListener {
            updateServerState()
        }
        updateServerState()

        start_button.setOnClickListener {
            startServer();
        }

        stop_button.setOnClickListener {
            stopServer();
        }
    }

    override fun onStart() {
        super.onStart()
        // Subscribe to the emissions of the user name from the view model.
        // Update the user name text view, at every onNext emission.
        // In case of error, log the exception.
        disposable.add(viewModel.userName()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ this.user_name.text = it },
                        { error -> Log.e(TAG, "Unable to get username", error) }))
    }

    override fun onStop() {
        super.onStop()

        // clear all the subscription
        disposable.clear()
    }

    private fun updateUserName() {
        val userName = user_name_input.text.toString()
        // Disable the update button until the user name update has been done
        update_user_button.isEnabled = false
        // Subscribe to updating the user name.
        // Enable back the button once the user name has been updated
        disposable.add(viewModel.updateUserName(userName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ update_user_button.isEnabled = true },
                        { error -> Log.e(TAG, "Unable to update username", error) }))
    }

    fun updateServerState() {
        Log.v("JH", "Update Button Clicked")
        "https://server-manager.majecty.tech/api/dev2/health"
                .httpPost()
                .body(""" { "apiKey": "caisson-outrage-kava-malapert-castanet-advised-armory-mink-church" } """)
                .liveDataResponseString()
                .observe(this, Observer {
                    val (response, result) = it
                    val (bytes, err) = result
                    if (bytes != null) {
                        Log.v("JH", "Success $bytes")
                        server_status.text = bytes
                    } else {
                        Log.e("JH", "Failed ${err.toString()}")
                        server_status.text = err.toString()
                    }
                })
    }

    fun startServer() {
        val timeout = 5000 // 5000 milliseconds = 5 seconds.
        val readTimeout = 60000 // 60000 milliseconds = 1 minute.

        Log.v("JH", "Start Button Clicked")
        "https://server-manager.majecty.tech/api/dev2/start"
                .httpPost()
                .body(""" { "apiKey": "caisson-outrage-kava-malapert-castanet-advised-armory-mink-church" } """)
                .timeout(timeout)
                .timeoutRead(readTimeout)
                .liveDataResponseString()
                .observe(this, Observer {
                    val (response, result) = it
                    val (bytes, err) = result
                    if (bytes != null) {
                        Log.v("JH", "Success $bytes")
                        server_status.text = bytes
                    } else {
                        Log.e("JH", "Failed ${err.toString()}")
                        server_status.text = err.toString()
                    }
                })
    }

    fun stopServer() {
        val timeout = 5000 // 5000 milliseconds = 5 seconds.
        val readTimeout = 60000 // 60000 milliseconds = 1 minute.

        Log.v("JH", "Stop Button Clicked")
        "https://server-manager.majecty.tech/api/dev2/stop"
                .httpPost()
                .body(""" { "apiKey": "caisson-outrage-kava-malapert-castanet-advised-armory-mink-church" } """)
                .timeout(timeout)
                .timeoutRead(readTimeout)
                .liveDataResponseString()
                .observe(this, Observer {
                    val (response, result) = it
                    val (bytes, err) = result
                    if (bytes != null) {
                        Log.v("JH", "Success $bytes")
                        server_status.text = bytes
                    } else {
                        Log.e("JH", "Failed ${err.toString()}")
                        server_status.text = err.toString()
                    }
                })
    }

    companion object {
        private val TAG = UserActivity::class.java.simpleName
    }
}
