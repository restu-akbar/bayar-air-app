package org.com.bayarair.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.com.bayarair.shared.home.HomePresenter
import org.com.bayarair.data.repository.GreetingRepo
import javax.inject.Inject

@HiltViewModel
class HomeViewModel
    @Inject
    constructor(
        repo: GreetingRepo,
    ) : ViewModel() {
        val presenter = HomePresenter(repo)

        override fun onCleared() {
            super.onCleared()
            presenter.clear()
        }
    }
