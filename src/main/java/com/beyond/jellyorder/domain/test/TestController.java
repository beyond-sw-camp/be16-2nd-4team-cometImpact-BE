package com.beyond.jellyorder.domain.test;

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

    @PostMapping("/s3test")
    public ResponseEntity<?> s3Test(
            @RequestParam("file") MultipartFile file,
            @RequestParam("name") String name
    ) {
        TestDTO testDTO = new TestDTO(name, 0); // 임시 데이터
        String imageUrl = s3Uploader.uploadMenuImage(file, testDTO);
        return ApiResponse.ok(imageUrl);
    }

    // 형진 테스트
    @GetMapping("/hyungjin")
    public ResponseEntity<?> testHyungJin() {
        TestDTO testDTO = new TestDTO("hello", 10);
        return ApiResponse.created(testDTO, "커스텀한 메시지입니다.");
    }

    // 현지 테스트
    @GetMapping("/hyunji")
    public ResponseEntity<?> testHyunji() {
        TestDTO testDTO = new TestDTO("hello", 20);
        return ApiResponse.created(testDTO, "커스텀한 메시지입니다.");
    }

    // 혜성 테스트
    @GetMapping("/hyeseong")
    public ResponseEntity<?> testHyeseong() {
        TestDTO testDTO = new TestDTO("hello", 10);
        return ApiResponse.created(testDTO, "커스텀한 메시지입니다.");
    }

    // 진호 테스트
    @GetMapping("/jinho")
    public ResponseEntity<?> testJinho() {
        TestDTO testDTO = new TestDTO("hello", 23);
        return ApiResponse.ok(testDTO);
    }
}
