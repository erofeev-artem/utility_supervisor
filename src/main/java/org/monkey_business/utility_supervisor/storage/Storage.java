package org.monkey_business.utility_supervisor.storage;

import org.monkey_business.utility_supervisor.dto.ResultOutageDto;
import org.monkey_business.utility_supervisor.dto.RossetiOutageResponseDto;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Storage {
    private static final Map<String, ResultOutageDto<RossetiOutageResponseDto>> storage = new ConcurrentHashMap<>();

    public static void put(String key, ResultOutageDto<RossetiOutageResponseDto> value) {
        storage.put(key, value);
    }

    public static ResultOutageDto<RossetiOutageResponseDto> get(String key) {
        return storage.get(key);
    }

    public static void remove(String key) {
        storage.remove(key);
    }

    public static void clear() {
        storage.clear();
    }
}
