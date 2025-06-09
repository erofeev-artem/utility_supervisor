package org.monkey_business.utility_supervisor.parser;

import org.jsoup.nodes.Document;

import java.util.List;

public interface TableParser {
    List<?> parse(Document document);
}
