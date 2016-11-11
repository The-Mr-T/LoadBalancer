package tp2.shared;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

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

            stream.read(fileBytes, 0, (int)file.length());
                //throw new IllegalArgumentException();

            fileContents = new String(fileBytes);
        }
        catch (IOException ex)
        {
            System.err.println(ex.getMessage());
            System.exit(-1);
        }

        return fileContents;
    }

    public static int firstIndexWhereStatusNotEquals(List<Operation> opList, Status status)
    {
        return firstIndexWhereStatusNotEquals(opList, status, 0);
    }

    public static int firstIndexWhereStatusNotEquals(List<Operation> opList, Status status, int initIndex)
    {
        for (int i = initIndex; i < opList.size(); i++)
            if (opList.get(i).status != status)
                return i;

        return opList.size();
    }

    public static int firstIndexWhereStatusEquals(List<Operation> opList, Status status)
    {
        return firstIndexWhereStatusEquals(opList, status, 0);
    }

    public static int firstIndexWhereStatusEquals(List<Operation> opList, Status status, int initIndex)
    {
        for (int i = initIndex; i < opList.size(); i++)
            if (opList.get(i).status == status)
                return i;

        return opList.size();
    }
    
    public static boolean allNull(List list) {
        for (Object t : list)
            if (t != null) 
                return false;
        return true;
    }
}
