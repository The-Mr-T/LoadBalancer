package tp2.shared;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by Rusty on 11/10/2016.
 */
public class Utils
{
    public static String readFile(String filename) throws IllegalArgumentException
    {
        String fileContents = "";

        File file = new File(filename);
        if (!file.exists() || !file.isFile())
            throw new IllegalArgumentException();

        try (FileInputStream stream = new FileInputStream(file))
        {
            byte[] fileBytes = new byte[(int)file.length()];

            if (stream.read(fileBytes, 0, (int)file.length()) != -1)
                throw new IllegalArgumentException();

            fileContents = new String(fileBytes);
        }
        catch (IOException ex)
        {
            System.err.println(ex.getMessage());
            System.exit(-1);
        }

        return fileContents;
    }
}
