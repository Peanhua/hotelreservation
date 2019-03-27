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
public class Option extends DatabaseEntity {
    
    private Long   option_id;
    private String name;
    
    public Option(Long option_id, String name) {
        this.option_id = option_id;
        this.name      = name;
    }
    
    public Option(String name) {
        this(null, name);
    }
    
    
    public Long getId() {
        return this.option_id;
    }
    
    public String getName() {
        return this.name;
    }
    
    @Override
    public String toString() {
        return "Option#" + this.option_id + "(name=" + this.name + ")";
    }
    
    @Override
    public void load(JdbcTemplate jdbct) {
        if(this.option_id == null) {
            this.option_id = jdbct.queryForObject("SELECT option_id FROM Option WHERE name = ?;",
                                                  Long.class,
                                                  this.name);
        }
    }

    @Override
    public void save(JdbcTemplate jdbct) {
        if(this.option_id == null) { // Create new.
            GeneratedKeyHolder holder = new GeneratedKeyHolder();
            jdbct.update(new PreparedStatementCreator() {
                @Override
                public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                    PreparedStatement stmt = con.prepareStatement("INSERT INTO Option ( name ) VALUES ( ? );", Statement.RETURN_GENERATED_KEYS);

                    int i = 1;
                    stmt.setString(i++, name);

                    return stmt;
                }
            }, holder);

            this.option_id = holder.getKey().longValue();
        }
    }
}
