package com.receiptofi.web.helper.json;

import com.receiptofi.domain.MileageEntity;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * User: hitender
 * Date: 1/5/14 11:24 PM
 */
public final class Mileages {

    @JsonProperty("ms")
    private List<Mileage> mileages = new LinkedList<>();

    @JsonProperty("mm")
    private int monthlyMileage;

    public List<Mileage> getMileages() {
        return mileages;
    }

    public void setMileages(List<MileageEntity> mileages) {
        for(MileageEntity mileageEntity : mileages) {
            this.setMileages(mileageEntity);
        }
    }

    public void setMileages(MileageEntity mileageEntity) {
        this.mileages.add(Mileage.newInstance(mileageEntity.getId(),
                mileageEntity.getStart(),
                mileageEntity.getEnd(),
                mileageEntity.getStartDate(),
                mileageEntity.getEndDate(),
                mileageEntity.getMileageNotes() == null ? StringUtils.EMPTY : mileageEntity.getMileageNotes().getText(),
                mileageEntity.getTotal(), mileageEntity.isComplete())
        );
    }

    public int getMonthlyMileage() {
        return monthlyMileage;
    }

    public void setMonthlyMileage(int monthlyMileage) {
        this.monthlyMileage = monthlyMileage;
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

class Mileage {

    @JsonProperty("i")
    private String id;

    @JsonProperty("s")
    private int start;

    @JsonProperty("e")
    private int end;

    @JsonProperty("sd")
    private Date startDate;

    @JsonProperty("ed")
    private Date endDate;

    @JsonProperty("n")
    private String notes;

    @JsonProperty("t")
    private int total;

    @JsonProperty("c")
    private boolean complete;

    @SuppressWarnings("unused")
    public Mileage() {}

    private Mileage(String id, int start, int end, Date startDate, Date endDate, String notes, int total, boolean complete) {
        this.id = id;
        this.start = start;
        this.end = end;
        this.startDate = startDate;
        this.endDate = endDate;
        this.notes = notes;
        this.total = total;
        this.complete = complete;
    }

    public static Mileage newInstance(String id, int start, int end, Date startDate, Date endDate, String notes, int total, boolean complete) {
        return new Mileage(id, start, end, startDate, endDate, notes, total, complete);
    }

    public String getId() {
        return id;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    @JsonProperty("na")
    public String getNotesAbbreviated() {
        return StringUtils.abbreviate(notes, 22);
    }

    public String getNotes() {
        return notes;
    }

    public int getTotal() {
        return total;
    }

    public boolean isComplete() {
        return complete;
    }
}



