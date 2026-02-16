package org.monkey_business.utility_supervisor.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Builder
@Getter
public class KoltushiOutageResponseDto {
    private String href;
    private LocalDate date;
    private String description;
    private List<String> matchedTps;
}