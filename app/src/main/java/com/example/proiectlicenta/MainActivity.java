package com.example.proiectlicenta;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.renderscript.ScriptGroup;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends Activity {

    Button listDevices , paintBtn , firework , clear , animation;
    Button save1,save2,save3;
    Button load1,load2,load3;
    ListView listView;
    TextView status;
    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice[] btArray;

    sendClass sendClass;


    static final int STATE_LISTENING = 1;
    static final int STATE_CONNECTING = 2;
    static final int STATE_CONNECTED = 3;
    static final int STATE_CONNECTION_FAILED = 4;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Basic UUID for hc-05
    int REQUEST_ENABLE_BLUETOOTH = 1;
    int counter = 0;
    public static byte sendImageBuffer[];
    private static final String[] BTPermissions = {Manifest.permission.BLUETOOTH_CONNECT};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //function that initialises needed parameters
        init();

        //this part of the code makes sure that the bluetooth is on for the phone
        if (false == bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                ActivityCompat.requestPermissions(MainActivity.this,BTPermissions,1);
            }
            startActivityForResult(enableIntent, REQUEST_ENABLE_BLUETOOTH);
        }

        //function that implements all listeners needed
        implementListeners();

        //allocate memory for the sendImageBuffer
        sendImageBuffer = new byte[1024];

        //Fill the array with the needed information for the bluetooth connection
        Arrays.fill(sendImageBuffer , (byte) 0);
        for(int j =0 ; j<16 ; j++)
        {
            sendImageBuffer[(j*64)] = 'M';
            sendImageBuffer[(j*64)+1] = 'O';
            sendImageBuffer[(j*64)+2] = 'D';
            sendImageBuffer[(j*64)+3] = 'E';
            sendImageBuffer[(j*64)+4] = '4';
            sendImageBuffer[(j*64)+5] = (byte) ('0' + j);
            for(int i = 54 ; i< 64;i++)
            {
                sendImageBuffer[(j*64) + i] = ';';
            }
        }
        sendImageBuffer[1021] = 'F';
        sendImageBuffer[1022] = 'I';
        sendImageBuffer[1023] = 'N';

    }

    //this function handles all listeners needed
    private void implementListeners() {
        listDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                //This part makes sure we are permitted to connect to other bluetooth devices
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    ActivityCompat.requestPermissions(MainActivity.this,BTPermissions,1);
                }

                //Get all bonded devices
                Set<BluetoothDevice> bt = bluetoothAdapter.getBondedDevices();
                String[] strings = new String[bt.size()];
                btArray = new  BluetoothDevice[bt.size()];
                int index = 0;

                //take all those devices and add them in the 2 arrays
                if (0 < bt.size()) {
                    for (BluetoothDevice device : bt) {
                        btArray[index] = device;
                        strings[index] = device.getName();
                        index++;
                    }

                    //list all devices
                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, strings);
                    listView.setAdapter(arrayAdapter);
                }
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //This makes all the devices in the list clickable(to connect to them)
                ClientClass clientClass = new ClientClass(btArray[i]);
                clientClass.start();

                //update status message
                status.setText("Connecting");
            }
        });


        save1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Send the code needed for the arduino to save the current image on slot 1
                String string = "MODE50FIN";
                sendClass.write(string.getBytes());
            }
        });

        save2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Send the code needed for the arduino to save the current image on slot 2
                String string = "MODE51FIN";
                sendClass.write(string.getBytes());
            }
        });

        save3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Send the code needed for the arduino to save the current image on slot 3
                String string = "MODE52FIN";
                sendClass.write(string.getBytes());
            }
        });

        load1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Send the code needed for the arduino to load the image on slot 1 and display it
                String string = "MODE60FIN";
                sendClass.write(string.getBytes());
            }
        });

        load2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Send the code needed for the arduino to load the image on slot 2 and display it
                String string = "MODE61FIN";
                sendClass.write(string.getBytes());
            }
        });

        load3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Send the code needed for the arduino to load the image on slot 3 and display it
                String string = "MODE62FIN";
                sendClass.write(string.getBytes());
            }
        });

        firework.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Send the code needed for the arduino to start the firework mode
                String string = "MODE3FIN";
                sendClass.write(string.getBytes());
            }
        });

        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Send the code needed for the arduino to clear the LED matrix
                String string = "MODE2FIN";
                sendClass.write(string.getBytes());
            }
        });

        animation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Send the code needed for the arduino to start the animation mode
                String string = "MODE7FIN";
                sendClass.write(string.getBytes());
            }
        });

        paintBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                //If we are connected to another device
                if(status.getText() == "Connected")
                {
                    //open the PixelArt activity and wait for result
                    Intent intent = new Intent(getApplicationContext() , PixelArt.class);
                    startActivityForResult(intent, 2);
                }
                else
                {
                    Toast.makeText(getApplicationContext() , "Please connect to the device" , Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    //This function handles the sending of the image in 16 messages of 64 bytes
    //each message represents one line of the matrix
    public void sendImage()
    {
        if(counter < 16)
        {
            //Toast message to know the current line being sent
            Toast.makeText(getApplicationContext() , "Current sent line is " + (counter + 1) , Toast.LENGTH_SHORT).show();
            byte sendingArray[] = Arrays.copyOfRange(sendImageBuffer , counter*64 , ((counter+1)*64));
            counter++;
            sendClass.write(sendingArray);
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run()
                {
                    //wait for 3000 seconds and send another line until we reach 16
                    sendImage();
                }
            }, 3000);
        }

    }

    //This function handles the status message using the output of the bluetooth connection
    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {

            switch (msg.what) {
                case STATE_LISTENING:
                    status.setText("Listening");
                    break;
                case STATE_CONNECTING:
                    status.setText("Connecting");
                    break;
                case STATE_CONNECTED:
                    status.setText("Connected");

                    //if bluetooth is connected set all buttons that require it to visible
                    save1.setVisibility(View.VISIBLE);
                    save2.setVisibility(View.VISIBLE);
                    save3.setVisibility(View.VISIBLE);
                    load1.setVisibility(View.VISIBLE);
                    load2.setVisibility(View.VISIBLE);
                    load3.setVisibility(View.VISIBLE);
                    firework.setVisibility(View.VISIBLE);
                    clear.setVisibility(View.VISIBLE);
                    animation.setVisibility(View.VISIBLE);
                    paintBtn.setVisibility(View.VISIBLE);
                    break;
                case STATE_CONNECTION_FAILED:
                    status.setText("Connection failed");

                    //if bluetooth is not connected set all buttons that require it to invisible
                    save1.setVisibility(View.INVISIBLE);
                    save2.setVisibility(View.INVISIBLE);
                    save3.setVisibility(View.INVISIBLE);
                    load1.setVisibility(View.INVISIBLE);
                    load2.setVisibility(View.INVISIBLE);
                    load3.setVisibility(View.INVISIBLE);
                    firework.setVisibility(View.INVISIBLE);
                    clear.setVisibility(View.INVISIBLE);
                    animation.setVisibility(View.INVISIBLE);
                    paintBtn.setVisibility(View.INVISIBLE);
                    break;

            }

            return true;
        }
    });

    //this function initialises all the needed parameters
    private void init()
    {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        listDevices = (Button) findViewById(R.id.listDevices);
        listView = (ListView) findViewById(R.id.listview);
        status = (TextView) findViewById(R.id.status);
        paintBtn = (Button) findViewById(R.id.Paint);
        firework = (Button) findViewById(R.id.firework);
        clear = (Button) findViewById(R.id.clear);
        animation = (Button) findViewById(R.id.Slideshow);

        save1 = (Button) findViewById(R.id.save1);
        save2 = (Button) findViewById(R.id.save2);
        save3 = (Button) findViewById(R.id.save3);

        load1 = (Button) findViewById(R.id.load1);
        load2 = (Button) findViewById(R.id.load2);
        load3 = (Button) findViewById(R.id.load3);

        //set all buttons that require a bluetooth connection as invisible at the start
        save1.setVisibility(View.INVISIBLE);
        save2.setVisibility(View.INVISIBLE);
        save3.setVisibility(View.INVISIBLE);
        load1.setVisibility(View.INVISIBLE);
        load2.setVisibility(View.INVISIBLE);
        load3.setVisibility(View.INVISIBLE);
        firework.setVisibility(View.INVISIBLE);
        clear.setVisibility(View.INVISIBLE);
        animation.setVisibility(View.INVISIBLE);
        paintBtn.setVisibility(View.INVISIBLE);
    }

    //This class handles the bluetooth connection and sending of messages
    //The phone will be set as a clientClass because the hc-05 module is already a serverClass
    private class ClientClass extends Thread {
        private BluetoothDevice device;
        private BluetoothSocket socket;


        public ClientClass(BluetoothDevice device1) {
            device = device1;

            try {
                //the constructor checks if we have the permission to connect via bluetooth
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    ActivityCompat.requestPermissions(MainActivity.this,BTPermissions,1);
                }
                //and then creates a socket
                socket = device.createRfcommSocketToServiceRecord(myUUID);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public void run() {
            //if we have the permission to connect via bluetooth
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                ActivityCompat.requestPermissions(MainActivity.this,BTPermissions,1);
            }
            try {
                //we connect with the bluetooth module
                socket.connect();
                //show that we managed to connect
                Message message = Message.obtain();
                message.what=STATE_CONNECTED;
                handler.sendMessage(message);

                //we create a new thread to send messages
                sendClass = new sendClass(socket);
                sendClass.start();

            } catch (IOException e) {
                //show that we could not connect
                Message message = Message.obtain();
                message.what=STATE_CONNECTION_FAILED;
                handler.sendMessage(message);
                e.printStackTrace();
            }
        }
    }

    //Thread that handles the sending of messages
    private class sendClass extends Thread
    {
        private final BluetoothSocket bluetoothSocket;
        private final OutputStream outputStream;

        //Constructor initializes the socked and outputStream
        public sendClass(BluetoothSocket socket)
        {
            bluetoothSocket = socket;
            OutputStream tempOut = null;

            try {
                tempOut = bluetoothSocket.getOutputStream();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            outputStream = tempOut;
        }

        //function that uses the outputStream to send an array of bytes to the bluetooth module
        public void write(byte[] bytes)
        {
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    //This function handles the result send by the pixelArt class
    //if it gets a resultCode of 2 we start sending the color saved in the array
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==2)
        {
            counter = 0;
            sendImage();
        }
    }
}