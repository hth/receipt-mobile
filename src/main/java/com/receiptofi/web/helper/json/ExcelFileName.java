package com.receiptofi.web.helper.json;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

/**
 * User: hitender
 * Date: 12/7/13 6:10 PM
 */
public class ExcelFileName {

    private String filename;

    public ExcelFileName(String filename) {
        this.filename = filename;
    }

    @JsonProperty("filename")
    public String getFilename() {
        return this.filename;
    }

    //Converts this object to JSON representation
    public String asJson() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Writer writer = new StringWriter();
            mapper.writeValue(writer, this);
            return writer.toString();
        } catch (IOException e) {
            return "{}";
        }
    }
}
