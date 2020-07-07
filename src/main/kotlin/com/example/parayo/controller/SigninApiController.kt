package com.example.parayo.controller

import com.example.parayo.common.ApiResponse
import com.example.parayo.domain.auth.JWTUtil
import com.example.parayo.domain.auth.SigninRequest
import com.example.parayo.domain.auth.SigninService
import com.example.parayo.domain.auth.UserContextHolder
import com.example.parayo.interceptor.TokenValidationInterceptor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1")
class SigninApiController @Autowired constructor(
    private val signinService: SigninService,
    private val userContextHolder: UserContextHolder
){
    @PostMapping("/signin")
    fun signin(@RequestBody signinRequest: SigninRequest) =
        ApiResponse.ok(signinService.signin(signinRequest))

    @PostMapping("/refresh_token")
    fun refreshToken(
        @RequestParam("grant_type") grantType: String
    ) : ApiResponse {
        if(grantType != TokenValidationInterceptor.GRANT_TYPE_REFRESH) {
            throw IllegalArgumentException("grant_type 없음")
        }
        return userContextHolder.email?.let {
            ApiResponse.ok(JWTUtil.createToken(it))
        } ?: throw java.lang.IllegalArgumentException("사용자 정보 없음")
    }
}