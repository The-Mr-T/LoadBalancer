package tp2.shared;

import java.rmi.RemoteException;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by Rusty on 11/10/2016.
 */
public class ServerRequest implements Callable<ServerRequest>
{
    private ServerInterface server;
    public List<Operation> operations;
    public int lastResult;
    public int result;

    public ServerRequest(ServerInterface serverInterface, List<Operation> operationList)
    {
        server = serverInterface;
        operations = operationList;
        lastResult = -1;
        result = -1;
    }

    public ServerRequest call() throws Exception
    {
        try
        {
            result = server.executeRequests(operations);
        }
        catch (RemoteException ex)
        {
            System.out.println("Caught error: " + ex.getMessage());
            result = -1;
        }
        catch (OperationRefusedException ex)
        {
            System.out.println("Operation refused!");
            result = -1;
        }
        
        return this;
    }
}
