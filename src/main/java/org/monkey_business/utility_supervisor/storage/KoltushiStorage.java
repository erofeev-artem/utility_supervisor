package org.monkey_business.utility_supervisor.storage;

import org.monkey_business.utility_supervisor.dto.KoltushiOutageResponseDto;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

@Component
public class KoltushiStorage {

    private final Map<LocalDate, List<KoltushiOutageResponseDto>> storage = new ConcurrentHashMap<>();

    public void put(LocalDate date, List<KoltushiOutageResponseDto> outages) {
        storage.put(date, outages);
    }

    public List<KoltushiOutageResponseDto> getForNextDays(int days) {
        LocalDate today = LocalDate.now();
        return IntStream.rangeClosed(1, days)
                .mapToObj(i -> storage.getOrDefault(today.plusDays(i), List.of()))
                .flatMap(List::stream)
                .toList();
    }

    public void clear() {
        storage.clear();
    }
}