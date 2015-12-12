package com.open.antbuild;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;

public class AntTest {     
	
	
	   // private final static ArrayList<String> flagList = new ArrayList<String>(); //也可以使用集合,不过需要手动添加项  
    private final static String[] channelList = new String[]{"A"};//此处初始化市场标识  
    private final static String projectName="antTest";
    private final static String projectBasePath = "E:\\workspace\\antTest"; //项目的根目录
    private final static String outPutPath = "E:\\workspace\\antTest\\outapk"; //apk输出目录
    private final static String placeHolder = "@market@"; //占位符
      
    public static void main(String args[]) {     
    	
    	try {     
	    		System.out.println("---------打包开始A----------\n");  
	    		
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
        }
    }  
    
    
    private Project project;     
    
    public void init(String _buildFile, String _baseDir) throws Exception {     
        project = new Project();     
    
        project.init();     
    
        DefaultLogger consoleLogger = new DefaultLogger();     
        consoleLogger.setErrorPrintStream(System.err);     
        consoleLogger.setOutputPrintStream(System.out);     
        consoleLogger.setMessageOutputLevel(Project.MSG_INFO);     
        project.addBuildListener(consoleLogger);      
             
        // Set the base directory. If none is given, "." is used.     
        if (_baseDir == null)     
            _baseDir = new String(".");     
    
        project.setBasedir(_baseDir);     
    
        if (_buildFile == null)     
            _buildFile = new String(projectBasePath + File.separator + "build.xml");     
    
        //ProjectHelper.getProjectHelper().parse(project, new File(_buildFile));     
        // 关键代码   
        ProjectHelper.configureProject(project, new File(_buildFile));     
    }     
    
    public void runTarget(String _target) throws Exception {     
        // Test if the project exists     
        if (project == null)     
            throw new Exception(     
                    "No target can be launched because the project has not been initialized. Please call the 'init' method first !");     
        // If no target is specified, run the default one.     
        if (_target == null)     
            _target = project.getDefaultTarget();     
             
        // Run the target     
        project.executeTarget(_target);     
    
    }     
    
    static void modifyAndroidManifest(String itemChannel)
    {
        //先修改AndroidManifest文件:读取临时文件中的@market@修改为市场标识,然后写入manifest.xml中  
        String tempFilePath = projectBasePath + File.separator + "AndroidManifest.xml.temp";  
        String filePath = projectBasePath + File.separator + "AndroidManifest.xml";  
        FileUtil.deleteFile(filePath);
        write(filePath,replaceChannel(tempFilePath, itemChannel));  
    }
    
    
    static void buildApk() throws Exception
    {
        //执行打包命令  
        AntTest mytest = new AntTest();     
        mytest.init(projectBasePath + File.separator + "build.xml",projectBasePath);   
        mytest.runTarget("clean");  
        mytest.runTarget("release"); 
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