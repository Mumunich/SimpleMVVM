package com.bignerdranch.android.foundation.views

import java.io.Serializable

interface BaseScreen : Serializable{
    companion object {
        const val ARG_SCREEN = "ARG_SCREEN"
    }
}