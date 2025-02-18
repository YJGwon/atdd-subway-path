package wooteco.subway.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import static wooteco.subway.dao.Fixture.TERMINATION_DOWN;
import static wooteco.subway.dao.Fixture.TERMINATION_UP;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

import wooteco.subway.domain.Line;
import wooteco.subway.domain.Station;
import wooteco.subway.domain.path.Fare;
import wooteco.subway.domain.section.Distance;
import wooteco.subway.domain.section.Section;

@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@JdbcTest
@Import({JdbcSectionDao.class, JdbcStationDao.class, JdbcLineDao.class})
public class SectionDaoTest {
    @Autowired
    private SectionDao sectionDao;
    @Autowired
    private StationDao stationDao;
    @Autowired
    private LineDao lineDao;
    private final Station station = new Station(3L, "추가역");
    private Line line;

    @BeforeEach
    void setUp() {
        stationDao.save(TERMINATION_UP);
        stationDao.save(TERMINATION_DOWN);
        stationDao.save(station);

        Section section = new Section(TERMINATION_UP, TERMINATION_DOWN, Distance.fromMeter(10));
        line = lineDao.save(new Line("신분당선", "bg-red-600", section), Fixture.FARE_1000);
    }

    @DisplayName("기존 노선에 구간을 추가할 수 있다")
    @Test
    void save_sections() {
        Section section = new Section(TERMINATION_DOWN, station, Distance.fromMeter(5));
        line.addSection(section);
        sectionDao.save(line.getSections(), line.getId());
    }

    @DisplayName("특정 구간을 삭제할 수 있다")
    @Test
    void delete() {
        Section section = new Section(TERMINATION_DOWN, station, Distance.fromMeter(5));
        line.addSection(section);
        sectionDao.save(line.getSections(), line.getId());

        Line updatedLine = lineDao.findById(line.getId());
        Section deletedSection = updatedLine.delete(station);
        assertThat(sectionDao.delete(deletedSection)).isEqualTo(1);
    }

    @DisplayName("특정 노선의 구간을 모두 삭제할 수 있다")
    @Test
    void deleteByLine() {
        Section section = new Section(TERMINATION_DOWN, station, Distance.fromMeter(5));
        line.addSection(section);
        sectionDao.save(line.getSections(), line.getId());
        assertThat(sectionDao.deleteByLine(line.getId())).isEqualTo(2);
    }

    @DisplayName("삭제할 구간이 없을 경우 예외가 발생한다")
    @Test
    void delete_no_data() {
        Section section = new Section(1L, TERMINATION_UP, TERMINATION_DOWN, Distance.fromMeter(10));
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> sectionDao.delete(section))
                .withMessageContaining("존재하지 않습니다");
    }

    @DisplayName("저장한 모든 구간 목록을 불러온다")
    @Test
    void findAll() {
        Section section = new Section(TERMINATION_DOWN, station, Distance.fromMeter(10));

        line.addSection(section);

        sectionDao.save(line.getSections(), line.getId());
        assertThat(sectionDao.findAll()).hasSize(2);
    }

    @DisplayName("특정 구간의 추가 요금을 조회한다")
    @Test
    void findExtraFareById() {
        sectionDao.save(line.getSections(), line.getId());
        Fare extraFare = sectionDao.findExtraFareById(1L);

        assertThat(extraFare.getValue()).isEqualTo(1000);
    }
}
