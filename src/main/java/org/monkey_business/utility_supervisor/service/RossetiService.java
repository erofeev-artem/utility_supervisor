package org.monkey_business.utility_supervisor.service;

import org.jsoup.nodes.Document;
import org.monkey_business.utility_supervisor.client.RossetiClient;
import org.monkey_business.utility_supervisor.dto.RossetiResultOutageDto;
import org.monkey_business.utility_supervisor.dto.RossetiOutageResponseDto;
import org.monkey_business.utility_supervisor.parser.RossetiParser;
import org.monkey_business.utility_supervisor.config.RossetiConfig;
import org.monkey_business.utility_supervisor.request.RossetiRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RossetiService {

    private final RossetiClient rossetiClient;
    private final RossetiConfig rossetiConfig;
    private final RossetiParser rossetiTableParser;

    @Autowired
    public RossetiService(RossetiClient rossetiClient,
                          RossetiConfig rossetiConfig, RossetiParser rossetiTableParser) {
        this.rossetiClient = rossetiClient;
        this.rossetiConfig = rossetiConfig;
        this.rossetiTableParser = rossetiTableParser;
    }

    public RossetiResultOutageDto<RossetiOutageResponseDto> find(RossetiRequest request) {
        Document document = rossetiClient.call(request, rossetiConfig.getUrl());
        return new RossetiResultOutageDto<>(rossetiTableParser.parse(document), document.connection().response().statusCode());
    }

    public RossetiResultOutageDto<RossetiOutageResponseDto> find(RossetiRequest request, int limit) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        RossetiResultOutageDto<RossetiOutageResponseDto> rossetiResultOutageDto = find(request);
        List<RossetiOutageResponseDto> data = rossetiResultOutageDto.getData();
        rossetiResultOutageDto.getData().sort(Comparator.comparing(event -> LocalDate.parse(event.getStartDate(), formatter)));
        rossetiResultOutageDto.setData(data.stream().limit(limit).collect(Collectors.toList()));
        return rossetiResultOutageDto;
    }
}
