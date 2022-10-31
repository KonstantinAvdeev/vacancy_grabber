package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class HabrCareerParse {

    private static final String SOURCE_LINK = "https://career.habr.com";

    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer", SOURCE_LINK);

    public static void main(String[] args) throws IOException {
        for (int i = 1; i < 6; i++) {
            Connection connection = Jsoup.connect(PAGE_LINK + "?page=" + i);
            Document document = connection.get();
            Elements rows = document.select(".vacancy-card__inner");
            rows.forEach(row -> {
                Element titleElement = row.select(".vacancy-card__title").first();
                Element linkElement = titleElement.child(0);
                String vacancyName = titleElement.text();
                String link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
                Element dateElement = row.select(".vacancy-card__date").first();
                Element dateTime = dateElement.child(0);
                String date = dateTime.attr("datetime");
                String desc = "";
                try {
                    desc = retrieveDescription(link);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.printf("%s %s%n %s%n %s%n", vacancyName, link, date, desc);
            });
        }
    }

    private static String retrieveDescription(String link) throws IOException {
        Document document = Jsoup.connect(link).get();
        Elements row = document.select(".style-ugc");
        return row.text();
    }

}