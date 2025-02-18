package wooteco.subway.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static wooteco.subway.dao.Fixture.FARE_1000;
import static wooteco.subway.dao.Fixture.LINE_신분당선;
import static wooteco.subway.dao.Fixture.TERMINATION_DOWN;
import static wooteco.subway.dao.Fixture.TERMINATION_UP;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;

import wooteco.subway.domain.Line;
import wooteco.subway.domain.path.Fare;

@JdbcTest
@Import({JdbcLineDao.class, JdbcStationDao.class})
class LineDaoTest {
    @Autowired
    private LineDao linDao;
    @Autowired
    private StationDao stationDao;

    @BeforeEach
    void setUp() {
        stationDao.save(TERMINATION_UP);
        stationDao.save(TERMINATION_DOWN);
    }

    @Test
    @DisplayName("노선을 저장한다.")
    public void save() {
        // given & when
        final Line saved = linDao.save(LINE_신분당선, FARE_1000);
        // then
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    @DisplayName("중복된 이름을 저장하는 경우 예외를 던진다.")
    public void save_throwsExceptionWithDuplicatedName() {
        // given & when
        linDao.save(LINE_신분당선, FARE_1000);
        // then
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> linDao.save(LINE_신분당선, FARE_1000));
    }

    @Test
    @DisplayName("전체 노선을 조회한다.")
    public void findAll() {
        // given & when
        List<Line> lines = linDao.findAll();
        // then
        assertThat(lines).hasSize(0);
    }

    @Test
    @DisplayName("노선을 하나 추가한 뒤, 전체 노선을 조회한다")
    public void findAll_afterSaveOneLine() {
        // given
        linDao.save(LINE_신분당선, FARE_1000);
        // when
        List<Line> lines = linDao.findAll();
        // then
        assertThat(lines).hasSize(1);
    }

    @Test
    @DisplayName("ID 값으로 노선을 조회한다")
    public void findById() {
        // given
        final Line saved = linDao.save(LINE_신분당선, FARE_1000);
        // when
        final Line found = linDao.findById(saved.getId());
        // then
        Assertions.assertAll(
                () -> assertThat(found.getId()).isEqualTo(saved.getId()),
                () -> assertThat(found.getName()).isEqualTo(saved.getName()),
                () -> assertThat(found.getColor()).isEqualTo(saved.getColor())
        );
    }

    @Test
    @DisplayName("존재하지 않는 ID 값으로 노선을 조회하면 예외를 던진다")
    public void findById_invalidID() {
        // given & when
        linDao.save(LINE_신분당선, FARE_1000);
        // then
        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> linDao.findById(2L));
    }

    @Test
    @DisplayName("ID 값으로 노선의 추가 요금을 조회한다")
    public void findFareById() {
        // given
        final Line saved = linDao.save(LINE_신분당선, FARE_1000);
        // when
        final Fare found = linDao.findExtraFareById(saved.getId());
        // then
        assertThat(found.getValue()).isEqualTo(1000);
    }

    @Test
    @DisplayName("노선 정보를 수정한다.")
    public void update() {
        // given
        final Line saved = linDao.save(LINE_신분당선, FARE_1000);
        //when
        Line updatingLine = new Line(saved.getId(), "구분당선", "bg-red-500");
        Fare updatingFare = new Fare(700);
        // then
        assertThat(linDao.update(updatingLine, updatingFare)).isEqualTo(1);
    }

    @Test
    @DisplayName("존재하지 않는 ID값을 수정하는 경우 예외를 던진다.")
    public void update_throwsExceptionWithInvalidId() {
        // given
        linDao.save(LINE_신분당선, FARE_1000);
        // when
        Line updatingLine = new Line(100L, "사랑이넘치는", "우테코");
        Fare updatingFare = new Fare(100);
        // then
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> linDao.update(updatingLine, updatingFare));
    }

    @Test
    @DisplayName("ID값으로 노선을 삭제한다.")
    public void delete() {
        // given & when
        final Line saved = linDao.save(LINE_신분당선, FARE_1000);
        // then
        assertThat(linDao.delete(saved.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("존재하지않는 ID값을 삭제하는 경우 예외를 던진다.")
    public void delete_throwsExceptionWithInvalidId() {
        // given
        linDao.save(LINE_신분당선, FARE_1000);
        // when
        Long deleteId = 100L;
        // then
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> linDao.delete(deleteId));
    }
}