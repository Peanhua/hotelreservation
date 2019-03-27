/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package varausjarjestelma;

import java.time.LocalDateTime;
import java.time.*;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author joyr
 */
public class DbDate {
    private LocalDateTime date;
    
    public DbDate(LocalDateTime date) {
        this.date = date.withHour(12);
    }
    
    public DbDate(String datestr) {
        this.date = LocalDateTime.parse(datestr + " " + "12:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
    
    public LocalDateTime getDate() {
        return this.date;
    }
    
    public long getDaysTo(DbDate end_date) {
        Duration d = Duration.between(this.getDate(), end_date.getDate());
        return d.toDays();
    }
    
    public void decreaseDay() {
        this.date = this.date.plusDays(-1);
    }

    @Override
    public String toString() {
        return this.date.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);
    }
}
