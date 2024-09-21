package com.member.exchange.util;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Objects;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.io.InputStream;

public class FileUtil {
    // 파일명에서 특수 문자 제거
    private static String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
    }

    // 조합 파일명 생성
    private static String generateUniqueFileName(String uuid, String originalFileName, String fileExtension) {
        try {
            // 파일명을 해시로 변환
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(originalFileName.getBytes());

            // 해시를 Base64로 인코딩하고 100자로 자름
            String encoded = Base64.getEncoder().encodeToString(hash);

            // '/' 문자를 '_'로, '+' 문자를 '-'로 대체
            String replaced = encoded.replace('/', '_').replace('+', '-');

            // 최대 길이를 100자로 제한
            String trimmed = replaced.substring(0, Math.min(replaced.length(), 100));

            // UUID와 함께 조합하여 파일명 생성
            return uuid + "：：" + trimmed + fileExtension;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hashing algorithm not found", e);
        }
    }

    // 파일 확장자 추출
    private static String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            return fileName.substring(dotIndex);
        }

        return "";
    }

    // 프로필 이미지 로컬 저장
    public static String saveProfileImage(MultipartFile profileImage, String uuid, String uploadPath) throws IOException {
        if (profileImage == null || profileImage.isEmpty()) {
            return null;
        }

        String originalFileName = profileImage.getOriginalFilename();
        String sanitizedFileName = sanitizeFileName(Objects.requireNonNull(originalFileName));
        String fileExtension = getFileExtension(originalFileName);
        String newProfileImage = generateUniqueFileName(uuid, sanitizedFileName, fileExtension);

        // newProfileImage 문자열을 Base64로 인코딩
        newProfileImage = Base64.getEncoder().encodeToString(newProfileImage.getBytes());

        Path uploadFolderPath = Paths.get(uploadPath);
        if (!Files.exists(uploadFolderPath)) {
            Files.createDirectories(uploadFolderPath);
        }

        Path filePath = Paths.get(uploadPath, newProfileImage);
        try (InputStream inputStream = profileImage.getInputStream()) {
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        }

        return newProfileImage;
    }
}

