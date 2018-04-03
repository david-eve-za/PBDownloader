package gon.cue;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.BaseEncoding;
import com.google.common.io.Files;
import com.google.common.io.LineProcessor;

/**
 * Hello world!
 */
@SuppressWarnings("restriction")
public class App {

    public static void main(String[] args) {
        
        Operations.getOperations().createDataBase();

        processMagnet();

    }


    @SuppressWarnings("deprecation")
    private static void processMagnet() {
        List<String> lines = null;
        try {
            lines = Files.readLines(new File("/projects/torrent_dump_full.csv"), Charsets.UTF_8, new LineProcessor<List<String>>() {
                @Override
                public boolean processLine(String s) throws IOException {
                    result.add(s.trim());
                    return true;
                }

                List<String> result = Lists.newArrayList();

                @Override
                public List<String> getResult() {
                    return result;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        String b64String = "nothing";

        lines.remove(lines.get(0));

        for (String s : lines) {
            String[] spl = s.split(";");

            Long peso = Long.decode(spl[3]);

            if ((peso >= 10000000) && (peso <= 20000000)) {
                b64String = spl[1];
                break;
            }
        }

        if (b64String.equals("nothing")) {
            System.out.println("Nothing to Download");
            System.exit(0);
        }

        //byte[] decoded = BaseEncoding.base64().decode(b64String);
        byte[] decoded = BaseEncoding.base64().decode("BaNh07QU39nDNYUsDBu3lHIZCw4=");

        System.out.println("Base64 decoded: " + decoded);

        String magnetURI = "magnet:?xt=urn:btih:" + bytesToHex(decoded);
        System.out.println(magnetURI);
        System.exit(0);
        Storage storage = new FileSystemStorage(new File("/projects").toPath());

        Module dhtModule = new DHTModule(new DHTConfig() {
            @Override
            public boolean shouldUseRouterBootstrap() {
                return true;
            }
        });

        BtClient client = Bt.client().magnet(magnetURI).storage(storage).autoLoadModules().module(dhtModule).build();

        client.startAsync(state -> {
                              System.out.println("Connected Peers: " + state.getConnectedPeers() + " Total Pieces: "
                                                 + state.getPiecesTotal() + " Downloaded: " + state.getDownloaded());
                              if (state.getPiecesRemaining() == 0) {
                                  client.stop();
                              }
                          }, 1000).join();
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }


}
