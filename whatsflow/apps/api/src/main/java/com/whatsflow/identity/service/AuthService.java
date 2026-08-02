package com.whatsflow.identity.service;


import com.whatsflow.company.domain.Company;
import com.whatsflow.company.repository.CompanyRepository;
import com.whatsflow.exception.BusinessException;
import com.whatsflow.exception.ErrorCode;
import com.whatsflow.identity.domain.RefreshToken;
import com.whatsflow.identity.domain.Role;
import com.whatsflow.identity.domain.UserAccount;
import com.whatsflow.identity.dto.AuthResponse;
import com.whatsflow.identity.dto.LoginRequest;
import com.whatsflow.identity.dto.RegisterRequest;
import com.whatsflow.identity.repository.RefreshTokenRepository;
import com.whatsflow.identity.repository.RoleRepository;
import com.whatsflow.identity.repository.UserAccountRepository;
import com.whatsflow.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class AuthService {
    private final CompanyRepository companies;
    private final UserAccountRepository users;
    private final RoleRepository roles;
    private final RefreshTokenRepository refreshTokens;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthService(CompanyRepository companies, UserAccountRepository users, RoleRepository roles,
                       RefreshTokenRepository refreshTokens, PasswordEncoder encoder, JwtService jwt) {
        this.companies = companies; this.users = users; this.roles = roles;
        this.refreshTokens = refreshTokens; this.encoder = encoder; this.jwt = jwt;
    }

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        users.findByEmailIgnoreCaseAndDeletedFalse(req.email()).ifPresent(u -> {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        });
        Company company = new Company();
        company.setName(req.companyName());
        company.setSlug(slugify(req.companyName()) + "-" + UUID.randomUUID().toString().substring(0, 8));
        company.setStatus("ACTIVE");
        companies.save(company);

        Role admin = new Role();
        admin.setTenantId(company.getId());
        admin.setCode("COMPANY_ADMIN");
        admin.setName("Company Admin");
        roles.save(admin);

        UserAccount user = new UserAccount();
        user.setTenantId(company.getId());
        user.setEmail(req.email().toLowerCase(Locale.ROOT));
        user.setPasswordHash(encoder.encode(req.password()));
        user.setFullName(req.fullName());
        user.setEmailVerified(false);
        user.setEnabled(true);
        users.save(user);

        return issueTokens(user, List.of("COMPANY_ADMIN"));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        UserAccount user = users.findByEmailIgnoreCaseAndDeletedFalse(req.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));
        if (!encoder.matches(req.password(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
        return issueTokens(user, List.of("COMPANY_ADMIN"));
    }

    @Transactional
    public AuthResponse refresh(String rawRefresh) {
        String hash = sha256(rawRefresh);
        RefreshToken token = refreshTokens.findByTokenHashAndDeletedFalse(hash)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN));
        if (token.isRevoked() || token.getExpiresAt().isBefore(Instant.now())) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
        token.setRevoked(true);
        UserAccount user = users.findById(token.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN));
        return issueTokens(user, List.of("COMPANY_ADMIN"));
    }

    private AuthResponse issueTokens(UserAccount user, List<String> roleList) {
        String access = jwt.createAccessToken(user.getId(), user.getTenantId(), user.getEmail(), roleList);
        String refresh = UUID.randomUUID() + "." + UUID.randomUUID();
        RefreshToken rt = new RefreshToken();
        rt.setTenantId(user.getTenantId());
        rt.setUserId(user.getId());
        rt.setTokenHash(sha256(refresh));
        rt.setFamilyId(UUID.randomUUID());
        rt.setExpiresAt(Instant.now().plusSeconds(14L * 24 * 3600));
        refreshTokens.save(rt);
        return new AuthResponse(access, refresh, user.getId(), user.getTenantId(), user.getEmail(), roleList);
    }

    private static String slugify(String name) {
        return name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
    }

    private static String sha256(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
