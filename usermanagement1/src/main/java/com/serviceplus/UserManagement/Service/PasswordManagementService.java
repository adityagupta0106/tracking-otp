package com.serviceplus.UserManagement.Service;

import com.serviceplus.UserManagement.util.Utility;
import org.springframework.stereotype.Service;

import static com.serviceplus.UserManagement.util.CryptoUtil.md5;
import static com.serviceplus.UserManagement.util.CryptoUtil.sha256;

@Service
public class PasswordManagementService {

    public String generateSecurePassword() {
        return Utility.generateSecurePassword(16);
    }

    public String hashPassword(String rawPassword) {
        return sha256(md5(rawPassword));
    }
}
