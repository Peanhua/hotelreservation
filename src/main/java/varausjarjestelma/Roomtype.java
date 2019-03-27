/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package varausjarjestelma;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;

/**
 *
 * @author joyr
 */
public class Roomtype extends DatabaseEntity {
    private Long   roomtype_id;
    private String name;
    
    public Roomtype(Long roomtype_id, String name) {
        this.roomtype_id = roomtype_id;
        this.name        = name;
    }
    
    public Roomtype(String name) {
        this(null, name);
    }
    
    
    public Long getId() {
        return this.roomtype_id;
    }
    
    public String getName() {
        return this.name;
    }
    
    @Override
    public String toString() {
        return "Roomtype#" + this.roomtype_id + "(name=" + this.name + ")";
    }
    
    @Override
    public void load(JdbcTemplate jdbct) {
        if(this.roomtype_id == null) {
            try {
                this.roomtype_id = jdbct.queryForObject("SELECT roomtype_id FROM Roomtype WHERE name = ?;",
                                                        Long.class,
                                                        this.name);
            } catch(Exception e) {
            }
        }
    }

    @Override
    public void save(JdbcTemplate jdbct) {
        if(this.roomtype_id == null) { // Create new.
            GeneratedKeyHolder holder = new GeneratedKeyHolder();
            jdbct.update(new PreparedStatementCreator() {
                @Override
                public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                    PreparedStatement stmt = con.prepareStatement("INSERT INTO Roomtype ( name ) VALUES ( ? );", Statement.RETURN_GENERATED_KEYS);

                    int i = 1;
                    stmt.setString(i++, name);

                    return stmt;
                }
            }, holder);

            this.roomtype_id = holder.getKey().longValue();
        }
    }
    
}
