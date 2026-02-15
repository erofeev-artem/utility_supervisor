package org.monkey_business.utility_supervisor.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Builder
@Getter
public class KoltushiOutageResponseDto {
    String href;
    LocalDate date;
    String description;
    List<String> matchedTps;
}