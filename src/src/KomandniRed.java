import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;

//Komandni red je red cekanja u koji se ubacuju sve komande pre nego sto krenu ka serveru

public class KomandniRed{
    private final BlockingQueue<Komanda> red = new LinkedBlockingQueue<>();

    public CompletableFuture<String> dodaj(Komanda k) {

        red.offer(k);
        //ubaci komandu na kraj reda
        return k.getOdgovor();
        //odmah vrati future te komande da pozivalac(ko god da je pozvao dodaj())


    }

     public Komanda uzmiSledecu() throws InterruptedException {
        return red.take(); //cekaj sledecu


     }
}


