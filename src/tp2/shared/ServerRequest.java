package tp2.shared;

import java.util.concurrent.Callable;

/**
 * Created by Rusty on 11/10/2016.
 */
public class ServerRequest implements Callable<Integer>
{
    public ServerInterface server;
    public Operation[] operations;

    public ServerRequest(ServerInterface serverInterface, Operation[] operationList)
    {
        server = serverInterface;
        operations = operationList;
    }

    public Integer call() throws Exception
    {
        return server.executeRequests(operations);
    }
}
