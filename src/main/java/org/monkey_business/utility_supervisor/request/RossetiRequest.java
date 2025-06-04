package org.monkey_business.utility_supervisor.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public record RossetiRequest(
//      административный район
        @JsonProperty("city")
        String city,
        @JsonProperty("date_start")
        LocalDate startDate,
        @JsonProperty("date_finish")
        LocalDate finishDate,
//      населенный пункт / Адрес
        @JsonProperty("street")
        String street) {
}
