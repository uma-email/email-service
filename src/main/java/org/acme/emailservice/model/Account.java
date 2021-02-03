package org.acme.emailservice.model;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "account")
@NamedQuery(name = "Account.get", query = "SELECT a FROM Account a WHERE a.user.username=:username AND a.id=:id")
@NamedQuery(name = "Account.getAll", query = "SELECT a FROM Account a WHERE a.user.username=:username ORDER BY a.username")
@NamedQuery(name = "Account.getByEmailAddress", query = "SELECT a FROM Account a WHERE a.user.username=:username AND a.emailAddress=:emailAddress")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonbTransient
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    @Column(unique = false, nullable = true)
    private String displayName;

    @Column(unique = true, nullable = false)
    private String emailAddress;

    @Column(updatable = false, unique = true, nullable = false)
    private String username;

    @Column(unique = false, nullable = true)
    private String imapAddr;
    @Column(unique = false, nullable = true)
    private Integer imapPort;
    @Column(unique = false, nullable = true)
    private Integer imapType;
    @Column(unique = false, nullable = true)
    private String imapPassword;

    @Column(unique = false, nullable = true)
    private String smtpAddr;
    @Column(unique = false, nullable = true)
    private Integer smtpPort;
    @Column(unique = false, nullable = true)
    private Integer smtpType;
    @Column(unique = false, nullable = true)
    private String smtpPassword;
    @Column(name="OAUTH2_PROVIDER", unique = false, nullable = true)
    private String oAuth2Provider;
    @Column(name="OAUTH2_SCOPE", unique = false, nullable = true, columnDefinition="TEXT")
    private String oAuth2Scope;
    @Column(name="OAUTH2_ACCESS_TOKEN", unique = false, nullable = true, columnDefinition="TEXT")
    private String oAuth2AccessToken;
    @Column(name="OAUTH2_REFRESH_TOKEN", unique = false, nullable = true, columnDefinition="TEXT")
    private String oAuth2RefreshToken;
    @Column(name="OAUTH2_EXPIRY_DATE", unique = false, nullable = true)
    private Long oAuth2ExpiryDate;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Message> messages = new LinkedHashSet<>();

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ResourceServer> resourceServers = new LinkedHashSet<>();

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false, insertable = false, updatable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()")
    private Date timestamp;
    
    public void addMessage(Message message) {
        messages.add(message);
        message.setAccount(this);
    }

    public void removeMessage(Message message) {
        messages.remove(message);
        message.setAccount(null);
    }

    public void addResourceServer(ResourceServer resourceServer) {
        resourceServers.add(resourceServer);
        resourceServer.setAccount(this);
    }

    public void removeResourceServer(ResourceServer resourceServer) {
        resourceServers.remove(resourceServer);
        resourceServer.setAccount(null);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getImapAddr() {
        return imapAddr;
    }

    public void setImapAddr(String imapAddr) {
        this.imapAddr = imapAddr;
    }

    public Integer getImapPort() {
        return imapPort;
    }

    public void setImapPort(Integer imapPort) {
        this.imapPort = imapPort;
    }

    public Integer getImapType() {
        return imapType;
    }

    public void setImapType(Integer imapType) {
        this.imapType = imapType;
    }

    /* public String getImapPassword() {
        return imapPassword;
    } */

    public void setImapPassword(String imapPassword) {
        this.imapPassword = imapPassword;
    }

    public String getSmtpAddr() {
        return smtpAddr;
    }

    public void setSmtpAddr(String smtpAddr) {
        this.smtpAddr = smtpAddr;
    }

    public Integer getSmtpPort() {
        return smtpPort;
    }

    public void setSmtpPort(Integer smtpPort) {
        this.smtpPort = smtpPort;
    }

    public Integer getSmtpType() {
        return smtpType;
    }

    public void setSmtpType(Integer smtpType) {
        this.smtpType = smtpType;
    }

    /* public String getSmtpPassword() {
        return smtpPassword;
    } */

    public void setSmtpPassword(String smtpPassword) {
        this.smtpPassword = smtpPassword;
    }

    public String getOAuth2Provider() {
        return oAuth2Provider;
    }

    public void setOAuth2Provider(String oAuth2provider) {
        this.oAuth2Provider = oAuth2provider;
    }

    public String getOAuth2Scope() {
        return oAuth2Scope;
    }

    public void setOAuth2Scope(String oAuth2scope) {
        this.oAuth2Scope = oAuth2scope;
    }

    public String getOAuth2AccessToken() {
        return oAuth2AccessToken;
    }

    public void setOAuth2AccessToken(String oAuth2accessToken) {
        this.oAuth2AccessToken = oAuth2accessToken;
    }

    public String getOAuth2RefreshToken() {
        return oAuth2RefreshToken;
    }

    public void setOAuth2RefreshToken(String oAuth2refreshToken) {
        this.oAuth2RefreshToken = oAuth2refreshToken;
    }

    public Long getOAuth2ExpiryDate() {
        return oAuth2ExpiryDate;
    }

    public void setOAuth2ExpiryDate(Long oAuth2ExpiryDate) {
        this.oAuth2ExpiryDate = oAuth2ExpiryDate;
    }

    public Set<Message> getMessages() {
        return messages;
    }

    public void setMessages(Set<Message> messages) {
        this.messages = messages;
    }

    public Set<ResourceServer> getResourceServers() {
        return resourceServers;
    }

    public void setResourceServers(Set<ResourceServer> resourceServers) {
        this.resourceServers = resourceServers;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (!(o instanceof Account))
            return false;

        return id != null && id.equals(((Account) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
