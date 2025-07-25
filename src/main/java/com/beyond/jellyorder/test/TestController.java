package com.beyond.jellyorder.test;

import com.beyond.jellyorder.common.apiResponse.ApiResponse;
import com.beyond.jellyorder.common.s3.S3Uploader;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/test")
public class TestController {

    private final S3Uploader s3Uploader;

    public TestController(S3Uploader s3Uploader) {
        this.s3Uploader = s3Uploader;
    }

    @GetMapping
    public ResponseEntity<?> testConnect() {
        TestDTO testDTO = new TestDTO("hello", 10);
        return ApiResponse.created(testDTO, "커스텀한 메시지입니다.");
    }

    @PostMapping("/s3test")
    public ResponseEntity<?> s3Test(
            @RequestParam(value = "photo") MultipartFile photo
            ) {
        System.out.println(photo.getOriginalFilename());
        TestDTO testDTO = new TestDTO("이미지 파일 테스트", 10);
        String imageurl = s3Uploader.uploadMenuImage(photo, testDTO);
        return ApiResponse.ok(imageurl);
    }
    
    // feature1 테스트




}
