/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package varausjarjestelma;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;


/**
 *
 * @author joyr
 */
public class Reservation extends DatabaseEntity {
    
    private Long         reservation_id;
    private Client       client;
    private DbDate       start_date;
    private DbDate       end_date;
    private long         total_price;
    private int          option_count;
    private List<Room>   rooms;
    private List<Option> options;
    
    public Reservation(Long reservation_id, Client client, DbDate start_date, DbDate end_date, long total_price, List<Room> rooms, List<Option> options) {
        this.reservation_id = reservation_id;
        this.client         = client;
        this.start_date     = start_date;
        this.end_date       = end_date;
        this.total_price    = total_price;
        this.option_count   = options == null ? 0 : options.size();
        this.rooms          = rooms;
        this.options        = options;
        
        this.calculateTotalPrice();
    }
    
    public Reservation(Client client, DbDate start_date, DbDate end_date, List<Room> rooms, List<Option> options) {
        this(null, client, start_date, end_date, 0, rooms, options);
    }
    
    public Reservation(Client client) {
        this(null, client, null, null, 0, null, null);
    }
    
    private void calculateTotalPrice() {
        if(this.rooms != null) {
            this.total_price = 0;
            for(Room r : this.rooms)
                this.total_price += r.getPricePerDay();
            this.total_price *= this.getDays();
        }
    }
    
    public Long getId() {
        return this.reservation_id;
    }
    
    public Client getClient() {
        return this.client;
    }
    
    public List<Room> getRooms() {
        return this.rooms;
    }
    
    public DbDate getStartDate() {
        return this.start_date;
    }
    
    public DbDate getEndDate() {
        return this.end_date;
    }
    
    public long getDays() {
        return this.start_date.getDaysTo(this.end_date);
    }
    
    public long getTotalPrice() {
        return this.total_price;
    }
    
    public int getOptionCount() {
        return this.option_count;
    }
    
    public void loadRoomFromSRS(SqlRowSet rs) {
        if(this.rooms == null)
            this.rooms = new ArrayList<>();
        
        Room r = new Room(rs);
        this.rooms.add(r);
    }
    
    @Override
    public String toString() {
        return "Reservation#" + this.reservation_id + "(client=" + this.client + ",start_date=" + this.start_date + ",end_date=" + this.end_date + ",roomcount=" + (this.rooms == null ? 0 : this.rooms.size()) + ")";
    }
    
    @Override
    public void loadFromRS(ResultSet rs) throws SQLException {
        this.reservation_id = rs.getLong("Reservation.reservation_id");
        this.start_date     = new DbDate(rs.getString("Reservation.start_date"));
        this.end_date       = new DbDate(rs.getString("Reservation.end_date"));
        this.total_price    = rs.getLong("Reservation.total_price");
        this.option_count   = rs.getInt("Reservation.option_count");
    }

    @Override
    public void loadFromSRS(SqlRowSet rs) throws SQLException {
        this.reservation_id = rs.getLong(this.getSqlRowSetColumnIndex(rs,              "RESERVATION", "RESERVATION_ID"));
        this.start_date     = new DbDate(rs.getString(this.getSqlRowSetColumnIndex(rs, "RESERVATION", "START_DATE")));
        this.end_date       = new DbDate(rs.getString(this.getSqlRowSetColumnIndex(rs, "RESERVATION", "END_DATE")));
        this.total_price    = rs.getLong(this.getSqlRowSetColumnIndex(rs,              "RESERVATION", "TOTAL_PRICE"));
        this.option_count   = rs.getInt(this.getSqlRowSetColumnIndex(rs,               "RESERVATION", "OPTION_COUNT"));
    }
    
    @Override
    public void save(JdbcTemplate jdbct) {
        if(this.reservation_id == null) { // Only save if this has not been saved before because the system does not support changing a reservation.
            GeneratedKeyHolder holder = new GeneratedKeyHolder();
            jdbct.update(new PreparedStatementCreator() {
                @Override
                public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                    PreparedStatement stmt = con.prepareStatement("INSERT INTO Reservation ( client_id, start_date, end_date, total_price, option_count ) VALUES ( ?, ?, ?, ?, ? );", Statement.RETURN_GENERATED_KEYS);

                    int i = 1;
                    stmt.setLong(i++,   client.getId());
                    stmt.setString(i++, start_date.toString());
                    stmt.setString(i++, end_date.toString());
                    stmt.setLong(i++,   total_price);
                    stmt.setInt(i++,    option_count);

                    return stmt;
                }
            }, holder);

            this.reservation_id = holder.getKey().longValue();

            for(Room r : this.rooms)
                jdbct.update("INSERT INTO ReservationRoom ( reservation_id, room_id ) VALUES ( ?, ? );", this.reservation_id, r.getId());

            for(Option o : this.options)
                jdbct.update("INSERT INTO ReservationOption ( reservation_id, option_id ) VALUES ( ?, ? );", this.reservation_id, o.getId());
        }
    }
}
