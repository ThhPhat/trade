package vn.bookstore.the4bookstore.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

@Service
public class CloudinaryService {

    private static final Logger log = LoggerFactory.getLogger(CloudinaryService.class);

    @Autowired(required = false)
    private Cloudinary cloudinary;

    /**
     * Kiểm tra xem dịch vụ Cloudinary đã được cấu hình API key hay chưa
     */
    public boolean isCloudinaryActive() {
        return cloudinary != null;
    }

    /**
     * Tải ảnh lên Cloudinary. Nếu Cloudinary chưa được cấu hình API Key hoặc lỗi mạng,
     * tự động lưu ảnh an toàn vào thư mục cục bộ (uploads/{folder}/) để hệ thống hoạt động bình thường 100%.
     *
     * @param file Tệp MultipartFile người dùng tải lên
     * @param folder Thư mục phân loại (ví dụ: "books", "avatars", "shops")
     * @return Đường dẫn URL công khai (HTTPS của Cloudinary hoặc đường dẫn /uploads/... cục bộ)
     * @throws IOException nếu có lỗi đọc file
     */
    public String uploadImage(MultipartFile file, String folder) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String subFolder = (folder != null && !folder.isBlank()) ? folder.trim() : "general";

        // 1. Ưu tiên tải lên Cloudinary nếu đã cấu hình
        if (cloudinary != null) {
            try {
                String targetFolder = "the4bookstore/" + subFolder;
                Map<?, ?> uploadResult = cloudinary.uploader().upload(
                        file.getBytes(),
                        ObjectUtils.asMap(
                                "folder", targetFolder,
                                "resource_type", "image"
                        )
                );
                Object secureUrl = uploadResult.get("secure_url");
                if (secureUrl != null) {
                    log.info("Tải ảnh lên Cloudinary thành công: {}", secureUrl);
                    return secureUrl.toString();
                }
            } catch (Exception ex) {
                log.warn("Không thể tải lên Cloudinary ({}), chuyển sang lưu trữ cục bộ: {}", ex.getMessage(), file.getOriginalFilename());
            }
        }

        // 2. Dự phòng: Lưu vào thư mục uploads/{folder}/ trên máy cục bộ
        Path uploadDir = Paths.get("uploads", subFolder);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        String originalFilename = file.getOriginalFilename();
        String extension = ".png";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
            if (!extension.matches("\\.(png|jpg|jpeg|webp|gif)")) {
                extension = ".png";
            }
        }

        String safeFileName = UUID.randomUUID().toString() + extension;
        Path targetLocation = uploadDir.resolve(safeFileName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        String localUrl = "/uploads/" + subFolder + "/" + safeFileName;
        log.info("Đã lưu ảnh cục bộ thành công: {}", localUrl);
        return localUrl;
    }
}
