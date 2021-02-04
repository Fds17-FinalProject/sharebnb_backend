package com.mip.sharebnb.service;

import com.mip.sharebnb.dto.AccommodationDto;
import com.mip.sharebnb.model.Accommodation;
import com.mip.sharebnb.model.AccommodationPicture;
import com.mip.sharebnb.repository.AccommodationRepository;
import com.mip.sharebnb.repository.dynamic.DynamicAccommodationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccommodationServiceTest {

    @InjectMocks
    private AccommodationService accommodationService;

    @Mock
    private AccommodationRepository accommodationRepository;

    @Mock
    private DynamicAccommodationRepository dynamicAccommodationRepository;

    @DisplayName("도시별 검색")
    @Test
    void findByCityContaining() {
        when(accommodationRepository.findByCityContaining("서울", PageRequest.of(1, 10))).thenReturn(mockAccommodationPage());

        Page<Accommodation> accommodations = accommodationService.findByCityContaining("서울", PageRequest.of(1, 10));

        assertThat(accommodations.getSize()).isEqualTo(10);
        assertThat(accommodations.toList().get(0).getCity()).isEqualTo("서울특별시");
    }

    @DisplayName("건물 유형별 검색")
    @Test
    void findByBuildingTypeContaining() {
        when(accommodationRepository.findByBuildingTypeContaining("원룸", PageRequest.of(1, 10))).thenReturn(mockAccommodationPage());

        Page<Accommodation> accommodations = accommodationService.findByBuildingTypeContaining("원룸", PageRequest.of(1, 10));

        assertThat(accommodations.getSize()).isEqualTo(10);
        assertThat(accommodations.toList().get(0).getBuildingType()).isEqualTo("원룸");
        assertThat(accommodations.toList().get(0).getAccommodationPictures().size()).isEqualTo(5);
        assertThat(accommodations.toList().get(0).getAccommodationPictures().get(0).getUrl()).isEqualTo("https://sharebnb.co.kr/pictures/1.jpg");

    }

    @DisplayName("메인 검색 (지역, 인원)")
    @Test
    void searchAccommodationsByQueryDsl1() {
        when(dynamicAccommodationRepository.findAccommodationsBySearch("원룸", null, null, 1, PageRequest.of(1, 10))).thenReturn(mockAccommodationList());

        List<Accommodation> accommodations = accommodationService.searchAccommodationsByQueryDsl("원룸", null, null, 1, PageRequest.of(1, 10));

        assertThat(accommodations.size()).isEqualTo(10);
        assertThat(accommodations.get(0).getBuildingType()).isEqualTo("원룸");
        assertThat(accommodations.get(0).getAccommodationPictures().size()).isEqualTo(5);
        assertThat(accommodations.get(0).getAccommodationPictures().get(0).getUrl()).isEqualTo("https://sharebnb.co.kr/pictures/1.jpg");
    }

    @DisplayName("메인 검색 (체크인, 체크아웃)")
    @Test
    void searchAccommodationsByQueryDsl2() {
        when(dynamicAccommodationRepository.findAccommodationsBySearch(null, null, null, 0, PageRequest.of(1, 10))).thenReturn(mockAccommodationList());

        List<Accommodation> accommodations = accommodationService.searchAccommodationsByQueryDsl(null, null, null, 0, PageRequest.of(1, 10));

        assertThat(accommodations.size()).isEqualTo(10);
        assertThat(accommodations.get(0).getAccommodationPictures().size()).isEqualTo(5);
        assertThat(accommodations.get(0).getAccommodationPictures().get(0).getUrl()).isEqualTo("https://sharebnb.co.kr/pictures/1.jpg");
    }

    @DisplayName("메인 검색 (체크인이 체크아웃 보다 나중일 경우)")
    @Test
    void searchAccommodationsByQueryDsl3() {
        when(dynamicAccommodationRepository.findAccommodationsBySearch(null, LocalDate.of(2022, 5, 1), LocalDate.of(2022, 4, 25), 0, PageRequest.of(1, 10))).thenReturn(mockAccommodationList());

        List<Accommodation> accommodations = accommodationService.searchAccommodationsByQueryDsl(null, LocalDate.of(2022, 5, 1), LocalDate.of(2022, 4, 25), 0, PageRequest.of(1, 10));

        assertThat(accommodations.size()).isEqualTo(10);
        assertThat(accommodations.get(0).getAccommodationPictures().size()).isEqualTo(5);
        assertThat(accommodations.get(0).getAccommodationPictures().get(0).getUrl()).isEqualTo("https://sharebnb.co.kr/pictures/1.jpg");
    }

    @DisplayName("메인 검색 (체크인만)")
    @Test
    void searchAccommodationsByQueryDsl4() {
        when(dynamicAccommodationRepository.findAccommodationsBySearch(null, LocalDate.of(2022, 5, 1), null, 0, PageRequest.of(1, 10))).thenReturn(mockAccommodationList());

        List<Accommodation> accommodations = accommodationService.searchAccommodationsByQueryDsl(null, LocalDate.of(2022, 5, 1), null, 0, PageRequest.of(1, 10));

        assertThat(accommodations.size()).isEqualTo(10);
        assertThat(accommodations.get(0).getAccommodationPictures().size()).isEqualTo(5);
        assertThat(accommodations.get(0).getAccommodationPictures().get(0).getUrl()).isEqualTo("https://sharebnb.co.kr/pictures/1.jpg");
    }

    private AccommodationDto mockAccommodationDto() {
        Accommodation accommodation = mockAccommodation(1L);

        return new AccommodationDto(accommodation, null, null, accommodation.getAccommodationPictures());
    }

    private Accommodation mockAccommodation(Long id) {
        Accommodation accommodation = new Accommodation(id, "서울특별시", "마포구", "원룸", 1, 1, 1, 40000, 2, "010-1234-5678", "36.141", "126.531", "마포역 1번 출구 앞", "버스 7016", "깨끗해요", "착해요", "4.56", 125, "전체", "원룸", "이재복", 543, null, null, null, null, null);
        List<AccommodationPicture> accommodationPictures = new ArrayList<>();

        accommodationPictures.add(new AccommodationPicture("https://sharebnb.co.kr/pictures/1.jpg"));
        accommodationPictures.add(new AccommodationPicture("https://sharebnb.co.kr/pictures/2.jpg"));
        accommodationPictures.add(new AccommodationPicture("https://sharebnb.co.kr/pictures/3.jpg"));
        accommodationPictures.add(new AccommodationPicture("https://sharebnb.co.kr/pictures/4.jpg"));
        accommodationPictures.add(new AccommodationPicture("https://sharebnb.co.kr/pictures/5.jpg"));
        accommodation.setAccommodationPictures(accommodationPictures);

        return accommodation;
    }

    private List<Accommodation> mockAccommodationList() {
        List<Accommodation> accommodations = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            accommodations.add(mockAccommodation((long) i));
        }

        return accommodations;
    }

    private Page<Accommodation> mockAccommodationPage() {

        return new PageImpl<>(mockAccommodationList());
    }
}