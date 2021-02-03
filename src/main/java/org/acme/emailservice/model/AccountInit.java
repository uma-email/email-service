package org.acme.emailservice.model;

import java.util.List;

public class AccountInit {
    
    private String username;
    private String provider;
    private String email_address;
    private List<String> scopes;
    private String access_token;
    private String refresh_token;
    // https://stackoverflow.com/questions/39661002/google-oauth-expiry-date-format
    private long expiry_date;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getEmail_address() {
        return email_address;
    }

    public void setEmail_address(String email_address) {
        this.email_address = email_address;
    }

    public List<String> getScopes() {
        return scopes;
    }

    public void setScopes(List<String> scopes) {
        this.scopes = scopes;
    }

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public String getRefresh_token() {
        return refresh_token;
    }

    public void setRefresh_token(String refresh_token) {
        this.refresh_token = refresh_token;
    }

    public long getExpiry_date() {
        return expiry_date;
    }

    public void setExpiry_date(long expiry_date) {
        this.expiry_date = expiry_date;
    }
}
