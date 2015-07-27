package pond.common;

import java.io.*;
import java.util.Properties;

import static pond.common.S._try_ret;

/**
 * Created by ed on 15-7-15.
 */
public class FILE {
    /**
     * (Util method)
     * Load properties from the file
     */
    public static Properties loadProperties(File conf) {
        return _try_ret(() -> {
            Properties config = new Properties();
            if (conf.exists() && conf.canRead())
                config.load(new FileInputStream(conf));
                //using default settings;
            else
                System.out.println(
                        "Can`t read properties file, using default.");
            return config;
        });
    }

    /**
     * (Util method)
     * Load properties from the file, under the classroot
     */
    public static Properties loadProperties(String fileName) {
        return _try_ret(() -> {
            Properties config = new Properties();
            File conf = new File(S.path.classpathRoot()
                    + File.separator + fileName);
            if (conf.exists() && conf.canRead())
                config.load(new FileInputStream(conf));
                //using default settings;
            else
                System.out.println(
                        "Can`t read properties file, using default.");
            return config;
        });
    }

    public static void inputStreamToFile(InputStream ins, File file) throws IOException {
        OutputStream os = new FileOutputStream(file);
        int bytesRead = 0;
        byte[] buffer = new byte[8192];
        while ((bytesRead = ins.read(buffer, 0, 8192)) != -1) {
            os.write(buffer, 0, bytesRead);
        }
        os.close();
        ins.close();
    }

    public static String fileNameFromPath(String path) {
        return path.substring(path.lastIndexOf("\\") + 1);
    }

    /**
     * Returns file extension name,
     * return null if it has no extension.
     *
     * @param fileName
     * @return
     */
    public static String fileExt(String fileName) {
        String[] filename = splitFileName(fileName);
        return filename[filename.length - 1];
    }

    public static String[] splitFileName(String filename) {
        int idx_dot = filename.lastIndexOf('.');
        if (idx_dot <= 0 || idx_dot == filename.length()) {
            return new String[]{filename, null};
        }
        return new String[]{filename.substring(0, idx_dot), filename.substring(idx_dot + 1)};
    }

    /**
     * abc.txt => [abc, txt] abc.def.txt => [abc.def, txt] abc. =>
     * [abc.,null] .abc => [.abc,null] abc => [abc,null]
     *
     * @param file file
     * @return string array with size of 2, first is the filename, remain the suffix;
     */
    public static String[] splitFileName(File file) {
        return splitFileName(file.getName());
    }

    public static File mkdir(File par, String name) throws IOException {
        final String path = par.getAbsolutePath() + File.separatorChar + name;
        File f = new File(path);
        if (f.mkdirs() && f.createNewFile()) {
            return f;
        }
        return null;
    }

    public static File touch(File par, String name) throws IOException {
        final String path = par.getAbsolutePath() + File.separatorChar + name;
        File f = new File(path);
        if (f.createNewFile()) {
            return f;
        }
        return null;
    }

    /**
     * Delete a dir recursively deleting anything inside it.
     *
     * @param file The dir to delete
     * @return true if the dir was successfully deleted
     */
    public static boolean rm(File file) {
        if (!file.exists() || !file.isDirectory()) {
            return false;
        }

        String[] files = file.list();
        for (String file1 : files) {
            File f = new File(file, file1);
            if (f.isDirectory()) {
                rm(f);
            } else {
                f.delete();
            }
        }
        return file.delete();
    }
}
