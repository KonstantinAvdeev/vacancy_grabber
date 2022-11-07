package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.DateTimeParser;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HabrCareerParse implements Parse {

    private static final String SOURCE_LINK = "https://career.habr.com";

    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer", SOURCE_LINK);

    private static final int PAGES = 5;

    private final DateTimeParser dateTimeParser;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    public static void main(String[] args) {
        HabrCareerParse habrCareerParse = new HabrCareerParse(new HabrCareerDateTimeParser());
        System.out.println(habrCareerParse.list(PAGE_LINK + "?page="));
    }

    private static String retrieveDescription(String link) {
        Document document;
        try {
            document = Jsoup.connect(link).get();
        } catch (IOException e) {
            throw new IllegalArgumentException("Something wrong with open " + link);
        }
        Elements row = document.select(".style-ugc");
        return row.text();
    }

    @Override
    public List<Post> list(String link) {
        List<Post> postList = new ArrayList<>();
        for (int i = 1; i <= PAGES; i++) {
            Connection connection = Jsoup.connect(link + i);
            Document document;
            try {
                document = connection.get();
            } catch (IOException e) {
                throw new IllegalArgumentException("Something wrong with reading information and make list of Posts!");
            }
            Elements rows = document.select(".vacancy-card__inner");
            for (Element row : rows) {
                postList.add(parsePost(row));
            }
        }
        return postList;
    }

    private Post parsePost(Element element) {
        Element titleElement = element.select(".vacancy-card__title").first();
        Element linkElement = titleElement.child(0);
        String vacancyName = titleElement.text();
        String linkString = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
        Element dateElement = element.select(".vacancy-card__date").first();
        Element dateTime = dateElement.child(0);
        String date = dateTime.attr("datetime");
        String desc = retrieveDescription(linkString);
        return new Post(vacancyName, linkString, desc, this.dateTimeParser.parse(date));
    }

}