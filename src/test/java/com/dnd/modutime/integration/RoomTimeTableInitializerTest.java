package com.dnd.modutime.integration;

import static com.dnd.modutime.domain.fixture.RoomFixture.getRoom;
import static com.dnd.modutime.fixture.TimeFixture._11_00;
import static com.dnd.modutime.fixture.TimeFixture._13_00;
import static com.dnd.modutime.fixture.TimeFixture._2023_02_09;
import static com.dnd.modutime.fixture.TimeFixture._2023_02_10;
import static org.assertj.core.api.Assertions.assertThat;

import com.dnd.modutime.application.RoomTimeTableInitializer;
import com.dnd.modutime.domain.room.Room;
import com.dnd.modutime.domain.timetable.TimeInfo;
import com.dnd.modutime.domain.timetable.TimeTable;
import com.dnd.modutime.repository.RoomRepository;
import com.dnd.modutime.repository.TimeTableRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
public class RoomTimeTableInitializerTest {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private RoomTimeTableInitializer roomTimeTableInitializer;

    @Autowired
    private TimeTableRepository timeTableRepository;

    @Test
    void 방의_날짜와_시작_끝시간에_해당하는_인원수를_0으로_초기화한다() {
        // given
        Room room = getRoom(_11_00, _13_00, List.of(_2023_02_09, _2023_02_10), 2);
        roomRepository.save(room);
        TimeTable timeTable = timeTableRepository.save(new TimeTable(room.getUuid()));
        roomTimeTableInitializer.initialize(room.getUuid(), timeTable);

        // when
        List<Integer> counts = timeTable.getDateInfos().stream()
                .flatMap(it -> it.getTimeInfos().stream())
                .map(TimeInfo::getCount)
                .collect(Collectors.toList());

        // then
        assertThat(counts).hasSize(8)
                .allMatch(it -> it.equals(0));
    }

    @Test
    void 방의_날짜에_해당하는_인원수를_0으로_초기화한다() {
        // given
        Room room = getRoom(null, null, List.of(_2023_02_09, _2023_02_10), 2);
        roomRepository.save(room);
        TimeTable timeTable = timeTableRepository.save(new TimeTable(room.getUuid()));
        roomTimeTableInitializer.initialize(room.getUuid(), timeTable);

        // when
        List<Integer> counts = timeTable.getDateInfos().stream()
                .flatMap(it -> it.getTimeInfos().stream())
                .map(TimeInfo::getCount)
                .collect(Collectors.toList());

        // then
        assertThat(counts).hasSize(2)
                .allMatch(it -> it.equals(0));
    }
}
