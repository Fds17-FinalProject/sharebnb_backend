package com.mip.sharebnb.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.config.location="
        + "classpath:application.yml,"
        + "classpath:datasource.yml")
class AccommodationControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void before(WebApplicationContext was) {
        mockMvc = MockMvcBuilders.webAppContextSetup(was)
                .alwaysDo(print())
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .build();
    }

    @DisplayName("id로 숙박 검색")
    @Test
    void getAccommodation() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/accommodation/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.city").value("서울특별시"))
                .andExpect(jsonPath("$.gu").value("마포구"))
                .andExpect(jsonPath("$.contact").value("010-1234-5678"));
    }

    @DisplayName("모든 숙박 검색")
    @Test
    void getAllAccommodations() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/accommodations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(200)));
    }

    @DisplayName("도시명으로 숙박 검색")
    @Test
    void getAccommodationsByCity() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/accommodations/city/서울?page=0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].city").value("서울특별시"));
    }

    @DisplayName("검색어로 숙박 검색")
    @Test
    void getAccommodationsBySearchKeyword() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/accommodations/search/서울?page=0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].city").value("서울특별시"));
    }

    @DisplayName("메인 검색 기능 (검색어, 체크인, 체크아웃, 인원수)")
    @Test
    void getAccommodationsBySearch() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/accommodations/search?searchKeyword=마포&checkIn=2021-02-03&checkout=2021-02-05&page=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].city").value("서울특별시"))
                .andExpect(jsonPath("$[0].gu").value("마포구"));
    }
}