/*
 * Copyright (c) 2012-2017 ZoxWeb.com LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.zoxweb.server.util;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import org.zoxweb.server.io.IOUtil;
import org.zoxweb.shared.data.RuntimeResultDAO;
import org.zoxweb.shared.data.VMInfoDAO;
import org.zoxweb.shared.util.Const.JavaClassVersion;
import org.zoxweb.shared.data.RuntimeResultDAO.ResultAttribute;

public class RuntimeUtil
{

	private RuntimeUtil()
    {

	}

	/**
	 * Get the output of the command as string. IT will block until the
	 * executing process closes the stream.
	 * @param p 
	 * @param ra 
	 * 
	 * @return the output of the command. If there is no output, "" is returned.
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public static String getRuntimeResponse(Process p, ResultAttribute ra ) 
        throws IOException, InterruptedException
    {

		if (p == null)
		{
            return "";
        }

		InputStream is = null;

		switch(ra)
		{
		case OUTPUT:
			is = p.getInputStream();
			break;
		case ERROR:
			is = p.getErrorStream();
			break;
		case EXIT_CODE:
			p.waitFor();
			return ""+p.exitValue();
		}

		p.waitFor();
		
		return IOUtil.inputStreamToString(is, false);
	}

	/**
	 * This will execute a system command till it finishes.
	 * 
	 * @param command
	 *            to be executed.
	 * @return The execution result the process exit code and the output stream
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	public static RuntimeResultDAO runAndFinish(String command)
        throws InterruptedException, IOException
    {
		return runAndFinish(command, ResultAttribute.OUTPUT);
	}

	/**
	 * This will execute a system command till it finishes.
	 * 
	 * @param command
	 *            to be executed.
	 * @param ra 
	 * @return The execution result the process exit code and the output stream
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	public static RuntimeResultDAO runAndFinish(String command, ResultAttribute ra)
        throws InterruptedException, IOException
    {
		Process p = Runtime.getRuntime().exec(command);
		String ret = getRuntimeResponse(p, ra);
	
		return new RuntimeResultDAO(p.exitValue(), ret);
	}

	/**
	 * This method will create an executable file scripts based on the command and filename on the file system 
	 * and then execute it and command line.
	 * @param command
	 * @param filename
	 * @return the exceution result
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public static RuntimeResultDAO runAndFinish(String command, String filename)
        throws InterruptedException, IOException
    {
		return runAndFinish( command, new File(filename));
	}

	/**
	 * This method will create an executable file scripts based on the command and f on the file system 
	 * and then execute it and command line.
	 * @param command
	 * @param f
	 * @return RuntimeResultDAO
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public static RuntimeResultDAO runAndFinish(String command, File f)
        throws InterruptedException, IOException
    {
		f.createNewFile();
		f.setExecutable(true);
		IOUtil.writeToFile(f, command.getBytes());
		return runAndFinish(f.getCanonicalPath());
	}
	
    public static VMInfoDAO vmSnapshot()
    {
		Runtime rt = Runtime.getRuntime();
		VMInfoDAO ret = new VMInfoDAO();
		
		ret.setCoreCount(rt.availableProcessors());
		ret.setMaxMemory(rt.maxMemory());
		ret.setFreeMemory(rt.freeMemory());
		ret.setUsedMemory(rt.totalMemory() - rt.freeMemory());
		ret.setTotalMemory(rt.totalMemory());
		ret.setTimeStamp( new Date());
		return ret;
	}

	public static JavaClassVersion checkClassVersion(String filename)
        throws IOException
    {
		FileInputStream fis = null;

		try
        {
        	File file = new File(filename);
        	if (!file.exists())
        	{
        		file = new File(filename + ".class");
        	}

        	if (!file.exists())
        	{
        		throw new FileNotFoundException("File:" + filename);
        	}

        	fis = new FileInputStream(file);

        	return checkClassVersion(fis);
        }
        finally
        {
        	IOUtil.close(fis);
        }
    }

	public static JavaClassVersion checkClassVersion(InputStream fis)
        throws IOException
    {
	    DataInputStream in = null;

	    try
        {
	        in = new DataInputStream(fis);
	        int magic = in.readInt();

	        if (magic != 0xcafebabe)
	        {
	          throw new IOException("Invalid class!");
	        }

	        int minor = in.readUnsignedShort();
	        int major = in.readUnsignedShort();
	        return JavaClassVersion.lookup(major, minor);
	    }
	    finally
        {
	    	IOUtil.close(fis);
	    	IOUtil.close(in);
	    }
	}

	public static void main(String[] args)
    {
        for (int i = 0; i < args.length; i++)
        {
            try
            {
                System.out.println(args[i] + ":" + checkClassVersion(args[i]));
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
	 }

}