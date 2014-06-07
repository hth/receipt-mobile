package com.receiptofi.domain;

import com.receiptofi.domain.types.ProviderEnum;
import com.receiptofi.domain.types.UserLevelEnum;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.social.facebook.api.EducationEntry;
import org.springframework.social.facebook.api.Reference;
import org.springframework.social.facebook.api.WorkEntry;

/**
 * User: hitender
 * Date: 4/13/14 2:19 AM
 */
@Document(collection = "USER_PROFILE")
@CompoundIndexes({
        @CompoundIndex(name = "user_profile_provider_uid_em_idx",   def = "{'RID': -1, 'UID': -1, 'P_E': 1, 'EM' : 1}", unique = true)
})
public final class UserProfileEntity extends BaseEntity {

    @NotNull
    @Field("RID")
    private String receiptUserId;

    @NotNull
    @Field("UID")
    private String userId;

    @NotNull
    @Field("P_E")
    private ProviderEnum providerId;

    @Field("UN")
    private String username;

    @Field("N")
    private String name;

    @Field("FN")
    private String firstName;

    @Field("MN")
    private String middleName;

    @Field("LN")
    private String lastName;

    @Field("GE")
    private String gender;

    @Field("LO")
    private Locale locale;

    @Field("URL")
    private String link;

    @Field("WS")
    private String website;

    @Field("EM")
    private String email;

    @Field("TP_ID")
    private String thirdPartyId;

    @Field("TZ")
    private Float timezone;

    @Field("UT")
    private Date updatedTime;

    @Field("VR")
    private Boolean verified;

    @Field("AB")
    private String about;

    @Field("BI")
    private String bio;

    @Field("BD")
    private String birthday;

    @DBRef
    @Field("LK")
    private Reference location;

    @DBRef
    @Field("HT")
    private Reference hometown;

    @DBRef
    @Field("II")
    private List<String> interestedIn;

    @DBRef
    @Field("IP")
    private List<Reference> inspirationalPeople;

    @DBRef
    @Field("LA")
    private List<Reference> languages;

    @DBRef
    @Field("SP")
    private List<Reference> sports;

    @DBRef
    @Field("FT")
    private List<Reference> favoriteTeams;

    @DBRef
    @Field("FA")
    private List<Reference> favoriteAthletes;

    @Field("RL")
    private String religion;

    @Field("PO")
    private String political;

    @Field("QU")
    private String quotes;

    @Field("RS")
    private String relationshipStatus;

    @Field("SO")
    private Reference significantOther;

    @DBRef
    @Field("WE")
    private List<WorkEntry> work;

    @DBRef
    @Field("EE")
    private List<EducationEntry> education;

    @NotNull
    @Field("USER_LEVEL_ENUM")
    private UserLevelEnum level = UserLevelEnum.USER;

    /** To make bean happy */
    public UserProfileEntity() {}

    private UserProfileEntity(String email, String firstName, String lastName, String receiptUserId) {
        super();
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.receiptUserId = receiptUserId;
    }

	/**
	 * This method is used when the Entity is created for the first time.
	 *
	 * @param firstName
	 * @param lastName
	 * @return
	 */
	public static UserProfileEntity newInstance(String email, String firstName, String lastName, String receiptUserId) {
		return new UserProfileEntity(email, firstName, lastName, receiptUserId);
    }

    public String getReceiptUserId() {
        return receiptUserId;
    }

    public void setReceiptUserId(String receiptUserId) {
        this.receiptUserId = receiptUserId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public ProviderEnum getProviderId() {
        return providerId;
    }

    public void setProviderId(ProviderEnum providerId) {
        this.providerId = providerId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return StringUtils.isBlank(name) ? firstName + " " + lastName : name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getThirdPartyId() {
        return thirdPartyId;
    }

    public void setThirdPartyId(String thirdPartyId) {
        this.thirdPartyId = thirdPartyId;
    }

    public Float getTimezone() {
        return timezone;
    }

    public void setTimezone(Float timezone) {
        this.timezone = timezone;
    }

    public Date getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(Date updatedTime) {
        this.updatedTime = updatedTime;
    }

    public Boolean getVerified() {
        return verified;
    }

    public void setVerified(Boolean verified) {
        this.verified = verified;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public Reference getLocation() {
        return location;
    }

    public void setLocation(Reference location) {
        this.location = location;
    }

    public Reference getHometown() {
        return hometown;
    }

    public void setHometown(Reference hometown) {
        this.hometown = hometown;
    }

    public List<String> getInterestedIn() {
        return interestedIn;
    }

    public void setInterestedIn(List<String> interestedIn) {
        this.interestedIn = interestedIn;
    }

    public List<Reference> getInspirationalPeople() {
        return inspirationalPeople;
    }

    public void setInspirationalPeople(List<Reference> inspirationalPeople) {
        this.inspirationalPeople = inspirationalPeople;
    }

    public List<Reference> getLanguages() {
        return languages;
    }

    public void setLanguages(List<Reference> languages) {
        this.languages = languages;
    }

    public List<Reference> getSports() {
        return sports;
    }

    public void setSports(List<Reference> sports) {
        this.sports = sports;
    }

    public List<Reference> getFavoriteTeams() {
        return favoriteTeams;
    }

    public void setFavoriteTeams(List<Reference> favoriteTeams) {
        this.favoriteTeams = favoriteTeams;
    }

    public List<Reference> getFavoriteAthletes() {
        return favoriteAthletes;
    }

    public void setFavoriteAthletes(List<Reference> favoriteAthletes) {
        this.favoriteAthletes = favoriteAthletes;
    }

    public String getReligion() {
        return religion;
    }

    public void setReligion(String religion) {
        this.religion = religion;
    }

    public String getPolitical() {
        return political;
    }

    public void setPolitical(String political) {
        this.political = political;
    }

    public String getQuotes() {
        return quotes;
    }

    public void setQuotes(String quotes) {
        this.quotes = quotes;
    }

    public String getRelationshipStatus() {
        return relationshipStatus;
    }

    public void setRelationshipStatus(String relationshipStatus) {
        this.relationshipStatus = relationshipStatus;
    }

    public Reference getSignificantOther() {
        return significantOther;
    }

    public void setSignificantOther(Reference significantOther) {
        this.significantOther = significantOther;
    }

    public List<WorkEntry> getWork() {
        return work;
    }

    public void setWork(List<WorkEntry> work) {
        this.work = work;
    }

    public List<EducationEntry> getEducation() {
        return education;
    }

    public void setEducation(List<EducationEntry> education) {
        this.education = education;
    }

    public UserLevelEnum getLevel() {
        return level;
    }

    public void setLevel(UserLevelEnum level) {
        this.level = level;
    }
}
