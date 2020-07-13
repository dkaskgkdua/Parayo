package com.example.parayo.domain.product.registration

import com.example.parayo.common.ParayoException
import com.example.parayo.domain.product.ProductImage
import com.example.parayo.domain.product.ProductImageRepository
import net.coobird.thumbnailator.Thumbnails
import net.coobird.thumbnailator.geometry.Positions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


@Service
class ProductImageService @Autowired constructor(
    private val productImageRepository: ProductImageRepository
) {
    // Value 는 application.yml에 기입한 파일 업로드 디렉토리 설정을 읽어 변수에 대입해주는 역할
    @Value("\${parayo.file-upload.default-dir}")
    var uploadPath: String? = ""

    fun uploadImage(image: MultipartFile)
            : ProductImageUploadResponse {
        val filePath = saveImageFile(image)
        val productImage = saveImageData(filePath)

        return productImage.id?.let {
            ProductImageUploadResponse(it, filePath)
        } ?: throw ParayoException("이미지 저장 실패. 다시 시도해주세요.")
    }

    private fun saveImageFile(image: MultipartFile): String {
      val extension = image.originalFilename
          ?.takeLastWhile { it != '.' } // String의 takeLastWhilee 함수는  Char -> boolean 타입의 함수로 인자를 받음,
                                        // 함수의 반환값이 true가 되기 전까지의 마지막 문자열을 반환한다. 예제는 '.'을 기준으로 함
          ?: throw ParayoException("다른 이미지로 다시 시도해주세요.")
          // 파일명을 그대로 저장한다면 서로 다른 사용자가 같은 이름을 가진 파일을 업로드했을 때 그대로 덮어 쓸 위험이 있음. 랜덤 생성함
          val uuid = UUID.randomUUID().toString()
          val date = SimpleDateFormat("yyyyMMdd").format(Date())

        val filePath = "/images/$date/$uuid.$extension"
        val targetFile = File("$uploadPath/$filePath")
        val thumbnail = targetFile.absolutePath
            .replace(uuid, "$uuid-thumb")
            .let(::File)
        // 파일이 저장될 디렉토리를 생성하는 함수. 만약 에러가 뜬다면 쓰기 권한이 있는지 확인해야 한다. 셸의 chmod를 사용해야함.
        targetFile.parentFile.mkdir()
        // transferTo  함수는 MultipartFile 클래스에 선언된 함수로 업로드 팡리을 파라미터 지정된 파일 경로에 지정해주는 함수이다.
        //targetFile 변수는 /parayo/images/{년월일}/uuid.ext 형태의 경로를 가진 파일임. 이 위치에 이미지가 저장됨
        image.transferTo(targetFile)

        // Thumbnails.of 체인은 앞서 추가한 thumbnailator 라이브러리에서 제공하는 이미지 리사이징 함수를 사용한 것이다.
        // 가로세로 300, 정사각형으로 크롭 및 리사이징 해 uuid 앞에  thumb- 를 추가해 썸네일 파일로 저장했다.
        Thumbnails.of(targetFile)
            .crop(Positions.CENTER)
            .size(300,300)
            .outputFormat("jpg")
            .outputQuality(0.8f)
            .toFile(thumbnail)

        return filePath
    }

    private fun saveImageData(filePath: String): ProductImage {
        val productImage = ProductImage(filePath)
        return productImageRepository.save(productImage)
    }
}