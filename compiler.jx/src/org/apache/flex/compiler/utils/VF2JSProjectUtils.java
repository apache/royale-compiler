package org.apache.flex.compiler.utils;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import com.google.common.io.Files;


public class VF2JSProjectUtils
{

    private static File tempDir;
    
    public static final String createTempProject(String projectFilePath,
            boolean isFlashBuilderProject)
    {
        tempDir = Files.createTempDir();
        
        String fileName = projectFilePath.substring(projectFilePath.lastIndexOf(File.separator) + 1, projectFilePath.length());
        
        String path = projectFilePath.substring(0, projectFilePath.lastIndexOf(File.separator));
        
        createTempProjectDir(new File(path).listFiles(), "");
        
        return tempDir.getAbsolutePath() + File.separator + fileName;
    }

    private static void createTempProjectDir(File[] files, String parentPath)
    {
        for (File file : files) 
        {
            if (file.isDirectory()) 
            {
                String path = parentPath + File.separator + file.getName(); 
                
                new File(tempDir + File.separator + path).mkdirs();

                createTempProjectDir(file.listFiles(), path);
            } 
            else 
            {
                String fileName = file.getName();

                if (fileName.contains(".") && fileName.charAt(0) != '.')
                {
                    String extension = fileName.substring(fileName.lastIndexOf("."), fileName.length());
    
                    if (extension.equals(".mxml") || extension.equals(".as"))
                    {
                        File intermediateFile = file;
                        String tempFileName = fileName.substring(0, fileName.indexOf("."));
                        File targetDir = new File(tempDir + File.separator + parentPath);
                        
                        createTempFileWithVF2JSNamespace(intermediateFile, 
                                tempFileName, false, targetDir, extension);
                    }
                }
            }
        }
    }
    
    private static File createTempFileWithVF2JSNamespace(File intermediateFile,
            String tempFileName, boolean createTempFile, File targetDir,
            String extension)
    {
        File tempFile = null;
        
        try 
        {
            String content = FileUtils.readFileToString(intermediateFile, "UTF-8");

            // mx (MXML)
            content = content.replace(
                    "xmlns:mx=\"library://ns.adobe.com/flex/mx\"", 
                    "xmlns:vf2js_mx=\"http://flex.apache.org/vf2js_mx/ns\"");
            content = content.replace("<mx:", "<vf2js_mx:");
            content = content.replace("</mx:", "</vf2js_mx:");

            // mx (AS)
            content = content.replace("mx.", "vf2js_mx.");

            // s (MXML)
            content = content.replace(
                    "xmlns:s=\"library://ns.adobe.com/flex/spark\"", 
                    "xmlns:vf2js_s=\"http://flex.apache.org/vf2js_s/ns\"");
            content = content.replace("<s:", "<vf2js_s:");
            content = content.replace("</s:", "</vf2js_s:");

            // s (AS)
            content = content.replace("spark.", "vf2js_s.");

            if (createTempFile)
            {
                tempFile = File.createTempFile(tempFileName, extension,
                        targetDir);
                tempFile.deleteOnExit();
            }
            else
            {
                tempFile = new File(targetDir.getAbsolutePath(),
                        tempFileName + extension);
            }
            FileUtils.writeStringToFile(tempFile, content, "UTF-8");
        } 
        catch (IOException e) 
        {
            throw new RuntimeException("Generating file failed", e);
        }

        return tempFile;
    }
}
