package com.denis.home.sunnynotes.dropbox;
import com.denis.home.sunnynotes.Utility;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.http.OkHttpRequestor;
import com.dropbox.core.v2.DbxClientV2;

/**
 * Created by Denis on 25.03.2016.
 */
/*  From com.dropbox.core.examples.android; */
/**
 * Singleton instance of {@link DbxClientV2} and friends
 */
public class DropboxClientFactory {
    private static DbxClientV2 sDbxClient;

    public static void init(String accessToken) {
        if (sDbxClient == null) {
            String userLocale = Utility.getUserLocale();
            DbxRequestConfig requestConfig = new DbxRequestConfig(
                    Utility.getDropboxClientIdentifier(),
                    userLocale,
                    OkHttpRequestor.INSTANCE);

            sDbxClient = new DbxClientV2(requestConfig, accessToken);
        }
    }

    public static DbxClientV2 getClient() {
        if (sDbxClient == null) {
            throw new IllegalStateException("Client not initialized.");
        }
        return sDbxClient;
    }
}
