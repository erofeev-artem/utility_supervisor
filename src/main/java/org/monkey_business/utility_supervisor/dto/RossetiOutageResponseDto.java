package org.monkey_business.utility_supervisor.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class RossetiOutageResponseDto {
    String dataRecordId;
    String region;
    String district;
    List<String> address;
    String startDate;
    String startTime;
    String endDate;
    String endTime;
    String branch;
    String res;
    String comment;
    List<String> fias;
}
