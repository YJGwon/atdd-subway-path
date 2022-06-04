package wooteco.subway.ui.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wooteco.subway.dao.LineDao;
import wooteco.subway.dao.SectionDao;
import wooteco.subway.dao.StationDao;
import wooteco.subway.domain.section.Distance;
import wooteco.subway.domain.Line;
import wooteco.subway.domain.section.Section;
import wooteco.subway.domain.Station;
import wooteco.subway.dto.request.SectionRequest;

@Service
public class SectionService {
    private final SectionDao sectionDao;
    private final LineDao lineDao;
    private final StationDao stationDao;

    public SectionService(SectionDao sectionDao, LineDao lineDao, StationDao stationDao) {
        this.sectionDao = sectionDao;
        this.lineDao = lineDao;
        this.stationDao = stationDao;
    }

    public void add(SectionRequest sectionRequest, Long lineId) {
        Station upStation = stationDao.findById(sectionRequest.getUpStationId());
        Station downStation = stationDao.findById(sectionRequest.getDownStationId());
        Distance distance = Distance.fromMeter(sectionRequest.getDistance());
        Section section = new Section(upStation, downStation, distance);

        Line line = lineDao.findById(lineId);
        line.addSection(section);
        sectionDao.save(line.getSections(), line.getId());
    }

    @Transactional
    public void delete(Long lineId, Long stationId) {
        Line line = lineDao.findById(lineId);
        Station station = stationDao.findById(stationId);
        sectionDao.delete(line.delete(station));
        sectionDao.save(line.getSections(), line.getId());
    }

    public void deleteByLine(Long id) {
        sectionDao.deleteByLine(id);
    }
}
