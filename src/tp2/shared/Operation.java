package tp2.shared;

import java.util.concurrent.Callable;

/**
 * Created by Rusty on 11/10/2016.
 */
public class Operation
{
    private String operation;
    private int value;

    public Status status = Status.TODO;

    public Operation(String operationLine) throws IllegalArgumentException
    {
        String[] separated = operationLine.split(" ");
        if (separated.length != 2)
            throw new IllegalArgumentException();

        operation = separated[0];
        if (!operation.equals("pell") && !operation.equals("prime"))
            throw new IllegalArgumentException();
        value = Integer.parseInt(separated[1]);
    }

    public int execute() throws IllegalArgumentException
    {
        int result;

        switch (operation)
        {
            case "pell":
                result = Operations.pell(value);
                break;
            case "prime":
                result = Operations.prime(value);
                break;
            default:
                throw new IllegalArgumentException();   // should never happen
        }

        return result % 4000;
    }
}
