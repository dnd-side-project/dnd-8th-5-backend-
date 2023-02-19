package com.dnd.modutime.application;

import com.dnd.modutime.domain.timeblock.AvailableDateTime;
import com.dnd.modutime.domain.timeblock.AvailableTime;
import com.dnd.modutime.domain.timeblock.TimeBlock;
import com.dnd.modutime.dto.request.TimeReplaceRequest;
import com.dnd.modutime.repository.AvailableDateTimeRepository;
import com.dnd.modutime.repository.TimeBlockRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TimeTableService {

    private final TimeReplaceValidator timeReplaceValidator;
    private final TimeBlockRepository timeBlockRepository;
    private final AvailableDateTimeRepository availableDateTimeRepository;

    public void replace(String roomUuid, TimeReplaceRequest timeReplaceRequest) {
        TimeBlock timeBlock = getTimeBlockByRoomUuidAndParticipantName(roomUuid, timeReplaceRequest.getName());

        List<AvailableDateTime> availableDateTimes = timeReplaceRequest.getAvailableDateTimes().stream()
                .map(it -> new AvailableDateTime(timeBlock, it.getDate(), it.getTimes().stream()
                        .map(AvailableTime::new)
                        .collect(Collectors.toList())))
                .collect(Collectors.toList());

        timeReplaceValidator.validate(roomUuid, availableDateTimes);
        availableDateTimeRepository.deleteAllByTimeBlockId(timeBlock.getId());
        availableDateTimeRepository.saveAll(availableDateTimes);
        timeBlock.replace(availableDateTimes);
    }

    private TimeBlock getTimeBlockByRoomUuidAndParticipantName(String roomUuid, String participantName) {
        return timeBlockRepository.findByRoomUuidAndParticipantName(roomUuid, participantName)
                .orElseThrow(() -> new IllegalArgumentException("해당하는 TimeBlock을 찾을 수 없습니다."));
    }
}
