package com.training.util;
/**
 * @author Eric.Yan
 * @version A 2008/07/31
 */

import wt.fc.ObjectReference;
import wt.fc.Persistable;
import wt.fc.QueryResult;
import wt.fc.ReferenceFactory;
import wt.folder.Folder;
import wt.folder.FolderHelper;
import wt.folder.SubFolder;
import wt.inf.container.WTContainer;
import wt.inf.container.WTContainerRef;
import wt.util.WTException;

import java.util.ArrayList;
import java.util.List;

public class FolderUtil {

    /**
     * 得到文件夹结构
     *
     * @param folderPath
     * @param wtcontainer
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static String getFolderOnly(String folderPath, WTContainer wtcontainer) throws Exception {
        if (folderPath == null || folderPath.equals("")) {
            return "";
        }
        Folder subfolder = null;
        String folderRef = "";

        if (!folderPath.startsWith("/")) {
            folderPath = "/" + folderPath;
        }

        if (!folderPath.equalsIgnoreCase("/Default") && !folderPath.startsWith("/Default")) {
            folderPath = "/Default" + folderPath;
        }

        String nextfolder[] = folderPath.split("/");
        ArrayList list = new ArrayList();
        for (int p = 0; p < nextfolder.length; p++) {
            if (nextfolder[p] != null && !nextfolder[p].trim().equals("") && !nextfolder[p].trim().equals("Default")) {
                list.add(nextfolder[p]);
            }
        }

        subfolder = FolderHelper.service.getFolder(folderPath, WTContainerRef.newWTContainerRef(wtcontainer));
        if (subfolder == null) {
            folderPath = "/Default";
            subfolder = FolderHelper.service.getFolder(folderPath, WTContainerRef.newWTContainerRef(wtcontainer));
        } else {
            ReferenceFactory rf = new ReferenceFactory();
            folderRef = rf.getReferenceString(ObjectReference
                    .newObjectReference(((Persistable) subfolder).getPersistInfo().getObjectIdentifier()));
        }
        return folderRef;
    }

    /**
     * 得到文件夹结构,没有的则创建
     *
     * @param folderPath
     * @param wtcontainer
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static String getFolderRef(String folderPath, WTContainer wtcontainer) throws Exception {
        if (folderPath == null || folderPath.equals("")) {
            return "";
        }
        Folder subfolder = null;
        String folderRef = "";

        if (!folderPath.startsWith("/")) {
            folderPath = "/" + folderPath;
        }

        if (!folderPath.equalsIgnoreCase("/Default") && !folderPath.startsWith("/Default")) {
            folderPath = "/Default" + folderPath;
        }

        String nextfolder[] = folderPath.split("/");
        // System.out.println("nextfolder[]==="+nextfolder.length);
        ArrayList list = new ArrayList();
        for (int p = 0; p < nextfolder.length; p++) {
            if (nextfolder[p] != null && !nextfolder[p].trim().equals("") && !nextfolder[p].trim().equals("Default")) {
                list.add(nextfolder[p]);
            }
        }
        // System.out.println("list==="+list);
        createMultiLevelDirectory(list, WTContainerRef.newWTContainerRef(wtcontainer));

        subfolder = FolderHelper.service.getFolder(folderPath, WTContainerRef.newWTContainerRef(wtcontainer));
        if (subfolder == null) {
            folderPath = "/Default";
            subfolder = FolderHelper.service.getFolder(folderPath, WTContainerRef.newWTContainerRef(wtcontainer));
        } else {
            ReferenceFactory rf = new ReferenceFactory();
            folderRef = rf.getReferenceString(ObjectReference
                    .newObjectReference(((Persistable) subfolder).getPersistInfo().getObjectIdentifier()));
        }
        return folderRef;
    }

    /**
     * 得到文件夹结构,没有的则创建
     *
     * @param folderPath
     * @param wtcontainer
     * @return
     * @throws Exception
     */
    public static Folder getFolder(String folderPath, WTContainer wtcontainer) throws Exception {
        System.out.println(folderPath);
        if (folderPath == null || folderPath.equals("")) {
            return null;
        }
        Folder subfolder = null;
        String folderRef = "";

        if (!folderPath.startsWith("/")) {
            folderPath = "/" + folderPath;
        }

        if (!folderPath.equalsIgnoreCase("/Default") && !folderPath.startsWith("/Default")) {
            folderPath = "/Default" + folderPath;
        }

        String nextfolder[] = folderPath.split("/");
        ArrayList list = new ArrayList();
        for (int p = 0; p < nextfolder.length; p++) {
            if (nextfolder[p] != null && !nextfolder[p].trim().equals("") && !nextfolder[p].trim().equals("Default")) {
                list.add(nextfolder[p]);
            }
        }
        // System.out.println("list==="+list);
        // createMultiLevelDirectory(list, WTContainerRef.newWTContainerRef(wtcontainer));

        subfolder = FolderHelper.service.getFolder(folderPath, WTContainerRef.newWTContainerRef(wtcontainer));
        if (subfolder == null) {
            folderPath = "/Default";
            subfolder = FolderHelper.service.getFolder(folderPath, WTContainerRef.newWTContainerRef(wtcontainer));
        } else {
            ReferenceFactory rf = new ReferenceFactory();
            folderRef = rf.getReferenceString(ObjectReference
                    .newObjectReference(((Persistable) subfolder).getPersistInfo().getObjectIdentifier()));
        }
        return subfolder;
    }

    /**
     * 创建多层级文件目录
     *
     * @param list
     * @param wtContainerRef
     * @return
     */
    public static Folder createMultiLevelDirectory(List<String> list, WTContainerRef wtContainerRef) {
        Folder subFolder = null;
        String path = ((WTContainer) wtContainerRef.getObject()).getDefaultCabinet().getFolderPath();
        int size = list.size();
        for (int i = 0; i < size; i++) {
            Folder folder = null;
            try {
                folder = FolderHelper.service.getFolder(path, wtContainerRef);
                path = path + "/" + list.get(i);
                QueryResult result = FolderHelper.service.findSubFolders(folder);
                if (!checkFolderExits(result, list.get(i)))
                    subFolder = FolderHelper.service.createSubFolder(path, wtContainerRef);
            } catch (WTException e) {
                e.printStackTrace();
            }
        }

        if (subFolder == null) {
            try {
                Folder folder = FolderHelper.service.getFolder(path, wtContainerRef);
                subFolder = (Folder) folder;
                // System.out.println(">>>>SubFolder'Name is:" +
                // subFolder.getName());
            } catch (WTException e) {
                e.printStackTrace();
            }
        }
        return subFolder;
    }

    public static void createMultiLevelDirectory(String[] array, WTContainerRef wtContainerRef) {

        String path = ((WTContainer) wtContainerRef.getObject()).getDefaultCabinet().getFolderPath();
        if (array != null) {
            int size = array.length;
            Folder folder = null;
            for (int i = 0; i < size; i++) {
                try {
                    folder = FolderHelper.service.getFolder(path, wtContainerRef);
                    path = path + "/" + array[i];
                    QueryResult result = FolderHelper.service.findSubFolders(folder);
                    if (!checkFolderExits(result, array[i])) {
                        FolderHelper.service.createSubFolder(path, wtContainerRef);
                    }
                } catch (WTException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    
    /**   
    * @Function: FolderUtil.java
    * @Description: 检查文件夹是否存在
    * @param: 
    * @return：boolean
    * @throws：
    * @author: Hu Yaxiong
    * @date: 2021年9月1日 下午4:01:11 
    */
    private static boolean checkFolderExits(QueryResult result, String str) {
        if (result == null)
            return false;
        while (result.hasMoreElements()) {
            Object obj = result.nextElement();
            if (obj instanceof SubFolder) {
                SubFolder subFolder = (SubFolder) obj;
                if (subFolder.getName().equals(str))
                    return true;
            } else {
                return false;
            }
        }
        return false;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub

    }

    
    /**   
    * @Function: FolderUtil.java
    * @Description: 根据文件夹路径获取Folder对象
    * @param: 
    * @return：Folder
    * @throws：
    * @author: Hu Yaxiong
    * @date: 2021年9月1日 下午4:02:02 
    */
    public static Folder getFolderByPath(String folderPath, WTContainer wtcontainer) {
        Folder subfolder = null;
        if (folderPath == null || folderPath.equals("")) {
            return null;
        }
        try {
            subfolder = FolderHelper.service.getFolder(folderPath, WTContainerRef.newWTContainerRef(wtcontainer));
        } catch (Exception e) {
            subfolder = null;
        }
        // 若文件夹不存在，则创建该文件夹
        if (subfolder == null) {
            try {
                subfolder = FolderHelper.service.saveFolderPath(folderPath,
                        WTContainerRef.newWTContainerRef(wtcontainer));
            } catch (WTException e) {
                e.printStackTrace();
            }
        }
        return subfolder;
    }
}
