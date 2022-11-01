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

    private final DateTimeParser dateTimeParser;
    private int id = 1;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    public static void main(String[] args) throws IOException {
        HabrCareerParse habrCareerParse = new HabrCareerParse(new HabrCareerDateTimeParser());
        for (int i = 1; i < 6; i++) {
            System.out.println(habrCareerParse.list(PAGE_LINK + "?page=" + i));
        }
    }

    private static String retrieveDescription(String link) throws IOException {
        Document document = Jsoup.connect(link).get();
        Elements row = document.select(".style-ugc");
        return row.text();
    }

    @Override
    public List<Post> list(String link) throws IOException {
        List<Post> postList = new ArrayList<>();
        Connection connection = Jsoup.connect(link);
        Document document = connection.get();
        Elements rows = document.select(".vacancy-card__inner");
        for (Element row : rows) {
            Element titleElement = row.select(".vacancy-card__title").first();
            Element linkElement = titleElement.child(0);
            String vacancyName = titleElement.text();
            String linkString = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
            Element dateElement = row.select(".vacancy-card__date").first();
            Element dateTime = dateElement.child(0);
            String date = dateTime.attr("datetime");
            String desc = "";
            try {
                desc = retrieveDescription(linkString);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Post post = new Post(id, vacancyName, linkString, desc, this.dateTimeParser.parse(date));
            postList.add(post);
            id++;
        }
        return postList;
    }

}