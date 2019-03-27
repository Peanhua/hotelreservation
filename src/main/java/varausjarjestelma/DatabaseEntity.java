/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package varausjarjestelma;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;

/**
 *
 * @author joyr
 */



public abstract class DatabaseEntity {
    public void load(JdbcTemplate jdbct) {
        throw new RuntimeException("NOT IMPLEMENTED: " + this + "::load()");
    }
    
    public void loadFromRS(ResultSet rs) throws SQLException {
        throw new RuntimeException("NOT IMPLEMENTED: " + this + "::loadFromRS()");
    }

    public void loadFromSRS(SqlRowSet rs) throws SQLException {
        throw new RuntimeException("NOT IMPLEMENTED: " + this + "::loadFromSRS()");
    }
    
    public abstract void save(JdbcTemplate jdbct);
    
    protected int getSqlRowSetColumnIndex(SqlRowSet rs, String table, String column) {
        // SqlRowSet doesn't support table.column -getters.
        SqlRowSetMetaData meta = rs.getMetaData();
        for(int i = 1; i <= meta.getColumnCount(); i++) {
            if(meta.getTableName(i).equals(table)) {
                if(meta.getColumnName(i).equals(column)) {
                    return i;
                }
            }
        }
        throw new RuntimeException(table + "." + column + " NOT FOUND ERROR");
    }
    
    public void dumpSqlRowSetColummns(SqlRowSet rs) {
        System.out.println("Dumping columns for " + rs);
        SqlRowSetMetaData meta = rs.getMetaData();
        for(int i = 1; i <= meta.getColumnCount(); i++)
            System.out.println("" + i + ": " + meta.getTableName(i) + "." + meta.getColumnName(i));
    }
}

