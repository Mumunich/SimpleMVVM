package com.bignerdranch.android.foundation.model.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers


class IoDispatcher(
    val value: CoroutineDispatcher = Dispatchers.IO
)


class WorkerDispatcher(
    val value: CoroutineDispatcher = Dispatchers.Default
)