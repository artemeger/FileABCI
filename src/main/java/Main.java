import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Main {

    private static final Logger LOG = Logger.getLogger(Main.class.getName());

    public static void  main (String[] args){

        final AtomicBoolean finish = new AtomicBoolean(false);
        try {
            new FileABCIApp((name, count) -> {
               if (count == 3) LOG.info("ABCI Socket fully connected");
            });
        } catch (InterruptedException e) {
            LOG.log(Level.INFO, "Application crashed on Main Level: " + e.getMessage());
        }
        while(!finish.get()){
            try{
                Thread.sleep(1000L);
            } catch (InterruptedException e){
                LOG.log(Level.INFO, "Socket was interrrupted: " + e.getMessage());
            }
        }
    }
}
