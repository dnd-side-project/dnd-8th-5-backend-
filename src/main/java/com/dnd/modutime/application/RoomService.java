package com.dnd.modutime.application;

import com.dnd.modutime.domain.Room;
import com.dnd.modutime.dto.RoomRequest;
import com.dnd.modutime.dto.RoomResponse;
import com.dnd.modutime.dto.TimerRequest;
import com.dnd.modutime.repository.RoomRepository;
import com.dnd.modutime.util.TimeProvider;
import com.dnd.modutime.util.Timer;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final TimeProvider timeProvider;
    private final RoomRepository roomRepository;

    public RoomResponse create(RoomRequest roomRequest) {
        TimerRequest timerRequest = roomRequest.getTimerRequest();
        Room room = new Room(roomRequest.getStartTime(),
                roomRequest.getEndTime(),
                roomRequest.getDates(),
                roomRequest.getHeadCount(),
                findDeadLineOrNull(timerRequest),
                timeProvider);
        roomRepository.save(room);
        return new RoomResponse(room.getUuid());
    }

    private LocalDateTime findDeadLineOrNull(TimerRequest timerRequest) {
        if (hasDeadLine(timerRequest)) {
            return null;
        }
        return Timer.calculateDeadLine(timerRequest.getDay(),
                timerRequest.getHour(),
                timerRequest.getMinute(),
                timeProvider);
    }

    private boolean hasDeadLine(TimerRequest timerRequest) {
        return timerRequest == null || checkAllValueZero(timerRequest);
    }

    private boolean checkAllValueZero(TimerRequest timerRequest) {
        return timerRequest.getDay() == 0 && timerRequest.getHour() == 0 && timerRequest.getMinute() == 0;
    }
}
