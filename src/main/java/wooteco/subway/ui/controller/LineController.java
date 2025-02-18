package wooteco.subway.ui.controller;

import java.net.URI;
import java.util.List;

import javax.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import wooteco.subway.dto.request.LineRequest;
import wooteco.subway.dto.response.LineResponse;
import wooteco.subway.ui.service.LineService;
import wooteco.subway.ui.service.SectionService;

@RestController
public class LineController {
    private final LineService lineService;
    private final SectionService sectionService;

    public LineController(LineService lineService, SectionService sectionService) {
        this.lineService = lineService;
        this.sectionService = sectionService;
    }

    @PostMapping("/lines")
    public ResponseEntity<LineResponse> createLine(@Valid @RequestBody LineRequest lineRequest) {
        LineResponse lineResponse = lineService.create(lineRequest);
        Long id = lineResponse.getId();
        return ResponseEntity.created(URI.create("/lines/" + id)).body(lineResponse);
    }

    @GetMapping(value = "/lines", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<LineResponse>> showLines() {
        return ResponseEntity.ok().body(lineService.findAll());
    }

    @GetMapping("/lines/{id}")
    public ResponseEntity<LineResponse> showLine(@PathVariable Long id) {
        return ResponseEntity.ok(lineService.findById(id));
    }

    @PutMapping("/lines/{id}")
    public ResponseEntity<Void> modifyLine(@PathVariable Long id, @RequestBody LineRequest lineRequest) {
        lineService.modify(id, lineRequest);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/lines/{id}")
    public ResponseEntity<Void> deleteLine(@PathVariable Long id) {
        sectionService.deleteByLine(id);
        lineService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
