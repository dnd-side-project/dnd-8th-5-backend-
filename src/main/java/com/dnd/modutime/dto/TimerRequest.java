package com.dnd.modutime.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TimerRequest {
    private int day;
    private int hour;
    private int minute;
}
