package com.example.team26.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Helper factory function to create a ViewModelProvider.Factory
 *
 * Usage example:
 *                 val viewModel = viewModel<SomeViewModel>(
 *                     factory = viewModelFactory {
 *                         SomeViewModel(
 *                             PilotApp.someModule.someDependency,
 *                             PilotApp.someModule.someOtherDependency
 *                         )
 *                     }
*/

fun <VM: ViewModel> viewModelFactory(initializer: () -> VM): ViewModelProvider.Factory {
    return object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return initializer() as T
        }
    }
}