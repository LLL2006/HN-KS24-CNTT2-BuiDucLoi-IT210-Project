package com.re.it210project.service;

import com.re.it210project.model.dto.AuthRequest;
import com.re.it210project.model.dto.RegisterRequest;
import com.re.it210project.model.entity.SessionUser;

public interface AuthService {
    SessionUser login(AuthRequest request);
    void register(RegisterRequest request);
}