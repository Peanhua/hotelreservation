package varausjarjestelma;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Tekstikayttoliittyma {

    @Autowired
    private ReservationSystem rsystem;
    
    
    public void kaynnista(Scanner lukija) {
        while (true) {
            System.out.println("");
            System.out.println("Komennot: ");
            System.out.println(" x - lopeta");
            System.out.println(" 1 - lisaa huone");
            System.out.println(" 2 - listaa huoneet");
            System.out.println(" 3 - hae huoneita");
            System.out.println(" 4 - lisaa varaus");
            System.out.println(" 5 - listaa varaukset");
            System.out.println(" 6 - tilastoja");

            String komento = lukija.nextLine();
            if (komento.equals("x")) {
                break;
            }

            if (komento.equals("1")) {
                lisaaHuone(lukija);
            } else if (komento.equals("2")) {
                listaaHuoneet();
            } else if (komento.equals("3")) {
                haeHuoneita(lukija);
            } else if (komento.equals("4")) {
                lisaaVaraus(lukija);
            } else if (komento.equals("5")) {
                listaaVaraukset();
            } else if (komento.equals("6")) {
                tilastoja(lukija);
            }
        }
    }

    private void lisaaHuone(Scanner s) {
        System.out.println("Lisätään huone");
        System.out.println("");

        System.out.println("Minkä tyyppinen huone on?");
        String tyyppi = s.nextLine();
        System.out.println("Mikä huoneen numeroksi asetetaan?");
        int numero = Integer.valueOf(s.nextLine());
        System.out.println("Kuinka monta euroa huone maksaa yöltä?");
        int hinta = Integer.valueOf(s.nextLine());
        
        Room r = rsystem.addRoom(tyyppi, numero, hinta);
    }

    private void listaaHuoneet() {
        System.out.println("Listataan huoneet");
        System.out.println("");

        /*
        // esimerkkitulostus -- tässä oletetaan, että huoneita on 4
        // tulostuksessa tulostetaan huoneen tyyppi, huoneen numero sekä hinta
        System.out.println("Excelsior, 604, 119 euroa");
        System.out.println("Excelsior, 605, 119 euroa");
        System.out.println("Superior, 705, 159 euroa");
        System.out.println("Commodore, 128, 229 euroa");
        */
        printRooms(rsystem.getRooms(), null);
    }
    
    private void printRooms(List<Room> rooms, Integer max) {
        for(int i = 0; (max == null || i < max) && i < rooms.size(); i++) {
            printRoom(rooms.get(i));
        }
    }
    
    private void printRoom(Room r) {
        System.out.println(r.getType() + ", " + r.getName() + ", " + r.getPricePerDay() + " euroa");
    }
    
    private void haeHuoneita(Scanner s) {
        System.out.println("Haetaan huoneita");
        System.out.println("");

        LocalDateTime alku;
        LocalDateTime loppu;
        while(true) {
            System.out.println("Milloin varaus alkaisi (yyyy-MM-dd)?");;
            alku = LocalDateTime.parse(s.nextLine() + " " + "16:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            System.out.println("Milloin varaus loppuisi (yyyy-MM-dd)?");
            loppu = LocalDateTime.parse(s.nextLine() + " " + "10:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            if(alku.isBefore(loppu))
                break;
            else
                System.out.println("Alku päivämäärä pitää olla ennen loppupäivämäärää!");
        }
        System.out.println("Minkä tyyppinen huone? (tyhjä = ei rajausta)");
        String tyyppi = s.nextLine();
        System.out.println("Minkä hintainen korkeintaan? (tyhjä = ei rajausta)");
        String maksimihinta = s.nextLine();

        List<Room> rooms = rsystem.getFreeRooms(alku, loppu, tyyppi.length() == 0 ? null : tyyppi, maksimihinta.length() == 0 ? null : Integer.parseInt(maksimihinta));
        if(rooms.size() > 0) {
            // esimerkkitulostus -- tässä oletetaan, että vapaita huoneita löytyy 2
            System.out.println("Vapaat huoneet: ");
            /*
            System.out.println("Excelsior, 604, 119 euroa");
            System.out.println("Excelsior, 605, 119 euroa");
            */
            printRooms(rooms, null);
        } else {
            // vaihtoehtoisesti, mikäli yhtäkään huonetta ei ole vapaana, ohjelma
            // tulostaa
            System.out.println("Ei vapaita huoneita.");
        }
    }

    private void lisaaVaraus(Scanner s) {
        System.out.println("Haetaan huoneita");
        System.out.println("");
        
        LocalDateTime alku;
        LocalDateTime loppu;
        while(true) {
            System.out.println("Milloin varaus alkaisi (yyyy-MM-dd)?");
            alku = LocalDateTime.parse(s.nextLine() + " " + "16:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            System.out.println("Milloin varaus loppuisi (yyyy-MM-dd)?");
            loppu = LocalDateTime.parse(s.nextLine() + " " + "10:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            if(alku.isBefore(loppu))
                break;
            else
                System.out.println("Alku päivämäärä pitää olla ennen loppupäivämäärää!");
        }

        System.out.println("Minkä tyyppinen huone? (tyhjä = ei rajausta)");
        String tyyppi = s.nextLine();
        System.out.println("Minkä hintainen korkeintaan? (tyhjä = ei rajausta)");
        String maksimihinta = s.nextLine();

        String type = tyyppi.length() == 0 ? null : tyyppi;
        Integer maxprice = maksimihinta.length() == 0 ? null : Integer.parseInt(maksimihinta);
        
        List<Room> rooms = rsystem.getFreeRooms(alku, loppu, type, maxprice);
        if(rooms.isEmpty()) {
            // mikäli huoneita ei ole vapaana, ohjelma tulostaa seuraavan viestin
            // ja varauksen lisääminen loppuu
            System.out.println("Ei vapaita huoneita.");
            return;
        }
        // muulloin, ohjelma kertoo vapaiden huoneiden lukumäärän. Tässä 
        // oletetaan että vapaita huoneita on 2.
        System.out.println("Huoneita vapaana: " + rooms.size());
        System.out.println("");

        // tämän jälkeen kysytään varattavien huoneiden lukumäärää
        // luvuksi tulee hyväksyä vain sopiva luku, esimerkissä 3 ei esim
        // kävisi, sillä vapaita huoneita vain 2
        int huoneita = -1;
        while (true) {
            System.out.println("Montako huonetta varataan?");
            String tmp = s.nextLine();
            huoneita = Integer.valueOf(tmp.length() == 0 ? "-1" : tmp);
            if (huoneita >= 1 && huoneita <= rooms.size()) {
                break;
            }

            System.out.println("Epäkelpo huoneiden lukumäärä.");
        }

        // tämän jälkeen kysytään lisävarusteet
        List<String> lisavarusteet = new ArrayList<>();
        while (true) {
            System.out.println("Syötä lisävaruste, tyhjä lopettaa");
            String lisavaruste = s.nextLine();
            if (lisavaruste.isEmpty()) {
                break;
            }

            lisavarusteet.add(lisavaruste);
        }

        // ja lopuksi varaajan tiedot
        System.out.println("Syötä varaajan nimi:");
        String nimi = s.nextLine();
        System.out.println("Syötä varaajan puhelinnumero:");
        String puhelinnumero = s.nextLine();
        System.out.println("Syötä varaajan sähköpostiosoite:");
        String sahkoposti = s.nextLine();

        // kun kaikki tiedot on kerätty, ohjelma lisää varauksen tietokantaan
        // -- varaukseen tulee lisätä kalleimmat vapaat huoneet!
        Reservation r = rsystem.makeReservation(nimi, puhelinnumero, sahkoposti,
                                                  alku, loppu,
                                                  huoneita, type, maxprice,
                                                  lisavarusteet);
        if(r != null) {
            System.out.println("Varaus#" + r.getId() + " luotu:");
            tulostaVaraus(r);
        } else {
            System.out.println("Varauksen teko epäonnistui!");
        }
    }
    
    private void tulostaVaraus(Reservation reservation) {
        Client client = reservation.getClient();
        List<Room> rooms = reservation.getRooms();

        // First line:
        System.out.print(client.getName() + ", ");
        System.out.print(client.getEmail() + ", ");
        System.out.print(reservation.getStartDate() + ", ");
        System.out.print(reservation.getEndDate() + ", ");
        System.out.print(reservation.getDays() + (reservation.getDays() == 1 ? " päivä" : " päivää") + ", ");
        System.out.println(reservation.getOptionCount() + (reservation.getOptionCount() == 1 ? " lisävaruste" : " lisävarustetta") + ", ");
        System.out.print(rooms.size() + (rooms.size() == 1 ? " huone" : " huonetta") + ". ");

        System.out.println("Huoneet:");

        for(Room room : rooms) {
            System.out.print("\t");
            printRoom(room);
        }

        System.out.println("\tYhteensä: " + reservation.getTotalPrice() + " euroa");
    }

    private void listaaVaraukset() {
        System.out.println("Listataan varaukset");

        // alla olevassa esimerkissä oletetaan, että tietokannassa on 
        // kolme varausta
        /*
        System.out.println("Essi Esimerkki, essi@esimerkki.net, 2019-02-14, 2019-02-15, 1 päivä, 2 lisävarustetta, 1 huone. Huoneet:");
        System.out.println("\tCommodore, 128, 229 euroa");
        System.out.println("\tYhteensä: 229 euroa");
        System.out.println("");
        System.out.println("Anssi Asiakas, anssi@asiakas.net, 2019-02-14, 2019-02-15, 1 päivä, 0 lisävarustetta, 1 huone. Huoneet:");
        System.out.println("\tSuperior, 705, 159 euroa");
        System.out.println("\tYhteensä: 159 euroa");
        System.out.println("");
        System.out.println("Anssi Asiakas, anssi@asiakas.net, 2020-03-18, 2020-03-21, 3 päivää, 6 lisävarustetta, 2 huonetta. Huoneet:");
        System.out.println("\tSuperior, 705, 159 euroa");
        System.out.println("\tCommodore, 128, 229 euroa");
        System.out.println("\tYhteensä: 1164 euroa");
        */
        rsystem
            .getReservations()
            .forEach((reservation) -> {
                System.out.println("");
                tulostaVaraus(reservation);
            });
    }

    private void tilastoja(Scanner lukija) {
        System.out.println("Mitä tilastoja tulostetaan?");
        System.out.println("");

        // tilastoja pyydettäessä käyttäjältä kysytään tilasto
        System.out.println(" 1 - Suosituimmat lisävarusteet");
        System.out.println(" 2 - Parhaat asiakkaat");
        System.out.println(" 3 - Varausprosentti huoneittain");
        System.out.println(" 4 - Varausprosentti huonetyypeittäin");

        System.out.println("Syötä komento: ");
        int komento = Integer.valueOf(lukija.nextLine());

        if (komento == 1) {
            suosituimmatLisavarusteet();
        } else if (komento == 2) {
            parhaatAsiakkaat();
        } else if (komento == 3) {
            varausprosenttiHuoneittain(lukija);
        } else if (komento == 4) {
            varausprosenttiHuonetyypeittain(lukija);
        }
    }

    private void suosituimmatLisavarusteet() {
        System.out.println("Tulostetaan suosituimmat lisävarusteet");
        System.out.println("");

        // alla oletetaan, että lisävarusteita on vain muutama
        // mikäli tietokannassa niitä on enemmän, tulostetaan 10 suosituinta
        /*
        System.out.println("Teekannu, 2 varausta");
        System.out.println("Kahvinkeitin, 2 varausta");
        System.out.println("Silitysrauta, 1 varaus");
        */
        rsystem
            .getMostPopularOptions(10)
            .forEach((o) -> System.out.println(o.first.getName() + ", " + o.second + (o.second == 1 ? " varaus" : " varausta")));
    }

    private void parhaatAsiakkaat() {
        System.out.println("Tulostetaan parhaat asiakkaat");
        System.out.println("");

        // alla oletetaan, että asiakkaita on vain 2
        // mikäli tietokannassa niitä on enemmän, tulostetaan asiakkaita korkeintaan 10
        /*
        System.out.println("Anssi Asiakas, anssi@asiakas.net, +358441231234, 1323 euroa");
        System.out.println("Essi Esimerkki, essi@esimerkki.net, +358443214321, 229 euroa");
        */
        rsystem
            .getBestClients(10)
            .forEach((t) -> System.out.println(t.first.getName() + ", " + t.first.getEmail() + ", " + t.first.getPhone() + ", " + t.second + " euroa"));
    }
    
    private DbDate lueVuosiKuukausi(Scanner lukija, String kehote) {
        while(true) {
            System.out.println(kehote);
            try {
                LocalDateTime d = LocalDateTime.parse(lukija.nextLine() + "-01 " + "12:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                return new DbDate(d);
            } catch(Exception e) {
                System.out.println("Virheellinen syöte! Syötä muodossa: YYYY-MM");
            }
        }
    }

    private void varausprosenttiHuoneittain(Scanner lukija) {
        System.out.println("Tulostetaan varausprosentti huoneittain");
        System.out.println("");

        DbDate alku;
        DbDate loppu;
        while(true) {
            alku = lueVuosiKuukausi(lukija, "Mistä lähtien tarkastellaan?");
            loppu = lueVuosiKuukausi(lukija, "Mihin asti tarkastellaan?");
            loppu.decreaseDay(); // Ei lasketa mukaan loppukuukauden ensimmäistä päivää.
            if(alku.getDate().isBefore(loppu.getDate()))
                break;
            else
                System.out.println("Alkupäivämäärä pitää olla ennen loppupäivämäärää!");
        }

        // alla esimerkkitulostus
        System.out.println("Tulostetaan varausprosentti huoneittain");
        /*
        System.out.println("Excelsior, 604, 119 euroa, 0.0%");
        System.out.println("Excelsior, 605, 119 euroa, 0.0%");
        System.out.println("Superior, 705, 159 euroa, 22.8%");
        System.out.println("Commodore, 128, 229 euroa, 62.8%");
        */
        List<Tuple<Room, Double>> utilization = rsystem.getRoomUtilization(alku, loppu);
        utilization.forEach((u) -> {
            Room r = u.first;
            Double util = u.second;
            System.out.println(r.getType() + ", " + r.getName() + ", " + r.getPricePerDay() + " euroa, " + String.format("%.1f", util) + "%");
        });
    }

    private void varausprosenttiHuonetyypeittain(Scanner lukija) {
        System.out.println("Tulostetaan varausprosentti huonetyypeittäin");
        System.out.println("");

        DbDate alku;
        DbDate loppu;
        while(true) {
            alku = lueVuosiKuukausi(lukija, "Mistä lähtien tarkastellaan?");
            loppu = lueVuosiKuukausi(lukija, "Mihin asti tarkastellaan?");
            loppu.decreaseDay(); // Ei lasketa mukaan loppukuukauden ensimmäistä päivää.
            if(alku.getDate().isBefore(loppu.getDate()))
                break;
            else
                System.out.println("Alkupäivämäärä pitää olla ennen loppupäivämäärää!");
        }

        // alla esimerkkitulostus
        System.out.println("Tulostetaan varausprosentti huonetyypeittän");
        /*
        System.out.println("Excelsior, 0.0%");
        System.out.println("Superior, 22.8%");
        System.out.println("Commodore, 62.8%");
        */
        List<Tuple<String, Double>> utilization = rsystem.getRoomTypeUtilization(alku, loppu);
        utilization.forEach((u) -> System.out.println(u.first + ", " + String.format("%.1f", u.second) + "%"));
    }
}
