package com.receiptofi.domain.social;

import com.receiptofi.domain.BaseEntity;

import javax.validation.constraints.NotNull;

import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;

/**
 * User: hitender
 * Date: 3/30/14 3:27 PM
 */
@Document(collection = "REMEMBER_ME_TOKEN")
@CompoundIndexes({
        @CompoundIndex(name = "remember_username_idx", def = "{'UN': 1}"),
        @CompoundIndex(name = "remember_series_idx", def = "{'S': 1}")
})
public class RememberMeTokenEntity extends BaseEntity {

    @NotNull
    @Field("UN")
    private String username;

    @NotNull
    @Field("S")
    private String series;

    @NotNull
    @Field("TV")
    private String tokenValue;

    @SuppressWarnings("unused")
    public RememberMeTokenEntity() {}

    public RememberMeTokenEntity(PersistentRememberMeToken persistentToken) {
        this.series = persistentToken.getSeries();
        this.username = persistentToken.getUsername();
        this.tokenValue = persistentToken.getTokenValue();
        this.setCreateAndUpdate(persistentToken.getDate());
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSeries() {
        return series;
    }

    public void setSeries(String series) {
        this.series = series;
    }

    public String getTokenValue() {
        return tokenValue;
    }

    public void setTokenValue(String tokenValue) {
        this.tokenValue = tokenValue;
    }
}
