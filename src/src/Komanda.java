import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

//Komanda klasa je jedan zahtev koji saljes serveru.


public class Komanda{

     private static final AtomicLong brojac = new AtomicLong(0);

    private final String requestId;

    private final String poruka;

    private final CompletableFuture<String> odgovor = new CompletableFuture<>();
    //kutija za odgovore

    public Komanda(String poruka){
        this.requestId = String.valueOf(brojac.incrementAndGet());
         this.poruka = poruka;
    }

    public String getRequestID() {
        return requestId;
    }
    public String getPoruka(){
        return poruka;
    }

      public CompletableFuture<String> getOdgovor(){
        return odgovor;
    }

}

