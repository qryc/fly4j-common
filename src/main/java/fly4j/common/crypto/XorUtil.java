package fly4j.common.crypto;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 图片工具
 * Created by qryc on 2016/10/31.
 */
public class XorUtil {

    public static void xorFile2File(Path sourceFileUrl, Path targetFileUrl, int pass) throws IOException {
        if (!Files.exists(targetFileUrl)) {
            Files.createFile(targetFileUrl);
        }
        try (FileInputStream in = new FileInputStream(sourceFileUrl.toFile());
             FileOutputStream out = new FileOutputStream(targetFileUrl.toFile())) {
            xorEncrypt(in, out, pass);
        } catch (Exception e) {
            throw e;
        }
    }

    private static void xorEncrypt(InputStream in, OutputStream out, int pass) throws IOException {
        int data = 0;
        while ((data = in.read()) != -1) {
            //将读取到的字节异或上一个数，加密输出
            out.write(data ^ pass);
        }
    }


    public static byte[] xorFile2Byte(Path sourceFileUrl, int pass) {
        try (FileInputStream in = new FileInputStream(sourceFileUrl.toFile()); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            xorEncrypt(in, out, pass);
            return out.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] xorByte2Byte(byte[] bytes, int pass) throws IOException {
        try (InputStream in = new ByteArrayInputStream(bytes); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            xorEncrypt(in, out, pass);
            return out.toByteArray();
        }
    }
}
