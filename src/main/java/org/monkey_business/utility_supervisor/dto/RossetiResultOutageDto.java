package org.monkey_business.utility_supervisor.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@Getter
@Setter
public class RossetiResultOutageDto<T> {
    private List<T> data;
    private int statusCode;
}
