package com.taller.m01.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public class AppSecurityProperties {
    private String jwtSecret;
    private long jwtExpirationMinutes = 60;
    private boolean cookieSecure;
    private boolean developmentMode;
    private String initialAdminName;
    private String initialAdminEmail;
    private String initialAdminPassword;
    private String frontendOrigin;
    public String getJwtSecret() { return jwtSecret; } public void setJwtSecret(String v) { jwtSecret = v; }
    public long getJwtExpirationMinutes() { return jwtExpirationMinutes; } public void setJwtExpirationMinutes(long v) { jwtExpirationMinutes = v; }
    public boolean isCookieSecure() { return cookieSecure; } public void setCookieSecure(boolean v) { cookieSecure = v; }
    public boolean isDevelopmentMode() { return developmentMode; } public void setDevelopmentMode(boolean v) { developmentMode = v; }
    public String getInitialAdminName() { return initialAdminName; } public void setInitialAdminName(String v) { initialAdminName = v; }
    public String getInitialAdminEmail() { return initialAdminEmail; } public void setInitialAdminEmail(String v) { initialAdminEmail = v; }
    public String getInitialAdminPassword() { return initialAdminPassword; } public void setInitialAdminPassword(String v) { initialAdminPassword = v; }
    public String getFrontendOrigin() { return frontendOrigin; } public void setFrontendOrigin(String v) { frontendOrigin = v; }
}
