package meeting.api;

import java.io.*;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ConnectionManager {

    private OutputStream writer;
    private DataInputStream reader;
    private Socket socket;

    private byte[] bufferPackage; // zbiera paczke tutaj
    private byte[] portion; // pojedyncze porcje od read socket
    private int bytesNeeded; // ile bajtow potrzebujemy jeszcze
    private int bytesReceived; // ile juz otrzymalismy
    private int messageSize; // jak juz wiemy ile ma wiadomosc, inaczej -1
    private int packageSize; // jak juz wiemy ile ma paczka, inaczej -1
    private int bytesFromRead; // ile bajtow pobrac read socket

    private boolean isConnected;

    private String address;
    private int port;

    public ConnectionManager(String address, String port) {
        this.address = address;
        this.port = Integer.parseInt(port);
    }

    private int initSocket() {
        try {
            SocketAddress sockAddr = new InetSocketAddress(address, port);
            socket = new Socket();
            socket.setKeepAlive(true);
            socket.connect(sockAddr, 3000);

            writer = socket.getOutputStream();
            reader = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

            System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Klient rozstawil gniazdo");

            init();
            isConnected = true;
        } catch (IOException e) {
            return -1;
        }

        return 0;
    }

    private void init() {
        bytesNeeded = 200; // ile chcemy za pierwszym zamahem pobrac bajtow
        bufferPackage = new byte[bytesNeeded]; // robie bufor zeby pomiescil ten pierwszy rzut
        messageSize = -1;
        bytesReceived = 0;
        packageSize = -1;
        bytesFromRead = 0;
    }

    public String sendRequestRecResponse(String request) {

        if(!isConnected && initSocket() == -1) {
                isConnected = false;
                return "{\"flag\" : \"DISCONN\"}";
        }

        System.out.println("----------------------------------------------------------------" + socket.isConnected() );

        // przychodzi sam json w stringu, wiec dodajemy do niego dlugosc i to jest paczka
        byte[] header = convertIntToHeader(request.length());

        System.out.println("Do wyslania wiadomosc (bez headera): " + request);
        System.out.println("Do wyslania bajtow (z naglowkiem): " + (request.getBytes().length + 4));

        // wysyla naszego requesta
        byte[] pack = conc(header, request.getBytes());
        sendRequest(pack);

        // odbierze cala pacze i umiesci w bufferPackage
        if(receiveResponse() == -1) {
            isConnected = false;
            return "{\"flag\" : \"DISCONN\"}";
        }

        String response = removeHeader(bufferPackage);

        System.out.println("Odebrana wiadomosc (bez header): " + response);
        System.out.println("Odebrana bajtow (bez header): " + messageSize);

        // mam juz odpowiedz dla wyzszej warstwy wiec szykuje na nastepny odbior
        init();

        return response;
    }

    private byte[] conc(byte[] header, byte[] reqBytes) {
        byte[] pack = new byte[header.length + reqBytes.length];
        System.arraycopy(header, 0, pack, 0, header.length);
        System.arraycopy(reqBytes, 0, pack, header.length, reqBytes.length);

        return pack;
    }

    private String removeHeader(byte[] responsePackage) {

        // wycinam naglowek
        byte[] response = Arrays.copyOfRange(responsePackage, 4, packageSize);

        String responseString;
        responseString = new String(response, StandardCharsets.UTF_8);

        return responseString;
    }

    private int receiveResponse() {

        while(bytesReceived != packageSize) {
            portion = new byte[bytesNeeded];

            try {
                bytesFromRead = reader.read(portion);
                if (bytesFromRead == -1)
                    return -1;

            } catch (IOException e) {
                e.printStackTrace();
            }

            handlePortion();
        }

        return 0;
    }

    private void handlePortion() {
        System.arraycopy(portion, 0, bufferPackage, bytesReceived, bytesFromRead);
        bytesReceived += bytesFromRead;
        bytesNeeded -= bytesFromRead;

        System.out.println("bytesreceived: " + bytesReceived);

        // jesli mamy juz naglowek a jeszcze nie wyliczylismy rozmiaru, to go wyliczmy
        if(bytesReceived >= 4 && messageSize == -1)
        {
            messageSize = convertHeaderToInt(Arrays.copyOfRange(bufferPackage, 0, 4));
            packageSize = messageSize + 4;

            // naglowek juz mam wiec potrzebuje jeszcze tyle ile ma cala paczka minus to co juz odebralem
            bytesNeeded = packageSize - bytesReceived;

            // jak juz wiem ile bajtow ma cala paczka, to alokuje bufor i przerzucam tam dotychczasowe dane
            byte[] tmp = Arrays.copyOfRange(bufferPackage, 0, bytesReceived);
            //System.arraycopy(bufferPackage, 0, tmp, 0, bytesReceived);
            bufferPackage = new byte[packageSize];

            System.out.println("packageSize: " + packageSize);

            //tu sie wywala client (connectionManager)
            System.arraycopy(tmp, 0, bufferPackage, 0, bytesReceived);

        }
    }

    private void sendRequest(byte[] requestPackage) {
        try {
            // chyba java martwi sie o to zeby to zostalo cale wyslane
            writer.write(requestPackage);

            writer.flush(); // czy z tym???
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int convertHeaderToInt(byte[] header) {

        ByteBuffer buffer = ByteBuffer.wrap(header);
        buffer.order(ByteOrder.BIG_ENDIAN);
        return buffer.getInt();
    }

    private byte[] convertIntToHeader(int mSize) {

        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.putInt(mSize);
        return buffer.array();
    }

    public void closeConnection() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
