import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;


//cita sve sto server salje
public class PrijemniThread extends Thread {

    private final BufferedReader ulaz;
    private final Map<String, Komanda> naCekanju;

    public PrijemniThread(BufferedReader ulaz, Map<String, Komanda> naCekanju) {
        this.ulaz = ulaz;
        this.naCekanju = naCekanju;
    }

    @Override
    public void run() {
        try {
            String linija;

            while ((linija = ulaz.readLine()) != null) {
                obradiLiniju(linija);
            }

        } catch (IOException e) {
            System.out.println("Greska pri prijemu podataka: " + e.getMessage());
        }
    }

    private void obradiLiniju(String linija) {

        if (linija == null || linija.trim().isEmpty()) {
            System.out.println("Primljena je prazna poruka.");
            return;
        }

        String[] delovi = linija.split(";", -1);
        String tip = delovi[0];

        if (tip.equals("RESP")) {

            // RESP mora imati najmanje:
            // RESP;requestId;status
            if (delovi.length < 3) {
                System.out.println("Neispravan RESP format: " + linija);
                return;
            }

            String requestId = delovi[1];
            String status = delovi[2];
            String poruka = delovi.length > 3 ? delovi[3] : "";

            Komanda k = naCekanju.remove(requestId);

            if (k != null) {

                if (status.equals("OK")) {

                    k.getOdgovor().complete(poruka);

                } else {

                    k.getOdgovor().completeExceptionally(
                            new RuntimeException(poruka)
                    );
                }

            } else {
                System.out.println(
                        "Stigao odgovor za nepoznat request ID: " + requestId
                );
            }

        } else if (tip.equals("DATA")) {

            // DATA mora biti:
            // DATA;counter;sendTimestamp;vrednost
            if (delovi.length < 4) {
                System.out.println("Neispravan DATA format: " + linija);
                return;
            }

            try {

                long counter = Long.parseLong(delovi[1]);
                long sendTimestamp = Long.parseLong(delovi[2]);
                int vrednost = Integer.parseInt(delovi[3]);

                obradiPodatak(counter, sendTimestamp, vrednost);

            } catch (NumberFormatException e) {

                System.out.println(
                        "Neispravne brojcane vrednosti u DATA poruci: " + linija
                );
            }

        } else {

            System.out.println("Nepoznat tip poruke: " + linija);
        }
    }

    private void obradiPodatak(
            long counter,
            long sendTimestamp,
            int vrednost
    ) {

        System.out.println(
                "Paket #" + counter +
                        " | vreme: " + sendTimestamp +
                        " | vrednost: " + vrednost
        );
    }
}