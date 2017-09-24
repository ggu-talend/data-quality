// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.dataquality.semantic.model;

import java.io.Serializable;
import java.net.URL;
import java.util.Date;
import java.util.List;

public class DQCategory implements Serializable {

    private String id;

    private String name;

    private String label;

    private String description;

    private CategoryType type; // A type: RE, DD, KW (needed? How to manage OR clause: RE or in DD?)

    private CategoryPrivacyLevel privacyLevel;

    private URL accessLink;// The Regex or the DD/KW access link

    private List<URL> dataSources;// data sources where possible

    private String version;

    private String creator;

    private Date createdAt;

    private String technicalDataType;

    private List<String> countries;

    private List<String> languages;

    private DQRegEx regEx;

    private Date modifiedAt;

    private String lastModifier;

    private Boolean completeness;

    private Date publishedAt;

    private String lastPublisher;

    private ValidationMode validationMode;

    private CategoryState state;

    private List<DQCategory> children;

    private List<DQCategory> parents;

    private Boolean isModified;

    private Boolean isDeleted;

    public DQCategory(String id) {
        this.id = id;
    }

    public DQCategory() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CategoryType getType() {
        return type;
    }

    public void setType(CategoryType type) {
        this.type = type;
    }

    public CategoryPrivacyLevel getPrivacyLevel() {
        return privacyLevel;
    }

    public void setPrivacyLevel(CategoryPrivacyLevel privacyLevel) {
        this.privacyLevel = privacyLevel;
    }

    public URL getAccessLink() {
        return accessLink;
    }

    public void setAccessLink(URL accessLink) {
        this.accessLink = accessLink;
    }

    public List<URL> getDataSources() {
        return dataSources;
    }

    public void setDataSources(List<URL> dataSources) {
        this.dataSources = dataSources;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getTechnicalDataType() {
        return technicalDataType;
    }

    public void setTechnicalDataType(String technicalDataType) {
        this.technicalDataType = technicalDataType;
    }

    public List<String> getCountries() {
        return countries;
    }

    public void setCountries(List<String> countries) {
        this.countries = countries;
    }

    public List<String> getLanguages() {
        return languages;
    }

    public void setLanguages(List<String> languages) {
        this.languages = languages;
    }

    public DQRegEx getRegEx() {
        return regEx;
    }

    public void setRegEx(DQRegEx regEx) {
        this.regEx = regEx;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getCompleteness() {
        return completeness;
    }

    public void setCompleteness(Boolean completeness) {
        this.completeness = completeness;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Date getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(Date modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    public String getLastModifier() {
        return lastModifier;
    }

    public void setLastModifier(String lastModifier) {
        this.lastModifier = lastModifier;
    }

    public Date getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Date publishedAt) {
        this.publishedAt = publishedAt;
    }

    public String getLastPublisher() {
        return lastPublisher;
    }

    public void setLastPublisher(String lastPublisher) {
        this.lastPublisher = lastPublisher;
    }

    public CategoryState getState() {
        return state;
    }

    public void setState(CategoryState state) {
        this.state = state;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public List<DQCategory> getChildren() {
        return children;
    }

    public void setChildren(List<DQCategory> children) {
        this.children = children;
    }

    public List<DQCategory> getParents() {
        return parents;
    }

    public void setParents(List<DQCategory> parents) {
        this.parents = parents;
    }

    public ValidationMode getValidationMode() {
        return validationMode;
    }

    public void setValidationMode(ValidationMode validationMode) {
        this.validationMode = validationMode;
    }

    public Boolean isModified() {
        return isModified;
    }

    public void setModified(Boolean modified) {
        isModified = modified;
    }

    public Boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(Boolean deleted) {
        isDeleted = deleted;
    }

    @Override
    public String toString() {
        return String.format(
                "Category [ID=%s  Type=%s  Name=%-20s  Label=%-20s  Completeness=%-5s  Description=%s Creator=%s Last Modifier=%s State=%-20s Last published=%s]",
                id, type, name, label, completeness, description, creator, lastModifier, state, publishedAt);
    }

}
