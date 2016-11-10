package tp2.shared;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by Rusty on 11/10/2016.
 */
public class ServerRequest implements Callable<Integer>
{
    private ServerInterface server;
    private List<Operation> operations;

    public ServerRequest(ServerInterface serverInterface, List<Operation> operationList)
    {
        server = serverInterface;
        operations = operationList;
    }

    public Integer call() throws Exception
    {
        return server.executeRequests(operations);
    }
}
