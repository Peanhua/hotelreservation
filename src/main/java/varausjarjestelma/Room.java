/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package varausjarjestelma;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.core.PreparedStatementCreator;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.rowset.SqlRowSet;


/**
 *
 * @author joyr
 */
public class Room extends DatabaseEntity {
    
    private Long   room_id;
    private String type;
    private int    name;
    private int    price_per_day;
    
    
    public static ResultSetExtractor<List<Room>> getListResultSetExtractor() {
        ResultSetExtractor<List<Room>> rse = new ResultSetExtractor<List<Room>>() {
            @Override
            public List<Room> extractData(ResultSet rs) throws java.sql.SQLException, DataAccessException {
                ArrayList<Room> rv = new ArrayList<>();
                while(rs.next()) {
                    rv.add(new Room(rs.getLong("room_id"),
                                    rs.getString("type"),
                                    rs.getInt("name"),
                                    rs.getInt("price_per_day")
                                   )
                    );
                }
                return rv;
            }
        };
        
        return rse;
    }

    
    
    public Room(Long room_id, String type, int name, int price_per_day) {
        this.room_id       = room_id;
        this.type          = type;
        this.name          = name;
        this.price_per_day = price_per_day;
    }
    
    public Room(String type, int name, int price_per_day) {
        this(null, type, name, price_per_day);
    }
    
    public Room(Long room_id) {
        this(room_id, null, -1, -1);
    }
    
    public Room(SqlRowSet rs) {
        this.room_id       = rs.getLong(getSqlRowSetColumnIndex(rs,   "ROOM",     "ROOM_ID"));
        this.type          = rs.getString(getSqlRowSetColumnIndex(rs, "ROOMTYPE", "NAME"));
        this.name          = rs.getInt(getSqlRowSetColumnIndex(rs,    "ROOM",     "NAME"));
        this.price_per_day = rs.getInt(getSqlRowSetColumnIndex(rs,    "ROOM",     "PRICE_PER_DAY"));
    }
    
    public Long getId() {
        return this.room_id;
    }
    
    public String getType() {
        return this.type;
    }
    
    public int getName() {
        return this.name;
    }
    
    public int getPricePerDay() {
        return this.price_per_day;
    }
    
    @Override
    public String toString() {
        return "Room#" + this.room_id + "(type=" + this.type + ",name=" + this.name + ",price=" + this.price_per_day + ")";
    }

    @Override
    public void save(JdbcTemplate jdbct) {
        if(this.room_id == null) { // Create new.
            Roomtype t = new Roomtype(this.type);
            t.load(jdbct);
            t.save(jdbct);
            
            GeneratedKeyHolder holder = new GeneratedKeyHolder();
            jdbct.update(new PreparedStatementCreator() {
                @Override
                public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                    PreparedStatement stmt = con.prepareStatement("INSERT INTO Room ( roomtype_id, name, price_per_day ) VALUES ( ?, ?, ? );", Statement.RETURN_GENERATED_KEYS);

                    int i = 1;
                    stmt.setLong(i++, t.getId());
                    stmt.setInt(i++,  name);
                    stmt.setInt(i++,  price_per_day);

                    return stmt;
                }
            }, holder);

            this.room_id = holder.getKey().longValue();
        }
    }
}
