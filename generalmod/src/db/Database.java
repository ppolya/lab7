package db;

import exceptions.EmptyIOException;
import mainlib.Reader;
import mainlib.User;
import models.*;
import server.Server;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;

public class Database {
    private static final Logger log = Logger.getLogger(Database.class.getName());
//        private static final String URL = "jdbc:postgresql://pg:5432/studs";
    private static final String URL = "jdbc:postgresql://localhost:5674/studs";
    private static final String LOGIN = "s312196";
    UserManager userManager = new UserManager();
    Map<String,Set<Integer>> ohMyUsers = new HashMap<>();
    //    private static final String PASSWORD = System.getenv().get("PASSWORD");
    Connection connection;
    Statement statement;
    private boolean isValid;


    public boolean connect() {
        try {
            Class.forName("org.postgresql.Driver");
            log.info("JDBC Driver has been successfully loaded");
            connection = DriverManager.getConnection(URL, LOGIN, "msw447");
            statement = connection.createStatement();
            log.info("database connection successfully established");
            isValid = connection.isValid(2);
        } catch (SQLException throwables) {
            Reader.PrintErr("no ways to get connection with database");
        } catch (ClassNotFoundException e) {
            Reader.PrintErr("PostgreSQL JDBC Driver is not found. Include it in your library path");
        }
        return isValid;
    }

    public boolean addToDatabase(Ticket ticket, String username) {
        boolean isAddedToDB = false;
        String statement = "INSERT INTO tickets(ticket, coordinate1, coordinate2, creation, price, valuation, venue, place, street, zip, coordinate3, coordinate4, coordinate5, town,capacity) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try {
            if (isValid()) {
                ticket.setName(username);
                PreparedStatement preparedStatement = connection.prepareStatement(statement);
                preparedStatement.setString(1, username);
                preparedStatement.setDouble(2, ticket.getCoordinates().getX());
                preparedStatement.setInt(3, ticket.getCoordinates().getY());
                preparedStatement.setString(4, String.valueOf(ticket.getCreationDate()));
                preparedStatement.setInt(5, ticket.getPrice());
                preparedStatement.setString(6, String.valueOf(ticket.getType()));
                preparedStatement.setString(7, ticket.getVenue().getName());
                preparedStatement.setString(8, String.valueOf(ticket.getVenue().getType()));
                preparedStatement.setString(9, ticket.getVenue().getAddress().getStreet());
                preparedStatement.setString(10, ticket.getVenue().getAddress().getZipCode());
                preparedStatement.setFloat(11, ticket.getVenue().getAddress().getTown().getX());
                preparedStatement.setInt(12, ticket.getVenue().getAddress().getTown().getY());
                preparedStatement.setInt(13, ticket.getVenue().getAddress().getTown().getZ());
                preparedStatement.setString(14, ticket.getVenue().getAddress().getTown().getName());
                preparedStatement.setInt(15, ticket.getVenue().getCapacity());
                int i = preparedStatement.executeUpdate();
                log.info("add object: " + i);
                if (i != 0) {
                    isAddedToDB = true;
                    ohMyUsers.put(username,getIds(username));
                    System.out.println(ohMyUsers);
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return isAddedToDB;
    }

    public boolean addIfMin(Ticket ticket, String username) {
        boolean isAdded = false;
        Integer min = getTickets().stream().map(Ticket::getId).reduce(Integer::compareTo).orElse(-1);
        if (ticket.getId() < min) {
            isAdded = addToDatabase(ticket,username);
        }
        return isAdded;
    }

    public void clearCollection() {
        String sql = "TRUNCATE TABLE tickets";
        execute(sql);
    }

    public boolean execute(String request) {
        try {
            if(statement.execute(request)) {
                return true;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }



    public boolean checkId(Integer id) {
        String statement = "SELECT FROM tickets WHERE id=?";
        ResultSet resultSet = null;
        int count = 0;
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(statement);
            preparedStatement.setInt(1, id);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                count++;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return count > 0;
    }


    public boolean removeById(Integer id, String userName) {
        String statement = "DELETE FROM tickets WHERE (id = ?) AND (ticket = ?)";
        try {
            if (isValid()) {
                PreparedStatement preparedStatement = connection.prepareStatement(statement);
                preparedStatement.setInt(1, id);
                preparedStatement.setString(2, userName);
                if (preparedStatement.executeUpdate() != 0) {
                    return true;
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }

    public boolean removeByLowerId(Integer id) {
        String statement = "DELETE FROM tickets WHERE (id < ?) AND (ticket=?)";
        if (isValid()) {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(statement);
                preparedStatement.setInt(1, id);
                preparedStatement.setString(2, userManager.getName());
                if (preparedStatement.executeUpdate() != 0) {
                    return true;
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        return false;
    }

    public boolean updateCollection(Ticket update) {
        String sql = "UPDATE tickets set coordinate1 = ?, coordinate2 = ?, creation = ?, price = ?, valuation = ?, venue = ?, place = ?, street = ?, zip = ?, coordinate3 = ?, coordinate4 = ?, coordinate5 = ?, town = ?,capacity = ? WHERE (id = ?) and (ticket = ?)";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setDouble(1, update.getCoordinates().getX());
            preparedStatement.setInt(2, update.getCoordinates().getY());
            preparedStatement.setString(3, String.valueOf(update.getCreationDate()));
            preparedStatement.setInt(4, update.getPrice());
            preparedStatement.setString(5, String.valueOf(update.getType()));
            preparedStatement.setString(6, update.getVenue().getName());
            preparedStatement.setString(7, String.valueOf(update.getVenue().getType()));
            preparedStatement.setString(8, update.getVenue().getAddress().getStreet());
            preparedStatement.setString(9, update.getVenue().getAddress().getZipCode());
            preparedStatement.setFloat(10, update.getVenue().getAddress().getTown().getX());
            preparedStatement.setInt(11, update.getVenue().getAddress().getTown().getY());
            preparedStatement.setInt(12, update.getVenue().getAddress().getTown().getZ());
            preparedStatement.setString(13, update.getVenue().getAddress().getTown().getName());
            preparedStatement.setInt(14, update.getVenue().getCapacity());
            preparedStatement.setInt(15, update.getId());
            preparedStatement.setString(16, userManager.getName());
            if (preparedStatement.executeUpdate() != 0) {
                return true;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }


    public void closeConnection() throws SQLException {
        connection.close();
    }

    public Set<Integer> getIds(String username){
        String sql = "SELECT * from tickets WHERE ticket = ?";
        Set<Integer> ids = new HashSet<>();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1,username);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                int id = resultSet.getInt("id");
                ids.add(id);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ids;
    }

    public Vector<Ticket> getTickets() {
        String statement = "SELECT * from tickets";
        Vector<Ticket> tickets = new Vector<>();
        int lastId = 0;
        Statement stm;
        try {
            if (isValid()) {
                stm = Server.getDatabase().getConnection().createStatement();
                ResultSet resultSet = stm.executeQuery(statement);
                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    lastId = id;
                    String name = resultSet.getString("ticket");
                    double coordX = resultSet.getDouble("coordinate1");
                    int coordY = resultSet.getInt("coordinate2");
                    int price = resultSet.getInt("price");
                    TicketType ticketType = TicketType.valueOf(resultSet.getString("valuation"));
                    long venueId = resultSet.getLong("id2");
                    String venue = resultSet.getString("venue");
                    int capacity = resultSet.getInt("capacity");
                    VenueType venueType = VenueType.valueOf(resultSet.getString("place"));
                    String street = resultSet.getString("street");
                    String zipCode = resultSet.getString("zip");
                    float venueX = resultSet.getFloat("coordinate3");
                    int venueY = resultSet.getInt("coordinate4");
                    int venueZ = resultSet.getInt("coordinate5");
                    String town = resultSet.getString("town");
                    String date = resultSet.getString("creation");
                    Location location = new Location(venueX, venueY, venueZ, town);
                    Address address = new Address(street, zipCode, location);
                    Venue venue1 = new Venue(venue, capacity, venueType, address);
                    venue1.setId(venueId);
                    Coordinates coordinates = new Coordinates(coordX, coordY);
                    Ticket ticket = new Ticket(coordinates, price, ticketType, venue1);
                    ticket.setId(id);
                    ticket.setCreationDate(LocalDateTime.parse(date));
                    ticket.setName(name);
                    tickets.add(ticket);
                }
                Ticket.setLastId(lastId);
            } else {
                throw new EmptyIOException();
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } catch (EmptyIOException e) {
            Reader.PrintErr("cannot find jdbc driver");
        }
        return tickets;
    }


    public boolean isValid() {
        return isValid;
    }

    public Connection getConnection() {
        return connection;
    }

    public UserManager getUserManager() {
        return userManager;
    }
}
