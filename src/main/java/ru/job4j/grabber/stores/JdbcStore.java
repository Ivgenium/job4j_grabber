package ru.job4j.grabber.stores;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.job4j.grabber.model.Post;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcStore implements Store {
    private final Connection connection;
    private static final Logger LOG = LoggerFactory.getLogger(JdbcStore.class.getName());

    public JdbcStore(Connection connection) {
        this.connection = connection;
    }

    private Post createPost(ResultSet resultSet) throws SQLException {
        return new Post(resultSet.getLong("id"),
                resultSet.getString("name"),
                resultSet.getString("link"),
                resultSet.getString("text"),
                resultSet.getTimestamp("created").toLocalDateTime());
    }

    @Override
    public void save(Post post) {
        String sql = "INSERT INTO post (name, text, link, created) VALUES (?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, post.getTitle());
            preparedStatement.setString(2, post.getDescription());
            preparedStatement.setString(3, post.getLink());
            preparedStatement.setTimestamp(4, Timestamp.valueOf(post.getTime()));
            preparedStatement.execute();
        } catch (SQLException e) {
            LOG.error("When save post into database ", e);
        }
    }

    @Override
    public List<Post> getAll() {
        List<Post> rsl = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT * FROM post")) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                rsl.add(createPost(resultSet));
            }
        } catch (SQLException e) {
            LOG.error("When get all posts in the database ", e);
        }
        return rsl;
    }

    @Override
    public Optional<Post> findById(Long id) {
        Optional<Post> rsl = Optional.empty();
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT * FROM post WHERE id = ?")) {
            preparedStatement.setLong(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                rsl = Optional.of(createPost(resultSet));
            }
        } catch (SQLException e) {
            LOG.error("When find post by id in the database ", e);
        }
        return rsl;
    }
}