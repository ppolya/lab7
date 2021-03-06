package server;


import mainlib.Reader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class DataHolder  {
    public boolean isEstablishedConnection = false;
    private final byte[] b = new byte[65536];
    private SocketAddress clientAdr;
    ByteBuffer buffer = ByteBuffer.wrap(b);
    DatagramChannel channel = null;


   public  Object getReceivedData()  {
       Object data = null;
       try(ByteArrayInputStream in = new ByteArrayInputStream(b);
           ObjectInputStream ois = new ObjectInputStream(in)){
           data = ois.readObject();
       } catch (IOException e) {
           Reader.PrintErr("problems with receiving data");
       } catch (ClassNotFoundException e) {
           Reader.PrintErr(" problems with reading data");
       }
       return data;
   }


    public ByteBuffer getBuffer(){
       return buffer;
    }

    public SocketAddress getClientAdr() {
        return clientAdr;
    }

    public void setClientAdr(SocketAddress clientAdr) {
        this.clientAdr = clientAdr;
    }

}
