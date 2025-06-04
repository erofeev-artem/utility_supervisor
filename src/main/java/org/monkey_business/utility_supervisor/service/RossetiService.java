package org.monkey_business.utility_supervisor.service;

import org.jsoup.nodes.Document;
import org.monkey_business.utility_supervisor.client.RossetiClient;
import org.monkey_business.utility_supervisor.dto.ResultOutageDto;
import org.monkey_business.utility_supervisor.dto.RossetiOutageResponseDto;
import org.monkey_business.utility_supervisor.parser.RossetiTableParser;
import org.monkey_business.utility_supervisor.properties.RossetiConfig;
import org.monkey_business.utility_supervisor.request.RossetiRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

//@Service
public class RossetiService {

    private final RossetiClient rossetiClient;
    private final RossetiConfig rossetiConfig;
    private final RossetiTableParser rossetiTableParser;

    @Autowired
    public RossetiService(RossetiClient rossetiClient,
                          RossetiConfig rossetiConfig, RossetiTableParser rossetiTableParser) {
        this.rossetiClient = rossetiClient;
        this.rossetiConfig = rossetiConfig;
        this.rossetiTableParser = rossetiTableParser;
    }

    public ResultOutageDto<RossetiOutageResponseDto> find(RossetiRequest request) {
        Document document = rossetiClient.call(request, rossetiConfig.getUrl());
        return new ResultOutageDto<>(rossetiTableParser.parse(document), document.connection().response().statusCode());
    }

    public ResultOutageDto<RossetiOutageResponseDto> find(RossetiRequest request, int limit) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        ResultOutageDto<RossetiOutageResponseDto> resultOutageDto = find(request);
        List<RossetiOutageResponseDto> data = resultOutageDto.getData();
        resultOutageDto.getData().sort(Comparator.comparing(event -> LocalDate.parse(event.getStartDate(), formatter)));
        resultOutageDto.setData(data.stream().limit(limit).collect(Collectors.toList()));
        return resultOutageDto;
    }
}
