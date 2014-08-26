package com.receiptofi.mobile.domain.mapping;

import com.receiptofi.domain.CommentEntity;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * User: hitender
 * Date: 8/25/14 12:13 AM
 */
@JsonAutoDetect (
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE
)
@JsonPropertyOrder (alphabetic = true)
@JsonIgnoreProperties (ignoreUnknown = true)
@JsonInclude (JsonInclude.Include.NON_NULL)
public final class Comment {

    @SuppressWarnings("unused")
    @JsonProperty ("text")
    private String text;

    private Comment(CommentEntity commentEntity) {
        if(commentEntity != null) {
            this.text = commentEntity.getText();
        }
    }

    public static Comment newInstance(CommentEntity commentEntity) {
        return new Comment(commentEntity);
    }
}
