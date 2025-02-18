package wooteco.subway.ui.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import wooteco.subway.dao.LineDao;
import wooteco.subway.dao.SectionDao;
import wooteco.subway.dao.StationDao;
import wooteco.subway.domain.section.Distance;
import wooteco.subway.domain.path.Fare;
import wooteco.subway.domain.Line;
import wooteco.subway.domain.section.Section;
import wooteco.subway.domain.Station;
import wooteco.subway.dto.request.LineRequest;
import wooteco.subway.dto.response.LineResponse;

@Service
public class LineService {
    private final LineDao lineDao;
    private final StationDao stationDao;
    private final SectionDao sectionDao;

    public LineService(LineDao lineDao, StationDao stationDao, SectionDao sectionDao) {
        this.lineDao = lineDao;
        this.stationDao = stationDao;
        this.sectionDao = sectionDao;
    }

    @Transactional
    public LineResponse create(LineRequest lineRequest) {
        String name = lineRequest.getName();
        String color = lineRequest.getColor();

        Station upStation = stationDao.findById(lineRequest.getUpStationId());
        Station downStation = stationDao.findById(lineRequest.getDownStationId());
        Distance distance = Distance.fromMeter(lineRequest.getDistance());
        Section section = new Section(upStation, downStation, distance);

        Line line = new Line(name, color, section);
        Fare extraFare = new Fare(lineRequest.getExtraFare());
        Line createdLine = lineDao.save(line, extraFare);
        sectionDao.save(section, createdLine.getId());

        return LineResponse.from(createdLine, extraFare);
    }

    @Transactional(readOnly = true)
    public List<LineResponse> findAll() {
        final List<Line> lines = lineDao.findAll();
        return lines.stream()
                .map(line -> LineResponse.from(line, lineDao.findExtraFareById(line.getId())))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public LineResponse findById(Long id) {
        final Line line = lineDao.findById(id);
        final Fare extraFare = lineDao.findExtraFareById(id);
        return LineResponse.from(line, extraFare);
    }

    public void modify(Long id, LineRequest lineRequest) {
        final Line line = new Line(id, lineRequest.getName(), lineRequest.getColor());
        final Fare extraFare = new Fare(lineRequest.getExtraFare());
        lineDao.update(line, extraFare);
    }

    public void delete(Long id) {
        lineDao.delete(id);
    }
}
