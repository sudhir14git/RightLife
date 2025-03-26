package com.example.rlapp.ai_package.data.repository

import com.example.rlapp.ai_package.data.remote.UnsplashApiService
import com.example.rlapp.ai_package.utils.StringUtils
import javax.inject.Inject

/**
 * This is an implementation of [ImagineRepository] to handle communication with [UnsplashApiService] server.
 */
class MainRepositoryImpl @Inject constructor(
    private val stringUtils: StringUtils,
    private val apiService: UnsplashApiService
) : MainRepository {


}
