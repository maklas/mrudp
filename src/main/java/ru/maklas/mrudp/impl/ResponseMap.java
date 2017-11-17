package ru.maklas.mrudp.impl;

import ru.maklas.mrudp.Response;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ResponseMap {

    private final int deleteDelay;
    private final Object mutex;
    private final HashMap<ResponseIdentifier, AnsweredResponse> map;

    public ResponseMap(int deleteDelay) {
        this.map = new HashMap<ResponseIdentifier, AnsweredResponse>();
        this.deleteDelay = deleteDelay;
        mutex = this;
    }





    public void put(ResponseWriterImpl response){
        AnsweredResponse answeredResponse = new AnsweredResponse(response);
        synchronized (mutex){
            map.put(new ResponseIdentifier(response), answeredResponse);
        }
    }

    public ResponseWriterImpl get(InetAddress address, int port, int seq) {
        ResponseIdentifier id = new ResponseIdentifier(address, port, seq);
        AnsweredResponse r;
        synchronized (mutex){
            r = map.get(id);
        }
        if (r == null){
            return null;
        }
        return r.response;
    }

    public void cleanup(int deletionDelay){
        long currentTime = System.currentTimeMillis();
        synchronized (mutex){
            Iterator<Map.Entry<ResponseIdentifier, AnsweredResponse>> iterator = map.entrySet().iterator();

            while (iterator.hasNext()){
                Map.Entry<ResponseIdentifier, AnsweredResponse> next = iterator.next();
                if (currentTime - next.getValue().creationTime > deletionDelay){
                    iterator.remove();
                }
            }
        }
    }

    public void update(){
        cleanup(deleteDelay);
    }

    /**
     * Удаляет все ответы которые пробыли в памяти дольше чем minTime
     */
    public void deleteOld(int minTime){
        cleanup(minTime);
    }


    private static class AnsweredResponse{

        private final long creationTime;
        private final ResponseWriterImpl response;

        public AnsweredResponse(ResponseWriterImpl response) {
            this.response = response;
            this.creationTime = System.currentTimeMillis();
        }
    }

    private static class ResponseIdentifier {

        private final byte[] address;
        private final int port;
        private final int seq;

        public ResponseIdentifier(InetAddress address, int port, int seq) {
            this.address = address.getAddress();
            this.port = port;
            this.seq = seq;
        }

        public ResponseIdentifier(Response response) {
            this.address = response.getAddress().getAddress();
            this.port = response.getPort();
            this.seq = response.getSequenceNumber();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ResponseIdentifier that = (ResponseIdentifier) o;

            if (port != that.port) return false;
            if (seq != that.seq) return false;
            return Arrays.equals(address, that.address);
        }

        @Override
        public int hashCode() {
            int result = 31 + port;
            for (byte element : address)
                result = 31 * result + element;
            result = 31 * result + seq;
            return result;
        }
    }
}
