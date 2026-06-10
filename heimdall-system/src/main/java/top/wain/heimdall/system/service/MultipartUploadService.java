package top.wain.heimdall.system.service;

import org.springframework.web.multipart.MultipartFile;
import top.wain.heimdall.system.model.entity.FileDO;
import top.wain.heimdall.system.model.req.MultipartUploadInitReq;
import top.wain.heimdall.system.model.resp.file.MultipartUploadInitResp;
import top.wain.heimdall.system.model.resp.file.MultipartUploadResp;

/**
 * 分片上传业务接口
 *
 * @author KAI
 * @since 2025/7/3 8:42
 */
public interface MultipartUploadService {

    MultipartUploadInitResp initMultipartUpload(MultipartUploadInitReq multiPartUploadInitReq);

    MultipartUploadResp uploadPart(MultipartFile file, String uploadId, Integer partNumber, String path);

    FileDO completeMultipartUpload(String uploadId);

    void cancelMultipartUpload(String uploadId);
}
