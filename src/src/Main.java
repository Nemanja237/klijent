import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

public class Main {

    public static void main(String[] args) throws IOException {

        String host = "localhost"; //adresa
        int port = 5000;

        Socket socket = new Socket(host, port);

        BufferedReader ulaz = new BufferedReader(
                new InputStreamReader(socket.getInputStream())
        );

        OutputStream izlaz = socket.getOutputStream();

        Map<String, Komanda> naCekanju = new ConcurrentHashMap<>();

        KomandniRed red = new KomandniRed();

        SenderThread sender = new SenderThread(red, izlaz, naCekanju);
        PrijemniThread prijemnik = new PrijemniThread(ulaz, naCekanju);

        sender.start();
        prijemnik.start();

        try {

            Komanda freq = new Komanda("FREQ;10");
            CompletableFuture<String> freqOdgovor = red.dodaj(freq);

            System.out.println(
                    "FREQ odgovor: " + freqOdgovor.get()
            );



            Komanda start = new Komanda("START");
            CompletableFuture<String> startOdgovor = red.dodaj(start);

            System.out.println(
                    "START odgovor: " + startOdgovor.get()
            );


            // slanje podataka
            Thread.sleep(5000);


            // Zaustavi
            Komanda stop = new Komanda("STOP");
            CompletableFuture<String> stopOdgovor = red.dodaj(stop);

            System.out.println(
                    "STOP odgovor: " + stopOdgovor.get()
            );


        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();
            System.out.println("Main thread je prekinut.");

        } catch (ExecutionException e) {

            System.out.println(
                    "Komanda nije uspela: " + e.getCause().getMessage()
            );

        } finally {

            // Zaustavi sender thread
            sender.interrupt();

            socket.close();

            System.out.println("Klijent je ugasen.");
        }
    }
}