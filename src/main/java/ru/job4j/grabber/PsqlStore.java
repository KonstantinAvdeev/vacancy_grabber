package ru.job4j.grabber;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PsqlStore implements Store, AutoCloseable {

    private Connection cnn;

    public PsqlStore(Properties cfg) {
        try {
            Class.forName(cfg.getProperty("driver-class-name"));
            cnn = DriverManager.getConnection(
                    cfg.getProperty("url"),
                    cfg.getProperty("username"),
                    cfg.getProperty("password"));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public static void main(String[] args) {
        try (InputStream in = PsqlStore.class.getClassLoader().getResourceAsStream("rabbit.properties")) {
            Properties config = new Properties();
            config.load(in);
            try (PsqlStore store = new PsqlStore(config)) {
                Post post1 = new Post("Java Разработчик (Тинькофф Инвестиции)", "https://career.habr.com/vacancies/1000103322",
                        "Описание вакансии\n"
                                + "Увеличиваем мощность разработки в Тинькофф Инвестициях и приглашаем в команду!\n"
                                + "\n"
                                + "Тинькофф Инвестиции создали собственного уникального и технологичного онлайн-брокера,"
                                + " сделали самый сложный финансовый продукт доступным и удобным для всех.",
                        LocalDateTime.of(2022, 10, 31, 20, 27, 43));
                Post post2 = new Post("Android разработчик: Kotlin\\Java", "https://career.habr.com/vacancies/1000112120",
                        "Описание вакансии\n"
                                + "ПРИМЕРЫ РЕАЛИЗОВАННЫХ ЗАДАЧ:\n"
                                + "Разработать выпуск социальной виртуальной «Пушкинской карты»\u200E для любителей театра;\n"
                                + "Зафигачить защиту от мошенников: разобраться с механизмом подтверждения операции, "
                                + "переписать чисто без использования сторонних библиотек; Добавить токенизацию карты МИР;\n"
                                + "Создать универсальный механизм заполнения полей: сделать так, чтобы человек заполнял не"
                                + " длинную портянку из инпутов, а видел удобное поле на отдельном экране.",
                        LocalDateTime.of(2022, 10, 31, 19, 53, 30));
                store.save(post1);
                store.save(post2);
                System.out.println(store.getAll());
                System.out.println(store.findById(1));
                System.out.println(store.findById(2));

            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

    }

    @Override
    public void save(Post post) {
        try (PreparedStatement preparedStatement = cnn.prepareStatement("insert into post(name, link, text, created) " +
                        "values(?, ?, ?, ?);",
                Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, post.getTitle());
            preparedStatement.setString(2, post.getLink());
            preparedStatement.setString(3, post.getDescription());
            preparedStatement.setTimestamp(4, Timestamp.valueOf(post.getCreated()));
            preparedStatement.execute();
            try (ResultSet set = preparedStatement.getGeneratedKeys()) {
                if (set.next()) {
                    post.setId(set.getInt(1));
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public List<Post> getAll() {
        List<Post> list = new ArrayList<>();
        try (PreparedStatement preparedStatement = cnn.prepareStatement("select * from post;")) {
            try (ResultSet set = preparedStatement.executeQuery()) {
                while (set.next()) {
                    list.add(returnPost(set));
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        return list;
    }

    @Override
    public Post findById(int id) {
        Post post = null;
        try (PreparedStatement statement = cnn.prepareStatement("select * from post where id = ?;")) {
            statement.setInt(1, id);
            statement.execute();
            try (ResultSet set = statement.getResultSet()) {
                if (set.next()) {
                    post = returnPost(set);
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        return post;
    }

    @Override
    public void close() throws Exception {
        if (cnn != null) {
            cnn.close();
        }
    }

    public Post returnPost(ResultSet set) throws SQLException {
        return new Post(set.getInt("id"),
                set.getString("name"),
                set.getString("link"),
                set.getString("text"),
                set.getTimestamp("created").toLocalDateTime());
    }

}