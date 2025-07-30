package com.beyond.jellyorder.domain.test;

import com.beyond.jellyorder.common.apiResponse.ApiResponse;
import com.beyond.jellyorder.common.s3.S3Manager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/test")
public class TestController {

    private final S3Manager s3Manager;

    public TestController(S3Manager s3Manager) {
        this.s3Manager = s3Manager;
    }

    /**
     * S3에 이미지를 업로드합니다.
     *
     * @param file 업로드할 MultipartFile 형식의 이미지 파일
     * @return 업로드된 이미지의 S3 URL
     *
     * [prefix 설명]
     * - 두 번째 인자인 "test"는 S3 저장 경로 상의 디렉토리 prefix로 사용됩니다.
     * - 예: prefix가 "test"이고 파일명이 "image.png"인 경우,
     *       S3에 저장되는 key는 "test/{UUID}-image.png" 형식이 됩니다.
     * - 이를 통해 메뉴, 프로필, 배너 등 업로드 대상에 따라 구분된 디렉토리 구조를 유지할 수 있습니다.
     */
    @PostMapping("/s3upload")
    public ResponseEntity<?> s3Test(
            @RequestParam("file") MultipartFile file
    ) {
        String imageUrl = s3Manager.upload(file, "test");
        return ApiResponse.ok(imageUrl);
    }

    /**
     * S3에 업로드된 이미지 URL을 기반으로 해당 이미지를 삭제합니다.
     */
    @DeleteMapping("/s3delete")
    public ResponseEntity<?> s3Delete(@RequestParam("fileUrl") String fileUrl) {
        s3Manager.delete(fileUrl);
        return ApiResponse.ok("삭제가 완료되었습니다.");
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
