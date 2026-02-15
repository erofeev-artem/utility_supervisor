package org.monkey_business.utility_supervisor.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.monkey_business.utility_supervisor.config.KoltushiConfig;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class KoltushiOutagePageParser {

    private final Set<String> targetTp;

    public KoltushiOutagePageParser(KoltushiConfig koltushiConfig) {
        this.targetTp = Set.of(koltushiConfig.getTargetTp().split(","));
    }

    public Map<String, List<String>> parse(Document document) {
        Map<String, List<String>> result = new HashMap<>();

        Elements uls = document.select(".entry-inner ul");
        for (Element ul : uls) {
            List<String> matched = targetTp.stream()
                    .filter(tp -> ul.select("li").stream().anyMatch(li -> li.text().contains(tp)))
                    .toList();

            if (!matched.isEmpty()) {
                Element p = ul.previousElementSibling();
                if (p != null && p.tagName().equals("p")) {
                    result.put(p.text(), matched);
                }
            }
        }

        return result;
    }
}