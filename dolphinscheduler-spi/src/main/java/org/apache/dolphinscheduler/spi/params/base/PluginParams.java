package org.apache.dolphinscheduler.spi.params.base;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.apache.dolphinscheduler.spi.params.InputParam;
import org.apache.dolphinscheduler.spi.params.RadioParam;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class PluginParams {

    /**
     * param name
     */
    private String name;

    private ParamsProps props;

    private String formType;

    private String title;

    private Object value;

    private List<Validate> validateList;

    public PluginParams() {

    }

    public PluginParams(String name, FormType formType, String title) {
        requireNonNull(name , "name is null");
        requireNonNull(formType , "formType is null");
        requireNonNull(title , "title is null");
        this.name = name;
        this.formType = formType.getFormType();
        this.title = title;
    }

    public PluginParams addValidate(Validate validate) {
        if(this.getValidateList() == null) {
            this.validateList = new ArrayList<>();
        }
        this.getValidateList().add(validate);
        return this;
    }

    @JsonProperty("field")
    public String getName() {
        return name;
    }

    @JsonProperty(value = "field")
    public PluginParams setName(String name) {
        this.name = name;
        return this;
    }

    @JsonProperty("props")
    public ParamsProps getProps() {
        return props;
    }

    @JsonProperty(value = "props")
    public PluginParams setProps(ParamsProps props) {
        this.props = props;
        return this;
    }

    @JsonProperty("type")
    public String getFormType() {
        return formType;
    }

    @JsonProperty(value = "type")
    public PluginParams setFormType(String formType) {
        this.formType = formType;
        return this;
    }

    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    public PluginParams setTitle(String title) {
        this.title = title;
        return this;
    }

    @JsonProperty("value")
    public Object getValue() {
        return value;
    }

    public PluginParams setValue(Object value) {
        this.value = value;
        return this;
    }

    @JsonProperty("validate")
    public List<Validate> getValidateList() {
        return validateList;
    }

    @JsonProperty(value = "validate")
    public PluginParams setValidateList(List<Validate> validateList) {
        this.validateList = validateList;
        return this;
    }
}


