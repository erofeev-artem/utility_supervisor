package org.monkey_business.utility_supervisor.storage;

import org.monkey_business.utility_supervisor.dto.RossetiResultOutageDto;
import org.monkey_business.utility_supervisor.dto.RossetiOutageResponseDto;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RossetiStorage {
    private static final Map<String, RossetiResultOutageDto<RossetiOutageResponseDto>> storage = new ConcurrentHashMap<>();

    public static void put(String key, RossetiResultOutageDto<RossetiOutageResponseDto> value) {
        storage.put(key, value);
    }

    public static RossetiResultOutageDto<RossetiOutageResponseDto> get(String key) {
        return storage.get(key);
    }

    public static void remove(String key) {
        storage.remove(key);
    }

    public static void clear() {
        storage.clear();
    }
}
