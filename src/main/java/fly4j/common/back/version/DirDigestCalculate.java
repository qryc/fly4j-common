package fly4j.common.back.version;

import fly4j.common.file.FileUtil;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class DirDigestCalculate {
    public static boolean ignoreMacShadowFile = true;
    public static final String DIR_VALUE = "dir";


    public static LinkedHashMap<String, String> getDirDigestMap(String checkBaseDirStr, DirVersionCheckParam checkParam) {

        LinkedHashMap<File, String> digestFileMap = getDirMd5FileMap(checkBaseDirStr, checkParam);

        LinkedHashMap<String, String> md5Map = new LinkedHashMap<>();
        digestFileMap.forEach((file, str) -> {
            var dirKey = FileUtil.getSubPathUnix(file.getAbsolutePath(), checkBaseDirStr);
            md5Map.put(dirKey, str);
        });
        return md5Map;
    }

    public static LinkedHashMap<File, String> getDirMd5FileMap(String checkBaseDirStr, DirVersionCheckParam checkParam) {
        LinkedHashMap<File, String> md5FileMap = new LinkedHashMap<>();
        DirMd5OutputParam outPutParam = new DirMd5OutputParam(md5FileMap, new AtomicLong(0));

        DirDigestCalculate.getDirMd5FileMapInner(new File(checkBaseDirStr), outPutParam, checkParam);
        return md5FileMap;
    }


    private static void getDirMd5FileMapInner(File dirFile, DirMd5OutputParam outputParam, DirVersionCheckParam dirMd5Param) {
        try {
            File[] files = dirFile.listFiles();
            //如果不是空文件夹，把父亲文件夹加入
            if (dirMd5Param.checkDirFlag()) {
                outputParam.md5Map.put(dirFile, DIR_VALUE);
            }
            for (File cfile : files) {
                if (null != dirMd5Param.noNeedCalMd5FileFilter() && dirMd5Param.noNeedCalMd5FileFilter().accept(cfile)) {
                    continue;
                }
                if (cfile.isDirectory()) {
                    //递归
                    getDirMd5FileMapInner(cfile, outputParam, dirMd5Param);
                } else {
                    //生成md5
                    Long count = outputParam.count.incrementAndGet();
                    System.out.println("check file " + count + " :" + cfile.getAbsolutePath());

                    if (ignoreMacShadowFile) {
                        if (!cfile.getAbsolutePath().contains("._")) {
                            outputParam.md5Map.put(cfile, getMd5(cfile, dirMd5Param.digestType()));
                        }
                    } else {
                        outputParam.md5Map.put(cfile, getMd5(cfile, dirMd5Param.digestType()));
                    }

                }

            }
        } catch (Exception e) {
            System.out.println("dirFile:" + dirFile);
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }

    public static String getMd5(File file, DigestType versionType) {
        if (DigestType.LEN.equals(versionType)) {
            return "" + file.length();
        } else {
            return FileUtil.getMD5(file);
        }
    }

    private static record DirMd5OutputParam(LinkedHashMap<File, String> md5Map, AtomicLong count) {
    }


}