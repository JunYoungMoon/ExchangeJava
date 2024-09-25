package com.mjy.exchange.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URLConnection;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/profile")
public class ProfileController {
    @Value("${upload.profile.directory}")
    private String uploadPath;

    private final MessageSourceAccessor messageSourceAccessor;

    @Autowired
    public ProfileController(MessageSourceAccessor messageSourceAccessor) {
        this.messageSourceAccessor = messageSourceAccessor;
    }

    @Operation(summary = "프로필 이미지 제공", description = "url로 접근할때 프로필 이미지를 제공합니다.", security = {@SecurityRequirement(name = "csrfToken")})
    @GetMapping("/{fileName:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String fileName) {
        // 외부 디렉터리 경로 설정
        Path filePath = Paths.get(Paths.get("").toAbsolutePath().normalize() + uploadPath).resolve(fileName).normalize();

        // 파일 읽기 및 Resource 객체 생성
        Resource resource;
        try {
            resource = new UrlResource(filePath.toUri());
        } catch (MalformedURLException e) {
            String errorMessage = messageSourceAccessor.getMessage("profile.serveFile.fileFind.error.message");

            throw new RuntimeException(errorMessage);
        }

        // 파일의 MIME 타입 동적으로 결정
        String contentType;
        try {
            contentType = determineMimeType(resource);
        } catch (IOException e) {
            String errorMessage = messageSourceAccessor.getMessage("profile.serveFile.fileMime.error.message");

            throw new RuntimeException(errorMessage);
        }

        // 파일 다운로드를 위한 HttpHeaders 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }

    private String determineMimeType(Resource resource) throws IOException {
        try (InputStream inputStream = resource.getInputStream()) {
            return URLConnection.guessContentTypeFromStream(inputStream);
        }
    }
}
