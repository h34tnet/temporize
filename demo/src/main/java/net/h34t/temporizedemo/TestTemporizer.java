package net.h34t.temporizedemo;

import includes.Header;
import index.Index;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Tests the generated classes
 */
public class TestTemporizer {

    public static void main(String... args) throws IOException {

        List<Author> authors = getTestAuthors();

        String output = new Index()
                .setHeader(new Header()
                        .setIsLoggedIn(true)
                        .setUsername("<& Ümläut encódèd &>"))
                .setTitle("hallo \"mängus\"")
                .setIntro("a short intro for short people")
                .setAuthors(authors.stream().map(
                        a -> new Index.Authors()
                                .setName(a.getName())
                                .setLink(a.getLink())
                                .setAge(a.getAge() > 0 ? String.valueOf(a.getAge()) : "n.a.")
                                .setRating(String.format(Locale.GERMAN, "%.2f", a.getRating() * 100))
                        ).collect(Collectors.toList())
                ).toString();

        StringWriter sw = new StringWriter();
        new Index()
                .setHeader(new Header()
                        .setIsLoggedIn(true)
                        .setUsername("<& Ümläut encódèd &>"))
                .setTitle("hallo \"mängus\"")
                .setIntro("a short intro for short people")
                .setAuthors(authors.stream().map(
                        a -> new Index.Authors()
                                .setName(a.getName())
                                .setLink(a.getLink())
                                .setAge(a.getAge() > 0 ? String.valueOf(a.getAge()) : "n.a.")
                                .setRating(String.format(Locale.GERMAN, "%.2f", a.getRating() * 100))
                        ).collect(Collectors.toList())
                ).write(sw);

        System.out.printf("toString.length: %d, writer.length: %d%n", output.length(), sw.toString().length());

    }

    public static List<Author> getTestAuthors() {
        List<Author> as = new ArrayList<>();

        as.add(new Author(1, "Stephen King", "http://king.com", 55, 0.5));
        as.add(new Author(2, "John Doe", "http://doe.com/john", 34, 0.22));
        as.add(new Author(3, "Jane Doe", "http://doe.com/jane", 77, 0.98));
        as.add(new Author(4, "Master Yoda", "http://yoda.starwars.org", 22, 0.12));
        as.add(new Author(7, "The Red Queen", "http://king.com", 67, 0.87));
        as.add(new Author(10, "Riot Girl", "http://girls.at/riot", 88, 0.23));
        as.add(new Author(100, "The Riddler", "http://crosswordpuzzles.io", -1, 0.21));
        as.add(new Author(9, "Boss Man", "http://man.com/bossman", 12, 0.99999999));

        return as;
    }

    public static class Author {

        private final long id;
        private final String name;
        private final String link;
        private final int age;
        private final double rating;

        public Author(long id, String name, String link, int age, double rating) {
            this.id = id;
            this.name = name;
            this.link = link;
            this.age = age;
            this.rating = rating;
        }

        public long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getLink() {
            return link;
        }

        public int getAge() {
            return age;
        }

        public double getRating() {
            return rating;
        }
    }

}
