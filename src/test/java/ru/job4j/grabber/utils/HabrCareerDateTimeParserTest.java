package ru.job4j.grabber.utils;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class HabrCareerDateTimeParserTest {

    @Test
    public void whenParseDateTime1() {
        HabrCareerDateTimeParser parser = new HabrCareerDateTimeParser();
        LocalDateTime rsl = parser.parse("2025-08-01T11:34:33+03:00");
        LocalDateTime expected = LocalDateTime.of(2025, 8, 1, 11, 34, 33);
        assertThat(rsl).isEqualTo(expected);
    }

    @Test
    public void whenParseDateTime2() {
        HabrCareerDateTimeParser parser = new HabrCareerDateTimeParser();
        String temp = "2025-08-01T11:34:33";
        assertThatThrownBy(() -> parser.parse(temp))
                .isInstanceOf(DateTimeParseException.class)
                .hasMessage(String.format("Text '%s' could not be parsed at index 19", temp));
    }

}