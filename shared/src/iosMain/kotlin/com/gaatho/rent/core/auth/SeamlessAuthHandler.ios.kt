package com.gaatho.rent.core.auth

import com.skydoves.sandwich.ApiResponse

class IosSeamlessAuthHandler : SeamlessAuthHandler {
    override suspend fun requestProfile(autoSelect: Boolean): ApiResponse<SeamlessProfile> {
        // iOS implementation could use ASAuthorizationController (Sign in with Apple)
        // or a similar mechanism for seamless Google Sign In.
        // For now, we return a failure to trigger the standard flow.
        return ApiResponse.Failure.Exception(
            NotImplementedError("Seamless profile request not yet implemented on iOS")
        )
    }
}
