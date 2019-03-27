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
public class Client extends DatabaseEntity {
    
    private Long    client_id;
    private String  name;
    private String  phone;
    private String  email;
    private boolean loaded;
    
    public Client(Long client_id, String name, String phone, String email) {
        this.client_id = client_id;
        this.name      = name;
        this.phone     = phone;
        this.email     = email;
        this.loaded    = false;
    }
    
    public Client(String name, String phone, String email) {
        this(null, name, phone, email);
    }
    
    public Client() {
        this(null, null, null, null);
    }
    
    public Long getId() {
        return this.client_id;
    }
    
    public String getName() {
        return this.name;
    }
    
    public String getPhone() {
        return this.phone;
    }
    
    public String getEmail() {
        return this.email;
    }
    
    @Override
    public String toString() {
        return "Client#" + this.client_id + "(name=" + this.name + ",phone=" + this.phone + ",email=" + this.email + ")";
    }

    @Override
    public void load(JdbcTemplate jdbct) {
        if(!this.loaded) {
            if(this.client_id == null) { // Load just the id using name, phone and email.
                this.client_id = jdbct.queryForObject("SELECT client_id FROM Client WHERE name = ? AND phone = ? and email = ?;",
                                                      Long.class,
                                                      this.name, this.phone, this.email);
                this.loaded = true;
                
            } else { // Load using client_id, not ignoring errors, because the client must exist.
                jdbct.query("SELECT * FROM Client WHERE Client.client_id = ?",
                        (rs, rowNum) -> {
                            loadFromRS(rs);
                            return null;
                        },
                        this.client_id);
                this.loaded = true;
            }
        }
    }
    
    @Override
    public void loadFromRS(ResultSet rs) throws SQLException {
        this.client_id = rs.getLong("Client.client_id");
        this.name      = rs.getString("Client.name");
        this.phone     = rs.getString("Client.phone");
        this.email     = rs.getString("Client.email");
        this.loaded    = true;
    }
    
    @Override
    public void loadFromSRS(SqlRowSet rs) throws SQLException {
        this.client_id = rs.getLong(this.getSqlRowSetColumnIndex(rs,   "CLIENT", "CLIENT_ID"));
        this.name      = rs.getString(this.getSqlRowSetColumnIndex(rs, "CLIENT", "NAME"));
        this.phone     = rs.getString(this.getSqlRowSetColumnIndex(rs, "CLIENT", "PHONE"));
        this.email     = rs.getString(this.getSqlRowSetColumnIndex(rs, "CLIENT", "EMAIL"));
        this.loaded    = true;
    }
    
    @Override
    public void save(JdbcTemplate jdbct) {
        if(this.client_id == null) { // Create new.
            GeneratedKeyHolder holder = new GeneratedKeyHolder();
            jdbct.update(new PreparedStatementCreator() {
                @Override
                public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                    PreparedStatement stmt = con.prepareStatement("INSERT INTO Client ( name, phone, email ) VALUES ( ?, ?, ? );", Statement.RETURN_GENERATED_KEYS);

                    int i = 1;
                    stmt.setString(i++, name);
                    stmt.setString(i++, phone);
                    stmt.setString(i++, email);

                    return stmt;
                }
            }, holder);

            this.client_id = holder.getKey().longValue();
        }
    }

}
