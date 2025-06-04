package org.monkey_business.utility_supervisor.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.monkey_business.utility_supervisor.dto.RossetiOutageResponseDto;
import org.monkey_business.utility_supervisor.exception.ParseException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RossetiTableParser implements TableParser {
    @Override
    public List<RossetiOutageResponseDto> parse(Document document) {
        List<RossetiOutageResponseDto> dtos = new ArrayList<>();
        String className = "even";
        Element table = document.select("table").get(1);
        Element tbody = table.select("tbody").get(0);
        Elements dataRows = tbody.select("tr");
        try {
            for (Element dataRow : dataRows) {
                if (dataRow.hasClass(className)) {
                    RossetiOutageResponseDto.RossetiOutageResponseDtoBuilder builder = RossetiOutageResponseDto.builder();
                    builder.dataRecordId(dataRow.getElementsByClass(className).attr("data-record-id"));
                    Elements td = dataRow.getElementsByTag("td");
                    builder.region(td.get(0).text())
                            .district(td.get(1).text())
                            .address(td.get(2).getElementsByTag("span").eachText())
                            .startDate(td.get(3).text())
                            .startTime(td.get(4).text())
                            .endDate(td.get(5).text())
                            .endTime(td.get(6).text())
                            .branch(td.get(7).text())
                            .res(td.get(8).text())
                            .comment(td.get(9).text())
                            .fias(td.get(10).getElementsByTag("small").eachText());
                    dtos.add(builder.build());
                }
            }
        } catch (Exception e) {
            throw new ParseException("Parser is broken");
        }
        return dtos;
    }
}
