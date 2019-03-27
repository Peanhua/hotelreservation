/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package varausjarjestelma;

import java.time.LocalDateTime;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.rowset.SqlRowSet;

/**
 *
 * @author joyr
 */
@Component
public class ReservationSystem {
    
    private JdbcTemplate jdbct;
    private boolean      debug = false;

    @Autowired
    public ReservationSystem(JdbcTemplate jdbct) {
        if(this.debug)
            System.out.println("ReservationSystem::constructor()");
        
        this.jdbct = jdbct;
        
        int dbver = 1;
        int curdbver = getDatabaseVersion();
        
        if(this.debug) {
            System.out.println("ReservationSystem::Supported database version=" + dbver);
            System.out.println("ReservationSystem::Database version=" + curdbver);
        }
        
        if(curdbver < dbver)
            setupDatabase();
        else if(curdbver > dbver)
            throw new RuntimeException("ERROR! The currently initialized database is not supported: installed version=" + curdbver + ", maximum supported version=" + dbver);
    }

    
    public Room addRoom(String type, int name, int price_per_day) {
        Room r = new Room(type, name, price_per_day);
        r.save(this.jdbct);
        return r;
    }
    
    
    public List<Room> getRooms() {
        String q = "SELECT room_id, Roomtype.name AS type, Room.name AS name, price_per_day"
                 + "  FROM Room"
                 + "  JOIN Roomtype ON Roomtype.roomtype_id = Room.roomtype_id";
        List<Room> rv = this.jdbct.query(q, Room.getListResultSetExtractor());
        return rv;
    }
    
    
    public List<Room> getFreeRooms(LocalDateTime start, LocalDateTime end, String type, Integer max_price_per_day) {
        DbDate sd = new DbDate(start);
        DbDate ed = new DbDate(end);

        String q = "SELECT room_id, Roomtype.name AS type, Room.name AS name, price_per_day"
                   + "  FROM Room"
                   + "  JOIN Roomtype ON Roomtype.roomtype_id = Room.roomtype_id"
                   + " WHERE Room.room_id NOT IN ("
                   + "       SELECT DISTINCT ReservationRoom.room_id"
                   + "         FROM ReservationRoom"
                   + "         JOIN Reservation ON Reservation.reservation_id = ReservationRoom.reservation_id"
                   + "        WHERE Reservation.start_date < '" + ed + "'"
                   + "          AND Reservation.end_date   > '" + sd + "'"
                   + "       )"
                   + " ";
        
        String sorting = " ORDER BY Room.price_per_day DESC, Room.name ASC;";
        
        ResultSetExtractor<List<Room>> rse = Room.getListResultSetExtractor();
        
        List<Room> rv;
        
        if(type == null && max_price_per_day == null) {
            rv = this.jdbct.query(q + sorting,
                                  rse);
            
        } else if(type != null && max_price_per_day == null) {
            rv = this.jdbct.query(q + "AND Roomtype.name = ?" + sorting,
                                  rse,
                                  type);
                    
        } else if(type == null && max_price_per_day != null) {
            rv = this.jdbct.query(q + "AND Room.price_per_day <= ?" + sorting,
                                  rse,
                                  max_price_per_day);
            
        } else /* if(type != null && max_price_per_day != null) */ {
            rv = this.jdbct.query(q + "AND Roomtype.name = ? AND Room.price_per_day <= ?" + sorting,
                                  rse,
                                  type, max_price_per_day);
            
        }
        
        return rv;
    }

    
    public Reservation makeReservation(String client_name, String client_phone, String client_email,
                                   LocalDateTime start, LocalDateTime end,
                                   int room_count, String room_type, Integer max_room_price_per_day,
                                   List<String> options) {
        try {
            this.jdbct.update("BEGIN WORK;");

            List<Room> tmp = this.getFreeRooms(start, end, room_type, max_room_price_per_day);
            if(tmp.size() < room_count) {
                throw new RuntimeException("Not enough available rooms.");
            }
            List<Room> rooms = tmp.subList(0, room_count);

            List<Option> opts = new ArrayList<>();
            for(String opt : options) {
                Option o = new Option(opt);
                try {
                    o.save(this.jdbct);
                } catch(Exception e) {
                    // Ignore errors when saving, most likely error is that the option already existed.
                }
                o.load(this.jdbct);
                opts.add(o);
            }
            
            Client c = new Client(client_name, client_phone, client_email);
            try {
                c.save(this.jdbct);
            } catch(Exception e) {
                // Ignore errors when saving.
            }
            c.load(this.jdbct);
            
            Reservation r = new Reservation(c, new DbDate(start), new DbDate(end), rooms, opts);
            r.save(this.jdbct);
            
            this.jdbct.update("COMMIT WORK;");
            
            return r;
      
        } catch(Exception e) {
            System.out.println(e.toString() + ": " + e.getMessage());
            try {
                this.jdbct.update("ROLLBACK WORK;");

            } catch(Exception ee) {
            }
        }
                
        return null;
    }
    
    
    public List<Reservation> getReservations() {
        String q = "SELECT *"
                 + "  FROM Reservation"
                 + "  JOIN Client          ON Client.client_id               = Reservation.client_id"
                 + "  JOIN ReservationRoom ON ReservationRoom.reservation_id = Reservation.reservation_id"
                 + "  JOIN Room            ON Room.room_id                   = ReservationRoom.room_id"
                 + "  JOIN Roomtype        ON Roomtype.roomtype_id           = Room.roomtype_id"
                 + " ORDER BY Reservation.start_date";
        List<Reservation> rv = new ArrayList<>();
        SqlRowSet rs = this.jdbct.queryForRowSet(q);

        Reservation current = null;
        int resid_index = new Option("").getSqlRowSetColumnIndex(rs, "RESERVATION", "RESERVATION_ID");
        while(rs.next()) {
            if(current == null || rs.getLong(resid_index) != current.getId()) {
                try {
                    Client c = new Client();
                    c.loadFromSRS(rs);

                    current = new Reservation(c);
                    current.loadFromSRS(rs);

                    rv.add(current);
                    
                } catch(Exception e) {
                    System.out.println(e);
                    current = null;
                }
            }
            
            if(current != null) {
                current.loadRoomFromSRS(rs);
            }
        }

        return rv;
    }
    
    
    public List<Tuple<Option, Integer>> getMostPopularOptions(Integer max_number_of_options) {
        String q = "SELECT Option.name, COUNT(ReservationOption.reservation_id) AS cnt"
                 + "  FROM ReservationOption"
                 + "  JOIN Option ON Option.option_id = ReservationOption.option_id"
                 + " GROUP BY ReservationOption.option_id"
                 + " ORDER BY cnt DESC";
        if(max_number_of_options != null)
            q += " LIMIT " + max_number_of_options;
        
        return this.jdbct.query(q, (rs, rowNum) -> new Tuple<>(new Option(rs.getString("Option.name")), rs.getInt("cnt")));
    }
    
    
    public List<Tuple<Client, Long>> getBestClients(Integer max_number_of_clients) {
        String q = "SELECT Client.client_id, Client.name, Client.phone, Client.email, SUM(Reservation.total_price) AS value"
                 + "  FROM Client"
                 + "  JOIN Reservation ON Reservation.client_id = Client.client_id"
                 + " GROUP BY Client.client_id"
                 + " ORDER BY value DESC";
        if(max_number_of_clients != null)
            q += " LIMIT " + max_number_of_clients;
        
        List<Tuple<Client, Long>> rv = this.jdbct.query(q, (rs, rowNum) -> {
            Client c = new Client();
            c.loadFromRS(rs); //rs.getLong("Client.client_id"), rs.getString("Client.name"), rs.getString("Client.phone"), rs.getString("Client.email"));
            long v   = rs.getLong("value");
            return new Tuple<>(c, v);
        });
        
        return rv;
    }
    
    
    public List<Tuple<Room, Double>> getRoomUtilization(DbDate start_date, DbDate end_date) {
        // Calculate the number of days each room has been used:
        HashMap<Long, Long> utilization = new HashMap<>(); // room_id, number of days reserved
        
        String q = "SELECT ReservationRoom.room_id,"
                 + "       CASE WHEN Reservation.start_date > '" + start_date + "'"
                 + "            THEN Reservation.start_date"
                 + "            ELSE '" + start_date + "'"
                 + "       END AS start_date,"
                 + "       CASE WHEN Reservation.end_date < '" + end_date + "'"
                 + "            THEN Reservation.end_date"
                 + "            ELSE '" + end_date + "'"
                 + "       END AS end_date"
                 + "  FROM ReservationRoom"
                 + "  JOIN Reservation ON Reservation.reservation_id = ReservationRoom.reservation_id"
                 + " WHERE Reservation.start_date <= '" + end_date + "'"
                 + "   AND Reservation.end_date   >= '" + start_date + "'";
        
        this.jdbct.query(q, (rs, rowNum) -> {
            long room_id = rs.getLong("room_id");
            DbDate start = new DbDate(rs.getString("start_date"));
            DbDate end   = new DbDate(rs.getString("end_date"));
            
            long count = utilization.getOrDefault(room_id, 0L);
            //System.out.print("" + room_id + ": " + start + "->" + end + ": " + count + "+" + start.getDaysTo(end) + "=");
            count += start.getDaysTo(end);
            utilization.put(room_id, count);
            //System.out.println(count);
            
            return null;
        });
        
        // Calculate the utilization percentage for each room:
        List<Tuple<Room, Double>> rv = new ArrayList<>();
        long days = start_date.getDaysTo(end_date) + 1; // +1 to include the end date
        this.getRooms().forEach((room) -> rv.add(new Tuple<>(room, (double) utilization.getOrDefault(room.getId(), 0L) * 100.0 / (double) days)));
        
        return rv;
    }
    
    
    public List<Tuple<String, Double>> getRoomTypeUtilization(DbDate start_date, DbDate end_date) {
        // Calculate the number of days each room type has been used:
        HashMap<String, Long> utilization = new HashMap<>(); // type, number of days reserved
        // Count the number of rooms each type has.
        // Also initialize utilization with all room types, so we also return types with zero reservations.
        HashMap<String, Integer> roomtypecounts = new HashMap<>(); // type, number of rooms for the type
        this.jdbct.query("SELECT Roomtype.name AS type, COUNT(room_id) AS cnt"
                         + " FROM Room"
                         + " JOIN Roomtype ON Roomtype.roomtype_id = Room.roomtype_id"
                         + " GROUP BY Room.roomtype_id",
                         (rs, rowNum) -> {
                             String type = rs.getString("type");
                             roomtypecounts.put(type, rs.getInt("cnt"));
                             utilization.put(type, 0L);
                             return null;
                         });
        
        String q = "SELECT Roomtype.name AS type,"
                 + "       CASE WHEN Reservation.start_date > '" + start_date + "'"
                 + "            THEN Reservation.start_date"
                 + "            ELSE '" + start_date + "'"
                 + "       END AS start_date,"
                 + "       CASE WHEN Reservation.end_date < '" + end_date + "'"
                 + "            THEN Reservation.end_date"
                 + "            ELSE '" + end_date + "'"
                 + "       END AS end_date"
                 + "  FROM Room"
                 + "  JOIN Roomtype ON Roomtype.roomtype_id = Room.roomtype_id"
                 + "  JOIN ReservationRoom ON ReservationRoom.room_id = Room.room_id"
                 + "  JOIN Reservation ON Reservation.reservation_id = ReservationRoom.reservation_id"
                 + " WHERE Reservation.start_date <= '" + end_date + "'"
                 + "   AND Reservation.end_date   >= '" + start_date + "'";
        
        this.jdbct.query(q, (rs, rowNum) -> {
            String type  = rs.getString("type");
            DbDate start = new DbDate(rs.getString("start_date"));
            DbDate end   = new DbDate(rs.getString("end_date"));
            //System.out.print("room=" + rs.getInt("Room.name") + ": " + start + "->" + end + ": " + utilization.get(type) + "+" + start.getDaysTo(end));
            utilization.put(type, utilization.get(type) + start.getDaysTo(end));
            //System.out.println("=" + utilization.get(type));
            
            return null;
        });

        // Calculate the utilization percentage for each room:
        List<Tuple<String, Double>> rv = new ArrayList<>();
        long days = start_date.getDaysTo(end_date) + 1; // +1 to include the end date
        utilization.keySet().forEach((type) -> rv.add(new Tuple<>(type, (double) utilization.get(type) * 100.0 / (double) (days * roomtypecounts.get(type)))));
        
        return rv;
    }
    
    
    private int getDatabaseVersion() {
        try {
            return this.jdbct.queryForObject("SELECT version FROM ReservationSystem WHERE system_name = 'database';", Integer.class);
        } catch(Exception e) {
        }
        
        return 0;
    }
    
    private void setupDatabase() {
        if(this.debug)
            System.out.println("ReservationSystem::setupDatabase()");
        
        try {
            this.jdbct.update("BEGIN WORK;");
            
            this.jdbct.update("DROP TABLE ReservationSystem IF EXISTS;");
            this.jdbct.update("CREATE TABLE ReservationSystem ( system_name VARCHAR(8) PRIMARY KEY, version INTEGER NOT NULL );");
            
            this.jdbct.update("DROP TABLE Roomtype IF EXISTS;");
            this.jdbct.update("CREATE TABLE Roomtype ("
                            + "  roomtype_id    INTEGER AUTO_INCREMENT NOT NULL PRIMARY KEY,"
                            + "  name           VARCHAR(80) NOT NULL"
                            + ");");

            this.jdbct.update("DROP TABLE Room IF EXISTS;");
            this.jdbct.update("CREATE TABLE Room ("
                            + "  room_id        INTEGER AUTO_INCREMENT NOT NULL PRIMARY KEY,"
                            + "  roomtype_id    INTEGER NOT NULL,"
                            + "  name           INTEGER NOT NULL,"
                            + "  price_per_day  INTEGER NOT NULL,"
                            + " FOREIGN KEY(roomtype_id) REFERENCES Roomtype(roomtype_id)"
                            + ");");
            
            this.jdbct.update("DROP TABLE Client IF EXISTS;");
            this.jdbct.update("CREATE TABLE Client ("
                            + "  client_id      INTEGER AUTO_INCREMENT NOT NULL PRIMARY KEY,"
                            + "  name           VARCHAR(80) NOT NULL,"
                            + "  phone          VARCHAR(40) NOT NULL,"
                            + "  email          VARCHAR(80) NOT NULL"
                            + ");");
            this.jdbct.update("CREATE UNIQUE INDEX idx_client_name_phone_email ON Client(name, phone, email);");
            
            this.jdbct.update("DROP TABLE Reservation IF EXISTS;");
            this.jdbct.update("CREATE TABLE Reservation ("
                            + "  reservation_id INTEGER AUTO_INCREMENT NOT NULL PRIMARY KEY,"
                            + "  client_id      INTEGER NOT NULL,"
                            + "  start_date     DATE NOT NULL,"
                            + "  end_date       DATE NOT NULL,"
                            + "  total_price    LONG NOT NULL,"
                            + "  option_count   INTEGER NOT NULL,"
                            + " FOREIGN KEY (client_id) REFERENCES Client(client_id)"
                            + ");");
            
            this.jdbct.update("DROP TABLE Option IF EXISTS;");
            this.jdbct.update("CREATE TABLE Option ("
                            + "  option_id      INTEGER AUTO_INCREMENT NOT NULL PRIMARY KEY,"
                            + "  name           VARCHAR(80) NOT NULL UNIQUE"
                            + ");");
            this.jdbct.update("CREATE UNIQUE INDEX idx_option_name ON Option(name);");
 
            this.jdbct.update("DROP TABLE ReservationRoom IF EXISTS;");
            this.jdbct.update("CREATE TABLE ReservationRoom ("
                            + "  reservation_id INTEGER NOT NULL,"
                            + "  room_id        INTEGER NOT NULL,"
                            + "  FOREIGN KEY (reservation_id) REFERENCES Reservation(reservation_id),"
                            + "  FOREIGN KEY (room_id)        REFERENCES Room(room_id)"
                            + ");");

            this.jdbct.update("DROP TABLE ReservationOption IF EXISTS;");
            this.jdbct.update("CREATE TABLE ReservationOption ("
                            + "  reservation_id INTEGER NOT NULL,"
                            + "  option_id      INTEGER NOT NULL,"
                            + "  FOREIGN KEY (reservation_id) REFERENCES Reservation(reservation_id),"
                            + "  FOREIGN KEY (option_id)      REFERENCES Option(option_id)"
                            + ");");

            this.jdbct.update("INSERT INTO ReservationSystem ( system_name, version ) VALUES ( 'database', 1 );");
            
            this.jdbct.update("COMMIT WORK;");
            
        } catch(Exception e) {
            System.out.println(e.toString() + ": " + e.getMessage());
        }
    }
}
