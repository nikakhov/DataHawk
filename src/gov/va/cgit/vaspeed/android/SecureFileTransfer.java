/*
Copyright (c) 2013, California State University Monterey Bay (CSUMB).
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

    1. Redistributions of source code must retain the above copyright notice,
       this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above
	   copyright notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. Neither the name of the CPUC, CSU Monterey Bay, nor the names of
       its contributors may be used to endorse or promote products derived from
       this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package gov.va.cgit.vaspeed.android;

/**
 *
 * @author California State University Monterery Bay ITCD
 */

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;
import com.jcraft.jsch.*;



public class SecureFileTransfer {

    String username;
    String host;
    String password;
    String khfile;
    String identityfile;
    String filename;
    String sourcepath;
    String destpath;
    
    SecureFileTransfer(String username, String password, String host, String khfile, String identityfile,
            String filename,String sourcepath,String destpath)
{

        this.username = username;
        this.host = host;
        this.password = password;
        this.khfile = khfile;
        this.identityfile = identityfile;
        this.filename= filename;
        this.sourcepath = sourcepath;
        this.destpath = destpath;
    }
    
    private String cpAndRetrunPath() {
	    File f = new File(Calspeed.getContext().getCacheDir()+"/almeraKey.pem");
	    if (!f.exists()) try {
	
	      InputStream is = Calspeed.getContext().getAssets().open("almeraKey.pem");
	      int size = is.available();
	      byte[] buffer = new byte[size];
	      is.read(buffer);
	      is.close();
	
	
	      FileOutputStream fos = new FileOutputStream(f);
	      fos.write(buffer);
	      fos.close();
	    } catch (Exception e) { throw new RuntimeException(e); }
	
	    return f.getPath();
    }
    public String send() {
    	if (Constants.DEBUG)
    		Log.v("LAWDebug", "Start send function");
        JSch jsch = null;
        Session session = null;
        Channel channel = null;
        ChannelSftp c = null;

       try {
            jsch = new JSch();
            /*
            session = jsch.getSession(username, host, 22);
            
           
           
            session.setPassword("");
 
            session.setConfig("StrictHostKeyChecking", "no"); 


            */
            String a = Environment.getExternalStorageDirectory().getAbsolutePath();
            jsch.addIdentity(cpAndRetrunPath());
            jsch.setConfig("StrictHostKeyChecking", "no");

            //enter your own EC2 instance IP here
            session=jsch.getSession("ubuntu", host, 22);
            session.connect();

            channel = session.openChannel("sftp");
            channel.connect();
            c = (ChannelSftp) channel;

        } catch (Exception e) {
        	System.out.println(e);
            e.printStackTrace();
            if (Constants.DEBUG)
            	Log.v("LAWDebug", "End send, Upload File error -- Unable to connect to server for upload");
            return("Upload File error--Unable to connect to server for upload.\n");
        }

        try {
            System.out.println("Starting File Upload:");
            String fsrc = sourcepath+ filename;
            String fdest = destpath + filename;
            ///My code
            File f1 = new File(fsrc);
            File f2 = new File(fdest);
            fdest=c.pwd() + Constants.dataServerDir + "/" + filename;
            //My code ends
            c.put(fsrc, fdest);

        } catch (Exception e) {
            e.printStackTrace();
            if (Constants.DEBUG)
            	Log.v("LAWDebug", "end send, unable to transfer data");
            return("Upload File error--unable to transfer data.\n");
        }

        c.disconnect();
        session.disconnect();
        if (Constants.DEBUG)
        	Log.v("LAWDebug", "successfully uploaded");
        return("File "+sourcepath+filename + " successfully uploaded.\n");
    }
}
