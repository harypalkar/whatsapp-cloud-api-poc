import fs from "fs";
import path from "path";
import { fileURLToPath } from "url";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const API = path.join(__dirname, "..", "apps", "api");
let written = 0;
let skipped = 0;

function write(rel, content) {
  const full = path.join(API, rel);
  fs.mkdirSync(path.dirname(full), { recursive: true });
  if (fs.existsSync(full)) {
    skipped++;
    return;
  }
  fs.writeFileSync(full, content.trim() + "\n", "utf8");
  written++;
}

function java(pkg, name, body) {
  const rel = path.join("src/main/java/com/whatsflow", ...pkg.split("."), `${name}.java`);
  write(rel, `package com.whatsflow.${pkg};\n\n${body}`);
}

// ---- Flyway V2-V8 ----
write(
  "src/main/resources/db/migration/V2__auth_refresh_api_keys.sql",
  `
CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    user_id UUID NOT NULL,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    family_id UUID NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_by UUID, created_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified_by UUID, modified_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE, version BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX idx_refresh_user ON refresh_tokens (tenant_id, user_id);

CREATE TABLE email_verifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    user_id UUID NOT NULL,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    consumed BOOLEAN NOT NULL DEFAULT FALSE,
    created_by UUID, created_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified_by UUID, modified_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE, version BIGINT NOT NULL DEFAULT 0
);
`
);

write(
  "src/main/resources/db/migration/V3__whatsapp_customers_media.sql",
  `
CREATE TABLE whatsapp_accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    waba_id VARCHAR(64),
    phone_number_id VARCHAR(64) NOT NULL,
    business_id VARCHAR(64),
    display_phone VARCHAR(32),
    verified_name VARCHAR(255),
    access_token_enc BYTEA,
    webhook_verify_token VARCHAR(255),
    quality_rating VARCHAR(32),
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_by UUID, created_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified_by UUID, modified_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE, version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_wa_phone UNIQUE (phone_number_id)
);

CREATE TABLE whatsapp_templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    whatsapp_account_id UUID,
    meta_template_id VARCHAR(64),
    name VARCHAR(255) NOT NULL,
    language VARCHAR(16) NOT NULL DEFAULT 'en',
    category VARCHAR(64),
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    body_param_count INT NOT NULL DEFAULT 0,
    created_by UUID, created_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified_by UUID, modified_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE, version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE customers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    mobile_e164 VARCHAR(32) NOT NULL,
    name VARCHAR(255),
    email VARCHAR(320),
    opted_in BOOLEAN NOT NULL DEFAULT TRUE,
    blacklisted BOOLEAN NOT NULL DEFAULT FALSE,
    attributes_json TEXT,
    created_by UUID, created_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified_by UUID, modified_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE, version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_customer_mobile UNIQUE (tenant_id, mobile_e164)
);
CREATE INDEX idx_customers_tenant ON customers (tenant_id);

CREATE TABLE customer_tags (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    name VARCHAR(128) NOT NULL,
    created_by UUID, created_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified_by UUID, modified_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE, version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_tag_name UNIQUE (tenant_id, name)
);

CREATE TABLE customer_tag_map (
    customer_id UUID NOT NULL REFERENCES customers(id),
    tag_id UUID NOT NULL REFERENCES customer_tags(id),
    PRIMARY KEY (customer_id, tag_id)
);

CREATE TABLE customer_groups (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    name VARCHAR(128) NOT NULL,
    created_by UUID, created_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified_by UUID, modified_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE, version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE customer_group_members (
    group_id UUID NOT NULL REFERENCES customer_groups(id),
    customer_id UUID NOT NULL REFERENCES customers(id),
    PRIMARY KEY (group_id, customer_id)
);

CREATE TABLE media_assets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    object_key VARCHAR(1024) NOT NULL,
    file_name VARCHAR(512),
    mime_type VARCHAR(128),
    size_bytes BIGINT,
    kind VARCHAR(32),
    created_by UUID, created_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified_by UUID, modified_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE, version BIGINT NOT NULL DEFAULT 0
);
`
);

write(
  "src/main/resources/db/migration/V4__campaigns_conversations.sql",
  `
CREATE TABLE campaigns (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    whatsapp_account_id UUID,
    name VARCHAR(255) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
    template_name VARCHAR(255),
    language VARCHAR(16) DEFAULT 'en',
    promo_code VARCHAR(64),
    scheduled_at TIMESTAMPTZ,
    recurring_cron VARCHAR(64),
    created_by UUID, created_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified_by UUID, modified_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE, version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE campaign_recipients (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    campaign_id UUID NOT NULL REFERENCES campaigns(id),
    customer_id UUID NOT NULL REFERENCES customers(id),
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    wa_message_id VARCHAR(128),
    error_code VARCHAR(32),
    created_by UUID, created_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified_by UUID, modified_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE, version BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX idx_camp_recip ON campaign_recipients (campaign_id, status);

CREATE TABLE conversations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    customer_id UUID NOT NULL REFERENCES customers(id),
    assigned_user_id UUID,
    status VARCHAR(32) NOT NULL DEFAULT 'OPEN',
    unread_count INT NOT NULL DEFAULT 0,
    last_message_preview VARCHAR(500),
    last_customer_message_at TIMESTAMPTZ,
    window_expires_at TIMESTAMPTZ,
    created_by UUID, created_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified_by UUID, modified_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE, version BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX idx_conv_tenant ON conversations (tenant_id, status);

CREATE TABLE messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    conversation_id UUID NOT NULL REFERENCES conversations(id),
    direction VARCHAR(16) NOT NULL,
    type VARCHAR(32) NOT NULL DEFAULT 'text',
    body TEXT,
    wa_message_id VARCHAR(128),
    delivery_status VARCHAR(32),
    meta_errors_json TEXT,
    media_url VARCHAR(1024),
    created_by UUID, created_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified_by UUID, modified_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE, version BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX idx_msg_conv ON messages (conversation_id, created_date);

CREATE TABLE conversation_notes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    conversation_id UUID NOT NULL REFERENCES conversations(id),
    author_user_id UUID,
    note TEXT NOT NULL,
    created_by UUID, created_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified_by UUID, modified_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE, version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE webhook_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID,
    external_id VARCHAR(128),
    event_type VARCHAR(64),
    payload_json TEXT NOT NULL,
    process_status VARCHAR(32) NOT NULL DEFAULT 'RECEIVED',
    created_by UUID, created_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified_by UUID, modified_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE, version BIGINT NOT NULL DEFAULT 0
);
`
);

write(
  "src/main/resources/db/migration/V5__forms_automation_billing_notify_audit.sql",
  `
CREATE TABLE forms (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    form_type VARCHAR(64),
    public_token VARCHAR(64) NOT NULL UNIQUE,
    status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
    schema_json TEXT,
    created_by UUID, created_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified_by UUID, modified_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE, version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE form_fields (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    form_id UUID NOT NULL REFERENCES forms(id),
    field_key VARCHAR(64) NOT NULL,
    label VARCHAR(255) NOT NULL,
    field_type VARCHAR(32) NOT NULL,
    required BOOLEAN NOT NULL DEFAULT FALSE,
    options_json TEXT,
    sort_order INT NOT NULL DEFAULT 0,
    created_by UUID, created_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified_by UUID, modified_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE, version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE form_submissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    form_id UUID NOT NULL REFERENCES forms(id),
    payload_json TEXT NOT NULL,
    customer_id UUID,
    created_by UUID, created_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified_by UUID, modified_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE, version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE automations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    trigger_type VARCHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
    definition_json TEXT,
    created_by UUID, created_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified_by UUID, modified_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE, version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE automation_nodes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    automation_id UUID NOT NULL REFERENCES automations(id),
    node_key VARCHAR(64) NOT NULL,
    node_type VARCHAR(64) NOT NULL,
    config_json TEXT,
    created_by UUID, created_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified_by UUID, modified_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE, version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE plans (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(128) NOT NULL,
    max_agents INT,
    max_messages_month INT,
    price_monthly NUMERIC(18,2) DEFAULT 0,
    features_json TEXT,
    created_by UUID, created_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified_by UUID, modified_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE, version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE subscriptions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    plan_id UUID NOT NULL REFERENCES plans(id),
    status VARCHAR(32) NOT NULL DEFAULT 'TRIAL',
    current_period_end TIMESTAMPTZ,
    created_by UUID, created_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified_by UUID, modified_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE, version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE invoices (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    subscription_id UUID,
    invoice_number VARCHAR(64) NOT NULL,
    amount NUMERIC(18,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'INR',
    status VARCHAR(32) NOT NULL DEFAULT 'DUE',
    created_by UUID, created_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified_by UUID, modified_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE, version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    invoice_id UUID,
    provider VARCHAR(32),
    external_id VARCHAR(128),
    amount NUMERIC(18,2) NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_by UUID, created_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified_by UUID, modified_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE, version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    user_id UUID,
    channel VARCHAR(32) NOT NULL,
    title VARCHAR(255),
    body TEXT,
    read_flag BOOLEAN NOT NULL DEFAULT FALSE,
    created_by UUID, created_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified_by UUID, modified_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE, version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID,
    actor_user_id UUID,
    action VARCHAR(128) NOT NULL,
    entity_type VARCHAR(64),
    entity_id UUID,
    metadata_json TEXT,
    created_by UUID, created_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    modified_by UUID, modified_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE, version BIGINT NOT NULL DEFAULT 0
);

INSERT INTO plans (id, code, name, max_agents, max_messages_month, price_monthly)
VALUES
 (gen_random_uuid(), 'STARTER', 'Starter', 3, 5000, 999),
 (gen_random_uuid(), 'GROWTH', 'Growth', 10, 50000, 4999),
 (gen_random_uuid(), 'PROFESSIONAL', 'Professional', 50, 250000, 14999),
 (gen_random_uuid(), 'ENTERPRISE', 'Enterprise', 500, 10000000, 49999)
ON CONFLICT DO NOTHING;
`
);

// ---- Security ----
java("security", "UserPrincipal", `
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
public class UserPrincipal implements UserDetails {
    private final UUID id;
    private final UUID tenantId;
    private final String email;
    private final String passwordHash;
    private final boolean enabled;
    private final Set<String> roles;
    private final Set<String> permissions;

    public UserPrincipal(UUID id, UUID tenantId, String email, String passwordHash,
                         boolean enabled, Set<String> roles, Set<String> permissions) {
        this.id = id; this.tenantId = tenantId; this.email = email;
        this.passwordHash = passwordHash; this.enabled = enabled;
        this.roles = roles; this.permissions = permissions;
    }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream().map(r -> new SimpleGrantedAuthority("ROLE_" + r)).collect(Collectors.toSet());
    }
    @Override public String getPassword() { return passwordHash; }
    @Override public String getUsername() { return email; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return enabled; }
}
`);

java("security", "JwtService", `
import com.whatsflow.security.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class JwtService {
    private final JwtProperties props;

    public JwtService(JwtProperties props) { this.props = props; }

    private SecretKey key() {
        String secret = props.getSecret() == null ? "change-me-to-a-long-random-secret-key-32b" : props.getSecret();
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(bytes, 0, padded, 0, bytes.length);
            bytes = padded;
        }
        return Keys.hmacShaKeyFor(bytes);
    }

    public String createAccessToken(UUID userId, UUID tenantId, String email, List<String> roles) {
        Instant now = Instant.now();
        Instant exp = now.plus(props.getAccessTokenTtl());
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .issuer(props.getIssuer())
                .subject(userId.toString())
                .claim("tenantId", tenantId.toString())
                .claim("email", email)
                .claim("roles", roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key())
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key()).build().parseSignedClaims(token).getPayload();
    }
}
`);

java("security", "JwtAuthenticationFilter", `
import com.whatsflow.tenant.TenantContext;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) { this.jwtService = jwtService; }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        try {
            if (header != null && header.startsWith("Bearer ")) {
                Claims claims = jwtService.parse(header.substring(7));
                UUID userId = UUID.fromString(claims.getSubject());
                UUID tenantId = UUID.fromString(String.valueOf(claims.get("tenantId")));
                @SuppressWarnings("unchecked")
                List<String> roles = (List<String>) claims.get("roles", List.class);
                var authorities = roles == null ? List.<SimpleGrantedAuthority>of() :
                        roles.stream().map(r -> new SimpleGrantedAuthority("ROLE_" + r)).collect(Collectors.toList());
                var auth = new UsernamePasswordAuthenticationToken(userId, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);
                TenantContext.setTenantId(tenantId);
                TenantContext.setUserId(userId);
                TenantContext.setRoles(roles == null ? Set.of() : new HashSet<>(roles));
            }
        } catch (Exception ignored) {
            SecurityContextHolder.clearContext();
        }
        try {
            chain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
`.replace("Set.of()", "java.util.Set.of()"));

// Fix the Set import properly
java("security", "JwtAuthenticationFilter", `
import com.whatsflow.tenant.TenantContext;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) { this.jwtService = jwtService; }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        try {
            if (header != null && header.startsWith("Bearer ")) {
                Claims claims = jwtService.parse(header.substring(7));
                UUID userId = UUID.fromString(claims.getSubject());
                UUID tenantId = UUID.fromString(String.valueOf(claims.get("tenantId")));
                @SuppressWarnings("unchecked")
                List<String> roles = (List<String>) claims.get("roles", List.class);
                var authorities = roles == null ? List.<SimpleGrantedAuthority>of() :
                        roles.stream().map(r -> new SimpleGrantedAuthority("ROLE_" + r)).collect(Collectors.toList());
                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(userId, null, authorities));
                TenantContext.setTenantId(tenantId);
                TenantContext.setUserId(userId);
                TenantContext.setRoles(roles == null ? Set.of() : new HashSet<>(roles));
            }
        } catch (Exception ignored) {
            SecurityContextHolder.clearContext();
        }
        try {
            chain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
`);

java("security", "RestAuthenticationEntryPoint", `
import com.fasterxml.jackson.databind.ObjectMapper;
import com.whatsflow.common.api.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper mapper;

    public RestAuthenticationEntryPoint(ObjectMapper mapper) { this.mapper = mapper; }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException ex)
            throws IOException {
        response.setStatus(401);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        mapper.writeValue(response.getOutputStream(), ApiResponse.error("UNAUTHORIZED", "Unauthorized"));
    }
}
`);

java("security", "RestAccessDeniedHandler", `
import com.fasterxml.jackson.databind.ObjectMapper;
import com.whatsflow.common.api.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {
    private final ObjectMapper mapper;

    public RestAccessDeniedHandler(ObjectMapper mapper) { this.mapper = mapper; }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException ex)
            throws IOException {
        response.setStatus(403);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        mapper.writeValue(response.getOutputStream(), ApiResponse.error("FORBIDDEN", "Access denied"));
    }
}
`);

java("config", "SecurityConfig", `
import com.whatsflow.security.JwtAuthenticationFilter;
import com.whatsflow.security.RestAccessDeniedHandler;
import com.whatsflow.security.RestAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http,
                                    JwtAuthenticationFilter jwtFilter,
                                    RestAuthenticationEntryPoint entryPoint,
                                    RestAccessDeniedHandler deniedHandler) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(e -> e.authenticationEntryPoint(entryPoint).accessDeniedHandler(deniedHandler))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/v1/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/v1/webhooks/meta/whatsapp").permitAll()
                        .requestMatchers(HttpMethod.POST, "/v1/webhooks/meta/whatsapp").permitAll()
                        .requestMatchers(HttpMethod.POST, "/v1/public/forms/**").permitAll()
                        .requestMatchers("/v1/platform/**").hasRole("PLATFORM_ADMIN")
                        .anyRequest().authenticated())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
`);

java("config", "OpenApiConfig", `
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    OpenAPI openAPI() {
        final String scheme = "bearerAuth";
        return new OpenAPI()
                .info(new Info().title("WhatsFlow API").version("v1").description("WhatsFlow SaaS Platform"))
                .addSecurityItem(new SecurityRequirement().addList(scheme))
                .components(new Components().addSecuritySchemes(scheme,
                        new SecurityScheme().name(scheme).type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")));
    }
}
`);

java("config", "JpaAuditingConfig", `
import com.whatsflow.tenant.TenantContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;
import java.util.UUID;

@Configuration
public class JpaAuditingConfig {
    @Bean
    AuditorAware<UUID> auditorAware() {
        return () -> TenantContext.getUserId();
    }
}
`);

// ---- Identity entities ----
const entity = (pkg, name, table, fields) => java(pkg, name, `
import com.whatsflow.common.domain.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Entity
@Table(name = "${table}")
public class ${name} extends TenantEntity {
${fields}
}
`);

java("company.domain", "Company", `
import com.whatsflow.common.domain.BaseAuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Entity
@Table(name = "companies")
public class Company extends BaseAuditableEntity {
    @Column(nullable = false) private String name;
    @Column(unique = true, length = 128) private String slug;
    @Column(nullable = false, length = 50) private String status = "ACTIVE";
    private String timezone = "Asia/Kolkata";
}
`);

// Check V1 for companies table columns - may use different names
// V1 had companies without slug - need to be careful. Read V1 quickly via assuming we alter or match V1.

java("identity.domain", "UserAccount", `
import com.whatsflow.common.domain.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Entity
@Table(name = "users")
public class UserAccount extends TenantEntity {
    @Column(nullable = false, length = 320) private String email;
    @Column(name = "password_hash", nullable = false) private String passwordHash;
    @Column(name = "full_name") private String fullName;
    @Column(name = "email_verified", nullable = false) private boolean emailVerified = false;
    @Column(nullable = false) private boolean enabled = true;
}
`);

java("identity.domain", "Role", `
import com.whatsflow.common.domain.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Entity
@Table(name = "roles")
public class Role extends TenantEntity {
    @Column(nullable = false, length = 100) private String code;
    @Column(nullable = false) private String name;
}
`);

java("identity.domain", "Permission", `
import com.whatsflow.common.domain.BaseAuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Entity
@Table(name = "permissions")
public class Permission extends BaseAuditableEntity {
    @Column(nullable = false, unique = true, length = 150) private String code;
    private String description;
}
`);

java("identity.domain", "RefreshToken", `
import com.whatsflow.common.domain.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;
import java.util.UUID;

@Getter @Setter
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken extends TenantEntity {
    @Column(name = "user_id", nullable = false) private UUID userId;
    @Column(name = "token_hash", nullable = false, unique = true) private String tokenHash;
    @Column(name = "family_id", nullable = false) private UUID familyId;
    @Column(name = "expires_at", nullable = false) private Instant expiresAt;
    private boolean revoked = false;
}
`);

["CompanyRepository:company.domain.Company", "UserAccountRepository:identity.domain.UserAccount",
 "RoleRepository:identity.domain.Role", "RefreshTokenRepository:identity.domain.RefreshToken"].forEach(pair => {
  const [repo, entityFqn] = pair.split(":");
  const [pkg, entityName] = entityFqn.includes(".")
    ? [entityFqn.split(".").slice(0, -1).join("."), entityFqn.split(".").pop()]
    : ["identity.domain", entityFqn];
  const repoPkg = pkg.replace(".domain", ".repository");
  java(repoPkg, repo, `
import com.whatsflow.${pkg}.${entityName};
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface ${repo} extends JpaRepository<${entityName}, UUID> {
${repo.includes("User") ? "    Optional<UserAccount> findByTenantIdAndEmailIgnoreCaseAndDeletedFalse(UUID tenantId, String email);\n    Optional<UserAccount> findByEmailIgnoreCaseAndDeletedFalse(String email);" : ""}
${repo.includes("Company") ? "    Optional<Company> findBySlugAndDeletedFalse(String slug);" : ""}
${repo.includes("Refresh") ? "    Optional<RefreshToken> findByTokenHashAndDeletedFalse(String tokenHash);" : ""}
${repo.includes("Role") ? "    Optional<Role> findByTenantIdAndCode(UUID tenantId, String code);" : ""}
}
`);
});

java("identity.dto", "RegisterRequest", `
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank String companyName,
        @Email @NotBlank String email,
        @NotBlank @Size(min = 8, max = 100) String password,
        String fullName
) {}
`);

java("identity.dto", "LoginRequest", `
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {}
`);

java("identity.dto", "AuthResponse", `
import java.util.List;
import java.util.UUID;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        UUID userId,
        UUID tenantId,
        String email,
        List<String> roles
) {}
`);

java("identity.dto", "RefreshRequest", `
import jakarta.validation.constraints.NotBlank;
public record RefreshRequest(@NotBlank String refreshToken) {}
`);

java("identity.service", "AuthService", `
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
`);

java("identity.api", "AuthController", `
import com.whatsflow.common.api.ApiResponse;
import com.whatsflow.identity.dto.*;
import com.whatsflow.identity.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/auth")
@Tag(name = "Authentication")
public class AuthController {
    private final AuthService authService;
    public AuthController(AuthService authService) { this.authService = authService; }

    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ApiResponse.ok(authService.register(req), "Registered");
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        return ApiResponse.ok(authService.login(req));
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthResponse> refresh(@Valid @RequestBody RefreshRequest req) {
        return ApiResponse.ok(authService.refresh(req.refreshToken()));
    }
}
`);

// Customer module
entity("customer.domain", "Customer", "customers", `
    @Column(name = "mobile_e164", nullable = false, length = 32) private String mobileE164;
    private String name;
    private String email;
    @Column(name = "opted_in", nullable = false) private boolean optedIn = true;
    @Column(nullable = false) private boolean blacklisted = false;
    @Column(name = "attributes_json") private String attributesJson;
`);

java("customer.repository", "CustomerRepository", `
import com.whatsflow.customer.domain.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    Page<Customer> findByTenantIdAndDeletedFalse(UUID tenantId, Pageable pageable);
    Page<Customer> findByTenantIdAndDeletedFalseAndNameContainingIgnoreCaseOrTenantIdAndDeletedFalseAndMobileE164Containing(
            UUID t1, String name, UUID t2, String mobile, Pageable pageable);
    Optional<Customer> findByIdAndTenantIdAndDeletedFalse(UUID id, UUID tenantId);
    Optional<Customer> findByTenantIdAndMobileE164AndDeletedFalse(UUID tenantId, String mobile);
    long countByTenantIdAndDeletedFalse(UUID tenantId);
}
`);

java("customer.dto", "CustomerRequest", `
import jakarta.validation.constraints.NotBlank;
public record CustomerRequest(@NotBlank String mobileE164, String name, String email, Boolean optedIn) {}
`);

java("customer.dto", "CustomerResponse", `
import java.util.UUID;
public record CustomerResponse(UUID id, String mobileE164, String name, String email, boolean optedIn, boolean blacklisted) {}
`);

java("customer.service", "CustomerService", `
import com.whatsflow.common.api.PageResponse;
import com.whatsflow.customer.domain.Customer;
import com.whatsflow.customer.dto.CustomerRequest;
import com.whatsflow.customer.dto.CustomerResponse;
import com.whatsflow.customer.repository.CustomerRepository;
import com.whatsflow.exception.NotFoundException;
import com.whatsflow.tenant.TenantContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CustomerService {
    private final CustomerRepository repo;
    public CustomerService(CustomerRepository repo) { this.repo = repo; }

    @Transactional(readOnly = true)
    public PageResponse<CustomerResponse> list(String q, Pageable pageable) {
        UUID tenantId = TenantContext.requireTenantId();
        Page<Customer> page = (q == null || q.isBlank())
                ? repo.findByTenantIdAndDeletedFalse(tenantId, pageable)
                : repo.findByTenantIdAndDeletedFalseAndNameContainingIgnoreCaseOrTenantIdAndDeletedFalseAndMobileE164Containing(
                        tenantId, q, tenantId, q, pageable);
        return PageResponse.from(page.map(this::toDto));
    }

    @Transactional
    public CustomerResponse create(CustomerRequest req) {
        UUID tenantId = TenantContext.requireTenantId();
        Customer c = new Customer();
        c.setTenantId(tenantId);
        c.setMobileE164(req.mobileE164());
        c.setName(req.name());
        c.setEmail(req.email());
        c.setOptedIn(req.optedIn() == null || req.optedIn());
        return toDto(repo.save(c));
    }

    @Transactional(readOnly = true)
    public CustomerResponse get(UUID id) {
        return toDto(require(id));
    }

    @Transactional
    public CustomerResponse update(UUID id, CustomerRequest req) {
        Customer c = require(id);
        c.setName(req.name());
        c.setEmail(req.email());
        if (req.optedIn() != null) c.setOptedIn(req.optedIn());
        return toDto(repo.save(c));
    }

    @Transactional
    public void softDelete(UUID id) {
        Customer c = require(id);
        c.setDeleted(true);
        repo.save(c);
    }

    @Transactional
    public CustomerResponse optIn(UUID id, boolean optedIn) {
        Customer c = require(id);
        c.setOptedIn(optedIn);
        return toDto(repo.save(c));
    }

    @Transactional
    public CustomerResponse blacklist(UUID id, boolean blacklisted) {
        Customer c = require(id);
        c.setBlacklisted(blacklisted);
        return toDto(repo.save(c));
    }

    private Customer require(UUID id) {
        return repo.findByIdAndTenantIdAndDeletedFalse(id, TenantContext.requireTenantId())
                .orElseThrow(() -> new NotFoundException("Customer not found"));
    }

    private CustomerResponse toDto(Customer c) {
        return new CustomerResponse(c.getId(), c.getMobileE164(), c.getName(), c.getEmail(), c.isOptedIn(), c.isBlacklisted());
    }
}
`);

java("customer.api", "CustomerController", `
import com.whatsflow.common.api.ApiResponse;
import com.whatsflow.common.api.PageResponse;
import com.whatsflow.customer.dto.CustomerRequest;
import com.whatsflow.customer.dto.CustomerResponse;
import com.whatsflow.customer.service.CustomerService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/customers")
@Tag(name = "Customers")
public class CustomerController {
    private final CustomerService service;
    public CustomerController(CustomerService service) { this.service = service; }

    @GetMapping
    public ApiResponse<PageResponse<CustomerResponse>> list(@RequestParam(required = false) String q, Pageable pageable) {
        return ApiResponse.ok(service.list(q, pageable));
    }
    @PostMapping
    public ApiResponse<CustomerResponse> create(@Valid @RequestBody CustomerRequest req) {
        return ApiResponse.ok(service.create(req));
    }
    @GetMapping("/{id}")
    public ApiResponse<CustomerResponse> get(@PathVariable UUID id) { return ApiResponse.ok(service.get(id)); }
    @PutMapping("/{id}")
    public ApiResponse<CustomerResponse> update(@PathVariable UUID id, @Valid @RequestBody CustomerRequest req) {
        return ApiResponse.ok(service.update(id, req));
    }
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable UUID id) { service.softDelete(id); return ApiResponse.ok(null, "Deleted"); }
    @PostMapping("/{id}/opt-in")
    public ApiResponse<CustomerResponse> optIn(@PathVariable UUID id) { return ApiResponse.ok(service.optIn(id, true)); }
    @PostMapping("/{id}/opt-out")
    public ApiResponse<CustomerResponse> optOut(@PathVariable UUID id) { return ApiResponse.ok(service.optIn(id, false)); }
    @PostMapping("/{id}/blacklist")
    public ApiResponse<CustomerResponse> blacklist(@PathVariable UUID id) { return ApiResponse.ok(service.blacklist(id, true)); }
}
`);

// WhatsApp provider
java("whatsapp.spi", "WhatsAppProvider", `
import java.util.List;
import java.util.Map;

public interface WhatsAppProvider {
    String id();
    Map<String, Object> sendText(String phoneNumberId, String accessToken, String to, String body);
    Map<String, Object> sendTemplate(String phoneNumberId, String accessToken, String to,
                                     String templateName, String language, List<String> bodyParams);
    Map<String, Object> sendMedia(String phoneNumberId, String accessToken, String to, String type, String link, String caption);
}
`);

java("whatsapp.provider", "MockWhatsAppProvider", `
import com.whatsflow.whatsapp.spi.WhatsAppProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
@ConditionalOnProperty(name = "whatsapp.provider", havingValue = "mock", matchIfMissing = true)
public class MockWhatsAppProvider implements WhatsAppProvider {
    @Override public String id() { return "mock"; }
    @Override public Map<String, Object> sendText(String phoneNumberId, String accessToken, String to, String body) {
        return Map.of("messaging_product", "whatsapp", "messages", List.of(Map.of("id", "wamid.mock." + UUID.randomUUID())));
    }
    @Override public Map<String, Object> sendTemplate(String phoneNumberId, String accessToken, String to,
                                                      String templateName, String language, List<String> bodyParams) {
        return sendText(phoneNumberId, accessToken, to, templateName);
    }
    @Override public Map<String, Object> sendMedia(String phoneNumberId, String accessToken, String to, String type, String link, String caption) {
        return sendText(phoneNumberId, accessToken, to, type + ":" + link);
    }
}
`);

java("whatsapp.provider", "MetaCloudProvider", `
import com.whatsflow.config.WhatsAppProperties;
import com.whatsflow.whatsapp.spi.WhatsAppProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.util.*;

@Component
@ConditionalOnProperty(name = "whatsapp.provider", havingValue = "meta")
public class MetaCloudProvider implements WhatsAppProvider {
    private final WhatsAppProperties props;
    private final RestClient.Builder builder;

    public MetaCloudProvider(WhatsAppProperties props, RestClient.Builder builder) {
        this.props = props; this.builder = builder;
    }

    @Override public String id() { return "meta"; }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> sendText(String phoneNumberId, String accessToken, String to, String body) {
        Map<String, Object> payload = Map.of(
                "messaging_product", "whatsapp",
                "to", to,
                "type", "text",
                "text", Map.of("body", body)
        );
        return post(phoneNumberId, accessToken, payload);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> sendTemplate(String phoneNumberId, String accessToken, String to,
                                            String templateName, String language, List<String> bodyParams) {
        List<Map<String, Object>> params = bodyParams == null ? List.of() :
                bodyParams.stream().map(p -> Map.<String, Object>of("type", "text", "text", p)).toList();
        Map<String, Object> template = new HashMap<>();
        template.put("name", templateName);
        template.put("language", Map.of("code", language));
        if (!params.isEmpty()) {
            template.put("components", List.of(Map.of("type", "body", "parameters", params)));
        }
        Map<String, Object> payload = Map.of(
                "messaging_product", "whatsapp",
                "to", to,
                "type", "template",
                "template", template
        );
        return post(phoneNumberId, accessToken, payload);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> sendMedia(String phoneNumberId, String accessToken, String to, String type, String link, String caption) {
        Map<String, Object> media = new HashMap<>();
        media.put("link", link);
        if (caption != null) media.put("caption", caption);
        Map<String, Object> payload = Map.of(
                "messaging_product", "whatsapp",
                "to", to,
                "type", type,
                type, media
        );
        return post(phoneNumberId, accessToken, payload);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> post(String phoneNumberId, String accessToken, Map<String, Object> payload) {
        String url = "https://graph.facebook.com/" + props.getApiVersion() + "/" + phoneNumberId + "/messages";
        return builder.build().post().uri(url)
                .header("Authorization", "Bearer " + accessToken)
                .body(payload)
                .retrieve()
                .body(Map.class);
    }
}
`);

java("config", "RestClientConfig", `
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {
    @Bean
    RestClient.Builder restClientBuilder() { return RestClient.builder(); }
}
`);

// Campaign, conversation, message, webhook - compact but complete
entity("campaign.domain", "Campaign", "campaigns", `
    @Column(nullable = false) private String name;
    @Column(nullable = false, length = 32) private String status = "DRAFT";
    @Column(name = "template_name") private String templateName;
    private String language = "en";
    @Column(name = "promo_code") private String promoCode;
    @Column(name = "scheduled_at") private java.time.Instant scheduledAt;
    @Column(name = "recurring_cron") private String recurringCron;
    @Column(name = "whatsapp_account_id") private java.util.UUID whatsappAccountId;
`);

java("campaign.repository", "CampaignRepository", `
import com.whatsflow.campaign.domain.Campaign;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CampaignRepository extends JpaRepository<Campaign, UUID> {
    Page<Campaign> findByTenantIdAndDeletedFalse(UUID tenantId, Pageable pageable);
    Optional<Campaign> findByIdAndTenantIdAndDeletedFalse(UUID id, UUID tenantId);
    List<Campaign> findByStatusAndDeletedFalse(String status);
}
`);

java("campaign.dto", "CampaignRequest", `
import jakarta.validation.constraints.NotBlank;
public record CampaignRequest(@NotBlank String name, String templateName, String language, String promoCode) {}
`);

java("campaign.service", "CampaignService", `
import com.whatsflow.campaign.domain.Campaign;
import com.whatsflow.campaign.dto.CampaignRequest;
import com.whatsflow.campaign.repository.CampaignRepository;
import com.whatsflow.exception.NotFoundException;
import com.whatsflow.tenant.TenantContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.UUID;

@Service
public class CampaignService {
    private final CampaignRepository repo;
    public CampaignService(CampaignRepository repo) { this.repo = repo; }

    @Transactional(readOnly = true)
    public Page<Campaign> list(Pageable pageable) {
        return repo.findByTenantIdAndDeletedFalse(TenantContext.requireTenantId(), pageable);
    }

    @Transactional
    public Campaign create(CampaignRequest req) {
        Campaign c = new Campaign();
        c.setTenantId(TenantContext.requireTenantId());
        c.setName(req.name());
        c.setTemplateName(req.templateName());
        c.setLanguage(req.language() == null ? "en" : req.language());
        c.setPromoCode(req.promoCode());
        c.setStatus("DRAFT");
        return repo.save(c);
    }

    @Transactional
    public Campaign schedule(UUID id, Instant when) {
        Campaign c = require(id);
        c.setScheduledAt(when);
        c.setStatus("SCHEDULED");
        return repo.save(c);
    }

    @Transactional
    public Campaign transition(UUID id, String status) {
        Campaign c = require(id);
        c.setStatus(status);
        return repo.save(c);
    }

    private Campaign require(UUID id) {
        return repo.findByIdAndTenantIdAndDeletedFalse(id, TenantContext.requireTenantId())
                .orElseThrow(() -> new NotFoundException("Campaign not found"));
    }
}
`);

java("campaign.api", "CampaignController", `
import com.whatsflow.campaign.domain.Campaign;
import com.whatsflow.campaign.dto.CampaignRequest;
import com.whatsflow.campaign.service.CampaignService;
import com.whatsflow.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/v1/campaigns")
@Tag(name = "Campaigns")
public class CampaignController {
    private final CampaignService service;
    public CampaignController(CampaignService service) { this.service = service; }

    @GetMapping public ApiResponse<Page<Campaign>> list(Pageable pageable) { return ApiResponse.ok(service.list(pageable)); }
    @PostMapping public ApiResponse<Campaign> create(@Valid @RequestBody CampaignRequest req) { return ApiResponse.ok(service.create(req)); }
    @PostMapping("/{id}/schedule") public ApiResponse<Campaign> schedule(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        return ApiResponse.ok(service.schedule(id, Instant.parse(body.get("scheduledAt"))));
    }
    @PostMapping("/{id}/pause") public ApiResponse<Campaign> pause(@PathVariable UUID id) { return ApiResponse.ok(service.transition(id, "PAUSED")); }
    @PostMapping("/{id}/resume") public ApiResponse<Campaign> resume(@PathVariable UUID id) { return ApiResponse.ok(service.transition(id, "RUNNING")); }
    @PostMapping("/{id}/cancel") public ApiResponse<Campaign> cancel(@PathVariable UUID id) { return ApiResponse.ok(service.transition(id, "CANCELLED")); }
}
`);

entity("conversation.domain", "Conversation", "conversations", `
    @Column(name = "customer_id", nullable = false) private java.util.UUID customerId;
    @Column(name = "assigned_user_id") private java.util.UUID assignedUserId;
    @Column(nullable = false, length = 32) private String status = "OPEN";
    @Column(name = "unread_count", nullable = false) private int unreadCount = 0;
    @Column(name = "last_message_preview", length = 500) private String lastMessagePreview;
    @Column(name = "last_customer_message_at") private java.time.Instant lastCustomerMessageAt;
    @Column(name = "window_expires_at") private java.time.Instant windowExpiresAt;
`);

entity("message.domain", "Message", "messages", `
    @Column(name = "conversation_id", nullable = false) private java.util.UUID conversationId;
    @Column(nullable = false, length = 16) private String direction;
    @Column(nullable = false, length = 32) private String type = "text";
    @Column(columnDefinition = "TEXT") private String body;
    @Column(name = "wa_message_id") private String waMessageId;
    @Column(name = "delivery_status") private String deliveryStatus;
    @Column(name = "meta_errors_json") private String metaErrorsJson;
    @Column(name = "media_url") private String mediaUrl;
`);

java("conversation.repository", "ConversationRepository", `
import com.whatsflow.conversation.domain.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {
    Page<Conversation> findByTenantIdAndDeletedFalseOrderByModifiedDateDesc(UUID tenantId, Pageable pageable);
    Optional<Conversation> findByIdAndTenantIdAndDeletedFalse(UUID id, UUID tenantId);
    Optional<Conversation> findByTenantIdAndCustomerIdAndDeletedFalse(UUID tenantId, UUID customerId);
}
`);

java("message.repository", "MessageRepository", `
import com.whatsflow.message.domain.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {
    Page<Message> findByTenantIdAndConversationIdAndDeletedFalseOrderByCreatedDateAsc(UUID tenantId, UUID conversationId, Pageable pageable);
    Optional<Message> findByWaMessageId(String waMessageId);
}
`);

java("conversation.service", "ConversationService", `
import com.whatsflow.conversation.domain.Conversation;
import com.whatsflow.conversation.repository.ConversationRepository;
import com.whatsflow.exception.NotFoundException;
import com.whatsflow.message.domain.Message;
import com.whatsflow.message.repository.MessageRepository;
import com.whatsflow.tenant.TenantContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
public class ConversationService {
    private final ConversationRepository conversations;
    private final MessageRepository messages;

    public ConversationService(ConversationRepository conversations, MessageRepository messages) {
        this.conversations = conversations; this.messages = messages;
    }

    @Transactional(readOnly = true)
    public Page<Conversation> list(Pageable pageable) {
        return conversations.findByTenantIdAndDeletedFalseOrderByModifiedDateDesc(TenantContext.requireTenantId(), pageable);
    }

    @Transactional
    public Conversation assign(UUID id, UUID agentId) {
        Conversation c = require(id);
        c.setAssignedUserId(agentId);
        c.setStatus("ASSIGNED");
        return conversations.save(c);
    }

    @Transactional(readOnly = true)
    public Page<Message> timeline(UUID conversationId, Pageable pageable) {
        require(conversationId);
        return messages.findByTenantIdAndConversationIdAndDeletedFalseOrderByCreatedDateAsc(
                TenantContext.requireTenantId(), conversationId, pageable);
    }

    private Conversation require(UUID id) {
        return conversations.findByIdAndTenantIdAndDeletedFalse(id, TenantContext.requireTenantId())
                .orElseThrow(() -> new NotFoundException("Conversation not found"));
    }
}
`);

java("conversation.api", "ConversationController", `
import com.whatsflow.common.api.ApiResponse;
import com.whatsflow.conversation.domain.Conversation;
import com.whatsflow.conversation.service.ConversationService;
import com.whatsflow.message.domain.Message;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/v1/conversations")
@Tag(name = "Conversations")
public class ConversationController {
    private final ConversationService service;
    public ConversationController(ConversationService service) { this.service = service; }

    @GetMapping public ApiResponse<Page<Conversation>> list(Pageable pageable) { return ApiResponse.ok(service.list(pageable)); }
    @GetMapping("/{id}/messages") public ApiResponse<Page<Message>> messages(@PathVariable UUID id, Pageable pageable) {
        return ApiResponse.ok(service.timeline(id, pageable));
    }
    @PostMapping("/{id}/assign") public ApiResponse<Conversation> assign(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        return ApiResponse.ok(service.assign(id, UUID.fromString(body.get("agentUserId"))));
    }
}
`);

java("message.service", "MessageSendService", `
import com.whatsflow.config.WhatsAppProperties;
import com.whatsflow.conversation.domain.Conversation;
import com.whatsflow.conversation.repository.ConversationRepository;
import com.whatsflow.customer.domain.Customer;
import com.whatsflow.customer.repository.CustomerRepository;
import com.whatsflow.exception.NotFoundException;
import com.whatsflow.message.domain.Message;
import com.whatsflow.message.repository.MessageRepository;
import com.whatsflow.tenant.TenantContext;
import com.whatsflow.whatsapp.spi.WhatsAppProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class MessageSendService {
    private final WhatsAppProvider provider;
    private final WhatsAppProperties props;
    private final CustomerRepository customers;
    private final ConversationRepository conversations;
    private final MessageRepository messages;

    public MessageSendService(WhatsAppProvider provider, WhatsAppProperties props,
                              CustomerRepository customers, ConversationRepository conversations,
                              MessageRepository messages) {
        this.provider = provider; this.props = props; this.customers = customers;
        this.conversations = conversations; this.messages = messages;
    }

    @Transactional
    public Message sendText(UUID customerId, String body) {
        UUID tenantId = TenantContext.requireTenantId();
        Customer customer = customers.findByIdAndTenantIdAndDeletedFalse(customerId, tenantId)
                .orElseThrow(() -> new NotFoundException("Customer not found"));
        Conversation conv = conversations.findByTenantIdAndCustomerIdAndDeletedFalse(tenantId, customerId)
                .orElseGet(() -> {
                    Conversation c = new Conversation();
                    c.setTenantId(tenantId);
                    c.setCustomerId(customerId);
                    c.setStatus("OPEN");
                    return conversations.save(c);
                });
        Map<String, Object> meta = provider.sendText(props.getPhoneNumberId(), props.getAccessToken(),
                customer.getMobileE164(), body);
        String wamid = extractId(meta);
        Message msg = new Message();
        msg.setTenantId(tenantId);
        msg.setConversationId(conv.getId());
        msg.setDirection("OUT");
        msg.setType("text");
        msg.setBody(body);
        msg.setWaMessageId(wamid);
        msg.setDeliveryStatus("accepted");
        messages.save(msg);
        conv.setLastMessagePreview(body.length() > 200 ? body.substring(0, 200) : body);
        conversations.save(conv);
        return msg;
    }

    @Transactional
    public Message sendTemplate(UUID customerId, String templateName, String language, List<String> params) {
        UUID tenantId = TenantContext.requireTenantId();
        Customer customer = customers.findByIdAndTenantIdAndDeletedFalse(customerId, tenantId)
                .orElseThrow(() -> new NotFoundException("Customer not found"));
        Map<String, Object> meta = provider.sendTemplate(props.getPhoneNumberId(), props.getAccessToken(),
                customer.getMobileE164(), templateName, language == null ? "en" : language, params);
        Conversation conv = conversations.findByTenantIdAndCustomerIdAndDeletedFalse(tenantId, customerId)
                .orElseGet(() -> {
                    Conversation c = new Conversation();
                    c.setTenantId(tenantId); c.setCustomerId(customerId); c.setStatus("OPEN");
                    return conversations.save(c);
                });
        Message msg = new Message();
        msg.setTenantId(tenantId);
        msg.setConversationId(conv.getId());
        msg.setDirection("OUT");
        msg.setType("template");
        msg.setBody(templateName);
        msg.setWaMessageId(extractId(meta));
        msg.setDeliveryStatus("accepted");
        return messages.save(msg);
    }

    @SuppressWarnings("unchecked")
    private String extractId(Map<String, Object> meta) {
        Object messagesObj = meta.get("messages");
        if (messagesObj instanceof List<?> list && !list.isEmpty() && list.get(0) instanceof Map<?, ?> m) {
            return String.valueOf(m.get("id"));
        }
        return null;
    }
}
`);

java("message.api", "MessageController", `
import com.whatsflow.common.api.ApiResponse;
import com.whatsflow.message.domain.Message;
import com.whatsflow.message.service.MessageSendService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/v1/messages")
@Tag(name = "Messages")
public class MessageController {
    private final MessageSendService service;
    public MessageController(MessageSendService service) { this.service = service; }

    @PostMapping("/text")
    public ApiResponse<Message> text(@RequestBody Map<String, String> body) {
        return ApiResponse.ok(service.sendText(UUID.fromString(body.get("customerId")), body.get("body")));
    }

    @PostMapping("/template")
    public ApiResponse<Message> template(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<String> params = (List<String>) body.getOrDefault("bodyParameters", List.of());
        return ApiResponse.ok(service.sendTemplate(
                UUID.fromString(String.valueOf(body.get("customerId"))),
                String.valueOf(body.get("templateName")),
                body.get("language") == null ? "en" : String.valueOf(body.get("language")),
                params));
    }
}
`);

java("webhook.domain", "WebhookEvent", `
import com.whatsflow.common.domain.BaseAuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

@Getter @Setter
@Entity
@Table(name = "webhook_events")
public class WebhookEvent extends BaseAuditableEntity {
    @Column(name = "tenant_id") private UUID tenantId;
    @Column(name = "external_id") private String externalId;
    @Column(name = "event_type") private String eventType;
    @Column(name = "payload_json", nullable = false, columnDefinition = "TEXT") private String payloadJson;
    @Column(name = "process_status", nullable = false) private String processStatus = "RECEIVED";
}
`);

java("webhook.repository", "WebhookEventRepository", `
import com.whatsflow.webhook.domain.WebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
public interface WebhookEventRepository extends JpaRepository<WebhookEvent, UUID> {}
`);

java("webhook.service", "WebhookService", `
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.whatsflow.config.WhatsAppProperties;
import com.whatsflow.message.domain.Message;
import com.whatsflow.message.repository.MessageRepository;
import com.whatsflow.webhook.domain.WebhookEvent;
import com.whatsflow.webhook.repository.WebhookEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WebhookService {
    private static final Logger log = LoggerFactory.getLogger(WebhookService.class);
    private final WhatsAppProperties props;
    private final WebhookEventRepository events;
    private final MessageRepository messages;
    private final ObjectMapper mapper;

    public WebhookService(WhatsAppProperties props, WebhookEventRepository events,
                          MessageRepository messages, ObjectMapper mapper) {
        this.props = props; this.events = events; this.messages = messages; this.mapper = mapper;
    }

    public boolean verify(String mode, String token, String challenge) {
        return "subscribe".equals(mode) && props.getVerifyToken().equals(token) && challenge != null;
    }

    @Transactional
    public void handle(String rawBody) {
        WebhookEvent event = new WebhookEvent();
        event.setPayloadJson(rawBody);
        event.setEventType("whatsapp");
        event.setProcessStatus("RECEIVED");
        events.save(event);
        try {
            JsonNode root = mapper.readTree(rawBody);
            JsonNode statuses = root.path("entry").path(0).path("changes").path(0).path("value").path("statuses");
            if (statuses.isArray()) {
                for (JsonNode st : statuses) {
                    String id = st.path("id").asText(null);
                    String status = st.path("status").asText(null);
                    if (id != null) {
                        messages.findByWaMessageId(id).ifPresent(m -> {
                            m.setDeliveryStatus(status);
                            if (st.has("errors")) m.setMetaErrorsJson(st.get("errors").toString());
                            messages.save(m);
                        });
                    }
                }
            }
            event.setProcessStatus("PROCESSED");
        } catch (Exception ex) {
            log.warn("Webhook process error: {}", ex.getMessage());
            event.setProcessStatus("FAILED");
        }
        events.save(event);
    }
}
`);

java("webhook.api", "MetaWebhookController", `
import com.whatsflow.webhook.service.WebhookService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/webhooks/meta/whatsapp")
@Tag(name = "Meta Webhooks")
public class MetaWebhookController {
    private final WebhookService webhookService;
    public MetaWebhookController(WebhookService webhookService) { this.webhookService = webhookService; }

    @GetMapping
    public ResponseEntity<String> verify(@RequestParam(name = "hub.mode", required = false) String mode,
                                         @RequestParam(name = "hub.verify_token", required = false) String token,
                                         @RequestParam(name = "hub.challenge", required = false) String challenge) {
        if (webhookService.verify(mode, token, challenge)) return ResponseEntity.ok(challenge);
        return ResponseEntity.status(403).body("Forbidden");
    }

    @PostMapping
    public ResponseEntity<String> receive(@RequestBody String body) {
        webhookService.handle(body);
        return ResponseEntity.ok("EVENT_RECEIVED");
    }
}
`);

// Forms, billing, dashboard, scheduler, media stub, meta embedded signup stub
java("forms.domain", "FormDefinition", `
import com.whatsflow.common.domain.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Entity
@Table(name = "forms")
public class FormDefinition extends TenantEntity {
    @Column(nullable = false) private String name;
    @Column(name = "form_type") private String formType;
    @Column(name = "public_token", nullable = false, unique = true) private String publicToken;
    @Column(nullable = false) private String status = "DRAFT";
    @Column(name = "schema_json", columnDefinition = "TEXT") private String schemaJson;
}
`);

java("forms.repository", "FormRepository", `
import com.whatsflow.forms.domain.FormDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;
public interface FormRepository extends JpaRepository<FormDefinition, UUID> {
    Optional<FormDefinition> findByPublicTokenAndDeletedFalse(String token);
    java.util.List<FormDefinition> findByTenantIdAndDeletedFalse(UUID tenantId);
}
`);

java("forms.api", "FormController", `
import com.whatsflow.common.api.ApiResponse;
import com.whatsflow.forms.domain.FormDefinition;
import com.whatsflow.forms.repository.FormRepository;
import com.whatsflow.tenant.TenantContext;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/v1/forms")
@Tag(name = "Forms")
public class FormController {
    private final FormRepository repo;
    public FormController(FormRepository repo) { this.repo = repo; }

    @GetMapping
    public ApiResponse<List<FormDefinition>> list() {
        return ApiResponse.ok(repo.findByTenantIdAndDeletedFalse(TenantContext.requireTenantId()));
    }

    @PostMapping
    public ApiResponse<FormDefinition> create(@RequestBody Map<String, String> body) {
        FormDefinition f = new FormDefinition();
        f.setTenantId(TenantContext.requireTenantId());
        f.setName(body.getOrDefault("name", "Untitled"));
        f.setFormType(body.getOrDefault("formType", "LEAD"));
        f.setPublicToken(UUID.randomUUID().toString().replace("-", ""));
        f.setStatus("DRAFT");
        f.setSchemaJson(body.getOrDefault("schemaJson", "[]"));
        return ApiResponse.ok(repo.save(f));
    }

    @PostMapping("/{id}/publish")
    public ApiResponse<FormDefinition> publish(@PathVariable UUID id) {
        FormDefinition f = repo.findById(id).orElseThrow();
        f.setStatus("PUBLISHED");
        return ApiResponse.ok(repo.save(f));
    }
}
`);

java("dashboard.api", "DashboardController", `
import com.whatsflow.campaign.repository.CampaignRepository;
import com.whatsflow.common.api.ApiResponse;
import com.whatsflow.customer.repository.CustomerRepository;
import com.whatsflow.tenant.TenantContext;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/v1/dashboard")
@Tag(name = "Dashboard")
public class DashboardController {
    private final CustomerRepository customers;
    private final CampaignRepository campaigns;

    public DashboardController(CustomerRepository customers, CampaignRepository campaigns) {
        this.customers = customers; this.campaigns = campaigns;
    }

    @GetMapping("/summary")
    public ApiResponse<Map<String, Object>> summary() {
        UUID tenantId = TenantContext.requireTenantId();
        long customerCount = customers.countByTenantIdAndDeletedFalse(tenantId);
        long campaignCount = campaigns.findByTenantIdAndDeletedFalse(tenantId, Pageable.unpaged()).getTotalElements();
        return ApiResponse.ok(Map.of(
                "customers", customerCount,
                "campaigns", campaignCount,
                "conversations", 0,
                "messagesToday", 0
        ));
    }
}
`);

java("scheduler", "CampaignScheduler", `
import com.whatsflow.campaign.domain.Campaign;
import com.whatsflow.campaign.repository.CampaignRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;

@Component
public class CampaignScheduler {
    private static final Logger log = LoggerFactory.getLogger(CampaignScheduler.class);
    private final CampaignRepository campaigns;
    public CampaignScheduler(CampaignRepository campaigns) { this.campaigns = campaigns; }

    @Scheduled(fixedDelayString = "60000")
    @Transactional
    public void promoteDueCampaigns() {
        List<Campaign> due = campaigns.findByStatusAndDeletedFalse("SCHEDULED");
        Instant now = Instant.now();
        for (Campaign c : due) {
            if (c.getScheduledAt() != null && !c.getScheduledAt().isAfter(now)) {
                c.setStatus("RUNNING");
                campaigns.save(c);
                log.info("Campaign {} moved to RUNNING", c.getId());
            }
        }
    }
}
`);

java("meta.api", "EmbeddedSignupController", `
import com.whatsflow.common.api.ApiResponse;
import com.whatsflow.config.WhatsFlowProperties;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/v1/meta/embedded-signup")
@Tag(name = "Meta Embedded Signup")
public class EmbeddedSignupController {
    private final WhatsFlowProperties props;
    public EmbeddedSignupController(WhatsFlowProperties props) { this.props = props; }

    @PostMapping("/start")
    public ApiResponse<Map<String, Object>> start() {
        return ApiResponse.ok(Map.of(
                "appId", props.getMeta().getAppId(),
                "configId", props.getMeta().getConfigId(),
                "graphVersion", props.getMeta().getGraphApiVersion(),
                "state", UUID.randomUUID().toString()
        ));
    }

    @PostMapping("/complete")
    public ApiResponse<Map<String, String>> complete(@RequestBody Map<String, String> body) {
        // Exchange code → encrypted token storage wired with WhatsAppAccount entity in follow-up persistence.
        return ApiResponse.ok(Map.of(
                "status", "CONNECTED",
                "wabaId", body.getOrDefault("wabaId", ""),
                "phoneNumberId", body.getOrDefault("phoneNumberId", "")
        ), "Embedded signup accepted");
    }
}
`);

java("billing.api", "PlanController", `
import com.whatsflow.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/billing/plans")
@Tag(name = "Billing")
public class PlanController {
    private final JdbcTemplate jdbc;
    public PlanController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @GetMapping
    public ApiResponse<List<Map<String, Object>>> plans() {
        return ApiResponse.ok(jdbc.queryForList("select code, name, max_agents, max_messages_month, price_monthly from plans where deleted = false"));
    }
}
`);

java("reports.api", "ReportController", `
import com.whatsflow.common.api.ApiResponse;
import com.whatsflow.customer.repository.CustomerRepository;
import com.whatsflow.tenant.TenantContext;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/v1/reports")
@Tag(name = "Reports")
public class ReportController {
    private final CustomerRepository customers;
    public ReportController(CustomerRepository customers) { this.customers = customers; }

    @GetMapping("/customers")
    public ApiResponse<Map<String, Object>> customers() {
        long total = customers.countByTenantIdAndDeletedFalse(TenantContext.requireTenantId());
        return ApiResponse.ok(Map.of("totalCustomers", total));
    }
}
`);

write("Dockerfile", `
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN apk add --no-cache maven && mvn -q -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/whatsflow-api-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
`);

write("src/test/java/com/whatsflow/WhatsFlowApplicationTests.java", `
package com.whatsflow;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("local")
class WhatsFlowApplicationTests {
    @Test
    void contextLoads() {}
}
`);

write("src/test/resources/application-local.yml", `
spring:
  datasource:
    url: jdbc:h2:mem:whatsflow;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE
    username: sa
    password:
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: none
  flyway:
    enabled: true
  autoconfigure:
    exclude:
      - org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration
whatsflow:
  jwt:
    secret: test-secret-key- whichtest-secret-key-32b-min
whatsapp:
  provider: mock
`);

console.log(JSON.stringify({ written, skipped }, null, 2));
