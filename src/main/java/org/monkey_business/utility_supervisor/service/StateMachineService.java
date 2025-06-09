package org.monkey_business.utility_supervisor.service;

import org.monkey_business.utility_supervisor.enums.UserState;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class StateMachineService {
    private final Map<String, String> userStates = new ConcurrentHashMap<>();

    public void setUserState(String chatId, String state) {
        userStates.put(chatId, state);
    }

    public String getCurrentState(String chatId) {
        return userStates.getOrDefault(chatId, UserState.DEFAULT.getState());
    }

    public void resetState(String chatId) {
        userStates.remove(chatId);
    }
}
