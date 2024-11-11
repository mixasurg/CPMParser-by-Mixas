/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package database;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author yamis
 */
public class DatabaseConnect {

    Connection connection;
    String url = "jdbc:postgresql://localhost:5432/CPMParser1";
    String user = "postgres";
    String password = "mixasurg";

    public Connection dbConnection() throws SQLException, ClassNotFoundException {
        Class.forName("org.postgresql.Driver");

        connection = DriverManager.getConnection(url, user, password);

        return connection;
    }

    public List<String> getAllUsers() throws SQLException {
        String query = "SELECT * FROM public.users ";
        List<String> users = new ArrayList<>();
        Statement statement = connection.createStatement();
        ResultSet result = statement.executeQuery(query);
        while (result.next()) {
            if (result.getBoolean("Access")) {
                users.add(result.getString("Discord_id"));
            }
        }

        return users;
    }

    public List<String> getUserVison(int vision) throws SQLException {
        String query = "SELECT \"Discord_id\" FROM public.users WHERE \"Vision\" = ?";
        PreparedStatement countStatement = connection.prepareStatement(query);
        countStatement.setInt(1, vision);
        ResultSet result = countStatement.executeQuery();
        List<String> userid = new ArrayList<>();
        while (result.next()) {
            userid.add(result.getString("Discord_id"));
        }
        
        query = "SELECT \"Discord_id\" FROM public.users WHERE \"Vision\" = ?";
        countStatement = connection.prepareStatement(query);
        countStatement.setInt(1, 3);
        result = countStatement.executeQuery();
        while (result.next()) {
            userid.add(result.getString("Discord_id"));
        }
        return userid;
    }

    public List<String> getAllCategory() throws SQLException {
        String query = "SELECT * FROM \"Category\"";
        List<String> category = new ArrayList<>();
        Statement statement = connection.createStatement();
        ResultSet result = statement.executeQuery(query);
        while (result.next()) {
            category.add(result.getString("Category"));
        }

        return category;
    }

    public List<String> getUserCategory(String id) throws SQLException {
        List<String> category = new ArrayList<>();
        String sql = "SELECT c.* "
                + "FROM public.\"UserCategory\" uc "
                + "JOIN public.\"Category\" c ON uc.\"Category_id\" = c.\"Category_id\" "
                + "WHERE uc.\"Discord_id\" = ?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, id);
        ResultSet resultSet = statement.executeQuery();

        while (resultSet.next()) {
            category.add(resultSet.getString("Category"));
        }

        return category;
    }

    public Map<String, String> getGuildsChannels() throws SQLException {
        Map<String, String> guildsChannels = new HashMap<>();
        String query = "SELECT * FROM \"GuildChannel\"";
        Statement statement = connection.createStatement();
        ResultSet result = statement.executeQuery(query);

        while (result.next()) {
            guildsChannels.put(result.getString("Guild_id"), result.getString("Channel_id"));
        }

        return guildsChannels;
    }

    public void addGuildChannel(String guildId, String channelId) throws SQLException {
        String query = "INSERT INTO public.\"GuildChannel\"(\"Guild_id\", \"Channel_id\") VALUES (?, ?)";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, guildId);
        statement.setString(2, channelId);
        statement.executeUpdate();
    }

    public boolean addUserCatgeroy(String discord_id, String category) throws SQLException {

        String query = "SELECT \"Category_id\" FROM public.\"Category\" WHERE \"Category\" = ?";
        PreparedStatement categoryIdStatement = connection.prepareStatement(query);
        categoryIdStatement.setString(1, category);
        ResultSet resultSet = categoryIdStatement.executeQuery();

        int categoryId = 0;
        if (resultSet.next()) {
            categoryId = resultSet.getInt("Category_id");
        }
            System.out.println(discord_id + "  " + categoryId);

        query = "INSERT INTO public.\"UserCategory\"(\"Discord_id\", \"Category_id\") VALUES (?, ?)";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, discord_id);
        statement.setInt(2, categoryId);
        statement.executeUpdate();
                    System.out.println(discord_id + "  " + categoryId);

        return true;
    }

    public void addUser(String Discord_id, String Username) throws SQLException {
        String query = "INSERT INTO public.users( \"Discord_id\", \"Username\", \"Access\" , \"Vision\") VALUES (?, ?, false, 3)";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, Discord_id);
        statement.setString(2, Username);
        statement.executeUpdate();
    }

    public void updateVision(String Discord_id, int newVision) throws SQLException {
        String updateQuery = "UPDATE public.users SET \"Vision\" = ? WHERE \"Discord_id\" = ?";
        PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
        updateStatement.setInt(1, newVision);
        updateStatement.setString(2, Discord_id);
        updateStatement.executeUpdate();
    }
    
}
