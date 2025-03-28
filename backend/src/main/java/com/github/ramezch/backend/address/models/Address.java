package com.github.ramezch.backend.address.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.annotation.Nullable;

public record Address(
        @NotBlank(message = "Address ID cannot be blank")
        String id,

        @Nullable
        @Size(max = 100, message = "Country must be less than 100 characters")
        String country,

        @NotBlank(message = "City cannot be blank")
        @Size(max = 100, message = "City must be less than 100 characters")
        String city,

        @NotBlank(message = "Street address cannot be blank")
        @Size(max = 200, message = "Street address must be less than 200 characters")
        String street,

        @Nullable
        @Pattern(regexp = "^[a-zA-Z0-9\\-\\s]{3,10}$",
                message = "Postal code must be 3-10 alphanumeric characters")
        String postalCode
) {
    /**
     * Returns a formatted address string suitable for mailing labels.
     * Handles optional country field gracefully.
     */
    public String toFormattedString() {
        StringBuilder sb = new StringBuilder();
        sb.append(street).append("\n")
                .append(city);

        if (postalCode != null && !postalCode.isBlank()) {
            sb.append("\n").append(postalCode);
        }

        if (country != null && !country.isBlank()) {
            sb.append("\n").append(country);
        }

        return sb.toString();
    }

    /**
     * German-style inline format:
     * "Country, Street, City, PostalCode"
     * Example: "Germany, Alexanderplatz 1, Berlin, 10178"
     */
    public String toGermanInlineFormat() {
        return String.join(", ",
                country != null ? country : "",
                street,
                city,
                postalCode != null ? postalCode : ""
        ).replace(", , ", ", ").replaceAll("^, ", "");
    }
}