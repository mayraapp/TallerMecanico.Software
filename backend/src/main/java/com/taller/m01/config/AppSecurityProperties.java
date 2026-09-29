package com.taller.m01.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
/**
 * Binds local security configuration without persisting any secret in source code.
 *
 * <p>Phase 02 adds the two operational temporary-password inputs. They are supplied only by the
 * ignored environment file and are consumed by the provisioning service as BCrypt input.</p>
 */
public class AppSecurityProperties {
    private String jwtSecret;
    private long jwtExpirationMinutes = 60;
    private boolean cookieSecure;
    private boolean developmentMode;
    private String initialAdminName;
    private String initialAdminEmail;
    private String initialAdminPassword;
    private String initialAdministratorPassword;
    private String initialReceptionistPassword;
    private String frontendOrigin;
    public String getJwtSecret() { return jwtSecret; } public void setJwtSecret(String v) { jwtSecret = v; }
    public long getJwtExpirationMinutes() { return jwtExpirationMinutes; } public void setJwtExpirationMinutes(long v) { jwtExpirationMinutes = v; }
    public boolean isCookieSecure() { return cookieSecure; } public void setCookieSecure(boolean v) { cookieSecure = v; }
    public boolean isDevelopmentMode() { return developmentMode; } public void setDevelopmentMode(boolean v) { developmentMode = v; }
    public String getInitialAdminName() { return initialAdminName; } public void setInitialAdminName(String v) { initialAdminName = v; }
    public String getInitialAdminEmail() { return initialAdminEmail; } public void setInitialAdminEmail(String v) { initialAdminEmail = v; }
    public String getInitialAdminPassword() { return initialAdminPassword; } public void setInitialAdminPassword(String v) { initialAdminPassword = v; }
    /** Returns the local-only temporary password used when the administrator account is first provisioned. */
    public String getInitialAdministratorPassword() { return initialAdministratorPassword; }
    /** Receives the administrator password from an environment variable; it is never persisted as plaintext. */
    public void setInitialAdministratorPassword(String v) { initialAdministratorPassword = v; }
    /** Returns the local-only temporary password used when the receptionist account is first provisioned. */
    public String getInitialReceptionistPassword() { return initialReceptionistPassword; }
    /** Receives the receptionist password from an environment variable; it is never persisted as plaintext. */
    public void setInitialReceptionistPassword(String v) { initialReceptionistPassword = v; }
    public String getFrontendOrigin() { return frontendOrigin; } public void setFrontendOrigin(String v) { frontendOrigin = v; }
}
