import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

//vadi komande i salje serveru
public class SenderThread extends Thread {

    private final KomandniRed red;
    private final OutputStream izlaz;
    private final Map<String, Komanda> naCekanju;


    public SenderThread(KomandniRed red, OutputStream izlaz, Map<String, Komanda> naCekanju) {
          this.red = red;  this.izlaz = izlaz;
        this.naCekanju = naCekanju;
    }

    @Override
    public void run() {
        while (true) {
             Komanda k;

            try {
                k = red.uzmiSledecu();
            }  catch (InterruptedException e) {
                return;
            }

            posaljiIObradi(k);
        }
    }

    private static final int MAX_RETRY = 2;

    private void posaljiIObradi(Komanda k) {

        naCekanju.put(k.getRequestID(), k);

        int pokusaj = 0;

        while (pokusaj <= MAX_RETRY) {

            try {

                String poruka =
                        "CMD;" + k.getRequestID() + ";" + k.getPoruka() + "\n";

                izlaz.write(poruka.getBytes());
                izlaz.flush();

                k.getOdgovor().get(2, TimeUnit.SECONDS);

                return;

            } catch (TimeoutException e) {

                pokusaj++;

                if (pokusaj > MAX_RETRY) {

                    naCekanju.remove(k.getRequestID());

                    k.getOdgovor().completeExceptionally(
                            new RuntimeException(
                                    "Komanda nije uspela nakon svih pokusaja"
                            )
                    );

                    return;
                }

                System.out.println(
                        "Timeout. Ponovno slanje komande "
                                + k.getRequestID()
                                + " | retry: " + pokusaj
                );
//
//
// *
// * - Retry salje ISTI requestId i ceka ISTI future. Ideja: ako odgovor na
//                        *   prvi pokusaj stigne kasno (dok retry vec ceka), prijemnik ce ga naci
//                        *   u mapi i zavrsiti future, pa bi retry trebalo odmah da se zavrsi.
//                        *   Nisam siguran da li je ovo dovoljno ili treba posebna obrada.
// *
// * - Ako stignu dva odgovora (na oba pokusaja), drugi se ne nalazi u mapi
//                        *   i ispisuje se "nepoznat request ID". Da li je to ok ili treba drugacije?
//                        *
//                        * - Na klijentu ne mogu da sprecim da server izvrsi komandu dva puta
//                        *   ako dobije oba slanja. Kako se to obicno resava?
//                        *
//                        * - Ne obradjujem slucaj kada neko otkaze future (cancel()) - mislim da
//                        *   bi tada sender thread mogao da padne. Nisam jos resio.
//                        */

            } catch (IOException e) {

                naCekanju.remove(k.getRequestID());

                k.getOdgovor().completeExceptionally(
                        new RuntimeException("Greska pri slanju komande")
                );

                return;

            } catch (ExecutionException e) {

                return;

            } catch (InterruptedException e) {

                Thread.currentThread().interrupt();

                naCekanju.remove(k.getRequestID());

                k.getOdgovor().completeExceptionally(
                        new RuntimeException("Sender thread je prekinut")
                );

                return;
            }
        }
    }
}