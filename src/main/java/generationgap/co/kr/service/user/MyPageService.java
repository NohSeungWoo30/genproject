package generationgap.co.kr.service.user;

import generationgap.co.kr.domain.mypage.*;
import generationgap.co.kr.domain.user.UserDTO;
import generationgap.co.kr.mapper.history.HistoryMapper;
import generationgap.co.kr.mapper.payment.PaymentMapper;
import generationgap.co.kr.mapper.post.BoardPostMapper;
import generationgap.co.kr.mapper.user.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class MyPageService {

    @Value("${upload.path}")
    private String uploadPath;

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final BoardPostMapper boardPostMapper;
    private final HistoryMapper historyMapper;
    private final PaymentMapper paymentMapper;

    @Autowired
    public MyPageService(UserMapper userMapper, PasswordEncoder passwordEncoder,
                         BoardPostMapper boardPostMapper, HistoryMapper historyMapper, PaymentMapper paymentMapper) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.boardPostMapper = boardPostMapper;
        this.historyMapper = historyMapper;
        this.paymentMapper = paymentMapper;
    }

    public List<PostDto> getMyPosts(Long userIdx) {
        return boardPostMapper.findPostsByAuthorIdx(userIdx);
    }

    public List<HistoryDto> getMyHistory(Long userIdx) {
        return historyMapper.findHistoryByUserIdx(userIdx);
    }

    public List<PaymentDto> getMyPayments(Long userIdx) {
        return paymentMapper.findPaymentsByUserIdx(userIdx);
    }

    @Transactional
    public void updateInfo(UpdateInfoDTO dto) {
        userMapper.updateUserInfo(dto);
    }

    @Transactional
    public boolean changePassword(ChangePasswordDTO dto) {
        UserDTO currentUser = userMapper.findByUserIdx(dto.getUserIdx());
        if (currentUser == null || !passwordEncoder.matches(dto.getCurrentPassword(), currentUser.getPasswordHash())) {
            return false;
        }
        String newPasswordHash = passwordEncoder.encode(dto.getNewPassword());
        UserDTO userToUpdate = new UserDTO();
        userToUpdate.setUserIdx(dto.getUserIdx());
        userToUpdate.setPasswordHash(newPasswordHash);
        userMapper.updateUserPassword(userToUpdate);
        return true;
    }

    @Transactional
    public void updateProfileImage(Long userIdx, MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return;
        }
        String originalFilename = file.getOriginalFilename();
        String newFilename = UUID.randomUUID().toString() + "_" + originalFilename;

        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        File dest = new File(uploadPath + newFilename);
        file.transferTo(dest);

        UserDTO userToUpdate = new UserDTO();
        userToUpdate.setUserIdx(userIdx);
        userToUpdate.setProfileName(newFilename);
        userMapper.updateUserProfileImage(userToUpdate);
    }
}