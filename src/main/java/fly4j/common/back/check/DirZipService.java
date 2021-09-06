package fly4j.common.back.check;

import fly4j.common.back.model.DirVersionCheckParam;
import fly4j.common.back.model.DirVersionModel;
import fly4j.common.back.model.ZipConfig;
import fly4j.common.back.zip.Zip4jTool;
import fly4j.common.file.FileAndDirFilter;
import fly4j.common.file.FileUtil;
import fly4j.common.lang.FlyResult;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;

/**
 * alter by qryc in 2020/07/04
 * 不再先拷贝，删除不需要备份文件，再压缩，直接压缩，通过提前规划好需要备份和不需要备份文件
 */
public class DirZipService {
    static final Logger log = LoggerFactory.getLogger(DirZipService.class);

    public FlyResult zipDirWithVerify(ZipConfig zipConfig) {
        FlyResult backResult = new FlyResult().success();
        try {
            //生成MD5摘要文件
            Path md5StorePath = Path.of(zipConfig.getDefaultSourceMd5File().toString(),
                    DirVersionGen.getDefaultVersionFileName(zipConfig.sourceDir().getAbsolutePath()));
            DirVersionModel dirVersionModel = DirVersionGen.saveDirVersionModel2File(zipConfig.sourceDir().toString(),
                    zipConfig.noNeedCalMd5FileFilter(), md5StorePath);
            //执行备份 backFile
            Zip4jTool.zipDir(zipConfig.destZipFile(), zipConfig.sourceDir(), zipConfig.password());
            backResult.append("executeBack success srcFile(" + zipConfig.sourceDir()).append(") zipe to (")
                    .append(zipConfig.destZipFile().getAbsolutePath()).append(")")
                    .append(StringUtils.LF);

            //执行Test
            var checkResult = checkZip(zipConfig);
            backResult.merge(checkResult);

        } catch (Exception e) {
            log.error("Zip4jTool.zip  srcFile:" + zipConfig.sourceDir(), e);
            backResult.append("Zip4jTool.zip  srcFile:" + zipConfig.sourceDir()).append(" error ").append(e.getMessage()).append(StringUtils.LF);
        }
        return backResult;
    }


    private FlyResult checkZip(ZipConfig zipConfig) throws Exception {
        File zipFile = zipConfig.destZipFile();
        var backResult = new FlyResult().success();
        var builder = new StringBuilder();
        var unzipDirName = "unzipT4"
                + zipFile.getName().replaceAll("\\.", "_");
        var unzipDestDirPath = Path.of(zipFile.getParent(), unzipDirName);
        Zip4jTool.unZip(zipFile, unzipDestDirPath.toFile(), zipConfig.password());
        builder.append("executeUnzip  (")
                .append(zipFile.getAbsolutePath())
                .append(")  to (")
                .append(unzipDestDirPath.toString())
                .append(")")
                .append(StringUtils.LF);
        var checkPath = Path.of(unzipDestDirPath.toString(), zipConfig.sourceDir().getName());
        var md5Path = Path.of(unzipDestDirPath.toFile().getAbsolutePath(), zipConfig.sourceDir().getName(), ZipConfig.DEFAULT_VERSIONDATA_PATH);
        DirVersionCheckParam checkParam = new DirVersionCheckParam(zipConfig.versionType(), false, zipConfig.noNeedCalMd5FileFilter());
        FlyResult result = DirCompareService.checkDirChange(checkPath.toFile(),
                FileUtil.getDirLastModifyFile(md5Path.toFile(), ".md5"),
                checkParam);
        builder.append("executeCheckVersion:" + checkPath.toFile().getAbsolutePath()).append(StringUtils.LF);
        if (result.isSuccess()) {
            builder.append("*******check ok").append(StringUtils.LF);
        } else {
            builder.append("******check fail!!!!!!!!!!!!").append(StringUtils.LF);
            backResult.fail();
        }
        builder.append(result.getMsg()).append(StringUtils.LF);

        return backResult.append(builder.toString());
    }

}
