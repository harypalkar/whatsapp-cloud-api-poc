package com.example.whatsapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Altitude Labs outbound message request")
public class SendMessageV1Request {

    @NotBlank
    @Pattern(regexp = "^[0-9]+$", message = "customerNumber must be digits only with country code")
    @Schema(example = "917506426501")
    private String customerNumber;

    @Schema(example = "Harish")
    private String customerName;

    @Schema(example = "WELCOME100")
    private String promoCode;

    @Schema(example = "Hello Harish, welcome to Altitude Labs.")
    private String message;

    /**
     * Optional: text | template | image | document | video | interactive
     */
    @Builder.Default
    @Schema(example = "text")
    private String type = "text";

    @Schema(description = "Approved template name when type=template", example = "hello_world")
    private String templateName;

    @Builder.Default
    private String languageCode = "en_US";

    @Schema(description = "Public media URL for image/document/video")
    private String mediaUrl;

    @Schema(description = "Caption for media messages")
    private String caption;

    @Schema(description = "Filename for document messages")
    private String filename;

    @Schema(description = "Latitude when type=location", example = "19.0760")
    private Double latitude;

    @Schema(description = "Longitude when type=location", example = "72.8777")
    private Double longitude;

    @Schema(example = "Altitude Labs HQ")
    private String locationName;

    @Schema(example = "Mumbai, India")
    private String locationAddress;
}
