package com.bignerdranch.android.simplemvvm.views.currentcolor


import android.Manifest
import kotlinx.coroutines.launch
import com.bignerdranch.android.foundation.model.PendingResult
import com.bignerdranch.android.foundation.model.SuccessResult
import com.bignerdranch.android.foundation.model.takeSuccess
import com.bignerdranch.android.foundation.sideeffects.dialogs.Dialogs
import com.bignerdranch.android.foundation.sideeffects.dialogs.plugin.DialogConfig
import com.bignerdranch.android.foundation.sideeffects.intents.Intents
import com.bignerdranch.android.foundation.sideeffects.navigator.Navigator
import com.bignerdranch.android.foundation.sideeffects.permissions.Permissions
import com.bignerdranch.android.foundation.sideeffects.permissions.plugin.PermissionStatus
import com.bignerdranch.android.foundation.sideeffects.resources.Resources
import com.bignerdranch.android.foundation.sideeffects.toasts.Toasts
import com.bignerdranch.android.foundation.views.BaseViewModel
import com.bignerdranch.android.foundation.views.LiveResult
import com.bignerdranch.android.foundation.views.MutableLiveResult
import com.bignerdranch.android.simplemvvm.R
import com.bignerdranch.android.simplemvvm.model.colors.ColorListener
import com.bignerdranch.android.simplemvvm.model.colors.ColorsRepository
import com.bignerdranch.android.simplemvvm.model.colors.NamedColor
import com.bignerdranch.android.simplemvvm.views.changecolor.ChangeColorFragment
import kotlinx.coroutines.flow.collect

class CurrentColorViewModel(
    private val navigator: Navigator,
    private val toasts: Toasts,
    private val resources: Resources,
    private val permissions: Permissions,
    private val intents: Intents,
    private val dialogs: Dialogs,
    private val colorsRepository: ColorsRepository,
) : BaseViewModel() {

    private val _currentColor = MutableLiveResult<NamedColor>(PendingResult())
    val currentColor: LiveResult<NamedColor> = _currentColor


    // Запускаем корутину,в ней терминальным методом коллект,слушаем обновление цвета и если он обновился,обновляем результат,бесконечно

    init {
        viewModelScope.launch {
            colorsRepository.listenCurrentColor().collect {
                _currentColor.postValue(SuccessResult(it))
            }
        }

        load()
    }

    // --- example of listening results directly from the screen

    override fun onResult(result: Any) {
        super.onResult(result)
        if (result is NamedColor) {
            val message = resources.getString(R.string.changed_color, result.name)
            toasts.toast(message)
        }
    }

    // ---

    fun changeColor() {
        val currentColor = currentColor.value.takeSuccess() ?: return
        val screen = ChangeColorFragment.Screen(currentColor.id)
        navigator.launch(screen)
    }

    /**
     * Example of using side-effect plugins
     */
    fun requestPermission() = viewModelScope.launch {
        val permission = Manifest.permission.ACCESS_FINE_LOCATION
        val hasPermission = permissions.hasPermissions(permission)
        if (hasPermission) {
            dialogs.show(createPermissionAlreadyGrantedDialog())
        } else {
            when (permissions.requestPermission(permission)) {
                PermissionStatus.GRANTED -> {
                    toasts.toast(resources.getString(R.string.permissions_grated))
                }
                PermissionStatus.DENIED -> {
                    toasts.toast(resources.getString(R.string.permissions_denied))
                }
                PermissionStatus.DENIED_FOREVER -> {
                    if (dialogs.show(createAskForLaunchingAppSettingsDialog())) {
                        intents.openAppSettings()
                    }
                }
            }
        }
    }

    fun tryAgain() {
        load()
    }

    private fun load() = into(_currentColor) { colorsRepository.getCurrentColor() }

    private fun createPermissionAlreadyGrantedDialog() = DialogConfig(
        title = resources.getString(R.string.dialog_permissions_title),
        message = resources.getString(R.string.permissions_already_granted),
        positiveButton = resources.getString(R.string.action_ok)
    )

    private fun createAskForLaunchingAppSettingsDialog() = DialogConfig(
        title = resources.getString(R.string.dialog_permissions_title),
        message = resources.getString(R.string.open_app_settings_message),
        positiveButton = resources.getString(R.string.action_open),
        negativeButton = resources.getString(R.string.action_cancel)
    )
}