package wooteco.subway.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.sql.DataSource;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import wooteco.subway.domain.section.Distance;
import wooteco.subway.domain.path.Fare;
import wooteco.subway.domain.section.Section;
import wooteco.subway.domain.Station;

@Repository
public class JdbcSectionDao implements SectionDao {
    private final SimpleJdbcInsert jdbcInsert;
    private final JdbcTemplate jdbcTemplate;

    public JdbcSectionDao(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        this.jdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("section")
                .usingGeneratedKeyColumns("id");
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(Section section, Long lineId) {
        save(section, lineId, 0);
    }

    @Override
    public void save(List<Section> sections, Long lineId) {
        for (int i = 0; i < sections.size(); i++) {
            Section section = sections.get(i);
            update(section, lineId, i);
        }
    }

    private void update(Section section, Long lineId, int index) {
        if (Objects.isNull(section.getId())) {
            save(section, lineId, index);
            return;
        }
        String sql = "UPDATE section "
                + "SET up_station_id = ?, down_station_id = ?, distance = ?, index_num = ? "
                + "WHERE id = ?";
        jdbcTemplate.update(sql,
                section.getUpStationId(), section.getDownStationId(), section.getDistance(), index,
                section.getId());
    }

    private void save(Section section, Long lineId, int index) {
        Map<String, Object> param = new HashMap<>();
        param.put("line_id", lineId);
        param.put("up_station_id", section.getUpStationId());
        param.put("down_station_id", section.getDownStationId());
        param.put("distance", section.getDistance());
        param.put("index_num", index);
        jdbcInsert.executeAndReturnKey(param);
    }

    @Override
    public int delete(Section section) {
        String sql = "DELETE FROM section WHERE id = ?";
        return delete(sql, section.getId());
    }

    @Override
    public int deleteByLine(Long lineId) {
        String sql = "DELETE FROM section WHERE line_id = ?";
        return delete(sql, lineId);
    }

    @Override
    public List<Section> findAll() {
        String sql = "SELECT "
                + "sec.id, sec.distance, "
                + "sec.up_station_id, us.name up_station_name,"
                + "sec.down_station_id, ds.name down_station_name "
                + "FROM section AS sec "
                + "JOIN station AS us ON sec.up_station_id = us.id "
                + "JOIN station AS ds ON sec.down_station_id = ds.id ";
        return jdbcTemplate.query(sql, (resultSet, rowNum) -> mapToSection(resultSet));
    }

    @Override
    public Fare findExtraFareById(Long id) {
        String sql = "SELECT "
                + "l.extra_fare "
                + "FROM section AS sec "
                + "JOIN line AS l ON sec.line_id = l.id "
                + "WHERE sec.id = ?";
        try {
            Integer extraFare = jdbcTemplate.queryForObject(sql, Integer.class, id);
            return new Fare(extraFare);
        } catch (EmptyResultDataAccessException e) {
            throw new IllegalStateException("조회하고자 하는 구간이 존재하지 않습니다.");
        }
    }

    private Section mapToSection(ResultSet resultSet) throws SQLException {
        Long upStationId = resultSet.getLong("up_station_id");
        String upStationName = resultSet.getString("up_station_name");

        Long downStationId = resultSet.getLong("down_station_id");
        String downStationName = resultSet.getString("down_station_name");

        return new Section(
                resultSet.getLong("id"),
                new Station(upStationId, upStationName),
                new Station(downStationId, downStationName),
                Distance.fromKilometer(resultSet.getDouble("distance"))
        );
    }

    private int delete(String sql, Long id) {
        int deletedCount = jdbcTemplate.update(sql, id);
        validateRemoved(deletedCount);
        return deletedCount;
    }

    private void validateRemoved(int count) {
        if (count == 0) {
            throw new IllegalStateException("삭제할 구간이 존재하지 않습니다.");
        }
    }
}
