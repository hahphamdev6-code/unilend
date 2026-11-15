package vn.edu.unilend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.unilend.dto.common.ApiResponse;
import vn.edu.unilend.dto.equipment.FacultyDto;
import vn.edu.unilend.repository.FacultyRepository;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/faculties")
@RequiredArgsConstructor
public class FacultyController {

    private final FacultyRepository facultyRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<FacultyDto>>> getAllFaculties() {
        List<FacultyDto> faculties = facultyRepository.findAll().stream()
                .map(f -> FacultyDto.builder()
                        .id(f.getId())
                        .code(f.getCode())
                        .name(f.getName())
                        .description(f.getDescription())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok("Danh sách khoa / đơn vị", faculties));
    }
}
