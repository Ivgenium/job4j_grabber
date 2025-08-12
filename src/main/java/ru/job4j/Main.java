package ru.job4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.job4j.grabber.model.Post;
import ru.job4j.grabber.service.*;
import ru.job4j.grabber.stores.JdbcStore;
import ru.job4j.grabber.stores.Store;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

public class Main {
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        var config = new Config();
        config.load("application.properties");
        try (var connection = DriverManager.getConnection(config.get("db.url"),
                config.get("db.username"),
                config.get("db.password"));
             var scheduler = new
                     SchedulerManager()) {
            Store store = new JdbcStore(connection);

            // Добавляем данные с Хабра
            HabrCareerParse habrCareerParse = new HabrCareerParse(new HabrCareerDateTimeParser());
            List<Post> rsl = habrCareerParse.fetch();
            for (Post el : rsl) {
                store.save(el);
            }

            // Настраиваем и запускаем планировщик
            scheduler.init();
            scheduler.load(
                    Integer.parseInt(config.get("rabbit.interval")),
                    SuperJobGrab.class,
                    store
            );

            // Запускаем веб-сервер
            new Web(store).start(Integer.parseInt(config.get("server.port")));
        } catch (SQLException e) {
            LOG.error("When creating a connection", e);
        }
    }
}