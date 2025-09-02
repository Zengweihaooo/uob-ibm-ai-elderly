package com.voicecommand.model;

import java.util.List;
import java.util.Map;

public class FunctionInfo {
    private String name;
    private String description;
    private Map<String, ParameterInfo> parameters;
    private List<String> examples;
    private List<String> keywords;
    private String endpoint;

    // Constructor
    public FunctionInfo() {}

    public FunctionInfo(String name, String description, Map<String, ParameterInfo> parameters, 
                       List<String> examples, List<String> keywords, String endpoint) {
        this.name = name;
        this.description = description;
        this.parameters = parameters;
        this.examples = examples;
        this.keywords = keywords;
        this.endpoint = endpoint;
    }

    // Getter and Setter methods
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, ParameterInfo> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, ParameterInfo> parameters) {
        this.parameters = parameters;
    }

    public List<String> getExamples() {
        return examples;
    }

    public void setExamples(List<String> examples) {
        this.examples = examples;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    // Inner class: Parameter information
    public static class ParameterInfo {
        private String type;
        private boolean required;
        private String description;

        public ParameterInfo() {}

        public ParameterInfo(String type, boolean required, String description) {
            this.type = type;
            this.required = required;
            this.description = description;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public boolean isRequired() {
            return required;
        }

        public void setRequired(boolean required) {
            this.required = required;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}

