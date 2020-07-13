package com.example.parayo.domain.product.registration

data class ProductRegistrationRequest (
    val name: String,
    val description: String,
    val price: Int,
    val categoryId: Int,
    val imageIds: List<Long?>
// 상품이 기본 정보 외에 미리 등록된 이미지들의 id들을 리스트로 받기 위해 Long 타입의 리스트를 프로퍼티로 추가했다.
){
}