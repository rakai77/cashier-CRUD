package com.example.cashier.presentation.screen.home

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
        useCases.getAllCashier().onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        cashierList = result.data ?: emptyList(),
                        isLoading = false
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            }
        }.launchIn(viewModelScope)
    }

    fun onEvent(event: HomeEvent) {
        viewModelScope.launch {
            when (event) {
                is HomeEvent.InsertOrUpdate -> {
                    if (!validateForm()) return@launch

                    val nominalValue = try {
                        _uiState.value.nominal.toLong()
                    } catch (e: NumberFormatException) {
                        _uiState.value = _uiState.value.copy(
                            nominalError = "Invalid number"
                        )
                        return@launch
                    }

                    val cashier = Cashier(
                        id = if (event.mode == "update") event.id ?: 0 else 0,
                        nameInput = _uiState.value.nameInput,
                        nameOutput = _uiState.value.nameOutput,
                        date = _uiState.value.date,
                        time = _uiState.value.time,
                        nominal = nominalValue,
                        struck = _uiState.value.struck,
                        description = _uiState.value.description
                    )

                    if (event.mode == "add") {
                        useCases.insertCashier(cashier)
                    } else {
                        useCases.updateCashier(cashier)
                    }
                    _uiState.value = _uiState.value.copy(isSaved = true)
                }

                is HomeEvent.Delete -> {
                    useCases.deleteCashier(event.id)
                }

                is HomeEvent.LoadCashier -> {
                    useCases.getCashierById(event.id).onEach { result ->
                        when (result) {
                            is Resource.Loading -> {
                                _uiState.value = _uiState.value.copy(isLoading = true)
                            }
                            is Resource.Success -> {
                                val cashier = result.data
                                _uiState.value = _uiState.value.copy(
                                    cashier = cashier,
                                    nameInput = cashier?.nameInput ?: "",
                                    nameOutput = cashier?.nameOutput ?: "",
                                    date = cashier?.date ?: "",
                                    time = cashier?.time ?: "",
                                    nominal = cashier?.nominal?.toString() ?: "",
                                    struck = cashier?.struck ?: "",
                                    description = cashier?.description ?: "",
                                    isSaved = false,
                                    isLoading = false
                                )
                            }
                            is Resource.Error -> {
                                _uiState.value = _uiState.value.copy(
                                    isSaved = false,
                                    isLoading = false
                                )
                            }
                        }
                    }.launchIn(viewModelScope)
                }

                is HomeEvent.ClearForm -> {
                    _uiState.value = HomeUiState(
                        cashierList = _uiState.value.cashierList
                    )
                }

                is HomeEvent.ClearStruckImage -> {
                    _uiState.value = _uiState.value.copy(struck = "")
                }

                is HomeEvent.OnNameInputChanged -> {
                    _uiState.value = _uiState.value.copy(
                        nameInput = event.value,
                        nameInputError = null
                    )
                }

                is HomeEvent.OnNameOutputChanged -> {
                    _uiState.value = _uiState.value.copy(
                        nameOutput = event.value,
                        nameOutputError = null
                    )
                }

                is HomeEvent.OnDateChanged -> {
                    _uiState.value = _uiState.value.copy(
                        date = event.value,
                        dateError = null
                    )
                }

                is HomeEvent.OnTimeChanged -> {
                    _uiState.value = _uiState.value.copy(
                        time = event.value,
                        timeError = null
                    )
                }

                is HomeEvent.OnNominalChanged -> {
                    _uiState.value = _uiState.value.copy(
                        nominal = event.value,
                        nominalError = null
                    )
                }

                is HomeEvent.OnStruckChanged -> {
                    _uiState.value = _uiState.value.copy(
                        struck = event.value.toString()
                    )
                }
                is HomeEvent.OnDescChanged -> {
                    _uiState.value = _uiState.value.copy(
                        description = event.value
                    )
                }
            }
        }
    }

    private fun validateForm(): Boolean {
        val nameInputError = if (_uiState.value.nameInput.isBlank()) {
            "Name Input is required"
        } else null

        val nameOutputError = if (_uiState.value.nameOutput.isBlank()) {
            "Name Output is required"
        } else null

        val dateError = if (_uiState.value.date.isBlank()) {
            "Date is required"
        } else null

        val timeError = if (_uiState.value.time.isBlank()) {
            "Time is required"
        } else null

        val nominalError = when {
            _uiState.value.nominal.isBlank() -> "Nominal is required"
            _uiState.value.nominal.toLongOrNull() == null -> "Must be a valid number"
            else -> null
        }

        _uiState.value = _uiState.value.copy(
            nameInputError = nameInputError,
            nameOutputError = nameOutputError,
            dateError = dateError,
            timeError = timeError,
            nominalError = nominalError
        )

        return nameInputError == null &&
                nameOutputError == null &&
                dateError == null &&
                timeError == null &&
                nominalError == null
    }
}
