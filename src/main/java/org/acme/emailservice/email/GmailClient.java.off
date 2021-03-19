package org.acme.emailservice.email;

import java.awt.Desktop;
import java.net.URI;
import java.util.Collections;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

import org.acme.emailservice.email.oauth2.OAuth2Authenticator;
import org.acme.emailservice.model.Account;
import org.acme.emailservice.model.GoogleCredentials;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import jakarta.mail.Folder;
import jakarta.mail.Store;

public class GmailClient extends BaseClassClient {

    private static Logger log = Logger.getLogger(GmailClient.class);

    protected final OAuth2Authenticator oAuth2Authenticator = new OAuth2Authenticator();
    private boolean connected = false;
	private boolean usePush = true;
	private String accountName;
	private String serverAddress;
	private String userEmail;
	private String accessToken;
	private String refreshToken;
    private Store store;
    // private IMAPFolder folder;
    private static final JsonFactory JSON_FACTORY = new JacksonFactory();
    private static final String GOOGLE_CREDENTIALS_CONFIG = ConfigProvider.getConfig().getValue("GOOGLE_CREDENTIALS", String.class);
    private static GoogleCredentials googleCredentials;

    public GmailClient(Account account) {

        super(account);
        // USER_NAME = getProperty("email.username");
        // created = true;
        // emails = new HashSet<Mail>();

        this.accountName = account.getDisplayName();
        this.userEmail = account.getEmailAddress();
        this.refreshToken = account.getOAuth2RefreshToken();
        this.accessToken = account.getOAuth2AccessToken();

        Jsonb jsonb = JsonbBuilder.create();
        googleCredentials = jsonb.fromJson(GOOGLE_CREDENTIALS_CONFIG, GoogleCredentials.class);

        OAuth2Authenticator.initialize();

        log.info("GmailClient created");
    }

    /* public void promptCode() {
		String redirectUrl = "urn:ietf:wg:oauth:2.0:oob";
		String url = new GoogleAuthorizationCodeRequestUrl(googleCredentials.getClient_id(), redirectUrl,
				Collections.singleton("https://mail.google.com/")).setAccessType("offline").build();
		try {
			if (Desktop.isDesktopSupported()) {
				Desktop.getDesktop().browse(new URI(url));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("If a url did not automatically open, please open the following in your browser:\n" + url);
		try {
			String start = getProperty("gmail.code");
			for (int i = 0; i < 4; i++) {
				Thread.sleep(20000);
				if (!getProperty("gmail.code").equals(start))
					break;
			}
			if (getProperty("gmail.code").equals(start)) {
				promptCode();
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		GoogleTokenResponse response;
		try {
			response = new GoogleAuthorizationCodeTokenRequest(new NetHttpTransport(), new JacksonFactory(),
            googleCredentials.getClient_id(), googleCredentials.getClient_secret(), getProperty("gmail.code"),
					redirectUrl).execute();
			refreshToken = response.getRefreshToken();
			accessToken = response.getAccessToken();
			System.out.println("Tokens added.");
		} catch (Exception e) {
		}
	} */

    public void refreshTokens() {
		try {
            log.info("GmailClient/refreshTokens client_id: " + googleCredentials.getClient_id());
            log.info("GmailClient/refreshTokens secret: " + googleCredentials.getClient_secret());
			HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
			GoogleTokenResponse response = new GoogleRefreshTokenRequest(httpTransport, JSON_FACTORY, refreshToken, googleCredentials.getClient_id(), googleCredentials.getClient_secret()).execute();
			accessToken = response.getAccessToken();
			System.out.println("Access Token updated: " + accessToken);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    public void login() {
        log.info("GmailClient/SynchronizedTask login");
        if (userEmail.equals("izboran@gmail.com")) {
            refreshTokens();
            try {
                // OAuth2Authenticator.initialize();
                store = OAuth2Authenticator.connectToImap(userEmail, accessToken);
                log.info("Entering connect()");
                try {
                    Folder inbox = store.getFolder("INBOX");
                    inbox.open(Folder.READ_WRITE);
                    // server.getFolder("");
                    // prober.start();
                    connected = true;
                    log.info(accountName + " connected!");
                    // onConnect();
                } catch (IllegalStateException ex) {
                    log.warn(accountName, ex);
                    connected = true;
                    // onConnect();
                }   
                log.info("Exiting connect()");
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    public void synchronize() {
        log.info("GmailClient/SynchronizedTask synchronize");
    }
}
