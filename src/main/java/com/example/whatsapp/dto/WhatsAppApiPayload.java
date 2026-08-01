package com.example.whatsapp.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WhatsAppApiPayload {

    @JsonProperty("messaging_product")
    private String messagingProduct;

    @JsonProperty("recipient_type")
    private String recipientType;

    private String to;

    private String type;

    private TextContent text;
    private TemplateContent template;
    private MediaContent image;
    private MediaContent document;
    private MediaContent video;
    private MediaContent audio;
    private LocationContent location;
    private InteractiveContent interactive;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TextContent {
        private String body;
        @JsonProperty("preview_url")
        private Boolean previewUrl;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TemplateContent {
        private String name;
        private TemplateLanguage language;
        private List<TemplateComponent> components;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TemplateLanguage {
        private String code;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TemplateComponent {
        private String type;
        private List<TemplateParameter> parameters;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TemplateParameter {
        private String type;
        private String text;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class MediaContent {
        private String link;
        private String id;
        private String caption;
        private String filename;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class LocationContent {
        private Double latitude;
        private Double longitude;
        private String name;
        private String address;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class InteractiveContent {
        private String type;
        private InteractiveBody body;
        private InteractiveAction action;
        private InteractiveHeader header;
        private InteractiveFooter footer;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InteractiveBody {
        private String text;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class InteractiveHeader {
        private String type;
        private String text;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InteractiveFooter {
        private String text;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class InteractiveAction {
        private List<InteractiveButton> buttons;
        @JsonProperty("button")
        private String listButton;
        private List<InteractiveSection> sections;
        /** Used by interactive type cta_url */
        private String name;
        private CtaUrlParameters parameters;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CtaUrlParameters {
        @JsonProperty("display_text")
        private String displayText;
        private String url;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InteractiveButton {
        private String type;
        private InteractiveReply reply;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InteractiveReply {
        private String id;
        private String title;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InteractiveSection {
        private String title;
        private List<InteractiveRow> rows;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InteractiveRow {
        private String id;
        private String title;
        private String description;
    }
}
