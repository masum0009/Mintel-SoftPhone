package com.mintel.sip;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import android.app.Activity;

import android.preference.PreferenceManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;


import net.sourceforge.peers.Config;
import net.sourceforge.peers.JavaConfig;
import net.sourceforge.peers.Logger;
import net.sourceforge.peers.media.MediaManager;
import net.sourceforge.peers.media.MediaMode;
import net.sourceforge.peers.sip.RFC3261;
import net.sourceforge.peers.sip.Utils;
import net.sourceforge.peers.sip.core.useragent.SipListener;
import net.sourceforge.peers.sip.core.useragent.UserAgent;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderFieldName;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaderFieldValue;
import net.sourceforge.peers.sip.syntaxencoding.SipHeaders;
import net.sourceforge.peers.sip.syntaxencoding.SipUriSyntaxException;
import net.sourceforge.peers.sip.transactionuser.Dialog;
import net.sourceforge.peers.sip.transactionuser.DialogManager;
import net.sourceforge.peers.sip.transport.SipMessage;
import net.sourceforge.peers.sip.transport.SipRequest;
import net.sourceforge.peers.sip.transport.SipResponse;

public class PeerManager  implements SipListener{

	public PeerManager(Context mContext,PeerListner peer){
		
    	 this.mContext = mContext;
    	 
    	this.peer = peer;
    
    	
    	 
    	
    	
    	 
		
	}
	
	public void register(){
		if(onCall == true ) return;
		 String peerHome = 	new String(mContext.getCacheDir().getAbsolutePath());
         Logger logger;
       //  UserAgent userAgent;
    	 logger = new Logger(peerHome);
    	 InetAddress localIp = null;
    	 SharedPreferences sharedPrefs = PreferenceManager
  				.getDefaultSharedPreferences(mContext);
    	 config = new JavaConfig();
    	 config.setUserPart(sharedPrefs.getString("prefUsername", "1001"));
    	 config.setPassword(sharedPrefs.getString("prefPassword", "1234"));
    	 config.setDomain(sharedPrefs.getString("prefServerIp", "192.168.137.1"));
    	 config.setMediaMode(MediaMode.captureAndPlayback);
    	 try {
 			localIp = InetAddress.getByName(IpUtils.getIPAddress(true));
 			config.setLocalInetAddress(localIp); 
 			System.out.println("peer:local Ip address is "  + IpUtils.getIPAddress(true));
 			 try {
 	    		 userAgent = new UserAgent(this, config , logger);
 	    		 try{    			 
 	    			 userAgent.getUac().register();	 
 	    		
 	    		  } catch (SipUriSyntaxException e) {
 	    			  System.out.println("Could not register");
 	    		  }
 	    		 
 	         } catch (SocketException e) {
 	        	 
 	        	 System.out.println("Failed to initiate register");
 	         }
 			
 		} catch (UnknownHostException e1) {
 			System.out.println("peer: network exception here");
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
		
	}
	
	public void invite(String phoneNo){
		 if(userAgent.isRegistered()==false) return;
		
		 if(request != null){
			  hangup();
			  return;
		 }	  
		 String invite = String.format("sip:%s@%s", phoneNo,config.getDomain());
		 System.out.println("peer:" + invite );	
		 try{    			 
    		
    		 
    		 String callId = Utils.randomString(15);
    		 request =  userAgent.getUac().invite(invite, callId); 
    	     onCall = true;	 
    	     peer.setRegisterStatus("Calling...");
    		 AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
    		 audioManager.setMode(AudioManager.MODE_IN_CALL);
    		 audioManager.setMode(AudioManager.STREAM_VOICE_CALL);
    		// audioManager.setMode(AudioManager.MODE_NORMAL);
    		 audioManager.setSpeakerphoneOn(false);
    		 
		  } catch (SipUriSyntaxException e) {
			 
		  }
		
	}
	
	public void hangup(){
		if(request != null){
		userAgent.getUac().terminate(request);
		peer.setRegisterStatus("Call ended.");
		request = null;
		onCall = false;
		}
	}
	
	public void acceptCall(){
		 String callId = Utils.getMessageCallId(request);
         DialogManager dialogManager = userAgent.getDialogManager();
         Dialog dialog = dialogManager.getDialog(callId);
         userAgent.getUas().acceptCall(request, dialog);
		 onCall = true;
		 incoming = false;
		 peer.setRegisterStatus("Call established.");
	}
	
	public void rejectCall(){
		userAgent.getUas().rejectCall(request);	 
		incoming = false;
		peer.setRegisterStatus("Call rejected.");
	}
	
    @Override
    public void registering(SipRequest sipRequest) {
    	
    	System.out.println("Peer:Trying to register");
    	peer.setRegisterStatus("Registering...");
    }

    @Override
    public synchronized void registerFailed(SipResponse sipResponse) {
    	
    	System.out.println("Peer:Registeration failed");
    	peer.setRegisterStatus("Registeration failed");
    }

    @Override
    public synchronized void registerSuccessful(SipResponse sipResponse) {
    	 System.out.println("Peer:Successfully registered");
    	 
    	 peer.setRegisterStatus("Successfully registered");
    	 
    	
    }

    @Override
    public synchronized void calleePickup(SipResponse sipResponse) {
    	peer.setRegisterStatus("Call established.");
    }

    @Override
    public synchronized void error(SipResponse sipResponse) {
    	peer.setRegisterStatus("Error happened.");
    	request = null;
    	onCall = false;
    }

    @Override
    public synchronized void incomingCall(final SipRequest sipRequest,
            SipResponse provResponse) {
    	
    	if(onCall == false || incoming == true){
    		//peer.setRegisterStatus("Incoming call...");
    		incoming = true;
    		request = sipRequest;
    		peer.setCallAlert(sipRequest.getRequestUri().getUserinfo().toString());
    	}else
    		userAgent.getUas().rejectCall(sipRequest);	 
    }

    @Override
    public synchronized void remoteHangup(SipRequest sipRequest) {
    	peer.setRegisterStatus("Call ended.");
    	request = null;
    	onCall = false;
    	incoming = false;
    }

    @Override
    public synchronized void ringing(SipResponse sipResponse) {
    	peer.setRegisterStatus("Ringing...");
    	
    	
    }
	 // main frame events
    
    private UserAgent userAgent;
	private SipRequest request;
	public static Context mContext;
	PeerListner peer;
	private Config config;
	private boolean incoming;
	private boolean onCall;

}
