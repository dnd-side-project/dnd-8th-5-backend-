package com.dnd.modutime.timeblock.application;

import com.dnd.modutime.dto.request.TimeReplaceRequest;
import com.dnd.modutime.dto.response.AvailableDateTimeResponse;
import com.dnd.modutime.dto.response.TimeBlockResponse;
import com.dnd.modutime.exception.NotFoundException;
import com.dnd.modutime.timeblock.domain.AvailableDateTime;
import com.dnd.modutime.timeblock.domain.AvailableTime;
import com.dnd.modutime.timeblock.domain.TimeBlock;
import com.dnd.modutime.timeblock.repository.AvailableDateTimeRepository;
import com.dnd.modutime.timeblock.repository.TimeBlockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class TimeBlockService {

    private final TimeReplaceValidator timeReplaceValidator;
    private final TimeBlockRepository timeBlockRepository;
    private final AvailableDateTimeRepository availableDateTimeRepository;

    public void replace(String roomUuid, TimeReplaceRequest timeReplaceRequest) {
        TimeBlock timeBlock = getTimeBlockByRoomUuidAndParticipantName(roomUuid, timeReplaceRequest.getName());

        List<AvailableDateTime> availableDateTimes = timeReplaceRequest.getAvailableDateTimes().stream()
                .map(it -> new AvailableDateTime(timeBlock, it.getDate(), convertToAvailableTimesOrNull(it.getTimes())))
                .collect(Collectors.toList());

        timeReplaceValidator.validate(roomUuid, availableDateTimes);
        availableDateTimeRepository.deleteAllByTimeBlockId(timeBlock.getId());
        availableDateTimeRepository.saveAll(availableDateTimes);
        timeBlock.replace(availableDateTimes);
        timeBlockRepository.save(timeBlock);
    }

    private List<AvailableTime> convertToAvailableTimesOrNull(List<LocalTime> times) {
        if (times == null) {
            return null;
        }

        return times.stream()
                .map(AvailableTime::new)
                .collect(Collectors.toList());
    }

    private TimeBlock getTimeBlockByRoomUuidAndParticipantName(String roomUuid, String participantName) {
        return timeBlockRepository.findByRoomUuidAndParticipantName(roomUuid, participantName)
                .orElseThrow(() -> new NotFoundException("해당하는 TimeBlock을 찾을 수 없습니다."));
    }

    public TimeBlockResponse getTimeBlock(String roomUuid, String name) {
        TimeBlock timeBlock = getTimeBlockByRoomUuidAndParticipantName(roomUuid, name);
        List<AvailableDateTime> availableDateTimes = availableDateTimeRepository.findByTimeBlockId(timeBlock.getId());

//        return new TimeBlockResponse(timeBlock.getParticipantName(), availableDateTimes.stream()
//                .map(it -> new AvailableDateTimeResponse(LocalDateTime.of(it.getDate(), it.getTimesOrNull().stream()
//                        .map(AvailableTime::getTime)
//                        .collect(Collectors.toList())))
//                .collect(Collectors.toList()));

//        return new TimeBlockResponse(timeBlock.getParticipantName(), availableDateTimes.stream()
//                .flatMap(it -> it.getTimesOrNull().stream())
//                .map(it2 -> AvailableDateTimeResponse(LocalDateTime.of(, it2.getTime())))
//                .collect(Collectors.toList()));

        List<AvailableDateTimeResponse> availableDateTimeResponses = new ArrayList<>();

        for (AvailableDateTime availableDateTime : availableDateTimes) {
            LocalDate localDate = availableDateTime.getDate();
            if (availableDateTime.getTimesOrNull().isEmpty()) {
                availableDateTimeResponses.add(new AvailableDateTimeResponse(LocalDateTime.of(localDate, LocalTime.of(0, 0))));
            }
            else {
                for (AvailableTime availableTime : availableDateTime.getTimesOrNull()) {
                    LocalTime localTime = availableTime.getTime();
                    availableDateTimeResponses.add(new AvailableDateTimeResponse(LocalDateTime.of(localDate, localTime)));
                }
            }
        }
        return new TimeBlockResponse(timeBlock.getParticipantName(), availableDateTimeResponses);
    }

    private List<LocalTime> getTimesOrNull(List<AvailableTime> availableTimes) {
        if (availableTimes == null) {
            return null;
        }
        return availableTimes.stream()
                .map(AvailableTime::getTime)
                .collect(Collectors.toList());
    }
}
