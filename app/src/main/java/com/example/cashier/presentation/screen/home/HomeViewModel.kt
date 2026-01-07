package com.example.cashier.presentation.screen.home

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cashier.domain.model.Cashier
import com.example.cashier.domain.model.Resource
import com.example.cashier.domain.usecase.CashierUseCases
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class HomeViewModel(private val useCases: CashierUseCases) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        getAllCashier()
    }

    private fun getAllCashier() {
        useCases.getAllCashier().onEach {
            when (it) {
                is Resource.Loading -> _uiState.value = _uiState.value.copy(isLoading = true)
                is Resource.Success -> _uiState.value = _uiState.value.copy(cashierList = it.data ?: emptyList(), isLoading = false)
                is Resource.Error -> _uiState.value = _uiState.value.copy(errorMessage = it.message, isLoading = false)
            }
        }.launchIn(viewModelScope)
    }

    fun onEvent(event: HomeEvent) {
        viewModelScope.launch {
            when (event) {
                is HomeEvent.InsertOrUpdate -> {
                    val nameInput = _uiState.value.nameInput
                    val nameOutput = _uiState.value.nameOutput
                    val date = _uiState.value.date
                    val time = _uiState.value.time
                    val nominal = _uiState.value.nominal
                    val struck = _uiState.value.struck
                    if (nameInput.isBlank() || nameOutput.isBlank() || nominal.isBlank() || date.isBlank() || time.isBlank()) {
                        _uiState.value = _uiState.value.copy(errorMessage = "All fields must be filled")
                        return@launch
                    }

                    val cashier = Cashier(
                        id = event.id ?: 0,
                        nameInput = nameInput,
                        nameOutput = nameOutput,
                        date = date,
                        time = time,
                        nominal = nominal.toLong(),
                        struck = struck
                    )

                    if (isDuplicate(cashier)) {
                        _uiState.value = _uiState.value.copy(errorMessage = "Data already exists")
                        return@launch
                    }

                    if (event.id == null) {
                        useCases.insertCashier(cashier)
                    } else {
                        useCases.updateCashier(cashier)
                    }
                    _uiState.value = HomeUiState() // Reset the form
                }
                is HomeEvent.Delete -> {
                    useCases.deleteCashier(event.id)
                }
                is HomeEvent.OnNameInputChanged -> _uiState.value = _uiState.value.copy(nameInput = event.value)
                is HomeEvent.OnNameOutputChanged -> _uiState.value = _uiState.value.copy(nameOutput = event.value)
                is HomeEvent.OnDateChanged -> _uiState.value = _uiState.value.copy(date = event.value)
                is HomeEvent.OnTimeChanged -> _uiState.value = _uiState.value.copy(time = event.value)
                is HomeEvent.OnNominalChanged -> _uiState.value = _uiState.value.copy(nominal = event.value)
                is HomeEvent.OnStruckChanged -> {
                    _uiState.value = _uiState.value.copy(struck = event.value.toString())
                }
            }
        }
    }

    private fun isDuplicate(cashier: Cashier): Boolean {
        return _uiState.value.cashierList.any {
            it.nameInput == cashier.nameInput &&
                    it.nameOutput == cashier.nameOutput &&
                    it.nominal == cashier.nominal &&
                    it.date == cashier.date &&
                    it.time == cashier.time &&
                    it.id != cashier.id // Ignore the same item on update
        }
    }
}