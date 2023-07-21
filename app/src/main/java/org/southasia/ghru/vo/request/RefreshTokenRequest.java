package org.southasia.ghru.vo.request;

import androidx.databinding.BaseObservable;

import javax.inject.Inject;


/**
 * Created by shanuka on 10/26/17.
 */

public class RefreshTokenRequest extends BaseObservable {

    static final String CLIENT_ID = "1";
    static final String CLIENT_SECRET = "wQiDB3rFxclEKK04ZUJsU4Fk5LMtkyBeAaRQyXVt";
    static final String GRANT_TYPE = "refresh_token";
    private String refreshToken;

    private String clientId;
    private String clientSecret;
    private String grantType;
    private String passwordEmptyMessage;

    @Inject
    RefreshTokenRequest() {
        refreshToken = new String();
        clientId = CLIENT_ID;
        clientSecret = CLIENT_SECRET;
        grantType = GRANT_TYPE;

    }


    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }


    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }


    public String getClient_secret() {
        return clientSecret;
    }


    public String getGrantType() {
        return grantType;
    }

    public void setGrantType(String grantType) {
        this.grantType = grantType;
    }

    @Override
    public String toString() {
        return "RefreshTokenRequest{" +
                "refreshToken='" + refreshToken + '\'' +
                ", clientId='" + clientId + '\'' +
                ", clientSecret='" + clientSecret + '\'' +
                ", grantType='" + grantType + '\'' +
                ", passwordEmptyMessage='" + passwordEmptyMessage + '\'' +
                '}';
    }
}
