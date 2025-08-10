package ru.job4j.grabber.service;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import ru.job4j.grabber.model.Post;
import ru.job4j.grabber.utils.DateTimeParser;

import java.io.IOException;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public class HabrCareerParse implements Parse {
    private static final Logger LOG = Logger.getLogger(HabrCareerParse.class);
    private static final String SOURCE_LINK = "https://career.habr.com";
    private static final String PREFIX = "/vacancies?page=";
    private static final String SUFFIX = "&q=Java%20developer&type=all";
    private static final int COUNT = 5;
    private final DateTimeParser dateTimeParser;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    @Override
    public List<Post> fetch() {
        var result = new ArrayList<Post>();
        try {
            int pageNumber = 1;
            while (pageNumber <= COUNT) {
                String fullLink = "%s%s%d%s".formatted(SOURCE_LINK, PREFIX, pageNumber, SUFFIX);
                var connection = Jsoup.connect(fullLink);
                var document = connection.get();
                var rows = document.select(".vacancy-card__inner");
                rows.forEach(row -> {
                    var titleElement = row.select(".vacancy-card__title").first();
                    var linkElement = titleElement.child(0);
                    String vacancyName = titleElement.text();
                    String link = String.format("%s%s", SOURCE_LINK,
                            linkElement.attr("href"));
                    var description = retrieveDescription(link);
                    var dateTimeElement = row.select(".vacancy-card__date").first();
                    String dateTime = dateTimeElement.child(0).attr("datetime");
                    var post = new Post();
                    post.setTitle(vacancyName);
                    post.setLink(link);
                    post.setDescription(description);
                    post.setTime(dateTimeParser.parse(dateTime).toEpochSecond(ZoneOffset.UTC));
                    result.add(post);
                });
                pageNumber++;
            }
        } catch (IOException e) {
            LOG.error("When load page", e);
        }
        return result;
    }

    private String retrieveDescription(String link) {
        StringBuilder rsl = new StringBuilder();
        try {
            var connection = Jsoup.connect(link);
            var document = connection.get();
            var rows = document.select(".vacancy-description__text");
            rows.forEach(row -> {
                for (int i = 0; i < rows.size(); i++) {
                    var element = row.child(i);
                    rsl.append(element.text()).append(System.lineSeparator());
                }
            });
        } catch (IOException e) {
            LOG.error("When load page", e);
        }
        return rsl.toString();
    }
}
