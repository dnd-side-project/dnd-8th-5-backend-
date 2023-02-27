package com.dnd.modutime.participant.domain;

import static javax.persistence.GenerationType.IDENTITY;

import com.dnd.modutime.timeblock.application.ParticipantCreationEvent;
import java.util.regex.Pattern;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.PostPersist;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.AbstractAggregateRoot;

@Entity
@NoArgsConstructor
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"room_uuid", "name"})})
public class Participant extends AbstractAggregateRoot<Participant> {

    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^[0-9]{4}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[_a-z0-9-]+(.[_a-z0-9-]+)*@(?:\\w+\\.)+\\w+$");

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(name = "room_uuid", nullable = false)
    private String roomUuid;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String password;

    @Column
    private String email;

    public Participant(String roomUuid, String name, String password) {
        validateRoomUuid(roomUuid);
        validateName(name);
        validatePassword(password);

        this.roomUuid = roomUuid;
        this.name = name;
        this.password = password;
        this.email = null;
    }

    private void validateRoomUuid(String roomUuid) {
        if (roomUuid == null) {
            throw new IllegalArgumentException("roomUuid는 null일 수 없습니다");
        }
    }

    private static void validateName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("이름은 null일 수 없습니다");
        }
    }

    private void validatePassword(String password) {
        if (isRightPassword(password)) {
            throw new IllegalArgumentException("비밀번호는 4자리 숫자여야 합니다.");
        }
    }

    @PostPersist
    private void registerCreateEvent() {
        registerEvent(new ParticipantCreationEvent(roomUuid, name));
    }

    private boolean isRightPassword(String password) {
        return !PASSWORD_PATTERN.matcher(password).find();
    }

    public void registerEmail(String email) {
        validateRightEmailPattern(email);
        this.email = email;
    }

    private void validateRightEmailPattern(String email) {
        if (!EMAIL_PATTERN.matcher(email).find()) {
            throw new IllegalArgumentException("email 형식에 맞지 않습니다.");
        }
    }

    public boolean hasEmail() {
        return email != null;
    }

    public boolean matchPassword(String password) {
        return this.password.equals(password);
    }

    public String getRoomUuid() {
        return roomUuid;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
}
