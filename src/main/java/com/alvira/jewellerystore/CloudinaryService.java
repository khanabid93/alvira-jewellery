package com.alvira.jewellerystore;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private Cloudinary cloudinary;

    @Value("${cloudinary.cloud-name}")
    private String cloudName;

    @Value("${cloudinary.api-key}")
    private String apiKey;

    @Value("${cloudinary.api-secret}")
    private String apiSecret;

    private Cloudinary getCloudinary() {
        if (cloudinary == null) {
            cloudinary = new Cloudinary(ObjectUtils.asMap(
                    "cloud_name", cloudName,
                    "api_key", apiKey,
                    "api_secret", apiSecret,
                    "secure", true
            ));
        }
        return cloudinary;
    }

    // Uploads any image or video file to Cloudinary and returns its public URL
    public String uploadFile(MultipartFile file) throws IOException {
        boolean isVideo = file.getContentType() != null && file.getContentType().startsWith("video");
        Map uploadResult = getCloudinary().uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "resource_type", isVideo ? "video" : "image",
                "folder", "alvira-jewellery"
        ));
        return uploadResult.get("secure_url").toString();
    }
}