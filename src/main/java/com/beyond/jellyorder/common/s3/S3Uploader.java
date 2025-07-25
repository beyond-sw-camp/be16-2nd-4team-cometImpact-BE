package com.beyond.jellyorder.common.s3;

import com.beyond.jellyorder.domain.test.TestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class S3Uploader {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    /**
     * 사용자 프로필 이미지를 업로드하고 URL을 반환합니다.
     */
    public String uploadMenuImage(MultipartFile menuImage, TestDTO testDTO) {
        // image명 설정
        String fileName = "menu-" + testDTO.getName() + "-menuImage-" + menuImage.getOriginalFilename();

        // 저장 객체 구성
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(fileName)
                .contentType(menuImage.getContentType()) //image/jpeg, video/mp4...
                .build();

        // 이미지를 업로드(byte형태로)
        try {
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(menuImage.getBytes()));
        } catch (IOException e) {
            // checked -> unchecked로 바꿔 전체 rollback되도록 예외처리
            throw new RuntimeException(e);
        }

        // 이미지 url 추출
        String imgUrl = s3Client.utilities().getUrl(a->a.bucket(bucket).key(fileName)).toExternalForm();

//        author.updateImageUlr(imgUrl);
        return imgUrl;
    }

    /**
     * S3에 저장된 파일을 삭제합니다.
     */
    public void deleteFileByUrl() {
    }
}

