package com.example.project;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.TimeUnit;
import java.io.PrintWriter;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	EditText serverIP = null;
    EditText serverPort = null;
    Button button = null;
    InputStreamReader reader = null;
    BufferedReader read = null;
    PrintWriter writer = null;
    
    public TextView songName,startTimeField,endTimeField;
   // public TextView songName;
    private MediaPlayer mediaPlayer;
    private double startTime = 0;
    private double finalTime = 0;
    private Handler myHandler = new Handler();;
    private int forwardTime = 5000; 
    private int backwardTime = 5000;
    private SeekBar seekbar;
    private ImageButton playButton,pauseButton;
    public static int oneTimeOnly = 0;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		songName = (TextView)findViewById(R.id.textView4);
		startTimeField =(TextView)findViewById(R.id.textView1);
	    endTimeField =(TextView)findViewById(R.id.textView2);
	    seekbar = (SeekBar)findViewById(R.id.seekBar1);
		playButton = (ImageButton)findViewById(R.id.imageButton1);
	    pauseButton = (ImageButton)findViewById(R.id.imageButton2);
	    songName.setText("song.mp3");
	    mediaPlayer = MediaPlayer.create(this, R.raw.song);
	    seekbar.setClickable(false);
	  
		serverIP = (EditText)findViewById(R.id.editText);
        serverPort = (EditText)findViewById(R.id.editText2);
        button = (Button)findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Thread clientThr = new Thread(new Client());
                clientThr.start();
            }
        });
	}
	
	public void play(View view){
			System.out.println("I am enter to start");
		
			Toast.makeText(getApplicationContext(), "Playing sound", 
		   Toast.LENGTH_SHORT).show();
		   writer.println("start");
		   mediaPlayer.start();
		      

		      
		      finalTime = mediaPlayer.getDuration();
		      startTime = mediaPlayer.getCurrentPosition();

		      if(oneTimeOnly == 0){
		         seekbar.setMax((int) finalTime);
		         oneTimeOnly = 1;
		      } 

		      endTimeField.setText(String.format("%d min, %d sec", 
		         TimeUnit.MILLISECONDS.toMinutes((long) finalTime),
		         TimeUnit.MILLISECONDS.toSeconds((long) finalTime) - 
		         TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
		         toMinutes((long) finalTime)))
		      );
		      startTimeField.setText(String.format("%d min, %d sec", 
		         TimeUnit.MILLISECONDS.toMinutes((long) startTime),
		         TimeUnit.MILLISECONDS.toSeconds((long) startTime) - 
		         TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
		         toMinutes((long) startTime)))
		      );

		      seekbar.setProgress((int)startTime);
		      myHandler.postDelayed(UpdateSongTime,100);
		      pauseButton.setEnabled(true);
		      //playButton.setEnabled(false);
	}
	

	private Runnable UpdateSongTime = new Runnable() {
	      public void run() {
	         startTime = mediaPlayer.getCurrentPosition();
	         startTimeField.setText(String.format("%d min, %d sec", 
	            TimeUnit.MILLISECONDS.toMinutes((long) startTime),
	            TimeUnit.MILLISECONDS.toSeconds((long) startTime) - 
	            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
	            toMinutes((long) startTime)))
	         );
	         seekbar.setProgress((int)startTime);
	         myHandler.postDelayed(this, 100);
	      }
	};

   
	public void pause(View view){
	      Toast.makeText(getApplicationContext(), "Pausing sound", 
	      Toast.LENGTH_SHORT).show();
	      writer.println("pause");
	      mediaPlayer.pause();
	      //pauseButton.setEnabled(false);
	      playButton.setEnabled(true);
	}	
	
	public void forward(View view){
	      int temp = (int)startTime;
	      if((temp+forwardTime)<=finalTime){
	         startTime = startTime + forwardTime;
	         mediaPlayer.seekTo((int) startTime);
	      }
	      else{
	         Toast.makeText(getApplicationContext(), 
	         "Cannot jump forward 5 seconds", 
	         Toast.LENGTH_SHORT).show();
	      }

	   }
	   
	public void rewind(View view){
	      int temp = (int)startTime;
	      if((temp-backwardTime)>0){
	         startTime = startTime - backwardTime;
	         mediaPlayer.seekTo((int) startTime);
	      }
	      else{
	         Toast.makeText(getApplicationContext(), 
	         "Cannot jump backward 5 seconds",
	         Toast.LENGTH_SHORT).show();
	      }

	   }

	public class Client implements Runnable{
        String recvMessage = null;
        String IP = serverIP.getText().toString();
        int port = Integer.parseInt(serverPort.getText().toString());
        Socket clientSocket = null;

        public void run(){
            try {
            	System.out.println(IP);
            	System.out.println(port);
                clientSocket = new Socket(IP, port);
                
            }catch (IOException e) {
                System.out.println("Error creating Socket!");
            }
            try {
                reader = new InputStreamReader(clientSocket.getInputStream());
                read = new BufferedReader(reader);
                writer = new PrintWriter(clientSocket.getOutputStream(), true);
                int i =0;

                while(true){
                	recvMessage = read.readLine(); //work
                	runOnUiThread(new Runnable() {
                		public void run() {
                			System.out.println(recvMessage);
                			Toast.makeText(getApplicationContext(), "Received Message from Server : "
                					+ recvMessage,Toast.LENGTH_LONG).show();
                        
                		}               
                	});
                	System.out.println("OUTSIDE IF");
                	if(recvMessage.equalsIgnoreCase("pause") )
                	{
                		System.out.println("I receive pause msg");
                		//pause(null);
              	        mediaPlayer.pause();
            	        //pauseButton.setEnabled(false);
            	        //playButton.setEnabled(true);
                	}

                	if(recvMessage.equalsIgnoreCase("start") )
                	{
                		System.out.println("I receive start msg");
                		//play(null);
                		mediaPlayer.start();
          		        //pauseButton.setEnabled(true);
        		        //playButton.setEnabled(false);
        		        System.out.println("I start to play");
                	}
                }
            } catch (IOException e) {
                System.out.println("Error reading from Socket!");
            }
            
        }
        
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
