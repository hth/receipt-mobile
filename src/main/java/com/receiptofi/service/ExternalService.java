package com.receiptofi.service;

import com.receiptofi.domain.BizStoreEntity;
import com.receiptofi.utils.DateUtil;
import com.receiptofi.utils.PerformanceProfiling;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;

import org.springframework.stereotype.Service;

import org.joda.time.DateTime;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * User: hitender
 * Date: 5/9/13
 * Time: 7:51 PM
 */
@Service
public final class ExternalService {
    private static final Logger log = LoggerFactory.getLogger(ExternalService.class);

    private static final String ADDRESS_DECODE_URL = "https://maps.googleapis.com/maps/api/geocode/json?sensor=false&address=";

    /**
     * Finds Latitude and Longitude for a given address from Google Web Service
     *
     * @param bizStoreEntity
     * @throws Exception
     */
    public void decodeAddress(BizStoreEntity bizStoreEntity) throws IOException {
        DateTime time = DateUtil.now();
        URL url = null;
        try {
            String encodeAddress = URLEncoder.encode(bizStoreEntity.getAddress(), "UTF-8");
            url = new URL(ADDRESS_DECODE_URL + encodeAddress);
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));

            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }
            reader.close();

            populateBizStore(output, bizStoreEntity);
            PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName());
        } catch (MalformedURLException e) {
            String result = (url == null) ? bizStoreEntity.getAddress() : url.toString() + ", " +  bizStoreEntity.getAddress();
            log.error("URL: " + result + ", " + e.getLocalizedMessage());
            PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName(), false);
            throw new MalformedURLException("URL: " + result + ", " + e.getLocalizedMessage());
        } catch (IOException e) {
            String result = (url == null) ? bizStoreEntity.getAddress() : url.toString() + ", " +  bizStoreEntity.getAddress();
            PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName(), false);
            throw new IOException("URL: " + result + ", " + e.getLocalizedMessage());
        }
    }

    /**
     * Populates address, lat, lng for address submitted
     *
     * @param output - JSON returned by google web service
     * @param bizStoreEntity
     */
    private void populateBizStore(StringBuilder output, BizStoreEntity bizStoreEntity) {
        JsonElement root = new JsonParser().parse(output.toString());
        JsonArray results = root.getAsJsonObject().getAsJsonArray("results");
        Iterator<JsonElement> jsonElementIterator = results.iterator();
        if(jsonElementIterator.hasNext()) {
            JsonElement element = jsonElementIterator.next();

            JsonElement geometry = element.getAsJsonObject().get("geometry");
            JsonElement location = geometry.getAsJsonObject().get("location");
            double lat = location.getAsJsonObject().get("lat").getAsDouble();
            double lng = location.getAsJsonObject().get("lng").getAsDouble();
            bizStoreEntity.setLat(lat);
            bizStoreEntity.setLng(lng);

            JsonElement formatted_address_element = element.getAsJsonObject().get("formatted_address");
            String formatted_address = formatted_address_element.getAsString();
            bizStoreEntity.setAddress(formatted_address);
        }
    }
}
