package com.open.antbuild;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


/**
 * java -->cmd 命令-->打包
 * @author Administrator
 *
 */
public class AntTest1 {     
	
	
	   // private final static ArrayList<String> flagList = new ArrayList<String>(); //也可以使用集合,不过需要手动添加项  
    private final static String[] channelList = new String[]{"A","B","C","D"};//此处初始化市场标识  
    private final static String projectName="git_antbuild_project";
    private final static String projectBasePath = "E:\\git\\antbuild\\git_antbuild_project"; //项目的根目录
    private final static String outPutPath = "E:\\git\\antbuild\\git_antbuild_project\\outapk"; //apk输出目录
    private final static String placeHolder = "channelID"; //占位符
      
    public static void main(String args[]) {     
    	
    	try {     
	    		System.out.println("---------打包开始A----------\n");  
	    		
	    		setProjectEnvironmet();
	    		
	    		for(String itemChannel : channelList)
	            {  
	            	modifyAndroidManifest(itemChannel);
	            	buildApk();
	            	copyApk(itemChannel);
	            }  
	            
	    		System.out.println("---------打包结束B---------\n");  
            
        } catch (Exception e) {     
            e.printStackTrace();    
            System.out.println("---------打包异常C---------\n");  
        }finally{
        	cleanProjectEnvironment();
    	}
    }  
    
    static void setProjectEnvironmet() throws Exception
    {
    	InputStream in=AntTest0.class.getResourceAsStream("resource/ant.properties");
    	FileUtil.writeFile(projectBasePath+File.separator+"ant.properties", in);
    	
    	in=AntTest0.class.getResourceAsStream("resource/keystore");
    	FileUtil.writeFile(projectBasePath+File.separator+"keystore", in);
    	
    	FileUtil.copyFiles(projectBasePath+File.separator+"AndroidManifest.xml", projectBasePath+File.separator+"AndroidManifest.xml.tmp", true);
    	
    	String buildPath=projectBasePath+File.separator+"build.xml";
    	String projectpropertiesPath=projectBasePath+File.separator+"project.properties";
    	
    	if(!FileUtil.isFileExist(buildPath)&&!FileUtil.isFileExist(projectpropertiesPath))
    	{
//    		Runtime.getRuntime().exec("cmd /c "+"android update project -p "+projectBasePath+" -t android-4");
    		
    		execCommand("cmd /c "+"android update project -p "+projectBasePath+" -t android-4");
    	}
    	else if(!FileUtil.isFileExist(buildPath)&&FileUtil.isFileExist(projectpropertiesPath))
    	{
//    		Runtime.getRuntime().exec("cmd /c "+"android update project -p "+projectBasePath);
    		
    		execCommand("cmd /c "+"android update project -p "+projectBasePath);
    	}
    	
    	modifyBuild();
    }
    
    
    static void cleanProjectEnvironment()
    {
    	FileUtil.deleteFile(projectBasePath+File.separator+"build.xml");
    	FileUtil.deleteFile(projectBasePath+File.separator+"ant.properties");
    	FileUtil.deleteFile(projectBasePath+File.separator+"keystore");
    	FileUtil.deleteFile(projectBasePath+File.separator+"local.properties");
    	FileUtil.copyFiles(projectBasePath+File.separator+"AndroidManifest.xml.tmp", projectBasePath+File.separator+"AndroidManifest.xml", true);
    	FileUtil.deleteFile(projectBasePath+File.separator+"AndroidManifest.xml.tmp");
    }
    
    
    static void execCommand(String cmd)
    {
    	try {
           
    		Process p = Runtime.getRuntime().exec(cmd);  
            InputStream is = p.getInputStream();  
            BufferedReader br = new BufferedReader(new InputStreamReader(is));  
            String line = null;  
            while ((line = br.readLine()) != null)
            {  
                System.out.println(line);  
            }  
            
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    static void modifyAndroidManifest(String itemChannel)
    {
        //先修改AndroidManifest文件:读取临时文件中的@market@修改为市场标识,然后写入manifest.xml中  
        String tempFilePath = projectBasePath + File.separator + "AndroidManifest.xml.tmp";  
        String filePath = projectBasePath + File.separator + "AndroidManifest.xml";  
        FileUtil.deleteFile(filePath);
        write(filePath,replaceChannel(tempFilePath, itemChannel));  
    }
    
    
    static void buildApk() throws Exception
    {
        //执行打包命令  
    	String buildPath=projectBasePath+File.separator+"build.xml";
//    	Runtime.getRuntime().exec("cmd /c "+"ant clean -f "+buildPath);
//    	Runtime.getRuntime().exec("cmd /c "+"ant release -f "+buildPath);
    	
    	execCommand("cmd /c "+"ant clean -f "+buildPath);
    	execCommand("cmd /c "+"ant release -f "+buildPath);
    }
    
    static void copyApk(String itemChannel)
    {
        //打完包后执行重命名加拷贝操作  
        String srcPath=projectBasePath + File.separator +"bin"+File.separator+"%s-release.apk";
        srcPath=String.format(srcPath, projectName);
        
        String desPath=outPutPath + File.separator + "%s_%s.apk";
        desPath=String.format(desPath, projectName,itemChannel);
        
        FileUtil.copyFiles(srcPath, desPath, true);
    }
    
    static void setProguard() throws Exception
    {
    	String projectpropertiesPath=projectBasePath+File.separator+"project.properties";
    	
    	boolean hasSetProguard=false;
    	
        BufferedReader br = null;    
        String line = null;    
        try {    
            // 根据文件路径创建缓冲输入流    
            br = new BufferedReader(new FileReader(projectpropertiesPath));    
                
            // 循环读取文件的每一行, 对需要修改的行进行修改, 放入缓冲对象中    
            while ((line = br.readLine()) != null) 
            {    
                // 此处根据实际需要修改某些行的内容    
                if (line.startsWith("#")) 
                {
                   continue;  
                } 
                else if(line.contains("proguard.config"))
                {
                	hasSetProguard =true;
                	break;
                }
            }    
        } catch (Exception e) {    
            e.printStackTrace();    
        } finally {    
            // 关闭流    
            if (br != null) {    
                try {    
                    br.close();    
                } catch (IOException e) {    
                    br = null;    
                }    
            }    
        }    
        
        String proguardcfgPath=projectBasePath+File.separator+"proguard.cfg";
        if(hasSetProguard&&!FileUtil.isFileExist(proguardcfgPath))
        {
        	 throw new Exception(     
                     "缺少混淆文件  proguard.cfg");  
        }
        
        if(!hasSetProguard&&FileUtil.isFileExist(proguardcfgPath))
        {
        	StringBuffer buf = new StringBuffer();
        	buf.append(System.getProperty("line.separator"));
        	buf.append("proguard.config=proguard.cfg");
        	buf.append(System.getProperty("line.separator"));
        	 
        	byte[]  data=buf.toString().getBytes();
        	FileUtil.appendFile(projectpropertiesPath, data, 0, data.length);
        }
    }
    
    static void cancelProguard()
    {
    	String projectpropertiesPath=projectBasePath+File.separator+"project.properties";
    	
        BufferedReader br = null;    
        String line = null;    
        StringBuffer buf = new StringBuffer();    
            
        try {    
            // 根据文件路径创建缓冲输入流    
            br = new BufferedReader(new FileReader(projectpropertiesPath));    
                
            // 循环读取文件的每一行, 对需要修改的行进行修改, 放入缓冲对象中    
            while ((line = br.readLine()) != null) 
            {    
                // 此处根据实际需要修改某些行的内容    
            	if (line.startsWith("#")) 
            	{
            		 buf.append(line);
            	}
            	else if (line.contains("proguard.config")) 
                {    
                   continue;  
                }    
                else
                {    
                    buf.append(line);    
                }    
                buf.append(System.getProperty("line.separator"));    
            }    
        } catch (Exception e) {    
            e.printStackTrace();    
        } finally {    
            // 关闭流    
            if (br != null) {    
                try {    
                    br.close();    
                } catch (IOException e) {    
                    br = null;    
                }    
            }    
        }    
            
        write(projectpropertiesPath, buf.toString());
    }

    static void modifyBuild() throws Exception
    {
    	String buildPath=projectBasePath+File.separator+"build.xml";
    	String tmpPlaceHolder="MainActivity";
        
    	boolean fristFind=true;
        BufferedReader br = null;    
        String line = null;    
        StringBuffer buf = new StringBuffer();    
            
        try {    
            // 根据文件路径创建缓冲输入流    
            br = new BufferedReader(new FileReader(buildPath));    
            while ((line = br.readLine()) != null) 
            {    
                if (fristFind&&line.startsWith("<project")&&line.contains(tmpPlaceHolder)) 
                {    
                    line = line.replace(tmpPlaceHolder, projectName);  
                    buf.append(line);   
                    fristFind=false;
                }    
                else
                {    
                    buf.append(line);    
                }    
                buf.append(System.getProperty("line.separator"));    
            }    
        } catch (Exception e) {    
            e.printStackTrace();    
        } finally {    
            // 关闭流    
            if (br != null) {    
                try {    
                    br.close();    
                } catch (IOException e) {    
                    br = null;    
                }    
            }    
        }    
            
        write(buildPath, buf.toString());    
    }
    
    /**
     * 读出占位符，并替换为渠道号
     * @param filePath
     * @param channel
     * @return
     */
    public static String replaceChannel(String filePath,String channel) {    
        BufferedReader br = null;    
        String line = null;    
        StringBuffer buf = new StringBuffer();    
            
        try {    
            // 根据文件路径创建缓冲输入流    
            br = new BufferedReader(new FileReader(filePath));    
                
            // 循环读取文件的每一行, 对需要修改的行进行修改, 放入缓冲对象中    
            while ((line = br.readLine()) != null) 
            {    
                // 此处根据实际需要修改某些行的内容    
                if (line.contains(placeHolder)) 
                {    
                    line = line.replace(placeHolder, channel);  
                    buf.append(line);    
                }    
                else
                {    
                    buf.append(line);    
                }    
                buf.append(System.getProperty("line.separator"));    
            }    
        } catch (Exception e) {    
            e.printStackTrace();    
        } finally {    
            // 关闭流    
            if (br != null) {    
                try {    
                    br.close();    
                } catch (IOException e) {    
                    br = null;    
                }    
            }    
        }    
            
        return buf.toString();    
    }    
      
    /**  
     * 将内容回写到文件中  
     *   
     * @param filePath  
     * @param content  
     */    
    public static void write(String filePath, String content) {    
        BufferedWriter bw = null;    
            
        try {    
            // 根据文件路径创建缓冲输出流    
            bw = new BufferedWriter(new FileWriter(filePath));    
            // 将内容写入文件中    
            bw.write(content);    
        } catch (Exception e) {    
            e.printStackTrace();    
        } finally {    
            // 关闭流    
            if (bw != null) {    
                try {    
                    bw.close();    
                } catch (IOException e) {    
                    bw = null;    
                }    
            }    
        }    
    }
    
}    